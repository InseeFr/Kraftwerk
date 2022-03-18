package cucumber;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class DocumentationDefinitions {
	public int nombreActuel;
	@Given("I have a number, let's say {int}")
	public void initialiser(int entier) throws Exception {
		nombreActuel = entier;
	}

	@When("I try to increment that number")
	public void ajouter_un() throws Exception {
		nombreActuel++;
	}

	@Then("I see if the function did increment my number, it should answer {int}")
	public void verifier(int nombreAttendu) throws Exception {
		assertEquals(nombreAttendu, nombreActuel);

	}

}