package fr.insee.kraftwerk.core.metadata;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

/**
 * Object class to represent a group of variables.
 */
@Slf4j
public class Group {

    protected String name;
    protected String parentName;

    /** Protected constructor designed to create the root group.
     * Root group is (only) created in VariablesMap constructor. */
    protected Group(String name) {
        this.name = name;
        this.parentName = null;
    }

    public Group(@NonNull String name, @NonNull String parentName){
        this.name = name;
        this.parentName = parentName;
        if (parentName.equals("")) {
            String msg = "Parent group name must be provided, \"\" name is invalid.";
            log.debug(msg);
            throw new IllegalArgumentException(msg);
        }
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getParentName() {
        return parentName;
    }
    public void setParentName(String parentName) {
        this.parentName = parentName;
    }

    public boolean hasParent() {
        return parentName != null && !(parentName.equals(""));
    }
    public boolean isRoot() {
        return !hasParent();
    }

}
