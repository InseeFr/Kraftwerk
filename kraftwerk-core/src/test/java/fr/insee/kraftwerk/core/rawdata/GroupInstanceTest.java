package fr.insee.kraftwerk.core.rawdata;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class GroupInstanceTest {

    private GroupInstance groupInstance;

    @BeforeEach
    void setUp() {
        groupInstance = new GroupInstance("TestGroup", "1234");
    }

    @Test
    void testGetId() {
        assertEquals("1234", groupInstance.getId());
    }

    @Test
    void testPutAndGetValue() {
        groupInstance.putValue("var1", "value1");
        assertEquals("value1", groupInstance.getValue("var1"));
    }

    @Test
    void testPutValues() {
        Map<String, String> values = Map.of("var1", "value1", "var2", "value2");
        groupInstance.putValues(values);

        assertEquals("value1", groupInstance.getValue("var1"));
        assertEquals("value2", groupInstance.getValue("var2"));
    }

    @Test
    void testGetVariableNames() {
        groupInstance.putValue("var1", "value1");
        groupInstance.putValue("var2", "value2");

        Set<String> variableNames = groupInstance.getVariableNames();
        assertTrue(variableNames.contains("var1"));
        assertTrue(variableNames.contains("var2"));
    }

    @Test
    void testGetSubGroup() {
        GroupData subGroup = groupInstance.getSubGroup("SubGroup1");
        assertNotNull(subGroup);
        assertEquals("SubGroup1", subGroup.getName());
    }

    @Test
    void testHasSubGroups() {
        assertFalse(groupInstance.hasSubGroups());
        groupInstance.getSubGroup("SubGroup1");
        assertTrue(groupInstance.hasSubGroups());
    }

    @Test
    void testGetSubGroupNames() {
        groupInstance.getSubGroup("SubGroup1");
        groupInstance.getSubGroup("SubGroup2");

        Set<String> subGroupNames = groupInstance.getSubGroupNames();
        assertTrue(subGroupNames.contains("SubGroup1"));
        assertTrue(subGroupNames.contains("SubGroup2"));
    }


}