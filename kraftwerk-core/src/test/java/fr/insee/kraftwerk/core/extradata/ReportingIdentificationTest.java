package fr.insee.kraftwerk.core.extradata;

import fr.insee.kraftwerk.core.extradata.reportingdata.ReportingIdentification;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ReportingIdentificationTest {
	
	@Test
	void getOutcomeSpottingTest() {
		ReportingIdentification reportingIdentification = new ReportingIdentification("DESTROY","","","","","","");
		Assertions.assertEquals("DESTROY",reportingIdentification.getOutcomeSpotting());

		reportingIdentification = new ReportingIdentification("UNIDENTIFIED","","","","","","");
		Assertions.assertEquals("UNIDENTIF",reportingIdentification.getOutcomeSpotting());

		reportingIdentification = new ReportingIdentification("IDENTIFIED","NACC","NOORDINARY","","","","");
		Assertions.assertEquals("NACCNO",reportingIdentification.getOutcomeSpotting());

		reportingIdentification = new ReportingIdentification("IDENTIFIED","NACC","ABSORBED","","","","");
		Assertions.assertEquals("NACCABS",reportingIdentification.getOutcomeSpotting());

		reportingIdentification = new ReportingIdentification("IDENTIFIED","NACC","ORDINARY","VACANT","","","");
		Assertions.assertEquals("NACCVAC",reportingIdentification.getOutcomeSpotting());

		reportingIdentification = new ReportingIdentification("IDENTIFIED","NACC","ORDINARY","SECONDARY","","","");
		Assertions.assertEquals("NACCSEC",reportingIdentification.getOutcomeSpotting());

		reportingIdentification = new ReportingIdentification("IDENTIFIED","NACC","ORDINARY","DK","UNIDENTIFIED","","");
		Assertions.assertEquals("NACCDKUNIDENT",reportingIdentification.getOutcomeSpotting());

		reportingIdentification = new ReportingIdentification("IDENTIFIED","NACC","ORDINARY","PRIMARY","IDENTIFIED","","");
		Assertions.assertEquals("NACCPRIDENT",reportingIdentification.getOutcomeSpotting());

		reportingIdentification = new ReportingIdentification("IDENTIFIED","ACC","ORDINARY","OCCASIONAL","IDENTIFIED","","");
		Assertions.assertEquals("ACCOCCIDENT",reportingIdentification.getOutcomeSpotting());

	}


	
}