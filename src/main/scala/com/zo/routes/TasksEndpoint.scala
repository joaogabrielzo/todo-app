package com.zo.routes

import akka.event.slf4j.Logger
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.zo.models.repositories.TasksRepository
import com.zo.models.{JsonProtocol, Task}

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

class TasksEndpoint(repo: TasksRepository)(implicit ec: ExecutionContext) extends JsonProtocol
                                                                          with SprayJsonSupport {
    
    val log = Logger("tasks-endpoint")
    
    val tasksRoute: Route =
        pathPrefix("v1" / "tasks") {
            (post & entity(as[Task])) { task =>
                onComplete(repo.insert(task)) {
                    case Success(id) =>
                        log.info(s"Added task with id $id")
                        complete(StatusCodes.Created)
                    case Failure(ex) =>
                        log.warn(s"Adding task failed with: $ex")
                        complete(StatusCodes.InternalServerError)
                }
            } ~
            (put & entity(as[Task])) { newTask =>
                onComplete(repo.update(newTask)) {
                    case Success(id) =>
                        log.info(s"Updated task with id $id")
                        complete(StatusCodes.OK)
                    case Failure(ex) =>
                        log.warn(s"Updating task failed with: $ex")
                        complete(StatusCodes.InternalServerError)
                }
            } ~
            (delete & path(IntNumber)) { taskId =>
                onComplete(repo.deleteId(taskId)) {
                    case Success(true)  =>
                        log.info(s"Deleted task $taskId")
                        complete(StatusCodes.OK)
                    case Success(false) =>
                        log.warn(s"Failed on deleting task $taskId")
                        complete(StatusCodes.BadRequest)
                    case Failure(ex)    =>
                        log.warn(s"Deleting task $taskId failed with: $ex")
                        complete(StatusCodes.InternalServerError)
                }
            } ~
            (get & path(Segment)) { user =>
                onComplete(repo.selectByUser(user)) {
                    case Success(Some(_)) =>
                        log.info(s"Retrieved all tasks for user $user")
                        complete(StatusCodes.OK)
                    case Success(None)    =>
                        log.info(s"Didn't retrieve any task for user $user")
                        complete(HttpResponse(StatusCodes.BadRequest, entity = s"User $user doesn't have any task"))
                    case Failure(ex)      =>
                        log.warn(s"Retrieving task for user $user failed with: $ex")
                        complete(StatusCodes.InternalServerError)
                }
            } ~
            (get & path(IntNumber)) { id =>
                onComplete(repo.selectId(id)) {
                    case Success(Some(_)) =>
                        log.info(s"Retrieved task with id $id")
                        complete(StatusCodes.OK)
                    case Success(None)    =>
                        log.info(s"Didn't retrieve any task for id $id")
                        complete(HttpResponse(StatusCodes.BadRequest, entity = s"Task with id $id doesn't exist"))
                    case Failure(ex)      =>
                        log.warn(s"Retrieving task with $id failed with: $ex")
                        complete(StatusCodes.InternalServerError)
                }
            }
        }
    
}
