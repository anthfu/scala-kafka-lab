package com.anthfu.kafka.zio

import org.scalatest.BeforeAndAfterAll
import org.scalatest.flatspec.AnyFlatSpec
import org.slf4j.LoggerFactory
import org.testcontainers.containers.output.Slf4jLogConsumer
import org.testcontainers.containers.startupcheck.IndefiniteWaitOneShotStartupCheckStrategy
import org.testcontainers.containers.{GenericContainer, KafkaContainer, Network}
import org.testcontainers.utility.DockerImageName

import java.util.concurrent.TimeUnit

class ZioKafkaClientIT extends AnyFlatSpec with BeforeAndAfterAll {
  private val kafkaImage = DockerImageName.parse("confluentinc/cp-kafka:6.1.1")
  private val producerImage = DockerImageName.parse("zio-producer:1.0.0-SNAPSHOT")
  private val consumerImage = DockerImageName.parse("zio-consumer:1.0.0-SNAPSHOT")

  private val logger = LoggerFactory.getLogger(getClass)
  private val kafkaNetwork = Network.newNetwork()

  class ProducerContainer extends GenericContainer[ProducerContainer](producerImage)
  class ConsumerContainer extends GenericContainer[ConsumerContainer](consumerImage)

  private val kafka = new KafkaContainer(kafkaImage)
    .withNetwork(kafkaNetwork)
    .withNetworkAliases("kafka")

  private val consumer = new ConsumerContainer()
    .withNetwork(kafkaNetwork)
    .withEnv("BOOTSTRAP_SERVER", "kafka:9092")
    .withEnv("GROUP_ID", "zio-consumers")
    .withEnv("TOPIC", "zio-stream")
    .withLogConsumer(new Slf4jLogConsumer(logger).withPrefix("zio-consumer"))
    .dependsOn(kafka)

  private val producer = new ProducerContainer()
    .withNetwork(kafkaNetwork)
    .withEnv("BOOTSTRAP_SERVER", "kafka:9092")
    .withEnv("TOPIC", "zio-stream")
    .withLogConsumer(new Slf4jLogConsumer(logger).withPrefix("zio-producer"))
    .withStartupCheckStrategy(new IndefiniteWaitOneShotStartupCheckStrategy())
    .dependsOn(consumer)

  override protected def beforeAll(): Unit = {
    kafka.start()
    consumer.start()
    producer.start()
  }

  override protected def afterAll(): Unit = {
    producer.stop()
    consumer.stop()
    kafka.stop()
  }

  "producers and consumers" should "send and receive messages" in {
    TimeUnit.SECONDS.sleep(30)
    assert(producer.getLogs.contains("value: 1000"))
    assert(consumer.getLogs.contains("value: 1000"))
  }
}
