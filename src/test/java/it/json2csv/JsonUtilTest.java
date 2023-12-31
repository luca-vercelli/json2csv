package it.json2csv;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.List;

import javax.json.JsonArray;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonString;
import javax.json.JsonValue;
import javax.json.JsonValue.ValueType;

import org.junit.jupiter.api.Test;

public class JsonUtilTest {

    JsonUtil util = new JsonUtil();

    @Test
    public void testExtractIdentifiers() {
        String path;
        List<String> extracted, expected;

        path = "aaa/bbb/2/ccc";
        extracted = util.extractIdentifiers(path);
        expected = List.of("aaa", "bbb", "2", "ccc");
        assertNotNull(extracted);
        assertEquals(expected, extracted);

        path = "aaa/bbb//2/ccc";
        extracted = util.extractIdentifiers(path);
        expected = List.of("aaa", "bbb/2", "ccc");
        assertNotNull(extracted);
        assertEquals(expected, extracted);

        path = "aaa/2:c\u00E0\"@\u00E8::///ccc";
        extracted = util.extractIdentifiers(path);
        expected = List.of("aaa", "2:c\u00E0\"@\u00E8::/", "ccc");
        assertNotNull(extracted);
        assertEquals(expected, extracted);

        path = "";
        extracted = util.extractIdentifiers(path);
        expected = List.of();
        assertNotNull(extracted);
        assertEquals(expected, extracted);

    }

    public String getResourceFileName(String filename) {
        URL url = Thread.currentThread().getContextClassLoader().getResource(filename);
        return url.getPath();
    }

    @Test
    public void testJsonFromFile() throws FileNotFoundException, IOException {
        JsonValue result = util.jsonFromFile(getResourceFileName("sample2.json"));
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
    public void testGetRoot() throws FileNotFoundException, IOException {
        JsonValue value = util.jsonFromFile(getResourceFileName("sample-nested-2.json"));

        JsonValue response = util.getRoot(value, "data");
        assertNotNull(response);
        assertTrue(response instanceof JsonArray);

        response = util.getRoot(value, "data/0/name");
        assertNotNull(response);
        assertTrue(response instanceof JsonString);

        response = util.getRoot(value, "data/1/value/1");
        assertNotNull(response);
        assertTrue(response instanceof JsonNumber);

        // missing elements return null
        response = util.getRoot(value, "foo");
        assertNull(response);

        response = util.getRoot(value, "foo/bar/1/foo");
        assertNull(response);
    }

    @Test
    public void testGetRootErrors() throws FileNotFoundException, IOException {
        JsonValue value = util.jsonFromFile(getResourceFileName("sample-nested-2.json"));

        try {
            util.getRoot(value, "data/0/name/0");
            fail("Should give exception: not an array");
        } catch (IllegalArgumentException e) {
            // ok
        }

        try {
            util.getRoot(value, "data/0/name/foo");
            fail("Should give exception: not an object");
        } catch (IllegalArgumentException e) {
            // ok
        }

        try {
            util.getRoot(value, "data/100");
            fail("Should give exception: index out of bounds");
        } catch (IndexOutOfBoundsException e) {
            // ok
        }
    }

    @Test
    public void testExclude1() throws FileNotFoundException, IOException {
        JsonValue value = util.jsonFromFile(getResourceFileName("sample-nested-2.json"));
        JsonValue result;
        JsonArray data;
        
        assertTrue(value instanceof JsonObject);
        assertTrue(((JsonObject)value).get("data") instanceof JsonArray);
        data = (JsonArray) ((JsonObject)value).get("data");
        assertEquals(2, data.size());
        assertTrue(data.get(1) instanceof JsonObject);
        assertNotNull(((JsonObject)data.get(1)).get("value"));

        result = util.removeNode(value, "data/1/value");
        assertTrue(result instanceof JsonObject);
        assertTrue(((JsonObject)result).get("data") instanceof JsonArray);
        data = (JsonArray) ((JsonObject)result).get("data");
        assertEquals(2, data.size());
        assertTrue(data.get(1) instanceof JsonObject);
        assertNull(((JsonObject)data.get(1)).get("value"));
    }

    @Test
    public void testExclude2() throws FileNotFoundException, IOException {
        JsonValue value = util.jsonFromFile(getResourceFileName("sample-nested-2.json"));
        JsonValue result;
        JsonArray data;

        result = util.removeNode(value, "data/0/name");
        assertTrue(result instanceof JsonObject);
        assertTrue(((JsonObject)result).get("data") instanceof JsonArray);
        data = (JsonArray) ((JsonObject)result).get("data");
        assertTrue(data.size() == 2);
        assertTrue(data.get(0) instanceof JsonObject);
        assertNull(((JsonObject)(data.get(0))).get("name"));
        assertNotNull(((JsonObject)(data.get(1))).get("name"));
    }

    @Test
    public void testExcludeStar() throws FileNotFoundException, IOException {
        JsonValue value = util.jsonFromFile(getResourceFileName("sample-nested-array.json"));
        JsonValue result;
        JsonArray data;

        result = util.removeNode(value, "data/*/name");
        assertTrue(result instanceof JsonObject);
        assertTrue(((JsonObject)result).get("data") instanceof JsonArray);
        data = (JsonArray) ((JsonObject)result).get("data");
        assertTrue(data.size() == 2);
        assertTrue(data.get(0) instanceof JsonObject);
        assertNull(((JsonObject)(data.get(0))).get("name"));
        assertNull(((JsonObject)(data.get(1))).get("name"));
    }

    @Test
    public void testOAA() throws FileNotFoundException, IOException {
        JsonValue value = util.jsonFromFile(getResourceFileName("sample-oaa.json"));
        JsonValue result;
        JsonArray data;

        result = util.oaa(value, "data", "key", "value");
        assertTrue(result instanceof JsonObject);
        assertTrue(((JsonObject)result).get("data") instanceof JsonArray);
        data = (JsonArray) ((JsonObject)result).get("data");
        assertTrue(data.size() == 3);
        assertTrue(data.get(0) instanceof JsonObject);
        
        assertNotNull(((JsonObject)(data.get(0))).get("name"));
        assertNotNull(((JsonObject)(data.get(0))).get("key"));
        assertEquals("\"goofy\"", ((JsonObject)(data.get(0))).get("key").toString());

        assertNotNull(((JsonObject)(data.get(2))).get("name"));
        assertNotNull(((JsonObject)(data.get(2))).get("key"));
        assertEquals("\"donald\"", ((JsonObject)(data.get(2))).get("key").toString());
    }
}
