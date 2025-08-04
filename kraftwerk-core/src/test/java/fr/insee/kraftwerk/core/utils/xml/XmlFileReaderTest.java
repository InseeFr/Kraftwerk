package fr.insee.kraftwerk.core.utils.xml;

import fr.insee.kraftwerk.core.utils.files.FileSystemImpl;
import fr.insee.kraftwerk.core.utils.files.FileUtilsInterface;
import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.ValidityException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class XmlFileReaderTest {

    @Test
    void readXmlFile_fileDoesNotExist(@TempDir Path tempDir) {
        Path file = tempDir.resolve("does_not_exist.xml");

        FileUtilsInterface fileUtilsInterface = new FileSystemImpl(tempDir.toString());
        XmlFileReader xmlFileReader = new XmlFileReader(fileUtilsInterface);

        //In case of file that does not exist, the method only returns null...
        Document document = xmlFileReader.readXmlFile(file);
        assertNull(document);
    }


    @Test
    void readXmlFile_badContent(@TempDir Path tempDir) throws Exception {
        Path file = tempDir.resolve("bad.xml");
        Files.write(file, "<?xml version=\"1.0\" encoding=\"UTF-8\"?>".getBytes());
        Files.write(file, "<Campaign>".getBytes(), StandardOpenOption.APPEND);
        Files.write(file, "    <Id>TEST01".getBytes(), StandardOpenOption.APPEND); //"<Id>" not closed to generate a "ParsingException" Exception
        Files.write(file, "</Campaign>".getBytes(), StandardOpenOption.APPEND);

        FileUtilsInterface fileUtilsInterface = new FileSystemImpl(tempDir.toString());
        XmlFileReader xmlFileReader = new XmlFileReader(fileUtilsInterface);
        //In case of bad content, the method does NOT throw any exception : it only logs the error & returns null...
        Document document = xmlFileReader.readXmlFile(file);
        assertNull(document);
    }


    @Test
    void readXmlFile_ValidityException(@TempDir Path tempDir) throws Exception {
        Path file = tempDir.resolve("tmp.xml");
        Files.write(file, "\n".getBytes());

        FileUtilsInterface fileUtilsInterface = new FileSystemImpl(tempDir.toString());
        XmlFileReader xmlFileReader = Mockito.spy(new XmlFileReader(fileUtilsInterface));

        // 1. Mock the dependencies
        Builder mockXmlBuilder = mock(Builder.class);
        doReturn(mockXmlBuilder).when(xmlFileReader).getXmlBuilder();
        when(mockXmlBuilder.build(any(InputStream.class))).thenThrow(ValidityException.class);

        //In case of ValidityException, the method does (normally) NOT throw any exception : it only logs the error & returns null...
        //Here, as we CANNOT mock "", we expect a "NullPointerException on "ValidityException.getErrorCount()"
        NullPointerException thrown = assertThrows(NullPointerException.class, () -> xmlFileReader.readXmlFile(file));
        assertEquals("Cannot invoke \"java.util.List.size()\" because \"this.saxExceptions\" is null", thrown.getMessage());
    }


    @Test
    void readXmlFile_IOException(@TempDir Path tempDir) throws Exception {
        Path file = tempDir.resolve("tmp.xml");
        Files.write(file, "\n".getBytes());

        FileUtilsInterface fileUtilsInterface = new FileSystemImpl(tempDir.toString());
        XmlFileReader xmlFileReader = Mockito.spy(new XmlFileReader(fileUtilsInterface));

        // 1. Mock the dependencies
        Builder mockXmlBuilder = mock(Builder.class);
        doReturn(mockXmlBuilder).when(xmlFileReader).getXmlBuilder();
        when(mockXmlBuilder.build(any(InputStream.class))).thenThrow(IOException.class);

        //In case of IOException, the method does NOT throw any exception : it only logs the error & returns null...
        Document document = xmlFileReader.readXmlFile(file);
        assertNull(document);
    }


}
