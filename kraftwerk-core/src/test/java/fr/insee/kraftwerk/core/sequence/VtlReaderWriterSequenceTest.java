package fr.insee.kraftwerk.core.sequence;

import fr.insee.kraftwerk.core.dataprocessing.StepEnum;
import fr.insee.kraftwerk.core.vtl.VtlExecute;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Path;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) //JUnit v5
class VtlReaderWriterSequenceTest {

    @Mock
    VtlExecute vtlExecute;

    @InjectMocks
    private VtlReaderWriterSequence vtlReaderWriterSequence = new VtlReaderWriterSequence(null);

    @Test
    void readDataset4params_Test() {
        // 1. Mock the dependencies
        VtlReaderWriterSequence spyVtlReaderWriterSequence = Mockito.spy(vtlReaderWriterSequence);
        doNothing().when(spyVtlReaderWriterSequence).readDataset(any(), any(), any());
        // 2. Launch test
        spyVtlReaderWriterSequence.readDataset(null, null, StepEnum.BUILD_BINDINGS, null);
        // 3. checks
        verify(spyVtlReaderWriterSequence, times(1)).readDataset(any(), any(), any());
    }


    @Test
    void readDataset3params_Test() {
        // 1. Mock the dependencies
        VtlReaderWriterSequence spyVtlReaderWriterSequence = Mockito.spy(vtlReaderWriterSequence);
        doNothing().when(vtlExecute).putVtlDataset(any(), any(), any());
        // 2. Launch test
        spyVtlReaderWriterSequence.readDataset(any(), any(), any());
        // 3. checks
        verify(vtlExecute, times(1)).putVtlDataset(any(), any(), any());
    }


    @Test
    void writeTempBindings_Test() {
        // 1. Mock the dependencies
        VtlReaderWriterSequence spyVtlReaderWriterSequence = Mockito.spy(vtlReaderWriterSequence);
        doNothing().when(vtlExecute).writeJsonDataset(any(), any(), any());
        // 2. Launch test
        spyVtlReaderWriterSequence.writeTempBindings(Path.of("aaa/specs"), null, null, StepEnum.BUILD_BINDINGS);
        // 3. checks
        verify(vtlExecute, times(1)).writeJsonDataset(any(), any(), any());
    }


}
