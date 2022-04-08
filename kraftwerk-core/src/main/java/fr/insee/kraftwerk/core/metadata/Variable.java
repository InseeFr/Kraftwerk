package fr.insee.kraftwerk.core.metadata;

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

	public Variable(String name, Group group, VariableType type) {
		this.name = name.toUpperCase();
		this.group = group;
		this.type = type;
	}

	public Variable(String name, Group group, VariableType type, String length) {
		this.name = name.toUpperCase();
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
