package fr.insee.kraftwerk.core.metadata;

import fr.insee.kraftwerk.core.Constants;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@Log4j2
public class MetadataModelTest {

    private MetadataModel metadataModel;

    @BeforeEach
    public void createTestVariablesMap() {
        metadataModel = new MetadataModel();

        Group rootGroup = metadataModel.getRootGroup();
        Group individualsGroup = new Group("INDIVIDUALS_LOOP", Constants.ROOT_GROUP_NAME);
        Group carsGroup = new Group("CARS_LOOP", "INDIVIDUALS_LOOP");

        metadataModel.putGroup(individualsGroup);
        metadataModel.putGroup(carsGroup);

        metadataModel.getVariables().putVariable(
                new Variable("ADDRESS", rootGroup, VariableType.STRING));
        metadataModel.getVariables().putVariable(
                new Variable("HOUSEHOLD_INCOME", rootGroup, VariableType.NUMBER));
        metadataModel.getVariables().putVariable(
                new Variable("FIRST_NAME", individualsGroup, VariableType.STRING));
        metadataModel.getVariables().putVariable(
                new Variable("LAST_NAME", individualsGroup, VariableType.STRING));
        metadataModel.getVariables().putVariable(
                new Variable("GENDER", individualsGroup, VariableType.STRING));
        metadataModel.getVariables().putVariable(
                new Variable("CAR_COLOR", carsGroup, VariableType.STRING));
    }

    @Test
    void testRootGroup() {
        assertTrue(metadataModel.hasGroup(Constants.ROOT_GROUP_NAME));
        assertEquals(Constants.ROOT_GROUP_NAME, metadataModel.getRootGroup().getName());
        assertNull(metadataModel.getRootGroup().getParentName());
    }

    @Test
    void testGetVariableByName() {

        // Get
        Variable rootVariable = metadataModel.getVariables().getVariable("HOUSEHOLD_INCOME");
        Variable group1Variable = metadataModel.getVariables().getVariable("FIRST_NAME");
        Variable group2Variable = metadataModel.getVariables().getVariable("CAR_COLOR");

        // Get a variable that does not exist
        log.debug("Trying to get a variable that does not exist in a test function, " +
                "a second message should pop in the log.");
        Variable dummyVariable = metadataModel.getVariables().getVariable("DUMMY");

        //
        assertEquals("HOUSEHOLD_INCOME", rootVariable.getName());
        assertEquals("FIRST_NAME", group1Variable.getName());
        assertEquals("CAR_COLOR", group2Variable.getName());
        assertNull(dummyVariable);
    }

    @Test
    void testRemoveAndHasVariable() {

        // Remove
        metadataModel.getVariables().removeVariable("HOUSEHOLD_INCOME");
        metadataModel.getVariables().removeVariable("CAR_COLOR");

        // Remove a variable that does not exist
        log.debug("Trying to remove a variable that does not exist in a test function, " +
                "a second message should pop in the log.");
        metadataModel.getVariables().removeVariable("FOO");

        //
        assertFalse(metadataModel.getVariables().hasVariable("HOUSEHOLD_INCOME"));
        assertTrue(metadataModel.getVariables().hasVariable("FIRST_NAME"));
        assertFalse(metadataModel.getVariables().hasVariable("CAR_COLOR"));
    }

    @Test
    void getIdentifierNamesTest() {
        assertEquals(
                List.of(Constants.ROOT_IDENTIFIER_NAME, "INDIVIDUALS_LOOP", "CARS_LOOP"),
                metadataModel.getIdentifierNames()
        );
    }

    @Test
    void getFullyQualifiedNameTest() {
        assertEquals("HOUSEHOLD_INCOME",
                metadataModel.getFullyQualifiedName("HOUSEHOLD_INCOME"));
        assertEquals("INDIVIDUALS_LOOP.FIRST_NAME",
                metadataModel.getFullyQualifiedName("FIRST_NAME"));
        assertEquals("INDIVIDUALS_LOOP.CARS_LOOP.CAR_COLOR",
                metadataModel.getFullyQualifiedName("CAR_COLOR"));
    }

    @Test
    void testGetGroupVariableNames() {
        assertTrue(metadataModel.getVariables().getGroupVariableNames(Constants.ROOT_GROUP_NAME)
                .containsAll(Set.of("ADDRESS", "HOUSEHOLD_INCOME")));
        assertTrue(metadataModel.getVariables().getGroupVariableNames("INDIVIDUALS_LOOP")
                .containsAll(Set.of("FIRST_NAME", "LAST_NAME", "GENDER")));
        assertTrue(metadataModel.getVariables().getGroupVariableNames("CARS_LOOP")
                .contains("CAR_COLOR"));
    }

    @Test
    void testMcqMethods() {
        //
        Group group = metadataModel.getGroup("INDIVIDUALS_LOOP");
        metadataModel.getVariables().putVariable(McqVariable.builder()
                .name("RELATIONSHIP_A").group(group).questionItemName("RELATIONSHIP").text("Spouse").build());
        metadataModel.getVariables().putVariable(McqVariable.builder()
                .name("RELATIONSHIP_B").group(group).questionItemName("RELATIONSHIP").text("Child").build());
        metadataModel.getVariables().putVariable(McqVariable.builder()
                .name("RELATIONSHIP_C").group(group).questionItemName("RELATIONSHIP").text("Parent").build());
        metadataModel.getVariables().putVariable(McqVariable.builder()
                .name("RELATIONSHIP_D").group(group).questionItemName("RELATIONSHIP").text("Other").build());
        //
        assertTrue(metadataModel.getVariables().hasMcq("RELATIONSHIP"));
        assertSame("RELATIONSHIP", metadataModel.getVariables().getVariable("RELATIONSHIP_A").getQuestionItemName());
        assertFalse(metadataModel.getVariables().hasMcq("ADDRESS"));
        assertFalse(metadataModel.getVariables().hasMcq("FIRST_NAME"));
        assertFalse(metadataModel.getVariables().hasMcq("CAR_COLOR"));
        assertFalse(metadataModel.getVariables().hasMcq("UNKNOWN_QUESTION"));
        //
        assertSame(group, metadataModel.getVariables().getMcqGroup("RELATIONSHIP"));
        assertNull(metadataModel.getVariables().getMcqGroup("ADDRESS"));
        assertNull(metadataModel.getVariables().getMcqGroup("FIRST_NAME"));
        assertNull(metadataModel.getVariables().getMcqGroup("CAR_COLOR"));
        assertNull(metadataModel.getVariables().getMcqGroup("UNKNOWN_QUESTION"));
    }
    
    @Test
    void testGetVariablesNames() {
    	MetadataModel metadataMod = createCompleteFakeVariablesMap();
    	// KSE et KGA à trouver, une par liste
    	List<String> ucqMcqVariablesNames = metadataMod.getVariables().getUcqVariablesNames();
    	List<String> mcqVariablesNames = metadataMod.getVariables().getMcqVariablesNames();
    	Set<String> variablesNames = metadataMod.getVariables().getVariableNames();
    	// Check ucq
        assertTrue(ucqMcqVariablesNames.contains("VEHICLE_OWNER"));
        assertFalse(ucqMcqVariablesNames.contains("CAR_OWNER"));
    	// Check mcq
        assertFalse(mcqVariablesNames.contains("VEHICLE_OWNER"));
        assertTrue(mcqVariablesNames.contains("RELATIONSHIP"));
        assertFalse(mcqVariablesNames.contains("RELATIONSHIP_A"));
    	// Check mcq
        assertFalse(variablesNames.contains("VEHICLE_OWNER"));
        assertTrue(variablesNames.contains("CAR_OWNER"));
        assertTrue(variablesNames.contains("MOTO_OWNER"));

        assertTrue(metadataMod.getVariables().hasMcq("RELATIONSHIP"));
        assertTrue(metadataMod.getVariables().hasUcq("CAR_OWNER"));
        assertTrue(metadataMod.getVariables().hasUcqMcq("CAR_OWNER"));
        assertFalse(metadataMod.getVariables().hasUcqMcq("VEHICLE_OWNER"));
        assertFalse(metadataMod.getVariables().hasMcq("ADDRESS"));
        assertFalse(metadataMod.getVariables().hasMcq("FIRST_NAME"));
        assertFalse(metadataMod.getVariables().hasMcq("CAR_COLOR"));
        assertFalse(metadataMod.getVariables().hasMcq("UNKNOWN_QUESTION"));
    }

    /* Variables map objects to test multimode management */

    /**
     * Return a VariablesMap object containing variables named as follows:
     * - FIRST_NAME, LAST_NAME, AGE at the root
     * - CAR_COLOR in a group named CARS_LOOP
     */
    public static MetadataModel createCompleteFakeVariablesMap(){

        MetadataModel metadataM = new MetadataModel();

        // Groups
        Group rootGroup = metadataM.getRootGroup();
        Group carsGroup = new Group("CARS_LOOP", Constants.ROOT_GROUP_NAME);
        metadataM.putGroup(carsGroup);

        // Variables
        metadataM.getVariables().putVariable(new Variable("LAST_NAME", rootGroup, VariableType.STRING, "20"));
        metadataM.getVariables().putVariable(new Variable("FIRST_NAME", rootGroup, VariableType.STRING, "50"));
        metadataM.getVariables().putVariable(new Variable("AGE", rootGroup, VariableType.INTEGER, "50"));
        metadataM.getVariables().putVariable(new Variable("CAR_COLOR", carsGroup, VariableType.STRING, "50"));

        // unique choice question variable
        UcqVariable ucq = new UcqVariable("SEXE", rootGroup, VariableType.STRING, "50");
        ucq.addModality("1", "Male");
        ucq.addModality("2", "Female");
        Variable paperUcq1 = new PaperUcq("SEXE_1", ucq, "1");
        Variable paperUcq2 = new PaperUcq("SEXE_2", ucq, "2");
        metadataM.getVariables().putVariable(ucq);
        metadataM.getVariables().putVariable(paperUcq1);
        metadataM.getVariables().putVariable(paperUcq2);

        // unique choice question variable related to multiple choices question
        UcqVariable ucqMcq1 = new UcqVariable("CAR_OWNER", rootGroup, VariableType.STRING, "50");
        ucqMcq1.setQuestionItemName("VEHICLE_OWNER");
        ucqMcq1.addModality("1", "Yes");
        ucqMcq1.addModality("2", "No");
        UcqVariable ucqMcq2 = new UcqVariable("MOTO_OWNER", rootGroup, VariableType.STRING, "50");
        ucqMcq2.setQuestionItemName("VEHICLE_OWNER");
        ucqMcq2.addModality("1", "Yes");
        ucqMcq2.addModality("2", "No");
        metadataM.getVariables().putVariable(ucqMcq1);
        metadataM.getVariables().putVariable(ucqMcq2);

        // multiple choices question variable
        metadataM.getVariables().putVariable(McqVariable.builder()
                .name("RELATIONSHIP_A").group(rootGroup).questionItemName("RELATIONSHIP").text("Spouse").build());
        metadataM.getVariables().putVariable(McqVariable.builder()
                .name("RELATIONSHIP_B").group(rootGroup).questionItemName("RELATIONSHIP").text("Child").build());
        metadataM.getVariables().putVariable(McqVariable.builder()
                .name("RELATIONSHIP_C").group(rootGroup).questionItemName("RELATIONSHIP").text("Parent").build());
        metadataM.getVariables().putVariable(McqVariable.builder()
                .name("RELATIONSHIP_D").group(rootGroup).questionItemName("RELATIONSHIP").text("Other").build());
        
        return metadataM;
    }

    public static MetadataModel createAnotherFakeVariablesMap(){

        MetadataModel metadataM = new MetadataModel();

        // Groups
        Group rootGroup = metadataM.getRootGroup();
        Group carsGroup = new Group("CARS_LOOP", Constants.ROOT_GROUP_NAME);
        metadataM.putGroup(carsGroup);

        // Variables
        metadataM.getVariables().putVariable(new Variable("LAST_NAME", rootGroup, VariableType.STRING, "50"));
        metadataM.getVariables().putVariable(new Variable("FIRST_NAME", rootGroup, VariableType.STRING, "20"));
        metadataM.getVariables().putVariable(new Variable("ADDRESS", rootGroup, VariableType.STRING, "50"));
        metadataM.getVariables().putVariable(new Variable("CAR_COLOR", carsGroup, VariableType.STRING, "500"));

        return metadataM;
    }

    /* Variables map objects to test information levels management */

    public static MetadataModel createVariablesMap_rootOnly() {
        MetadataModel metadataModel1 = new MetadataModel();

        Group rootGroup = metadataModel1.getRootGroup();

        metadataModel1.putGroup(rootGroup);

        metadataModel1.getVariables().putVariable(
                new Variable("ADDRESS", rootGroup, VariableType.STRING));
        metadataModel1.getVariables().putVariable(
                new Variable("HOUSEHOLD_INCOME", rootGroup, VariableType.NUMBER));

        return metadataModel1;
    }

    public static MetadataModel createVariablesMap_oneLevel() {
        MetadataModel metadataModel1 = createVariablesMap_rootOnly();

        Group individualsGroup = new Group("INDIVIDUALS_LOOP", Constants.ROOT_GROUP_NAME);

        metadataModel1.putGroup(individualsGroup);

        metadataModel1.getVariables().putVariable(
                new Variable("FIRST_NAME", individualsGroup, VariableType.STRING));
        metadataModel1.getVariables().putVariable(
                new Variable("LAST_NAME", individualsGroup, VariableType.STRING));
        metadataModel1.getVariables().putVariable(
                new Variable("GENDER", individualsGroup, VariableType.STRING));

        return metadataModel1;
    }

    public static MetadataModel createVariablesMap_twoLevels() {
        MetadataModel metadataModel1 = createVariablesMap_oneLevel();

        Group carsGroup = new Group("CARS_LOOP", "INDIVIDUALS_LOOP");

        metadataModel1.putGroup(carsGroup);

        metadataModel1.getVariables().putVariable(
                new Variable("CAR_COLOR", carsGroup, VariableType.STRING));

        return metadataModel1;
    }

}
