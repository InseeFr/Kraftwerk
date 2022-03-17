package cucumber;

import static org.junit.Assert.assertEquals;

import javax.script.Bindings;

import fr.insee.kraftwerk.core.rawdata.SurveyRawData;
import fr.insee.kraftwerk.core.rawdata.SurveyRawDataTest;
import fr.insee.kraftwerk.core.vtl.VtlBindings;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

// Used in do_we_apply_vtl_instruction and do_we_apply_vtl_script
public class EvalScriptDefinitions {
	public VtlBindings vtlBindings = new VtlBindings();
	public Bindings bindings = vtlBindings.getBindings();

	@Given("We have some simple VTLBindings")
	public void initialize() throws Exception {
		SurveyRawData surveyColeman = SurveyRawDataTest.createFakeCawiSurveyRawData();
		vtlBindings.convertToVtlDataset(surveyColeman, "COLEMAN");
		SurveyRawData surveyPaper = SurveyRawDataTest.createFakePapiSurveyRawData();
		vtlBindings.convertToVtlDataset(surveyPaper, "PAPER");

		assertEquals(9, vtlBindings.getDataset("COLEMAN").getDataStructure().size());
	} 
	
	@When("I try to apply some VTL instruction : {string}")
	public void exportDataset(String vtlScript) throws Exception {
		vtlBindings.evalVtlScript(vtlScript);
	}

	@Then("The binding {string} should have {int} variables")
	public void the_aggregated_dataset_shoud_exist(String outputDataset, int numberVariables) {
		assertEquals(numberVariables, vtlBindings.getDataset(outputDataset).getDataStructure().size());
	}

}