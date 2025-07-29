package actors

import org.apache.pekko.actor.typed.{ActorRef, Behavior}
import org.apache.pekko.actor.typed.scaladsl.Behaviors
import models.{Vocabulary, PracticeSession}
import java.time.Instant
import scala.concurrent.duration.*

object LearningSessionActor {

  sealed trait Command
  case class StartVocabularySession(userId: Long, vocabularyIds: List[Long], replyTo: ActorRef[SessionResponse]) extends Command
  case class SubmitAnswer(userId: Long, vocabularyId: Long, answer: String, isCorrect: Boolean, replyTo: ActorRef[AnswerResponse]) extends Command
  case class EndSession(userId: Long, replyTo: ActorRef[SessionEndResponse]) extends Command
  case class GetSessionStats(userId: Long, replyTo: ActorRef[SessionStatsResponse]) extends Command
  private case class SessionTimeout(userId: Long) extends Command

  sealed trait Response
  case class SessionResponse(sessionId: String, vocabularyIds: List[Long]) extends Response
  case class AnswerResponse(isCorrect: Boolean, score: Int, nextVocabularyId: Option[Long]) extends Response
  case class SessionEndResponse(totalScore: Int, totalTime: Long, correctAnswers: Int, totalQuestions: Int) extends Response
  case class SessionStatsResponse(currentScore: Int, questionsRemaining: Int, timeElapsed: Long) extends Response

  case class SessionState(
    userId: Long,
    vocabularyIds: List[Long],
    currentIndex: Int = 0,
    score: Int = 0,
    correctAnswers: Int = 0,
    totalQuestions: Int = 0,
    startTime: Instant = Instant.now(),
    lastActivity: Instant = Instant.now()
  )

  def apply(): Behavior[Command] = {
    learningSession(Map.empty[Long, SessionState])
  }

  private def learningSession(sessions: Map[Long, SessionState]): Behavior[Command] = {
    Behaviors.receive { (context, message) =>
      message match {
        case StartVocabularySession(userId, vocabularyIds, replyTo) =>
          val sessionState = SessionState(
            userId = userId,
            vocabularyIds = vocabularyIds,
            totalQuestions = vocabularyIds.length
          )
          
          context.scheduleOnce(30.minutes, context.self, SessionTimeout(userId))
          
          replyTo ! SessionResponse(s"session-$userId-${Instant.now().toEpochMilli}", vocabularyIds)
          learningSession(sessions + (userId -> sessionState))

        case SubmitAnswer(userId, vocabularyId, answer, isCorrect, replyTo) =>
          sessions.get(userId) match {
            case Some(session) =>
              val updatedSession = session.copy(
                currentIndex = session.currentIndex + 1,
                score = if (isCorrect) session.score + 10 else session.score,
                correctAnswers = if (isCorrect) session.correctAnswers + 1 else session.correctAnswers,
                lastActivity = Instant.now()
              )
              
              val nextVocabularyId = if (updatedSession.currentIndex < session.vocabularyIds.length) {
                Some(session.vocabularyIds(updatedSession.currentIndex))
              } else None
              
              replyTo ! AnswerResponse(isCorrect, updatedSession.score, nextVocabularyId)
              learningSession(sessions + (userId -> updatedSession))
              
            case None =>
              replyTo ! AnswerResponse(isCorrect = false, 0, None)
              Behaviors.same
          }

        case EndSession(userId, replyTo) =>
          sessions.get(userId) match {
            case Some(session) =>
              val timeElapsed = java.time.Duration.between(session.startTime, Instant.now()).toSeconds
              replyTo ! SessionEndResponse(
                session.score,
                timeElapsed,
                session.correctAnswers,
                session.totalQuestions
              )
              learningSession(sessions - userId)
              
            case None =>
              replyTo ! SessionEndResponse(0, 0, 0, 0)
              Behaviors.same
          }

        case GetSessionStats(userId, replyTo) =>
          sessions.get(userId) match {
            case Some(session) =>
              val timeElapsed = java.time.Duration.between(session.startTime, Instant.now()).toSeconds
              val questionsRemaining = session.vocabularyIds.length - session.currentIndex
              replyTo ! SessionStatsResponse(session.score, questionsRemaining, timeElapsed)
              
            case None =>
              replyTo ! SessionStatsResponse(0, 0, 0)
          }
          Behaviors.same

        case SessionTimeout(userId) =>
          context.log.info(s"Session timeout for user $userId")
          learningSession(sessions - userId)
      }
    }
  }
}
