package com.acn.ebx.scheduler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.acn.ebx.model.CountryRef;
import com.acn.ebx.model.Customer;
import com.onwbp.adaptation.Adaptation;
import com.onwbp.adaptation.AdaptationName;
import com.onwbp.adaptation.AdaptationTable;
import com.orchestranetworks.addon.dataexchange.DataExchangeException;
import com.orchestranetworks.addon.dataexchange.transformation.ApplicationType;
import com.orchestranetworks.addon.dex.DataExchangeServiceFactory;
import com.orchestranetworks.addon.dex.DataExchangeSpec;
import com.orchestranetworks.addon.dex.configuration.CSVImportConfigurationSpec;
import com.orchestranetworks.addon.dex.configuration.ImportMode;
import com.orchestranetworks.addon.dex.configuration.Separator;
import com.orchestranetworks.addon.dex.mapping.ApplicationMapping;
import com.orchestranetworks.addon.dex.mapping.CSVField;
import com.orchestranetworks.addon.dex.mapping.CSVTable;
import com.orchestranetworks.addon.dex.mapping.CommonApplication;
import com.orchestranetworks.addon.dex.mapping.EBXField;
import com.orchestranetworks.addon.dex.mapping.EBXLinkField;
import com.orchestranetworks.addon.dex.mapping.EBXTable;
import com.orchestranetworks.addon.dex.mapping.FieldMapping;
import com.orchestranetworks.addon.dex.mapping.FieldMappingList;
import com.orchestranetworks.addon.dex.mapping.TableMapping;
import com.orchestranetworks.addon.dex.mapping.TableMappingList;
import com.orchestranetworks.addon.dex.result.DataExchangeResult;
import com.orchestranetworks.addon.dex.result.DataExchangeResult.CSVImport;
import com.orchestranetworks.instance.HomeKey;
import com.orchestranetworks.instance.Repository;
import com.orchestranetworks.scheduler.ScheduledExecutionContext;
import com.orchestranetworks.scheduler.ScheduledTask;
import com.orchestranetworks.scheduler.ScheduledTaskInterruption;
import com.orchestranetworks.schema.Path;
import com.orchestranetworks.schema.PathAccessException;
import com.orchestranetworks.schema.SchemaNode;
import com.orchestranetworks.service.OperationException;
import com.orchestranetworks.service.Session;

public class CustAutoImport extends ScheduledTask {
	private static final int CUSTOMER_ID_IDX = 0;
	private static final int CUSTOMER_COUNTRY_IDX = 1;
	private static final int CUSTOMER_NAME_IDX = 2;
	private static final int CUSTOMER_SCHOOL_IDX = 3;

	private static final int COUNTRYREF_ID_IDX = 0;
	private static final int COUNTRYREF_CODE_IDX = 1;
	private static final int COUNTRYREF_DESCRIPTION_IDX = 2;

	private static final String FILE_HEADER = "Id;CountryId;Name;School";
	private static final String NEW_LINE_SEPARATOR = "\r\n";

	public String srcFilePath;

	public String getSrcFilePath() {
		return srcFilePath;
	}

	public void setSrcFilePath(String srcFilePath) {
		this.srcFilePath = srcFilePath;
	}

	@Override
	public void execute(ScheduledExecutionContext context) throws OperationException, ScheduledTaskInterruption {
		System.out.println("*****************Starting Scheduler*************" + System.currentTimeMillis());
		try {

			List<Customer> customers = new ArrayList<Customer>();
			File fileInFolder = new File(srcFilePath/* "C:\\In\\CustomerIn.csv" */);
			if (fileInFolder.exists() && fileInFolder.canRead()) {
				Map<String, String> countryMap = readReference();
				readAndWriteSource(customers, countryMap);
				File importFromFolder = new File("C:\\ebx\\importFrom\\Customer.csv");
				if (importFromFolder.exists() && importFromFolder.canRead()) {

					importToEBX(context, importFromFolder);
				}
				long ct = System.currentTimeMillis();
				File Processed = new File("C:\\In\\Processed\\Processed" + ct + ".csv");
				fileInFolder.renameTo(Processed);
				fileInFolder.delete();
				System.out.println("*****************End Scheduler*************" + System.currentTimeMillis());
			}
		} catch (PathAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DataExchangeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * @param context
	 * @param importFromFolder
	 * @throws DataExchangeException
	 */
	private void importToEBX(ScheduledExecutionContext context, File importFromFolder) throws DataExchangeException {
		System.out.println("Processing start from intermediate table");
		Repository repo = context.getRepository();
		Adaptation currentDataset = repo.lookupHome(HomeKey.forBranchName("customer-dataspace"))
				.findAdaptationOrNull(AdaptationName.forName("customer-dataset"));

		Path sourceTablePath = Path.parse("/root/Customer");
		AdaptationTable currentTable = currentDataset.getTable(sourceTablePath);
		Path refTablePath = Path.parse("/root/CountryReference");
		AdaptationTable referenceAdaptationTable = currentDataset.getTable(refTablePath);

		List<CSVField> csvFields = new ArrayList<CSVField>();
		CSVField idField = new CSVField(0, "Id", "Id");
		CSVField countryField = new CSVField(1, "CountryId", "CountryId");
		CSVField nameField = new CSVField(2, "Name", "Name");
		CSVField schoolField = new CSVField(3, "School", "School");
		csvFields.add(idField);
		csvFields.add(countryField);
		csvFields.add(nameField);
		csvFields.add(schoolField);
		CSVTable csvTable = new CSVTable("Customer", csvFields);
		System.out.println(idField.toString() + countryField + nameField + schoolField);
		Session session = context.getSession();
		CSVImportConfigurationSpec importCSVSpec = new CSVImportConfigurationSpec(currentTable, csvTable, session);

		importCSVSpec.setImportedFile(importFromFolder);
		importCSVSpec.setSeparator(Separator.SEMICOLON.getSeparatorCharacter());
		importCSVSpec.setImportMode(ImportMode.INSERT_ONLY);
		// importCSVSpec.setFileEncoding("ISO_8859_1");
		importCSVSpec.setDataValidatedBeforeTransformation(false);

		CommonApplication sourceApplication = new CommonApplication("cust_source-1571654488571", ApplicationType.CSV);
		CommonApplication targetApplication = new CommonApplication("cust_target-1571654550261", ApplicationType.EBX);

		TableMappingList<CSVField, EBXField> tableMappingList = new TableMappingList<CSVField, EBXField>();
		AdaptationTable targetAdaptationTable = importCSVSpec.getCurrentDataset()
				.getTable(Path.parse("/root/Customer"));
		EBXField targetIdField = new EBXField(
				targetAdaptationTable.getTableOccurrenceRootNode().getNode(Path.parse("/root/Customer/id")));
		EBXField targetNameField = new EBXField(
				targetAdaptationTable.getTableOccurrenceRootNode().getNode(Path.parse("/root/Customer/name")));
		EBXField targetSchoolField = new EBXField(
				targetAdaptationTable.getTableOccurrenceRootNode().getNode(Path.parse("/root/Customer/School")));
		EBXField targetCountryField = new EBXField(
				targetAdaptationTable.getTableOccurrenceRootNode().getNode(Path.parse("/root/Customer/Country")));

		List<EBXField> ebxFields = new ArrayList<EBXField>();
		ebxFields.add(targetIdField);
		ebxFields.add(targetNameField);
		ebxFields.add(targetSchoolField);
		ebxFields.add(targetCountryField);
		EBXTable tarEBXTable = new EBXTable(targetAdaptationTable, ebxFields);

		// FieldMapping<CSVField, EBXField> idFieldMapping = new
		// FieldMapping<CSVField, EBXField>(idField, targetIdField);
		FieldMapping<CSVField, EBXField> nameFieldMapping = new FieldMapping<CSVField, EBXField>(nameField,
				targetNameField);
		FieldMapping<CSVField, EBXField> schoolFieldMapping = new FieldMapping<CSVField, EBXField>(schoolField,
				targetSchoolField);

		FieldMappingList<CSVField, EBXField> fieldMappingList = new FieldMappingList<CSVField, EBXField>();
		// fieldMappingList.add(idFieldMapping);
		fieldMappingList.add(nameFieldMapping);
		fieldMappingList.add(schoolFieldMapping);

		// FK reference mapping
		FieldMapping<CSVField, EBXField> fkClassFieldMapping = new FieldMapping<CSVField, EBXField>(countryField,
				targetCountryField);
		SchemaNode fkClassNode = targetCountryField.getSchemaNode();
		SchemaNode tableReferenceNode = fkClassNode.getFacetOnTableReference().getTableNode();
		SchemaNode idReferenceTblNode = tableReferenceNode.getNode(Path.parse("/root/CountryReference/Id"));
		EBXLinkField idLinkField = new EBXLinkField(referenceAdaptationTable, idReferenceTblNode, Locale.US);
		fkClassFieldMapping.setReferenceField(idLinkField);
		fieldMappingList.add(fkClassFieldMapping);
		//

		tableMappingList.add(new TableMapping<CSVField, EBXField>(csvTable, tarEBXTable, fieldMappingList));

		ApplicationMapping<CSVField, EBXField> applicationMapping = new ApplicationMapping<CSVField, EBXField>(
				sourceApplication, targetApplication, tableMappingList);

		DataExchangeSpec dataExchangeSpec = new DataExchangeSpec();
		dataExchangeSpec.setApplicationMapping(applicationMapping);
		dataExchangeSpec.setConfigurationSpec(importCSVSpec);
		DataExchangeResult.CSVImport result = (CSVImport) DataExchangeServiceFactory.getDataExchangeService()
				.execute(dataExchangeSpec);
		long ct = System.currentTimeMillis();
		File Processed = new File("C:\\ebx\\importFrom\\Processed\\Processed" + ct + ".csv");
		importFromFolder.renameTo(Processed);
		importFromFolder.deleteOnExit();
	}

	/**
	 * @return
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	private Map<String, String> readReference() throws FileNotFoundException, IOException {
		BufferedReader fileRefReader = null;
		List<CountryRef> countryRefs = new ArrayList<CountryRef>();
		String lineRef = "";
		fileRefReader = new BufferedReader(new FileReader("C:\\In\\CountryRef.csv"));
		fileRefReader.readLine();
		System.out.println("file read start");
		Map<String, String> countryMap = new HashMap<String, String>();
		while ((lineRef = fileRefReader.readLine()) != null) {
			String[] tokens = lineRef.split(";");
			if (tokens.length > 0) {
				CountryRef countryRef = new CountryRef(Long.parseLong(tokens[COUNTRYREF_ID_IDX]),
						tokens[COUNTRYREF_CODE_IDX], tokens[COUNTRYREF_DESCRIPTION_IDX]);
				countryRefs.add(countryRef);
				countryMap.put(countryRef.getDescription(), Long.toString(countryRef.getId()));
			}
		}
		fileRefReader.close();
		return countryMap;
	}

	/**
	 * @param customers
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	private void readAndWriteSource(List<Customer> customers, Map<String, String> countryMap)
			throws FileNotFoundException, IOException {
		BufferedReader fileReader;
		String line;
		fileReader = new BufferedReader(new FileReader("C:\\In\\CustomerIn.csv"));
		fileReader.readLine();
		FileWriter fileWriter;
		fileWriter = new FileWriter("C:\\ebx\\importFrom\\Customer.csv");
		fileWriter.append(FILE_HEADER.toString());
		fileWriter.append(NEW_LINE_SEPARATOR);
		while ((line = fileReader.readLine()) != null) {
			String[] tokens = line.split(";");
			if (tokens.length > 0) {
				// Customer customer = new
				// Customer(Long.parseLong(tokens[CUSTOMER_ID_IDX]),
				// tokens[CUSTOMER_COUNTRY_IDX],
				// tokens[CUSTOMER_NAME_IDX], tokens[CUSTOMER_SCHOOL_IDX]);
				Customer customer = new Customer();
				customers.add(customer);
				fileWriter.append(String.valueOf(customer.getCustomerID()));
				fileWriter.append(";");
				fileWriter.append(countryMap.get(customer.getCountryCode()));

				fileWriter.append(NEW_LINE_SEPARATOR);
			}

		}
		fileReader.close();
		fileWriter.flush();
		fileWriter.close();
	}

}
