package com.acn.ebx;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.acn.ebx.utility.EBXProductConstants;
import com.acn.ebx.utility.PropertyMapper;
import com.acn.ebx.utility.UtilityFunctions;

public class Demo {

	static PropertyMapper propertyMapper = new PropertyMapper();
	static UtilityFunctions utility = new UtilityFunctions();

	public static void main(String[] args) {

		String folderLocation = "";
		String fileName = "";
		FileInputStream file = null;
		XSSFWorkbook workbook = null;
		XSSFSheet sheet = null;
		List<String> listOfFieldsMappings = null;
		String country = "Italy";

		try {
			TreeMap<String, String> propertyMap = propertyMapper.getPropertyMap();
			folderLocation = propertyMap.get("MasterDataFolderLocation");

			if (EBXProductConstants.Spain.equalsIgnoreCase(country)) {
				fileName = folderLocation.concat(propertyMap.get("MasterDataFile"));
			} else if (EBXProductConstants.Italy.equalsIgnoreCase(country)) {
				fileName = folderLocation.concat(propertyMap.get("ItalyMasterDataFile"));
			}

			file = new FileInputStream(new File(fileName));
			workbook = new XSSFWorkbook(file);
			sheet = workbook.getSheetAt(0);

			if (EBXProductConstants.Spain.equalsIgnoreCase(country)) {
				listOfFieldsMappings = utility.getSpainLogicData(sheet, propertyMap);
			} else if (EBXProductConstants.Italy.equalsIgnoreCase(country)) {
				listOfFieldsMappings = getLogicData(sheet, propertyMap);
			}

			System.out.println(listOfFieldsMappings);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static List<String> getLogicData(XSSFSheet sheet, TreeMap<String, String> propertyMap) {
		String objectName = null;
		String descriptionValue = null;
		String ruleValue = null;

		String iqviaTable = null;
		String iqviaField = null;
		Cell cell = null;
		Row row = null;
		StringBuffer logicData = new StringBuffer();
		boolean isRowEmpty = false;
		CellStyle style = null;
		Font cellFont = null;
		String[] tempArr = null;
		String[] iqviaFieldTempArr = null;
		boolean iqviaTableContainsNextRow = false;
		boolean iqviaFieldContainsNextRow = false;
		List<String> iqviaTableNameList = new ArrayList<>();

		List<String> listOfCodes = new ArrayList<String>();

		try {
			for (int rowIndex = 3; rowIndex <= sheet.getLastRowNum(); rowIndex++) {

				logicData = new StringBuffer();
				row = sheet.getRow(rowIndex);
				isRowEmpty = isEmptyRow(row);

				if (!isRowEmpty) {
					cell = row.getCell(Integer.parseInt(propertyMap.get("ItalyObjectColumnIndex")));

					if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {
						if (cell.getCellType() == Cell.CELL_TYPE_STRING) {
							objectName = cell.getStringCellValue();

						} else if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
							objectName = String.valueOf(cell.getNumericCellValue());

						} else {
							objectName = "NULL";
						}

						logicData.append(objectName);

						cell = row.getCell(Integer.parseInt(propertyMap.get("ItalyDescriptionIndex")));

						if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {

							style = cell.getCellStyle();
							cellFont = cell.getSheet().getWorkbook().getFontAt(style.getFontIndex());
							if (cellFont.getStrikeout()) {
								logicData = null;
								continue;
							}

							if (cell.getCellType() == Cell.CELL_TYPE_STRING) {
								descriptionValue = cell.getStringCellValue();

							} else if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
								descriptionValue = String.valueOf(cell.getNumericCellValue());

							}

						}

						else {
							descriptionValue = "NULL";
						}

						if (descriptionValue == null) {
							continue;
						}

						logicData.append("~~");
						logicData.append(descriptionValue);

						cell = row.getCell(Integer.parseInt(propertyMap.get("ItalyRulesIndex")));
						if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {
							ruleValue = cell.getStringCellValue();

						}

						else {
							ruleValue = "NULL";
						}
						// logicData.append("~~");
						// logicData.append(ruleValue);

						cell = row.getCell(Integer.parseInt(propertyMap.get("ItalyIqviaTableIndex").toString()));

						if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {
							if (cell.getCellType() == Cell.CELL_TYPE_STRING) {
								iqviaTable = cell.getStringCellValue();

							}
							if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
								iqviaTable = String.valueOf(cell.getNumericCellValue());
							}

							if (iqviaTable != null && iqviaTable.contains("\n")) {

								iqviaTableContainsNextRow = true;

							} else {
								logicData.append("~~");
								logicData.append(iqviaTable);
							}

						} else {
							iqviaTable = "NULL";
							logicData.append("~~");
							logicData.append(iqviaTable);
						}

						cell = row.getCell(Integer.parseInt(propertyMap.get("ItalyIqviaFieldIndex").toString()));
						if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {

							if (cell.getCellType() == Cell.CELL_TYPE_STRING) {
								iqviaField = cell.getStringCellValue();

							}
							if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
								iqviaField = String.valueOf(cell.getNumericCellValue());

							}

							if (iqviaField != null && iqviaField.contains("\n")) {

								iqviaFieldContainsNextRow = true;
							} else {
								logicData.append("~~");
								logicData.append(iqviaField);
							}

						} else {
							iqviaField = "NULL";
							logicData.append("~~");
							logicData.append(iqviaField);
						}

					}

				}

				if (iqviaTableContainsNextRow || iqviaFieldContainsNextRow) {

					if (iqviaTableContainsNextRow) {

						tempArr = iqviaTable.split("\n");
						iqviaTableNameList = new ArrayList<>();

						for (int i = 0; i < tempArr.length; i++) {

							if (tempArr[i] != null && tempArr[i] != "") {
								iqviaTableNameList.add(tempArr[i]);
							}
						}

					}

					// System.out.println(veevaFieldAPINameList);
					if (iqviaFieldContainsNextRow) {

						iqviaFieldTempArr = Arrays.stream(iqviaField.split("\n")).filter(x -> !x.isEmpty())
								.toArray(String[]::new);
					}

					for (int i = 0; i < iqviaTableNameList.size(); i++) {

						logicData = new StringBuffer();
						logicData.append(objectName);
						logicData.append("~~");
						logicData.append(descriptionValue);
						// logicData.append("~~");
						// logicData.append(ruleValue);
						logicData.append("~~");
						logicData.append(iqviaTableNameList.get(i));
						logicData.append("~~");

						if (iqviaFieldContainsNextRow && iqviaFieldTempArr != null) {

							if (iqviaFieldTempArr.length == iqviaTableNameList.size()) {
								logicData.append(iqviaFieldTempArr[i]);
							} else {
								logicData.append(iqviaField);
							}

						} else {
							logicData.append(iqviaField);
						}

						listOfCodes.add(logicData.toString());
					}

				} else {
					listOfCodes.add(logicData.toString());
				}

				iqviaTableContainsNextRow = false;
				iqviaFieldContainsNextRow = false;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		for (String str : listOfCodes) {
			System.out.println(str);
		}

		return listOfCodes;
	}

	private static boolean isEmptyRow(Row row) {
		boolean isEmptyRow = true;
		if (row != null) {
			for (int cellNum = row.getFirstCellNum(); cellNum < row.getLastCellNum(); cellNum++) {
				Cell cell = row.getCell(cellNum);
				if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK
						&& StringUtils.isNotBlank(cell.toString())) {
					isEmptyRow = false;
				}
			}
		}

		return isEmptyRow;
	}

	public static void main1(String[] args) {

		String fileName = "";
		Writer csvWriter = null;
		String fileHeader = "\"Id\",\"NVS_CORE_Novartis_Unique_ID__c\",\"IsPersonAccount\",\"NVS_CORE_STATUS__c\",\"xR1_Account_Status__c\",\"xR1_Data_Provider__c\",\"CreatedDate\",\"Salutation\",\"FirstName\",\"LastName\",\"Gender_vod__c\",\"Account_Type__c\",\"Name\",\"Beds__c\",\"NVS_CORE_Account_Subtype__c\",\"xR1_Account_SubType__c\",\"NVS_CORE_Account_Category__c\",\"xR31_Dependency__c\",\"NVS_CORE_Key_Account__c\",\"Phone\",\"PersonEmail\",\"NVS_CORE_Secondary_Email__c\",\"xR1_Organization_Email__c\",\"PersonMobilePhone\",\"NVS_CORE_HCO_Email__c\",\"NVS_CORE_National_Code__c\",\"xR3_ESP_National_Code__c\",\"xR1_Licence_Number__c\",\"NPI_vod__c\",\"NVS_CORE_Council_HCP_ID__c\",\"Specialty_1_vod__c\",\"Country_vod__c\",\"Country_vod__r.Name\",\"xR1_Country__c\",\"RecordTypeId\",\"RecordType.Name\",\"Primary_Parent_vod__c\",\"Primary_Parent_vod__r.Name\"";
		String data1 = "\"1010B00001jPQaOQAW\",\"ES-a1m0B000006TUMCQA4\",\"TRUE\",\"Active\",\"Active\",\"FALSE\",\"2016-08-29T12:03:19.000Z\",\"Mrs.\",\"Abc\",\"Def\",\"F\",\"Doctor\",\"Abc def\",\"\",\"CORE2\",\"CORE 1\",\"\",\"\",\"FALSE\",\"915504800\",\"sample@hotmail.com\",\"\",\"\",\"\",\"\",\"7897878768\",\"2354545\",\"282871046\",\"72751224Q\",\"282871046\",\"NEUMOLOGIA\",\"a49U0000000PFAXIA4\",\"ES\",\"United Arab Emirates\",\"1120B000000kLJyQAM\",\"HCP\",\"101U000000sw4FyIAI\",\"Neumologia - Hospital Universitario Fundacion Jimenez Diaz-UTE\"";
		String data2 = "\"1010B00001jPQaPQAW\",\"ES-a1m0B000006TUQKQA4\",\"TRUE\",\"Active\",\"Active\",\"FALSE\",\"2016-08-29T12:03:19.000Z\",\"Dr.\",\"Mno\",\"Pqr\",\"M\",\"Doctor\",\"Mno Pqr\",\"\",\"\",\"\",\"\",\"\",\"FALSE\",\"965835011\",\"sample@hotmail.com\",\"\",\"\",\"\",\"\",\"\",\"\",\"32910066\",\"54487587N\",\"32910066\",\"MEDICINA DE FAMILIA\",\"a49U0000000PFAXIA4\",\"ES\",\"Spain\",\"1120B000000kLJyQAM\",\"HCP\",\"101U000000sw0gFIAQ\",\"Centro Salud Calpe\"";
		String data3 = "\"1010B00001jPQvAQAW\",\"\",\"false\",\"\",\"Inactive\",\"false\",\"2016-08-29T14:08:58.000Z\",\"\",\"\",\"\",\"\",\"Hospital\",\"Hospital Nuestra Señora de las Nieves\",\"\",\"\",\"S. Cardiología\",\"\",\"Público\",\"false\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"Spain\",\"012U0000000UciqIAC\",\"Hospital\",\"\",\"\"";
		String data4 = "\"1010B00001jPQwmQAG\",\"\",\"false\",\"\",\"Active\",\"false\",\"2016-08-29T14:16:13.000Z\",\"\",\"\",\"\",\"\",\"Servicio\",\"Cardiologia - Hospital General de la Palma\",\"\",\"\",\"S. Cardiología\",\"\",\"Público\",\"false\",\"922185000\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"Spain\",\"012U0000000UcirIAC\",\"Hospital Department\",\"001U000000sw4JFIAY\",\"Hospital General de la Palma\"";

		try {

			if (fileHeader != null) {
				Charset charset = Charset.forName("UTF-8");
				fileName = "AccountFile1" + ".csv";
				csvWriter = new BufferedWriter(new OutputStreamWriter(
						new FileOutputStream("C:\\EBX\\Novartis DeepBlue\\DeepBlue\\Source File(s)\\" + fileName),
						charset));

				csvWriter.append(fileHeader);
				csvWriter.append("\n");

				for (int j = 0; j < 1000000; j++) {
					System.out.println("Writing : " + j);
					csvWriter.append(data1);
					csvWriter.append("\n");
					csvWriter.append(data2);
					csvWriter.append("\n");
					csvWriter.append(data3);
					csvWriter.append("\n");
					csvWriter.append(data4);
					csvWriter.append("\n");

				}

				csvWriter.flush();
				csvWriter.close();

				System.out.println(fileName + " written successfully on disk.");

			}

		} catch (Exception e) {

			System.out.println("Exception Occurred in writeOutputCSVFile() . Exception Details: " + e.getMessage());
			e.printStackTrace();
		}

	}

	public static boolean containsIgnoreCase(String str, String subString) {
		return str.toLowerCase().contains(subString.toLowerCase());
	}

	public static List<HashMap<String, String>> readCSVFile(FileInputStream inputFile) {

		List<String> headerRowList = null;
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

	private static List<ArrayList<String>> processInputFile(FileInputStream inputFile) {
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

	private static Function<String, ArrayList<String>> mapToItem = (line) -> {
		String[] p = line.split(",");// a CSV has comma separated lines
		ArrayList<String> item = new ArrayList<String>();
		// String cellValue = "";
		for (int i = 0; i < p.length; i++) {
			// cellValue = p[i];
			// if (cellValue != null && cellValue != "" &&
			// cellValue.contains("\"")) {

			// cellValue = cellValue.replace("\"", "");

			// }
			item.add(p[i]);
		}
		return item;
	};

}
