package controllers

import javax.inject._
import play.api.mvc._
import play.api.libs.json._
import org.apache.pekko.actor.typed.{ActorSystem, ActorRef}
import org.apache.pekko.util.Timeout
import actors.{LearningSessionActor, ConversationActor}
import services.{AuthService, OpenAIService}
import models.{VocabularyPracticeRequest, ConversationRequest}
import models.Lesson.{vocabularyPracticeRequestFormat, conversationRequestFormat}
import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._

@Singleton
class PracticeController @Inject()(
  val controllerComponents: ControllerComponents,
  authService: AuthService,
  openAIService: OpenAIService
)(implicit system: ActorSystem[_], ec: ExecutionContext) extends BaseController {

  private implicit val timeout: Timeout = 30.seconds
  
  private val learningSessionActor: ActorRef[LearningSessionActor.Command] = 
    system.systemActorOf(LearningSessionActor(), "learning-session")
  
  private val conversationActor: ActorRef[ConversationActor.Command] = 
    system.systemActorOf(ConversationActor(openAIService), "conversation")

  def practiceVocabulary() = Action.async(parse.json) { implicit request =>
    request.body.validate[VocabularyPracticeRequest].fold(
      errors => Future.successful(BadRequest(Json.obj("error" -> "Invalid JSON"))),
      practiceRequest => {
        import org.apache.pekko.actor.typed.scaladsl.AskPattern._
        
        learningSessionActor.ask[LearningSessionActor.SessionResponse] { replyTo =>
          LearningSessionActor.StartVocabularySession(1L, practiceRequest.vocabularyIds, replyTo)
        }.map { response =>
          Ok(Json.obj(
            "sessionId" -> response.sessionId,
            "vocabularyIds" -> response.vocabularyIds,
            "message" -> "Vocabulary practice session started"
          ))
        }.recover {
          case ex => InternalServerError(Json.obj("error" -> ex.getMessage))
        }
      }
    )
  }

  def submitVocabularyAnswer() = Action.async(parse.json) { implicit request =>
    val userId = (request.body \ "userId").as[Long]
    val vocabularyId = (request.body \ "vocabularyId").as[Long]
    val answer = (request.body \ "answer").as[String]
    val isCorrect = (request.body \ "isCorrect").as[Boolean]

    import org.apache.pekko.actor.typed.scaladsl.AskPattern._
    
    learningSessionActor.ask[LearningSessionActor.AnswerResponse] { replyTo =>
      LearningSessionActor.SubmitAnswer(userId, vocabularyId, answer, isCorrect, replyTo)
    }.map { response =>
      Ok(Json.obj(
        "isCorrect" -> response.isCorrect,
        "score" -> response.score,
        "nextVocabularyId" -> response.nextVocabularyId
      ))
    }.recover {
      case ex => InternalServerError(Json.obj("error" -> ex.getMessage))
    }
  }

  def practiceGrammar() = Action.async(parse.json) { implicit request =>
    Future.successful(Ok(Json.obj(
      "message" -> "Grammar practice not yet implemented",
      "exercises" -> List(
        Json.obj(
          "type" -> "case_selection",
          "sentence" -> "Ich gebe ___ Kind einen Apfel.",
          "options" -> List("das", "dem", "den", "der"),
          "correct" -> "dem"
        ),
        Json.obj(
          "type" -> "verb_conjugation",
          "sentence" -> "Du ___ heute nach Hause.",
          "options" -> List("gehe", "gehst", "geht", "gehen"),
          "correct" -> "gehst"
        )
      )
    )))
  }

  def conversationPractice() = Action.async(parse.json) { implicit request =>
    request.body.validate[ConversationRequest].fold(
      errors => Future.successful(BadRequest(Json.obj("error" -> "Invalid JSON"))),
      conversationRequest => {
        import org.apache.pekko.actor.typed.scaladsl.AskPattern._
        
        conversationRequest.conversationId match {
          case Some(conversationId) =>
            conversationActor.ask[ConversationActor.MessageResponse] { replyTo =>
              ConversationActor.SendMessage(1L, conversationId, conversationRequest.message, replyTo)
            }.map { response =>
              Ok(Json.obj(
                "response" -> response.response,
                "suggestions" -> response.suggestions
              ))
            }
          
          case None =>
            conversationActor.ask[ConversationActor.ConversationStarted] { replyTo =>
              ConversationActor.StartConversation(1L, "general", replyTo)
            }.map { response =>
              Ok(Json.obj(
                "conversationId" -> response.conversationId,
                "systemMessage" -> response.systemMessage,
                "message" -> "New conversation started"
              ))
            }
        }
      }
    ).recover {
      case ex => InternalServerError(Json.obj("error" -> ex.getMessage))
    }
  }

  def getSessionStats(userId: Long) = Action.async { implicit request =>
    import org.apache.pekko.actor.typed.scaladsl.AskPattern._
    
    learningSessionActor.ask[LearningSessionActor.SessionStatsResponse] { replyTo =>
      LearningSessionActor.GetSessionStats(userId, replyTo)
    }.map { stats =>
      Ok(Json.obj(
        "currentScore" -> stats.currentScore,
        "questionsRemaining" -> stats.questionsRemaining,
        "timeElapsed" -> stats.timeElapsed
      ))
    }.recover {
      case ex => InternalServerError(Json.obj("error" -> ex.getMessage))
    }
  }

  def endSession(userId: Long) = Action.async { implicit request =>
    import org.apache.pekko.actor.typed.scaladsl.AskPattern._
    
    learningSessionActor.ask[LearningSessionActor.SessionEndResponse] { replyTo =>
      LearningSessionActor.EndSession(userId, replyTo)
    }.map { endResponse =>
      Ok(Json.obj(
        "totalScore" -> endResponse.totalScore,
        "totalTime" -> endResponse.totalTime,
        "correctAnswers" -> endResponse.correctAnswers,
        "totalQuestions" -> endResponse.totalQuestions,
        "accuracy" -> (if (endResponse.totalQuestions > 0) 
          endResponse.correctAnswers.toDouble / endResponse.totalQuestions * 100 
        else 0.0)
      ))
    }.recover {
      case ex => InternalServerError(Json.obj("error" -> ex.getMessage))
    }
  }
}
