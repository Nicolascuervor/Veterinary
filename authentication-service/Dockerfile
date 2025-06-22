# ======================================================================
# Fase 1: Construcción (si quisieras compilar dentro de Docker)
# Para simplificar, asumiremos que ya has compilado el .jar con 'mvn package'
# ======================================================================


FROM eclipse-temurin:21-jdk-jammy

LABEL maintainer="nicocu1127@gmail.com"

WORKDIR /app


COPY target/authentication-service-0.0.1-SNAPSHOT.jar app.jar


EXPOSE 8082


ENTRYPOINT ["java","-jar","app.jar"]