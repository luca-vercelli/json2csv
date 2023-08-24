package it.json2csv;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonString;
import javax.json.JsonValue;
import javax.json.JsonValue.ValueType;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeEach;
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
    public void testJsonFromFile() throws FileNotFoundException, IOException {
        setSample("sample2.json");
        JsonValue result = converter.jsonFromFile(jsonFullFilename);
        assertTrue(result instanceof JsonObject);
        assertTrue(((JsonObject)result).get("code") instanceof JsonNumber);
        assertEquals(200, ((JsonNumber)((JsonObject)result).get("code")).intValue());
        assertTrue(((JsonObject)result).get("status") instanceof JsonString);
        assertTrue(((JsonObject)result).get("result") instanceof JsonObject);
        assertTrue(((JsonObject)result).get("canRead").getValueType() == ValueType.TRUE);
        assertTrue(((JsonObject)result).get("canWrite").getValueType() == ValueType.FALSE);
        assertTrue(((JsonObject)result).get("owner").getValueType() == ValueType.NULL);
    }

    @Test
    public void testRead1() throws FileNotFoundException, IOException {
        setSample("sample1.json");
        List<SortedMap<String, Object>> rows = converter.readData();
        assertEquals(1, rows.size());
        SortedMap<String, Object> row = rows.get(0);
        assertEquals(5, row.keySet().size());
        assertEquals(new BigDecimal(200), row.get("code"));
        assertEquals("success", row.get("status"));
        assertEquals(true, row.get("canRead"));
        assertEquals(false, row.get("canWrite"));
        assertEquals("", row.get("owner"));
    }

    @Test
    public void testRead2() throws FileNotFoundException, IOException {
        setSample("sample2.json");
        List<SortedMap<String, Object>> rows = converter.readData();
        assertEquals(1, rows.size());
        SortedMap<String, Object> row = rows.get(0);
        assertEquals(9, row.keySet().size());
        assertEquals("27188002", row.get("result-codice"));
        assertEquals("05", row.get("result-settore-codice"));
    }

    @Test
    public void testFullJoinNoArray() throws FileNotFoundException, IOException {
        SortedMap<String, Object> map1 = new TreeMap<>();
        map1.put("somestring", "foo");
        List<SortedMap<String, Object>> fullJoin = converter.fullJoin(map1);
        assertEquals(1, fullJoin.size());
        assertEquals(map1, fullJoin.get(0));
    }

    @Test
    public void testFullJoin1Array() throws FileNotFoundException, IOException {
        SortedMap<String, Object> map1 = new TreeMap<>();
        map1.put("somestring", "foo");
        map1.put("somearray", List.of("a", "b"));
        List<SortedMap<String, Object>> fullJoin = converter.fullJoin(map1);
        assertEquals(2, fullJoin.size());
        assertEquals(2, fullJoin.get(0).entrySet().size());
        assertEquals("a", fullJoin.get(0).get("somearray"));
        assertEquals(2, fullJoin.get(1).entrySet().size());
        assertEquals("b", fullJoin.get(1).get("somearray"));
    }

    @Test
    public void testFullJoin3Arrays() throws FileNotFoundException, IOException {
        SortedMap<String, Object> map1 = new TreeMap<>();
        map1.put("somestring", "foo");
        map1.put("array1", List.of("a", "b"));
        map1.put("array2", List.of("c", "d"));
        map1.put("array3", List.of("x", "y", "z"));
        List<SortedMap<String, Object>> fullJoin = converter.fullJoin(map1);
        assertEquals(12, fullJoin.size());
        assertEquals(4, fullJoin.get(0).entrySet().size());
        assertEquals(4, fullJoin.get(11).entrySet().size());
    }

    @Test
    public void testFullJoinEmptyArray() throws FileNotFoundException, IOException {
        SortedMap<String, Object> map1 = new TreeMap<>();
        map1.put("somestring", "foo");
        map1.put("somearray", new ArrayList<String>());
        List<SortedMap<String, Object>> fullJoin = converter.fullJoin(map1);
        assertEquals(1, fullJoin.size());
        assertEquals(2, fullJoin.get(0).entrySet().size());
        assertEquals("foo", fullJoin.get(0).get("somestring"));
        assertEquals("", fullJoin.get(0).get("somearray"));
    }

    @Test
    public void testRead3() throws FileNotFoundException, IOException {
        setSample("sample3.json");
        List<SortedMap<String, Object>> rows = converter.readData();
        assertEquals(4, rows.size());
        SortedMap<String, Object> row = rows.get(0);
        assertEquals(7, row.keySet().size());
        Object s = row.get("friends");
        assertTrue(row.get("friends") instanceof String);
    }

    @Test
    public void testPrintCSV() throws IOException {
        File tempFile = File.createTempFile("temp-", ".csv");
        tempFile.deleteOnExit();

        options.setOutput(tempFile.getAbsolutePath());

        assertNotNull(converter.createCsvPrinter());

        List<Object[]> data = new ArrayList<>();
        data.add(new Object[]{ "a", 1, "c"});
        data.add(new Object[]{ "A,B", 2, "C"});

        converter.printCSV(data);

        assertTrue(tempFile.length() > 0);
        String fileContent = FileUtils.readFileToString(tempFile, "utf-8");
        assertTrue(fileContent.contains("a,1,c"));
        assertTrue(fileContent.contains("\"A,B\",2,C")); // MINIMAL quote mode
    }

    @Test
    public void testE2E() throws IOException {
        File tempFile = File.createTempFile("temp-", ".csv");
        tempFile.deleteOnExit();
        
        setSample("sample1.json");
        options.setOutput(tempFile.getAbsolutePath());

        converter.run();
        
        assertTrue(tempFile.length() > 0);
    }
}
