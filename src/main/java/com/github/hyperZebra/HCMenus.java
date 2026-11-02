/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package com.github.hyperZebra;

import java.awt.Component;
import java.awt.Toolkit;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;


/**
 * HyperCard 2 menu scripting: "create menu", "delete menu", "put ... after menu ... with menuMsg ...",
 * "menuItem n of menu m" and its properties (checkMark, cmdChar, enabled, menuMsg, name).
 * <p>
 * Menus are edited in the source menu bar ({@link GMenu#mb}) which is copied to the window by
 * {@link GMenu#menuUpdate}. Item numbers count separators as HyperCard does.
 */
final class HCMenus {

    private static final Logger logger = System.getLogger(HCMenus.class.getName());

    private HCMenus() {
    }

    private static final String HC_NAME = "hcName";
    private static final String MENU_MSG = "hcMenuMsg";

    private static JMenuBar bar() {
        return PCARD.pc.menu.mb;
    }

    /** @return the menu of the HyperCard name (English or localized), null if none */
    static JMenu find(String name) {
        if (name == null) return null;
        String n = name.trim();
        JMenuBar mb = bar();
        for (int i = 0; i < mb.getComponentCount(); i++) {
            if (mb.getComponent(i) instanceof JMenu m) {
                Object hcName = m.getClientProperty(HC_NAME);
                if (n.equalsIgnoreCase(m.getText()) || (hcName != null && n.equalsIgnoreCase(hcName.toString())) ||
                        n.equalsIgnoreCase(PCARD.getEngText("main", m.getText()))) {
                    return m;
                }
            }
        }
        // "menu 2"
        if (n.matches("\\d+")) {
            int i = Integer.parseInt(n) - 1;
            if (i >= 0 && i < mb.getComponentCount() && mb.getComponent(i) instanceof JMenu m) return m;
        }
        return null;
    }

    static JMenu create(String name) {
        JMenu m = new JMenu(name.trim());
        m.putClientProperty(HC_NAME, name.trim());
        bar().add(m);
        refresh();
        return m;
    }

    static boolean delete(String name) {
        JMenu m = find(name);
        if (m == null) return false;
        bar().remove(m);
        refresh();
        return true;
    }

    /** @return the item of the number (1 based, separators count) or the name, null if none */
    static JMenuItem item(JMenu m, String spec) {
        if (m == null || spec == null) return null;
        String s = spec.trim();
        if (s.matches("\\d+")) {
            int i = Integer.parseInt(s) - 1;
            if (i < 0 || i >= m.getMenuComponentCount()) return null;
            return m.getMenuComponent(i) instanceof JMenuItem mi ? mi : null;
        }
        for (Component c : m.getMenuComponents()) {
            if (c instanceof JMenuItem mi && s.equalsIgnoreCase(mi.getText())) return mi;
        }
        return null;
    }

    /** @return the number of items (separators count) */
    static int count(JMenu m) {
        return m.getMenuComponentCount();
    }

    /** a new item, the message is sent when it is chosen (or doMenu with its name) */
    private static JMenuItem newItem(String text, String menuMsg) {
        JCheckBoxMenuItem mi = new JCheckBoxMenuItem(text);
        mi.putClientProperty(MENU_MSG, menuMsg);
        // copies of the item in the window share this listener, it resolves the source item by its name
        ActionListener l = e -> {
            Object src = e.getSource();
            if (src instanceof JCheckBoxMenuItem cb) cb.setSelected(mi.isSelected()); // not toggled by a click
            Object msg = mi.getClientProperty(MENU_MSG);
            String script = msg != null && !msg.toString().isBlank() ? msg.toString() : "doMenu \"" + mi.getText() + "\"";
            logger.log(Level.DEBUG, "menu: " + script);
            TTalk.CallMessage(script, "", PCARD.pc.stack.curCard, false, true);
        };
        mi.addActionListener(l);
        return mi;
    }

    /**
     * put value before|after|into menu m [with menuMsg msgs]
     * values (lines or items) become items, "-" is a separator.
     */
    static void put(JMenu m, String where, String value, String menuMsgs) {
        List<String> texts = split(value);
        List<String> msgs = menuMsgs != null ? split(menuMsgs) : List.of();
        if (where.equalsIgnoreCase("into")) {
            m.removeAll();
        }
        int at = where.equalsIgnoreCase("before") ? 0 : -1;
        for (int i = 0; i < texts.size(); i++) {
            String t = texts.get(i);
            Component c;
            if (t.equals("-")) {
                c = new JPopupMenu.Separator();
            } else {
                c = newItem(t, i < msgs.size() ? msgs.get(i) : (msgs.size() == 1 ? msgs.get(0) : null));
            }
            if (at >= 0) {
                m.getPopupMenu().insert(c, at++);
            } else {
                m.getPopupMenu().add(c);
            }
        }
        refresh();
    }

    /** put value into menuItem n of menu m [with menuMsg msg] */
    static void putIntoItem(JMenuItem mi, String value, String menuMsg) {
        mi.setText(value);
        if (menuMsg != null) mi.putClientProperty(MENU_MSG, menuMsg);
        refresh();
    }

    private static List<String> split(String value) {
        List<String> list = new ArrayList<>();
        if (value == null) return list;
        String[] parts = value.split("\\r\\n|\\r|\\n", -1);
        if (parts.length == 1 && !value.contains("\"")) parts = value.split(",", -1);
        for (String p : parts) {
            if (!p.isEmpty()) list.add(p);
        }
        return list;
    }

    /** @return true if the property is of the menu item */
    static boolean setProperty(JMenuItem mi, String prop, String value) {
        switch (prop) {
            case "checkmark" -> mi.setSelected(value.trim().equalsIgnoreCase("true"));
            case "cmdchar", "commandchar" -> {
                String v = value.trim();
                if (v.isEmpty()) {
                    mi.setAccelerator(null);
                } else {
                    int code = KeyEvent.getExtendedKeyCodeForChar(Character.toLowerCase(v.charAt(0)));
                    mi.setAccelerator(code == KeyEvent.VK_UNDEFINED ? null :
                            KeyStroke.getKeyStroke(code, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
                }
            }
            case "enabled" -> mi.setEnabled(value.trim().equalsIgnoreCase("true"));
            case "menumsg", "menumessage" -> mi.putClientProperty(MENU_MSG, value);
            case "name", "text" -> mi.setText(value);
            default -> {
                return false;
            }
        }
        refresh();
        return true;
    }

    /** @return the value, null if it is not a property of a menu item */
    static String getProperty(JMenuItem mi, String prop) {
        return switch (prop) {
            case "checkmark" -> Boolean.toString(mi.isSelected());
            case "cmdchar", "commandchar" -> mi.getAccelerator() != null ? KeyEvent.getKeyText(mi.getAccelerator().getKeyCode()) : "";
            case "enabled" -> Boolean.toString(mi.isEnabled());
            case "menumsg", "menumessage" -> {
                Object o = mi.getClientProperty(MENU_MSG);
                yield o != null ? o.toString() : "";
            }
            case "name", "text" -> mi.getText();
            default -> null;
        };
    }

    private static volatile boolean refreshPending;

    /** copies the source menu bar to the window, coalesced */
    static void refresh() {
        if (refreshPending) return;
        refreshPending = true;
        SwingUtilities.invokeLater(() -> {
            refreshPending = false;
            try {
                GMenu.menuUpdate(bar());
                if (logger.isLoggable(Level.DEBUG)) {
                    StringBuilder sb = new StringBuilder();
                    for (Component c : bar().getComponents()) {
                        if (c instanceof JMenu m) {
                            sb.append(m.getText()).append(": ");
                            for (Component c2 : m.getMenuComponents()) {
                                if (c2 instanceof JMenuItem mi) {
                                    sb.append(mi.getText()).append(mi.isSelected() ? "[x]" : "").append(mi.isEnabled() ? "" : "(disabled)")
                                            .append(mi.getAccelerator() != null ? "{" + KeyEvent.getKeyText(mi.getAccelerator().getKeyCode()) + "}" : "").append(", ");
                                } else {
                                    sb.append("-, ");
                                }
                            }
                            sb.append(" | ");
                        }
                    }
                    logger.log(Level.DEBUG, "menus: " + sb);
                }
            } catch (Exception e) {
                logger.log(Level.WARNING, "menu update: " + e);
            }
        });
    }
}
