package cucumber.functional_tests;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import cucumber.TestConstants;
import fr.insee.kraftwerk.core.Constants;
import fr.insee.kraftwerk.core.utils.CsvUtils;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;




public class StandardVtlDefinitions {
    static Path outDirectory = Paths.get(TestConstants.FUNCTIONAL_TESTS_OUTPUT_DIRECTORY);
    @Given("We have test standard vtl scripts")
    public void generate_test_vtl_scripts() throws IOException {
        // Unimode
        // TEL
        String vtlScript = "TEL := TEL[calc REPORTINGDATA.OUTCOME_SPOTTING := \"TEST\"];\n";
        Path path = Files.createDirectories(Path.of(Constants.VTL_FOLDER_PATH).resolve("unimode")).resolve("TEL.vtl");

        if(!Files.exists(path)) Files.createFile(path);
        Files.write(path,vtlScript.getBytes());

        // FAF
        vtlScript = "FAF := FAF[calc TESTVTLFAF := \"TESTFAF\"];";
        path = Files.createDirectories(Path.of(Constants.VTL_FOLDER_PATH).resolve("unimode")).resolve("FAF.vtl");

        if(!Files.exists(path)) Files.createFile(path);
        Files.write(path,vtlScript.getBytes());


        // Multimode
        vtlScript = "MULTIMODE := MULTIMODE[calc TESTVTLMULTIMODE := \"TESTMULTIMODE\"];";
        path = Files.createDirectories(Path.of(Constants.VTL_FOLDER_PATH).resolve("multimode")).resolve("multimode.vtl");

        if(!Files.exists(path)) Files.createFile(path);
        Files.write(path,vtlScript.getBytes());


        // Information levels
        vtlScript = "MULTIMODE := MULTIMODE[calc TESTVTLINFOLEVEL := \"TESTINFOLEVEL\"];";
        path = Files.createDirectories(Path.of(Constants.VTL_FOLDER_PATH).resolve("information_levels")).resolve("information_levels.vtl");

        if(!Files.exists(path)) Files.createFile(path);
        Files.write(path,vtlScript.getBytes());
    }


    @Then("We should have a field named {string} in the root output file of {string} filled with {string}")
    public void check_field_creation(String expectedField, String surveyName, String expectedContent) throws IOException, CsvException {
        CSVReader csvReader = CsvUtils.getReader(
                outDirectory.resolve(surveyName).resolve(surveyName + "_" + Constants.ROOT_GROUP_NAME + ".csv")
        );

        // Get file content
        List<String[]> content = csvReader.readAll();
        csvReader.close();
        assertThat(content).isNotEmpty();

        String[] header = content.get(0);
        assertThat(header).contains(expectedField);

        int fieldIndex = 0;
        int expectedFieldIndex = 0;
        for(String fieldName: header){
            if(fieldName.equals(expectedField))
                expectedFieldIndex = fieldIndex;
            fieldIndex++;
        }

        //Exclude header
        content.remove(0);

        for (String[] row : content){
            //Row syntax assert
            assertThat(row).hasSize(fieldIndex);
            //Row content assert
            assertThat(row[expectedFieldIndex]).isEqualTo(expectedContent);
        }
    }

    @Then("we shouldn't have any _keep file in {string} output directory")
    public void check_lack_keep_file(String surveyName) throws IOException {
        try(Stream<Path> stream = Files.list(outDirectory.resolve(surveyName))){
            Set<String> fileNames = stream
                    .filter(file -> !Files.isDirectory(file))
                    .map(Path::getFileName)
                    .map(Path::toString)
                    .collect(Collectors.toSet());

            for(String fileName : fileNames){
                assertThat(fileName).doesNotEndWith("_keep.csv");
            }
        }
    }
}
