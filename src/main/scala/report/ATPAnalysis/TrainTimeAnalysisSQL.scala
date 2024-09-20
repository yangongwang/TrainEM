package report.ATPAnalysis

import config.ConfigHelper
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{DataFrame, SparkSession}

/**
  * 离线报表1：
  *   列车出厂时间ATP报警统计
  *   利用sparksql实现
  */


object TrainTimeAnalysisSQL {
  def main(args: Array[String]): Unit = {
    //session
    val session = SparkSession
      .builder()
      .master("local[*]")
      .appName(this.getClass.getName)
      .config("spark.serializer",ConfigHelper.serializer)
      .getOrCreate()
    //导入隐式转换
    import session.implicits._
    //读取原始日志
    val source = session.read.parquet("F:\\项目\\高铁项目\\parquet0722")
    //读取列车出厂时间的数据
    val trainTimeSource: RDD[String] = session.sparkContext.textFile("F:\\项目\\高铁项目\\列车出厂时间数据.txt")
    //处理并切分
    val filtedRDD = trainTimeSource.map(_.split("\\|",-1)).filter(_.length>=2)
    //rdd转dataframe
    val trainTimeTable: DataFrame = filtedRDD.map(arr=>(arr(0),arr(1))).toDF("trainId","trainTime")
    //注册字典表
    trainTimeTable.createTempView("trainTi")
    //注册表
    source.createTempView("logs")
    //统计
    //聚合一次在join
    //先join在聚合
//sum(a.cnt),sum(a.atpAllerror),sum(a.main),sum(a.wifi),sum(a.balise),sum(a.TCR),sum(a.speed),sum(a.DMI),sum(a.TIU),sum(a.JRU) from
    session.sql(
      """
        |
        |select if(c.trainTime = '',c.trainId,c.trainTime) as ti,
        |sum(a.cnt),sum(a.atpAllerror),sum(a.main),sum(a.wifi),sum(a.balise),sum(a.TCR),sum(a.speed),sum(a.DMI),sum(a.TIU),sum(a.JRU) from
        |trainTi c
        |right join
        |(select MPacketHead_TrainID as trainid,count(*) as cnt,
        |sum(if(MATPBaseInfo_AtpError != '',1,0)) as atpAllerror,
        |sum(if(MATPBaseInfo_AtpError = '车载主机',1,0)) as main,
        |sum(if(MATPBaseInfo_AtpError = '无线传输单元',1,0)) as wifi,
        |sum(if(MATPBaseInfo_AtpError = '应答器信息接收单元',1,0)) as balise,
        |sum(if(MATPBaseInfo_AtpError = '轨道电路信息读取器',1,0)) as TCR,
        |sum(if(MATPBaseInfo_AtpError = '测速测距单元',1,0)) as speed,
        |sum(if(MATPBaseInfo_AtpError = '人机交互接口单元',1,0)) as DMI,
        |sum(if(MATPBaseInfo_AtpError = '列车接口单元',1,0)) as TIU,
        |sum(if(MATPBaseInfo_AtpError = '司法记录单元',1,0)) as JRU
        |from logs
        |group by MPacketHead_TrainID) as a
        |on c.trainId = a.trainid
        |group by ti
      """.stripMargin).show(30000)

    //释放资源
    session.stop()
  }
}
