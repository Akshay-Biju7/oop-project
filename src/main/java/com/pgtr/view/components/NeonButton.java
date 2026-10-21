package com.pgtr.view.components;

import com.pgtr.util.Theme;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Modern button with custom painted rounded styling, neon cyan and gold accents, and hover states.
 */
public class NeonButton extends JButton {

    public enum ButtonType {
        PRIMARY,
        GOLD,
        SECONDARY,
        DANGER,
        OUTLINE
    }

    private final ButtonType type;
    private boolean hovered = false;
    private boolean pressed = false;
    private int cornerRadius = 10;

    public NeonButton(String text) {
        this(text, ButtonType.PRIMARY);
    }

    public NeonButton(String text, ButtonType type) {
        super(text);
        this.type = type;

        setFont(Theme.BODY_BOLD);
        setFocusPainted(false);
        setBorderPainted(false);
        setContentAreaFilled(false);
        setOpaque(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setBorder(BorderFactory.createEmptyBorder(10, 18, 10, 18));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                hovered = true;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hovered = false;
                repaint();
            }

            @Override
            public void mousePressed(MouseEvent e) {
                pressed = true;
                repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                pressed = false;
                repaint();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();

        Color bg;
        Color fg;
        Color border = null;

        switch (type) {
            case PRIMARY:
                bg = pressed ? Theme.PRIMARY_HOVER.darker() : (hovered ? Theme.PRIMARY_HOVER : Theme.PRIMARY);
                fg = Theme.BG_SIDEBAR; // High contrast dark text on bright cyan
                break;
            case GOLD:
                bg = pressed ? Theme.GOLD_HOVER.darker() : (hovered ? Theme.GOLD_HOVER : Theme.GOLD);
                fg = Theme.BG_SIDEBAR; // High contrast dark text on bright gold
                break;
            case DANGER:
                bg = pressed ? Theme.DANGER.darker() : (hovered ? Theme.DANGER : new Color(0x3B, 0x18, 0x18));
                fg = hovered ? Color.WHITE : Theme.DANGER;
                border = Theme.DANGER;
                break;
            case OUTLINE:
                bg = hovered ? Theme.PRIMARY_DIM : Theme.BG_PANEL;
                fg = hovered ? Theme.PRIMARY : Theme.TEXT_PRIMARY;
                border = hovered ? Theme.PRIMARY : Theme.BORDER;
                break;
            case SECONDARY:
            default:
                bg = pressed ? Theme.BG_DARK : (hovered ? Theme.BG_PANEL_ALT : Theme.BG_PANEL);
                fg = Theme.TEXT_PRIMARY;
                border = hovered ? Theme.TEXT_MUTED : Theme.BORDER;
                break;
        }

        // Fill button shape
        g2.setColor(bg);
        g2.fillRoundRect(0, 0, width - 1, height - 1, cornerRadius, cornerRadius);

        // Draw border if defined
        if (border != null) {
            g2.setColor(border);
            g2.setStroke(new BasicStroke(1.2f));
            g2.drawRoundRect(0, 0, width - 1, height - 1, cornerRadius, cornerRadius);
        }

        // Text
        g2.setFont(getFont());
        FontMetrics fm = g2.getFontMetrics();
        int textX = (width - fm.stringWidth(getText())) / 2;
        int textY = (height + fm.getAscent() - fm.getDescent()) / 2;

        g2.setColor(isEnabled() ? fg : Theme.TEXT_DIM);
        g2.drawString(getText(), textX, textY);

        g2.dispose();
    }
}
