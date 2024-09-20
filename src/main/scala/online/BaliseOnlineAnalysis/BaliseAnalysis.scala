package online.BaliseOnlineAnalysis

import config.ConfigHelper
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.spark.SparkConf
import org.apache.spark.rdd.RDD
import org.apache.spark.streaming.dstream.InputDStream
import org.apache.spark.streaming.kafka010.{ConsumerStrategies, KafkaUtils, LocationStrategies}
import org.apache.spark.streaming.{Seconds, StreamingContext}
import utils.MakeBaliseKPI

/**
  * 1.创建接收器
  * 2.接收数据
  * 3.处理数据
  * （1）获取偏移量
  * （2）处理数据
  * （3）更新偏移量
  */
object BaliseAnalysis {
  def main(args: Array[String]): Unit = {
    //conf
    val conf = new SparkConf()
      .setMaster("local[*]")
      .setAppName(this.getClass.getName)
      .set("spark.serializer", ConfigHelper.serializer)
      .set("spark.streaming.stopGracefullyOnShutdown", "true")
    //创建一个接收器
    val ssc: StreamingContext = new StreamingContext(conf, Seconds(2))
    //kafka的配置参数
    val kafkaParams = Map[String, Object](
      "bootstrap.servers" -> "linux01:9092,linux02:9092,linux03:9092",
      "key.deserializer" -> classOf[StringDeserializer],
      "value.deserializer" -> classOf[StringDeserializer],
      "group.id" -> "test",
      "auto.offset.reset" -> "latest",
      "enable.auto.commit" -> (true: java.lang.Boolean)
    )
    //接收kafka的数据
    val dstream: InputDStream[ConsumerRecord[String, String]] = KafkaUtils.createDirectStream(
      ssc,
      LocationStrategies.PreferConsistent,
      ConsumerStrategies.Subscribe[String, String](
        ConfigHelper.topic,
        ConfigHelper.kafkaParams
      )
    )
    //处理数据，每一个rdd维护一个偏移量
    dstream.foreachRDD(rdd => {
      //判断rdd是否为空，如果不为空在做处理
      if (!rdd.isEmpty()) {
        //数据预处理切分
        val filted: RDD[Array[String]] = rdd.map(_.value()).map(_.split("\\|", -1)).filter(_.length >= 55)
        //统计指标
        val reduceData: RDD[(String, List[Int], String, String, String)] = filted.map(arr => {
          //获取list
          val list: List[Int] = MakeBaliseKPI.getBaliseKPI(arr)
          //获取数据时间
          val dataTime = arr(7)
          //获取天
          val day = dataTime.substring(0, 8)
          //获取小时
          val hour = dataTime.substring(0, 10)
          //获取分钟数
          val minute = dataTime.substring(0, 12)
          //获取路局
          val AttachRWBureau = arr(23)
          (day, list, hour, minute,AttachRWBureau)
        })
        //j聚合
        //获取铁路总公司报警数
        //        MakeBaliseKPIStream.getDayKPI(reduceData)
        //获取铁路总公司每小时的报警数
//        MakeBaliseKPIStream.getHourKPI(reduceData)
        //获取每分钟铁路总公司的报警数
//        MakeBaliseKPIStream.getMinuteKPI(reduceData)
        //获取铁路局公司报警数
//        MakeBaliseKPIStream.getDayAndBureauKPI(reduceData)

      }
    })

    //启动程序
    ssc.start()
    ssc.awaitTermination()
  }
}
