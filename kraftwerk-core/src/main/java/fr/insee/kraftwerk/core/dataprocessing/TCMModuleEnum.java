package fr.insee.kraftwerk.core.dataprocessing;

import java.util.ArrayList;
import java.util.List;

public enum TCMModuleEnum {
    TCM_THL_DET
    ,TCM_THL_SIMPLE
    ,TCM_THL_LDV
    ,TCM_ACT_PRINC
    ,TCM_ACT_ANTE
    ,TCM_ACT_BIS
    ,TCM_F_FORM
    ,TCM_F_DIPL_DET
    ,TCM_F_DIPL_SIMP
    ,TCM_SANTE_EU
    ,TCM_SANTE_DIFF
    ,TCM_SANTE_DET
    ,TCM_OR
    ,TCM_FAM
    ,TCM_LGT_ARCHI
    ,TCM_LGT_LOCPR;

    public static List<TCMModuleEnum> getModules(TCMSequenceEnum tcmSequenceEnum){
        List<TCMModuleEnum> tcmModuleEnumList = new ArrayList<>();
        switch (tcmSequenceEnum){
            case TCM_THLHAB : tcmModuleEnumList.add(TCM_THL_DET); tcmModuleEnumList.add(TCM_THL_SIMPLE);
            break;
            case TCM_THL_LDVIE: tcmModuleEnumList.add(TCM_THL_LDV);
            break;
            case TCM_ACT: tcmModuleEnumList.add(TCM_ACT_PRINC);
            break;
            case TCM_ACT_ANT: tcmModuleEnumList.add(TCM_ACT_ANTE);
            break;
            case TCM_ACTI_BIS: tcmModuleEnumList.add(TCM_ACT_BIS);
            break;
            case TCM_FORM_FFCOUR: tcmModuleEnumList.add(TCM_F_FORM);
            break;
            case TCM_SANTE: tcmModuleEnumList.add(TCM_SANTE_EU);
            break;
            case TCM_SANTE_GALI: tcmModuleEnumList.add(TCM_SANTE_DIFF);
            break;
            case TCM_SANT_DET: tcmModuleEnumList.add(TCM_SANTE_DET);
            break;
            case TCM_ORIGINES: tcmModuleEnumList.add(TCM_OR);
            break;
            case TCM_FAMILLE: tcmModuleEnumList.add(TCM_FAM);
            break;
            case TCM_LGT: tcmModuleEnumList.add(TCM_LGT_ARCHI);
            break;
            case TCM_LGT_LPR: tcmModuleEnumList.add(TCM_LGT_LOCPR);
            break;
        }
        return tcmModuleEnumList;
    }
}