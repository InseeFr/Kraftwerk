package cucumber.functional_tests;

import static cucumber.TestConstants.FUNCTIONAL_TESTS_INPUT_DIRECTORY;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Path;
import java.nio.file.Paths;

import fr.insee.kraftwerk.core.Constants;
import fr.insee.kraftwerk.core.exceptions.KraftwerkException;
import fr.insee.kraftwerk.core.exceptions.NullException;
import fr.insee.kraftwerk.core.extradata.paradata.Paradata;
import fr.insee.kraftwerk.core.extradata.paradata.ParadataParser;
import fr.insee.kraftwerk.core.inputs.ModeInputs;
import fr.insee.kraftwerk.core.inputs.UserInputs;
import fr.insee.kraftwerk.core.metadata.DDIReader;
import fr.insee.kraftwerk.core.parsers.DataParser;
import fr.insee.kraftwerk.core.parsers.DataParserManager;
import fr.insee.kraftwerk.core.rawdata.QuestionnaireData;
import fr.insee.kraftwerk.core.rawdata.SurveyRawData;
import fr.insee.kraftwerk.core.sequence.ControlInputSequence;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class ParadataDefinitions {

	SurveyRawData data;
	Path paradataFolder;
	Paradata paradata;
	String userInputFileName = Constants.USER_INPUT_FILE;
	ModeInputs modeInputs;
	private ControlInputSequence controlInputSequence;

	@Given("We read data from input named {string}")
	public void launch_all_steps(String campaignName) throws KraftwerkException {

		Path campaignDirectory = Paths.get(FUNCTIONAL_TESTS_INPUT_DIRECTORY).resolve(campaignName);
		controlInputSequence = new ControlInputSequence(campaignDirectory.toString());
		UserInputs userInputs = controlInputSequence.getUserInputs(campaignDirectory);;
		// For now, only one file
		String modeName = userInputs.getModes().get(0);
		modeInputs = userInputs.getModeInputs(modeName);
		// parse data
		data = new SurveyRawData();
		data.setDataMode(modeInputs.getDataMode());
		data.setVariablesMap(DDIReader.getVariablesFromDDI(modeInputs.getDdiUrl()));
		DataParser parser = DataParserManager.getParser(modeInputs.getDataFormat(), data);
		parser.parseSurveyData(modeInputs.getDataFile());
		// get paradata folder
		paradataFolder = modeInputs.getParadataFolder();
	}
 
	@When("I try to collect paradata's useful infos")
	public void collect_paradata_test() throws NullException {
		if (modeInputs.getParadataFolder() != null
				&& !modeInputs.getParadataFolder().toString().contentEquals("")) {
			ParadataParser paraDataParser = new ParadataParser();
			paradata = new Paradata(paradataFolder);
			paraDataParser.parseParadata(paradata, data);
			paradata.getListParadataUE().get(0).getEvents().stream().forEach(e -> log.debug(e.getTimestamp() + ","+ e.getIdParadataObject()));
		}
	}
	
	@Then("We check we have {int} paradata lines in total, and that UE {string} has {int} orchestrators during {string} and {int} sessions during {string}")
	public void check_results(int expectedNbLines, String identifier, int expectedNb, String expectedDuration, int nbSessions, String expectedSessionTime) throws Exception {
		assertEquals(expectedNbLines, paradata.getListParadataUE().size());
		QuestionnaireData questionnaire = data.getQuestionnaires().stream().filter(
				questionnaireToSearch -> identifier.equals(questionnaireToSearch.getIdentifier()))
				.findAny().orElse(null);
		assertEquals(expectedNb, Integer.parseInt(questionnaire.getAnswers().getValue(Constants.NUMBER_ORCHESTRATORS_NAME)));
		assertEquals(expectedDuration.trim(), questionnaire.getAnswers().getValue(Constants.LENGTH_ORCHESTRATORS_NAME).trim());
		
		assertEquals(nbSessions, Integer.parseInt(questionnaire.getAnswers().getValue(Constants.NUMBER_SESSIONS_NAME)));
		assertEquals(expectedSessionTime.trim(), questionnaire.getAnswers().getValue(Constants.LENGTH_SESSIONS_NAME).trim());
		

	}
	
}