package edu.umd.mith.yajq

import akka.pattern.pipe
import java.net.InetSocketAddress
import java.util.UUID
import redis.actors.RedisSubscriberActor
import redis.api.pubsub._
import scala.collection.mutable.{ Map => MMap }
import scala.concurrent._
import scala.concurrent.duration._
import spray.json._

import JobProtocol._

class JobClient(
  val queue: String,
  val uuid: UUID
) extends RedisSubscriberActor(uuid.toString :: Nil, Nil) with RedisActor {
  def this() = this("jobs", UUID.randomUUID())
  override val address = new InetSocketAddress("localhost", 6379)
  private[this] val pending = MMap.empty[UUID, Promise[JsValue]]

  def onMessage(m: Message) = {
    val res = m.data.asJson.convertTo[JobResult]
    pending.remove(res.id).foreach { p =>
      res match {
        case JobSuccess(id, data) => p.success(data)
        case JobFailure(id, msg) => p.failure(new Exception(msg))
      }
    }
  }

  def onPMessage(pm: PMessage) = ()

  override def connected = super.connected orElse {
    case (method: String, parameters: JsValue) =>
      val p = promise[JsValue]
      val id = UUID.randomUUID()

      client.lpush(queue, Job(method, uuid.toString, parameters, id))
      pending(id) = p
      p.future pipeTo sender
  }
}

