FROM eclipse-temurin:17-jre
WORKDIR /app
COPY target/formula-base-0.1.0-SNAPSHOT.jar app.jar
ENV PORT=8080
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
