package services

import org.apache.pekko.stream.scaladsl.{Source, Flow, Sink}
import org.apache.pekko.NotUsed
import org.apache.pekko.actor.typed.ActorSystem
import models.{Vocabulary, UserVocabularyProgress}
import zio.*
import zio.stream.*
import javax.inject._
import scala.concurrent.{Future, ExecutionContext}

@Singleton
class VocabularyStreamService @Inject()(
  vocabularyService: VocabularyService
)(implicit system: ActorSystem[?], ec: ExecutionContext) {

  def processVocabularyBatch(vocabularyIds: List[Long]): Source[Vocabulary, NotUsed] = {
    Source(vocabularyIds)
      .mapAsync(parallelism = 4) { id =>
        vocabularyService.getWordById(id).map(_.toList)
      }
      .mapConcat(identity)
  }

  def difficultyAnalysisFlow: Flow[Vocabulary, (String, Int), NotUsed] = {
    Flow[Vocabulary]
      .groupBy(maxSubstreams = 5, _.difficultyLevel)
      .fold(("", 0)) { case ((level, count), vocab) =>
        (vocab.difficultyLevel, count + 1)
      }
      .mergeSubstreams
  }

  def categoryStatisticsFlow: Flow[Vocabulary, Map[String, Int], NotUsed] = {
    Flow[Vocabulary]
      .fold(Map.empty[String, Int]) { (stats, vocab) =>
        val category = vocab.category.getOrElse("uncategorized")
        stats + (category -> (stats.getOrElse(category, 0) + 1))
      }
  }

  def generatePersonalizedVocabulary(
    userId: Long,
    userLevel: String,
    limit: Int = 20
  ): Future[List[Vocabulary]] = {
    val effect = for {
      allWords <- ZIO.fromFuture(_ => vocabularyService.getVocabulary(Some(userLevel), None, 1000, 0))
      personalizedWords <- ZIO.succeed(
        allWords
          .sortBy(_ => scala.util.Random.nextDouble())
          .take(limit)
      )
    } yield personalizedWords

    Unsafe.unsafe { implicit unsafe =>
      Runtime.default.unsafe.runToFuture(effect)
    }
  }

  def vocabularyRecommendationFlow(userLevel: String): Flow[UserVocabularyProgress, Vocabulary, NotUsed] = {
    Flow[UserVocabularyProgress]
      .filter(_.masteryLevel < 3) // Focus on words not yet mastered
      .mapAsync(parallelism = 4) { progress =>
        vocabularyService.getWordById(progress.vocabularyId)
      }
      .collect {
        case Some(word) if word.difficultyLevel == userLevel => word
      }
  }

  def streamVocabularyAnalytics(): ZStream[Any, Throwable, Map[String, Any]] = {
    vocabularyService.getWordsStream()
      .grouped(100)
      .map { chunk =>
        val words = chunk.toList
        Map[String, Any](
          "totalWords" -> words.length,
          "categories" -> words.groupBy(_.category.getOrElse("uncategorized")).view.mapValues(_.length).toMap,
          "difficulties" -> words.groupBy(_.difficultyLevel).view.mapValues(_.length).toMap,
          "wordTypes" -> words.groupBy(_.wordType).view.mapValues(_.length).toMap,
          "timestamp" -> java.time.Instant.now().toString
        )
      }
  }

  def processLearningProgress(userId: Long): Future[Map[String, Double]] = {
    val effect = for {
      // This would typically come from UserVocabularyProgressRepository
      progressData <- ZIO.succeed(List.empty[UserVocabularyProgress]) // Placeholder
      analytics <- ZIO.succeed(calculateProgressAnalytics(progressData))
    } yield analytics

    Unsafe.unsafe { implicit unsafe =>
      Runtime.default.unsafe.runToFuture(effect)
    }
  }

  private def calculateProgressAnalytics(progressData: List[UserVocabularyProgress]): Map[String, Double] = {
    if (progressData.isEmpty) {
      Map(
        "averageMastery" -> 0.0,
        "completionRate" -> 0.0,
        "accuracyRate" -> 0.0
      )
    } else {
      val averageMastery = progressData.map(_.masteryLevel.toDouble).sum / progressData.length
      val completionRate = progressData.count(_.masteryLevel >= 4).toDouble / progressData.length
      val totalAttempts = progressData.map(_.totalAttempts).sum
      val totalCorrect = progressData.map(_.correctAnswers).sum
      val accuracyRate = if (totalAttempts > 0) totalCorrect.toDouble / totalAttempts else 0.0

      Map(
        "averageMastery" -> averageMastery,
        "completionRate" -> completionRate,
        "accuracyRate" -> accuracyRate
      )
    }
  }
}
