package fr.insee.kraftwerk.core.utils.log;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;

class KraftwerkExecutionLogTest {
    @Test
    void getFormattedString_test(){
        //GIVEN
        KraftwerkExecutionLog kraftwerkExecutionLog = new KraftwerkExecutionLog();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS");
        long start = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC);
        long stop = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC);

        kraftwerkExecutionLog.setStartTimeStamp(start);
        kraftwerkExecutionLog.setEndTimeStamp(stop);
        kraftwerkExecutionLog.setOkFileNames(new ArrayList<>());
        kraftwerkExecutionLog.setLineCountByTableMap(new HashMap<>());

        kraftwerkExecutionLog.getOkFileNames().add("TEST.xml");
        kraftwerkExecutionLog.getLineCountByTableMap().put("RACINE",1);

        //WHEN
        String formattedString = kraftwerkExecutionLog.getFormattedString();

        //THEN
        Assertions.assertThat(formattedString).contains("TEST.xml", "RACINE", simpleDateFormat.format(start), simpleDateFormat.format(stop));
    }
}
