package cucumber.functional_tests;

import io.cucumber.java.en.Then;
import org.assertj.core.api.Assertions;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.stream.Stream;

import static cucumber.TestConstants.FUNCTIONAL_TESTS_OUTPUT_DIRECTORY;

public class LogFileDefinitions {
    static Path outDirectory = Paths.get(FUNCTIONAL_TESTS_OUTPUT_DIRECTORY);
    @Then("We should have a log file in directory {string}")
    public void logFileExistenceCheck(String directory) throws IOException {
        Path executionOutDirectory = outDirectory.resolve(directory);
        executionOutDirectory = executionOutDirectory.resolve(Objects.requireNonNull(new File(executionOutDirectory.toString()).listFiles(File::isDirectory))[0].getName());

        try(Stream<Path> folderStream = Files.list(executionOutDirectory)){
            Assertions.assertThat(folderStream.filter(path ->
                    path.getFileName().toString().startsWith(directory + "_LOG_")
            )).isNotEmpty();
        }

    }

    @Then("We should have log files for each execution in directory {string}")
    public void logFilesExistenceCheck(String directory) throws IOException {
        Path surveyOutDirectory = outDirectory.resolve(directory);
        File surveyOutDirectoryFile = surveyOutDirectory.toFile();

        for (File executionOutDirectoryFile : Objects.requireNonNull(surveyOutDirectoryFile.listFiles(File::isDirectory))) {
            Path executionOutDirectory = executionOutDirectoryFile.toPath();

            try (Stream<Path> folderStream = Files.list(executionOutDirectory)) {
                Assertions.assertThat(folderStream.filter(path ->
                        path.getFileName().toString().startsWith(directory + "_LOG_")
                )).isNotEmpty();
            }
        }
    }
}
