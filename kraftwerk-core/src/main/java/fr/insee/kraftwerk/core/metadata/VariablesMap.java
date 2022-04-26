package fr.insee.kraftwerk.core.metadata;


import fr.insee.kraftwerk.core.Constants;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Object class to represent a set of variables.
 * Contains a flat map and its structured equivalent.
 */
@Slf4j
public class VariablesMap {

    /** Map containing the variables.
     * Keys: a variable name.
     * Values: Variable. */
    protected final LinkedHashMap<String, Variable> variables = new LinkedHashMap<>();

    /** Map containing the variables.
     * Keys: group name.
     * Values: Group. */
    protected final LinkedHashMap<String, Group> groups = new LinkedHashMap<>();

    /** The root group is created when creating a VariablesMap instance. */
    public VariablesMap() {
        groups.put(Constants.ROOT_GROUP_NAME, new Group(Constants.ROOT_GROUP_NAME));
    }

    /** Register a variable in the map. */
    public void putVariable(Variable variable) {
        variables.put(variable.getName(), variable);
    }
    /** Remove the variable with given name from the map. */
    public void removeVariable(String name){
        if (variables.get(name) != null) {
            variables.remove(name);
        } else {
            log.debug(String.format("Variable named \"%s\" is not in the variables map", name));
        }
    }
    /** Return the variable with given name. */
    public Variable getVariable(String variableName){
        Variable variable = variables.get(variableName);
        if(variable == null) {
            log.debug(String.format("Variable named \"%s\" is unknown", variableName));
        }
        return variable;
    }
    /** Return the names of all variables in the map. */
    public Set<String> getVariableNames() {
        return variables.keySet();
    }


    /** Return the number of all variables registered in the map. */
    public int getVariablesCount() {
        return variables.size();
    }

    /** Use other methods instead. */
    @Deprecated
    public Map<String, Variable> getVariables(){
        return variables;
    }

    /** Register a group in the map. */
    public void putGroup(Group group) {
        groups.put(group.getName(), group);
    }
    /** Return the group with given name. */
    public Group getGroup(String groupName) {
        return groups.get(groupName);
    }
    /** Return the root group. */
    public Group getRootGroup() {
        if (! groups.containsKey(Constants.ROOT_GROUP_NAME)) {
            log.debug("Root group not in the variables map.");
        }
        return groups.get(Constants.ROOT_GROUP_NAME);
    }
    /** Return the name of all groups registered in the map, including the root group. */
    public List<String> getGroupNames() {
        return new ArrayList<>(groups.keySet());
    }
    /** Return the names of all groups registered in the map, except the root group. */
    public List<String> getSubGroupNames() {
        return groups.keySet()
                .stream().filter(name -> ! groups.get(name).isRoot())
                .collect(Collectors.toList());
    }
    /** Return the number of groups in the map (including the root group). */
    public int getGroupsCount() {
        return groups.size();
    }

    /** Use other methods instead. */
    @Deprecated
    public Map<String, Group> getGroups() {
        return groups;
    }

    /** Return true if there is a variable under the given name. */
    public boolean hasVariable(String variableName) {
        return variables.containsKey(variableName);
    }
    /** Return true if there is a group under the given name. */
    public boolean hasGroup(String groupName) {
        return groups.containsKey(groupName);
    }
    /** Return false is there is only the root group. */
    public boolean hasSubGroups() {
        int size = groups.size();
        if (size < 1) {
            log.debug("No groups in this variables map. Should have at least the root group.");
        }
        return size > 1;
    }

    /** Return the fully qualified name of a variable, that is
     * - the name of the variable if it is in the root group.
     * - the variable name prefixed with its group and parent group names, otherwise.
     *
     * In the second case, the separator use is defined by Constants.METADATA_SEPARATOR. */
    public String getFullyQualifiedName(String variableName) {
        if (this.hasVariable(variableName)) {

            /* done using StringBuilder, maybe concatenate a list of strings is better
            https://stackoverflow.com/a/523913/13425151 */

            StringBuilder res = new StringBuilder(variableName);
            Variable variable = variables.get(variableName);
            Group group = variable.getGroup();
            while(! group.isRoot()) {
                res.insert(0, group.getName() + Constants.METADATA_SEPARATOR);
                group = groups.get(group.getParentName());
            }
            return res.toString();
        }
        else {
            log.debug(String.format( "Trying to get fully qualified name for unknown variable \"%s\". null returned.",
                    variableName));
            return null;
        }
    }

    /** Return the fully qualified names of all variables in the map. */
    public Set<String> getFullyQualifiedNames() {
        return variables.keySet().stream().map(this::getFullyQualifiedName).collect(Collectors.toSet());
    }

    /** Return the variables names that belongs to the group. */
    public Set<String> getGroupVariableNames(String groupName) {
        return variables.keySet()
                .stream().filter(name -> variables.get(name).getGroupName().equals(groupName))
                .collect(Collectors.toSet());
    }

    /** Return true if there is a McqVariable that has the given question name in its mcqName attribute. */
    public boolean hasMcq(String questionName) {
        return variables.values().stream()
                .filter(variable -> variable instanceof McqVariable)
                .anyMatch(mcqVariable -> // (FILTER_RESULT variables are upper case)
                        ((McqVariable) mcqVariable).getMqcName().equals(questionName)
                                || ((McqVariable) mcqVariable).getMqcName().toUpperCase().equals(questionName));
    }
    public Group getMcqGroup(String questionName) {
        return variables.values().stream()
                .filter(variable -> variable instanceof McqVariable)
                .filter(mcqVariable -> ((McqVariable) mcqVariable).getMqcName().equals(questionName))
                .map(Variable::getGroup)
                .findFirst().orElse(null);
    }

	/** Return true if there is a UCQ variable with the name given. */
	public boolean hasUcq(String variableName) {
        Variable variable = variables.get(variableName);
        if (variable != null) {
            return variable instanceof UcqVariable;
        }
        return false;
    }
	/** Return the list of all UCQ variables registered in the map. */
	public List<UcqVariable> getUcqVariables() {
        return variables.values().stream()
                .filter(variable -> variable instanceof UcqVariable)
                .map(variable -> (UcqVariable) variable)
                .collect(Collectors.toList());
    }
    /** Return the list of all paper UCQ indicators registered in the map. */
    public List<PaperUcq> getPaperUcq() {
        return variables.values().stream()
                .filter(variable -> variable instanceof PaperUcq)
                .map(variable -> (PaperUcq) variable)
                .collect(Collectors.toList());
    }
}
