package fr.insee.kraftwerk.core.rawdata;

import fr.insee.kraftwerk.core.Constants;
import org.apache.commons.lang3.tuple.Pair;

/**
 * Object class to store data from a questionnaire.
 *
 */
public class QuestionnaireData {

    protected String identifier;
    protected GroupInstance answers = new GroupInstance(Constants.ROOT_GROUP_NAME, "");

    public String getIdentifier() {
        return identifier;
    }
    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public GroupInstance getAnswers() {
        return answers;
    }
    
    /**
     * Put the given value corresponding to the variable given in the data object.
     * If it is a root variable, no instance reference must be given.
     * Else, the value is put in the correct instance using instance references given.
     * The instance references are pairs of group name / instance id or number
     * to indicate in which instance to put the value.
     * The first pair is associated to the first level of group,
     * the last pair corresponds to the deepest group.
     *
     * If a group or instance does not already exist in the data object,
     * the method will create these instances.
     *
     * @param value A string value.
     * @param variableName A variable name.
     * @param instanceReferences
     * To indicate where to put the value.
     * Key: a group name
     * Value: a group id (String) or group number (Integer)
     */
    @SafeVarargs
    public final void putValue(String value, String variableName, Pair<String, Object>... instanceReferences) {
        goToInstance(instanceReferences).putValue(variableName, value);
    }

    /**
     * Return the value corresponding to the variable name given.
     * If it is a root variable, no instance reference must be given.
     * Else, the value is found using instance references given.
     * The instance references are pairs of group name / instance id or number
     * to indicate in which instance is stored the value.
     * The first pair is associated to the first level of group,
     * the last pair corresponds to the deepest group.
     *
     * Warning: If a group or instance indicated by the references does not exist in the data object,
     * the method will create these instances, and a null value will be returned.
     *
     * @param variableName A variable name.
     * @param instanceReferences
     * To indicate where is stored the value.
     * Key: a group name
     * Value: a group id (String) or group number (Integer)
     */
    @SafeVarargs
    public final String getValue(String variableName, Pair<String, Object>... instanceReferences) {
        return goToInstance(instanceReferences).getValue(variableName);
    }

    /** Return the instance described by the instance references given. */
    private GroupInstance goToInstance(Pair<String, Object>[] instanceReferences) {
        GroupInstance currentInstance = answers;
        GroupData groupData;
        for (Pair<String, Object> instanceReference : instanceReferences) {
            String groupName = instanceReference.getLeft();
            if (instanceReference.getRight() instanceof String) {
                String groupId = (String) instanceReference.getRight();
                groupData = currentInstance.getSubGroup(groupName);
                currentInstance = groupData.getInstance(groupId);
            } else if (instanceReference.getRight() instanceof Integer) {
                Integer instanceNumber = (Integer) instanceReference.getRight();
                groupData = currentInstance.getSubGroup(groupName);
                currentInstance = groupData.getInstance(instanceNumber);
            } else {
                throw new IllegalArgumentException(
                        "Right value in an instance reference must be either String or Integer");
            }
        }
        return currentInstance;
    }
}
