package cucumber.functional_tests;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvValidationException;
import cucumber.TestConstants;
import fr.insee.kraftwerk.api.process.MainProcessingGenesisLegacy;
import fr.insee.kraftwerk.api.process.MainProcessingGenesisNew;
import fr.insee.kraftwerk.core.Constants;
import fr.insee.kraftwerk.core.data.model.Mode;
import fr.insee.kraftwerk.core.data.model.SurveyUnitUpdateLatest;
import fr.insee.kraftwerk.core.data.model.VariableModel;
import fr.insee.kraftwerk.core.exceptions.KraftwerkException;
import fr.insee.kraftwerk.core.utils.KraftwerkExecutionContext;
import fr.insee.kraftwerk.core.utils.SqlUtils;
import fr.insee.kraftwerk.core.utils.files.FileSystemImpl;
import fr.insee.kraftwerk.core.utils.files.FileUtilsInterface;
import fr.insee.libjavachiffrement.symmetric.SymmetricEncryptionEndpoint;
import fr.insee.libjavachiffrement.symmetric.SymmetricEncryptionException;
import fr.insee.libjavachiffrement.vault.VaultCaller;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.assertj.core.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import stubs.ConfigStub;
import stubs.GenesisClientStub;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static cucumber.functional_tests.MainDefinitions.database;
import static cucumber.functional_tests.MainDefinitions.outDirectory;

@SpringBootTest
public class GenesisDefinitions {

    ConfigStub configStub = new ConfigStub();
    GenesisClientStub genesisClientStub = new GenesisClientStub(configStub);

    private boolean isUsingEncryption;
    KraftwerkExecutionContext kraftwerkExecutionContext;

    @Autowired
    private ApplicationContext context;

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

    @Given("We have a collected variable {string} in a document with QuestionnaireModelId {string}, InterrogationId {string} " +
            "with value {string}")
    public void add_collected_variable_document_by_questionnaire(String variableName, String questionnaireModelId, String interrogationId,
                                                String value) {

        SurveyUnitUpdateLatest surveyUnitUpdateLatest = getSurveyUnitUpdateLatestByQuestionnaire(questionnaireModelId, interrogationId);

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

    @Given("We have a collected variable {string} in a loop named {string} iteration {int} in a document with " +
            "QuestionnaireModelId " +
            "{string}, " +
            "InterrogationId {string} with value {string}")
    public void add_loop_collected_variable_document_by_questionnaire( String variableName,
                                                                       String loopName,
                                                                       int iteration,
                                                                       String questionnaireModelId,
                                                                       String interrogationId,
                                                                       String value) {

        SurveyUnitUpdateLatest surveyUnitUpdateLatest = getSurveyUnitUpdateLatestByQuestionnaire(questionnaireModelId, interrogationId);

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

    @Given("We have a external variable {string} in a document with QuestionnaireModelId {string}, InterrogationId {string} " +
            "with value {string}")
    public void add_external_variable_document_by_questionnaire(String variableName,
                                                                String questionnaireModelId,
                                                                String interrogationId,
                                                                String value) {
        SurveyUnitUpdateLatest surveyUnitUpdateLatest = getSurveyUnitUpdateLatestByQuestionnaire(questionnaireModelId, interrogationId);

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

    @Given("We have a external variable {string} in a loop named {string} iteration {int} in a document with " +
            "QuestionnaireModelId " +
            "{string}, " +
            "InterrogationId {string} with value {string}")
    public void add_loop_external_variable_document_by_questionnaire(String variableName,
                                                    String loopName,
                                                    int iteration,
                                                    String questionnaireModelId,
                                                    String interrogationId,
                                                    String value
    ) {

        SurveyUnitUpdateLatest surveyUnitUpdateLatest = getSurveyUnitUpdateLatestByQuestionnaire(questionnaireModelId, interrogationId);

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
            surveyUnitUpdateLatest.setCollectionInstrumentId(campaignId);
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

    private SurveyUnitUpdateLatest getSurveyUnitUpdateLatestByQuestionnaire(String questionnaireModelId, String interrogationId) {
        List<SurveyUnitUpdateLatest> mongoFiltered = genesisClientStub.getMongoStub().stream().filter(
                surveyUnitUpdateLatest -> surveyUnitUpdateLatest.getCollectionInstrumentId().equals(questionnaireModelId)
                        && surveyUnitUpdateLatest.getInterrogationId().equals(interrogationId)
        ).toList();

        SurveyUnitUpdateLatest surveyUnitUpdateLatest;
        if (mongoFiltered.isEmpty()) {
            surveyUnitUpdateLatest = new SurveyUnitUpdateLatest();

            surveyUnitUpdateLatest.setCampaignId(questionnaireModelId);
            surveyUnitUpdateLatest.setCollectionInstrumentId(questionnaireModelId);
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

    // To delete when we remove historical genesis endpoints
    @When("We use the Genesis service with campaignId {string}")
    public void launch_genesis(String campaignId) throws IOException, KraftwerkException {
        configStub.setDefaultDirectory(TestConstants.FUNCTIONAL_TESTS_DIRECTORY);

        kraftwerkExecutionContext =
                TestConstants.getKraftwerkExecutionContext(null, isUsingEncryption);


        MainProcessingGenesisLegacy mainProcessingGenesisLegacy = new MainProcessingGenesisLegacy(
                configStub,
                genesisClientStub,
                new FileSystemImpl(configStub.getDefaultDirectory()),
                kraftwerkExecutionContext
        );
        mainProcessingGenesisLegacy.runMain(campaignId,1000);
        System.out.println();
    }

    @When("We use the Genesis service with questionnaireModelId {string}")
    public void launch_genesis_by_questionnaire(String questionnaireModelId) throws IOException, KraftwerkException {
        configStub.setDefaultDirectory(TestConstants.FUNCTIONAL_TESTS_DIRECTORY);

        kraftwerkExecutionContext =
                TestConstants.getKraftwerkExecutionContext(null, isUsingEncryption);


        MainProcessingGenesisNew mainProcessingGenesisNew = new MainProcessingGenesisNew(
                configStub,
                genesisClientStub,
                new FileSystemImpl(configStub.getDefaultDirectory()),
                kraftwerkExecutionContext
        );
        mainProcessingGenesisNew.runMain(questionnaireModelId,1000,null);
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
    @Then("We should be able to decrypt the file \\(Genesis)")
    public void check_genesis_file_decryption() throws IOException, SymmetricEncryptionException, SQLException {

        SymmetricEncryptionEndpoint symmetricEncryptionEndpoint =
                TestConstants.getSymmetricEncryptionEndpointForTest(context.getBean(VaultCaller.class));

        Path tmpParquet = decryptZipAssertCsvAndExtractParquet(symmetricEncryptionEndpoint);

        try (Statement statement = database.createStatement()) {
            String parquetPath = tmpParquet.toAbsolutePath().toString().replace("\\", "/");

            ResultSet resultSet = statement.executeQuery(
                    ("SELECT %s FROM read_parquet('%s') LIMIT 1")
                            .formatted(Constants.ROOT_IDENTIFIER_NAME, parquetPath)
            );

            Assertions.assertThat(resultSet.next()).isTrue();
        }
    }

    private Path decryptZipAssertCsvAndExtractParquet(SymmetricEncryptionEndpoint endpoint)
            throws IOException, SymmetricEncryptionException {

        Path zipFolder = outDirectory;
        Assertions.assertThat(zipFolder).isNotNull();
        Assertions.assertThat(zipFolder.toFile()).exists().isDirectory();

        Path encryptedZipPath;
        try (var s = Files.list(zipFolder)) {
            encryptedZipPath = s.filter(p -> p.getFileName().toString().endsWith(".zip.enc"))
                    .max(Comparator.comparingLong(p -> p.toFile().lastModified()))
                    .orElseThrow(() -> new AssertionError(
                            "No .zip.enc found in " + zipFolder +
                                    ". Files are: " + listFilesForDebug(zipFolder)
                    ));
        }

        Assertions.assertThat(encryptedZipPath).exists();
        Assertions.assertThat(encryptedZipPath.toFile()).content().isNotEmpty();

        byte[] decryptedZipBytes = endpoint.decrypt(Files.readAllBytes(encryptedZipPath));
        Assertions.assertThat(decryptedZipBytes).isNotNull().isNotEmpty();

        boolean foundCsv = false;
        boolean foundParquet = false;

        String expectedCsvSuffix = Constants.ROOT_GROUP_NAME + ".csv";
        String expectedParquetSuffix = Constants.ROOT_GROUP_NAME + ".parquet";

        Path tmpParquet = Files.createTempFile("genesis-decrypted-", ".parquet");
        tmpParquet.toFile().deleteOnExit();

        try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(decryptedZipBytes))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                String entryName = entry.getName();

                if (entryName.endsWith(expectedCsvSuffix)) {
                    foundCsv = true;
                    String csvText = new String(zis.readAllBytes(), StandardCharsets.UTF_8);
                    Assertions.assertThat(csvText).contains(Constants.ROOT_IDENTIFIER_NAME);
                } else if (entryName.endsWith(expectedParquetSuffix)) {
                    foundParquet = true;
                    Files.write(tmpParquet, zis.readAllBytes(),
                            StandardOpenOption.CREATE,
                            StandardOpenOption.TRUNCATE_EXISTING);
                }
            }
        }

        Assertions.assertThat(foundCsv)
                .as("CSV entry should exist inside decrypted zip")
                .isTrue();

        Assertions.assertThat(foundParquet)
                .as("Parquet entry should exist inside decrypted zip")
                .isTrue();

        return tmpParquet;
    }

    private String listFilesForDebug(Path dir) {
        try (var s = Files.list(dir)) {
            return s
                    .map(p -> p.getFileName().toString())
                    .sorted()
                    .toList()
                    .toString();
        } catch (IOException e) {
            return "<error listing files: " + e.getMessage() + ">";
        }
    }

}
