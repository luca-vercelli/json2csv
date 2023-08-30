package it.json2csv;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class ConverterTest {
    
    String jsonFullFilename;
    Options options;
    Converter converter;

    @BeforeEach
    public void setup() {
        options = new Options();
        converter = new Converter(options);
    }

    public void setSample(String filename) {
        URL url = Thread.currentThread().getContextClassLoader().getResource(filename);
        jsonFullFilename = url.getPath();
        options.getFiles().add(jsonFullFilename);
    }

    @Test
    public void testRead1() throws FileNotFoundException, IOException {
        setSample("sample1.json");
        List<LinkedHashMap<String, Object>> rows = converter.readData();
        assertEquals(1, rows.size());
        LinkedHashMap<String, Object> row = rows.get(0);
        assertEquals(6, row.keySet().size());
        assertEquals(new NumberWrapper(200, options.getNumberFormat()), row.get("code"));
        assertEquals("success", row.get("status"));
        assertEquals(true, row.get("canRead"));
        assertEquals(false, row.get("canWrite"));
        assertEquals("", row.get("owner"));
    }

    @Test
    public void testRead2() throws FileNotFoundException, IOException {
        setSample("sample2.json");
        List<LinkedHashMap<String, Object>> rows = converter.readData();
        assertEquals(1, rows.size());
        LinkedHashMap<String, Object> row = rows.get(0);
        assertEquals(9, row.keySet().size());
        assertEquals("27188002", row.get("result-codice"));
        assertEquals("05", row.get("result-settore-codice"));
    }

    @Test
    public void testFullJoinNoArray() throws FileNotFoundException, IOException {
        LinkedHashMap<String, Object> map1 = new LinkedHashMap<>();
        map1.put("somestring", "foo");
        List<LinkedHashMap<String, Object>> fullJoin = converter.fullJoin(map1);
        assertEquals(1, fullJoin.size());
        assertEquals(map1, fullJoin.get(0));
    }

    @Test
    public void testFullJoin1Array() throws FileNotFoundException, IOException {
        LinkedHashMap<String, Object> map1 = new LinkedHashMap<>();
        map1.put("somestring", "foo");
        map1.put("somearray", List.of("a", "b"));
        List<LinkedHashMap<String, Object>> fullJoin = converter.fullJoin(map1);
        assertEquals(2, fullJoin.size());
        assertEquals(2, fullJoin.get(0).entrySet().size());
        assertEquals("a", fullJoin.get(0).get("somearray"));
        assertEquals(2, fullJoin.get(1).entrySet().size());
        assertEquals("b", fullJoin.get(1).get("somearray"));
    }

    @Test
    public void testFullJoin3Arrays() throws FileNotFoundException, IOException {
        LinkedHashMap<String, Object> map1 = new LinkedHashMap<>();
        map1.put("somestring", "foo");
        map1.put("array1", List.of("a", "b"));
        map1.put("array2", List.of("c", "d"));
        map1.put("array3", List.of("x", "y", "z"));
        List<LinkedHashMap<String, Object>> fullJoin = converter.fullJoin(map1);
        assertEquals(12, fullJoin.size());
        assertEquals(4, fullJoin.get(0).entrySet().size());
        assertEquals(4, fullJoin.get(11).entrySet().size());
    }

    @Test
    public void testFullJoinEmptyArray() throws FileNotFoundException, IOException {
        LinkedHashMap<String, Object> map1 = new LinkedHashMap<>();
        map1.put("somestring", "foo");
        map1.put("somearray", new ArrayList<String>());
        List<LinkedHashMap<String, Object>> fullJoin = converter.fullJoin(map1);
        assertEquals(1, fullJoin.size());
        assertEquals(1, fullJoin.get(0).entrySet().size());
        assertEquals("foo", fullJoin.get(0).get("somestring"));
        assertEquals(null, fullJoin.get(0).get("somearray"));
    }

    @Test
    public void testRead3() throws FileNotFoundException, IOException {
        setSample("sample3.json");
        List<LinkedHashMap<String, Object>> rows = converter.readData();
        assertEquals(4, rows.size());
        LinkedHashMap<String, Object> row = rows.get(0);
        assertEquals(7, row.keySet().size());
        assertTrue(row.get("friends") instanceof String);
    }

    @Test
    public void testE2E() throws IOException {
        File tempFile = File.createTempFile("temp-", ".csv");
        tempFile.deleteOnExit();
        
        setSample("sample1.json");
        options.setOutput(tempFile.getAbsolutePath());

        converter.run();
        
        assertTrue(tempFile.length() > 0);

        //check that columns order is preserved
        LineIterator it = FileUtils.lineIterator(tempFile, "UTF-8");
        String firstLine = it.nextLine();
        assertTrue(firstLine.startsWith("code,status,canRead,canWrite,owner"));
    }

    @Test
    @Disabled
    // this will take 5-10 sec
    public void testE2ELarge() throws IOException {
        File tempFile = File.createTempFile("temp-", ".csv");
        tempFile.deleteOnExit();
        
        setSample("sample-large.json");
        options.setOutput(tempFile.getAbsolutePath());

        converter.run();
        
        assertTrue(tempFile.length() > 400000000l);
    }

    @Test
    public void testMaxDepth() throws IOException {
        File tempFile = File.createTempFile("temp-", ".csv");
        tempFile.deleteOnExit();
        
        setSample("sample-large.json");
        options.setOutput(tempFile.getAbsolutePath());
        options.setMaxDepth(2);

        converter.run();
        
        assertTrue(tempFile.length() < 6000);
    }

    @Test
    public void testE2Enull() throws IOException {
        File tempFile = File.createTempFile("temp-", ".csv");
        tempFile.deleteOnExit();
        
        setSample("sample-null.json");
        options.setOutput(tempFile.getAbsolutePath());

        converter.run();
    
        String fileContent = FileUtils.readFileToString(tempFile, "utf-8");
        assertTrue(fileContent.contains("value"));
        assertTrue(fileContent.contains("\"\"")); // MINIMAL quote mode + null is represented as ""
    }

    @Test
    public void testE2Estring() throws IOException {
        File tempFile = File.createTempFile("temp-", ".csv");
        tempFile.deleteOnExit();
        
        setSample("sample-string.json");
        options.setOutput(tempFile.getAbsolutePath());

        converter.run();
    
        String fileContent = FileUtils.readFileToString(tempFile, "utf-8");
        assertTrue(fileContent.contains("value"));
        assertTrue(fileContent.contains("hello"));
    }

    @Test
    public void testE2Earray() throws IOException {
        File tempFile = File.createTempFile("temp-", ".csv");
        tempFile.deleteOnExit();
        
        setSample("sample-array.json");
        options.setOutput(tempFile.getAbsolutePath());

        converter.run();
    
        String fileContent = FileUtils.readFileToString(tempFile, "utf-8");
        assertTrue(fileContent.contains("name,surname"));
        assertTrue(fileContent.contains("foo,bar"));
        assertTrue(fileContent.contains("x,y"));
    }

    @Test
    public void testE2EnestedArray() throws IOException {
        File tempFile = File.createTempFile("temp-", ".csv");
        tempFile.deleteOnExit();
        
        setSample("sample-nested-array.json");
        options.setOutput(tempFile.getAbsolutePath());

        converter.run();
    
        String fileContent = FileUtils.readFileToString(tempFile, "utf-8");
        assertTrue(fileContent.contains("data-name,data-surname"));
        assertTrue(fileContent.contains("foo,bar"));
        assertTrue(fileContent.contains("x,y"));
    }
    
    @Test
    public void testUnix() {
        setSample("sample-nested-array.json");
        options.setUnix(true);

        // replace stout for test
        final PrintStream standardOut = System.out;
        final ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputStreamCaptor));
        try {

            converter.run();
    
            String output = outputStreamCaptor.toString().trim();

            assertFalse(output.contains("data-name"));
            assertEquals("foo bar\nx y", output.trim());
        } finally {
            System.setOut(standardOut);
        }
    }

    @Test
    public void testE2ERoot() throws IOException {
        File tempFile = File.createTempFile("temp-", ".csv");
        tempFile.deleteOnExit();
        
        setSample("sample-nested-2.json");
        options.setOutput(tempFile.getAbsolutePath());
        options.setRoot("data/0");

        converter.run();
        
        assertTrue(tempFile.length() > 0);

        LineIterator it = FileUtils.lineIterator(tempFile, "UTF-8");
        String firstLine = it.nextLine();
        assertEquals("name,surname", firstLine);
        String secondLine = it.nextLine();
        assertEquals("foo,bar", secondLine);
        assertFalse(it.hasNext());
    }

    @Test
    public void testE2EExclude() throws IOException {
        File tempFile = File.createTempFile("temp-", ".csv");
        tempFile.deleteOnExit();
        
        setSample("sample-nested-2.json");
        options.setOutput(tempFile.getAbsolutePath());
        options.setExclude(List.of("data/0/surname", "data/1/value"));

        converter.run();
        
        assertTrue(tempFile.length() > 0);

        LineIterator it = FileUtils.lineIterator(tempFile, "UTF-8");
        String firstLine = it.nextLine();
        assertEquals("data-name", firstLine);
        String secondLine = it.nextLine();
        assertEquals("foo", secondLine);
        String thirdLine = it.nextLine();
        assertEquals("x", thirdLine);
        assertFalse(it.hasNext());
    }

    @Test
    public void testE2EOutputColumns() throws IOException {
        File tempFile = File.createTempFile("temp-", ".csv");
        tempFile.deleteOnExit();
        
        setSample("sample-nested-2.json");
        options.setOutput(tempFile.getAbsolutePath());
        options.setOutputColumns(List.of("data-name", "boo"));

        converter.run();
        
        assertTrue(tempFile.length() > 0);

        LineIterator it = FileUtils.lineIterator(tempFile, "UTF-8");
        String firstLine = it.nextLine();
        assertEquals("data-name,boo", firstLine);
        String line = it.nextLine();
        assertEquals("foo,", line);
        line = it.nextLine();
        assertEquals("x,", line);
        line = it.nextLine();
        assertEquals("x,", line);
        line = it.nextLine();
        assertEquals("x,", line);
        assertFalse(it.hasNext());
    }

    @Test
    public void testE2EOutputColumns2() throws IOException {
        File tempFile = File.createTempFile("temp-", ".csv");
        tempFile.deleteOnExit();
        
        setSample("sample-nested-2.json");
        options.setOutput(tempFile.getAbsolutePath());
        options.setOutputColumns(List.of("data-name", "aaaaaaa"));

        converter.run();
        
        assertTrue(tempFile.length() > 0);

        LineIterator it = FileUtils.lineIterator(tempFile, "UTF-8");
        String firstLine = it.nextLine();
        assertEquals("data-name,aaaaaaa", firstLine);
        String line = it.nextLine();
        assertEquals("foo,", line);
        line = it.nextLine();
        assertEquals("x,", line);
        line = it.nextLine();
        assertEquals("x,", line);
        line = it.nextLine();
        assertEquals("x,", line);
        assertFalse(it.hasNext());
    }

}
