package fr.insee.kraftwerk.batch;

import fr.insee.kraftwerk.core.Constants;
import fr.insee.kraftwerk.core.inputs.ModeInputs;
import fr.insee.kraftwerk.core.inputs.UserInputs;
import fr.insee.kraftwerk.core.metadata.DDIReader;
import fr.insee.kraftwerk.core.parsers.DataParser;
import fr.insee.kraftwerk.core.parsers.DataParserManager;
import fr.insee.kraftwerk.core.rawdata.SurveyRawData;
import fr.insee.kraftwerk.core.vtl.VtlBindings;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
public class UserVtlBatch implements Tasklet {

    @Setter
    Path campaignDirectory;

    @Override
    public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws Exception {

        Path userInputFile = campaignDirectory.resolve(Constants.USER_INPUT_FILE);

        if (Files.exists(userInputFile)) {

            UserInputs userInputs = new UserInputs(userInputFile, campaignDirectory);

            VtlBindings vtlBindings = new VtlBindings();

            for (String dataMode : userInputs.getModes()) {

                ModeInputs modeInputs = userInputs.getModeInputs(dataMode);

                SurveyRawData data = new SurveyRawData();

                data.setVariablesMap(DDIReader.getVariablesFromDDI(modeInputs.getDDIURL()));

                DataParser parser = DataParserManager.getParser(modeInputs.getDataFormat(), data);
                parser.parseSurveyData(modeInputs.getDataFile());

                vtlBindings.convertToVtlDataset(data, dataMode);


            }

        }

        else {
            log.error(String.format("No '%s' configuration file found at location: %s",
                    Constants.USER_INPUT_FILE, campaignDirectory));
        }

        return RepeatStatus.FINISHED;
    }
}
