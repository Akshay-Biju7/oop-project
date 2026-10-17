package com.pgtr.util;

import com.pgtr.model.Exercise;
import com.pgtr.model.Meal;
import com.pgtr.model.Workout;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

/**
 * Exports personal fitness and nutrition datasets to standard RFC 4180 CSV format.
 */
public class CsvExporter {

    public static void exportWorkouts(List<Workout> workouts, File targetFile) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(targetFile))) {
            writer.println("Workout Date,Workout Name,Muscle Group,Duration (min),Calories Burned,Exercise Name,Exercise Type,Sets,Reps,Weight (kg),Volume (kg),Notes");
            for (Workout w : workouts) {
                if (w.getExercises() == null || w.getExercises().isEmpty()) {
                    writer.printf("%s,%s,%s,%d,%d,%s,%s,%s,%s,%s,%s,%s%n",
                            escape(DateUtil.formatDate(w.getWorkoutDate())),
                            escape(w.getWorkoutName()),
                            escape(w.getMuscleGroup()),
                            w.getDurationMinutes(),
                            w.getCaloriesBurned(),
                            "", "", "", "", "", "",
                            escape(w.getNotes()));
                } else {
                    for (Exercise ex : w.getExercises()) {
                        writer.printf("%s,%s,%s,%d,%d,%s,%s,%d,%d,%.2f,%.2f,%s%n",
                                escape(DateUtil.formatDate(w.getWorkoutDate())),
                                escape(w.getWorkoutName()),
                                escape(w.getMuscleGroup()),
                                w.getDurationMinutes(),
                                w.getCaloriesBurned(),
                                escape(ex.getExerciseName()),
                                escape(ex.getExerciseType()),
                                ex.getSets(),
                                ex.getReps(),
                                ex.getWeightKg(),
                                ex.getVolumeKg(),
                                escape(ex.getNotes()));
                    }
                }
            }
        }
    }

    public static void exportMeals(List<Meal> meals, File targetFile) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(targetFile))) {
            writer.println("Date,Time,Meal Type,Food Name,Serving Quantity,Calories (kcal),Protein (g),Carbs (g),Fat (g),Notes");
            for (Meal m : meals) {
                writer.printf("%s,%s,%s,%s,%.2f,%.1f,%.1f,%.1f,%.1f,%s%n",
                        escape(DateUtil.formatDate(m.getMealDate())),
                        escape(DateUtil.formatTime(m.getMealTime())),
                        escape(m.getMealType().getDisplayName()),
                        escape(m.getFoodName()),
                        m.getServingQuantity(),
                        m.getCalories(),
                        m.getProteinG(),
                        m.getCarbsG(),
                        m.getFatG(),
                        escape(m.getNotes()));
            }
        }
    }

    private static String escape(String field) {
        if (field == null) return "\"\"";
        return "\"" + field.replace("\"", "\"\"") + "\"";
    }
}
