package fr.insee.kraftwerk.core.extradata;

import fr.insee.kraftwerk.core.extradata.reportingdata.ReportingIdentification;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ReportingIdentificationTest {

	// Moog case : no identificationConfiguration and no identification
	@Test
	void getOutcomeSpottingTest_moog_no_identification() {
		ReportingIdentification reportingIdentification = new ReportingIdentification(null,null,null,null,null,null,null);
        Assertions.assertNull(reportingIdentification.getOutcomeSpotting(null));
	}

	// Case old file : no identificationConfiguration but identification is present
	@Test
	void getOutcomeSpottingTest_old_files() {
		String identificationConfiguration = null;
		ReportingIdentification reportingIdentification = new ReportingIdentification("DESTROY","","","","","","");
		Assertions.assertEquals("DESTROY",reportingIdentification.getOutcomeSpotting(identificationConfiguration));

		reportingIdentification = new ReportingIdentification("UNIDENTIFIED","","","","","","");
		Assertions.assertEquals("UNIDENTIF",reportingIdentification.getOutcomeSpotting(identificationConfiguration));

		reportingIdentification = new ReportingIdentification("IDENTIFIED","NACC","NOORDINARY","","","","");
		Assertions.assertEquals("NACCNO",reportingIdentification.getOutcomeSpotting(identificationConfiguration));

		reportingIdentification = new ReportingIdentification("IDENTIFIED","NACC","ABSORBED","","","","");
		Assertions.assertEquals("NACCABS",reportingIdentification.getOutcomeSpotting(identificationConfiguration));

		reportingIdentification = new ReportingIdentification("IDENTIFIED","NACC","ORDINARY","VACANT","","","");
		Assertions.assertEquals("NACCVAC",reportingIdentification.getOutcomeSpotting(identificationConfiguration));

		reportingIdentification = new ReportingIdentification("IDENTIFIED","NACC","ORDINARY","SECONDARY","","","");
		Assertions.assertEquals("NACCSEC",reportingIdentification.getOutcomeSpotting(identificationConfiguration));

		reportingIdentification = new ReportingIdentification("IDENTIFIED","NACC","ORDINARY","DK","UNIDENTIFIED","","");
		Assertions.assertEquals("NACCDKUNIDENT",reportingIdentification.getOutcomeSpotting(identificationConfiguration));

		reportingIdentification = new ReportingIdentification("IDENTIFIED","NACC","ORDINARY","PRIMARY","IDENTIFIED","","");
		Assertions.assertEquals("NACCPRIDENT",reportingIdentification.getOutcomeSpotting(identificationConfiguration));

		reportingIdentification = new ReportingIdentification("IDENTIFIED","ACC","ORDINARY","OCCASIONAL","IDENTIFIED","","");
		Assertions.assertEquals("ACCOCCIDENT",reportingIdentification.getOutcomeSpotting(identificationConfiguration));
	}
	
	@Test
	void getOutcomeSpottingTest_IASCO() {
		String identificationConfiguration = "IASCO";
		ReportingIdentification reportingIdentification = new ReportingIdentification("DESTROY","","","","","","");
		Assertions.assertEquals("DESTROY",reportingIdentification.getOutcomeSpotting(identificationConfiguration));

		reportingIdentification = new ReportingIdentification("UNIDENTIFIED","","","","","","");
		Assertions.assertEquals("UNIDENTIF",reportingIdentification.getOutcomeSpotting(identificationConfiguration));

		reportingIdentification = new ReportingIdentification("IDENTIFIED","NACC","NOORDINARY","","","","");
		Assertions.assertEquals("NACCNO",reportingIdentification.getOutcomeSpotting(identificationConfiguration));

		reportingIdentification = new ReportingIdentification("IDENTIFIED","NACC","ABSORBED","","","","");
		Assertions.assertEquals("NACCABS",reportingIdentification.getOutcomeSpotting(identificationConfiguration));

		reportingIdentification = new ReportingIdentification("IDENTIFIED","NACC","ORDINARY","VACANT","","","");
		Assertions.assertEquals("NACCVAC",reportingIdentification.getOutcomeSpotting(identificationConfiguration));

		reportingIdentification = new ReportingIdentification("IDENTIFIED","NACC","ORDINARY","SECONDARY","","","");
		Assertions.assertEquals("NACCSEC",reportingIdentification.getOutcomeSpotting(identificationConfiguration));

		reportingIdentification = new ReportingIdentification("IDENTIFIED","NACC","ORDINARY","DK","UNIDENTIFIED","","");
		Assertions.assertEquals("NACCDKUNIDENT",reportingIdentification.getOutcomeSpotting(identificationConfiguration));

		reportingIdentification = new ReportingIdentification("IDENTIFIED","NACC","ORDINARY","PRIMARY","IDENTIFIED","","");
		Assertions.assertEquals("NACCPRIDENT",reportingIdentification.getOutcomeSpotting(identificationConfiguration));

		reportingIdentification = new ReportingIdentification("IDENTIFIED","ACC","ORDINARY","OCCASIONAL","IDENTIFIED","","");
		Assertions.assertEquals("ACCOCCIDENT",reportingIdentification.getOutcomeSpotting(identificationConfiguration));
	}

	@Test
	void getOutcomeSpottingTest_INDTEL() {
		String identificationConfiguration = "INDTEL";

		ReportingIdentification reportingIdentification = new ReportingIdentification("","","","","","DCD","");
		Assertions.assertEquals("INDDCD",reportingIdentification.getOutcomeSpotting(identificationConfiguration));

		reportingIdentification = new ReportingIdentification("","","","","","NOIDENT","");
		Assertions.assertEquals("INDNOIDENT",reportingIdentification.getOutcomeSpotting(identificationConfiguration));

		reportingIdentification = new ReportingIdentification("","","","","","NOFIELD","");
		Assertions.assertEquals("INDNOFIELD",reportingIdentification.getOutcomeSpotting(identificationConfiguration));

		reportingIdentification = new ReportingIdentification("","","ORDINARY","","","SAMEADRESS","");
		Assertions.assertEquals("INDORDSADR",reportingIdentification.getOutcomeSpotting(identificationConfiguration));

		reportingIdentification = new ReportingIdentification("","","NOORDINARY","","","SAMEADRESS","");
		Assertions.assertEquals("INDNORDSADR",reportingIdentification.getOutcomeSpotting(identificationConfiguration));

		reportingIdentification = new ReportingIdentification("","","ORDINARY","","","OTHERADRESS","");
		Assertions.assertEquals("INDORDOADR",reportingIdentification.getOutcomeSpotting(identificationConfiguration));

		reportingIdentification = new ReportingIdentification("","","NOORDINARY","","","OTHERADRESS","");
		Assertions.assertEquals("INDNORDOADR",reportingIdentification.getOutcomeSpotting(identificationConfiguration));

	}

	@Test
	void getOutcomeSpottingTest_INDF2F() {
		String identificationConfiguration = "INDF2F";

		ReportingIdentification reportingIdentification = new ReportingIdentification("","","","","","DCD","");
		Assertions.assertEquals("INDDCD",reportingIdentification.getOutcomeSpotting(identificationConfiguration));

		reportingIdentification = new ReportingIdentification("","","","","","NOIDENT","");
		Assertions.assertEquals("INDNOIDENT",reportingIdentification.getOutcomeSpotting(identificationConfiguration));

		reportingIdentification = new ReportingIdentification("","","","","","NOFIELD","");
		Assertions.assertEquals("INDNOFIELD",reportingIdentification.getOutcomeSpotting(identificationConfiguration));

		reportingIdentification = new ReportingIdentification("","","ORDINARY","","","SAMEADRESS","");
		Assertions.assertEquals("INDORDSADR",reportingIdentification.getOutcomeSpotting(identificationConfiguration));

		reportingIdentification = new ReportingIdentification("","","","","","SAMEADRESS","-");
		Assertions.assertEquals("INDNORDSADR",reportingIdentification.getOutcomeSpotting(identificationConfiguration));

		reportingIdentification = new ReportingIdentification("","","ORDINARY","","","OTHERADRESS","YES");
		Assertions.assertEquals("INDORDOADR",reportingIdentification.getOutcomeSpotting(identificationConfiguration));

		reportingIdentification = new ReportingIdentification("","","NOORDINARY","","","OTHERADRESS","YES");
		Assertions.assertEquals("INDNORDOADR",reportingIdentification.getOutcomeSpotting(identificationConfiguration));

		reportingIdentification = new ReportingIdentification("","","","","","OTHERADRESS","NO");
		Assertions.assertEquals("NOTREAT",reportingIdentification.getOutcomeSpotting(identificationConfiguration));
	}

}