package fr.insee.kraftwerk.core.data.model;

import org.springframework.lang.Nullable;

import java.util.EnumSet;

public enum Mode {

	WEB("WEB", "WEB"),TEL("TEL", "ENQ"),F2F("F2F", "ENQ"),OTHER("OTHER", ""),PAPER("PAPER", "");

	@Nullable
	private final String modeName;
	private final String folder;

	Mode(String modeName, String folder) {
		this.modeName = modeName;
		this.folder = folder;
	}

	public static Mode getEnumFromModeName(String modeName) {
		return EnumSet.allOf(Mode.class)
				.stream()
				.filter(mode -> mode.getModeName().equals(modeName))
				.findFirst()
				.orElseThrow(() -> new IllegalStateException(String.format("Unsupported mode %s.", modeName)));
	}

	public String getModeName() {
		return modeName;
	}

	public String getFolder() {
		return folder;
	}
}
