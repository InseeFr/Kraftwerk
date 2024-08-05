package cucumber.functional_tests;

import cucumber.TestConstants;
import fr.insee.bpm.exceptions.MetadataParserException;
import fr.insee.kraftwerk.core.Constants;
import fr.insee.kraftwerk.core.KraftwerkError;
import fr.insee.kraftwerk.core.dataprocessing.CalculatedProcessing;
import fr.insee.kraftwerk.core.dataprocessing.DataProcessing;
import fr.insee.kraftwerk.core.exceptions.KraftwerkException;
import fr.insee.bpm.metadata.model.CalculatedVariables;
import fr.insee.bpm.metadata.reader.ddi.DDIReader;
import fr.insee.bpm.metadata.reader.lunatic.LunaticReader;
import fr.insee.bpm.metadata.model.MetadataModel;
import fr.insee.kraftwerk.core.parsers.DataParser;
import fr.insee.kraftwerk.core.parsers.LunaticXmlDataParser;
import fr.insee.kraftwerk.core.rawdata.SurveyRawData;
import fr.insee.kraftwerk.core.utils.files.FileSystemImpl;
import fr.insee.kraftwerk.core.vtl.VtlBindings;
import fr.insee.kraftwerk.core.vtl.VtlExecute;
import fr.insee.vtl.model.Dataset;
import io.cucumber.java.Before;
import io.cucumber.java.ParameterType;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

// calculated_processing.feature
public class CalculatedProcessingDefinition {

    private final Map<String, Map<String, Map<String, String>>> campaignPacks = new HashMap<>();

    private String campaignName;
    private String dataMode;
    private MetadataModel metadataModel;
    private VtlBindings vtlBindings;
    private Dataset outDataset;
    private List<String> variableNamesList;
	
	VtlExecute vtlExecute = new VtlExecute(new FileSystemImpl());
    List<KraftwerkError> errors = new ArrayList<>();

    @ParameterType("(?:[^,]*)(?:,\\s?[^,]*)*")
    public List<String> listOfStrings(String arg){
        return Arrays.asList(arg.split(",\\s?"));
    }


    @Before
    public void setUpCampaignPacks() {
        //Sample test campaign (with reporting data)
        campaignPacks.put(TestConstants.SAMPLETEST_REPORTINGDATA_CAMPAIGN_NAME, new HashMap<>());
        //TODO Put other sample test campaigns once they are ready
    }
    @Given("I read data from campaign {string}, mode {string}")
    public void getCampaignFiles(String campaignName, String dataMode) throws MalformedURLException, KraftwerkException, URISyntaxException, MetadataParserException {
        this.campaignName = campaignName;
        this.dataMode = dataMode;
        //
        metadataModel = DDIReader.getMetadataFromDDI(
                Constants.convertToUrl(campaignPacks.get(campaignName).get(dataMode).get("ddi")).toString(), new FileSystemImpl().readFile(campaignPacks.get(campaignName).get(dataMode).get("ddi")));
        //
        SurveyRawData data = new SurveyRawData();
        data.setMetadataModel(metadataModel);
        DataParser parser = new LunaticXmlDataParser(data, new FileSystemImpl());
        parser.parseSurveyData(Paths.get(campaignPacks.get(campaignName).get(dataMode).get("data")),null);
        //
        vtlBindings = new VtlBindings();
        vtlExecute.convertToVtlDataset(data, "TEST", vtlBindings);
    }

    @When("I process calculated variables")
    public void readCampaignData() {
        //
        CalculatedVariables calculatedVariables = LunaticReader.getCalculatedFromLunatic(
                new FileSystemImpl().readFile(Path.of(campaignPacks.get(campaignName).get(dataMode).get("lunatic")).toString()));
        DataProcessing calculatedProcessing = new CalculatedProcessing(vtlBindings,calculatedVariables, new FileSystemImpl());
        calculatedProcessing.applyVtlTransformations("TEST", null,errors);
        //
        outDataset = vtlBindings.getDataset("TEST");
    }

    @Then("I should have the calculated variables \"{listOfStrings}\" in the dataset structure")
    public void calculatedVariablesAreThere(List<String> variableNamesList) {
        this.variableNamesList = variableNamesList;
        //
        assertNotNull(outDataset);
        //
        variableNamesList.forEach(calculatedName ->
                assertTrue(outDataset.getDataStructure().containsKey(calculatedName)));
    }

    @And("I should have corresponding values \"{listOfStrings}\" in dataset line {int}")
    public void valuesAreCorrect(List<String> expectedValues, int lineNumber) {
        for (int j=0; j<variableNamesList.size(); j++) {
            //
            String calculatedName = variableNamesList.get(j);
            Object expectedValue = switch (metadataModel.getVariables().getVariable(calculatedName).getType()) {
				case STRING -> expectedValues.get(j);
				case NUMBER -> Long.valueOf(expectedValues.get(j));
				default -> throw new IllegalArgumentException(String.format(
						"Couldn't cast value \"%s\" defined in \"Calculated Processing\" scenario.",
						expectedValues.get(j)));
			};
			//
            assertEquals(expectedValue, outDataset.getDataPoints().get(lineNumber).get(calculatedName));
        }
    }

}
