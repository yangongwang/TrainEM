package etl

import java.text.SimpleDateFormat
import java.util.Date
import org.apache.log4j.{Level, Logger}
import beans.{Logs, LogSchema}
import config.ConfigHelper
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{DataFrame, Row, SaveMode, SparkSession}

object Log2Parquet {
  def main(args: Array[String]): Unit = {
//    if (args.length < 1) {
//      println("参数错误")
//      return
//    }

    Logger.getLogger("org").setLevel(Level.ERROR)
    //session
    val session = SparkSession
      .builder()
      .master("local[*]")
      .appName(s"${this.getClass.getSimpleName}")
      .config("spark.serializer", ConfigHelper.serializer)
      .config("spark.sql.parquet.compression.codec", ConfigHelper.codec)
      .getOrCreate()
    //导入隐式转换
    import session.implicits._
    //读取数据  args(代表传入的参数)，args（0）代表读入的hdfs的原始数据
    val sc = session.sparkContext
    sc.hadoopConfiguration.set("fs.defaultFS", "hdfs://mycluster")
    sc.hadoopConfiguration.set("dfs.nameservices", "mycluster")
    sc.hadoopConfiguration.set("dfs.ha.namenodes.mycluster", "nn1,nn2")
    sc.hadoopConfiguration.set("dfs.name" +
      "node.rpc-address.mycluster.nn1", "master1:9000")
    sc.hadoopConfiguration.set("dfs.namenode.rpc-address.mycluster.nn2", "master2:9000")
    sc.hadoopConfiguration.set("dfs.client.failover.proxy.provider.mycluster", "org.apache.hadoop.hdfs.server.namenode.ha.ConfiguredFailoverProxyProvider")
   // val source: RDD[String] = session.sparkContext.textFile(args(0))
   val source: RDD[String] = session.sparkContext.textFile("hdfs://mycluster/flume/traindata/*/*/*/*")
    //val source: RDD[String] = session.sparkContext.textFile("file:///home/data/TrainData_min.txt")
    //val source: RDD[String] = session.sparkContext.textFile("hdfs://mycluster/flume/events/23-04-25/1730/00/FlumeData.1682415213061")

    //切分并且过滤第一步
    val splitRDD: RDD[Array[String]] = source.map(_.split("\\|", -1)).filter(_.length >= 55).cache()
    //获取当前时间
    val format = new SimpleDateFormat("yyyyMMddHHmmss")
    val date = new Date()
    val time: Long = date.getTime
    //代码中尽量少使用filter，效率比较差的
    val filted300T: RDD[Array[String]] = splitRDD.filter(arr => arr(0).startsWith("300T")
      && !(arr(17).contains("复位") || arr(17).contains("SB待机") || arr(17).contains("常用制动"))
      && time - format.parse(arr(7)).getTime <= 30000000000000L
      && !(arr(17).contains("休眠") || arr(17).contains("未知") || arr(17).contains("无致命错误") || arr(17).contains("一致性消息错误") || arr(17).contains("NVMEM故障"))).cache()
    //拿出所有atperror中带当前ATP系统处于故障中[SF]的
    val filted300TATP: RDD[Array[String]] = filted300T.filter(arr => arr(17).contains("当前ATP系统处于故"))
      .map(arr => arr(0) + "|" +
        arr(1) + "|" +
        arr(2) + "|" +
        arr(3) + "|" +
        arr(4) + "|" +
        arr(5) + "|" +
        arr(6) + "|" +
        arr(7).substring(0, 12) + "00|" +
        arr(8) + "|" +
        arr(9) + "|" +
        arr(10) + "|" +
        arr(11) + "|" +
        arr(12) + "|" +
        arr(13) + "|" +
        arr(14) + "|" +
        arr(15) + "|" +
        arr(16) + "|" +
        arr(17) + "|" +
        arr(18) + "|" +
        arr(19) + "|" +
        arr(20) + "|" +
        arr(21) + "|" +
        arr(22) + "|" +
        arr(23) + "|" +
        arr(24) + "|" +
        arr(25) + "|" +
        arr(26) + "|" +
        arr(27) + "|" +
        arr(28) + "|" +
        arr(29) + "|" +
        arr(30) + "|" +
        arr(31) + "|" +
        arr(32) + "|" +
        arr(33) + "|" +
        arr(34) + "|" +
        arr(35) + "|" +
        arr(36) + "|" +
        arr(37) + "|" +
        arr(38) + "|" +
        arr(39) + "|" +
        arr(40) + "|" +
        arr(41) + "|" +
        arr(42) + "|" +
        arr(43) + "|" +
        arr(44) + "|" +
        arr(45) + "|" +
        arr(46) + "|" +
        arr(47) + "|" +
        arr(48) + "|" +
        arr(49) + "|" +
        arr(50) + "|" +
        arr(51) + "|" +
        arr(52) + "|" +
        arr(53) + "|" +
        arr(54)
      ).map(_.split("\\|", -1))

    //300S及300SATO
    val filed300S = splitRDD.filter(arr => arr(0).startsWith("300S")
      && !arr(17).contains("休眠")
      && !(arr(9).contains("CTCS-3") && (arr(17).contains("SB") || arr(17).contains("备系"))))

    //300H
    val filted300H = splitRDD.filter(arr => arr(0).equals("300H") && !arr(17).contains("休眠"))

    //200H
    val filted200H = splitRDD.filter(arr => arr(0).equals("200H") && !arr(17).contains("休眠") && !arr(17).contains("VC2"))

    //将所有过滤完的数据进行取并集
    val rddresult = filted300T.filter(arr => !arr(17).contains("当前ATP系统处于故"))
      .union(filted300TATP)
      .union(filed300S)
      .union(filted300H)
      .union(filted200H)
      .union(splitRDD.filter(arr => !arr(0).startsWith("300T") && !arr(0).startsWith("300S") && !arr(0).contains("H")))

    //创建一个rddrow
    //    val rddRow: RDD[Row] = rddresult.map(arr => Row(
    //      arr(0),
    //      arr(1),
    //      arr(2),
    //      arr(3),
    //      arr(4),
    //      arr(5),
    //      arr(6),
    //      arr(7),
    //      arr(8),
    //      arr(9),
    //      arr(10),
    //      arr(11),
    //      arr(12),
    //      arr(13),
    //      arr(14),
    //      arr(15),
    //      arr(16),
    //      arr(17),
    //      arr(18),
    //      arr(19),
    //      arr(20),
    //      arr(21),
    //      arr(22),
    //      arr(23),
    //      arr(24),
    //      arr(25),
    //      arr(26),
    //      arr(27),
    //      arr(28),
    //      arr(29),
    //      arr(30),
    //      arr(31),
    //      arr(32),
    //      arr(33),
    //      arr(34),
    //      arr(35),
    //      arr(36),
    //      arr(37),
    //      arr(38),
    //      arr(39),
    //      arr(40),
    //      arr(41),
    //      arr(42),
    //      arr(43),
    //      arr(44),
    //      arr(45),
    //      arr(46),
    //      arr(47),
    //      arr(48),
    //      arr(49),
    //      arr(50),
    //      arr(51),
    //      arr(52),
    //      arr(53),
    //      arr(54)
    //    ))
    //    rddRow
    //
    //
    //    //将数据转成parquet（row+schema）=dataframe
    //    val frame: DataFrame = session.createDataFrame(rddRow,logSchema.shema)

    val logRDD: RDD[Logs] = rddresult.map(arr => Logs(
      arr(0),
      arr(1),
      arr(2),
      arr(3),
      arr(4),
      arr(5),
      arr(6),
      arr(7),
      arr(8),
      arr(9),
      arr(10),
      arr(11),
      arr(12),
      arr(13),
      arr(14),
      arr(15),
      arr(16),
      arr(17),
      arr(18),
      arr(19),
      arr(20),
      arr(21),
      arr(22),
      arr(23),
      arr(24),
      arr(25),
      arr(26),
      arr(27),
      arr(28),
      arr(29),
      arr(30),
      arr(31),
      arr(32),
      arr(33),
      arr(34),
      arr(35),
      arr(36),
      arr(37),
      arr(38),
      arr(39),
      arr(40),
      arr(41),
      arr(42),
      arr(43),
      arr(44),
      arr(45),
      arr(46),
      arr(47),
      arr(48),
      arr(49),
      arr(50),
      arr(51),
      arr(52),
      arr(53),
      arr(54)
    ))
    val frame = session.createDataFrame(logRDD)

    frame.write.mode(SaveMode.Overwrite).parquet("/data/parquet_traindata")
//    frame.write.mode(SaveMode.Overwrite).save("file:///home/data/parquet0722")



    //释放资源
    session.stop()
  }
}
