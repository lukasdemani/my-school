package repositories

import javax.inject._
import models.{User, UserProfile}
import play.api.db.Database
import zio.*
import scala.concurrent.{Future, ExecutionContext}
import java.sql.{Connection, PreparedStatement, ResultSet}

trait UserRepository {
  def create(user: User): ZIO[Any, DatabaseError, Option[User]]
  def findById(id: Long): ZIO[Any, DatabaseError, Option[User]]
  def findByEmail(email: String): ZIO[Any, DatabaseError, Option[User]]
  def update(user: User): ZIO[Any, DatabaseError, Option[User]]
  def delete(id: Long): ZIO[Any, DatabaseError, Boolean]
}

sealed trait DatabaseError extends Throwable
case class ConnectionError(message: String) extends DatabaseError
case class QueryError(message: String) extends DatabaseError
case class DataMappingError(message: String) extends DatabaseError

@Singleton
class UserRepositoryImpl @Inject()(
  database: Database
)(implicit ec: ExecutionContext) extends UserRepository {

  def create(user: User): ZIO[Any, DatabaseError, Option[User]] = {
    executeQuery { conn =>
      val sql = """
        INSERT INTO users (email, password_hash, first_name, last_name, level, native_language, created_at, updated_at)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        RETURNING id, email, password_hash, first_name, last_name, level, native_language, created_at, updated_at
      """
      
      val stmt = conn.prepareStatement(sql)
      stmt.setString(1, user.email)
      stmt.setString(2, user.passwordHash)
      stmt.setString(3, user.firstName)
      stmt.setString(4, user.lastName)
      stmt.setString(5, user.level)
      stmt.setString(6, user.nativeLanguage)
      stmt.setTimestamp(7, java.sql.Timestamp.from(user.createdAt))
      stmt.setTimestamp(8, java.sql.Timestamp.from(user.updatedAt))
      
      val rs = stmt.executeQuery()
      if (rs.next()) Some(extractUser(rs)) else None
    }
  }

  def findById(id: Long): ZIO[Any, DatabaseError, Option[User]] = {
    executeQuery { conn =>
      val sql = "SELECT * FROM users WHERE id = ?"
      val stmt = conn.prepareStatement(sql)
      stmt.setLong(1, id)
      
      val rs = stmt.executeQuery()
      if (rs.next()) Some(extractUser(rs)) else None
    }
  }

  def findByEmail(email: String): ZIO[Any, DatabaseError, Option[User]] = {
    executeQuery { conn =>
      val sql = "SELECT * FROM users WHERE email = ?"
      val stmt = conn.prepareStatement(sql)
      stmt.setString(1, email)
      
      val rs = stmt.executeQuery()
      if (rs.next()) Some(extractUser(rs)) else None
    }
  }

  def update(user: User): ZIO[Any, DatabaseError, Option[User]] = {
    executeQuery { conn =>
      val sql = """
        UPDATE users 
        SET first_name = ?, last_name = ?, level = ?, updated_at = ?
        WHERE id = ?
        RETURNING id, email, password_hash, first_name, last_name, level, native_language, created_at, updated_at
      """
      
      val stmt = conn.prepareStatement(sql)
      stmt.setString(1, user.firstName)
      stmt.setString(2, user.lastName)
      stmt.setString(3, user.level)
      stmt.setTimestamp(4, java.sql.Timestamp.from(user.updatedAt))
      stmt.setLong(5, user.id.get)
      
      val rs = stmt.executeQuery()
      if (rs.next()) Some(extractUser(rs)) else None
    }
  }

  def delete(id: Long): ZIO[Any, DatabaseError, Boolean] = {
    executeQuery { conn =>
      val sql = "DELETE FROM users WHERE id = ?"
      val stmt = conn.prepareStatement(sql)
      stmt.setLong(1, id)
      
      stmt.executeUpdate() > 0
    }
  }

  private def executeQuery[A](query: Connection => A): ZIO[Any, DatabaseError, A] = {
    ZIO.attempt {
      database.withConnection(query)
    }.mapError {
      case ex: java.sql.SQLException => QueryError(s"SQL Error: ${ex.getMessage}")
      case ex => ConnectionError(s"Database connection error: ${ex.getMessage}")
    }
  }

  private def extractUser(rs: ResultSet): User = {
    try {
      User(
        id = Some(rs.getLong("id")),
        email = rs.getString("email"),
        passwordHash = rs.getString("password_hash"),
        firstName = rs.getString("first_name"),
        lastName = rs.getString("last_name"),
        level = rs.getString("level"),
        nativeLanguage = rs.getString("native_language"),
        createdAt = rs.getTimestamp("created_at").toInstant,
        updatedAt = rs.getTimestamp("updated_at").toInstant
      )
    } catch {
      case ex: Exception => throw DataMappingError(s"Failed to map user data: ${ex.getMessage}")
    }
  }

  // Legacy Future-based methods for Play Framework compatibility
  def create(user: User): Future[Option[User]] = {
    Unsafe.unsafe { implicit unsafe =>
      Runtime.default.unsafe.runToFuture(create(user).mapError(_.asInstanceOf[Throwable]))
    }
  }

  def findByEmail(email: String): Future[Option[User]] = {
    Unsafe.unsafe { implicit unsafe =>
      Runtime.default.unsafe.runToFuture(findByEmail(email).mapError(_.asInstanceOf[Throwable]))
    }
  }
}
