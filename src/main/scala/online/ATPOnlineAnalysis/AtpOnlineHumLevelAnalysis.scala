package online.ATPOnlineAnalysis

import java.util.Properties

import config.ConfigHelper
import org.apache.spark.sql.{DataFrame, Dataset, SaveMode, SparkSession}
import utils._
import config.ConfigHelper
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.spark.SparkConf
import org.apache.spark.rdd.RDD
import org.apache.spark.streaming.dstream.InputDStream
import org.apache.spark.streaming.kafka010._
import org.apache.spark.streaming.{Seconds, StreamingContext}

/**
 * 实时报表9
 * 实时湿度ATP报警统计
 * 用sparkcore实现
 */


object AtpOnlineHumLevelAnalysis {
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
        //数据预处理
        val filted: RDD[Array[String]] = rdd.map(_.value()).map(_.split("\\|")).filter(_.length >= 55)
        filted.map(arr => {
          //获取湿度
          val hum = TurnType.toDouble(arr(54))
          //获取湿度等级
          val humLevel = MakeOnlineWeather.getHumLevel(hum)
          //获取指标哦
          val atpKpi = MakeATPKPI.getATPKPI(arr)
          (humLevel, atpKpi)
        }).reduceByKey {
          (list1, list2) => list1.zip(list2).map(tp => tp._1 + tp._2)
        }.foreachPartition(partition => {
          //获取redis连接
          val lettuce = MakeRedisConnLettuce.getRedis()
          partition.foreach(tp => {
            //写入redis  hincby
            //println(tp._1+":"+tp._2)
            lettuce.hincrby("atpError", tp._1 + "总报警数", tp._2(1))
            lettuce.hincrby("atpError", tp._1 + "main", tp._2(2))
            lettuce.hincrby("atpError", tp._1 + "无线传输单元", tp._2(3))
            lettuce.hincrby("atpError", tp._1 + "应答器信息接收单元", tp._2(4))
            lettuce.hincrby("atpError", tp._1 + "轨道电路信息读取器", tp._2(5))
            lettuce.hincrby("atpError", tp._1 + "测速测距单元", tp._2(6))
            lettuce.hincrby("atpError", tp._1 + "人机交互接口单元", tp._2(7))
            lettuce.hincrby("atpError", tp._1 + "列车接口单元", tp._2(8))
            lettuce.hincrby("atpError", tp._1 + "司法记录单元", tp._2(9))
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
