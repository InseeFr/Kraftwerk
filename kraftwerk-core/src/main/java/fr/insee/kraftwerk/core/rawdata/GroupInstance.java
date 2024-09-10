package fr.insee.kraftwerk.core.rawdata;

import fr.insee.kraftwerk.core.Constants;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Object containing concrete data of a group.
 */
public class GroupInstance {

	/**
	 * Group name (the "typeGroupe" field of group instances in Coleman data)
	 */
	String groupName;

	/**
	 * The identifier of the group instance in data (the "idGroupe" field of group
	 * instances in Coleman data)
	 */
	String groupId;

	/**
	 * A map containing group's variables' data. Keys: a variable name. Values: the
	 * String value of a variable in data file.
	 */
	Map<String, String> values = new LinkedHashMap<>();
	/**
	 * A map containing subgroups' data. Keys: a group name. Values: a GroupData
	 * (which is a subgroup of the current group).
	 */
	Map<String, GroupData> subGroups = new LinkedHashMap<>();

	public GroupInstance(String groupName, String groupId) {
		this.groupName = groupName;
		this.groupId = groupId;
	}

	public GroupInstance(String groupName, Integer instanceNumber) {
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

	public void putValue(String variableName, String value) {
		values.put(variableName, value);
	}
	public void putValues (Map<String,String> mapToAdd){
		mapToAdd.forEach(this::putValue);
	}

	public GroupData getSubGroup(String groupName) {
		if (subGroups.containsKey(groupName)) {
			return subGroups.get(groupName);
		} else {
			GroupData newGroup = new GroupData(groupName);
				if (!groupName.contains(Constants.ROOT_GROUP_NAME)) {
				subGroups.put(groupName, newGroup);	
			}
			return newGroup;
		}
	}

	public boolean hasSubGroups() {
		return !subGroups.isEmpty();
	}

	public Set<String> getSubGroupNames() {
		return new LinkedHashSet<>(subGroups.keySet());
	}

}
