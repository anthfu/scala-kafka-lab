package com.anthfu.kafka.zio

import com.dimafeng.testcontainers.lifecycle.and
import com.dimafeng.testcontainers.munit.TestContainersForAll
import com.dimafeng.testcontainers.{GenericContainer, KafkaContainer}
import munit.FunSuite
import org.slf4j.LoggerFactory
import org.testcontainers.containers.Network
import org.testcontainers.containers.output.Slf4jLogConsumer
import org.testcontainers.containers.wait.strategy.Wait

class ZioKafkaClientIT extends FunSuite with TestContainersForAll {
  private val logger = LoggerFactory.getLogger(getClass)

  override type Containers = KafkaContainer and ConsumerContainer and ProducerContainer

  override def startContainers(): Containers = {
    val network = Network.newNetwork()

    val kafkaContainer = KafkaContainer("6.2.0").configure { c =>
      c.withNetwork(network)
      c.withNetworkAliases("kafka")
    }

    val consumerContainer = ConsumerContainer().configure { c =>
      c.withNetwork(network)
      c.withLogConsumer(new Slf4jLogConsumer(logger).withPrefix("zio-consumer"))
      c.dependsOn(kafkaContainer)
    }

    val producerContainer = ProducerContainer().configure { c =>
      c.withNetwork(network)
      c.withLogConsumer(new Slf4jLogConsumer(logger).withPrefix("zio-producer"))
      c.dependsOn(kafkaContainer)
    }

    kafkaContainer.start()
    consumerContainer.start()
    producerContainer.start()

    kafkaContainer and consumerContainer and producerContainer
  }

  test("Send and receive messages") {
    withContainers { case _ and consumerContainer and producerContainer =>
      assert(producerContainer.logs.contains("value: 1000"))
      assert(consumerContainer.logs.contains("value: 1000"))
    }
  }
}

class ConsumerContainer(underlying: GenericContainer) extends GenericContainer(underlying)
object ConsumerContainer {
  def apply() = new ConsumerContainer(GenericContainer(
    dockerImage = "zio-consumer:1.0.0-SNAPSHOT",
    exposedPorts = Seq(8080),
    waitStrategy = Wait.forHttp("/")
  ))
}

class ProducerContainer(underlying: GenericContainer) extends GenericContainer(underlying)
object ProducerContainer {
  def apply() = new ProducerContainer(GenericContainer(
    dockerImage = "zio-producer:1.0.0-SNAPSHOT",
    exposedPorts = Seq(8080),
    waitStrategy = Wait.forHttp("/")
  ))
}
