package fr.insee.kraftwerk.core.metadata;

import java.util.Date;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Enum class for variable types.
 *
 *  */
@Slf4j
public enum VariableType {
	STRING ("character","character"),
	INTEGER ("integer","integer"),
	NUMBER ("number","numeric"),
	BOOLEAN ("logical","logical"),
	DATE ("Date","Date");
	
	@Getter
	private String dataTableType;
	@Getter
	private String formatR;

	VariableType(String dataTableType,String formatR) {
		this.dataTableType =dataTableType;
		this.formatR=formatR;
	}

	public static VariableType getTypeFromJavaClass(Class<?> clazz){
		// memo: https://www.w3schools.com/java/java_data_types.asp
		if(clazz.isAssignableFrom(Float.class)
				|| clazz.isAssignableFrom(Double.class)){
			return NUMBER;
		} else if (clazz.isAssignableFrom(Integer.class)
				|| clazz.isAssignableFrom(Long.class)
				|| clazz.isAssignableFrom(Short.class)){
			return INTEGER;
		} else if (clazz.isAssignableFrom(String.class)
				|| clazz.isAssignableFrom(Character.class)){
			return STRING;
		} else if (clazz.isAssignableFrom(Boolean.class)){
			return BOOLEAN;
		} else if (clazz.isAssignableFrom(Date.class)){
			return DATE;
		} else {
			log.warn(String.format("Unrecognized type for class %s ", clazz));
			return null;
		}
	}
	
}
