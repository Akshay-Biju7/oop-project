package com.pgtr.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Encapsulates a complete gym workout session containing multiple exercises.
 */
public class Workout extends IdentifiableEntity {
    private String userId;
    private LocalDate workoutDate;
    private String workoutName;
    private String muscleGroup;
    private int durationMinutes;
    private int caloriesBurned;
    private String notes;
    private List<Exercise> exercises;

    public Workout() {
        super();
        this.workoutDate = LocalDate.now();
        this.durationMinutes = 45;
        this.caloriesBurned = 0;
        this.exercises = new ArrayList<>();
    }

    public Workout(String id, String userId, LocalDate workoutDate, String workoutName, 
                   String muscleGroup, int durationMinutes, int caloriesBurned, String notes) {
        super(id);
        this.userId = userId;
        this.workoutDate = workoutDate != null ? workoutDate : LocalDate.now();
        this.workoutName = workoutName;
        this.muscleGroup = muscleGroup;
        this.durationMinutes = durationMinutes;
        this.caloriesBurned = caloriesBurned;
        this.notes = notes;
        this.exercises = new ArrayList<>();
    }

    public void addExercise(Exercise exercise) {
        if (exercise != null) {
            exercise.setWorkoutId(this.id);
            exercise.setUserId(this.userId);
            this.exercises.add(exercise);
        }
    }

    /**
     * Calculates total training volume across all exercises in this workout session.
     */
    public double getTotalVolume() {
        if (exercises == null || exercises.isEmpty()) return 0.0;
        double sum = 0.0;
        for (Exercise ex : exercises) {
            sum += ex.getVolumeKg();
        }
        return sum;
    }

    /**
     * Calculates total sets performed.
     */
    public int getTotalSets() {
        if (exercises == null || exercises.isEmpty()) return 0;
        int sum = 0;
        for (Exercise ex : exercises) {
            sum += ex.getSets();
        }
        return sum;
    }

    /**
     * Calculates total repetitions performed.
     */
    public int getTotalReps() {
        if (exercises == null || exercises.isEmpty()) return 0;
        int sum = 0;
        for (Exercise ex : exercises) {
            sum += (ex.getSets() * ex.getReps());
        }
        return sum;
    }

    // --- Getters and Setters ---

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public LocalDate getWorkoutDate() {
        return workoutDate;
    }

    public void setWorkoutDate(LocalDate workoutDate) {
        this.workoutDate = workoutDate;
    }

    public String getWorkoutName() {
        return workoutName;
    }

    public void setWorkoutName(String workoutName) {
        this.workoutName = workoutName;
    }

    public String getMuscleGroup() {
        return muscleGroup;
    }

    public void setMuscleGroup(String muscleGroup) {
        this.muscleGroup = muscleGroup;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(int durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public int getCaloriesBurned() {
        return caloriesBurned;
    }

    public void setCaloriesBurned(int caloriesBurned) {
        this.caloriesBurned = caloriesBurned;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public List<Exercise> getExercises() {
        return exercises;
    }

    public void setExercises(List<Exercise> exercises) {
        this.exercises = exercises != null ? exercises : new ArrayList<>();
    }
}
