package fr.insee.kraftwerk.core.outputs.scripts;

import fr.insee.kraftwerk.core.KraftwerkError;
import fr.insee.kraftwerk.core.dataprocessing.GroupProcessing;
import fr.insee.kraftwerk.core.metadata.MetadataModel;
import fr.insee.kraftwerk.core.metadata.MetadataModelTest;
import fr.insee.kraftwerk.core.metadata.Variable;
import fr.insee.kraftwerk.core.metadata.VariableType;
import fr.insee.kraftwerk.core.outputs.ImportScript;
import fr.insee.kraftwerk.core.outputs.TableScriptInfo;
import fr.insee.kraftwerk.core.rawdata.SurveyRawData;
import fr.insee.kraftwerk.core.rawdata.SurveyRawDataTest;
import fr.insee.kraftwerk.core.vtl.VtlBindings;
import fr.insee.kraftwerk.core.vtl.VtlExecute;
import fr.insee.vtl.model.Dataset;
import fr.insee.vtl.model.InMemoryDataset;
import fr.insee.vtl.model.Structured;
import fr.insee.vtl.model.Structured.DataStructure;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ImportScriptTest {
	
	VtlBindings vtlBindings = new VtlBindings();

	DataStructure dataStructure;

	TableScriptInfo tableScriptInfo;

	Map<String, MetadataModel> metadata;
	
	VtlExecute vtlExecute = new VtlExecute();

	@BeforeEach
	public void initMetadata() {
		metadata = new LinkedHashMap<>();
	}

	private void instantiateMap() {
		metadata.put("CAWI", MetadataModelTest.createCompleteFakeVariablesMap());
		SurveyRawData srdWeb = SurveyRawDataTest.createFakeCawiSurveyRawData();
		srdWeb.setMetadataModel(MetadataModelTest.createCompleteFakeVariablesMap());
		vtlExecute.convertToVtlDataset(srdWeb, "CAWI", vtlBindings);

		metadata.put("PAPI", MetadataModelTest.createAnotherFakeVariablesMap());
		SurveyRawData srdPaper = SurveyRawDataTest.createFakePapiSurveyRawData();
		srdPaper.setMetadataModel(MetadataModelTest.createAnotherFakeVariablesMap());
		vtlExecute.convertToVtlDataset(srdPaper, "PAPI", vtlBindings);

		// add group prefixes
		List<KraftwerkError> errors = new ArrayList<>();
		GroupProcessing groupProcessing = new GroupProcessing(vtlBindings, srdWeb.getMetadataModel());
		groupProcessing.applyVtlTransformations("CAWI", null, errors);
		GroupProcessing groupProcessing2 = new GroupProcessing(vtlBindings, srdPaper.getMetadataModel());
		groupProcessing2.applyVtlTransformations("PAPI", null, errors);

		dataStructure = vtlBindings.getDataset("CAWI").getDataStructure();
		tableScriptInfo = new TableScriptInfo("MULTIMODE", "TEST", dataStructure, metadata);
		
	}
	
	@Test
	void getAllLengthTest() {
		instantiateMap();
		Map<String, Variable> listVariables = ImportScript.getAllLength(dataStructure, metadata);
		assertEquals("50", listVariables.get("LAST_NAME").getSasFormat());
		assertEquals("50", listVariables.get("FIRST_NAME").getSasFormat());
		assertEquals("50", listVariables.get("AGE").getSasFormat());
		assertEquals("500", listVariables.get("CAR_COLOR").getSasFormat());
	}

	@Test
	void testGetAllLengthWithNumberType() {
		//
		MetadataModel testMetadata1 = new MetadataModel();
		testMetadata1.getVariables().putVariable(new Variable("FOO", testMetadata1.getRootGroup(), VariableType.NUMBER, "4.1"));
		metadata.put("TEST1", testMetadata1);
		MetadataModel testMetadata2 = new MetadataModel();
		testMetadata2.getVariables().putVariable(new Variable("FOO", testMetadata2.getRootGroup(), VariableType.NUMBER, "4"));
		metadata.put("TEST2", testMetadata2);
		//
		DataStructure testDataStructure = new DataStructure(List.of(
				new Structured.Component("ID", String.class, Dataset.Role.IDENTIFIER),
				new Structured.Component("FOO", Double.class, Dataset.Role.MEASURE)
		));
		//
		assertDoesNotThrow(() -> ImportScript.getAllLength(testDataStructure, metadata));
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
