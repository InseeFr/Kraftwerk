package fr.insee.kraftwerk.api.configuration.security;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@ConditionalOnProperty(name = "fr.insee.kraftwerk.authentication", havingValue = "NONE")
@Configuration
@EnableWebSecurity
@Slf4j
@AllArgsConstructor
public class NoAuthConfiguration {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable).authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        return http.build();
    }
}
