package cucumber.functional_tests;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import com.opencsv.exceptions.CsvValidationException;
import fr.insee.kraftwerk.core.extradata.reportingdata.ContactAttemptType;
import fr.insee.kraftwerk.core.utils.CsvUtils;
import io.cucumber.java.en.Then;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

import static cucumber.TestConstants.FUNCTIONAL_TESTS_OUTPUT_DIRECTORY;
import static fr.insee.kraftwerk.core.Constants.OUTCOME_ATTEMPT_SUFFIX_NAME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;


// These definitions are used in do_we_export_contact_attempts feature
public class ExportContactAttemptDefinitions {
    Path outDirectory = Paths.get(FUNCTIONAL_TESTS_OUTPUT_DIRECTORY);

    // Volumetry test
    @Then("We should have {int} lines different than header in a file named {string} in directory {string}")
    public void check_contact_attempt_count(int expectedCount, String fileName, String directory) throws IOException, CsvException {
        CSVReader csvReader = CsvUtils.getReader(
                Path.of(outDirectory + "/" + directory + "/" + fileName)
        );

        // Get file content
        List<String[]> content = csvReader.readAll();

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

        // attempt lack of attempt field
        assertThat(header).doesNotContain(OUTCOME_ATTEMPT_SUFFIX_NAME + "_1");
        assertThat(header).doesNotContain(OUTCOME_ATTEMPT_SUFFIX_NAME + "1_DATE");

    }
}
