package report.ATPAnalysis


import java.util.Properties

import config.ConfigHelper
import org.apache.spark.sql.{DataFrame, Dataset, SaveMode, SparkSession}
import utils.{MakeATPKPI, MakeWeather}

/**
  * 离线报表7
  * 温度ATP报警统计
  * 用sparkcore实现
  */


object AtpTemLevelAnalysis {
  def main(args: Array[String]): Unit = {
    //创建一个sparksession
    val session = SparkSession
      .builder()
      .master("local[*]")
      .appName(this.getClass.getName)
      .config("spark.serializer", ConfigHelper.serializer)
      .getOrCreate()
    //导入隐式转换
    import session.implicits._
    //读取数据
    val sourceFrame: DataFrame = session.read.load(args(0))
    //处理数据
    val kpiDataSet: Dataset[(String, List[Int])] = sourceFrame.map(row => {
      //获取温度等级
      val temLevel = MakeWeather.getTemLevel(row)
      //获取list指标
      val list = MakeATPKPI.getATPKPI(row)
      (temLevel, list)
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
      .toDF("temLevel", "dataAll", "allerror", "main", "wifi", "balise", "TCR", "speed", "DMI", "JRU", "TIU")
      .write.mode(SaveMode.Overwrite).jdbc(ConfigHelper.url, "AtpTemLevelAnalysis", props)


    //释放资源
    session.stop()
  }
}
