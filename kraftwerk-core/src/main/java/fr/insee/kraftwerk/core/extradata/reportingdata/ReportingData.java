package fr.insee.kraftwerk.core.extradata.reportingdata;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class ReportingData {


	private Path filepath;
	
	public List<ReportingDataUE> listReportingDataUE;

	public ReportingData() {
		super();
		this.listReportingDataUE = new ArrayList<ReportingDataUE>();
	}

	public ReportingData(Path filepath) {
		super();
		this.filepath = filepath;
		this.listReportingDataUE = new ArrayList<ReportingDataUE>();
	}

	public Path getFilepath() {
		return filepath;
	}
	
	public void setFilepath(Path filepath) {
		this.filepath = filepath;
	}
	
	public List<ReportingDataUE> getListReportingDataUE() {
		return listReportingDataUE;
	}

	public void setListReportingDataUE(List<ReportingDataUE> listReportingDataUE) {
		this.listReportingDataUE = listReportingDataUE;
	}

	public void addReportingDataUE(ReportingDataUE reportingDataUE) {
		this.listReportingDataUE.add(reportingDataUE);
	}

	public void putReportingDataUE(List<ReportingDataUE> reportingDataUEs) {
		for (ReportingDataUE ue : reportingDataUEs) {
			this.listReportingDataUE.add(ue);
		}
	}

	public boolean containsReportingDataUE(String identifier) {
		ReportingDataUE result = this.listReportingDataUE.stream()
				.filter(reportingDataUE -> identifier.equals(reportingDataUE.getIdentifier()))
				.findAny().orElse(null);
		return result != null;
	}
	
}
