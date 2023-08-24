# json2csv
json2csv utility written in Java

I have tried a lot of online JSON->CSV converters, and most of them produce unreadable output in presence of FULL JOIN of list attributes. We try to do better.

Java 11+ is required.

Build with:

    mvn clean package

Run with:

    json2csv -o output.csv input.json

A number of options is supported, you can list them with

    json2csv -h
        Usage: <main class> [options] <input JSON files>
    Options:
        -a, --append
        Append to output file instead of overwrite
        Default: false
        -e, --escape
        Escape character (for quote character). Must be a single character.
        Default: "
        -f, --field-delimiter
        Field delimiter.
        Default: ,
        -h, --help
        Displays help information
        --max, --max-depth
        Max depth for objects inspection
        -n, --nested-attribute-separator
        String used to separate nested attributes in CSV header
        Default: -
        -o, --output
        Output file. If none, output will be echoed to stdout
        -q, --quote
        String delimiter (or quote). Must be a single character.
        Default: "
        -r, --record-delimiter
        Field delimiter. Default '\r\n'
