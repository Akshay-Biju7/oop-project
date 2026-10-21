package com.pgtr.view;

import com.pgtr.database.DatabaseManager;
import com.pgtr.model.Reminder;
import com.pgtr.model.User;
import com.pgtr.service.ReminderService;
import com.pgtr.service.UserSession;
import com.pgtr.util.DateUtil;
import com.pgtr.util.Theme;
import com.pgtr.view.components.ModernCard;
import com.pgtr.view.components.NeonButton;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Primary Vyntrix application window featuring official branding, futuristic sidebar,
 * top navigation bar, live streak counter, in-app alerts, and dynamic view switching.
 */
public class MainFrame extends JFrame {

    private final CardLayout cardLayout;
    private final JPanel contentCardsPanel;
    private final ReminderService reminderService;

    // View Panels
    private DashboardPanel dashboardPanel;
    private WorkoutPanel workoutPanel;
    private NutritionPanel nutritionPanel;
    private HealthPanel healthPanel;
    private HabitsPanel habitsPanel;
    private GoalsPanel goalsPanel;
    private PlannerPanel plannerPanel;
    private ReportsPanel reportsPanel;
    private SettingsPanel settingsPanel;

    // Top Bar Indicators
    private JLabel currentViewTitleLabel;
    private JLabel streakBadgeLabel;
    private JLabel dbStatusBadgeLabel;
    private JLabel reminderBellLabel;
    private JLabel userProfileNameLabel;

    // Sidebar button references
    private final Map<String, JButton> navButtonsMap = new HashMap<>();

    public MainFrame(User initialUser) {
        super("Vyntrix — Fitness & Personal Gain Tracking App");
        this.reminderService = new ReminderService();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1320, 840);
        setMinimumSize(new Dimension(1080, 700));
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLocationRelativeTo(null);
        getContentPane().setBackground(Theme.BG_DARK);
        setLayout(new BorderLayout());

        Theme.setWindowIcon(this);

        // Left Sidebar Navigation
        JPanel sidebar = createSidebar();
        add(sidebar, BorderLayout.WEST);

        // Center Container: TopBar + Content Area
        JPanel centerRoot = new JPanel(new BorderLayout());
        centerRoot.setOpaque(false);

        JPanel topBar = createTopBar();
        centerRoot.add(topBar, BorderLayout.NORTH);

        cardLayout = new CardLayout();
        contentCardsPanel = new JPanel(cardLayout);
        contentCardsPanel.setOpaque(false);

        initViews();
        centerRoot.add(contentCardsPanel, BorderLayout.CENTER);

        add(centerRoot, BorderLayout.CENTER);

        // Show Dashboard first
        showView("DASHBOARD");
        updateProfileBadge(initialUser);
        updateDatabaseStatus();
        updateRemindersCount();
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setPreferredSize(new Dimension(250, 0));
        sidebar.setBackground(Theme.BG_SIDEBAR);
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Theme.BORDER));

        // Top Brand Logo Block
        JPanel brandPanel = new JPanel(new BorderLayout(10, 0));
        brandPanel.setOpaque(false);
        brandPanel.setBorder(BorderFactory.createEmptyBorder(20, 18, 20, 18));

        ImageIcon smallLogo = Theme.getAppLogo(42, 42);
        JLabel logoIcon = new JLabel(smallLogo != null ? smallLogo : new ImageIcon());
        brandPanel.add(logoIcon, BorderLayout.WEST);

        JPanel textBlock = new JPanel(new GridLayout(2, 1, 0, 2));
        textBlock.setOpaque(false);

        JLabel brandText = new JLabel("VYNTRIX");
        brandText.setFont(new Font("Segoe UI", Font.BOLD, 20));
        brandText.setForeground(Theme.TEXT_PRIMARY);

        JLabel tagText = new JLabel("REDEFINE YOUR LIMITS");
        tagText.setFont(new Font("Segoe UI", Font.BOLD, 10));
        tagText.setForeground(Theme.GOLD);

        textBlock.add(brandText);
        textBlock.add(tagText);
        brandPanel.add(textBlock, BorderLayout.CENTER);

        sidebar.add(brandPanel, BorderLayout.NORTH);

        // Center Navigation Menu
        JPanel navMenu = new JPanel(new GridLayout(0, 1, 0, 4));
        navMenu.setOpaque(false);
        navMenu.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        addNavItem(navMenu, "DASHBOARD", "📊  Dashboard");
        addNavItem(navMenu, "WORKOUTS", "🏋️  Gym Workouts");
        addNavItem(navMenu, "NUTRITION", "🥗  Nutrition & Water");
        addNavItem(navMenu, "SETTINGS", "👤  Profile & Health");

        sidebar.add(navMenu, BorderLayout.CENTER);

        // Bottom User Summary & Logout
        JPanel userFooter = new JPanel(new BorderLayout(0, 8));
        userFooter.setOpaque(false);
        userFooter.setBorder(BorderFactory.createEmptyBorder(16, 16, 20, 16));

        JPanel userCard = new JPanel(new GridLayout(2, 1, 0, 2));
        userCard.setOpaque(false);

        userProfileNameLabel = new JLabel("Athlete");
        userProfileNameLabel.setFont(Theme.BODY_BOLD);
        userProfileNameLabel.setForeground(Theme.TEXT_PRIMARY);

        JLabel roleLabel = new JLabel("Vyntrix Athlete Pro");
        roleLabel.setFont(Theme.SMALL);
        roleLabel.setForeground(Theme.PRIMARY);

        userCard.add(userProfileNameLabel);
        userCard.add(roleLabel);
        userFooter.add(userCard, BorderLayout.NORTH);

        NeonButton logoutBtn = new NeonButton("Sign Out", NeonButton.ButtonType.SECONDARY);
        logoutBtn.addActionListener(e -> handleLogout());
        userFooter.add(logoutBtn, BorderLayout.SOUTH);

        sidebar.add(userFooter, BorderLayout.SOUTH);

        return sidebar;
    }

    private void addNavItem(JPanel menu, String key, String text) {
        JButton btn = new JButton(text);
        btn.setFont(Theme.BODY);
        btn.setForeground(Theme.TEXT_MUTED);
        btn.setBackground(Theme.BG_SIDEBAR);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(true);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));

        btn.addActionListener(e -> showView(key));
        navButtonsMap.put(key, btn);
        menu.add(btn);
    }

    private JPanel createTopBar() {
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setPreferredSize(new Dimension(0, 60));
        topBar.setBackground(Theme.BG_DARK);
        topBar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.BORDER));

        // Left Title
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 24, 16));
        left.setOpaque(false);
        currentViewTitleLabel = new JLabel("Vyntrix Fitness Dashboard");
        currentViewTitleLabel.setFont(Theme.TITLE);
        currentViewTitleLabel.setForeground(Theme.TEXT_PRIMARY);
        left.add(currentViewTitleLabel);
        topBar.add(left, BorderLayout.WEST);

        // Right Indicators & Notifications
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 12));
        right.setOpaque(false);

        // Streak Badge Pill
        streakBadgeLabel = new JLabel("🔥 0 Day Streak", SwingConstants.CENTER);
        streakBadgeLabel.setFont(Theme.BODY_BOLD);
        streakBadgeLabel.setForeground(Theme.GOLD);
        streakBadgeLabel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER),
                BorderFactory.createEmptyBorder(4, 10, 4, 10)
        ));
        right.add(streakBadgeLabel);

        // Database Pill
        dbStatusBadgeLabel = new JLabel("● Database", SwingConstants.CENTER);
        dbStatusBadgeLabel.setFont(Theme.SMALL);
        dbStatusBadgeLabel.setForeground(Theme.PRIMARY);
        dbStatusBadgeLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        dbStatusBadgeLabel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER),
                BorderFactory.createEmptyBorder(4, 10, 4, 10)
        ));
        dbStatusBadgeLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                showView("SETTINGS");
            }
        });
        right.add(dbStatusBadgeLabel);

        // Reminders Bell Icon with Badge Counter
        reminderBellLabel = new JLabel("🔔 0 Reminders", SwingConstants.CENTER);
        reminderBellLabel.setFont(Theme.SMALL);
        reminderBellLabel.setForeground(Theme.WATER_CYAN);
        reminderBellLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        reminderBellLabel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER),
                BorderFactory.createEmptyBorder(4, 10, 4, 10)
        ));
        reminderBellLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                showRemindersDialog();
            }
        });
        right.add(reminderBellLabel);

        // Date Display
        JLabel dateLabel = new JLabel(DateUtil.formatDate(LocalDate.now()));
        dateLabel.setFont(Theme.BODY);
        dateLabel.setForeground(Theme.TEXT_MUTED);
        right.add(dateLabel);

        topBar.add(right, BorderLayout.EAST);
        return topBar;
    }

    private void initViews() {
        dashboardPanel = new DashboardPanel(this);
        workoutPanel = new WorkoutPanel(this);
        nutritionPanel = new NutritionPanel(this);
        healthPanel = new HealthPanel(this);
        habitsPanel = new HabitsPanel(this);
        goalsPanel = new GoalsPanel(this);
        plannerPanel = new PlannerPanel(this);
        reportsPanel = new ReportsPanel(this);
        settingsPanel = new SettingsPanel(this, healthPanel, reportsPanel);

        contentCardsPanel.add(dashboardPanel, "DASHBOARD");
        contentCardsPanel.add(workoutPanel, "WORKOUTS");
        contentCardsPanel.add(nutritionPanel, "NUTRITION");
        contentCardsPanel.add(settingsPanel, "SETTINGS");
    }

    public void showView(String viewKey) {
        // Smooth redirects for migrated modules
        if ("HEALTH".equals(viewKey)) {
            showView("SETTINGS");
            settingsPanel.selectTab("HEALTH");
            return;
        }
        if ("REPORTS".equals(viewKey)) {
            showView("SETTINGS");
            settingsPanel.selectTab("REPORTS");
            return;
        }
        if ("GOALS".equals(viewKey)) {
            showView("WORKOUTS");
            return;
        }
        if ("PLANNER".equals(viewKey)) {
            showView("WORKOUTS");
            return;
        }
        if ("HABITS".equals(viewKey)) {
            showView("NUTRITION");
            return;
        }

        cardLayout.show(contentCardsPanel, viewKey);

        // Update active sidebar button visual
        for (Map.Entry<String, JButton> entry : navButtonsMap.entrySet()) {
            boolean active = entry.getKey().equals(viewKey);
            JButton b = entry.getValue();
            if (active) {
                b.setBackground(new Color(0x13, 0x22, 0x2E));
                b.setForeground(Theme.PRIMARY);
                b.setFont(Theme.BODY_BOLD);
                b.setBorder(BorderFactory.createMatteBorder(0, 4, 0, 0, Theme.PRIMARY));
            } else {
                b.setBackground(Theme.BG_SIDEBAR);
                b.setForeground(Theme.TEXT_MUTED);
                b.setFont(Theme.BODY);
                b.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));
            }
        }

        // Trigger on-navigate refresh
        switch (viewKey) {
            case "DASHBOARD":
                currentViewTitleLabel.setText("Vyntrix Dashboard Overview & Daily Metrics");
                dashboardPanel.refreshDashboard();
                break;
            case "WORKOUTS":
                currentViewTitleLabel.setText("Gym Workouts — Cut & Bulk Modes");
                workoutPanel.refreshWorkouts();
                break;
            case "NUTRITION":
                currentViewTitleLabel.setText("Nutrition Engine & Daily Hydration Tracking");
                nutritionPanel.refreshNutrition();
                break;
            case "SETTINGS":
                currentViewTitleLabel.setText("Athlete Profile, Health Metrics & Reports");
                settingsPanel.loadProfile();
                break;
        }

        updateRemindersCount();
    }

    public void updateStreak(int streak) {
        streakBadgeLabel.setText(String.format("🔥 %d Day Streak", streak));
    }

    public void updateProfileBadge(User user) {
        if (user != null) {
            userProfileNameLabel.setText(user.getFullName());
        }
    }

    public void updateDatabaseStatus() {
        boolean isSupabase = DatabaseManager.getInstance().isConnectedToSupabase();
        if (isSupabase) {
            dbStatusBadgeLabel.setText("● Supabase Cloud");
            dbStatusBadgeLabel.setForeground(Theme.PRIMARY);
        } else {
            dbStatusBadgeLabel.setText("● Local Storage");
            dbStatusBadgeLabel.setForeground(Theme.GOLD);
        }
    }

    public void updateRemindersCount() {
        User u = UserSession.getInstance().getCurrentUser();
        if (u == null) return;
        try {
            List<Reminder> active = reminderService.getActiveReminders(u.getId());
            reminderBellLabel.setText(String.format("🔔 %d Reminder%s", active.size(), active.size() == 1 ? "" : "s"));
        } catch (Exception ignored) {}
    }

    private void showRemindersDialog() {
        User u = UserSession.getInstance().getCurrentUser();
        if (u == null) return;

        JDialog dialog = new JDialog(this, "Upcoming Reminders & In-App Alerts", true);
        dialog.setSize(480, 420);
        dialog.setLocationRelativeTo(this);
        dialog.getContentPane().setBackground(Theme.BG_DARK);
        dialog.setLayout(new BorderLayout(14, 14));

        ModernCard container = new ModernCard(16);
        container.setLayout(new BorderLayout(0, 10));

        JPanel listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setOpaque(false);

        try {
            List<Reminder> reminders = reminderService.getActiveReminders(u.getId());
            if (reminders.isEmpty()) {
                JLabel empty = new JLabel("No active reminders scheduled.", SwingConstants.CENTER);
                empty.setForeground(Theme.TEXT_MUTED);
                listPanel.add(empty);
            } else {
                for (Reminder r : reminders) {
                    JPanel rCard = new JPanel(new BorderLayout(8, 0));
                    rCard.setBackground(Theme.BG_PANEL_ALT);
                    rCard.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));

                    JLabel text = new JLabel("<html><b>" + r.getTitle() + "</b> (" +
                            r.getReminderTime().toString() + ")<br><span style='color:#94A3B8;'>" +
                            r.getMessage() + "</span></html>");
                    text.setForeground(Theme.TEXT_PRIMARY);
                    rCard.add(text, BorderLayout.CENTER);

                    NeonButton dismissBtn = new NeonButton("Dismiss", NeonButton.ButtonType.SECONDARY);
                    dismissBtn.setFont(Theme.SMALL);
                    dismissBtn.addActionListener(e -> {
                        try {
                            reminderService.dismissReminder(r.getId(), u.getId());
                            dialog.dispose();
                            showRemindersDialog();
                            updateRemindersCount();
                        } catch (Exception ignored) {}
                    });
                    rCard.add(dismissBtn, BorderLayout.EAST);
                    listPanel.add(rCard);
                    listPanel.add(Box.createVerticalStrut(6));
                }
            }
        } catch (Exception ignored) {}

        container.add(new JScrollPane(listPanel), BorderLayout.CENTER);

        JPanel btnBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnBar.setOpaque(false);

        NeonButton addRemBtn = new NeonButton("+ New Reminder", NeonButton.ButtonType.PRIMARY);
        addRemBtn.addActionListener(e -> {
            String title = JOptionPane.showInputDialog(dialog, "Reminder Title:", "Workout Session");
            if (title != null && !title.trim().isEmpty()) {
                Reminder r = new Reminder(null, u.getId(), com.pgtr.model.ReminderType.WORKOUT,
                        title.trim(), "Stay dedicated to your gains!", java.time.LocalTime.of(18, 0), true, false);
                try {
                    reminderService.createReminder(r);
                    dialog.dispose();
                    showRemindersDialog();
                    updateRemindersCount();
                } catch (Exception ignored) {}
            }
        });

        NeonButton close = new NeonButton("Close", NeonButton.ButtonType.SECONDARY);
        close.addActionListener(e -> dialog.dispose());

        btnBar.add(addRemBtn);
        btnBar.add(close);
        container.add(btnBar, BorderLayout.SOUTH);

        dialog.add(container, BorderLayout.CENTER);
        dialog.setVisible(true);
    }

    private void handleLogout() {
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to sign out of Vyntrix?",
                "Sign Out", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            UserSession.getInstance().logout();
            SwingUtilities.invokeLater(() -> {
                LoginFrame login = new LoginFrame();
                login.setVisible(true);
                this.dispose();
            });
        }
    }

    public DashboardPanel getDashboardPanel() {
        return dashboardPanel;
    }

    public NutritionPanel getNutritionPanel() {
        return nutritionPanel;
    }

    public WorkoutPanel getWorkoutPanel() {
        return workoutPanel;
    }

    public SettingsPanel getSettingsPanel() {
        return settingsPanel;
    }

    public HealthPanel getHealthPanel() {
        return healthPanel;
    }

    public ReportsPanel getReportsPanel() {
        return reportsPanel;
    }
}
