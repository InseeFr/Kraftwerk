package fr.insee.kraftwerk.core.utils.log;

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
        KraftwerkExecutionContext kraftwerkExecutionContext = new KraftwerkExecutionContext();
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
}
