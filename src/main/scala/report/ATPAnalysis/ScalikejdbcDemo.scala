package report.ATPAnalysis

import scalikejdbc.config.DBs
import scalikejdbc.{DB, SQL}

/**
  * 测试练习：scalikejdbc
  */

object ScalikejdbcDemo {
  def main(args: Array[String]): Unit = {
    //scalikejdbc是高效的，因为scalikejdbc主动帮助咱们维护线程
    //加载配置文件
    DBs.setup()
    //    //插入数据
    //    DB.autoCommit{implicit session=>
    //      //写sql语句
    //      SQL("insert into ip0723 values (?,?)")
    //      //绑定参数
    //        .bind("北京",100)
    //      //进行提交
    //        .update()
    //      //发送请求
    //        .apply()
    //    }

    //删除数据
    //    DB.autoCommit{implicit session=>
    //      //写sql语句
    //      SQL("delete from ip0722 where province=?")
    //      //绑定参数
    //        .bind("北京")
    //      //执行
    //        .update()
    //      //发送请求
    //        .apply()
    //    }

    //修改数据
    //    DB.autoCommit { implicit session =>
    //      //写sql语句
    //      SQL("update ip0722 set count=? where count=?")
    //        //绑定参数
    //        .bind(12, 1825)
    //        //执行
    //        .update()
    //        //发送请求
    //        .apply()
    //    }

    //查询数据
    //    val tuples: List[(String, Int)] = DB.readOnly { implicit session =>
    //      //写sql语句
    //      SQL("select * from ip0722")
    //        //提取字段
    //        .map(ra => (
    //        ra.string("province"),
    //        ra.int("count")
    //      ))
    //        //将所有元组组合成list
    //        .list()
    //        //只读不需要update
    //        .apply()
    //    }
    //    println(tuples)


    //插入100条数据
    //    for (i <- 0 until 10) {
    //      DB.autoCommit { implicit session =>
    //        if (i == 5) {
    //          SQL("insert into ip0722 values (?,?)")
    //            .bind("河北", "kkkk")
    //            .update()
    //            .apply()
    //        } else {
    //          SQL("insert into ip0722 values (?,?)")
    //            .bind("河北", 20)
    //            .update()
    //            .apply()
    //        }
    //      }

    //事物
    DB.localTx { implicit session =>
      for (i <- 0 until 10) {
        if (i == 5) {
          SQL("insert into ip0722 values (?,?)")
            .bind("辽宁", "kkkk")
            .update()
            .apply()
        } else {
          SQL("insert into ip0722 values (?,?)")
            .bind("辽宁", 20)
            .update()
            .apply()
        }
      }

    }

  }

}
