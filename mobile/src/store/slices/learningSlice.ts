import type { Exercise, LearningPath, Lesson, Topic } from '@/types/learning';
import { createSlice, PayloadAction } from '@reduxjs/toolkit';

interface LearningState {
  currentPath: LearningPath | null;
  availablePaths: LearningPath[];
  currentTopic: Topic | null;
  currentLesson: Lesson | null;
  currentExercise: Exercise | null;
  recommendedContent: {
    topics: Topic[];
    lessons: Lesson[];
    exercises: Exercise[];
  };
  recentActivity: {
    lessonIds: string[];
    topicIds: string[];
    completedAt: string[];
  };
  isLoading: boolean;
  error: string | null;
}

const initialState: LearningState = {
  currentPath: null,
  availablePaths: [],
  currentTopic: null,
  currentLesson: null,
  currentExercise: null,
  recommendedContent: {
    topics: [],
    lessons: [],
    exercises: [],
  },
  recentActivity: {
    lessonIds: [],
    topicIds: [],
    completedAt: [],
  },
  isLoading: false,
  error: null,
};

export const learningSlice = createSlice({
  name: 'learning',
  initialState,
  reducers: {
    // Loading states
    fetchLearningDataStart: (state) => {
      state.isLoading = true;
      state.error = null;
    },
    fetchLearningDataSuccess: (
      state,
      action: PayloadAction<{
        currentPath?: LearningPath;
        availablePaths: LearningPath[];
        recommendedContent: {
          topics: Topic[];
          lessons: Lesson[];
          exercises: Exercise[];
        };
      }>
    ) => {
      if (action.payload.currentPath) {
        state.currentPath = action.payload.currentPath;
      }
      state.availablePaths = action.payload.availablePaths;
      state.recommendedContent = action.payload.recommendedContent;
      state.isLoading = false;
      state.error = null;
    },
    fetchLearningDataFailure: (state, action: PayloadAction<string>) => {
      state.isLoading = false;
      state.error = action.payload;
    },

    // Path management
    setCurrentPath: (state, action: PayloadAction<LearningPath>) => {
      state.currentPath = action.payload;
    },
    updatePathProgress: (
      state,
      action: PayloadAction<{
        pathId: string;
        topicId: string;
        progress: number;
      }>
    ) => {
      if (state.currentPath && state.currentPath.id === action.payload.pathId) {
        const topic = state.currentPath.topics.find(
          (t) => t.id === action.payload.topicId
        );
        if (topic) {
          topic.progress = action.payload.progress;
        }
      }
    },

    // Current content
    setCurrentTopic: (state, action: PayloadAction<Topic>) => {
      state.currentTopic = action.payload;
    },
    setCurrentLesson: (state, action: PayloadAction<Lesson>) => {
      state.currentLesson = action.payload;
    },
    setCurrentExercise: (state, action: PayloadAction<Exercise>) => {
      state.currentExercise = action.payload;
    },

    // Progress tracking
    completeLessonSuccess: (
      state,
      action: PayloadAction<{
        lessonId: string;
        score: number;
        xpEarned: number;
      }>
    ) => {
      // Update recent activity
      state.recentActivity.lessonIds.unshift(action.payload.lessonId);
      state.recentActivity.completedAt.unshift(new Date().toISOString());

      // Keep only last 10 activities
      if (state.recentActivity.lessonIds.length > 10) {
        state.recentActivity.lessonIds = state.recentActivity.lessonIds.slice(
          0,
          10
        );
        state.recentActivity.completedAt =
          state.recentActivity.completedAt.slice(0, 10);
      }

      // Update current lesson if it matches
      if (
        state.currentLesson &&
        state.currentLesson.id === action.payload.lessonId
      ) {
        state.currentLesson.isCompleted = true;
        state.currentLesson.score = action.payload.score;
      }
    },

    completeTopicSuccess: (
      state,
      action: PayloadAction<{
        topicId: string;
        finalScore: number;
      }>
    ) => {
      // Update recent activity
      state.recentActivity.topicIds.unshift(action.payload.topicId);

      // Keep only last 10 activities
      if (state.recentActivity.topicIds.length > 10) {
        state.recentActivity.topicIds = state.recentActivity.topicIds.slice(
          0,
          10
        );
      }

      // Update current topic if it matches
      if (
        state.currentTopic &&
        state.currentTopic.id === action.payload.topicId
      ) {
        state.currentTopic.status = 'completed';
        state.currentTopic.progress = 100;
      }
    },

    // Recommendations
    updateRecommendations: (
      state,
      action: PayloadAction<{
        topics: Topic[];
        lessons: Lesson[];
        exercises: Exercise[];
      }>
    ) => {
      state.recommendedContent = action.payload;
    },

    // Clear state
    clearLearningData: (state) => {
      state.currentPath = null;
      state.availablePaths = [];
      state.currentTopic = null;
      state.currentLesson = null;
      state.currentExercise = null;
      state.recommendedContent = {
        topics: [],
        lessons: [],
        exercises: [],
      };
      state.recentActivity = {
        lessonIds: [],
        topicIds: [],
        completedAt: [],
      };
      state.isLoading = false;
      state.error = null;
    },
  },
});

export const {
  fetchLearningDataStart,
  fetchLearningDataSuccess,
  fetchLearningDataFailure,
  setCurrentPath,
  updatePathProgress,
  setCurrentTopic,
  setCurrentLesson,
  setCurrentExercise,
  completeLessonSuccess,
  completeTopicSuccess,
  updateRecommendations,
  clearLearningData,
} = learningSlice.actions;

export default learningSlice.reducer;
