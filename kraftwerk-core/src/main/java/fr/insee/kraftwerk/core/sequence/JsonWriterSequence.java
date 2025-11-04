package fr.insee.kraftwerk.core.sequence;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.insee.bpm.metadata.model.Group;
import fr.insee.bpm.metadata.model.MetadataModel;
import fr.insee.kraftwerk.core.data.model.InterrogationId;
import fr.insee.kraftwerk.core.data.model.SurveyUnitUpdateLatest;
import fr.insee.kraftwerk.core.exceptions.KraftwerkException;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class JsonWriterSequence {

    public void tmpJsonOutput(List<InterrogationId> listId,
                                List<SurveyUnitUpdateLatest> suLatest,
                                ObjectMapper objectMapper,
                                JsonGenerator jsonGenerator,
                                Map<String,MetadataModel> metadataModelsByMode,
                                Statement database) throws KraftwerkException, IOException {
        for (InterrogationId interrogationId : listId){
            Map<String, Object> result = buildResultMap(suLatest, interrogationId, metadataModelsByMode, database);
            if (result != null) {
                objectMapper.writeValue(jsonGenerator, result);
            }
        }
    }

    private Map<String,Object> buildResultMap(List<SurveyUnitUpdateLatest> suLatest,
                                              InterrogationId interrogationId,
                                              Map<String,MetadataModel> metadataModelsByMode,
                                              Statement database) throws KraftwerkException{
        Map<String,Object> resultById = new HashMap<>();
        SurveyUnitUpdateLatest currentSu = suLatest.stream()
                .filter(su -> su.getInterrogationId().equals(interrogationId.getId()))
                .findFirst()
                .orElse(null);

        if (currentSu == null) return null;

        resultById.put("partitionId",currentSu.getCampaignId());
        resultById.put("interrogationId", interrogationId.getId());
        resultById.put("surveyUnitId",currentSu.getSurveyUnitId());
        resultById.put("contextualId",currentSu.getContextualId());
        resultById.put("questionnaireModelId",currentSu.getQuestionnaireId());
        resultById.put("mode",currentSu.getMode());
        resultById.put("isCapturedIndirectly",currentSu.getIsCapturedIndirectly());
        resultById.put("validationDate",currentSu.getValidationDate());
        resultById.put("data",transformDataToJson(interrogationId, metadataModelsByMode, database));
        return resultById;
    }

    private Map<String,Object> transformDataToJson(InterrogationId interrogationId,
                                                   Map<String, MetadataModel> metadataModelsByMode,
                                                   Statement database) throws KraftwerkException {
        Map<String, Object> resultByScope = new LinkedHashMap<>();
        try {
            MetadataModel firstMetadataModel = metadataModelsByMode.entrySet().iterator().next().getValue();
            Map<String, Group> groups = firstMetadataModel.getGroups();
            for (String group : groups.keySet()){
                List<Object> listIteration = new ArrayList<>();
                String request = String.format("SELECT * FROM %s WHERE interrogationId='%s'", group, interrogationId.getId());
                ResultSet rs = database.executeQuery(request);
                ResultSetMetaData meta = rs.getMetaData();
                int cols = meta.getColumnCount();
                while (rs.next()) {
                    Map<String, Object> row = new LinkedHashMap<>();
                    for (int i = 1; i <= cols; i++) {
                        if (meta.getColumnLabel(i).equals("interrogationId")) continue;
                        addVariableToRow(rs,meta, i, row);
                    }
                    listIteration.add(row);
                }
                resultByScope.put(group,listIteration);
            }
        } catch (Exception e){
            log.error(e.getMessage());
            throw new KraftwerkException(500,"SQL error : extraction step");
        }
        return resultByScope;
    }

    /**
     * Convert to BigDecimal if double to avoid scientific notation
     * @param resultSet source table of data
     * @param meta metadata of that source
     * @param columnIndex index of column in table
     * @param row map to append
     */
    protected static void addVariableToRow(ResultSet resultSet,
                                         ResultSetMetaData meta,
                                         int columnIndex,
                                         Map<String, Object> row
    ) throws SQLException {
        if (meta.getColumnType(columnIndex) == Types.DOUBLE){
            BigDecimal bigDecimal = resultSet.getBigDecimal(columnIndex);
            row.put(meta.getColumnLabel(columnIndex), bigDecimal);
            return;
        }
        row.put(meta.getColumnLabel(columnIndex), resultSet.getObject(columnIndex));
    }
}
