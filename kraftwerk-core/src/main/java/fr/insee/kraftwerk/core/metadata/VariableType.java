package fr.insee.kraftwerk.core.metadata;

import java.util.Date;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;

/**
 * Enum class for variable types.
 *
 *  */
@Log4j2
@Getter
public enum VariableType {
	STRING ("character","character", "STRING"),
	INTEGER ("integer","integer", "INTEGER"),
	NUMBER ("number","numeric", "NUMBER"),
	BOOLEAN ("logical","logical", "BOOLEAN"),
	DATE ("Date","Date", "STRING");
	
	private String dataTableType;
	private String formatR;
	private String vtlType;	
	
	VariableType(String dataTableType,String formatR, String vtlType) {
		this.dataTableType =dataTableType;
		this.formatR=formatR;
		this.vtlType=vtlType;
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
