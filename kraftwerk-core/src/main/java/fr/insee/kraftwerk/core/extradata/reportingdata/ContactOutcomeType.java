package fr.insee.kraftwerk.core.extradata.reportingdata;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

public enum ContactOutcomeType {
  CONTACT_OUTCOME01("INA", "Enquête acceptée"),
  CONTACT_OUTCOME02("REF", "Refus"),
  CONTACT_OUTCOME03("IMP", "Impossible à joindre"),
  CONTACT_OUTCOME04("UCD", "Données de contact inutilisables (tél, mail)"),
  CONTACT_OUTCOME05("UTR", "Incapacité à répondre"),
  CONTACT_OUTCOME06("ALA", "A déjà répondu à une autre enquête de l'Insee depuis moins d'un an"),
  CONTACT_OUTCOME07("ACP", "Absence pendant toute la période de collecte"),
  CONTACT_OUTCOME08("DCD", "Enquêté décédé"),
  CONTACT_OUTCOME09("NUH", "Logement ayant perdu son usage d'habitation"),
  CONTACT_OUTCOME10("NER", "Non enquêté pour cause exceptionnelle");

  @Getter
  private final String key;
  
  private final String value;
  
  private static Map<String, String> valueToTextMapping;
  
  ContactOutcomeType(String key, String value) {
    this.key = key;
    this.value = value;
  }
  
  private static void initMapping() {
    valueToTextMapping = new HashMap<>();
    byte b;
    int i;
    ContactOutcomeType[] arrayOfContactOutcomeType = values();
    for (i = arrayOfContactOutcomeType.length, b = 0; b < i; ) {
      ContactOutcomeType s = arrayOfContactOutcomeType[b];
      valueToTextMapping.put(s.key, s.value);
      b++;
    } 
  }
  
  public static String getOutcomeType(String key) {
    if (valueToTextMapping == null)
      initMapping(); 
    return valueToTextMapping.get(key);
  }
}
