package fr.insee.kraftwerk.core.data.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Getter;
import org.springframework.lang.Nullable;

import java.util.EnumSet;
import java.util.Objects;

@Getter
public enum Mode {

	WEB("WEB", "WEB","CAWI"),
	TEL("TEL", "ENQ","CATI"),
	F2F("F2F", "ENQ","CAPI"),
	OTHER("OTHER", "",""),
	PAPER("PAPER", "","PAPI");

	@Nullable
	private final String modeName;
	private final String folder;
	private final String jsonName;

	Mode(@Nullable String modeName, String folder, String jsonName) {
		this.modeName = modeName;
		this.folder = folder;
        this.jsonName = jsonName;
    }

	public static Mode fromString(String value) {
		if (value == null) return null;

		for (Mode m : values()) {
			if (value.equalsIgnoreCase(m.modeName) ||
					value.equalsIgnoreCase(m.jsonName)) {
				return m;
			}
		}
		throw new IllegalArgumentException("Invalid Mode: " + value);
	}

	public static Mode getEnumFromModeName(String modeName) {
		return EnumSet.allOf(Mode.class)
				.stream()
				.filter(Objects::nonNull)
				.filter(mode->mode.getModeName()!=null)
				.filter(mode -> mode.getModeName().equals(modeName))
				.findFirst()
				.orElseThrow(() -> new IllegalStateException(String.format("Unsupported mode %s.", modeName)));
	}

	public static Mode getEnumFromJsonName(String modeName) {
		if (modeName == null){
			return null;
		}
		for (Mode mode : Mode.values()) {
			if (modeName.equals(mode.getJsonName())) {
				return mode;
			}
		}
		return null;
	}

}
