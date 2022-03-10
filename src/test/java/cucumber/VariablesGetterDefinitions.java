package cucumber;

import fr.insee.kraftwerk.metadata.DDIReader;
import fr.insee.kraftwerk.metadata.VariablesMap;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

// Used in do_we_get_variables
public class VariablesGetterDefinitions {

	private VariablesMap variablesMap;
	private String linkDDI;
    
    /**
	 * Save link 
	 */
    @Given("DDI is here at {string}")
    public void set_linkDDI(String linkDDI) {
    	this.linkDDI = linkDDI;
    }

    @When("I try to collect the variables's infos")
    public void collect_variables() {
    	variablesMap = DDIReader.getVariablesFromDDI(linkDDI);
    }

    @Then("The variables I try to count should answer {int} and have {string} in it")
    public void i_should_again_be_told(int expectedNumberVariables, String expectedGroup) {
    	assertEquals(expectedNumberVariables, variablesMap.getVariables().size());
    	assertTrue(variablesMap.getGroups().containsKey(expectedGroup));
    }

}