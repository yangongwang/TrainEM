package tools

import java.time.Duration
import java.time.temporal.ChronoUnit

import io.lettuce.core.{RedisClient, RedisURI, SetArgs}
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SparkSession



object trainTime2redis {
  def main(args: Array[String]): Unit = {
    //session
    val session = SparkSession
      .builder()
      .master("local[*]")
      .appName(this.getClass.getName)
      .getOrCreate()
    //读取数据
    val trainTimeSource: RDD[String] = session.sparkContext.textFile("/home/data/字典文件/列车出厂时间数据.txt")
    //进行预处理切分
    val filtedRDD: RDD[Array[String]] = trainTimeSource.map(_.split("\\|", -1)).filter(_.length >= 2)
    //写入redis
    filtedRDD.foreachPartition(partition => {
      //获取redis连接
      val redisUri = RedisURI.builder()
        .withHost("master1")
        .withPort(6379)
        .withTimeout(Duration.of(10, ChronoUnit.SECONDS))
        .build()

      val redisClient = RedisClient.create(redisUri)
      val connection = redisClient.connect()
      val redisCommands = connection.sync()
      val setArgs = SetArgs.Builder.nx().ex(5)

      partition.foreach(arr => {
        //写入redis
        redisCommands.hset("trainTime",arr(0),arr(1))
      })
      connection.close()
      redisClient.shutdown()
    })

    //释放资源
    session.stop()



  }


}
