-- ==============================================================================
-- PERSONAL GAIN TRACKING SYSTEM (PGTR)
-- Initial Seed Data: Food Items Library & Achievements Catalog
-- ==============================================================================

-- 1. ACHIEVEMENTS CATALOG
INSERT INTO achievements (code, title, description, icon_name, badge_tier) VALUES
('FIRST_WORKOUT', 'First Step', 'Recorded your very first gym workout!', 'dumbbell', 'BRONZE')
ON CONFLICT (code) DO NOTHING;

INSERT INTO achievements (code, title, description, icon_name, badge_tier) VALUES
('STREAK_7_DAYS', 'Iron Consistency', 'Maintained a 7-day workout streak!', 'flame', 'SILVER')
ON CONFLICT (code) DO NOTHING;

INSERT INTO achievements (code, title, description, icon_name, badge_tier) VALUES
('WORKOUTS_10', 'Gym Regular', 'Completed 10 total recorded workouts!', 'medal', 'SILVER')
ON CONFLICT (code) DO NOTHING;

INSERT INTO achievements (code, title, description, icon_name, badge_tier) VALUES
('WORKOUTS_50', 'Beast Mode', 'Crushed 50 gym workouts like a champion!', 'trophy', 'GOLD')
ON CONFLICT (code) DO NOTHING;

INSERT INTO achievements (code, title, description, icon_name, badge_tier) VALUES
('PROTEIN_TARGET', 'Macro Master', 'Hit or exceeded your daily protein goal!', 'egg', 'BRONZE')
ON CONFLICT (code) DO NOTHING;

INSERT INTO achievements (code, title, description, icon_name, badge_tier) VALUES
('NEW_PR', 'PR Smasher', 'Achieved a new personal record in weight lifted!', 'award', 'GOLD')
ON CONFLICT (code) DO NOTHING;

INSERT INTO achievements (code, title, description, icon_name, badge_tier) VALUES
('GOAL_COMPLETED', 'Goal Crusher', 'Successfully completed a personal fitness goal!', 'star', 'GOLD')
ON CONFLICT (code) DO NOTHING;

INSERT INTO achievements (code, title, description, icon_name, badge_tier) VALUES
('HYDRATION_HERO', 'Hydration Hero', 'Met your daily water intake target!', 'droplet', 'BRONZE')
ON CONFLICT (code) DO NOTHING;

INSERT INTO achievements (code, title, description, icon_name, badge_tier) VALUES
('SLEEP_CHAMPION', 'Sleep Champion', 'Logged 7+ hours of quality sleep for recovery!', 'moon', 'BRONZE')
ON CONFLICT (code) DO NOTHING;

-- 2. FOOD ITEMS CATALOG (Nutrition estimates per 100g or standard serving)
INSERT INTO food_items (id, food_name, category, serving_unit, calories, protein_g, carbs_g, fat_g, is_custom, user_id) VALUES
('food_egg', 'Whole Egg (Large, Boiled)', 'Proteins', '1 egg (50g)', 78.0, 6.3, 0.6, 5.3, FALSE, NULL),
('food_egg_white', 'Egg White (Boiled)', 'Proteins', '1 white (33g)', 17.0, 3.6, 0.2, 0.1, FALSE, NULL),
('food_chicken_breast', 'Chicken Breast (Cooked, Skinless)', 'Proteins', '100g', 165.0, 31.0, 0.0, 3.6, FALSE, NULL),
('food_paneer', 'Paneer (Cottage Cheese)', 'Dairy & Protein', '100g', 265.0, 18.3, 1.2, 20.8, FALSE, NULL),
('food_whey', 'Whey Protein Powder', 'Supplements', '1 scoop (30g)', 120.0, 24.0, 2.0, 1.5, FALSE, NULL),
('food_salmon', 'Salmon Fillet (Baked)', 'Fish & Seafood', '100g', 208.0, 20.4, 0.0, 13.4, FALSE, NULL),
('food_tuna', 'Canned Tuna in Water', 'Fish & Seafood', '100g', 116.0, 25.5, 0.0, 0.8, FALSE, NULL),
('food_greek_yogurt', 'Greek Yogurt (Plain 0% Fat)', 'Dairy & Protein', '100g', 59.0, 10.0, 3.6, 0.4, FALSE, NULL),
('food_cow_milk', 'Whole Cow Milk', 'Dairy & Protein', '1 cup (240ml)', 149.0, 7.7, 11.7, 7.9, FALSE, NULL),
('food_tofu', 'Tofu (Firm)', 'Plant Protein', '100g', 144.0, 17.3, 2.8, 8.7, FALSE, NULL),
('food_dal', 'Yellow Moong Dal (Cooked)', 'Plant Protein', '1 bowl (150g)', 147.0, 9.8, 24.0, 0.8, FALSE, NULL),
('food_chickpeas', 'Chickpeas / Chana (Boiled)', 'Legumes', '100g', 164.0, 8.9, 27.4, 2.6, FALSE, NULL),
('food_white_rice', 'White Basmati Rice (Cooked)', 'Carbohydrates', '100g', 130.0, 2.7, 28.2, 0.3, FALSE, NULL),
('food_brown_rice', 'Brown Rice (Cooked)', 'Carbohydrates', '100g', 111.0, 2.6, 23.0, 0.9, FALSE, NULL),
('food_oats', 'Rolled Oats (Raw)', 'Carbohydrates', '100g', 389.0, 16.9, 66.3, 6.9, FALSE, NULL),
('food_roti', 'Whole Wheat Roti / Chapati', 'Carbohydrates', '1 medium (40g)', 104.0, 3.1, 22.0, 0.5, FALSE, NULL),
('food_bread', 'Whole Wheat Bread Slice', 'Carbohydrates', '1 slice (35g)', 82.0, 4.0, 13.8, 1.1, FALSE, NULL),
('food_sweet_potato', 'Sweet Potato (Baked)', 'Carbohydrates', '100g', 90.0, 2.0, 20.7, 0.2, FALSE, NULL),
('food_banana', 'Banana (Medium)', 'Fruits', '1 fruit (118g)', 105.0, 1.3, 27.0, 0.3, FALSE, NULL),
('food_apple', 'Apple (Medium)', 'Fruits', '1 fruit (182g)', 95.0, 0.5, 25.1, 0.3, FALSE, NULL),
('food_peanut_butter', 'Peanut Butter (Natural)', 'Healthy Fats', '2 tbsp (32g)', 188.0, 8.0, 6.0, 16.0, FALSE, NULL),
('food_almonds', 'Almonds (Raw)', 'Nuts & Seeds', '28g (approx 23 nuts)', 164.0, 6.0, 6.1, 14.2, FALSE, NULL),
('food_broccoli', 'Broccoli (Steamed)', 'Vegetables', '100g', 35.0, 2.4, 7.2, 0.4, FALSE, NULL),
('food_spinach', 'Spinach (Cooked)', 'Vegetables', '100g', 23.0, 3.0, 3.8, 0.3, FALSE, NULL)
ON CONFLICT (id) DO NOTHING;
