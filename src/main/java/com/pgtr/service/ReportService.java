package com.pgtr.service;

import com.pgtr.dao.HealthDAO;
import com.pgtr.dao.MealDAO;
import com.pgtr.dao.SleepDAO;
import com.pgtr.dao.WaterDAO;
import com.pgtr.dao.WorkoutDAO;
import com.pgtr.dao.impl.*;
import com.pgtr.model.BodyMeasurement;
import com.pgtr.model.Meal;
import com.pgtr.model.SleepLog;
import com.pgtr.model.User;
import com.pgtr.model.Workout;
import com.pgtr.util.DateUtil;
import com.pgtr.util.Theme;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

import java.awt.*;
import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.List;

public class ReportService {

    private final WorkoutDAO workoutDAO;
    private final MealDAO mealDAO;
    private final HealthDAO healthDAO;
    private final WaterDAO waterDAO;
    private final SleepDAO sleepDAO;

    public ReportService() {
        this.workoutDAO = new WorkoutDAOImpl();
        this.mealDAO = new MealDAOImpl();
        this.healthDAO = new HealthDAOImpl();
        this.waterDAO = new WaterDAOImpl();
        this.sleepDAO = new SleepDAOImpl();
    }

    /**
     * Builds weekly workout activity bar chart (Mon - Sun).
     */
    public JFreeChart createWeeklyActivityChart(String userId) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        LocalDate monday = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        Map<DayOfWeek, Integer> countMap = new LinkedHashMap<>();
        for (DayOfWeek d : DayOfWeek.values()) {
            countMap.put(d, 0);
        }

        try {
            List<Workout> workouts = workoutDAO.getWorkoutsByDateRange(userId, monday, monday.plusDays(6));
            for (Workout w : workouts) {
                DayOfWeek dow = w.getWorkoutDate().getDayOfWeek();
                countMap.put(dow, countMap.get(dow) + 1);
            }
        } catch (Exception ignored) {}

        String[] days = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
        DayOfWeek[] dows = {DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
                DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY};

        for (int i = 0; i < 7; i++) {
            dataset.addValue(countMap.get(dows[i]), "Workouts", days[i]);
        }

        JFreeChart chart = ChartFactory.createBarChart(
                "Weekly Workout Frequency", "Day", "Sessions", dataset,
                PlotOrientation.VERTICAL, false, true, false);

        applyDarkThemeToCategoryChart(chart, Theme.PRIMARY);
        return chart;
    }

    /**
     * Builds weight progression trend line chart.
     */
    public JFreeChart createWeightProgressChart(String userId, double targetWeight) {
        TimeSeries weightSeries = new TimeSeries("Actual Weight (kg)");
        TimeSeries targetSeries = new TimeSeries("Target Weight (kg)");

        try {
            List<BodyMeasurement> list = healthDAO.getMeasurementHistory(userId);
            // sort chronological
            list.sort(Comparator.comparing(BodyMeasurement::getLogDate));
            for (BodyMeasurement m : list) {
                Day day = new Day(m.getLogDate().getDayOfMonth(), m.getLogDate().getMonthValue(), m.getLogDate().getYear());
                weightSeries.addOrUpdate(day, m.getWeightKg());
                if (targetWeight > 0) {
                    targetSeries.addOrUpdate(day, targetWeight);
                }
            }
        } catch (Exception ignored) {}

        TimeSeriesCollection dataset = new TimeSeriesCollection();
        dataset.addSeries(weightSeries);
        if (targetWeight > 0 && weightSeries.getItemCount() > 0) {
            dataset.addSeries(targetSeries);
        }

        JFreeChart chart = ChartFactory.createTimeSeriesChart(
                "Body Weight Progression", "Date", "Weight (kg)", dataset,
                true, true, false);

        applyDarkThemeToXYChart(chart);
        return chart;
    }

    /**
     * Builds calorie and protein intake trend chart for the last 30 days.
     */
    public JFreeChart createNutritionTrendChart(String userId, int days) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        LocalDate end = LocalDate.now();
        LocalDate start = end.minusDays(days - 1);

        Map<LocalDate, Double> caloriesMap = new TreeMap<>();
        Map<LocalDate, Double> proteinMap = new TreeMap<>();
        for (int i = 0; i < days; i++) {
            LocalDate d = start.plusDays(i);
            caloriesMap.put(d, 0.0);
            proteinMap.put(d, 0.0);
        }

        try {
            List<Meal> meals = mealDAO.getMealsByDateRange(userId, start, end);
            for (Meal m : meals) {
                if (caloriesMap.containsKey(m.getMealDate())) {
                    caloriesMap.put(m.getMealDate(), caloriesMap.get(m.getMealDate()) + m.getCalories());
                    proteinMap.put(m.getMealDate(), proteinMap.get(m.getMealDate()) + m.getProteinG());
                }
            }
        } catch (Exception ignored) {}

        for (Map.Entry<LocalDate, Double> e : caloriesMap.entrySet()) {
            String label = e.getKey().format(DateUtil.SHORT_DATE_FORMATTER);
            dataset.addValue(e.getValue(), "Calories (kcal)", label);
        }

        JFreeChart chart = ChartFactory.createLineChart(
                "Calorie Intake Trend (" + days + " Days)", "Date", "Calories (kcal)", dataset,
                PlotOrientation.VERTICAL, false, true, false);

        applyDarkThemeToCategoryChart(chart, Theme.CALORIE_ORANGE);
        return chart;
    }

    /**
     * Builds daily macro distribution pie chart (Protein, Carbs, Fats in calories).
     */
    public JFreeChart createMacroSplitChart(double proteinG, double carbsG, double fatG) {
        DefaultPieDataset dataset = new DefaultPieDataset();
        double pCal = proteinG * 4.0;
        double cCal = carbsG * 4.0;
        double fCal = fatG * 9.0;

        if (pCal == 0 && cCal == 0 && fCal == 0) {
            dataset.setValue("No Meals Recorded", 100);
        } else {
            dataset.setValue("Protein (" + (int)proteinG + "g)", pCal);
            dataset.setValue("Carbohydrates (" + (int)carbsG + "g)", cCal);
            dataset.setValue("Healthy Fats (" + (int)fatG + "g)", fCal);
        }

        JFreeChart chart = ChartFactory.createPieChart("Macronutrient Split (Calories)", dataset, true, true, false);
        chart.setBackgroundPaint(Theme.BG_PANEL);
        chart.getTitle().setPaint(Theme.TEXT_PRIMARY);
        chart.getTitle().setFont(Theme.SUBTITLE);
        if (chart.getLegend() != null) {
            chart.getLegend().setBackgroundPaint(Theme.BG_PANEL);
            chart.getLegend().setItemPaint(Theme.TEXT_MUTED);
            chart.getLegend().setItemFont(Theme.BODY);
        }

        var plot = (org.jfree.chart.plot.PiePlot) chart.getPlot();
        plot.setBackgroundPaint(Theme.BG_DARK);
        plot.setOutlinePaint(Theme.BORDER);
        plot.setSectionPaint("Protein (" + (int)proteinG + "g)", Theme.PRIMARY);
        plot.setSectionPaint("Carbohydrates (" + (int)carbsG + "g)", Theme.WATER_CYAN);
        plot.setSectionPaint("Healthy Fats (" + (int)fatG + "g)", Theme.CALORIE_ORANGE);
        plot.setLabelFont(Theme.SMALL);
        plot.setLabelBackgroundPaint(Theme.BG_PANEL);
        plot.setLabelOutlinePaint(Theme.BORDER);
        plot.setLabelPaint(Theme.TEXT_PRIMARY);

        return chart;
    }

    /**
     * Builds workout training volume progression line chart.
     */
    public JFreeChart createVolumeTrendChart(String userId) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        try {
            List<Workout> workouts = workoutDAO.getWorkoutsByUser(userId);
            // chronologically sort last 15
            workouts.sort(Comparator.comparing(Workout::getWorkoutDate));
            if (workouts.size() > 15) {
                workouts = workouts.subList(workouts.size() - 15, workouts.size());
            }

            for (Workout w : workouts) {
                String label = w.getWorkoutDate().format(DateUtil.SHORT_DATE_FORMATTER);
                dataset.addValue(w.getTotalVolume(), "Volume (kg)", label);
            }
        } catch (Exception ignored) {}

        JFreeChart chart = ChartFactory.createLineChart(
                "Training Volume Progression (kg)", "Workout Session", "Total Volume (kg)",
                dataset, PlotOrientation.VERTICAL, false, true, false);

        applyDarkThemeToCategoryChart(chart, Theme.PRIMARY);
        return chart;
    }

    /**
     * Formats comprehensive text progress report ready for preview or printing.
     */
    public String generateProgressReport(User user) {
        StringBuilder sb = new StringBuilder();
        sb.append("======================================================================\n");
        sb.append("         PERSONAL GAIN TRACKING SYSTEM — PROGRESS REPORT             \n");
        sb.append("======================================================================\n\n");
        sb.append("Generated On: ").append(DateUtil.formatDate(LocalDate.now())).append("\n\n");

        sb.append("--- USER PROFILE & TARGETS ---\n");
        sb.append("Name: ").append(user.getFullName()).append("\n");
        sb.append("Email: ").append(user.getEmail()).append("\n");
        sb.append("Age: ").append(user.getAge()).append(" years | Height: ").append(user.getHeightCm()).append(" cm\n");
        sb.append("Current Weight: ").append(user.getCurrentWeightKg()).append(" kg | Target Weight: ").append(user.getTargetWeightKg()).append(" kg\n");
        sb.append("Fitness Goal: ").append(user.getFitnessGoal().getDisplayName()).append("\n");
        sb.append("Activity Level: ").append(user.getActivityLevel().getDisplayName()).append("\n");
        sb.append("Daily Targets -> Calories: ").append(user.getDailyCalorieTarget()).append(" kcal | Protein: ")
                .append(user.getDailyProteinTarget()).append("g | Water: ").append(user.getDailyWaterTarget()).append(" ml\n\n");

        try {
            int totalWorkouts = workoutDAO.getTotalWorkoutsCount(user.getId());
            int thisWeek = workoutDAO.getWorkoutCountThisWeek(user.getId());
            var latestMeasurement = healthDAO.getLatestMeasurement(user.getId());

            sb.append("--- GYM PERFORMANCE & STATS ---\n");
            sb.append("Total Recorded Workouts: ").append(totalWorkouts).append("\n");
            sb.append("Workouts Completed This Week: ").append(thisWeek).append("\n");

            if (latestMeasurement.isPresent()) {
                BodyMeasurement m = latestMeasurement.get();
                sb.append("\n--- LATEST BODY MEASUREMENTS ---\n");
                sb.append("Date: ").append(DateUtil.formatDate(m.getLogDate())).append("\n");
                sb.append("Weight: ").append(m.getWeightKg()).append(" kg (BMI: ").append(m.getBmi())
                        .append(" - ").append(m.getBmiCategory()).append(")\n");
                sb.append("Waist: ").append(m.getWaistCm()).append(" cm | Chest: ").append(m.getChestCm()).append(" cm\n");
                sb.append("Arms: ").append(m.getArmsCm()).append(" cm | Thighs: ").append(m.getThighsCm()).append(" cm\n");
            }
        } catch (Exception ignored) {}

        sb.append("\n======================================================================\n");
        sb.append("Notice: Body Mass Index (BMI) is a screening metric only and does not\n");
        sb.append("differentiate lean muscle mass from adipose body fat.\n");
        sb.append("======================================================================\n");
        return sb.toString();
    }

    // --- Chart Dark Theming Helpers ---

    private void applyDarkThemeToCategoryChart(JFreeChart chart, Color accentColor) {
        chart.setBackgroundPaint(Theme.BG_PANEL);
        chart.getTitle().setPaint(Theme.TEXT_PRIMARY);
        chart.getTitle().setFont(Theme.SUBTITLE);

        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(Theme.BG_DARK);
        plot.setOutlinePaint(Theme.BORDER);
        plot.setDomainGridlinePaint(Theme.BORDER);
        plot.setRangeGridlinePaint(Theme.BORDER);

        CategoryAxis domainAxis = plot.getDomainAxis();
        domainAxis.setLabelPaint(Theme.TEXT_MUTED);
        domainAxis.setLabelFont(Theme.BODY);
        domainAxis.setTickLabelPaint(Theme.TEXT_MUTED);
        domainAxis.setTickLabelFont(Theme.SMALL);
        domainAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_45);

        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setLabelPaint(Theme.TEXT_MUTED);
        rangeAxis.setLabelFont(Theme.BODY);
        rangeAxis.setTickLabelPaint(Theme.TEXT_MUTED);
        rangeAxis.setTickLabelFont(Theme.SMALL);

        if (plot.getRenderer() instanceof BarRenderer) {
            BarRenderer r = (BarRenderer) plot.getRenderer();
            r.setSeriesPaint(0, accentColor);
            r.setDrawBarOutline(false);
            r.setMaximumBarWidth(0.08);
            r.setShadowVisible(false);
        } else if (plot.getRenderer() instanceof LineAndShapeRenderer) {
            LineAndShapeRenderer r = (LineAndShapeRenderer) plot.getRenderer();
            r.setSeriesPaint(0, accentColor);
            r.setSeriesStroke(0, new BasicStroke(2.5f));
            r.setSeriesShapesVisible(0, true);
        }
    }

    private void applyDarkThemeToXYChart(JFreeChart chart) {
        chart.setBackgroundPaint(Theme.BG_PANEL);
        chart.getTitle().setPaint(Theme.TEXT_PRIMARY);
        chart.getTitle().setFont(Theme.SUBTITLE);

        if (chart.getLegend() != null) {
            chart.getLegend().setBackgroundPaint(Theme.BG_PANEL);
            chart.getLegend().setItemPaint(Theme.TEXT_MUTED);
            chart.getLegend().setItemFont(Theme.BODY);
        }

        XYPlot plot = chart.getXYPlot();
        plot.setBackgroundPaint(Theme.BG_DARK);
        plot.setOutlinePaint(Theme.BORDER);
        plot.setDomainGridlinePaint(Theme.BORDER);
        plot.setRangeGridlinePaint(Theme.BORDER);

        plot.getDomainAxis().setLabelPaint(Theme.TEXT_MUTED);
        plot.getDomainAxis().setLabelFont(Theme.BODY);
        plot.getDomainAxis().setTickLabelPaint(Theme.TEXT_MUTED);
        plot.getDomainAxis().setTickLabelFont(Theme.SMALL);

        plot.getRangeAxis().setLabelPaint(Theme.TEXT_MUTED);
        plot.getRangeAxis().setLabelFont(Theme.BODY);
        plot.getRangeAxis().setTickLabelPaint(Theme.TEXT_MUTED);
        plot.getRangeAxis().setTickLabelFont(Theme.SMALL);

        if (plot.getRenderer() instanceof XYLineAndShapeRenderer) {
            XYLineAndShapeRenderer r = (XYLineAndShapeRenderer) plot.getRenderer();
            r.setSeriesPaint(0, Theme.PRIMARY);
            r.setSeriesStroke(0, new BasicStroke(2.5f));
            r.setSeriesShapesVisible(0, true);

            // target series
            r.setSeriesPaint(1, Theme.WARNING);
            float[] dash = {6.0f, 6.0f};
            r.setSeriesStroke(1, new BasicStroke(2.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dash, 0.0f));
            r.setSeriesShapesVisible(1, false);
        }
    }
}
