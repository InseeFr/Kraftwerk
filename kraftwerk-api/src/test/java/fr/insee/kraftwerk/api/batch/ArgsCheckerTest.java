package fr.insee.kraftwerk.api.batch;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

class ArgsCheckerTest {

    @Test
    void checkArgs_validServiceName_setsKraftwerkServiceType() {
        ArgsChecker checker = getArgCheckerBuilder().build();

        assertThatNoException().isThrownBy(checker::checkArgs);
        assertThat(checker.getKraftwerkServiceType()).isEqualTo(KraftwerkServiceType.MAIN);
    }

    @Test
    void checkArgs_invalidServiceName_throwsIllegalArgumentException() {
        ArgsChecker checker = getArgCheckerBuilder()
                .argServiceName("INVALID_SERVICE")
                .build();

        assertThatIllegalArgumentException()
                .isThrownBy(checker::checkArgs)
                .withMessageContaining("Invalid service argument");
    }

    @Test
    void checkArgs_nullServiceName_throwsIllegalArgumentException() {
        ArgsChecker checker = getArgCheckerBuilder()
                .argServiceName(null)
                .build();

        assertThatIllegalArgumentException().isThrownBy(checker::checkArgs);
    }

    @Test
    void checkArgs_nullQuestionnaireId_throwsIllegalArgumentException() {
        ArgsChecker checker = getArgCheckerBuilder()
                .argQuestionnaireId(null)
                .build();

        assertThatIllegalArgumentException()
                .isThrownBy(checker::checkArgs)
                .withMessageContaining("No questionnaireId");
    }

    @Test
    void checkArgs_validQuestionnaireId_setsField() {
        ArgsChecker checker = getArgCheckerBuilder()
                .argQuestionnaireId("my-questionnaire")
                .build();

        assertThatNoException().isThrownBy(checker::checkArgs);
        assertThat(checker.getQuestionnaireId()).isEqualTo("my-questionnaire");
    }

    @Test
    void checkArgs_nullIsReportingData_defaultsFalse() {
        ArgsChecker checker = getArgCheckerBuilder().build();

        assertThatNoException().isThrownBy(checker::checkArgs);
        assertThat(checker.isReportingData()).isFalse();
    }

    @Test
    void checkArgs_isReportingDataTrue_withFilePath_setsCorrectly() {
        ArgsChecker checker = getArgCheckerBuilder()
                .argIsReportingData("true")
                .argReportingDataFilePath("/path/to/file.xml")
                .build();

        assertThatNoException().isThrownBy(checker::checkArgs);
        assertThat(checker.isReportingData()).isTrue();
        assertThat(checker.getReportingDataFilePath()).isEqualTo("/path/to/file.xml");
    }

    @Test
    void checkArgs_isReportingDataFalse_noFilePath_ok() {
        ArgsChecker checker = getArgCheckerBuilder()
                .argIsReportingData("false")
                .build();

        assertThatNoException().isThrownBy(checker::checkArgs);
        assertThat(checker.isReportingData()).isFalse();
    }

    @Test
    void checkArgs_isReportingDataTrue_noFilePath_throwsIllegalArgumentException() {
        ArgsChecker checker = getArgCheckerBuilder()
                .argIsReportingData("true")
                .build();

        assertThatIllegalArgumentException()
                .isThrownBy(checker::checkArgs)
                .withMessageContaining("No reporting data file argument");
    }

    @ParameterizedTest
    @ValueSource(strings = {"yes", "1", "TRUE", "oui", ""})
    void checkArgs_isReportingDataInvalidBoolean_throwsIllegalArgumentException(String value) {
        ArgsChecker checker = getArgCheckerBuilder()
                .argIsReportingData(value)
                .build();

        assertThatIllegalArgumentException()
                .isThrownBy(checker::checkArgs)
                .withMessageContaining("Invalid reportingData boolean argument");
    }


    @Test
    void checkArgs_nullWithEncryption_defaultsFalse() {
        ArgsChecker checker = getArgCheckerBuilder().build();

        assertThatNoException().isThrownBy(checker::checkArgs);
        assertThat(checker.isWithEncryption()).isFalse();
    }

    @Test
    void checkArgs_withEncryptionTrue_setsTrue() {
        ArgsChecker checker = getArgCheckerBuilder()
                .argWithEncryption("true")
                .build();

        assertThatNoException().isThrownBy(checker::checkArgs);
        assertThat(checker.isWithEncryption()).isTrue();
    }

    @Test
    void checkArgs_withEncryptionFalse_setsFalse() {
        ArgsChecker checker = getArgCheckerBuilder()
                .argWithEncryption("false")
                .build();

        assertThatNoException().isThrownBy(checker::checkArgs);
        assertThat(checker.isWithEncryption()).isFalse();
    }

    @ParameterizedTest
    @ValueSource(strings = {"yes", "no", "1", "0"})
    void checkArgs_withEncryptionInvalidBoolean_throwsIllegalArgumentException(String value) {
        ArgsChecker checker = getArgCheckerBuilder()
                .argWithEncryption(value)
                .build();

        assertThatIllegalArgumentException()
                .isThrownBy(checker::checkArgs)
                .withMessageContaining("Invalid argWithEncryption boolean argument");
    }

    @Test
    void checkArgs_nullWithDDI_defaultsTrue() {
        ArgsChecker checker = getArgCheckerBuilder().build();

        assertThatNoException().isThrownBy(checker::checkArgs);
        assertThat(checker.isWithDDI()).isTrue();
    }

    @Test
    void checkArgs_withDDIFalse_setsFalse() {
        ArgsChecker checker = getArgCheckerBuilder()
                .argWithDDI("false")
                .build();

        assertThatNoException().isThrownBy(checker::checkArgs);
        assertThat(checker.isWithDDI()).isFalse();
    }

    @ParameterizedTest
    @ValueSource(strings = {"yes", "no"})
    void checkArgs_withDDIInvalidBoolean_throwsIllegalArgumentException(String value) {
        ArgsChecker checker = getArgCheckerBuilder()
                .argWithDDI(value)
                .build();

        assertThatIllegalArgumentException()
                .isThrownBy(checker::checkArgs)
                .withMessageContaining("Invalid withDDI boolean argument");
    }

    @Test
    void checkArgs_jsonService_validSince_parsesSince() {
        ArgsChecker checker = ArgsChecker.builder()
                .argServiceName("JSON")
                .argQuestionnaireId("questionnaire-123")
                .argSince("2024-01-15T10:30:00")
                .build();

        assertThatNoException().isThrownBy(checker::checkArgs);
        assertThat(checker.getSince()).isEqualTo(LocalDateTime.of(2024, 1, 15, 10, 30, 0));
    }

    @Test
    void checkArgs_jsonService_nullSince_sinceRemainsNull() {
        ArgsChecker checker = ArgsChecker.builder()
                .argServiceName("JSON")
                .argQuestionnaireId("questionnaire-123")
                .build();

        assertThatNoException().isThrownBy(checker::checkArgs);
        assertThat(checker.getSince()).isNull();
    }

    @Test
    void checkArgs_jsonService_invalidSince_throwsIllegalArgumentException() {
        ArgsChecker checker = ArgsChecker.builder()
                .argServiceName("JSON")
                .argQuestionnaireId("questionnaire-123")
                .argSince("not-a-date")
                .build();

        assertThatIllegalArgumentException()
                .isThrownBy(checker::checkArgs)
                .withMessageContaining("Invalid since argument");
    }

    @Test
    void checkArgs_nonJsonService_sinceIgnored() {
        ArgsChecker checker = getArgCheckerBuilder()
                .argSince("not-a-date") // invalide mais ne doit pas être vérifié
                .build();

        assertThatNoException().isThrownBy(checker::checkArgs);
        assertThat(checker.getSince()).isNull();
    }

    @Test
    void checkArgs_nonJsonService_nullAddStates_defaultsFalse() {
        ArgsChecker checker = getArgCheckerBuilder().build();

        assertThatNoException().isThrownBy(checker::checkArgs);
        assertThat(checker.isAddStates()).isFalse();
    }

    @Test
    void checkArgs_addStatesTrue_setsTrue() {
        ArgsChecker checker = getArgCheckerBuilder()
                .argAddStates("true")
                .build();

        assertThatNoException().isThrownBy(checker::checkArgs);
        assertThat(checker.isAddStates()).isTrue();
    }

    @Test
    void checkArgs_addStatesFalse_setsFalse() {
        ArgsChecker checker = getArgCheckerBuilder()
                .argAddStates("false")
                .build();

        assertThatNoException().isThrownBy(checker::checkArgs);
        assertThat(checker.isAddStates()).isFalse();
    }

    @ParameterizedTest
    @ValueSource(strings = {"yes", "1", "TRUE"})
    void checkArgs_addStatesInvalidBoolean_throwsIllegalArgumentException(String value) {
        ArgsChecker checker = getArgCheckerBuilder()
                .argAddStates(value)
                .build();

        assertThatIllegalArgumentException()
                .isThrownBy(checker::checkArgs)
                .withMessageContaining("Invalid addStates boolean argument");
    }

    @Test
    void checkArgs_fileByFileService_setsIsFileByFileTrue() {
        ArgsChecker checker = ArgsChecker.builder()
                .argServiceName("FILE_BY_FILE")
                .argQuestionnaireId("questionnaire-123")
                .build();

        assertThatNoException().isThrownBy(checker::checkArgs);
        assertThat(checker.isFileByFile()).isTrue();
    }

    @Test
    void checkArgs_nonFileByFileService_setsIsFileByFileFalse() {
        ArgsChecker checker = getArgCheckerBuilder().build();

        assertThatNoException().isThrownBy(checker::checkArgs);
        assertThat(checker.isFileByFile()).isFalse();
    }

    //UTILS
    private ArgsChecker.ArgsCheckerBuilder getArgCheckerBuilder() {
        return ArgsChecker.builder()
                .argServiceName("MAIN")
                .argQuestionnaireId("questionnaire-123");
    }
}