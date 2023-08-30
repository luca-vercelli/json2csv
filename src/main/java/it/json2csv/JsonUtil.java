package it.json2csv;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
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

    /**
     * Estract subtree from given JSON.
     *
     * Please notiche that really <i>any</i> character can be used as key in a JSON.
     * 
     * @param value input JSON
     * @param rootPath path inside JSON, in the form
     * "value/data/4/root/"reàlly&anything:including"quotes//slashes.dots/"
     * Use "//" to denote a single slash character instead of a separator
     */
    public JsonValue getRoot(JsonValue value, String rootPath) {
        if (value == null) {
            return null;
        }
        List<String> ids = extractIdentifiers(rootPath);
        for (String id: ids) {
            if (value instanceof JsonObject) {
                value = ((JsonObject)value).get(id);
            } else if (value instanceof JsonArray) {
                int index = Integer.valueOf(id); // may throw NumberFormatException
                value = ((JsonArray)value).get(index); // may throw IndexOutOfBoundsException
            } else {
                throw new IllegalArgumentException("rootPath " + rootPath + " expects Object or Array");
            }
            if (value == null) {
                break;
            }
        }
        return value;
    }

    List<String> extractIdentifiers(String path) {
        int lastIndex = 0;
        List<String> pieces = new ArrayList<>();
        for (int i = 0; i < path.length(); ++i) {
            if (path.charAt(i) == '/') {
                if (path.length() == i + 1 || path.charAt(i + 1) != '/') {
                    String piece = path.substring(lastIndex, i);
                    pieces.add(piece.replaceAll("//", "/"));
                    lastIndex = i + 1;
                } else if (path.charAt(i + 1) == '/') {
                    ++i;
                }
            } else if (i == path.length() - 1) {
                String piece = path.substring(lastIndex);
                pieces.add(piece.replaceAll("//", "/"));
            }
        }
        return pieces;
    }

    /**
     * Remove subtree from given JSON.
     *
     * Please notiche that really <i>any</i> character can be used as key in a JSON.
     * 
     * @param value input JSON (please notice JsonValue's are immutable)
     * @param rootPath path inside JSON, in the form
     * "value/data/4/root/"reàlly&anything:including"quotes//slashes.dots/"
     * Use "//" to denote a single slash character instead of a separator
     * @return modified copy of input
     */
    public JsonValue removeNode(JsonValue value, String nodePath) {
        if (value == null) {
            return null;
        }
        List<String> ids = extractIdentifiers(nodePath);
        return removeNodeRecursive(value, ids);
    }

    protected JsonValue removeNodeRecursive(JsonValue value, List<String> nodePath) {
        if (nodePath.size() < 1) {
            // quite uncommon
            return value;
        }
        String firstPath = nodePath.get(0);
        if (value instanceof JsonObject) {
            JsonObjectBuilder builder = Json.createObjectBuilder();
            for (Entry<String, JsonValue> entry:((JsonObject)value).entrySet()) {
                if (!entry.getKey().equals(firstPath)) {
                    builder.add(entry.getKey(), entry.getValue());
                } else if (nodePath.size() > 1) {
                    List<String> morePaths = new LinkedList<>(nodePath);
                    morePaths.remove(0);
                    builder.add(entry.getKey(), removeNodeRecursive(entry.getValue(), morePaths));
                }
            }
            return builder.build();
        } else if (value instanceof JsonArray) {
            int index = Integer.valueOf(nodePath.get(0));
            JsonArrayBuilder builder = Json.createArrayBuilder();
            for (int i = 0; i < ((JsonArray)value).size(); ++i) {
                if (i != index) {
                    builder.add(((JsonArray)value).get(i));
                } else if (nodePath.size() > 1) {
                    List<String> morePaths = new LinkedList<>(nodePath);
                    morePaths.remove(0);
                    builder.add(removeNodeRecursive(((JsonArray)value).get(i), morePaths));
                }
            }
            return builder.build();
        } else {
            throw new IllegalArgumentException("nodePath " + nodePath + " expects Object or Array");
        }
    }
}
