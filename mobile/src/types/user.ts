export type CEFRLevel = 'A1' | 'A2' | 'B1' | 'B2' | 'C1' | 'C2';

export type LearningStyle = 'visual' | 'auditory' | 'kinesthetic' | 'reading';

export type InterestCategory =
  | 'sports'
  | 'technology'
  | 'travel'
  | 'food'
  | 'music'
  | 'business'
  | 'health'
  | 'culture'
  | 'science'
  | 'arts';

export interface Interest {
  id: string;
  category: InterestCategory;
  subcategories: string[];
  intensity: 1 | 2 | 3 | 4 | 5; // Priority level
  isActive: boolean;
}

export interface UserProfile {
  id: string;
  email: string;
  firstName: string;
  lastName: string;
  currentLevel: CEFRLevel;
  targetLevel: CEFRLevel;
  nativeLanguage: string;
  interests: Interest[];
  learningStyle: LearningStyle;
  dailyGoal: number; // minutes per day
  studyStreak: number;
  totalXP: number;
  createdAt: string;
  updatedAt: string;
}

export interface SkillProgress {
  listening: number; // 0-100
  reading: number;
  speaking: number;
  writing: number;
  grammar: number;
  vocabulary: number;
}

export interface LearningGoal {
  id: string;
  type: 'daily' | 'weekly' | 'monthly' | 'custom';
  target: number;
  current: number;
  unit: 'minutes' | 'lessons' | 'words' | 'exercises';
  deadline?: string;
  isCompleted: boolean;
}

export interface Achievement {
  id: string;
  title: string;
  description: string;
  icon: string;
  unlockedAt?: string;
  xpReward: number;
  criteria: {
    type: string;
    value: number;
  };
}
