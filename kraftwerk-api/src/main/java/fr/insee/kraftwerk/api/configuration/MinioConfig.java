package fr.insee.kraftwerk.api.configuration;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "fr.insee.postcollecte.minio")
@Getter
public class MinioConfig {
    private String endpoint;
    private String accessKey;
    private String secretKey;
    private boolean enable;

    private String bucketName;
}
