package com.acn.ebx.utility;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;

public class UtilityFunctions {

	public List<String> getSpainLogicData(XSSFSheet sheet, TreeMap<String, String> propertyMap) {
		String objectName = null;
		String descriptionValue = null;
		String ruleValue = null;

		String veevaTable = null;
		String veevaFieldName = null;
		String veevaFieldApiName = null;
		String legacyVeevaFieldName = null;
		Cell cell = null;
		Row row = null;
		StringBuffer logicData = new StringBuffer();
		boolean isRowEmpty = false;
		CellStyle style = null;
		Font cellFont = null;
		// String[] tempArr = null;
		// String[] legacyTempArr = null;
		// boolean containsNextRow = false;
		// boolean legacyContainsNextRow = false;
		// List<String> veevaFieldAPINameList = new ArrayList<>();

		List<String> listOfCodes = new ArrayList<String>();
		for (int rowIndex = 3; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
			// mdmCodesMap = new HashMap<String, List<Map<String, String>>>();
			logicData = new StringBuffer();
			row = sheet.getRow(rowIndex);
			isRowEmpty = isEmptyRow(row);

			if (!isRowEmpty) {
				cell = row.getCell(Integer.parseInt(propertyMap.get("ObjectColumnIndex")));

				if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {
					if (cell.getCellType() == Cell.CELL_TYPE_STRING) {
						objectName = cell.getStringCellValue();

					} else if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
						objectName = String.valueOf(cell.getNumericCellValue());

					} else {
						objectName = "NULL";
					}

					logicData.append(objectName);

					cell = row.getCell(Integer.parseInt(propertyMap.get("DescriptionIndex")));

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

					cell = row.getCell(Integer.parseInt(propertyMap.get("RulesIndex")));
					if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {
						ruleValue = cell.getStringCellValue();

					}

					else {
						ruleValue = "NULL";
					}
					logicData.append("~~");
					logicData.append(ruleValue);

					cell = row.getCell(Integer.parseInt(propertyMap.get("VeevaTableIndex").toString()));

					if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {
						if (cell.getCellType() == Cell.CELL_TYPE_STRING) {
							veevaTable = cell.getStringCellValue();

						}
						if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
							veevaTable = String.valueOf(cell.getNumericCellValue());
						}

					} else {
						veevaTable = "NULL";
					}

					logicData.append("~~");

					logicData.append(veevaTable);

					cell = row.getCell(Integer.parseInt(propertyMap.get("VeevaFieldIndex").toString()));
					if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {
						if (cell.getCellType() == Cell.CELL_TYPE_STRING) {
							veevaFieldName = cell.getStringCellValue();

						}
						if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
							veevaFieldName = String.valueOf(cell.getNumericCellValue());
						}

					} else {
						veevaFieldName = "NULL";
					}

					logicData.append("~~");

					logicData.append(veevaFieldName);
					cell = row.getCell(Integer.parseInt(propertyMap.get("VeevaFeildApiIndex").toString()));
					if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {

						if (cell.getCellType() == Cell.CELL_TYPE_STRING) {
							veevaFieldApiName = cell.getStringCellValue();

						}
						if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
							veevaFieldApiName = String.valueOf(cell.getNumericCellValue());

						}

						// if (!"Address".equalsIgnoreCase(objectName) &&
						// veevaFieldApiName != null
						// && veevaFieldApiName.contains("\n")) {
						//
						// containsNextRow = true;
						//
						// }

					} else {
						veevaFieldApiName = "NULL";
					}
					logicData.append("~~");

					logicData.append(veevaFieldApiName);

					cell = row.getCell(Integer.parseInt(propertyMap.get("LegacyVeevaFeildApiIndex").toString()));
					if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {
						if (cell.getCellType() == Cell.CELL_TYPE_STRING) {
							legacyVeevaFieldName = cell.getStringCellValue();

						}
						if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
							legacyVeevaFieldName = String.valueOf(cell.getNumericCellValue());

						}

					} else {
						legacyVeevaFieldName = "NULL";
					}
					logicData.append("~~");

					logicData.append(legacyVeevaFieldName);

				}

			}

			// if (containsNextRow) {
			//
			// tempArr = veevaFieldApiName.split("\n");
			// veevaFieldAPINameList = new ArrayList<>();
			// for (int i = 0; i < tempArr.length; i++) {
			//
			// if (tempArr[i] != null && tempArr[i] != "" &&
			// !tempArr[i].isEmpty()) {
			//
			// if (!tempArr[i].contains("below attributes:")) {
			// veevaFieldAPINameList.add(tempArr[i].replace("-", "").replace("
			// ", ""));
			// }
			//
			// }
			// }

			// System.out.println(veevaFieldAPINameList);
			// if (legacyVeevaFieldName != null &&
			// legacyVeevaFieldName.contains("\n")) {
			//
			// legacyContainsNextRow = true;
			// legacyTempArr =
			// Arrays.stream(legacyVeevaFieldName.split("\n")).filter(x ->
			// !x.isEmpty())
			// .toArray(String[]::new);
			// }
			//
			// for (int i = 0; i < veevaFieldAPINameList.size(); i++) {
			//
			// logicData = new StringBuffer();
			// logicData.append(objectName);
			// logicData.append("~~");
			// logicData.append(descriptionValue);
			// logicData.append("~~");
			// logicData.append(ruleValue);
			// logicData.append("~~");
			// logicData.append(veevaTable);
			//
			// logicData.append("~~");
			// logicData.append(veevaFieldName);
			// logicData.append("~~");
			// logicData.append(veevaFieldAPINameList.get(i));
			// logicData.append("~~");
			//
			// if (legacyContainsNextRow) {
			//
			// if (i == 0) {
			// logicData.append(legacyTempArr[0]);
			// } else if (i == 2) {
			// logicData.append(legacyTempArr[1]);
			// } else {
			// logicData.append("NULL");
			// }
			//
			// } else {
			//
			// if (objectName.equalsIgnoreCase("Communication (HCO)")
			// && descriptionValue.equalsIgnoreCase("Value")) {
			//
			// if (i == 2) {
			// logicData.append(legacyVeevaFieldName);
			// } else {
			// logicData.append("NULL");
			// }
			// } else {
			// logicData.append(legacyVeevaFieldName);
			// }
			//
			// }
			//
			// listOfCodes.add(logicData.toString());
			// // System.out.println(logicData.toString());
			// }
			//
			// } else {
			// listOfCodes.add(logicData.toString());
			// }

			listOfCodes.add(logicData.toString());
			// containsNextRow = false;
			// legacyContainsNextRow = false;
		}
		// System.out.println(listOfCodes);
		return listOfCodes;
	}

	public List<String> getItalyLogicData(XSSFSheet sheet, TreeMap<String, String> propertyMap) {
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
				// mdmCodesMap = new HashMap<String, List<Map<String,
				// String>>>();
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
						logicData.append("~~");
						logicData.append(ruleValue);

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
						logicData.append("~~");
						logicData.append(ruleValue);
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

		return listOfCodes;
	}

	private boolean isEmptyRow(Row row) {
		boolean isEmptyRow = true;
		if (row != null) {
			for (int cellNum = row.getFirstCellNum(); cellNum < row.getLastCellNum(); cellNum++) {
				Cell cell = row.getCell(cellNum);
				if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK
						&& StringUtils.isNotBlank(cell.toString())) {
					isEmptyRow = false;
					break;
				}
			}
		}

		return isEmptyRow;
	}

	public void readingMap(List<String> data) {
		// MasterSheetReaderService srService= new MasterSheetReaderService();

		for (String dataMap : data) {

			// System.out.println(dataMap);

			String a[] = dataMap.split("~~");
			if (a != null && a.length > 2) {
				String ruleString = a[2];
				if (ruleString != null) {
					if (ruleString.contains("MasterData Id") || ruleString.contains("MasterDataId")) {

					}

				}

			}

		}

	}

	public String extractNumber(final String str) {

		String digit = "";
		if (str == null || str.isEmpty())
			return "";

		Pattern p = Pattern.compile("\\d+");
		Matcher m = p.matcher(str);
		while (m.find()) {
			if (m.group().length() > 4) {
				digit = m.group();
			}
		}

		return digit;
	}

	@SuppressWarnings("unchecked")
	public String getMasterDataCode(Map<String, String> mapOfCommonCodes, String masterDataId,
			Map<String, List<Map<String, String>>> mapOfCode, String colName, String cellValue) {

		String code = "";
		String keytoBeChecked = null;

		if (masterDataId != null && mapOfCode != null && !mapOfCode.isEmpty()) {

			String commonMasterDataDesc = mapOfCommonCodes.get(masterDataId);

			if (commonMasterDataDesc != null) {
				keytoBeChecked = commonMasterDataDesc.concat(" - Spain");
				for (Map.Entry<String, String> entry : mapOfCommonCodes.entrySet()) {

					if (entry.getValue().contains(keytoBeChecked)) {
						masterDataId = entry.getKey();
					}
				}
			}

			List<Map<String, String>> masterIdCodeMapList = mapOfCode.get(masterDataId);

			if (masterIdCodeMapList != null) {

				for (Map<String, String> codeDesc : masterIdCodeMapList) {

					if (EBXProductConstants.StatusCode.equalsIgnoreCase(colName)) {
						code = codeDesc.get("Active");
						break;
					} else if (EBXProductConstants.ValidationStatus.equalsIgnoreCase(colName)) {
						code = codeDesc.get("Validated");
						break;
					} else if (EBXProductConstants.TypeCode.equalsIgnoreCase(colName)) {

						if (cellValue != null && cellValue.equalsIgnoreCase("FALSE")
								&& codeDesc.containsKey("Health Care Organization")) {
							code = codeDesc.get("Health Care Organization");
							break;

						} else if (cellValue != null && cellValue.equalsIgnoreCase("TRUE")
								&& codeDesc.containsKey("Health Care Professional")) {
							code = codeDesc.get("Health Care Professional");
							break;

						} else if (cellValue != null && codeDesc.containsKey(cellValue)) {
							code = codeDesc.get(cellValue);
							break;
						}

					} else if ((EBXProductConstants.CustomerCountryCode.equalsIgnoreCase(colName)
							|| EBXProductConstants.countryCode.equalsIgnoreCase(colName)
							|| EBXProductConstants.Customer_Country_Code.equalsIgnoreCase(colName))
							&& codeDesc.containsKey("Spain")) {
						code = codeDesc.get("Spain");
						break;
					} else if (EBXProductConstants.AddressStatusCode.equalsIgnoreCase(colName)) {
						if (cellValue != null && cellValue.equalsIgnoreCase("FALSE")
								&& codeDesc.containsKey("Active")) {
							code = codeDesc.get("Active");
							break;

						} else if (cellValue != null && cellValue.equalsIgnoreCase("TRUE")
								&& codeDesc.containsKey("In-Active")) {
							code = codeDesc.get("In-Active");
							break;

						}
					} else if (EBXProductConstants.SystemCode.equalsIgnoreCase(colName)
							&& codeDesc.containsKey("MDM")) {
						code = codeDesc.get("MDM");
						break;
					} else if (EBXProductConstants.Communication_Type_Code.equalsIgnoreCase(colName)
							&& codeDesc.containsKey("Communication Channel")) {
						code = codeDesc.get("Communication Channel");
						break;
					} else {

						if (cellValue != null && codeDesc.containsKey(cellValue)) {
							code = codeDesc.get(cellValue);
							break;
						}

					}

				}

			}

		}
		return code;
	}

	public Map<String, String> getMasterSheet(XSSFSheet sheet, TreeMap<String, String> propertyMap) {
		Cell cell = null;
		Row row = null;
		Map<String, String> mdmCommonCodesMap = null;

		String mdmID = "";
		String mdmCodeDesc = "";

		boolean isRowEmpty = false;

		mdmCommonCodesMap = new HashMap<String, String>();
		for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {

			row = sheet.getRow(rowIndex);
			isRowEmpty = isEmptyRow(row);

			if (!isRowEmpty) {
				cell = row.getCell(Integer.parseInt(propertyMap.get("Master_Data_Id")));

				if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {
					if (cell.getCellType() == Cell.CELL_TYPE_STRING) {
						mdmID = cell.getStringCellValue();

					} else if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
						mdmID = "" + (int) cell.getNumericCellValue();
					}
				}

				cell = row.getCell(Integer.parseInt(propertyMap.get("Master_Data_Desc")));

				if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {
					if (cell.getCellType() == Cell.CELL_TYPE_STRING) {
						mdmCodeDesc = cell.getStringCellValue();

					}

				}
				mdmCommonCodesMap.put(mdmID, mdmCodeDesc);
			}

		}
		return mdmCommonCodesMap;
	}

	public HashMap<String, String> readOutputFile(String inputFilePath, String outputFileName) {
		List<ArrayList<String>> inputList = new ArrayList<ArrayList<String>>();
		HashMap<String, String> inputMap = new HashMap<String, String>();
		try {

			Charset charset = Charset.forName("UTF-8");
			File inputF = new File(inputFilePath);
			InputStream inputFS = new FileInputStream(inputF);
			BufferedReader br = new BufferedReader(new InputStreamReader(inputFS, charset));
			// skip the header of the csv
			inputList = br.lines().skip(1).map(mapToItem).collect(Collectors.toList());

			if (EBXProductConstants.RemoteKeys.equalsIgnoreCase(outputFileName)) {
				for (List<String> list : inputList) {
					inputMap.put(list.get(2), list.get(0));
				}
			} else if (EBXProductConstants.Customer.equalsIgnoreCase(outputFileName)) {
				for (List<String> list : inputList) {
					inputMap.put(list.get(0), list.get(1));
				}
			}

			br.close();
		} catch (Exception e) {

		}
		return inputMap;
	}

	private Function<String, ArrayList<String>> mapToItem = (line) -> {
		String[] p = line.split(";");// a CSV has comma separated lines
		ArrayList<String> item = new ArrayList<String>();

		for (int i = 0; i < p.length; i++) {
			item.add(p[i]);
		}

		return item;
	};

	public HashMap<String, List<String>> getColumnNamesList(FileInputStream inputFile) {

		List<String> colNamesList = new ArrayList<String>();
		HashMap<String, List<String>> targetColNameListMap = new HashMap<String, List<String>>();
		try {

			Charset charset = Charset.forName("UTF-8");
			BufferedReader br = new BufferedReader(new InputStreamReader(inputFile, charset));
			String header = "";
			int counter = 0;
			String rowNum = "row";
			while ((header = br.readLine()) != null) {
				rowNum = "row";
				colNamesList = new ArrayList<String>();
				String[] columns = header.split(",");

				for (int i = 0; i < columns.length; i++) {
					// colNamesList.add("\"" + columns[i] + "\"");
					colNamesList.add(columns[i]);
				}
				rowNum = rowNum + counter;
				targetColNameListMap.put(rowNum, colNamesList);
				counter++;
			}
			br.close();
		} catch (Exception e) {
			System.out.println("Exception Occurred. Exception Details: " + e.getMessage());
		}

		return targetColNameListMap;

	}

	public List<String> getTemplateColumnNamesList(FileInputStream inputFile) {

		List<String> colNamesList = new ArrayList<String>();

		try {

			Charset charset = Charset.forName("UTF-8");
			BufferedReader br = new BufferedReader(new InputStreamReader(inputFile, charset));
			String header = "";

			while ((header = br.readLine()) != null) {
				colNamesList = new ArrayList<String>();
				String[] columns = header.split(",");

				for (int i = 0; i < columns.length; i++) {
					colNamesList.add(columns[i]);
				}

			}
			br.close();
		} catch (Exception e) {
			System.out.println("Exception Occurred. Exception Details: " + e.getMessage());
		}

		return colNamesList;

	}

	public String extractMasterCodeForSpecialty(final String ruleString, String colName) {

		String[] tempArr = null;
		String masterCode = "";

		if (ruleString == null || ruleString.isEmpty())
			return "";

		if (ruleString.contains("\n")) {

			tempArr = ruleString.split("\n");

			if (tempArr != null) {

				for (String str : tempArr) {

					if (str.contains(colName)) {
						masterCode = extractNumber(str);

					}
				}
			}

		}

		return masterCode;
	}

	public String getCamelCasedString(String string) {

		String camelCasedStr = "";

		if (string == null || string.length() == 0) {
			return string;
		}

		if (containsIgnoreCase(string, "Remote")) {
			char c[] = string.toCharArray();
			c[0] = Character.toLowerCase(c[0]);

			camelCasedStr = new String(c);
			camelCasedStr = camelCasedStr.replace(" ", "");
			System.out.println("camelCasedStr : " + camelCasedStr);
		} else {
			camelCasedStr = string.toLowerCase();
		}

		return camelCasedStr;
	}

	public boolean containsIgnoreCase(String str, String subString) {
		if (subString != null && subString.contains(" ")) {
			subString = subString.replaceAll(" ", "");
		}

		if (str != null && str.contains(" ")) {
			str = str.replaceAll(" ", "");

		}
		return str.toLowerCase().contains(subString.toLowerCase());
	}

	public void getExecutionTime(long d1, long d2, String methodName) {

		long elapsed = d2 - d1;

		int hours = (int) Math.floor(elapsed / 3600000);

		int minutes = (int) Math.floor((elapsed - hours * 3600000) / 60000);

		int seconds = (int) Math.floor((elapsed - hours * 3600000 - minutes * 60000) / 1000);

		System.out.print("Time Taken By " + methodName + " for Execution : ");
		System.out.format("%d hours %d minutes %d seconds %d milliseconds%n", hours, minutes, seconds, elapsed);
	}
}
