package services

import javax.inject._
import models.{User, UserProfile}
import repositories.UserRepository
import scala.concurrent.{Future, ExecutionContext}

@Singleton
class UserService @Inject()(
  userRepository: UserRepository
)(implicit ec: ExecutionContext) {

  def getUserProfile(userId: Long): Future[Option[UserProfile]] = {
    userRepository.findById(userId).map {
      case Some(user) => Some(UserProfile(
        id = user.id.get,
        email = user.email,
        firstName = user.firstName,
        lastName = user.lastName,
        level = user.level,
        nativeLanguage = user.nativeLanguage
      ))
      case None => None
    }
  }

  def updateUserProfile(userId: Long, firstName: String, lastName: String, level: String): Future[Option[UserProfile]] = {
    userRepository.findById(userId).flatMap {
      case Some(user) =>
        val updatedUser = user.copy(
          firstName = firstName,
          lastName = lastName,
          level = level,
          updatedAt = java.time.Instant.now()
        )
        userRepository.update(updatedUser).map {
          case Some(updated) => Some(UserProfile(
            id = updated.id.get,
            email = updated.email,
            firstName = updated.firstName,
            lastName = updated.lastName,
            level = updated.level,
            nativeLanguage = updated.nativeLanguage
          ))
          case None => None
        }
      case None => Future.successful(None)
    }
  }

  def getUserProgress(userId: Long): Future[Map[String, Any]] = {
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
