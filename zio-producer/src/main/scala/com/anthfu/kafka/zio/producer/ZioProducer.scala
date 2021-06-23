package com.anthfu.kafka.zio.producer

import org.apache.kafka.clients.producer.ProducerRecord
import zio._
import zio.console.putStrLn
import zio.kafka.producer.{Producer, ProducerSettings}
import zio.kafka.serde.Serde
import zio.stream.ZStream

import java.util.UUID

object ZioProducer extends App {
  override def run(args: List[String]): URIO[ZEnv, ExitCode] = {
    val settings = ProducerSettings(List("localhost:9092"))
    val producer = ZLayer.fromManaged(Producer.make[Any, UUID, String](settings, Serde.uuid, Serde.string))
    stream.provideSomeLayer[ZEnv](producer).exitCode
  }

  private lazy val stream =
    ZStream
      .fromIterable(0 to 1000)
      .map(n => new ProducerRecord("test-topic", UUID.randomUUID(), n.toString))
      .mapM { rec =>
        putStrLn(s"Sent: ${rec.value}") *>
          Producer.produce[Any, UUID, String](rec)
      }
      .runDrain
}
