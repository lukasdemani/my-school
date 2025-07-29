export interface Vocabulary {
  id: string;
  germanWord: string;
  englishTranslation: string;
  portugueseTranslation: string;
  pronunciation?: string;
  wordType: WordType;
  difficultyLevel: string;
  category?: string;
  exampleSentenceDe?: string;
  exampleSentenceEn?: string;
  exampleSentencePt?: string;
  audioUrl?: string;
  imageUrl?: string;
  masteryLevel: number; // 0-5
  lastPracticed?: string;
  timesCorrect: number;
  timesIncorrect: number;
  createdAt: string;
}

export type WordType =
  | 'noun'
  | 'verb'
  | 'adjective'
  | 'adverb'
  | 'preposition'
  | 'conjunction'
  | 'article'
  | 'pronoun'
  | 'interjection';

export interface FlashCard {
  id: string;
  vocabularyId: string;
  front: string;
  back: string;
  hint?: string;
  difficulty: number; // 1-5
  nextReview: string;
  reviewCount: number;
  successStreak: number;
  interval: number; // days until next review
}

export interface ConversationSession {
  id: string;
  userId: string;
  topic: string;
  level: string;
  startTime: string;
  endTime?: string;
  messages: ConversationMessage[];
  context: ConversationContext;
  feedback: ConversationFeedback;
}

export interface ConversationMessage {
  id: string;
  sender: 'user' | 'ai' | 'system';
  content: string;
  timestamp: string;
  audioUrl?: string;
  corrections?: Correction[];
  suggestions?: string[];
}

export interface ConversationContext {
  scenario: string; // e.g., "restaurant", "job_interview", "travel"
  setting: string;
  goal: string;
  vocabulary: string[];
  grammar_focus: string[];
}

export interface ConversationFeedback {
  overallScore: number; // 0-100
  fluency: number;
  accuracy: number;
  vocabulary: number;
  pronunciation: number;
  corrections: Correction[];
  suggestions: string[];
  achievements: string[];
}

export interface Correction {
  original: string;
  corrected: string;
  explanation: string;
  type: 'grammar' | 'vocabulary' | 'pronunciation' | 'style';
}

export interface AIResponse {
  message: string;
  audioUrl?: string;
  suggestions: string[];
  corrections: Correction[];
  contextualHelp?: string;
}
