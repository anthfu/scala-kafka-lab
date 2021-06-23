package com.anthfu.kafka.zio.producer

import org.apache.kafka.clients.producer.ProducerRecord
import zio._
import zio.config.ConfigDescriptor._
import zio.config._
import zio.config.yaml.YamlConfig
import zio.console.putStrLn
import zio.kafka.producer.{Producer, ProducerSettings}
import zio.kafka.serde.Serde
import zio.stream.ZStream

import java.nio.file.Path
import java.util.UUID

object ZioProducer extends App {
  case class AppConfig(topic: String, bootstrapServer: String)

  type AppEnv = ZEnv with Has[AppConfig] with Producer[Any, UUID, String]

  override def run(args: List[String]): ZIO[ZEnv, Nothing, ExitCode] = {
    val configLayer = ZEnv.live ++ getConfigLayer
    val appLayer = configLayer ++ (configLayer >>> getProducerLayer)
    app.provideLayer(appLayer).exitCode
  }

  private def app: ZIO[AppEnv, Throwable, Unit] =
    ZStream
      .fromIterable(0 to 1000)
      .mapM { n =>
        for {
          conf <- getConfig[AppConfig]
          rec   = new ProducerRecord(conf.topic, UUID.randomUUID(), n.toString)
          _    <- Producer.produce[Any, UUID, String](rec)
          _    <- putStrLn(s"Sent: ${rec.value}")
        } yield ()
      }
      .runDrain

  private def getConfigLayer: ZLayer[Any, Throwable, Has[AppConfig]] = {
    val descriptor = (string("app/topic") |@| string("app/bootstrap_server"))(AppConfig.apply, AppConfig.unapply)
    YamlConfig.fromPath(Path.of("src/main/resources/application.yml"), descriptor)
  }

  private def getProducerLayer: ZLayer[ZEnv with Has[AppConfig], Throwable, Producer[Any, UUID, String]] = {
    val managed = ZManaged.access[Has[AppConfig]](_.get)
      .flatMap { conf =>
        val settings = ProducerSettings(List(conf.bootstrapServer))
        Producer.make[Any, UUID, String](settings, Serde.uuid, Serde.string)
      }

    ZLayer.fromManaged(managed)
  }
}
