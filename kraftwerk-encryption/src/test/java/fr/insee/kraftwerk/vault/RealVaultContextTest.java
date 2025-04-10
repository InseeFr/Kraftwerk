package fr.insee.kraftwerk.vault;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

public class RealVaultContextTest {
	@Test
	public void RealVaultContext() {
		VaultConfig vaultConfig = null;
		RealVaultContext expected = new RealVaultContext(null);
		RealVaultContext actual = new RealVaultContext(vaultConfig);

		assertEquals(expected, actual);
	}
}
