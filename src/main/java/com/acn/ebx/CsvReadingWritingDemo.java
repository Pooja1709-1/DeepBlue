package com.acn.ebx;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;

public class CsvReadingWritingDemo {

	public static void main(String[] args) {

		String csvFile = "C:\\EBX\\Novartis DeepBlue\\DeepBlue\\Source File(s)\\AccountFile1.csv";

		// CSVReader reader = null;
		try {

			readAllDataAtOnce(csvFile);
			// reader = new CSVReader(new FileReader(csvFile));
			// List allData = reader.readAll();
			//
			//
			// for (int i = 0; i < 9; i++) {
			//
			// for()
			// System.out.println("size of Records ; " + allData.size());
			// }

			// String[] line;
			// while ((line = reader.readNext()) != null) {
			// System.out.println("Country [id= " + line[0] + ", code= " +
			// line[1] + " , name=" + line[2] + "]");
			// }
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static void readAllDataAtOnce(String file) {

		Map<Integer, Map<String, String>> rowColumnNameValueMap = new TreeMap<>();
		Map<String, String> columnNameValueMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

		try {
			// Create an object of file reader
			// class with CSV file as a parameter.
			FileReader filereader = new FileReader(file);

			// create csvReader object and skip first Line
			CSVReader csvReader = new CSVReaderBuilder(filereader).build();
			List<String[]> allData = csvReader.readAll();
			String[] headerRow = allData.get(0);
			String rowData[] = null;

			// print Data
			for (int rowIndex = 1; rowIndex < allData.size(); rowIndex++) {

				rowData = allData.get(rowIndex);

				for (int cellIndex = 0; cellIndex < rowData.length; cellIndex++) {
					columnNameValueMap.put(headerRow[cellIndex], rowData[cellIndex]);
				}

				rowColumnNameValueMap.put(Integer.valueOf(rowIndex), columnNameValueMap);

			}

			System.out.println(rowColumnNameValueMap.size());

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static Function<String, String[]> mapToItem = (line) -> {
		String[] p = line.split(",");// a CSV has comma separated lines
		return p;
	};

	public static void main1(String[] args) {
		CsvReadingWritingDemo app = new CsvReadingWritingDemo();
		app.write();
		// app.read();
	}

	private void write() {
		Instant start = Instant.now();
		int limit = 250000; // 10_000 100_000 1_000_000
		String fileName = "";
		Writer csvWriter = null;
		String fileHeader = "\"Id\",\"NVS_CORE_Novartis_Unique_ID__c\",\"IsPersonAccount\",\"NVS_CORE_STATUS__c\",\"xR1_Account_Status__c\",\"xR1_Data_Provider__c\",\"CreatedDate\",\"Salutation\",\"FirstName\",\"LastName\",\"Gender_vod__c\",\"Account_Type__c\",\"Name\",\"Beds__c\",\"NVS_CORE_Account_Subtype__c\",\"xR1_Account_SubType__c\",\"NVS_CORE_Account_Category__c\",\"xR31_Dependency__c\",\"NVS_CORE_Key_Account__c\",\"Phone\",\"PersonEmail\",\"NVS_CORE_Secondary_Email__c\",\"xR1_Organization_Email__c\",\"PersonMobilePhone\",\"NVS_CORE_HCO_Email__c\",\"NVS_CORE_National_Code__c\",\"xR3_ESP_National_Code__c\",\"xR1_Licence_Number__c\",\"NPI_vod__c\",\"NVS_CORE_Council_HCP_ID__c\",\"Specialty_1_vod__c\",\"Country_vod__c\",\"Country_vod__r.Name\",\"xR1_Country__c\",\"RecordTypeId\",\"RecordType.Name\",\"Primary_Parent_vod__c\",\"Primary_Parent_vod__r.Name\"";
		String data1 = "\"1010B00001jPQaOQAW\",\"ES-a1m0B000006TUMCQA4\",\"TRUE\",\"Active\",\"Active\",\"FALSE\",\"2016-08-29T12:03:19.000Z\",\"Mrs.\",\"Abc\",\"Def\",\"F\",\"Doctor\",\"Abc def\",\"\",\"CORE2\",\"CORE 1\",\"\",\"\",\"FALSE\",\"915504800\",\"sample@hotmail.com\",\"\",\"\",\"\",\"\",\"7897878768\",\"2354545\",\"282871046\",\"72751224Q\",\"282871046\",\"NEUMOLOGIA\",\"a49U0000000PFAXIA4\",\"ES\",\"United Arab Emirates\",\"1120B000000kLJyQAM\",\"HCP\",\"101U000000sw4FyIAI\",\"Neumologia - Hospital Universitario Fundacion Jimenez Diaz-UTE\"";
		// String data2 =
		// "\"1010B00001jPQaPQAW\",\"ES-a1m0B000006TUQKQA4\",\"TRUE\",\"Active\",\"Active\",\"FALSE\",\"2016-08-29T12:03:19.000Z\",\"Dr.\",\"Mno\",\"Pqr\",\"M\",\"Doctor\",\"Mno
		// Pqr\",\"\",\"\",\"\",\"\",\"\",\"FALSE\",\"965835011\",\"sample@hotmail.com\",\"\",\"\",\"\",\"\",\"\",\"\",\"32910066\",\"54487587N\",\"32910066\",\"MEDICINA
		// DE
		// FAMILIA\",\"a49U0000000PFAXIA4\",\"ES\",\"Spain\",\"1120B000000kLJyQAM\",\"HCP\",\"101U000000sw0gFIAQ\",\"Centro
		// Salud Calpe\"";
		// String data3 =
		// "\"1010B00001jPQvAQAW\",\"\",\"false\",\"\",\"Inactive\",\"false\",\"2016-08-29T14:08:58.000Z\",\"\",\"\",\"\",\"\",\"Hospital\",\"Hospital
		// Nuestra Señora de las Nieves\",\"\",\"\",\"S.
		// Cardiología\",\"\",\"Público\",\"false\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"Spain\",\"012U0000000UciqIAC\",\"Hospital\",\"\",\"\"";
		// String data4 =
		// "\"1010B00001jPQwmQAG\",\"\",\"false\",\"\",\"Active\",\"false\",\"2016-08-29T14:16:13.000Z\",\"\",\"\",\"\",\"\",\"Servicio\",\"Cardiologia
		// - Hospital General de la Palma\",\"\",\"\",\"S.
		// Cardiología\",\"\",\"Público\",\"false\",\"922185000\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"Spain\",\"012U0000000UcirIAC\",\"Hospital
		// Department\",\"001U000000sw4JFIAY\",\"Hospital General de la
		// Palma\"";

		try {

			if (fileHeader != null) {
				Charset charset = Charset.forName("UTF-8");
				fileName = "AccountFile1" + ".csv";
				csvWriter = new BufferedWriter(new OutputStreamWriter(
						new FileOutputStream("C:\\EBX\\Novartis DeepBlue\\DeepBlue\\Source File(s)\\" + fileName),
						charset));

				csvWriter.append(fileHeader);
				csvWriter.append("\n");

				for (int j = 0; j < limit; j++) {
					csvWriter.append(data1);
					csvWriter.append("\n");
					// csvWriter.append(data2);
					// csvWriter.append("\n");
					// csvWriter.append(data3);
					// csvWriter.append("\n");
					// csvWriter.append(data4);
					// csvWriter.append("\n");

				}

				csvWriter.flush();
				csvWriter.close();

				System.out.println(fileName + " written successfully on disk.");

			}

		} catch (Exception e) {

			System.out.println("Exception Occurred in writeOutputCSVFile() . Exception Details: " + e.getMessage());
			e.printStackTrace();
		}

		Instant stop = Instant.now();
		Duration d = Duration.between(start, stop);
		System.out.println("Wrote CSV for limit: " + limit);
		System.out.println("Elapsed: " + d);
	}

	private void read() {
		Instant start = Instant.now();
		int limit = 1000000;
		int count = 0;
		Path path = Paths.get("C:\\EBX\\Novartis DeepBlue\\DeepBlue\\Source File(s)\\AccountFile1.csv");
		try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8);) {
			CSVFormat format = CSVFormat.RFC4180.withFirstRecordAsHeader();
			CSVParser parser = CSVParser.parse(reader, format);
			// List<CSVRecord> csvRrecordList = parser.getRecords();

			// System.out.println(parser.getRecords());
			for (CSVRecord csvRecord : parser) {
				if (true) {

					System.out.println(csvRecord.toMap());

				}

				count++;
				if (count % 1_000 == 0) // Every so often, report progress.
				{
					System.out.println("Added Number of records: " + count);
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

		Instant stop = Instant.now();
		Duration d = Duration.between(start, stop);
		System.out.println("Read CSV for count: " + count);
		System.out.println("Elapsed: " + d);
	}
}