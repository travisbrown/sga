package edu.umd.mith.yajq

import akka.actor.{ ActorRef, Props }
import akka.pattern.{ ask, pipe }
import akka.util.ByteString
import redis.RedisBlockingClient
import scala.util.{ Failure, Success }
import spray.json._

import JobProtocol._

class QueueManager(name: String, handler: ActorRef) extends RedisActor {
  def this(handler: ActorRef) = this("jobs", handler)

  val inProcessName = s"$name:in-process"
  val blocking = RedisBlockingClient()
  val runner = context.actorOf(Props(classOf[JobRunner], handler), "runner")

  def processNext() = blocking.brpopplush(name, inProcessName)

  override def preStart() {
    processNext() pipeTo self
  }

  def receive = {
    case Some(bulk: ByteString) =>
      processNext() pipeTo self
      ask(runner, bulk.decodeString("UTF-8").asJson.convertTo[Job]).flatMap(
        _ => client.lrem(inProcessName, 0, bulk)
      )
  }
}

class JobRunner(handler: ActorRef) extends RedisActor {
  var counter = 0

  def receive = {
    case job @ Job(method, channel, parameters, _) =>
      counter += 1
      ask(handler, (method, parameters)).mapTo[JsValue].map(
        job.succeed
      ).recover {
        case err: Throwable => job.fail(err.getMessage)
      }.flatMap {
        (res: JobResult) => client.publish(channel, res)
      } pipeTo sender
  }
}

