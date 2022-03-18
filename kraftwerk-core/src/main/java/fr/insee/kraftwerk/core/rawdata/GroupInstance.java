package fr.insee.kraftwerk.core.rawdata;

import java.util.*;

/**
 * Object containing concrete data of a group.
 */
public class GroupInstance {

    /** Group name
     * (the "typeGroupe" field of group instances in Coleman data) */
    String groupName;

    /** The identifier of the group instance in data
     * (the "idGroupe" field of group instances in Coleman data) */
    String groupId;

    /** A map containing group's variables' data.
     * Keys: a variable name.
     * Values: the String value of a variable in data file. */
    LinkedHashMap<String, String> values = new LinkedHashMap<>();
    /** A map containing sub groups' data.
     * Keys: a group name.
     * Values: a GroupData (which is a subgroup of the current group). */
    LinkedHashMap<String, GroupData> subGroups = new LinkedHashMap<>();

    public GroupInstance(String groupName, String groupId){
        this.groupName = groupName;
        this.groupId = groupId;
    }

    public GroupInstance(String groupName, Integer instanceNumber){
        this.groupName = groupName;
        this.groupId = GroupData.getInstanceId(groupName, instanceNumber);
    }

    public String getId() {
        return groupId;
    }

    public String getValue(String variableName) {
        return values.get(variableName);
    }

    public Set<String> getVariableNames() {
        return values.keySet();
    }

    @Deprecated // use getVariableNames
    public LinkedHashMap<String, String> getValues() {
        return values;
    }

    public void putValue(String variableName, String value){
        values.put(variableName, value);
    }
    
    public GroupData getSubGroup(String groupName){
        if (subGroups.containsKey(groupName)) {
            return subGroups.get(groupName);
        } else {
            GroupData newGroup = new GroupData(groupName);
            subGroups.put(groupName, newGroup);
            return newGroup;
        }
    }

    public boolean hasSubGroups() {
        return subGroups.size() > 0;
    }

    public Set<String> getSubGroupNames() {
        return new LinkedHashSet<>(subGroups.keySet());
    }

    @Deprecated // use getSubGroupNames instead
    public LinkedHashMap<String, GroupData> getSubGroups() {
        return subGroups;
    }

    @Deprecated // use getSubGroupInstead
    public void putSubGroup(String groupName, GroupData subGroup){
        subGroups.put(groupName, subGroup);
    }

}
