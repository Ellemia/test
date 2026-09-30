import { useCallback, useEffect, useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { recommend, sendFeedback } from '../api/client';
import type { FeedbackType, RecommendRequest, RecommendResponse } from '../api/types';
import { DIFFICULTY_LABELS } from '../labels';

export default function ResultPage() {
  const location = useLocation();
  const navigate = useNavigate();
  const context = location.state as RecommendRequest | null;

  const [result, setResult] = useState<RecommendResponse | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [feedbackSent, setFeedbackSent] = useState<FeedbackType | null>(null);

  const fetchRecommendation = useCallback(async () => {
    if (!context) return;
    setLoading(true);
    setError(null);
    setFeedbackSent(null);
    try {
      const res = await recommend(context);
      setResult(res);
    } catch (e) {
      setError(e instanceof Error ? e.message : '추천을 받아오지 못했어요.');
      setResult(null);
    } finally {
      setLoading(false);
    }
  }, [context]);

  useEffect(() => {
    if (!context) {
      navigate('/', { replace: true });
      return;
    }
    fetchRecommendation();
  }, [context, fetchRecommendation, navigate]);

  async function giveFeedback(feedback: FeedbackType) {
    if (!result) return;
    setFeedbackSent(feedback);
    try {
      await sendFeedback(result.recommendationId, result.recipeId, feedback);
    } catch {
      // best-effort — UI already reflects the choice
    }
  }

  if (!context) return null;

  return (
    <div className="flex flex-col gap-6">
      <button
        type="button"
        onClick={() => navigate('/')}
        className="self-start text-sm text-stone-500"
      >
        ← 다시 선택하기
      </button>

      {loading && (
        <div className="rounded-xl bg-white p-8 text-center text-stone-500">추천을 찾고 있어요…</div>
      )}

      {error && (
        <div className="rounded-xl border border-orange-200 bg-white p-6 text-center">
          <p className="text-stone-600">{error}</p>
        </div>
      )}

      {!loading && !error && result && (
        <div className="flex flex-col gap-4">
          <div className="rounded-2xl bg-white p-5 shadow-sm">
            <p className="text-xs font-medium text-orange-500">오늘의 추천</p>
            <h1 className="mt-1 text-xl font-bold text-orange-900">{result.title}</h1>
            <p className="mt-2 text-sm leading-relaxed text-stone-600">{result.reason}</p>
            <div className="mt-4 flex flex-wrap gap-3 text-xs text-stone-500">
              {result.totalTimeMin != null && <span>⏱ {result.totalTimeMin}분</span>}
              {result.servings != null && <span>🍽 {result.servings}인분</span>}
              {result.difficulty && (
                <span>
                  난이도{' '}
                  {DIFFICULTY_LABELS[result.difficulty as keyof typeof DIFFICULTY_LABELS] ??
                    result.difficulty}
                </span>
              )}
            </div>
          </div>

          <button
            type="button"
            onClick={() => navigate(`/recipes/${result.recipeId}`)}
            className="rounded-xl bg-orange-500 py-3 text-center text-sm font-semibold text-white"
          >
            레시피 자세히 보기
          </button>

          <div className="grid grid-cols-3 gap-2">
            <FeedbackButton
              active={feedbackSent === 'LIKE'}
              label="👍 좋아요"
              onClick={() => giveFeedback('LIKE')}
            />
            <FeedbackButton
              active={feedbackSent === 'DISLIKE'}
              label="👎 별로예요"
              onClick={() => giveFeedback('DISLIKE')}
            />
            <FeedbackButton
              active={feedbackSent === 'COOKED'}
              label="🍳 만들어볼래요"
              onClick={() => giveFeedback('COOKED')}
            />
          </div>

          <button
            type="button"
            onClick={() => {
              giveFeedback('SKIPPED');
              fetchRecommendation();
            }}
            className="rounded-xl border border-stone-300 bg-white py-3 text-center text-sm font-medium text-stone-600"
          >
            🔁 다른 메뉴 보여주세요
          </button>
        </div>
      )}
    </div>
  );
}

function FeedbackButton({
  label,
  active,
  onClick,
}: {
  label: string;
  active: boolean;
  onClick: () => void;
}) {
  return (
    <button
      type="button"
      onClick={onClick}
      className={`rounded-xl border py-2 text-xs font-medium transition ${
        active ? 'border-orange-500 bg-orange-100 text-orange-800' : 'border-stone-200 bg-white text-stone-600'
      }`}
    >
      {label}
    </button>
  );
}
