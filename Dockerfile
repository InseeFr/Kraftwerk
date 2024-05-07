# Java 21
FROM gitlab-registry.insee.fr:443/kubernetes/images/run/jre:21.0.1_12-jre-jammy-rootless
ARG VERSION_KRAFTWERK
COPY --chown=$JAVA_USER:$JAVA_USER Kraftwerk/kraftwerk-api/target/kraftwerk-api-$VERSION_KRAFTWERK.jar kraftwerk.jar

EXPOSE 8080

ENTRYPOINT ["java","-jar","/kraftwerk.jar"]