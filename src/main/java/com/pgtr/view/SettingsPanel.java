package com.pgtr.view;

import com.pgtr.database.DatabaseManager;
import com.pgtr.model.ActivityLevel;
import com.pgtr.model.FitnessGoalType;
import com.pgtr.model.User;
import com.pgtr.service.AuthService;
import com.pgtr.service.UserSession;
import com.pgtr.util.Theme;
import com.pgtr.view.components.ModernCard;
import com.pgtr.view.components.NeonButton;

import javax.swing.*;
import java.awt.*;

/**
 * Settings & Profile Management module.
 * Allows updating profile parameters, password changes, and configuring Supabase PostgreSQL credentials.
 */
public class SettingsPanel extends JPanel {

    private final MainFrame mainFrame;
    private final AuthService authService;

    // Profile Fields
    private JTextField nameField;
    private JTextField emailField;
    private JSpinner ageSpinner;
    private JSpinner heightSpinner;
    private JSpinner weightSpinner;
    private JSpinner targetWeightSpinner;
    private JComboBox<FitnessGoalType> goalCombo;
    private JComboBox<ActivityLevel> activityCombo;
    private JSpinner calorieTargetSpinner;
    private JSpinner proteinTargetSpinner;
    private JSpinner waterTargetSpinner;
    private JSpinner sleepTargetSpinner;

    // Database Status
    private JLabel dbStatusLabel;
    private JTextField dbUrlField;
    private JTextField dbUserField;
    private JPasswordField dbPassField;

    public SettingsPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.authService = new AuthService();

        setOpaque(false);
        setLayout(new BorderLayout(0, 16));
        setBorder(BorderFactory.createEmptyBorder(20, 24, 24, 24));

        initComponents();
    }

    private void initComponents() {
        JPanel scrollContainer = new JPanel();
        scrollContainer.setLayout(new BoxLayout(scrollContainer, BoxLayout.Y_AXIS));
        scrollContainer.setOpaque(false);

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        JPanel titleBlock = new JPanel(new GridLayout(2, 1, 0, 2));
        titleBlock.setOpaque(false);
        JLabel pageTitle = new JLabel("SETTINGS & PROFILE MANAGEMENT");
        pageTitle.setFont(Theme.TITLE_LARGE);
        pageTitle.setForeground(Theme.TEXT_PRIMARY);
        JLabel pageSub = new JLabel("Manage your physical metrics, macronutrient targets, and database connection");
        pageSub.setFont(Theme.BODY);
        pageSub.setForeground(Theme.TEXT_MUTED);
        titleBlock.add(pageTitle);
        titleBlock.add(pageSub);
        headerPanel.add(titleBlock, BorderLayout.WEST);
        scrollContainer.add(headerPanel);
        scrollContainer.add(Box.createVerticalStrut(16));

        // 1. Personal Profile Card
        ModernCard profileCard = createProfileCard();
        scrollContainer.add(profileCard);
        scrollContainer.add(Box.createVerticalStrut(16));

        // 2. Change Password Card
        ModernCard passwordCard = createPasswordCard();
        scrollContainer.add(passwordCard);
        scrollContainer.add(Box.createVerticalStrut(16));

        // 3. Database Connection Configuration Card
        ModernCard dbCard = createDatabaseConfigCard();
        scrollContainer.add(dbCard);

        JScrollPane scrollPane = new JScrollPane(scrollContainer);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        add(scrollPane, BorderLayout.CENTER);
    }

    private ModernCard createProfileCard() {
        ModernCard card = new ModernCard(20);
        card.setLayout(new BorderLayout(0, 14));

        JLabel title = new JLabel("USER PROFILE & DAILY TARGETS");
        title.setFont(Theme.SUBTITLE);
        title.setForeground(Theme.PRIMARY);
        card.add(title, BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(0, 3, 14, 12));
        grid.setOpaque(false);

        nameField = new JTextField();
        emailField = new JTextField();
        emailField.setEditable(false); // Email is unique primary identifier

        ageSpinner = new JSpinner(new SpinnerNumberModel(22, 12, 100, 1));
        heightSpinner = new JSpinner(new SpinnerNumberModel(175.0, 80.0, 250.0, 0.5));
        weightSpinner = new JSpinner(new SpinnerNumberModel(75.0, 25.0, 300.0, 0.5));
        targetWeightSpinner = new JSpinner(new SpinnerNumberModel(80.0, 25.0, 300.0, 0.5));

        goalCombo = new JComboBox<>(FitnessGoalType.values());
        activityCombo = new JComboBox<>(ActivityLevel.values());

        calorieTargetSpinner = new JSpinner(new SpinnerNumberModel(2400, 800, 8000, 50));
        proteinTargetSpinner = new JSpinner(new SpinnerNumberModel(150, 20, 500, 5));
        waterTargetSpinner = new JSpinner(new SpinnerNumberModel(3000, 500, 10000, 250));
        sleepTargetSpinner = new JSpinner(new SpinnerNumberModel(8.0, 4.0, 14.0, 0.5));

        grid.add(wrapField("FULL NAME", nameField));
        grid.add(wrapField("EMAIL (ACCOUNT ID)", emailField));
        grid.add(wrapField("AGE (YEARS)", ageSpinner));

        grid.add(wrapField("HEIGHT (CM)", heightSpinner));
        grid.add(wrapField("CURRENT WEIGHT (KG)", weightSpinner));
        grid.add(wrapField("TARGET WEIGHT (KG)", targetWeightSpinner));

        grid.add(wrapField("PRIMARY FITNESS GOAL", goalCombo));
        grid.add(wrapField("DAILY ACTIVITY LEVEL", activityCombo));
        grid.add(wrapField("DAILY CALORIE TARGET (KCAL)", calorieTargetSpinner));

        grid.add(wrapField("DAILY PROTEIN GOAL (G)", proteinTargetSpinner));
        grid.add(wrapField("DAILY WATER TARGET (ML)", waterTargetSpinner));
        grid.add(wrapField("DAILY SLEEP TARGET (HRS)", sleepTargetSpinner));

        card.add(grid, BorderLayout.CENTER);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnRow.setOpaque(false);

        NeonButton autoCalcBtn = new NeonButton("Auto-Recalculate Suggested Targets", NeonButton.ButtonType.SECONDARY);
        autoCalcBtn.addActionListener(e -> handleAutoCalculate());

        NeonButton saveProfileBtn = new NeonButton("Save Profile Changes", NeonButton.ButtonType.PRIMARY);
        saveProfileBtn.addActionListener(e -> handleSaveProfile());

        btnRow.add(autoCalcBtn);
        btnRow.add(saveProfileBtn);
        card.add(btnRow, BorderLayout.SOUTH);

        return card;
    }

    private ModernCard createPasswordCard() {
        ModernCard card = new ModernCard(20);
        card.setLayout(new BorderLayout(0, 12));

        JLabel title = new JLabel("CHANGE PASSWORD");
        title.setFont(Theme.SUBTITLE);
        title.setForeground(Theme.TEXT_PRIMARY);
        card.add(title, BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(1, 3, 14, 0));
        grid.setOpaque(false);

        JPasswordField currentPass = new JPasswordField();
        JPasswordField newPass = new JPasswordField();
        JPasswordField confirmPass = new JPasswordField();

        grid.add(wrapField("CURRENT PASSWORD", currentPass));
        grid.add(wrapField("NEW PASSWORD (MIN 6 CHARS)", newPass));
        grid.add(wrapField("CONFIRM NEW PASSWORD", confirmPass));
        card.add(grid, BorderLayout.CENTER);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnRow.setOpaque(false);

        NeonButton updatePassBtn = new NeonButton("Update Password", NeonButton.ButtonType.SECONDARY);
        updatePassBtn.addActionListener(e -> {
            User user = UserSession.getInstance().getCurrentUser();
            if (user == null) return;

            String oldP = new String(currentPass.getPassword());
            String newP = new String(newPass.getPassword());
            String confP = new String(confirmPass.getPassword());

            if (!newP.equals(confP)) {
                JOptionPane.showMessageDialog(this, "New password and confirmation do not match.", "Mismatch", JOptionPane.WARNING_MESSAGE);
                return;
            }

            try {
                authService.changePassword(user.getId(), oldP, newP);
                currentPass.setText("");
                newPass.setText("");
                confirmPass.setText("");
                JOptionPane.showMessageDialog(this, "Password updated successfully with BCrypt hashing!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnRow.add(updatePassBtn);
        card.add(btnRow, BorderLayout.SOUTH);

        return card;
    }

    private ModernCard createDatabaseConfigCard() {
        ModernCard card = new ModernCard(20);
        card.setLayout(new BorderLayout(0, 12));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        JLabel title = new JLabel("DATABASE CONNECTION & SUPABASE SETTINGS");
        title.setFont(Theme.SUBTITLE);
        title.setForeground(Theme.PRIMARY);
        top.add(title, BorderLayout.WEST);

        dbStatusLabel = new JLabel(DatabaseManager.getInstance().getStatusMessage());
        dbStatusLabel.setFont(Theme.SMALL);
        dbStatusLabel.setForeground(DatabaseManager.getInstance().isConnectedToSupabase() ? Theme.PRIMARY : Theme.WARNING);
        top.add(dbStatusLabel, BorderLayout.EAST);
        card.add(top, BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(0, 1, 0, 8));
        grid.setOpaque(false);

        dbUrlField = new JTextField(DatabaseManager.getInstance().getDbUrl() != null ? DatabaseManager.getInstance().getDbUrl() : "jdbc:postgresql://db.YOUR_PROJECT_REF.supabase.co:5432/postgres?sslmode=require");
        dbUserField = new JTextField(DatabaseManager.getInstance().getDbUser() != null ? DatabaseManager.getInstance().getDbUser() : "postgres");
        dbPassField = new JPasswordField();

        grid.add(wrapField("SUPABASE JDBC CONNECTION URL", dbUrlField));
        grid.add(wrapField("DATABASE USERNAME", dbUserField));
        grid.add(wrapField("DATABASE PASSWORD", dbPassField));

        JLabel info = new JLabel("<html><body style='color:#94A3B8; font-size:11px;'>" +
                "<b>Security & Architecture Notice:</b> Supabase credentials should be provided via environment variables " +
                "(<code>PGTR_DB_URL</code>, <code>PGTR_DB_USER</code>, <code>PGTR_DB_PASSWORD</code>) or saved locally in <code>db.properties</code>. " +
                "If Supabase is not reachable, the system seamlessly operates via the local storage engine so all features remain testable." +
                "</body></html>");
        grid.add(info);
        card.add(grid, BorderLayout.CENTER);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnRow.setOpaque(false);

        NeonButton testBtn = new NeonButton("Connect & Test Supabase", NeonButton.ButtonType.PRIMARY);
        testBtn.addActionListener(e -> {
            String url = dbUrlField.getText().trim();
            String user = dbUserField.getText().trim();
            String pass = new String(dbPassField.getPassword());

            boolean connected = DatabaseManager.getInstance().updateCredentials(url, user, pass);
            dbStatusLabel.setText(DatabaseManager.getInstance().getStatusMessage());
            dbStatusLabel.setForeground(connected ? Theme.PRIMARY : Theme.WARNING);
            mainFrame.updateDatabaseStatus();

            if (connected) {
                JOptionPane.showMessageDialog(this, "Successfully connected to Supabase PostgreSQL cloud database!", "Connected", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Could not reach remote Supabase database. Falling back to local storage engine.", "Connection Notice", JOptionPane.WARNING_MESSAGE);
            }
        });

        btnRow.add(testBtn);
        card.add(btnRow, BorderLayout.SOUTH);

        return card;
    }

    public void loadProfile() {
        User user = UserSession.getInstance().getCurrentUser();
        if (user == null) return;

        nameField.setText(user.getFullName());
        emailField.setText(user.getEmail());
        ageSpinner.setValue(user.getAge());
        heightSpinner.setValue(user.getHeightCm());
        weightSpinner.setValue(user.getCurrentWeightKg());
        targetWeightSpinner.setValue(user.getTargetWeightKg());
        goalCombo.setSelectedItem(user.getFitnessGoal());
        activityCombo.setSelectedItem(user.getActivityLevel());
        calorieTargetSpinner.setValue(user.getDailyCalorieTarget());
        proteinTargetSpinner.setValue(user.getDailyProteinTarget());
        waterTargetSpinner.setValue(user.getDailyWaterTarget());
        sleepTargetSpinner.setValue(user.getDailySleepTarget());

        dbStatusLabel.setText(DatabaseManager.getInstance().getStatusMessage());
        dbStatusLabel.setForeground(DatabaseManager.getInstance().isConnectedToSupabase() ? Theme.PRIMARY : Theme.WARNING);
    }

    private void handleAutoCalculate() {
        User temp = new User();
        temp.setCurrentWeightKg(((Number) weightSpinner.getValue()).doubleValue());
        temp.setHeightCm(((Number) heightSpinner.getValue()).doubleValue());
        temp.setAge((int) ageSpinner.getValue());
        temp.setFitnessGoal((FitnessGoalType) goalCombo.getSelectedItem());
        temp.setActivityLevel((ActivityLevel) activityCombo.getSelectedItem());

        int suggCal = temp.calculateSuggestedCalories();
        int suggProt = temp.calculateSuggestedProtein();

        calorieTargetSpinner.setValue(suggCal);
        proteinTargetSpinner.setValue(suggProt);

        JOptionPane.showMessageDialog(this,
                "Scientific targets calculated using Mifflin-St Jeor equation:\n" +
                        "• Suggested Calories: " + suggCal + " kcal\n" +
                        "• Suggested Protein: " + suggProt + " g",
                "Targets Recommended", JOptionPane.INFORMATION_MESSAGE);
    }

    private void handleSaveProfile() {
        User user = UserSession.getInstance().getCurrentUser();
        if (user == null) return;

        try {
            user.setFullName(nameField.getText().trim());
            user.setAge((int) ageSpinner.getValue());
            user.setHeightCm(((Number) heightSpinner.getValue()).doubleValue());
            user.setCurrentWeightKg(((Number) weightSpinner.getValue()).doubleValue());
            user.setTargetWeightKg(((Number) targetWeightSpinner.getValue()).doubleValue());
            user.setFitnessGoal((FitnessGoalType) goalCombo.getSelectedItem());
            user.setActivityLevel((ActivityLevel) activityCombo.getSelectedItem());
            user.setDailyCalorieTarget((int) calorieTargetSpinner.getValue());
            user.setDailyProteinTarget((int) proteinTargetSpinner.getValue());
            user.setDailyWaterTarget((int) waterTargetSpinner.getValue());
            user.setDailySleepTarget(((Number) sleepTargetSpinner.getValue()).doubleValue());

            authService.updateProfile(user);
            mainFrame.updateProfileBadge(user);
            mainFrame.getDashboardPanel().refreshDashboard();
            JOptionPane.showMessageDialog(this, "Profile updated successfully!", "Saved", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error saving profile: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
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
