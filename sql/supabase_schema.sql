-- ==============================================================================
-- PERSONAL GAIN TRACKING SYSTEM (PGTR)
-- Supabase PostgreSQL Database Schema
-- B.Tech CSE OOP Project
-- ==============================================================================

-- 1. EXTENSIONS
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- 2. USERS TABLE
CREATE TABLE IF NOT EXISTS users (
    id VARCHAR(64) PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(120) NOT NULL,
    age INT CHECK (age >= 10 AND age <= 120),
    height_cm NUMERIC(5,2) CHECK (height_cm >= 50 AND height_cm <= 260),
    current_weight_kg NUMERIC(5,2) CHECK (current_weight_kg >= 20 AND current_weight_kg <= 350),
    target_weight_kg NUMERIC(5,2) CHECK (target_weight_kg >= 20 AND target_weight_kg <= 350),
    fitness_goal VARCHAR(50) DEFAULT 'MUSCLE_GAIN',
    activity_level VARCHAR(50) DEFAULT 'MODERATELY_ACTIVE',
    daily_calorie_target INT DEFAULT 2400,
    daily_protein_target INT DEFAULT 150,
    daily_water_target INT DEFAULT 3000,
    daily_sleep_target NUMERIC(3,1) DEFAULT 8.0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);

-- 3. WORKOUTS TABLE
CREATE TABLE IF NOT EXISTS workouts (
    id VARCHAR(64) PRIMARY KEY,
    user_id VARCHAR(64) NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    workout_date DATE NOT NULL,
    workout_name VARCHAR(150) NOT NULL,
    muscle_group VARCHAR(100) NOT NULL,
    duration_minutes INT DEFAULT 45 CHECK (duration_minutes >= 0),
    calories_burned INT DEFAULT 0 CHECK (calories_burned >= 0),
    notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_workouts_user_date ON workouts(user_id, workout_date DESC);
CREATE INDEX IF NOT EXISTS idx_workouts_muscle ON workouts(user_id, muscle_group);

-- 4. WORKOUT EXERCISES TABLE
CREATE TABLE IF NOT EXISTS workout_exercises (
    id VARCHAR(64) PRIMARY KEY,
    workout_id VARCHAR(64) NOT NULL REFERENCES workouts(id) ON DELETE CASCADE,
    user_id VARCHAR(64) NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    exercise_name VARCHAR(150) NOT NULL,
    exercise_type VARCHAR(50) DEFAULT 'Strength',
    sets INT NOT NULL DEFAULT 1 CHECK (sets >= 1),
    reps INT NOT NULL DEFAULT 1 CHECK (reps >= 1),
    weight_kg NUMERIC(6,2) NOT NULL DEFAULT 0.0 CHECK (weight_kg >= 0),
    volume_kg NUMERIC(10,2) NOT NULL DEFAULT 0.0,
    notes TEXT
);

CREATE INDEX IF NOT EXISTS idx_exercises_workout ON workout_exercises(workout_id);
CREATE INDEX IF NOT EXISTS idx_exercises_user_name ON workout_exercises(user_id, exercise_name);

-- 5. FOOD ITEMS (LIBRARY) TABLE
CREATE TABLE IF NOT EXISTS food_items (
    id VARCHAR(64) PRIMARY KEY,
    food_name VARCHAR(150) NOT NULL,
    category VARCHAR(80) DEFAULT 'General',
    serving_unit VARCHAR(50) DEFAULT '100g',
    calories NUMERIC(7,2) NOT NULL DEFAULT 0 CHECK (calories >= 0),
    protein_g NUMERIC(6,2) NOT NULL DEFAULT 0 CHECK (protein_g >= 0),
    carbs_g NUMERIC(6,2) NOT NULL DEFAULT 0 CHECK (carbs_g >= 0),
    fat_g NUMERIC(6,2) NOT NULL DEFAULT 0 CHECK (fat_g >= 0),
    is_custom BOOLEAN DEFAULT FALSE,
    user_id VARCHAR(64) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_food_items_name ON food_items(food_name);
CREATE INDEX IF NOT EXISTS idx_food_items_user ON food_items(user_id);

-- 6. MEALS (CONSUMPTION LOG) TABLE
CREATE TABLE IF NOT EXISTS meals (
    id VARCHAR(64) PRIMARY KEY,
    user_id VARCHAR(64) NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    meal_date DATE NOT NULL,
    meal_time TIME DEFAULT CURRENT_TIME,
    meal_type VARCHAR(50) NOT NULL, -- 'BREAKFAST', 'LUNCH', 'DINNER', 'SNACK'
    food_name VARCHAR(150) NOT NULL,
    serving_quantity NUMERIC(6,2) NOT NULL DEFAULT 1.0 CHECK (serving_quantity > 0),
    calories NUMERIC(7,2) NOT NULL DEFAULT 0 CHECK (calories >= 0),
    protein_g NUMERIC(6,2) NOT NULL DEFAULT 0 CHECK (protein_g >= 0),
    carbs_g NUMERIC(6,2) NOT NULL DEFAULT 0 CHECK (carbs_g >= 0),
    fat_g NUMERIC(6,2) NOT NULL DEFAULT 0 CHECK (fat_g >= 0),
    notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_meals_user_date ON meals(user_id, meal_date DESC);
CREATE INDEX IF NOT EXISTS idx_meals_type ON meals(user_id, meal_type);

-- 7. BODY MEASUREMENTS TABLE
CREATE TABLE IF NOT EXISTS body_measurements (
    id VARCHAR(64) PRIMARY KEY,
    user_id VARCHAR(64) NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    log_date DATE NOT NULL,
    weight_kg NUMERIC(5,2) NOT NULL CHECK (weight_kg > 0),
    height_cm NUMERIC(5,2) NOT NULL CHECK (height_cm > 0),
    bmi NUMERIC(4,1) NOT NULL,
    waist_cm NUMERIC(5,2) DEFAULT 0,
    chest_cm NUMERIC(5,2) DEFAULT 0,
    arms_cm NUMERIC(5,2) DEFAULT 0,
    thighs_cm NUMERIC(5,2) DEFAULT 0,
    hips_cm NUMERIC(5,2) DEFAULT 0,
    neck_cm NUMERIC(5,2) DEFAULT 0,
    notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_measurements_user_date ON body_measurements(user_id, log_date DESC);

-- 8. WATER LOGS TABLE
CREATE TABLE IF NOT EXISTS water_logs (
    id VARCHAR(64) PRIMARY KEY,
    user_id VARCHAR(64) NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    log_date DATE NOT NULL,
    amount_ml INT NOT NULL CHECK (amount_ml > 0),
    logged_at TIME DEFAULT CURRENT_TIME
);

CREATE INDEX IF NOT EXISTS idx_water_user_date ON water_logs(user_id, log_date DESC);

-- 9. SLEEP LOGS TABLE
CREATE TABLE IF NOT EXISTS sleep_logs (
    id VARCHAR(64) PRIMARY KEY,
    user_id VARCHAR(64) NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    sleep_date DATE NOT NULL,
    sleep_time TIME,
    wake_time TIME,
    duration_hours NUMERIC(4,2) NOT NULL CHECK (duration_hours >= 0 AND duration_hours <= 24),
    sleep_quality VARCHAR(30) DEFAULT 'Good',
    notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_sleep_user_date ON sleep_logs(user_id, sleep_date DESC);

-- 10. FITNESS GOALS TABLE
CREATE TABLE IF NOT EXISTS fitness_goals (
    id VARCHAR(64) PRIMARY KEY,
    user_id VARCHAR(64) NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    title VARCHAR(150) NOT NULL,
    description TEXT,
    category VARCHAR(50) NOT NULL, -- 'WEIGHT', 'STRENGTH', 'MUSCLE', 'NUTRITION', 'HABIT'
    starting_value NUMERIC(8,2) NOT NULL,
    target_value NUMERIC(8,2) NOT NULL,
    current_value NUMERIC(8,2) NOT NULL,
    unit VARCHAR(30) NOT NULL,
    start_date DATE NOT NULL,
    deadline DATE NOT NULL,
    status VARCHAR(30) DEFAULT 'IN_PROGRESS', -- 'IN_PROGRESS', 'ACHIEVED', 'ABANDONED'
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_goals_user ON fitness_goals(user_id, status);

-- 11. WORKOUT PLANS TABLE (CUT & BULK MODES)
CREATE TABLE IF NOT EXISTS workout_plans (
    id VARCHAR(64) PRIMARY KEY,
    user_id VARCHAR(64) NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    plan_mode VARCHAR(20) DEFAULT 'BULK', -- 'CUT' or 'BULK'
    day_of_week VARCHAR(20) NOT NULL, -- 'MONDAY', 'TUESDAY', ...
    workout_name VARCHAR(150) NOT NULL,
    muscle_group VARCHAR(100) NOT NULL,
    planned_exercises TEXT,
    rest_seconds INT DEFAULT 90,
    cardio VARCHAR(150),
    scheduled_time TIME,
    notes TEXT,
    is_active BOOLEAN DEFAULT TRUE,
    is_completed INT DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_plans_user_mode ON workout_plans(user_id, plan_mode, day_of_week);

-- 12. NUTRITION PLANS TABLE (CUT & BULK MODES)
CREATE TABLE IF NOT EXISTS nutrition_plans (
    id VARCHAR(64) PRIMARY KEY,
    user_id VARCHAR(64) NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    plan_mode VARCHAR(20) NOT NULL DEFAULT 'BULK', -- 'CUT' or 'BULK'
    meal_type VARCHAR(50) NOT NULL, -- 'BREAKFAST', 'MID_MORNING', 'LUNCH', 'EVENING_SNACK', 'DINNER', 'POST_WORKOUT'
    food_name VARCHAR(150) NOT NULL,
    serving_size VARCHAR(100),
    calories NUMERIC(7,2) DEFAULT 0,
    protein_g NUMERIC(6,2) DEFAULT 0,
    carbs_g NUMERIC(6,2) DEFAULT 0,
    fat_g NUMERIC(6,2) DEFAULT 0,
    meal_time VARCHAR(20),
    notes TEXT,
    is_consumed INT DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_nutrition_plans_user_mode ON nutrition_plans(user_id, plan_mode);

-- 12. REMINDERS TABLE
CREATE TABLE IF NOT EXISTS reminders (
    id VARCHAR(64) PRIMARY KEY,
    user_id VARCHAR(64) NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    reminder_type VARCHAR(50) NOT NULL, -- 'Workout', 'Hydration', 'Meal', 'Sleep'
    title VARCHAR(150) NOT NULL,
    message TEXT,
    reminder_time TIME NOT NULL,
    is_recurring BOOLEAN DEFAULT TRUE,
    is_dismissed BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_reminders_user ON reminders(user_id, is_dismissed);

-- 13. ACHIEVEMENTS CATALOG TABLE
CREATE TABLE IF NOT EXISTS achievements (
    code VARCHAR(50) PRIMARY KEY,
    title VARCHAR(100) NOT NULL,
    description TEXT NOT NULL,
    icon_name VARCHAR(50) NOT NULL,
    badge_tier VARCHAR(30) DEFAULT 'BRONZE'
);

-- 14. USER ACHIEVEMENTS TABLE
CREATE TABLE IF NOT EXISTS user_achievements (
    id VARCHAR(64) PRIMARY KEY,
    user_id VARCHAR(64) NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    achievement_code VARCHAR(50) NOT NULL REFERENCES achievements(code) ON DELETE CASCADE,
    earned_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_user_achievement UNIQUE (user_id, achievement_code)
);

CREATE INDEX IF NOT EXISTS idx_user_achievements ON user_achievements(user_id);
