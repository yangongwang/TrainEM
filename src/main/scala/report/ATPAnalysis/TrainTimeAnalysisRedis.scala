package report.ATPAnalysis

import config.ConfigHelper
import org.apache.commons.lang3.StringUtils
import org.apache.log4j.{Level, Logger}
import org.apache.spark.sql.{DataFrame, SparkSession}
import utils.{MakeATPKPI, MakeRedisConn, MakeRedisConnLettuce}

/**
 * 离线报表1：
 * 列车出厂时间ATP报警统计
 * 利用sparkcore redis存储中间变量实现
 * Redis中的数据需要运行代码tools.trainTime2redis将列车出场时间数据导入到Redis
 */

object TrainTimeAnalysisRedis {

  def main(args: Array[String]): Unit = {
    Logger.getLogger("org").setLevel(Level.ERROR)


    //session
    val session = SparkSession
      .builder()
      .master("local[*]")
      .appName("trainTimeanalysis")
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
    //读取原始数据
    val frame = session.read.parquet("hdfs://mycluster/data/parquet_traindata/*/*")

    /**
     * 处理数据：（按照分区去处理）
     * 1.获取trainid
     * 2.获取list
     * 3.根据trainid获取traintime（访问的第三方的库）
     */

    frame.mapPartitions(partition => {
      //获取reids连接
      val lettuce = MakeRedisConnLettuce.getRedis()
      val tuples: Iterator[(String,List[Int])] = partition.map(row => {
        //获取trainid
        val trainId: String = row.getAs[String]("MPacketHead_TrainID")
        //获取list
        val list = MakeATPKPI.getATPKPI(row)
        //去redis中根据trainid获取traintime
        var trainTime: String = lettuce.hget("trainTime", trainId)
        //判断traintime是否有值
        if (StringUtils.isEmpty(trainTime)) {
          trainTime = trainId
        }
        (trainTime, list)
      })
      tuples
    }).rdd.reduceByKey{
      (list1,list2)=>list1.zip(list2).map(tp=>(tp._1+tp._2))
    }.foreach(println(_))

    //释放redis连接
    MakeRedisConnLettuce.close()
    //释放资源
    session.stop()
  }
}
