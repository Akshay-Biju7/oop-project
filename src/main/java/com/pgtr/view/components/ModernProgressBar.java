package com.pgtr.view.components;

import com.pgtr.util.Theme;

import javax.swing.*;
import java.awt.*;

/**
 * Sleek rounded progress bar with custom gradient/solid fill and percentage indicator.
 */
public class ModernProgressBar extends JComponent {

    private int value = 0;
    private int maximum = 100;
    private Color fillColor = Theme.PRIMARY;
    private Color trackColor = Theme.BG_DARK;
    private int barHeight = 12;
    private boolean showText = true;

    public ModernProgressBar(int value, int maximum, Color fillColor) {
        this.value = Math.max(0, value);
        this.maximum = Math.max(1, maximum);
        this.fillColor = fillColor;
        setPreferredSize(new Dimension(200, 24));
    }

    public void setProgress(int value, int maximum) {
        this.value = Math.max(0, value);
        this.maximum = Math.max(1, maximum);
        repaint();
    }

    public void setFillColor(Color color) {
        this.fillColor = color;
        repaint();
    }

    public void setShowText(boolean show) {
        this.showText = show;
        repaint();
    }

    public int getPercentage() {
        return Math.min(100, (int) Math.round(((double) value / maximum) * 100.0));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();
        int y = (height - barHeight) / 2;
        int cornerRadius = barHeight;

        // Draw track background
        g2.setColor(trackColor);
        g2.fillRoundRect(0, y, width, barHeight, cornerRadius, cornerRadius);

        // Draw border around track
        g2.setColor(Theme.BORDER);
        g2.setStroke(new BasicStroke(1.0f));
        g2.drawRoundRect(0, y, width - 1, barHeight - 1, cornerRadius, cornerRadius);

        // Fill progress
        int fillWidth = (int) Math.round(((double) Math.min(value, maximum) / maximum) * width);
        if (fillWidth > 0) {
            g2.setColor(fillColor);
            g2.fillRoundRect(0, y, Math.max(barHeight, fillWidth), barHeight, cornerRadius, cornerRadius);
        }

        g2.dispose();
    }
}
