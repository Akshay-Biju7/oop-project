package com.pgtr.view;

import com.pgtr.model.ActivityLevel;
import com.pgtr.model.FitnessGoalType;
import com.pgtr.model.User;
import com.pgtr.service.AuthService;
import com.pgtr.util.Theme;
import com.pgtr.view.components.ModernCard;
import com.pgtr.view.components.NeonButton;

import javax.swing.*;
import java.awt.*;

/**
 * Modern Vyntrix authentication window with Sign In and Registration cards.
 * Features official logo, clean commercial fitness styling, and show/hide password toggle.
 */
public class LoginFrame extends JFrame {

    private final AuthService authService;
    private final CardLayout cardLayout;
    private final JPanel cardsPanel;

    // Sign In Fields
    private JTextField loginEmailField;
    private JPasswordField loginPasswordField;
    private JToggleButton showPasswordToggle;
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
        super("Vyntrix — Authentication");
        this.authService = new AuthService();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(920, 680);
        setMinimumSize(new Dimension(820, 620));
        setLocationRelativeTo(null);
        getContentPane().setBackground(Theme.BG_DARK);
        setLayout(new BorderLayout());

        Theme.setWindowIcon(this);

        // Center Container (CardLayout: LOGIN vs REGISTER)
        cardLayout = new CardLayout();
        cardsPanel = new JPanel(cardLayout);
        cardsPanel.setOpaque(false);

        cardsPanel.add(createLoginCard(), "LOGIN");
        cardsPanel.add(createRegisterCard(), "REGISTER");

        JPanel centerWrapper = new JPanel(new GridBagLayout());
        centerWrapper.setOpaque(false);
        centerWrapper.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        centerWrapper.add(cardsPanel);

        add(centerWrapper, BorderLayout.CENTER);
    }

    private JPanel createLoginCard() {
        ModernCard card = new ModernCard(28);
        card.setPreferredSize(new Dimension(460, 560));
        card.setBackgroundColor(Theme.BG_PANEL);
        card.setBorderColor(Theme.BORDER);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        // 1. Vyntrix Logo at the top, centered
        JPanel logoWrap = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        logoWrap.setOpaque(false);
        logoWrap.setAlignmentX(Component.CENTER_ALIGNMENT);

        ImageIcon loginLogo = Theme.getLoginLogo(280, 85);
        if (loginLogo == null) {
            loginLogo = Theme.getAppLogo(85, 85);
        }
        JLabel logoLabel = new JLabel(loginLogo != null ? loginLogo : new ImageIcon());
        logoWrap.add(logoLabel);
        card.add(logoWrap);
        card.add(Box.createVerticalStrut(10));

        // 2. Welcome Heading
        JLabel title = new JLabel("Welcome to Vyntrix", SwingConstants.CENTER);
        title.setFont(Theme.TITLE_LARGE);
        title.setForeground(Theme.TEXT_PRIMARY);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(title);
        card.add(Box.createVerticalStrut(4));

        // 3. Short Subtitle
        JLabel sub = new JLabel("Track your fitness. Build your future.", SwingConstants.CENTER);
        sub.setFont(Theme.BODY);
        sub.setForeground(Theme.TEXT_MUTED);
        sub.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(sub);
        card.add(Box.createVerticalStrut(14));

        // Validation / Error Banner
        loginErrorLabel = new JLabel(" ", SwingConstants.CENTER);
        loginErrorLabel.setFont(Theme.SMALL);
        loginErrorLabel.setForeground(Theme.DANGER);
        loginErrorLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(loginErrorLabel);
        card.add(Box.createVerticalStrut(8));

        // 4. Email address input
        JPanel emailPanel = new JPanel();
        emailPanel.setLayout(new BoxLayout(emailPanel, BoxLayout.Y_AXIS));
        emailPanel.setOpaque(false);
        emailPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        emailPanel.setMaximumSize(new Dimension(380, 60));

        JLabel emailLabel = createFieldLabel("EMAIL ADDRESS");
        emailLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        emailPanel.add(emailLabel);
        emailPanel.add(Box.createVerticalStrut(4));

        loginEmailField = new JTextField();
        loginEmailField.setMaximumSize(new Dimension(380, 38));
        loginEmailField.setPreferredSize(new Dimension(380, 38));
        loginEmailField.setFont(Theme.BODY);
        loginEmailField.setAlignmentX(Component.LEFT_ALIGNMENT);
        emailPanel.add(loginEmailField);
        card.add(emailPanel);
        card.add(Box.createVerticalStrut(10));

        // 5. Password input with Show/Hide password option
        JPanel passPanel = new JPanel();
        passPanel.setLayout(new BoxLayout(passPanel, BoxLayout.Y_AXIS));
        passPanel.setOpaque(false);
        passPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        passPanel.setMaximumSize(new Dimension(380, 65));

        JLabel passLabel = createFieldLabel("PASSWORD");
        passLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        passPanel.add(passLabel);
        passPanel.add(Box.createVerticalStrut(4));

        JPanel passInputRow = new JPanel(new BorderLayout(6, 0));
        passInputRow.setOpaque(false);
        passInputRow.setMaximumSize(new Dimension(380, 38));

        loginPasswordField = new JPasswordField();
        loginPasswordField.setFont(Theme.BODY);
        passInputRow.add(loginPasswordField, BorderLayout.CENTER);

        showPasswordToggle = new JToggleButton("Show");
        showPasswordToggle.setFont(Theme.SMALL);
        showPasswordToggle.setFocusPainted(false);
        showPasswordToggle.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        showPasswordToggle.setPreferredSize(new Dimension(68, 38));
        char defaultEcho = loginPasswordField.getEchoChar();

        showPasswordToggle.addActionListener(e -> {
            if (showPasswordToggle.isSelected()) {
                loginPasswordField.setEchoChar((char) 0);
                showPasswordToggle.setText("Hide");
            } else {
                loginPasswordField.setEchoChar(defaultEcho);
                showPasswordToggle.setText("Show");
            }
        });
        passInputRow.add(showPasswordToggle, BorderLayout.EAST);
        passPanel.add(passInputRow);
        card.add(passPanel);
        card.add(Box.createVerticalStrut(18));

        // 6. Modern Login Button
        JPanel btnPanel = new JPanel();
        btnPanel.setLayout(new BoxLayout(btnPanel, BoxLayout.Y_AXIS));
        btnPanel.setOpaque(false);
        btnPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnPanel.setMaximumSize(new Dimension(380, 95));

        NeonButton signInBtn = new NeonButton("Sign In to Vyntrix", NeonButton.ButtonType.PRIMARY);
        signInBtn.setMaximumSize(new Dimension(380, 42));
        signInBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnPanel.add(signInBtn);
        btnPanel.add(Box.createVerticalStrut(8));

        NeonButton demoBtn = new NeonButton("Quick Demo Account", NeonButton.ButtonType.SECONDARY);
        demoBtn.setMaximumSize(new Dimension(380, 36));
        demoBtn.setFont(Theme.BODY);
        demoBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnPanel.add(demoBtn);
        card.add(btnPanel);
        card.add(Box.createVerticalStrut(16));

        // 7. Registration link for new users
        JPanel togglePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
        togglePanel.setOpaque(false);
        togglePanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel noAcc = new JLabel("New to Vyntrix?");
        noAcc.setFont(Theme.BODY);
        noAcc.setForeground(Theme.TEXT_MUTED);

        JButton regLink = new JButton("Create an Account");
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
        ModernCard card = new ModernCard(26);
        card.setPreferredSize(new Dimension(500, 580));
        card.setBackgroundColor(Theme.BG_PANEL);
        card.setBorderColor(Theme.BORDER);
        card.setLayout(new BorderLayout(0, 10));

        JPanel top = new JPanel(new GridLayout(0, 1, 0, 3));
        top.setOpaque(false);

        JLabel title = new JLabel("Join Vyntrix");
        title.setFont(Theme.TITLE_LARGE);
        title.setForeground(Theme.TEXT_PRIMARY);

        JLabel sub = new JLabel("Set up your personalized fitness & nutrition targets");
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

        regNameField = new JTextField();
        form.add(wrapField("FULL NAME", regNameField));

        regEmailField = new JTextField();
        form.add(wrapField("EMAIL ADDRESS", regEmailField));

        regPasswordField = new JPasswordField();
        form.add(wrapField("PASSWORD (MIN 6 CHARACTERS)", regPasswordField));

        regAgeSpinner = new JSpinner(new SpinnerNumberModel(22, 12, 100, 1));
        form.add(wrapField("AGE (YEARS)", regAgeSpinner));

        regHeightSpinner = new JSpinner(new SpinnerNumberModel(175.0, 80.0, 240.0, 0.5));
        form.add(wrapField("HEIGHT (CM)", regHeightSpinner));

        regWeightSpinner = new JSpinner(new SpinnerNumberModel(72.0, 30.0, 250.0, 0.5));
        form.add(wrapField("CURRENT WEIGHT (KG)", regWeightSpinner));

        regTargetWeightSpinner = new JSpinner(new SpinnerNumberModel(78.0, 30.0, 250.0, 0.5));
        form.add(wrapField("TARGET WEIGHT (KG)", regTargetWeightSpinner));

        regGoalCombo = new JComboBox<>(FitnessGoalType.values());
        regGoalCombo.setSelectedItem(FitnessGoalType.MUSCLE_GAIN);
        form.add(wrapField("FITNESS GOAL", regGoalCombo));

        regActivityCombo = new JComboBox<>(ActivityLevel.values());
        regActivityCombo.setSelectedItem(ActivityLevel.MODERATELY_ACTIVE);
        form.add(wrapField("ACTIVITY LEVEL", regActivityCombo));

        card.add(form, BorderLayout.CENTER);

        // Bottom Controls
        JPanel bottom = new JPanel(new GridLayout(0, 1, 0, 8));
        bottom.setOpaque(false);

        NeonButton createBtn = new NeonButton("Complete Registration & Enter Vyntrix", NeonButton.ButtonType.PRIMARY);
        createBtn.setPreferredSize(new Dimension(0, 42));
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
        String email = loginEmailField.getText().trim();
        String pass = new String(loginPasswordField.getPassword());

        if (email.isEmpty()) {
            loginErrorLabel.setText("Please enter your email address.");
            loginErrorLabel.setForeground(Theme.DANGER);
            loginEmailField.requestFocus();
            return;
        }

        if (pass.isEmpty()) {
            loginErrorLabel.setText("Please enter your password.");
            loginErrorLabel.setForeground(Theme.DANGER);
            loginPasswordField.requestFocus();
            return;
        }

        loginErrorLabel.setText("Authenticating with Vyntrix...");
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
        String name = regNameField.getText().trim();
        String email = regEmailField.getText().trim();
        String pass = new String(regPasswordField.getPassword());
        int age = (int) regAgeSpinner.getValue();
        double height = ((Number) regHeightSpinner.getValue()).doubleValue();
        double weight = ((Number) regWeightSpinner.getValue()).doubleValue();
        double targetWeight = ((Number) regTargetWeightSpinner.getValue()).doubleValue();
        FitnessGoalType goal = (FitnessGoalType) regGoalCombo.getSelectedItem();
        ActivityLevel activity = (ActivityLevel) regActivityCombo.getSelectedItem();

        if (name.isEmpty() || email.isEmpty() || pass.isEmpty()) {
            regErrorLabel.setText("Please fill out all required fields.");
            regErrorLabel.setForeground(Theme.DANGER);
            return;
        }

        if (pass.length() < 6) {
            regErrorLabel.setText("Password must be at least 6 characters.");
            regErrorLabel.setForeground(Theme.DANGER);
            return;
        }

        regErrorLabel.setText("Creating your Vyntrix account...");
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
                            "Registration successful! Welcome to Vyntrix, " + user.getFullName() + "!",
                            "Welcome to Vyntrix", JOptionPane.INFORMATION_MESSAGE);
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
        loginEmailField.setText("demo@vyntrix.com");
        loginPasswordField.setText("demo123");

        SwingWorker<User, Void> worker = new SwingWorker<>() {
            @Override
            protected User doInBackground() throws Exception {
                try {
                    return authService.login("demo@vyntrix.com", "demo123");
                } catch (Exception notFound) {
                    // Create demo account if it doesn't exist yet
                    return authService.register(
                            "Alex Mercer", "demo@vyntrix.com", "demo123",
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
