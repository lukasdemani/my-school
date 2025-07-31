package modules

import com.google.inject.{AbstractModule, Provides, Singleton}
import repositories.{UserRepository, UserRepositoryLive}
import services.{AuthService, AuthServiceLive, VocabularyService, VocabularyServiceImpl, OpenAIService, GeminiServiceImpl}
import org.apache.pekko.actor.typed.ActorSystem
import org.apache.pekko.actor.typed.scaladsl.Behaviors
import javax.sql.DataSource
import play.api.Configuration

class AppModule extends AbstractModule {

  override def configure(): Unit = {
    // Basic bindings for services that don't need complex setup
    bind(classOf[VocabularyService]).to(classOf[VocabularyServiceImpl])
    bind(classOf[OpenAIService]).to(classOf[GeminiServiceImpl])
  }

  @Provides
  @Singleton
  def provideActorSystem(): ActorSystem[_] = {
    ActorSystem(Behaviors.empty, "german-learning-system")
  }

  @Provides
  @Singleton  
  def provideDataSource(configuration: Configuration): DataSource = {
    // Create a simple DataSource from configuration
    val ds = new org.postgresql.ds.PGSimpleDataSource()
    ds.setServerNames(Array("localhost"))
    ds.setDatabaseName("german_learning")
    ds.setUser(configuration.get[String]("db.default.username"))
    ds.setPassword(configuration.get[String]("db.default.password"))
    ds.setPortNumbers(Array(5432))
    ds
  }

  @Provides
  @Singleton
  def provideUserRepository(): UserRepository = {
    // For now, return a simple implementation that compiles
    // TODO: Fix this to use proper Quill integration
    new UserRepository {
      import zio.*
      import models.*
      import java.util.UUID
      
      def create(user: User): Task[User] = ZIO.succeed(user)
      def findByEmail(email: String): Task[Option[User]] = ZIO.none
      def findById(id: UUID): Task[Option[User]] = ZIO.none
      def update(user: User): Task[User] = ZIO.succeed(user)
      def delete(id: UUID): Task[Boolean] = ZIO.succeed(false)
      def createWorkspace(workspace: LanguageWorkspace): Task[LanguageWorkspace] = ZIO.succeed(workspace)
      def findWorkspacesByUserId(userId: UUID): Task[List[LanguageWorkspace]] = ZIO.succeed(List.empty)
      def findWorkspaceById(id: UUID): Task[Option[LanguageWorkspace]] = ZIO.none
      def updateWorkspace(workspace: LanguageWorkspace): Task[LanguageWorkspace] = ZIO.succeed(workspace)
      def deleteWorkspace(id: UUID): Task[Boolean] = ZIO.succeed(false)
      def createOrUpdateProgress(progress: WorkspaceProgress): Task[WorkspaceProgress] = ZIO.succeed(progress)
      def findProgressByWorkspaceId(workspaceId: UUID): Task[Option[WorkspaceProgress]] = ZIO.none
      def saveRefreshToken(token: RefreshToken): Task[RefreshToken] = ZIO.succeed(token)
      def findRefreshToken(token: String): Task[Option[RefreshToken]] = ZIO.none
      def revokeRefreshToken(token: String): Task[Boolean] = ZIO.succeed(false)
      def revokeAllUserTokens(userId: UUID): Task[Boolean] = ZIO.succeed(false)
    }
  }

  @Provides
  @Singleton
  def provideAuthService(userRepository: UserRepository, config: Configuration): AuthService = {
    val jwtSecret = config.get[String]("jwt.secret")
    val jwtExpiration = config.get[Int]("jwt.expiration")
    AuthServiceLive(userRepository, jwtSecret, jwtExpiration, 30)
  }
}
