package fr.insee.kraftwerk.core.metadata;

import java.time.Instant;
import java.util.Date;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;

/**
 * Enum class for variable types.
 */
@Log4j2
@Getter
public enum VariableType {
	STRING ("character","character", "STRING", "VARCHAR"),
	INTEGER ("integer","integer", "INTEGER", "BIGINT"),
	NUMBER ("number","numeric", "NUMBER", "DOUBLE"),
	BOOLEAN ("logical","logical", "BOOLEAN", "BOOLEAN"),
	DATE ("Date","Date", "STRING", "DATE");
	
	private final String dataTableType;
	private final String formatR;
	private final String vtlType;
	private final String sqlType;

	
	VariableType(String dataTableType,String formatR, String vtlType, String sqlType) {
		this.dataTableType =dataTableType;
		this.formatR=formatR;
		this.vtlType=vtlType;
		this.sqlType=sqlType;
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
		} else if (clazz.isAssignableFrom(Date.class)||clazz.isAssignableFrom(Instant.class)){
			return DATE;
		} else {
			log.warn(String.format("Unrecognized type for class %s ", clazz));
			return null;
		}
	}
	
}
