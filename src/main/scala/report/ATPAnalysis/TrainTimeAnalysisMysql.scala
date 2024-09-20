package report.ATPAnalysis

import config.ConfigHelper
import org.apache.spark.sql.{DataFrame, SparkSession}
import scalikejdbc.config.DBs
import scalikejdbc.{DB, SQL}
import utils.MakeATPKPI

/**
  * 离线报表1：
  *   列车出厂时间ATP报警统计
  *   利用sparkcore mysql存储中间变量实现
  */

object TrainTimeAnalysisMysql {
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
    val source: DataFrame = session.read.parquet("F:\\项目\\高铁项目\\parquet0722")

    /**
      * 处理数据：（分区处理）
      * 1.获取trainid
      * 2.获取list
      * 3.根据trainid去mysql中查询traintime，第三方库
      */
    //加载配置参数
    DBs.setup()
    source.map(row => {
      //获取trainid
      val trainId: String = row.getAs[String]("MPacketHead_TrainID")
      //获取list
      val list = MakeATPKPI.getATPKPI(row)
      //根据trainid寻找traintime
      val trainTimeList: List[String] = DB.readOnly { implicit session =>
        SQL("select * from traintime0722 where trainId=?")
          .bind(trainId)
          .map(wrs => (wrs.string("trainTime")))
          .list()
          .apply()
      }
      //定义一个容器存放出厂时间
      var trainTime: String = ""
      //判断trainTimeList如果长度为0，给trainid，如果不为0，拿第一个元素
      if (trainTimeList.isEmpty) {
        trainTime = trainId
      } else {
        trainTime = trainTimeList(0)
      }
      (trainTime, list)
    }).rdd.reduceByKey{
      (list1,list2)=>list1.zip(list2).map(tp=>tp._1+tp._2)
    }.foreach(println(_))//输入到不同的库中（脏库）

    //释放资源
    session.stop()
  }
}
