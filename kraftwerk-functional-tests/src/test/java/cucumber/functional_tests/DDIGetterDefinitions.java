package cucumber.functional_tests;

import fr.insee.kraftwerk.core.Constants;
import fr.insee.kraftwerk.core.utils.TextFileReader;
import fr.insee.kraftwerk.core.utils.files.FileSystemImpl;
import fr.insee.kraftwerk.core.utils.xsl.SaxonTransformer;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

// Used in do_we_get_ddis
public class DDIGetterDefinitions {
	private String linkDDI = "";
    private String actualAnswer ="DDI not collected";
    private String actualString ="";

    /**
	 * Download DDI
	 */
    @Given("I collect the DDI named {string} using link {string}")
    public void get_DDI(String nameDDI, String linkDDI) throws Exception {
    	this.linkDDI = linkDDI;
    }

    /**
	 * Compare DDI with reference
	 */
    @When("I try to compare the DDI named {string}")
    public void compare_ddi(String nameDDI) throws IOException, URISyntaxException {

		File tempFile = File.createTempFile("ddi_temp", ".xml");
		tempFile.deleteOnExit();
		Path tempPath = Paths.get(tempFile.getAbsolutePath());
		URL url = new URI(linkDDI).toURL();
		SaxonTransformer transformer = new SaxonTransformer();
		transformer.xslTransform(url, Constants.XSLT_STRUCTURED_VARIABLES, tempPath);

		actualString = TextFileReader.readFromPath(tempPath, new FileSystemImpl());
    	
    	if (actualString.trim().contains("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")) {
    		actualAnswer = "DDI collected";
    	}
    }
	
	@Then("The DDIs should have some variables like {string}")
    public void search_for_variables(String expectedVariable)  {
    	if (actualAnswer.contentEquals("DDI collected") &&  actualString.contains(expectedVariable)){
    		actualAnswer = actualAnswer + " with variable";
    	}
    }

	@Then("The DDIs I compared should answer {string}")
    public void i_should_be_told(String expectedAnswer)  {
    	assertNotNull(actualString);
    	assertEquals(expectedAnswer, actualAnswer);
    	
    }

}