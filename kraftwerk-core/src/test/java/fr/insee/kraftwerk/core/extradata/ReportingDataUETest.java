package fr.insee.kraftwerk.core.extradata;

import fr.insee.kraftwerk.core.extradata.reportingdata.ReportingData;
import fr.insee.kraftwerk.core.extradata.reportingdata.ReportingDataUE;
import fr.insee.kraftwerk.core.extradata.reportingdata.State;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class ReportingDataUETest {
	
	public static List<ReportingDataUE> createFakeReportingDataUEs() {
		List<ReportingDataUE> result = new ArrayList<>();
		List<State> states = new ArrayList<>();
		
		// First UE
		ReportingDataUE ue1 = new ReportingDataUE("Report0001");
		states.add(new State("NVM", 1L));
		ue1.putStates(states);
		result.add(ue1);
		states.clear();

		// Second UE
		ReportingDataUE ue2 = new ReportingDataUE("Report0002");
		states.add(new State("NVM", 1L));
		states.add(new State("ANV", 1L));
		states.add(new State("AOC", 1L));
		states.add(new State("WFT", 1L));
		states.add(new State("FIN", 1L));
		ue2.putStates(states);
		result.add(ue2);
		states.clear();

		// Third UE
		ReportingDataUE ue3 = new ReportingDataUE("Report0003");
		states.add(new State("NVM", 1L));
		states.add(new State("ANV", 1L));
		states.add(new State("PRC", 1L));
		states.add(new State("WFT", 1L));
		states.add(new State("FIN", 1L));
		ue3.putStates(states);
		result.add(ue3);
		states.clear();

		// Fourth UE
		ReportingDataUE ue4 = new ReportingDataUE("Report0004");
		states.add(new State("NVM", 1L));
		states.add(new State("NVM", 1L));
		states.add(new State("NVM", 1L));
		states.add(new State("NVM", 1L));
		states.add(new State("NVM", 1L));
		states.add(new State("NVM", 1L));
		states.add(new State("NVM", 1L));
		states.add(new State("NVM", 1L));
		ue4.putStates(states);
		result.add(ue4);
		states.clear();
		
		return result;
		
	}

	@Test
	void containsReportingDataUETest() {
		List<ReportingDataUE> listReportingDataUE = new ArrayList<>();
		listReportingDataUE.add(new ReportingDataUE("Report0001"));
		listReportingDataUE.add(new ReportingDataUE("Report0002"));
		listReportingDataUE.add(new ReportingDataUE("Report0003"));
		listReportingDataUE.add(new ReportingDataUE("Report0004"));
		listReportingDataUE.add(new ReportingDataUE("Report0005"));
		listReportingDataUE.add(new ReportingDataUE("Report0006"));
		listReportingDataUE.add(new ReportingDataUE("Report0007"));
		listReportingDataUE.add(new ReportingDataUE("Report0008"));
		listReportingDataUE.add(new ReportingDataUE("Report0009"));
		ReportingData reportingData = new ReportingData(Path.of("test"), listReportingDataUE);
		Assertions.assertTrue(reportingData.containsReportingDataUE("Report0001"));
		Assertions.assertFalse(reportingData.containsReportingDataUE("Report9999"));
		
	}
	
}