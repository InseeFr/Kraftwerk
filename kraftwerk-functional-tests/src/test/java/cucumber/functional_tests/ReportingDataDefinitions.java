package cucumber.functional_tests;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import com.opencsv.exceptions.CsvValidationException;
import fr.insee.kraftwerk.api.process.MainProcessing;
import fr.insee.kraftwerk.core.exceptions.KraftwerkException;
import fr.insee.kraftwerk.core.extradata.reportingdata.ContactAttemptType;
import fr.insee.kraftwerk.core.sequence.ControlInputSequence;
import fr.insee.kraftwerk.core.utils.CsvUtils;
import fr.insee.kraftwerk.core.utils.FileUtils;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import static cucumber.TestConstants.FUNCTIONAL_TESTS_INPUT_DIRECTORY;
import static cucumber.TestConstants.FUNCTIONAL_TESTS_OUTPUT_DIRECTORY;
import static cucumber.TestConstants.FUNCTIONAL_TESTS_TEMP_DIRECTORY;
import static fr.insee.kraftwerk.core.Constants.OUTCOME_ATTEMPT_SUFFIX_NAME;
import static fr.insee.kraftwerk.core.Constants.ROOT_IDENTIFIER_NAME;
import static org.apache.tomcat.util.http.fileupload.FileUtils.deleteDirectory;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;


// These definitions are used in do_we_export_contact_attempts feature
public class ReportingDataDefinitions {

    Path inDirectory = Paths.get(FUNCTIONAL_TESTS_INPUT_DIRECTORY);
    static Path outDirectory = Paths.get(FUNCTIONAL_TESTS_OUTPUT_DIRECTORY);
    Path tempDirectory = Paths.get(FUNCTIONAL_TESTS_TEMP_DIRECTORY);
    String campaignName = "";

    // Existence and structure test
    @Then("We should have a file named {string} in directory {string} with {int} contact attempts fields")
    public void check_contact_attempt_file(String fileName, String directory, int expectedFieldCount) throws IOException, CsvException {
        File outputContactAttemptsFile = new File(outDirectory + "/" + directory + "/" + fileName);

        // File existence assertion
        assertThat(outputContactAttemptsFile).exists().isFile().canRead();

        CSVReader csvReader = CsvUtils.getReader(
                outputContactAttemptsFile.toPath()
        );


        // Get header
        String[] header = csvReader.readNext();
        csvReader.close();

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

    // Volumetry test
    @Then("We should have {int} lines different than header in a file named {string} in directory {string}")
    public void check_contact_attempt_count(int expectedCount, String fileName, String directory) throws IOException, CsvException {
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

        // Get header
        String[] header = content.get(0);

        // Fetch concerned survey unit line from file
        String[] concernedLine = null;
        for(String[] line : content){
            if(line[0].equals(surveyUnitId)){
                concernedLine = line;
                break;
            }
        }

        // Survey unit existence assertion
        assertThat(concernedLine).isNotNull();

        // Count contact attempts and check date formats
        int actualSpecificStatusCount = 0;
        int i = 0;

        for(String element : concernedLine){
            String fieldName = header[i];

            // Increment if valid
            if (element.equals(ContactAttemptType.getAttemptType(expectedStatus)) // the field content matches with expected
                    && fieldName.startsWith(OUTCOME_ATTEMPT_SUFFIX_NAME) // is a contact attempt field
                    && !fieldName.contains("DATE")){ // not the attempt date field
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
        for(String[] line : content){
            int i = 0;
            // For each field
            for(String element : line){
                String fieldName = header[i];

                // Date format assertion if filled
                if(fieldName.contains("_DATE") && !element.isEmpty()){
                    try {
                        sdf.parse(element);
                    }
                    catch (ParseException e){
                        fail("Wrong date format in file");
                    }
                }
                i++;
            }
        }
    }

    @Then("We shouldn't have any contact attempt in {string} in directory {string}")
    public void check_contact_attempts_in_root_file(String fileName, String directory) throws IOException, CsvValidationException {
        CSVReader csvReader = CsvUtils.getReader(
                Path.of(outDirectory + "/" + directory + "/" + fileName)
        );

        // Get header
        String[] header = csvReader.readNext();
        csvReader.close();

        // assert lack of attempt field
        assertThat(header).doesNotContain(OUTCOME_ATTEMPT_SUFFIX_NAME + "_1");
        assertThat(header).doesNotContain(OUTCOME_ATTEMPT_SUFFIX_NAME + "1_DATE");

    }
}
