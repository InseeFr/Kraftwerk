package fr.insee.kraftwerk.core.sequence;

import fr.insee.kraftwerk.core.Constants;
import fr.insee.kraftwerk.core.exceptions.KraftwerkException;
import fr.insee.kraftwerk.core.utils.files.FileUtilsInterface;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ControlInputSequenceTest {

    private static final String DEFAULT_DIRECTORY = "/some/default/dir";

    @Mock
    private FileUtilsInterface fileUtilsInterface;

    private ControlInputSequence controlInputSequence;

    @BeforeEach
    void setUp() {
        controlInputSequence = new ControlInputSequence(DEFAULT_DIRECTORY, fileUtilsInterface);
    }

    /**
     * The config file exists in the directory given in param.
     */
    @Test
    void getInDirectory_shouldReturnFirstPath_whenUserInputFileExistsInGivenDirectory() throws KraftwerkException {
        // given
        String inDirectoryParam = "myConfigDir";
        Path firstCandidate = Path.of(inDirectoryParam);
        Path userInputFirst = firstCandidate.resolve(Constants.USER_INPUT_FILE);

        when(fileUtilsInterface.isFileExists(userInputFirst.toString())).thenReturn(true);

        // when
        Path result = controlInputSequence.getInDirectory(inDirectoryParam);

        // then
        assertEquals(firstCandidate, result, "Path returned should be given in params");
        verify(fileUtilsInterface, times(1)).isFileExists(userInputFirst.toString());
        verifyNoMoreInteractions(fileUtilsInterface);//second path not called
    }

    /**
     * The config file DOES NOT exist in the directory given in param.
     * but is in defaultDirectory/in/inDirectoryParam.
     */
    @Test
    void getInDirectory_shouldFallbackToDefaultInDirectory_whenFileExistsThere() throws KraftwerkException {
        // given
        String inDirectoryParam = "myConfigDir";
        Path firstCandidate = Path.of(inDirectoryParam);
        Path secondCandidate = Paths.get(DEFAULT_DIRECTORY, "in", inDirectoryParam);

        Path userInputFirst = firstCandidate.resolve(Constants.USER_INPUT_FILE);
        Path userInputSecond = secondCandidate.resolve(Constants.USER_INPUT_FILE);

        when(fileUtilsInterface.isFileExists(userInputFirst.toString())).thenReturn(false);
        when(fileUtilsInterface.isFileExists(userInputSecond.toString())).thenReturn(true);

        // when
        Path result = controlInputSequence.getInDirectory(inDirectoryParam);

        // then
        assertEquals(secondCandidate, result, "Path returned should be based on defaultDirectory/in/...");
        verify(fileUtilsInterface).isFileExists(userInputFirst.toString());
        verify(fileUtilsInterface).isFileExists(userInputSecond.toString());
        verifyNoMoreInteractions(fileUtilsInterface);
    }

    /**
     * The config file DOES NOT exist.
     * should throw exception
     */
    @Test
    void getInDirectory_shouldThrowException_whenUserInputFileDoesNotExistInAnyLocation() {
        // given
        String inDirectoryParam = "myConfigDir";
        Path firstCandidate = Path.of(inDirectoryParam);
        Path secondCandidate = Paths.get(DEFAULT_DIRECTORY, "in", inDirectoryParam);

        Path userInputFirst = firstCandidate.resolve(Constants.USER_INPUT_FILE);
        Path userInputSecond = secondCandidate.resolve(Constants.USER_INPUT_FILE);

        when(fileUtilsInterface.isFileExists(userInputFirst.toString())).thenReturn(false);
        when(fileUtilsInterface.isFileExists(userInputSecond.toString())).thenReturn(false);

        String expectedSecondPathString = secondCandidate.toString();
        String expectedMessage = String.format(
                "Configuration file not found at paths %s and %s",
                inDirectoryParam,
                expectedSecondPathString
        );

        // when
        KraftwerkException exception = assertThrows(
                KraftwerkException.class,
                () -> controlInputSequence.getInDirectory(inDirectoryParam)
        );

        // then
        assertEquals(expectedMessage, exception.getMessage(),
                "Both path tested should be written in the message");
        verify(fileUtilsInterface).isFileExists(userInputFirst.toString());
        verify(fileUtilsInterface).isFileExists(userInputSecond.toString());
        verifyNoMoreInteractions(fileUtilsInterface);
    }

}
