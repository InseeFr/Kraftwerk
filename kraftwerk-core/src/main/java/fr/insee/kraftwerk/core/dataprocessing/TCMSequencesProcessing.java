package fr.insee.kraftwerk.core.dataprocessing;

import fr.insee.bpm.metadata.model.MetadataModel;
import fr.insee.kraftwerk.core.utils.TextFileReader;
import fr.insee.kraftwerk.core.utils.files.FileUtilsInterface;
import fr.insee.kraftwerk.core.utils.log.KraftwerkExecutionContext;
import fr.insee.kraftwerk.core.vtl.VtlBindings;
import fr.insee.kraftwerk.core.vtl.VtlScript;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.EnumUtils;

import java.nio.file.Path;

@Slf4j
public class TCMSequencesProcessing extends DataProcessing {
    private final String vtlDirectory;
    private final MetadataModel metadataModel;

    public TCMSequencesProcessing(VtlBindings vtlBindings, MetadataModel metadataModel, String vtlDirectory, FileUtilsInterface fileUtilsInterface) {
        super(vtlBindings, fileUtilsInterface);
        this.metadataModel = metadataModel;
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
    public String applyAutomatedVtlInstructions(String bindingName, KraftwerkExecutionContext kraftwerkExecutionContext){
        VtlScript automatedInstructions = generateVtlInstructions(bindingName, metadataModel);
        log.debug(String.format("Automated VTL instructions generated for step %s: see temp file", getStepName()));
        if (!(automatedInstructions.isEmpty() || automatedInstructions.toString().contentEquals(""))) {
            vtlExecute.evalVtlScript(automatedInstructions, vtlBindings, kraftwerkExecutionContext);
        }
        return automatedInstructions.toString();
    }

    /**
     * Generate the VTL script from TCM sequences contained in DDI
     * @param bindingName unimode binding
     * @param metadataModel variables from DDI
     * @return a script containing all the custom modules scripts
     */
    protected VtlScript generateVtlInstructions(String bindingName, MetadataModel metadataModel){
        StringBuilder vtlScriptBuilder = new StringBuilder();

        //For each variable name matching TCM sequence enum
        for(String tcmSequenceString : metadataModel.getSequencesName().stream().filter(
                sequenceName -> EnumUtils.isValidEnum(TCMSequenceEnum.class, sequenceName))
                .toList()){
            TCMSequenceEnum tcmSequenceEnum = TCMSequenceEnum.valueOf(tcmSequenceString);

            for(TCMModuleEnum tcmModuleEnum : tcmSequenceEnum.getTcmModules()){
                Path tcmModulePath = Path.of(this.vtlDirectory).resolve("tcm").resolve(tcmModuleEnum.toString() + ".vtl");
                if(tcmModulePath.toFile().exists()){
                    vtlScriptBuilder.append(TextFileReader.readFromPath(tcmModulePath, fileUtilsInterface));
                    vtlScriptBuilder.append(System.lineSeparator());
                    log.info("TCM VTL instructions read for module " + tcmModuleEnum);
                }else{
                    log.warn("TCM VTL instructions for module " + tcmModuleEnum + " not found");
                }
            }
        }

        return new VtlScript(vtlScriptBuilder.toString());
    }
}
