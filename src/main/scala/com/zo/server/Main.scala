package com.zo.server

import akka.actor.ActorSystem
import akka.http.scaladsl.server.{HttpApp, Route}
import com.zo.config.{MSQL, PasswordEncrypt}
import com.zo.models.Schemas
import com.zo.models.repositories.{TasksRepository, UsersRepository}
import com.zo.routes.{LoginEndpoint, TasksEndpoint, UsersEndpoint}

import scala.concurrent.ExecutionContextExecutor

object Main extends HttpApp
            with App
            with MSQL
            with Schemas {
    
    implicit val system: ActorSystem = ActorSystem()
    implicit val ec: ExecutionContextExecutor = system.dispatcher
    
    val userRepo = new UsersRepository
    val taskRepo = new TasksRepository
    
    val argon = new PasswordEncrypt
    
    val usersEndpoint = new UsersEndpoint(userRepo, argon).usersRoute
    val tasksEndpoint = new TasksEndpoint(taskRepo).tasksRoute
    val loginEndpoint = new LoginEndpoint(userRepo, argon).loginRoute
    
    val routes: Route = usersEndpoint ~ tasksEndpoint ~ loginEndpoint
    
    startServer("localhost", 9999)
}
