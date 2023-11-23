package cucumber.functional_tests;

import cucumber.TestConstants;
import fr.insee.kraftwerk.core.Constants;
import fr.insee.kraftwerk.core.KraftwerkError;
import fr.insee.kraftwerk.core.dataprocessing.CalculatedProcessing;
import fr.insee.kraftwerk.core.dataprocessing.DataProcessing;
import fr.insee.kraftwerk.core.exceptions.KraftwerkException;
import fr.insee.kraftwerk.core.metadata.CalculatedVariables;
import fr.insee.kraftwerk.core.metadata.LunaticReader;
import fr.insee.kraftwerk.core.metadata.VariablesMap;
import fr.insee.kraftwerk.core.metadata.XsltDDIReader;
import fr.insee.kraftwerk.core.parsers.DataParser;
import fr.insee.kraftwerk.core.parsers.LunaticXmlDataParser;
import fr.insee.kraftwerk.core.rawdata.SurveyRawData;
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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

// calculated_processing.feature
public class CalculatedProcessingDefinition {

    private final Map<String, Map<String, Map<String, String>>> campaignPacks = new HashMap<>();

    private String campaignName;
    private String dataMode;
    private VariablesMap variablesMap;
    private VtlBindings vtlBindings;
    private Dataset outDataset;
    private List<String> variableNamesList;
	
	VtlExecute vtlExecute = new VtlExecute();
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
    public void getCampaignFiles(String campaignName, String dataMode) throws MalformedURLException, KraftwerkException {
        this.campaignName = campaignName;
        this.dataMode = dataMode;
        //
        variablesMap = new XsltDDIReader().getVariablesFromDDI(
                Constants.convertToUrl(campaignPacks.get(campaignName).get(dataMode).get("ddi")));
        //
        SurveyRawData data = new SurveyRawData();
        data.setVariablesMap(variablesMap);
        DataParser parser = new LunaticXmlDataParser(data);
        parser.parseSurveyData(Paths.get(campaignPacks.get(campaignName).get(dataMode).get("data")));
        //
        vtlBindings = new VtlBindings();
        vtlExecute.convertToVtlDataset(data, "TEST", vtlBindings);
    }

    @When("I process calculated variables")
    public void readCampaignData() {
        //
        CalculatedVariables calculatedVariables = LunaticReader.getCalculatedFromLunatic(
                Path.of(campaignPacks.get(campaignName).get(dataMode).get("lunatic")));
        DataProcessing calculatedProcessing = new CalculatedProcessing(vtlBindings,calculatedVariables);
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
            Object expectedValue;
            switch (variablesMap.getVariable(calculatedName).getType()) {
                case STRING: expectedValue = expectedValues.get(j); break;
                case NUMBER: expectedValue = Long.valueOf(expectedValues.get(j)); break;
                default: throw new IllegalArgumentException(String.format(
                        "Couldn't cast value \"%s\" defined in \"Calculated Processing\" scenario.",
                        expectedValues.get(j)));
            }
            //
            assertEquals(expectedValue, outDataset.getDataPoints().get(lineNumber).get(calculatedName));
        }
    }

}
