package controllers

import javax.inject._
import play.api.mvc._
import play.api.libs.json._
import models._
import models.User._  // Import JSON formatters
import services.AuthService
import zio._
import zio.Unsafe
import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm

@Singleton
class AuthController @Inject()(
  val controllerComponents: ControllerComponents,
  authService: AuthService
)(implicit ec: ExecutionContext) extends BaseController {

  private val jwtSecret = "your-secret-key" // TODO: Move to config
  private val algorithm = Algorithm.HMAC256(jwtSecret)

  def signUp(): Action[JsValue] = Action.async(parse.json) { request =>
    request.body.validate[SignUpRequest] match {
      case JsSuccess(signUpRequest, _) =>
        runZIO(authService.signUp(signUpRequest))
          .map(response => Ok(Json.toJson(response)))
          .recover {
            case ex: RuntimeException if ex.getMessage == "Email already exists" =>
              Conflict(Json.obj("error" -> "Email already exists"))
            case ex =>
              BadRequest(Json.obj("error" -> ex.getMessage))
          }
      case JsError(errors) =>
        Future.successful(BadRequest(Json.obj("error" -> "Invalid request format", "details" -> JsError.toJson(errors))))
    }
  }

  def signIn(): Action[JsValue] = Action.async(parse.json) { request =>
    request.body.validate[SignInRequest] match {
      case JsSuccess(signInRequest, _) =>
        runZIO(authService.signIn(signInRequest))
          .map(response => Ok(Json.toJson(response)))
          .recover {
            case ex: RuntimeException if ex.getMessage == "Invalid credentials" =>
              Unauthorized(Json.obj("error" -> "Invalid credentials"))
            case ex =>
              BadRequest(Json.obj("error" -> ex.getMessage))
          }
      case JsError(errors) =>
        Future.successful(BadRequest(Json.obj("error" -> "Invalid request format", "details" -> JsError.toJson(errors))))
    }
  }

  def refreshToken(): Action[JsValue] = Action.async(parse.json) { request =>
    (request.body \ "refreshToken").asOpt[String] match {
      case Some(refreshToken) =>
        runZIO(authService.refreshToken(refreshToken))
          .map(response => Ok(Json.toJson(response)))
          .recover {
            case ex: RuntimeException if ex.getMessage == "Invalid refresh token" =>
              Unauthorized(Json.obj("error" -> "Invalid refresh token"))
            case ex =>
              BadRequest(Json.obj("error" -> ex.getMessage))
          }
      case None =>
        Future.successful(BadRequest(Json.obj("error" -> "refreshToken is required")))
    }
  }

  def logout(): Action[JsValue] = Action.async(parse.json) { request =>
    withAuthenticatedUser(request) { userId =>
      (request.body \ "refreshToken").asOpt[String] match {
        case Some(refreshToken) =>
          runZIO(authService.logout(userId, refreshToken))
            .map(_ => Ok(Json.obj("message" -> "Logged out successfully")))
            .recover {
              case ex => BadRequest(Json.obj("error" -> ex.getMessage))
            }
        case None =>
          Future.successful(BadRequest(Json.obj("error" -> "refreshToken is required")))
      }
    }
  }

  def createWorkspace(): Action[JsValue] = Action.async(parse.json) { request =>
    withAuthenticatedUser(request) { userId =>
      request.body.validate[CreateWorkspaceRequest] match {
        case JsSuccess(workspaceRequest, _) =>
          runZIO(authService.createWorkspace(userId, workspaceRequest))
            .map(workspace => Created(Json.toJson(workspace)))
            .recover {
              case ex => BadRequest(Json.obj("error" -> ex.getMessage))
            }
        case JsError(errors) =>
          Future.successful(BadRequest(Json.obj("error" -> "Invalid request format", "details" -> JsError.toJson(errors))))
      }
    }
  }

  def getProfile(): Action[AnyContent] = Action.async { request =>
    withAuthenticatedUser(request) { userId =>
      runZIO(authService.getUserWithWorkspaces(userId))
        .map(user => Ok(Json.toJson(user)))
        .recover {
          case ex => BadRequest(Json.obj("error" -> ex.getMessage))
        }
    }
  }

  // Helper methods
  private def withAuthenticatedUser[T](request: Request[T])(action: UUID => Future[Result]): Future[Result] = {
    extractUserIdFromToken(request) match {
      case Some(userId) => action(userId)
      case None => Future.successful(Unauthorized(Json.obj("error" -> "Invalid or missing token")))
    }
  }

  private def extractUserIdFromToken[T](request: Request[T]): Option[UUID] = {
    request.headers.get("Authorization")
      .filter(_.startsWith("Bearer "))
      .map(_.substring(7))
      .flatMap(validateToken)
  }

  private def validateToken(token: String): Option[UUID] = {
    try {
      val verifier = JWT.require(algorithm).build()
      val decodedJWT = verifier.verify(token)
      val userIdString = decodedJWT.getSubject
      Some(UUID.fromString(userIdString))
    } catch {
      case _: Exception => None
    }
  }

  private def runZIO[A](zio: Task[A]): Future[A] = {
    Unsafe.unsafe { implicit unsafe =>
      Runtime.default.unsafe.runToFuture(zio)
    }
  }
}
