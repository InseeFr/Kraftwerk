package cucumber.functional_tests;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvValidationException;
import cucumber.TestConstants;
import fr.insee.kraftwerk.api.process.MainProcessingGenesis;
import fr.insee.kraftwerk.core.Constants;
import fr.insee.kraftwerk.core.data.model.Mode;
import fr.insee.kraftwerk.core.data.model.SurveyUnitUpdateLatest;
import fr.insee.kraftwerk.core.data.model.VariableModel;
import fr.insee.kraftwerk.core.exceptions.KraftwerkException;
import fr.insee.kraftwerk.core.utils.SqlUtils;
import fr.insee.kraftwerk.core.utils.files.FileSystemImpl;
import fr.insee.kraftwerk.core.utils.files.FileUtilsInterface;
import fr.insee.kraftwerk.core.utils.log.KraftwerkExecutionContext;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.assertj.core.api.Assertions;
import stubs.ConfigStub;
import stubs.GenesisClientStub;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static cucumber.functional_tests.MainDefinitions.database;
import static cucumber.functional_tests.MainDefinitions.outDirectory;

public class GenesisDefinitions {

    ConfigStub configStub = new ConfigStub();
    GenesisClientStub genesisClientStub = new GenesisClientStub(configStub);

    private boolean isUsingEncryption;

    @Before
    public void clean() throws SQLException {
        configStub.setDefaultDirectory(TestConstants.FUNCTIONAL_TESTS_DIRECTORY);
        genesisClientStub.getMongoStub().clear();

        FileUtilsInterface fileUtilsInterface = new FileSystemImpl(TestConstants.FUNCTIONAL_TESTS_DIRECTORY);
        try {
            fileUtilsInterface.deleteDirectory(outDirectory);
        } catch (Exception ignored) {
            //Ignored exception
        }
        database = SqlUtils.openConnection();
        this.isUsingEncryption = false;
    }

    @Given("We have a collected variable {string} in a document with CampaignId {string}, InterrogationId {string} " +
            "with value {string}")
    public void add_collected_variable_document(String variableName, String campaignId, String interrogationId,
                                                String value) {

        SurveyUnitUpdateLatest surveyUnitUpdateLatest = getSurveyUnitUpdateLatest(campaignId, interrogationId);

        VariableModel variableModel = new VariableModel();
        variableModel.setVarId(variableName);
        variableModel.setIteration(1);
        variableModel.setScope(Constants.ROOT_GROUP_NAME);
        variableModel.setValue(value);

        surveyUnitUpdateLatest.getCollectedVariables().add(variableModel);
    }

    @Given("We have a collected variable {string} in a loop named {string} iteration {int} in a document with " +
            "CampaignId " +
            "{string}, " +
            "InterrogationId {string} with value {string}")
    public void add_loop_collected_variable_document(String variableName,
                                                     String loopName,
                                                     int iteration,
                                                     String campaignId, String interrogationId,
                                                     String value) {

        SurveyUnitUpdateLatest surveyUnitUpdateLatest = getSurveyUnitUpdateLatest(campaignId, interrogationId);

        VariableModel variableModel = new VariableModel();
        variableModel.setVarId(variableName);
        variableModel.setScope(loopName);
        variableModel.setIteration(iteration);
        variableModel.setIdParent(Constants.ROOT_GROUP_NAME);
        variableModel.setValue(value);

        surveyUnitUpdateLatest.getCollectedVariables().add(variableModel);
    }

    @Given("We have a external variable {string} in a document with CampaignId {string}, InterrogationId {string} " +
            "with value {string}")
    public void add_external_variable_document(String variableName,
                                               String campaignId,
                                               String interrogationId,
                                               String value) {
        SurveyUnitUpdateLatest surveyUnitUpdateLatest = getSurveyUnitUpdateLatest(campaignId, interrogationId);

        VariableModel variableModel = new VariableModel();
        variableModel.setVarId(variableName);
        variableModel.setIteration(1);
        variableModel.setScope(Constants.ROOT_GROUP_NAME);
        variableModel.setValue(value);

        surveyUnitUpdateLatest.getExternalVariables().add(variableModel);
    }

    @Given("We have a external variable {string} in a loop named {string} iteration {int} in a document with " +
            "CampaignId " +
            "{string}, " +
            "InterrogationId {string} with value {string}")
    public void add_loop_external_variable_document(String variableName,
                                                    String loopName,
                                                    int iteration,
                                                    String campaignId,
                                                    String interrogationId,
                                                    String value
    ) {

        SurveyUnitUpdateLatest surveyUnitUpdateLatest = getSurveyUnitUpdateLatest(campaignId, interrogationId);

        VariableModel variableModel = new VariableModel();
        variableModel.setVarId(variableName);
        variableModel.setScope(loopName);
        variableModel.setIteration(iteration);
        variableModel.setIdParent(Constants.ROOT_GROUP_NAME);
        variableModel.setValue(value);

        surveyUnitUpdateLatest.getExternalVariables().add(variableModel);
    }

    private SurveyUnitUpdateLatest getSurveyUnitUpdateLatest(String campaignId, String interrogationId) {
        List<SurveyUnitUpdateLatest> mongoFiltered = genesisClientStub.getMongoStub().stream().filter(
                surveyUnitUpdateLatest -> surveyUnitUpdateLatest.getCampaignId().equals(campaignId)
                        && surveyUnitUpdateLatest.getInterrogationId().equals(interrogationId)
        ).toList();

        SurveyUnitUpdateLatest surveyUnitUpdateLatest;
        if (mongoFiltered.isEmpty()) {
            surveyUnitUpdateLatest = new SurveyUnitUpdateLatest();

            surveyUnitUpdateLatest.setCampaignId(campaignId);
            surveyUnitUpdateLatest.setQuestionnaireId(campaignId);
            surveyUnitUpdateLatest.setInterrogationId(interrogationId);
            surveyUnitUpdateLatest.setMode(Mode.WEB);

            surveyUnitUpdateLatest.setCollectedVariables(new ArrayList<>());
            surveyUnitUpdateLatest.setExternalVariables(new ArrayList<>());
        } else {
            surveyUnitUpdateLatest = mongoFiltered.getFirst();
        }
        genesisClientStub.getMongoStub().add(surveyUnitUpdateLatest);
        return surveyUnitUpdateLatest;
    }

    @Given("We want to encrypt output data at the end of genesis process")
    public void activateEncryption(){
        this.isUsingEncryption = true;
    }

    @When("We use the Genesis service with campaignId {string}")
    public void launch_genesis(String campaignId) throws IOException, KraftwerkException {
        configStub.setDefaultDirectory(TestConstants.FUNCTIONAL_TESTS_DIRECTORY);

        KraftwerkExecutionContext kraftwerkExecutionContext = new KraftwerkExecutionContext(
                null,
                false,
                false,
                true,
                isUsingEncryption,
                419430400L
        );

        MainProcessingGenesis mainProcessingGenesis = new MainProcessingGenesis(
                configStub,
                genesisClientStub,
                new FileSystemImpl(configStub.getDefaultDirectory()),
                kraftwerkExecutionContext
        );
        mainProcessingGenesis.runMain(campaignId,1000);
        System.out.println();
    }

    @Then("In root csv output file we should have {string} for survey unit {string}, column {string}")
    public void check_root_csv_output(String value, String interrogationId, String variableName) throws IOException,
            CsvValidationException {
        Path executionOutDirectory =
                outDirectory.resolve(Objects.requireNonNull(new File(outDirectory.toString()).listFiles(File::isDirectory))[0].getName());
        CSVReader csvReader = getCSVReader(
                executionOutDirectory.resolve(outDirectory.getFileName() + "_" + Constants.ROOT_GROUP_NAME + ".csv"));
        // get header
        String[] header = csvReader.readNext();
        //Assert fields existence
        int interrogationIdIndex = Arrays.asList(header).indexOf(Constants.ROOT_IDENTIFIER_NAME);
        int fieldIndex = Arrays.asList(header).indexOf(variableName);

        while (
                csvReader.peek() != null
                        // Cursed condition to check if next line has specified interrogationId and Loop iteration
                        // (ex: "01,Loop-02" will be true if interrogationId = "01" and iterationIndex = 2)
                        && !(
                        csvReader.peek()[interrogationIdIndex].equals(interrogationId))
        ) {
            csvReader.readNext();
        }

        Assertions.assertThat(csvReader.peek()).isNotNull().hasSizeGreaterThan(fieldIndex);
        String fieldContent = csvReader.peek()[fieldIndex];

        //Check content
        Assertions.assertThat(fieldContent).isEqualTo(value);

        // Close reader
        csvReader.close();
    }

    //CSV Utilities
    private CSVReader getCSVReader(Path filePath) throws IOException {
        CSVParser parser = new CSVParserBuilder()
                .withSeparator(Constants.CSV_OUTPUTS_SEPARATOR)
                //.withQuoteChar(Constants.CSV_OUTPUTS_QUOTE_CHAR)
                //.withEscapeChar(CSVWriter.DEFAULT_ESCAPE_CHARACTER)
                .build();
        return new CSVReaderBuilder(new FileReader(filePath.toFile(), StandardCharsets.UTF_8))
                //.withSkipLines(1) // (uncomment to ignore header)
                .withCSVParser(parser)
                .build();
    }

    @Then("In root parquet output file we should have {string} for survey unit {string}, column {string}")
    public void check_root_parquet(String value, String interrogationId,
                                   String variableName) throws SQLException {
        Path executionOutDirectory =
                outDirectory.resolve(Objects.requireNonNull(new File(outDirectory.toString()).listFiles(File::isDirectory))[0].getName());
        Path filePath = executionOutDirectory.resolve(outDirectory.getFileName() + "_" + Constants.ROOT_GROUP_NAME +
                ".parquet");
        try (Statement statement = database.createStatement()) {
            SqlUtils.readParquetFile(statement, filePath);
            //Select concerned line from database
            ResultSet resultSet = statement.executeQuery(
                    ("SELECT %s " +
                            "FROM '%s' " +
                            "WHERE %s = '%s'").formatted(
                            variableName,
                            outDirectory.getFileName() + "_" + Constants.ROOT_GROUP_NAME,
                            Constants.ROOT_IDENTIFIER_NAME,
                            interrogationId
                    )
            );
            Assertions.assertThat(resultSet.next()).isTrue();
            Assertions.assertThat(resultSet.getString(variableName)).isNotNull().isEqualTo(value);
        }
    }
}
