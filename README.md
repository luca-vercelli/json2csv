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
        Usage: json2csv [options] <input JSON files>
    Options:
        -h, --help
            Displays help information and exit
        -v, --version
            Print program version and exit
            Default: false
        -o, --output
            Output file. If none, output will be echoed to stdout
        -a, --append
            Append to output file instead of overwrite
            Default: false
        -r, --record-delimiter
            Record delimiter. Default is Windows-style EOL, '\r\n'
        -f, --field-delimiter
            Field delimiter.
            Default: ,
        -q, --quote
            String delimiter (or quote). Must be a single character.
            Default: "
        -e, --escape
            Escape character (for quote character). Must be a single character.
            Default: "
        -n, --nested-attribute-separator
            String used to separate nested attributes in CSV header
            Default: -
        --max-depth
            Max depth for JSON objects inspection
        --skip-header
            Do not print header
            Default: false
        --number-format
            Number format for all numbers
        -l, --locale
            Locale, eg. en or en_US, to be used for formatting numbers. This is
            alternative to --number-format.
        --unix
            Output suitable for *NIX pipelines. Equivalent to --skip-header -f " "
            -r "\n" -q "" -e ""
        -x, --exclude
            Comma separated list of nodes to exclude. Path expressed in form 
            aaa/bbb/2/ccc 
        --root
            JSON root node. Path expressed in form aaa/bbb/2/ccc
        -k, --filter-columns
            List of columns as they will appear in output file
