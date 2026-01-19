# ====== Etapa 1: build con Maven ======
FROM maven:3.9.9-eclipse-temurin-23 AS build

WORKDIR /app

# Copiamos pom y resolvemos dependencias (cachea bien)
COPY pom.xml .
RUN mvn -B dependency:go-offline

# Copiamos el código
COPY src ./src

# Compilamos y empaquetamos (sin tests para acelerar)
RUN mvn -B -DskipTests clean package

# ====== Etapa 2: imagen liviana para correr la app ======
FROM eclipse-temurin:23-jre

WORKDIR /app

# Copiamos el jar generado en la etapa anterior
ARG JAR_FILE=target/*.jar
COPY --from=build /app/${JAR_FILE} app.jar

# Puerto por defecto de Spring Boot
EXPOSE 8080

# Variables opcionales (ej: timezone)
ENV TZ=America/Argentina/Buenos_Aires

# Levantamos la app
ENTRYPOINT ["java","-jar","app.jar"]
