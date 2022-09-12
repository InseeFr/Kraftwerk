package fr.insee.kraftwerk.core.vtl;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleBindings;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import fr.insee.kraftwerk.core.Constants;
import fr.insee.kraftwerk.core.metadata.Group;
import fr.insee.kraftwerk.core.metadata.Variable;
import fr.insee.kraftwerk.core.metadata.VariableType;
import fr.insee.kraftwerk.core.metadata.VariablesMap;
import fr.insee.kraftwerk.core.rawdata.SurveyRawData;
import fr.insee.kraftwerk.core.utils.TextFileWriter;
import fr.insee.vtl.jackson.TrevasModule;
import fr.insee.vtl.model.Dataset;
import fr.insee.vtl.model.Structured;
import lombok.extern.slf4j.Slf4j;

/**
 * Class that provide method to use the Trevas library.
 */
@Slf4j
public class VtlBindings {

    /** Mapper to convert json files into VTL Datasets. */
    private final ObjectMapper mapper;
    /** Bindings for VTL datasets */
    private Bindings bindings;
    /** Engine that will execute VTL instructions */
    private final ScriptEngine engine;


    public VtlBindings(){
        //
        mapper = new ObjectMapper();
        mapper.registerModule(new TrevasModule());
        // Bindings
        bindings = new SimpleBindings();
        // Engine
        engine = new ScriptEngineManager()
                .getEngineByName("vtl");
        engine.setBindings(bindings, ScriptContext.ENGINE_SCOPE);
    }

    public void clearBindings(){
        bindings = new SimpleBindings();
    }

    public Bindings getBindings(){
        return bindings;
    }

    /** Return an array list of all names registered in the bindings. */
    public List<String> getDatasetNames() {
        return new ArrayList<>(bindings.keySet());
    }

    /**
     * Transform the given data object into a Trevas VTL dataset, and put it in the bindings.
     * The variables map of the given data object is also stored and can later be get using the method
     * getDatasetVariablesMap.
     * The given binding name will be the reference name of the dataset during the evaluation of VTL scripts.
     *
     * @param surveyRawData Data object.
     * Path to the local json file.
     * @param bindingName
     * The name the dataset will be referred to when executing VTL instructions.
     */
    public void convertToVtlDataset(SurveyRawData surveyRawData, String bindingName){
        // Write data in a json file
        var vtlJsonDatasetWriter = new VtlJsonDatasetWriter(surveyRawData, bindingName);
        String tempDatasetPath = vtlJsonDatasetWriter.writeVtlJsonDataset();
        // Give this json file to the mapper to put a Dataset in the bindings
        putVtlDataset(tempDatasetPath, bindingName);
    }

    /**
     * Write the dataset registered under given name as a json file (Trevas module format).
     * @param bindingName Name of a dataset stored in the bindings.
     * @param jsonOutFile Path to write the output json file.
     * */
    public void writeJsonDataset(String bindingName, Path jsonOutFile) {
        if (bindings.containsKey(bindingName)) {
            try {
                TextFileWriter.writeFile(jsonOutFile, mapper.writeValueAsString(getDataset(bindingName)));
            } catch (JsonProcessingException e) {
                log.debug(String.format("Unable to serialize dataset stored under name '%s'.", bindingName), e);
            }
        } else {
            log.debug(String.format("No dataset under name '%s' in the bindings.", bindingName));
        }

    }

    /**
     * Put the VTL JSON file dataset from the given URL in the bindings.
     * The given binding name will be the reference name of the dataset during the evaluation of VTL scripts.
     *
     * @param url
     * The URL of the file.
     * @param bindingName
     * The name the dataset will be referred to when executing VTL instructions.
     */
    private void putVtlDataset(URL url, String bindingName){
        try {
            Dataset vtlDataset = mapper.readValue(url, Dataset.class);
            bindings.put(bindingName, vtlDataset);
        }
        catch(IOException e){
            log.error(String.format("Unable to connect dataset from url: %s", url));
        }
    }

    /**
     * Put the VTL JSON file dataset from the given URL in the bindings.
     * The given binding name will be the reference name of the dataset during the evaluation of VTL scripts.
     *
     * @param filePath
     * Path to the local json file.
     * @param bindingName
     * The name the dataset will be referred to when executing VTL instructions.
     */
    public void putVtlDataset(String filePath, String bindingName){
        try {
            URL url = new URL("file:///" + filePath);
            this.putVtlDataset(url, bindingName);
        } catch (MalformedURLException e) {
            log.error(String.format("Invalid file path: %s", filePath), e);
        }
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
        return (Dataset) bindings.get(bindingName);
    }

    /**
     * Evaluate the given VTL instructions and update the bindings.
     * The name of the input datasets in the script must refer to the names given in the bindings.
     *
     * @param vtlScript
     * A string containing vtl instructions.
     */
    public void evalVtlScript(String vtlScript){
        if(vtlScript != null && !vtlScript.equals("")) {
            try {
                // set script context
                ScriptContext context = engine.getContext();
                context.setBindings(bindings, ScriptContext.ENGINE_SCOPE);
                // eval
                engine.eval(vtlScript);
                // overwrite bindings
                bindings = engine.getContext().getBindings(ScriptContext.ENGINE_SCOPE);

            } catch (ScriptException e) {
                log.warn("The following VTL instruction given is invalid and has been skipped:\n" + vtlScript);
                log.warn(e.getMessage());
            } catch (NumberFormatException e) { // NOTE: issue sent to Trevas // TODO: see what's changed since 0.4.0
                log.warn("NumberFormatException caused by following VTL instruction:\n" + vtlScript);
                log.warn(e.getMessage());
                log.warn("Corresponding variable could not be calculated.");
            } catch (UnsupportedOperationException e) { // TODO: send issue to Trevas
                log.warn("UnsupportedOperationException caused by following VTL instruction:\n" + vtlScript);
                log.warn(e.getMessage());
                log.warn("Corresponding variable could not be calculated.");
            } catch (NullPointerException e) { // NOTE: issue sent to Trevas // TODO: see what's changed since 0.4.0
                log.debug("NullPointerException thrown when trying to evaluate following expression:\n" + vtlScript);
                log.debug(e.getMessage());
                log.debug("Probable cause: one of the operator used not yet supported by Trevas java library.");
            } catch (Error e) { // TODO: send issue to Trevas
                log.debug("Error thrown when trying to evaluate following expression:\n" + vtlScript);
                log.error(e.getMessage());
                log.error("Probable cause: Syntax error.");
            } catch (Exception e) {
                log.warn("Exception thrown when trying to evaluate following expression:\n" + vtlScript);
                log.warn(e.getMessage());
                log.warn("UNKNOWN EXCEPTION PLEASE REPORT IT!");
            }
        } else {
            log.info("null or empty VTL instruction given. VTL bindings has not been changed.");
        }
    }

    /**
     * Evaluate the given VTL instructions and update the bindings.
     * The name of the input datasets in the script must refer to the names given in the bindings.
     *
     * @param vtlScript
     * A string containing vtl instructions.
     */
    public void evalVtlScript(VtlScript vtlScript){
        if(vtlScript != null && !vtlScript.isEmpty()) {
            for(String vtlInstruction : vtlScript) {
                evalVtlScript(vtlInstruction);
            }
        } else {
            log.info("null or empty VTL instructions list given. VTL bindings has not been changed.");
        }
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
