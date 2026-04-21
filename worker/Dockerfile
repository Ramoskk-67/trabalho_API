FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

COPY Main.java worker/Main.java

RUN javac worker/Main.java

COPY entrypoint.sh /entrypoint.sh
RUN chmod +x /entrypoint.sh

ENTRYPOINT ["/entrypoint.sh"]