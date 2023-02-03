package fr.insee.kraftwerk.core.vtl;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.util.List;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import fr.insee.kraftwerk.core.rawdata.SurveyRawData;
import fr.insee.kraftwerk.core.utils.FileUtils;
import fr.insee.kraftwerk.core.utils.TextFileWriter;
import fr.insee.vtl.jackson.TrevasModule;
import fr.insee.vtl.model.Dataset;
import lombok.extern.slf4j.Slf4j;

/**
 * Class that provide method to use the Trevas library.
 */
@Slf4j
@Service
public class VtlExecute {

    /** Mapper to convert json files into VTL Datasets. */
    private final ObjectMapper mapper;
    /** Engine that will execute VTL instructions */
    private final ScriptEngine engine;


    public VtlExecute(){
        mapper = new ObjectMapper();
        mapper.registerModule(new TrevasModule());
        // Engine
        engine = new ScriptEngineManager()
                .getEngineByName("vtl");
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
    public void convertToVtlDataset(SurveyRawData surveyRawData, String bindingName, VtlBindings bindings){
        // Write data in a json file
        var vtlJsonDatasetWriter = new VtlJsonDatasetWriter(surveyRawData, bindingName);
        String tempDatasetPath = vtlJsonDatasetWriter.writeVtlJsonDataset();
        // Give this json file to the mapper to put a Dataset in the bindings
        putVtlDataset(tempDatasetPath, bindingName, bindings);
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
    private VtlBindings putVtlDataset(URL url, String bindingName, VtlBindings bindings){
        try {
            Dataset vtlDataset = mapper.readValue(url, Dataset.class);
            bindings.put(bindingName, vtlDataset);
        }
        catch(IOException e){
            log.error(String.format("Unable to connect dataset from url: %s", url));
        }
        return bindings;
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
    public void putVtlDataset(String filePath, String bindingName, VtlBindings bindings){
        try {
            URL url = new URL("file:///" + filePath);
            this.putVtlDataset(url, bindingName,  bindings);
        } catch (MalformedURLException e) {
            log.error(String.format("Invalid file path: %s", filePath), e);
        }
    }
    
    /**
     * Write the dataset registered under given name as a json file (Trevas module format).
     * @param bindingName Name of a dataset stored in the bindings.
     * @param jsonOutFile Path to write the output json file.
     * */
    public void writeJsonDataset(String bindingName, Path jsonOutFile, VtlBindings bindings) {
    	FileUtils.createDirectoryIfNotExist(jsonOutFile.getParent());
    	
    	//Write file    	
        if (bindings.containsKey(bindingName)) {
            try {
                TextFileWriter.writeFile(jsonOutFile, mapper.writeValueAsString(bindings.getDataset(bindingName)));
            } catch (JsonProcessingException e) {
                log.debug(String.format("Unable to serialize dataset stored under name '%s'.", bindingName), e);
            }
        } else {
            log.debug(String.format("No dataset under name '%s' in the bindings.", bindingName));
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
    public Dataset getDataset(String bindingName, VtlBindings bindings){
        return (Dataset) bindings.get(bindingName);
    }

    /**
     * Evaluate the given VTL instructions and update the bindings.
     * The name of the input datasets in the script must refer to the names given in the bindings.
     *
     * @param vtlScript
     * A string containing vtl instructions.
     */
    public void evalVtlScript(String vtlScript, VtlBindings bindings, List<ErrorVtlTransformation> errors){
        if(vtlScript != null && !vtlScript.equals("")) {
            try {
                // set script context
                ScriptContext context = engine.getContext();
                context.setBindings(bindings, ScriptContext.ENGINE_SCOPE);
                // eval
                engine.eval(vtlScript);
                // overwrite bindings
                bindings = (VtlBindings) engine.getContext().getBindings(ScriptContext.ENGINE_SCOPE);

            } catch (ScriptException e) {
                log.warn("ScriptException - Some VTL instruction given is invalid and has been skipped");
                errors.add(new ErrorVtlTransformation(vtlScript,e.getMessage()));
            } catch (NumberFormatException e) { 
                log.warn("NumberFormatException - Corresponding variable could not be calculated.");
                errors.add(new ErrorVtlTransformation(vtlScript,e.getMessage()));
            } catch (UnsupportedOperationException e) { 
                log.warn("UnsupportedOperationException - Corresponding variable could not be calculated.");
                errors.add(new ErrorVtlTransformation(vtlScript,e.getMessage()));
            } catch (NullPointerException e) {
                log.debug("NullPointerException - Probable cause: one of the operator used not yet supported by Trevas java library.");
                errors.add(new ErrorVtlTransformation(vtlScript,e.getMessage()));
            } catch (Error e) { 
                log.debug("Error - Probable cause: Syntax error.");
                errors.add(new ErrorVtlTransformation(vtlScript,e.getMessage()));
            } catch (Exception e) {
                log.warn("Exception - UNKNOWN EXCEPTION PLEASE REPORT IT!");
                errors.add(new ErrorVtlTransformation(vtlScript,e.getMessage()));
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
    public void evalVtlScript(VtlScript vtlScript, VtlBindings bindings, List<ErrorVtlTransformation> errors){
        if(vtlScript != null && !vtlScript.isEmpty()) {
            for(String vtlInstruction : vtlScript) {
                evalVtlScript(vtlInstruction, bindings, errors);
            }
        } else {
            log.info("null or empty VTL instructions list given. VTL bindings has not been changed.");
        }
    }


}
