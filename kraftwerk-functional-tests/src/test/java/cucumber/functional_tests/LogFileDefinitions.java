package cucumber.functional_tests;

import io.cucumber.java.en.Then;
import org.assertj.core.api.Assertions;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import static cucumber.TestConstants.FUNCTIONAL_TESTS_OUTPUT_DIRECTORY;

public class LogFileDefinitions {
    static Path outDirectory = Paths.get(FUNCTIONAL_TESTS_OUTPUT_DIRECTORY);
    @Then("We should have a log file named in directory {string}")
    public void logFileExistenceCheck(String directory) throws IOException {
        try(Stream<Path> folderStream = Files.list(outDirectory.resolve(directory))){
            Assertions.assertThat(folderStream.filter(path ->
                    path.getFileName().toString().startsWith(directory + "_LOG_")
            )).isNotEmpty();
        }

    }
}
