FROM jelastic/maven:3.9.5-openjdk-21 AS maven_build

LABEL "Author"="Ömer Rıza Bahadır"

COPY pom.xml /build/

COPY src /build/src/

WORKDIR /build/

RUN mvn package -Dmaven.test.skip=true

FROM openjdk:21-jdk-slim

WORKDIR /app

COPY --from=MAVEN_BUILD /build/target/rentacar-0.0.1-SNAPSHOT.jar /app/

ENTRYPOINT ["java", "-jar", "rentacar-0.0.1-SNAPSHOT.jar","--spring.profiles.active=dev"]