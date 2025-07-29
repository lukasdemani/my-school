# --- !Ups

-- Users table
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    level VARCHAR(20) DEFAULT 'BEGINNER',
    native_language VARCHAR(10) DEFAULT 'pt',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Vocabulary table
CREATE TABLE vocabulary (
    id BIGSERIAL PRIMARY KEY,
    german_word VARCHAR(255) NOT NULL,
    english_translation VARCHAR(255) NOT NULL,
    portuguese_translation VARCHAR(255) NOT NULL,
    pronunciation VARCHAR(255),
    word_type VARCHAR(50) NOT NULL, -- noun, verb, adjective, etc.
    difficulty_level VARCHAR(20) DEFAULT 'BEGINNER',
    category VARCHAR(100), -- food, family, work, etc.
    example_sentence_de VARCHAR(500),
    example_sentence_en VARCHAR(500),
    example_sentence_pt VARCHAR(500),
    audio_url VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Lessons table
CREATE TABLE lessons (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    difficulty_level VARCHAR(20) DEFAULT 'BEGINNER',
    lesson_type VARCHAR(50) NOT NULL, -- vocabulary, grammar, conversation
    content JSONB NOT NULL,
    estimated_duration INTEGER, -- in minutes
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- User progress table
CREATE TABLE user_progress (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
    lesson_id BIGINT REFERENCES lessons(id) ON DELETE CASCADE,
    completed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    score INTEGER,
    time_spent INTEGER, -- in seconds
    UNIQUE(user_id, lesson_id)
);

-- User vocabulary progress
CREATE TABLE user_vocabulary_progress (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
    vocabulary_id BIGINT REFERENCES vocabulary(id) ON DELETE CASCADE,
    mastery_level INTEGER DEFAULT 0, -- 0-5 scale
    last_practiced TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    correct_answers INTEGER DEFAULT 0,
    total_attempts INTEGER DEFAULT 0,
    UNIQUE(user_id, vocabulary_id)
);

-- Practice sessions
CREATE TABLE practice_sessions (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
    session_type VARCHAR(50) NOT NULL, -- vocabulary, grammar, conversation
    started_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP,
    score INTEGER,
    content JSONB
);

-- AI conversations
CREATE TABLE ai_conversations (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
    conversation_data JSONB NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for better performance
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_vocabulary_difficulty ON vocabulary(difficulty_level);
CREATE INDEX idx_vocabulary_category ON vocabulary(category);
CREATE INDEX idx_user_progress_user_id ON user_progress(user_id);
CREATE INDEX idx_user_vocabulary_progress_user_id ON user_vocabulary_progress(user_id);
CREATE INDEX idx_practice_sessions_user_id ON practice_sessions(user_id);
CREATE INDEX idx_ai_conversations_user_id ON ai_conversations(user_id);

# --- !Downs

DROP TABLE IF EXISTS ai_conversations;
DROP TABLE IF EXISTS practice_sessions;
DROP TABLE IF EXISTS user_vocabulary_progress;
DROP TABLE IF EXISTS user_progress;
DROP TABLE IF EXISTS lessons;
DROP TABLE IF EXISTS vocabulary;
DROP TABLE IF EXISTS users;
