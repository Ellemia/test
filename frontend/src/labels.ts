import type { CookingWillingness, DesiredTaste, Mood, Situation } from './api/types';

type Difficulty = 'EASY' | 'MEDIUM' | 'HARD';

export const MOOD_LABELS: Record<Mood, { emoji: string; label: string }> = {
  GOOD: { emoji: '😊', label: '기분 좋음' },
  NEUTRAL: { emoji: '😐', label: '그냥 그럼' },
  TIRED: { emoji: '😴', label: '피곤함' },
  STRESSED: { emoji: '😤', label: '스트레스 받음' },
  SAD: { emoji: '😢', label: '우울함' },
  EXCITED: { emoji: '🤩', label: '신남' },
  DRAINED: { emoji: '🫠', label: '기운 없음' },
};

export const TASTE_LABELS: Record<DesiredTaste, { emoji: string; label: string }> = {
  SPICY: { emoji: '🌶️', label: '매콤한 것' },
  WARM: { emoji: '🍲', label: '따뜻한 것' },
  HEARTY: { emoji: '🍖', label: '든든한 것' },
  FAMILIAR: { emoji: '🍚', label: '익숙한 것' },
  LIGHT: { emoji: '🥗', label: '가벼운 것' },
  SWEET: { emoji: '🍰', label: '달콤한 것' },
  SPECIAL: { emoji: '✨', label: '특별한 것' },
  SURPRISE_ME: { emoji: '🎲', label: '아무거나 놀라운 것' },
};

export const SITUATION_LABELS: Record<Situation, string> = {
  SOLO: '혼자',
  COUPLE: '둘이',
  FAMILY: '가족과',
  FRIENDS: '친구와',
  PARTY: '파티',
  BREAKFAST: '아침',
  LUNCH: '점심',
  DINNER: '저녁',
  LATE_NIGHT: '야식',
  QUICK_MEAL: '빠르게',
  WEEKEND_COOKING: '주말 요리',
};

export const COOKING_LABELS: Record<CookingWillingness, string> = {
  UNDER_10_MIN: '10분 이내로',
  SIMPLE: '간단하게',
  PROPER_COOKING: '제대로 요리',
  TAKEOUT_OK: '배달/포장도 괜찮음',
};

export const DIFFICULTY_LABELS: Record<Difficulty, string> = {
  EASY: '쉬움',
  MEDIUM: '보통',
  HARD: '어려움',
};
