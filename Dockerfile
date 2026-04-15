# Java 25
FROM gitlab-registry.insee.fr/kubernetes/images/run/java:25.0.1_8-jre-rootless
ARG VERSION_APPLICATION
COPY --chown=$JAVA_USER:$JAVA_USER Kraftwerk/kraftwerk-api/target/kraftwerk-api-$VERSION_APPLICATION.jar kraftwerk.jar

ENV JAVA_TOOL_OPTIONS \
    -XX:+UseZGC \
    -Xmx2g

EXPOSE 8080

#Docker run without additionnal params to use REST API mode, add batch parameters to use batch mode
ENTRYPOINT ["java","-jar","/kraftwerk.jar"]