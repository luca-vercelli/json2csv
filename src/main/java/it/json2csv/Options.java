package it.json2csv;

import java.util.ArrayList;
import java.util.List;

import com.beust.jcommander.Parameter;

public class Options {

    @Parameter(names = {"-h", "--help"},
            help = true,
            description = "Displays help information")
    private boolean help;

    @Parameter(
      names =  {"-o", "--output"},
      description = "Output file. If none, output will be echoed to stdout",
      required = false
    )
    private String output;

    @Parameter(
      names =  {"-a", "--append"},
      description = "Append to output file instead of overwrite",
      required = false
    )
    private boolean append = false;

    @Parameter(
      names =  {"-r", "--record-delimiter"},
      description = "Field delimiter. Default '\\r\\n'",
      required = false
    )
    private String recordDelimiter = "\r\n";

    @Parameter(
      names =  {"-f", "--field-delimiter"},
      description = "Field delimiter.",
      required = false
    )
    private String fieldDelimiter = ",";

    @Parameter(
      names =  {"-q", "--quote"},
      description = "String delimiter (or quote). Must be a single character.",
      required = false
    )
    private Character quote = '"';

    @Parameter(
      names =  {"-e", "--escape"},
      description = "Escape character (for quote character). Must be a single character.",
      required = false
    )
    private Character escape = '"';

    @Parameter(
      names =  {"-n", "--nested-attribute-separator"},
      description = "String used to separate nested attributes in CSV header",
      required = false
    )
    private String attributeSeparator = "-";

    @Parameter(
      description = "<input JSON files>",
      required = true,
      variableArity = true
    )
    private List<String> files = new ArrayList<>();

    @Parameter(
      names =  {"--max", "--max-depth"},
      description = "Max depth for objects inspection",
      required = false
    )
    private Integer maxDepth;

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
}
