package cucumber.functional_tests;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import com.opencsv.exceptions.CsvValidationException;
import fr.insee.kraftwerk.core.Constants;
import fr.insee.kraftwerk.core.extradata.reportingdata.ContactAttemptType;
import fr.insee.kraftwerk.core.extradata.reportingdata.StateType;
import fr.insee.kraftwerk.core.utils.CsvUtils;
import io.cucumber.java.en.Then;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;

import static cucumber.TestConstants.FUNCTIONAL_TESTS_INPUT_DIRECTORY;
import static cucumber.TestConstants.FUNCTIONAL_TESTS_OUTPUT_DIRECTORY;
import static cucumber.TestConstants.FUNCTIONAL_TESTS_TEMP_DIRECTORY;
import static fr.insee.kraftwerk.core.Constants.OUTCOME_ATTEMPT_SUFFIX_NAME;
import static fr.insee.kraftwerk.core.Constants.STATE_SUFFIX_NAME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;


// These definitions are used in do_we_export_contact_attempts feature
public class ReportingDataDefinitions {

    Path inDirectory = Paths.get(FUNCTIONAL_TESTS_INPUT_DIRECTORY);
    static Path outDirectory = Paths.get(FUNCTIONAL_TESTS_OUTPUT_DIRECTORY);
    Path tempDirectory = Paths.get(FUNCTIONAL_TESTS_TEMP_DIRECTORY);
    private static final String[] reportingDataFields = {
            Constants.INTERVIEWER_ID_NAME,
            Constants.ORGANIZATION_UNIT_ID_NAME,
            Constants.ADRESS_RGES_NAME,
            Constants.ADRESS_NUMFA_NAME,
            Constants.ADRESS_SSECH_NAME,
            Constants.ADRESS_LE_NAME,
            Constants.ADRESS_EC_NAME,
            Constants.ADRESS_BS_NAME,
            Constants.ADRESS_NOI_NAME,
            Constants.ADRESS_ID_STAT_INSEE,
            Constants.STATE_SUFFIX_NAME + "_1",
            Constants.STATE_SUFFIX_NAME + "_1_DATE",
            Constants.LAST_STATE_NAME,
            Constants.OUTCOME_NAME,
            Constants.OUTCOME_DATE,
            Constants.NUMBER_ATTEMPTS_NAME,
            Constants.LAST_ATTEMPT_DATE,
            Constants.OUTCOME_ATTEMPT_SUFFIX_NAME + "_1",
            Constants.OUTCOME_ATTEMPT_SUFFIX_NAME + "_1_DATE"
    };

    // Existence and structure tests
    @Then("We should have a file named {string} in directory {string} with {int} reporting data fields")
    public void check_contact_attempt_file(String fileName, String directory, int expectedFieldCount) throws IOException, CsvException {
        File outputReportingDataFile = new File(outDirectory + "/" + directory + "/" + fileName);

        // File existence assertion
        assertThat(outputReportingDataFile).exists().isFile().canRead();

        CSVReader csvReader = CsvUtils.getReader(
                outputReportingDataFile.toPath()
        );


        // Get header
        String[] header = csvReader.readNext();
        csvReader.close();

        // Header assertion
        assertThat(header).hasSize(expectedFieldCount + 1);
        assertThat(header).containsAll(Arrays.asList(reportingDataFields));
    }

    @Then("We shouldn't have any reporting data file in directory {string}")
    public void check_lack_of_contact_attempt_file(String directory) {
        File outputReportingDataFile = new File(outDirectory + "/" + directory + "/" + directory + "_REPORTINGDATA.csv");
        assertThat(outputReportingDataFile).doesNotExist();
    }


    // Volumetry test
    @Then("We should have {int} lines different than header in a file named {string} in directory {string}")
    public void check_reporting_data_count(int expectedCount, String fileName, String directory) throws IOException, CsvException {
        CSVReader csvReader = CsvUtils.getReader(
                Path.of(outDirectory + "/" + directory + "/" + fileName)
        );

        // Get file content
        List<String[]> content = csvReader.readAll();
        csvReader.close();

        int actualCount = content.size() - 1; // -1 to exclude header

        // Count assertion
        assertThat(actualCount).isEqualTo(expectedCount);
    }


    // Content tests
    @Then("For SurveyUnit {string} we should have {int} contact attempts with status {string} in a file named {string} in directory {string}")
    public void check_contact_attempt_content(String surveyUnitId, int expectedSpecificStatusCount, String expectedStatus, String fileName, String directory) throws IOException, CsvException {
        CSVReader csvReader = CsvUtils.getReader(
                Path.of(outDirectory + "/" + directory + "/" + fileName)
        );

        // Get file content
        List<String[]> content = csvReader.readAll();
        csvReader.close();

        // Get header
        String[] header = content.get(0);

        // Fetch concerned survey unit line from file
        String[] concernedLine = null;
        for (String[] line : content) {
            if (line[0].equals(surveyUnitId)) {
                concernedLine = line;
                break;
            }
        }

        // Survey unit existence assertion
        assertThat(concernedLine).isNotNull();

        // Count contact attempts and check date formats
        int actualSpecificStatusCount = 0;
        int i = 0;

        for (String element : concernedLine) {
            String fieldName = header[i];

            // Increment if valid
            if (element.equals(ContactAttemptType.getAttemptType(expectedStatus)) // the field content matches with expected
                    && fieldName.startsWith(OUTCOME_ATTEMPT_SUFFIX_NAME) // is a contact attempt field
                    && !fieldName.contains("DATE")) { // not the attempt date field
                actualSpecificStatusCount++;
            }

            i++;
        }

        // Contact attempts count assertion
        assertThat(actualSpecificStatusCount).isEqualTo(expectedSpecificStatusCount);
    }

    @Then("For SurveyUnit {string} we should have {int} contact states with status {string} in a file named {string} in directory {string}")
    public void check_contact_state_content(String surveyUnitId, int expectedSpecificStatusCount, String expectedStatus, String fileName, String directory) throws IOException, CsvException {
        CSVReader csvReader = CsvUtils.getReader(
                Path.of(outDirectory + "/" + directory + "/" + fileName)
        );

        // Get file content
        List<String[]> content = csvReader.readAll();
        csvReader.close();

        // Get header
        String[] header = content.get(0);

        // Fetch concerned survey unit line from file
        String[] concernedLine = null;
        for (String[] line : content) {
            if (line[0].equals(surveyUnitId)) {
                concernedLine = line;
                break;
            }
        }

        // Survey unit existence assertion
        assertThat(concernedLine).isNotNull();

        // Count contact attempts and check date formats
        int actualSpecificStatusCount = 0;
        int i = 0;

        for (String element : concernedLine) {
            String fieldName = header[i];

            // Increment if valid
            if (element.equals(StateType.getStateType(expectedStatus)) // the field content matches with expected
                    && fieldName.startsWith(STATE_SUFFIX_NAME) // is a contact attempt field
                    && !fieldName.contains("DATE")) { // not the attempt date field
                actualSpecificStatusCount++;
            }

            i++;
        }

        // Contact attempts count assertion
        assertThat(actualSpecificStatusCount).isEqualTo(expectedSpecificStatusCount);
    }

    @Then("In file named {string} in directory {string} we should have the following date format : {string}")
    public void check_contact_attempt_date_format(String fileName, String directory, String expectedDateFormat) throws IOException, CsvException {
        CSVReader csvReader = CsvUtils.getReader(
                Path.of(outDirectory + "/" + directory + "/" + fileName)
        );

        // Get file content
        List<String[]> content = csvReader.readAll();
        csvReader.close();

        // Get and exclude header
        assertThat(content).isNotEmpty();
        String[] header = content.get(0);
        content.remove(0);

        //Date check
        SimpleDateFormat sdf = new SimpleDateFormat(expectedDateFormat);
        for (String[] line : content) {
            int i = 0;
            // For each field
            for (String element : line) {
                String fieldName = header[i];

                // Date format assertion if filled
                if (fieldName.contains("_DATE") && !element.isEmpty()) {
                    try {
                        sdf.parse(element);
                    } catch (ParseException e) {
                        fail("Wrong date format in file");
                    }
                }
                i++;
            }
        }
    }

    @Then("We shouldn't have any reporting data in {string} in directory {string}")
    public void check_reporting_data_in_root_file(String fileName, String directory) throws IOException, CsvValidationException {
        CSVReader csvReader = CsvUtils.getReader(
                Path.of(outDirectory + "/" + directory + "/" + fileName)
        );

        // Get header
        String[] header = csvReader.readNext();
        csvReader.close();

        // assert lack of unique reporting data field
        assertThat(header).doesNotContainAnyElementsOf(Arrays.asList(reportingDataFields));
    }

    @Then("For SurveyUnit {string} in a file named {string} in directory {string} we should have {string} in the OUTCOME_SPOTTING field")
    public void check_outcome_spotting_result(String surveyUnitId, String fileName, String directory, String expectedOutcomeSpotting) throws IOException, CsvException {
        CSVReader csvReader = CsvUtils.getReader(
                Path.of(outDirectory + "/" + directory + "/" + fileName)
        );

        // Get file content
        List<String[]> content = csvReader.readAll();
        csvReader.close();

        // Get header
        String[] header = content.get(0);

        // OUTCOME_SPOTTING field existence assertion
        assertThat(header).contains("OUTCOME_SPOTTING");

        // Fetch concerned survey unit line from file
        String[] concernedLine = null;
        for (String[] line : content) {
            if (line[0].equals(surveyUnitId)) {
                concernedLine = line;
                break;
            }
        }

        // Survey unit existence assertion
        assertThat(concernedLine).isNotNull();

        // Check OUTCOME_SPOTTING content
        int i = 0;
        String outcomeSpottingContent = null;
        for (String element : concernedLine) {
            if (header[i].equals("OUTCOME_SPOTTING")) {
                outcomeSpottingContent = element;
                break;
            }
            i++;
        }
        assertThat(outcomeSpottingContent).isEqualTo(expectedOutcomeSpotting);
    }
}