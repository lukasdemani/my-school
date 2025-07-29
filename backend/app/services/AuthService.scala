package services

import javax.inject._
import models.{User, UserRegistration}
import repositories.UserRepository
import org.mindrot.jbcrypt.BCrypt
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import play.api.Configuration
import zio.*
import zio.json.*
import java.time.Instant
import java.util.Date
import scala.concurrent.{Future, ExecutionContext}

trait AuthService {
  def register(registration: UserRegistration): ZIO[Any, AuthError, Option[String]]
  def login(email: String, password: String): ZIO[Any, AuthError, Option[String]]
  def refreshToken(token: String): ZIO[Any, AuthError, Option[String]]
  def validateToken(token: String): ZIO[Any, AuthError, Option[Long]]
}

sealed trait AuthError extends Throwable
case class DatabaseError(message: String) extends AuthError
case class TokenError(message: String) extends AuthError
case class ValidationError(message: String) extends AuthError

@Singleton
class AuthServiceImpl @Inject()(
  userRepository: UserRepository,
  configuration: Configuration
) extends AuthService {

  private val jwtSecret = configuration.get[String]("jwt.secret")
  private val jwtExpiration = configuration.get[Int]("jwt.expiration")
  private val algorithm = Algorithm.HMAC256(jwtSecret)

  def register(registration: UserRegistration): ZIO[Any, AuthError, Option[String]] = {
    for {
      passwordHash <- hashPassword(registration.password)
      user = User(
        email = registration.email,
        passwordHash = passwordHash,
        firstName = registration.firstName,
        lastName = registration.lastName,
        nativeLanguage = registration.nativeLanguage
      )
      createdUser <- ZIO.fromFuture(_ => userRepository.create(user))
        .mapError(ex => DatabaseError(ex.getMessage))
      token <- createdUser match {
        case Some(u) => generateToken(u.id.get, u.email).map(Some(_))
        case None => ZIO.succeed(None)
      }
    } yield token
  }

  def login(email: String, password: String): ZIO[Any, AuthError, Option[String]] = {
    for {
      userOpt <- ZIO.fromFuture(_ => userRepository.findByEmail(email))
        .mapError(ex => DatabaseError(ex.getMessage))
      token <- userOpt match {
        case Some(user) =>
          verifyPassword(password, user.passwordHash).flatMap { isValid =>
            if (isValid) generateToken(user.id.get, user.email).map(Some(_))
            else ZIO.succeed(None)
          }
        case None => ZIO.succeed(None)
      }
    } yield token
  }

  def refreshToken(token: String): ZIO[Any, AuthError, Option[String]] = {
    for {
      decoded <- decodeToken(token)
      newToken <- decoded match {
        case Some((userId, email)) => generateToken(userId, email).map(Some(_))
        case None => ZIO.succeed(None)
      }
    } yield newToken
  }

  def validateToken(token: String): ZIO[Any, AuthError, Option[Long]] = {
    decodeToken(token).map(_.map(_._1))
  }

  private def hashPassword(password: String): ZIO[Any, AuthError, String] = {
    ZIO.attempt(BCrypt.hashpw(password, BCrypt.gensalt()))
      .mapError(ex => ValidationError(s"Password hashing failed: ${ex.getMessage}"))
  }

  private def verifyPassword(password: String, hash: String): ZIO[Any, AuthError, Boolean] = {
    ZIO.attempt(BCrypt.checkpw(password, hash))
      .mapError(ex => ValidationError(s"Password verification failed: ${ex.getMessage}"))
  }

  private def generateToken(userId: Long, email: String): ZIO[Any, AuthError, String] = {
    ZIO.attempt {
      val now = Instant.now()
      val expiration = now.plusSeconds(jwtExpiration.toLong)

      JWT.create()
        .withClaim("userId", userId)
        .withClaim("email", email)
        .withIssuedAt(Date.from(now))
        .withExpiresAt(Date.from(expiration))
        .sign(algorithm)
    }.mapError(ex => TokenError(s"Token generation failed: ${ex.getMessage}"))
  }

  private def decodeToken(token: String): ZIO[Any, AuthError, Option[(Long, String)]] = {
    ZIO.attempt {
      val verifier = JWT.require(algorithm).build()
      val decodedJWT = verifier.verify(token)
      val userId = decodedJWT.getClaim("userId").asLong()
      val email = decodedJWT.getClaim("email").asString()
      Some((userId, email))
    }.catchAll(_ => ZIO.succeed(None))
  }
}
