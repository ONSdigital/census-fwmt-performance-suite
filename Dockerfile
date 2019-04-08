FROM openjdk:11-jdk-slim
ARG jar
RUN groupadd -g 997 censusperformancetest && \
    useradd -r -u 997 -g censusperformancetest censusperformancetest
USER censusperformancetest
COPY $jar /opt/censusperformancetest.jar
ENV JAVA_OPTS=""
ENTRYPOINT [ "java",  "-jar", "/opt/censusperformancetest.jar" ]