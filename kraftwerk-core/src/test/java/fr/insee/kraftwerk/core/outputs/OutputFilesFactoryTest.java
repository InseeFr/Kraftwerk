package fr.insee.kraftwerk.core.outputs;

import fr.insee.kraftwerk.core.outputs.csv.CsvOutputFiles;
import fr.insee.kraftwerk.core.outputs.parquet.ParquetOutputFiles;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class OutputFilesFactoryTest {

    @MockitoBean
    private OutputFilesFactory spyOutputFilesFactory;

    @Test
    void createCsv_Test() {
        // 1. Mock the dependencies
        CsvOutputFiles mockCsvOutputFiles = mock(CsvOutputFiles.class);
        spyOutputFilesFactory = Mockito.spy(new OutputFilesFactory(null));
        doReturn(mockCsvOutputFiles).when(spyOutputFilesFactory).getCsvOutputFiles(any(), any(), any(), any(), any(), any(), any());

        // 2. Launch test
        CsvOutputFiles resultObject = spyOutputFilesFactory.createCsv(any(), any(), any(), any(), any(), any());

        // 3. checks
        verify(spyOutputFilesFactory, times(1)).getCsvOutputFiles(any(), any(), any(), any(), any(), any(), any());
        assertInstanceOf(CsvOutputFiles.class, resultObject);
    }


    @Test
    void createParquet_Test() {
        // 1. Mock the dependencies
        ParquetOutputFiles mockParquetOutputFiles = mock(ParquetOutputFiles.class);
        spyOutputFilesFactory = Mockito.spy(new OutputFilesFactory(null));
        doReturn(mockParquetOutputFiles).when(spyOutputFilesFactory).getParquetOutputFiles(any(), any(), any(), any(), any(), any(), any());

        // 2. Launch test
        ParquetOutputFiles resultObject = spyOutputFilesFactory.createParquet(any(), any(), any(), any(), any(), any());

        // 3. checks
        verify(spyOutputFilesFactory, times(1)).getParquetOutputFiles(any(), any(), any(), any(), any(), any(), any());
        assertInstanceOf(ParquetOutputFiles.class, resultObject);
    }


}
