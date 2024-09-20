package report.ATPAnalysis

import beans.Logs
import config.ConfigHelper
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{Dataset, SparkSession}
import utils.MakeATPKPI

/**
  * 离线报表1：
  *   列车出厂时间ATP报警统计
  *   利用sparkcore 广播变量实现
  */

object TrainTimeAnalysisbro {
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
    val trainTimeSource: RDD[String] = session.sparkContext.textFile("F:\\项目\\高铁项目\\列车出厂时间数据.txt")
    val trainTimeMap: collection.Map[String, String] = trainTimeSource.map(_.split("\\|",-1)).filter(_.length>=2).map(arr=>(arr(0),arr(1))).collectAsMap()
    val trainTimeBro: Broadcast[collection.Map[String, String]] = session.sparkContext.broadcast(trainTimeMap)

    //进行处理
    val result = dataSet.map(logClass => {
      //获取trainid
      val trainId: String = logClass.MPacketHead_TrainID
      //调用utils中MakeAtpErrpor方法获取list
      val list: List[Int] = MakeATPKPI.getATPKPI(logClass)
      //获取广播变量的值
      val trainTimeDict: collection.Map[String, String] = trainTimeBro.value
      //获取列出的出厂时间
      val trainTime = trainTimeDict.getOrElse(trainId, trainId)
      (trainTime, list)
    }).rdd.reduceByKey {
      (list1, list2) => list1 zip list2 map (tp => (tp._1 + tp._2))
    }.cache()
    result.filter(tp=>tp._1.length==7).foreach(println(_))//写入到好库中
    result.filter(tp=>tp._1.length != 7).foreach(println(_))//写入到脏库中，进行追溯


    //释放资源
    session.stop()
  }
}
