package utils

import config.ConfigHelper
import org.apache.kafka.common.TopicPartition
import org.apache.spark.streaming.kafka010.OffsetRange
import scalikejdbc.{DB, SQL}
import scalikejdbc.config.DBs

//管理偏移量
object ManagerOffset {
  //将偏移量写入到mysql中
  def saveOffset2Mysql(ranges: Array[OffsetRange]): Unit = {
    //导入scalikejdbc的依赖
    DBs.setup()
    //写sql语句，事物
    DB.localTx{implicit session=>
      ranges.foreach(osr=>{
        SQL("update offset0722 set offset=? where topic=? and partition=? and groupid=?")
          .bind(osr.untilOffset,osr.topic,osr.partition,ConfigHelper.groupid)
          .update()
          .apply()
      })
    }
  }

  //从mysql中读取偏移量
  def readOffsetFromMysql() ={
    DBs.setup()
    val map: Map[TopicPartition, Long] = DB.readOnly { implicit session =>
      SQL("select * from offset0722 where topic=? and groupid=?")
        .bind(ConfigHelper.topic(0), ConfigHelper.groupid)
        .map(line => (
          new TopicPartition(line.string("topic"), line.int("partition")),
          line.long("offset")
        )).list().apply()
    }.toMap
    map
  }
}
