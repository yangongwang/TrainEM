package online.SignalOnlineAnalysis

import java.util.Properties

import config.ConfigHelper
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.spark.SparkConf
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{DataFrame, SaveMode, SparkSession}
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.streaming.dstream.InputDStream
import org.apache.spark.streaming.kafka010._
import utils._

/**
  * 实时报表：
  *   信号机按照湿度等级实时故障统计
  *   利用sparkcore 实现
  */


object SingalHumOnlineAnalysis {
  def main(args: Array[String]): Unit = {
    //conf
    val conf = new SparkConf()
      .setMaster("local[*]")
      .setAppName(this.getClass.getName)
      .set("spark.serializer", ConfigHelper.serializer)
      .set("spark.streaming.stopGracefullyOnShutdown", "true")
    //创建一个接收器
    val ssc: StreamingContext = new StreamingContext(conf, Seconds(2))
    //接收kafka的数据
    val dstream: InputDStream[ConsumerRecord[String, String]] = KafkaUtils.createDirectStream(
      ssc,
      LocationStrategies.PreferConsistent,
      ConsumerStrategies.Subscribe[String, String](
        ConfigHelper.topic,
        ConfigHelper.kafkaParams
      )
    )
    //处理数据
    dstream.foreachRDD(rdd => {
      //判断rdd是否为空，如果不为空在做处理
      if (!rdd.isEmpty()) {
        //获取偏移量
        val ranges: Array[OffsetRange] = rdd.asInstanceOf[HasOffsetRanges].offsetRanges
        //数据预处理
        val filted: RDD[Array[String]] = rdd.map(_.value()).map(_.split("\\|")).filter(_.length >= 55)
        filted.map(arr => {
          //获取湿度
          val hum = TurnType.toDouble(arr(54))
          //获取湿度等级
          val humLevel = MakeOnlineWeather.getHumLevel(hum)
          //获取指标哦
          val signalKpi = MakeSignalKPI.getSignalKPI(arr)
          (humLevel, signalKpi)
        }).reduceByKey {
          (list1, list2) => list1.zip(list2).map(tp => tp._1 + tp._2)
        }.foreachPartition(partition => {
          //获取redis连接
          val lettuce = MakeRedisConnLettuce.getRedis()
          partition.foreach(tp => {
            //写入redis  hincby
            println(tp._1+":"+tp._2)
            lettuce.hincrby("signalError", tp._1+"总报警数", tp._2(1))
            lettuce.hincrby("signalError", tp._1+"电源", tp._2(2))
            lettuce.hincrby("signalError", tp._1+"灯泡", tp._2(3))
            lettuce.hincrby("signalError", tp._1+"开灯继电器", tp._2(4))
            lettuce.hincrby("signalError", tp._1+"信号机接口电路", tp._2(5))
          })
          //关闭redis连接
          MakeRedisConnLettuce.close()
        })
      }
    })
    //启动程序
    ssc.start()
    ssc.awaitTermination()
  }
}
