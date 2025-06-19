# Usamos una imagen oficial de Java 21 como base, igual que los otros servicios.
FROM eclipse-temurin:21-jdk-jammy

# Etiqueta para describir al autor de la imagen (opcional)
LABEL maintainer="tu-nombre@example.com"

# Creamos un directorio de trabajo dentro del contenedor.
WORKDIR /app


COPY target/authentication-service-0.0.1-SNAPSHOT.jar app.jar


# Exponemos el puerto 8084, que es el que usa este servicio según su 'application.properties'.
EXPOSE 8082

# Comando para ejecutar la aplicación cuando el contenedor se inicie.
ENTRYPOINT ["java","-jar","app.jar"]

