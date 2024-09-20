package report.ATPAnalysis

import java.io.File
import java.util.Properties

import com.google.gson.Gson
import config.ConfigHelper
import org.apache.commons.io.{FileUtils, IOUtils}
import org.apache.commons.lang.StringUtils
import org.apache.hadoop.fs.{FileSystem, Path}
import org.apache.spark.sql.{DataFrame, Dataset, SaveMode, SparkSession}
import scalikejdbc.{DB, SQL}
import scalikejdbc.config.DBs
import utils.MakeATPKPI

/**
  * 离线报表2
  *   配属铁路局ATP报警统计
  *   用sparkcore实现
  */


object AttachRWBureauAnalysis {
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
      //获取铁路局
      val MPacketHead_AttachRWBureau = row.getAs[String]("MPacketHead_AttachRWBureau")
      //获取list指标
      val list = MakeATPKPI.getATPKPI(row)
      (MPacketHead_AttachRWBureau,list)
    })
    //进行reducebykey
    //0, 1, 0, 0, 0, 0, 0, 0, 0
    //0, 1, 0, 0, 0, 0, 0, 0, 0
    //(0,0),(1,1)
    val result = kpiDataSet.rdd.reduceByKey {
      (list1, list2) => //list1 zip list2 map(x=>x._1+x._2)
        list1.zip(list2).map(x => x._1 + x._2)
    }
    result.foreach(println(_))


    //写入json方式1 dataframe
    //    result.map(tp => (tp._1, tp._2(0), tp._2(1), tp._2(2), tp._2(3), tp._2(4), tp._2(5), tp._2(6), tp._2(7), tp._2(8), tp._2(9)))
    //      .toDF("AttachRWBureau", "dataAll", "allerror", "main", "wifi", "balise", "TCR", "speed", "DMI", "JRU", "TIU")
    //      .write.mode(SaveMode.Overwrite).partitionBy("AttachRWBureau").json("F:\\项目\\高铁项目\\json0722")

    //写入json方式2  gson
    //获取hadoop配置文件
    val configuration = session.sparkContext.hadoopConfiguration
    //获取hdfs的客户端
    val fs: FileSystem = FileSystem.get(configuration)
    //判断文件是否存在
    val fileStr = "F:\\项目\\高铁项目\\json0722"
    val file = new File(fileStr)
    //    if (file.exists()){
    //      //删除文本的
    ////      file.delete()
    //      //只能删除本地
    ////      FileUtils.deleteDirectory(file)
    //    }
    //    val path = new Path(fileStr)
    //    if (fs.exists(path)){
    //      fs.delete(path,true)
    //    }
    //    result.map(tp => {
    //      //创建一个gson实例
    //      val gson = new Gson()
    //      //将每一条数据转成gson
    //      val jsonStr: String = gson.toJson(AttachRWBureau(tp._1,tp._2(0),tp._2(1),tp._2(2),tp._2(3),tp._2(4),tp._2(5),tp._2(6),tp._2(7),tp._2(8),tp._2(9)))
    //      jsonStr
    //    }).saveAsTextFile("F:\\项目\\高铁项目\\json0722")

    //写入mysql dataframe
    //    val props = new Properties()
    //    props.setProperty("driver",ConfigHelper.drvier)
    //    props.setProperty("user",ConfigHelper.user)
    //    props.setProperty("password",ConfigHelper.password)
    //    result.map(tp => (tp._1, tp._2(0), tp._2(1), tp._2(2), tp._2(3), tp._2(4), tp._2(5), tp._2(6), tp._2(7), tp._2(8), tp._2(9)))
    //      .toDF("AttachRWBureau", "dataAll", "allerror", "main", "wifi", "balise", "TCR", "speed", "DMI", "JRU", "TIU")
    //      .write.mode(SaveMode.Overwrite).jdbc(ConfigHelper.url,"AttachRWBureau0722",props)


    //写入mysql方式二  scalikejdbc,要求是原来有表
//    DBs.setup()
//    result.foreachPartition(partition => {
//      DB.localTx { implicit session =>
//        partition.foreach(tp => {
//          SQL("insert into AttachRWBureau0722 values (?,?,?,?,?,?,?,?,?,?,?)")
//            .bind(tp._1, tp._2(0), tp._2(1), tp._2(2), tp._2(3), tp._2(4), tp._2(5), tp._2(6), tp._2(7), tp._2(8), tp._2(9))
//            .update()
//            .apply()
//        })
//      }
//    })


    //释放资源
    session.stop()
  }
}


case class AttachRWBureau(
                           AttachRWBureau: String,
                           dataAll: Int,
                           allerror: Int,
                           main: Int,
                           wifi: Int,
                           balise: Int,
                           TCR: Int,
                           speed: Int,
                           DMI: Int,
                           JRU: Int,
                           TIU: Int
                         )