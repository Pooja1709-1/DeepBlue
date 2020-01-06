package com.acn.ebx.service;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.acn.ebx.utility.EBXProductConstants;
import com.acn.ebx.utility.UtilityFunctions;

import javolution.osgi.internal.OSGiServices;
import javolution.xml.stream.XMLOutputFactory;
import javolution.xml.stream.XMLStreamWriter;

public class CSVWriterService {

	UtilityFunctions utility = new UtilityFunctions();
	CSVReaderService csvReader = new CSVReaderService();
	MasterReferenceSheetReader masterReader = new MasterReferenceSheetReader();

	public void writeOutputXMLFile(List<String> colList, Map<Integer, List<String>> rowsList,
			List<List<String>> errorRowsList, String targetType, Charset charset, String outputFilePath) {

		List<String> cellList = null;
		String elementName = "";
		String fileName = "";
		try {

			long d1 = System.currentTimeMillis();

			if (colList != null) {
				System.out.println("Writing Output XML File For : " + targetType);
				fileName = outputFilePath + targetType + ".xml";

				XMLOutputFactory xMLOutputFactory = OSGiServices.getXMLOutputFactory();
				XMLStreamWriter xMLStreamWriter = xMLOutputFactory.createXMLStreamWriter(new FileOutputStream(fileName),
						"UTF-8");

				xMLStreamWriter.writeStartDocument();
				xMLStreamWriter.writeStartElement("root");
				targetType = targetType.replaceAll(" ", "");
				if (utility.containsIgnoreCase(targetType, "Remote")) {
					targetType = utility.getCamelCasedString(targetType);
				}

				if (rowsList != null) {

					for (Integer k : rowsList.keySet()) {

						xMLStreamWriter.writeStartElement(targetType);
						cellList = rowsList.get(k);
						if (cellList != null) {
							for (int j = 0; j < colList.size(); j++) {

								elementName = colList.get(j);
								xMLStreamWriter.writeStartElement(elementName.replaceAll(" ", ""));
								xMLStreamWriter.writeCharacters(cellList.get(j));
								xMLStreamWriter.writeEndElement();
							}
						}

						xMLStreamWriter.writeEndElement();
					}

				}

				xMLStreamWriter.writeEndElement();
				xMLStreamWriter.writeEndDocument();

				xMLStreamWriter.flush();
				xMLStreamWriter.close();

				long d2 = System.currentTimeMillis();

				utility.getExecutionTime(d1, d2, "XML Writting");
				System.out.println(targetType + ".xml written successfully on disk.");

			}

		} catch (Exception e) {

			System.out
					.println("Exception Occurred in writeOutputTemplateFile() . Exception Details: " + e.getMessage());
			e.printStackTrace();
		}

	}

	public void writeOutputCSVFile(List<List<String>> colList, Map<Integer, List<String>> rowsList,
			List<List<String>> errorRowsList, String targetType, Charset charset, String outputFilePath) {

		String fileName = "";
		List<String> cellList = null;
		Writer csvWriter = null;
		Writer csvErrorWriter = null;

		try {

			if (colList != null) {
				fileName = targetType + ".csv";
				csvWriter = new BufferedWriter(
						new OutputStreamWriter(new FileOutputStream(outputFilePath + targetType + ".csv"), charset));
				for (int j = 0; j < colList.size(); j++) {
					csvWriter.append(String.join(";", colList.get(j)));
					csvWriter.append("\n");

				}

				if (rowsList != null) {

					for (Integer k : rowsList.keySet()) {

						cellList = rowsList.get(k);

						if (cellList != null) {

							csvWriter.append(String.join(";", cellList));
							csvWriter.append("\n");

						}
					}

				}

				csvWriter.flush();
				csvWriter.close();

				System.out.println(fileName + " written successfully on disk.");

			}

			if (errorRowsList != null) {

				fileName = targetType + "Error.csv";

				csvErrorWriter = new BufferedWriter(
						new OutputStreamWriter(new FileOutputStream(outputFilePath + fileName), charset));

				for (int j = 0; j < colList.size(); j++) {
					csvErrorWriter.append(String.join(";", colList.get(j)));

				}
				csvErrorWriter.append(";Reason");
				csvErrorWriter.append("\n");

				for (int k = 0; k < errorRowsList.size(); k++) {
					csvErrorWriter.append(String.join(";", errorRowsList.get(k)));
					csvErrorWriter.append("\n");
				}

				csvErrorWriter.flush();
				csvErrorWriter.close();

				System.out.println(fileName + " written successfully on disk.");

			}

		} catch (Exception e) {

			System.out.println("Exception Occurred in writeOutputCSVFile() . Exception Details: " + e.getMessage());
			e.printStackTrace();
		}

	}

	public String findValueInMappingSheet(String targetType, Map<String, String> colNameValueMap, String colName,
			TreeMap<String, String> propertyMap, List<String> listOfFieldsMappings,
			HashMap<String, String> remoteKeyMap, HashMap<String, String> customerFileMap) {

		String description = "";
		String isPersonAccount = "";
		String mappingSheetDescriptionField = "";
		String[] tempArr = null;
		String fieldMappingString = "";
		String parentId = "";
		String customerId = "";

		try {

			description = colName.replace(" ", "_");
			mappingSheetDescriptionField = propertyMap.get(description);

			if (mappingSheetDescriptionField == null) {

				if (colName.contains(" - ")) {

					tempArr = colName.split(" - ");
					mappingSheetDescriptionField = tempArr[0];
				} else {

					mappingSheetDescriptionField = colName;
				}

			}

			String sourceTypeStr = targetType;

			if (EBXProductConstants.COMMUNICATION.equalsIgnoreCase(targetType)) {

				isPersonAccount = colNameValueMap.get(EBXProductConstants.ISPERSONACCOUNT);

				if ("TRUE".equalsIgnoreCase(isPersonAccount)) {

					sourceTypeStr = targetType + " (HCP)";
				} else if ("FALSE".equalsIgnoreCase(isPersonAccount)) {

					sourceTypeStr = targetType + " (HCO)";

				}
			} else if (EBXProductConstants.Affiliation.equalsIgnoreCase(targetType)) {

				parentId = colNameValueMap.get(EBXProductConstants.ParentId.toUpperCase());

				if (remoteKeyMap != null && remoteKeyMap.containsKey(parentId)) {
					customerId = remoteKeyMap.get(parentId);

					if (customerFileMap != null && customerFileMap.containsKey(customerId)) {
						isPersonAccount = customerFileMap.get(customerId);

						if (isPersonAccount != null) {

							isPersonAccount = propertyMap.get(isPersonAccount);

							if ("TRUE".equalsIgnoreCase(isPersonAccount)) {

								sourceTypeStr = targetType + " (Activity -> HCO-HCP)";
							} else if ("FALSE".equalsIgnoreCase(isPersonAccount)) {

								sourceTypeStr = targetType + " (Relation -> HCO-HCO)";

							}
						}
					}
				}

			}

			/*
			 * String descriptionField = sourceTypeStr + "~~" +
			 * mappingSheetDescriptionField; // Below change can be done using
			 * Stream Filters
			 * 
			 * List<String> fieldMappingStr_temp = listOfFieldsMappings.stream()
			 * .filter(s -> utility.containsIgnoreCase(s,
			 * descriptionField)).collect(Collectors.toList()); if
			 * (fieldMappingStr_temp != null && fieldMappingStr_temp.size() > 0)
			 * { // Extracting first field mapping value fieldMappingString =
			 * fieldMappingStr_temp.get(0); }
			 */

			String descriptionField = sourceTypeStr + "~~" + mappingSheetDescriptionField;

			// System.out.println("=====" + descriptionField);
			if (listOfFieldsMappings.stream().anyMatch(s -> s.contains(descriptionField))) {

				for (String fieldMappingStr : listOfFieldsMappings) {

					if (fieldMappingStr.contains(descriptionField)) {

						fieldMappingString = fieldMappingStr;
						break;
					}
				}

			}

		} catch (Exception e) {
			System.out.println("Exception Occurred In findValueInMappingSheet(). Exception Details" + e.getMessage());
			e.printStackTrace();
		}

		return fieldMappingString;
	}

	public boolean doesRuleStringContainsMDMRefID(String ruleString) {

		boolean result = false;

		try {

			if (ruleString != null && !ruleString.equals("")) {

				if (ruleString.contains("get MDM RefData") || ruleString.contains("MasterData")
						|| ruleString.contains("Masterdata") || ruleString.contains("Master data")
						|| ruleString.contains("Master Data")) {

					result = true;
				}
			}

		} catch (Exception e) {
			System.out.println(
					"Exception Occurred in doesRuleStringContainsMDMRefID(). Exception Details" + e.getMessage());
			e.printStackTrace();
		}

		return result;
	}

	public HashMap<String, HashMap<String, String>> getTypeCode(Map<String, String> mapOfCommonCodes,
			HashMap<String, HashMap<String, String>> sourceTypeValueMap, String masterDataId,
			Map<String, List<Map<String, String>>> mapOfCode, String colName, String fieldConstant, String fieldName,
			String value) {

		String cellValue = "";
		HashMap<String, String> sourceCodeValueMap = null;
		try {

			cellValue = fieldName;

			cellValue = utility.getMasterDataCode(mapOfCommonCodes, masterDataId, mapOfCode, colName, cellValue);

			sourceCodeValueMap = new HashMap<>();
			sourceCodeValueMap.put(cellValue, value);
			sourceTypeValueMap.put(fieldConstant, sourceCodeValueMap);

		} catch (Exception e) {
			System.out.println("Exception Occurred in getTypeCode(). Exception Details" + e.getMessage());
		}

		return sourceTypeValueMap;
	}

	public Map<Integer, List<String>> addCellListToRowList(List<String> cellList,
			HashMap<String, HashMap<String, String>> sourceTypeValueMap, List<String> colNameList, Integer rowIndex,
			Map<Integer, List<String>> rowsList, String fieldConstant) {

		List<String> tempCellList = new ArrayList<>();
		HashMap<String, String> sourceCodeValueMap = null;

		try {

			if (sourceTypeValueMap != null && !sourceTypeValueMap.isEmpty()
					&& sourceTypeValueMap.containsKey(fieldConstant)) {

				tempCellList = new ArrayList<>();
				tempCellList.addAll(cellList);

				sourceCodeValueMap = sourceTypeValueMap.get(fieldConstant);
				for (String credential : sourceCodeValueMap.keySet()) {
					tempCellList.set(colNameList.indexOf("Type - Code"), credential);
					tempCellList.set(colNameList.indexOf("Value"), sourceCodeValueMap.get(credential));
				}

				Integer tempRowIndex = rowsList.size();
				if (rowsList.containsKey(tempRowIndex)) {

					rowsList.put(++tempRowIndex, tempCellList);
				} else {
					rowsList.put(tempRowIndex, tempCellList);
				}

			}

		} catch (Exception e) {

			System.out.println("Exception Occurred. Exception Details" + e.getMessage());

		}

		return rowsList;

	}

	public void writeErrorCSVFile(Map<Integer, String> errorRowsMap,
			Map<Integer, Map<String, String>> rowColumnNameValueMap, String sourceFile, String outputFilePath) {

		String fileName = "";
		Writer csvErrorWriter = null;
		Map<String, String> columnNameValueMap = null;
		Charset charset = Charset.forName("UTF-8");

		// String errorReason
		long d1 = System.currentTimeMillis();
		try {

			if (errorRowsMap != null && !errorRowsMap.isEmpty()) {
				if (sourceFile.contains("AccountFile")) {
					fileName = "Account" + "_Error.csv";
				}

				else if (sourceFile.contains("Address")) {
					fileName = "Address" + "_Error.csv";
				} else if (sourceFile.contains("ChildAccount")) {
					fileName = "ChildAccount" + "_Error.csv";
				} else if (sourceFile.contains("Workplace")) {
					fileName = "Workplace" + "_Error.csv";
				} else if (sourceFile.contains("Individual")) {
					fileName = "Individual" + "_Error.csv";
				}
				csvErrorWriter = new BufferedWriter(
						new OutputStreamWriter(new FileOutputStream(outputFilePath + fileName), charset));

				// BufferedReader fileReader = new BufferedReader(
				// new InputStreamReader(new FileInputStream(sourceFile),
				// charset));

				// CSVReader csvReader = new
				// CSVReaderBuilder(fileReader).build();
				// List<String[]> allInputRows = csvReader.readAll();

				/*
				 * if (allInputRows != null && !allInputRows.isEmpty()) {
				 * csvErrorWriter.append(String.join(";", allInputRows.get(0)));
				 * 
				 * System.out.println("error " + csvErrorWriter.toString());
				 * csvErrorWriter.append(";Reason");
				 * csvErrorWriter.append("\n"); // method 2 with reading the
				 * source again here if (errorRowsMap != null &&
				 * !errorRowsMap.isEmpty()) { for (Integer rowIndex :
				 * errorRowsMap.keySet()) {
				 * 
				 * csvErrorWriter.append(String.join(";",
				 * allInputRows.get(rowIndex))); csvErrorWriter.append(";" +
				 * errorRowsMap.get(rowIndex)); csvErrorWriter.append("\n"); } }
				 * 
				 * }
				 */

				// csvErrorWriter.append("\n");

				// columnNameValueMap = rowColumnNameValueMap.get(0);
				// csvErrorWriter.append(String.join(";",
				// columnNameValueMap.keySet()));
				// csvErrorWriter.append("\n");
				// method 1 with lunkedhashmap change in reader service itself

				if (rowColumnNameValueMap != null && !rowColumnNameValueMap.isEmpty()) {

					columnNameValueMap = rowColumnNameValueMap.get(1);
					csvErrorWriter.append(String.join(",", columnNameValueMap.keySet()));
					csvErrorWriter.append(",Reason,CustomerID");
					csvErrorWriter.append("\n");

					for (Integer k : errorRowsMap.keySet()) {

						columnNameValueMap = rowColumnNameValueMap.get(k);

						if (columnNameValueMap != null) {

							csvErrorWriter.append(String.join(",", columnNameValueMap.values()));
							csvErrorWriter.append("," + errorRowsMap.get(k));
							csvErrorWriter.append("\n");

						}
					}
				}

				csvErrorWriter.flush();
				csvErrorWriter.close();

				long d2 = System.currentTimeMillis();
				long elapsed = d2 - d1;
				int hours = (int) Math.floor(elapsed / 3600000);
				int minutes = (int) Math.floor((elapsed - hours * 3600000) / 60000);
				int seconds = (int) Math.floor((elapsed - hours * 3600000 - minutes * 60000) / 1000);
				System.out.println(fileName + " written successfully on disk.");
				System.out.print("Time Taken to write error File : ");
				System.out.format("%d hours %d minutes %d seconds%n", hours, minutes, seconds);

			}

			// System.out.println(fileName + " written successfully on disk.");

		} catch (Exception e) {

			System.out.println("Exception Occurred in writeOutputCSVFile() . Exception Details: " + e.getMessage());
			e.printStackTrace();
		}

	}

	public Map<String, Integer> writeCustomerOutputFile(String country, String targetTypeList,
			Map<String, List<Map<String, String>>> mapOfCode, Map<String, String> mapOfCommonCodes,
			List<String> listOfFieldsMappings, TreeMap<String, String> propertyMap, String inputSourceFile,
			String outputFilePath, Integer autoGeneratedSequence) {

		int length = 0;
		Integer sourceCount = 0;
		Integer targetCount = 0;
		Integer targetErrorCount = 0;
		Integer sourceCountHCP = 0;
		Integer sourceCountHCO = 0;
		Integer autoGeneratedValue = 0;
		String HCOPrefix = "";
		String HCPPrefix = "";
		String customerPrefix = "";
		String[] fieldMappingArray = null;
		String[] tempErrorReasonArray = null;
		String mappingFieldName = "";
		String legacyFieldName = "";
		String cellValue = "";
		String ruleString = "";
		String stringLength = "";
		String nationalCode = "";
		String nationalHCPID = "";
		String councilHCPID = "";
		String customerId = "";
		String remoteKeyFilePath = null;
		String customerFilePath = null;
		String date = "";
		String errorReason = "";
		String isPersonAccount = "";
		String phone = "";
		String email = "";
		String personalEmail = "";
		String personalMobilePhone = "";
		String fieldMappingStr = "";
		String masterDataId = "";
		String fieldConstant = "";
		String fieldName = "";
		String specialtyType = "";
		String[] targetTypeArray = null;
		String targetType = "";
		String remoteKeyElement = "";
		String remoteKeyValueElement = "";
		String customerKeyElement = "";
		String customerValueElement = "";

		boolean isHCO = false;
		boolean isHCP = false;
		boolean isNationalCodePresent = false;
		boolean isNationalHCPIDPresent = false;
		boolean isCouncilHCPIDPresent = false;
		boolean addToRowList = true;
		boolean isErrorRecord = false;
		boolean isPhone = false;
		boolean isEmail = false;
		boolean isPersonalEmail = false;
		boolean isPersonMobilePhone = false;
		boolean containsRefID = false;

		SimpleDateFormat sdf = null;

		HashMap<String, String> remoteKeyMap = null;
		HashMap<String, String> customerFileMap = null;
		HashMap<String, HashMap<String, String>> sourceTypeValueMap = null;

		Map<Integer, String> customerIdsHCPMap = null;
		Map<Integer, String> customerIdsHCOMap = null;

		Map<Integer, List<String>> rowsList = null;
		List<List<String>> errorRowsList = null;
		List<String> cellList = null;
		Charset charset = null;
		List<String> colNameList = null;
		UtilityFunctions utility = new UtilityFunctions();
		XMLReaderService xmlReader = new XMLReaderService();
		File template = null;
		String templatePath = "";
		Map<String, String> colNameValueMap = null;
		FileInputStream templateStream = null;
		Map<Integer, Map<String, String>> rowColumnNameValueMap = null;
		HashMap<String, Integer> countMap = null;
		HashMap<Integer, String> errorRowsMap = null;
		String tempErrorReason = "";

		try {

			charset = Charset.forName("UTF-8");

			sourceTypeValueMap = new HashMap<String, HashMap<String, String>>();
			customerIdsHCPMap = new HashMap<>();
			customerIdsHCOMap = new HashMap<>();
			customerPrefix = propertyMap.get("Customer_ID_Prefix");
			HCOPrefix = propertyMap.get("HCO_ID_Prefix");
			HCPPrefix = propertyMap.get("HCP_ID_Prefix");

			// Required for Address and Affiliation
			remoteKeyFilePath = outputFilePath.concat("Remote Key.xml");
			customerFilePath = outputFilePath.concat("Customer.xml");
			templatePath = propertyMap.get("TemplateFolderLocation");
			remoteKeyMap = new HashMap<>();

			if (targetTypeList != null && !targetTypeList.equals("")) {

				targetTypeArray = targetTypeList.split(";");

				if (targetTypeArray != null) {

					countMap = new HashMap<>();
					rowColumnNameValueMap = csvReader.readInputSourceData(inputSourceFile);
					sourceCount = rowColumnNameValueMap.size();

					listOfFieldsMappings = masterReader.getMappingSheetData(country, propertyMap);
					sdf = new SimpleDateFormat("MM-dd-YYYY'T'HH:MM:ss");
					date = sdf.format(new Date());
					errorRowsMap = new HashMap<Integer, String>();

					// customerSequence =
					// propertyMap.get("Customer_ID_Sequence");

					for (int i = 0; i < targetTypeArray.length; i++) {

						targetType = targetTypeArray[i];
						errorRowsMap = new HashMap<Integer, String>();
						template = new File(templatePath + targetType + ".csv");
						templateStream = new FileInputStream(template);
						colNameList = utility.getTemplateColumnNamesList(templateStream);
						rowsList = new HashMap<Integer, List<String>>();
						errorRowsList = new ArrayList<List<String>>();

						addToRowList = true;

						if (colNameList != null && !colNameList.isEmpty()) {

							long d1 = System.currentTimeMillis();

							if (EBXProductConstants.Address.equalsIgnoreCase(targetType)
									|| EBXProductConstants.Affiliation.equalsIgnoreCase(targetType)) {

								remoteKeyElement = propertyMap.get("RemoteKey_KeyElement");
								remoteKeyValueElement = propertyMap.get("RemoteKey_ValueElement");
								remoteKeyMap = xmlReader.getMapFromXML(remoteKeyFilePath, remoteKeyElement,
										remoteKeyValueElement);
							}

							if (EBXProductConstants.Affiliation.equalsIgnoreCase(targetType)) {

								customerKeyElement = propertyMap.get("Customer_KeyElement");
								customerValueElement = propertyMap.get("Customer_ValueElement");
								customerFileMap = xmlReader.getMapFromXML(customerFilePath, customerKeyElement,
										customerValueElement);

							}

							if (rowColumnNameValueMap != null && !rowColumnNameValueMap.isEmpty()) {

								autoGeneratedValue = autoGeneratedSequence;

								for (Integer rowKeyIndex : rowColumnNameValueMap.keySet()) {

									colNameValueMap = rowColumnNameValueMap.get(rowKeyIndex);

									cellList = new ArrayList<>();

									for (String colName : colNameList) {

										cellValue = "";

										if (EBXProductConstants.Customer.equalsIgnoreCase(targetType)
												&& (EBXProductConstants.CustomerID.equalsIgnoreCase(colName)
														|| (EBXProductConstants.Spain.equalsIgnoreCase(country)
																&& EBXProductConstants.ProducerSystemRecordID
																		.equalsIgnoreCase(colName)))) {

											cellValue = customerPrefix + String.valueOf(autoGeneratedValue);
											cellList.add(cellValue);
											customerId = String.valueOf(autoGeneratedValue);
											continue;
										}

										else if (EBXProductConstants.HCO.equalsIgnoreCase(targetType)
												&& (EBXProductConstants.HealthCareOrganizationID
														.equalsIgnoreCase(colName)
														|| EBXProductConstants.CustomerCustomerID
																.equalsIgnoreCase(colName))) {

											if (customerIdsHCOMap != null && !customerIdsHCOMap.isEmpty()) {

												if (customerIdsHCOMap.containsKey(rowKeyIndex)) {

													if (EBXProductConstants.HealthCareOrganizationID
															.equalsIgnoreCase(colName)) {
														cellValue = HCOPrefix + customerIdsHCOMap.get(rowKeyIndex);

													} else if (EBXProductConstants.CustomerCustomerID
															.equalsIgnoreCase(colName)) {

														cellValue = customerPrefix + customerIdsHCOMap.get(rowKeyIndex);
														customerId = customerPrefix
																+ customerIdsHCOMap.get(rowKeyIndex);

													}

													cellList.add(cellValue);
													continue;
												} else {

													addToRowList = false;
													break;
												}

											}

											else {

												cellValue = colNameValueMap.get(EBXProductConstants.ISPERSONACCOUNT);
												if ("TRUE".equalsIgnoreCase(cellValue)) {
													addToRowList = false;
													break;
												}
											}

										} else if (EBXProductConstants.HCP.equalsIgnoreCase(targetType)
												&& (EBXProductConstants.HealthCareProfessionalID
														.equalsIgnoreCase(colName)
														|| EBXProductConstants.CustomerCustomerID
																.equalsIgnoreCase(colName))) {

											if (customerIdsHCPMap != null && !customerIdsHCPMap.isEmpty()) {

												if (customerIdsHCPMap.containsKey(rowKeyIndex)) {

													if (EBXProductConstants.HealthCareProfessionalID
															.equalsIgnoreCase(colName)) {
														cellValue = HCPPrefix + customerIdsHCPMap.get(rowKeyIndex);

													} else if (EBXProductConstants.CustomerCustomerID
															.equalsIgnoreCase(colName)) {

														cellValue = customerPrefix + customerIdsHCPMap.get(rowKeyIndex);
														customerId = customerPrefix
																+ customerIdsHCPMap.get(rowKeyIndex);
													}

													cellList.add(cellValue);
													continue;
												} else {

													addToRowList = false;
													break;
												}

											}

											else {

												cellValue = colNameValueMap.get(EBXProductConstants.ISPERSONACCOUNT);
												if ("FALSE".equalsIgnoreCase(cellValue)) {
													addToRowList = false;
													break;
												}
											}

										} else if (!EBXProductConstants.Address.equalsIgnoreCase(targetType)
												&& EBXProductConstants.CustomerCustomerID.equalsIgnoreCase(colName)
												|| (EBXProductConstants.Spain.equalsIgnoreCase(country)
														&& EBXProductConstants.ProducerSystemRecordID
																.equalsIgnoreCase(colName)))

										{

											cellValue = customerPrefix + String.valueOf(autoGeneratedValue);
											cellList.add(cellValue);
											customerId = customerPrefix + String.valueOf(autoGeneratedValue);
											continue;

										} else if ((EBXProductConstants.Credential.equalsIgnoreCase(targetType)
												|| EBXProductConstants.COMMUNICATION.equalsIgnoreCase(targetType))
												&& "Value".equalsIgnoreCase(colName)) {
											cellList.add("");
											continue;
										}

										else if (EBXProductConstants.CreatedDate.equalsIgnoreCase(colName)
												|| EBXProductConstants.LastUpdatedDate.equalsIgnoreCase(colName)) {

											cellValue = date;
											cellList.add(cellValue);
											continue;

										} else if (EBXProductConstants.UpdatedInMDM.equalsIgnoreCase(colName)) {

											cellValue = propertyMap.get("UpdatedInMDM");
											cellList.add(cellValue);
											continue;

										} else {

											// Get Field Mapping from
											// Mapping Sheet
											fieldMappingStr = findValueInMappingSheet(targetType, colNameValueMap,
													colName, propertyMap, listOfFieldsMappings, remoteKeyMap,
													customerFileMap);

											if (fieldMappingStr != null && !fieldMappingStr.equals("")) {

												cellValue = "";
												fieldMappingArray = fieldMappingStr.split("~~");

												if (fieldMappingArray != null) {

													ruleString = fieldMappingArray[2];

													if (fieldMappingArray.length > 5) {

														mappingFieldName = fieldMappingArray[5];
														legacyFieldName = fieldMappingArray[6];
													} else {
														mappingFieldName = fieldMappingArray[4];
													}

													if (!legacyFieldName.equals("NULL") && !legacyFieldName.equals("")
															&& legacyFieldName != null && legacyFieldName != "") {
														mappingFieldName = legacyFieldName;
													}

													mappingFieldName = mappingFieldName.toUpperCase().trim();

													// Check if Veeva
													// Field API Name is
													// N or N/A

													if (mappingFieldName.equalsIgnoreCase("N/A")
															|| mappingFieldName.equals("NULL")) {

														containsRefID = doesRuleStringContainsMDMRefID(ruleString);

														if (containsRefID) {

															masterDataId = utility.extractNumber(ruleString);

															// Setting
															// Type Code
															// for
															// Communication
															if (EBXProductConstants.COMMUNICATION
																	.equalsIgnoreCase(targetType)
																	&& EBXProductConstants.TypeCode
																			.equalsIgnoreCase(colName)) {

																isPersonAccount = colNameValueMap
																		.get(EBXProductConstants.ISPERSONACCOUNT);

																if (isPersonAccount != null
																		&& !isPersonAccount.equals("")) {

																	fieldConstant = EBXProductConstants.Phone;
																	fieldName = EBXProductConstants.Phone;
																	phone = colNameValueMap
																			.get(fieldName.toUpperCase());
																	if (phone != null && !phone.equals("")) {

																		isPhone = true;
																		sourceTypeValueMap = getTypeCode(
																				mapOfCommonCodes, sourceTypeValueMap,
																				masterDataId, mapOfCode, colName,
																				fieldConstant, fieldName, phone);

																	} else {
																		isPhone = false;
																	}
																}

																// Setting
																// Type
																// Code
																// for
																// HCP
																// Communication
																// Type
																if ("TRUE".equalsIgnoreCase(isPersonAccount)) {

																	fieldConstant = EBXProductConstants.Email;
																	fieldName = EBXProductConstants.EmailHCP;

																	email = colNameValueMap
																			.get(fieldName.toUpperCase());

																	if (email != null && !email.equals("")) {

																		isEmail = true;
																		sourceTypeValueMap = getTypeCode(
																				mapOfCommonCodes, sourceTypeValueMap,
																				masterDataId, mapOfCode, colName,
																				fieldConstant, fieldName, email);

																	} else {
																		isEmail = false;
																	}

																	fieldConstant = EBXProductConstants.PersonalEmail;
																	fieldName = EBXProductConstants.PersonalEmail;
																	personalEmail = colNameValueMap
																			.get(fieldName.toUpperCase());

																	if (personalEmail != null
																			&& !personalEmail.equals("")) {

																		isPersonalEmail = true;
																		sourceTypeValueMap = getTypeCode(
																				mapOfCommonCodes, sourceTypeValueMap,
																				masterDataId, mapOfCode, colName,
																				fieldConstant, fieldName,
																				personalEmail);

																	} else {
																		isPersonalEmail = false;
																	}

																	fieldConstant = EBXProductConstants.PersonalPhone;
																	fieldName = EBXProductConstants.PersonalPhone;
																	personalMobilePhone = colNameValueMap
																			.get(fieldName.toUpperCase());

																	if (personalMobilePhone != null
																			&& !personalMobilePhone.equals("")) {

																		isPersonMobilePhone = true;
																		sourceTypeValueMap = getTypeCode(
																				mapOfCommonCodes, sourceTypeValueMap,
																				masterDataId, mapOfCode, colName,
																				fieldConstant, fieldName,
																				personalMobilePhone);

																	} else {
																		isPersonMobilePhone = false;
																	}

																} else if ("FALSE".equalsIgnoreCase(isPersonAccount)) {

																	fieldConstant = EBXProductConstants.Email;
																	fieldName = EBXProductConstants.EmailHCO;
																	email = colNameValueMap
																			.get(fieldName.toUpperCase());

																	if (email != null && !email.equals("")) {

																		isEmail = true;
																		fieldName = fieldConstant;
																		sourceTypeValueMap = getTypeCode(
																				mapOfCommonCodes, sourceTypeValueMap,
																				masterDataId, mapOfCode, colName,
																				fieldConstant, fieldName, email);

																	} else {
																		isEmail = false;
																	}
																}

															} else {
																// Get
																// Value
																// from
																// Reference
																// sheet
																cellValue = utility.getMasterDataCode(mapOfCommonCodes,
																		masterDataId, mapOfCode, colName, cellValue);
															}

														} else {

															if ((EBXProductConstants.Specialty
																	.equalsIgnoreCase(targetType)
																	&& EBXProductConstants.StatusCode
																			.equalsIgnoreCase(colName))
																	|| (EBXProductConstants.Affiliation
																			.equalsIgnoreCase(targetType)
																			&& EBXProductConstants.Affiliation_Type_Code
																					.equalsIgnoreCase(colName))) {
																cellValue = utility.extractNumber(ruleString);
															} else if (EBXProductConstants.Specialty
																	.equalsIgnoreCase(targetType)
																	&& EBXProductConstants.TypeCode
																			.equalsIgnoreCase(colName)) {

																specialtyType = propertyMap
																		.get(EBXProductConstants.SPECIALTY_1)
																		.toUpperCase();
																if (colNameValueMap
																		.containsKey(specialtyType.toUpperCase())) {

																	cellValue = utility.extractMasterCodeForSpecialty(
																			ruleString,
																			EBXProductConstants.SPECIALTY_1);
																}

															}

															else if (EBXProductConstants.Credential
																	.equalsIgnoreCase(targetType)
																	&& EBXProductConstants.StatusCode
																			.equalsIgnoreCase(colName)) {

																String key = targetType + "_"
																		+ colName.replace(" ", "_");
																masterDataId = propertyMap.get(key);
																cellValue = utility.getMasterDataCode(mapOfCommonCodes,
																		masterDataId, mapOfCode, colName, cellValue);
															}

															else if (EBXProductConstants.Italy
																	.equalsIgnoreCase(country)) {

																if (EBXProductConstants.TypeCode
																		.equalsIgnoreCase(colName)) {

																	if (utility.containsIgnoreCase(inputSourceFile,
																			"workplace")) {
																		cellValue = "FALSE";

																	} else if (utility.containsIgnoreCase(
																			inputSourceFile, "individual")) {

																		cellValue = "TRUE";

																	}

																	String key1 = colName.replace(" ", "").replace("-",
																			"_");

																	masterDataId = propertyMap.get(key1);

																	cellValue = utility.getMasterDataCode(
																			mapOfCommonCodes, masterDataId, mapOfCode,
																			colName, cellValue);
																}
															} else {
																cellValue = propertyMap.get(colName.replace(" ", "_"));

																if (cellValue == null) {
																	cellValue = "";
																}
															}

														}

														// break;

													} else {

														containsRefID = doesRuleStringContainsMDMRefID(ruleString);

														if (containsRefID) {

															cellValue = colNameValueMap
																	.get(mappingFieldName.toUpperCase());
															masterDataId = utility.extractNumber(ruleString);

															cellValue = utility.getMasterDataCode(mapOfCommonCodes,
																	masterDataId, mapOfCode, colName, cellValue);

															if (cellValue == null || cellValue == "") {

																if (isErrorRecord) {

																	tempErrorReasonArray = errorReason.split(";");
																	if (tempErrorReasonArray != null
																			&& tempErrorReasonArray.length > 2) {
																		customerId = tempErrorReasonArray[1];
																		errorReason = tempErrorReasonArray[0];

																		errorReason = errorReason + ";" + colName + ","
																				+ customerId;
																	} else {
																		errorReason = errorReason + ";" + colName;
																	}

																} else {
																	errorReason = "Master ID not found for column - "
																			+ colName + "," + customerId;
																}
																isErrorRecord = true;

															}

														} else {

															// Rule
															// String
															// does not
															// contain
															// Master
															// Ref Data
															// Id

															cellValue = colNameValueMap
																	.get(mappingFieldName.toUpperCase());

															if (EBXProductConstants.CustomerCustomerID.equals(colName)
																	|| EBXProductConstants.ParentCustomerId
																			.equals(colName)
																	|| EBXProductConstants.ChildCustomerId
																			.equals(colName)) {

																if (mappingFieldName != null
																		&& mappingFieldName.contains("(")) {
																	int indexOfKey = mappingFieldName.indexOf("(");
																	mappingFieldName = mappingFieldName
																			.substring(0, indexOfKey).toUpperCase()
																			.trim();
																}

																cellValue = colNameValueMap
																		.get(mappingFieldName.toUpperCase());

																if (remoteKeyMap != null && !remoteKeyMap.isEmpty()) {

																	cellValue = remoteKeyMap.get(cellValue);
																}

															}

															if (ruleString.contains("first char of")) {

																stringLength = utility.extractNumber(ruleString);

																if (stringLength != null && stringLength != ""
																		&& cellValue != null && cellValue != "") {
																	length = Integer.parseInt(stringLength);

																	if (length <= cellValue.length()) {
																		cellValue = cellValue.substring(0, length);
																	}

																}

															}

															// Setting
															// Type for
															// Credentials
															if (EBXProductConstants.Credential
																	.equalsIgnoreCase(targetType)
																	&& ((EBXProductConstants.TypeCode
																			.equalsIgnoreCase(colName)))) {

																sourceTypeValueMap = new HashMap<>();
																String key = targetType + "_"
																		+ colName.replace(" ", "_");
																masterDataId = propertyMap.get(key);

																fieldConstant = EBXProductConstants.NATIONALCODE_Constant;
																fieldName = EBXProductConstants.NATIONALCODE;
																nationalCode = colNameValueMap
																		.get(fieldName.toUpperCase());

																if (nationalCode != null && nationalCode != ""
																		&& !nationalCode.isEmpty()) {

																	isNationalCodePresent = true;
																	fieldName = fieldConstant;
																	sourceTypeValueMap = getTypeCode(mapOfCommonCodes,
																			sourceTypeValueMap, masterDataId, mapOfCode,
																			colName, fieldConstant, fieldName,
																			nationalCode);

																} else {
																	isNationalCodePresent = false;
																}

																fieldConstant = EBXProductConstants.NATIONALHCPID_Constant;
																fieldName = EBXProductConstants.NATIONALHCPID;
																nationalHCPID = colNameValueMap
																		.get(fieldName.toUpperCase());

																if (nationalHCPID != null && nationalHCPID != ""
																		&& !nationalHCPID.isEmpty()) {

																	isNationalHCPIDPresent = true;
																	fieldName = fieldConstant;
																	sourceTypeValueMap = getTypeCode(mapOfCommonCodes,
																			sourceTypeValueMap, masterDataId, mapOfCode,
																			colName, fieldConstant, fieldName,
																			nationalHCPID);

																} else {
																	isNationalHCPIDPresent = false;
																}

																fieldConstant = EBXProductConstants.COUNCILHCPID_Constant;
																fieldName = EBXProductConstants.COUNCILHCPID;
																councilHCPID = colNameValueMap
																		.get(fieldName.toUpperCase());

																if (councilHCPID != null && councilHCPID != ""
																		&& !councilHCPID.isEmpty()) {

																	isCouncilHCPIDPresent = true;
																	fieldName = fieldConstant;
																	sourceTypeValueMap = getTypeCode(mapOfCommonCodes,
																			sourceTypeValueMap, masterDataId, mapOfCode,
																			colName, fieldConstant, fieldName,
																			councilHCPID);

																} else {
																	isCouncilHCPIDPresent = false;
																}

																// break;

															} else if (EBXProductConstants.TypeCode
																	.equalsIgnoreCase(colName)
																	|| EBXProductConstants.StatusCode
																			.equalsIgnoreCase(colName)
																	|| EBXProductConstants.StateAndProvinceCode
																			.equalsIgnoreCase(colName)) {

																if (EBXProductConstants.TypeCode
																		.equalsIgnoreCase(colName)
																		&& "FALSE".equalsIgnoreCase(cellValue)) {

																	isHCO = true;
																}
																if (EBXProductConstants.TypeCode
																		.equalsIgnoreCase(colName)
																		&& "TRUE".equalsIgnoreCase(cellValue)) {

																	isHCP = true;
																}

																String key1 = colName.replace(" ", "").replace("-",
																		"_");
																String key2 = colName.replace(" ", "_");

																if (propertyMap.containsKey(key1)) {
																	masterDataId = propertyMap.get(key1);
																} else if (propertyMap.containsKey(key2)) {
																	masterDataId = propertyMap.get(key2);
																}

																cellValue = utility.getMasterDataCode(mapOfCommonCodes,
																		masterDataId, mapOfCode, colName, cellValue);

															}

														}

														if (cellValue == null) {
															cellValue = "";
														}

														// break;
													}
												}

											} else {

												// If columnName not
												// found in mapping
												// sheet check if
												// columnName
												// exists in properties
												// file

												String key1 = colName.replace(" ", "").replace("-", "_");
												String key2 = colName.replace(" ", "_");
												masterDataId = "";

												if (propertyMap.containsKey(key1)) {
													masterDataId = propertyMap.get(key1);
												} else if (propertyMap.containsKey(key2)) {
													masterDataId = propertyMap.get(key2);
												}

												cellValue = utility.getMasterDataCode(mapOfCommonCodes, masterDataId,
														mapOfCode, colName, cellValue);

												if (cellValue == "" && propertyMap.containsKey(key2)) {
													cellValue = propertyMap.get(key2);
												} else if (EBXProductConstants.Address.equalsIgnoreCase(targetType)
														&& EBXProductConstants.SystemKey.equalsIgnoreCase(colName)) {
													cellValue = colNameValueMap
															.get(propertyMap.get("System_Key_Mapping"));
												}

											}

											if (isHCO) {

												customerIdsHCOMap.put(rowKeyIndex, customerId);

											}

											if (isHCP) {

												customerIdsHCPMap.put(rowKeyIndex, customerId);

											}

											isHCO = false;
											isHCP = false;
											cellList.add(cellValue);
											cellValue = "";
										}
									}

									if (addToRowList) {

										if (!isErrorRecord) {

											if (EBXProductConstants.Credential.equalsIgnoreCase(targetType)) {

												if (isNationalCodePresent) {
													fieldConstant = EBXProductConstants.NATIONALCODE_Constant;
													rowsList = addCellListToRowList(cellList, sourceTypeValueMap,
															colNameList, rowKeyIndex, rowsList, fieldConstant);
												}

												if (isNationalHCPIDPresent) {
													fieldConstant = EBXProductConstants.NATIONALHCPID_Constant;
													rowsList = addCellListToRowList(cellList, sourceTypeValueMap,
															colNameList, rowKeyIndex, rowsList, fieldConstant);
												}

												if (isCouncilHCPIDPresent) {
													fieldConstant = EBXProductConstants.COUNCILHCPID_Constant;
													rowsList = addCellListToRowList(cellList, sourceTypeValueMap,
															colNameList, rowKeyIndex, rowsList, fieldConstant);
												}
												if (!isNationalCodePresent && !isNationalCodePresent
														&& !isNationalCodePresent) {
													errorRowsList.add(cellList);

												}
											} else if (EBXProductConstants.COMMUNICATION.equalsIgnoreCase(targetType)) {

												if (isPhone) {

													fieldConstant = EBXProductConstants.Phone;
													rowsList = addCellListToRowList(cellList, sourceTypeValueMap,
															colNameList, rowKeyIndex, rowsList, fieldConstant);
												}

												if (isPersonalEmail) {
													fieldConstant = EBXProductConstants.PersonalEmail;
													rowsList = addCellListToRowList(cellList, sourceTypeValueMap,
															colNameList, rowKeyIndex, rowsList, fieldConstant);

												}

												if (isEmail) {
													fieldConstant = EBXProductConstants.Email;
													rowsList = addCellListToRowList(cellList, sourceTypeValueMap,
															colNameList, rowKeyIndex, rowsList, fieldConstant);

												}

												if (isPersonMobilePhone) {
													fieldConstant = EBXProductConstants.PersonalPhone;
													rowsList = addCellListToRowList(cellList, sourceTypeValueMap,
															colNameList, rowKeyIndex, rowsList, fieldConstant);

												}
												if (!isPhone) {
													if ("TRUE".equalsIgnoreCase(
															colNameValueMap.get(EBXProductConstants.ISPERSONACCOUNT))) {
														if (!isEmail && !isPersonalEmail && !isPersonMobilePhone) {
															errorRowsList.add(cellList);
														}

													} else if ("FALSE".equalsIgnoreCase(
															colNameValueMap.get(EBXProductConstants.ISPERSONACCOUNT))) {
														if (!isEmail) {
															errorRowsList.add(cellList);
														}

													}
												}

											} else {

												rowsList.put(rowKeyIndex, cellList);
											}

										} else {
											cellList.add(errorReason);
											tempErrorReason = errorRowsMap.get(rowKeyIndex);
											if (tempErrorReason == null) {
												errorRowsMap.put(rowKeyIndex, errorReason);
											} else {
												if (tempErrorReason.contains("NVS")) {
													tempErrorReasonArray = tempErrorReason.split(",");
													if (tempErrorReasonArray != null
															&& !tempErrorReasonArray[1].isEmpty()
															&& !tempErrorReasonArray[0].isEmpty()) {

														tempErrorReason = tempErrorReasonArray[0];

													}

												}

												errorReason = tempErrorReason.concat(";" + errorReason);
												errorRowsMap.put(rowKeyIndex, errorReason);

											}

											errorRowsList.add(cellList);
										}
									}

									autoGeneratedValue++;
									addToRowList = true;
									isErrorRecord = false;
									errorReason = "";
									sourceTypeValueMap = new HashMap<>();
									isNationalCodePresent = false;
									isNationalHCPIDPresent = false;
									isCouncilHCPIDPresent = false;
									isPhone = false;
									isEmail = false;
									isPersonalEmail = false;
									isPersonMobilePhone = false;

								}

							}

							long d2 = System.currentTimeMillis();
							utility.getExecutionTime(d1, d2, "preparing dataset for XML");

							targetCount = rowsList.size();
							targetErrorCount = errorRowsList.size();
							sourceCountHCP = customerIdsHCPMap.size();
							sourceCountHCO = customerIdsHCOMap.size();

							countMap.put("PreviousIncrementedValue", autoGeneratedValue);
							countMap.put("sourceCount", sourceCount);
							countMap.put("sourceCountHCP", sourceCountHCP);
							countMap.put("sourceCountHCO", sourceCountHCO);
							countMap.put(targetType + "targetCount", targetCount);
							countMap.put(targetType + "targetErrorCount", targetErrorCount);

							writeOutputXMLFile(colNameList, rowsList, errorRowsList, targetType, charset,
									outputFilePath);
							if (errorRowsMap != null && !errorRowsMap.isEmpty()) {

								writeErrorCSVFile(errorRowsMap, rowColumnNameValueMap, inputSourceFile, outputFilePath);
							}

						}

					}
				}

			}

		} catch (

		Exception e) {
			System.out.println("Exception Occurred In writeOutputTemplate. Exception Details: " + e.getMessage());
			e.printStackTrace();
		}

		return countMap;
	}

}
