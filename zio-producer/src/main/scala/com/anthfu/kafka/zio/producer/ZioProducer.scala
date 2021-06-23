package com.anthfu.kafka.zio.producer

import org.apache.kafka.clients.producer.ProducerRecord
import zio._
import zio.console.putStrLn
import zio.kafka.producer.{Producer, ProducerSettings}
import zio.kafka.serde.Serde
import zio.stream.ZStream

import java.util.UUID

object ZioProducer extends App {
  type ProducerEnv = ZEnv with Producer[Any, UUID, String]

  override def run(args: List[String]): ZIO[ZEnv, Nothing, ExitCode] =
    stream.provideSomeLayer[ZEnv](producerLayer).exitCode

  private def stream: ZIO[ProducerEnv, Throwable, Unit] =
    ZStream
      .fromIterable(0 to 1000)
      .map(n => new ProducerRecord("test-topic", UUID.randomUUID(), n.toString))
      .mapM { rec =>
        putStrLn(s"Sent: ${rec.value}") *>
          Producer.produce[Any, UUID, String](rec)
      }
      .runDrain

  private def producerLayer: ZLayer[ZEnv, Throwable, Producer[Any, UUID, String]] = {
    val settings = ProducerSettings(List("localhost:9092"))
    ZLayer.fromManaged(Producer.make[Any, UUID, String](settings, Serde.uuid, Serde.string))
  }
}
