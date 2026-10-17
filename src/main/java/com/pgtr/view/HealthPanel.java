package com.pgtr.view;

import com.pgtr.model.BodyMeasurement;
import com.pgtr.model.User;
import com.pgtr.service.HealthService;
import com.pgtr.service.ReportService;
import com.pgtr.service.UserSession;
import com.pgtr.util.DateUtil;
import com.pgtr.util.Theme;
import com.pgtr.view.components.DarkTable;
import com.pgtr.view.components.ModernCard;
import com.pgtr.view.components.NeonButton;
import com.pgtr.view.components.StatCard;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Health and Body Measurement Tracking module.
 * Features anthropometric circumferences (waist, chest, arms, thighs, hips, neck),
 * scientific BMI calculation with screening disclaimers, date-to-date comparison diff engine,
 * and historical weight progress charts.
 */
public class HealthPanel extends JPanel {

    private final MainFrame mainFrame;
    private final HealthService healthService;
    private final ReportService reportService;

    // Stat Cards
    private StatCard currentWeightCard;
    private StatCard bmiCard;
    private StatCard waistCard;
    private StatCard chestCard;

    // Comparison Tool Cards
    private JComboBox<BodyMeasurement> compareDateACombo;
    private JComboBox<BodyMeasurement> compareDateBCombo;
    private JLabel compWeightDiff;
    private JLabel compBmiDiff;
    private JLabel compWaistDiff;
    private JLabel compChestDiff;
    private JLabel compArmsDiff;

    // Charts & Tables
    private JPanel weightChartPanel;
    private DefaultTableModel tableModel;
    private DarkTable measurementTable;
    private List<BodyMeasurement> currentMeasurements = new ArrayList<>();

    public HealthPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.healthService = new HealthService();
        this.reportService = new ReportService();

        setOpaque(false);
        setLayout(new BorderLayout(0, 16));
        setBorder(BorderFactory.createEmptyBorder(20, 24, 24, 24));

        initComponents();
    }

    private void initComponents() {
        JPanel mainContainer = new JPanel();
        mainContainer.setLayout(new BoxLayout(mainContainer, BoxLayout.Y_AXIS));
        mainContainer.setOpaque(false);

        // 1. Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JPanel titleBlock = new JPanel(new GridLayout(2, 1, 0, 2));
        titleBlock.setOpaque(false);
        JLabel pageTitle = new JLabel("HEALTH & BODY MEASUREMENTS");
        pageTitle.setFont(Theme.TITLE_LARGE);
        pageTitle.setForeground(Theme.TEXT_PRIMARY);
        JLabel pageSub = new JLabel("Track circumference improvements, weight trajectory, and screening BMI");
        pageSub.setFont(Theme.BODY);
        pageSub.setForeground(Theme.TEXT_MUTED);
        titleBlock.add(pageTitle);
        titleBlock.add(pageSub);
        headerPanel.add(titleBlock, BorderLayout.WEST);

        NeonButton logMeasureBtn = new NeonButton("+ Record Measurement", NeonButton.ButtonType.PRIMARY);
        logMeasureBtn.addActionListener(e -> showRecordDialog(null));
        headerPanel.add(logMeasureBtn, BorderLayout.EAST);

        mainContainer.add(headerPanel);
        mainContainer.add(Box.createVerticalStrut(16));

        // 2. Stat Cards Grid
        JPanel statsGrid = new JPanel(new GridLayout(1, 4, 16, 0));
        statsGrid.setOpaque(false);

        currentWeightCard = new StatCard("⚖️", "LATEST WEIGHT", "-- kg", "Measured recently", Theme.PRIMARY);
        bmiCard = new StatCard("🩺", "BODY MASS INDEX", "--", "Screening estimate", Theme.WATER_CYAN);
        waistCard = new StatCard("📏", "WAIST CIRCUMFERENCE", "-- cm", "Core midpoint", Theme.WARNING);
        chestCard = new StatCard("📐", "CHEST CIRCUMFERENCE", "-- cm", "Pectoral & Lat spread", Theme.PROTEIN_BLUE);

        statsGrid.add(currentWeightCard);
        statsGrid.add(bmiCard);
        statsGrid.add(waistCard);
        statsGrid.add(chestCard);

        mainContainer.add(statsGrid);
        mainContainer.add(Box.createVerticalStrut(16));

        // 3. BMI Screening Medical Disclaimer Alert
        ModernCard disclaimerCard = new ModernCard(12);
        disclaimerCard.setBackgroundColor(new Color(0x1E, 0x22, 0x28));
        disclaimerCard.setBorderColor(Theme.BORDER);
        disclaimerCard.setLayout(new BorderLayout(8, 0));

        JLabel infoIcon = new JLabel("ℹ️ ");
        infoIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));
        disclaimerCard.add(infoIcon, BorderLayout.WEST);

        JLabel disclaimerText = new JLabel("<html><body style='color:#94A3B8; font-size:12px;'>" +
                "<b style='color:#F8FAFC;'>Clinical & Screening Notice:</b> Body Mass Index (BMI) is a general screening indicator based strictly on height and weight. " +
                "It does not differentiate lean muscle hypertrophy from adipose tissue mass. For athletic and strength training individuals, progress is best evaluated via circumference changes and lifting performance." +
                "</body></html>");
        disclaimerCard.add(disclaimerText, BorderLayout.CENTER);
        mainContainer.add(disclaimerCard);
        mainContainer.add(Box.createVerticalStrut(16));

        // 4. Split Row: Weight Trend Chart on Left, Date-to-Date Comparison Tool on Right
        JPanel middleSplit = new JPanel(new GridLayout(1, 2, 16, 0));
        middleSplit.setOpaque(false);

        weightChartPanel = new ModernCard(8);
        weightChartPanel.setPreferredSize(new Dimension(0, 270));
        middleSplit.add(weightChartPanel);

        ModernCard compCard = createComparisonCard();
        middleSplit.add(compCard);

        mainContainer.add(middleSplit);
        mainContainer.add(Box.createVerticalStrut(16));

        // 5. Measurements History Table
        ModernCard tableCard = new ModernCard(16);
        tableCard.setLayout(new BorderLayout(0, 10));

        JPanel tableHeader = new JPanel(new BorderLayout());
        tableHeader.setOpaque(false);
        JLabel tableTitle = new JLabel("BODY MEASUREMENTS LOG HISTORY");
        tableTitle.setFont(Theme.SUBTITLE);
        tableTitle.setForeground(Theme.TEXT_PRIMARY);
        tableHeader.add(tableTitle, BorderLayout.WEST);

        JPanel tableBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        tableBtns.setOpaque(false);
        NeonButton deleteBtn = new NeonButton("Delete Log", NeonButton.ButtonType.DANGER);
        deleteBtn.addActionListener(e -> handleDeleteMeasurement());
        tableBtns.add(deleteBtn);
        tableHeader.add(tableBtns, BorderLayout.EAST);
        tableCard.add(tableHeader, BorderLayout.NORTH);

        tableModel = new DefaultTableModel(new String[]{
                "Date", "Weight (kg)", "Height (cm)", "BMI", "Category", "Waist (cm)", "Chest (cm)", "Arms (cm)", "Thighs (cm)", "Notes"
        }, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        measurementTable = new DarkTable(tableModel);
        tableCard.add(DarkTable.createScrollPane(measurementTable), BorderLayout.CENTER);

        mainContainer.add(tableCard);

        JScrollPane scrollPane = new JScrollPane(mainContainer);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        add(scrollPane, BorderLayout.CENTER);
    }

    private ModernCard createComparisonCard() {
        ModernCard card = new ModernCard(16);
        card.setLayout(new BorderLayout(0, 10));

        JLabel title = new JLabel("BODY RECOMPOSITION COMPARISON");
        title.setFont(Theme.SUBTITLE);
        title.setForeground(Theme.TEXT_PRIMARY);
        card.add(title, BorderLayout.NORTH);

        // Selectors row
        JPanel selectors = new JPanel(new GridLayout(1, 2, 10, 0));
        selectors.setOpaque(false);

        compareDateACombo = new JComboBox<>();
        compareDateBCombo = new JComboBox<>();

        selectors.add(wrapField("BASELINE DATE (A)", compareDateACombo));
        selectors.add(wrapField("COMPARISON DATE (B)", compareDateBCombo));
        card.add(selectors, BorderLayout.CENTER);

        // Differences display grid
        JPanel diffsGrid = new JPanel(new GridLayout(1, 5, 8, 0));
        diffsGrid.setOpaque(false);

        compWeightDiff = createDiffBadge("Weight", "--");
        compBmiDiff = createDiffBadge("BMI", "--");
        compWaistDiff = createDiffBadge("Waist", "--");
        compChestDiff = createDiffBadge("Chest", "--");
        compArmsDiff = createDiffBadge("Arms", "--");

        diffsGrid.add(compWeightDiff);
        diffsGrid.add(compBmiDiff);
        diffsGrid.add(compWaistDiff);
        diffsGrid.add(compChestDiff);
        diffsGrid.add(compArmsDiff);
        card.add(diffsGrid, BorderLayout.SOUTH);

        compareDateACombo.addActionListener(e -> updateComparison());
        compareDateBCombo.addActionListener(e -> updateComparison());

        return card;
    }

    private JLabel createDiffBadge(String title, String initialVal) {
        JLabel l = new JLabel("<html><center style='color:#94A3B8; font-size:10px;'>" + title + "<br>" +
                "<b style='color:#F8FAFC; font-size:14px;'>" + initialVal + "</b></center></html>", SwingConstants.CENTER);
        l.setBorder(BorderFactory.createLineBorder(Theme.BORDER));
        return l;
    }

    public void refreshHealth() {
        User user = UserSession.getInstance().getCurrentUser();
        if (user == null) return;

        SwingWorker<List<BodyMeasurement>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<BodyMeasurement> doInBackground() throws Exception {
                return healthService.getMeasurementHistory(user.getId());
            }

            @Override
            protected void done() {
                try {
                    currentMeasurements = get();

                    // Update Table
                    tableModel.setRowCount(0);
                    compareDateACombo.removeAllItems();
                    compareDateBCombo.removeAllItems();

                    for (BodyMeasurement m : currentMeasurements) {
                        tableModel.addRow(new Object[]{
                                DateUtil.formatDate(m.getLogDate()),
                                String.format("%.1f", m.getWeightKg()),
                                String.format("%.1f", m.getHeightCm()),
                                String.format("%.1f", m.getBmi()),
                                m.getBmiCategory(),
                                m.getWaistCm() > 0 ? String.format("%.1f", m.getWaistCm()) : "--",
                                m.getChestCm() > 0 ? String.format("%.1f", m.getChestCm()) : "--",
                                m.getArmsCm() > 0 ? String.format("%.1f", m.getArmsCm()) : "--",
                                m.getThighsCm() > 0 ? String.format("%.1f", m.getThighsCm()) : "--",
                                m.getNotes() != null ? m.getNotes() : ""
                        });

                        compareDateACombo.addItem(m);
                        compareDateBCombo.addItem(m);
                    }

                    // Update Top Cards with Latest
                    if (!currentMeasurements.isEmpty()) {
                        BodyMeasurement latest = currentMeasurements.get(0);
                        currentWeightCard.setValue(String.format("%.1f kg", latest.getWeightKg()));
                        bmiCard.setValue(String.format("%.1f", latest.getBmi()));
                        bmiCard.setSubtext(latest.getBmiCategory());
                        waistCard.setValue(latest.getWaistCm() > 0 ? String.format("%.1f cm", latest.getWaistCm()) : "--");
                        chestCard.setValue(latest.getChestCm() > 0 ? String.format("%.1f cm", latest.getChestCm()) : "--");

                        if (currentMeasurements.size() >= 2) {
                            compareDateACombo.setSelectedIndex(currentMeasurements.size() - 1); // oldest
                            compareDateBCombo.setSelectedIndex(0); // newest
                            updateComparison();
                        }
                    }

                    // Update Weight Progression Chart
                    JFreeChart weightChart = reportService.createWeightProgressChart(user.getId(), user.getTargetWeightKg());
                    weightChartPanel.removeAll();
                    weightChartPanel.add(new ChartPanel(weightChart), BorderLayout.CENTER);
                    weightChartPanel.revalidate();
                    weightChartPanel.repaint();

                } catch (Exception ex) {
                    System.err.println("[HealthPanel] Refresh error: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void updateComparison() {
        BodyMeasurement a = (BodyMeasurement) compareDateACombo.getSelectedItem();
        BodyMeasurement b = (BodyMeasurement) compareDateBCombo.getSelectedItem();
        if (a == null || b == null) return;

        var comp = healthService.compare(a, b);
        compWeightDiff.setText(formatDiffBadge("Weight", comp.weightDiff, "kg"));
        compBmiDiff.setText(formatDiffBadge("BMI", comp.bmiDiff, ""));
        compWaistDiff.setText(formatDiffBadge("Waist", comp.waistDiff, "cm"));
        compChestDiff.setText(formatDiffBadge("Chest", comp.chestDiff, "cm"));
        compArmsDiff.setText(formatDiffBadge("Arms", comp.armsDiff, "cm"));
    }

    private String formatDiffBadge(String title, double diff, String unit) {
        String sign = diff > 0 ? "+" : "";
        String color = diff < 0 ? "#00E676" : (diff > 0 ? "#FFAB00" : "#94A3B8");
        // For chest/arms muscle gain, positive is green!
        if (title.equalsIgnoreCase("Chest") || title.equalsIgnoreCase("Arms")) {
            color = diff > 0 ? "#00E676" : (diff < 0 ? "#FF5252" : "#94A3B8");
        }
        return "<html><center style='color:#94A3B8; font-size:10px;'>" + title + "<br>" +
                "<b style='color:" + color + "; font-size:14px;'>" + sign + diff + " " + unit + "</b></center></html>";
    }

    private void handleDeleteMeasurement() {
        int row = measurementTable.getSelectedRow();
        if (row < 0 || row >= currentMeasurements.size()) {
            JOptionPane.showMessageDialog(this, "Please select a measurement log to delete.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        BodyMeasurement m = currentMeasurements.get(row);
        int confirm = JOptionPane.showConfirmDialog(this,
                "Delete measurement log from " + DateUtil.formatDate(m.getLogDate()) + "?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                healthService.deleteMeasurement(m.getId(), m.getUserId());
                refreshHealth();
                mainFrame.getDashboardPanel().refreshDashboard();
                JOptionPane.showMessageDialog(this, "Measurement log deleted.", "Deleted", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error deleting measurement: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showRecordDialog(BodyMeasurement existing) {
        User user = UserSession.getInstance().getCurrentUser();
        if (user == null) return;

        JDialog dialog = new JDialog(mainFrame, existing == null ? "Log Body Measurements" : "Edit Measurements", true);
        dialog.setSize(560, 520);
        dialog.setLocationRelativeTo(mainFrame);
        dialog.getContentPane().setBackground(Theme.BG_DARK);
        dialog.setLayout(new BorderLayout(14, 14));

        ModernCard container = new ModernCard(20);
        container.setLayout(new BorderLayout(0, 12));

        JPanel form = new JPanel(new GridLayout(0, 2, 12, 10));
        form.setOpaque(false);

        JTextField dateField = new JTextField(existing != null ? existing.getLogDate().toString() : LocalDate.now().toString());
        JSpinner weightSpinner = new JSpinner(new SpinnerNumberModel(existing != null ? existing.getWeightKg() : user.getCurrentWeightKg(), 25.0, 300.0, 0.2));
        JSpinner heightSpinner = new JSpinner(new SpinnerNumberModel(existing != null ? existing.getHeightCm() : user.getHeightCm(), 80.0, 250.0, 0.5));
        JSpinner waistSpinner = new JSpinner(new SpinnerNumberModel(existing != null ? existing.getWaistCm() : 82.0, 0.0, 200.0, 0.5));
        JSpinner chestSpinner = new JSpinner(new SpinnerNumberModel(existing != null ? existing.getChestCm() : 100.0, 0.0, 200.0, 0.5));
        JSpinner armsSpinner = new JSpinner(new SpinnerNumberModel(existing != null ? existing.getArmsCm() : 36.0, 0.0, 80.0, 0.2));
        JSpinner thighsSpinner = new JSpinner(new SpinnerNumberModel(existing != null ? existing.getThighsCm() : 58.0, 0.0, 120.0, 0.5));
        JTextField notesField = new JTextField(existing != null ? existing.getNotes() : "");

        form.add(wrapField("DATE (YYYY-MM-DD)", dateField));
        form.add(wrapField("BODY WEIGHT (KG)", weightSpinner));
        form.add(wrapField("HEIGHT (CM)", heightSpinner));
        form.add(wrapField("WAIST CIRCUMFERENCE (CM)", waistSpinner));
        form.add(wrapField("CHEST CIRCUMFERENCE (CM)", chestSpinner));
        form.add(wrapField("ARMS / BICEPS (CM)", armsSpinner));
        form.add(wrapField("THIGHS (CM)", thighsSpinner));
        form.add(wrapField("NOTES / PROGRESS", notesField));

        container.add(form, BorderLayout.CENTER);

        JPanel btnBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnBar.setOpaque(false);

        NeonButton cancel = new NeonButton("Cancel", NeonButton.ButtonType.SECONDARY);
        cancel.addActionListener(e -> dialog.dispose());

        NeonButton save = new NeonButton("Save Measurements", NeonButton.ButtonType.PRIMARY);
        save.addActionListener(e -> {
            try {
                LocalDate date = LocalDate.parse(dateField.getText().trim());
                double weight = ((Number) weightSpinner.getValue()).doubleValue();
                double height = ((Number) heightSpinner.getValue()).doubleValue();
                double waist = ((Number) waistSpinner.getValue()).doubleValue();
                double chest = ((Number) chestSpinner.getValue()).doubleValue();
                double arms = ((Number) armsSpinner.getValue()).doubleValue();
                double thighs = ((Number) thighsSpinner.getValue()).doubleValue();

                BodyMeasurement m = new BodyMeasurement(null, user.getId(), date, weight, height, waist, chest, arms, thighs, 0, 0, notesField.getText().trim());
                healthService.logMeasurement(m);

                dialog.dispose();
                refreshHealth();
                mainFrame.getDashboardPanel().refreshDashboard();
                JOptionPane.showMessageDialog(mainFrame, "Measurements logged! Calculated BMI: " +
                        String.format("%.1f (%s)", m.getBmi(), m.getBmiCategory()), "Saved", JOptionPane.INFORMATION_MESSAGE);

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Error: " + ex.getMessage(), "Validation Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnBar.add(cancel);
        btnBar.add(save);
        container.add(btnBar, BorderLayout.SOUTH);
        dialog.add(container, BorderLayout.CENTER);

        dialog.setVisible(true);
    }

    private JPanel wrapField(String label, JComponent comp) {
        JPanel p = new JPanel(new BorderLayout(0, 4));
        p.setOpaque(false);
        JLabel l = new JLabel(label);
        l.setFont(Theme.SMALL);
        l.setForeground(Theme.TEXT_MUTED);
        p.add(l, BorderLayout.NORTH);
        p.add(comp, BorderLayout.CENTER);
        return p;
    }
}
