package services

import javax.inject._
import play.api.Configuration
import play.api.libs.json.*
import zio.*
import zio.http.*
import models.{ConversationRequest, ConversationResponse}
import scala.concurrent.{Future, ExecutionContext}
import play.api.libs.ws._
import scala.util.{Success, Failure}

// Gemini API request/response models
case class GeminiRequest(
  contents: List[GeminiContent],
  generationConfig: Option[GeminiGenerationConfig] = None
)

case class GeminiContent(
  parts: List[GeminiPart],
  role: Option[String] = None
)

case class GeminiPart(
  text: String
)

case class GeminiGenerationConfig(
  temperature: Option[Double] = None,
  topK: Option[Int] = None,
  topP: Option[Double] = None,
  maxOutputTokens: Option[Int] = None
)

case class GeminiResponse(
  candidates: List[GeminiCandidate]
)

case class GeminiCandidate(
  content: GeminiContent,
  finishReason: Option[String] = None,
  index: Option[Int] = None,
  safetyRatings: Option[List[GeminiSafetyRating]] = None
)

case class GeminiSafetyRating(
  category: String,
  probability: String
)

// Legacy message format for compatibility
case class OpenAIMessage(
  role: String,
  content: String
)

sealed trait OpenAIError extends Throwable
case class APIError(message: String) extends OpenAIError
case class ConfigurationError(message: String) extends OpenAIError
case class NetworkError(message: String) extends OpenAIError

// JSON formatters
implicit val geminiPartFormat: Format[GeminiPart] = Json.format[GeminiPart]
implicit val geminiContentFormat: Format[GeminiContent] = Json.format[GeminiContent]
implicit val geminiGenerationConfigFormat: Format[GeminiGenerationConfig] = Json.format[GeminiGenerationConfig]
implicit val geminiRequestFormat: Format[GeminiRequest] = Json.format[GeminiRequest]
implicit val geminiSafetyRatingFormat: Format[GeminiSafetyRating] = Json.format[GeminiSafetyRating]
implicit val geminiCandidateFormat: Format[GeminiCandidate] = Json.format[GeminiCandidate]
implicit val geminiResponseFormat: Format[GeminiResponse] = Json.format[GeminiResponse]

// Legacy format for backward compatibility
implicit val openAIMessageFormat: Format[OpenAIMessage] = Json.format[OpenAIMessage]

trait OpenAIService {
  def generateResponse(conversationHistory: List[OpenAIMessage]): ZIO[Any, OpenAIError, String]
  def translateText(text: String, targetLanguage: String = "de"): ZIO[Any, OpenAIError, String]
  def explainGrammar(sentence: String): ZIO[Any, OpenAIError, String]
  def generateConversationSuggestions(context: String, userLevel: String): ZIO[Any, OpenAIError, List[String]]
}

@Singleton
class GeminiServiceImpl @Inject()(
  configuration: Configuration,
  ws: WSClient
)(implicit ec: ExecutionContext) extends OpenAIService {

  private val apiKey = configuration.getOptional[String]("gemini.api.key")
    .getOrElse {
      // Para desenvolvimento, usar a chave diretamente (NUNCA fazer isso em produção)
      if (play.api.Environment.simple().mode == play.api.Mode.Dev) {
        "AIzaSyCK-9M4e3Bmby78WAi2c0cNOvSFymeHDoI"
      } else {
        throw new RuntimeException("GEMINI_API_KEY environment variable not set")
      }
    }
  
  private val apiUrl = configuration.get[String]("gemini.api.url")

  def generateResponse(conversationHistory: List[OpenAIMessage]): ZIO[Any, OpenAIError, String] = {
    for {
      request <- buildGeminiRequest(conversationHistory, temperature = Some(0.7), maxTokens = Some(150))
      response <- callGemini(request)
      message <- extractResponseMessage(response)
    } yield message
  }

  def translateText(text: String, targetLanguage: String = "de"): ZIO[Any, OpenAIError, String] = {
    val messages = List(
      OpenAIMessage("system", s"Translate the following text to $targetLanguage. Only return the translation."),
      OpenAIMessage("user", text)
    )
    for {
      request <- buildGeminiRequest(messages, temperature = Some(0.3), maxTokens = Some(100))
      response <- callGemini(request)
      translation <- extractResponseMessage(response)
    } yield translation.trim
  }

  def explainGrammar(sentence: String): ZIO[Any, OpenAIError, String] = {
    val messages = List(
      OpenAIMessage("system", "You are a German grammar teacher. Explain the grammar structure of the given German sentence in simple terms."),
      OpenAIMessage("user", sentence)
    )
    for {
      request <- buildGeminiRequest(messages, temperature = Some(0.5), maxTokens = Some(200))
      response <- callGemini(request)
      explanation <- extractResponseMessage(response)
    } yield explanation
  }

  def generateConversationSuggestions(context: String, userLevel: String): ZIO[Any, OpenAIError, List[String]] = {
    val messages = List(
      OpenAIMessage("system", s"Generate 3 helpful German conversation suggestions for a $userLevel level student in this context: $context. Return only the suggestions, one per line."),
      OpenAIMessage("user", "Generate suggestions")
    )
    for {
      request <- buildGeminiRequest(messages, temperature = Some(0.6), maxTokens = Some(100))
      response <- callGemini(request)
      rawSuggestions <- extractResponseMessage(response)
      suggestions <- parseSuggestions(rawSuggestions)
    } yield suggestions
  }

  private def buildGeminiRequest(
    messages: List[OpenAIMessage],
    temperature: Option[Double] = None,
    maxTokens: Option[Int] = None
  ): ZIO[Any, OpenAIError, GeminiRequest] = {
    ZIO.attempt {
      // Convert OpenAI-style messages to Gemini format
      val geminiContents = convertMessagesToGeminiFormat(messages)
      
      val generationConfig = Some(GeminiGenerationConfig(
        temperature = temperature,
        maxOutputTokens = maxTokens,
        topK = Some(40),
        topP = Some(0.95)
      ))
      
      GeminiRequest(
        contents = geminiContents,
        generationConfig = generationConfig
      )
    }.mapError(ex => ConfigurationError(s"Failed to build Gemini request: ${ex.getMessage}"))
  }

  private def convertMessagesToGeminiFormat(messages: List[OpenAIMessage]): List[GeminiContent] = {
    // Gemini doesn't have a system role, so we'll combine system messages with user messages
    val systemMessages = messages.filter(_.role == "system")
    val userMessages = messages.filter(_.role == "user")
    val assistantMessages = messages.filter(_.role == "assistant")
    
    val combinedText = if (systemMessages.nonEmpty) {
      val systemText = systemMessages.map(_.content).mkString("\n")
      val userText = userMessages.map(_.content).mkString("\n")
      s"$systemText\n\n$userText"
    } else {
      userMessages.map(_.content).mkString("\n")
    }
    
    List(GeminiContent(
      parts = List(GeminiPart(combinedText)),
      role = Some("user")
    ))
  }

  private def callGemini(request: GeminiRequest): ZIO[Any, OpenAIError, GeminiResponse] = {
    ZIO.fromFuture { _ =>
      val url = s"$apiUrl?key=$apiKey"
      
      ws.url(url)
        .addHttpHeaders("Content-Type" -> "application/json")
        .post(Json.toJson(request))
        .map { response =>
          if (response.status == 200) {
            response.json.as[GeminiResponse]
          } else {
            throw new RuntimeException(s"Gemini API error: ${response.status} - ${response.body}")
          }
        }
        .recover {
          case ex => throw new RuntimeException(s"Network error calling Gemini: ${ex.getMessage}")
        }
    }.mapError {
      case ex: RuntimeException if ex.getMessage.contains("Network error") => 
        NetworkError(ex.getMessage)
      case ex: RuntimeException if ex.getMessage.contains("Gemini API error") => 
        APIError(ex.getMessage)
      case ex => 
        APIError(s"Unexpected error: ${ex.getMessage}")
    }
  }

  private def extractResponseMessage(response: GeminiResponse): ZIO[Any, OpenAIError, String] = {
    response.candidates.headOption
      .flatMap(_.content.parts.headOption)
      .map(_.text)
      .fold[ZIO[Any, OpenAIError, String]](
        ZIO.fail(APIError("No response from Gemini"))
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
}
