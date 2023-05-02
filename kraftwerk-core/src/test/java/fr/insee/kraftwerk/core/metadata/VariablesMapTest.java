package fr.insee.kraftwerk.core.metadata;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import fr.insee.kraftwerk.core.Constants;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class VariablesMapTest {

    private VariablesMap variablesMap;

    @BeforeEach
    public void createTestVariablesMap() {
        variablesMap = new VariablesMap();

        Group rootGroup = variablesMap.getRootGroup();
        Group individualsGroup = new Group("INDIVIDUALS_LOOP", Constants.ROOT_GROUP_NAME);
        Group carsGroup = new Group("CARS_LOOP", "INDIVIDUALS_LOOP");

        variablesMap.putGroup(individualsGroup);
        variablesMap.putGroup(carsGroup);

        variablesMap.putVariable(
                new Variable("ADDRESS", rootGroup, VariableType.STRING));
        variablesMap.putVariable(
                new Variable("HOUSEHOLD_INCOME", rootGroup, VariableType.NUMBER));
        variablesMap.putVariable(
                new Variable("FIRST_NAME", individualsGroup, VariableType.STRING));
        variablesMap.putVariable(
                new Variable("LAST_NAME", individualsGroup, VariableType.STRING));
        variablesMap.putVariable(
                new Variable("GENDER", individualsGroup, VariableType.STRING));
        variablesMap.putVariable(
                new Variable("CAR_COLOR", carsGroup, VariableType.STRING));
    }

    @Test
    public void testRootGroup() {
        assertTrue(variablesMap.hasGroup(Constants.ROOT_GROUP_NAME));
        assertEquals(Constants.ROOT_GROUP_NAME, variablesMap.getRootGroup().getName());
        assertNull(variablesMap.getRootGroup().getParentName());
    }

    @Test
    public void testGetVariableByName() {

        // Get
        Variable rootVariable = variablesMap.getVariable("HOUSEHOLD_INCOME");
        Variable group1Variable = variablesMap.getVariable("FIRST_NAME");
        Variable group2Variable = variablesMap.getVariable("CAR_COLOR");

        // Get a variable that does not exist
        log.debug("Trying to get a variable that does not exist in a test function, " +
                "a second message should pop in the log.");
        Variable dummyVariable = variablesMap.getVariable("DUMMY");

        //
        assertEquals("HOUSEHOLD_INCOME", rootVariable.getName());
        assertEquals("FIRST_NAME", group1Variable.getName());
        assertEquals("CAR_COLOR", group2Variable.getName());
        assertNull(dummyVariable);
    }

    @Test
    public void testRemoveAndHasVariable() {

        // Remove
        variablesMap.removeVariable("HOUSEHOLD_INCOME");
        variablesMap.removeVariable("CAR_COLOR");

        // Remove a variable that does not exist
        log.debug("Trying to remove a variable that does not exist in a test function, " +
                "a second message should pop in the log.");
        variablesMap.removeVariable("FOO");

        //
        assertFalse(variablesMap.hasVariable("HOUSEHOLD_INCOME"));
        assertTrue(variablesMap.hasVariable("FIRST_NAME"));
        assertFalse(variablesMap.hasVariable("CAR_COLOR"));
    }

    @Test
    public void getIdentifierNamesTest() {
        assertEquals(
                List.of(Constants.ROOT_IDENTIFIER_NAME, "INDIVIDUALS_LOOP", "CARS_LOOP"),
                variablesMap.getIdentifierNames()
        );
    }

    @Test
    public void getFullyQualifiedNameTest() {
        assertEquals("HOUSEHOLD_INCOME",
                variablesMap.getFullyQualifiedName("HOUSEHOLD_INCOME"));
        assertEquals("INDIVIDUALS_LOOP.FIRST_NAME",
                variablesMap.getFullyQualifiedName("FIRST_NAME"));
        assertEquals("INDIVIDUALS_LOOP.CARS_LOOP.CAR_COLOR",
                variablesMap.getFullyQualifiedName("CAR_COLOR"));
    }

    @Test
    public void testGetGroupVariableNames() {
        assertTrue(variablesMap.getGroupVariableNames(Constants.ROOT_GROUP_NAME)
                .containsAll(Set.of("ADDRESS", "HOUSEHOLD_INCOME")));
        assertTrue(variablesMap.getGroupVariableNames("INDIVIDUALS_LOOP")
                .containsAll(Set.of("FIRST_NAME", "LAST_NAME", "GENDER")));
        assertTrue(variablesMap.getGroupVariableNames("CARS_LOOP")
                .contains("CAR_COLOR"));
    }

    @Test
    public void testMcqMethods() {
        //
        Group group = variablesMap.getGroup("INDIVIDUALS_LOOP");
        variablesMap.putVariable(McqVariable.builder()
                .name("RELATIONSHIP_A").group(group).questionItemName("RELATIONSHIP").text("Spouse").build());
        variablesMap.putVariable(McqVariable.builder()
                .name("RELATIONSHIP_B").group(group).questionItemName("RELATIONSHIP").text("Child").build());
        variablesMap.putVariable(McqVariable.builder()
                .name("RELATIONSHIP_C").group(group).questionItemName("RELATIONSHIP").text("Parent").build());
        variablesMap.putVariable(McqVariable.builder()
                .name("RELATIONSHIP_D").group(group).questionItemName("RELATIONSHIP").text("Other").build());
        //
        assertTrue(variablesMap.hasMcq("RELATIONSHIP"));
        assertSame("RELATIONSHIP", variablesMap.getVariable("RELATIONSHIP_A").getQuestionItemName());
        assertFalse(variablesMap.hasMcq("ADDRESS"));
        assertFalse(variablesMap.hasMcq("FIRST_NAME"));
        assertFalse(variablesMap.hasMcq("CAR_COLOR"));
        assertFalse(variablesMap.hasMcq("UNKNOWN_QUESTION"));
        //
        assertSame(group, variablesMap.getMcqGroup("RELATIONSHIP"));
        assertNull(variablesMap.getMcqGroup("ADDRESS"));
        assertNull(variablesMap.getMcqGroup("FIRST_NAME"));
        assertNull(variablesMap.getMcqGroup("CAR_COLOR"));
        assertNull(variablesMap.getMcqGroup("UNKNOWN_QUESTION"));
    }
    
    @Test
    public void testGetVariablesNames() {
    	variablesMap = createCompleteFakeVariablesMap();
    	// KSE et KGA à trouver, une par liste
    	List<String> ucqMcqVariablesNames = variablesMap.getUcqVariablesNames();
    	List<String> mcqVariablesNames = variablesMap.getMcqVariablesNames();
    	Set<String> variablesNames = variablesMap.getVariableNames();
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

        assertTrue(variablesMap.hasMcq("RELATIONSHIP"));
        assertTrue(variablesMap.hasUcq("CAR_OWNER"));
        assertTrue(variablesMap.hasUcqMcq("CAR_OWNER"));
        assertFalse(variablesMap.hasUcqMcq("VEHICLE_OWNER"));
        assertFalse(variablesMap.hasMcq("ADDRESS"));
        assertFalse(variablesMap.hasMcq("FIRST_NAME"));
        assertFalse(variablesMap.hasMcq("CAR_COLOR"));
        assertFalse(variablesMap.hasMcq("UNKNOWN_QUESTION"));
    }

    /* Variables map objects to test multimode management */

    /**
     * Return a VariablesMap object containing variables named as follows:
     * - FIRST_NAME, LAST_NAME, AGE at the root
     * - CAR_COLOR in a group named CARS_LOOP
     */
    public static VariablesMap createCompleteFakeVariablesMap(){

        VariablesMap variablesMap = new VariablesMap();

        // Groups
        Group rootGroup = variablesMap.getRootGroup();
        Group carsGroup = new Group("CARS_LOOP", Constants.ROOT_GROUP_NAME);
        variablesMap.putGroup(carsGroup);

        // Variables
        variablesMap.putVariable(new Variable("LAST_NAME", rootGroup, VariableType.STRING, "20"));
        variablesMap.putVariable(new Variable("FIRST_NAME", rootGroup, VariableType.STRING, "50"));
        variablesMap.putVariable(new Variable("AGE", rootGroup, VariableType.INTEGER, "50"));
        variablesMap.putVariable(new Variable("CAR_COLOR", carsGroup, VariableType.STRING, "50"));

        // unique choice question variable
        UcqVariable ucq = new UcqVariable("SEXE", rootGroup, VariableType.STRING, "50");
        ucq.addModality("1", "Male");
        ucq.addModality("2", "Female");
        Variable paperUcq1 = new PaperUcq("SEXE_1", ucq, "1");
        Variable paperUcq2 = new PaperUcq("SEXE_2", ucq, "2");
        variablesMap.putVariable(ucq);
        variablesMap.putVariable(paperUcq1);
        variablesMap.putVariable(paperUcq2);

        // unique choice question variable related to multiple choices question
        UcqVariable ucqMcq1 = new UcqVariable("CAR_OWNER", rootGroup, VariableType.STRING, "50");
        ucqMcq1.setQuestionItemName("VEHICLE_OWNER");
        ucqMcq1.addModality("1", "Yes");
        ucqMcq1.addModality("2", "No");
        UcqVariable ucqMcq2 = new UcqVariable("MOTO_OWNER", rootGroup, VariableType.STRING, "50");
        ucqMcq2.setQuestionItemName("VEHICLE_OWNER");
        ucqMcq2.addModality("1", "Yes");
        ucqMcq2.addModality("2", "No");
        variablesMap.putVariable(ucqMcq1);
        variablesMap.putVariable(ucqMcq2);

        // multiple choices question variable
        variablesMap.putVariable(McqVariable.builder()
                .name("RELATIONSHIP_A").group(rootGroup).questionItemName("RELATIONSHIP").text("Spouse").build());
        variablesMap.putVariable(McqVariable.builder()
                .name("RELATIONSHIP_B").group(rootGroup).questionItemName("RELATIONSHIP").text("Child").build());
        variablesMap.putVariable(McqVariable.builder()
                .name("RELATIONSHIP_C").group(rootGroup).questionItemName("RELATIONSHIP").text("Parent").build());
        variablesMap.putVariable(McqVariable.builder()
                .name("RELATIONSHIP_D").group(rootGroup).questionItemName("RELATIONSHIP").text("Other").build());
        
        return variablesMap;
    }

    public static VariablesMap createAnotherFakeVariablesMap(){

        VariablesMap variablesMap = new VariablesMap();

        // Groups
        Group rootGroup = variablesMap.getRootGroup();
        Group carsGroup = new Group("CARS_LOOP", Constants.ROOT_GROUP_NAME);
        variablesMap.putGroup(carsGroup);

        // Variables
        variablesMap.putVariable(new Variable("LAST_NAME", rootGroup, VariableType.STRING, "50"));
        variablesMap.putVariable(new Variable("FIRST_NAME", rootGroup, VariableType.STRING, "20"));
        variablesMap.putVariable(new Variable("ADDRESS", rootGroup, VariableType.STRING, "50"));
        variablesMap.putVariable(new Variable("CAR_COLOR", carsGroup, VariableType.STRING, "500"));

        return variablesMap;
    }

    /* Variables map objects to test information levels management */

    public static VariablesMap createVariablesMap_rootOnly() {
        VariablesMap variablesMap = new VariablesMap();

        Group rootGroup = variablesMap.getRootGroup();

        variablesMap.putGroup(rootGroup);

        variablesMap.putVariable(
                new Variable("ADDRESS", rootGroup, VariableType.STRING));
        variablesMap.putVariable(
                new Variable("HOUSEHOLD_INCOME", rootGroup, VariableType.NUMBER));

        return variablesMap;
    }

    public static VariablesMap createVariablesMap_oneLevel() {
        VariablesMap variablesMap = createVariablesMap_rootOnly();

        Group individualsGroup = new Group("INDIVIDUALS_LOOP", Constants.ROOT_GROUP_NAME);

        variablesMap.putGroup(individualsGroup);

        variablesMap.putVariable(
                new Variable("FIRST_NAME", individualsGroup, VariableType.STRING));
        variablesMap.putVariable(
                new Variable("LAST_NAME", individualsGroup, VariableType.STRING));
        variablesMap.putVariable(
                new Variable("GENDER", individualsGroup, VariableType.STRING));

        return variablesMap;
    }

    public static VariablesMap createVariablesMap_twoLevels() {
        VariablesMap variablesMap = createVariablesMap_oneLevel();

        Group carsGroup = new Group("CARS_LOOP", "INDIVIDUALS_LOOP");

        variablesMap.putGroup(carsGroup);

        variablesMap.putVariable(
                new Variable("CAR_COLOR", carsGroup, VariableType.STRING));

        return variablesMap;
    }

}
