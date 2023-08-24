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
		List<String> columns = new ArrayList<>();
		List<Object[]> dataAsListOfArrays = new ArrayList<>();
		arrangeData(dataAsListOfMaps, columns, dataAsListOfArrays);
		printCSV(dataAsListOfArrays);
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
		return json2list(data, true, "");
	}

	List<SortedMap<String,Object>> json2list(JsonValue data, boolean root, String prefix) {
		String doublePrefix = root ? "value" : "";
	 
		// if object is not an array, the resulting Excel could have a single line
		List<SortedMap<String,Object>> ret = new ArrayList<>();
		switch (data.getValueType()) {
			case ARRAY:
				for (JsonValue elem : (JsonArray)data) {
					ret.addAll(json2list(elem));
				}
				break;
			case NULL:
				ret.add(sortedMapOf(prefix + doublePrefix, "")); // null as an empy line, ok ?
			case TRUE:
				ret.add(sortedMapOf(prefix + doublePrefix, true));
				break;
			case FALSE:
				ret.add(sortedMapOf(prefix + doublePrefix, false));
				break;
			case STRING:
				ret.add(sortedMapOf(prefix + doublePrefix, ((JsonString)data).getString()));
				break;
			case NUMBER:
				ret.add(sortedMapOf(prefix + doublePrefix, ((JsonNumber)data).bigDecimalValue()));
				break;
			case OBJECT:
				// This is the interesting case
				SortedMap<String, Object> map = mapObjectS1((JsonObject)data, prefix);
				List<SortedMap<String, Object>> map2 = fullJoin(map);
				ret.addAll(map2);
		}
		return ret;
	}

	// Similar to Map.of, returning a SortedMap
	SortedMap<String, Object> sortedMapOf(String key, Object value) {
		TreeMap<String, Object> map = new TreeMap<>();
		map.put(key, value);
		return map;
	}

	SortedMap<String, Object> mapObjectS1(JsonObject data, String prefix) {
		SortedMap<String, Object> ret = new TreeMap<>(); // keep ordering
		for (Map.Entry<String, JsonValue> entry : data.entrySet()) {
			String key = entry.getKey();
			JsonValue v = entry.getValue();
			switch (v.getValueType()) {
				case NULL:
					ret.put(prefix + key, "");
					break;
				case TRUE:
					ret.put(prefix + key, true);
					break;
				case FALSE:
					ret.put(prefix + key, false);
					break;
				case STRING:
					ret.put(prefix + key, ((JsonString)v).getString());
					break;
				case NUMBER:
					ret.put(prefix + key, ((JsonNumber)v).bigDecimalValue());
					break;
				case OBJECT:
					// Subroperties are mapped as new columns in the same row
					Map<String, Object> submap = mapObjectS1((JsonObject)v, prefix + key + options.getAttributeSeparator());
					ret.putAll(submap);
					break;
				case ARRAY:
					// So far, we map arrays to List's.
					List<Map<String,Object>> list = new ArrayList<>();
					for (JsonValue elem : (JsonArray)v) {
						List<SortedMap<String, Object>> submap1 = json2list((JsonValue)elem, false, prefix + key);
						list.addAll(submap1);
					}
					ret.put(prefix + key, list);
			}
		}
		return ret;
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

	void printCSV(List<Object[]> dataAsListOfArrays) {
		try (CSVPrinter printer = createCsvPrinter()) {
			for (Object[] row: dataAsListOfArrays) {
				printer.printRecord(row);
			}
		} catch (IOException e1) {
			System.err.println("Exception writing CSV file :" + e1.getMessage());
			this.rc = 10;
		}
	}
}
