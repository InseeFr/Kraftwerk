package fr.insee.kraftwerk.core.extradata.reportingdata;

import java.util.HashMap;
import java.util.Map;

public enum ContactAttemptType {
  CONTACT_ATTEMPT01("INA", "Enquête acceptée"),
  CONTACT_ATTEMPT02("APT", "Rendez-vous pris"),
  CONTACT_ATTEMPT03("REF", "Refus"),
  CONTACT_ATTEMPT04("TUN", "Indisponibilité provisoire"),
  CONTACT_ATTEMPT05("NOC", "Pas de contact"),
  CONTACT_ATTEMPT06("MES", "Dépôt d'un message (tél, sms, mail)"),
  CONTACT_ATTEMPT07("UCD", "Données de contact inutilisables (tél, mail)"),
  CONTACT_ATTEMPT08("PUN", "Indisponibilité définitive");
  
  private final String key;
  
  private final String value;
  
  private static Map<String, String> valueToTextMapping;
  
  ContactAttemptType(String key, String value) {
    this.key = key;
    this.value = value;
  }
  
  private static void initMapping() {
    valueToTextMapping = new HashMap<>();
    byte b;
    int i;
    ContactAttemptType[] arrayOfContactAttemptType = values();
    for (i = arrayOfContactAttemptType.length, b = 0; b < i; ) {
      ContactAttemptType s = arrayOfContactAttemptType[b];
      valueToTextMapping.put(s.key, s.value);
      b++;
    } 
  }
  
  public static String getAttemptType(String key) {
    if (valueToTextMapping == null)
      initMapping(); 
    return valueToTextMapping.get(key);
  }
}
