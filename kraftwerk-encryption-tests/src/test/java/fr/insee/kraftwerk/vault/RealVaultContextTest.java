package fr.insee.kraftwerk.vault;

import fr.insee.kraftwerk.encryption.vault.RealVaultContext;
import fr.insee.libjavachiffrement.vault.VaultConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class RealVaultContextTest {
	@Test
	void RealVaultContext() {
		VaultConfig vaultConfig = null;
		RealVaultContext expected = new RealVaultContext(null);
		RealVaultContext actual = new RealVaultContext(vaultConfig);

		assertNotNull(actual);
		Assertions.assertEquals(expected.getVaultPath(), actual.getVaultPath());
	}
}
