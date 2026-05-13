package fr.insee.kraftwerk.core.inputs;

import fr.insee.bpm.metadata.model.CalculatedVariables;
import fr.insee.kraftwerk.core.TestConstants;
import fr.insee.kraftwerk.core.exceptions.NullLunaticFileException;
import fr.insee.kraftwerk.core.utils.files.FileSystemImpl;
import lombok.SneakyThrows;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class ModeInputsTest {

    private ModeInputs modeInputs;
    private static final String LUNATIC_PATH = "unit_tests/lunatic/log2021x21_tel.json";

    FileSystemImpl fileSystem;

    @BeforeEach
    void setUp() {
        fileSystem = spy(FileSystemImpl.class);
        modeInputs = new ModeInputs(fileSystem);
    }


    @Nested
    class getCalculatedVariables_tests{
        @Test
        @SneakyThrows
        void getCalculatedVariables_parse_test() {
            //GIVEN
            Path lunaticFilePath = Path.of(TestConstants.TEST_RESOURCES_DIRECTORY, LUNATIC_PATH);
            modeInputs.setLunaticFile(lunaticFilePath);

            //WHEN
            CalculatedVariables calculatedVariables = modeInputs.getCalculatedVariables();

            //THEN
            verify(fileSystem, times(1)).readFile(lunaticFilePath.toString());
            Assertions.assertThat(calculatedVariables).isNotNull().isNotEmpty();
            Assertions.assertThat(modeInputs.getCalculatedVariablesCache()).isEqualTo(calculatedVariables);
        }

        @Test
        @SneakyThrows
        void getCalculatedVariables_getFromCache_test() {
            //GIVEN
            Path lunaticFilePath = Path.of(TestConstants.TEST_RESOURCES_DIRECTORY, LUNATIC_PATH);
            modeInputs.setLunaticFile(lunaticFilePath);

            CalculatedVariables givenCalculatedVariables = new CalculatedVariables();
            modeInputs.setCalculatedVariablesCache(givenCalculatedVariables);

            //WHEN
            CalculatedVariables calculatedVariables = modeInputs.getCalculatedVariables();

            //THEN
            verify(fileSystem, never()).readFile(lunaticFilePath.toString());
            Assertions.assertThat(calculatedVariables).isEqualTo(givenCalculatedVariables);
        }

        @Test
        @SneakyThrows
        void getCalculatedVariables_null_lunatic_file_exception_test() {
            //WHEN + THEN
            Assertions.assertThatThrownBy(() -> modeInputs.getCalculatedVariables()).isInstanceOf(
                    NullLunaticFileException.class
            );
        }
    }
}