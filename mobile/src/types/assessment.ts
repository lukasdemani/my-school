import { CEFRLevel, SkillProgress } from './user';

export interface ProficiencyTest {
  id: string;
  title: string;
  description: string;
  sections: TestSection[];
  totalQuestions: number;
  estimatedTime: number; // minutes
  isAdaptive: boolean;
  currentLevel?: CEFRLevel;
  targetLevel?: CEFRLevel;
}

export interface TestSection {
  id: string;
  name: string;
  type: TestSectionType;
  questions: TestQuestion[];
  timeLimit: number; // seconds
  instructions: string;
  weight: number; // contribution to final score
}

export type TestSectionType =
  | 'listening'
  | 'reading'
  | 'grammar'
  | 'vocabulary'
  | 'speaking'
  | 'writing';

export interface TestQuestion {
  id: string;
  type: 'multiple_choice' | 'true_false' | 'fill_blank' | 'essay' | 'speaking';
  level: CEFRLevel;
  question: string;
  options?: string[];
  correctAnswer: string | string[];
  points: number;
  audioUrl?: string;
  imageUrl?: string;
  timeLimit?: number;
  adaptiveWeight?: number; // for adaptive testing
}

export interface TestResult {
  id: string;
  testId: string;
  userId: string;
  startTime: string;
  endTime: string;
  duration: number; // minutes
  overallScore: number; // 0-100
  estimatedLevel: CEFRLevel;
  sectionScores: SectionScore[];
  skillBreakdown: SkillProgress;
  strengths: string[];
  weaknesses: string[];
  recommendations: string[];
  certificationReady: boolean;
}

export interface SectionScore {
  section: TestSectionType;
  score: number; // 0-100
  questionsCorrect: number;
  totalQuestions: number;
  timeSpent: number; // seconds
}

export interface LevelRequirements {
  level: CEFRLevel;
  minScore: number;
  requiredSkills: {
    listening: number;
    reading: number;
    speaking: number;
    writing: number;
    grammar: number;
    vocabulary: number;
  };
  description: string;
  canDoStatements: string[];
}

export interface CertificationPrep {
  examType: 'Goethe' | 'TestDaF' | 'DSH' | 'telc';
  targetLevel: CEFRLevel;
  readinessScore: number; // 0-100
  estimatedDate: string;
  studyPlan: StudyPlanItem[];
  practiceTests: PracticeTest[];
}

export interface StudyPlanItem {
  topic: string;
  priority: 'high' | 'medium' | 'low';
  estimatedHours: number;
  completed: boolean;
  resources: string[];
}

export interface PracticeTest {
  id: string;
  name: string;
  type: 'full' | 'section' | 'mini';
  duration: number;
  difficulty: CEFRLevel;
  isCompleted: boolean;
  score?: number;
  takenAt?: string;
}
