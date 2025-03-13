package fr.insee.kraftwerk.core.utils.log;

import fr.insee.bpm.metadata.model.Group;
import fr.insee.bpm.metadata.model.Variable;
import fr.insee.bpm.metadata.model.VariableType;
import fr.insee.kraftwerk.core.KraftwerkError;
import fr.insee.kraftwerk.core.metadata.ErrorVariableLength;
import fr.insee.kraftwerk.core.vtl.ErrorVtlTransformation;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;

class KraftwerkExecutionContextTest {
    @Test
    void getFormattedString_test(){
        //GIVEN
        KraftwerkExecutionContext kraftwerkExecutionContext = new KraftwerkExecutionContext(
                null,
                false,
                false,
                true,
                false,
                419430400L
        );
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS");
        long start = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC);
        long stop = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC);

        kraftwerkExecutionContext.setStartTimeStamp(start);
        kraftwerkExecutionContext.setEndTimeStamp(stop);
        kraftwerkExecutionContext.setOkFileNames(new ArrayList<>());
        kraftwerkExecutionContext.setLineCountByTableMap(new HashMap<>());

        kraftwerkExecutionContext.getOkFileNames().add("TEST.xml");
        kraftwerkExecutionContext.getLineCountByTableMap().put("RACINE",1);

        //WHEN
        String formattedString = kraftwerkExecutionContext.getFormattedString();

        //THEN
        Assertions.assertThat(formattedString).contains("TEST.xml", "RACINE", simpleDateFormat.format(start), simpleDateFormat.format(stop));
    }

    @Test
    void addUniqueError_test(){
        //Given
        KraftwerkExecutionContext kraftwerkExecutionContext = new KraftwerkExecutionContext(
                null,
                false,
                false,
                true,
                false,
                419430400L
        );
        KraftwerkError kraftwerkError = new ErrorVtlTransformation("test","test");

        //When
        kraftwerkExecutionContext.addUniqueError(kraftwerkError);

        //Then
        Assertions.assertThat(kraftwerkExecutionContext.getErrors()).isNotEmpty().containsExactly(kraftwerkError);
    }

    @Test
    void addUniqueError_2xSame_test(){
        //Given
        KraftwerkExecutionContext kraftwerkExecutionContext = new KraftwerkExecutionContext(
                null,
                false,
                false,
                true,
                false,
                419430400L
        );
        KraftwerkError kraftwerkError = new ErrorVtlTransformation("test","test");

        //When
        kraftwerkExecutionContext.addUniqueError(kraftwerkError);
        kraftwerkExecutionContext.addUniqueError(kraftwerkError);

        //Then
        Assertions.assertThat(kraftwerkExecutionContext.getErrors()).isNotEmpty().hasSize(1).containsExactly(kraftwerkError);
    }

    @Test
    void addUniqueError_2xDifferent_test(){
        //Given
        KraftwerkExecutionContext kraftwerkExecutionContext = new KraftwerkExecutionContext(
                null,
                false,
                false,
                true,
                false,
                419430400L
        );
        KraftwerkError kraftwerkError = new ErrorVtlTransformation("test","test");
        KraftwerkError kraftwerkError2 = new ErrorVariableLength(new Variable("TESTVAR",new Group("TESTGROUP"), VariableType.STRING),"WEB");

        //When
        kraftwerkExecutionContext.addUniqueError(kraftwerkError);
        kraftwerkExecutionContext.addUniqueError(kraftwerkError2);

        //Then
        Assertions.assertThat(kraftwerkExecutionContext.getErrors()).isNotEmpty().hasSize(2).containsExactly(kraftwerkError, kraftwerkError2);
    }
}
