package utils

import org.apache.commons.lang.StringUtils
import org.apache.spark.sql.Row

object MakeSignalKPI {
  def getSignalKPI(row:Row) ={
    //获取atperror
    val signalError = row.getAs[String]("Signal_SignalError")
    //首先判断他是否为空
    val listAllError: List[Int] = if (StringUtils.isNotEmpty(signalError)) {
      val listError: List[Int] = if (signalError.contains("电源")) {
        List[Int](1, 0, 0, 0)
      } else if (signalError.contains("灯泡")) {
        List[Int](0, 1, 0, 0)
      } else if (signalError.contains("开灯继电器")) {
        List[Int](0, 0, 1, 0)
      } else if (signalError.contains("信号机接口电路")) {
        List[Int](0, 0, 0, 1)
      } else {
        List[Int](0, 0, 0, 0)
      }
      List[Int](1) ++ listError
    } else {
      List[Int](0, 0, 0, 0, 0)
    }
    List[Int](1)++listAllError
  }

  def getSignalKPI(row:Array[String]) ={
    //获取signalerror
    val signalError = row(35)
    //首先判断他是否为空
    val listAllError: List[Int] = if (StringUtils.isNotEmpty(signalError)) {
      val listError: List[Int] = if (signalError.contains("电源")) {
        List[Int](1, 0, 0, 0)
      } else if (signalError.contains("灯泡")) {
        List[Int](0, 1, 0, 0)
      } else if (signalError.contains("开灯继电器")) {
        List[Int](0, 0, 1, 0)
      } else if (signalError.contains("信号机接口电路")) {
        List[Int](0, 0, 0, 1)
      } else {
        List[Int](0, 0, 0, 0)
      }
      List[Int](1) ++ listError
    } else {
      List[Int](0, 0, 0, 0, 0)
    }
    List[Int](1)++listAllError
  }
}
