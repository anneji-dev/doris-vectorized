package org.apache.doris.clickhouse.planner;

import com.google.common.base.Joiner;
import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type;
import org.apache.doris.analysis.*;
import org.apache.doris.catalog.Catalog;
import org.apache.doris.catalog.Column;
import org.apache.doris.catalog.PrimitiveType;
import org.apache.doris.catalog.ScalarType;
import org.apache.doris.clickhouse.catalog.CHMetaTable;
import org.apache.doris.clickhouse.CHCluster;
import org.apache.doris.clickhouse.CHShard;
import org.apache.doris.clickhouse.CHTenant;
import org.apache.doris.clickhouse.meta.CHDatabase;
import org.apache.doris.clickhouse.meta.CHTable;
import org.apache.doris.clickhouse.meta.CHTablet;
import org.apache.doris.common.AnalysisException;
import org.apache.doris.common.UserException;
import org.apache.doris.planner.PlanNodeId;
import org.apache.doris.planner.ScanNode;
import org.apache.doris.planner.AggregationNode;
import org.apache.doris.qe.ConnectContext;
import org.apache.doris.system.Backend;
import org.apache.doris.thrift.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class CHScanNode extends ScanNode {
    private static final Logger LOG = LogManager.getLogger(CHScanNode.class);

    private final List<String> columns = new ArrayList<String>();
    private final List<String> filters = new ArrayList<String>();
    private String tblName;
    private ConnectContext context;

    // add scan range locations
    private List<TScanRangeLocations> result = new ArrayList<TScanRangeLocations>();

    // doris and ch funcs map<funcName, funcName>, when cur funcName is in map, will use ch funcName replace
    private static final HashMap<String, String> functionsMap = createFunctionsMap();

    boolean isAggregated = false;
    private AggregateInfo aggInfo;
    private List<String> group_by_column_names = Lists.newArrayList();
    private List<String> aggregate_columns = new ArrayList<String>();
    private Optional<String> reason_why_agg_not_push_down = Optional.empty();

    private static HashMap<String, String> createFunctionsMap()
    {
        HashMap<String, String> functionsMap = new HashMap<String, String>();
        functionsMap.put("date_format", "formatDateTime");
        return functionsMap;
    }

    /**
     * Constructs node to scan given data files of table 'tbl'.
     * use ClickhouseTable instead of Table
     */
    public CHScanNode(PlanNodeId id, TupleDescriptor desc, CHMetaTable tbl) {
        super(id, desc, "SCAN Clickhouse");
        tblName = tbl.getName();
    }

    public void pushDownAggregationNode(AggregateInfo aggInfo, DescriptorTable desTable) {
        this.aggInfo = aggInfo;
        this.planNodeName = "ChAggregationNode";
        TupleDescriptor aggTupleDesc = desTable.createTupleDescriptor("ChAggTuple");
        for (SlotDescriptor slotDescriptor : aggInfo.getIntermediateTupleDesc().getSlots()) {
            List<Expr> sourceExprs = slotDescriptor.getSourceExprs();
            if (sourceExprs.size() == 1 && sourceExprs.get(0) instanceof SlotRef) {
                desTable.copySlotDescriptor(aggTupleDesc, slotDescriptor);
            } else {
                SlotDescriptor aggSlotDesc = desTable.addSlotDescriptor(aggTupleDesc);
                aggSlotDesc.setIsMaterialized(true);
                aggSlotDesc.setType(ScalarType.createType(PrimitiveType.STRING));
                aggSlotDesc.setIsAgg(true);
                aggSlotDesc.setIsNullable(false);
            }
        }
        this.tupleIds = aggTupleDesc.getId().asList();
        this.isAggregated = true;
    }

    public AggregateInfo getAggInfo() { return aggInfo; }

    @Override
    public void init(Analyzer analyzer) throws UserException {
        super.init(analyzer);
        computeStats(analyzer);
        this.context = analyzer.getContext();
    }

    @Override
    protected String debugString() {
        MoreObjects.ToStringHelper helper = MoreObjects.toStringHelper(this);
        return helper.addValue(super.debugString()).toString();
    }

    @Override
    public void finalize(Analyzer analyzer) throws UserException {
        LOG.info("[APUS] ClickHouseScanNode get scan range locations. Tuple: {}", desc);
        getScanRangeLocations();

        // Convert predicates to Clickhouse columns and filters.
        createCHColumns(analyzer);
        createCHFilters(analyzer);
    }

    public boolean supportAggregationPushDown(AggregationNode aggregationNode) {
        if (!context.getSessionVariable().isEnablePushdownAggToCH()) {
            reason_why_agg_not_push_down = Optional.of("Session Variable [enable_pushdown_agg_to_ch] is disabled");
            return false;
        }

        if (this.hasLimit()) {
            // select avg from (select * from t1 limit x) t group by xx
            reason_why_agg_not_push_down = Optional.of("Scan Limit has not been pushed down to Ch Aggregation");
            return false;
        }

        ImmutableSet<String> functions = ImmutableSet.of("sum","avg","count","min","max");
        if (!(aggregationNode.getChildren().size() == 1
                && aggregationNode.getChild(0) instanceof CHScanNode)) {
            reason_why_agg_not_push_down = Optional.of("Now only support one table aggregation push down to ch");
            return false;
        }

        AggregateInfo aggInfo = aggregationNode.getAggInfo();

        // aggregation Expr check
        for (FunctionCallExpr function : aggInfo.getMaterializedAggregateExprs()) {
            reason_why_agg_not_push_down = Optional.of(String.format("aggregate function [%s] do not support", function.getFnName().getFunction()));
            return false;
        }

        for (SlotDescriptor slotDescriptor : aggInfo.getIntermediateTupleDesc().getSlots()) {
            List<Expr> sourceExprs = slotDescriptor.getSourceExprs();
            if (sourceExprs.size() == 1) {
                if (sourceExprs.get(0) instanceof SlotRef) {
                    // it's a group by column
                    SlotRef colRef = (SlotRef) sourceExprs.get(0);
                    group_by_column_names.add(colRef.getColumnName());
                    aggregate_columns.add(colRef.getColumnName());
                } else if (sourceExprs.get(0) instanceof FunctionCallExpr) {
                    // it's a aggregation Function column
                    FunctionCallExpr functionCallExpr = (FunctionCallExpr) sourceExprs.get(0);
                    aggregate_columns.add(functionCallExpr.toSqlImpl());
                } else {
                    reason_why_agg_not_push_down = Optional.of("Now only support base aggregation with actual column, eg: sum(1) is not ok");
                    return false;
                }
            } else {
                reason_why_agg_not_push_down = Optional.of("unexpected error");
                return false;
            }
        }
        return true;
    }

    @Override
    public String getNodeExplainString(String prefix, TExplainLevel detailLevel) {
        StringBuilder output = new StringBuilder();

        if (isAggregated) {
            if (aggInfo.getAggregateExprs() != null && aggInfo.getMaterializedAggregateExprs().size() > 0) {
                output.append(prefix + "output: ").append(
                        getExplainString(aggInfo.getAggregateExprs()) + "\n");
            }
            // group by can be very long. Break it into multiple lines
            output.append(prefix + "group by: ").append(
                    getExplainString(aggInfo.getGroupingExprs()) + "\n");
        }

        output.append(prefix).append("TABLE: ").append(tblName).append("\n");

        if (detailLevel == TExplainLevel.BRIEF) {
            return output.toString();
        }
        if (null != sortColumn) {
            output.append(prefix).append("SORT COLUMN: ").append(sortColumn).append("\n");
        }

        if (ConnectContext.get().getSessionVariable().isEnablePushdownAggToCH()) {
            if (reason_why_agg_not_push_down.isPresent()) {
                output.append(prefix).append("agg push down: ").append("false").append(",")
                        .append(" reason: ").append(reason_why_agg_not_push_down.get()).append("\n");
            } else if (isAggregated) {
                output.append(prefix).append("agg push down: ").append("true").append("\n");
            }
        }

        output.append(prefix).append("Query: ").append(getCHQueryStr()).append("\n");
        return output.toString();
    }

    private String getCHQueryStr() {
        StringBuilder sql = new StringBuilder("SELECT ");
        sql.append(Joiner.on(", ").join(columns));
        sql.append(" FROM ").append(tblName);

        if (!filters.isEmpty()) {
            sql.append(" WHERE (");
            sql.append(Joiner.on(") AND (").join(filters));
            sql.append(")");
        }

        if (!group_by_column_names.isEmpty()) {
            sql.append(" GROUP BY ");
            sql.append(Joiner.on(", ").join(group_by_column_names));
        }

        /**
         * should have limit clause
         * Generate the explain plan tree. The plan will be in the form of:
         * <p/>
         * root
         * |
         * |----child 2
         * |      limit:1
         * |
         * |----child 3
         * |      limit:2
         * |
         * child 1
         * <p/>
         */
        if (limit != -1) {
            sql.append(" LIMIT " + limit);
        }

        return sql.toString();
    }

    private void createCHColumns(Analyzer analyzer) {
        if (isAggregated) {
            for (String str : aggregate_columns)
                columns.add(str);
            return;
        }

        for (SlotDescriptor slot : desc.getSlots()) {
            if (!slot.isMaterialized()) {
                continue;
            }
            Column col = slot.getColumn();
            columns.add("`" + col.getName() + "`");
        }
        // when count(*) happens,analyzer generate SlotDescriptor.col = null
        if (0 == columns.size()) {
            columns.add("*");
        }
    }

    private void useCHFuncNameReplaceCurFunc(Expr expr)
    {
        if (expr instanceof FunctionCallExpr)
        {
            FunctionCallExpr funcExpr = (FunctionCallExpr)expr;
            FunctionName funcName = funcExpr.getFnName();
            if (functionsMap.containsKey(funcName.getFunction().toLowerCase()))
            {
                funcName.setFunction(functionsMap.get(funcName.getFunction().toLowerCase()));
            }
        }

        for (Expr child: expr.getChildren()) {
            useCHFuncNameReplaceCurFunc(child);
        }
    }

    private void castDateTimeToDate(Expr expr)
    {
        /// for example: select tc1 from t1 where tc1='2020-10-31'; tc1 is Date type, fe covert string '2020-10-31' to datetime '2020-10-31 00:00:00',
        /// but clickhouse not support string '2020-10-31 00:00:00' to Date function, so this will covert '2020-10-31 00:00:00' to '2020-10-31' again.
        if (expr instanceof BinaryPredicate) {
            Expr lchild = expr.getChild(0);
            Expr rchild = expr.getChild(1);
            if ((lchild instanceof CastExpr
                    && lchild.getType().isDateType()
                    && lchild.getChildren().size() == 1
                    && lchild.getChild(0) instanceof SlotRef
                    && lchild.getChild(0).getType().isDate())
                    && (rchild instanceof DateLiteral
                    && rchild.getType().isDatetime())) {
                ((DateLiteral) rchild).castToDate();
            }
        }
    }

    // We convert predicates of the form <slotref> op <constant> to CLICKHOUSE filters
    private void createCHFilters(Analyzer analyzer) {
        if (conjuncts.isEmpty()) {
            return;
        }
        List<SlotRef> slotRefs = Lists.newArrayList();
        Expr.collectList(conjuncts, SlotRef.class, slotRefs);
        ExprSubstitutionMap sMap = new ExprSubstitutionMap();
        for (SlotRef slotRef : slotRefs) {
            SlotRef tmpRef = (SlotRef) slotRef.clone();
            tmpRef.setTblName(null);
            sMap.put(slotRef, tmpRef);
        }
        ArrayList<Expr> clickhouseConjuncts = Expr.cloneList(conjuncts, sMap);
        for (Expr p : clickhouseConjuncts) {
            castDateTimeToDate(p);
            useCHFuncNameReplaceCurFunc(p);
            filters.add(p.toMySql());
        }
    }

    @Override
    protected void toThrift(TPlanNode msg) {
        msg.node_type = TPlanNodeType.CH_SCAN_NODE;
        TCHScanNode chScanNode = new TCHScanNode();
        chScanNode.setTupleId(this.tupleIds.get(0).asInt());
        chScanNode.setColumns(columns);
        chScanNode.setFilters(filters);

        List<Expr> groupingExprs = aggInfo.getGroupingExprs();
        if (groupingExprs != null) {
            chScanNode.setGroupByColumnNames(group_by_column_names);
        }
        msg.ch_scan_node = chScanNode;
    }

    /**
     * We query CLICKHOUSE Meta to get request's data location
     * extra result info will pass to backend ScanNode
     */
    @Override
    public List<TScanRangeLocations> getScanRangeLocations(long maxScanRangeLength) {
        return result;
    }

    /**
     * should rewrite it using ch tablet info
     */
    public void getScanRangeLocations() throws UserException {
        CHTenant tenant = Catalog.getCurrentCatalog().getTenantMgr().getTenant(context.getTenantName());
        CHCluster cluster = tenant.getCluster(context.getCHClusterName());
        String curDbName = desc.getRef().getName().getDb();
        CHDatabase database = tenant.getDatabase(curDbName.substring(curDbName.indexOf(":") + 1));
        if (database == null)
            throw new UserException("[APUS] database " + curDbName + " is not exist");
        CHTable chTable = database.getTable(tblName);
        List<CHTablet> chTablets = chTable.getAllTablets();

        LOG.info("[APUS] get query tenant={}, cluster={}, database={}, table={}, tablet size={}", tenant.getTenantName(), cluster.getClusterName(), database.getDatabaseName(), tblName, chTablets.size());

        for (CHTablet tablet : chTablets) {
            String tabletName = tablet.getTabletName();
            String tabletUid = tablet.getTabletUid();

            TScanRangeLocations scanRangeLocations = new TScanRangeLocations();
            TCHScanRange chScanRange = new TCHScanRange();
            chScanRange.setDbName(database.getDatabaseName());
            chScanRange.setDbUid(database.getDatabaseUid());
            chScanRange.setTableName(tabletName);
            chScanRange.setTableUid(tabletUid);

            int shardId = tablet.getShardId();
            ConcurrentHashMap<Integer, CHShard> shards = cluster.getShards();
            CHShard shard = shards.get(shardId);
            List<Long> backendIds = Lists.newArrayList();
            backendIds.addAll(shard.getBackendIds());

            LOG.info("[APUS] shardId={} set chScanRange dbName={}, dbUid={}, tabletName={}, tabletUid={}", shardId, database.getDatabaseName(), database.getDatabaseUid(), tabletName, tabletUid);
            Collections.shuffle(backendIds);
            boolean tabletIsNull = true;
            for (Long backendId : backendIds) {
                Backend backend = Catalog.getCurrentSystemInfo().getBackend(backendId);
                if (backend == null || !backend.isAlive()) {
                    LOG.info("[APUS] backend {} not exists or is not alive for shard {}",
                            backendId, shardId);
                    continue;
                }

                String ip = backend.getHost();
                int port = backend.getBePort();
                TScanRangeLocation scanRangeLocation = new TScanRangeLocation(new TNetworkAddress(ip, port));
                scanRangeLocation.setBackendId(backendId);
                scanRangeLocations.addToLocations(scanRangeLocation);
                tabletIsNull = false;
                LOG.info("[APUS] shardId={} add scanRangeLocation ip={}, port={}, backendId={}", shardId, ip, port, backendId);
            }

            if (tabletIsNull) {
                throw new UserException("[APUS] shardId " + shardId + " have no alive backend");
            }

            TScanRange scanRange = new TScanRange();
            scanRange.setChScanRange(chScanRange);
            scanRangeLocations.setScanRange(scanRange);

            LOG.info("[APUS] shardId={} have size={} scanRangeLocation", shardId, scanRangeLocations.locations.size());

            result.add(scanRangeLocations);
        }

        if (chTablets.size() == 0) {
            desc.setCardinality(0);
        } else {
            desc.setCardinality(cardinality);
        }
        LOG.info("[APUS] table {} have size={} scanRangeLocations", tblName, result.size());
    }

    @Override
    public int getNumInstances() {
        return result.size();
    }

    @Override
    public void computeStats(Analyzer analyzer) {
        super.computeStats(analyzer);
        // even if current node scan has no data,at least on backend will be assigned when the fragment actually execute
        numNodes = numNodes <= 0 ? 1 : numNodes;
        // this is just to avoid mysql scan node's cardinality being -1. So that we can calculate the join cost
        // normally.
        // We assume that the data volume of all mysql tables is very small, so set cardinality directly to 1.
        cardinality = cardinality == -1 ? 1 : cardinality;
    }
}
