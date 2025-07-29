import { CEFRLevel, InterestCategory } from './user';

export type TopicStatus = 'locked' | 'available' | 'in-progress' | 'completed';
export type ContentType = 'lesson' | 'exercise' | 'test' | 'conversation';
export type DifficultyLevel = 'easy' | 'medium' | 'hard';

export interface Topic {
  id: string;
  title: string;
  description: string;
  category: string; // e.g., "grammar", "vocabulary", "conversation"
  level: CEFRLevel;
  prerequisites: string[]; // Topic IDs
  estimatedTime: number; // minutes
  status: TopicStatus;
  progress: number; // 0-100
  lessons: Lesson[];
  exercises: Exercise[];
  xpReward: number;
  icon: string;
  color: string;
}

export interface Lesson {
  id: string;
  topicId: string;
  title: string;
  description: string;
  type: ContentType;
  order: number;
  estimatedTime: number;
  content: LessonContent;
  isCompleted: boolean;
  score?: number;
  completedAt?: string;
}

export interface LessonContent {
  introduction: string;
  explanation: string;
  examples: Example[];
  keyPoints: string[];
  culturalNotes?: string[];
  audioUrl?: string;
  videoUrl?: string;
}

export interface Example {
  german: string;
  english: string;
  portuguese: string;
  context?: string;
  audioUrl?: string;
  difficulty: DifficultyLevel;
}

export interface Exercise {
  id: string;
  topicId: string;
  type: ExerciseType;
  title: string;
  instruction: string;
  questions: Question[];
  timeLimit?: number; // seconds
  passingScore: number; // percentage
  maxAttempts?: number;
  attempts: number;
  bestScore?: number;
  isCompleted: boolean;
}

export type ExerciseType =
  | 'multiple_choice'
  | 'fill_blank'
  | 'translation'
  | 'matching'
  | 'ordering'
  | 'speaking'
  | 'listening'
  | 'conversation';

export interface Question {
  id: string;
  type: ExerciseType;
  question: string;
  options?: string[]; // for multiple choice
  correctAnswer: string | string[];
  explanation?: string;
  hints?: string[];
  audioUrl?: string;
  imageUrl?: string;
  points: number;
  context?: InterestCategory; // for personalized content
}

export interface LearningPath {
  id: string;
  name: string;
  description: string;
  level: CEFRLevel;
  topics: Topic[];
  totalTopics: number;
  completedTopics: number;
  estimatedDuration: number; // hours
  isPersonalized: boolean;
  interests: InterestCategory[];
}

export interface StudySession {
  id: string;
  userId: string;
  startTime: string;
  endTime?: string;
  duration: number; // minutes
  topicsStudied: string[];
  exercisesCompleted: number;
  xpEarned: number;
  accuracy: number; // percentage
  type: 'lesson' | 'practice' | 'test' | 'conversation';
}
