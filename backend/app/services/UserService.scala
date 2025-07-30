package services

import javax.inject._
import models.User
import repositories.UserRepository
import scala.concurrent.{Future, ExecutionContext}
import java.util.UUID
import zio.*

@Singleton
class UserService @Inject()(
  userRepository: UserRepository
)(implicit ec: ExecutionContext) {

  def getUserProfile(userId: UUID): Future[Option[User]] = {
    // Convert ZIO to Future
    Unsafe.unsafe { implicit unsafe =>
      Runtime.default.unsafe.runToFuture(
        userRepository.findById(userId)
      )
    }
  }

  def updateUserProfile(userId: UUID, name: String, bio: Option[String], interests: Option[String]): Future[Option[User]] = {
    val effect: ZIO[Any, Throwable, Option[User]] = userRepository.findById(userId).flatMap {
      case Some(user) =>
        val updatedUser = user.copy(
          name = name,
          bio = bio,
          interests = interests,
          updatedAt = java.time.Instant.now()
        )
        userRepository.update(updatedUser).map(Some(_))
      case None => ZIO.succeed(None)
    }

    Unsafe.unsafe { implicit unsafe =>
      Runtime.default.unsafe.runToFuture(effect)
    }
  }

  def getUserProgress(userId: UUID): Future[Map[String, Any]] = {
    // This would typically aggregate data from multiple repositories
    // For now, returning a simple structure
    Future.successful(Map(
      "totalLessons" -> 0,
      "completedLessons" -> 0,
      "vocabularyKnown" -> 0,
      "currentLevel" -> "BEGINNER",
      "streakDays" -> 0,
      "totalStudyTime" -> 0
    ))
  }
}
