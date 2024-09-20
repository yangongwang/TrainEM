package utils

import java.time.Duration
import java.time.temporal.ChronoUnit

import io.lettuce.core.api.StatefulRedisConnection
import io.lettuce.core.{RedisClient, RedisURI, SetArgs}


object MakeRedisConnLettuce {

  private lazy val  redisUri = RedisURI.builder()
    .withHost("master1")
    .withPort(6379)
    .withTimeout(Duration.of(10, ChronoUnit.SECONDS))
    .build()
  private lazy val redisClient = RedisClient.create(redisUri)
  private lazy val connection=redisClient.connect()


  def getRedis() = {
    val redisCommands = connection.sync()
    redisCommands
  }

  def close(): Unit ={
    connection.close()
    redisClient.shutdown()
  }

}
