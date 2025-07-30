package models

import play.api.libs.json._
import java.time.Instant
import java.util.UUID

// User principal
case class User(
  id: Option[UUID] = None,
  name: String,
  email: String,
  passwordHash: String,
  nativeLanguage: String,
  bio: Option[String] = None,
  interests: Option[String] = None,
  isActive: Boolean = true,
  createdAt: Instant = Instant.now(),
  updatedAt: Instant = Instant.now()
)

// Workspace para cada idioma que o usuário quer aprender
case class LanguageWorkspace(
  id: Option[UUID] = None,
  userId: UUID,
  targetLanguage: String, // Idioma que está aprendendo (ex: "german", "french")
  languageLevel: String, // "beginner", "intermediate", "advanced"
  name: String, // Nome personalizado do workspace (ex: "Alemão para Negócios")
  description: Option[String] = None,
  isActive: Boolean = true,
  createdAt: Instant = Instant.now(),
  updatedAt: Instant = Instant.now()
)

// Progresso geral do usuário em um workspace
case class WorkspaceProgress(
  id: Option[UUID] = None,
  workspaceId: UUID,
  totalLessonsCompleted: Int = 0,
  totalPoints: Int = 0,
  currentStreak: Int = 0,
  longestStreak: Int = 0,
  lastActivity: Option[Instant] = None,
  updatedAt: Instant = Instant.now()
)

// Para JWT tokens
case class RefreshToken(
  id: Option[UUID] = None,
  userId: UUID,
  token: String,
  expiresAt: Instant,
  createdAt: Instant = Instant.now(),
  isRevoked: Boolean = false
)

// DTOs para requests
case class SignUpRequest(
  name: String,
  email: String,
  password: String,
  nativeLanguage: String,
  bio: Option[String] = None,
  interests: Option[String] = None
)

case class SignInRequest(
  email: String,
  password: String
)

case class CreateWorkspaceRequest(
  targetLanguage: String,
  languageLevel: String,
  name: String,
  description: Option[String] = None
)

// DTOs para responses
case class AuthResponse(
  user: UserResponse,
  accessToken: String,
  refreshToken: String
)

case class UserResponse(
  id: UUID,
  name: String,
  email: String,
  nativeLanguage: String,
  bio: Option[String],
  interests: Option[String],
  workspaces: List[WorkspaceResponse]
)

case class WorkspaceResponse(
  id: UUID,
  targetLanguage: String,
  languageLevel: String,
  name: String,
  description: Option[String],
  progress: Option[WorkspaceProgressResponse] = None
)

case class WorkspaceProgressResponse(
  totalLessonsCompleted: Int,
  totalPoints: Int,
  currentStreak: Int,
  longestStreak: Int,
  lastActivity: Option[Instant]
)

// Error types for authentication
sealed trait AuthError extends Throwable
case class UserNotFound(message: String) extends AuthError
case class InvalidCredentials(message: String) extends AuthError
case class UserAlreadyExists(message: String) extends AuthError
case class ValidationError(message: String) extends AuthError
case class TokenError(message: String) extends AuthError
case class DatabaseError(message: String) extends AuthError

object User {
  implicit val userFormat: Format[User] = Json.format[User]
  implicit val languageWorkspaceFormat: Format[LanguageWorkspace] = Json.format[LanguageWorkspace]
  implicit val workspaceProgressFormat: Format[WorkspaceProgress] = Json.format[WorkspaceProgress]
  implicit val refreshTokenFormat: Format[RefreshToken] = Json.format[RefreshToken]
  
  implicit val signUpRequestFormat: Format[SignUpRequest] = Json.format[SignUpRequest]
  implicit val signInRequestFormat: Format[SignInRequest] = Json.format[SignInRequest]
  implicit val createWorkspaceRequestFormat: Format[CreateWorkspaceRequest] = Json.format[CreateWorkspaceRequest]
  
  implicit val workspaceProgressResponseFormat: Format[WorkspaceProgressResponse] = Json.format[WorkspaceProgressResponse]
  implicit val workspaceResponseFormat: Format[WorkspaceResponse] = Json.format[WorkspaceResponse]
  implicit val userResponseFormat: Format[UserResponse] = Json.format[UserResponse]
  implicit val authResponseFormat: Format[AuthResponse] = Json.format[AuthResponse]
}
