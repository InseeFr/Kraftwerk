package fr.insee.kraftwerk.core.metadata;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

/**
 * Object class to represent a group of variables.
 */
@Log4j2
@Getter
@Setter
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


    private boolean hasParent() {
        return parentName != null && !(parentName.equals(""));
    }
    public boolean isRoot() {
        return !hasParent();
    }

}
