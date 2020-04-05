package com.zo.models

import java.sql.Date
import java.text.SimpleDateFormat

import spray.json._

import scala.util.Try

case class User(
                   id: Int = 0,
                   username: String,
                   password: String
               )

case class Task(
                   id: Int = 0,
                   user: String,
                   description: String,
                   deadline: Date = null,
                   completed: Boolean
               )

trait JsonProtocol extends DefaultJsonProtocol {
    
    implicit val dateMarshalling = new DateMarshalling().DateFormat
    
    implicit val userFormat: RootJsonFormat[User] = jsonFormat3(User)
    
    implicit val taskFormat: RootJsonFormat[Task] = jsonFormat5(Task)
}

class DateMarshalling {
    implicit object DateFormat extends JsonFormat[Date] {
        
        def write(date: Date) = JsString(dateToIsoString(date))
        def read(json: JsValue) = json match {
            case JsString(rawDate) =>
                parseIsoDateString(rawDate)
                    .fold(deserializationError(s"Expected ISO Date format, got $rawDate"))(identity)
            case error             => deserializationError(s"Expected JsString, got $error")
        }
    }
    
    private val localIsoDateFormatter = new ThreadLocal[SimpleDateFormat] {
        override def initialValue() = new SimpleDateFormat("dd-MM-yyyy")
    }
    
    private def dateToIsoString(date: Date) =
        localIsoDateFormatter.get().format(date)
    
    private def parseIsoDateString(date: String): Option[Date] =
        Try {localIsoDateFormatter.get().parse(date)}.asInstanceOf[Option[Date]]
}
