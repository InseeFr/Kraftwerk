package fr.insee.kraftwerk.core.cucumber.unit_tests;

import fr.insee.kraftwerk.core.TestConstants;
import fr.insee.kraftwerk.core.rawdata.SurveyRawData;
import fr.insee.kraftwerk.core.rawdata.SurveyRawDataTest;
import fr.insee.kraftwerk.core.utils.files.FileSystemImpl;
import fr.insee.kraftwerk.core.utils.KraftwerkExecutionContext;
import fr.insee.kraftwerk.core.vtl.VtlBindings;
import fr.insee.kraftwerk.core.vtl.VtlExecute;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import static org.junit.Assert.assertEquals;

// Used in do_we_apply_vtl_instruction and do_we_apply_vtl_script
public class EvalScriptDefinitions {
	public VtlBindings vtlBindings = new VtlBindings();
	
	VtlExecute vtlExecute = new VtlExecute(new FileSystemImpl(TestConstants.TEST_RESOURCES_DIRECTORY));
	KraftwerkExecutionContext kraftwerkExecutionContext = TestConstants.getKraftwerkExecutionContext();

	@Given("We have some simple VTLBindings")
	public void initialize() {
		SurveyRawData surveyColeman = SurveyRawDataTest.createFakeCawiSurveyRawData();
		vtlExecute.convertToVtlDataset(surveyColeman, "COLEMAN", vtlBindings);
		SurveyRawData surveyPaper = SurveyRawDataTest.createFakePapiSurveyRawData();
		vtlExecute.convertToVtlDataset(surveyPaper, "PAPER", vtlBindings);

		assertEquals(15, vtlBindings.getDataset("COLEMAN").getDataStructure().size());
	} 
	
	@When("I try to apply some VTL instruction : {string}")
	public void exportDataset(String vtlScript) {
		vtlExecute.evalVtlScript(vtlScript, vtlBindings, kraftwerkExecutionContext);
	}

	@Then("The binding {string} should have {int} variables")
	public void the_aggregated_dataset_shoud_exist(String outputDataset, int numberVariables) {
		assertEquals(numberVariables, vtlBindings.getDataset(outputDataset).getDataStructure().size());
	}

}