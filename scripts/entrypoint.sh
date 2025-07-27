#!/bin/bash

SCRIPT_DIR=$(cd -P -- "$(dirname -- "$0")" && pwd -P)
WORK_DIR=$SCRIPT_DIR/..
WORK_DIR=$(realpath "$WORK_DIR")

service mysql start

if [[ -z "${INSIGHT_HOME}" ]]; then
    export INSIGHT_HOME=$WORK_DIR/build
    echo "The env variable INSIGHT_HOME has not been set, set INSIGHT_HOME=$WORK_DIR/build"
fi

${INSIGHT_HOME}/run.sh