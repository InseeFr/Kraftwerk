package fr.insee.kraftwerk.core.utils.log;

import lombok.Getter;
import lombok.Setter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class KraftwerkExecutionLog {
    private long startTimeStamp;
    private long endTimeStamp;
    private Map<String,Integer> lineCountByTableMap;
    private List<String> okFileNames;

    public KraftwerkExecutionLog() {
        this.startTimeStamp = System.currentTimeMillis();
        this.lineCountByTableMap = new HashMap<>();
        this.okFileNames = new ArrayList<>();
    }

    public String getFormattedString() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS");
        Date startDate = new Date(startTimeStamp);
        Date endDate = new Date(endTimeStamp);

        StringBuilder toWrite = new StringBuilder("Début exécution: " + simpleDateFormat.format(startDate) + "\n"
                + "Fin exécution: " + simpleDateFormat.format(endDate) + "\n"
                + "Lignes par table:\n");

        for(Map.Entry<String,Integer> entry : lineCountByTableMap.entrySet()){
            toWrite.append("\t").append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
        }

        toWrite.append("Fichiers traités avec succès: \n");

        for (String fileName : okFileNames){
            toWrite.append("\t").append(fileName).append("\n");
        }

        return toWrite.toString();

    }
}
