package stubs;

import fr.insee.kraftwerk.api.configuration.ConfigProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ConfigStub extends ConfigProperties {
    private String genesisUrl;

    private String defaultDirectory;

    //Auth
    private String authServerUrl;
    private String realm;
    private String oidcClaimRole;
    private String oidcClaimUsername;
    private String[] whiteList;
}
