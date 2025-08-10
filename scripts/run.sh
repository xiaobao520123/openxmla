#!/bin/bash

SCRIPT_DIR=$(cd -P -- "$(dirname -- "$0")" && pwd -P)
WORK_DIR=$SCRIPT_DIR/..
WORK_DIR=$(realpath "$WORK_DIR")

docker run --rm -it --name openxmla -p 6068:6068 -p 5005:5005 openxmla:latest