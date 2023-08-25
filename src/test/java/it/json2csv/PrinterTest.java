package it.json2csv;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class PrinterTest {

    Options options;
    Printer printer;

    @BeforeEach
    public void setup() throws IOException {
        options = new Options();
        printer = new Printer(options);
    }

    @Test
    public void testPrintCSVLocale() throws IOException {
        File tempFile = File.createTempFile("temp-", ".csv");
        tempFile.deleteOnExit();

        options.setOutput(tempFile.getAbsolutePath());
        options.setLocaleText("it_IT");

        List<String> headers = List.of("value");

        List<Object[]> data = new ArrayList<>();
        data.add(new Object[]{ 1234.5678 });
        assertNotNull(printer.createCsvPrinter());

        printer.printCSV(data, headers);

        assertTrue(tempFile.length() > 0);
        String fileContent = FileUtils.readFileToString(tempFile, "utf-8");
        assertFalse(fileContent.contains("1234,5678"));
    }

    @Test
    public void testPrintCSVNoHeader() throws IOException {
        File tempFile = File.createTempFile("temp-", ".csv");
        tempFile.deleteOnExit();

        options.setOutput(tempFile.getAbsolutePath());
        options.setSkipHeader(true);

        List<String> headers = List.of("first", "second", "third");
        assertNotNull(printer.createCsvPrinter());

        List<Object[]> data = new ArrayList<>();
        data.add(new Object[]{ "a", 1, "c"});
        data.add(new Object[]{ "A,B", 2, "C"});

        printer.printCSV(data, headers);

        assertTrue(tempFile.length() > 0);
        String fileContent = FileUtils.readFileToString(tempFile, "utf-8");
        assertFalse(fileContent.contains("first,second,third"));
        assertTrue(fileContent.contains("a,1,c"));
        assertTrue(fileContent.contains("\"A,B\",2,C")); // MINIMAL quote mode
    }

    @Test
    public void testPrintCSVNumberFormat() throws IOException {
        File tempFile = File.createTempFile("temp-", ".csv");
        tempFile.deleteOnExit();

        options.setOutput(tempFile.getAbsolutePath());
        options.setNumberFormatText("0,000,000.000");
    
        List<String> headers = List.of("value");
        assertNotNull(printer.createCsvPrinter());

        List<Object[]> data = new ArrayList<>();
        data.add(new Object[]{ 1234.5678 });

        printer.printCSV(data, headers);

        assertTrue(tempFile.length() > 0);
        String fileContent = FileUtils.readFileToString(tempFile, "utf-8");
        assertFalse(fileContent.contains("1,234.568"));
    }

    @Test
    public void testPrintCSV() throws IOException {
        File tempFile = File.createTempFile("temp-", ".csv");
        tempFile.deleteOnExit();

        options.setOutput(tempFile.getAbsolutePath());

        List<String> headers = List.of("first", "second", "third");
        assertNotNull(printer.createCsvPrinter());

        List<Object[]> data = new ArrayList<>();
        data.add(new Object[]{ "a", 1, "c"});
        data.add(new Object[]{ "A,B", 2, "C"});

        printer.printCSV(data, headers);

        assertTrue(tempFile.length() > 0);
        String fileContent = FileUtils.readFileToString(tempFile, "utf-8");
        assertTrue(fileContent.contains("first,second,third"));
        assertTrue(fileContent.contains("a,1,c"));
        assertTrue(fileContent.contains("\"A,B\",2,C")); // MINIMAL quote mode
    }
}
