package it.json2csv;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonValue;

public class JsonUtil {

	JsonValue jsonFromFile(String filename) throws FileNotFoundException, IOException {
		JsonValue data = null;
		try (FileReader fr = new FileReader(filename)) {
			try (JsonReader jsonReader = Json.createReader(fr)) {
				data = jsonReader.readValue();
			}
		}
		return data;
	}
    
    Pattern patternArray = Pattern.compile("^(\\w+)\\[(\\d+)\\]$");
    Pattern patternObject = Pattern.compile("^\\w+$");

    /**
     * Estract subtree from given JSON
     * @param value input JSON
     * @param root JavaScript-like path inside JSON, e.g. "value.data[4].root"
     */
    public JsonValue getRoot(JsonValue value, String root) {
        String[] paths = root.split("\\.");
        for (String path: paths) {
            Matcher matchArray = patternArray.matcher(path);
            Matcher matchObject = patternObject.matcher(path);
            if (matchArray.matches()) {
                JsonValue attr = getObjectAttribute(value, matchArray.group(1));
                int index = Integer.valueOf(matchArray.group(2));
                value = getArrayItem(attr, index);
            } else if (matchObject.matches()) {
                value = getObjectAttribute(value, path);
            } else {
                    throw new IllegalArgumentException("Malformed path: " + root);
            }
        }
        return value;
    }

    private JsonValue getObjectAttribute(JsonValue object, String attribute) {
        if (object == null) {
            return null;
        }
        if (object instanceof JsonObject) {
            return ((JsonObject)object).get(attribute);
        } else {
            throw new IllegalArgumentException("Given root expects Object, but it is not");
        }
    }

    // may throw IndexOutOfBoundsException
    private JsonValue getArrayItem(JsonValue object, int index) {
        if (object == null) {
            return null;
        }
        if (object instanceof JsonArray) {
            return ((JsonArray)object).get(index);
        } else {
            throw new IllegalArgumentException("Given root expects Array, but it is not");
        }
    }
}
