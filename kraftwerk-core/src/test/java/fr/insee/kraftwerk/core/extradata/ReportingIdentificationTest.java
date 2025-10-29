package fr.insee.kraftwerk.core.extradata;

import fr.insee.kraftwerk.core.extradata.reportingdata.ReportingIdentification;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;
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


	@Test
	void getOutcomeSpottingTest_moog_unknown_identification() {
		ReportingIdentification reportingIdentification = new ReportingIdentification(null,null,null,null,null,null,null);
		Assertions.assertNull(reportingIdentification.getOutcomeSpotting("aaa"));
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



	private static Stream<Arguments> oldFilesAndIASCOParameterizedTests() {
		return Stream.of(
				//Case old file : no identificationConfiguration but identification is present
				Arguments.of("DESTROY","","","","","","",                                   null, "DESTROY"),
				Arguments.of("UNIDENTIFIED","","","","","","",                              null, "UNIDENTIF"),
				Arguments.of("IDENTIFIED","NACC","NOORDINARY","","","","",                  null, "NACCNO"),
				Arguments.of("IDENTIFIED","NACC","ABSORBED","","","","",                    null, "NACCABS"),
				Arguments.of("IDENTIFIED","NACC","ORDINARY","VACANT","","","",              null, "NACCVAC"),
				Arguments.of("IDENTIFIED","NACC","ORDINARY","SECONDARY","","","",           null, "NACCSEC"),
				Arguments.of("IDENTIFIED","NACC","ORDINARY","DK","UNIDENTIFIED","","",      null, "NACCDKUNIDENT"),
				Arguments.of("IDENTIFIED","NACC","ORDINARY","PRIMARY","IDENTIFIED","","",   null, "NACCPRIDENT"),
				Arguments.of("IDENTIFIED","ACC","ORDINARY","OCCASIONAL","IDENTIFIED","","", null, "ACCOCCIDENT"),
				//IASCO
				Arguments.of("DESTROY","","","","","","",                                   "IASCO", "DESTROY"),
				Arguments.of("UNIDENTIFIED","","","","","","",                              "IASCO", "UNIDENTIF"),
				Arguments.of("IDENTIFIED","NACC","NOORDINARY","","","","",                  "IASCO", "NACCNO"),
				Arguments.of("IDENTIFIED","NACC","ABSORBED","","","","",                    "IASCO", "NACCABS"),
				Arguments.of("IDENTIFIED","NACC","ORDINARY","VACANT","","","",              "IASCO", "NACCVAC"),
				Arguments.of("IDENTIFIED","NACC","ORDINARY","SECONDARY","","","",           "IASCO", "NACCSEC"),
				Arguments.of("IDENTIFIED","NACC","ORDINARY","DK","UNIDENTIFIED","","",      "IASCO", "NACCDKUNIDENT"),
				Arguments.of("IDENTIFIED","NACC","ORDINARY","PRIMARY","IDENTIFIED","","",   "IASCO", "NACCPRIDENT"),
				Arguments.of("IDENTIFIED","ACC","ORDINARY","OCCASIONAL","IDENTIFIED","","", "IASCO", "ACCOCCIDENT"),
				//HOUSEF2F
				Arguments.of("DESTROY","","","","","","",                                   "HOUSEF2F", "DESTROY"),
				Arguments.of("UNIDENTIFIED","","","","","","",                              "HOUSEF2F", "UNIDENTIF"),
				Arguments.of("IDENTIFIED","NACC","NOORDINARY","","","","",                  "HOUSEF2F", "NACCNO"),
				Arguments.of("IDENTIFIED","NACC","ABSORBED","","","","",                    "HOUSEF2F", "NACCABS"),
				Arguments.of("IDENTIFIED","NACC","ORDINARY","VACANT","","","",              "HOUSEF2F", "NACCVAC"),
				Arguments.of("IDENTIFIED","NACC","ORDINARY","SECONDARY","","","",           "HOUSEF2F", "NACCSEC"),
				Arguments.of("IDENTIFIED","NACC","ORDINARY","DK","UNIDENTIFIED","","",      "HOUSEF2F", "NACCDKUNIDENT"),
				Arguments.of("IDENTIFIED","NACC","ORDINARY","PRIMARY","IDENTIFIED","","",   "HOUSEF2F", "NACCPRIDENT"),
				Arguments.of("IDENTIFIED","ACC","ORDINARY","OCCASIONAL","IDENTIFIED","","", "HOUSEF2F", "ACCOCCIDENT"),
				//INDTEL
				Arguments.of("","","","","","DCD","",                   "INDTEL", "INDDCD"),
				Arguments.of("","","","","","NOIDENT","",               "INDTEL", "INDNOIDENT"),
				Arguments.of("","","","","","NOFIELD","",               "INDTEL", "INDNOFIELD"),
				Arguments.of("","","ORDINARY","","","SAMEADRESS","",    "INDTEL", "INDORDSADR"),
				Arguments.of("","","NOORDINARY","","","SAMEADRESS","",  "INDTEL", "INDNORDSADR"),
				Arguments.of("","","ORDINARY","","","OTHERADRESS","",   "INDTEL", "INDORDOADR"),
				Arguments.of("","","NOORDINARY","","","OTHERADRESS","", "INDTEL", "INDNORDOADR"),
				Arguments.of("","","NOORDINARY","","",null,"",          "INDTEL", null),
				Arguments.of("","","NOORDINARY","","","aaaaaaa","",     "INDTEL", null),
				//INTELNOR
				Arguments.of("","","","","","DCD","",                   "INTELNOR", "INDDCD"),
				Arguments.of("","","","","","NOIDENT","",               "INTELNOR", "INDNOIDENT"),
				Arguments.of("","","","","","NOFIELD","",               "INTELNOR", "INDNOFIELD"),
				Arguments.of("","","ORDINARY","","","SAMEADRESS","",    "INTELNOR", "INDORDSADR"),
				Arguments.of("","","NOORDINARY","","","SAMEADRESS","",  "INTELNOR", "INDNORDSADR"),
				Arguments.of("","","ORDINARY","","","OTHERADRESS","",   "INTELNOR", "INDORDOADR"),
				Arguments.of("","","NOORDINARY","","","OTHERADRESS","", "INTELNOR", "INDNORDOADR"),
				Arguments.of("","","NOORDINARY","","",null,"",          "INDTEL", null),
				Arguments.of("","","NOORDINARY","","","aaaaaaa","",     "INDTEL", null),
				//INDF2F
				Arguments.of("","","","","","DCD","",                      "INDF2F", "INDDCD"),
				Arguments.of("","","","","","NOIDENT","",                  "INDF2F", "INDNOIDENT"),
				Arguments.of("","","","","","NOFIELD","",                  "INDF2F", "INDNOFIELD"),
				Arguments.of("","","ORDINARY","","","SAMEADRESS","",       "INDF2F", "INDORDSADR"),
				Arguments.of("","","","","","SAMEADRESS","-",              "INDF2F", "INDNORDSADR"),
				Arguments.of("","","ORDINARY","","","OTHERADRESS","YES",   "INDF2F", "INDORDOADR"),
				Arguments.of("","","NOORDINARY","","","OTHERADRESS","YES", "INDF2F", "INDNORDOADR"),
				Arguments.of("","","","","","OTHERADRESS","NO",            "INDF2F", "NOTREAT"),
				Arguments.of("","","NOORDINARY","","",null,"",             "INDF2F", null),
				Arguments.of("","","NOORDINARY","","","aaaaaaa","",        "INDF2F", null),
				//INF2FNOR
				Arguments.of("","","","","","DCD","",                      "INF2FNOR", "INDDCD"),
				Arguments.of("","","","","","NOIDENT","",                  "INF2FNOR", "INDNOIDENT"),
				Arguments.of("","","","","","NOFIELD","",                  "INF2FNOR", "INDNOFIELD"),
				Arguments.of("","","ORDINARY","","","SAMEADRESS","",       "INF2FNOR", "INDORDSADR"),
				Arguments.of("","","","","","SAMEADRESS","-",              "INF2FNOR", "INDNORDSADR"),
				Arguments.of("","","ORDINARY","","","OTHERADRESS","YES",   "INF2FNOR", "INDORDOADR"),
				Arguments.of("","","NOORDINARY","","","OTHERADRESS","YES", "INF2FNOR", "INDNORDOADR"),
				Arguments.of("","","","","","OTHERADRESS","NO",            "INF2FNOR", "NOTREAT"),
				Arguments.of("","","NOORDINARY","","",null,"",             "INDF2F", null),
				Arguments.of("","","NOORDINARY","","","aaaaaaa","",        "INDF2F", null)
		);
	}


	@ParameterizedTest
	@MethodSource("oldFilesAndIASCOParameterizedTests")
	void getOutcomeSpotting_ParameterizedTest(String identification, String access, String situation,
										  String category, String occupant, String individualStatus, String interviewerCanProcess,
										  String param, String expectedResult) {
		ReportingIdentification reportingIdentification = new ReportingIdentification(identification, access, situation, category, occupant, individualStatus, interviewerCanProcess);
		Assertions.assertEquals(expectedResult, reportingIdentification.getOutcomeSpotting(param));
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