package fr.insee.kraftwerk.core.vtl;

import fr.insee.kraftwerk.core.Constants;
import fr.insee.kraftwerk.core.metadata.VariablesMap;
import fr.insee.kraftwerk.core.rawdata.SurveyRawData;
import fr.insee.kraftwerk.core.rawdata.SurveyRawDataTest;
import fr.insee.vtl.model.Dataset;
import fr.insee.vtl.model.InMemoryDataset;
import fr.insee.vtl.model.Structured;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static fr.insee.vtl.model.Dataset.Role;
import static org.junit.jupiter.api.Assertions.*;

@Slf4j
public class VtlBindingsTest {

	private VtlBindings vtlBindings;

	Dataset ds1 = new InMemoryDataset(
			List.of(
					List.of("UE001", "Lille", "INDIVIDU-1", "Jean", 30),
					List.of("UE001", "Lille", "INDIVIDU-2", "Frédéric", 42),
					List.of("UE004", "Amiens", "INDIVIDU-1", "David", 26),
					List.of("UE005", "", "INDIVIDU-1", "Thibaud ", 18)
			),
			List.of(
					new Structured.Component(Constants.ROOT_IDENTIFIER_NAME, String.class, Role.IDENTIFIER),
					new Structured.Component("LIB_COMMUNE", String.class, Role.MEASURE),
					new Structured.Component("INDIVIDU", String.class, Role.IDENTIFIER),
					new Structured.Component("INDIVIDU.PRENOM", String.class, Role.MEASURE),
					new Structured.Component("INDIVIDU.AGE", Integer.class, Role.MEASURE)
			)
	);

	@BeforeEach
	public void initVtlBindings() {
		vtlBindings = new VtlBindings();
	}

	@Test
	public void removeNonExistingDataset() {
		Assertions.assertDoesNotThrow(() -> vtlBindings.getBindings().remove("NOT_IN_BINDINGS"));
	}

	@Test
	public void constructorsAndDataModeTest() {
		SurveyRawData surveyRawData = SurveyRawDataTest.createFakePapiSurveyRawData();
		vtlBindings.convertToVtlDataset(surveyRawData, "SRD1");
		Dataset dataset = vtlBindings.getDataset("SRD1");
		assertEquals("Simpson in PAPI", dataset.getDataPoints().get(0).get("LAST_NAME"));
		assertEquals(8, dataset.getDataPoints().size());
	}

	@Test
	public void convertToVtlDatasetTest() {
		SurveyRawData surveyRawDataPapi = SurveyRawDataTest.createFakePapiSurveyRawData();
		SurveyRawData surveyRawDataCapi = SurveyRawDataTest.createFakeCapiSurveyRawData();
		SurveyRawData surveyRawDataCawi = SurveyRawDataTest.createFakeCawiSurveyRawData();
		vtlBindings.convertToVtlDataset(surveyRawDataPapi, "Papi");
		vtlBindings.convertToVtlDataset(surveyRawDataCapi, "Capi avec espace");
		vtlBindings.convertToVtlDataset(surveyRawDataCawi, "Cawi avec accent é");
		assertEquals(3, vtlBindings.getBindings().size());
		assertTrue(vtlBindings.getBindings().containsKey("Papi"));
		assertTrue(vtlBindings.getBindings().containsKey("Capi avec espace"));
		assertTrue(vtlBindings.getBindings().containsKey("Cawi avec accent é"));

		Dataset capi = vtlBindings.getDataset("Capi avec espace");
		assertEquals(4, capi.getDataPoints().size());
		assertEquals(9, capi.getDataStructure().keySet().size());
	}

	@Test
	public void evalVtlScriptTest_uniqueString() {
		//
		vtlBindings.getBindings().put("TEST", ds1);
		//
		StringBuilder vtlScript = new StringBuilder("\n");
		vtlScript.append("TEST := TEST [calc CODE_POSTAL := \n");
		vtlScript.append("    if LIB_COMMUNE = \"Lille\" then \"59000\" else ( \n");
		vtlScript.append("    if LIB_COMMUNE = \"Amiens\" then \"80000\" else ( \n");
		vtlScript.append("    \"\" ))];");
		log.info("Test VTL script:");
		log.info(vtlScript.toString());
		vtlBindings.evalVtlScript(vtlScript.toString());
		//
		Dataset ds = vtlBindings.getDataset("TEST");

		//
		assertTrue(ds.getDataStructure().containsKey("CODE_POSTAL"));
		assertEquals("59000", ds.getDataPoints().get(0).get("CODE_POSTAL"));
		assertEquals("59000", ds.getDataPoints().get(1).get("CODE_POSTAL"));
		assertEquals("80000", ds.getDataPoints().get(2).get("CODE_POSTAL"));
		assertEquals("", ds.getDataPoints().get(3).get("CODE_POSTAL"));
	}

	@Test
	public void evalEmptyVtlString() {
		vtlBindings.evalVtlScript((String) null);
		vtlBindings.evalVtlScript((VtlScript) null);
		vtlBindings.evalVtlScript("");
		vtlBindings.evalVtlScript(new VtlScript());
	}

	@Test
	public void evalVtlScriptTest_scriptObject() {
		//
		vtlBindings.getBindings().put("TEST", ds1);
		//
		VtlScript vtlScript = new VtlScript();
		vtlScript.add("TEST := TEST [calc new1 := \"new\"];");
		vtlScript.add("nOt VtL cOdE "); // should write a warning in the log but not throw an exception
		vtlScript.add("TEST := TEST [calc new2 := 2];");
		vtlBindings.evalVtlScript(vtlScript);
		//
		Dataset ds = vtlBindings.getDataset("TEST");

		//
		assertTrue(ds.getDataStructure().containsKey("new1"));
		assertTrue(ds.getDataStructure().containsKey("new2"));
	}

	@Test
	public void evalEmptyVtlScriptObject() {
		vtlBindings.evalVtlScript(new VtlScript());
	}

	@Test
	public void testGetDatasetVariablesMap(){
		VtlBindings vtlBindings = new VtlBindings();
		vtlBindings.getBindings().put("TEST", ds1);
		VariablesMap variablesMap = vtlBindings.getDatasetVariablesMap("TEST");
		//
		assertEquals(2, variablesMap.getGroups().size());
		assertTrue(variablesMap.hasGroup(Constants.ROOT_GROUP_NAME));
		assertTrue(variablesMap.hasGroup("INDIVIDU"));
		// Variable objects = measures in VTL dataset => 3 variables expected
		assertEquals(3, variablesMap.getVariables().size());
		assertTrue(variablesMap.hasVariable("LIB_COMMUNE"));
		assertEquals(Constants.ROOT_GROUP_NAME, variablesMap.getVariable("LIB_COMMUNE").getGroupName());
		assertTrue(variablesMap.hasVariable("PRENOM"));
		assertTrue(variablesMap.hasVariable("AGE"));
		assertEquals("INDIVIDU", variablesMap.getVariable("PRENOM").getGroupName());
		assertEquals("INDIVIDU", variablesMap.getVariable("AGE").getGroupName());
	}

}
