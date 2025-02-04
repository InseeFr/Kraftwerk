package fr.insee.kraftwerk.core.rawdata;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class GroupDataTest {

    private GroupData groupData;

    @BeforeEach
    void setUp() {
        groupData = new GroupData("TestGroup");
    }

    @Test
    void testGetName() {
        assertEquals("TestGroup", groupData.getName());
    }

    @Test
    void testHasInstances() {
        assertFalse(groupData.hasInstances());
        groupData.getInstance("TestGroup-01");
        assertTrue(groupData.hasInstances());
    }

    @Test
    void testGetInstanceById() {
        GroupInstance instance1 = groupData.getInstance("TestGroup-01");
        assertNotNull(instance1);
        assertEquals("TestGroup-01", instance1.getId());
    }

    @Test
    void testGetInstanceByNumber() {
        GroupInstance instance2 = groupData.getInstance(0);
        assertNotNull(instance2);
        assertEquals("TestGroup-01", instance2.getId());

        GroupInstance instance3 = groupData.getInstance(5);
        assertNotNull(instance3);
        assertEquals("TestGroup-06", instance3.getId());
    }

    @Test
    void testGetInstanceIds() {
        groupData.getInstance("TestGroup-01");
        groupData.getInstance("TestGroup-02");

        Set<String> instanceIds = groupData.getInstanceIds();
        assertTrue(instanceIds.contains("TestGroup-01"));
        assertTrue(instanceIds.contains("TestGroup-02"));
    }

    @Test
    void testPutAndGetValue() {
        groupData.putValue("value1", "var1", "TestGroup-01");

        assertEquals("value1", groupData.getValue("var1", "TestGroup-01"));
        assertNull(groupData.getValue("var2", "TestGroup-01"));
        assertNull(groupData.getValue("var1", "TestGroup-02"));
    }

    @Test
    void testPutAndGetValueByInstanceNumber() {
        groupData.putValue("value1", "var1", 0);
        assertEquals("value1", groupData.getValue("var1", 0));
        assertNull(groupData.getValue("var1", 1));
    }

    @Test
    void testGetInstanceIdFormatting() {
        assertEquals("TestGroup-01", GroupData.getInstanceId("TestGroup", 0));
        assertEquals("TestGroup-02", GroupData.getInstanceId("TestGroup", 1));
        assertEquals("TestGroup-10", GroupData.getInstanceId("TestGroup", 9));
    }
}