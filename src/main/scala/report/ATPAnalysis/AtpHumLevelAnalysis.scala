package report.ATPAnalysis

import java.util.Properties

import config.ConfigHelper
import org.apache.log4j.{Level, Logger}
import org.apache.spark.sql.{DataFrame, Dataset, SaveMode, SparkSession}
import utils.{MakeATPKPI, MakeWeather}

/**
  * 离线报表9
  * 湿度ATP报警统计
  * 用sparkcore实现
  */


object AtpHumLevelAnalysis {
  def main(args: Array[String]): Unit = {
    Logger.getLogger("org").setLevel(Level.ERROR)
    //创建一个sparksession
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
    val dataPath="hdfs://mycluster/data/parquet_traindata/*/*"
//    val sourceFrame: DataFrame = session.read.load(args(0))
val sourceFrame: DataFrame = session.read.load(dataPath)
    //处理数据
    val kpiDataSet: Dataset[(String, List[Int])] = sourceFrame.map(row => {
      //获取湿度等级
      val humLevel = MakeWeather.getHumLevel(row)
      //获取list指标
      val list = MakeATPKPI.getATPKPI(row)
      (humLevel, list)
    })
    //进行reducebykey
    val result = kpiDataSet.rdd.reduceByKey {
      (list1, list2) =>
        list1.zip(list2).map(x => x._1 + x._2)
    }
    // 写入mysql
    val props = new Properties()
    props.setProperty("driver", ConfigHelper.drvier)
    props.setProperty("user", ConfigHelper.user)
    props.setProperty("password", ConfigHelper.password)
    result.map(tp => (tp._1, tp._2(0), tp._2(1), tp._2(2), tp._2(3), tp._2(4), tp._2(5), tp._2(6), tp._2(7), tp._2(8), tp._2(9)))
      .toDF("humLevel", "dataAll", "allerror", "main", "wifi", "balise", "TCR", "speed", "DMI", "JRU", "TIU")
      .write.mode(SaveMode.Overwrite).jdbc(ConfigHelper.url, "AtpHumLevelAnalysis", props)


    //释放资源
    session.stop()
  }
}
