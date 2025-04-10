package fr.insee.kraftwerk.vault;

import fr.insee.libjavachiffrement.vault.VaultConfig;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RealVaultContextTest {
	@Test
	void RealVaultContext() {
		VaultConfig vaultConfig = null;
		RealVaultContext expected = new RealVaultContext(null);
		RealVaultContext actual = new RealVaultContext(vaultConfig);

		assertEquals(expected, actual);
	}
}
