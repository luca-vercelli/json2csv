package it.json2csv;

import java.util.ArrayList;
import java.util.List;

import com.beust.jcommander.Parameter;

public class Options {

    @Parameter(names = {"-h", "--help"},
            help = true,
            description = "Displays help information and exit",
            order = 10
    )
    private boolean help;

    @Parameter(
      names =  {"-v", "--version"},
      description = "Print program version and exit",
      required = false,
      order = 20
    )
    private boolean version;

    @Parameter(
      names =  {"-o", "--output"},
      description = "Output file. If none, output will be echoed to stdout",
      required = false,
      order = 30
    )
    private String output;

    @Parameter(
      names =  {"-a", "--append"},
      description = "Append to output file instead of overwrite",
      required = false,
      order = 40
    )
    private boolean append = false;

    @Parameter(
      names =  {"-r", "--record-delimiter"},
      description = "Record delimiter. Default is Windows-style EOL, '\\r\\n'",
      required = false,
      order = 50
    )
    private String recordDelimiter = "\r\n";

    @Parameter(
      names =  {"-f", "--field-delimiter"},
      description = "Field delimiter.",
      required = false,
      order = 60
    )
    private String fieldDelimiter = ",";

    @Parameter(
      names =  {"-q", "--quote"},
      description = "String delimiter (or quote). Must be a single character.",
      required = false,
      order = 70
    )
    private Character quote = '"';

    @Parameter(
      names =  {"-e", "--escape"},
      description = "Escape character (for quote character). Must be a single character.",
      required = false,
      order = 80
    )
    private Character escape = '"';

    @Parameter(
      names =  {"-n", "--nested-attribute-separator"},
      description = "String used to separate nested attributes in CSV header",
      required = false,
      order = 90
    )
    private String attributeSeparator = "-";

    @Parameter(
      names =  {"--max", "--max-depth"},
      description = "Max depth for JSON objects inspection",
      required = false,
      order = 100
    )
    private Integer maxDepth;

    @Parameter(
      description = "<input JSON files>",
      required = true,
      variableArity = true
    )
    private List<String> files = new ArrayList<>();

    public String getOutput() {
        return output;
    }

    public void setOutput(String output) {
        this.output = output;
    }

    public boolean isAppend() {
        return append;
    }

    public void setAppend(boolean append) {
        this.append = append;
    }

    public List<String> getFiles() {
        return files;
    }

    public void setFiles(List<String> files) {
        this.files = files;
    }

    public boolean isHelp() {
        return help;
    }

    public void setHelp(boolean help) {
        this.help = help;
    }

    public String getRecordDelimiter() {
        return recordDelimiter;
    }

    public void setRecordDelimiter(String recordDelimiter) {
        this.recordDelimiter = recordDelimiter;
    }

    public String getFieldDelimiter() {
        return fieldDelimiter;
    }

    public void setFieldDelimiter(String fieldDelimiter) {
        this.fieldDelimiter = fieldDelimiter;
    }

    public Character getQuote() {
        return quote;
    }

    public void setQuote(Character quote) {
        this.quote = quote;
    }

    public Character getEscape() {
        return escape;
    }

    public void setEscape(Character escape) {
        this.escape = escape;
    }

    public String getAttributeSeparator() {
        return attributeSeparator;
    }

    public void setAttributeSeparator(String attributeSeparator) {
        this.attributeSeparator = attributeSeparator;
    }

    public Integer getMaxDepth() {
        return maxDepth;
    }

    public void setMaxDepth(Integer maxDepth) {
        this.maxDepth = maxDepth;
    }

	public boolean isVersion() {
		return version;
	}

	public void setVersion(boolean version) {
		this.version = version;
	}
}
