package models

import play.api.libs.json._
import java.time.Instant

case class Lesson(
  id: Option[Long] = None,
  title: String,
  description: Option[String] = None,
  difficultyLevel: String = "BEGINNER",
  lessonType: String, // vocabulary, grammar, conversation
  content: JsValue,
  estimatedDuration: Option[Int] = None, // in minutes
  createdAt: Instant = Instant.now()
)

case class UserProgress(
  id: Option[Long] = None,
  userId: Long,
  lessonId: Long,
  completedAt: Instant = Instant.now(),
  score: Option[Int] = None,
  timeSpent: Option[Int] = None // in seconds
)

case class PracticeSession(
  id: Option[Long] = None,
  userId: Long,
  sessionType: String, // vocabulary, grammar, conversation
  startedAt: Instant = Instant.now(),
  completedAt: Option[Instant] = None,
  score: Option[Int] = None,
  content: JsValue
)

case class AIConversation(
  id: Option[Long] = None,
  userId: Long,
  conversationData: JsValue,
  createdAt: Instant = Instant.now()
)

// Request/Response DTOs
case class LessonCompletion(
  score: Int,
  timeSpent: Int
)

case class VocabularyPracticeRequest(
  vocabularyIds: List[Long],
  practiceType: String = "flashcard"
)

case class ConversationRequest(
  message: String,
  conversationId: Option[Long] = None
)

case class ConversationResponse(
  message: String,
  conversationId: Long,
  suggestions: List[String] = List.empty
)

object Lesson {
  implicit val lessonFormat: Format[Lesson] = Json.format[Lesson]
  implicit val userProgressFormat: Format[UserProgress] = Json.format[UserProgress]
  implicit val practiceSessionFormat: Format[PracticeSession] = Json.format[PracticeSession]
  implicit val aiConversationFormat: Format[AIConversation] = Json.format[AIConversation]
  implicit val lessonCompletionFormat: Format[LessonCompletion] = Json.format[LessonCompletion]
  implicit val vocabularyPracticeRequestFormat: Format[VocabularyPracticeRequest] = Json.format[VocabularyPracticeRequest]
  implicit val conversationRequestFormat: Format[ConversationRequest] = Json.format[ConversationRequest]
  implicit val conversationResponseFormat: Format[ConversationResponse] = Json.format[ConversationResponse]
}
