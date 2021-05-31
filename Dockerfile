FROM maven:3.8.1-adoptopenjdk-11

WORKDIR /usr/src/app

CMD ["mvn", "quarkus:dev", "-f", "shot-distribution-aggregator/pom.xml"]
