package com.pgtr.view;

import com.pgtr.model.Meal;
import com.pgtr.model.User;
import com.pgtr.model.Workout;
import com.pgtr.service.*;
import com.pgtr.util.CsvExporter;
import com.pgtr.util.Theme;
import com.pgtr.view.components.ModernCard;
import com.pgtr.view.components.NeonButton;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;

import javax.swing.*;
import java.awt.*;
import java.awt.print.PrinterJob;
import java.io.File;
import java.util.List;

/**
 * Progress Analysis and Reports module.
 * Visualizes analytics with JFreeChart, provides CSV export for workouts & nutrition,
 * and generates formatted printable progress reports.
 */
public class ReportsPanel extends JPanel {

    private final MainFrame mainFrame;
    private final ReportService reportService;
    private final WorkoutService workoutService;
    private final NutritionService nutritionService;

    // Charts
    private JPanel activityChartPanel;
    private JPanel calorieTrendPanel;
    private JPanel weightTrendPanel;
    private JPanel volumeTrendPanel;

    private JComboBox<String> timeRangeCombo;

    public ReportsPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.reportService = new ReportService();
        this.workoutService = new WorkoutService();
        this.nutritionService = new NutritionService();

        setOpaque(false);
        setLayout(new BorderLayout(0, 16));
        setBorder(BorderFactory.createEmptyBorder(20, 24, 24, 24));

        initComponents();
    }

    private void initComponents() {
        JPanel scrollContainer = new JPanel();
        scrollContainer.setLayout(new BoxLayout(scrollContainer, BoxLayout.Y_AXIS));
        scrollContainer.setOpaque(false);

        // Header & Controls
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JPanel titleBlock = new JPanel(new GridLayout(2, 1, 0, 2));
        titleBlock.setOpaque(false);
        JLabel pageTitle = new JLabel("PROGRESS ANALYSIS & REPORTS");
        pageTitle.setFont(Theme.TITLE_LARGE);
        pageTitle.setForeground(Theme.TEXT_PRIMARY);
        JLabel pageSub = new JLabel("Data-driven analytics, longitudinal charts, and CSV/Print exports");
        pageSub.setFont(Theme.BODY);
        pageSub.setForeground(Theme.TEXT_MUTED);
        titleBlock.add(pageTitle);
        titleBlock.add(pageSub);
        headerPanel.add(titleBlock, BorderLayout.WEST);

        // Export & Range Actions
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setOpaque(false);

        timeRangeCombo = new JComboBox<>(new String[]{"Last 7 Days", "Last 30 Days", "Last 90 Days"});
        timeRangeCombo.setSelectedIndex(1);
        timeRangeCombo.addActionListener(e -> refreshReports());
        actions.add(timeRangeCombo);

        NeonButton exportWorkoutsBtn = new NeonButton("Export Workouts CSV", NeonButton.ButtonType.SECONDARY);
        exportWorkoutsBtn.addActionListener(e -> handleExportWorkouts());

        NeonButton exportMealsBtn = new NeonButton("Export Nutrition CSV", NeonButton.ButtonType.SECONDARY);
        exportMealsBtn.addActionListener(e -> handleExportMeals());

        NeonButton printReportBtn = new NeonButton("🖨️ Print Report", NeonButton.ButtonType.PRIMARY);
        printReportBtn.addActionListener(e -> handlePrintReport());

        actions.add(exportWorkoutsBtn);
        actions.add(exportMealsBtn);
        actions.add(printReportBtn);

        headerPanel.add(actions, BorderLayout.EAST);
        scrollContainer.add(headerPanel);
        scrollContainer.add(Box.createVerticalStrut(16));

        // Row 1 Charts: Weekly Activity (Bar) & 30-Day Calorie Trend (Line)
        JPanel row1 = new JPanel(new GridLayout(1, 2, 16, 0));
        row1.setOpaque(false);

        activityChartPanel = new ModernCard(8);
        activityChartPanel.setPreferredSize(new Dimension(0, 280));

        calorieTrendPanel = new ModernCard(8);
        calorieTrendPanel.setPreferredSize(new Dimension(0, 280));

        row1.add(activityChartPanel);
        row1.add(calorieTrendPanel);
        scrollContainer.add(row1);
        scrollContainer.add(Box.createVerticalStrut(16));

        // Row 2 Charts: Weight Progression & Training Volume Progression
        JPanel row2 = new JPanel(new GridLayout(1, 2, 16, 0));
        row2.setOpaque(false);

        weightTrendPanel = new ModernCard(8);
        weightTrendPanel.setPreferredSize(new Dimension(0, 280));

        volumeTrendPanel = new ModernCard(8);
        volumeTrendPanel.setPreferredSize(new Dimension(0, 280));

        row2.add(weightTrendPanel);
        row2.add(volumeTrendPanel);
        scrollContainer.add(row2);

        JScrollPane scrollPane = new JScrollPane(scrollContainer);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        add(scrollPane, BorderLayout.CENTER);
    }

    public void refreshReports() {
        User user = UserSession.getInstance().getCurrentUser();
        if (user == null) return;

        int days = 30;
        int idx = timeRangeCombo.getSelectedIndex();
        if (idx == 0) days = 7;
        else if (idx == 2) days = 90;

        final int finalDays = days;

        SwingWorker<ReportCharts, Void> worker = new SwingWorker<>() {
            @Override
            protected ReportCharts doInBackground() throws Exception {
                String uid = user.getId();
                JFreeChart actChart = reportService.createWeeklyActivityChart(uid);
                JFreeChart calChart = reportService.createNutritionTrendChart(uid, finalDays);
                JFreeChart wtChart = reportService.createWeightProgressChart(uid, user.getTargetWeightKg());
                JFreeChart volChart = reportService.createVolumeTrendChart(uid);
                return new ReportCharts(actChart, calChart, wtChart, volChart);
            }

            @Override
            protected void done() {
                try {
                    ReportCharts charts = get();

                    activityChartPanel.removeAll();
                    activityChartPanel.add(new ChartPanel(charts.activityChart), BorderLayout.CENTER);
                    activityChartPanel.revalidate();
                    activityChartPanel.repaint();

                    calorieTrendPanel.removeAll();
                    calorieTrendPanel.add(new ChartPanel(charts.calorieChart), BorderLayout.CENTER);
                    calorieTrendPanel.revalidate();
                    calorieTrendPanel.repaint();

                    weightTrendPanel.removeAll();
                    weightTrendPanel.add(new ChartPanel(charts.weightChart), BorderLayout.CENTER);
                    weightTrendPanel.revalidate();
                    weightTrendPanel.repaint();

                    volumeTrendPanel.removeAll();
                    volumeTrendPanel.add(new ChartPanel(charts.volumeChart), BorderLayout.CENTER);
                    volumeTrendPanel.revalidate();
                    volumeTrendPanel.repaint();

                } catch (Exception ex) {
                    System.err.println("[ReportsPanel] Refresh error: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void handleExportWorkouts() {
        User user = UserSession.getInstance().getCurrentUser();
        if (user == null) return;

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setSelectedFile(new File("pgtr_workouts_export.csv"));
        int res = fileChooser.showSaveDialog(mainFrame);
        if (res == JFileChooser.APPROVE_OPTION) {
            File target = fileChooser.getSelectedFile();
            try {
                List<Workout> workouts = workoutService.getUserWorkouts(user.getId());
                CsvExporter.exportWorkouts(workouts, target);
                JOptionPane.showMessageDialog(mainFrame, "Successfully exported " + workouts.size() +
                        " workouts to CSV:\n" + target.getAbsolutePath(), "Export Successful", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(mainFrame, "Export error: " + ex.getMessage(), "Export Failed", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleExportMeals() {
        User user = UserSession.getInstance().getCurrentUser();
        if (user == null) return;

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setSelectedFile(new File("pgtr_nutrition_export.csv"));
        int res = fileChooser.showSaveDialog(mainFrame);
        if (res == JFileChooser.APPROVE_OPTION) {
            File target = fileChooser.getSelectedFile();
            try {
                List<Meal> meals = nutritionService.getMealsForDate(user.getId(), java.time.LocalDate.now());
                CsvExporter.exportMeals(meals, target);
                JOptionPane.showMessageDialog(mainFrame, "Successfully exported meals to CSV:\n" + target.getAbsolutePath(), "Export Successful", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(mainFrame, "Export error: " + ex.getMessage(), "Export Failed", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handlePrintReport() {
        User user = UserSession.getInstance().getCurrentUser();
        if (user == null) return;

        String reportText = reportService.generateProgressReport(user);

        JTextArea area = new JTextArea(reportText);
        area.setFont(new Font("Consolas", Font.PLAIN, 12));
        area.setBackground(Theme.BG_DARK);
        area.setForeground(Theme.TEXT_PRIMARY);
        area.setCaretPosition(0);
        area.setEditable(false);

        JScrollPane sp = new JScrollPane(area);
        sp.setPreferredSize(new Dimension(600, 480));

        int choice = JOptionPane.showOptionDialog(mainFrame, sp,
                "Printable Progress Report — " + user.getFullName(),
                JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null,
                new String[]{"Print Document", "Close"}, "Print Document");

        if (choice == 0) {
            try {
                PrinterJob job = PrinterJob.getPrinterJob();
                if (job.printDialog()) {
                    area.print();
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(mainFrame, "Print error: " + ex.getMessage(), "Printer Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private static class ReportCharts {
        final JFreeChart activityChart;
        final JFreeChart calorieChart;
        final JFreeChart weightChart;
        final JFreeChart volumeChart;

        public ReportCharts(JFreeChart activityChart, JFreeChart calorieChart,
                            JFreeChart weightChart, JFreeChart volumeChart) {
            this.activityChart = activityChart;
            this.calorieChart = calorieChart;
            this.weightChart = weightChart;
            this.volumeChart = volumeChart;
        }
    }
}
