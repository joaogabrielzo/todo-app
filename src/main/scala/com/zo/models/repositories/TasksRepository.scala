package com.zo.models.repositories

import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.Date

import com.zo.config.{DB, MSQL}
import com.zo.models.{Schemas, Task}

import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.duration._
import scala.language.postfixOps

class TasksRepository(implicit ec: ExecutionContext) extends Schemas with MSQL {
    
    private object getNow {
        
        val formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss")
        val currentDatetime = new Date
        val sqlTimestamp: Timestamp = new Timestamp(currentDatetime.getTime)
    }
    
    import driver.api._
    
    val table = TableQuery[Tasks]
    
    Await.result(db.run(table.schema.createIfNotExists), 3 seconds)
    
    def insert(task: Task): Future[Int] =
        db.run(table += task)
    
    def selectId(id: Int): Future[Option[Task]] =
        db.run(table.filter(_.id === id).result.headOption)
    
    def selectByUser(user: String): Future[Option[Task]] =
        db.run(table.filter(_.user === user).result.headOption)
    
    def update(task: Task): Future[Int] =
        db.run(table.filter(_.id === task.id).update(task))
    
    def deleteId(id: Int): Future[Boolean] =
        db.run(table.filter(_.id === id).delete.map(_ > 0))
    
}
