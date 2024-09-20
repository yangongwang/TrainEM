package report.BaliseAnalysis

import java.util.Properties

import config.ConfigHelper
import org.apache.spark.sql.{DataFrame, SaveMode, SparkSession}
import utils.{MakeBaliseKPI, MakeWeather}

/**
  * 离线报表14：
  *   应答器按照列车速度等级报警统计
  *   利用sparkcore实现
  */

object BaliseSpeedAnalysis {
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

    //进行处理
    val result = dataframe.map(row => {
      //获取速度等级
      val spelevel = MakeWeather.getSpeLevel(row)
      //调用utils中MMakeBaliseKPI方法获取list
      val list: List[Int] = MakeBaliseKPI.getBaliseKPI(row)
      (spelevel, list)
    }).rdd.reduceByKey {
      (list1, list2) => list1 zip list2 map (tp => (tp._1 + tp._2))
    }

    val props = new Properties()
    props.setProperty("driver", ConfigHelper.drvier)
    props.setProperty("user", ConfigHelper.user)
    props.setProperty("password", ConfigHelper.password)
    result.map(tp => (tp._1, tp._2(0), tp._2(1), tp._2(2), tp._2(3), tp._2(4), tp._2(5)))
      .toDF("spelevel", "dataAll", "allerror", "balise", "switch", "switchMachine", "FTGS")
      .write.mode(SaveMode.Overwrite).jdbc(ConfigHelper.url, "BaliseSpeedAnalysis", props)




    //释放资源
    session.stop()
  }
}
