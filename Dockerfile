FROM openjdk:11-jdk-slim
ARG jar
COPY $jar /opt/censusperformancetest.jar
ENV JAVA_OPTS=""
ENTRYPOINT [ "java",  "-jar", "/opt/censusperformancetest.jar" ]