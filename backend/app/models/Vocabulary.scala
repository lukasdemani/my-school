package models

import play.api.libs.json._
import java.time.Instant

case class Vocabulary(
  id: Option[Long] = None,
  germanWord: String,
  englishTranslation: String,
  portugueseTranslation: String,
  pronunciation: Option[String] = None,
  wordType: String, // noun, verb, adjective, etc.
  difficultyLevel: String = "BEGINNER",
  category: Option[String] = None,
  exampleSentenceDe: Option[String] = None,
  exampleSentenceEn: Option[String] = None,
  exampleSentencePt: Option[String] = None,
  audioUrl: Option[String] = None,
  createdAt: Instant = Instant.now()
)

case class VocabularyCreate(
  germanWord: String,
  englishTranslation: String,
  portugueseTranslation: String,
  pronunciation: Option[String] = None,
  wordType: String,
  difficultyLevel: String = "BEGINNER",
  category: Option[String] = None,
  exampleSentenceDe: Option[String] = None,
  exampleSentenceEn: Option[String] = None,
  exampleSentencePt: Option[String] = None
)

case class UserVocabularyProgress(
  id: Option[Long] = None,
  userId: Long,
  vocabularyId: Long,
  masteryLevel: Int = 0, // 0-5 scale
  lastPracticed: Instant = Instant.now(),
  correctAnswers: Int = 0,
  totalAttempts: Int = 0
)

object Vocabulary {
  implicit val vocabularyFormat: Format[Vocabulary] = Json.format[Vocabulary]
  implicit val vocabularyCreateFormat: Format[VocabularyCreate] = Json.format[VocabularyCreate]
  implicit val userVocabularyProgressFormat: Format[UserVocabularyProgress] = Json.format[UserVocabularyProgress]
}
