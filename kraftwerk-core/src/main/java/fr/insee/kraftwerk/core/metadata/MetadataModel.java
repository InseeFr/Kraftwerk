package fr.insee.kraftwerk.core.metadata;

import fr.insee.kraftwerk.core.Constants;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

@Log4j2
@Getter @Setter
public class MetadataModel {

	private VariablesMap variables = new VariablesMap();
	
    /** Map containing the groups.
     * Keys: group name.
     * Values: Group. */
    private LinkedHashMap<String, Group> groups  = new LinkedHashMap<>();
	
    private List<Sequence> sequences = new ArrayList<>();
	
	
    /** The root group is created when creating a MetadataModel instance. */
    public MetadataModel() {
		groups.put(Constants.ROOT_GROUP_NAME, new Group(Constants.ROOT_GROUP_NAME));
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
    /** Return the reporting data group. */
    public Group getReportingDataGroup() {
        if (! groups.containsKey(Constants.REPORTING_DATA_GROUP_NAME)) {
            log.debug("Reporting data group not in the variables map.");
        }
        return groups.get(Constants.REPORTING_DATA_GROUP_NAME);
    }
    /** Return the name of all groups registered in the map, including the root group. */
    public List<String> getGroupNames() {
        return new ArrayList<>(groups.keySet());
    }
    /** Return the names of all groups registered in the map, except the root group. */
    public List<String> getSubGroupNames() {
        return groups.keySet()
                .stream().filter(name -> ! groups.get(name).isRoot())
                .toList();
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

    /** Return true if there is a group under the given name. */
    public boolean hasGroup(String groupName) {
        return groups.containsKey(groupName);
    }


    /** Return the fully qualified name of a variable, that is
     * - the name of the variable if it is in the root group.
     * - the variable name prefixed with its group and parent group names, otherwise.
     * In the second case, the separator use is defined by Constants.METADATA_SEPARATOR. */
    public String getFullyQualifiedName(String variableName) {
        if (getVariables().hasVariable(variableName) && StringUtils.isNotEmpty(variableName)) {

            /* done using StringBuilder, maybe concatenate a list of strings is better
            https://stackoverflow.com/a/523913/13425151 */

            StringBuilder res = new StringBuilder(variableName);
            Variable variable = getVariables().getVariables().get(variableName);
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

    public Set<String> getFullyQualifiedNames() {
        return getVariables().getVariables().keySet().stream().map(this::getFullyQualifiedName).collect(Collectors.toSet());
    }

    public Set<String> getDistinctVariableNamesAndFullyQualifiedNames(){
        Set<String> set  = new HashSet<>();
        set.addAll(getFullyQualifiedNames());
        set.addAll(getVariables().getVariableNames());
        return set;
    }

    public List<String> getSequencesName(){
        return sequences.stream().map(Sequence::getName).toList();
    }

}
