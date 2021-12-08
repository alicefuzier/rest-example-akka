package example.rest

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, StatusCodes}
import akka.http.scaladsl.server.Directives.{post, _}
import example.models.EmailAccount
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.server.{ExceptionHandler, Route}
import spray.json._

import scala.io.StdIn

trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val emailFormat = jsonFormat2(EmailAccount)
}

object ExampleApi extends JsonSupport {

  implicit val myExceptionHandler: ExceptionHandler =
    ExceptionHandler {
      case _: ArithmeticException =>
        extractUri { uri =>
          println(s"Request to $uri could not be handled normally")
          complete(StatusCodes.BadRequest,"Bad numbers, bad result!!!")
        }
    }

  def main(args: Array[String]): Unit = {
    implicit val system = ActorSystem()
    implicit val executionContext = system.dispatcher

    val route1 = get {
      concat(
        pathSingleSlash {
          complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, "<html><body>Hello world!</body></html>"))
        },

        path("emailaccount" / IntNumber){ i =>
          complete(s"got ${10/i}")
        },
        path("emailaccount") {

          parameters("email", "name".optional) { (email, name) =>
            failWith(new RuntimeException("BOOM!"))
          }

        }
      )
    }
    val route2 = post {
      entity(as[EmailAccount]) { account =>
        complete(account.toString)
      }
    }~ post {
      entity(as[String]) { account =>
        complete(s"Received string: $account")
      }
    }
    val route: Route =
      handleExceptions(myExceptionHandler){route1 ~ route2}



    val res =   Http().newServerAt("localhost", 8080).bind(route)
    StdIn.readLine() // let it run until user presses return
    res
      .flatMap(_.unbind()) // trigger unbinding from the port
      .onComplete(_ => system.terminate()) // and shutdown when done

  }
}
