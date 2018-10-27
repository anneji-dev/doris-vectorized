/**
 * Autogenerated by Thrift Compiler (0.9.3)
 *
 * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
 *  @generated
 */
package com.baidu.palo.thrift;

import org.apache.thrift.scheme.IScheme;
import org.apache.thrift.scheme.SchemeFactory;
import org.apache.thrift.scheme.StandardScheme;

import org.apache.thrift.scheme.TupleScheme;
import org.apache.thrift.protocol.TTupleProtocol;
import org.apache.thrift.protocol.TProtocolException;
import org.apache.thrift.EncodingUtils;
import org.apache.thrift.TException;
import org.apache.thrift.async.AsyncMethodCallback;
import org.apache.thrift.server.AbstractNonblockingServer.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.EnumMap;
import java.util.Set;
import java.util.HashSet;
import java.util.EnumSet;
import java.util.Collections;
import java.util.BitSet;
import java.nio.ByteBuffer;
import java.util.Arrays;
import javax.annotation.Generated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings({"cast", "rawtypes", "serial", "unchecked"})
@Generated(value = "Autogenerated by Thrift Compiler (0.9.3)", date = "2018-10-27")
public class TBrokerOpenWriterResponse implements org.apache.thrift.TBase<TBrokerOpenWriterResponse, TBrokerOpenWriterResponse._Fields>, java.io.Serializable, Cloneable, Comparable<TBrokerOpenWriterResponse> {
  private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("TBrokerOpenWriterResponse");

  private static final org.apache.thrift.protocol.TField OP_STATUS_FIELD_DESC = new org.apache.thrift.protocol.TField("opStatus", org.apache.thrift.protocol.TType.STRUCT, (short)1);
  private static final org.apache.thrift.protocol.TField FD_FIELD_DESC = new org.apache.thrift.protocol.TField("fd", org.apache.thrift.protocol.TType.STRUCT, (short)2);

  private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();
  static {
    schemes.put(StandardScheme.class, new TBrokerOpenWriterResponseStandardSchemeFactory());
    schemes.put(TupleScheme.class, new TBrokerOpenWriterResponseTupleSchemeFactory());
  }

  public TBrokerOperationStatus opStatus; // required
  public TBrokerFD fd; // optional

  /** The set of fields this struct contains, along with convenience methods for finding and manipulating them. */
  public enum _Fields implements org.apache.thrift.TFieldIdEnum {
    OP_STATUS((short)1, "opStatus"),
    FD((short)2, "fd");

    private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();

    static {
      for (_Fields field : EnumSet.allOf(_Fields.class)) {
        byName.put(field.getFieldName(), field);
      }
    }

    /**
     * Find the _Fields constant that matches fieldId, or null if its not found.
     */
    public static _Fields findByThriftId(int fieldId) {
      switch(fieldId) {
        case 1: // OP_STATUS
          return OP_STATUS;
        case 2: // FD
          return FD;
        default:
          return null;
      }
    }

    /**
     * Find the _Fields constant that matches fieldId, throwing an exception
     * if it is not found.
     */
    public static _Fields findByThriftIdOrThrow(int fieldId) {
      _Fields fields = findByThriftId(fieldId);
      if (fields == null) throw new IllegalArgumentException("Field " + fieldId + " doesn't exist!");
      return fields;
    }

    /**
     * Find the _Fields constant that matches name, or null if its not found.
     */
    public static _Fields findByName(String name) {
      return byName.get(name);
    }

    private final short _thriftId;
    private final String _fieldName;

    _Fields(short thriftId, String fieldName) {
      _thriftId = thriftId;
      _fieldName = fieldName;
    }

    public short getThriftFieldId() {
      return _thriftId;
    }

    public String getFieldName() {
      return _fieldName;
    }
  }

  // isset id assignments
  private static final _Fields optionals[] = {_Fields.FD};
  public static final Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;
  static {
    Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
    tmpMap.put(_Fields.OP_STATUS, new org.apache.thrift.meta_data.FieldMetaData("opStatus", org.apache.thrift.TFieldRequirementType.REQUIRED, 
        new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, TBrokerOperationStatus.class)));
    tmpMap.put(_Fields.FD, new org.apache.thrift.meta_data.FieldMetaData("fd", org.apache.thrift.TFieldRequirementType.OPTIONAL, 
        new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, TBrokerFD.class)));
    metaDataMap = Collections.unmodifiableMap(tmpMap);
    org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(TBrokerOpenWriterResponse.class, metaDataMap);
  }

  public TBrokerOpenWriterResponse() {
  }

  public TBrokerOpenWriterResponse(
    TBrokerOperationStatus opStatus)
  {
    this();
    this.opStatus = opStatus;
  }

  /**
   * Performs a deep copy on <i>other</i>.
   */
  public TBrokerOpenWriterResponse(TBrokerOpenWriterResponse other) {
    if (other.isSetOpStatus()) {
      this.opStatus = new TBrokerOperationStatus(other.opStatus);
    }
    if (other.isSetFd()) {
      this.fd = new TBrokerFD(other.fd);
    }
  }

  public TBrokerOpenWriterResponse deepCopy() {
    return new TBrokerOpenWriterResponse(this);
  }

  @Override
  public void clear() {
    this.opStatus = null;
    this.fd = null;
  }

  public TBrokerOperationStatus getOpStatus() {
    return this.opStatus;
  }

  public TBrokerOpenWriterResponse setOpStatus(TBrokerOperationStatus opStatus) {
    this.opStatus = opStatus;
    return this;
  }

  public void unsetOpStatus() {
    this.opStatus = null;
  }

  /** Returns true if field opStatus is set (has been assigned a value) and false otherwise */
  public boolean isSetOpStatus() {
    return this.opStatus != null;
  }

  public void setOpStatusIsSet(boolean value) {
    if (!value) {
      this.opStatus = null;
    }
  }

  public TBrokerFD getFd() {
    return this.fd;
  }

  public TBrokerOpenWriterResponse setFd(TBrokerFD fd) {
    this.fd = fd;
    return this;
  }

  public void unsetFd() {
    this.fd = null;
  }

  /** Returns true if field fd is set (has been assigned a value) and false otherwise */
  public boolean isSetFd() {
    return this.fd != null;
  }

  public void setFdIsSet(boolean value) {
    if (!value) {
      this.fd = null;
    }
  }

  public void setFieldValue(_Fields field, Object value) {
    switch (field) {
    case OP_STATUS:
      if (value == null) {
        unsetOpStatus();
      } else {
        setOpStatus((TBrokerOperationStatus)value);
      }
      break;

    case FD:
      if (value == null) {
        unsetFd();
      } else {
        setFd((TBrokerFD)value);
      }
      break;

    }
  }

  public Object getFieldValue(_Fields field) {
    switch (field) {
    case OP_STATUS:
      return getOpStatus();

    case FD:
      return getFd();

    }
    throw new IllegalStateException();
  }

  /** Returns true if field corresponding to fieldID is set (has been assigned a value) and false otherwise */
  public boolean isSet(_Fields field) {
    if (field == null) {
      throw new IllegalArgumentException();
    }

    switch (field) {
    case OP_STATUS:
      return isSetOpStatus();
    case FD:
      return isSetFd();
    }
    throw new IllegalStateException();
  }

  @Override
  public boolean equals(Object that) {
    if (that == null)
      return false;
    if (that instanceof TBrokerOpenWriterResponse)
      return this.equals((TBrokerOpenWriterResponse)that);
    return false;
  }

  public boolean equals(TBrokerOpenWriterResponse that) {
    if (that == null)
      return false;

    boolean this_present_opStatus = true && this.isSetOpStatus();
    boolean that_present_opStatus = true && that.isSetOpStatus();
    if (this_present_opStatus || that_present_opStatus) {
      if (!(this_present_opStatus && that_present_opStatus))
        return false;
      if (!this.opStatus.equals(that.opStatus))
        return false;
    }

    boolean this_present_fd = true && this.isSetFd();
    boolean that_present_fd = true && that.isSetFd();
    if (this_present_fd || that_present_fd) {
      if (!(this_present_fd && that_present_fd))
        return false;
      if (!this.fd.equals(that.fd))
        return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    List<Object> list = new ArrayList<Object>();

    boolean present_opStatus = true && (isSetOpStatus());
    list.add(present_opStatus);
    if (present_opStatus)
      list.add(opStatus);

    boolean present_fd = true && (isSetFd());
    list.add(present_fd);
    if (present_fd)
      list.add(fd);

    return list.hashCode();
  }

  @Override
  public int compareTo(TBrokerOpenWriterResponse other) {
    if (!getClass().equals(other.getClass())) {
      return getClass().getName().compareTo(other.getClass().getName());
    }

    int lastComparison = 0;

    lastComparison = Boolean.valueOf(isSetOpStatus()).compareTo(other.isSetOpStatus());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetOpStatus()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.opStatus, other.opStatus);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetFd()).compareTo(other.isSetFd());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetFd()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.fd, other.fd);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    return 0;
  }

  public _Fields fieldForId(int fieldId) {
    return _Fields.findByThriftId(fieldId);
  }

  public void read(org.apache.thrift.protocol.TProtocol iprot) throws org.apache.thrift.TException {
    schemes.get(iprot.getScheme()).getScheme().read(iprot, this);
  }

  public void write(org.apache.thrift.protocol.TProtocol oprot) throws org.apache.thrift.TException {
    schemes.get(oprot.getScheme()).getScheme().write(oprot, this);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("TBrokerOpenWriterResponse(");
    boolean first = true;

    sb.append("opStatus:");
    if (this.opStatus == null) {
      sb.append("null");
    } else {
      sb.append(this.opStatus);
    }
    first = false;
    if (isSetFd()) {
      if (!first) sb.append(", ");
      sb.append("fd:");
      if (this.fd == null) {
        sb.append("null");
      } else {
        sb.append(this.fd);
      }
      first = false;
    }
    sb.append(")");
    return sb.toString();
  }

  public void validate() throws org.apache.thrift.TException {
    // check for required fields
    if (opStatus == null) {
      throw new org.apache.thrift.protocol.TProtocolException("Required field 'opStatus' was not present! Struct: " + toString());
    }
    // check for sub-struct validity
    if (opStatus != null) {
      opStatus.validate();
    }
    if (fd != null) {
      fd.validate();
    }
  }

  private void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
    try {
      write(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(out)));
    } catch (org.apache.thrift.TException te) {
      throw new java.io.IOException(te);
    }
  }

  private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException {
    try {
      read(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(in)));
    } catch (org.apache.thrift.TException te) {
      throw new java.io.IOException(te);
    }
  }

  private static class TBrokerOpenWriterResponseStandardSchemeFactory implements SchemeFactory {
    public TBrokerOpenWriterResponseStandardScheme getScheme() {
      return new TBrokerOpenWriterResponseStandardScheme();
    }
  }

  private static class TBrokerOpenWriterResponseStandardScheme extends StandardScheme<TBrokerOpenWriterResponse> {

    public void read(org.apache.thrift.protocol.TProtocol iprot, TBrokerOpenWriterResponse struct) throws org.apache.thrift.TException {
      org.apache.thrift.protocol.TField schemeField;
      iprot.readStructBegin();
      while (true)
      {
        schemeField = iprot.readFieldBegin();
        if (schemeField.type == org.apache.thrift.protocol.TType.STOP) { 
          break;
        }
        switch (schemeField.id) {
          case 1: // OP_STATUS
            if (schemeField.type == org.apache.thrift.protocol.TType.STRUCT) {
              struct.opStatus = new TBrokerOperationStatus();
              struct.opStatus.read(iprot);
              struct.setOpStatusIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 2: // FD
            if (schemeField.type == org.apache.thrift.protocol.TType.STRUCT) {
              struct.fd = new TBrokerFD();
              struct.fd.read(iprot);
              struct.setFdIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          default:
            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
        }
        iprot.readFieldEnd();
      }
      iprot.readStructEnd();

      // check for required fields of primitive type, which can't be checked in the validate method
      struct.validate();
    }

    public void write(org.apache.thrift.protocol.TProtocol oprot, TBrokerOpenWriterResponse struct) throws org.apache.thrift.TException {
      struct.validate();

      oprot.writeStructBegin(STRUCT_DESC);
      if (struct.opStatus != null) {
        oprot.writeFieldBegin(OP_STATUS_FIELD_DESC);
        struct.opStatus.write(oprot);
        oprot.writeFieldEnd();
      }
      if (struct.fd != null) {
        if (struct.isSetFd()) {
          oprot.writeFieldBegin(FD_FIELD_DESC);
          struct.fd.write(oprot);
          oprot.writeFieldEnd();
        }
      }
      oprot.writeFieldStop();
      oprot.writeStructEnd();
    }

  }

  private static class TBrokerOpenWriterResponseTupleSchemeFactory implements SchemeFactory {
    public TBrokerOpenWriterResponseTupleScheme getScheme() {
      return new TBrokerOpenWriterResponseTupleScheme();
    }
  }

  private static class TBrokerOpenWriterResponseTupleScheme extends TupleScheme<TBrokerOpenWriterResponse> {

    @Override
    public void write(org.apache.thrift.protocol.TProtocol prot, TBrokerOpenWriterResponse struct) throws org.apache.thrift.TException {
      TTupleProtocol oprot = (TTupleProtocol) prot;
      struct.opStatus.write(oprot);
      BitSet optionals = new BitSet();
      if (struct.isSetFd()) {
        optionals.set(0);
      }
      oprot.writeBitSet(optionals, 1);
      if (struct.isSetFd()) {
        struct.fd.write(oprot);
      }
    }

    @Override
    public void read(org.apache.thrift.protocol.TProtocol prot, TBrokerOpenWriterResponse struct) throws org.apache.thrift.TException {
      TTupleProtocol iprot = (TTupleProtocol) prot;
      struct.opStatus = new TBrokerOperationStatus();
      struct.opStatus.read(iprot);
      struct.setOpStatusIsSet(true);
      BitSet incoming = iprot.readBitSet(1);
      if (incoming.get(0)) {
        struct.fd = new TBrokerFD();
        struct.fd.read(iprot);
        struct.setFdIsSet(true);
      }
    }
  }

}

