export type Mood = 'GOOD' | 'NEUTRAL' | 'TIRED' | 'STRESSED' | 'SAD' | 'EXCITED' | 'DRAINED';

export type DesiredTaste =
  | 'SPICY'
  | 'WARM'
  | 'HEARTY'
  | 'FAMILIAR'
  | 'LIGHT'
  | 'SWEET'
  | 'SPECIAL'
  | 'SURPRISE_ME';

export type Situation =
  | 'SOLO'
  | 'COUPLE'
  | 'FAMILY'
  | 'FRIENDS'
  | 'PARTY'
  | 'BREAKFAST'
  | 'LUNCH'
  | 'DINNER'
  | 'LATE_NIGHT'
  | 'QUICK_MEAL'
  | 'WEEKEND_COOKING';

export type CookingWillingness = 'UNDER_10_MIN' | 'SIMPLE' | 'PROPER_COOKING' | 'TAKEOUT_OK';

export type FeedbackType = 'LIKE' | 'DISLIKE' | 'COOKED' | 'SKIPPED';

export interface RecommendRequest {
  mood: Mood;
  desiredTaste: DesiredTaste;
  situation?: Situation | null;
  cookingWillingness?: CookingWillingness | null;
  timeAvailableMin?: number | null;
  budgetWon?: number | null;
}

export interface ScoreBreakdown {
  moodFit: number;
  tasteFit: number;
  situationFit: number;
  cookingFit: number;
  timeFit: number;
  budgetFit: number;
  novelty: number;
}

export interface RecommendResponse {
  recommendationId: number;
  recipeId: number;
  title: string;
  imageUrl: string | null;
  reason: string;
  servings: number | null;
  totalTimeMin: number | null;
  difficulty: string | null;
  score: number;
  breakdown: ScoreBreakdown;
}

export interface IngredientDto {
  rawText: string;
  canonicalName: string | null;
}

export interface NutritionDto {
  calories: number | null;
  proteinG: number | null;
  fatG: number | null;
  carbsG: number | null;
  sodiumMg: number | null;
  source: string | null;
}

export interface ProvenanceDto {
  sourceType: string;
  sourceName: string;
  sourceUrl: string | null;
  license: string;
  attributionRequired: boolean;
}

export interface RecipeDetail {
  id: number;
  title: string;
  description: string | null;
  servings: number | null;
  prepTimeMin: number | null;
  cookTimeMin: number | null;
  totalTimeMin: number | null;
  difficulty: string | null;
  category: string | null;
  cuisine: string | null;
  imageUrl: string | null;
  ingredients: IngredientDto[];
  steps: string[];
  nutrition: NutritionDto | null;
  tags: string[];
  provenance: ProvenanceDto;
}
