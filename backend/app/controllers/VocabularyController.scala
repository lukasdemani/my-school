package controllers

import javax.inject._
import play.api.mvc._
import play.api.libs.json._
import services.{VocabularyService, AuthService}
import models.VocabularyCreate
import zio.*
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class VocabularyController @Inject()(
  val controllerComponents: ControllerComponents,
  vocabularyService: VocabularyService,
  authService: AuthService
)(implicit ec: ExecutionContext) extends BaseController {

  def getVocabulary() = Action.async { implicit request =>
    val level = request.getQueryString("level")
    val category = request.getQueryString("category")
    val limit = request.getQueryString("limit").map(_.toInt).getOrElse(50)
    val offset = request.getQueryString("offset").map(_.toInt).getOrElse(0)

    val effect = vocabularyService.getVocabulary(level, category, limit, offset)
      .map(vocabulary => Ok(Json.toJson(vocabulary)))
      .catchAll {
        case services.VocabularyDatabaseError(msg) => 
          ZIO.succeed(InternalServerError(Json.obj("error" -> s"Database error: $msg")))
        case ex => 
          ZIO.succeed(InternalServerError(Json.obj("error" -> ex.getMessage)))
      }

    Unsafe.unsafe { implicit unsafe =>
      Runtime.default.unsafe.runToFuture(effect)
    }
  }

  def getWord(id: Long) = Action.async { implicit request =>
    val effect = vocabularyService.getWordById(id)
      .map {
        case Some(word) => Ok(Json.toJson(word))
        case None => NotFound(Json.obj("error" -> "Word not found"))
      }
      .catchAll {
        case services.VocabularyDatabaseError(msg) => 
          ZIO.succeed(InternalServerError(Json.obj("error" -> s"Database error: $msg")))
        case ex => 
          ZIO.succeed(InternalServerError(Json.obj("error" -> ex.getMessage)))
      }

    Unsafe.unsafe { implicit unsafe =>
      Runtime.default.unsafe.runToFuture(effect)
    }
  }

  def createWord() = Action.async(parse.json) { implicit request =>
    request.body.validate[VocabularyCreate].fold(
      errors => Future.successful(BadRequest(Json.obj("error" -> "Invalid JSON"))),
      wordData => {
        val effect = vocabularyService.createWord(wordData)
          .map(word => Created(Json.toJson(word)))
          .catchAll {
            case services.VocabularyValidationError(msg) => 
              ZIO.succeed(BadRequest(Json.obj("error" -> msg)))
            case services.VocabularyDatabaseError(msg) => 
              ZIO.succeed(InternalServerError(Json.obj("error" -> s"Database error: $msg")))
            case ex => 
              ZIO.succeed(InternalServerError(Json.obj("error" -> ex.getMessage)))
          }

        Unsafe.unsafe { implicit unsafe =>
          Runtime.default.unsafe.runToFuture(effect)
        }
      }
    )
  }

  def updateWord(id: Long) = Action.async(parse.json) { implicit request =>
    request.body.validate[VocabularyCreate].fold(
      errors => Future.successful(BadRequest(Json.obj("error" -> "Invalid JSON"))),
      wordData => {
        val effect = vocabularyService.updateWord(id, wordData)
          .map {
            case Some(word) => Ok(Json.toJson(word))
            case None => NotFound(Json.obj("error" -> "Word not found"))
          }
          .catchAll {
            case services.VocabularyValidationError(msg) => 
              ZIO.succeed(BadRequest(Json.obj("error" -> msg)))
            case services.VocabularyDatabaseError(msg) => 
              ZIO.succeed(InternalServerError(Json.obj("error" -> s"Database error: $msg")))
            case ex => 
              ZIO.succeed(InternalServerError(Json.obj("error" -> ex.getMessage)))
          }

        Unsafe.unsafe { implicit unsafe =>
          Runtime.default.unsafe.runToFuture(effect)
        }
      }
    )
  }

  def deleteWord(id: Long) = Action.async { implicit request =>
    val effect = vocabularyService.deleteWord(id)
      .map { deleted =>
        if (deleted) Ok(Json.obj("message" -> "Word deleted"))
        else NotFound(Json.obj("error" -> "Word not found"))
      }
      .catchAll {
        case services.VocabularyDatabaseError(msg) => 
          ZIO.succeed(InternalServerError(Json.obj("error" -> s"Database error: $msg")))
        case ex => 
          ZIO.succeed(InternalServerError(Json.obj("error" -> ex.getMessage)))
      }

    Unsafe.unsafe { implicit unsafe =>
      Runtime.default.unsafe.runToFuture(effect)
    }
  }
}
