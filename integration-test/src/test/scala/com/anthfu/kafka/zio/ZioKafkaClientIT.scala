package com.anthfu.kafka.zio

import org.scalatest.BeforeAndAfterAll
import org.scalatest.flatspec.AnyFlatSpec
import org.slf4j.LoggerFactory
import org.testcontainers.containers.output.Slf4jLogConsumer
import org.testcontainers.containers.startupcheck.IndefiniteWaitOneShotStartupCheckStrategy
import org.testcontainers.containers.{GenericContainer, KafkaContainer, Network}
import org.testcontainers.utility.DockerImageName

class ZioKafkaClientIT extends AnyFlatSpec with BeforeAndAfterAll {
  private val kafkaImage = DockerImageName.parse("confluentinc/cp-kafka:6.1.1")
  private val producerImage = DockerImageName.parse("zio-producer:1.0.0-SNAPSHOT")
  private val consumerImage = DockerImageName.parse("zio-consumer:1.0.0-SNAPSHOT")

  private val logger = LoggerFactory.getLogger(getClass)
  private val kafkaNetwork = Network.newNetwork()

  private lazy val kafka = new KafkaContainer(kafkaImage)
    .withNetwork(kafkaNetwork)
    .withNetworkAliases("kafka")

  private val consumer = new GenericContainer(consumerImage)
  consumer.withNetwork(kafkaNetwork)
  consumer.withEnv("BOOTSTRAP_SERVER", "kafka:9092")
  consumer.withEnv("GROUP_ID", "zio-consumers")
  consumer.withEnv("TOPIC", "zio-stream")
  consumer.withLogConsumer(new Slf4jLogConsumer(logger).withPrefix("zio-consumer"))
  consumer.dependsOn(kafka)

  private val producer = new GenericContainer(producerImage)
  producer.withNetwork(kafkaNetwork)
  producer.withEnv("BOOTSTRAP_SERVER", "kafka:9092")
  producer.withEnv("TOPIC", "zio-stream")
  producer.withLogConsumer(new Slf4jLogConsumer(logger).withPrefix("zio-producer"))
  producer.withStartupCheckStrategy(new IndefiniteWaitOneShotStartupCheckStrategy())
  producer.dependsOn(consumer)

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
    Thread.sleep(30000)
    assert(producer.getLogs.contains("value: 1000"))
    assert(consumer.getLogs.contains("value: 1000"))
  }
}
