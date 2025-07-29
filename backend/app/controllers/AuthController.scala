package controllers

import javax.inject._
import play.api.mvc._
import play.api.libs.json._
import services.{AuthService, UserService}
import models.{UserRegistration, UserLogin}
import zio.*
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AuthController @Inject()(
  val controllerComponents: ControllerComponents,
  authService: AuthService
)(implicit ec: ExecutionContext) extends BaseController {

  def register() = Action.async(parse.json) { implicit request =>
    request.body.validate[UserRegistration].fold(
      errors => Future.successful(BadRequest(Json.obj("error" -> "Invalid JSON"))),
      registration => {
        val effect = authService.register(registration)
          .map {
            case Some(token) => Created(Json.obj("token" -> token))
            case None => BadRequest(Json.obj("error" -> "Email already exists"))
          }
          .catchAll {
            case services.DatabaseError(msg) => ZIO.succeed(InternalServerError(Json.obj("error" -> s"Database error: $msg")))
            case services.ValidationError(msg) => ZIO.succeed(BadRequest(Json.obj("error" -> msg)))
            case ex => ZIO.succeed(InternalServerError(Json.obj("error" -> ex.getMessage)))
          }

        Unsafe.unsafe { implicit unsafe =>
          Runtime.default.unsafe.runToFuture(effect)
        }
      }
    )
  }

  def login() = Action.async(parse.json) { implicit request =>
    request.body.validate[UserLogin].fold(
      errors => Future.successful(BadRequest(Json.obj("error" -> "Invalid JSON"))),
      loginData => {
        val effect = authService.login(loginData.email, loginData.password)
          .map {
            case Some(token) => Ok(Json.obj("token" -> token))
            case None => Unauthorized(Json.obj("error" -> "Invalid credentials"))
          }
          .catchAll {
            case services.DatabaseError(msg) => ZIO.succeed(InternalServerError(Json.obj("error" -> s"Database error: $msg")))
            case ex => ZIO.succeed(InternalServerError(Json.obj("error" -> ex.getMessage)))
          }

        Unsafe.unsafe { implicit unsafe =>
          Runtime.default.unsafe.runToFuture(effect)
        }
      }
    )
  }

  def refreshToken() = Action.async { implicit request =>
    request.headers.get("Authorization").map(_.replace("Bearer ", "")) match {
      case Some(token) =>
        val effect = authService.refreshToken(token)
          .map {
            case Some(newToken) => Ok(Json.obj("token" -> newToken))
            case None => Unauthorized(Json.obj("error" -> "Invalid token"))
          }
          .catchAll {
            case services.TokenError(msg) => ZIO.succeed(Unauthorized(Json.obj("error" -> msg)))
            case ex => ZIO.succeed(InternalServerError(Json.obj("error" -> ex.getMessage)))
          }

        Unsafe.unsafe { implicit unsafe =>
          Runtime.default.unsafe.runToFuture(effect)
        }
      case None =>
        Future.successful(BadRequest(Json.obj("error" -> "Authorization header missing")))
    }
  }
}
