package edu.umd.mith.yajq

import akka.actor.{ Actor, ActorSystem, Props }
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent._
import scala.concurrent.duration._
import scala.util.Random
import spray.json._

object DemoClient extends App {
  implicit val system = ActorSystem()
  implicit val timeout = Timeout(10000 seconds)
  import system.dispatcher

  val clientCount = 1000
  val jobsEach = 100
  val rand = new Random(1)

  def randomStuff() = JsObject(
    "a" -> JsNumber(rand.nextDouble()),
    "b" -> JsString(List.fill(20)(rand.nextPrintableChar()).mkString),
    "c" -> JsBoolean(rand.nextBoolean()),
    "d" -> JsNumber(rand.nextInt())
  )

  val clients = List.tabulate(clientCount)(i =>
    system.actorOf(Props[JobClient], "client-%04d".format(i))
  )

  Thread.sleep(10000)

  val res = Future.traverse(clients) { client =>
    Future.traverse((0 until jobsEach).map(_ => randomStuff()))(
      i => client ? ("echo", i)
    )
  }

  val before = System.currentTimeMillis
  Await.result(res, duration.Duration.Inf)
  printf(
    "Processed %d jobs from %d clients in %d ms!\n",
    jobsEach * clientCount,
    clientCount,
    System.currentTimeMillis - before
  )
}

class Echo extends Actor {
  def receive = {
    case ("echo", msg) => sender ! msg
  }
}

object DemoServer extends App {
  implicit val system = ActorSystem()
  val handler = system.actorOf(Props[Echo], "echo")
  val manager = system.actorOf(Props(classOf[QueueManager], handler), "manager")
}

