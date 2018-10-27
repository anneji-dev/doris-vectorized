#!/usr/bin/env bash

# Copyright (c) 2017, Baidu.com, Inc. All Rights Reserved

# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#   http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.

set -e
ROOT=`dirname "$0"`
ROOT=`cd "$ROOT"; pwd`

# check java version
if [ -z $JAVA_HOME ]; then
    echo "Error: JAVA_HOME is not set, use thirdparty/installed/jdk1.8.0_131"
    export JAVA_HOME=${ROOT}/../../thirdparty/installed/jdk1.8.0_131
fi
JAVA=${JAVA_HOME}/bin/java
JAVA_VER=$(${JAVA} -version 2>&1 | sed 's/.* version "\(.*\)\.\(.*\)\..*"/\1\2/; 1q' | cut -f1 -d " ")
if [ $JAVA_VER -lt 18 ]; then
    echo "Error: java version is too old" $JAVA_VER" need jdk 1.8."
    exit 1
fi

export BROKER_HOME=$ROOT

MVN=mvn
# Check ant
if ! ${MVN} --version; then
    echo "mvn is not found"
    exit 1
fi

# prepare thrift
mkdir -p ${BROKER_HOME}/src/main/resources/thrift
mkdir -p ${BROKER_HOME}/src/main/thrift

cp ${BROKER_HOME}/../../gensrc/thrift/PaloBrokerService.thrift ${BROKER_HOME}/src/main/resources/thrift/

$MVN package -DskipTests

echo "Install broker..."
BROKER_OUTPUT=${BROKER_HOME}/output/apache_hdfs_broker/
rm -rf ${BROKER_OUTPUT}

install -d ${BROKER_OUTPUT}/bin ${BROKER_OUTPUT}/conf \
           ${BROKER_OUTPUT}lib/

cp -r -p ${BROKER_HOME}/bin/*.sh ${BROKER_OUTPUT}/bin/
cp -r -p ${BROKER_HOME}/conf/*.conf ${BROKER_OUTPUT}/conf/
cp -r -p ${BROKER_HOME}/conf/log4j.properties ${BROKER_OUTPUT}/conf/
cp -r -p ${BROKER_HOME}/target/lib/* ${BROKER_OUTPUT}/lib/
cp -r -p ${BROKER_HOME}/target/apache_hdfs_broker.jar ${BROKER_OUTPUT}/lib/

echo "Finished"
