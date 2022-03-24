package cucumber;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import javax.script.Bindings;

import fr.insee.kraftwerk.core.Constants;
import fr.insee.kraftwerk.core.dataprocessing.DataProcessing;
import fr.insee.kraftwerk.core.dataprocessing.ReconciliationProcessing;
import fr.insee.kraftwerk.core.rawdata.SurveyRawDataTest;
import fr.insee.kraftwerk.core.vtl.VtlBindings;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

// Used in do_we_aggregate
public class AggregateDefinitions {
	public VtlBindings vtlBindings = new VtlBindings();
	public Bindings bindings = vtlBindings.getBindings();
	public String tempDatasetPath = "";

	@Given("We have some VTLBindings named {string} and {string}")
	public void initialize(String firstDataset, String secondDataset) throws Exception {
		vtlBindings.convertToVtlDataset(SurveyRawDataTest.createFakeCawiSurveyRawData(), firstDataset);
		vtlBindings.convertToVtlDataset(SurveyRawDataTest.createFakePapiSurveyRawData(), secondDataset);

		assertTrue(vtlBindings.getBindings().containsKey(firstDataset));
		assertTrue(vtlBindings.getBindings().containsKey(secondDataset));

	}

	@When("I try to aggregate the bindings")
	public void collect_variables() throws Exception {
		DataProcessing reconciliationProcessing = new ReconciliationProcessing(vtlBindings);
		reconciliationProcessing.applyVtlTransformations(
				"MULTIMODE", null);
	}

	@Then("The datasets I try to aggregate should return an aggregated dataset")
	public void the_aggregated_dataset_shoud_exist() {
		assertEquals(11, vtlBindings.getDataset("MULTIMODE").getDataStructure().size());
		// On check que l'aggregation a conserv� les identifiants
		assertTrue(vtlBindings.getDataset("MULTIMODE").getDataStructure().keySet().contains(Constants.ROOT_IDENTIFIER_NAME));
		// On check que l'aggregation a conserve les variables qui sont en commun.
		assertTrue(vtlBindings.getDataset("MULTIMODE").getDataStructure().keySet().contains("LAST_NAME"));
		// On check que l'aggregation a conserve les variables qui sont dans un seul des deux datasets.
		assertTrue(vtlBindings.getDataset("MULTIMODE").getDataStructure().keySet().contains("AGE"));
		assertTrue(vtlBindings.getDataset("MULTIMODE").getDataStructure().keySet().contains("ADDRESS"));
		// On vérifie les valeurs de quelques variables
		assertEquals(40, ((Long) vtlBindings.getDataset("MULTIMODE").getDataPoints().get(0).get("AGE")).intValue());
		assertEquals("Simpson in PAPI", vtlBindings.getDataset("MULTIMODE").getDataPoints().get(4).get("LAST_NAME"));
		assertEquals("742 Evergreen Terrace", vtlBindings.getDataset("MULTIMODE").getDataPoints().get(4).get("ADDRESS"));
		assertEquals("Purple", vtlBindings.getDataset("MULTIMODE").getDataPoints().get(4).get("CARS_LOOP.CAR_COLOR"));
		assertEquals("Flanders", vtlBindings.getDataset("MULTIMODE").getDataPoints().get(6).get("LAST_NAME"));
		assertEquals("740 Evergreen Terrace", vtlBindings.getDataset("MULTIMODE").getDataPoints().get(6).get("ADDRESS"));
		assertEquals("Red", vtlBindings.getDataset("MULTIMODE").getDataPoints().get(6).get("CARS_LOOP.CAR_COLOR"));
	}

}