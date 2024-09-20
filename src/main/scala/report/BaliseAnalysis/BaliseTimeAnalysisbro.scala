package report.BaliseAnalysis

import java.util.Properties

import beans.Logs
import config.ConfigHelper
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{DataFrame, Dataset, SaveMode, SparkSession}
import utils.{MakeATPKPI, MakeBaliseKPI}

/**
  * 离线报表12：
  *   应答器按照出厂时间报警统计
  *   利用sparkcore 广播变量实现
  */

object BaliseTimeAnalysisbro {
  def main(args: Array[String]): Unit = {
    //session
    val session = SparkSession
      .builder()
      .master("local[*]")
      .appName(this.getClass.getName)
      .config("spark.serializer", ConfigHelper.serializer)
      .getOrCreate()
    //导入隐式转换
    import session.implicits._
    //读取数据
    val dataframe: DataFrame = session.read.parquet("F:\\项目\\高铁项目\\parquet0722")
    //读取列出出厂时间的数据
    val baliseTimeSource: RDD[String] = session.sparkContext.textFile("F:\\项目\\高铁项目\\应答器出场时间")
    val baliseTimeMap: collection.Map[String, String] = baliseTimeSource.map(_.split("\t",-1)).filter(_.length>=2).map(arr=>(arr(0),arr(1))).collectAsMap()
    val baliseTimeBro: Broadcast[collection.Map[String, String]] = session.sparkContext.broadcast(baliseTimeMap)

    //进行处理
    val result = dataframe.map(row => {
      //获取baliseid
      val baliseId = row.getAs[String]("MBalisePocket_BaliseID")
      //调用utils中MakeBaliseKPI方法获取list
      val list: List[Int] = MakeBaliseKPI.getBaliseKPI(row)
      //获取广播变量的值
      val baliseTime: collection.Map[String, String] = baliseTimeBro.value
      //获取列出的出厂时间
      val BaliseTime = baliseTime.getOrElse(baliseId, baliseId)
      (BaliseTime, list)
    }).rdd.reduceByKey {
      (list1, list2) => list1 zip list2 map (tp => (tp._1 + tp._2))
    }

    val props = new Properties()
    props.setProperty("driver", ConfigHelper.drvier)
    props.setProperty("user", ConfigHelper.user)
    props.setProperty("password", ConfigHelper.password)
    result.map(tp => (tp._1, tp._2(0), tp._2(1), tp._2(2), tp._2(3), tp._2(4), tp._2(5)))
      .toDF("BaliseTime", "dataAll", "allerror", "balise", "switch", "switchMachine", "FTGS")
      .write.mode(SaveMode.Overwrite).jdbc(ConfigHelper.url, "BaliseTimeAnalysisbro", props)




    //释放资源
    session.stop()
  }
}
