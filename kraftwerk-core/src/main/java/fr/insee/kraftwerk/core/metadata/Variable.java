package fr.insee.kraftwerk.core.metadata;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * Object class to represent a variable.
 *
 */
@Slf4j
public class Variable {

	/** Variable name. */
	@Getter
	protected String name;

	/** Group reference */
	@Getter
	protected Group group;

	/** Variable type from the enum class (STRING, INTEGER, DATE, ...) */
	@Getter
	protected VariableType type;

	/** Format for SAS script import */
	@Getter
	protected String sasFormat;

	/** Maximum length received in input for the variable. */
	@Getter
	@Setter
	protected int maxLengthData;

	/** Name of the item used to collect the answer. */
	@Getter
	@Setter
	protected String questionItemName;

	/** Identifies if the variable is part a question grid */
	@Getter
	@Setter
	protected boolean isInQuestionGrid;

	public Variable(String name, Group group, VariableType type) {
		this.name = name;
		this.group = group;
		this.type = type;
	}

	public Variable(String name, Group group, VariableType type, String sasFormat) {
		this.name = name;
		this.group = group;
		this.type = type;
		if (!"".equals(sasFormat)) this.sasFormat = sasFormat;
	}

	public String getGroupName() {
		return group.getName();
	}

	public int getExpectedLength(){
		if (this.sasFormat != null && this.sasFormat.contains(".")){
			String[] sasFormatPart = this.sasFormat.split("\\.");
			return Integer.parseInt(sasFormatPart[0]);
		}
		if (this.sasFormat != null){
			try {
				return Integer.parseInt(this.sasFormat);
			}
			catch (NumberFormatException e){
				log.error("Variable {} expected length is not a number : {} ", name, sasFormat);
				return 1;
			}
		}
		// Not sure about that return
		return 1;
	}

}
