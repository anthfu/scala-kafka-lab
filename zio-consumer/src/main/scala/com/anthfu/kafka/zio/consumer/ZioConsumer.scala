package com.anthfu.kafka.zio.consumer

import zio._
import zio.config.ConfigDescriptor._
import zio.config._
import zio.config.yaml.YamlConfig
import zio.console.putStrLn
import zio.kafka.consumer.{Consumer, ConsumerSettings, Subscription}
import zio.kafka.serde.Serde

import java.nio.file.Path

object ZioConsumer extends App {
  case class AppConfig(topic: String, bootstrapServer: String, groupId: String)

  type AppEnv = ZEnv with Has[AppConfig] with Consumer

  override def run(args: List[String]): ZIO[ZEnv, Nothing, ExitCode] = {
    val configLayer = ZEnv.live ++ getConfigLayer
    val appLayer = configLayer ++ (configLayer >>> getConsumerLayer)
    app.provideLayer(appLayer).exitCode
  }

  private def app: ZIO[AppEnv, Throwable, Unit] =
    for {
      conf <- getConfig[AppConfig]
      _    <- Consumer.subscribeAnd(Subscription.topics(conf.topic))
                .plainStream(Serde.uuid, Serde.string)
                .tap(rec => putStrLn(s"key: ${rec.record.key}, value: ${rec.record.value}"))
                .map(_.offset)
                .aggregateAsync(Consumer.offsetBatches)
                .mapM(_.commit)
                .runDrain
    } yield ()

  private def getConfigLayer: ZLayer[Any, Throwable, Has[AppConfig]] = {
    val descriptor = (
      string("app/topic") |@|
      string("app/bootstrap_server") |@|
      string("app/group_id")
    )(AppConfig.apply, AppConfig.unapply)

    YamlConfig.fromPath(Path.of("src/main/resources/application.yml"), descriptor)
  }

  private def getConsumerLayer: ZLayer[ZEnv with Has[AppConfig], Throwable, Consumer] = {
    val managed = ZManaged.access[Has[AppConfig]](_.get)
      .flatMap { conf =>
        val settings = ConsumerSettings(List(conf.bootstrapServer)).withGroupId(conf.groupId)
        Consumer.make(settings)
      }

    ZLayer.fromManaged(managed)
  }
}
