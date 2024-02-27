package fr.insee.kraftwerk.core.dataprocessing;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;

@Getter
public enum TCMSequenceEnum {
    TCM_THLHAB (TCMModuleEnum.TCM_THL_DET,TCMModuleEnum.TCM_THL_SIMPLE),
    TCM_THL_LDVIE(TCMModuleEnum.TCM_THL_LDV),
    TCM_ACT(TCMModuleEnum.TCM_ACT_PRINC),
    TCM_ACT_ANT(TCMModuleEnum.TCM_ACT_ANTE),
    TCM_ACTI_BIS(TCMModuleEnum.TCM_ACT_BIS),
    TCM_FORM_FFCOUR(TCMModuleEnum.TCM_F_FORM),
    TCM_SANTE(TCMModuleEnum.TCM_SANTE_EU),
    TCM_SANTE_GALI(TCMModuleEnum.TCM_SANTE_DIFF),
    TCM_SANT_DET(TCMModuleEnum.TCM_SANTE_DET),
    TCM_ORIGINES(TCMModuleEnum.TCM_OR),
    TCM_FAMILLE(TCMModuleEnum.TCM_FAM),
    TCM_LGT(TCMModuleEnum.TCM_LGT_ARCHI),
    TCM_LGT_LPR(TCMModuleEnum.TCM_LGT_LOCPR) ;
    
	private List<TCMModuleEnum> tcmModules;
	 
	private TCMSequenceEnum(TCMModuleEnum... tcmModules){
		this.tcmModules = Arrays.asList(tcmModules);
	}
}
