package com.pgtr.main;

import com.pgtr.database.DatabaseManager;
import com.pgtr.model.*;
import com.pgtr.service.*;
import com.pgtr.util.CsvExporter;
import com.pgtr.util.DateUtil;
import com.pgtr.util.PasswordHasher;
import org.jfree.chart.JFreeChart;

import java.io.File;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Automated end-to-end test verification suite for Personal Gain Tracking System (PGTR).
 * Validates database connectivity, CRUD operations, calculations, achievements, and export generation.
 */
public class SystemVerification {

    public static void main(String[] args) {
        System.out.println("=================================================================");
        System.out.println("  STARTING PERSONAL GAIN TRACKING SYSTEM (PGTR) VERIFICATION     ");
        System.out.println("=================================================================\n");

        int testsPassed = 0;
        int totalTests = 0;

        try {
            // 1. Test Database Connectivity
            totalTests++;
            System.out.println("[Test 1] Initializing Database Connection...");
            DatabaseManager db = DatabaseManager.getInstance();
            System.out.println("  Status: " + db.getStatusMessage());
            assertNotNull(db.getConnection(), "Connection should not be null");
            System.out.println("  ✓ Database initialized successfully!");
            testsPassed++;

            // 2. Test User Registration & Password Hashing
            totalTests++;
            System.out.println("\n[Test 2] Testing User Registration & BCrypt Password Hashing...");
            AuthService auth = new AuthService();
            String testEmail = "test_athlete_" + System.currentTimeMillis() + "@pgtr.com";
            User user = auth.register(
                    "Marcus Vance", testEmail, "SecureGainz123",
                    23, 180.0, 78.0, 82.0,
                    FitnessGoalType.MUSCLE_GAIN, ActivityLevel.MODERATELY_ACTIVE
            );
            assertNotNull(user.getId(), "User ID should be generated");
            assertTrue(PasswordHasher.verify("SecureGainz123", user.getPasswordHash()), "Password hash must match");
            System.out.println("  User registered: " + user.getFullName() + " (" + user.getEmail() + ")");
            System.out.println("  ✓ Registration & BCrypt hashing verified!");
            testsPassed++;

            // 3. Test Login Authentication
            totalTests++;
            System.out.println("\n[Test 3] Testing Login Authentication...");
            User loggedIn = auth.login(testEmail, "SecureGainz123");
            assertEquals(user.getId(), loggedIn.getId(), "Logged in user ID must match registered ID");
            System.out.println("  ✓ Login successful! UserSession active.");
            testsPassed++;

            // 4. Test Scientific Calculators (BMR, TDEE, Calories, BMI)
            totalTests++;
            System.out.println("\n[Test 4] Testing Scientific Calculators...");
            double bmr = user.calculateBmr();
            double tdee = user.calculateTdee();
            int suggCal = user.calculateSuggestedCalories();
            int suggProt = user.calculateSuggestedProtein();
            double bmi = user.calculateBmi();
            System.out.printf("  Calculated BMR: %.1f kcal | TDEE: %.1f kcal%n", bmr, tdee);
            System.out.printf("  Suggested Daily Calories: %d kcal | Suggested Protein: %d g%n", suggCal, suggProt);
            System.out.printf("  Calculated BMI: %.1f%n", bmi);
            assertTrue(bmr > 1500 && bmr < 2200, "BMR should be in reasonable human range");
            assertTrue(bmi > 20.0 && bmi < 26.0, "BMI should match 78kg @ 180cm");
            System.out.println("  ✓ Scientific fitness calculations verified!");
            testsPassed++;

            // 5. Test Workout Logging & Volume Calculation
            totalTests++;
            System.out.println("\n[Test 5] Testing Workout & Exercise Logging...");
            WorkoutService workoutService = new WorkoutService();
            Workout workout = new Workout(null, user.getId(), LocalDate.now(),
                    "Heavy Chest & Triceps Blast", "Chest", 55, 380, "Felt powerful today");

            Exercise bench = new Exercise(null, null, user.getId(), "Barbell Bench Press", "Strength", 4, 8, 90.0, "Clean form");
            Exercise incline = new Exercise(null, null, user.getId(), "Incline Dumbbell Press", "Hypertrophy", 3, 10, 32.0, "Slow eccentric");
            workout.addExercise(bench);
            workout.addExercise(incline);

            double expectedVolume = (4 * 8 * 90.0) + (3 * 10 * 32.0); // 2880 + 960 = 3840 kg
            assertEquals(expectedVolume, workout.getTotalVolume(), 0.1, "Total volume should match sets * reps * weight");

            boolean wSaved = workoutService.logWorkout(workout);
            assertTrue(wSaved, "Workout should be saved to database");
            System.out.printf("  Logged workout '%s' with volume: %.1f kg%n", workout.getWorkoutName(), workout.getTotalVolume());
            System.out.println("  ✓ Workout management & volume calculations verified!");
            testsPassed++;

            // 6. Test Nutrition & Meal Logging
            totalTests++;
            System.out.println("\n[Test 6] Testing Nutrition Logging & Daily Summaries...");
            NutritionService nutritionService = new NutritionService();
            Meal meal1 = new Meal(null, user.getId(), LocalDate.now(), LocalTime.of(8, 30),
                    MealType.BREAKFAST, "Rolled Oats with Whey & Banana", 1.5, 650.0, 48.0, 85.0, 12.0, "Pre-workout fuel");
            Meal meal2 = new Meal(null, user.getId(), LocalDate.now(), LocalTime.of(13, 15),
                    MealType.LUNCH, "Grilled Chicken Breast with White Rice & Broccoli", 2.0, 720.0, 65.0, 70.0, 14.0, "Post-workout recovery");

            nutritionService.logMeal(meal1);
            nutritionService.logMeal(meal2);

            var dailySummary = nutritionService.getDailySummary(user.getId(), LocalDate.now());
            assertEquals(2, dailySummary.mealsCount, "Should have 2 meals logged today");
            assertEquals(1370.0, dailySummary.totalCalories, 0.1, "Total calories should sum up");
            assertEquals(113.0, dailySummary.totalProtein, 0.1, "Total protein should sum up");
            System.out.printf("  Daily Nutrition -> Calories: %.0f kcal | Protein: %.1f g | Carbs: %.1f g | Fat: %.1f g%n",
                    dailySummary.totalCalories, dailySummary.totalProtein, dailySummary.totalCarbs, dailySummary.totalFat);
            System.out.println("  ✓ Nutrition tracking & daily summaries verified!");
            testsPassed++;

            // 7. Test Body Measurements & Date Comparison
            totalTests++;
            System.out.println("\n[Test 7] Testing Body Measurements & Anthropometric Comparison...");
            HealthService healthService = new HealthService();
            BodyMeasurement m1 = new BodyMeasurement(null, user.getId(), LocalDate.now().minusDays(30),
                    80.0, 180.0, 84.0, 102.0, 36.0, 58.0, 98.0, 39.0, "Baseline check");
            BodyMeasurement m2 = new BodyMeasurement(null, user.getId(), LocalDate.now(),
                    78.0, 180.0, 81.0, 104.0, 37.5, 59.0, 97.0, 39.0, "Progress check");

            healthService.logMeasurement(m1);
            healthService.logMeasurement(m2);

            var comp = healthService.compare(m1, m2);
            assertEquals(-2.0, comp.weightDiff, 0.1, "Weight difference should be -2.0 kg");
            assertEquals(-3.0, comp.waistDiff, 0.1, "Waist difference should be -3.0 cm");
            assertEquals(2.0, comp.chestDiff, 0.1, "Chest difference should be +2.0 cm");
            assertEquals(1.5, comp.armsDiff, 0.1, "Arms difference should be +1.5 cm");
            System.out.printf("  Recomposition: Weight %.1f kg, Waist %.1f cm, Chest +%.1f cm, Arms +%.1f cm%n",
                    comp.weightDiff, comp.waistDiff, comp.chestDiff, comp.armsDiff);
            System.out.println("  ✓ Body measurement tracking and comparison verified!");
            testsPassed++;

            // 8. Test Water & Sleep Habit Tracking
            totalTests++;
            System.out.println("\n[Test 8] Testing Water Intake & Sleep Tracking...");
            HabitService habitService = new HabitService();
            habitService.addWater(user.getId(), 500);
            habitService.addWater(user.getId(), 750);
            int totalWater = habitService.getTodayWaterTotal(user.getId());
            assertEquals(1250, totalWater, "Water total should equal 1250 ml");

            SleepLog sleep = new SleepLog(null, user.getId(), LocalDate.now(),
                    LocalTime.of(23, 30), LocalTime.of(7, 30), 8.0, "Excellent", "Deep sleep, felt recharged");
            habitService.logSleep(sleep);
            System.out.printf("  Water Today: %d ml | Sleep Duration: %.1f hrs (%s)%n",
                    totalWater, sleep.getDurationHours(), sleep.getSleepQuality());
            System.out.println("  ✓ Water & sleep tracking verified!");
            testsPassed++;

            // 9. Test Fitness Goals & Progress Percentage
            totalTests++;
            System.out.println("\n[Test 9] Testing Fitness Goal Tracking & Progress Math...");
            GoalService goalService = new GoalService();
            FitnessGoal goal = new FitnessGoal(null, user.getId(), "Reach 100 kg Bench Press",
                    "Progressive overload milestone", GoalCategory.STRENGTH, 70.0, 100.0, 90.0, "kg",
                    LocalDate.now().minusDays(15), LocalDate.now().plusMonths(2), GoalStatus.IN_PROGRESS);

            // Starting 70, Target 100, Current 90 -> (90-70)/(100-70) = 20/30 = 67%
            assertEquals(67, goal.getProgressPercentage(), "Progress percentage should be 67%");
            goalService.createGoal(goal);
            System.out.printf("  Goal '%s': %.1f -> %.1f %s (%d%% complete)%n",
                    goal.getTitle(), goal.getCurrentValue(), goal.getTargetValue(), goal.getUnit(), goal.getProgressPercentage());
            System.out.println("  ✓ Goal creation & progress percentage verified!");
            testsPassed++;

            // 10. Test Weekly Planner & Routine Scheduling
            totalTests++;
            System.out.println("\n[Test 10] Testing Weekly Workout Planner...");
            PlannerService plannerService = new PlannerService();
            WorkoutPlan planMonday = new WorkoutPlan(null, user.getId(), DayOfWeek.MONDAY,
                    "Chest & Triceps Hypertrophy", "Chest", "Bench Press, DB Incline, Dips",
                    LocalTime.of(18, 0), "Push day", true);
            plannerService.savePlan(planMonday);

            var mondayPlan = plannerService.getWeeklySchedule(user.getId());
            assertTrue(!mondayPlan.isEmpty(), "Weekly plan should contain saved Monday routine");
            System.out.println("  Monday Plan: " + mondayPlan.get(0).getWorkoutName() + " (" + mondayPlan.get(0).getPlannedExercises() + ")");
            System.out.println("  ✓ Weekly planner verified!");
            testsPassed++;

            // 11. Test Gamified Achievements System
            totalTests++;
            System.out.println("\n[Test 11] Testing Gamified Achievements System...");
            AchievementService achievementService = new AchievementService();
            var userBadges = achievementService.getUserAchievements(user.getId());
            System.out.println("  Earned Badges Count: " + userBadges.size());
            for (var b : userBadges) {
                System.out.println("    🏆 " + b.getAchievement().getTitle() + " (" + b.getAchievement().getDescription() + ")");
            }
            assertTrue(!userBadges.isEmpty(), "User should have unlocked 'FIRST_WORKOUT' achievement");
            System.out.println("  ✓ Achievements and gamification engine verified!");
            testsPassed++;

            // 12. Test JFreeChart Rendering & CSV Export
            totalTests++;
            System.out.println("\n[Test 12] Testing JFreeChart Data Visualizations & CSV Export...");
            ReportService reportService = new ReportService();
            JFreeChart activityChart = reportService.createWeeklyActivityChart(user.getId());
            JFreeChart weightChart = reportService.createWeightProgressChart(user.getId(), user.getTargetWeightKg());
            JFreeChart calorieChart = reportService.createNutritionTrendChart(user.getId(), 30);
            assertNotNull(activityChart, "Activity chart should be generated");
            assertNotNull(weightChart, "Weight chart should be generated");
            assertNotNull(calorieChart, "Calorie chart should be generated");

            File testCsv = new File("test_workouts_export.csv");
            List<Workout> workouts = workoutService.getUserWorkouts(user.getId());
            CsvExporter.exportWorkouts(workouts, testCsv);
            assertTrue(testCsv.exists() && testCsv.length() > 0, "Exported CSV file should exist and have data");
            testCsv.delete(); // cleanup test file

            String textReport = reportService.generateProgressReport(user);
            assertTrue(textReport.contains("PERSONAL GAIN TRACKING SYSTEM"), "Report header must exist");
            System.out.println("  ✓ JFreeChart and CSV exports generated successfully!");
            testsPassed++;

            System.out.println("\n=================================================================");
            System.out.printf("  ALL TESTS PASSED: %d / %d (100%% Success Rate)%n", testsPassed, totalTests);
            System.out.println("=================================================================\n");

        } catch (Throwable t) {
            System.err.println("\n❌ TEST FAILED at step: " + t.getMessage());
            t.printStackTrace();
            System.exit(1);
        }
    }

    private static void assertEquals(Object expected, Object actual, String message) {
        if (!expected.equals(actual)) {
            throw new AssertionError(message + " - Expected: " + expected + ", Actual: " + actual);
        }
    }

    private static void assertEquals(double expected, double actual, double epsilon, String message) {
        if (Math.abs(expected - actual) > epsilon) {
            throw new AssertionError(message + " - Expected: " + expected + ", Actual: " + actual);
        }
    }

    private static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError("Assertion failed: " + message);
        }
    }

    private static void assertNotNull(Object obj, String message) {
        if (obj == null) {
            throw new AssertionError("Null check failed: " + message);
        }
    }
}
