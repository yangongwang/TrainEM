package report.ATPAnalysis

import org.apache.log4j.{Level, Logger}
import org.apache.spark.sql.SparkSession

/**
  * 离线报表2
  *   配属铁路局ATP报警统计
  *   用sparksql实现
  */


object AttachRWBureauAnalysisSQL {
  def main(args: Array[String]): Unit = {
    Logger.getLogger("org").setLevel(Level.ERROR)
    //session
    val session = SparkSession
      .builder()
      .master("local[*]")
      .appName(this.getClass.getName)
      .getOrCreate()

    val sc = session.sparkContext
    sc.hadoopConfiguration.set("fs.defaultFS", "hdfs://mycluster")
    sc.hadoopConfiguration.set("dfs.nameservices", "mycluster")
    sc.hadoopConfiguration.set("dfs.ha.namenodes.mycluster", "nn1,nn2")
    sc.hadoopConfiguration.set("dfs.name" +
      "node.rpc-address.mycluster.nn1", "master1:9000")
    sc.hadoopConfiguration.set("dfs.namenode.rpc-address.mycluster.nn2", "master2:9000")
    sc.hadoopConfiguration.set("dfs.client.failover.proxy.provider.mycluster", "org.apache.hadoop.hdfs.server.namenode.ha.ConfiguredFailoverProxyProvider")


    //读取数据
    val dataPath="hdfs://mycluster/data/parquet_traindata/*/*"
//    val frame = session.read.parquet(args(0))
    val frame = session.read.parquet(dataPath)
    //注册表
    frame.createTempView("logs")
    //注册一个udf
    session.udf.register("myif",(boolean:Boolean)=>if (boolean) 1 else 0)
    //写sql语句
    session.sql(
      """
        |select
        | MPacketHead_AttachRWBureau,
        | count(*) as sumData,
        | sum(if(MATPBaseInfo_AtpError != '',1,0)) as errorAll,
        | sum(case when MATPBaseInfo_AtpError = '车载主机' then 1 else 0 end) as main,
        | sum(myif(MATPBaseInfo_AtpError = '无线传输单元')) as wifi,
        | sum(if(MATPBaseInfo_AtpError = '应答器信息接收单元',1,0)) as balise,
        | sum(if(MATPBaseInfo_AtpError = '轨道电路信息读取器',1,0)) as TCR,
        | sum(if(MATPBaseInfo_AtpError = '测速测距单元',1,0)) as speed,
        | sum(if(MATPBaseInfo_AtpError = '人机交互接口单元',1,0)) as DMI,
        | sum(if(MATPBaseInfo_AtpError = '列车接口单元',1,0)) as TIU,
        | sum(if(MATPBaseInfo_AtpError = '司法记录单元',1,0)) as JRU
        |from logs
        |group by MPacketHead_AttachRWBureau
      """.stripMargin).show()

    //释放资源
    session.stop()
  }
}
