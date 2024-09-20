package config

import com.typesafe.config.{Config, ConfigFactory}
import org.apache.kafka.common.serialization.StringDeserializer

object ConfigHelper {
  //加载配置文件
  private lazy val load: Config = ConfigFactory.load()

  //加载序列化
  val serializer: String = load.getString("spark.serializer")

  //加载压缩
  val codec: String = load.getString("spark.code")

  //记载jdbc
  val drvier: String = load.getString("db.default.driver")
  val url: String = load.getString("db.default.url")
  val user: String = load.getString("db.default.user")
  val password: String = load.getString("db.default.password")

  //加载kafka相关
  val topic: Array[String] = load.getString("topics").split(",")
  val groupid: String = load.getString("group.id")
  //kafka的配置参数
  val kafkaParams = Map[String, Object](
    "bootstrap.servers" -> "slave1:9092,slave2:9092,slave3:9092",
    "key.deserializer" -> classOf[StringDeserializer],
    "value.deserializer" -> classOf[StringDeserializer],
    "group.id" -> groupid,
    "auto.offset.reset" -> "earliest",
    "enable.auto.commit" -> (false: java.lang.Boolean)
  )
}
