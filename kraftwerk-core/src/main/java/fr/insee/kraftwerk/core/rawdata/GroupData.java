package fr.insee.kraftwerk.core.rawdata;

import java.util.LinkedHashMap;
import java.util.Set;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class GroupData {

    /** A group name. */
    String groupName;

    /** A map containing data of one of the instances of a group.
     * Keys: a group instance id.
     * Values: a GroupInstance (which is a subgroup of the current group). */
    public LinkedHashMap<String, GroupInstance> groupInstances = new LinkedHashMap<>();

    public GroupData(String name){
        this.groupName = name;
    }

    public String getName() {
        return groupName;
    }

    /**
     * Method to test if a group data object is empty or not.
     * @return True if there is at least one instance in the group data object.
     */
    public boolean hasInstances() {
        return !groupInstances.isEmpty();
    }

    /** Return the instance corresponding to the given id.
     * The instance is created if it doesn't already exist. */
    public GroupInstance getInstance(String groupId){
        if (groupInstances.containsKey(groupId)) {
            return groupInstances.get(groupId);
        } else {
            GroupInstance newInstance = new GroupInstance(groupName, groupId);
            groupInstances.put(groupId, newInstance);
            return newInstance;
        }

    }

    /** Return the instance corresponding to the given number.
     * The instance is created if it doesn't already exist. */
    public GroupInstance getInstance(Integer instanceNumber){
        String groupId = getInstanceId(instanceNumber);
        return getInstance(groupId);
    }

    /** Return the set of groups ids that are in the group data object. */
    public Set<String> getInstanceIds() {
        return groupInstances.keySet();
    }

    /**
     * Put the given value corresponding to the variable given in the data object.
     * The value is put in the correct instance using the id given.
     *
     * If the instance does not already exist, the method creates it.
     *
     * @param value A string value.
     * @param variableName A variable name.
     * @param groupId To indicate in which group instance to put the value.
     */
    public void putValue(String value, String variableName, String groupId) {
        getInstance(groupId).putValue(variableName, value);
    }

    /**
     * Put the given value corresponding to the variable given in the data object.
     * The value is put in the correct instance using instance number given.
     *
     * If the instance does not already exist, the method creates it.
     *
     * @param value A string value.
     * @param variableName A variable name.
     * @param instanceNumber To indicate in which group instance to put the value.
     */
    public void putValue(String value, String variableName, Integer instanceNumber) {
        getInstance(instanceNumber).putValue(variableName, value);
    }

    /**
     * Return the value corresponding to the variable name given, in the group instance given.
     * If the group id or the variable name doesn't exist in the data object, null is returned.
     */
    public String getValue(String variableName, String groupId) {
        if (groupInstances.containsKey(groupId)) {
            return groupInstances.get(groupId).getValue(variableName);
        } else {
            log.debug(String.format("Instance named \"%s\" is not registered in group \"%s\", null value returned.",
                    groupId, groupName));
            return null;
        }
    }

    public String getValue(String variableName, Integer instanceNumber) {
        String groupId = getInstanceId(groupName, instanceNumber);
        return getValue(variableName, groupId);
    }

    /**
     * Converts the integer given to the string instance id that will be used in datasets.
     * @param instanceNumber Instance number.
     * @return The string identifier of the instance.
     */
    protected String getInstanceId(Integer instanceNumber) {
        return getInstanceId(groupName, instanceNumber);
    }

    /** This method so that we don't duplicate code in GroupData and in GroupInstance. */
    public static String getInstanceId(String groupName, Integer instanceNumber) {
        if (instanceNumber <= 10) {
            return String.format("%s-0%d", groupName, instanceNumber + 1);
        } else {
            return String.format("%s-%d", groupName, instanceNumber + 1);
        }
    }

}
