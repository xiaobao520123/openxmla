#!/bin/bash

SCRIPT_DIR=$(cd -P -- "$(dirname -- "$0")" && pwd -P)
WORK_DIR=$SCRIPT_DIR/..
WORK_DIR=$(realpath "$WORK_DIR")

cd $WORK_DIR && docker build -t openxmla:latest .