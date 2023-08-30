package it.json2csv;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonString;
import javax.json.JsonValue;

public class Converter implements Runnable {

	Options options;
	int rc = 0;
	JsonUtil jsonUtil = new JsonUtil();

	public Converter(Options options) {
		this.options = options;
	}

	public int getRc() {
		return rc;
	}

	/**
	 * Entry point
	 */
	@Override
	public void run() {
		handleIsUnixOption();
	
		// Read all JSON files and transform into a single list of maps
		List<LinkedHashMap<String, Object>> dataAsListOfMaps = readData();

		// convert list of maps into list of arrays
		List<String> headers = new ArrayList<>();
		List<Object[]> dataAsListOfArrays = new ArrayList<>();
		arrangeData(dataAsListOfMaps, headers, dataAsListOfArrays);

		// print to CSV
		print(headers, dataAsListOfArrays);
	}

	void handleIsUnixOption() {
		if (options.isUnix()) {
			options.setFieldDelimiter(" ");
			options.setRecordDelimiter("\n");
			options.setQuote(null);
			options.setEscape(null);
			options.setSkipHeader(true);
		}
	}

	/**
	 * Read all input JSON, parse each of them, then generate a unique list of rows with all of them
	 * (columns may be different for different rows)
	 */
	List<LinkedHashMap<String, Object>> readData() {
		List<LinkedHashMap<String, Object>> dataAsListOfMaps = new ArrayList<>();
		for (String jsonFileName : options.getFiles()) {
			JsonValue data;
			try {
				data = jsonUtil.jsonFromFile(jsonFileName);
			} catch (FileNotFoundException e) {
				System.err.println("File does not exist: " + jsonFileName);
				rc = 1;
				continue;
			} catch (IOException e) {
				System.err.println("I/O Error while reading file " + jsonFileName + ": " + e.getMessage());
				rc = 2;
				continue;
			}
			List<LinkedHashMap<String, Object>> dataAsListOfMapsForFile = json2list(data);
			dataAsListOfMaps.addAll(dataAsListOfMapsForFile);
		}
		return dataAsListOfMaps;
	}

	/**
	 * Applied to root JSON structure, convert it into a tabellar structure.
	 * Output is a list of maps (column name => cell value), where the cell value can only be a basic datataype.
	 * Maps (i.e. "rows") are not required to have the same columns.
	 * 
	 * In the most common case, data is an array containing objects with the same structure;
	 * the resulting CSV will have at least one row per each of these objects. Maybe more,
	 * because of FULL JOIN among list attributes.
	 *  
	 * @param data
	 * @return
	 */
	List<LinkedHashMap<String,Object>> json2list(JsonValue data) {
		LinkedHashMap<String, Object> l = json2listNoJoin(data, "", 1);
		List<LinkedHashMap<String, Object>> l2 = fullJoin(l);
		return l2;
	}

	/**
	 * Similar to jsonList, however FULL JOIN is not performed.
	 * A single JsonObject is always mapped into a single LinkedHashMap.
	 * 
	 * @param data
	 * @param prefix
	 * @param depth
	 * @return
	 */
	LinkedHashMap<String,Object> json2listNoJoin(JsonValue data, String prefix, int depth) {
		LinkedHashMap<String,Object> targetMap = new LinkedHashMap<>();
		addValue(targetMap, "", data, prefix, depth);
		return targetMap;
	}

	/**
	 * Recursively transoform JsonValue into a LinkedHashMap. FULL JOIN is not performed.
	*/
	void addValue(LinkedHashMap<String, Object> targetMap, String key, JsonValue value, String prefix, int depth) {
		String nonObjectKey = ((prefix + key).isEmpty()) ? "value" : prefix + key;
		switch (value.getValueType()) {
			case NULL:
				targetMap.put(nonObjectKey, "");
				break;
			case TRUE:
				targetMap.put(nonObjectKey, true);
				break;
			case FALSE:
				targetMap.put(nonObjectKey, false);
				break;
			case STRING:
				targetMap.put(nonObjectKey, ((JsonString)value).getString());
				break;
			case NUMBER:
				double number = ((JsonNumber)value).doubleValue();
				targetMap.put(nonObjectKey, new NumberWrapper(number, options.getNumberFormat()));
				break;
			case OBJECT:
				// Subproperties are mapped as new columns in the same row
				if (options.getMaxDepth() == null || depth <= options.getMaxDepth()) {
					for (Map.Entry<String, JsonValue> entry : ((JsonObject)value).entrySet()) {
						String k = entry.getKey();
						JsonValue v = entry.getValue();
						String newPrefix = prefix + key;
						if (!newPrefix.isEmpty()) {
							newPrefix += options.getAttributeSeparator();
						}
						addValue(targetMap, k, v, newPrefix, depth + 1);
					}
				} else {
					targetMap.put(prefix + key, "[object]");
				}
				break;
			case ARRAY:
				// Array entries are put into a single List.
				if (options.getMaxDepth() == null || depth <= options.getMaxDepth()) {
					List<Map<String,Object>> list = new ArrayList<>();
					for (JsonValue elem : (JsonArray)value) {
						LinkedHashMap<String, Object> submap1 = json2listNoJoin((JsonValue)elem, prefix + key, depth + 1);
						list.add(submap1);
					}
					targetMap.put(prefix + key, list);
				} else {
					targetMap.put(prefix + key, "[array]");
				}
		}
	}

	/**
	 * Remove List's from row, eventually multiplying the row itself.
	 * @param map
	 * @return
	 */
	List<LinkedHashMap<String, Object>> fullJoin(LinkedHashMap<String, Object> map) {

		// This method must handle three cases:
		// 1. map does not contain any list attribute -> just return the map
		// 2. map contains one non empty lists -> perform full join on that list (then repeat recursively for other list attributes)
		// 3. map contains one empty list -> remove the attribute (then repeat recursively for other list attributes)
		List<LinkedHashMap<String, Object>> ret = new ArrayList<>();
		boolean foundList = false;
		for (Map.Entry<String,Object> attribute: map.entrySet()) {
			if (attribute.getValue() instanceof List) {
				List<?> listAttr = (List<?>) attribute.getValue();
				if (listAttr.isEmpty()) {
					map.remove(attribute.getKey());
				} else {
					for (Object x: listAttr) {
						LinkedHashMap<String, Object> copy = new LinkedHashMap<>(map);
						if (x instanceof Map) {
							// array of objects: attributes become new columns of same map
							copy.remove(attribute.getKey());
							copy.putAll((Map)x);
						} else {
							// any other type, including array
							copy.put(attribute.getKey(), x);
						}
						List<LinkedHashMap<String, Object>> parseNextLists = fullJoin(copy);
						ret.addAll(parseNextLists);
					}
					foundList = true;
					break;
				}
			}
		}
		if (!foundList) {
			ret.add(map);
		}
		return ret;
	}

	/**
	 * Arrange rows so that they all have same lenght and order of columns.
	 * We try to keep the original order of attributes, if possible.
	 * 
	 * @param dataAsListOfMaps input list of maps
	 * @param columns output list of column headers
	 * @param dataAsListOfArrays output list of arrays
	 */
	void arrangeData(List<LinkedHashMap<String, Object>> dataAsListOfMaps, List<String> columns,
			List<Object[]> dataAsListOfArrays) {

		// calculate columns
		// the LinkedHashSet should avoid duplicates, while preserving ordering
		LinkedHashSet<String> columnNames = new LinkedHashSet<>();
		for (Map<String, Object> map: dataAsListOfMaps) {
			columnNames.addAll(map.keySet());
		}
		columns.addAll(columnNames);

		// calculate reverse map of column indexes
		Map<String, Integer> columnIndexes = new HashMap<>(columns.size());
		for (int i = 0; i < columns.size(); ++i) {
			columnIndexes.put(columns.get(i), i);
		}

		// calculate output list
		for (Map<String, Object> map: dataAsListOfMaps) {
			Object[] row = new Object[columns.size()];
			for (Map.Entry<String,Object> attribute: map.entrySet()) {
				row[columnIndexes.get(attribute.getKey())] = attribute.getValue();
			}
			dataAsListOfArrays.add(row);
		}
	}

	void print(List<String> headers, List<Object[]> dataAsListOfArrays) {
		Printer printer;
		try {
			printer = new Printer(options);
			printer.printCSV(dataAsListOfArrays, headers);
		} catch (IOException e) {
			System.err.println("Error printing CSV: " + e.getMessage());
			rc = 33;
		}
	}
}
