package utils

import redis.clients.jedis.{Jedis, JedisPool}

object MakeRedisConn {

  private lazy val pool = new JedisPool("master1", 6379)
  def getRedisThread(index:Int=0) = {

    val jedis: Jedis = pool.getResource
    jedis.select(index)
    jedis
  }
}
