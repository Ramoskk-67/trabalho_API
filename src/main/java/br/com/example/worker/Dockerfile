# Etapa 1: Compilação
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app
COPY Main.java .
RUN javac Main.java

# Etapa 2: Execução mínima
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/*.class .
COPY entrypoint.sh /entrypoint.sh
RUN chmod +x /entrypoint.sh
ENTRYPOINT ["/entrypoint.sh"]