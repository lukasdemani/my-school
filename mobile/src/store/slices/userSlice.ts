import type {
  Achievement,
  Interest,
  LearningGoal,
  SkillProgress,
  UserProfile,
} from '@/types/user';
import { createSlice, PayloadAction } from '@reduxjs/toolkit';

interface UserState {
  profile: UserProfile | null;
  skillProgress: SkillProgress | null;
  goals: LearningGoal[];
  achievements: Achievement[];
  interests: Interest[];
  isLoading: boolean;
  error: string | null;
}

const initialState: UserState = {
  profile: null,
  skillProgress: null,
  goals: [],
  achievements: [],
  interests: [],
  isLoading: false,
  error: null,
};

export const userSlice = createSlice({
  name: 'user',
  initialState,
  reducers: {
    fetchUserStart: (state) => {
      state.isLoading = true;
      state.error = null;
    },
    fetchUserSuccess: (
      state,
      action: PayloadAction<{
        profile: UserProfile;
        skillProgress: SkillProgress;
        goals: LearningGoal[];
        achievements: Achievement[];
        interests: Interest[];
      }>
    ) => {
      state.profile = action.payload.profile;
      state.skillProgress = action.payload.skillProgress;
      state.goals = action.payload.goals;
      state.achievements = action.payload.achievements;
      state.interests = action.payload.interests;
      state.isLoading = false;
      state.error = null;
    },
    fetchUserFailure: (state, action: PayloadAction<string>) => {
      state.isLoading = false;
      state.error = action.payload;
    },
    updateUserProfile: (state, action: PayloadAction<Partial<UserProfile>>) => {
      if (state.profile) {
        state.profile = { ...state.profile, ...action.payload };
      }
    },
    updateSkillProgress: (
      state,
      action: PayloadAction<Partial<SkillProgress>>
    ) => {
      if (state.skillProgress) {
        state.skillProgress = { ...state.skillProgress, ...action.payload };
      }
    },
    addGoal: (state, action: PayloadAction<LearningGoal>) => {
      state.goals.push(action.payload);
    },
    updateGoal: (
      state,
      action: PayloadAction<{ id: string; updates: Partial<LearningGoal> }>
    ) => {
      const goal = state.goals.find((g) => g.id === action.payload.id);
      if (goal) {
        Object.assign(goal, action.payload.updates);
      }
    },
    addAchievement: (state, action: PayloadAction<Achievement>) => {
      state.achievements.push(action.payload);
    },
    updateInterests: (state, action: PayloadAction<Interest[]>) => {
      state.interests = action.payload;
    },
    clearUserData: (state) => {
      state.profile = null;
      state.skillProgress = null;
      state.goals = [];
      state.achievements = [];
      state.interests = [];
      state.isLoading = false;
      state.error = null;
    },
  },
});

export const {
  fetchUserStart,
  fetchUserSuccess,
  fetchUserFailure,
  updateUserProfile,
  updateSkillProgress,
  addGoal,
  updateGoal,
  addAchievement,
  updateInterests,
  clearUserData,
} = userSlice.actions;

export default userSlice.reducer;
