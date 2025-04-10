package fr.insee.kraftwerk.core.encryption;

public interface VaultContext {
    String getVaultPath();

    Object getVaultCaller();
}