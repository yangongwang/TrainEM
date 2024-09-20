package utils

import org.apache.spark.sql.Row

object MakeOnlineWeather {
  //获取湿度区间
  def getHumLevel(hum: Double) = {
    //判断hum的情况
    if (hum >= 0 && hum <= 20) {
      "hum:0~20"
    } else if (hum > 20 && hum <= 40) {
      "hum:20~40"
    } else if (hum > 40 && hum <= 60) {
      "hum:40~60"
    } else if (hum > 60 && hum <= 80) {
      "hum:60~80"
    } else if (hum > 80 && hum <= 100) {
      "hum:80~100"
    } else {
      "hum:数据不合法"
    }
  }


  //获取温度区间
  def getTemLevel(tem: Double) = {
    //判断tem的情况
    if (tem >= -30 && tem <= -10) {
      "tem:-30~-10"
    } else if (tem > -10 && tem <= 10) {
      "tem:-10~10"
    } else if (tem > 10 && tem <= 30) {
      "tem:10~30"
    } else if (tem > 30 && tem <= 50) {
      "tem:30~50"
    } else {
      "tem:数据不合法"
    }
  }

  //获取天气情况
  def getWeaLevel(wea: String) = {
    //获取天气
    "wea:"+wea
  }

  //获取速度区间
  def getSpeLevel(spe: Double) = {
    //判断spe的情况
    if (spe >= 0 && spe <= 100) {
      "spe:0~100"
    } else if (spe > 100 && spe <= 200) {
      "spe:100~200"
    } else if (spe > 200 && spe <= 300) {
      "spe:200~300"
    } else if (spe > 300 && spe <= 350) {
      "spe:300~350"
    } else {
      "spe:数据不合法"
    }
  }
}
