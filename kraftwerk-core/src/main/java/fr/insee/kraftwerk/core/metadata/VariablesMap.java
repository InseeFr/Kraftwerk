package fr.insee.kraftwerk.core.metadata;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import fr.insee.kraftwerk.core.Constants;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

/**
 * Object class to represent a set of variables.
 * Contains a flat map and its structured equivalent.
 */
@Log4j2
public class VariablesMap {

    /** Map containing the variables.
     * Keys: a variable name.
     * Values: Variable. */
	@Getter
    protected final LinkedHashMap<String, Variable> variables = new LinkedHashMap<>();

    /** Map containing the groups.
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
    
    public Set<String> getDistinctVariableNamesAndFullyQualifiedNames(){
    	Set<String> set  = new HashSet<>();
    	set.addAll(getFullyQualifiedNames());
    	set.addAll(getVariableNames());
    	return set;
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

    /** Identifiers are not represented by Variable objects, they are:
     * - the root identifier (fixed value),
     * - each subgroup name is also an identifier name.
     * @return The list of all identifiers associated to the variables map. */
    public List<String> getIdentifierNames() {
        List<String> res = new ArrayList<>(List.of(Constants.ROOT_IDENTIFIER_NAME));
        res.addAll(getSubGroupNames());
        return res;
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

    public List<String> getGroupVariableNamesAsList(String groupName) {
        return variables.keySet()
                .stream().filter(name -> variables.get(name).getGroupName().equals(groupName))
                .toList();
    }

    /** Return true if there is a McqVariable that has the given question name in its mcqName attribute. */
    public boolean hasMcq(String questionName) {
        return variables.values().stream()
                .filter(McqVariable.class::isInstance)
                .anyMatch(mcqVariable -> // (FILTER_RESULT variables are upper case)
                        ((McqVariable) mcqVariable).getQuestionItemName().equals(questionName)
                                || ((McqVariable) mcqVariable).getQuestionItemName().equalsIgnoreCase(questionName));
    }

    public Group getMcqGroup(String questionName) {
        return variables.values().stream()
                .filter(McqVariable.class::isInstance)
                .filter(mcqVariable -> ((McqVariable) mcqVariable).getQuestionItemName().equals(questionName))
                .map(Variable::getGroup)
                .findFirst().orElse(null);
    }

    /** Return true if there is a variable from a question grid that has the given question name in its questionName attribute. */
    public boolean isInQuestionGrid(String questionName){
        return variables.values().stream()
                .filter(Variable::isInQuestionGrid)
                .anyMatch(var -> // (FILTER_RESULT variables are upper case)
                        (var.getQuestionItemName().equals(questionName)
                                || var.getQuestionItemName().equalsIgnoreCase(questionName)));
    }

    public Group getQuestionGridGroup(String questionName) {
        return variables.values().stream()
                .filter(Variable::isInQuestionGrid)
                .filter(var -> var.getQuestionItemName().equals(questionName))
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

	/** Return true if there is a UCQ variable depending on a MCQ variable with the name given. */
	public boolean hasUcqMcq(String variableName) {
        Variable variable = variables.get(variableName);
        if (variable != null) {
            return variable instanceof UcqVariable && !((UcqVariable) variable).getQuestionItemName().isEmpty();
        }
        return false;
    }
	/** Return the list of all UCQ variables registered in the map. */
	public List<UcqVariable> getUcqVariables() {
        return variables.values().stream()
                .filter(UcqVariable.class::isInstance)
                .map(UcqVariable.class::cast)
                .collect(Collectors.toList());
    }
	/** Return the list of all names of UCQ variables registered in the map. */
    public List<String> getUcqVariablesNames() {
        return variables.values().stream()
                .filter(UcqVariable.class::isInstance)
                .map(ucqVariable -> ((UcqVariable) ucqVariable).getQuestionItemName())
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
    }
	/** Return the list of all names of MCQ variables registered in the map. */
    public List<String> getMcqVariablesNames() {
        return variables.values().stream()
                .filter(McqVariable.class::isInstance)
                .map(mcqVariable -> ((McqVariable) mcqVariable).getQuestionItemName())
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
    }
    /** Return the list of all paper UCQ indicators registered in the map. */
    public List<PaperUcq> getPaperUcq() {
        return variables.values().stream()
                .filter(PaperUcq.class::isInstance)
                .map(PaperUcq.class::cast)
                .collect(Collectors.toList());
    }
}
