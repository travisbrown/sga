package edu.umd.mith.yajq

import akka.actor.Actor
import akka.util.Timeout
import redis.RedisClient
import scala.concurrent.duration._

trait RedisActor extends Actor {
  implicit val timeout = Timeout(10000 seconds)
  implicit val system = context.system
  implicit val exec = context.dispatcher

  val client = RedisClient()
}

