#!/bin/bash

VERSION=1.0
FOLDER=./target/

java -jar ${FOLDER}json2csv-$VERSION.jar "$@"
