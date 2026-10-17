package com.pgtr.util;

import javax.swing.*;
import java.awt.*;

/**
 * Central design tokens, color palette, fonts, and styling helpers for modern dark-charcoal & neon-lime theme.
 */
public class Theme {

    // --- Modern Color Palette ---
    public static final Color BG_DARK = new Color(0x12, 0x12, 0x14);         // Primary window charcoal background
    public static final Color BG_PANEL = new Color(0x18, 0x18, 0x1B);        // Card / panel surface
    public static final Color BG_PANEL_ALT = new Color(0x20, 0x20, 0x24);    // Elevated surface / Hover
    public static final Color BG_SIDEBAR = new Color(0x0C, 0x0C, 0x0E);      // Deep black sidebar background
    public static final Color BORDER = new Color(0x2E, 0x2E, 0x34);          // Subtle charcoal border
    public static final Color BORDER_ACCENT = new Color(0x00, 0xE6, 0x76, 120); // Translucent neon highlight

    // --- High-Energy Accents ---
    public static final Color PRIMARY = new Color(0x00, 0xE6, 0x76);        // Electric Lime / Neon Green accent
    public static final Color PRIMARY_HOVER = new Color(0x00, 0xC8, 0x64);  // Slightly darker neon on hover
    public static final Color PRIMARY_DIM = new Color(0x00, 0xE6, 0x76, 30);// Ultra-soft lime glow
    public static final Color TEXT_PRIMARY = new Color(0xF8, 0xFA, 0xFC);   // Pure crisp white headings
    public static final Color TEXT_MUTED = new Color(0x94, 0xA3, 0xB8);     // Muted secondary slate grey
    public static final Color TEXT_DIM = new Color(0x64, 0x74, 0x8B);       // Subtle helper text

    // --- Semantic / Metric Colors ---
    public static final Color SUCCESS = new Color(0x00, 0xE6, 0x76);        // Neon green
    public static final Color DANGER = new Color(0xFF, 0x52, 0x52);         // Neon red / error
    public static final Color WARNING = new Color(0xFF, 0xAB, 0x00);        // Amber / warning
    public static final Color WATER_CYAN = new Color(0x00, 0xD2, 0xFF);     // Water intake electric cyan
    public static final Color SLEEP_PURPLE = new Color(0xB3, 0x88, 0xFF);   // Sleep electric lavender
    public static final Color CALORIE_ORANGE = new Color(0xFF, 0x70, 0x43); // Calories burnt/consumed orange
    public static final Color PROTEIN_BLUE = new Color(0x44, 0x8A, 0xFF);   // Protein macro blue

    // --- Fonts ---
    public static final Font TITLE_LARGE = new Font("Segoe UI", Font.BOLD, 22);
    public static final Font TITLE = new Font("Segoe UI", Font.BOLD, 17);
    public static final Font SUBTITLE = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font BODY = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font BODY_BOLD = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font SMALL = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font STAT_VALUE = new Font("Segoe UI", Font.BOLD, 26);
    public static final Font STAT_UNIT = new Font("Segoe UI", Font.PLAIN, 13);

    /**
     * Initializes FlatLaf Dark Look and Feel with custom accent styling.
     */
    public static void setupFlatLaf() {
        try {
            UIManager.setLookAndFeel(new com.formdev.flatlaf.FlatDarkLaf());
            // Configure custom theme properties
            UIManager.put("Button.arc", 10);
            UIManager.put("Component.arc", 10);
            UIManager.put("ProgressBar.arc", 10);
            UIManager.put("TextComponent.arc", 10);
            UIManager.put("ScrollBar.showButtons", false);
            UIManager.put("ScrollBar.width", 10);
            UIManager.put("ScrollBar.thumbArc", 10);
            UIManager.put("ScrollBar.thumb", new Color(0x3E, 0x3E, 0x46));
            UIManager.put("ScrollBar.track", BG_DARK);
            UIManager.put("Table.selectionBackground", new Color(0x00, 0xE6, 0x76, 50));
            UIManager.put("Table.selectionForeground", TEXT_PRIMARY);
            UIManager.put("Table.gridColor", new Color(0x28, 0x28, 0x30));
            UIManager.put("TableHeader.background", BG_PANEL);
            UIManager.put("TableHeader.foreground", TEXT_MUTED);
            UIManager.put("TableHeader.font", BODY_BOLD);
        } catch (Exception e) {
            // Fallback to system LAF if FlatLaf fails
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}
        }
    }

    public static BorderFactory createEmptyPadding(int top, int left, int bottom, int right) {
        return null;
    }
}
