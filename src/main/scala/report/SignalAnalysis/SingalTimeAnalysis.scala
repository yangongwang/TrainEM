package report.SignalAnalysis

import java.util.Properties

import config.ConfigHelper
import org.apache.log4j.{Level, Logger}
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{DataFrame, SaveMode, SparkSession}
import utils.{MakeBaliseKPI, MakeSignalKPI, MakeWeather}

/**
  * 离线报表19：
  *   信号机按照出厂时间故障统计
  *   利用sparkcore 广播变量实现
  */


object SingalTimeAnalysis {
  def main(args: Array[String]): Unit = {
    Logger.getLogger("org").setLevel(Level.ERROR)
    //session
    val session = SparkSession
      .builder()
      .master("local[*]")
      .appName(this.getClass.getName)
      .config("spark.serializer", ConfigHelper.serializer)
      .getOrCreate()

    val sc = session.sparkContext
    sc.hadoopConfiguration.set("fs.defaultFS", "hdfs://mycluster")
    sc.hadoopConfiguration.set("dfs.nameservices", "mycluster")
    sc.hadoopConfiguration.set("dfs.ha.namenodes.mycluster", "nn1,nn2")
    sc.hadoopConfiguration.set("dfs.name" +
      "node.rpc-address.mycluster.nn1", "master1:9000")
    sc.hadoopConfiguration.set("dfs.namenode.rpc-address.mycluster.nn2", "master2:9000")
    sc.hadoopConfiguration.set("dfs.client.failover.proxy.provider.mycluster", "org.apache.hadoop.hdfs.server.namenode.ha.ConfiguredFailoverProxyProvider")


    //导入隐式转换
    import session.implicits._
    //读取数据
    val dataframe: DataFrame = session.read.parquet("hdfs://mycluster/data/parquet_traindata/*/*")
    //读取列出出厂时间的数据
    val signalTimeSource: RDD[String] = session.sparkContext.textFile("file:///home/data/字典文件/信号机生产日期.txt")
    val signalTimeMap: collection.Map[String, String] = signalTimeSource.map(_.split("\\|",-1)).filter(_.length>=2).map(arr=>(arr(0),arr(1))).collectAsMap()
    val signalTimeBro: Broadcast[collection.Map[String, String]] = session.sparkContext.broadcast(signalTimeMap)

    //进行处理
    val result = dataframe.map(row => {
      //获取信号机id
      val Signal_SignalID = row.getAs[String]("Signal_SignalID")
      //获取出厂时间
      val Signal_time = signalTimeBro.value.getOrElse(Signal_SignalID,Signal_SignalID)
      //调用utils中MMakeSignalKPI方法获取list
      val list: List[Int] = MakeSignalKPI.getSignalKPI(row)
      (Signal_time, list)
    }).rdd.reduceByKey {
      (list1, list2) => list1 zip list2 map (tp => (tp._1 + tp._2))
    }

    val props = new Properties()
    props.setProperty("driver", ConfigHelper.drvier)
    props.setProperty("user", ConfigHelper.user)
    props.setProperty("password", ConfigHelper.password)
    result.map(tp => (tp._1, tp._2(0), tp._2(1), tp._2(2), tp._2(3), tp._2(4), tp._2(5)))
      .toDF("Signal_time", "dataAll", "allerror", "balise", "switch", "switchMachine", "FTGS")
      .write.mode(SaveMode.Overwrite).jdbc(ConfigHelper.url, "SingalTimeAnalysis", props)

    //释放资源
    session.stop()
  }
}
