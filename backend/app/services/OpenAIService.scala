package services

import javax.inject._
import play.api.Configuration
import play.api.libs.json.*
import zio.*
import zio.http.*
import zio.json.*
import models.{ConversationRequest, ConversationResponse}
import scala.concurrent.{Future, ExecutionContext}

case class OpenAIRequest(
  model: String,
  messages: List[OpenAIMessage],
  max_tokens: Int = 150,
  temperature: Double = 0.7
)

case class OpenAIMessage(
  role: String,
  content: String
)

case class OpenAIResponse(
  choices: List[OpenAIChoice]
)

case class OpenAIChoice(
  message: OpenAIMessage
)

sealed trait OpenAIError extends Throwable
case class APIError(message: String) extends OpenAIError
case class ConfigurationError(message: String) extends OpenAIError
case class NetworkError(message: String) extends OpenAIError

implicit val openAIMessageFormat: Format[OpenAIMessage] = Json.format[OpenAIMessage]
implicit val openAIRequestFormat: Format[OpenAIRequest] = Json.format[OpenAIRequest]
implicit val openAIChoiceFormat: Format[OpenAIChoice] = Json.format[OpenAIChoice]
implicit val openAIResponseFormat: Format[OpenAIResponse] = Json.format[OpenAIResponse]

trait OpenAIService {
  def generateResponse(conversationHistory: List[OpenAIMessage]): ZIO[Any, OpenAIError, String]
  def translateText(text: String, targetLanguage: String = "de"): ZIO[Any, OpenAIError, String]
  def explainGrammar(sentence: String): ZIO[Any, OpenAIError, String]
  def generateConversationSuggestions(context: String, userLevel: String): ZIO[Any, OpenAIError, List[String]]
}

@Singleton
class OpenAIServiceImpl @Inject()(
  configuration: Configuration
)(implicit ec: ExecutionContext) extends OpenAIService {

  private val apiKey = configuration.get[String]("openai.api.key")
  private val apiUrl = configuration.get[String]("openai.api.url")

  def generateResponse(conversationHistory: List[OpenAIMessage]): ZIO[Any, OpenAIError, String] = {
    for {
      request <- buildOpenAIRequest(conversationHistory, "gpt-3.5-turbo", 150, 0.7)
      response <- callOpenAI(request)
      message <- extractResponseMessage(response)
    } yield message
  }

  def translateText(text: String, targetLanguage: String = "de"): ZIO[Any, OpenAIError, String] = {
    for {
      messages <- createTranslationMessages(text, targetLanguage)
      request <- buildOpenAIRequest(messages, "gpt-3.5-turbo", 100, 0.3)
      response <- callOpenAI(request)
      translation <- extractResponseMessage(response)
    } yield translation.trim
  }

  def explainGrammar(sentence: String): ZIO[Any, OpenAIError, String] = {
    for {
      messages <- createGrammarExplanationMessages(sentence)
      request <- buildOpenAIRequest(messages, "gpt-3.5-turbo", 200, 0.5)
      response <- callOpenAI(request)
      explanation <- extractResponseMessage(response)
    } yield explanation
  }

  def generateConversationSuggestions(context: String, userLevel: String): ZIO[Any, OpenAIError, List[String]] = {
    for {
      messages <- createSuggestionMessages(context, userLevel)
      request <- buildOpenAIRequest(messages, "gpt-3.5-turbo", 100, 0.6)
      response <- callOpenAI(request)
      rawSuggestions <- extractResponseMessage(response)
      suggestions <- parseSuggestions(rawSuggestions)
    } yield suggestions
  }

  private def buildOpenAIRequest(
    messages: List[OpenAIMessage],
    model: String,
    maxTokens: Int,
    temperature: Double
  ): ZIO[Any, OpenAIError, OpenAIRequest] = {
    ZIO.succeed(OpenAIRequest(
      model = model,
      messages = messages,
      max_tokens = maxTokens,
      temperature = temperature
    ))
  }

  private def createTranslationMessages(text: String, targetLanguage: String): ZIO[Any, OpenAIError, List[OpenAIMessage]] = {
    ZIO.succeed(List(
      OpenAIMessage("system", s"Translate the following text to $targetLanguage. Only return the translation."),
      OpenAIMessage("user", text)
    ))
  }

  private def createGrammarExplanationMessages(sentence: String): ZIO[Any, OpenAIError, List[OpenAIMessage]] = {
    ZIO.succeed(List(
      OpenAIMessage("system", "You are a German grammar teacher. Explain the grammar structure of the given German sentence in simple terms."),
      OpenAIMessage("user", sentence)
    ))
  }

  private def createSuggestionMessages(context: String, userLevel: String): ZIO[Any, OpenAIError, List[OpenAIMessage]] = {
    ZIO.succeed(List(
      OpenAIMessage("system", s"Generate 3 helpful German conversation suggestions for a $userLevel level student in this context: $context. Return only the suggestions, one per line."),
      OpenAIMessage("user", "Generate suggestions")
    ))
  }

  private def callOpenAI(request: OpenAIRequest): ZIO[Any, OpenAIError, OpenAIResponse] = {
    ZIO.attempt {
      generateMockResponse(request)
    }.mapError(ex => APIError(s"OpenAI API call failed: ${ex.getMessage}"))
  }

  private def extractResponseMessage(response: OpenAIResponse): ZIO[Any, OpenAIError, String] = {
    response.choices.headOption
      .map(_.message.content)
      .fold[ZIO[Any, OpenAIError, String]](
        ZIO.fail(APIError("No response from OpenAI"))
      )(message => ZIO.succeed(message))
  }

  private def parseSuggestions(rawSuggestions: String): ZIO[Any, OpenAIError, List[String]] = {
    ZIO.succeed(
      rawSuggestions
        .split("\n")
        .toList
        .map(_.trim)
        .filter(_.nonEmpty)
        .take(3)
    )
  }

  private def generateMockResponse(request: OpenAIRequest): OpenAIResponse = {
    val lastMessage = request.messages.lastOption.map(_.content).getOrElse("")
    
    val responseContent = if (lastMessage.toLowerCase.contains("restaurant")) {
      "Guten Tag! Was möchten Sie heute bestellen? Wir haben heute ein besonderes Menü."
    } else if (lastMessage.toLowerCase.contains("travel")) {
      "Wohin möchten Sie reisen? Deutschland hat viele schöne Städte zu besuchen!"
    } else if (lastMessage.toLowerCase.contains("hello") || lastMessage.toLowerCase.contains("hallo")) {
      "Hallo! Wie geht es Ihnen heute? Worüber möchten Sie sprechen?"
    } else if (request.messages.exists(_.content.contains("Generate suggestions"))) {
      "Ja, gerne\nNein, danke\nKönnen Sie das wiederholen?"
    } else {
      "Das ist sehr interessant! Können Sie mir mehr darüber erzählen? Versuchen Sie, auf Deutsch zu antworten."
    }
    
    OpenAIResponse(
      choices = List(
        OpenAIChoice(
          message = OpenAIMessage(
            role = "assistant",
            content = responseContent
          )
        )
      )
    )
  }
}
