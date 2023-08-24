package it.json2csv;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonString;
import javax.json.JsonValue;
import javax.json.JsonValue.ValueType;

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
    public void testFullJoin1() throws FileNotFoundException, IOException {
        SortedMap<String, Object> map1 = new TreeMap<>();
        map1.put("somestring", "foo");
        List<SortedMap<String, Object>> fullJoin = converter.fullJoin(map1);
        assertEquals(1, fullJoin.size());
        assertEquals(map1, fullJoin.get(0));
    }

    @Test
    public void testFullJoin2() throws FileNotFoundException, IOException {
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
}
