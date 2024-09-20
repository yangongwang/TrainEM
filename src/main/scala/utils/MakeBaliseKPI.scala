package utils

import beans.Logs
import org.apache.commons.lang.StringUtils
import org.apache.spark.sql.Row

object MakeBaliseKPI {
  def getBaliseKPI(row:Row) ={
    //获取atperror
    val baliseError = row.getAs[String]("MBalisePocket_BaliseError")
    //首先判断他是否为空
    val listAllError: List[Int] = if (StringUtils.isNotEmpty(baliseError)) {
      val listError: List[Int] = if (baliseError.contains("应答器")) {
        List[Int](1, 0, 0, 0)
      } else if (baliseError.contains("道岔")) {
        List[Int](0, 1, 0, 0)
      } else if (baliseError.contains("转辙机")) {
        List[Int](0, 0, 1, 0)
      } else if (baliseError.contains("轨道电路")) {
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

  def getBaliseKPI(arr:Array[String]) ={
    //获取atperror
    val baliseError = arr(26)
    //首先判断他是否为空
    val listAllError: List[Int] = if (StringUtils.isNotEmpty(baliseError)) {
      val listError: List[Int] = if (baliseError.contains("应答器")) {
        List[Int](1, 0, 0, 0)
      } else if (baliseError.contains("道岔")) {
        List[Int](0, 1, 0, 0)
      } else if (baliseError.contains("转辙机")) {
        List[Int](0, 0, 1, 0)
      } else if (baliseError.contains("轨道电路")) {
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
