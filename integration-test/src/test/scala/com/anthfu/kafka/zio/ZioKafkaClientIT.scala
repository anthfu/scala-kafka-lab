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

    val kafkaContainer = KafkaContainer.Def("6.2.0").start()
    kafkaContainer.container.withNetwork(network)
    kafkaContainer.container.withNetworkAliases("kafka")

    val consumerContainer = ConsumerContainer.Def().start()
    consumerContainer.container.withNetwork(network)
    consumerContainer.container.withLogConsumer(new Slf4jLogConsumer(logger).withPrefix("zio-consumer"))
    consumerContainer.container.dependsOn(kafkaContainer)

    val producerContainer = ProducerContainer.Def().start()
    producerContainer.container.withNetwork(network)
    producerContainer.container.withLogConsumer(new Slf4jLogConsumer(logger).withPrefix("zio-producer"))
    producerContainer.container.dependsOn(consumerContainer)

    kafkaContainer and consumerContainer and producerContainer
  }

  test("Send and receive messages") {
    withContainers { case _ and consumerContainer and producerContainer =>
      assert(producerContainer.container.getLogs.contains("value: 1000"))
      assert(consumerContainer.container.getLogs.contains("value: 1000"))
    }
  }
}

class ConsumerContainer(underlying: GenericContainer) extends GenericContainer(underlying)
object ConsumerContainer {
  case class Def() extends GenericContainer.Def[ConsumerContainer](
    new ConsumerContainer(GenericContainer(
      dockerImage = "zio-consumer:1.0.0-SNAPSHOT",
      exposedPorts = Seq(8080),
      waitStrategy = Wait.forHttp("/")
    ))
  )
}

class ProducerContainer(underlying: GenericContainer) extends GenericContainer(underlying)
object ProducerContainer {
  case class Def() extends GenericContainer.Def[ProducerContainer](
    new ProducerContainer(GenericContainer(
      dockerImage = "zio-producer:1.0.0-SNAPSHOT",
      exposedPorts = Seq(8080),
      waitStrategy = Wait.forHttp("/")
    ))
  )
}
