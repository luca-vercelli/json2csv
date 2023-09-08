#!/bin/bash

VERSION=1.2
FOLDER=./target/

java -jar ${FOLDER}json2csv-$VERSION.jar "$@"
