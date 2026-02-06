#FROM openjdk:17-jdk-slim
#COPY kbo_cert.cer /tmp/kbo_cert.cer

#RUN keytool -import -trustcacerts -keystore $JAVA_HOME/lib/security/cacerts \
#    -storepass changeit -noprompt -alias kbo_official -file /tmp/kbo_cert.cer

#COPY build/libs/*.jar app.jar

#ENTRYPOINT ["java", "-jar", "/app.jar"]