package fr.insee.kraftwerk.core.metadata;


import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Object class to represent a set of variables.
 * Contains a flat map and its structured equivalent.
 */
@Log4j2
@Getter
public class VariablesMap {

    /** Map containing the variables.
     * Keys: a variable name.
     * Values: Variable. */
    protected final LinkedHashMap<String, Variable> variables = new LinkedHashMap<>();



    /** Register a variable in the map. */
    public void putVariable(Variable variable) {
    	if (StringUtils.isEmpty(variable.getName())){return;}
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
    

    /** Return the variables names that belongs to the group. */
    public Set<String> getGroupVariableNames(String groupName) {
        return variables.keySet()
                .stream().filter(name -> variables.get(name).getGroupName().equals(groupName))
                .collect(Collectors.toSet());
    }


    /** Return true if there is a variable under the given name. */
    public boolean hasVariable(String variableName) {
        return variables.containsKey(variableName);
    }
    
    /** Return true if there is a McqVariable that has the given question name in its mcqName attribute. */
    public boolean hasMcq(String questionName) {
        return variables.values().stream()
                .filter(McqVariable.class::isInstance)
                .anyMatch(mcqVariable -> // (FILTER_RESULT variables are upper case)
                        mcqVariable.getQuestionItemName().equals(questionName)
                                || mcqVariable.getQuestionItemName().equalsIgnoreCase(questionName));
    }

    public Group getMcqGroup(String questionName) {
        return variables.values().stream()
                .filter(McqVariable.class::isInstance)
                .filter(mcqVariable -> mcqVariable.getQuestionItemName().equals(questionName))
                .map(Variable::getGroup)
                .findFirst().orElse(null);
    }

    /** Return true if there is a variable from a question grid that has the given question name in its questionName attribute. */
    public boolean isInQuestionGrid(String questionName){
        return variables.values().stream()
                .filter(Variable::isInQuestionGrid)
                .anyMatch(varInQuestionGrid -> // (FILTER_RESULT variables are upper case)
                        (varInQuestionGrid.getQuestionItemName().equals(questionName)
                                || varInQuestionGrid.getQuestionItemName().equalsIgnoreCase(questionName)));
    }

    public Group getQuestionGridGroup(String questionName) {
        return variables.values().stream()
                .filter(Variable::isInQuestionGrid)
                .filter(varInQuestionGrid -> varInQuestionGrid.getQuestionItemName().equals(questionName))
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
            return variable instanceof UcqVariable ucqVariable && !ucqVariable.getQuestionItemName().isEmpty();
        }
        return false;
    }
	/** Return the list of all UCQ variables registered in the map. */
	public List<UcqVariable> getUcqVariables() {
        return variables.values().stream()
                .filter(UcqVariable.class::isInstance)
                .map(UcqVariable.class::cast)
                .toList();
    }
	/** Return the list of all names of UCQ variables registered in the map. */
    public List<String> getUcqVariablesNames() {
        return variables.values().stream()
                .filter(UcqVariable.class::isInstance)
                .map(Variable::getQuestionItemName)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
    }
	/** Return the list of all names of MCQ variables registered in the map. */
    public List<String> getMcqVariablesNames() {
        return variables.values().stream()
                .filter(McqVariable.class::isInstance)
                .map(Variable::getQuestionItemName)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
    }
    /** Return the list of all paper UCQ indicators registered in the map. */
    public List<PaperUcq> getPaperUcq() {
        return variables.values().stream()
                .filter(PaperUcq.class::isInstance)
                .map(PaperUcq.class::cast)
                .toList();
    }
    



}
