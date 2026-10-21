package com.pgtr.util;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import javax.imageio.ImageIO;

/**
 * Central design tokens, color palette, fonts, and branding assets for Vyntrix.
 * Futuristic dark navy & black background with cyan, teal, and golden-yellow accents.
 */
public class Theme {

    // --- Official Vyntrix Color Palette ---
    public static final Color BG_DARK = new Color(0x0A, 0x0E, 0x17);         // Primary window deep navy/black
    public static final Color BG_PANEL = new Color(0x11, 0x18, 0x27);        // Card / panel surface
    public static final Color BG_PANEL_ALT = new Color(0x19, 0x22, 0x35);    // Elevated surface / Hover
    public static final Color BG_SIDEBAR = new Color(0x06, 0x09, 0x11);      // Deep black sidebar background
    public static final Color BORDER = new Color(0x1E, 0x29, 0x3E);          // Subtle navy charcoal border
    public static final Color BORDER_ACCENT = new Color(0x00, 0xE5, 0xFF, 120); // Translucent cyan highlight

    // --- High-Energy Branding Accents ---
    public static final Color PRIMARY = new Color(0x00, 0xE5, 0xFF);         // Electric Cyan accent
    public static final Color PRIMARY_HOVER = new Color(0x00, 0xB4, 0xD8);   // Deeper cyan on hover
    public static final Color PRIMARY_DIM = new Color(0x00, 0xE5, 0xFF, 30); // Soft cyan glow
    public static final Color GOLD = new Color(0xFF, 0xB8, 0x00);            // Golden-yellow orbital accent
    public static final Color GOLD_HOVER = new Color(0xE5, 0xA6, 0x00);      // Golden-yellow hover
    public static final Color GOLD_DIM = new Color(0xFF, 0xB8, 0x00, 30);    // Soft gold glow
    public static final Color TEAL = new Color(0x00, 0xF2, 0xFE);            // Neon teal gradient accent

    // --- Text Hierarchy ---
    public static final Color TEXT_PRIMARY = new Color(0xF8, 0xFA, 0xFC);    // Pure crisp white headings
    public static final Color TEXT_MUTED = new Color(0x94, 0xA3, 0xB8);      // Muted secondary slate grey
    public static final Color TEXT_DIM = new Color(0x64, 0x74, 0x8B);        // Subtle helper text

    // --- Semantic / Metric Colors ---
    public static final Color SUCCESS = new Color(0x00, 0xE6, 0x76);         // Success green
    public static final Color DANGER = new Color(0xFF, 0x52, 0x52);          // Neon red / error
    public static final Color WARNING = new Color(0xFF, 0xB8, 0x00);         // Golden Amber / warning
    public static final Color WATER_CYAN = new Color(0x00, 0xE5, 0xFF);      // Water intake electric cyan
    public static final Color SLEEP_PURPLE = new Color(0xB3, 0x88, 0xFF);    // Sleep electric lavender
    public static final Color CALORIE_ORANGE = new Color(0xFF, 0x70, 0x43);  // Calories burnt/consumed orange
    public static final Color PROTEIN_BLUE = new Color(0x38, 0xBD, 0xF8);    // Protein macro sky blue

    // --- Fonts ---
    public static final Font TITLE_LARGE = new Font("Segoe UI", Font.BOLD, 22);
    public static final Font TITLE = new Font("Segoe UI", Font.BOLD, 17);
    public static final Font SUBTITLE = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font BODY = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font BODY_BOLD = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font SMALL = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font STAT_VALUE = new Font("Segoe UI", Font.BOLD, 26);
    public static final Font STAT_UNIT = new Font("Segoe UI", Font.PLAIN, 13);

    // Cached application icon
    private static Image cachedAppIcon = null;

    /**
     * Initializes FlatLaf Dark Look and Feel with Vyntrix custom styling tokens.
     */
    public static void setupFlatLaf() {
        try {
            UIManager.setLookAndFeel(new com.formdev.flatlaf.FlatDarkLaf());
            UIManager.put("Button.arc", 10);
            UIManager.put("Component.arc", 10);
            UIManager.put("ProgressBar.arc", 10);
            UIManager.put("TextComponent.arc", 10);
            UIManager.put("ScrollBar.showButtons", false);
            UIManager.put("ScrollBar.width", 10);
            UIManager.put("ScrollBar.thumbArc", 10);
            UIManager.put("ScrollBar.thumb", new Color(0x28, 0x35, 0x4E));
            UIManager.put("ScrollBar.track", BG_DARK);
            UIManager.put("Table.selectionBackground", new Color(0x00, 0xE5, 0xFF, 50));
            UIManager.put("Table.selectionForeground", TEXT_PRIMARY);
            UIManager.put("Table.gridColor", new Color(0x1F, 0x2A, 0x3E));
            UIManager.put("TableHeader.background", BG_PANEL);
            UIManager.put("TableHeader.foreground", TEXT_MUTED);
            UIManager.put("TableHeader.font", BODY_BOLD);
        } catch (Exception e) {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}
        }
    }

    /**
     * Loads the official Vyntrix application logo (futuristic silhouette with cyan & gold orbital rings).
     * Maintains original proportions and scales smoothly.
     */
    public static ImageIcon getAppLogo(int width, int height) {
        return loadScaledIcon("/images/vyntrix_logo.png", width, height);
    }

    /**
     * Loads the official Vyntrix login banner logo.
     */
    public static ImageIcon getLoginLogo(int width, int height) {
        return loadScaledIcon("/images/vyntrix_login_logo.png", width, height);
    }

    /**
     * Loads the window icon image for Vyntrix.
     */
    public static Image getAppIcon() {
        if (cachedAppIcon != null) return cachedAppIcon;
        try (InputStream in = Theme.class.getResourceAsStream("/images/vyntrix_icon.png")) {
            if (in != null) {
                cachedAppIcon = ImageIO.read(in);
                return cachedAppIcon;
            }
        } catch (Exception ignored) {}

        // Fallback: load vyntrix_logo.png
        try (InputStream in = Theme.class.getResourceAsStream("/images/vyntrix_logo.png")) {
            if (in != null) {
                cachedAppIcon = ImageIO.read(in);
                return cachedAppIcon;
            }
        } catch (Exception ignored) {}

        return null;
    }

    /**
     * Sets the application window icon if supported by the OS and window.
     */
    public static void setWindowIcon(Window window) {
        if (window == null) return;
        try {
            Image icon = getAppIcon();
            if (icon != null) {
                window.setIconImage(icon);
            }
        } catch (Exception ignored) {}
    }

    private static ImageIcon loadScaledIcon(String resourcePath, int targetWidth, int targetHeight) {
        try (InputStream in = Theme.class.getResourceAsStream(resourcePath)) {
            if (in != null) {
                BufferedImage original = ImageIO.read(in);
                if (original != null) {
                    int origW = original.getWidth();
                    int origH = original.getHeight();

                    // Maintain aspect ratio
                    double ratio = Math.min((double) targetWidth / origW, (double) targetHeight / origH);
                    int scaledW = (int) Math.round(origW * ratio);
                    int scaledH = (int) Math.round(origH * ratio);

                    Image scaled = original.getScaledInstance(scaledW, scaledH, Image.SCALE_SMOOTH);
                    return new ImageIcon(scaled);
                }
            }
        } catch (Exception e) {
            System.err.println("[Theme] Could not load image resource: " + resourcePath + " (" + e.getMessage() + ")");
        }
        return null;
    }
}
