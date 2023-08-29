#!/bin/bash

#java -cp /usr/share/java/commons-csv.jar:/usr/share/java/commons-io.jar:/usr/share/java/javax.json.jar:/usr/share/java/jcommander.jar:/usr/share/java/json2csv.jar it.json2csv.Main "$@"

java -jar /usr/share/java/json2csv.jar "$@"
