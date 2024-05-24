package cucumber.functional_tests;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import fr.insee.kraftwerk.api.process.MainProcessing;
import fr.insee.kraftwerk.core.Constants;
import fr.insee.kraftwerk.core.KraftwerkError;
import fr.insee.kraftwerk.core.exceptions.KraftwerkException;
import fr.insee.kraftwerk.core.exceptions.NullException;
import fr.insee.kraftwerk.core.inputs.UserInputsFile;
import fr.insee.kraftwerk.core.metadata.MetadataModel;
import fr.insee.kraftwerk.core.metadata.MetadataUtils;
import fr.insee.kraftwerk.core.metadata.VariableType;
import fr.insee.kraftwerk.core.outputs.OutputFiles;
import fr.insee.kraftwerk.core.outputs.csv.CsvOutputFiles;
import fr.insee.kraftwerk.core.sequence.*;
import fr.insee.kraftwerk.core.utils.CsvUtils;
import fr.insee.kraftwerk.core.utils.FileUtils;
import fr.insee.kraftwerk.core.vtl.VtlBindings;
import fr.insee.kraftwerk.core.vtl.VtlExecute;
import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static cucumber.TestConstants.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;

// Main example
public class MainDefinitions {

	Path inDirectory = Paths.get(FUNCTIONAL_TESTS_INPUT_DIRECTORY);
	static Path outDirectory = Paths.get(FUNCTIONAL_TESTS_OUTPUT_DIRECTORY);
	Path tempDirectory = Paths.get(FUNCTIONAL_TESTS_TEMP_DIRECTORY);
	String userInputFileName = Constants.USER_INPUT_FILE;
	UserInputsFile userInputsFile;
	String campaignName = "";
	VtlBindings vtlBindings = new VtlBindings();
	OutputFiles outputFiles;
	Map<String, MetadataModel> metadataModelMap;

	private ControlInputSequence controlInputSequence;

	VtlExecute vtlExecute = new VtlExecute();
	List<KraftwerkError> errors = new ArrayList<>();

	@Before
	public void clean() throws KraftwerkException {
		FileUtils.deleteDirectory(outDirectory);
	}

	@Given("Step 0 : We have some survey in directory {string}")
	public void launch_all_steps(String campaignDirectoryName) {
		outDirectory = Paths.get(FUNCTIONAL_TESTS_OUTPUT_DIRECTORY);

		this.campaignName = campaignDirectoryName;
		inDirectory = inDirectory.resolve(campaignName);
		outDirectory = outDirectory.resolve(campaignName);
		controlInputSequence = new ControlInputSequence(inDirectory.toString());
		// We clean the output and the temp directory
		deleteDirectory(outDirectory.toFile());
		deleteDirectory(tempDirectory.toFile());
	}

	@Given("We have a test VTL script named {string} creating a variable {string} in dataset {string}")
	public void init_VTL_script(String vtlScriptName, String variableToCreate, String datasetName) throws IOException {
		String generatedVTL = datasetName + " := " + datasetName + " [calc " + variableToCreate + " := \"TEST\"];";

		Path vtlFolderPath = Files.createDirectories(Path.of(Constants.VTL_FOLDER_PATH).resolve("tcm"));
		Path vtlPath = vtlFolderPath.resolve(vtlScriptName + ".vtl");

		if(Files.exists(vtlPath)) //If file already exist (legitimate script) backup
			Files.copy(vtlPath,vtlFolderPath.resolve(vtlScriptName + ".bkp"));
		else
			Files.createFile(vtlPath);
		Files.write(vtlPath,generatedVTL.getBytes());
	}

	@When("Step 1 : We initialize the input files")
	public void initialize_input_files() throws KraftwerkException {
		System.out.println("InDirectory value : " + inDirectory);
		userInputsFile = controlInputSequence.getUserInputs(inDirectory);
		vtlBindings = new VtlBindings();
	}

	@When("Step 1 : We initialize with input file {string}")
	public void initialize_with_specific_input(String inputFileName) throws KraftwerkException {
		userInputsFile = new UserInputsFile(inDirectory.resolve(inputFileName), inDirectory);
		vtlBindings = new VtlBindings();
	}

	@When("Step 1 : We initialize metadata model with lunatic specification only")
	public void initialize_metadata_model_with_lunatic() throws KraftwerkException {
		MainProcessing mp = new MainProcessing(inDirectory.toString(), false,false,false, "defaultDirectory", 419430400L);
		mp.init();
		userInputsFile=mp.getUserInputsFile();
		metadataModelMap=mp.getMetadataModels();
	}

	@When("Step 1 : We initialize metadata model with DDI specification only")
	public void initialize_metadata_model_with_DDI() throws KraftwerkException {
		MainProcessing mp = new MainProcessing(inDirectory.toString(), false,false,true, "defaultDirectory", 419430400L);
		mp.init();
		userInputsFile=mp.getUserInputsFile();
		metadataModelMap=mp.getMetadataModels();
	}

	@When("Step 1 : We launch main service")
	public void launch_main() throws KraftwerkException {
		// We clean the output and the temp directory
		deleteDirectory(outDirectory.toFile());
		deleteDirectory(tempDirectory.toFile());
		MainProcessing mp = new MainProcessing(inDirectory.toString(), false, "defaultDirectory", 419430400L);
		mp.runMain();
	}

	@When("We launch main service 2 times")
	public void launch_main_2() throws KraftwerkException, InterruptedException {
		// We clean the output and the temp directory
		deleteDirectory(outDirectory.toFile());
		deleteDirectory(tempDirectory.toFile());
		MainProcessing mp = new MainProcessing(inDirectory.toString(), false, "defaultDirectory", 419430400L);
		mp.runMain();
		await().atMost(2, TimeUnit.SECONDS);
		mp = new MainProcessing(inDirectory.toString(), false, "defaultDirectory", 419430400L);
		mp.runMain();
	}

	@When("Step 1 : We launch main service file by file")
	public void launch_main_filebyfile() throws KraftwerkException {
		// We clean the output and the temp directory
		deleteDirectory(outDirectory.toFile());
		deleteDirectory(tempDirectory.toFile());
		MainProcessing mp = new MainProcessing(inDirectory.toString(), true,
				Paths.get(FUNCTIONAL_TESTS_INPUT_DIRECTORY).resolve(campaignName).toString(), 419430400L);
		mp.runMain();
	}

	@When("Step 2 : We get each unimodal dataset")
	public void unimodal_treatments() throws NullException {
		metadataModelMap = MetadataUtils.getMetadata(userInputsFile.getModeInputsMap());
		BuildBindingsSequence buildBindingsSequence = new BuildBindingsSequence(true);
		for (String dataMode : userInputsFile.getModeInputsMap().keySet()) {
			boolean withDDI = true;
			buildBindingsSequence.buildVtlBindings(userInputsFile, dataMode, vtlBindings, metadataModelMap.get(dataMode), withDDI,null);
			UnimodalSequence unimodal = new UnimodalSequence();
			unimodal.applyUnimodalSequence(userInputsFile, dataMode, vtlBindings, errors, metadataModelMap);
		}
	}

	@When("Step 3 : We aggregate each unimodal dataset into a multimodal dataset")
	public void aggregate_datasets() {
		MultimodalSequence multimodalSequence = new MultimodalSequence();
		multimodalSequence.multimodalProcessing(userInputsFile, vtlBindings, errors, metadataModelMap);
	}

	@When("Step 4 : We export the final version")
	public void export_results() throws KraftwerkException {
		WriterSequence writerSequence = new WriterSequence();
		LocalDateTime localDateTime = LocalDateTime.now();
		writerSequence.writeOutputFiles(inDirectory, localDateTime, vtlBindings, userInputsFile.getModeInputsMap(), metadataModelMap, errors);
		writeErrorsFile(inDirectory, localDateTime, errors);
		outputFiles = new CsvOutputFiles(outDirectory, vtlBindings, userInputsFile.getModes());
	}

	@Then("Step 5 : We check if we have {int} lines")
	public void count_lines_in_root_tables(int expectedLineCount) throws CsvValidationException, IOException {
		// Go to first datetime folder
		Path executionOutDirectory = outDirectory.resolve(Objects.requireNonNull(new File(outDirectory.toString()).listFiles(File::isDirectory))[0].getName());
		// Get reader to read the root table written in outputs
		System.out.println("Check output file path : "
				+ executionOutDirectory.resolve(outputFiles.outputFileName(Constants.ROOT_GROUP_NAME)));
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
		// Go to first datetime folder
		Path executionOutDirectory = outDirectory.resolve(Objects.requireNonNull(new File(outDirectory.toString()).listFiles(File::isDirectory))[0].getName());

		CSVReader csvReader = CsvUtils
				.getReader(executionOutDirectory.resolve(executionOutDirectory.getFileName() + "_" + Constants.ROOT_GROUP_NAME + ".csv"));
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
		if (tableName == null || tableName.isEmpty())
			tableName = Constants.ROOT_GROUP_NAME;

		// Get reader to read the root table written in outputs
		CSVReader csvReader = CsvUtils
				.getReader(outputFiles.getOutputFolder().resolve(outputFiles.outputFileName(tableName)));
		// get header
		String[] header = csvReader.readNext();
		int idUEPosition = Arrays.asList(header).indexOf(Constants.ROOT_IDENTIFIER_NAME);
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

	private void writeErrorsFile(Path inDirectory,LocalDateTime localDateTime, List<KraftwerkError> errors) {
		Path tempOutputPath = FileUtils.transformToOut(inDirectory,localDateTime).resolve(Constants.ERRORS_FILE_NAME);
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

	@Then("In a file named {string} there should be a {string} field")
	public void check_field_existence(String fileName, String fieldName) throws IOException, CsvValidationException {
		// Go to first datetime folder
		Path executionOutDirectory = outDirectory.resolve(Objects.requireNonNull(new File(outDirectory.toString()).listFiles(File::isDirectory))[0].getName());

		File outputReportingDataFile = new File(executionOutDirectory + "/" + fileName);

		// File existence assertion
		assertThat(outputReportingDataFile).exists().isFile().canRead();

		CSVReader csvReader = CsvUtils.getReader(
				outputReportingDataFile.toPath()
		);


		// Get header
		String[] header = csvReader.readNext();
		csvReader.close();

		assertThat(header).isNotEmpty().contains(fieldName);
	}

	@When("We clean the test VTL script named {string}")
	public void clean_vtl(String vtlScriptName) throws IOException {
		Path vtlPath = Path.of(Constants.VTL_FOLDER_PATH).resolve("tcm").resolve(vtlScriptName + ".vtl");
		Path bkpPath = vtlPath.getParent().resolve(vtlScriptName + ".bkp");

		Files.deleteIfExists(vtlPath);

		if(Files.exists(bkpPath)) // Put backup back
			Files.copy(bkpPath,bkpPath.getParent().resolve(vtlScriptName + ".vtl"));

		Files.deleteIfExists(bkpPath);
	}

	@Then("We should have a metadata model with {int} variables")
	public void check_variables_count(int nbVariablesExpected) throws IOException, CsvValidationException {
		String mode = userInputsFile.getModes().getFirst();
		int nbVariables = metadataModelMap.get(mode).getVariables().getVariables().size();
		assertThat(nbVariables).isEqualTo(nbVariablesExpected);
	}

	@And("We should have {int} of type STRING")
	public void check_string_variables_count(int nbStringVariablesExpected) throws IOException, CsvValidationException {
		String mode = userInputsFile.getModes().getFirst();
		int nbStringVariables = metadataModelMap.get(mode).getVariables().getVariables().values().stream().filter(v -> v.getType()== VariableType.STRING).toArray().length;
		assertThat(nbStringVariables).isEqualTo(nbStringVariablesExpected);
	}
}