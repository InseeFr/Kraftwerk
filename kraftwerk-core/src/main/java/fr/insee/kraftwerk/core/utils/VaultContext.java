package fr.insee.kraftwerk.core.utils;

import fr.insee.libjavachiffrement.vault.VaultCaller;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.context.annotation.Profile;

@Builder
@Getter
@AllArgsConstructor
@Profile("default-with-private-lib")
public class VaultContext { // Cannot use record because of stub subclass
    private VaultCaller vaultCaller;
    private String vaultPath;
}
