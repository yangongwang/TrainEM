import utils.{MakeRedisConn, MakeRedisConnLettuce}

object JedisTest {

  def main(args: Array[String]): Unit = {
    val lettuce = MakeRedisConnLettuce.getRedis()
    lettuce.hincrby("atpError", "abczbjs", 1000)

    println(lettuce.hexists("atpError", "abczbjs"))
  }

}
