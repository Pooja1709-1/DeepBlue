package com.acn.ebx.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;

public class CSVReaderService {

	public List<HashMap<String, String>> readCSVFile(String filePath) throws FileNotFoundException {

		List<String> headerRowList = null;
		FileInputStream inputFile = new FileInputStream(new File(filePath));
		List<ArrayList<String>> listOfList = processInputFile(inputFile);
		HashMap<String, String> inputMap = new HashMap<String, String>();
		List<HashMap<String, String>> inputMapList = new ArrayList<HashMap<String, String>>();
		List<String> innerList = null;
		String colName = "";
		List<String> columnHeaderList = new ArrayList<>();
		if (listOfList != null) {

			headerRowList = listOfList.get(0);

			if (headerRowList != null) {

				for (String header : headerRowList) {

					if (header != null && header != "") {

						columnHeaderList.add(header.toUpperCase().trim());
					} else {
						columnHeaderList.add(header);
					}
				}
			}

		}

		for (int i = 1; i < listOfList.size(); i++) {
			inputMap = new HashMap<String, String>();
			innerList = listOfList.get(i);
			for (int j = 0; j < innerList.size(); j++) {

				colName = columnHeaderList.get(j);
				inputMap.put(colName, innerList.get(j));

			}
			inputMapList.add(inputMap);

		}

		return inputMapList;
	}

	private List<ArrayList<String>> processInputFile(FileInputStream inputFile) {
		List<ArrayList<String>> inputList = new ArrayList<ArrayList<String>>();

		try {

			Charset charset = Charset.forName("UTF-8");
			BufferedReader br = new BufferedReader(new InputStreamReader(inputFile, charset));
			inputList = br.lines().map(mapToItem).collect(Collectors.toList());
			br.close();
		} catch (IOException e) {

			e.printStackTrace();
		}
		return inputList;
	}

	private Function<String, ArrayList<String>> mapToItem = (line) -> {
		String[] p = line.split("\",\"");// a CSV has comma separated lines
		ArrayList<String> item = new ArrayList<String>();
		String cellValue = "";
		for (int i = 0; i < p.length; i++) {
			cellValue = p[i];
			if (cellValue != null && cellValue != "" && cellValue.contains("\"")) {

				cellValue = cellValue.replace("\"", "");

			}
			item.add(cellValue);
		}
		p = null;
		return item;
	};

	public Map<Integer, Map<String, String>> readInputSourceData(String file) {

		Map<Integer, Map<String, String>> rowColumnNameValueMap = new LinkedHashMap<>();
		Map<String, String> columnNameValueMap = null;

		try {

			Charset charset = Charset.forName("UTF-8");
			long d1 = System.currentTimeMillis();
			// Create an object of file reader
			// class with CSV file as a parameter.
			// FileReader fileReader = new FileReader(file);

			BufferedReader fileReader = new BufferedReader(new InputStreamReader(new FileInputStream(file), charset));

			// create csvReader object and skip first Line
			CSVReader csvReader = new CSVReaderBuilder(fileReader).build();
			List<String[]> allData = csvReader.readAll();
			String[] headerRow = allData.get(0);
			// String[] headerRow = headerRowData[0].split(";");
			String[] rowData = null;
			// String rowsData[] = null;

			for (int i = 0; i < headerRow.length; i++) {
				headerRow[i] = headerRow[i].toUpperCase();
			}

			// print Data
			for (int rowIndex = 1; rowIndex < allData.size(); rowIndex++) {

				rowData = allData.get(rowIndex);
				// rowsData = rowData[0].split(";");
				// columnNameValueMap = new
				// TreeMap<>(String.CASE_INSENSITIVE_ORDER);
				columnNameValueMap = new LinkedHashMap<>();
				for (int cellIndex = 0; cellIndex < rowData.length; cellIndex++) {
					columnNameValueMap.put(headerRow[cellIndex], rowData[cellIndex]);
				}

				rowColumnNameValueMap.put(Integer.valueOf(rowIndex), columnNameValueMap);

			}

			long d2 = System.currentTimeMillis();
			long elapsed = d2 - d1;
			int hours = (int) Math.floor(elapsed / 3600000);
			int minutes = (int) Math.floor((elapsed - hours * 3600000) / 60000);
			int seconds = (int) Math.floor((elapsed - hours * 3600000 - minutes * 60000) / 1000);

			System.out.print("Time Taken to read source File : ");
			System.out.format("%d hours %d minutes %d seconds%n", hours, minutes, seconds);

		} catch (Exception e) {
			e.printStackTrace();
		}

		return rowColumnNameValueMap;
	}

}
