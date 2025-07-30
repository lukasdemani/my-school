package modules

import com.google.inject.AbstractModule
import repositories.{UserRepository, UserRepositoryLive, VocabularyRepository}
import services.{AuthService, AuthServiceLive, VocabularyService, VocabularyServiceImpl, OpenAIService, OpenAIServiceImpl}
import org.apache.pekko.actor.typed.ActorSystem
import org.apache.pekko.actor.typed.scaladsl.Behaviors

class AppModule extends AbstractModule {

  override def configure(): Unit = {
    // Configure Pekko ActorSystem
    val actorSystem = ActorSystem(Behaviors.empty, "german-learning-system")
    bind(classOf[ActorSystem[_]]).toInstance(actorSystem)
    
    // Bind repositories
    bind(classOf[UserRepository]).to(classOf[UserRepositoryLive])
    // VocabularyRepository is a concrete class, not interface
    
    // Bind services
    bind(classOf[AuthService]).to(classOf[AuthServiceLive])
    bind(classOf[VocabularyService]).to(classOf[VocabularyServiceImpl])
    bind(classOf[OpenAIService]).to(classOf[OpenAIServiceImpl])
  }
}
