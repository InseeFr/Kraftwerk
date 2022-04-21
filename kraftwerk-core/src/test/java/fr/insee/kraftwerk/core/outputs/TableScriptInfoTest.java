package fr.insee.kraftwerk.core.outputs;

import fr.insee.kraftwerk.core.dataprocessing.GroupProcessing;
import fr.insee.kraftwerk.core.metadata.Variable;
import fr.insee.kraftwerk.core.metadata.VariableType;
import fr.insee.kraftwerk.core.metadata.VariablesMap;
import fr.insee.kraftwerk.core.metadata.VariablesMapTest;
import fr.insee.kraftwerk.core.rawdata.SurveyRawData;
import fr.insee.kraftwerk.core.rawdata.SurveyRawDataTest;
import fr.insee.kraftwerk.core.vtl.VtlBindings;
import fr.insee.vtl.model.Dataset;
import fr.insee.vtl.model.InMemoryDataset;
import fr.insee.vtl.model.Structured;
import fr.insee.vtl.model.Structured.DataStructure;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TableScriptInfoTest {
	
	VtlBindings vtlBindings = new VtlBindings();

	DataStructure dataStructure;

	TableScriptInfo tableScriptInfo;

	Map<String, VariablesMap> metadataVariables;

	@BeforeEach
	public void initMetadataVariablesMap() {
		metadataVariables = new LinkedHashMap<>();
	}

	private void instantiateMap() {
		metadataVariables.put("CAWI", VariablesMapTest.createFakeVariablesMap());
		SurveyRawData srdWeb = SurveyRawDataTest.createFakeCawiSurveyRawData();
		srdWeb.setVariablesMap(VariablesMapTest.createFakeVariablesMap());
		vtlBindings.convertToVtlDataset(srdWeb, "CAWI");
		
		metadataVariables.put("PAPI", VariablesMapTest.createAnotherFakeVariablesMap());
		SurveyRawData srdPaper = SurveyRawDataTest.createFakePapiSurveyRawData();
		srdPaper.setVariablesMap(VariablesMapTest.createAnotherFakeVariablesMap());
		vtlBindings.convertToVtlDataset(srdPaper, "PAPI");

		// add group prefixes
		GroupProcessing groupProcessing = new GroupProcessing(vtlBindings);
		groupProcessing.applyVtlTransformations("CAWI", null, srdWeb.getVariablesMap());
		groupProcessing.applyVtlTransformations("PAPI", null, srdPaper.getVariablesMap());

		dataStructure = vtlBindings.getDataset("CAWI").getDataStructure();
		tableScriptInfo = new TableScriptInfo("MULTIMODE", "TEST", dataStructure, metadataVariables);
		System.out.println(dataStructure.keySet());
		System.out.println(metadataVariables.get("CAWI").getVariableNames());
		System.out.println(metadataVariables.get("CAWI").getFullyQualifiedNames());
	}
	
	@Test
	public void getAllLengthTest() {
		instantiateMap();
		Map<String, Variable> listVariables = tableScriptInfo.getAllLength(dataStructure, metadataVariables);
		assertEquals("50", listVariables.get("LAST_NAME").getLength());
		assertEquals("50", listVariables.get("FIRST_NAME").getLength());
		assertEquals("50", listVariables.get("AGE").getLength());
		assertEquals("500", listVariables.get("CARS_LOOP.CAR_COLOR").getLength());
	}

	@Test
	public void testGetAllLengthWithNumberType() {
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
		TableScriptInfo testTableScriptInfo = new TableScriptInfo(
				"TEST", "test.csv", testDataStructure, metadataVariables);
		assertDoesNotThrow(() -> testTableScriptInfo.getAllLength(testDataStructure, metadataVariables));
	}

	@Test
	private void numberTypeInDatasets() {
		Dataset ds = new InMemoryDataset(
				List.of(List.of(1L)),
				List.of(new Structured.Component("ID", Long.class, Dataset.Role.IDENTIFIER))
		);
		vtlBindings.getBindings().put("test", ds);
		vtlBindings.evalVtlScript("test := test [calc foo := 4.1];");
		Dataset outDs = vtlBindings.getDataset("test");
		assertEquals(Double.class, outDs.getDataPoints().get(0).get("foo").getClass());
		// => "NUMBER" type in Trevas datasets is java "Double" type
	}

}
