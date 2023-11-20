package fr.insee.kraftwerk.core.inputs;

import org.springframework.lang.Nullable;

public enum Mode {

	WEB("WEB", "WEB"),TEL("TEL", "ENQ"),FAF("F2F", "ENQ"),OTHER("OTHER","");

	@Nullable
	private final String modeName;

	private final String folder;

	Mode(String modeName, String folder) {
		this.modeName = modeName;
		this.folder = folder;
	}

	public static Mode getEnumFromModeName(String modeName) {
		for (Mode mode : Mode.values()) {
			if (mode.getModeName().equals(modeName)) {
				return mode;
			}
		}
		return null;
	}

	public String getModeName() {
		return modeName;
	}

	public String getFolder() {
		return folder;
	}
}
