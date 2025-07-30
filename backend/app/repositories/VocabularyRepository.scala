package repositories

import javax.inject._
import models.Vocabulary
import javax.sql.DataSource
import zio.*
import scala.concurrent.{Future, ExecutionContext}
import java.sql.{Connection, PreparedStatement, ResultSet}

@Singleton
class VocabularyRepository @Inject()(
  dataSource: DataSource
)(implicit ec: ExecutionContext) {

  def findFiltered(
    level: Option[String],
    category: Option[String],
    limit: Int,
    offset: Int
  ): Future[List[Vocabulary]] = {
    val effect = ZIO.attempt {
      val conn = dataSource.getConnection()
      try {
        val baseQuery = "SELECT * FROM vocabulary WHERE 1=1"
        val conditions = List(
          level.map(_ => "AND difficulty_level = ?"),
          category.map(_ => "AND category = ?")
        ).flatten
        
        val sql = s"$baseQuery ${conditions.mkString(" ")} ORDER BY created_at DESC LIMIT ? OFFSET ?"
        
        val stmt = conn.prepareStatement(sql)
        var paramIndex = 1
        
        level.foreach { l =>
          stmt.setString(paramIndex, l)
          paramIndex += 1
        }
        
        category.foreach { c =>
          stmt.setString(paramIndex, c)
          paramIndex += 1
        }
        
        stmt.setInt(paramIndex, limit)
        stmt.setInt(paramIndex + 1, offset)
        
        val rs = stmt.executeQuery()
        extractVocabularyList(rs)
      } finally {
        conn.close()
      }
    }.catchAll { _ =>
      ZIO.succeed(List.empty)
    }

    Unsafe.unsafe { implicit unsafe =>
      Runtime.default.unsafe.runToFuture(effect)
    }
  }

  def findById(id: Long): Future[Option[Vocabulary]] = {
    val effect = ZIO.attempt {
      val conn = dataSource.getConnection()
      try {
        val sql = "SELECT * FROM vocabulary WHERE id = ?"
        val stmt = conn.prepareStatement(sql)
        stmt.setLong(1, id)
        
        val rs = stmt.executeQuery()
        if (rs.next()) {
          Some(extractVocabulary(rs))
        } else {
          None
        }
      } finally {
        conn.close()
      }
    }.catchAll { _ =>
      ZIO.succeed(None)
    }

    Unsafe.unsafe { implicit unsafe =>
      Runtime.default.unsafe.runToFuture(effect)
    }
  }

  def create(vocabulary: Vocabulary): Future[Vocabulary] = {
    val effect = ZIO.attempt {
      val conn = dataSource.getConnection()
      try {
        val sql = """
          INSERT INTO vocabulary (
            german_word, english_translation, portuguese_translation, pronunciation,
            word_type, difficulty_level, category, example_sentence_de,
            example_sentence_en, example_sentence_pt, audio_url, created_at
          ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
          RETURNING *
        """
        
        val stmt = conn.prepareStatement(sql)
        stmt.setString(1, vocabulary.germanWord)
        stmt.setString(2, vocabulary.englishTranslation)
        stmt.setString(3, vocabulary.portugueseTranslation)
        stmt.setString(4, vocabulary.pronunciation.orNull)
        stmt.setString(5, vocabulary.wordType)
        stmt.setString(6, vocabulary.difficultyLevel)
        stmt.setString(7, vocabulary.category.orNull)
        stmt.setString(8, vocabulary.exampleSentenceDe.orNull)
        stmt.setString(9, vocabulary.exampleSentenceEn.orNull)
        stmt.setString(10, vocabulary.exampleSentencePt.orNull)
        stmt.setString(11, vocabulary.audioUrl.orNull)
        stmt.setTimestamp(12, java.sql.Timestamp.from(vocabulary.createdAt))
        
        val rs = stmt.executeQuery()
        rs.next()
        extractVocabulary(rs)
      } finally {
        conn.close()
      }
    }

    Unsafe.unsafe { implicit unsafe =>
      Runtime.default.unsafe.runToFuture(effect)
    }
  }

  def update(vocabulary: Vocabulary): Future[Option[Vocabulary]] = {
    val effect = ZIO.attempt {
      val conn = dataSource.getConnection()
      try {
        val sql = """
          UPDATE vocabulary SET
            german_word = ?, english_translation = ?, portuguese_translation = ?,
            pronunciation = ?, word_type = ?, difficulty_level = ?, category = ?,
            example_sentence_de = ?, example_sentence_en = ?, example_sentence_pt = ?,
            audio_url = ?
          WHERE id = ?
          RETURNING *
        """
        
        val stmt = conn.prepareStatement(sql)
        stmt.setString(1, vocabulary.germanWord)
        stmt.setString(2, vocabulary.englishTranslation)
        stmt.setString(3, vocabulary.portugueseTranslation)
        stmt.setString(4, vocabulary.pronunciation.orNull)
        stmt.setString(5, vocabulary.wordType)
        stmt.setString(6, vocabulary.difficultyLevel)
        stmt.setString(7, vocabulary.category.orNull)
        stmt.setString(8, vocabulary.exampleSentenceDe.orNull)
        stmt.setString(9, vocabulary.exampleSentenceEn.orNull)
        stmt.setString(10, vocabulary.exampleSentencePt.orNull)
        stmt.setString(11, vocabulary.audioUrl.orNull)
        stmt.setLong(12, vocabulary.id.get)
        
        val rs = stmt.executeQuery()
        if (rs.next()) {
          Some(extractVocabulary(rs))
        } else {
          None
        }
      } finally {
        conn.close()
      }
    }.catchAll { _ =>
      ZIO.succeed(None)
    }

    Unsafe.unsafe { implicit unsafe =>
      Runtime.default.unsafe.runToFuture(effect)
    }
  }

  def delete(id: Long): Future[Boolean] = {
    val effect = ZIO.attempt {
      val conn = dataSource.getConnection()
      try {
        val sql = "DELETE FROM vocabulary WHERE id = ?"
        val stmt = conn.prepareStatement(sql)
        stmt.setLong(1, id)
        
        stmt.executeUpdate() > 0
      } finally {
        conn.close()
      }
    }.catchAll { _ =>
      ZIO.succeed(false)
    }

    Unsafe.unsafe { implicit unsafe =>
      Runtime.default.unsafe.runToFuture(effect)
    }
  }

  def findBatch(offset: Long, batchSize: Int): Future[List[Vocabulary]] = {
    val effect = ZIO.attempt {
      val conn = dataSource.getConnection()
      try {
        val sql = "SELECT * FROM vocabulary ORDER BY id LIMIT ? OFFSET ?"
        val stmt = conn.prepareStatement(sql)
        stmt.setInt(1, batchSize)
        stmt.setLong(2, offset)
        
        val rs = stmt.executeQuery()
        extractVocabularyList(rs)
      } finally {
        conn.close()
      }
    }.catchAll { _ =>
      ZIO.succeed(List.empty)
    }

    Unsafe.unsafe { implicit unsafe =>
      Runtime.default.unsafe.runToFuture(effect)
    }
  }

  private def extractVocabulary(rs: ResultSet): Vocabulary = {
    Vocabulary(
      id = Some(rs.getLong("id")),
      germanWord = rs.getString("german_word"),
      englishTranslation = rs.getString("english_translation"),
      portugueseTranslation = rs.getString("portuguese_translation"),
      pronunciation = Option(rs.getString("pronunciation")),
      wordType = rs.getString("word_type"),
      difficultyLevel = rs.getString("difficulty_level"),
      category = Option(rs.getString("category")),
      exampleSentenceDe = Option(rs.getString("example_sentence_de")),
      exampleSentenceEn = Option(rs.getString("example_sentence_en")),
      exampleSentencePt = Option(rs.getString("example_sentence_pt")),
      audioUrl = Option(rs.getString("audio_url")),
      createdAt = rs.getTimestamp("created_at").toInstant
    )
  }

  private def extractVocabularyList(rs: ResultSet): List[Vocabulary] = {
    val buffer = scala.collection.mutable.ListBuffer[Vocabulary]()
    while (rs.next()) {
      buffer += extractVocabulary(rs)
    }
    buffer.toList
  }
}
