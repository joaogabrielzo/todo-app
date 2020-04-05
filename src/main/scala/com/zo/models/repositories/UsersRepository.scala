package com.zo.models.repositories

import com.zo.config.MSQL
import com.zo.models.{Schemas, User}

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.language.postfixOps

class UsersRepository(implicit ec: ExecutionContext) extends Schemas with MSQL {
    
    import driver.api._
    
    private val table = TableQuery[Users]
    
    Await.result(db.run(table.schema.createIfNotExists), 3 seconds)
    
    def insert(user: User): Future[Int] =
        db.run(table += user)
    
    def select(username: String): Future[Option[User]] =
        db.run(table.filter(_.username === username).result.headOption)
    
    def update(user: User): Future[Int] =
        db.run(table.filter(_.username === user.username).update(user))
    
    def checkUser(username: String): Future[Boolean] = {
        db.run(table.filter(_.username === username).exists.result)
    }
    
}
