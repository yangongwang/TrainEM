package tools

import java.sql.DriverManager

import config.ConfigHelper
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SparkSession
import scalikejdbc.{DB, SQL}
import scalikejdbc.config.DBs

object trainTime2Mysql {
  def main(args: Array[String]): Unit = {
    //session
    val session = SparkSession
      .builder()
      .master("local[*]")
      .appName(this.getClass.getName)
      .getOrCreate()
    //读取数据
    val trainTimeSource: RDD[String] = session.sparkContext.textFile("F:\\项目\\高铁项目\\列车出厂时间数据.txt")
    //预处理切分
    val filtedRDD: RDD[Array[String]] = trainTimeSource.map(_.split("\\|",-1)).filter(_.length>=2)
    //灌入mysql
    //导入scalikejdbc的依赖库
    DBs.setup()
    filtedRDD.foreachPartition(partition=>{
      //事物
      DB.localTx{implicit session=>
        partition.foreach(arr=>{
          SQL("insert into trainTime0722 values (?,?)")
            .bind(arr(0),arr(1))
            .update()
            .apply()
        })
      }
    })
    //释放资源
    session.stop()
  }
}
