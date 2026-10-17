package com.pgtr.view;

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
 * Modern authentication window with seamless Sign In and Register cards.
 */
public class LoginFrame extends JFrame {

    private final AuthService authService;
    private final CardLayout cardLayout;
    private final JPanel cardsPanel;

    // Sign In Fields
    private JTextField loginEmailField;
    private JPasswordField loginPasswordField;
    private JLabel loginErrorLabel;

    // Register Fields
    private JTextField regNameField;
    private JTextField regEmailField;
    private JPasswordField regPasswordField;
    private JSpinner regAgeSpinner;
    private JSpinner regHeightSpinner;
    private JSpinner regWeightSpinner;
    private JSpinner regTargetWeightSpinner;
    private JComboBox<FitnessGoalType> regGoalCombo;
    private JComboBox<ActivityLevel> regActivityCombo;
    private JLabel regErrorLabel;

    public LoginFrame() {
        super("Personal Gain Tracking System — Authentication");
        this.authService = new AuthService();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(960, 680);
        setMinimumSize(new Dimension(850, 600));
        setLocationRelativeTo(null);
        getContentPane().setBackground(Theme.BG_DARK);
        setLayout(new BorderLayout());

        // Left Branding Banner
        JPanel brandBanner = createBrandingPanel();
        add(brandBanner, BorderLayout.WEST);

        // Right Auth Container (CardLayout: LOGIN vs REGISTER)
        cardLayout = new CardLayout();
        cardsPanel = new JPanel(cardLayout);
        cardsPanel.setOpaque(false);

        cardsPanel.add(createLoginCard(), "LOGIN");
        cardsPanel.add(createRegisterCard(), "REGISTER");

        JPanel centerWrapper = new JPanel(new GridBagLayout());
        centerWrapper.setOpaque(false);
        centerWrapper.add(cardsPanel);

        add(centerWrapper, BorderLayout.CENTER);
    }

    private JPanel createBrandingPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setPreferredSize(new Dimension(360, 0));
        panel.setBackground(Theme.BG_SIDEBAR);
        panel.setBorder(BorderFactory.createEmptyBorder(40, 36, 40, 36));

        JPanel top = new JPanel(new GridLayout(0, 1, 0, 8));
        top.setOpaque(false);

        JLabel logoBadge = new JLabel("⚡ PGTR");
        logoBadge.setFont(new Font("Segoe UI", Font.BOLD, 32));
        logoBadge.setForeground(Theme.PRIMARY);
        top.add(logoBadge);

        JLabel appName = new JLabel("Personal Gain");
        appName.setFont(new Font("Segoe UI", Font.BOLD, 22));
        appName.setForeground(Theme.TEXT_PRIMARY);
        top.add(appName);

        JLabel appSub = new JLabel("Tracking System");
        appSub.setFont(new Font("Segoe UI", Font.BOLD, 22));
        appSub.setForeground(Theme.TEXT_PRIMARY);
        top.add(appSub);

        JLabel tag = new JLabel("<html><body style='width: 250px; color: #94A3B8; font-size: 11px;'>" +
                "Centralized high-performance fitness, gym workouts, nutrition, body measurements, " +
                "and lifestyle habit engineering dashboard.</body></html>");
        top.add(tag);

        panel.add(top, BorderLayout.NORTH);

        // Features list
        JPanel featuresPanel = new JPanel(new GridLayout(0, 1, 0, 14));
        featuresPanel.setOpaque(false);

        featuresPanel.add(createFeatureItem("🏋️ Gym Workout Logger", "Track volume, sets, reps & PRs"));
        featuresPanel.add(createFeatureItem("🥗 Macro & Calorie Engine", "Searchable food database & logs"));
        featuresPanel.add(createFeatureItem("📏 Body Metrics & BMI", "Measurement tracking & progress charts"));
        featuresPanel.add(createFeatureItem("💧 Water & Sleep Habits", "Intake rings and sleep recovery logs"));
        featuresPanel.add(createFeatureItem("🏆 Gamified Badges", "Earn streaks and achievement trophies"));

        panel.add(featuresPanel, BorderLayout.CENTER);

        JLabel footer = new JLabel("B.Tech CSE OOP Project • Eclipse & Supabase");
        footer.setFont(Theme.SMALL);
        footer.setForeground(Theme.TEXT_DIM);
        panel.add(footer, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createFeatureItem(String title, String subtitle) {
        JPanel p = new JPanel(new GridLayout(2, 1, 0, 2));
        p.setOpaque(false);
        JLabel t = new JLabel(title);
        t.setFont(Theme.BODY_BOLD);
        t.setForeground(Theme.TEXT_PRIMARY);
        JLabel s = new JLabel(subtitle);
        s.setFont(Theme.SMALL);
        s.setForeground(Theme.TEXT_DIM);
        p.add(t);
        p.add(s);
        return p;
    }

    private JPanel createLoginCard() {
        ModernCard card = new ModernCard(28);
        card.setPreferredSize(new Dimension(420, 480));
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Welcome Back");
        title.setFont(Theme.TITLE_LARGE);
        title.setForeground(Theme.TEXT_PRIMARY);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(title);

        JLabel sub = new JLabel("Sign in to your fitness dashboard");
        sub.setFont(Theme.BODY);
        sub.setForeground(Theme.TEXT_MUTED);
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(sub);
        card.add(Box.createVerticalStrut(20));

        // Error banner
        loginErrorLabel = new JLabel(" ");
        loginErrorLabel.setFont(Theme.SMALL);
        loginErrorLabel.setForeground(Theme.DANGER);
        loginErrorLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(loginErrorLabel);
        card.add(Box.createVerticalStrut(8));

        // Email
        card.add(createFieldLabel("EMAIL ADDRESS"));
        loginEmailField = new JTextField();
        loginEmailField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        loginEmailField.setPreferredSize(new Dimension(360, 38));
        loginEmailField.setFont(Theme.BODY);
        loginEmailField.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(loginEmailField);
        card.add(Box.createVerticalStrut(14));

        // Password
        card.add(createFieldLabel("PASSWORD"));
        loginPasswordField = new JPasswordField();
        loginPasswordField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        loginPasswordField.setPreferredSize(new Dimension(360, 38));
        loginPasswordField.setFont(Theme.BODY);
        loginPasswordField.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(loginPasswordField);
        card.add(Box.createVerticalStrut(22));

        // Sign In Button
        NeonButton signInBtn = new NeonButton("Sign In to Account", NeonButton.ButtonType.PRIMARY);
        signInBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        signInBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(signInBtn);
        card.add(Box.createVerticalStrut(10));

        // Demo User Quick Button
        NeonButton demoBtn = new NeonButton("Quick Demo Account", NeonButton.ButtonType.SECONDARY);
        demoBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        demoBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(demoBtn);
        card.add(Box.createVerticalStrut(20));

        // Toggle to Register
        JPanel togglePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
        togglePanel.setOpaque(false);
        togglePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel noAcc = new JLabel("Don't have an account?");
        noAcc.setFont(Theme.BODY);
        noAcc.setForeground(Theme.TEXT_MUTED);

        JButton regLink = new JButton("Create Account");
        regLink.setFont(Theme.BODY_BOLD);
        regLink.setForeground(Theme.PRIMARY);
        regLink.setBorderPainted(false);
        regLink.setContentAreaFilled(false);
        regLink.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        regLink.addActionListener(e -> {
            loginErrorLabel.setText(" ");
            cardLayout.show(cardsPanel, "REGISTER");
        });

        togglePanel.add(noAcc);
        togglePanel.add(regLink);
        card.add(togglePanel);

        // Actions
        signInBtn.addActionListener(e -> handleLogin());
        loginPasswordField.addActionListener(e -> handleLogin());
        demoBtn.addActionListener(e -> handleDemoLogin());

        return card;
    }

    private JPanel createRegisterCard() {
        ModernCard card = new ModernCard(24);
        card.setPreferredSize(new Dimension(450, 560));
        card.setLayout(new BorderLayout(0, 10));

        JPanel top = new JPanel(new GridLayout(0, 1, 0, 4));
        top.setOpaque(false);
        JLabel title = new JLabel("Create Your Profile");
        title.setFont(Theme.TITLE_LARGE);
        title.setForeground(Theme.TEXT_PRIMARY);
        JLabel sub = new JLabel("Personalize your calories, targets, and stats");
        sub.setFont(Theme.BODY);
        sub.setForeground(Theme.TEXT_MUTED);
        regErrorLabel = new JLabel(" ");
        regErrorLabel.setFont(Theme.SMALL);
        regErrorLabel.setForeground(Theme.DANGER);

        top.add(title);
        top.add(sub);
        top.add(regErrorLabel);
        card.add(top, BorderLayout.NORTH);

        // Form fields in grid
        JPanel form = new JPanel(new GridLayout(0, 2, 12, 10));
        form.setOpaque(false);

        // Name
        regNameField = new JTextField();
        form.add(wrapField("FULL NAME", regNameField));

        // Email
        regEmailField = new JTextField();
        form.add(wrapField("EMAIL ADDRESS", regEmailField));

        // Password
        regPasswordField = new JPasswordField();
        form.add(wrapField("PASSWORD (MIN 6 CHARS)", regPasswordField));

        // Age
        regAgeSpinner = new JSpinner(new SpinnerNumberModel(21, 12, 100, 1));
        form.add(wrapField("AGE (YEARS)", regAgeSpinner));

        // Height
        regHeightSpinner = new JSpinner(new SpinnerNumberModel(175.0, 80.0, 240.0, 0.5));
        form.add(wrapField("HEIGHT (CM)", regHeightSpinner));

        // Weight
        regWeightSpinner = new JSpinner(new SpinnerNumberModel(72.0, 30.0, 250.0, 0.5));
        form.add(wrapField("CURRENT WEIGHT (KG)", regWeightSpinner));

        // Target Weight
        regTargetWeightSpinner = new JSpinner(new SpinnerNumberModel(78.0, 30.0, 250.0, 0.5));
        form.add(wrapField("TARGET WEIGHT (KG)", regTargetWeightSpinner));

        // Goal
        regGoalCombo = new JComboBox<>(FitnessGoalType.values());
        regGoalCombo.setSelectedItem(FitnessGoalType.MUSCLE_GAIN);
        form.add(wrapField("FITNESS GOAL", regGoalCombo));

        // Activity Level
        regActivityCombo = new JComboBox<>(ActivityLevel.values());
        regActivityCombo.setSelectedItem(ActivityLevel.MODERATELY_ACTIVE);
        form.add(wrapField("ACTIVITY LEVEL", regActivityCombo));

        card.add(form, BorderLayout.CENTER);

        // Bottom Controls
        JPanel bottom = new JPanel(new GridLayout(0, 1, 0, 8));
        bottom.setOpaque(false);

        NeonButton createBtn = new NeonButton("Complete Registration & Enter", NeonButton.ButtonType.PRIMARY);
        createBtn.setPreferredSize(new Dimension(0, 40));
        bottom.add(createBtn);

        JPanel backPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
        backPanel.setOpaque(false);
        JLabel haveAcc = new JLabel("Already registered?");
        haveAcc.setFont(Theme.BODY);
        haveAcc.setForeground(Theme.TEXT_MUTED);

        JButton backBtn = new JButton("Sign In Here");
        backBtn.setFont(Theme.BODY_BOLD);
        backBtn.setForeground(Theme.PRIMARY);
        backBtn.setBorderPainted(false);
        backBtn.setContentAreaFilled(false);
        backBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        backBtn.addActionListener(e -> {
            regErrorLabel.setText(" ");
            cardLayout.show(cardsPanel, "LOGIN");
        });

        backPanel.add(haveAcc);
        backPanel.add(backBtn);
        bottom.add(backPanel);

        card.add(bottom, BorderLayout.SOUTH);

        createBtn.addActionListener(e -> handleRegister());

        return card;
    }

    private JPanel wrapField(String labelText, JComponent comp) {
        JPanel p = new JPanel(new BorderLayout(0, 4));
        p.setOpaque(false);
        JLabel l = createFieldLabel(labelText);
        p.add(l, BorderLayout.NORTH);
        comp.setFont(Theme.BODY);
        p.add(comp, BorderLayout.CENTER);
        return p;
    }

    private JLabel createFieldLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(Theme.SMALL);
        l.setForeground(Theme.TEXT_MUTED);
        return l;
    }

    private void handleLogin() {
        String email = loginEmailField.getText();
        String pass = new String(loginPasswordField.getPassword());

        loginErrorLabel.setText("Authenticating...");
        loginErrorLabel.setForeground(Theme.TEXT_MUTED);

        SwingWorker<User, Void> worker = new SwingWorker<>() {
            @Override
            protected User doInBackground() throws Exception {
                return authService.login(email, pass);
            }

            @Override
            protected void done() {
                try {
                    User user = get();
                    launchMainDashboard(user);
                } catch (Exception ex) {
                    Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                    loginErrorLabel.setText(cause.getMessage());
                    loginErrorLabel.setForeground(Theme.DANGER);
                }
            }
        };
        worker.execute();
    }

    private void handleRegister() {
        String name = regNameField.getText();
        String email = regEmailField.getText();
        String pass = new String(regPasswordField.getPassword());
        int age = (int) regAgeSpinner.getValue();
        double height = ((Number) regHeightSpinner.getValue()).doubleValue();
        double weight = ((Number) regWeightSpinner.getValue()).doubleValue();
        double targetWeight = ((Number) regTargetWeightSpinner.getValue()).doubleValue();
        FitnessGoalType goal = (FitnessGoalType) regGoalCombo.getSelectedItem();
        ActivityLevel activity = (ActivityLevel) regActivityCombo.getSelectedItem();

        regErrorLabel.setText("Creating account...");
        regErrorLabel.setForeground(Theme.TEXT_MUTED);

        SwingWorker<User, Void> worker = new SwingWorker<>() {
            @Override
            protected User doInBackground() throws Exception {
                return authService.register(name, email, pass, age, height, weight, targetWeight, goal, activity);
            }

            @Override
            protected void done() {
                try {
                    User user = get();
                    JOptionPane.showMessageDialog(LoginFrame.this,
                            "Registration successful! Welcome to PGTR, " + user.getFullName() + "!",
                            "Account Created", JOptionPane.INFORMATION_MESSAGE);
                    launchMainDashboard(user);
                } catch (Exception ex) {
                    Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                    regErrorLabel.setText(cause.getMessage());
                    regErrorLabel.setForeground(Theme.DANGER);
                }
            }
        };
        worker.execute();
    }

    private void handleDemoLogin() {
        loginEmailField.setText("demo@pgtr.com");
        loginPasswordField.setText("demo123");

        SwingWorker<User, Void> worker = new SwingWorker<>() {
            @Override
            protected User doInBackground() throws Exception {
                try {
                    return authService.login("demo@pgtr.com", "demo123");
                } catch (Exception notFound) {
                    // Create demo account if it doesn't exist yet
                    return authService.register(
                            "Alex Mercer", "demo@pgtr.com", "demo123",
                            22, 178.0, 75.0, 80.0,
                            FitnessGoalType.MUSCLE_GAIN, ActivityLevel.MODERATELY_ACTIVE
                    );
                }
            }

            @Override
            protected void done() {
                try {
                    User user = get();
                    launchMainDashboard(user);
                } catch (Exception ex) {
                    Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                    loginErrorLabel.setText(cause.getMessage());
                    loginErrorLabel.setForeground(Theme.DANGER);
                }
            }
        };
        worker.execute();
    }

    private void launchMainDashboard(User user) {
        SwingUtilities.invokeLater(() -> {
            MainFrame mainFrame = new MainFrame(user);
            mainFrame.setVisible(true);
            this.dispose();
        });
    }
}
