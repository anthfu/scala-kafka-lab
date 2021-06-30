package com.anthfu.kafka.zio

import com.dimafeng.testcontainers.munit.TestContainerForAll
import com.dimafeng.testcontainers.{ContainerDef, KafkaContainer}
import munit.FunSuite

class ZioKafkaClientIT extends FunSuite with TestContainerForAll {
  override val containerDef: ContainerDef = KafkaContainer.Def()

  test("Send and receive messages") {
    withContainers { kafkaContainer =>
      // TODO
    }
  }
}
