#!/bin/bash

VERSION=1.0-SNAPSHOT
FOLDER=./target/

java -jar ${FOLDER}json2csv-$VERSION.jar "$@"
