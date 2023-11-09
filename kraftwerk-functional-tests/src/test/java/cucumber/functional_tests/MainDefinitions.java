package src.test.java.cucumber.functional_tests;

import static cucumber.TestConstants.FUNCTIONAL_TESTS_INPUT_DIRECTORY;
import static cucumber.TestConstants.FUNCTIONAL_TESTS_OUTPUT_DIRECTORY;
import static cucumber.TestConstants.FUNCTIONAL_TESTS_TEMP_DIRECTORY;
import static fr.insee.kraftwerk.core.Constants.OUTCOME_ATTEMPT_SUFFIX_NAME;
import static fr.insee.kraftwerk.core.Constants.ROOT_IDENTIFIER_NAME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import com.opencsv.exceptions.CsvValidationException;

import fr.insee.kraftwerk.api.process.MainProcessing;
import fr.insee.kraftwerk.core.Constants;
import fr.insee.kraftwerk.core.KraftwerkError;
import fr.insee.kraftwerk.core.exceptions.KraftwerkException;
import fr.insee.kraftwerk.core.exceptions.NullException;
import fr.insee.kraftwerk.core.inputs.UserInputs;
import fr.insee.kraftwerk.core.metadata.MetadataUtils;
import fr.insee.kraftwerk.core.metadata.VariablesMap;
import fr.insee.kraftwerk.core.outputs.CsvOutputFiles;
import fr.insee.kraftwerk.core.outputs.OutputFiles;
import fr.insee.kraftwerk.core.sequence.BuildBindingsSequence;
import fr.insee.kraftwerk.core.sequence.ControlInputSequence;
import fr.insee.kraftwerk.core.sequence.MultimodalSequence;
import fr.insee.kraftwerk.core.sequence.UnimodalSequence;
import fr.insee.kraftwerk.core.sequence.WriterSequence;
import fr.insee.kraftwerk.core.utils.CsvUtils;
import fr.insee.kraftwerk.core.utils.FileUtils;
import fr.insee.kraftwerk.core.vtl.VtlBindings;
import fr.insee.kraftwerk.core.vtl.VtlExecute;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

// Main example
public class MainDefinitions {

	Path inDirectory = Paths.get(FUNCTIONAL_TESTS_INPUT_DIRECTORY);
	static Path outDirectory = Paths.get(FUNCTIONAL_TESTS_OUTPUT_DIRECTORY);
	Path tempDirectory = Paths.get(FUNCTIONAL_TESTS_TEMP_DIRECTORY);
	String userInputFileName = Constants.USER_INPUT_FILE;
	UserInputs userInputs;
	String campaignName = "";
	VtlBindings vtlBindings = new VtlBindings();
	OutputFiles outputFiles;
	Map<String, VariablesMap> metadataVariables;

	private ControlInputSequence controlInputSequence;

	VtlExecute vtlExecute = new VtlExecute();
	List<KraftwerkError> errors = new ArrayList<>();

	@Before
	public void clean() throws KraftwerkException {
		FileUtils.deleteDirectory(outDirectory);
	}

	@Given("Step 0 : We have some survey in directory {string}")
	public void launch_all_steps(String campaignDirectoryName) {
		this.campaignName = campaignDirectoryName;
		inDirectory = inDirectory.resolve(campaignName);
		outDirectory = outDirectory.resolve(campaignName);
		controlInputSequence = new ControlInputSequence(inDirectory.toString());
		// We clean the output and the temp directory
		deleteDirectory(outDirectory.toFile());
		deleteDirectory(tempDirectory.toFile());
	}

	@When("Step 1 : We initialize the input files")
	public void initialize_input_files() throws KraftwerkException {
		System.out.println("InDirectory value : " + inDirectory);
		userInputs = controlInputSequence.getUserInputs(inDirectory);
		vtlBindings = new VtlBindings();
	}

	@When("Step 1 : We initialize with input file {string}")
	public void initialize_with_specific_input(String inputFileName) throws KraftwerkException {
		userInputs = new UserInputs(inDirectory.resolve(inputFileName), inDirectory);
		vtlBindings = new VtlBindings();
	}

	@When("Step 1 : We launch main service")
	public void launch_main() throws KraftwerkException {
		// We clean the output and the temp directory
		deleteDirectory(outDirectory.toFile());
		deleteDirectory(tempDirectory.toFile());
		MainProcessing mp = new MainProcessing(inDirectory.toString(), false, "defaultDirectory");
		mp.runMain();
	}

	@When("Step 1 : We launch main service file by file")
	public void launch_main_filebyfile() throws KraftwerkException {
		// We clean the output and the temp directory
		deleteDirectory(outDirectory.toFile());
		deleteDirectory(tempDirectory.toFile());
		MainProcessing mp = new MainProcessing(inDirectory.toString(), true,
				Paths.get(FUNCTIONAL_TESTS_INPUT_DIRECTORY).resolve(campaignName).toString());
		mp.runMain();
	}

	@When("Step 2 : We get each unimodal dataset")
	public void unimodal_treatments() throws NullException {
		metadataVariables = MetadataUtils.getMetadata(userInputs.getModeInputsMap());
		BuildBindingsSequence buildBindingsSequence = new BuildBindingsSequence(true);
		for (String dataMode : userInputs.getModeInputsMap().keySet()) {
			boolean withDDI = true;
			buildBindingsSequence.buildVtlBindings(userInputs, dataMode, vtlBindings, metadataVariables, withDDI);
			UnimodalSequence unimodal = new UnimodalSequence();
			unimodal.applyUnimodalSequence(userInputs, dataMode, vtlBindings, errors, metadataVariables);
		}
	}

	@When("Step 3 : We aggregate each unimodal dataset into a multimodal dataset")
	public void aggregate_datasets() {
		MultimodalSequence multimodalSequence = new MultimodalSequence();
		multimodalSequence.multimodalProcessing(userInputs, vtlBindings, errors, metadataVariables);
	}

	@When("Step 4 : We export the final version")
	public void export_results() throws KraftwerkException {
		WriterSequence writerSequence = new WriterSequence();
		writerSequence.writeOutputFiles(inDirectory, vtlBindings, userInputs.getModeInputsMap(),
				userInputs.getMultimodeDatasetName(), metadataVariables, errors);
		writeErrorsFile(inDirectory, errors);
		outputFiles = new CsvOutputFiles(outDirectory, vtlBindings, userInputs.getModes(),
				userInputs.getMultimodeDatasetName());
	}

	@Then("Step 5 : We check if we have {int} lines")
	public void count_lines_in_root_tables(int expectedLineCount) throws CsvValidationException, IOException {
		// Get reader to read the root table written in outputs
		System.out.println("Check output file path : "
				+ outDirectory.resolve(outputFiles.outputFileName(Constants.ROOT_GROUP_NAME)));
		CSVReader csvReader = CsvUtils
				.getReader(outDirectory.resolve(outputFiles.outputFileName(Constants.ROOT_GROUP_NAME)));
		// Count
		int lineCount = 0;
		while ((csvReader.readNext()) != null) {
			lineCount++;
		}
		// Close reader
		csvReader.close();
		// Test
		assertEquals(expectedLineCount, lineCount);
	}

	@Then("Step 2 : We check root output file has {int} lines and {int} variables")
	public void check_output_root_table(int expectedLineCount, int expectedVariablesCount)
			throws IOException, CsvValidationException {
		CSVReader csvReader = CsvUtils
				.getReader(outDirectory.resolve(outDirectory.getFileName() + "_" + Constants.ROOT_GROUP_NAME + ".csv"));
		// get header
		String[] header = csvReader.readNext();
		// Count
		int variableCount = header.length;

		// Count
		int lineCount = 1;
		while ((csvReader.readNext()) != null) {
			lineCount++;
		}

		// Close reader
		csvReader.close();
		// Test
		assertEquals(expectedVariablesCount, variableCount);
		assertEquals(expectedLineCount, lineCount);

	}

	@Then("Step 6 : We check if we have {int} variables")
	public void count_variables_in_root_tables(int expectedVariablesCount) throws CsvValidationException, IOException {
		// Get reader to read the root table written in outputs
		CSVReader csvReader = CsvUtils.getReader(
				outputFiles.getOutputFolder().resolve(outputFiles.outputFileName(Constants.ROOT_GROUP_NAME)));
		// get header
		String[] header = csvReader.readNext();
		// Count
		int variableCount = header.length;

		// Close reader
		csvReader.close();
		// Test
		assertEquals(expectedVariablesCount, variableCount);
	}

	@Then("Step 7 : We check that id {string} has value {string} for variable {string} in table {string}")
	public void checkVariableValue(String idUE, String expectedValue, String variable, String tableName)
			throws IOException, CsvValidationException {
		if (tableName == null || tableName.equals(""))
			tableName = Constants.ROOT_GROUP_NAME;

		// Get reader to read the root table written in outputs
		CSVReader csvReader = CsvUtils
				.getReader(outputFiles.getOutputFolder().resolve(outputFiles.outputFileName(tableName)));
		// get header
		String[] header = csvReader.readNext();
		int idUEPosition = Arrays.asList(header).indexOf("IdUE");
		int varPosition = Arrays.asList(header).indexOf(variable);

		// get the line corresponding to idUE
		String[] line;
		String value = null;
		while ((line = csvReader.readNext()) != null) {
			if (line[idUEPosition].equals(idUE))
				value = line[varPosition];
		}

		// Close reader
		csvReader.close();
		// Test
		assertEquals(expectedValue, value);
	}

	boolean deleteDirectory(File directoryToBeDeleted) {
		File[] allContents = directoryToBeDeleted.listFiles();
		if (allContents != null) {
			for (File file : allContents) {
				deleteDirectory(file);
			}
		}
		return directoryToBeDeleted.delete();
	}

	private void writeErrorsFile(Path inDirectory, List<KraftwerkError> errors) {
		Path tempOutputPath = FileUtils.transformToOut(inDirectory).resolve("errors.txt");
		FileUtils.createDirectoryIfNotExist(tempOutputPath.getParent());

		// Write errors file
		if (!errors.isEmpty()) {
			try (FileWriter myWriter = new FileWriter(tempOutputPath.toFile(), true)) {
				for (KraftwerkError error : errors) {
					myWriter.write(error.toString());
				}
				System.out.println((String.format("Text file: %s successfully written", tempOutputPath)));
			} catch (IOException e) {
				System.out
						.println((String.format("Error occurred when trying to write text file: %s", tempOutputPath)));
			}
		} else {
			System.out.println("No error found during VTL transformations");
		}
	}

	

    // Existence and structure test
    @Then("We should have a file named {string} in directory {string} with {int} contact attempts fields")
    public void check_contact_attempt_file(String fileName, String directory, int expectedFieldCount) throws IOException, CsvException {
        File outputContactAttemptsFile = new File(outDirectory + "/" + directory + "/" + fileName);

        // File existence assertion
        assertThat(outputContactAttemptsFile).exists().isFile().canRead();

        CSVReader csvReader = CsvUtils.getReader(
                Path.of(outDirectory + "/" + directory + "/" + fileName)
        );

        // Get header
        String[] header = csvReader.readNext();

        // Compute expected header
        List<String> expectedHeaderList = new ArrayList<>();
        expectedHeaderList.add(ROOT_IDENTIFIER_NAME);
        for(int i = 1; i < expectedFieldCount + 1; i++){
            // append attempt field
            expectedHeaderList.add(OUTCOME_ATTEMPT_SUFFIX_NAME + "_" + i);
            // append attempt date field
            expectedHeaderList.add(OUTCOME_ATTEMPT_SUFFIX_NAME + "_" + i + "_DATE");
        }

        String[] expectedHeader = new String[expectedHeaderList.size()];
        expectedHeaderList.toArray(expectedHeader);


        // Header assertion
        assertThat(header).containsExactly(expectedHeader);
    }
}