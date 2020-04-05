package com.zo.routes

import akka.event.slf4j.Logger
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.zo.config.PasswordEncrypt
import com.zo.models.repositories.UsersRepository
import com.zo.models.{JsonProtocol, User}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

class UsersEndpoint(repo: UsersRepository, argon: PasswordEncrypt)(implicit ec: ExecutionContext)
    extends JsonProtocol
    with SprayJsonSupport {
    
    val log = Logger("user-endpoint")
    
    val usersRoute: Route =
        pathPrefix("v1" / "users") {
            ((get & path(Segment))) { username =>
                
                onComplete(repo.select(username)) {
                    case Success(Some(user)) =>
                        complete(user)
                    case Success(None)       =>
                        complete(HttpResponse(status = StatusCodes.NotFound))
                    case Failure(e)          =>
                        complete(HttpResponse(entity = e.getMessage, status = StatusCodes.InternalServerError))
                }
            } ~
            (post & entity(as[User])) { user =>
                
                onComplete(repo.checkUser(user.username)) {
                    case Success(true)  =>
                        complete(HttpResponse(StatusCodes.BadRequest,
                            entity = HttpEntity(ContentTypes.`text/plain(UTF-8)`, "User already exists.")))
                    case Success(false) =>
                        val encryptedPassword: String = argon.encrypt(user.password)
                        
                        onComplete(repo.insert(User(username = user.username, password = encryptedPassword))) {
                            case Success(_)  =>
                                log.info(s"User ${user.username} created!")
                                complete(HttpResponse(StatusCodes.Created,
                                    entity = HttpEntity(ContentTypes.`text/plain(UTF-8)`,
                                        s"User ${user.username} created!")))
                            case Failure(ex) =>
                                log.warn(s"User creation failed with $ex")
                                complete(HttpEntity(ContentTypes.`text/plain(UTF-8)`,
                                    s"Sorry, couldn't create user ${user.username}"))
                        }
                    case Failure(e)     =>
                        complete(HttpResponse(StatusCodes.InternalServerError, entity = e.getMessage))
                }
                
            }
        }
    
}
