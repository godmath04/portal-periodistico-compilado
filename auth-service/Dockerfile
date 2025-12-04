# ETAPA 1: Construcción (Build)
# Usamos una imagen de Maven que tenga Java 21
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

# Copiamos el archivo de configuración
COPY pom.xml .

# Copiamos el código fuente
COPY src ./src

# Compilamos generando el .jar (saltando tests para agilizar)
RUN mvn clean package -DskipTests

# ETAPA 2: Ejecución (Runtime)
# Usamos una imagen ligera de Java 21 para correr la app
FROM openjdk:21-jdk-slim
WORKDIR /app

# Copiamos el .jar generado en la etapa anterior
COPY --from=build /app/target/*.jar app.jar

# Comando de inicio
ENTRYPOINT ["java", "-jar", "app.jar"]