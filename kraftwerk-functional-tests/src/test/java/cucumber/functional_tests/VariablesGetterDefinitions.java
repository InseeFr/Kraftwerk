package cucumber.functional_tests;

import fr.insee.kraftwerk.core.exceptions.KraftwerkException;
import fr.insee.kraftwerk.core.metadata.DDIReader;
import fr.insee.kraftwerk.core.metadata.MetadataModel;
import fr.insee.kraftwerk.core.utils.files.FileSystemImpl;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

// Used in do_we_get_variables
public class VariablesGetterDefinitions {

	private MetadataModel metadataModel;
	private URL linkDDI;
    
    /**
	 * Save link 
	 */
    @Given("DDI is here at {string}")
    public void set_linkDDI(String linkDDI) throws MalformedURLException, URISyntaxException {
    	this.linkDDI = new URI(linkDDI).toURL();
    }

    @When("I try to collect the variables's infos")
    public void collect_variables() throws KraftwerkException {
		metadataModel = DDIReader.getMetadataFromDDI(linkDDI.toString(), new FileSystemImpl());
    }

    @Then("The variables I try to count should answer {int} and have {string} in it")
    public void i_should_again_be_told(int expectedNumberVariables, String expectedGroup) {
    	assertEquals(expectedNumberVariables, metadataModel.getVariables().getVariables().size());
    	assertNotNull(metadataModel.getGroup(expectedGroup));
    }

    @Then("The variable {string} I want to check should have a type of {string} and have a length of {string}")
    public void i_should_again_be_told(String variableName, String expectedType, String expectedLength) {
    	assertEquals(expectedType, metadataModel.getVariables().getVariable(variableName).getType().toString());
    	assertEquals(expectedLength, metadataModel.getVariables().getVariable(variableName).getSasFormat());
    }

}