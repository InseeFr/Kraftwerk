package fr.insee.kraftwerk.core.utils.log;

import fr.insee.kraftwerk.core.Constants;
import fr.insee.kraftwerk.core.KraftwerkError;
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
public class KraftwerkExecutionContext {
    private long startTimeStamp;
    private long endTimeStamp;
    private Map<String,Integer> lineCountByTableMap;
    private List<String> okFileNames;

    private List<KraftwerkError> errors;

    public KraftwerkExecutionContext() {
        this.startTimeStamp = System.currentTimeMillis();
        this.lineCountByTableMap = new HashMap<>();
        this.okFileNames = new ArrayList<>();
        this.errors = new ArrayList<>();
    }

    public String getFormattedString() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS");
        Date startDate = new Date(startTimeStamp);
        Date endDate = new Date(endTimeStamp);

        StringBuilder toWrite = new StringBuilder();

        toWrite.append("Début exécution: ").append(simpleDateFormat.format(startDate)).append(Constants.END_LINE)
                .append("Fin exécution: ").append(simpleDateFormat.format(endDate)).append(Constants.END_LINE)
                .append("Lignes par table:").append(Constants.END_LINE);

        for(Map.Entry<String,Integer> entry : lineCountByTableMap.entrySet()){
            toWrite.append("\t").append(entry.getKey()).append(": ").append(entry.getValue()).append(Constants.END_LINE);
        }

        toWrite.append("Fichiers traités avec succès: ").append(Constants.END_LINE);

        for (String fileName : okFileNames){
            toWrite.append("\t").append(fileName).append(Constants.END_LINE);
        }

        return toWrite.toString();

    }
}
