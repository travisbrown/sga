package edu.umd.mith.yajq

import akka.util.ByteString
import java.util.UUID
import redis.RedisValueConverter
import spray.json._

case class Job(method: String, channel: String, parameters: JsValue, id: UUID) {
  def succeed(data: JsValue) = JobSuccess(id, data)
  def fail(msg: String) = JobFailure(id, msg)
}

sealed trait JobResult {
  def id: UUID
}

case class JobSuccess(id: UUID, data: JsValue) extends JobResult
case class JobFailure(id: UUID, msg: String) extends JobResult

object JobProtocol extends DefaultJsonProtocol {
  implicit val uuidFormat = new JsonFormat[UUID] {
    def read(json: JsValue) = json match {
      case JsString(s) => UUID.fromString(s)
      case _ => deserializationError("Can't parse UUID!")
    }
    def write(uuid: UUID) = JsString(uuid.toString)
  }

  implicit val jobFormat = jsonFormat4(Job.apply)

  implicit val jobResultFormat = new RootJsonFormat[JobResult] {
    def read(json: JsValue) = json match {
      case JsObject(fields) =>
        val id = fields("id").convertTo[UUID]
        
        if (fields("success").convertTo[Boolean])
          JobSuccess(id, fields("data"))
        else
          JobFailure(id, fields("error").convertTo[String])
      case _ => deserializationError("Can't parse job result!")
    }
    def write(res: JobResult) = res match {
      case JobSuccess(id, data) => JsObject(
        "success" -> true.toJson,
        "id" -> id.toJson,
        "data" -> data
      )
      case JobFailure(id, msg) => JsObject(
        "success" -> false.toJson,
        "id" -> id.toJson,
        "error" -> msg.toJson
      )
    }
  }

  implicit val jobConverter = new RedisValueConverter[Job] {
    def from(job: Job) = ByteString(job.toJson.compactPrint)
  }

  implicit val jobResultConverter = new RedisValueConverter[JobResult] {
    def from(res: JobResult) = ByteString(res.toJson.compactPrint)
  }
}

