package com.pgtr.view.components;

import com.pgtr.util.Theme;

import javax.swing.*;
import java.awt.*;

/**
 * Metric stat card displaying an icon/symbol, category title, big bold value, and status subtitle.
 */
public class StatCard extends ModernCard {

    private final JLabel titleLabel;
    private final JLabel valueLabel;
    private final JLabel subtextLabel;
    private final JLabel iconLabel;

    public StatCard(String iconSymbol, String title, String initialValue, String subtext, Color accentColor) {
        super(14);
        setLayout(new BorderLayout(8, 6));

        // Top Header: Title + Icon Pill
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);

        titleLabel = new JLabel(title.toUpperCase());
        titleLabel.setFont(Theme.SMALL);
        titleLabel.setForeground(Theme.TEXT_MUTED);
        topPanel.add(titleLabel, BorderLayout.WEST);

        iconLabel = new JLabel(iconSymbol, SwingConstants.CENTER);
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));
        iconLabel.setForeground(accentColor);
        topPanel.add(iconLabel, BorderLayout.EAST);

        add(topPanel, BorderLayout.NORTH);

        // Center: Big Value
        valueLabel = new JLabel(initialValue);
        valueLabel.setFont(Theme.STAT_VALUE);
        valueLabel.setForeground(Theme.TEXT_PRIMARY);
        add(valueLabel, BorderLayout.CENTER);

        // Bottom: Subtext / Diff indicator
        subtextLabel = new JLabel(subtext);
        subtextLabel.setFont(Theme.SMALL);
        subtextLabel.setForeground(Theme.TEXT_DIM);
        add(subtextLabel, BorderLayout.SOUTH);
    }

    public void setValue(String value) {
        valueLabel.setText(value);
    }

    public void setSubtext(String subtext) {
        subtextLabel.setText(subtext);
    }

    public void setSubtextColor(Color color) {
        subtextLabel.setForeground(color);
    }
}
