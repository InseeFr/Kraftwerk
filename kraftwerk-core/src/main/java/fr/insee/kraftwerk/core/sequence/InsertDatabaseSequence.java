package fr.insee.kraftwerk.core.sequence;

import fr.insee.kraftwerk.core.utils.SqlUtils;
import fr.insee.kraftwerk.core.vtl.VtlBindings;
import lombok.NoArgsConstructor;

import java.sql.Statement;

@NoArgsConstructor
public class InsertDatabaseSequence {
    public void insertDatabaseProcessing(VtlBindings vtlBindings, Statement database){
        SqlUtils.convertVtlBindingsIntoSqlDatabase(vtlBindings, database);
    }
}
