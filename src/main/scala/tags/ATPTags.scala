package tags

import java.text.SimpleDateFormat

import config.ConfigHelper
import org.apache.commons.lang.StringUtils
import org.apache.log4j.{Level, Logger}
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SparkSession
import utils.MakeWeather


//用户画像1：针对于atp做用户画像
object ATPTags {
  def main(args: Array[String]): Unit = {
    Logger.getLogger("org").setLevel(Level.ERROR)
    //session
    val sesssion = SparkSession
      .builder()
      .master("local[*]")
      .appName(this.getClass.getName)
      .config("spark.serializer", ConfigHelper.serializer)
      .getOrCreate()

    val sc = sesssion.sparkContext
    sc.hadoopConfiguration.set("fs.defaultFS", "hdfs://mycluster")
    sc.hadoopConfiguration.set("dfs.nameservices", "mycluster")
    sc.hadoopConfiguration.set("dfs.ha.namenodes.mycluster", "nn1,nn2")
    sc.hadoopConfiguration.set("dfs.name" +
      "node.rpc-address.mycluster.nn1", "master1:9000")
    sc.hadoopConfiguration.set("dfs.namenode.rpc-address.mycluster.nn2", "master2:9000")
    sc.hadoopConfiguration.set("dfs.client.failover.proxy.provider.mycluster", "org.apache.hadoop.hdfs.server.namenode.ha.ConfiguredFailoverProxyProvider")


    //导入隐式转换
    import sesssion.implicits._
    //读取日志文件
    val source = sesssion.read.parquet("hdfs://mycluster/data/parquet_traindata/*").cache()
    //读取列车出厂时间数据
    val trainTimeSource = sesssion.sparkContext.textFile("hdfs://mycluster/字典文件/列车出厂时间数据.txt")
    val trainTimeMap = trainTimeSource.map(_.split("\\|",-1)).filter(_.length>=2).map(arr=>(arr(0),arr(1))).collectAsMap()
    val trainTimeBro = sesssion.sparkContext.broadcast(trainTimeMap)
    //读取列车生产厂家数据
    val trainFaSource = sesssion.sparkContext.textFile("hdfs://mycluster/字典文件/列车生产厂家.txt")
    val trainFaMap = trainFaSource.map(_.split("\\|",-1)).filter(_.length>=2).map(arr=>(arr(0),arr(1))).collectAsMap()
    val trainFaBro = sesssion.sparkContext.broadcast(trainFaMap)
    //读取列车检修台账数据
    val trainCheckSource = sesssion.sparkContext.textFile("hdfs://mycluster/检修台账/ATP检修台账.txt")
    val trainCheckMap = trainCheckSource.map(_.split("\\|",-1)).filter(_.length>=4).map(arr=>(arr(0),(arr(1),arr(2),arr(3)))).collectAsMap()
    val trainCheckBro = sesssion.sparkContext.broadcast(trainCheckMap)
    //打个数的标签
    val countTags: RDD[(String, List[(String, Long)])] = source.map(row => {
      //获取唯一标识
      val trainId: String = row.getAs[String]("MPacketHead_TrainID")
      //获取去atperror
      val atpError = row.getAs[String]("MATPBaseInfo_AtpError")
      //创建一个人容器来存放各种标签
      var list = List[(String, Long)]()
      //判断atpError是否为空
      if (StringUtils.isNotEmpty(atpError)) {
        //要获取湿度、温度、天气、速度等级
        val humLevel = MakeWeather.getHumLevel(row)
        val temLevel = MakeWeather.getTemLevel(row)
        val weather = MakeWeather.getWeaLevel(row)
        val speLevel = MakeWeather.getSpeLevel(row)
        //打标签
        list :+= ("PA" + humLevel + "/" + temLevel + "/" + weather + "/" + speLevel + ":" + atpError, 1L)
      }

      //出厂时间标签
      val trainTime = trainTimeBro.value.getOrElse(trainId, "无返回值")
      if (!trainTime.equals("无返回值")) {
        list :+= ("TI" + trainTime, 1L)
      }

      //atp类型
      val atpType = row.getAs[String]("MPacketHead_ATPType")
      if (StringUtils.isNotEmpty(atpType)) {
        list :+= ("TY" + atpType, 1L)
      }

      //司机标签
      val drvierId = row.getAs[String]("DriverInfo_DriverID")
      if (StringUtils.isNotEmpty(drvierId)) {
        list :+= ("DR" + drvierId, 1L)
      }

      //厂家标签
      val trainFa = trainFaBro.value.getOrElse(trainId.substring(0, 1), "无返回值")
      if (!trainFa.equals("无返回值")) {
        list :+= ("FA" + trainFa, 1L)
      }

      //检修更换标签
      val tuple: (String, String, String) = trainCheckBro.value.getOrElse(trainId, ("", "", ""))
      if (StringUtils.isNotEmpty(tuple._1)) {
        if (tuple._1.equals("检修")) {
          list :+= ("AJX" + tuple._2 + tuple._3, 1L)
        } else {
          list :+= ("AGH" + tuple._2 + tuple._3, 1L)
        }
      }
      (trainId, list)
    }).rdd.reduceByKey {
      (list1, list2) => (list1 ++ list2).groupBy(_._1).mapValues(tp => tp.map(_._2).sum).toList
    }


    //时长标签
    val timeTags: RDD[(String, List[(String, Long)])] = source.map(row => {
      //获取唯一标识
      val trainId: String = row.getAs[String]("MPacketHead_TrainID")
      //要获取湿度、温度、天气、速度等级
      val humLevel = MakeWeather.getHumLevel(row)
      val temLevel = MakeWeather.getTemLevel(row)
      val weather = MakeWeather.getWeaLevel(row)
      val speLevel = MakeWeather.getSpeLevel(row)
      //获取数据时间
      val dataTimeStr = row.getAs[String]("MATPBaseInfo_DataTime")
      //dataTimeStr转时间戳
      val format = new SimpleDateFormat("yyyyMMddhhmmss")
      val timeStapm: Long = format.parse(dataTimeStr).getTime
      //打标签
      //创建一个容器来存放各种标签
      var list = List[(String, Long)]()
      list :+= (humLevel, timeStapm)
      list :+= (temLevel, timeStapm)
      list :+= (weather, timeStapm)
      list :+= (speLevel, timeStapm)

      (trainId, list)
    }).rdd.reduceByKey((list1, list2) => list1 ++ list2)
      .map(tp => {
        val list = tp._2.sliding(5).map(li => (li.head._1, li.last._2 - li.head._2)).toList
          .groupBy(_._1).mapValues(tp => tp.map(_._2).sum).toList
        (tp._1, list)
      })

    //聚合
    countTags.union(timeTags).reduceByKey((li1,li2)=>li1++li2).foreach(println(_))

    //释放资源
    sesssion.stop()
  }
}
