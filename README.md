# json2csv
json2csv utility written in Java

I have tried a lot of online JSON->CSV converters, and most of them produce unreadable output in presence of FUll-Join of list attributes. We try to do better.

Java 8+ is required.

Build with:

    mvn clean package

Run with:

    json2csv -o output.csv input.json

(version number may change of course).

A number of options is supported, you can list them with

    json2csv -h
