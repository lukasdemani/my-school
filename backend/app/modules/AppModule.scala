package modules

import com.google.inject.AbstractModule
import io.getquill.jdbczio.Quill
import io.getquill.{PostgresZioJdbcContext, SnakeCase}
import repositories.{UserRepository, UserRepositoryLive}
import services.{AuthService, AuthServiceLive}
import zio._
import javax.sql.DataSource

class AppModule extends AbstractModule {

  override def configure(): Unit = {
    // Configure Quill context
    val quillLayer = Quill.Postgres.fromNamingStrategy(SnakeCase)
    
    // Configure repositories
    val userRepositoryLayer = UserRepositoryLive.layer
    
    // Configure services  
    val authServiceLayer = AuthServiceLive.layer
    
    // Complete layer composition
    val appLayer = quillLayer >>> userRepositoryLayer >>> authServiceLayer
    
    // Bind implementations
    bind(classOf[UserRepository]).to(classOf[UserRepositoryLive])
    bind(classOf[AuthService]).to(classOf[AuthServiceLive])
  }
}
