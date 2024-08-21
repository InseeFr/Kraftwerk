package fr.insee.kraftwerk.core.outputs.scripts;

import fr.insee.bpm.metadata.model.Group;
import fr.insee.bpm.metadata.model.McqVariable;
import fr.insee.bpm.metadata.model.PaperUcq;
import fr.insee.bpm.metadata.model.UcqVariable;
import fr.insee.kraftwerk.core.Constants;
import fr.insee.kraftwerk.core.KraftwerkError;
import fr.insee.kraftwerk.core.dataprocessing.GroupProcessing;
import fr.insee.bpm.metadata.model.MetadataModel;
import fr.insee.bpm.metadata.model.Variable;
import fr.insee.bpm.metadata.model.VariableType;
import fr.insee.kraftwerk.core.outputs.ImportScript;
import fr.insee.kraftwerk.core.outputs.TableScriptInfo;
import fr.insee.kraftwerk.core.rawdata.SurveyRawData;
import fr.insee.kraftwerk.core.rawdata.SurveyRawDataTest;
import fr.insee.kraftwerk.core.utils.files.FileSystemImpl;
import fr.insee.kraftwerk.core.utils.files.FileUtilsInterface;
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

public class ImportScriptTest {
	
	VtlBindings vtlBindings = new VtlBindings();

	DataStructure dataStructure;

	TableScriptInfo tableScriptInfo;

	Map<String, MetadataModel> metadata;
	private final FileUtilsInterface fileUtilsInterface = new FileSystemImpl();
	
	VtlExecute vtlExecute = new VtlExecute(fileUtilsInterface);

	@BeforeEach
	public void initMetadata() {
		metadata = new LinkedHashMap<>();
	}

	private void instantiateMap() {
		metadata.put("CAWI", createCompleteFakeVariablesMap());
		SurveyRawData srdWeb = SurveyRawDataTest.createFakeCawiSurveyRawData();
		srdWeb.setMetadataModel(createCompleteFakeVariablesMap());
		vtlExecute.convertToVtlDataset(srdWeb, "CAWI", vtlBindings);

		metadata.put("PAPI", createAnotherFakeVariablesMap());
		SurveyRawData srdPaper = SurveyRawDataTest.createFakePapiSurveyRawData();
		srdPaper.setMetadataModel(createAnotherFakeVariablesMap());
		vtlExecute.convertToVtlDataset(srdPaper, "PAPI", vtlBindings);

		// add group prefixes
		List<KraftwerkError> errors = new ArrayList<>();
		GroupProcessing groupProcessing = new GroupProcessing(vtlBindings, srdWeb.getMetadataModel(), fileUtilsInterface);
		groupProcessing.applyVtlTransformations("CAWI", null, errors);
		GroupProcessing groupProcessing2 = new GroupProcessing(vtlBindings, srdPaper.getMetadataModel(), fileUtilsInterface);
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

	/* Variables map objects to test multimode management */

	/**
	 * Return a VariablesMap object containing variables named as follows:
	 * - FIRST_NAME, LAST_NAME, AGE at the root
	 * - CAR_COLOR in a group named CARS_LOOP
	 */
	public static MetadataModel createCompleteFakeVariablesMap(){

		MetadataModel metadataM = new MetadataModel();

		// Groups
		Group rootGroup = metadataM.getRootGroup();
		Group carsGroup = new Group("CARS_LOOP", Constants.ROOT_GROUP_NAME);
		metadataM.putGroup(carsGroup);

		// Variables
		metadataM.getVariables().putVariable(new Variable("LAST_NAME", rootGroup, VariableType.STRING, "20"));
		metadataM.getVariables().putVariable(new Variable("FIRST_NAME", rootGroup, VariableType.STRING, "50"));
		metadataM.getVariables().putVariable(new Variable("AGE", rootGroup, VariableType.INTEGER, "50"));
		metadataM.getVariables().putVariable(new Variable("CAR_COLOR", carsGroup, VariableType.STRING, "50"));

		// unique choice question variable
		UcqVariable ucq = new UcqVariable("SEXE", rootGroup, VariableType.STRING, "50");
		ucq.addModality("1", "Male");
		ucq.addModality("2", "Female");
		Variable paperUcq1 = new PaperUcq("SEXE_1", ucq, "1");
		Variable paperUcq2 = new PaperUcq("SEXE_2", ucq, "2");
		metadataM.getVariables().putVariable(ucq);
		metadataM.getVariables().putVariable(paperUcq1);
		metadataM.getVariables().putVariable(paperUcq2);

		// unique choice question variable related to multiple choices question
		UcqVariable ucqMcq1 = new UcqVariable("CAR_OWNER", rootGroup, VariableType.STRING, "50");
		ucqMcq1.setQuestionItemName("VEHICLE_OWNER");
		ucqMcq1.addModality("1", "Yes");
		ucqMcq1.addModality("2", "No");
		UcqVariable ucqMcq2 = new UcqVariable("MOTO_OWNER", rootGroup, VariableType.STRING, "50");
		ucqMcq2.setQuestionItemName("VEHICLE_OWNER");
		ucqMcq2.addModality("1", "Yes");
		ucqMcq2.addModality("2", "No");
		metadataM.getVariables().putVariable(ucqMcq1);
		metadataM.getVariables().putVariable(ucqMcq2);

		// multiple choices question variable
		metadataM.getVariables().putVariable(McqVariable.builder()
				.name("RELATIONSHIP_A").group(rootGroup).questionItemName("RELATIONSHIP").text("Spouse").build());
		metadataM.getVariables().putVariable(McqVariable.builder()
				.name("RELATIONSHIP_B").group(rootGroup).questionItemName("RELATIONSHIP").text("Child").build());
		metadataM.getVariables().putVariable(McqVariable.builder()
				.name("RELATIONSHIP_C").group(rootGroup).questionItemName("RELATIONSHIP").text("Parent").build());
		metadataM.getVariables().putVariable(McqVariable.builder()
				.name("RELATIONSHIP_D").group(rootGroup).questionItemName("RELATIONSHIP").text("Other").build());

		return metadataM;
	}

	public static MetadataModel createAnotherFakeVariablesMap(){

		MetadataModel metadataM = new MetadataModel();

		// Groups
		Group rootGroup = metadataM.getRootGroup();
		Group carsGroup = new Group("CARS_LOOP", Constants.ROOT_GROUP_NAME);
		metadataM.putGroup(carsGroup);

		// Variables
		metadataM.getVariables().putVariable(new Variable("LAST_NAME", rootGroup, VariableType.STRING, "50"));
		metadataM.getVariables().putVariable(new Variable("FIRST_NAME", rootGroup, VariableType.STRING, "20"));
		metadataM.getVariables().putVariable(new Variable("ADDRESS", rootGroup, VariableType.STRING, "50"));
		metadataM.getVariables().putVariable(new Variable("CAR_COLOR", carsGroup, VariableType.STRING, "500"));

		return metadataM;
	}

	/* Variables map objects to test information levels management */

	public static MetadataModel createVariablesMap_rootOnly() {
		MetadataModel metadataModel1 = new MetadataModel();

		Group rootGroup = metadataModel1.getRootGroup();

		metadataModel1.putGroup(rootGroup);

		metadataModel1.getVariables().putVariable(
				new Variable("ADDRESS", rootGroup, VariableType.STRING));
		metadataModel1.getVariables().putVariable(
				new Variable("HOUSEHOLD_INCOME", rootGroup, VariableType.NUMBER));

		return metadataModel1;
	}

	public static MetadataModel createVariablesMap_oneLevel() {
		MetadataModel metadataModel1 = createVariablesMap_rootOnly();

		Group individualsGroup = new Group("INDIVIDUALS_LOOP", Constants.ROOT_GROUP_NAME);

		metadataModel1.putGroup(individualsGroup);

		metadataModel1.getVariables().putVariable(
				new Variable("FIRST_NAME", individualsGroup, VariableType.STRING));
		metadataModel1.getVariables().putVariable(
				new Variable("LAST_NAME", individualsGroup, VariableType.STRING));
		metadataModel1.getVariables().putVariable(
				new Variable("GENDER", individualsGroup, VariableType.STRING));

		return metadataModel1;
	}

	public static MetadataModel createVariablesMap_twoLevels() {
		MetadataModel metadataModel1 = createVariablesMap_oneLevel();

		Group carsGroup = new Group("CARS_LOOP", "INDIVIDUALS_LOOP");

		metadataModel1.putGroup(carsGroup);

		metadataModel1.getVariables().putVariable(
				new Variable("CAR_COLOR", carsGroup, VariableType.STRING));

		return metadataModel1;
	}


}
