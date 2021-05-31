# Shipwars Streams

Java application that uses [Quarkus](https://quarkus.io/) and [Apache Kafka Streams](https://kafka.apache.org/documentation/streams)
to analyse events sent to Kafka by the [Shipwars Game Server](https://github.com/redhat-gamedev/shipwars-game-server).

## Streams Topology

* _shot-distribution-aggregator_ - Aggregates the distribution of shots for given game generations (each deploy is a new generation).

The _shot-distribution-aggregator_ exposes a single endpoint:

* `/shot-distribution` - Returns the shot distribution for all game generations.

## Building

```bash
mvn clean install
```

## Running Locally

Refer to the Docker/Podman guides in [Shipwars Deployment](https://github.com/redhat-gamedev/shipwars-deployment).

## Running for Development

Follow the guide above to run the associated services using Docker/Podman, but
remove this service from the Dockerfile, i.e start all of the services using
`docker-compose up` except this one.

Once the other services have started, you can start this one using:

```bash
QUARKUS_KAFKA_STREAMS_BOOTSTRAP_SERVERS=localhost:9094 \
QUARKUS_HTTP_PORT=8585 \
./mvnw quarkus:dev -f shot-distribution-aggregator/pom.xml
```

## Scaling

Kafka Streams pipelines can be scaled out, i.e. the load can be distributed
amongst multiple application instances running the same pipeline.

This particular example has not been designed to support this functionality.
