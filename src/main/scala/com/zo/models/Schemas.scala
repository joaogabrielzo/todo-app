package com.zo.models

import java.sql.Timestamp

import com.zo.config.DB
import slick.lifted._

trait Schemas extends DB {
    
    
    import driver.api._
    
    class Users(tag: Tag) extends Table[User](tag, "users") {
        
        def id: Rep[Int] = column[Int]("id", O.PrimaryKey, O.AutoInc)
        def username: Rep[String] = column[String]("username")
        def password: Rep[String] = column[String]("password")
        
        def * : ProvenShape[User] = (id, username, password) <> (User.tupled, User.unapply)
    }
    
    class Tasks(tag: Tag) extends Table[Task](tag, "tasks") {
        
        def id: Rep[Int] = column[Int]("id", O.PrimaryKey, O.AutoInc)
        def user: Rep[String] = column[String]("user")
        def description: Rep[String] = column[String]("description")
        def createdAt: Rep[Timestamp] = column[Timestamp]("created_at")
        def completed: Rep[Boolean] = column[Boolean]("completed")
        
        def * : ProvenShape[Task] = (id, user, description, createdAt.?, completed) <> (Task.tupled, Task
            .unapply)
    }
}
