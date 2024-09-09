package fr.insee.kraftwerk.core.vtl;

import fr.insee.bpm.metadata.model.Group;
import fr.insee.bpm.metadata.model.MetadataModel;
import fr.insee.bpm.metadata.model.Variable;
import fr.insee.bpm.metadata.model.VariableType;
import fr.insee.kraftwerk.core.Constants;
import fr.insee.vtl.model.Dataset;
import fr.insee.vtl.model.Structured;
import fr.insee.vtl.model.Structured.Component;
import lombok.extern.log4j.Log4j2;

import javax.script.SimpleBindings;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

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
    	Stream<String> datasetNames = this.keySet().stream().filter(name -> !name.endsWith("_TEMP_KW"));
        return datasetNames.toList();
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

    public static List<String> getComponentNamesWithRole(Dataset dataset, Dataset.Role role) {
        if (dataset != null) {
            return dataset.getDataStructure().values().stream()
                    .filter(component -> component.getRole() == role)
                    .map(Structured.Component::getName)
                    .toList();
        } else {
            return new ArrayList<>();
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
     * @return String measure
     */
    public String getMeasureType(String datasetName, String measure){
        String type="";
        Class<?> measureClass = this.getDataset(datasetName).getDataStructure().get(measure).getType();
        try {
            if (measureClass == Class.forName("java.lang.Long")){
                type="integer";
            }
            if (measureClass == Class.forName("java.lang.Double")){
                type="number";
            }
            if (measureClass == Class.forName("java.lang.Boolean")){
                type="boolean";
            }
            if (measureClass == Class.forName("java.lang.String")){
                type="string";
            }
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
    public MetadataModel getDatasetVariablesMap(String bindingName){
        MetadataModel metadataModel = new MetadataModel();
        Group rootGroup = metadataModel.getRootGroup();

        Dataset ds = this.getDataset(bindingName);
        if (ds == null) {
            log.warn("Dataset {} inexistant !", bindingName);
            return metadataModel;
        }
        Structured.DataStructure dataStructure = ds.getDataStructure();

        for (Entry<String, Component> fullyQualifiedEntry : dataStructure.entrySet()) {

            String fullyQualifiedName = fullyQualifiedEntry.getKey();
            Structured.Component datasetVariable = dataStructure.get(fullyQualifiedName);

            switch (datasetVariable.getRole()) {

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
                        metadataModel.getVariables().putVariable(new Variable(variableName, rootGroup, type));
                    } else {
                        Group group = new Group(decomposition[0], Constants.ROOT_GROUP_NAME);
                        metadataModel.putGroup(group);
                        for (int k = 1; k < decomposition.length - 1; k++) {
                            group = new Group(decomposition[k], decomposition[k - 1]);
                            metadataModel.putGroup(group);
                        }
                        metadataModel.getVariables().putVariable(
                                new Variable(decomposition[decomposition.length - 1], group, type));
                    }
                    break;

                default:
                    log.debug(String.format(
                            "Unexpected role %s found in dataset under binding name %s. (Should not happen!)",
                            datasetVariable.getRole(), bindingName));

            }
        }
        return metadataModel;
    }

}
