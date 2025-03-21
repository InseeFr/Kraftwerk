package fr.insee.kraftwerk;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Date;
import java.util.List;
import java.util.Map;

public class TestUtils {

    // JWT claim properties loaded from application properties
    @Value("${fr.insee.kraftwerk.security.token.oidc-claim-role}")
    private String claimRoleDotRoles;
    @Value("${fr.insee.kraftwerk.security.token.oidc-claim-username}")
    private String claimName;

    @Getter
    @Value("${fr.insee.postcollecte.files}")
    private String defaultDirectory;

    /**
     * Generates a mock JWT token with specified roles and username.
     *
     * @param roles List of roles assigned to the user.
     * @param name  Username for the JWT.
     * @return A mock Jwt object.
     */
    public Jwt generateJwt(List<String> roles, String name) {
        Date issuedAt = new Date();
        Date expiresAT = Date.from((new Date()).toInstant().plusSeconds(100));
        var claimRole = claimRoleDotRoles.split("\\.")[0];
        var attributRole = claimRoleDotRoles.split("\\.")[1];
        return new Jwt("token", issuedAt.toInstant(), expiresAT.toInstant(),
                Map.of("alg", "RS256", "typ", "JWT"),
                Map.of(claimRole, Map.of(attributRole, roles),
                        claimName, name
                )
        );
    }
}
