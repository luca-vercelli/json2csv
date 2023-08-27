@echo off

SET VERSION=1.0-SNAPSHOT
SET FOLDER=target\

java -jar %FOLDER%json2csv-%VERSION%.jar %*

