package it.json2csv;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.beust.jcommander.Parameter;

public class Options {

  @Parameter( //
      names = { "-h", "--help" }, //
      help = true, //
      description = "Displays help information and exit", //
      order = 10 //
  )
  private boolean help;

  @Parameter( //
      names = { "-v", "--version" }, //
      help = true, //
      description = "Print program version and exit", //
      required = false, //
      order = 20 //
  )
  private boolean version;

  @Parameter( //
      names = { "-o", "--output" }, //
      description = "Output file. If none, output will be echoed to stdout", //
      required = false, //
      order = 30 //
  )
  private String output;

  @Parameter( //
      names = { "-a", "--append" }, //
      description = "Append to output file instead of overwrite", //
      required = false, //
      order = 40 //
  )
  private boolean append = false;

  @Parameter( //
      names = { "-r", "--record-delimiter" }, //
      description = "Record delimiter. Default is Windows-style EOL, '\\r\\n'", //
      required = false, //
      order = 50 //
  )
  private String recordDelimiter = "\r\n";

  @Parameter( //
      names = { "-f", "--field-delimiter" }, //
      description = "Field delimiter.", //
      required = false, //
      order = 60 //
  )
  private String fieldDelimiter = ",";

  @Parameter( //
      names = { "-q", "--quote" }, //
      description = "String delimiter (or quote). Must be a single character.", //
      required = false, //
      order = 70 //
  )
  private Character quote = '"';

  @Parameter( //
      names = { "-e", "--escape" }, //
      description = "Escape character (for quote character). Must be a single character.", //
      required = false, //
      order = 80 //
  )
  private Character escape = '"';

  @Parameter( //
      names = { "-n", "--nested-attribute-separator" }, //
      description = "String used to separate nested attributes in CSV header", //
      required = false, //
      order = 90 //
  )
  private String attributeSeparator = "-";

  @Parameter( //
      names = { "--max-depth" }, //
      description = "Max depth for JSON objects inspection", //
      required = false, //
      order = 100 //
  )
  private Integer maxDepth;

  @Parameter( //
      names = { "--skip-header" }, //
      description = "Do not print header", //
      required = false, //
      order = 110 //
  )
  private boolean skipHeader;

  @Parameter( //
      names = { "--number-format" }, //
      description = "Number format for all numbers, es. #,##0.00", //
      required = false, //
      order = 120 //
  )
  private String numberFormatText;

  @Parameter( //
      names = { "-l", "--locale" }, //
      description = "Locale, eg. en or en_US, to be used for formatting numbers. This is alternative to --number-format.", //
      required = false, //
      order = 130 //
  )
  private String localeText;

  @Parameter( //
      names = { "-x", "--exclude" }, //
      description = "Comma separated list of nodes to exclude. Path expressed in form aaa/bbb/2/ccc. Use * instead of number to denote all array elements.", //
      required = false, //
      order = 140 //
  )
  private List<String> exclude;

  @Parameter( //
      names = { "--root" }, //
      description = "JSON root node. Path expressed in form aaa/bbb/2/ccc", //
      required = false, //
      order = 150 //
  )
  private String root;

  @Parameter( //
      names = { "-k", "--filter-columns" }, //
      description = "List of columns as they will appear in output file", //
      required = false, //
      order = 160 //
  )
  private List<String> outputColumns;

  @Parameter( //
      names = { "--unix" }, //
      description = "Output suitable for *NIX pipelines. Equivalent to --skip-header -f \" \" -r \"\" -q \"\" -e \"\"", //
      required = false, //
      order = 135 //
  )
  private boolean unix;

  @Parameter( //
      names = { "--oaa", "--objects-as-arrays" }, //
      description = "Comma separated list of object nodes that should be considered arrays", //
      required = false, //
      order = 170 //
  )
  private List<String> oaa;

  @Parameter( //
      description = "<input JSON files>", //
      required = true, //
      variableArity = true //
  )
  private List<String> files;

  private NumberFormat numberFormat;
  private Locale locale;

  // ===== Getters and setters ===============================================

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

  public boolean isUnix() {
    return unix;
  }

  public void setUnix(boolean unix) {
    this.unix = unix;
  }

  public boolean isSkipHeader() {
    return skipHeader;
  }

  public void setSkipHeader(boolean skipHeader) {
    this.skipHeader = skipHeader;
  }

  public String getNumberFormatText() {
    return numberFormatText;
  }

  public void setNumberFormatText(String numberFormatText) {
    this.numberFormatText = numberFormatText == null || numberFormatText.isBlank() ? null : numberFormatText;
  }

  public String getLocaleText() {
    return localeText;
  }

  public void setLocaleText(String localeText) {
    this.localeText = localeText == null || localeText.isBlank() ? null : localeText;
  }

  public List<String> getExclude() {
    return exclude;
  }

  public void setExclude(List<String> exclude) {
    this.exclude = exclude;
  }

  public String getRoot() {
    return root;
  }

  public void setRoot(String root) {
    this.root = root;
  }

  public List<String> getOutputColumns() {
    return outputColumns;
  }

  public void setOutputColumns(List<String> outputColumns) {
    this.outputColumns = outputColumns;
  }

  // ===== Other methods ===============================================

  /**
   * Return a NumberFormat object built on <code>localeText</code> or
   * <code>numberFormatText</code>
   * 
   * @return
   */
  public NumberFormat getNumberFormat() {
    if (numberFormat == null && (localeText != null || numberFormatText != null)) {
      if (localeText != null && numberFormatText != null) {
        System.err.println("Warning! You should only pass one among --locale and --number-format");
      }
      numberFormat = localeText != null ? NumberFormat.getInstance(getLocale()) : new DecimalFormat(numberFormatText);
    }
    return numberFormat;
  }

  /**
   * Return a Locale object built on <code>localeText</code>
   * 
   * @return
   */
  public Locale getLocale() {
    if (locale == null && localeText != null) {
      locale = new Locale(localeText);
    }
    return locale;
  }
}
