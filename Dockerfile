# Java 21
FROM gitlab-registry.insee.fr:443/kubernetes/images/run/jre:21.0.1_12-jre-jammy-rootless
RUN export VERSION_KRAFTWERK=$(mvn -f pom.xml help:evaluate -Dexpression=project.version -q -DforceStdout)

COPY --chown=$JAVA_USER:$JAVA_USER --exclude=*-app-to-import.jar Kraftwerk/kraftwerk-api/target/kraftwerk-api-$VERSION_KRAFTWERK.jar kraftwerk.jar

EXPOSE 8080

ENTRYPOINT ["java","-jar","/kraftwerk.jar"]