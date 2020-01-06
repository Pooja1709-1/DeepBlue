package com.acn.ebx.scheduler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.acn.ebx.service.CSVWriterService;
import com.acn.ebx.service.MasterReferenceSheetReader;
import com.acn.ebx.utility.EBXProductConstants;
import com.acn.ebx.utility.PropertyMapper;
import com.acn.ebx.utility.UtilityFunctions;
import com.onwbp.adaptation.Adaptation;
import com.onwbp.adaptation.AdaptationHome;
import com.onwbp.adaptation.AdaptationName;
import com.onwbp.adaptation.AdaptationTable;
import com.onwbp.base.text.UserMessageRefXML;
import com.orchestranetworks.addon.dataexchange.DataExchangeException;
import com.orchestranetworks.addon.dataexchange.transformation.ApplicationType;
import com.orchestranetworks.addon.dex.DataExchangeServiceFactory;
import com.orchestranetworks.addon.dex.DataExchangeSpec;
import com.orchestranetworks.addon.dex.common.generation.ApplicationMappingHelperFactory;
import com.orchestranetworks.addon.dex.configuration.ImportMode;
import com.orchestranetworks.addon.dex.configuration.XMLImportConfigurationSpec;
import com.orchestranetworks.addon.dex.mapping.ApplicationMapping;
import com.orchestranetworks.addon.dex.mapping.CommonApplication;
import com.orchestranetworks.addon.dex.mapping.XMLTable;
import com.orchestranetworks.addon.dex.result.DataExchangeResult;
import com.orchestranetworks.addon.dex.result.DataExchangeResult.XMLImport;
import com.orchestranetworks.instance.HomeCreationSpec;
import com.orchestranetworks.instance.HomeKey;
import com.orchestranetworks.instance.Repository;
import com.orchestranetworks.scheduler.ScheduledExecutionContext;
import com.orchestranetworks.scheduler.ScheduledTask;
import com.orchestranetworks.scheduler.ScheduledTaskInterruption;
import com.orchestranetworks.schema.Path;
import com.orchestranetworks.schema.PathAccessException;
import com.orchestranetworks.service.OperationException;
import com.orchestranetworks.service.Procedure;
import com.orchestranetworks.service.ProcedureContext;
import com.orchestranetworks.service.ProcedureResult;
import com.orchestranetworks.service.ProgrammaticService;
import com.orchestranetworks.service.Session;

public class CustomerImport extends ScheduledTask {
	public String srcFilePath;
	public String dataspace;
	public String dataset;
	public String srcLogicalName;
	public String targetLogicalName;
	public String country;
	public String spainCustomerIdSequence;

	UtilityFunctions utility = new UtilityFunctions();

	public String getSrcFilePath() {
		return srcFilePath;
	}

	public void setSrcFilePath(String srcFilePath) {
		this.srcFilePath = srcFilePath;
	}

	public String getDataspace() {
		return dataspace;
	}

	public void setDataspace(String dataspace) {
		this.dataspace = dataspace;
	}

	public String getDataset() {
		return dataset;
	}

	public void setDataset(String dataset) {
		this.dataset = dataset;
	}

	public String getSrcLogicalName() {
		return srcLogicalName;
	}

	public void setSrcLogicalName(String srcLogicalName) {
		this.srcLogicalName = srcLogicalName;
	}

	public String getTargetLogicalName() {
		return targetLogicalName;
	}

	public void setTargetLogicalName(String targetLogicalName) {
		this.targetLogicalName = targetLogicalName;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getSpainCustomerIdSequence() {
		return spainCustomerIdSequence;
	}

	public void setSpainCustomerIdSequence(String spainCustomerIdSequence) {
		this.spainCustomerIdSequence = spainCustomerIdSequence;
	}

	@Override
	public void execute(ScheduledExecutionContext context) throws OperationException, ScheduledTaskInterruption {
		System.out.println("****************************Starting Scheduler**************************** --> "
				+ System.currentTimeMillis());
		long d1 = System.currentTimeMillis();
		String[] srcFilePathArray = null;
		File fileInFolder = null;
		String targetType = "";
		String customerSequence = "";
		String[] targetTypeArray = null;
		TreeMap<String, String> propertyMap = null;
		CSVWriterService csvWriter = null;
		MasterReferenceSheetReader masterReader = null;
		PropertyMapper propertyMapper = null;
		Map<String, List<Map<String, String>>> referenceDataMap = null;
		Map<String, String> mapOfCommonCodes = null;
		List<String> listOfFieldsMappings = null;
		String outputXMLFilePath = "";
		File importFromFolder = null;
		String[] srcLogicalNameArray = null;
		String[] targetArray = null;
		String[] countryArray = null;
		List<String> targetTypeList = new ArrayList<>();
		Map<String, Integer> countMap = new HashMap<>();
		int autoGeneratedValue = 0;

		Integer sourceCount = 0;
		Integer targetCount = 0;
		Integer targetErrorCount = 0;
		// Integer sourceAccountCount = 0;
		Integer previoussourceCount = 0;
		Integer previoustargetCount = 0;
		Integer previoustargetErrorCount = 0;

		boolean doMerge = true;
		List<String> dbErrorList = new ArrayList<>();
		Repository repository = context.getRepository();
		AdaptationHome parentDataSpace = repository.lookupHome(HomeKey.forBranchName(dataspace));
		long t1 = System.currentTimeMillis();
		// Create Child Dataspace from Parent Dataspace
		AdaptationHome childDataSpace = createChildDSFromParentDS(repository, dataspace, parentDataSpace, context);
		long t2 = System.currentTimeMillis();
		if (childDataSpace == null) {
			System.out.println("ERROR::Creation of Child DataSpace is Unsuccessful!!");
			return;
		}
		System.out.println("INFO::Time taken to create CHILD DATASPACE - " + (t2 - t1) + " milli seconds.");
		try {

			if (srcFilePath != null && !srcFilePath.equals("")) {

				csvWriter = new CSVWriterService();
				masterReader = new MasterReferenceSheetReader();
				propertyMapper = new PropertyMapper();
				propertyMap = propertyMapper.getPropertyMap();

				referenceDataMap = masterReader.getReferenceDataMap(propertyMap);
				mapOfCommonCodes = masterReader.getMasterDataMap(propertyMap);

				outputXMLFilePath = propertyMap.get("MasterOutPutFolderLocation");
				srcLogicalNameArray = srcLogicalName.split(";");
				srcFilePathArray = srcFilePath.split(";");

				countryArray = country.split(";");

				// Path dir = Paths.get(outputXMLFilePath);
				// new FileTriggerService(dir).processEvents(context,
				// outputXMLFilePath, srcLogicalNameArray);

				for (String countryVal : countryArray) {
					countMap = null;
					if (EBXProductConstants.Spain.equalsIgnoreCase(countryVal)) {
						customerSequence = spainCustomerIdSequence;
					} else if (EBXProductConstants.Italy.equalsIgnoreCase(countryVal)) {
						customerSequence = propertyMap.get("Italy_Customer_ID_Sequence");
					}

					if (customerSequence != null) {

						autoGeneratedValue = Integer.parseInt(customerSequence);
					}

					listOfFieldsMappings = masterReader.getMappingSheetData(countryVal, propertyMap);

					for (String srcFile : srcFilePathArray) {

						targetTypeList = new ArrayList<>();
						targetType = "";
						if (EBXProductConstants.Spain.equalsIgnoreCase(countryVal)) {

							if (srcFile.contains("AccountFile")) {

								for (String sourceLogicalName : srcLogicalNameArray) {

									if (utility.containsIgnoreCase(sourceLogicalName, "Customer")) {
										targetTypeList.add("Customer");
									}

									else if (utility.containsIgnoreCase(sourceLogicalName, "hco")) {
										targetTypeList.add("HCO");
									}

									else if (utility.containsIgnoreCase(sourceLogicalName, "hcp")) {
										targetTypeList.add("HCP");
									} else if (utility.containsIgnoreCase(sourceLogicalName, "remoteKey")) {
										targetTypeList.add("Remote Key");
									}

									else if (utility.containsIgnoreCase(sourceLogicalName, "specialty")) {
										targetTypeList.add("Specialty");
									} else if (utility.containsIgnoreCase(sourceLogicalName, "credential")) {
										targetTypeList.add("Credential");
									}

									else if (utility.containsIgnoreCase(sourceLogicalName, "communication")) {
										targetTypeList.add("Communication");
									}
								}

								fileInFolder = new File(srcFile);

							} else if (srcFile.contains("ChildAccount")) {
								targetType = "Affiliation";
								fileInFolder = new File(srcFile);

							} else if (srcFile.contains("Address")) {
								targetType = "Address";
								fileInFolder = new File(srcFile);
							} else
								continue;

						} else if (EBXProductConstants.Italy.equalsIgnoreCase(countryVal)) {

							if (utility.containsIgnoreCase(srcFile, "Workplace")) {

								targetTypeList.add("Customer");
								// targetTypeList.add("HCO");

							} else if (utility.containsIgnoreCase(srcFile, "Individual")) {

								targetTypeList.add("Customer");
								// targetTypeList.add("HCP");

							} else if (utility.containsIgnoreCase(srcFile, "ItalyAddress")) {

								// targetType = "Address";

							} else
								continue;
							fileInFolder = new File(srcFile);

						}

						if (fileInFolder.exists() && fileInFolder.canRead()) {

							if (targetTypeList != null && !targetTypeList.isEmpty()) {

								targetType = StringUtils.join(targetTypeList, ";");
							}

							if (EBXProductConstants.Italy.equalsIgnoreCase(countryVal) && countMap != null
									&& !countMap.isEmpty() && (utility.containsIgnoreCase(srcFile, "individual")
											|| utility.containsIgnoreCase(srcFile, "workplace"))) {

								autoGeneratedValue = countMap.get("PreviousIncrementedValue");

							} else {

								autoGeneratedValue = Integer.parseInt(customerSequence);

							}

							countMap = csvWriter.writeCustomerOutputFile(countryVal, targetType, referenceDataMap,
									mapOfCommonCodes, listOfFieldsMappings, propertyMap, srcFile, outputXMLFilePath,
									autoGeneratedValue);

							if (targetType != null && !targetType.equals("")) {

								targetTypeArray = targetType.split(";");

								if (targetTypeArray != null) {

									for (String target : targetTypeArray) {

										// importFromFolder = new
										// File(outputXMLFilePath + target +
										// ".xml");
										// importToEBX(context,
										// importFromFolder, target,
										// srcLogicalNameArray,
										// outputXMLFilePath);

										if (target != null) {
											importFromFolder = new File(outputXMLFilePath + target + ".xml");

											boolean success = importToEBX(context, importFromFolder, target,
													srcLogicalNameArray, outputXMLFilePath, childDataSpace,
													dbErrorList);

											if (!success && doMerge)
												doMerge = false;

											if (success)
												if (target.equals("HCP")) {
													sourceCount = countMap.get("sourceCountHCP");

												} else if (target.equals("HCO")) {
													sourceCount = countMap.get("sourceCountHCO");
												}

												else if (target.equals("Customer")
														&& countryVal.equals(EBXProductConstants.Italy)
														&& srcFile.contains("Workplace")
														|| srcFile.contains("Individual")) {

													if (previoussourceCount != 0) {
														sourceCount = previoussourceCount + countMap.get("sourceCount");
														targetCount = previoustargetCount
																+ countMap.get(target + "targetCount");
														targetErrorCount = previoustargetErrorCount
																+ countMap.get(target + "targetErrorCount");
														context.addExecutionInformation("For " + countryVal + " "
																+ target + " target " + " , Source count records are "
																+ sourceCount + " ;Successfully uploaded records for "
																+ target + " is " + targetCount
																+ " ;Error in uploading " + targetErrorCount
																+ " records ");
														previoussourceCount = 0;
													} else {
														previoussourceCount = countMap.get("sourceCount");
														previoustargetCount = countMap.get(target + "targetCount");
														previoustargetErrorCount = countMap
																.get(target + "targetErrorCount");

														// break;
													}
													break;
												} else {
													sourceCount = countMap.get("sourceCount");
												}
											// if (target.equals("Customer")
											// &&
											// countryVal.equals(EBXProductConstants.Italy)
											// &&
											// srcFile.contains("Individual")) {
											// targetCount = countMap.get(target
											// + "targetCount");
											//
											// }
											targetCount = countMap.get(target + "targetCount");
											targetErrorCount = countMap.get(target + "targetErrorCount");
											if (target.equalsIgnoreCase("Communication")
													|| target.equalsIgnoreCase("Credential")) {

												context.addExecutionInformation("For " + countryVal + " " + target
														+ " target " + " , Source count records are " + sourceCount
														+ " ;Successfully uploaded records for " + target + " is "
														+ targetCount + " ;No " + target + " Data found for  "
														+ targetErrorCount + " records ");
											} else {
												context.addExecutionInformation("For " + countryVal + " " + target
														+ " target " + " , Source count records are " + sourceCount
														+ " ;Successfully uploaded records for " + target + " is "
														+ targetCount + " ;Error in uploading " + targetErrorCount
														+ " records ");
											}

										}

									}

								}
							}

						}

					}
				}

				long d2 = System.currentTimeMillis();

				utility.getExecutionTime(d1, d2, "For Data Uploading");

			}

		} catch (PathAccessException e) {

			e.printStackTrace();

		} catch (FileNotFoundException e) {

			e.printStackTrace();
		} catch (URISyntaxException e) {

			e.printStackTrace();
		} catch (IOException e) {

			e.printStackTrace();
		} catch (DataExchangeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// CODE BLOCK TO MERGE/CLOSE CHILD DATASPACE.
		{
			long time1 = System.currentTimeMillis();
			try {
				// Merge or Close Child DataSpace
				mergeOrCloseChildDataSpace(context, parentDataSpace, childDataSpace, repository, doMerge);
			} catch (OperationException oe) {
				System.out.print("EXCEPTION::Exception While Merging Child Dataspace:" + oe.getLocalizedMessage());
			}
			long time2 = System.currentTimeMillis();
			System.out
					.println("INFO::Time taken to merge/close CHILD DATASPACE: " + (time2 - time1) + " milli seconds.");
		}

		if (dbErrorList.size() > 0) {
			System.out.println("\n********************************DB ERRORS LIST**********************************");
			for (String error : dbErrorList) {
				System.out.println(" > " + error);
			}
			System.out.println("********************************************************************************\n");
		}

		System.out.println("****************************End Scheduler**************************** --> "
				+ System.currentTimeMillis());

	}
	// public void threadPoolExecutor() {
	//
	// ExecutorService executor = Executors.newFixedThreadPool(5);
	// for (int i = 0; i < 10; i++) {
	// Runnable worker = new CustomerImport();
	// executor.execute(worker);
	// }
	// executor.shutdown();
	// while (!executor.isTerminated()) {
	// }
	// System.out.println("Finished all threads");
	// }

	/*
	 * CREATES CHILD DATASPACE FROM PARENT DATASPACE.
	 */
	private AdaptationHome createChildDSFromParentDS(Repository repo, String dataspace, AdaptationHome parentDataSpace,
			ScheduledExecutionContext context) throws OperationException {
		HomeCreationSpec spec = new HomeCreationSpec();
		spec.setParent(parentDataSpace);
		String childDS = dataspace + System.currentTimeMillis();
		spec.setKey(HomeKey.forBranchName(childDS));
		// spec.setOwner(Profile.ADMINISTRATOR);
		spec.setOwner(parentDataSpace.getOwner());
		spec.setHomeToCopyPermissionsFrom(parentDataSpace);
		AdaptationHome childDataSpace = null;
		try {
			childDataSpace = repo.createHome(spec, context.getSession());
		} catch (OperationException oe) {
			System.out.println("EXCEPTION::Error occured while creating Child DataSpace:" + oe.getMessage());
			return null;
		}
		System.out.println("INFO::Created Child DataSpace name - " + childDataSpace.toDisplayString(Locale.US));
		return childDataSpace;
	}

	/*
	 * MERGES/CLOSES CHILD DATASPACE BASED ON doMerge VARIABLE.
	 */
	private void mergeOrCloseChildDataSpace(ScheduledExecutionContext context, AdaptationHome parentDataSpace,
			AdaptationHome childDataSpace, Repository repository, boolean doMerge) throws OperationException {
		if (doMerge) {
			// PLACE TO WRITE MERGING CHILD DATASPACE CODE
			ProgrammaticService svc = ProgrammaticService.createForSession(context.getSession(), parentDataSpace);
			Procedure proc = new Procedure() {
				@Override
				public void execute(ProcedureContext procedureContext) throws Exception {
					try {
						procedureContext.doMergeToParent(childDataSpace);
					} catch (Exception e) {
						System.out.println("EXCEPTION::Exception occured while merging Child Dataspace: "
								+ e.getLocalizedMessage());
					}
				}

			};
			ProcedureResult mergeResult = svc.execute(proc);
			if (mergeResult.hasFailed()) {
				System.out.println("FAILED::Merging Child DataSpace procedure is FAILED");
			} else {
				System.out.println("SUCCESS::Child Dataspace merged successfully with it's Parent Dataspace.");
			}

		} else {
			// Closes Child DataSpace
			repository.closeHome(childDataSpace, context.getSession());
		}

	}

	/**
	 * @param context
	 * @param importFromFolder
	 * @throws DataExchangeException
	 */
	@SuppressWarnings("rawtypes")
	private boolean importToEBX(ScheduledExecutionContext context, File importFromFolder, String target,
			String[] srcLogicalNameArray, String outputXMLFilePath, AdaptationHome childDataSpace,
			List<String> dbErrorList) throws DataExchangeException {
		System.out.println("Data Uploading Started for : " + target);
		long d1 = System.currentTimeMillis();
		String root = "/root/";
		CommonApplication sourceApplication = null;
		CommonApplication targetApplication = null;
		String ebxTarget = target;

		Repository repo = context.getRepository();
		// Adaptation currentDataset =
		// repo.lookupHome(HomeKey.forBranchName(dataspace))
		// .findAdaptationOrNull(AdaptationName.forName(dataset));
		Adaptation currentDataset = repo.lookupHome(childDataSpace.getKey())
				.findAdaptationOrNull(AdaptationName.forName(dataset));
		Session session = context.getSession();

		ebxTarget = utility.getCamelCasedString(target);

		Path ebxTablePath = Path.parse(root + ebxTarget);
		AdaptationTable currentTable = currentDataset.getTable(ebxTablePath);
		List<Path> pathList = new ArrayList<Path>();
		pathList.add(ebxTablePath);
		Set<Path> ebxTablePaths = new LinkedHashSet<Path>();

		XMLTable srcTable = new XMLTable(root + target);
		XMLImportConfigurationSpec importXMLSpec = new XMLImportConfigurationSpec(currentTable, srcTable, session);
		importXMLSpec.setImportedFile(importFromFolder);
		importXMLSpec.setImportMode(ImportMode.INSERT_ONLY);
		// importXMLSpec.se
		importXMLSpec.setHeaderUsed(false);

		List<String> srcLogicalNameList = Arrays.asList(srcLogicalNameArray).stream()
				.filter(s -> utility.containsIgnoreCase(s, target)).collect(Collectors.toList());
		if (srcLogicalNameList != null && srcLogicalNameList.size() > 0) {
			sourceApplication = new CommonApplication(srcLogicalNameList.get(0), ApplicationType.XML);
		}

		targetApplication = new CommonApplication(targetLogicalName, ApplicationType.EBX);

		ApplicationMapping appMappingConfig = ApplicationMappingHelperFactory.getApplicationMappingForXMLImportHelper()
				.getApplicationMapping(importXMLSpec, sourceApplication, targetApplication, ebxTablePaths);

		DataExchangeSpec dataExchangeSpec = new DataExchangeSpec();
		dataExchangeSpec.setApplicationMapping(appMappingConfig);
		dataExchangeSpec.setConfigurationSpec(importXMLSpec);
		try {
			DataExchangeResult.XMLImport der = (XMLImport) DataExchangeServiceFactory.getDataExchangeService()
					.execute(dataExchangeSpec);
			if (der.getErrorMessages().hasNext()) {
				// System.out.println("USER MESSGAE ERRORS USING FOR EACH:");
				// der.getErrorMessages().forEachRemaining(
				// um -> System.out.println(um.toString() + " -- " +
				// um.getSeverity() + " -- " + um.isError()));
				System.out.println(
						"-------------------------------------------------------------------------------------------");
				UserMessageRefXML um = (UserMessageRefXML) der.getErrorMessages().next();
				dbErrorList.add(um.formatMessage(Locale.US));
				System.out.println("ERROR MESSAGE:" + um.formatMessage(Locale.US));
				System.out.println(
						"-------------------------------------------------------------------------------------------");
				return false;
			}
			/*
			 * if (der.getResults().hasNext()) { ImportResult result =
			 * der.getResults().next();
			 * System.out.println("No. of Created Records:" +
			 * result.getNumberOfCreatedRecords());
			 * System.out.println("String representation of Object:" +
			 * result.toString()); System.out.println("Processed Records :" +
			 * result.getNumberOfProcessedRecords());
			 * System.out.println("Invalid Records :" +
			 * result.getNumberOfInvalidRecords());
			 * System.out.println("Error Messages :" +
			 * result.getErrorMessages()); }
			 */
		} catch (Exception e) {
			System.out.println("EXCEPTION CAUGHT IN Exception CATCH BLOCK");
		}
		// long ct = System.currentTimeMillis();
		// File Processed = new File(outputXMLFilePath + "Processed\\" +
		// target
		// + ct + ".xml");
		// importFromFolder.renameTo(Processed);
		// importFromFolder.deleteOnExit();

		long d2 = System.currentTimeMillis();

		utility.getExecutionTime(d1, d2, "For Data Uploading of " + target);
		return true;
	}

}
