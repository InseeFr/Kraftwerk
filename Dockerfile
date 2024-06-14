# Java 21
FROM gitlab-registry.insee.fr:443/kubernetes/images/run/jre:21.0.1_12-jre-jammy-rootless
COPY --chown=$JAVA_USER:$JAVA_USER Kraftwerk/kraftwerk-api/target/kraftwerk-api-*app-to-import.jar kraftwerk.jar

EXPOSE 8080

#Docker run without additionnal params to use REST API mode, add batch parameters to use batch mode
ENTRYPOINT ["java","-jar","/kraftwerk.jar"]