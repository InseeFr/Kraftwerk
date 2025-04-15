package fr.insee.kraftwerk.api.configuration.security;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
@Slf4j
public class RoleConfiguration {

    //Mapping des claims du jeton sur les roles applicatifs
    @Value("#{'${app.role.admin.claims}'.split(',')}")
    private List<String> adminClaims;
    @Value("#{'${app.role.user.claims}'.split(',')}")
    private List<String> userClaims;
    @Value("#{'${app.role.scheduler.claims}'.split(',')}")
    private List<String> schedulerClaims;

    @Getter
    private Map<String, List<String>> rolesByClaim;

    //Defines a role hierarchy
    //ADMIN implies USER   role too
    //USER  implies READER role too
    //so an admin has 3 roles: ADMIN/USER/READER
    @Bean
    static RoleHierarchy roleHierarchy() {
        return RoleHierarchyImpl.withDefaultRolePrefix()
                .role(ApplicationRole.ADMIN.toString()).implies(ApplicationRole.USER.toString())
                .role(ApplicationRole.ADMIN.toString()).implies(ApplicationRole.SCHEDULER.toString())
                .build();
    }

    // and, if using pre-post method security also add
    @Bean
    static MethodSecurityExpressionHandler methodSecurityExpressionHandler(RoleHierarchy roleHierarchy) {
        DefaultMethodSecurityExpressionHandler expressionHandler = new DefaultMethodSecurityExpressionHandler();
        expressionHandler.setRoleHierarchy(roleHierarchy);
        return expressionHandler;
    }

    @PostConstruct
    public void initialization() {

        rolesByClaim = new HashMap<>();

        // Add claims for the ADMIN role
        adminClaims.forEach(claim -> rolesByClaim
                .computeIfAbsent(claim, k -> new ArrayList<>())
                .add(String.valueOf(ApplicationRole.ADMIN)));

        // Add claims for the USER role
        userClaims.forEach(claim -> rolesByClaim
                .computeIfAbsent(claim, k -> new ArrayList<>())
                .add(String.valueOf(ApplicationRole.USER)));

        // Add claims for the SCHEDULER role
        schedulerClaims.forEach(claim -> rolesByClaim
                .computeIfAbsent(claim, k -> new ArrayList<>())
                .add(String.valueOf(ApplicationRole.SCHEDULER)));

        log.info("Roles configuration : {}", rolesByClaim);
    }



}

