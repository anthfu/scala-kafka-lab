package com.anthfu.kafka.zio

import com.dimafeng.testcontainers.lifecycle.and
import com.dimafeng.testcontainers.munit.TestContainersForAll
import com.dimafeng.testcontainers.{GenericContainer, KafkaContainer}
import munit.FunSuite
import org.testcontainers.containers.wait.strategy.Wait

class ZioKafkaClientIT extends FunSuite with TestContainersForAll {
  override type Containers = KafkaContainer and ConsumerContainer and ProducerContainer

  override def startContainers(): Containers = {
    val kafkaContainer = KafkaContainer.Def().start()
    val consumerContainer = ConsumerContainer.Def(port = 8080).start()
    val producerContainer = ProducerContainer.Def(port = 8080).start()
    kafkaContainer and consumerContainer and producerContainer
  }

  test("Send and receive messages") {
    withContainers { case kafkaContainer and consumerContainer and producerContainer =>
      // TODO
    }
  }
}

class ConsumerContainer(underlying: GenericContainer) extends GenericContainer(underlying)
object ConsumerContainer {
  case class Def(port: Int) extends GenericContainer.Def[ConsumerContainer](
    new ConsumerContainer(GenericContainer(
      dockerImage = "zio-consumer:1.0.0-SNAPSHOT",
      exposedPorts = Seq(port),
      waitStrategy = Wait.forHttp("/")
    ))
  )
}

class ProducerContainer(underlying: GenericContainer) extends GenericContainer(underlying)
object ProducerContainer {
  case class Def(port: Int) extends GenericContainer.Def[ProducerContainer](
    new ProducerContainer(GenericContainer(
      dockerImage = "zio-producer:1.0.0-SNAPSHOT",
      exposedPorts = Seq(port),
      waitStrategy = Wait.forHttp("/")
    ))
  )
}
