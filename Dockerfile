FROM openjdk:8-jdk
VOLUME /tmp
#COPY ./target/docker-demo-1.0.0.jar app.jar
COPY docker-demo-1.0.0.jar app.jar
RUN bash -c 'touch /app.jar'
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]
EXPOSE 8090