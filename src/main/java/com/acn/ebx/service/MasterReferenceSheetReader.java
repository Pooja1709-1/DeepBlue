package com.acn.ebx.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.acn.ebx.utility.EBXProductConstants;
import com.acn.ebx.utility.PropertyMapper;
import com.acn.ebx.utility.UtilityFunctions;

public class MasterReferenceSheetReader {

	UtilityFunctions utility = new UtilityFunctions();

	public List<String> getMappingSheetData(String country, TreeMap<String, String> propertyMap) {

		String folderLocation = "";
		String fileName = "";
		FileInputStream file = null;
		XSSFWorkbook workbook = null;
		XSSFSheet sheet = null;
		List<String> listOfFieldsMappings = null;

		try {
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
				listOfFieldsMappings = utility.getItalyLogicData(sheet, propertyMap);
			}

		} catch (Exception e) {
			System.out.println("Exception Occurred in getMappingSheetData. Exception Details : " + e.getMessage());
		}

		return listOfFieldsMappings;
	}

	public Map<String, String> getMasterDataMap(TreeMap<String, String> propertyMap) {

		String folderLocation = "";
		String fileName = "";
		FileInputStream file = null;
		XSSFWorkbook workbook = null;
		XSSFSheet sheet = null;
		Map<String, String> mapOfCommonCodes = null;

		try {

			folderLocation = propertyMap.get("MasterDataFolderLocation");
			fileName = folderLocation.concat(propertyMap.get("ReferenceSheetFile"));
			file = new FileInputStream(new File(fileName));
			workbook = new XSSFWorkbook(file);
			sheet = workbook.getSheetAt(1);
			mapOfCommonCodes = utility.getMasterSheet(sheet, propertyMap);

		} catch (Exception e) {
			System.out.println("Exception Occurred in getMasterDataMap. Exception Details : " + e.getMessage());
		}

		return mapOfCommonCodes;

	}

	public Map<String, List<Map<String, String>>> getReferenceDataMap(TreeMap<String, String> propertyMap) {

		String folderLocation = "";
		String fileName = "";
		FileInputStream file = null;
		XSSFWorkbook workbook = null;
		XSSFSheet sheet = null;

		Map<String, List<Map<String, String>>> mapOfCode = null;

		try {

			if (propertyMap != null && !propertyMap.isEmpty()) {
				// Loading Reference Sheet Data
				folderLocation = propertyMap.get("MasterDataFolderLocation");
				fileName = folderLocation.concat(propertyMap.get("ReferenceSheetFile"));
				file = new FileInputStream(new File(fileName));

				if (file != null) {
					workbook = new XSSFWorkbook(file);
					sheet = workbook.getSheetAt(0);
					mapOfCode = masterFileMap(sheet);
				}
			}

		} catch (Exception e) {
			System.out.println("Exception Occurred. Exception Details : " + e.getMessage());
		}

		return mapOfCode;
	}

	public Map<String, List<Map<String, String>>> masterFileMap(Sheet sheet) throws URISyntaxException, IOException {

		Cell cell = null;
		Row row = null;
		Map<String, String> mdmCodesMap = null;
		PropertyMapper propertyMapper = new PropertyMapper();
		TreeMap<String, String> propertyMap = propertyMapper.getPropertyMap();

		String mdmID = "";
		String mdmCode = "";

		boolean isRowEmpty = false;
		StringBuffer MdmCodeDesc = new StringBuffer();

		List<Map<String, String>> codeDesc = null;

		List<String> mdmIdList = new ArrayList<String>();
		List<String> mdmCodesList = new ArrayList<String>();
		List<String> mdmCodeDesclist = new ArrayList<String>();
		List<String> mdmcodedescArray = new ArrayList<String>();
		Map<String, List<Map<String, String>>> mapOfCodesFinal = null;

		for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
			MdmCodeDesc = new StringBuffer();
			row = sheet.getRow(rowIndex);
			isRowEmpty = isEmptyRow(row);

			if (!isRowEmpty) {
				cell = row.getCell(Integer.parseInt(propertyMap.get("MasterData_MDMid")));

				if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {
					if (cell.getCellType() == Cell.CELL_TYPE_STRING) {
						mdmID = cell.getStringCellValue();

					} else if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
						mdmID = "" + (int) cell.getNumericCellValue();
					}

					MdmCodeDesc.append(mdmID);
					if (!mdmIdList.contains(mdmID))
						mdmIdList.add(mdmID);
				}

				cell = row.getCell(Integer.parseInt(propertyMap.get("MasterDataCode")));

				if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {
					if (cell.getCellType() == Cell.CELL_TYPE_STRING) {
						mdmCode = cell.getStringCellValue();

					}
					if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
						mdmCode = "" + (int) cell.getNumericCellValue();

					}

					MdmCodeDesc.append("~~");

					MdmCodeDesc.append(mdmCode);
					mdmCodesList.add(mdmCode);
				}

				cell = row.getCell(Integer.parseInt(propertyMap.get("MasterDataDescription")));
				if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK)
					mdmCodeDesclist.add(cell.getStringCellValue());

				if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {

					MdmCodeDesc.append("~~");
					MdmCodeDesc.append(cell.getStringCellValue());

				}

				mdmcodedescArray.add(MdmCodeDesc.toString());
			}

		}

		mapOfCodesFinal = new HashMap<String, List<Map<String, String>>>();

		for (String s : mdmcodedescArray) {
			String codeDescArray[] = s.split("~~");
			String code = codeDescArray[1];
			String Description = codeDescArray[2];
			String mdmIDTe = codeDescArray[0];

			if (mapOfCodesFinal.containsKey(mdmIDTe)) {
				codeDesc = mapOfCodesFinal.get(mdmIDTe);
				mdmCodesMap = new HashMap<String, String>();
				mdmCodesMap.put(Description, code);
				codeDesc.add(mdmCodesMap);

			} else {

				mdmCodesMap = new HashMap<String, String>();
				mdmCodesMap.put(Description, code);
				codeDesc = new ArrayList<Map<String, String>>();
				codeDesc.add(mdmCodesMap);
			}
			mapOfCodesFinal.put(mdmIDTe, codeDesc);
		}

		return mapOfCodesFinal;

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

}
