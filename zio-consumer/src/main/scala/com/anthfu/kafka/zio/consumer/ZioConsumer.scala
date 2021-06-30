package com.anthfu.kafka.zio.consumer

import zhttp.http._
import zhttp.service.server.ServerChannelFactory
import zhttp.service.{EventLoopGroup, Server}
import zio._
import zio.config.ConfigDescriptor._
import zio.config.yaml.YamlConfig
import zio.console.putStrLn
import zio.kafka.consumer.{Consumer, ConsumerSettings, Subscription}
import zio.kafka.serde.Serde
import zio.stream.ZStream

import java.nio.file.Path

object ZioConsumer extends App {
  case class AppConfig(topic: String, bootstrapServer: String, groupId: String)

  type AppEnv = ZEnv with Has[AppConfig] with Consumer

  override def run(args: List[String]): ZIO[ZEnv, Nothing, ExitCode] = {
    val http = Http.collect[Request] {
      case Method.GET -> Root => Response.ok
    }

    val server = Server.port(8080) ++ Server.app(http)

    server.make.use { _ =>
      val configLayer = ZEnv.live ++ makeConfigLayer
      val appLayer = configLayer ++ (configLayer >>> makeConsumerLayer)
      consumerStream.provideLayer(appLayer).runDrain *> ZIO.never
    }
    .provideCustomLayer(ServerChannelFactory.auto ++ EventLoopGroup.auto(0))
    .exitCode
  }

  private def consumerStream: ZStream[AppEnv, Throwable, Unit] =
    for {
      conf <- ZStream.access[Has[AppConfig]](_.get)
      _    <- Consumer.subscribeAnd(Subscription.topics(conf.topic))
                .plainStream(Serde.uuid, Serde.string)
                .tap(rec => putStrLn(s"key: ${rec.record.key}, value: ${rec.record.value}"))
                .map(_.offset)
                .aggregateAsync(Consumer.offsetBatches)
                .mapM(_.commit)
    } yield ()

  private def makeConfigLayer: ZLayer[Any, Throwable, Has[AppConfig]] = {
    val descriptor = (
      nested("app")(string("topic")) |@|
      nested("app")(string("bootstrap_server")) |@|
      nested("app")(string("group_id"))
    )(AppConfig.apply, AppConfig.unapply)

    val configPath = getClass.getClassLoader.getResource("application.yml").toURI
    YamlConfig.fromPath(Path.of(configPath), descriptor)
  }

  private def makeConsumerLayer: ZLayer[ZEnv with Has[AppConfig], Throwable, Consumer] = {
    val managed = ZManaged.access[Has[AppConfig]](_.get)
      .flatMap { conf =>
        val settings = ConsumerSettings(List(conf.bootstrapServer)).withGroupId(conf.groupId)
        Consumer.make(settings)
      }

    ZLayer.fromManaged(managed)
  }
}
