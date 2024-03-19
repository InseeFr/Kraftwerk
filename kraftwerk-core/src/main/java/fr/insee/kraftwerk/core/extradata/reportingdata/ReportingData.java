package fr.insee.kraftwerk.core.extradata.reportingdata;

import lombok.Getter;
import lombok.Setter;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class ReportingData {
	private Path filepath;
  
	public List<ReportingDataUE> listReportingDataUE;
  
  public ReportingData() {
    this.listReportingDataUE = new ArrayList<>();
  }
  
  public ReportingData(Path filepath) {
    this.filepath = filepath;
    this.listReportingDataUE = new ArrayList<>();
  }

  
  public void addReportingDataUE(ReportingDataUE reportingDataUE) {
	  if (reportingDataUE != null)
    this.listReportingDataUE.add(reportingDataUE);
  }
  
  public void putReportingDataUE(List<ReportingDataUE> reportingDataUEs) {
    for (ReportingDataUE ue : reportingDataUEs) addReportingDataUE(ue);
  }
  
  public boolean containsReportingDataUE(String identifier) {
    ReportingDataUE result = this.listReportingDataUE.stream()
      .filter(reportingDataUE -> identifier.equals(reportingDataUE.getIdentifier()))
      .findAny().orElse(null);
    return (result != null);
  }
}
