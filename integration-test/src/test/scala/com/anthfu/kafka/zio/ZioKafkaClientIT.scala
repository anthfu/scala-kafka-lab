package com.anthfu.kafka.zio

import com.dimafeng.testcontainers.{ForAllTestContainer, GenericContainer, KafkaContainer, MultipleContainers}
import org.scalatest.flatspec.AnyFlatSpec
import org.slf4j.LoggerFactory
import org.testcontainers.containers.Network
import org.testcontainers.containers.output.Slf4jLogConsumer
import org.testcontainers.containers.startupcheck.IndefiniteWaitOneShotStartupCheckStrategy

class ZioKafkaClientIT extends AnyFlatSpec with ForAllTestContainer {
  private val logger = LoggerFactory.getLogger(getClass)
  private val network = Network.newNetwork()

  private lazy val kafka = KafkaContainer("6.1.1").configure { c =>
    c.withNetwork(network)
    c.withNetworkAliases("kafka")
  }

  private lazy val consumer = ConsumerContainer().configure { c =>
    c.withNetwork(network)
    c.withEnv("BOOTSTRAP_SERVER", "kafka:9092")
    c.withEnv("GROUP_ID", "zio-consumers")
    c.withEnv("TOPIC", "zio-stream")
    c.withLogConsumer(new Slf4jLogConsumer(logger).withPrefix("zio-consumer"))
    c.dependsOn(kafka)
  }

  private lazy val producer = ProducerContainer().configure { c =>
    c.withNetwork(network)
    c.withEnv("BOOTSTRAP_SERVER", "kafka:9092")
    c.withEnv("TOPIC", "zio-stream")
    c.withLogConsumer(new Slf4jLogConsumer(logger).withPrefix("zio-producer"))
    c.withStartupCheckStrategy(new IndefiniteWaitOneShotStartupCheckStrategy())
    c.dependsOn(kafka)
  }

  override val container: MultipleContainers =
    MultipleContainers(kafka, consumer, producer)

  "producers and consumers" should "send and receive messages" in {
    assert(producer.logs.contains("value: 1000"))
    assert(consumer.logs.contains("value: 1000"))
  }
}

class ConsumerContainer(underlying: GenericContainer) extends GenericContainer(underlying)
object ConsumerContainer {
  def apply() = new ConsumerContainer(GenericContainer(
    dockerImage = "zio-consumer:1.0.0-SNAPSHOT",
    exposedPorts = Seq(8080)
  ))
}

class ProducerContainer(underlying: GenericContainer) extends GenericContainer(underlying)
object ProducerContainer {
  def apply() = new ProducerContainer(GenericContainer(
    dockerImage = "zio-producer:1.0.0-SNAPSHOT",
    exposedPorts = Seq(8080)
  ))
}
