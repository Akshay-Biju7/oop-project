package com.pgtr.view.components;

import com.pgtr.util.Theme;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableModel;
import java.awt.*;

/**
 * Pre-configured JTable styled for dark charcoal and neon accent aesthetics.
 */
public class DarkTable extends JTable {

    public DarkTable(TableModel model) {
        super(model);
        setRowHeight(34);
        setShowGrid(false);
        setIntercellSpacing(new Dimension(0, 0));
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        setBackground(Theme.BG_PANEL);
        setForeground(Theme.TEXT_PRIMARY);
        setSelectionBackground(new Color(0x00, 0xE6, 0x76, 40));
        setSelectionForeground(Theme.TEXT_PRIMARY);
        setFont(Theme.BODY);

        getTableHeader().setReorderingAllowed(false);
        getTableHeader().setBackground(Theme.BG_DARK);
        getTableHeader().setForeground(Theme.TEXT_MUTED);
        getTableHeader().setFont(Theme.BODY_BOLD);
        getTableHeader().setPreferredSize(new Dimension(0, 36));

        // Default Alternating Cell Renderer for Text & Objects
        setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus,
                                                           int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? Theme.BG_PANEL : Theme.BG_PANEL_ALT);
                    c.setForeground(Theme.TEXT_PRIMARY);
                }
                setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
                return c;
            }
        });

        // Dedicated Boolean Checkbox Renderer for Dark Theme
        setDefaultRenderer(Boolean.class, new javax.swing.table.TableCellRenderer() {
            private final JCheckBox checkBox = new JCheckBox();
            {
                checkBox.setHorizontalAlignment(SwingConstants.CENTER);
                checkBox.setOpaque(true);
            }

            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus,
                                                           int row, int column) {
                checkBox.setSelected(Boolean.TRUE.equals(value));
                if (isSelected) {
                    checkBox.setBackground(table.getSelectionBackground());
                    checkBox.setForeground(table.getSelectionForeground());
                } else {
                    checkBox.setBackground(row % 2 == 0 ? Theme.BG_PANEL : Theme.BG_PANEL_ALT);
                    checkBox.setForeground(Theme.TEXT_PRIMARY);
                }
                return checkBox;
            }
        });

        // Boolean Checkbox Editor
        JCheckBox editorCheckBox = new JCheckBox();
        editorCheckBox.setHorizontalAlignment(SwingConstants.CENTER);
        editorCheckBox.setOpaque(true);
        setDefaultEditor(Boolean.class, new DefaultCellEditor(editorCheckBox));
    }

    public static JScrollPane createScrollPane(JTable table) {
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(Theme.BORDER));
        scrollPane.getViewport().setBackground(Theme.BG_PANEL);
        scrollPane.setOpaque(false);
        return scrollPane;
    }
}
