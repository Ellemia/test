-- Mood-Food canonical schema.
-- No user accounts: per spec §25/§32.5 (no forced signup), identity is an
-- anonymous client-generated session id (uuid, stored client-side).

CREATE TABLE sessions (
  id UUID PRIMARY KEY,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  last_seen_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE ingredients (
  id BIGSERIAL PRIMARY KEY,
  canonical_name VARCHAR(100) NOT NULL UNIQUE,
  display_name VARCHAR(100) NOT NULL,
  category VARCHAR(50),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE ingredient_aliases (
  id BIGSERIAL PRIMARY KEY,
  ingredient_id BIGINT NOT NULL REFERENCES ingredients(id) ON DELETE CASCADE,
  alias VARCHAR(100) NOT NULL UNIQUE
);

-- Raw import staging (spec §7) — external payloads land here first, never
-- directly into `recipes`, so a bad/duplicate import never corrupts the
-- published data and every fetch attempt (success or failure) is traceable.
CREATE TABLE recipe_imports (
  id BIGSERIAL PRIMARY KEY,
  source_type VARCHAR(30) NOT NULL,          -- PUBLIC_API | OPEN_DATASET | MANUAL | OWN_RECIPE
  source_name VARCHAR(100) NOT NULL,
  source_url VARCHAR(500),
  external_recipe_id VARCHAR(100),
  license VARCHAR(100),
  attribution_required BOOLEAN NOT NULL DEFAULT false,
  raw_payload JSONB NOT NULL,
  status VARCHAR(20) NOT NULL DEFAULT 'PENDING', -- PENDING/NORMALIZED/DUPLICATE/REVIEW/APPROVED/REJECTED/ERROR
  error_message TEXT,
  recipe_id BIGINT,
  retrieved_at TIMESTAMPTZ NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_recipe_imports_status ON recipe_imports(status);

CREATE TABLE recipes (
  id BIGSERIAL PRIMARY KEY,
  title VARCHAR(200) NOT NULL,
  description TEXT,
  servings INT,
  prep_time_min INT,
  cook_time_min INT,
  total_time_min INT,
  difficulty VARCHAR(20),
  category VARCHAR(30),
  cuisine VARCHAR(30),
  image_url VARCHAR(500),
  -- provenance (spec §2.2 — every recipe must carry its own, never optional)
  source_type VARCHAR(30) NOT NULL,
  source_name VARCHAR(100) NOT NULL,
  source_url VARCHAR(500),
  source_recipe_id VARCHAR(100),
  license VARCHAR(100) NOT NULL,
  attribution_required BOOLEAN NOT NULL DEFAULT false,
  retrieved_at TIMESTAMPTZ NOT NULL,
  -- quality gate (spec §21) — only PUBLISHED recipes are eligible for recommendation
  quality_status VARCHAR(20) NOT NULL DEFAULT 'RAW',
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_recipes_quality_status ON recipes(quality_status);
CREATE INDEX idx_recipes_cuisine ON recipes(cuisine);

CREATE TABLE recipe_ingredients (
  id BIGSERIAL PRIMARY KEY,
  recipe_id BIGINT NOT NULL REFERENCES recipes(id) ON DELETE CASCADE,
  ingredient_id BIGINT REFERENCES ingredients(id),
  raw_text VARCHAR(200) NOT NULL,
  quantity NUMERIC(10,2),
  unit VARCHAR(20),
  position INT NOT NULL DEFAULT 0
);
CREATE INDEX idx_recipe_ingredients_recipe ON recipe_ingredients(recipe_id);
CREATE INDEX idx_recipe_ingredients_ingredient ON recipe_ingredients(ingredient_id);

CREATE TABLE recipe_steps (
  id BIGSERIAL PRIMARY KEY,
  recipe_id BIGINT NOT NULL REFERENCES recipes(id) ON DELETE CASCADE,
  step_number INT NOT NULL,
  instruction TEXT NOT NULL,
  UNIQUE(recipe_id, step_number)
);

CREATE TABLE recipe_nutrition (
  recipe_id BIGINT PRIMARY KEY REFERENCES recipes(id) ON DELETE CASCADE,
  calories NUMERIC(8,2),
  protein_g NUMERIC(8,2),
  fat_g NUMERIC(8,2),
  carbs_g NUMERIC(8,2),
  sodium_mg NUMERIC(8,2),
  source VARCHAR(100)
);

-- Service-internal preference/association scores (spec §11: "감정과 음식의
-- 관계는 객관적 과학적 사실이 아니라 서비스용 선호 점수로 취급").
CREATE TABLE recipe_scores (
  recipe_id BIGINT PRIMARY KEY REFERENCES recipes(id) ON DELETE CASCADE,
  taste_spicy INT NOT NULL DEFAULT 0 CHECK (taste_spicy BETWEEN 0 AND 100),
  taste_sweet INT NOT NULL DEFAULT 0 CHECK (taste_sweet BETWEEN 0 AND 100),
  taste_salty INT NOT NULL DEFAULT 0 CHECK (taste_salty BETWEEN 0 AND 100),
  taste_sour INT NOT NULL DEFAULT 0 CHECK (taste_sour BETWEEN 0 AND 100),
  taste_savory INT NOT NULL DEFAULT 0 CHECK (taste_savory BETWEEN 0 AND 100),
  taste_bitter INT NOT NULL DEFAULT 0 CHECK (taste_bitter BETWEEN 0 AND 100),
  taste_rich INT NOT NULL DEFAULT 0 CHECK (taste_rich BETWEEN 0 AND 100),
  taste_light INT NOT NULL DEFAULT 0 CHECK (taste_light BETWEEN 0 AND 100),
  mood_comfort INT NOT NULL DEFAULT 0 CHECK (mood_comfort BETWEEN 0 AND 100),
  mood_stress_relief INT NOT NULL DEFAULT 0 CHECK (mood_stress_relief BETWEEN 0 AND 100),
  mood_happiness INT NOT NULL DEFAULT 0 CHECK (mood_happiness BETWEEN 0 AND 100),
  mood_energy INT NOT NULL DEFAULT 0 CHECK (mood_energy BETWEEN 0 AND 100),
  mood_calm INT NOT NULL DEFAULT 0 CHECK (mood_calm BETWEEN 0 AND 100),
  mood_nostalgia INT NOT NULL DEFAULT 0 CHECK (mood_nostalgia BETWEEN 0 AND 100),
  mood_refresh INT NOT NULL DEFAULT 0 CHECK (mood_refresh BETWEEN 0 AND 100),
  mood_reward INT NOT NULL DEFAULT 0 CHECK (mood_reward BETWEEN 0 AND 100),
  texture_crispy INT NOT NULL DEFAULT 0 CHECK (texture_crispy BETWEEN 0 AND 100),
  texture_soft INT NOT NULL DEFAULT 0 CHECK (texture_soft BETWEEN 0 AND 100),
  texture_chewy INT NOT NULL DEFAULT 0 CHECK (texture_chewy BETWEEN 0 AND 100),
  texture_juicy INT NOT NULL DEFAULT 0 CHECK (texture_juicy BETWEEN 0 AND 100),
  texture_warm INT NOT NULL DEFAULT 0 CHECK (texture_warm BETWEEN 0 AND 100),
  texture_cool INT NOT NULL DEFAULT 0 CHECK (texture_cool BETWEEN 0 AND 100),
  texture_hearty INT NOT NULL DEFAULT 0 CHECK (texture_hearty BETWEEN 0 AND 100),
  texture_light INT NOT NULL DEFAULT 0 CHECK (texture_light BETWEEN 0 AND 100),
  tagging_confidence NUMERIC(4,3),
  tagged_by VARCHAR(20) NOT NULL DEFAULT 'RULE'  -- RULE | LLM | ADMIN
);

CREATE TABLE recipe_situations (
  id BIGSERIAL PRIMARY KEY,
  recipe_id BIGINT NOT NULL REFERENCES recipes(id) ON DELETE CASCADE,
  situation VARCHAR(30) NOT NULL,
  UNIQUE(recipe_id, situation)
);
CREATE INDEX idx_recipe_situations_situation ON recipe_situations(situation);

CREATE TABLE recipe_tags (
  id BIGSERIAL PRIMARY KEY,
  recipe_id BIGINT NOT NULL REFERENCES recipes(id) ON DELETE CASCADE,
  tag VARCHAR(50) NOT NULL,
  UNIQUE(recipe_id, tag)
);

CREATE TABLE favorites (
  id BIGSERIAL PRIMARY KEY,
  session_id UUID NOT NULL REFERENCES sessions(id) ON DELETE CASCADE,
  recipe_id BIGINT NOT NULL REFERENCES recipes(id) ON DELETE CASCADE,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  UNIQUE(session_id, recipe_id)
);

CREATE TABLE recommendation_history (
  id BIGSERIAL PRIMARY KEY,
  session_id UUID NOT NULL REFERENCES sessions(id) ON DELETE CASCADE,
  recipe_id BIGINT NOT NULL REFERENCES recipes(id),
  algorithm_version VARCHAR(10) NOT NULL,
  score NUMERIC(6,2) NOT NULL,
  context JSONB NOT NULL,
  shown_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_recommendation_history_session ON recommendation_history(session_id, shown_at DESC);

CREATE TABLE recommendation_feedback (
  id BIGSERIAL PRIMARY KEY,
  recommendation_history_id BIGINT REFERENCES recommendation_history(id) ON DELETE SET NULL,
  session_id UUID NOT NULL REFERENCES sessions(id) ON DELETE CASCADE,
  recipe_id BIGINT NOT NULL REFERENCES recipes(id),
  feedback VARCHAR(20) NOT NULL,  -- LIKE | DISLIKE | COOKED | SKIPPED
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_recommendation_feedback_session ON recommendation_feedback(session_id);
CREATE INDEX idx_recommendation_feedback_recipe ON recommendation_feedback(recipe_id);

CREATE TABLE user_preferences (
  session_id UUID PRIMARY KEY REFERENCES sessions(id) ON DELETE CASCADE,
  preference JSONB NOT NULL DEFAULT '{}'::jsonb,
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE user_ingredients (
  id BIGSERIAL PRIMARY KEY,
  session_id UUID NOT NULL REFERENCES sessions(id) ON DELETE CASCADE,
  ingredient_id BIGINT NOT NULL REFERENCES ingredients(id),
  UNIQUE(session_id, ingredient_id)
);
