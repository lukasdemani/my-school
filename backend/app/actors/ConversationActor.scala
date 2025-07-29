package actors

import org.apache.pekko.actor.typed.{ActorRef, Behavior}
import org.apache.pekko.actor.typed.scaladsl.Behaviors
import org.apache.pekko.stream.scaladsl.{Flow, Source}
import org.apache.pekko.NotUsed
import models.{AIConversation, ConversationRequest, ConversationResponse}
import services.OpenAIService
import zio.*
import java.time.Instant
import scala.concurrent.duration.*

object ConversationActor {

  sealed trait Command
  case class StartConversation(userId: Long, topic: String, replyTo: ActorRef[ConversationStarted]) extends Command
  case class SendMessage(userId: Long, conversationId: Long, message: String, replyTo: ActorRef[MessageResponse]) extends Command
  case class EndConversation(userId: Long, conversationId: Long, replyTo: ActorRef[ConversationEnded]) extends Command
  case class GetConversationHistory(userId: Long, conversationId: Long, replyTo: ActorRef[ConversationHistory]) extends Command
  private case class ConversationTimeout(userId: Long, conversationId: Long) extends Command

  sealed trait Response
  case class ConversationStarted(conversationId: Long, systemMessage: String) extends Response
  case class MessageResponse(response: String, suggestions: List[String]) extends Response
  case class ConversationEnded(conversationId: Long, messageCount: Int, duration: Long) extends Response
  case class ConversationHistory(messages: List[ConversationMessage]) extends Response

  case class ConversationMessage(
    sender: String, // "user" or "ai"
    message: String,
    timestamp: Instant
  )

  case class ActiveConversation(
    id: Long,
    userId: Long,
    topic: String,
    messages: List[ConversationMessage],
    startTime: Instant,
    lastActivity: Instant
  )

  def apply(openAIService: OpenAIService): Behavior[Command] = {
    conversationManager(Map.empty[Long, ActiveConversation], openAIService, 0L)
  }

  private def conversationManager(
    conversations: Map[Long, ActiveConversation],
    openAIService: OpenAIService,
    nextId: Long
  ): Behavior[Command] = {
    Behaviors.receive { (context, message) =>
      message match {
        case StartConversation(userId, topic, replyTo) =>
          val conversationId = nextId + 1
          val systemMessage = getSystemMessage(topic)
          
          val conversation = ActiveConversation(
            id = conversationId,
            userId = userId,
            topic = topic,
            messages = List(ConversationMessage("system", systemMessage, Instant.now())),
            startTime = Instant.now(),
            lastActivity = Instant.now()
          )
          
          context.scheduleOnce(60.minutes, context.self, ConversationTimeout(userId, conversationId))
          
          replyTo ! ConversationStarted(conversationId, systemMessage)
          conversationManager(conversations + (conversationId -> conversation), openAIService, conversationId)

        case SendMessage(userId, conversationId, message, replyTo) =>
          conversations.get(conversationId) match {
            case Some(conversation) if conversation.userId == userId =>
              val userMessage = ConversationMessage("user", message, Instant.now())
              val updatedMessages = conversation.messages :+ userMessage
              
              // Simulate AI response (in real implementation, call OpenAI)
              val aiResponse = generateAIResponse(message, conversation.topic)
              val aiMessage = ConversationMessage("ai", aiResponse.message, Instant.now())
              
              val updatedConversation = conversation.copy(
                messages = updatedMessages :+ aiMessage,
                lastActivity = Instant.now()
              )
              
              replyTo ! MessageResponse(aiResponse.message, aiResponse.suggestions)
              conversationManager(conversations + (conversationId -> updatedConversation), openAIService, nextId)
              
            case _ =>
              replyTo ! MessageResponse("Conversation not found", List.empty)
              Behaviors.same
          }

        case EndConversation(userId, conversationId, replyTo) =>
          conversations.get(conversationId) match {
            case Some(conversation) if conversation.userId == userId =>
              val duration = java.time.Duration.between(conversation.startTime, Instant.now()).toMinutes
              replyTo ! ConversationEnded(conversationId, conversation.messages.length, duration)
              conversationManager(conversations - conversationId, openAIService, nextId)
              
            case _ =>
              replyTo ! ConversationEnded(conversationId, 0, 0)
              Behaviors.same
          }

        case GetConversationHistory(userId, conversationId, replyTo) =>
          conversations.get(conversationId) match {
            case Some(conversation) if conversation.userId == userId =>
              replyTo ! ConversationHistory(conversation.messages)
              
            case _ =>
              replyTo ! ConversationHistory(List.empty)
          }
          Behaviors.same

        case ConversationTimeout(userId, conversationId) =>
          context.log.info(s"Conversation $conversationId timeout for user $userId")
          conversationManager(conversations - conversationId, openAIService, nextId)
      }
    }
  }

  private def getSystemMessage(topic: String): String = topic match {
    case "restaurant" => "Du bist in einem deutschen Restaurant. Übe das Bestellen von Essen auf Deutsch."
    case "travel" => "Du planst eine Reise nach Deutschland. Übe Reise-Vokabular auf Deutsch."
    case "job_interview" => "Du hast ein Vorstellungsgespräch auf Deutsch. Übe professionelle Konversation."
    case _ => "Lass uns auf Deutsch sprechen! Ich helfe dir beim Üben."
  }

  private def generateAIResponse(userMessage: String, topic: String): ConversationResponse = {
    // Simplified response generation - in real implementation, use OpenAI
    val responses = Map(
      "restaurant" -> List(
        "Was möchten Sie bestellen?",
        "Haben Sie schon gewählt?",
        "Kann ich Ihnen etwas zu trinken bringen?"
      ),
      "travel" -> List(
        "Wohin möchten Sie reisen?",
        "Brauchen Sie Hilfe mit dem Ticket?",
        "Wie lange bleiben Sie in Deutschland?"
      )
    )
    
    val defaultResponses = List(
      "Das ist interessant! Können Sie mehr erzählen?",
      "Sehr gut! Versuchen Sie, einen ganzen Satz zu bilden.",
      "Prima! Ihr Deutsch wird immer besser."
    )
    
    val responseList = responses.getOrElse(topic, defaultResponses)
    val response = responseList(scala.util.Random.nextInt(responseList.length))
    
    ConversationResponse(
      message = response,
      conversationId = 0, // Will be set by caller
      suggestions = List("Ja, gerne", "Nein, danke", "Können Sie das wiederholen?")
    )
  }
}
