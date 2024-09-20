package utils

import beans.Logs
import org.apache.commons.lang.StringUtils
import org.apache.spark.sql.Row

object MakeATPKPI {
  def getATPKPI(row:Row) ={
    //获取atperror
    val atpError = row.getAs[String]("MATPBaseInfo_AtpError")
    //首先判断他是否为空
    val listAllError: List[Int] = if (StringUtils.isNotEmpty(atpError)) {
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
    List[Int](1)++listAllError
  }
  def getATPKPI(logClass:Logs) ={
    //获取atperror
    val atpError: String = logClass.MATPBaseInfo_AtpError
    //首先判断他是否为空
    val listAllError: List[Int] = if (StringUtils.isNotEmpty(atpError)) {
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
    List[Int](1)++listAllError
  }


  def getATPKPI(arr:Array[String]) ={
    //获取atperror
    val atpError = arr(17)
    //首先判断他是否为空
    val listAllError: List[Int] = if (StringUtils.isNotEmpty(atpError)) {
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
    List[Int](1)++listAllError
  }
}
