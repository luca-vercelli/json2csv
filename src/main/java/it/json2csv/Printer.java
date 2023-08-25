package it.json2csv;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

/**
 * Print data to CSV, either on file or on stdout
 */
public class Printer {

    private Options options;

    public Printer(Options options) throws IOException {
        this.options = options;
    }

    public void printCSV(List<Object[]> dataAsListOfArrays, List<String> headers) throws IOException {
        try (CSVPrinter printer = createCsvPrinter()) {
            if (!options.isSkipHeader()) {
                printer.printRecord(headers.toArray());
            }
            for (Object[] row : dataAsListOfArrays) {
                printer.printRecord(row);
            }
        }
    }

    CSVPrinter createCsvPrinter() throws IOException {
        CSVFormat format = createCsvFormat();
        Appendable appendable = createAppendable();
        return new CSVPrinter(appendable, format);
    }

    Appendable createAppendable() throws IOException {
        if (options.getOutput() == null || options.getOutput().trim().isEmpty()) {
            return System.out;
        } else {
            return new FileWriter(options.getOutput(), options.isAppend());
        }
    }

    CSVFormat createCsvFormat() {
        CSVFormat format = CSVFormat.Builder.create()
                .setDelimiter(options.getFieldDelimiter())
                .setRecordSeparator(options.getRecordDelimiter())
                .setQuote(options.getQuote())
                .setEscape(options.getEscape())
                .build();
        return format;
    }
}
