@echo off

SET VERSION=1.2
SET FOLDER=target\

java -jar %FOLDER%json2csv-%VERSION%.jar %*

