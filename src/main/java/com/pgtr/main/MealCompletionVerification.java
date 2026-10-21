package com.pgtr.main;

import com.pgtr.model.*;
import com.pgtr.service.*;

import java.time.LocalDate;
import java.util.List;

public class MealCompletionVerification {

    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println("  RUNNING VERIFICATION FOR MEAL COMPLETION BOX   ");
        System.out.println("=================================================\n");

        int passed = 0;
        int total = 0;

        try {
            // Test 1: Nutrition Plan Completion Box & Logging
            total++;
            System.out.println("[Test 1] Testing Nutrition Plan Completion Checkbox & Consumption Logging...");
            NutritionService nutritionService = new NutritionService();
            String testUid = "athlete_test_" + System.currentTimeMillis();

            List<NutritionPlanItem> plan = nutritionService.getNutritionPlan(testUid, "BULK");
            if (plan == null || plan.isEmpty()) {
                throw new AssertionError("Plan should have default items");
            }

            NutritionPlanItem item = plan.get(0);
            if (item.isConsumed()) {
                throw new AssertionError("Item should initially NOT be consumed");
            }

            LocalDate today = LocalDate.now();
            boolean logged = nutritionService.logPlannedMealAsConsumed(item, today);
            if (!logged) {
                throw new AssertionError("Planned meal logging failed!");
            }

            // Verify in DB
            List<NutritionPlanItem> updatedPlan = nutritionService.getNutritionPlan(testUid, "BULK");
            NutritionPlanItem updatedItem = updatedPlan.stream()
                    .filter(i -> i.getId().equals(item.getId()))
                    .findFirst().orElse(null);

            if (updatedItem == null || !updatedItem.isConsumed()) {
                throw new AssertionError("Item should be marked isConsumed = true in database!");
            }

            // Verify it appeared in today's consumed meals
            List<Meal> todayMeals = nutritionService.getMealsForDate(testUid, today);
            boolean inMeals = todayMeals.stream().anyMatch(m -> m.getFoodName().contains(item.getFoodName()));
            if (!inMeals) {
                throw new AssertionError("Meal was not recorded in today's consumed meals!");
            }
            System.out.println("  ✓ Checkbox marking persisted to database and recorded as consumed meal!");
            passed++;

            // Test 2: Unchecking the completion box
            total++;
            System.out.println("\n[Test 2] Testing Unchecking completion box (reversal)...");
            boolean unlogged = nutritionService.unlogPlannedMeal(item, today);
            if (!unlogged) {
                throw new AssertionError("Unlogging failed!");
            }

            List<NutritionPlanItem> unloggedPlan = nutritionService.getNutritionPlan(testUid, "BULK");
            NutritionPlanItem unloggedItem = unloggedPlan.stream()
                    .filter(i -> i.getId().equals(item.getId()))
                    .findFirst().orElse(null);

            if (unloggedItem == null || unloggedItem.isConsumed()) {
                throw new AssertionError("Item should be marked isConsumed = false in database after unchecking!");
            }

            List<Meal> mealsAfter = nutritionService.getMealsForDate(testUid, today);
            boolean stillThere = mealsAfter.stream().anyMatch(m -> m.getFoodName().contains(item.getFoodName()));
            if (stillThere) {
                throw new AssertionError("Meal should be removed from today's meals after unchecking!");
            }
            System.out.println("  ✓ Unchecking completion box reverses consumption cleanly!");
            passed++;

            System.out.printf("%n=================================================%n");
            System.out.printf("  VERIFICATION COMPLETED: %d/%d TESTS PASSED (100%%)%n", passed, total);
            System.out.printf("=================================================%n");

        } catch (Throwable t) {
            System.err.println("\nFAILED: " + t.getMessage());
            t.printStackTrace();
            System.exit(1);
        }
    }
}
