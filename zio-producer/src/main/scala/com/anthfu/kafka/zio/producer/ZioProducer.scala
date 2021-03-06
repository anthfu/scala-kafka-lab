package com.anthfu.kafka.zio.producer

import org.apache.kafka.clients.producer.{ProducerRecord, RecordMetadata}
import zhttp.http._
import zhttp.service.server.ServerChannelFactory
import zhttp.service.{EventLoopGroup, Server}
import zio._
import zio.config.ConfigDescriptor._
import zio.config._
import zio.console.putStrLn
import zio.kafka.producer.{Producer, ProducerSettings}
import zio.kafka.serde.Serde
import zio.stream.ZStream

import java.util.UUID

object ZioProducer extends App {
  case class AppConfig(bootstrapServer: String, topic: String)

  type AppEnv = ZEnv with Has[AppConfig] with Producer[Any, UUID, String]

  override def run(args: List[String]): ZIO[ZEnv, Nothing, ExitCode] = {
    val http = Http.collect[Request] {
      case Method.GET -> Root => Response.ok
    }

    val server = Server.port(8080) ++ Server.app(http)

    server.make.use { _ =>
      val configLayer = ZEnv.live ++ makeConfigLayer
      val appLayer = configLayer ++ (configLayer >>> makeProducerLayer)
      producerStream.provideLayer(appLayer).runDrain
    }
    .provideCustomLayer(
      ServerChannelFactory.auto ++ EventLoopGroup.auto(0))
    .exitCode
  }

  private def producerStream: ZStream[AppEnv, Throwable, RecordMetadata] =
    ZStream
      .fromIterable(0 to 1000)
      .mapM { n =>
        for {
          conf <- getConfig[AppConfig]
          rec   = new ProducerRecord(conf.topic, UUID.randomUUID(), n.toString)
          md   <- Producer.produce[Any, UUID, String](rec)
          _    <- putStrLn(s"partition: ${md.partition} offset: ${md.offset} key: ${rec.key}, value: ${rec.value}")
        } yield md
      }

  private def makeConfigLayer: ZLayer[ZEnv, Throwable, Has[AppConfig]] = {
    val descriptor = (
      string("BOOTSTRAP_SERVER") |@|
      string("TOPIC")
    )(AppConfig.apply, AppConfig.unapply)

    ZConfig.fromSystemEnv(descriptor)
  }

  private def makeProducerLayer: ZLayer[ZEnv with Has[AppConfig], Throwable, Producer[Any, UUID, String]] = {
    val managed = ZManaged.access[Has[AppConfig]](_.get)
      .flatMap { conf =>
        val settings = ProducerSettings(List(conf.bootstrapServer))
        Producer.make[Any, UUID, String](settings, Serde.uuid, Serde.string)
      }

    ZLayer.fromManaged(managed)
  }
}
