package fr.insee.kraftwerk.core.outputs.scripts;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import fr.insee.kraftwerk.core.KraftwerkError;
import fr.insee.kraftwerk.core.dataprocessing.GroupProcessing;
import fr.insee.kraftwerk.core.metadata.Variable;
import fr.insee.kraftwerk.core.metadata.VariableType;
import fr.insee.kraftwerk.core.metadata.VariablesMap;
import fr.insee.kraftwerk.core.metadata.VariablesMapTest;
import fr.insee.kraftwerk.core.rawdata.SurveyRawData;
import fr.insee.kraftwerk.core.rawdata.SurveyRawDataTest;
import fr.insee.kraftwerk.core.vtl.VtlBindings;
import fr.insee.kraftwerk.core.vtl.VtlExecute;
import fr.insee.vtl.model.Dataset;
import fr.insee.vtl.model.InMemoryDataset;
import fr.insee.vtl.model.Structured;
import fr.insee.vtl.model.Structured.DataStructure;

class ImportScriptTest {
	
	VtlBindings vtlBindings = new VtlBindings();

	DataStructure dataStructure;

	TableScriptInfo tableScriptInfo;

	Map<String, VariablesMap> metadataVariables;
	
	VtlExecute vtlExecute = new VtlExecute();

	@BeforeEach
	public void initMetadataVariablesMap() {
		metadataVariables = new LinkedHashMap<>();
	}

	private void instantiateMap() {
		metadataVariables.put("CAWI", VariablesMapTest.createCompleteFakeVariablesMap());
		SurveyRawData srdWeb = SurveyRawDataTest.createFakeCawiSurveyRawData();
		srdWeb.setVariablesMap(VariablesMapTest.createCompleteFakeVariablesMap());
		vtlExecute.convertToVtlDataset(srdWeb, "CAWI", vtlBindings);
		
		metadataVariables.put("PAPI", VariablesMapTest.createAnotherFakeVariablesMap());
		SurveyRawData srdPaper = SurveyRawDataTest.createFakePapiSurveyRawData();
		srdPaper.setVariablesMap(VariablesMapTest.createAnotherFakeVariablesMap());
		vtlExecute.convertToVtlDataset(srdPaper, "PAPI", vtlBindings);

		// add group prefixes
		List<KraftwerkError> errors = new ArrayList<>();
		GroupProcessing groupProcessing = new GroupProcessing(vtlBindings, srdWeb.getVariablesMap());
		groupProcessing.applyVtlTransformations("CAWI", null, errors);
		GroupProcessing groupProcessing2 = new GroupProcessing(vtlBindings, srdPaper.getVariablesMap());
		groupProcessing2.applyVtlTransformations("PAPI", null, errors);

		dataStructure = vtlBindings.getDataset("CAWI").getDataStructure();
		tableScriptInfo = new TableScriptInfo("MULTIMODE", "TEST", dataStructure, metadataVariables);
		
	}
	
	@Test
	void getAllLengthTest() {
		instantiateMap();
		Map<String, Variable> listVariables = ImportScript.getAllLength(dataStructure, metadataVariables);
		assertEquals("50", listVariables.get("LAST_NAME").getSasFormat());
		assertEquals("50", listVariables.get("FIRST_NAME").getSasFormat());
		assertEquals("50", listVariables.get("AGE").getSasFormat());
		assertEquals("500", listVariables.get("CAR_COLOR").getSasFormat());
	}

	@Test
	void testGetAllLengthWithNumberType() {
		//
		VariablesMap testVariablesMap1 = new VariablesMap();
		testVariablesMap1.putVariable(new Variable("FOO", testVariablesMap1.getRootGroup(), VariableType.NUMBER, "4.1"));
		metadataVariables.put("TEST1", testVariablesMap1);
		VariablesMap testVariablesMap2 = new VariablesMap();
		testVariablesMap2.putVariable(new Variable("FOO", testVariablesMap2.getRootGroup(), VariableType.NUMBER, "4"));
		metadataVariables.put("TEST2", testVariablesMap2);
		//
		DataStructure testDataStructure = new DataStructure(List.of(
				new Structured.Component("ID", String.class, Dataset.Role.IDENTIFIER),
				new Structured.Component("FOO", Double.class, Dataset.Role.MEASURE)
		));
		//
		assertDoesNotThrow(() -> ImportScript.getAllLength(testDataStructure, metadataVariables));
	}

	@Test
	void numberTypeInDatasets() {
		List<KraftwerkError> errors = new ArrayList<>();
		Dataset ds = new InMemoryDataset(
				List.of(List.of(1L)),
				List.of(new Structured.Component("ID", Long.class, Dataset.Role.IDENTIFIER))
		);
		vtlBindings.put("test", ds);
		vtlExecute.evalVtlScript("test := test [calc foo := 4.1];", vtlBindings,errors);
		Dataset outDs = vtlBindings.getDataset("test");
		assertEquals(Double.class, outDs.getDataPoints().get(0).get("foo").getClass());
		// => "NUMBER" type in Trevas datasets is java "Double" type
	}

}
