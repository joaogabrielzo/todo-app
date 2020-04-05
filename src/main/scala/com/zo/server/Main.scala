package com.zo.server

import akka.actor.ActorSystem
import akka.http.scaladsl.server.{HttpApp, Route}
import com.zo.config.{MSQL, PasswordEncrypt}
import com.zo.models.Schemas
import com.zo.models.repositories.UsersRepository
import com.zo.routes.UsersEndpoint

import scala.concurrent.ExecutionContextExecutor

object Main extends HttpApp
            with App
            with MSQL
            with Schemas {
    
    implicit val system: ActorSystem = ActorSystem()
    implicit val ec: ExecutionContextExecutor = system.dispatcher
    
    val userRepo = new UsersRepository
    val argon = new PasswordEncrypt
    
    val usersEndpoint = new UsersEndpoint(userRepo, argon).usersRoute
    
    val routes: Route = usersEndpoint
    
    startServer("localhost", 9999)
}
