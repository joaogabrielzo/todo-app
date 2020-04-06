package com.zo.routes

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.model.{HttpResponse, StatusCodes}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import authentikat.jwt.JsonWebToken
import com.zo.config.{Authentication, PasswordEncrypt}
import com.zo.models.repositories.UsersRepository
import com.zo.models.{JsonProtocol, Login}

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

class LoginEndpoint(repo: UsersRepository, argon: PasswordEncrypt)(implicit ec: ExecutionContext) extends Authentication
                                                                                                  with JsonProtocol
                                                                                                  with
                                                                                                  SprayJsonSupport {
    
    val loginRoute: Route =
        (path("v1" / "login") & post) {
            entity(as[Login]) { login =>
                onComplete(repo.select(login.username)) {
                    case Success(Some(user)) =>
                        val checkPassword = argon.check(user.password, login.password)
                        if (checkPassword) {
                            val claims = setClaims(login.username, expirationInDays)
                            respondWithHeader(RawHeader("Access-Token", JsonWebToken(header, claims, secretKey))) {
                                complete(StatusCodes.OK)
                            }
                        } else {
                            complete(HttpResponse(StatusCodes.BadRequest, entity = "Password is incorrect."))
                        }
                    case Success(None)       =>
                        complete(HttpResponse(StatusCodes.BadRequest, entity = "You are not signed up."))
                    case Failure(ex)         =>
                        complete(StatusCodes.InternalServerError)
                }
            }
        }
    
}
