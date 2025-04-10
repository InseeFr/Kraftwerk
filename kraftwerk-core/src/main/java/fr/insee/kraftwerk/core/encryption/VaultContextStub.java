package fr.insee.kraftwerk.core.encryption;

import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Profile("ci-public")
@AllArgsConstructor
@Component
public class VaultContextStub implements VaultContext{
    @Override
    public String getVaultPath() {
        return "";
    }

    @Override
    public Object getVaultCaller() {
        return null;
    }
}
