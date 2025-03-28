package fr.insee.kraftwerk.core.utils;

import fr.insee.libjavachiffrement.core.vault.VaultCaller;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@AllArgsConstructor
public class VaultContext { // Cannot use record because of stub subclass
    private VaultCaller vaultCaller;
    private String vaultPath;
}
