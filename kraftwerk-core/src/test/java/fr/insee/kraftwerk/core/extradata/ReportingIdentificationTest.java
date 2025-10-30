package fr.insee.kraftwerk.core.extradata;

import fr.insee.kraftwerk.core.extradata.reportingdata.ReportingIdentification;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

class ReportingIdentificationTest {

	private final String UNKNOWN_VALUE = "TRUC";

	// Moog case : no identificationConfiguration and no identification
	@Test
	void getOutcomeSpottingTest_moog_no_identification() {
		ReportingIdentification reportingIdentification = new ReportingIdentification(null,null,null,null,null,null,null);
        Assertions.assertNull(reportingIdentification.getOutcomeSpotting(null));
	}

	// null = Case old file : no identificationConfiguration but identification is present
	@ParameterizedTest
	@NullSource
	@ValueSource(strings = {"IASCO","HOUSEF2F"})
	void getOutcomeSpottingTest_IASCO_HOUSEF2F(String identificationConfiguration) {
		ReportingIdentification reportingIdentification = new ReportingIdentification("DESTROY","","","","","","");
		Assertions.assertEquals("DESTROY",reportingIdentification.getOutcomeSpotting(identificationConfiguration));

		reportingIdentification = new ReportingIdentification("UNIDENTIFIED","","","","","","");
		Assertions.assertEquals("UNIDENTIF",reportingIdentification.getOutcomeSpotting(identificationConfiguration));

		reportingIdentification = new ReportingIdentification("IDENTIFIED","NACC","","","","","");
		Assertions.assertEquals("NACCNO",reportingIdentification.getOutcomeSpotting(identificationConfiguration));

		reportingIdentification = new ReportingIdentification("IDENTIFIED","ACC","ABSORBED","","","","");
		Assertions.assertEquals("ACCABS",reportingIdentification.getOutcomeSpotting(identificationConfiguration));

		reportingIdentification = new ReportingIdentification("IDENTIFIED","ACC","NOORDINARY","","","","");
		Assertions.assertEquals("ACCNO",reportingIdentification.getOutcomeSpotting(identificationConfiguration));

		reportingIdentification = new ReportingIdentification("IDENTIFIED","ACC","ORDINARY","VACANT","","","");
		Assertions.assertEquals("ACCVAC",reportingIdentification.getOutcomeSpotting(identificationConfiguration));

		reportingIdentification = new ReportingIdentification("IDENTIFIED","ACC","ORDINARY","SECONDARY","","","");
		Assertions.assertEquals("ACCSEC",reportingIdentification.getOutcomeSpotting(identificationConfiguration));

		reportingIdentification = new ReportingIdentification("IDENTIFIED","ACC","ORDINARY","PRIMARY","IDENTIFIED","","");
		Assertions.assertEquals("ACCPRIDENT",reportingIdentification.getOutcomeSpotting(identificationConfiguration));

		reportingIdentification = new ReportingIdentification("IDENTIFIED","ACC","ORDINARY","PRIMARY","UNIDENTIFIED","","");
		Assertions.assertEquals("ACCPRUNIDENT",reportingIdentification.getOutcomeSpotting(identificationConfiguration));
	}

	@ParameterizedTest
	@ValueSource(strings = {"IASCO","HOUSEF2F"})
	void getOutcomeSpottingTest_IASCO_HOUSEF2F_unknown_values(String identificationConfiguration) {

		ReportingIdentification reportingIdentification = new ReportingIdentification("DESTROY","","","","","","");
		Assertions.assertNull(reportingIdentification.getOutcomeSpotting(UNKNOWN_VALUE));

		reportingIdentification = new ReportingIdentification(UNKNOWN_VALUE,"","","","","","");
		Assertions.assertNull(reportingIdentification.getOutcomeSpotting(identificationConfiguration));

		reportingIdentification = new ReportingIdentification("IDENTIFIED",UNKNOWN_VALUE,"","","","","");
		Assertions.assertNull(reportingIdentification.getOutcomeSpotting(identificationConfiguration));

		reportingIdentification = new ReportingIdentification("IDENTIFIED","ACC",UNKNOWN_VALUE,"","","","");
		Assertions.assertNull(reportingIdentification.getOutcomeSpotting(identificationConfiguration));

		reportingIdentification = new ReportingIdentification("IDENTIFIED","ACC","ORDINARY",UNKNOWN_VALUE,"","","");
		Assertions.assertNull(reportingIdentification.getOutcomeSpotting(identificationConfiguration));

		reportingIdentification = new ReportingIdentification("IDENTIFIED","ACC","ORDINARY","PRIMARY",UNKNOWN_VALUE,"","");
		Assertions.assertNull(reportingIdentification.getOutcomeSpotting(identificationConfiguration));
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

	@ParameterizedTest
	@ValueSource(strings = {"INDTEL","INDF2F"})
	void getOutcomeSpottingTest_INDTEL_INDF2F_unknown_value(String identificationConfiguration) {
		ReportingIdentification reportingIdentification = new ReportingIdentification("","","","","",UNKNOWN_VALUE,"");
		Assertions.assertNull(reportingIdentification.getOutcomeSpotting(identificationConfiguration));

		reportingIdentification = new ReportingIdentification("","","","","","OTHERADRESS",UNKNOWN_VALUE);
		Assertions.assertNull(reportingIdentification.getOutcomeSpotting(identificationConfiguration));
	}
}