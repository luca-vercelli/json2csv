package it.json2csv;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonString;
import javax.json.JsonValue;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

public class Converter implements Runnable {

	Options options;
	int rc = 0;

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
		List<SortedMap<String, Object>> dataAsListOfMaps = readData();
		List<String> headers = new ArrayList<>();
		List<Object[]> dataAsListOfArrays = new ArrayList<>();
		arrangeData(dataAsListOfMaps, headers, dataAsListOfArrays);
		printCSV(dataAsListOfArrays, headers);
	}

	/**
	 * Read all input JSON, parse each of them, then generate a unique list of rows with all of them
	 * (columns may be different for different rows)
	 */
	List<SortedMap<String, Object>> readData() {
		List<SortedMap<String, Object>> dataAsListOfMaps = new ArrayList<>();
		for (String jsonFileName : options.getFiles()) {
			JsonValue data;
			try {
				data = jsonFromFile(jsonFileName);
			} catch (FileNotFoundException e) {
				System.err.println("File does not exist: " + jsonFileName);
				rc = 1;
				continue;
			} catch (IOException e) {
				System.err.println("I/O Error while reading file " + jsonFileName + ": " + e.getMessage());
				rc = 2;
				continue;
			}
			List<SortedMap<String, Object>> dataAsListOfMapsForFile = json2list(data);
			dataAsListOfMaps.addAll(dataAsListOfMapsForFile);
		}
		return dataAsListOfMaps;
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
	List<SortedMap<String,Object>> json2list(JsonValue data) {
		SortedMap<String, Object> l = json2listNoJoin(data, true, "", 1);
		List<SortedMap<String, Object>> l2 = fullJoin(l);
		return l2;
	}

	/**
	 * Similar to jsonList, however FULL JOIN is not performed.
	 * A single JsonObject is always mapped into a single SortedMap.
	 * 
	 * @param data
	 * @param root
	 * @param prefix
	 * @param depth
	 * @return
	 */
	SortedMap<String,Object> json2listNoJoin(JsonValue data, boolean root, String prefix, int depth) {
		SortedMap<String,Object> targetMap = new TreeMap<>();
		addValue(targetMap, "", data, prefix, depth);
		return targetMap;
	}

	/**
	 * Recursively transoform JsonValue into a SortedMap. FULL JOIN is not performed.
	*/
	private void addValue(SortedMap<String, Object> targetMap, String key, JsonValue value, String prefix, int depth) {
		switch (value.getValueType()) {
			case NULL:
				targetMap.put(prefix + key, "");
				break;
			case TRUE:
				targetMap.put(prefix + key, true);
				break;
			case FALSE:
				targetMap.put(prefix + key, false);
				break;
			case STRING:
				targetMap.put(prefix + key, ((JsonString)value).getString());
				break;
			case NUMBER:
				targetMap.put(prefix + key, ((JsonNumber)value).bigDecimalValue());
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
						SortedMap<String, Object> submap1 = json2listNoJoin((JsonValue)elem, false, prefix + key, depth + 1);
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
	List<SortedMap<String, Object>> fullJoin(SortedMap<String, Object> map) {
		List<SortedMap<String, Object>> ret = new ArrayList<>();
		boolean foundList = false;
		for (Map.Entry<String,Object> attribute: map.entrySet()) {
			if (attribute.getValue() instanceof List) {
				List<?> listAttr = (List<?>) attribute.getValue();
				if (listAttr.isEmpty()) {
					map.put(attribute.getKey(), ""); // replace empty array with ""
				} else {
					for (Object x: listAttr) {
						SortedMap<String, Object> copy = new TreeMap<>(map);
						if (x instanceof Map) {
							// array of objects: attributes become new columns of same map
							copy.remove(attribute.getKey());
							copy.putAll((Map)x);
						} else {
							// any other type, including array
							copy.put(attribute.getKey(), x);
						}
						List<SortedMap<String, Object>> parseNextLists = fullJoin(copy);
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
	void arrangeData(List<SortedMap<String, Object>> dataAsListOfMaps, List<String> columns,
			List<Object[]> dataAsListOfArrays) {

		// calculate columns
		// the TreeSet should avoid duplicates, while preserving ordering
		TreeSet<String> columnNames = new TreeSet<>();
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

	void printCSV(List<Object[]> dataAsListOfArrays, List<String> headers) {
		try (CSVPrinter printer = createCsvPrinter()) {
			printer.printRecord(headers.toArray());
			for (Object[] row: dataAsListOfArrays) {
				printer.printRecord(row);
			}
		} catch (IOException e1) {
			System.err.println("Exception writing CSV file :" + e1.getMessage());
			this.rc = 10;
		}
	}
}
