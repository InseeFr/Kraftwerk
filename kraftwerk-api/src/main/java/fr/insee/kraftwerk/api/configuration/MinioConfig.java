package fr.insee.kraftwerk.api.configuration;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties
@Getter
public class MinioConfig {
    @Value("${fr.insee.postcollecte.minio.endpoint}")
    private String endpoint;

    @Value("${fr.insee.postcollecte.minio.access_key}")
    private String accessKey;

    @Value("${fr.insee.postcollecte.minio.secret_key}")
    private String secretKey;

    @Value("${fr.insee.postcollecte.minio.enable}")
    private boolean enable;

    @Value("${fr.insee.postcollecte.minio.bucket_name}")
    private String bucketName;
}
