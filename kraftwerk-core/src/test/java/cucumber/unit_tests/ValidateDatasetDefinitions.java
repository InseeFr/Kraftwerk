package cucumber.unit_tests;
/*
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;

import fr.insee.kraftwerk.Constants;
import fr.insee.kraftwerk.donnees.Donnees;
import fr.insee.kraftwerk.donnees.SourceDonnees;
import fr.insee.kraftwerk.entrees.DDI;
import fr.insee.kraftwerk.entrees.FichierDonnees;
import fr.insee.kraftwerk.entrees.LecteurRessources;
import fr.insee.kraftwerk.modes.ModesManager;
import fr.insee.kraftwerk.parsers.XformsDataParser;
import fr.insee.kraftwerk.parsers.DataParser;
import fr.insee.kraftwerk.parsers.DataParserManager;
import fr.insee.kraftwerk.parsers.DonneesParser;
import fr.insee.kraftwerk.parsers.DonneesParserManager;
import fr.insee.kraftwerk.rawdata.SurveyRawData;
import fr.insee.kraftwerk.traitements.Validation;
import fr.insee.kraftwerk.traitements.xsl.XslTransformation;
import fr.insee.kraftwerk.parsers.DataFormat;
import fr.insee.kraftwerk.rawdata.SurveyRawData;
*/
public class ValidateDatasetDefinitions {
	/*private SurveyRawData donnees = null;
	private DataFormat source = DataFormat.COLEMAN;
	private boolean actualAnswer = false;

	@Given("We have a dataset")
	public void create_dataset() throws Exception {

		XformsDataParser parser = (XformsDataParser) DataParserManager.getParser(source);
		LecteurRessources lecteurRessources = LecteurRessources.getInstance();
		List<FichierDonnees> listeFichiersEntree = lecteurRessources.getFichiersEntree();
		/* Lecture des donnees et validation technique (par le parser) */
/*
		for (FichierDonnees f : listeFichiersEntree) {
			donnees = parser.lireDonneesCollecte(f.getNomFichier(), Constants.TESTS_INPUTS_FOLDER);
			List<Donnees> listeDonnees = new ArrayList<>();
			listeDonnees.add(donnees);
			/* Empilement des fichiers de donnees */
			/*donnees = parser.empiler(listeDonnees);
			ModesManager manager = ModesManager.getManager(source);

			datasetToCheck = manager.traiterSpecs(f, donnees, inseeVtlConnector);

		}
	}

	@When("I check if it's valid")
	public void check_validity() throws Exception {
		if (Validation.valider(datasetToCheck)) {
			actualAnswer = true;
		}
	}

	@Then("It should answer validity {string}")
	public void it_should_say_validity(String expectedAnswer) {
		assertTrue(actualAnswer);
	}*/

}