#!/bin/bash

BASEDIR=$(dirname "$0")
PROJECT_DIR=$(realpath $BASEDIR/../semantic-mdx)

cd $PROJECT_DIR && \
  source ~/.jdk1.8 && \
  mvn clean package -Pprod -Dmaven.test.skip=true -pl semantic-deploy -am