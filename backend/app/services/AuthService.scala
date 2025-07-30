package services

import models.*
import repositories.UserRepository
import zio.*
import java.util.{UUID, Date}
import java.time.Instant
import org.mindrot.jbcrypt.BCrypt
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import java.time.temporal.ChronoUnit

trait AuthService {
  def signUp(request: SignUpRequest): Task[AuthResponse]
  def signIn(request: SignInRequest): Task[AuthResponse]
  def refreshToken(refreshToken: String): Task[AuthResponse]
  def logout(userId: UUID, refreshToken: String): Task[Boolean]
  def createWorkspace(userId: UUID, request: CreateWorkspaceRequest): Task[WorkspaceResponse]
  def getUserWithWorkspaces(userId: UUID): Task[UserResponse]
}

case class AuthServiceLive(
  userRepository: UserRepository,
  jwtSecret: String = "your-secret-key", // TODO: Move to config
  accessTokenExpirationMinutes: Int = 15,
  refreshTokenExpirationDays: Int = 30
) extends AuthService {

  private val algorithm = Algorithm.HMAC256(jwtSecret)
  private val jwtExpiration = accessTokenExpirationMinutes * 60 // Convert to seconds

  override def signUp(request: SignUpRequest): Task[AuthResponse] = {
    for {
      // Verify email doesn't exist
      existingUser <- userRepository.findByEmail(request.email)
      _ <- ZIO.when(existingUser.isDefined)(ZIO.fail(new RuntimeException("Email already exists")))
      
      // Hash password
      passwordHash = BCrypt.hashpw(request.password, BCrypt.gensalt())
      
      // Create user
      user = User(
        name = request.name,
        email = request.email,
        passwordHash = passwordHash,
        nativeLanguage = request.nativeLanguage,
        bio = request.bio,
        interests = request.interests
      )
      
      createdUser <- userRepository.create(user)
      
      // Generate tokens
      accessToken <- generateAccessToken(createdUser.id.get)
      refreshTokenValue = generateRefreshToken()
      refreshTokenExpiration = Instant.now().plus(refreshTokenExpirationDays, ChronoUnit.DAYS)
      
      // Save refresh token
      refreshToken = RefreshToken(
        userId = createdUser.id.get,
        token = refreshTokenValue,
        expiresAt = refreshTokenExpiration
      )
      _ <- userRepository.saveRefreshToken(refreshToken)
      
      // Create user response
      userResponse = UserResponse(
        id = createdUser.id.get,
        name = createdUser.name,
        email = createdUser.email,
        nativeLanguage = createdUser.nativeLanguage,
        bio = createdUser.bio,
        interests = createdUser.interests,
        workspaces = List.empty
      )
      
    } yield AuthResponse(userResponse, accessToken, refreshTokenValue)
  }

  override def signIn(request: SignInRequest): Task[AuthResponse] = {
    for {
      // Find user by email
      userOpt <- userRepository.findByEmail(request.email)
      user <- ZIO.fromOption(userOpt).orElseFail(new RuntimeException("Invalid credentials"))
      
      // Verify password
      _ <- ZIO.when(!BCrypt.checkpw(request.password, user.passwordHash))(
        ZIO.fail(new RuntimeException("Invalid credentials"))
      )
      
      // Revoke old tokens
      _ <- userRepository.revokeAllUserTokens(user.id.get)
      
      // Generate new tokens
      accessToken <- generateAccessToken(user.id.get)
      refreshTokenValue = generateRefreshToken()
      refreshTokenExpiration = Instant.now().plus(refreshTokenExpirationDays, ChronoUnit.DAYS)
      
      // Save refresh token
      refreshToken = RefreshToken(
        userId = user.id.get,
        token = refreshTokenValue,
        expiresAt = refreshTokenExpiration
      )
      _ <- userRepository.saveRefreshToken(refreshToken)
      
      // Get user workspaces
      workspaces <- userRepository.findWorkspacesByUserId(user.id.get)
      workspaceResponses <- ZIO.foreach(workspaces) { workspace =>
        userRepository.findProgressByWorkspaceId(workspace.id.get).map { progressOpt =>
          WorkspaceResponse(
            id = workspace.id.get,
            targetLanguage = workspace.targetLanguage,
            languageLevel = workspace.languageLevel,
            name = workspace.name,
            description = workspace.description,
            progress = progressOpt.map(p => WorkspaceProgressResponse(
              totalLessonsCompleted = p.totalLessonsCompleted,
              totalPoints = p.totalPoints,
              currentStreak = p.currentStreak,
              longestStreak = p.longestStreak,
              lastActivity = p.lastActivity
            ))
          )
        }
      }
      
      userResponse = UserResponse(
        id = user.id.get,
        name = user.name,
        email = user.email,
        nativeLanguage = user.nativeLanguage,
        bio = user.bio,
        interests = user.interests,
        workspaces = workspaceResponses
      )
      
    } yield AuthResponse(userResponse, accessToken, refreshTokenValue)
  }

  override def refreshToken(refreshTokenValue: String): Task[AuthResponse] = {
    for {
      // Find and validate refresh token
      tokenOpt <- userRepository.findRefreshToken(refreshTokenValue)
      token <- ZIO.fromOption(tokenOpt).orElseFail(new RuntimeException("Invalid refresh token"))
      
      // Get user
      userOpt <- userRepository.findById(token.userId)
      user <- ZIO.fromOption(userOpt).orElseFail(new RuntimeException("User not found"))
      
      // Revoke old token
      _ <- userRepository.revokeRefreshToken(refreshTokenValue)
      
      // Generate new tokens
      accessToken <- generateAccessToken(user.id.get)
      newRefreshTokenValue = generateRefreshToken()
      refreshTokenExpiration = Instant.now().plus(refreshTokenExpirationDays, ChronoUnit.DAYS)
      
      // Save new refresh token
      newRefreshToken = RefreshToken(
        userId = user.id.get,
        token = newRefreshTokenValue,
        expiresAt = refreshTokenExpiration
      )
      _ <- userRepository.saveRefreshToken(newRefreshToken)
      
      // Get user with workspaces
      userResponse <- getUserWithWorkspaces(user.id.get)
      
    } yield AuthResponse(userResponse, accessToken, newRefreshTokenValue)
  }

  override def logout(userId: UUID, refreshToken: String): Task[Boolean] = {
    userRepository.revokeRefreshToken(refreshToken)
  }

  override def createWorkspace(userId: UUID, request: CreateWorkspaceRequest): Task[WorkspaceResponse] = {
    for {
      // Verify user exists
      userOpt <- userRepository.findById(userId)
      _ <- ZIO.fromOption(userOpt).orElseFail(new RuntimeException("User not found"))
      
      // Create workspace
      workspace = LanguageWorkspace(
        userId = userId,
        targetLanguage = request.targetLanguage,
        languageLevel = request.languageLevel,
        name = request.name,
        description = request.description
      )
      
      createdWorkspace <- userRepository.createWorkspace(workspace)
      
      // Create initial progress
      progress = WorkspaceProgress(
        workspaceId = createdWorkspace.id.get
      )
      _ <- userRepository.createOrUpdateProgress(progress)
      
    } yield WorkspaceResponse(
      id = createdWorkspace.id.get,
      targetLanguage = createdWorkspace.targetLanguage,
      languageLevel = createdWorkspace.languageLevel,
      name = createdWorkspace.name,
      description = createdWorkspace.description,
      progress = Some(WorkspaceProgressResponse(
        totalLessonsCompleted = 0,
        totalPoints = 0,
        currentStreak = 0,
        longestStreak = 0,
        lastActivity = None
      ))
    )
  }

  override def getUserWithWorkspaces(userId: UUID): Task[UserResponse] = {
    for {
      userOpt <- userRepository.findById(userId)
      user <- ZIO.fromOption(userOpt).orElseFail(new RuntimeException("User not found"))
      
      workspaces <- userRepository.findWorkspacesByUserId(userId)
      workspaceResponses <- ZIO.foreach(workspaces) { workspace =>
        userRepository.findProgressByWorkspaceId(workspace.id.get).map { progressOpt =>
          WorkspaceResponse(
            id = workspace.id.get,
            targetLanguage = workspace.targetLanguage,
            languageLevel = workspace.languageLevel,
            name = workspace.name,
            description = workspace.description,
            progress = progressOpt.map(p => WorkspaceProgressResponse(
              totalLessonsCompleted = p.totalLessonsCompleted,
              totalPoints = p.totalPoints,
              currentStreak = p.currentStreak,
              longestStreak = p.longestStreak,
              lastActivity = p.lastActivity
            ))
          )
        }
      }
    } yield UserResponse(
      id = user.id.get,
      name = user.name,
      email = user.email,
      nativeLanguage = user.nativeLanguage,
      bio = user.bio,
      interests = user.interests,
      workspaces = workspaceResponses
    )
  }

  private def generateAccessToken(userId: UUID): Task[String] = ZIO.attempt {
     val now = Instant.now()
     val expiration = now.plusSeconds(accessTokenExpirationMinutes * 60L)
     
     JWT.create()
       .withSubject(userId.toString)
       .withIssuedAt(Date.from(now))
       .withExpiresAt(Date.from(expiration))
       .sign(Algorithm.HMAC256(jwtSecret))
   }.mapError(ex => TokenError(s"Token generation failed: ${ex.getMessage}"))

  private def generateRefreshToken(): String = {
    UUID.randomUUID().toString
  }
}

object AuthServiceLive {
  val layer: ZLayer[UserRepository, Nothing, AuthService] =
    ZLayer.fromFunction(AuthServiceLive.apply(_, "your-secret-key", 15, 30))
}
