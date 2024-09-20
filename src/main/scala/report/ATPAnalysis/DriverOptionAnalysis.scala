package report.ATPAnalysis

import java.util.Properties

import config.ConfigHelper
import org.apache.commons.lang.StringUtils
import org.apache.spark.sql.{DataFrame, Dataset, SaveMode, SparkSession}

/**
  * 离线报表10
  * 司机违规操作ATP报警统计
  * 用sparkcore实现
  */


object DriverOptionAnalysis {
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
      //获取司机编号
      val driverId = row.getAs[String]("DriverInfo_DriverID")
      //获取atperror
      val atpError: String = row.getAs[String]("MATPBaseInfo_AtpError")
      //获取司机操作
      val DriverInfo_DriverOption = row.getAs[String]("DriverInfo_DriverOption")
      //获取司机操作是否合规
      val DriverInfo_Validit = row.getAs[String]("DriverInfo_Validit")
      //首先判断他是否为空
      val listAllError = if (StringUtils.isNotEmpty(DriverInfo_DriverOption)) {
        val list1: List[Int] = if (StringUtils.isNotEmpty(atpError)) {
          if (DriverInfo_Validit.equals("false")) {
            val listError: List[Int] = if (atpError.contains("车载主机")) {
              List[Int](1, 0, 0, 0, 0, 0, 0, 0)
            } else if (atpError.contains("无线传输单元")) {
              List[Int](0, 1, 0, 0, 0, 0, 0, 0)
            } else if (atpError.contains("应答器信息接收单元")) {
              List[Int](0, 0, 1, 0, 0, 0, 0, 0)
            } else if (atpError.contains("轨道电路信息读取器")) {
              List[Int](0, 0, 0, 1, 0, 0, 0, 0)
            } else if (atpError.contains("测速测距单元")) {
              List[Int](0, 0, 0, 0, 1, 0, 0, 0)
            } else if (atpError.contains("人机交互接口单元")) {
              List[Int](0, 0, 0, 0, 0, 1, 0, 0)
            } else if (atpError.contains("列车接口单元")) {
              List[Int](0, 0, 0, 0, 0, 0, 1, 0)
            } else if (atpError.contains("司法记录单元")) {
              List[Int](0, 0, 0, 0, 0, 0, 0, 1)
            } else {
              List[Int](0, 0, 0, 0, 0, 0, 0, 0)
            }
            List[Int](1) ++ listError
          } else {
            List[Int](0, 0, 0, 0, 0, 0, 0, 0, 0)
          }
        } else {
          List[Int](0, 0, 0, 0, 0, 0, 0, 0, 0)
        }
        List[Int](1) ++ list1
      } else {
        List[Int](0, 0, 0, 0, 0, 0, 0, 0, 0, 0)
      }
      (driverId, List[Int](1) ++ listAllError)
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
    result.map(tp => (tp._1, tp._2(0), tp._2(1), tp._2(2), tp._2(3), tp._2(4), tp._2(5), tp._2(6), tp._2(7), tp._2(8), tp._2(9), tp._2(10)))
      .toDF("driverId", "dataAll", "OptionAll","allerror", "main", "wifi", "balise", "TCR", "speed", "DMI", "JRU", "TIU")
      .write.mode(SaveMode.Overwrite).jdbc(ConfigHelper.url, "DriverOptionAnalysis", props)


    //释放资源
    session.stop()
  }
}
