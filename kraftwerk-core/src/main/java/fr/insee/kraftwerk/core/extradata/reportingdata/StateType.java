package fr.insee.kraftwerk.core.extradata.reportingdata;

import java.util.HashMap;
import java.util.Map;

import lombok.Getter;

public enum StateType {
  STATE01("NVM", "UE non visible gestionnaire"),
  STATE02("NNS", "Non affecté, non commencé"),
  STATE03("ANV", "Affectée, non visible enquêteur"),
  STATE04("VIN", "Visible enquêteur et non cliquable"),
  STATE05("VIC", "Visible enquêteur et cliquable"),
  STATE06("PRC", "Prise de contact en préparation"),
  STATE07("AOC", "Au moins un contact"),
  STATE08("APS", "RDV pris (qui inclut enquête acceptée)"),
  STATE09("INS", "Questionnaire démarré"),
  STATE10("WFT", "UE en attente de transmission"),
  STATE11("WFS", "UE en attente de synchronisation"),
  STATE12("TBR", "UE à relire DEM"),
  STATE13("FIN", "UE finalisée"),
  STATE14("QNA", "Questionnaire non accessible enquêteur"),
  STATE15("CLO", "UE close"),
  STATE16("NVA", "UE non visible gestionnaire"),
  STATE17("INITLA", "INITLA"),
  STATE18("PND", "PND"),
  STATE19("DECHET", "DECHET"),
  STATE20("PARTIELINT", "PARTIELINT"),
  STATE21("HC", "HC"),
  STATE22("VALPAP", "VALPAP"),
  STATE23("VALINT", "VALINT"),
  STATE24("REFUS", "REFUS"),
  STATE25("RELANCE", "RELANCE");
  
	@Getter
  private final String key;
	@Getter
  private final String value;
  
  private static Map<String, String> valueToTextMapping  = new HashMap<>();
  
  StateType(String key, String value) {
    this.key = key;
    this.value = value;
  }
  
	static {
		// Populate out lookup when enum is created
		for (StateType e : StateType.values()) {
			valueToTextMapping.put(e.getKey(), e.getValue());
		}
	}
  
  public static String getStateType(String key) { 
    return valueToTextMapping.get(key);
  }
}
