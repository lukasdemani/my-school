package models

import play.api.libs.json._
import java.time.Instant

case class User(
  id: Option[Long] = None,
  email: String,
  passwordHash: String,
  firstName: String,
  lastName: String,
  level: String = "BEGINNER",
  nativeLanguage: String = "pt",
  createdAt: Instant = Instant.now(),
  updatedAt: Instant = Instant.now()
)

case class UserRegistration(
  email: String,
  password: String,
  firstName: String,
  lastName: String,
  nativeLanguage: String = "pt"
)

case class UserLogin(
  email: String,
  password: String
)

case class UserProfile(
  id: Long,
  email: String,
  firstName: String,
  lastName: String,
  level: String,
  nativeLanguage: String
)

object User {
  implicit val userFormat: Format[User] = Json.format[User]
  implicit val userRegistrationFormat: Format[UserRegistration] = Json.format[UserRegistration]
  implicit val userLoginFormat: Format[UserLogin] = Json.format[UserLogin]
  implicit val userProfileFormat: Format[UserProfile] = Json.format[UserProfile]
}
