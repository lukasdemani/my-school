package controllers

import javax.inject._
import play.api.mvc._
import play.api.libs.json._
import services.OpenAIService
import models.ConversationRequest
import models.Lesson.conversationRequestFormat
import zio.*
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AIController @Inject()(
  val controllerComponents: ControllerComponents,
  openAIService: OpenAIService
)(implicit ec: ExecutionContext) extends BaseController {

  def chat() = Action.async(parse.json) { implicit request =>
    request.body.validate[ConversationRequest].fold(
      errors => Future.successful(BadRequest(Json.obj("error" -> "Invalid JSON"))),
      chatRequest => {
        val messages = List(
          services.OpenAIMessage("system", "Du bist ein hilfreicher Deutschlehrer. Antworte auf Deutsch und korrigiere Fehler höflich."),
          services.OpenAIMessage("user", chatRequest.message)
        )
        
        val effect = openAIService.generateResponse(messages)
          .map { response =>
            Ok(Json.obj(
              "response" -> response,
              "conversationId" -> chatRequest.conversationId.getOrElse(java.lang.System.currentTimeMillis())
            ))
          }
          .catchAll {
            case services.APIError(msg) => ZIO.succeed(BadGateway(Json.obj("error" -> s"AI service error: $msg")))
            case services.NetworkError(msg) => ZIO.succeed(ServiceUnavailable(Json.obj("error" -> s"Network error: $msg")))
            case ex => ZIO.succeed(InternalServerError(Json.obj("error" -> ex.getMessage)))
          }

        Unsafe.unsafe { implicit unsafe =>
          Runtime.default.unsafe.runToFuture(effect)
        }
      }
    )
  }

  def translate() = Action.async(parse.json) { implicit request =>
    val text = (request.body \ "text").asOpt[String]
    val targetLanguage = (request.body \ "targetLanguage").asOpt[String].getOrElse("de")
    
    text match {
      case Some(textToTranslate) =>
        val effect = openAIService.translateText(textToTranslate, targetLanguage)
          .map { translation =>
            Ok(Json.obj(
              "originalText" -> textToTranslate,
              "translatedText" -> translation,
              "targetLanguage" -> targetLanguage
            ))
          }
          .catchAll {
            case services.APIError(msg) => ZIO.succeed(BadGateway(Json.obj("error" -> s"Translation service error: $msg")))
            case ex => ZIO.succeed(InternalServerError(Json.obj("error" -> ex.getMessage)))
          }

        Unsafe.unsafe { implicit unsafe =>
          Runtime.default.unsafe.runToFuture(effect)
        }
      
      case None =>
        Future.successful(BadRequest(Json.obj("error" -> "Text field is required")))
    }
  }

  def explainGrammar() = Action.async(parse.json) { implicit request =>
    val sentence = (request.body \ "sentence").asOpt[String]
    
    sentence match {
      case Some(germanSentence) =>
        val effect = openAIService.explainGrammar(germanSentence)
          .map { explanation =>
            Ok(Json.obj(
              "sentence" -> germanSentence,
              "explanation" -> explanation
            ))
          }
          .catchAll {
            case services.APIError(msg) => ZIO.succeed(BadGateway(Json.obj("error" -> s"Grammar service error: $msg")))
            case ex => ZIO.succeed(InternalServerError(Json.obj("error" -> ex.getMessage)))
          }

        Unsafe.unsafe { implicit unsafe =>
          Runtime.default.unsafe.runToFuture(effect)
        }
      
      case None =>
        Future.successful(BadRequest(Json.obj("error" -> "Sentence field is required")))
    }
  }

  def generateExercise() = Action.async(parse.json) { implicit request =>
    val level = (request.body \ "level").asOpt[String].getOrElse("BEGINNER")
    val topic = (request.body \ "topic").asOpt[String].getOrElse("general")
    
    val messages = List(
      services.OpenAIMessage("system", s"Generate a German language exercise for $level level students on the topic of $topic. Include question, options, and correct answer in JSON format."),
      services.OpenAIMessage("user", s"Create a $level level exercise about $topic")
    )
    
    val effect = openAIService.generateResponse(messages)
      .map { response =>
        Ok(Json.obj(
          "exercise" -> response,
          "level" -> level,
          "topic" -> topic,
          "generated_at" -> java.time.Instant.now().toString
        ))
      }
      .catchAll {
        case services.APIError(msg) => ZIO.succeed(BadGateway(Json.obj("error" -> s"Exercise generation error: $msg")))
        case ex => ZIO.succeed(InternalServerError(Json.obj("error" -> ex.getMessage)))
      }

    Unsafe.unsafe { implicit unsafe =>
      Runtime.default.unsafe.runToFuture(effect)
    }
  }

  def getSuggestions() = Action.async(parse.json) { implicit request =>
    val context = (request.body \ "context").asOpt[String].getOrElse("general")
    val userLevel = (request.body \ "userLevel").asOpt[String].getOrElse("BEGINNER")
    
    val effect = openAIService.generateConversationSuggestions(context, userLevel)
      .map { suggestions =>
        Ok(Json.obj(
          "suggestions" -> suggestions,
          "context" -> context,
          "userLevel" -> userLevel
        ))
      }
      .catchAll {
        case services.APIError(msg) => ZIO.succeed(BadGateway(Json.obj("error" -> s"Suggestions service error: $msg")))
        case ex => ZIO.succeed(InternalServerError(Json.obj("error" -> ex.getMessage)))
      }

    Unsafe.unsafe { implicit unsafe =>
      Runtime.default.unsafe.runToFuture(effect)
    }
  }
}
