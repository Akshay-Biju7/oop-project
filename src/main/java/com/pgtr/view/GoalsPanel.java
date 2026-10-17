package com.pgtr.view;

import com.pgtr.model.FitnessGoal;
import com.pgtr.model.GoalCategory;
import com.pgtr.model.GoalStatus;
import com.pgtr.model.User;
import com.pgtr.service.AchievementService;
import com.pgtr.service.GoalService;
import com.pgtr.service.UserSession;
import com.pgtr.util.DateUtil;
import com.pgtr.util.Theme;
import com.pgtr.view.components.ModernCard;
import com.pgtr.view.components.ModernProgressBar;
import com.pgtr.view.components.NeonButton;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Goal Setting, Personal Milestone Tracking, and Gamified Achievement Showcase.
 */
public class GoalsPanel extends JPanel {

    private final MainFrame mainFrame;
    private final GoalService goalService;
    private final AchievementService achievementService;

    private JPanel activeGoalsContainer;
    private JPanel badgesGrid;

    public GoalsPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.goalService = new GoalService();
        this.achievementService = new AchievementService();

        setOpaque(false);
        setLayout(new BorderLayout(0, 16));
        setBorder(BorderFactory.createEmptyBorder(20, 24, 24, 24));

        initComponents();
    }

    private void initComponents() {
        JPanel scrollableContainer = new JPanel();
        scrollableContainer.setLayout(new BoxLayout(scrollableContainer, BoxLayout.Y_AXIS));
        scrollableContainer.setOpaque(false);

        // 1. Header + Add Goal Button
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JPanel titleBlock = new JPanel(new GridLayout(2, 1, 0, 2));
        titleBlock.setOpaque(false);
        JLabel pageTitle = new JLabel("FITNESS GOALS & ACHIEVEMENTS");
        pageTitle.setFont(Theme.TITLE_LARGE);
        pageTitle.setForeground(Theme.TEXT_PRIMARY);
        JLabel pageSub = new JLabel("Set targets, track personal gain milestones, and unlock athletic badges");
        pageSub.setFont(Theme.BODY);
        pageSub.setForeground(Theme.TEXT_MUTED);
        titleBlock.add(pageTitle);
        titleBlock.add(pageSub);
        headerPanel.add(titleBlock, BorderLayout.WEST);

        NeonButton addGoalBtn = new NeonButton("+ Set New Goal", NeonButton.ButtonType.PRIMARY);
        addGoalBtn.addActionListener(e -> showGoalDialog(null));
        headerPanel.add(addGoalBtn, BorderLayout.EAST);

        scrollableContainer.add(headerPanel);
        scrollableContainer.add(Box.createVerticalStrut(16));

        // 2. Active Fitness Goals Section
        JLabel goalsSecTitle = new JLabel("ACTIVE PERSONAL GAIN GOALS");
        goalsSecTitle.setFont(Theme.SUBTITLE);
        goalsSecTitle.setForeground(Theme.PRIMARY);
        scrollableContainer.add(goalsSecTitle);
        scrollableContainer.add(Box.createVerticalStrut(8));

        activeGoalsContainer = new JPanel();
        activeGoalsContainer.setLayout(new GridLayout(0, 2, 16, 16));
        activeGoalsContainer.setOpaque(false);
        scrollableContainer.add(activeGoalsContainer);
        scrollableContainer.add(Box.createVerticalStrut(24));

        // 3. Achievements Showcase Section
        JLabel badgesSecTitle = new JLabel("TROPHY CASE & EARNED BADGES");
        badgesSecTitle.setFont(Theme.SUBTITLE);
        badgesSecTitle.setForeground(Theme.WARNING);
        scrollableContainer.add(badgesSecTitle);
        scrollableContainer.add(Box.createVerticalStrut(8));

        badgesGrid = new JPanel(new GridLayout(0, 3, 16, 16));
        badgesGrid.setOpaque(false);
        scrollableContainer.add(badgesGrid);

        JScrollPane scrollPane = new JScrollPane(scrollableContainer);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        add(scrollPane, BorderLayout.CENTER);
    }

    public void refreshGoals() {
        User user = UserSession.getInstance().getCurrentUser();
        if (user == null) return;

        SwingWorker<GoalData, Void> worker = new SwingWorker<>() {
            @Override
            protected GoalData doInBackground() throws Exception {
                List<FitnessGoal> goals = goalService.getGoalsByUser(user.getId());
                var badgeStatuses = achievementService.getCatalogWithUserStatus(user.getId());
                return new GoalData(goals, badgeStatuses);
            }

            @Override
            protected void done() {
                try {
                    GoalData data = get();

                    // Render Active Goals Cards
                    activeGoalsContainer.removeAll();
                    if (data.goals.isEmpty()) {
                        ModernCard empty = new ModernCard(20);
                        empty.setLayout(new BorderLayout());
                        JLabel l = new JLabel("<html><center style='color:#94A3B8;'>No fitness goals created yet.<br>Click 'Set New Goal' above to track your next milestone!</center></html>", SwingConstants.CENTER);
                        empty.add(l, BorderLayout.CENTER);
                        activeGoalsContainer.add(empty);
                    } else {
                        for (FitnessGoal g : data.goals) {
                            activeGoalsContainer.add(createGoalCard(g));
                        }
                    }
                    activeGoalsContainer.revalidate();
                    activeGoalsContainer.repaint();

                    // Render Badges Grid
                    badgesGrid.removeAll();
                    for (AchievementService.AchievementBadgeStatus bs : data.badges) {
                        badgesGrid.add(createBadgeCard(bs));
                    }
                    badgesGrid.revalidate();
                    badgesGrid.repaint();

                } catch (Exception ex) {
                    System.err.println("[GoalsPanel] Error refreshing goals: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }

    private ModernCard createGoalCard(FitnessGoal g) {
        ModernCard card = new ModernCard(16);
        card.setLayout(new BorderLayout(0, 10));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);

        JLabel catLabel = new JLabel(g.getCategory().getDisplayName().toUpperCase());
        catLabel.setFont(Theme.SMALL);
        catLabel.setForeground(Theme.TEXT_MUTED);
        top.add(catLabel, BorderLayout.WEST);

        JLabel statusLabel = new JLabel(g.getStatus().getDisplayName());
        statusLabel.setFont(Theme.SMALL);
        statusLabel.setForeground(g.getStatus() == GoalStatus.ACHIEVED ? Theme.PRIMARY :
                (g.getStatus() == GoalStatus.IN_PROGRESS ? Theme.WATER_CYAN : Theme.DANGER));
        top.add(statusLabel, BorderLayout.EAST);
        card.add(top, BorderLayout.NORTH);

        // Center Content
        JPanel center = new JPanel(new GridLayout(3, 1, 0, 4));
        center.setOpaque(false);

        JLabel titleLabel = new JLabel(g.getTitle());
        titleLabel.setFont(Theme.TITLE);
        titleLabel.setForeground(Theme.TEXT_PRIMARY);
        center.add(titleLabel);

        int pct = g.getProgressPercentage();
        JLabel valLabel = new JLabel(String.format("Current: %.1f %s  →  Target: %.1f %s (%d%%)",
                g.getCurrentValue(), g.getUnit(), g.getTargetValue(), g.getUnit(), pct));
        valLabel.setFont(Theme.BODY);
        valLabel.setForeground(Theme.TEXT_PRIMARY);
        center.add(valLabel);

        ModernProgressBar pb = new ModernProgressBar(pct, 100, Theme.PRIMARY);
        center.add(pb);
        card.add(center, BorderLayout.CENTER);

        // Bottom Actions
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(false);

        JLabel deadline = new JLabel("Deadline: " + DateUtil.formatDate(g.getDeadline()));
        deadline.setFont(Theme.SMALL);
        deadline.setForeground(Theme.TEXT_DIM);
        bottom.add(deadline, BorderLayout.WEST);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        btnRow.setOpaque(false);

        if (g.getStatus() == GoalStatus.IN_PROGRESS) {
            NeonButton completeBtn = new NeonButton("Done ✓", NeonButton.ButtonType.PRIMARY);
            completeBtn.setFont(Theme.SMALL);
            completeBtn.addActionListener(e -> {
                try {
                    goalService.markAchieved(g.getId(), g.getUserId());
                    refreshGoals();
                    mainFrame.getDashboardPanel().refreshDashboard();
                    JOptionPane.showMessageDialog(this, "Congratulations on completing '" + g.getTitle() + "'! Achievement unlocked!", "Goal Achieved", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            });
            btnRow.add(completeBtn);
        }

        NeonButton editBtn = new NeonButton("Edit", NeonButton.ButtonType.SECONDARY);
        editBtn.setFont(Theme.SMALL);
        editBtn.addActionListener(e -> showGoalDialog(g));
        btnRow.add(editBtn);

        NeonButton delBtn = new NeonButton("Delete", NeonButton.ButtonType.DANGER);
        delBtn.setFont(Theme.SMALL);
        delBtn.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this, "Delete goal '" + g.getTitle() + "'?", "Confirm", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    goalService.deleteGoal(g.getId(), g.getUserId());
                    refreshGoals();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Error deleting goal: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        btnRow.add(delBtn);

        bottom.add(btnRow, BorderLayout.EAST);
        card.add(bottom, BorderLayout.SOUTH);

        return card;
    }

    private ModernCard createBadgeCard(AchievementService.AchievementBadgeStatus bs) {
        ModernCard card = new ModernCard(14);
        card.setLayout(new BorderLayout(0, 6));

        if (bs.unlocked) {
            card.setBackgroundColor(new Color(0x1B, 0x24, 0x1E));
            card.setBorderColor(Theme.BORDER_ACCENT);
        } else {
            card.setBackgroundColor(Theme.BG_PANEL);
            card.setBorderColor(Theme.BORDER);
        }

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);

        JLabel icon = new JLabel(getBadgeEmoji(bs.achievement.getCode()), SwingConstants.CENTER);
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 28));
        top.add(icon, BorderLayout.WEST);

        JLabel tier = new JLabel(bs.unlocked ? ("UNLOCKED • " + bs.achievement.getBadgeTier()) : "LOCKED");
        tier.setFont(Theme.SMALL);
        tier.setForeground(bs.unlocked ? Theme.PRIMARY : Theme.TEXT_DIM);
        top.add(tier, BorderLayout.EAST);
        card.add(top, BorderLayout.NORTH);

        JPanel center = new JPanel(new GridLayout(2, 1, 0, 2));
        center.setOpaque(false);

        JLabel title = new JLabel(bs.achievement.getTitle());
        title.setFont(Theme.BODY_BOLD);
        title.setForeground(bs.unlocked ? Theme.TEXT_PRIMARY : Theme.TEXT_MUTED);
        center.add(title);

        JLabel desc = new JLabel("<html><body style='width:160px; color:#94A3B8; font-size:11px;'>" +
                bs.achievement.getDescription() + "</body></html>");
        center.add(desc);
        card.add(center, BorderLayout.CENTER);

        if (bs.unlocked && bs.earnedAt != null) {
            JLabel earned = new JLabel("Earned: " + bs.earnedAt.toLocalDate().format(DateUtil.DATE_FORMATTER));
            earned.setFont(Theme.SMALL);
            earned.setForeground(Theme.TEXT_DIM);
            card.add(earned, BorderLayout.SOUTH);
        }

        return card;
    }

    private String getBadgeEmoji(String code) {
        switch (code) {
            case "FIRST_WORKOUT": return "🏋️";
            case "STREAK_7_DAYS": return "🔥";
            case "WORKOUTS_10": return "🥉";
            case "WORKOUTS_50": return "👑";
            case "PROTEIN_TARGET": return "🥩";
            case "NEW_PR": return "⚡";
            case "GOAL_COMPLETED": return "🎯";
            case "HYDRATION_HERO": return "💧";
            case "SLEEP_CHAMPION": return "🌙";
            default: return "🏆";
        }
    }

    private void showGoalDialog(FitnessGoal existing) {
        User user = UserSession.getInstance().getCurrentUser();
        if (user == null) return;

        JDialog dialog = new JDialog(mainFrame, existing == null ? "Create Personal Fitness Goal" : "Edit Goal", true);
        dialog.setSize(520, 500);
        dialog.setLocationRelativeTo(mainFrame);
        dialog.getContentPane().setBackground(Theme.BG_DARK);
        dialog.setLayout(new BorderLayout(14, 14));

        ModernCard container = new ModernCard(20);
        container.setLayout(new GridLayout(0, 2, 12, 12));

        JTextField titleField = new JTextField(existing != null ? existing.getTitle() : "Bench Press 100 kg");
        JComboBox<GoalCategory> categoryCombo = new JComboBox<>(GoalCategory.values());
        if (existing != null) categoryCombo.setSelectedItem(existing.getCategory());

        JSpinner startVal = new JSpinner(new SpinnerNumberModel(existing != null ? existing.getStartingValue() : 70.0, 0.0, 10000.0, 1.0));
        JSpinner targetVal = new JSpinner(new SpinnerNumberModel(existing != null ? existing.getTargetValue() : 100.0, 0.0, 10000.0, 1.0));
        JSpinner currentVal = new JSpinner(new SpinnerNumberModel(existing != null ? existing.getCurrentValue() : 75.0, 0.0, 10000.0, 1.0));
        JTextField unitField = new JTextField(existing != null ? existing.getUnit() : "kg");

        JTextField startDateField = new JTextField(existing != null ? existing.getStartDate().toString() : LocalDate.now().toString());
        JTextField deadlineField = new JTextField(existing != null ? existing.getDeadline().toString() : LocalDate.now().plusMonths(3).toString());
        JComboBox<GoalStatus> statusCombo = new JComboBox<>(GoalStatus.values());
        if (existing != null) statusCombo.setSelectedItem(existing.getStatus());

        JTextField descField = new JTextField(existing != null ? existing.getDescription() : "Progressive overload on flat bench");

        container.add(wrapField("GOAL TITLE", titleField));
        container.add(wrapField("CATEGORY", categoryCombo));
        container.add(wrapField("STARTING VALUE", startVal));
        container.add(wrapField("TARGET VALUE", targetVal));
        container.add(wrapField("CURRENT VALUE", currentVal));
        container.add(wrapField("UNIT (KG, CM, DAYS)", unitField));
        container.add(wrapField("START DATE (YYYY-MM-DD)", startDateField));
        container.add(wrapField("DEADLINE (YYYY-MM-DD)", deadlineField));
        container.add(wrapField("STATUS", statusCombo));
        container.add(wrapField("DESCRIPTION / NOTES", descField));

        JPanel btnBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnBar.setOpaque(false);
        NeonButton cancel = new NeonButton("Cancel", NeonButton.ButtonType.SECONDARY);
        cancel.addActionListener(e -> dialog.dispose());

        NeonButton save = new NeonButton(existing == null ? "Create Goal" : "Save Changes", NeonButton.ButtonType.PRIMARY);
        save.addActionListener(e -> {
            try {
                String t = titleField.getText().trim();
                if (t.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "Please enter a goal title.", "Validation", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                FitnessGoal goal = existing != null ? existing : new FitnessGoal();
                goal.setUserId(user.getId());
                goal.setTitle(t);
                goal.setDescription(descField.getText().trim());
                goal.setCategory((GoalCategory) categoryCombo.getSelectedItem());
                goal.setStartingValue(((Number) startVal.getValue()).doubleValue());
                goal.setTargetValue(((Number) targetVal.getValue()).doubleValue());
                goal.setCurrentValue(((Number) currentVal.getValue()).doubleValue());
                goal.setUnit(unitField.getText().trim());
                goal.setStartDate(LocalDate.parse(startDateField.getText().trim()));
                goal.setDeadline(LocalDate.parse(deadlineField.getText().trim()));
                goal.setStatus((GoalStatus) statusCombo.getSelectedItem());

                if (existing == null) {
                    goalService.createGoal(goal);
                } else {
                    goalService.updateGoal(goal);
                }

                dialog.dispose();
                refreshGoals();
                mainFrame.getDashboardPanel().refreshDashboard();
                JOptionPane.showMessageDialog(mainFrame, "Goal saved successfully!", "Saved", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Error: " + ex.getMessage(), "Validation Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnBar.add(cancel);
        btnBar.add(save);

        dialog.add(container, BorderLayout.CENTER);
        dialog.add(btnBar, BorderLayout.SOUTH);
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

    private static class GoalData {
        final List<FitnessGoal> goals;
        final List<AchievementService.AchievementBadgeStatus> badges;

        public GoalData(List<FitnessGoal> goals, List<AchievementService.AchievementBadgeStatus> badges) {
            this.goals = goals;
            this.badges = badges;
        }
    }
}
