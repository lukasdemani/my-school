package services

import javax.inject._
import models.{Vocabulary, VocabularyCreate}
import repositories.VocabularyRepository
import zio.*
import zio.stream.*
import scala.concurrent.{Future, ExecutionContext}

trait VocabularyService {
  def getVocabulary(level: Option[String], category: Option[String], limit: Int, offset: Int): ZIO[Any, VocabularyError, List[Vocabulary]]
  def getWordById(id: Long): ZIO[Any, VocabularyError, Option[Vocabulary]]
  def createWord(wordData: VocabularyCreate): ZIO[Any, VocabularyError, Vocabulary]
  def updateWord(id: Long, wordData: VocabularyCreate): ZIO[Any, VocabularyError, Option[Vocabulary]]
  def deleteWord(id: Long): ZIO[Any, VocabularyError, Boolean]
  def getWordsStream(batchSize: Int = 100): ZStream[Any, VocabularyError, Vocabulary]
}

sealed trait VocabularyError extends Throwable
case class VocabularyNotFound(id: Long) extends VocabularyError
case class VocabularyDatabaseError(message: String) extends VocabularyError
case class VocabularyValidationError(message: String) extends VocabularyError

@Singleton
class VocabularyServiceImpl @Inject()(
  vocabularyRepository: VocabularyRepository
)(implicit ec: ExecutionContext) extends VocabularyService {

  def getVocabulary(
    level: Option[String],
    category: Option[String],
    limit: Int,
    offset: Int
  ): ZIO[Any, VocabularyError, List[Vocabulary]] = {
    ZIO.fromFuture(_ => vocabularyRepository.findFiltered(level, category, limit, offset))
      .mapError(ex => VocabularyDatabaseError(ex.getMessage))
  }

  def getWordById(id: Long): ZIO[Any, VocabularyError, Option[Vocabulary]] = {
    ZIO.fromFuture(_ => vocabularyRepository.findById(id))
      .mapError(ex => VocabularyDatabaseError(ex.getMessage))
  }

  def createWord(wordData: VocabularyCreate): ZIO[Any, VocabularyError, Vocabulary] = {
    for {
      _ <- validateVocabularyData(wordData)
      word = Vocabulary(
        germanWord = wordData.germanWord,
        englishTranslation = wordData.englishTranslation,
        portugueseTranslation = wordData.portugueseTranslation,
        pronunciation = wordData.pronunciation,
        wordType = wordData.wordType,
        difficultyLevel = wordData.difficultyLevel,
        category = wordData.category,
        exampleSentenceDe = wordData.exampleSentenceDe,
        exampleSentenceEn = wordData.exampleSentenceEn,
        exampleSentencePt = wordData.exampleSentencePt
      )
      created <- ZIO.fromFuture(_ => vocabularyRepository.create(word))
        .mapError(ex => VocabularyDatabaseError(ex.getMessage))
    } yield created
  }

  def updateWord(id: Long, wordData: VocabularyCreate): ZIO[Any, VocabularyError, Option[Vocabulary]] = {
    for {
      _ <- validateVocabularyData(wordData)
      existingWord <- getWordById(id)
      updated <- existingWord match {
        case Some(word) =>
          val updatedWord = word.copy(
            germanWord = wordData.germanWord,
            englishTranslation = wordData.englishTranslation,
            portugueseTranslation = wordData.portugueseTranslation,
            pronunciation = wordData.pronunciation,
            wordType = wordData.wordType,
            difficultyLevel = wordData.difficultyLevel,
            category = wordData.category,
            exampleSentenceDe = wordData.exampleSentenceDe,
            exampleSentenceEn = wordData.exampleSentenceEn,
            exampleSentencePt = wordData.exampleSentencePt
          )
          ZIO.fromFuture(_ => vocabularyRepository.update(updatedWord))
            .mapError(ex => VocabularyDatabaseError(ex.getMessage))
        case None => ZIO.succeed(None)
      }
    } yield updated
  }

  def deleteWord(id: Long): ZIO[Any, VocabularyError, Boolean] = {
    ZIO.fromFuture(_ => vocabularyRepository.delete(id))
      .mapError(ex => VocabularyDatabaseError(ex.getMessage))
  }

  def getWordsStream(batchSize: Int = 100): ZStream[Any, VocabularyError, Vocabulary] = {
    ZStream.unfoldZIO(0L) { offset =>
      ZIO.fromFuture(_ => vocabularyRepository.findBatch(offset, batchSize))
        .mapError(ex => VocabularyDatabaseError(ex.getMessage))
        .map { words =>
          if (words.nonEmpty) Some((Chunk.fromIterable(words), offset + batchSize))
          else None
        }
    }.flatMap(ZStream.fromChunk)
  }

  private def validateVocabularyData(wordData: VocabularyCreate): ZIO[Any, VocabularyError, Unit] = {
    val validations = List(
      ZIO.when(wordData.germanWord.trim.isEmpty)(ZIO.fail(VocabularyValidationError("German word cannot be empty"))),
      ZIO.when(wordData.englishTranslation.trim.isEmpty)(ZIO.fail(VocabularyValidationError("English translation cannot be empty"))),
      ZIO.when(wordData.portugueseTranslation.trim.isEmpty)(ZIO.fail(VocabularyValidationError("Portuguese translation cannot be empty"))),
      ZIO.when(wordData.wordType.trim.isEmpty)(ZIO.fail(VocabularyValidationError("Word type cannot be empty"))),
      ZIO.when(!Set("BEGINNER", "INTERMEDIATE", "ADVANCED").contains(wordData.difficultyLevel))(
        ZIO.fail(VocabularyValidationError("Invalid difficulty level"))
      )
    )

    ZIO.collectAll(validations).unit
  }
}
