package com.zo.models

import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.Date

import spray.json._

import scala.util.Try

case class User(
                   id: Int,
                   username: String,
                   password: String
               )
case class Task(
                   id: Int,
                   user: String,
                   description: String,
                   createdAt: Option[Timestamp],
                   completed: Boolean = false
               )
case class Login(
                    username: String,
                    password: String
                )

trait JsonProtocol extends DefaultJsonProtocol {
    
    implicit val dateMarshalling = new DateMarshalling().DateFormat
    
    implicit val userFormat: RootJsonFormat[User] = jsonFormat3(User)
    
    implicit val taskFormat: RootJsonFormat[Task] = jsonFormat5(Task)
    
    implicit val loginFormat: RootJsonFormat[Login] = jsonFormat2(Login)
}

class DateMarshalling {
    implicit object DateFormat extends JsonFormat[Timestamp] {
        
        def write(date: Timestamp) = JsString(dateToIsoString(date))
        def read(json: JsValue) = json match {
            case JsString(rawDate) =>
                parseIsoDateString(rawDate)
                    .fold(deserializationError(s"Expected ISO Date format, got $rawDate"))(identity)
            case error             => deserializationError(s"Expected JsString, got $error")
        }
    }
    
    private val localIsoDateFormatter = new ThreadLocal[SimpleDateFormat] {
        override def initialValue() = new SimpleDateFormat("dd-MM-yyyy HH:mm")
    }
    
    private def dateToIsoString(date: Timestamp) =
        localIsoDateFormatter.get().format(date)
    
    private def parseIsoDateString(date: String): Option[Timestamp] =
        Try {localIsoDateFormatter.get().parse(date)}.asInstanceOf[Option[Timestamp]]
}
