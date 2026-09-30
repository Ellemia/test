import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import type { CookingWillingness, DesiredTaste, Mood, Situation } from '../api/types';
import { COOKING_LABELS, MOOD_LABELS, SITUATION_LABELS, TASTE_LABELS } from '../labels';

const MOODS = Object.keys(MOOD_LABELS) as Mood[];
const TASTES = Object.keys(TASTE_LABELS) as DesiredTaste[];
const SITUATIONS = Object.keys(SITUATION_LABELS) as Situation[];
const COOKING = Object.keys(COOKING_LABELS) as CookingWillingness[];

export default function HomePage() {
  const navigate = useNavigate();
  const [mood, setMood] = useState<Mood | null>(null);
  const [taste, setTaste] = useState<DesiredTaste | null>(null);
  const [situation, setSituation] = useState<Situation | null>(null);
  const [cooking, setCooking] = useState<CookingWillingness | null>(null);

  const canSubmit = mood !== null && taste !== null;

  function submit() {
    if (!mood || !taste) return;
    navigate('/result', {
      state: { mood, desiredTaste: taste, situation, cookingWillingness: cooking },
    });
  }

  return (
    <div className="flex flex-col gap-8">
      <header className="pt-4 text-center">
        <h1 className="text-2xl font-bold text-orange-900">오늘 뭐 먹지?</h1>
        <p className="mt-1 text-sm text-stone-500">지금 기분에 맞는 메뉴를 찾아드려요</p>
      </header>

      <section>
        <h2 className="mb-3 text-sm font-semibold text-stone-600">지금 기분이 어떠세요?</h2>
        <div className="grid grid-cols-2 gap-2">
          {MOODS.map((m) => (
            <button
              key={m}
              type="button"
              onClick={() => setMood(m)}
              className={`flex items-center gap-2 rounded-xl border px-3 py-3 text-left text-sm transition ${
                mood === m
                  ? 'border-orange-500 bg-orange-100 font-semibold text-orange-900'
                  : 'border-stone-200 bg-white text-stone-700'
              }`}
            >
              <span className="text-lg">{MOOD_LABELS[m].emoji}</span>
              {MOOD_LABELS[m].label}
            </button>
          ))}
        </div>
      </section>

      <section>
        <h2 className="mb-3 text-sm font-semibold text-stone-600">어떤 맛이 생각나세요?</h2>
        <div className="grid grid-cols-2 gap-2">
          {TASTES.map((t) => (
            <button
              key={t}
              type="button"
              onClick={() => setTaste(t)}
              className={`flex items-center gap-2 rounded-xl border px-3 py-3 text-left text-sm transition ${
                taste === t
                  ? 'border-orange-500 bg-orange-100 font-semibold text-orange-900'
                  : 'border-stone-200 bg-white text-stone-700'
              }`}
            >
              <span className="text-lg">{TASTE_LABELS[t].emoji}</span>
              {TASTE_LABELS[t].label}
            </button>
          ))}
        </div>
      </section>

      <section>
        <h2 className="mb-3 text-sm font-semibold text-stone-600">
          누구와 함께인가요? <span className="text-stone-400">(선택)</span>
        </h2>
        <div className="flex flex-wrap gap-2">
          {SITUATIONS.map((s) => (
            <button
              key={s}
              type="button"
              onClick={() => setSituation(situation === s ? null : s)}
              className={`rounded-full border px-3 py-1.5 text-xs transition ${
                situation === s
                  ? 'border-orange-500 bg-orange-500 text-white'
                  : 'border-stone-300 bg-white text-stone-600'
              }`}
            >
              {SITUATION_LABELS[s]}
            </button>
          ))}
        </div>
      </section>

      <section>
        <h2 className="mb-3 text-sm font-semibold text-stone-600">
          요리는 어느 정도로 하고 싶으세요? <span className="text-stone-400">(선택)</span>
        </h2>
        <div className="flex flex-wrap gap-2">
          {COOKING.map((c) => (
            <button
              key={c}
              type="button"
              onClick={() => setCooking(cooking === c ? null : c)}
              className={`rounded-full border px-3 py-1.5 text-xs transition ${
                cooking === c
                  ? 'border-orange-500 bg-orange-500 text-white'
                  : 'border-stone-300 bg-white text-stone-600'
              }`}
            >
              {COOKING_LABELS[c]}
            </button>
          ))}
        </div>
      </section>

      <button
        type="button"
        disabled={!canSubmit}
        onClick={submit}
        className="rounded-xl bg-orange-500 py-4 text-center text-base font-semibold text-white transition disabled:cursor-not-allowed disabled:bg-stone-300"
      >
        메뉴 추천받기
      </button>
    </div>
  );
}
