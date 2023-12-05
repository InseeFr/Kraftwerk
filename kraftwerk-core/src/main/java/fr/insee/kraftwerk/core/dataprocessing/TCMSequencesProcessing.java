package fr.insee.kraftwerk.core.dataprocessing;

import fr.insee.kraftwerk.core.KraftwerkError;
import fr.insee.kraftwerk.core.metadata.VariablesMap;
import fr.insee.kraftwerk.core.utils.TextFileReader;
import fr.insee.kraftwerk.core.vtl.VtlBindings;
import fr.insee.kraftwerk.core.vtl.VtlScript;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.EnumUtils;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

@Slf4j
public class TCMSequencesProcessing extends DataProcessing {
    private final String vtlDirectory;
    private final Map<String, VariablesMap> metadataVariables;

    protected TCMSequencesProcessing(VtlBindings vtlBindings, Map<String, VariablesMap> metadataVariables,String vtlDirectory) {
        super(vtlBindings);
        this.metadataVariables = metadataVariables;
        this.vtlDirectory = vtlDirectory;
    }

    @Override
    public String getStepName() {
        return "TCM Sequences";
    }

    @Override
    protected VtlScript generateVtlInstructions(String bindingName) {
        return new VtlScript();
    }

    @Override
    protected String applyAutomatedVtlInstructions(String bindingName, List<KraftwerkError> errors){
        VtlScript automatedInstructions = generateVtlInstructions(bindingName, this.metadataVariables);
        log.debug(String.format("Automated VTL instructions generated for step %s: see temp file", getStepName()));
        if (!(automatedInstructions.isEmpty() || automatedInstructions.toString().contentEquals(""))) {
            vtlExecute.evalVtlScript(automatedInstructions, vtlBindings, errors);
        }
        return automatedInstructions.toString();
    }

    /**
     * Generate the VTL script from TCM sequences contained in DDI
     * @param bindingName unimode binding
     * @param metadataVariables variables from DDI
     * @return a script containing all the custom modules scripts
     */
    protected VtlScript generateVtlInstructions(String bindingName, Map<String, VariablesMap> metadataVariables){
        StringBuilder vtlScriptBuilder = new StringBuilder();
        VariablesMap variablesMap = metadataVariables.get(bindingName);

        //For each variable name matching TCM sequence enum
        for(String tcmSequenceString : variablesMap.getVariableNames().stream().filter(
                variableName -> EnumUtils.isValidEnum(TCMSequenceEnum.class, variableName))
                .toList()){
            TCMSequenceEnum tcmSequenceEnum = TCMSequenceEnum.valueOf(tcmSequenceString);

            for(TCMModuleEnum tcmModuleEnum : TCMModuleEnum.getModules(tcmSequenceEnum)){
                Path tcmModulePath = Path.of(this.vtlDirectory).resolve("tcm").resolve(tcmModuleEnum.toString() + ".vtl");
                if(tcmModulePath.toFile().exists()){
                    vtlScriptBuilder.append(TextFileReader.readFromPath(tcmModulePath));
                    log.info("TCM VTL instructions read for module " + tcmModuleEnum);
                }else{
                    log.warn("TCM VTL instructions for module " + tcmModuleEnum + " not found");
                }
            }
        }

        return new VtlScript(vtlScriptBuilder.toString());
    }
}
