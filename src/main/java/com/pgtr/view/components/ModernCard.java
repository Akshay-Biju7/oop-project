package com.pgtr.view.components;

import com.pgtr.util.Theme;

import javax.swing.*;
import java.awt.*;

/**
 * Sleek card container with rounded corners and subtle charcoal border.
 */
public class ModernCard extends JPanel {

    private int cornerRadius = 14;
    private Color backgroundColor = Theme.BG_PANEL;
    private Color borderColor = Theme.BORDER;

    public ModernCard() {
        setOpaque(false);
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
    }

    public ModernCard(LayoutManager layout) {
        setOpaque(false);
        setLayout(layout);
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
    }

    public ModernCard(int padding) {
        setOpaque(false);
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(padding, padding, padding, padding));
    }

    public void setBackgroundColor(Color color) {
        this.backgroundColor = color;
        repaint();
    }

    public void setBorderColor(Color color) {
        this.borderColor = color;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Fill rounded rect
        g2.setColor(backgroundColor);
        g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, cornerRadius, cornerRadius);

        // Border
        g2.setColor(borderColor);
        g2.setStroke(new BasicStroke(1.2f));
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, cornerRadius, cornerRadius);

        g2.dispose();
    }
}
