# Scala Kafka Lab

## Goal

When integrating multiple complex technologies, especially in a secondary language, it's often very helpful to see full implementations and even their runtime behavior. To that end, this is a collection of self-contained Kafka clients in Scala intended for local experimentation.

## Clients by Technology Stack

### ZIO

* **zio-producer**
* **zio-consumer**

## Requirements

* A full local installation of [Docker Desktop](https://www.docker.com/products/docker-desktop). It includes Docker Compose if you'd like to run services standalone.
* JDK 11+

## Build

All modules and Docker images can be built in one shot with the following:

```shell
$ cd scala-kafka-lab
$ sbt docker:publishLocal
```

You _will_ need to rebuild the images before running any integration tests if changes are made to the clients.

## Standalone Run

Although running the services within your IDE is typically easiest for local experimentation, it's definitely instructive to run them against a standalone Kafka deployment with a web interface.

Confluent provides [all-in-one distributions](https://github.com/confluentinc/cp-all-in-one) of its platform, which are very convenient for getting up and running quickly:

```shell
$ cd cp-all-in-one/cp-all-in-one
$ docker-compose up -d
```

Once all the Confluent Platform services are running and the cluster is healthy, we can start up our own services. For example:

```shell
$ cd scala-kafka-lab
$ docker-compose -f docker-compose.zio.yml up -d
```

This will bring up one `zio-producer` and one `zio-consumer`. The topic will be created automatically by default, and the producer will immediately begin sending messages. You can then visit `localhost:9021` to see the messages and data flow graphs in Confluent Control Center. If you'd like to try multiple consumers in a consumer group, append `--scale zio-consumer=<N>` to the command above.
