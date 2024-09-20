package utils

import org.apache.spark.rdd.RDD

object MakeBaliseKPIStream {
  //获取铁路总公司报警数
  def getDayKPI(reduceData: RDD[(String, List[Int],String,String, String)]): Unit ={
    reduceData.map(tp=>(tp._1,tp._2)).reduceByKey{
      (list1,list2)=> list1.zip(list2).map(tp=>tp._2+tp._1)
    }.foreachPartition(partition => {
      //获取redis连接
      val jedis = MakeRedisConn.getRedisThread(7)
      partition.foreach(tp => {
        //写入redis  hincby
        jedis.hincrBy("baliseError", tp._1+"总报警数", tp._2(1))
        jedis.hincrBy("baliseError", tp._1+"应答器", tp._2(2))
        jedis.hincrBy("baliseError", tp._1+"道岔", tp._2(3))
        jedis.hincrBy("baliseError", tp._1+"转辙机", tp._2(4))
        jedis.hincrBy("baliseError", tp._1+"轨道电路", tp._2(5))
      })
      //关闭redis连接
      jedis.close()
    })
  }


  //获取每小时铁路总公司报警数
  def getHourKPI(reduceData: RDD[(String, List[Int],String,String, String)]): Unit ={
    reduceData.map(tp=>(tp._3,tp._2)).reduceByKey{
      (list1,list2)=> list1.zip(list2).map(tp=>tp._2+tp._1)
    }.foreachPartition(partition => {
      //获取redis连接
      val jedis = MakeRedisConn.getRedisThread(7)
      partition.foreach(tp => {
        //写入redis  hincby
        jedis.hincrBy("baliseError", tp._1+"总报警数", tp._2(1))
        jedis.hincrBy("baliseError", tp._1+"应答器", tp._2(2))
        jedis.hincrBy("baliseError", tp._1+"道岔", tp._2(3))
        jedis.hincrBy("baliseError", tp._1+"转辙机", tp._2(4))
        jedis.hincrBy("baliseError", tp._1+"轨道电路", tp._2(5))
      })
      //关闭redis连接
      jedis.close()
    })
  }

  //获取每分钟铁路总公司报警数
  def getMinuteKPI(reduceData: RDD[(String, List[Int],String,String, String)]): Unit ={
    reduceData.map(tp=>(tp._4,tp._2)).reduceByKey{
      (list1,list2)=> list1.zip(list2).map(tp=>tp._2+tp._1)
    }.foreachPartition(partition => {
      //获取redis连接
      val jedis = MakeRedisConn.getRedisThread(7)
      partition.foreach(tp => {
        //写入redis  hincby
        jedis.hincrBy("baliseError", tp._1+"总报警数", tp._2(1))
        jedis.hincrBy("baliseError", tp._1+"应答器", tp._2(2))
        jedis.hincrBy("baliseError", tp._1+"道岔", tp._2(3))
        jedis.hincrBy("baliseError", tp._1+"转辙机", tp._2(4))
        jedis.hincrBy("baliseError", tp._1+"轨道电路", tp._2(5))
      })
      //关闭redis连接
      jedis.close()
    })
  }

  //获取铁路局公司报警数
  def getDayAndBureauKPI(reduceData: RDD[(String, List[Int],String,String, String)]): Unit ={
    reduceData.map(tp=>((tp._1,tp._5),tp._2)).reduceByKey{
      (list1,list2)=> list1.zip(list2).map(tp=>tp._2+tp._1)
    }.foreachPartition(partition => {
      //获取redis连接
      val jedis = MakeRedisConn.getRedisThread(7)
      partition.foreach(tp => {
        //写入redis  hincby
        jedis.hincrBy("baliseError", tp._1+"总报警数", tp._2(1))
        jedis.hincrBy("baliseError", tp._1+"应答器", tp._2(2))
        jedis.hincrBy("baliseError", tp._1+"道岔", tp._2(3))
        jedis.hincrBy("baliseError", tp._1+"转辙机", tp._2(4))
        jedis.hincrBy("baliseError", tp._1+"轨道电路", tp._2(5))
      })
      //关闭redis连接
      jedis.close()
    })
  }


  //获取每小时铁路总公司报警数
  def getHourBureauKPI(reduceData: RDD[(String, List[Int],String,String, String)]): Unit ={
    reduceData.map(tp=>((tp._3,tp._5),tp._2)).reduceByKey{
      (list1,list2)=> list1.zip(list2).map(tp=>tp._2+tp._1)
    }.foreachPartition(partition => {
      //获取redis连接
      val jedis = MakeRedisConn.getRedisThread(7)
      partition.foreach(tp => {
        //写入redis  hincby
        jedis.hincrBy("baliseError", tp._1+"总报警数", tp._2(1))
        jedis.hincrBy("baliseError", tp._1+"应答器", tp._2(2))
        jedis.hincrBy("baliseError", tp._1+"道岔", tp._2(3))
        jedis.hincrBy("baliseError", tp._1+"转辙机", tp._2(4))
        jedis.hincrBy("baliseError", tp._1+"轨道电路", tp._2(5))
      })
      //关闭redis连接
      jedis.close()
    })
  }

  //获取每分钟铁路总公司报警数
  def getMinuteBureauKPI(reduceData: RDD[(String, List[Int],String,String, String)]): Unit ={
    reduceData.map(tp=>((tp._4,tp._5),tp._2)).reduceByKey{
      (list1,list2)=> list1.zip(list2).map(tp=>tp._2+tp._1)
    }.foreachPartition(partition => {
      //获取redis连接
      val jedis = MakeRedisConn.getRedisThread(7)
      partition.foreach(tp => {
        //写入redis  hincby
        jedis.hincrBy("atpError", tp._1+"总报警数", tp._2(1))
        jedis.hincrBy("atpError", tp._1+"main", tp._2(2))
        jedis.hincrBy("atpError", tp._1+"无线传输单元", tp._2(3))
        jedis.hincrBy("atpError", tp._1+"应答器信息接收单元", tp._2(4))
        jedis.hincrBy("atpError", tp._1+"轨道电路信息读取器", tp._2(5))
        jedis.hincrBy("atpError", tp._1+"测速测距单元", tp._2(6))
        jedis.hincrBy("atpError", tp._1+"人机交互接口单元", tp._2(7))
        jedis.hincrBy("atpError", tp._1+"列车接口单元", tp._2(8))
        jedis.hincrBy("atpError", tp._1+"司法记录单元", tp._2(9))
      })
      //关闭redis连接
      jedis.close()
    })
  }
}
