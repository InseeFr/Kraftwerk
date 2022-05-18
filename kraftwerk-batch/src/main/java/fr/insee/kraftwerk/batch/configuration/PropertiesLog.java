package fr.insee.kraftwerk.batch.configuration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.AbstractEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Objects;
import java.util.Set;

@Component
@Slf4j
public class PropertiesLog {

    private final Environment environment;
    private final Set<String> hiddenWords = Set.of("password", "pwd", "jeton", "secret");

    public PropertiesLog(Environment environment) {
        Objects.requireNonNull(environment);
        this.environment=environment;

        log.info("===============================================================================================");
        log.info("                                          Properties                                           ");
        log.info("===============================================================================================");

        ((AbstractEnvironment) environment).getPropertySources().stream()
                .map(propertySource -> {
                    if (propertySource instanceof EnumerablePropertySource) {
                        return ((EnumerablePropertySource<?>)propertySource).getPropertyNames();
                    } else {
                        log.warn(propertySource + " is not an EnumerablePropertySource (cannot be displayed).");
                        return new String[] {};
                    }
                }
                )
                .flatMap(Arrays::stream)
                .distinct()
                .forEach(key->log.info(key+" = "+ displayValueWithPasswordMask(key)));

        log.info("===============================================================================================\n");
    }

    private Object displayValueWithPasswordMask(String key) {
        if (key == null) {
            return "null";
        } else {
            if (hiddenWords.stream().anyMatch(key::contains)) {
                return "******";
            } else {
                return environment.getProperty(key);
            }
        }
    }

}
