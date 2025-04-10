package fr.insee.kraftwerk.core.encryption;

import fr.insee.kraftwerk.core.exceptions.KraftwerkException;
import fr.insee.kraftwerk.core.utils.KraftwerkExecutionContext;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.nio.file.Path;

@Component
@Profile("ci-public")
public class EncryptionUtilsStub implements EncryptionUtils { //Stub used when module kraftwerk-encryption not loaded

    @Override
    public InputStream encryptOutputFile(Path pathOfFileToEncrypt, KraftwerkExecutionContext kraftwerkExecutionContext) throws KraftwerkException {
        return null;
    }

    @Override
    public String getEncryptedFileExtension() {
        return "";
    }
}