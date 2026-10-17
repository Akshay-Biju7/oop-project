package com.pgtr.model;

/**
 * Represents an individual exercise entry within a gym workout.
 */
public class Exercise extends IdentifiableEntity {
    private String workoutId;
    private String userId;
    private String exerciseName;
    private String exerciseType;
    private int sets;
    private int reps;
    private double weightKg;
    private double volumeKg;
    private String notes;

    public Exercise() {
        super();
        this.exerciseType = "Strength";
        this.sets = 3;
        this.reps = 10;
        this.weightKg = 0.0;
        this.volumeKg = 0.0;
    }

    public Exercise(String id, String workoutId, String userId, String exerciseName, 
                    String exerciseType, int sets, int reps, double weightKg, String notes) {
        super(id);
        this.workoutId = workoutId;
        this.userId = userId;
        this.exerciseName = exerciseName;
        this.exerciseType = exerciseType;
        this.sets = sets;
        this.reps = reps;
        this.weightKg = weightKg;
        this.volumeKg = calculateVolume();
        this.notes = notes;
    }

    /**
     * Calculates training volume = sets × reps × weight (kg).
     * For bodyweight/cardio, volume is based on reps or 0 if weight is 0.
     */
    public double calculateVolume() {
        return sets * reps * weightKg;
    }

    public void recalculateVolume() {
        this.volumeKg = calculateVolume();
    }

    // --- Getters and Setters ---

    public String getWorkoutId() {
        return workoutId;
    }

    public void setWorkoutId(String workoutId) {
        this.workoutId = workoutId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getExerciseName() {
        return exerciseName;
    }

    public void setExerciseName(String exerciseName) {
        this.exerciseName = exerciseName;
    }

    public String getExerciseType() {
        return exerciseType;
    }

    public void setExerciseType(String exerciseType) {
        this.exerciseType = exerciseType;
    }

    public int getSets() {
        return sets;
    }

    public void setSets(int sets) {
        this.sets = sets;
        recalculateVolume();
    }

    public int getReps() {
        return reps;
    }

    public void setReps(int reps) {
        this.reps = reps;
        recalculateVolume();
    }

    public double getWeightKg() {
        return weightKg;
    }

    public void setWeightKg(double weightKg) {
        this.weightKg = weightKg;
        recalculateVolume();
    }

    public double getVolumeKg() {
        return volumeKg;
    }

    public void setVolumeKg(double volumeKg) {
        this.volumeKg = volumeKg;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
