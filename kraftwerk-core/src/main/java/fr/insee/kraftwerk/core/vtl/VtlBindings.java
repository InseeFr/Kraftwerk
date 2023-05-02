package fr.insee.kraftwerk.core.vtl;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.script.SimpleBindings;

import fr.insee.kraftwerk.core.Constants;
import fr.insee.kraftwerk.core.metadata.Group;
import fr.insee.kraftwerk.core.metadata.Variable;
import fr.insee.kraftwerk.core.metadata.VariableType;
import fr.insee.kraftwerk.core.metadata.VariablesMap;
import fr.insee.vtl.model.Dataset;
import fr.insee.vtl.model.Structured;
import lombok.extern.log4j.Log4j2;

/**
 * Class that provide method to use the Trevas library.
 */
@Log4j2
public class VtlBindings extends SimpleBindings {

    public VtlBindings(){
        super();
    }

    /** Return an array list of all names registered in the bindings. */
    public List<String> getDatasetNames() {
        return new ArrayList<>(this.keySet());
    }

    


    /**
     * Get a dataset stored in the bindings.
     *
     * @param bindingName
     * The name which was put in the bindings.
     *
     * @return A VTL dataset.
     */
    public Dataset getDataset(String bindingName){
        return (Dataset) this.get(bindingName);
    }

     // TODO: these methods might be used in some data processing classes
    public static List<String> getComponentNamesWithRole(Dataset dataset, Dataset.Role role) {
        if (dataset != null) {
            return dataset.getDataStructure().values().stream()
                    .filter(component -> component.getRole() == role)
                    .map(Structured.Component::getName)
                    .collect(Collectors.toList());
        } else {
            return null;
        }

    }
    public static List<String> getDatasetIdentifierNames(Dataset dataset) {
        return getComponentNamesWithRole(dataset, Dataset.Role.IDENTIFIER);
    }
    public static List<String> getDatasetMeasureNames(Dataset dataset) {
        return getComponentNamesWithRole(dataset, Dataset.Role.MEASURE);
    }

    /**
     * Return a string corresponding to the type of a measure in a given dataset
     * @param datasetName Name of the dataset containing the measure
     * @param measure Label of the measure
     * @return
     */
    public String getMeasureType(String datasetName, String measure){
        String type="";
        Class<?> measureClass = this.getDataset(datasetName).getDataStructure().get(measure).getType();
        try {
            if (measureClass == Class.forName("java.lang.Long")){
                type="integer";
            };
            if (measureClass == Class.forName("java.lang.Double")){
                type="number";
            };
            if (measureClass == Class.forName("java.lang.Boolean")){
                type="boolean";
            };
            if (measureClass == Class.forName("java.lang.String")){
                type="string";
            };
            return type;
        } catch (ClassNotFoundException e) {
            log.error("Class not recognized");
            return "";
        }
    }

    /**
     * Return identifier names in the dataset registered in the bindings under the given name.
     * @param datasetName Name of a dataset stored in the bindings.
     * @return List of the identifier names in the dataset, or null if there is no dataset under the given name.
     */
    public List<String> getDatasetIdentifierNames(String datasetName) {
        return getDatasetIdentifierNames(this.getDataset(datasetName));
    }
    /**
     * Return measure names in the dataset registered in the bindings under the given name.
     * @param datasetName Name of a dataset stored in the bindings.
     * @return List of the measure names in the dataset, or null if there is no dataset under the given name.
     */
    public List<String> getDatasetMeasureNames(String datasetName) {
        return getDatasetMeasureNames(this.getDataset(datasetName));
    }

    /**
     * Generates a VariablesMap object corresponding to the dataset.
     *
     * @param bindingName
     * The name which was put in the bindings for the dataset.
     *
     * @return A VariablesMap object.
     */
    public VariablesMap getDatasetVariablesMap(String bindingName){
        VariablesMap variablesMap = new VariablesMap();
        Group rootGroup = variablesMap.getRootGroup();

        Dataset ds = this.getDataset(bindingName);
        Structured.DataStructure dataStructure = ds.getDataStructure();

        for(String fullyQualifiedName : dataStructure.keySet()){

            Structured.Component datasetVariable = dataStructure.get(fullyQualifiedName);

            switch (datasetVariable.getRole()){

                case IDENTIFIER:
                    /* Identifiers in VTL datasets are the root identifier or group identifiers,
                    * these are not variables.
                    * With the actual implementation, group structured is managed through the
                    * putVariable(Variable variable) method from VariablesMap */
                    log.info(String.format("\"%s\" identifier found in dataset under binding name \"%s\"",
                            fullyQualifiedName, bindingName));
                    break;

                case MEASURE:
                    /* Measures in VTL datasets are the variable objects.
                    * We will use these to create Variable and Group objects */
                    String[] decomposition = fullyQualifiedName.split("\\" + Constants.METADATA_SEPARATOR);
                    VariableType type = VariableType.getTypeFromJavaClass(datasetVariable.getType());
                    if (decomposition.length == 0) {
                        log.debug("Unable to decompose fully qualified name given: {}", fullyQualifiedName);
                        return null;
                    } else if (decomposition.length == 1) {
                        String variableName = decomposition[0];
                        variablesMap.putVariable(new Variable(variableName, rootGroup, type));
                    } else {
                        Group group = new Group(decomposition[0], Constants.ROOT_GROUP_NAME);
                        variablesMap.putGroup(group);
                        for(int k=1; k<decomposition.length - 1; k++) {
                            group = new Group(decomposition[k], decomposition[k-1]);
                            variablesMap.putGroup(group);
                        }
                        variablesMap.putVariable(
                                new Variable(decomposition[decomposition.length - 1], group, type));
                    }
                    break;

                default:
                    log.debug(String.format(
                            "Unexpected role %s found in dataset under binding name %s. (Should not happen!)",
                            datasetVariable.getRole(), bindingName));

            }
        }

        return variablesMap;
    }

}
