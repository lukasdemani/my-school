-- Database initialization script for German Learning App

-- Drop tables if they exist (for development)
DROP TABLE IF EXISTS refresh_tokens CASCADE;
DROP TABLE IF EXISTS workspace_progress CASCADE;
DROP TABLE IF EXISTS language_workspaces CASCADE;
DROP TABLE IF EXISTS users CASCADE;

-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Users table
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    native_language VARCHAR(50) NOT NULL,
    bio TEXT,
    interests TEXT,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Language workspaces table
CREATE TABLE language_workspaces (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    target_language VARCHAR(50) NOT NULL,
    language_level VARCHAR(50) NOT NULL CHECK (language_level IN ('beginner', 'intermediate', 'advanced')),
    name VARCHAR(255) NOT NULL,
    description TEXT,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Workspace progress table
CREATE TABLE workspace_progress (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    workspace_id UUID UNIQUE NOT NULL REFERENCES language_workspaces(id) ON DELETE CASCADE,
    total_lessons_completed INTEGER DEFAULT 0,
    total_points INTEGER DEFAULT 0,
    current_streak INTEGER DEFAULT 0,
    longest_streak INTEGER DEFAULT 0,
    last_activity TIMESTAMP WITH TIME ZONE,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Refresh tokens table
CREATE TABLE refresh_tokens (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token VARCHAR(255) UNIQUE NOT NULL,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    is_revoked BOOLEAN DEFAULT false,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for better performance
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_active ON users(is_active);
CREATE INDEX idx_workspaces_user_id ON language_workspaces(user_id);
CREATE INDEX idx_workspaces_active ON language_workspaces(is_active);
CREATE INDEX idx_workspaces_target_language ON language_workspaces(target_language);
CREATE INDEX idx_progress_workspace_id ON workspace_progress(workspace_id);
CREATE INDEX idx_refresh_tokens_token ON refresh_tokens(token);
CREATE INDEX idx_refresh_tokens_user_id ON refresh_tokens(user_id);
CREATE INDEX idx_refresh_tokens_expires_at ON refresh_tokens(expires_at);

-- Function to update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Triggers to automatically update updated_at
CREATE TRIGGER update_users_updated_at BEFORE UPDATE ON users
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_workspaces_updated_at BEFORE UPDATE ON language_workspaces
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_progress_updated_at BEFORE UPDATE ON workspace_progress
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Insert some sample data for testing
INSERT INTO users (name, email, password_hash, native_language, bio, interests) VALUES
(
    'Test User',
    'test@example.com',
    '$2a$10$example.hash.here', -- This would be a real bcrypt hash
    'portuguese',
    'Sou um desenvolvedor interessado em aprender alemão para expandir minhas oportunidades profissionais.',
    'Tecnologia, viagens, cultura alemã'
);

-- Get the test user ID for workspace creation
DO $$
DECLARE
    test_user_id UUID;
    workspace_id UUID;
BEGIN
    SELECT id INTO test_user_id FROM users WHERE email = 'test@example.com';
    
    -- Create a test workspace
    INSERT INTO language_workspaces (user_id, target_language, language_level, name, description)
    VALUES (
        test_user_id,
        'german',
        'beginner',
        'Alemão para Iniciantes',
        'Meu primeiro workspace para aprender alemão básico'
    ) RETURNING id INTO workspace_id;
    
    -- Create initial progress for the workspace
    INSERT INTO workspace_progress (workspace_id, total_lessons_completed, total_points, current_streak, longest_streak)
    VALUES (workspace_id, 0, 0, 0, 0);
END $$;
