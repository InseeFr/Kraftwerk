package fr.insee.kraftwerk.core.sequence;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.insee.bpm.metadata.model.Group;
import fr.insee.bpm.metadata.model.MetadataModel;
import fr.insee.kraftwerk.core.data.model.InterrogationId;
import fr.insee.kraftwerk.core.data.model.Mode;
import fr.insee.kraftwerk.core.data.model.SurveyUnitUpdateLatest;
import fr.insee.kraftwerk.core.exceptions.KraftwerkException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static fr.insee.kraftwerk.core.sequence.JsonWriterSequence.addVariableToRow;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JsonWriterSequenceTest {

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private JsonGenerator jsonGenerator;

    @Mock
    private Statement statement;

    @Mock
    private MetadataModel metadataModel;

    private JsonWriterSequence jsonWriterSequence;

    @BeforeEach
    void setUp() {
        jsonWriterSequence = new JsonWriterSequence();
    }

    /**
     * Happy path:
     * - There are two InterrogationId values, one of which has a matching SurveyUnitUpdateLatest.
     * - Only the matching interrogation ID should lead to a JSON write.
     * - The "data" map should contain one group with one row built from ResultSet.
     */
    @SuppressWarnings("unchecked")
    @Test
    void tmpJsonOutput_shouldWriteOneJsonObjectPerMatchingSurveyUnit() throws Exception {
        // Given
        InterrogationId interrogationId1 = new InterrogationId();
        interrogationId1.setId("INT1");
        InterrogationId interrogationId2 = new InterrogationId();
        interrogationId2.setId("INT2");
        List<InterrogationId> ids = List.of(interrogationId1, interrogationId2);

        SurveyUnitUpdateLatest su1 = new SurveyUnitUpdateLatest();
        su1.setInterrogationId("INT1");
        su1.setCampaignId("CAMP1");
        su1.setSurveyUnitId("SU1");
        su1.setContextualId("CTX1");
        su1.setQuestionnaireId("Q1");
        su1.setMode(Mode.WEB);
        su1.setIsCapturedIndirectly(Boolean.TRUE);
        su1.setValidationDate(LocalDateTime.parse("2025-01-01T12:00:00"));
        List<SurveyUnitUpdateLatest> suLatest = List.of(su1);

        // Metadata: one metadata model with one group
        LinkedHashMap<String, Group> groups = new LinkedHashMap<>();
        Group g = new Group();
        groups.put("GROUP_A",g);
        when(metadataModel.getGroups()).thenReturn(groups);
        Map<String, MetadataModel> metadataModelsByMode = Map.of("WEB", metadataModel);

        // ResultSet and metadata: one row, one variable column
        ResultSetMetaData meta = mock(ResultSetMetaData.class);
        when(meta.getColumnCount()).thenReturn(2);
        when(meta.getColumnLabel(1)).thenReturn("interrogationId");
        when(meta.getColumnLabel(2)).thenReturn("VAR");
        when(meta.getColumnType(2)).thenReturn(Types.VARCHAR);

        ResultSet rs = mock(ResultSet.class);
        when(rs.getMetaData()).thenReturn(meta);
        when(rs.next()).thenReturn(true, false);
        when(rs.getObject(2)).thenReturn("value1");

        // For each group, transformDataToJson will call statement.executeQuery(...) once.
        // We return the same ResultSet for simplicity.
        when(statement.executeQuery(anyString())).thenReturn(rs);

        ArgumentCaptor<Map<String, Object>> resultCaptor = ArgumentCaptor.forClass(Map.class);

        // When
        jsonWriterSequence.tmpJsonOutput(
                ids,
                suLatest,
                objectMapper,
                jsonGenerator,
                metadataModelsByMode,
                statement
        );

        // Then
        verify(objectMapper, times(1)).writeValue(eq(jsonGenerator), resultCaptor.capture());
        Map<String, Object> written = resultCaptor.getValue();

        assertEquals("CAMP1", written.get("partitionId"));
        assertEquals("INT1", written.get("interrogationId"));
        assertEquals("SU1", written.get("surveyUnitId"));
        assertEquals("CTX1", written.get("contextualId"));
        assertEquals("Q1", written.get("questionnaireModelId"));
        assertEquals(Mode.WEB, written.get("mode"));
        assertEquals(Boolean.TRUE, written.get("isCapturedIndirectly"));
        assertEquals(LocalDateTime.parse("2025-01-01T12:00:00"), written.get("validationDate"));

        Object dataObj = written.get("data");
        assertNotNull(dataObj, "data should not be null");
        assertInstanceOf(Map.class, dataObj);

        Map<String, Object> data = (Map<String, Object>) dataObj;
        assertTrue(data.containsKey("GROUP_A"), "data should contain the group name as key");

        Object groupDataObj = data.get("GROUP_A");
        assertInstanceOf(List.class, groupDataObj, "group data should be a list");

        List<?> groupData = (List<?>) groupDataObj;
        assertEquals(1, groupData.size(), "there should be exactly one row in the group");

        Object rowObj = groupData.getFirst();
        assertInstanceOf(Map.class, rowObj, "each row should be a map");

        Map<?, ?> row = (Map<?, ?>) rowObj;
        assertEquals("value1", row.get("VAR"));
        assertFalse(row.containsKey("interrogationId"), "interrogationId column should be skipped in rows");
    }

    /**
     * If no SurveyUnitUpdateLatest matches any interrogation ID,
     * tmpJsonOutput should not write anything to the JsonGenerator.
     */
    @Test
    void tmpJsonOutput_shouldNotWriteAnything_whenNoMatchingSurveyUnit() throws Exception {
        // Given
        InterrogationId interrogationId = new InterrogationId();
        List<InterrogationId> ids = List.of(interrogationId);

        // No matching SU in the list
        List<SurveyUnitUpdateLatest> suLatest = List.of();

        // Metadata still required to avoid NPE inside transformDataToJson,
        // but transformDataToJson will never be called because buildResultMap returns null.
        Map<String, MetadataModel> metadataModelsByMode = new HashMap<>();

        // When
        jsonWriterSequence.tmpJsonOutput(
                ids,
                suLatest,
                objectMapper,
                jsonGenerator,
                metadataModelsByMode,
                statement
        );

        // Then
        verifyNoInteractions(objectMapper);
    }

    /**
     * If an SQL error occurs (e.g., executeQuery throws SQLException),
     * tmpJsonOutput should propagate a KraftwerkException with the appropriate message.
     */
    @Test
    void tmpJsonOutput_shouldThrowKraftwerkException_whenSqlErrorOccurs() throws SQLException {
        // Given
        InterrogationId interrogationId = mock(InterrogationId.class);
        when(interrogationId.getId()).thenReturn("INT1");
        List<InterrogationId> ids = List.of(interrogationId);

        SurveyUnitUpdateLatest su1 = mock(SurveyUnitUpdateLatest.class);
        when(su1.getInterrogationId()).thenReturn("INT1");
        when(su1.getCampaignId()).thenReturn("CAMP1");
        when(su1.getSurveyUnitId()).thenReturn("SU1");
        when(su1.getContextualId()).thenReturn("CTX1");
        when(su1.getQuestionnaireId()).thenReturn("Q1");
        when(su1.getMode()).thenReturn(Mode.WEB);
        when(su1.getIsCapturedIndirectly()).thenReturn(Boolean.TRUE);
        when(su1.getValidationDate()).thenReturn(LocalDateTime.parse("2025-01-01T12:00:00"));
        List<SurveyUnitUpdateLatest> suLatest = List.of(su1);

        LinkedHashMap<String, Group> groups = new LinkedHashMap<>();
        groups.put("GROUP_A", mock(Group.class));
        when(metadataModel.getGroups()).thenReturn(groups);
        Map<String, MetadataModel> metadataModelsByMode = Map.of("WEB", metadataModel);

        when(statement.executeQuery(anyString())).thenThrow(new SQLException("Boom"));

        // When / Then
        KraftwerkException exception = assertThrows(
                KraftwerkException.class,
                () -> jsonWriterSequence.tmpJsonOutput(
                        ids,
                        suLatest,
                        objectMapper,
                        jsonGenerator,
                        metadataModelsByMode,
                        statement
                )
        );

        assertEquals("SQL error : extraction step", exception.getMessage());
    }

    /**
     * addVariableToRow should convert DOUBLE columns to BigDecimal
     * to avoid scientific notation.
     */
    @Test
    void addVariableToRow_shouldConvertDoubleToBigDecimal() throws SQLException {
        // Given
        ResultSet resultSet = mock(ResultSet.class);
        ResultSetMetaData meta = mock(ResultSetMetaData.class);
        Map<String, Object> row = new LinkedHashMap<>();

        when(meta.getColumnType(1)).thenReturn(Types.DOUBLE);
        when(meta.getColumnLabel(1)).thenReturn("AMOUNT");
        when(resultSet.getBigDecimal(1)).thenReturn(new BigDecimal("123.45"));

        // When
        addVariableToRow(resultSet, meta, 1, row);

        // Then
        assertEquals(new BigDecimal("123.45"), row.get("AMOUNT"));
        verify(resultSet).getBigDecimal(1);
        verify(resultSet, never()).getObject(1);
    }

    /**
     * addVariableToRow should fall back to getObject for non-DOUBLE types.
     */
    @Test
    void addVariableToRow_shouldUseGetObjectForNonDoubleType() throws SQLException {
        // Given
        ResultSet resultSet = mock(ResultSet.class);
        ResultSetMetaData meta = mock(ResultSetMetaData.class);
        Map<String, Object> row = new LinkedHashMap<>();

        when(meta.getColumnType(1)).thenReturn(Types.INTEGER);
        when(meta.getColumnLabel(1)).thenReturn("AGE");
        when(resultSet.getObject(1)).thenReturn(42);

        // When
        addVariableToRow(resultSet, meta, 1, row);

        // Then
        assertEquals(42, row.get("AGE"));
        verify(resultSet).getObject(1);
        verify(resultSet, never()).getBigDecimal(1);
    }
}
