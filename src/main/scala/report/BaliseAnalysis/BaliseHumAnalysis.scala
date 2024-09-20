package report.BaliseAnalysis

import java.util.Properties

import config.ConfigHelper
import org.apache.log4j.{Level, Logger}
import org.apache.spark.sql.{DataFrame, SaveMode, SparkSession}
import utils.{MakeBaliseKPI, MakeWeather}

/**
  * 离线报表17：
  *   应答器按照湿度等级报警统计
  *   利用sparkcore实现
  */

object BaliseHumAnalysis {
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

    //进行处理
    val result = dataframe.map(row => {
      //获取湿度等级
      val humevel = MakeWeather.getHumLevel(row)
      //调用utils中MakeBaliseKPI方法获取list
      val list: List[Int] = MakeBaliseKPI.getBaliseKPI(row)
      (humevel, list)
    }).rdd.reduceByKey {
      (list1, list2) => list1 zip list2 map (tp => (tp._1 + tp._2))
    }

    val props = new Properties()
    props.setProperty("driver", ConfigHelper.drvier)
    props.setProperty("user", ConfigHelper.user)
    props.setProperty("password", ConfigHelper.password)
    result.map(tp => (tp._1, tp._2(0), tp._2(1), tp._2(2), tp._2(3), tp._2(4), tp._2(5)))
      .toDF("humevel", "dataAll", "allerror", "balise", "switch", "switchMachine", "FTGS")
      .write.mode(SaveMode.Overwrite).jdbc(ConfigHelper.url, "BaliseHumAnalysis", props)



    //释放资源
    session.stop()
  }
}
