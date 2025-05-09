package fr.insee.kraftwerk.core.utils;

import fr.insee.kraftwerk.core.Constants;
import fr.insee.kraftwerk.core.KraftwerkError;
import lombok.Getter;
import lombok.Setter;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class KraftwerkExecutionContext {

    //Execution statistics

    private LocalDateTime executionDateTime;

    private long startTimeStamp;
    private long endTimeStamp;
    private Map<String,Integer> lineCountByTableMap;
    private List<String> okFileNames;

    private List<KraftwerkError> errors;

    //Parameters
    private String inDirectoryParam;
    private boolean fileByFile;
    private boolean withDDI;
    private boolean withEncryption;
    private long limitSize;


    public KraftwerkExecutionContext(
            String inDirectoryParam,
            boolean fileByFile,
            boolean withDDI,
            boolean withEncryption,
            long limitSize) {
        this.startTimeStamp = System.currentTimeMillis();
        this.executionDateTime = LocalDateTime.now();
        this.lineCountByTableMap = new HashMap<>();
        this.okFileNames = new ArrayList<>();
        this.errors = new ArrayList<>();

        this.inDirectoryParam = inDirectoryParam;
        this.fileByFile = fileByFile;
        this.withDDI = withDDI;
        this.withEncryption = withEncryption;
        this.limitSize = limitSize;

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

    public void addUniqueError(KraftwerkError kraftwerkError){
        if (!errors.contains(kraftwerkError)){
            errors.add(kraftwerkError);
        }
    }
}
