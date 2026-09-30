import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { addFavorite, getRecipe, listFavorites, removeFavorite } from '../api/client';
import type { RecipeDetail } from '../api/types';
import { DIFFICULTY_LABELS } from '../labels';

export default function RecipeDetailPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const recipeId = Number(id);

  const [recipe, setRecipe] = useState<RecipeDetail | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [isFavorite, setIsFavorite] = useState(false);

  useEffect(() => {
    if (!Number.isFinite(recipeId)) return;
    getRecipe(recipeId)
      .then(setRecipe)
      .catch((e) => setError(e instanceof Error ? e.message : '레시피를 불러오지 못했어요.'));
    listFavorites()
      .then((ids) => setIsFavorite(ids.includes(recipeId)))
      .catch(() => {});
  }, [recipeId]);

  async function toggleFavorite() {
    try {
      if (isFavorite) {
        await removeFavorite(recipeId);
        setIsFavorite(false);
      } else {
        await addFavorite(recipeId);
        setIsFavorite(true);
      }
    } catch {
      // best-effort
    }
  }

  if (error) {
    return (
      <div className="flex flex-col gap-4">
        <BackButton onClick={() => navigate(-1)} />
        <div className="rounded-xl bg-white p-6 text-center text-stone-600">{error}</div>
      </div>
    );
  }

  if (!recipe) {
    return (
      <div className="flex flex-col gap-4">
        <BackButton onClick={() => navigate(-1)} />
        <div className="rounded-xl bg-white p-8 text-center text-stone-500">불러오는 중…</div>
      </div>
    );
  }

  return (
    <div className="flex flex-col gap-6">
      <BackButton onClick={() => navigate(-1)} />

      <div className="rounded-2xl bg-white p-5 shadow-sm">
        <div className="flex items-start justify-between gap-3">
          <h1 className="text-xl font-bold text-orange-900">{recipe.title}</h1>
          <button
            type="button"
            onClick={toggleFavorite}
            aria-label="즐겨찾기"
            className="text-2xl"
          >
            {isFavorite ? '❤️' : '🤍'}
          </button>
        </div>
        {recipe.description && (
          <p className="mt-2 text-sm leading-relaxed text-stone-600">{recipe.description}</p>
        )}
        <div className="mt-4 flex flex-wrap gap-3 text-xs text-stone-500">
          {recipe.totalTimeMin != null && <span>⏱ {recipe.totalTimeMin}분</span>}
          {recipe.servings != null && <span>🍽 {recipe.servings}인분</span>}
          {recipe.difficulty && (
            <span>
              난이도{' '}
              {DIFFICULTY_LABELS[recipe.difficulty as keyof typeof DIFFICULTY_LABELS] ??
                recipe.difficulty}
            </span>
          )}
        </div>
        {recipe.tags.length > 0 && (
          <div className="mt-3 flex flex-wrap gap-1.5">
            {recipe.tags.map((tag) => (
              <span key={tag} className="rounded-full bg-orange-100 px-2.5 py-1 text-xs text-orange-800">
                #{tag}
              </span>
            ))}
          </div>
        )}
      </div>

      <section className="rounded-2xl bg-white p-5 shadow-sm">
        <h2 className="mb-3 text-sm font-semibold text-stone-700">재료</h2>
        <ul className="flex flex-col gap-1.5 text-sm text-stone-600">
          {recipe.ingredients.map((ing, i) => (
            <li key={i}>• {ing.rawText}</li>
          ))}
        </ul>
      </section>

      <section className="rounded-2xl bg-white p-5 shadow-sm">
        <h2 className="mb-3 text-sm font-semibold text-stone-700">조리 순서</h2>
        <ol className="flex flex-col gap-3 text-sm text-stone-600">
          {recipe.steps.map((step, i) => (
            <li key={i} className="flex gap-2">
              <span className="font-semibold text-orange-500">{i + 1}</span>
              <span>{step}</span>
            </li>
          ))}
        </ol>
      </section>

      {recipe.nutrition && (
        <section className="rounded-2xl bg-white p-5 shadow-sm">
          <h2 className="mb-3 text-sm font-semibold text-stone-700">영양 정보</h2>
          <div className="grid grid-cols-2 gap-2 text-sm text-stone-600">
            {recipe.nutrition.calories != null && <span>칼로리 {recipe.nutrition.calories}kcal</span>}
            {recipe.nutrition.proteinG != null && <span>단백질 {recipe.nutrition.proteinG}g</span>}
            {recipe.nutrition.fatG != null && <span>지방 {recipe.nutrition.fatG}g</span>}
            {recipe.nutrition.carbsG != null && <span>탄수화물 {recipe.nutrition.carbsG}g</span>}
          </div>
          {recipe.nutrition.source && (
            <p className="mt-2 text-xs text-stone-400">출처: {recipe.nutrition.source}</p>
          )}
        </section>
      )}

      <section className="rounded-xl border border-stone-200 bg-white/60 p-4 text-xs text-stone-400">
        <p>
          출처: {recipe.provenance.sourceName}
          {recipe.provenance.sourceUrl ? ` (${recipe.provenance.sourceUrl})` : ''}
        </p>
        <p className="mt-1">라이선스: {recipe.provenance.license}</p>
      </section>
    </div>
  );
}

function BackButton({ onClick }: { onClick: () => void }) {
  return (
    <button type="button" onClick={onClick} className="self-start text-sm text-stone-500">
      ← 뒤로
    </button>
  );
}
