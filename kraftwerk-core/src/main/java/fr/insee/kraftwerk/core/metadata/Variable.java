package fr.insee.kraftwerk.core.metadata;

import lombok.Getter;
import lombok.Setter;

/**
 * Object class to represent a variable.
 *
 */
public class Variable {

	/** Variable name. */
	protected String name;

	/** Group reference */
	protected Group group;

	/** Variable type from the enum class (STRING, INTEGER, DATE, ...) */
	protected VariableType type;

	/** Variable length. */
	protected String length;

	/** Name of the item used to collect the answer. */
	@Getter
	@Setter
	protected String questionItemName;

	public Variable(String name, Group group, VariableType type) {
		this.name = name;
		this.group = group;
		this.type = type;
	}

	public Variable(String name, Group group, VariableType type, String length) {
		this.name = name;
		this.group = group;
		this.type = type;
		this.length = length;
	}

	public String getName() {
		return name;
	}
	
	public String getGroupName() {
		return group.getName();
	}
	public Group getGroup() {
		return group;
	}
	public VariableType getType() {
		return type;
	}
	public String getLength() {
		return length;
	}

}
