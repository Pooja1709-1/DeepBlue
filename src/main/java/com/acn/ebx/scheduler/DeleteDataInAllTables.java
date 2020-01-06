package com.acn.ebx.scheduler;

import com.onwbp.adaptation.Adaptation;
import com.onwbp.adaptation.AdaptationHome;
import com.onwbp.adaptation.AdaptationName;
import com.onwbp.adaptation.AdaptationTable;
import com.onwbp.adaptation.RequestResult;
import com.orchestranetworks.instance.BranchKey;
import com.orchestranetworks.instance.Repository;
import com.orchestranetworks.scheduler.ScheduledExecutionContext;
import com.orchestranetworks.scheduler.ScheduledTask;
import com.orchestranetworks.scheduler.ScheduledTaskInterruption;
import com.orchestranetworks.schema.Path;
import com.orchestranetworks.service.OperationException;
import com.orchestranetworks.service.Procedure;
import com.orchestranetworks.service.ProcedureContext;
import com.orchestranetworks.service.ProcedureResult;
import com.orchestranetworks.service.ProgrammaticService;
import com.orchestranetworks.service.Session;;

public class DeleteDataInAllTables extends ScheduledTask {

	public String dataspace;
	public String dataset;
	public String tablesList;

	public String getTablesList() {
		return tablesList;
	}

	public void setTablesList(String tablesList) {
		this.tablesList = tablesList;
	}

	public String getDataspace() {
		return dataspace;
	}

	public void setDataspace(String dataSpace) {
		this.dataspace = dataSpace;
	}

	public String getDataset() {
		return dataset;
	}

	public void setDataset(String dataSet) {
		this.dataset = dataSet;
	}

	@Override
	public void execute(ScheduledExecutionContext aContext) throws OperationException, ScheduledTaskInterruption {
		String root = "/root/";
		Session session = aContext.getSession();
		Repository aRepository = aContext.getRepository();
		AdaptationHome dataSpace = aRepository.lookupHome(BranchKey.forBranchName(this.dataspace));
		Adaptation currentDataset = dataSpace.findAdaptationOrNull(AdaptationName.forName(this.dataset));
		String[] tablesArray = tablesList.split(";");
		long d1 = System.nanoTime();
		for (String table : tablesArray) {
			Path tablePath = Path.parse(root + table);
			AdaptationTable currentTable = currentDataset.getTable(tablePath);

			ProgrammaticService svc = ProgrammaticService.createForSession(session, dataSpace);
			svc.setSessionTrackingInfo("Deleting " + table + " table data");

			Procedure proc = new Procedure() {
				final RequestResult result = currentTable.createRequestResult(null);

				public void execute(ProcedureContext procedureContext) throws Exception {
					try {
						for (Adaptation record; (record = result.nextAdaptation()) != null;) {
							final AdaptationName recordName = record.getAdaptationName();
							procedureContext.setAllPrivileges(true);
							procedureContext.doDelete(recordName, true);
						}
					} finally {
						result.close();
					}
				}

			};
			ProcedureResult result = svc.execute(proc);
			if (result.hasFailed()) {
				System.out.println("-------FAILED while deleting Data from " + table.toUpperCase() + " table-------");
			} else {
				System.out.println("-------SUCCESSFULLY deleted data from " + table.toUpperCase() + " table-------");
			}
		}
		long d2 = System.nanoTime();
		System.out.println(
				"Time taken to DELETE the data from all the tables: " + (d2 - d1) * (0.000000001) + " seconds");

	}

}
