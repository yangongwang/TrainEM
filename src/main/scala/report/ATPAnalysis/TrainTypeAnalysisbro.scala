package report.ATPAnalysis

import java.util.Properties

import beans.Logs
import config.ConfigHelper
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{Dataset, SaveMode, SparkSession}
import utils.MakeATPKPI

/**
  * 离线报表6：
  *   车型ATP报警统计
  *   利用sparkcore 广播变量实现
  */

object TrainTypeAnalysisbro {
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
    val dataSet: Dataset[Logs] = session.read.parquet("F:\\项目\\高铁项目\\parquet0722").as[Logs]
    //读取列出出厂时间的数据
    val trainTypeSource: RDD[String] = session.sparkContext.textFile("F:\\项目\\高铁项目\\列车型号.txt")
    val trainTypeMap: collection.Map[String, String] = trainTypeSource.map(_.split("\\|",-1)).filter(_.length>=2).map(arr=>(arr(0),arr(1))).collectAsMap()
    val trainTypeBro: Broadcast[collection.Map[String, String]] = session.sparkContext.broadcast(trainTypeMap)

    //进行处理
    val result = dataSet.map(logClass => {
      //获取trainid
      val trainId: String = logClass.MPacketHead_TrainID
      //调用utils中MakeAtpErrpor方法获取list
      val list: List[Int] = MakeATPKPI.getATPKPI(logClass)
      //获取广播变量的值
      val trainTypeDict: collection.Map[String, String] = trainTypeBro.value
      //获取列出的出厂时间
      val trainType = trainTypeDict.getOrElse(trainId.substring(0,1), trainId)
      (trainType, list)
    }).rdd.reduceByKey {
      (list1, list2) => list1 zip list2 map (tp => (tp._1 + tp._2))
    }

    // 写入mysql
    val props = new Properties()
    props.setProperty("driver", ConfigHelper.drvier)
    props.setProperty("user", ConfigHelper.user)
    props.setProperty("password", ConfigHelper.password)
    result.map(tp => (tp._1, tp._2(0), tp._2(1), tp._2(2), tp._2(3), tp._2(4), tp._2(5), tp._2(6), tp._2(7), tp._2(8), tp._2(9)))
      .toDF("trainType", "dataAll", "allerror", "main", "wifi", "balise", "TCR", "speed", "DMI", "JRU", "TIU")
      .write.mode(SaveMode.Overwrite).jdbc(ConfigHelper.url, "TrainTypeAnalysisbro", props)

    //释放资源
    session.stop()
  }
}
