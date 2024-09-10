package fr.insee.kraftwerk.core.vtl;

import fr.insee.bpm.metadata.model.MetadataModel;
import fr.insee.kraftwerk.core.Constants;
import fr.insee.kraftwerk.core.KraftwerkError;
import fr.insee.kraftwerk.core.rawdata.SurveyRawData;
import fr.insee.kraftwerk.core.rawdata.SurveyRawDataTest;
import fr.insee.kraftwerk.core.utils.files.FileSystemImpl;
import fr.insee.kraftwerk.core.utils.files.FileUtilsInterface;
import fr.insee.kraftwerk.core.utils.log.KraftwerkExecutionContext;
import fr.insee.vtl.model.Dataset;
import fr.insee.vtl.model.Dataset.Role;
import fr.insee.vtl.model.InMemoryDataset;
import fr.insee.vtl.model.Structured;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Log4j2
class VtlBindingsTest {

	private VtlBindings vtlBindings;
	private KraftwerkExecutionContext kraftwerkExecutionContext = new KraftwerkExecutionContext();
	private final FileUtilsInterface fileUtilsInterface = new FileSystemImpl();
	VtlExecute vtlExecute = new VtlExecute(fileUtilsInterface);

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
		kraftwerkExecutionContext = new KraftwerkExecutionContext();
	}

	@Test
	void removeNonExistingDataset() {
		Assertions.assertDoesNotThrow(() -> vtlBindings.remove("NOT_IN_BINDINGS"));
	}

	@Test
	void constructorsAndDataModeTest() {
		SurveyRawData surveyRawData = SurveyRawDataTest.createFakePapiSurveyRawData();
		vtlExecute.convertToVtlDataset(surveyRawData, "SRD1", vtlBindings);
		Dataset dataset = vtlBindings.getDataset("SRD1");
		assertEquals("Simpson in PAPI", dataset.getDataPoints().get(0).get("LAST_NAME"));
		assertEquals(8, dataset.getDataPoints().size());
	}

	@Test
	void convertToVtlDatasetTest() {
		SurveyRawData surveyRawDataPapi = SurveyRawDataTest.createFakePapiSurveyRawData();
		SurveyRawData surveyRawDataCapi = SurveyRawDataTest.createFakeCapiSurveyRawData();
		SurveyRawData surveyRawDataCawi = SurveyRawDataTest.createFakeCawiSurveyRawData();
		vtlExecute.convertToVtlDataset(surveyRawDataPapi, "Papi", vtlBindings);
		vtlExecute.convertToVtlDataset(surveyRawDataCapi, "Capi avec espace", vtlBindings);
		vtlExecute.convertToVtlDataset(surveyRawDataCawi, "Cawi avec accent é", vtlBindings);
		assertEquals(3, vtlBindings.size());
		assertTrue(vtlBindings.containsKey("Papi"));
		assertTrue(vtlBindings.containsKey("Capi avec espace"));
		assertTrue(vtlBindings.containsKey("Cawi avec accent é"));

		Dataset capi = vtlBindings.getDataset("Capi avec espace");
		assertEquals(4, capi.getDataPoints().size());
		assertEquals(15, capi.getDataStructure().keySet().size());
	}

	@Test
	void evalVtlScriptTest_uniqueString() {
		vtlBindings.put("TEST", ds1);
		//
		StringBuilder vtlScript = new StringBuilder("\n");
		vtlScript.append("TEST := TEST [calc CODE_POSTAL := \n");
		vtlScript.append("    if LIB_COMMUNE = \"Lille\" then \"59000\" else ( \n");
		vtlScript.append("    if LIB_COMMUNE = \"Amiens\" then \"80000\" else ( \n");
		vtlScript.append("    \"\" ))];");
		log.info("Test VTL script:");
		log.info(vtlScript.toString());
		vtlExecute.evalVtlScript(vtlScript.toString(), vtlBindings, kraftwerkExecutionContext);
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
	void evalEmptyVtlString() {
		VtlBindings vtlBindingsInitial = vtlBindings;
		List<KraftwerkError> errors = new ArrayList<>();
		vtlExecute.evalVtlScript((String) null, vtlBindings, kraftwerkExecutionContext);
		vtlExecute.evalVtlScript((VtlScript) null, vtlBindings, kraftwerkExecutionContext);
		vtlExecute.evalVtlScript("", vtlBindings, kraftwerkExecutionContext);
		assertEquals(vtlBindingsInitial, vtlBindings);
		assertEquals(0, errors.size());
	}
	

	@Test
	void evalEmptyVtlScriptObject() {
		vtlExecute.evalVtlScript(new VtlScript(), vtlBindings, kraftwerkExecutionContext);
		assertEquals(0, kraftwerkExecutionContext.getErrors().size());

	}

	@Test
	void evalVtlScriptTest_scriptObject() {
		//
		vtlBindings.put("TEST", ds1);
		//
		VtlScript vtlScript = new VtlScript();
		vtlScript.add("TEST := TEST [calc new1 := \"new\"];");
		vtlScript.add("nOt VtL cOdE "); // should write a warning in the log but not throw an exception
		vtlScript.add("TEST := TEST [calc new2 := 2];");
		vtlExecute.evalVtlScript(vtlScript, vtlBindings, kraftwerkExecutionContext);
		//
		Dataset ds = vtlBindings.getDataset("TEST");

		//
		assertTrue(ds.getDataStructure().containsKey("new1"));
		assertTrue(ds.getDataStructure().containsKey("new2"));
	}


	@Test
	void testGetDatasetVariablesMap(){
		vtlBindings = new VtlBindings();
		vtlBindings.put("TEST", ds1);
		MetadataModel variablesMap = vtlBindings.getDatasetVariablesMap("TEST");
		//
		assertEquals(2, variablesMap.getGroupsCount());
		assertTrue(variablesMap.hasGroup(Constants.ROOT_GROUP_NAME));
		assertTrue(variablesMap.hasGroup("INDIVIDU"));
		// Variable objects = measures in VTL dataset => 3 variables expected
		assertEquals(3, variablesMap.getVariables().getVariables().size());
		assertTrue(variablesMap.getVariables().hasVariable("LIB_COMMUNE"));
		assertEquals(Constants.ROOT_GROUP_NAME, variablesMap.getVariables().getVariable("LIB_COMMUNE").getGroupName());
		assertTrue(variablesMap.getVariables().hasVariable("PRENOM"));
		assertTrue(variablesMap.getVariables().hasVariable("AGE"));
		assertEquals("INDIVIDU", variablesMap.getVariables().getVariable("PRENOM").getGroupName());
		assertEquals("INDIVIDU", variablesMap.getVariables().getVariable("AGE").getGroupName());
	}

}
