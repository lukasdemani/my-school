package repositories

import io.getquill._
import io.getquill.jdbczio.Quill
import models._
import zio._
import java.util.UUID
import java.time.Instant
import java.time.temporal.ChronoUnit
import math.Ordering.Implicits.infixOrderingOps

trait UserRepository {
  def create(user: User): Task[User]
  def findByEmail(email: String): Task[Option[User]]
  def findById(id: UUID): Task[Option[User]]
  def update(user: User): Task[User]
  def delete(id: UUID): Task[Boolean]
  
  // Workspace operations
  def createWorkspace(workspace: LanguageWorkspace): Task[LanguageWorkspace]
  def findWorkspacesByUserId(userId: UUID): Task[List[LanguageWorkspace]]
  def findWorkspaceById(id: UUID): Task[Option[LanguageWorkspace]]
  def updateWorkspace(workspace: LanguageWorkspace): Task[LanguageWorkspace]
  def deleteWorkspace(id: UUID): Task[Boolean]
  
  // Progress operations
  def createOrUpdateProgress(progress: WorkspaceProgress): Task[WorkspaceProgress]
  def findProgressByWorkspaceId(workspaceId: UUID): Task[Option[WorkspaceProgress]]
  
  // Token operations
  def saveRefreshToken(token: RefreshToken): Task[RefreshToken]
  def findRefreshToken(token: String): Task[Option[RefreshToken]]
  def revokeRefreshToken(token: String): Task[Boolean]
  def revokeAllUserTokens(userId: UUID): Task[Boolean]
}

case class UserRepositoryLive(quill: Quill.Postgres[SnakeCase]) extends UserRepository {
  import quill._

  // Schema definitions
  inline def users = quote {
    querySchema[User]("users")
  }
  
  inline def workspaces = quote {
    querySchema[LanguageWorkspace]("language_workspaces")
  }
  
  inline def progress = quote {
    querySchema[WorkspaceProgress]("workspace_progress")
  }
  
  inline def tokens = quote {
    querySchema[RefreshToken]("refresh_tokens")
  }

  override def create(user: User): Task[User] = {
    val userWithId = user.copy(id = Some(UUID.randomUUID()))
    run(quote {
      users.insertValue(lift(userWithId)).returning(_.id)
    }).map(_ => userWithId)
  }

  override def findByEmail(email: String): Task[Option[User]] = {
    run(quote {
      users.filter(_.email == lift(email))
    }).map(_.headOption)
  }

  override def findById(id: UUID): Task[Option[User]] = {
    run(quote {
      users.filter(_.id.contains(lift(id)))
    }).map(_.headOption)
  }

  override def update(user: User): Task[User] = {
    val updatedUser = user.copy(updatedAt = Instant.now())
    run(quote {
      users.filter(_.id == lift(user.id)).updateValue(lift(updatedUser))
    }).map(_ => updatedUser)
  }

  override def delete(id: UUID): Task[Boolean] = {
    run(quote {
      users.filter(_.id.contains(lift(id))).update(_.isActive -> false)
    }).map(_ > 0)
  }

  // Workspace operations
  override def createWorkspace(workspace: LanguageWorkspace): Task[LanguageWorkspace] = {
    val workspaceWithId = workspace.copy(id = Some(UUID.randomUUID()))
    run(quote {
      workspaces.insertValue(lift(workspaceWithId)).returning(_.id)
    }).map(_ => workspaceWithId)
  }

  override def findWorkspacesByUserId(userId: UUID): Task[List[LanguageWorkspace]] = {
    run(quote {
      workspaces.filter(w => w.userId == lift(userId) && w.isActive)
    })
  }

  override def findWorkspaceById(id: UUID): Task[Option[LanguageWorkspace]] = {
    run(quote {
      workspaces.filter(_.id.contains(lift(id)))
    }).map(_.headOption)
  }

  override def updateWorkspace(workspace: LanguageWorkspace): Task[LanguageWorkspace] = {
    val updatedWorkspace = workspace.copy(updatedAt = Instant.now())
    run(quote {
      workspaces.filter(_.id == lift(workspace.id)).updateValue(lift(updatedWorkspace))
    }).map(_ => updatedWorkspace)
  }

  override def deleteWorkspace(id: UUID): Task[Boolean] = {
    run(quote {
      workspaces.filter(_.id.contains(lift(id))).update(_.isActive -> false)
    }).map(_ > 0)
  }

  // Progress operations
  override def createOrUpdateProgress(workspaceProgress: WorkspaceProgress): Task[WorkspaceProgress] = {
    val progressWithId = workspaceProgress.copy(
      id = workspaceProgress.id.orElse(Some(UUID.randomUUID())),
      updatedAt = Instant.now()
    )
    
    run(quote {
      progress.insertValue(lift(progressWithId))
        .onConflictUpdate(_.workspaceId)(
          (t, e) => t.totalLessonsCompleted -> e.totalLessonsCompleted,
          (t, e) => t.totalPoints -> e.totalPoints,
          (t, e) => t.currentStreak -> e.currentStreak,
          (t, e) => t.longestStreak -> e.longestStreak,
          (t, e) => t.lastActivity -> e.lastActivity,
          (t, e) => t.updatedAt -> e.updatedAt
        )
    }).map(_ => progressWithId)
  }

  override def findProgressByWorkspaceId(workspaceId: UUID): Task[Option[WorkspaceProgress]] = {
    run(quote {
      progress.filter(_.workspaceId == lift(workspaceId))
    }).map(_.headOption)
  }

  // Token operations
  override def saveRefreshToken(token: RefreshToken): Task[RefreshToken] = {
    val tokenWithId = token.copy(id = Some(UUID.randomUUID()))
    run(quote {
      tokens.insertValue(lift(tokenWithId))
    }).map(_ => tokenWithId)
  }

  override def findRefreshToken(token: String): Task[Option[RefreshToken]] = {
    run(quote {
      tokens.filter(t => t.token == lift(token) && !t.isRevoked)
    }).map(_.headOption.filter(_.expiresAt.isAfter(Instant.now())))
  }

  override def revokeRefreshToken(token: String): Task[Boolean] = {
    run(quote {
      tokens.filter(_.token == lift(token)).update(_.isRevoked -> true)
    }).map(_ > 0)
  }

  override def revokeAllUserTokens(userId: UUID): Task[Boolean] = {
    run(quote {
      tokens.filter(_.userId == lift(userId)).update(_.isRevoked -> true)
    }).map(_ > 0)
  }
}

object UserRepositoryLive {
  val layer: ZLayer[Quill.Postgres[SnakeCase], Nothing, UserRepository] =
    ZLayer.fromFunction(UserRepositoryLive.apply _)
}
