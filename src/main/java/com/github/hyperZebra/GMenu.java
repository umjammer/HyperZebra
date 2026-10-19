package com.github.hyperZebra;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.ResourceBundle;
import javax.imageio.ImageIO;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.KeyStroke;
import javax.swing.filechooser.FileFilter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import com.github.hyperZebra.OStack.fontClass;


public class GMenu {

    private static final ResourceBundle rb = ResourceBundle.getBundle("messages");

    JMenuBar mb = new JMenuBar();

    public static boolean changeMenuName(String menu, String item, String name) {
        JMenuItem mi = searchMenuItem(PCARD.pc.getJMenuBar(), menu, item);
        if (mi == null) return false;

        mi.setText(PCARD.getText("main", name));
        return true;
    }

    public static boolean changeEnabled(String menu, String item, boolean enabled) {
        JMenuItem mi = searchMenuItem(PCARD.pc.getJMenuBar(), menu, item);
        if (mi == null) return false;

        mi.setEnabled(enabled);
        return true;
    }

    public static boolean changeMEnabled(String menu, boolean enabled) {
        JMenu m = searchMenu(PCARD.pc.getJMenuBar(), menu);
        if (m == null) return false;

        m.setEnabled(enabled);
        return true;
    }

    public static boolean changeSelected(String menu, String item, boolean selected) {
        JCheckBoxMenuItem mi = (JCheckBoxMenuItem) searchMenuItem(PCARD.pc.getJMenuBar(), menu, item);
        if (mi == null) return false;

        mi.setSelected(selected);
        return true;
    }

    public static JMenu searchMenu(JMenuBar mb, String menu) {
        if (mb == null) return null;
        int count = mb.getMenuCount();
        // Find as is
        for (int i = 0; i < count; i++) {
            JMenu m = mb.getMenu(i);
            if (m.getText().equals(menu)) {
                return m;
            }
        }
        // Find in English menu
        for (int i = 0; i < count; i++) {
            JMenu m = mb.getMenu(i);
            if (PCARD.getEngText("main", m.getText()).equals(menu)) {
                return m;
            }
        }

        return null;
    }

    public static JMenuItem searchMenuItem(JMenuBar mb, String menu, String item) {
        if (mb == null) return null;
        int count = mb.getMenuCount();
        // Find as is
        for (int i = 0; i < count; i++) {
            JMenu m = mb.getMenu(i);
            if (m.getText().equals(menu)) {
                int itemcount = m.getItemCount();
                for (int j = 0; j < itemcount; j++) {
                    JMenuItem mi = m.getItem(j);
                    if (mi != null && mi.getText().equals(item)) {
                        return mi;
                    }
                }
            }
        }
        // Find in English menu
        for (int i = 0; i < count; i++) {
            JMenu m = mb.getMenu(i);
            if (PCARD.getEngText("main", m.getText()).equals(menu)) {
                int itemcount = m.getItemCount();
                for (int j = 0; j < itemcount; j++) {
                    JMenuItem mi = m.getItem(j);
                    if (mi != null && PCARD.getEngText("main", mi.getText()).equals(item)) {
                        return mi;
                    }
                }
            }
        }

        return null;
    }


    public GMenu(PCARD pcard, int mode) {
        ActionListener listener = null;

        if (mode == 0) {
            listener = new GMenuBrowse();
        } else if (mode == 1) {
            listener = new GMenuPaint();
        }

        JMenu m;
        JMenuItem mi;
        JCheckBoxMenuItem cb;
        int s = InputEvent.CTRL_DOWN_MASK;
        int s_shi = InputEvent.CTRL_DOWN_MASK + InputEvent.SHIFT_DOWN_MASK;
        if (isMacOSX()) {
            s = InputEvent.META_DOWN_MASK;
            s_shi = InputEvent.META_DOWN_MASK + InputEvent.SHIFT_DOWN_MASK;
        }

        if (mode == 0) {
            // File menu
            m = new JMenu(rb.getString("main.File"));
            mb.add(m);
            m.add(mi = new JMenuItem(rb.getString("main.New_Stack…")));
            mi.addActionListener(listener);
            m.add(mi = new JMenuItem(rb.getString("main.Open_Stack…")));
            mi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, s));
            mi.addActionListener(listener);
            {
                JMenu subm = new JMenu(rb.getString("main.Open_Recent_Stack"));
                m.add(subm);
            }
            m.add(mi = new JMenuItem(rb.getString("main.Close_Stack")));
            mi.setEnabled(false);
            mi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W, s));
            mi.addActionListener(listener);
            m.add(mi = new JMenuItem(rb.getString("main.Save_a_Copy…")));
            mi.setEnabled(false);
            mi.addActionListener(listener);
            m.addSeparator();
            m.add(mi = new JMenuItem(rb.getString("main.Compact_Stack")));
            mi.setEnabled(false);
            mi.setEnabled(false);
            m.add(mi = new JMenuItem(rb.getString("main.Protect_Stack…")));
            mi.setEnabled(false);
            mi.addActionListener(listener);
            m.add(mi = new JMenuItem(rb.getString("main.Delete_Stack…")));
            mi.setEnabled(false);
            mi.setEnabled(false);
            m.addSeparator();
            m.add(mi = new JMenuItem(rb.getString("main.Print…")));
            mi.setEnabled(false);

            if (!isMacOSX()) {
                m.addSeparator();
                m.add(mi = new JMenuItem(PCARD.getText("main", "About " + PCARD.AppName + "…")));
                mi.addActionListener(listener);

                m.addSeparator();
                m.add(mi = new JMenuItem(rb.getString("main.Quit")));
                mi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, s));
                mi.addActionListener(listener);
            }

            // Edit menu
            m = new JMenu(rb.getString("main.Edit"));
            mb.add(m);
            m.add(mi = new JMenuItem(rb.getString("main.Undo")));
            mi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, s));
            mi.addActionListener(listener);
            m.addSeparator();
            m.add(mi = new JMenuItem(rb.getString("main.Cut")));
            mi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, s));
            mi.addActionListener(listener);
            m.add(mi = new JMenuItem(rb.getString("main.Copy")));
            mi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, s));
            mi.addActionListener(listener);
            m.add(mi = new JMenuItem(rb.getString("main.Paste")));
            mi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, s));
            mi.addActionListener(listener);
            m.add(mi = new JMenuItem(rb.getString("main.Delete")));
            mi.addActionListener(listener);
            m.addSeparator();
            m.add(mi = new JMenuItem(rb.getString("main.New_Card")));
            mi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, s));
            mi.addActionListener(listener);
            m.add(mi = new JMenuItem(rb.getString("main.Delete_Card")));
            mi.addActionListener(listener);
            // m.add(mi = new JMenuItem(rb.getString("main.Cut_Card")));mi.setEnabled(false);mi.addActionListener(listener);
            m.add(mi = new JMenuItem(rb.getString("main.Copy_Card")));
            mi.setEnabled(false);
            mi.addActionListener(listener);
            m.addSeparator();
            m.add(cb = new JCheckBoxMenuItem(rb.getString("main.Background")));
            cb.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_B, s));
            cb.addActionListener(listener);
            m.addSeparator();
            m.add(mi = new JMenuItem(rb.getString("main.Icon…")));
            mi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I, s));
            mi.addActionListener(listener);
            m.add(mi = new JMenuItem(rb.getString("main.Sound…")));
            mi.setEnabled(false);
            mi.addActionListener(listener);
            m.add(mi = new JMenuItem(rb.getString("main.Resource…")));
            mi.addActionListener(listener);
        }

        if (mode == 1) {
            // File menu
            m = new JMenu(rb.getString("main.File"));
            mb.add(m);
            m.setEnabled(false);
            m.add(mi = new JMenuItem(rb.getString("main.Import_Paint…")));
            mi.setEnabled(false);/*mi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, s));*/
            mi.addActionListener(listener);
            m.add(mi = new JMenuItem(rb.getString("main.Export_Paint…")));
            mi.setEnabled(false);/*mi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, s));*/
            mi.addActionListener(listener);
            // m.add(mi = new JMenuItem(rb.getString("main.Save_as_ppm…")));/*mi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, s));*/mi.addActionListener(listener);

            // Edit menu
            m = new JMenu(rb.getString("main.Edit"));
            mb.add(m);
            m.add(mi = new JMenuItem(rb.getString("main.Undo_Paint")));
            mi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, s));
            mi.addActionListener(listener);
            m.add(mi = new JMenuItem(rb.getString("main.Redo_Paint")));
            mi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, s_shi));
            mi.addActionListener(listener);
            m.addSeparator();
            m.add(mi = new JMenuItem(rb.getString("main.Cut_Picture")));
            mi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, s));
            mi.addActionListener(listener);
            m.add(mi = new JMenuItem(rb.getString("main.Copy_Picture")));
            mi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, s));
            mi.addActionListener(listener);
            m.add(mi = new JMenuItem(rb.getString("main.Paste_Picture")));
            mi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, s));
            mi.addActionListener(listener);
            m.add(mi = new JMenuItem(rb.getString("main.Clear_Selection")));
            mi.addActionListener(listener);
            m.addSeparator();
            m.add(mi = new JMenuItem(rb.getString("main.New_Card")));
            mi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, s));
            mi.addActionListener(listener);
            m.addSeparator();
            m.add(cb = new JCheckBoxMenuItem(rb.getString("main.Background")));
            cb.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_B, s));
            cb.addActionListener(listener);
            m.add(mi = new JMenuItem(rb.getString("main.Icon…")));
            mi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I, s));
            mi.addActionListener(listener);
            m.add(mi = new JMenuItem(rb.getString("main.Sound…")));
            mi.setEnabled(false);
            mi.addActionListener(listener);
            m.add(mi = new JMenuItem(rb.getString("main.Resource…")));
            mi.setEnabled(false);
            mi.addActionListener(listener);
        }

        // Go menu
        m = new JMenu(rb.getString("main.Go"));
        mb.add(m);
        m.add(mi = new JMenuItem(rb.getString("main.Back")));
        mi.setEnabled(false);
        mi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DEAD_TILDE, s));
        mi.addActionListener(listener);
        m.add(mi = new JMenuItem(rb.getString("main.Home")));
        mi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_H, s));
        mi.addActionListener(listener);
        m.add(mi = new JMenuItem(rb.getString("main.Help")));
        mi.setEnabled(false);
        m.add(mi = new JMenuItem(rb.getString("main.Recent")));
        mi.setEnabled(false);
        m.addSeparator();
        m.add(mi = new JMenuItem(rb.getString("main.First")));
        mi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_1, s));
        mi.addActionListener(listener);
        m.add(mi = new JMenuItem(rb.getString("main.Prev")));
        mi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_2, s));
        mi.addActionListener(listener);
        m.add(mi = new JMenuItem(rb.getString("main.Next")));
        mi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_3, s));
        mi.addActionListener(listener);
        m.add(mi = new JMenuItem(rb.getString("main.Last")));
        mi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_4, s));
        mi.addActionListener(listener);
        m.addSeparator();
        m.add(mi = new JMenuItem(rb.getString("main.Find…")));
        mi.setEnabled(false);
        m.add(mi = new JMenuItem(rb.getString("main.Message")));
        mi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_M, s));
        mi.addActionListener(listener);
        m.add(mi = new JMenuItem(rb.getString("main.Next_Window")));
        mi.setEnabled(false);
        mi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, s));
        mi.addActionListener(listener);

        // Tools menu
        m = new JMenu(rb.getString("main.Tools"));
        mb.add(m);
        m.add(mi = new JMenuItem(rb.getString("main.Show_ToolBar")));
        mi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T, s));
        mi.addActionListener(listener);
        m.addSeparator();
        m.add(mi = new JMenuItem(rb.getString("main.Browse")));
        mi.addActionListener(listener);
        m.add(mi = new JMenuItem(rb.getString("main.Button")));
        mi.addActionListener(listener);
        m.add(mi = new JMenuItem(rb.getString("main.Field")));
        mi.addActionListener(listener);
        m.addSeparator();
        m.add(mi = new JMenuItem(rb.getString("main.Select")));
        mi.addActionListener(listener);
        m.add(mi = new JMenuItem(rb.getString("main.Lasso")));
        mi.addActionListener(listener);
        m.add(mi = new JMenuItem(rb.getString("main.MagicWand")));
        mi.addActionListener(listener);
        m.add(mi = new JMenuItem(rb.getString("main.Pencil")));
        mi.addActionListener(listener);
        m.add(mi = new JMenuItem(rb.getString("main.Brush")));
        mi.addActionListener(listener);
        m.add(mi = new JMenuItem(rb.getString("main.Eraser")));
        mi.addActionListener(listener);
        m.add(mi = new JMenuItem(rb.getString("main.Line")));
        mi.addActionListener(listener);
        // m.add(mi = new JMenuItem(rb.getString("main.SprayCan")));mi.setEnabled(false);mi.addActionListener(listener);
        m.add(mi = new JMenuItem(rb.getString("main.Rect")));
        mi.addActionListener(listener);
        // m.add(mi = new JMenuItem(rb.getString("main.RoundRect")));mi.setEnabled(false);mi.addActionListener(listener);
        m.add(mi = new JMenuItem(rb.getString("main.PaintBucket")));
        mi.addActionListener(listener);
        m.add(mi = new JMenuItem(rb.getString("main.Oval")));
        mi.addActionListener(listener);
        // m.add(mi = new JMenuItem(rb.getString("main.Curve")));mi.setEnabled(false);mi.addActionListener(listener);
        m.add(mi = new JMenuItem(rb.getString("main.Type")));
        mi.addActionListener(listener);
        // m.add(mi = new JMenuItem(rb.getString("main.Polygon")));mi.setEnabled(false);mi.addActionListener(listener);
        // m.add(mi = new JMenuItem(rb.getString("main.FreePolygon")));mi.setEnabled(false);mi.addActionListener(listener);
        // m.add(mi = new JMenuItem(rb.getString("main.Spoit")));mi.setEnabled(false);mi.addActionListener(listener);

        // 0. Browse mode
        if (mode == 0) {
            // Objects menu
            m = new JMenu(rb.getString("main.Objects"));
            mb.add(m);
            m.add(mi = new JMenuItem(rb.getString("main.Button_Info…")));
            mi.addActionListener(listener);
            m.add(mi = new JMenuItem(rb.getString("main.Field_Info…")));
            mi.addActionListener(listener);
            m.add(mi = new JMenuItem(rb.getString("main.Card_Info…")));
            mi.addActionListener(listener);
            m.add(mi = new JMenuItem(rb.getString("main.Background_Info…")));
            mi.addActionListener(listener);
            m.add(mi = new JMenuItem(rb.getString("main.Stack_Info…")));
            mi.addActionListener(listener);
            m.addSeparator();
            m.add(mi = new JMenuItem(rb.getString("main.to_front")));
            mi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_PLUS, s));
            mi.addActionListener(listener);
            m.add(mi = new JMenuItem(rb.getString("main.to_back")));
            mi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, s));
            mi.addActionListener(listener);
            m.addSeparator();
            m.add(mi = new JMenuItem(rb.getString("main.New_Button")));
            mi.addActionListener(listener);
            m.add(mi = new JMenuItem(rb.getString("main.New_Field")));
            mi.addActionListener(listener);
            m.add(mi = new JMenuItem(rb.getString("main.New_Background")));
            mi.addActionListener(listener);

        }

        if (mode == 1) {
            // Paint menu
            m = new JMenu(rb.getString("main.Paint"));
            mb.add(m);
            // m.add(mi = new JMenuItem(rb.getString("main.Select")));mi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, s));mi.addActionListener(listener);
            m.add(mi = new JMenuItem(rb.getString("main.Select_All")));
            mi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, s));
            mi.addActionListener(listener);
            m.add(cb = new JCheckBoxMenuItem(rb.getString("main.FatBits")));
            cb.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, s));
            cb.addActionListener(listener);
            m.add(cb = new JCheckBoxMenuItem(rb.getString("main.Antialias")));
            cb.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I, s));
            cb.addActionListener(listener);
            m.addSeparator();
            m.add(mi = new JMenuItem(rb.getString("main.Reverse_Selection")));/*mi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, s));*/
            mi.addActionListener(listener);
            m.add(mi = new JMenuItem(rb.getString("main.Expand_Selection")));/*mi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, s));*/
            mi.addActionListener(listener);
            // m.add(mi = new JMenuItem(rb.getString("main.Add_to_Protect_Area")));/*mi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, s));*/mi.addActionListener(listener);
            // m.add(mi = new JMenuItem(rb.getString("main.Discard_Protect_Area")));/*mi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, s));*/mi.addActionListener(listener);
            m.add(mi = new JMenuItem(rb.getString("main.Color_Convert…")));/*mi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, s));*/
            mi.addActionListener(listener);
            // m.add(mi = new JMenuItem(rb.getString("main.Emboss…")));/*mi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, s));*/mi.addActionListener(listener);
            m.add(mi = new JMenuItem(rb.getString("main.Filter…")));/*mi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, s));*/
            mi.addActionListener(listener);
            m.add(mi = new JMenuItem(rb.getString("main.Blending_Mode…")));/*mi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, s));*/
            mi.addActionListener(listener);
            m.add(mi = new JMenuItem(rb.getString("main.Scale_Selection…")));/*mi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, s));*/
            mi.addActionListener(listener);
            m.addSeparator();
            m.add(mi = new JMenuItem(rb.getString("main.Fill")));/*mi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, s));*/
            mi.addActionListener(listener);
            m.add(mi = new JMenuItem(rb.getString("main.Invert")));/*mi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, s));*/
            mi.addActionListener(listener);
            m.add(mi = new JMenuItem(rb.getString("main.Pickup")));/*mi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, s));*/
            mi.addActionListener(listener);
            m.add(mi = new JMenuItem(rb.getString("main.Darken")));/*mi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, s));*/
            mi.addActionListener(listener);
            m.add(mi = new JMenuItem(rb.getString("main.Lighten")));/*mi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, s));*/
            mi.addActionListener(listener);
            m.add(mi = new JMenuItem(rb.getString("main.Rotate_Left")));/*mi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, s));*/
            mi.addActionListener(listener);
            m.add(mi = new JMenuItem(rb.getString("main.Rotate_Right")));/*mi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, s));*/
            mi.addActionListener(listener);
            m.add(mi = new JMenuItem(rb.getString("main.Flip_Horizontal")));/*mi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, s));*/
            mi.addActionListener(listener);
            m.add(mi = new JMenuItem(rb.getString("main.Flip_Vertical")));/*mi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y, s));*/
            mi.addActionListener(listener);
            m.addSeparator();
            m.add(mi = new JMenuItem(rb.getString("main.Opaque")));/*mi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, s));*/
            mi.addActionListener(listener);
            m.add(mi = new JMenuItem(rb.getString("main.Transparent")));/*mi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, s));*/
            mi.addActionListener(listener);
            m.addSeparator();
            m.add(mi = new JMenuItem(rb.getString("main.Keep")));
            mi.setEnabled(false);/*mi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, s));*/
            mi.addActionListener(listener);
            m.add(mi = new JMenuItem(rb.getString("main.Revert")));
            mi.setEnabled(false);/*mi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, s));*/
            mi.addActionListener(listener);
            m.addSeparator();
            m.add(mi = new JMenuItem(rb.getString("main.Rotate")));
            mi.setEnabled(false);/*mi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, s));*/
            mi.addActionListener(listener);
            m.add(mi = new JMenuItem(rb.getString("main.Distort")));
            mi.setEnabled(false);/*mi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, s));*/
            mi.addActionListener(listener);
            m.add(mi = new JMenuItem(rb.getString("main.Stretch")));
            mi.setEnabled(false);/*mi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, s));*/
            mi.addActionListener(listener);
            m.add(mi = new JMenuItem(rb.getString("main.Perspective")));
            mi.setEnabled(false);/*mi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, s));*/
            mi.addActionListener(listener);

        }

        // View menu
        m = new JMenu(rb.getString("main.View"));
        mb.add(m);
        m.add(cb = new JCheckBoxMenuItem(rb.getString("main.Zoom_x1")));
        cb.setSelected(pcard.zoom == 1);
        cb.addActionListener(listener);
        m.add(cb = new JCheckBoxMenuItem(rb.getString("main.Zoom_x2")));
        cb.setSelected(pcard.zoom == 2);
        cb.addActionListener(listener);
        m.add(cb = new JCheckBoxMenuItem(rb.getString("main.Zoom_x3")));
        cb.setSelected(pcard.zoom == 3);
        cb.addActionListener(listener);
        m.add(cb = new JCheckBoxMenuItem(rb.getString("main.Zoom_x4")));
        cb.setSelected(pcard.zoom == 4);
        cb.addActionListener(listener);

        // Font menu
        m = new JMenu(rb.getString("main.Font"));
        mb.add(m);
        for (String fontName : FONT_MENU_ITEMS) {
            m.add(cb = new JCheckBoxMenuItem(PCARD.getText("main", fontName)));
            if (fontName.equals("Default")) {
                cb.setSelected(pcard.selectedFont == null);
            } else {
                cb.setSelected(fontName.equals(pcard.selectedFont));
            }
            cb.addActionListener(listener);
        }
        m.addSeparator();
        for (int offset : FONT_SIZE_OFFSETS) {
            String label = "Size " + (offset > 0 ? "+" + offset : Integer.toString(offset));
            m.add(cb = new JCheckBoxMenuItem(PCARD.getText("main", label)));
            cb.setSelected(pcard.fontSizeOffset == offset);
            cb.addActionListener(listener);
        }

        // Encoding menu
        m = new JMenu(rb.getString("main.Encoding"));
        mb.add(m);
        for (String encName : ENCODING_MENU_ITEMS) {
            m.add(cb = new JCheckBoxMenuItem(PCARD.getText("main", encName)));
            cb.setSelected(encName.equalsIgnoreCase(pcard.textEncoding));
            cb.addActionListener(listener);
        }
    }

    static final String[] FONT_MENU_ITEMS = {
        "Default",
        "SansSerif",
        "Serif",
        "Monospaced",
        "Dialog",
        "Geneva",
        "Helvetica",
        "Monaco",
        "Palatino",
        "Osaka",
        "Hiragino Sans",
        "Hiragino Maru Gothic ProN",
        "Hiragino Mincho ProN",
        "YuGothic"
    };

    static final int[] FONT_SIZE_OFFSETS = { -4, -2, -1, 0, 1, 2 };

    static final String[] ENCODING_MENU_ITEMS = {
        "Auto",
        "Shift_JIS",
        "MacRoman",
        "UTF-8",
        "EUC-JP",
        "ISO-8859-1"
    };

    static boolean isMacOSX() {
        return System.getProperty("os.name").toLowerCase().startsWith("mac os x");
    }

    public static void menuUpdate(JMenuBar srcMenuBar) {
        JMenuBar menubar = PCARD.pc.getJMenuBar();
        menubar.removeAll();
        for (int i = 0; i < srcMenuBar.getComponentCount(); i++) {
            Component c = srcMenuBar.getComponent(i);
            if (c.getClass() == JMenu.class) {
                JMenu menu = (JMenu) c;
                JMenu newMenu = new JMenu(menu.getText());
                for (int j = 0; j < menu.getPopupMenu().getComponentCount(); j++) {
                    Component c2 = menu.getPopupMenu().getComponent(j);
                    if (c2.getClass() == JMenuItem.class) {
                        JMenuItem mi = (JMenuItem) c2;
                        JMenuItem newmi = new JMenuItem(mi.getText());
                        newmi.setEnabled(c2.isEnabled());
                        ActionListener[] listeners = mi.getActionListeners();
                        if (listeners.length > 0) {
                            newmi.addActionListener(listeners[0]);
                        }
                        newmi.setAccelerator(mi.getAccelerator());
                        newMenu.add(newmi);
                    } else if (c2.getClass() == JCheckBoxMenuItem.class) {
                        JCheckBoxMenuItem mi = (JCheckBoxMenuItem) c2;
                        JCheckBoxMenuItem newmi = new JCheckBoxMenuItem(mi.getText());
                        newmi.setEnabled(mi.isEnabled());
                        newmi.setSelected(mi.isSelected());
                        ActionListener[] listeners = mi.getActionListeners();
                        if (listeners.length > 0) {
                            newmi.addActionListener(listeners[0]);
                        }
                        newmi.setAccelerator(mi.getAccelerator());
                        newMenu.add(newmi);
                    } else if (c2.getClass() == JPopupMenu.Separator.class) {
                        newMenu.addSeparator();
                    } else if (c2.getClass() == JMenu.class) {
                        JMenu menu2 = (JMenu) c2;
                        JMenu newMenu2 = new JMenu(menu2.getText());
                        for (int k = 0; k < menu2.getPopupMenu().getComponentCount(); k++) {
                            Component c3 = menu2.getPopupMenu().getComponent(k);
                            if (c3.getClass() == JMenuItem.class) {
                                JMenuItem mi = (JMenuItem) c3;
                                JMenuItem newmi = new JMenuItem(mi.getText());
                                newmi.setEnabled(c3.isEnabled());
                                ActionListener[] listeners = mi.getActionListeners();
                                if (listeners.length > 0) {
                                    newmi.addActionListener(listeners[0]);
                                }
                                newmi.setAccelerator(mi.getAccelerator());
                                newMenu2.add(newmi);
                            }
                        }
                        newMenu.add(newMenu2);
                    }
                }
                menubar.add(newMenu);
            }
        }
        BuildRecentMenu((JMenu) menubar.getComponent(0));
    }

    static String[] recentsMenuName = new String[10];
    static String[] recentsMenuPath = new String[10];

    public static void BuildRecentMenu(JMenu m) {
        GMenuBrowse listener = new GMenuBrowse();

        JMenu subm = null;
        for (int i = 0; i < m.getPopupMenu().getComponentCount(); i++) {
            if (m.getPopupMenu().getComponent(i).getClass() == JMenu.class) {
                subm = (JMenu) m.getPopupMenu().getComponent(i);
                subm.removeAll();
            }
        }
        if (subm == null) return;

        // Recently used stacks
        File recentFile = new File("resource_trash" + File.separatorChar + "recent.txt");
        if (recentFile.exists()) {
            FileInputStream fis = null;
            try {
                fis = new FileInputStream(recentFile);
                int length = (int) recentFile.length();
                byte[] b = new byte[length];
                fis.read(b);
                String[] recents = new String(b).split("\n");
                for (int i = 0; i < 10 && i < recents.length; i++) {
                    if (new File(recents[i]).exists()) {
                        JMenuItem mi = new JMenuItem((i + 1) + " " + new File(recents[i]).getName());
                        mi.addActionListener(listener);
                        subm.add(mi);
                        recentsMenuPath[i] = recents[i];
                        recentsMenuName[i] = (i + 1) + " " + new File(recents[i]).getName();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        subm.addSeparator();
        JMenuItem mi = new JMenuItem(rb.getString("main.Clear_This_Menu"));
        mi.addActionListener(listener);
        subm.add(mi);
    }
}


/*class MyMenu extends JMenu
{
    private static final long serialVersionUID = 5858305584938822448L;

    public MyMenu(String text) {
        super(text);
        TearoffThread thread = new TearoffThread(this.getPopupMenu());
        thread.start();
    }

}


class TearoffThread extends Thread
{
    JPopupMenu popup;
    TearoffThread(JPopupMenu popup){
        this.popup = popup;
    }

    public void run(){
        while(!popup.isVisible()){
            try { sleep(10); } catch (InterruptedException e) {}
        }
        while(popup.isVisible()){
            try { sleep(10); } catch (InterruptedException e) {}

            PointerInfo pointerInfo = MouseInfo.getPointerInfo();
            Rectangle vrect = popup.getVisibleRect();
            Point p = pointerInfo.getLocation();
            if(p.y>vrect.y && (p.y>vrect.y+vrect.height || p.x<vrect.x || p.x>vrect.x+vrect.width)){
                popup.setLocation(p.x, p.y);
            }
        }
    }
}*/

// Menu actions during browsing (also used in authoring and painting)
class GMenuBrowse implements ActionListener {

    private static final ResourceBundle rb = ResourceBundle.getBundle("messages");

    @Override
    public void actionPerformed(ActionEvent e) {
        String cmd = e.getActionCommand();
        //System.out.println(cmd);

        // Do not execute menu during script execution
        if (TTalk.idle != true) return;

        try {
            doMenu(cmd);
        } catch (XTalkException e1) {
            e1.printStackTrace();
        }
    }

    private static boolean isFontMenuItem(String cmd) {
        for (String f : GMenu.FONT_MENU_ITEMS) {
            if (f.equalsIgnoreCase(cmd)) return true;
        }
        return false;
    }

    private static boolean isEncodingMenuItem(String cmd) {
        for (String enc : GMenu.ENCODING_MENU_ITEMS) {
            if (enc.equalsIgnoreCase(cmd)) return true;
        }
        return false;
    }

    public static void doMenu(String in_cmd)
            throws XTalkException {
        String cmd = PCARD.getEngText("main", in_cmd);
        // System.out.println(cmd);

        if (cmd.equalsIgnoreCase("First") ||
                cmd.equalsIgnoreCase("Prev") ||
                cmd.equalsIgnoreCase("Next") ||
                cmd.equalsIgnoreCase("Last")) {
            if (PCARD.pc.tool != null) {
                PaintTool.saveCdPictures();
            }

            if (cmd.equalsIgnoreCase("About " + PCARD.AppName + "…")) {
                TTalk.doScriptforMenu("about this");
            } else if (cmd.equalsIgnoreCase("First")) {
                TTalk.doScriptforMenu("go first");
            } else if (cmd.equalsIgnoreCase("Prev")) {
                TTalk.doScriptforMenu("go prev");
            } else if (cmd.equalsIgnoreCase("Next")) {
                TTalk.doScriptforMenu("go next");
            } else if (cmd.equalsIgnoreCase("Last")) {
                TTalk.doScriptforMenu("go last");
            }

            if (PCARD.pc.tool != null) {
                // For paint tools, do various things with this
                TBButtonListener.ChangeTool("DummyPaint", null);
            } else if (AuthTool.tool != null) {
                if (AuthTool.tool.getClass() == ButtonTool.class) {
                    ButtonGUI.gui.tgtOBtn = null;
                    ButtonGUI.gui.target = null;
                    PCARD.pc.mainPane.addMouseListener(ButtonGUI.gui);
                    PCARD.pc.mainPane.addMouseMotionListener(ButtonGUI.gui);
                } else if (AuthTool.tool.getClass() == FieldTool.class) {
                    FieldGUI.gui.tgtOFld = null;
                    FieldGUI.gui.target = null;
                    PCARD.pc.mainPane.addMouseListener(FieldGUI.gui);
                    PCARD.pc.mainPane.addMouseMotionListener(FieldGUI.gui);
                }
            }
        } else if (cmd.equalsIgnoreCase("Home")) {
            TTalk.doScriptforMenu("go home");
        } else if (cmd.equalsIgnoreCase("Message")) {
            if (GMsg.msg.isVisible()) TTalk.doScriptforMenu("hide msg");
            else TTalk.doScriptforMenu("show msg");
        } else if (cmd.equalsIgnoreCase("Show ToolBar")) {
            TTalk.doScriptforMenu("show tool window");
        } else if (cmd.equalsIgnoreCase("Hide ToolBar")) {
            TTalk.doScriptforMenu("hide tool window");
        } else if (cmd.equalsIgnoreCase("Browse")) {
            TTalk.doScriptforMenu("choose browse tool");
        } else if (cmd.equalsIgnoreCase("Button")) {
            TTalk.doScriptforMenu("choose button tool");
        } else if (cmd.equalsIgnoreCase("Field")) {
            TTalk.doScriptforMenu("choose field tool");
        } else if (cmd.equalsIgnoreCase("Select")) {
            TTalk.doScriptforMenu("choose select tool");
        } else if (cmd.equalsIgnoreCase("Lasso")) {
            TTalk.doScriptforMenu("choose lasso tool");
        } else if (cmd.equalsIgnoreCase("MagicWand")) {
            TTalk.doScriptforMenu("choose magicwand tool");
        } else if (cmd.equalsIgnoreCase("Pencil")) {
            TTalk.doScriptforMenu("choose pencil tool");
        } else if (cmd.equalsIgnoreCase("Brush")) {
            TTalk.doScriptforMenu("choose brush tool");
        } else if (cmd.equalsIgnoreCase("Eraser")) {
            TTalk.doScriptforMenu("choose eraser tool");
        } else if (cmd.equalsIgnoreCase("Line")) {
            TTalk.doScriptforMenu("choose line tool");
        } else if (cmd.equalsIgnoreCase("SprayCan")) {
            TTalk.doScriptforMenu("choose spraycan tool");
        } else if (cmd.equalsIgnoreCase("Rect")) {
            TTalk.doScriptforMenu("choose rect tool");
        } else if (cmd.equalsIgnoreCase("RoundRect")) {
            TTalk.doScriptforMenu("choose roundrect tool");
        } else if (cmd.equalsIgnoreCase("PaintBucket")) {
            TTalk.doScriptforMenu("choose paintbucket tool");
        } else if (cmd.equalsIgnoreCase("Oval")) {
            TTalk.doScriptforMenu("choose oval tool");
        } else if (cmd.equalsIgnoreCase("Curve")) {
            TTalk.doScriptforMenu("choose curve tool");
        } else if (cmd.equalsIgnoreCase("Type")) {
            TTalk.doScriptforMenu("choose type tool");
        } else if (cmd.equalsIgnoreCase("Polygon")) {
            TTalk.doScriptforMenu("choose polygon tool");
        } else if (cmd.equalsIgnoreCase("FreePolygon")) {
            TTalk.doScriptforMenu("choose freepolygon tool");
        } else if (cmd.equalsIgnoreCase("Spoit")) {
            TTalk.doScriptforMenu("choose spoit tool");
        } else if (cmd.equalsIgnoreCase("Home")) {
            TTalk.doScriptforMenu("go home");
        } else if (cmd.equalsIgnoreCase("Next Window")) {
            if (PCARD.pc.stack.scriptEditor != null) {
                PCARD.pc.stack.scriptEditor.toFront();
            }
        } else if (cmd.equalsIgnoreCase("Zoom x1") || cmd.equalsIgnoreCase("Zoom x2") || cmd.equalsIgnoreCase("Zoom x3") || cmd.equalsIgnoreCase("Zoom x4")) {
            int newZoom = Integer.parseInt(cmd.substring(cmd.length() - 1));
            PCARD.pc.zoom = newZoom;
            for (int z = 1; z <= 4; z++) {
                GMenu.changeSelected("View", "Zoom x" + z, z == newZoom);
            }
            PCARDFrame.setNewBounds();
            if (PCARD.pc.stack != null && PCARD.pc.mainPane != null) {
                PCARD.pc.mainPane.setBounds(PCARD.pc.stack.left * newZoom, PCARD.pc.stack.top * newZoom, PCARD.pc.stack.width * newZoom, PCARD.pc.stack.height * newZoom + PCARD.pc.getInsets().top);
                PCARD.pc.mainPane.repaint();
            }
        } else if (isFontMenuItem(cmd)) {
            PCARD.pc.selectedFont = cmd.equalsIgnoreCase("Default") ? null : cmd;
            for (String f : GMenu.FONT_MENU_ITEMS) {
                GMenu.changeSelected("Font", f, f.equalsIgnoreCase(cmd));
            }
            if (PCARD.pc.stack != null) {
                OCard.reloadCurrentCard();
            }
        } else if (cmd.startsWith("Size ")) {
            try {
                int newOffset = Integer.parseInt(cmd.substring(5).replace("+", "").trim());
                PCARD.pc.fontSizeOffset = newOffset;
                for (int offset : GMenu.FONT_SIZE_OFFSETS) {
                    String label = "Size " + (offset > 0 ? "+" + offset : Integer.toString(offset));
                    GMenu.changeSelected("Font", label, offset == newOffset);
                }
                if (PCARD.pc.stack != null) {
                    OCard.reloadCurrentCard();
                }
            } catch (NumberFormatException ignored) {
            }
        } else if (isEncodingMenuItem(cmd)) {
            PCARD.pc.textEncoding = cmd;
            for (String enc : GMenu.ENCODING_MENU_ITEMS) {
                GMenu.changeSelected("Encoding", enc, enc.equalsIgnoreCase(cmd));
            }
            if (PCARD.pc.stack != null) {
                HCData.redecodeStack(PCARD.pc.stack);
            }
        } else if (cmd.equalsIgnoreCase("Button Info…")) {
            if (ButtonGUI.gui.target != null) {
                OButton obtn = null;
                Component target = ButtonGUI.gui.target;
                if (target.getClass() == MyButton.class) obtn = ((MyButton) target).btnData;
                if (target.getClass() == RoundedCornerButton.class) obtn = ((RoundedCornerButton) target).btnData;
                if (target.getClass() == RectButton.class) obtn = ((RectButton) target).btnData;
                if (target.getClass() == RoundButton.class) obtn = ((RoundButton) target).btnData;
                if (target.getClass() == MyRadio.class) obtn = ((MyRadio) target).btnData;
                if (target.getClass() == MyCheck.class) obtn = ((MyCheck) target).btnData;
                if (target.getClass() == MyPopup.class) obtn = ((MyPopup) target).btnData;
                AuthDialog.openAuthDialog(PCARD.pc, "button", obtn);
            } else {
                new GDialog(PCARD.pc, rb.getString("dialog.No_selected_button."),
                        null, "OK", null, null);
            }
        } else if (cmd.equalsIgnoreCase("Field Info…")) {
            if (FieldGUI.gui.target != null) {
                OField obtn = null;
                Component target = FieldGUI.gui.target;
                if (target.getClass() == MyTextArea.class) obtn = ((MyTextArea) target).fldData;
                if (target.getClass() == MyScrollPane.class) obtn = ((MyScrollPane) target).fldData;

                AuthDialog.openAuthDialog(PCARD.pc, "field", obtn);
            } else {
                new GDialog(PCARD.pc, rb.getString("dialog.No_selected_field."),
                        null, "OK", null, null);
            }
        } else if (cmd.equalsIgnoreCase("Card Info…")) {
            AuthDialog.openAuthDialog(PCARD.pc, "card", PCARD.pc.stack.curCard);
        } else if (cmd.equalsIgnoreCase("Background Info…")) {
            AuthDialog.openAuthDialog(PCARD.pc, "background", PCARD.pc.stack.curCard.bg);
        } else if (cmd.equalsIgnoreCase("Stack Info…")) {
            AuthDialog.openAuthDialog(PCARD.pc, "stack", PCARD.pc.stack);
        } else if (cmd.equalsIgnoreCase("New Button")) {
            TTalk.doScriptforMenu("choose button tool");
            TTalk.doScriptforMenu("create button");
        } else if (cmd.equalsIgnoreCase("New Field")) {
            TTalk.doScriptforMenu("choose field tool");
            TTalk.doScriptforMenu("create field");
        } else if (cmd.equalsIgnoreCase("to front")) { // to front
            if (AuthTool.tool.getClass() == ButtonTool.class) {
                if (ButtonGUI.gui.target != null) {
                    OButton obtn = null;
                    Component target = ButtonGUI.gui.target;
                    if (target.getClass() == MyButton.class) obtn = ((MyButton) target).btnData;
                    if (target.getClass() == RoundedCornerButton.class) obtn = ((RoundedCornerButton) target).btnData;
                    if (target.getClass() == RectButton.class) obtn = ((RectButton) target).btnData;
                    if (target.getClass() == RoundButton.class) obtn = ((RoundButton) target).btnData;
                    if (target.getClass() == MyRadio.class) obtn = ((MyRadio) target).btnData;
                    if (target.getClass() == MyCheck.class) obtn = ((MyCheck) target).btnData;
                    if (target.getClass() == MyPopup.class) obtn = ((MyPopup) target).btnData;

                    if (obtn != null) {
                        int i = ((OCardBase) obtn.parent).GetNumberof(obtn) - 1;
                        int ip = ((OCardBase) obtn.parent).GetNumberofParts(obtn) - 1;
                        ip++;
                        if (ip >= ((OCardBase) obtn.parent).partsList.size()) ip--;
                        else if (((OCardBase) obtn.parent).partsList.get(ip).objectType.equals("button")) {
                            i++;
                        }
                        ((OCardBase) obtn.parent).partsList.remove(obtn);
                        ((OCardBase) obtn.parent).btnList.remove(obtn);
                        ((OCardBase) obtn.parent).partsList.add(ip, obtn);
                        ((OCardBase) obtn.parent).btnList.add(i, obtn);

                        OCard.reloadCurrentCard();
                        GUI.removeAllListener();
                        ButtonGUI.gui.addListenerToParts();

                        // obtn still exists but target is gone, so re-acquire it
                        ButtonGUI.gui.target = obtn.getComponent();
                    }
                } else {
                    new GDialog(PCARD.pc, rb.getString("dialog.No_selected_button."),
                            null, "OK", null, null);
                }
            }
            if (AuthTool.tool.getClass() == FieldTool.class) {
                if (FieldGUI.gui.target != null) {
                    OField obtn = null;
                    Component target = FieldGUI.gui.target;
                    if (target.getClass() == MyTextArea.class) obtn = ((MyTextArea) target).fldData;
                    if (target.getClass() == MyScrollPane.class) obtn = ((MyScrollPane) target).fldData;

                    if (obtn != null) {
                        int i = ((OCardBase) obtn.parent).GetNumberof(obtn) - 1;
                        int ip = ((OCardBase) obtn.parent).GetNumberofParts(obtn) - 1;
                        ip++;
                        if (ip >= ((OCardBase) obtn.parent).partsList.size()) ip--;
                        else if (((OCardBase) obtn.parent).partsList.get(ip).objectType.equals("field")) {
                            i++;
                        }
                        ((OCardBase) obtn.parent).partsList.remove(obtn);
                        ((OCardBase) obtn.parent).fldList.remove(obtn);
                        ((OCardBase) obtn.parent).partsList.add(ip, obtn);
                        ((OCardBase) obtn.parent).fldList.add(i, obtn);

                        OCard.reloadCurrentCard();
                        GUI.removeAllListener();
                        FieldGUI.gui.addListenerToParts();

                        // obtn still exists but target is gone, so re-acquire it
                        FieldGUI.gui.target = obtn.getComponent();
                    }
                } else {
                    new GDialog(PCARD.pc, rb.getString("dialog.No_selected_field."),
                            null, "OK", null, null);
                }
            }
        } else if (cmd.equalsIgnoreCase("to back")) { // to back
            if (AuthTool.tool.getClass() == ButtonTool.class) {
                if (ButtonGUI.gui.target != null) {
                    OButton obtn = null;
                    Component target = ButtonGUI.gui.target;
                    if (target.getClass() == MyButton.class) obtn = ((MyButton) target).btnData;
                    if (target.getClass() == RoundedCornerButton.class) obtn = ((RoundedCornerButton) target).btnData;
                    if (target.getClass() == RectButton.class) obtn = ((RectButton) target).btnData;
                    if (target.getClass() == RoundButton.class) obtn = ((RoundButton) target).btnData;
                    if (target.getClass() == MyRadio.class) obtn = ((MyRadio) target).btnData;
                    if (target.getClass() == MyCheck.class) obtn = ((MyCheck) target).btnData;
                    if (target.getClass() == MyPopup.class) obtn = ((MyPopup) target).btnData;

                    if (obtn != null) {
                        int i = ((OCardBase) obtn.parent).GetNumberof(obtn) - 1;
                        int ip = ((OCardBase) obtn.parent).GetNumberofParts(obtn) - 1;
                        ip--;
                        if (ip < 0) ip = 0;
                        else if (((OCardBase) obtn.parent).partsList.get(ip).objectType.equals("button")) {
                            i--;
                        }
                        ((OCardBase) obtn.parent).partsList.remove(obtn);
                        ((OCardBase) obtn.parent).btnList.remove(obtn);
                        ((OCardBase) obtn.parent).partsList.add(ip, obtn);
                        ((OCardBase) obtn.parent).btnList.add(i, obtn);

                        OCard.reloadCurrentCard();
                        GUI.removeAllListener();
                        ButtonGUI.gui.addListenerToParts();

                        // obtn still exists but target is gone, so re-acquire it
                        ButtonGUI.gui.target = obtn.getComponent();
                    }
                } else {
                    new GDialog(PCARD.pc, rb.getString("dialog.No_selected_button."),
                            null, "OK", null, null);
                }
            }
            if (AuthTool.tool.getClass() == FieldTool.class) {
                if (FieldGUI.gui.target != null) {
                    OField obtn = null;
                    Component target = FieldGUI.gui.target;
                    if (target.getClass() == MyTextArea.class) obtn = ((MyTextArea) target).fldData;
                    if (target.getClass() == MyScrollPane.class) obtn = ((MyScrollPane) target).fldData;

                    if (obtn != null) {
                        int i = ((OCardBase) obtn.parent).GetNumberof(obtn) - 1;
                        int ip = ((OCardBase) obtn.parent).GetNumberofParts(obtn) - 1;
                        ip--;
                        if (ip < 0) ip = 0;
                        else if (((OCardBase) obtn.parent).partsList.get(ip).objectType.equals("button")) {
                            i--;
                        }
                        ((OCardBase) obtn.parent).partsList.remove(obtn);
                        ((OCardBase) obtn.parent).fldList.remove(obtn);
                        ((OCardBase) obtn.parent).partsList.add(ip, obtn);
                        ((OCardBase) obtn.parent).fldList.add(i, obtn);

                        OCard.reloadCurrentCard();
                        GUI.removeAllListener();
                        FieldGUI.gui.addListenerToParts();

                        // obtn still exists but target is gone, so re-acquire it
                        FieldGUI.gui.target = obtn.getComponent();
                    }
                } else {
                    new GDialog(PCARD.pc, rb.getString("dialog.No_selected_field."),
                            null, "OK", null, null);
                }
            }
        } else if (cmd.equalsIgnoreCase("Delete Button")) {
            if (AuthTool.tool.getClass() == ButtonTool.class) {
                if (ButtonGUI.gui.tgtOBtn != null) {
                    ((OCardBase) ButtonGUI.gui.tgtOBtn.parent).partsList.remove(ButtonGUI.gui.tgtOBtn);
                    ((OCardBase) ButtonGUI.gui.tgtOBtn.parent).btnList.remove(ButtonGUI.gui.tgtOBtn);
                    if (ButtonGUI.gui.tgtOBtn.getComponent() != null) {
                        PCARDFrame.pc.mainPane.remove(ButtonGUI.gui.tgtOBtn.getComponent());
                    }
                    ButtonGUI.gui.target = null;
                    ButtonGUI.gui.tgtOBtn = null;

                    OCard.reloadCurrentCard();
                }
            }
        } else if (cmd.equalsIgnoreCase("Delete Field")) {
            if (AuthTool.tool.getClass() == FieldTool.class) {
                if (FieldGUI.gui.tgtOFld != null) {
                    ((OCardBase) FieldGUI.gui.tgtOFld.parent).partsList.remove(FieldGUI.gui.tgtOFld);
                    ((OCardBase) FieldGUI.gui.tgtOFld.parent).fldList.remove(FieldGUI.gui.tgtOFld);
                    if (FieldGUI.gui.tgtOFld.getComponent() != null) {
                        PCARDFrame.pc.mainPane.remove(FieldGUI.gui.tgtOFld.getComponent());
                    }
                    FieldGUI.gui.target = null;
                    FieldGUI.gui.tgtOFld = null;

                    OCard.reloadCurrentCard();
                }
            }
        } else if (cmd.equalsIgnoreCase("Cut Button") || cmd.equalsIgnoreCase("Copy Button")) {
            if (AuthTool.tool.getClass() == ButtonTool.class) {
                if (ButtonGUI.gui.tgtOBtn != null) {
                    // Get XML text
                    XMLOutputFactory factory = XMLOutputFactory.newInstance();
                    StringWriter stringWriter = new StringWriter();
                    try {
                        XMLStreamWriter writer = factory.createXMLStreamWriter(stringWriter);

                        writer.writeStartElement("buttonclip");
                        writer.writeCharacters("\n\t");

                        {
                            writer.writeStartElement("clipoption");
                            writer.writeCharacters("\n\t\t");

                            if (ButtonGUI.gui.tgtOBtn.icon != 0) {
                                writer.writeStartElement("iconpath");
                                writer.writeCharacters(/*PCARD.pc.stack.file.getParent()+File.separatorChar
                                        +*/PCARD.pc.stack.rsrc.getFilePathAll(ButtonGUI.gui.tgtOBtn.icon, "icon"));
                                writer.writeEndElement();
                                writer.writeCharacters("\n\t\t");

                                writer.writeStartElement("iconname");
                                writer.writeCharacters(PCARD.pc.stack.rsrc.getNameAll(ButtonGUI.gui.tgtOBtn.icon, "icon"));
                                writer.writeEndElement();
                                writer.writeCharacters("\n\t\t");
                            }

                            writer.writeStartElement("fontname");
                            writer.writeCharacters(ButtonGUI.gui.tgtOBtn.textFont);
                            writer.writeEndElement();
                            writer.writeCharacters("\n\t\t");

                            writer.writeStartElement("text");
                            writer.writeCharacters(ButtonGUI.gui.tgtOBtn.getText());
                            writer.writeEndElement();
                            writer.writeCharacters("\n\t");
                            writer.writeEndElement();
                            writer.writeCharacters("\n\t\t");
                        }

                        // Convert button info to XML
                        ButtonGUI.gui.tgtOBtn.writeXML(writer);

                        // Close buttonclip tag
                        writer.writeEndElement();

                        writer.close();

                        // Copy to clipboard
                        {
                            Toolkit kit = Toolkit.getDefaultToolkit();
                            Clipboard clip = kit.getSystemClipboard();

                            StringSelection ss = new StringSelection(stringWriter.toString());
                            clip.setContents(ss, ss);
                        }

                        if (cmd.equalsIgnoreCase("Cut Button")) {
                            // Delete on cut
                            ((OCardBase) ButtonGUI.gui.tgtOBtn.parent).partsList.remove(ButtonGUI.gui.tgtOBtn);
                            ((OCardBase) ButtonGUI.gui.tgtOBtn.parent).btnList.remove(ButtonGUI.gui.tgtOBtn);
                            if (ButtonGUI.gui.tgtOBtn.getComponent() != null) {
                                PCARDFrame.pc.mainPane.remove(ButtonGUI.gui.tgtOBtn.getComponent());
                            }
                            ButtonGUI.gui.target = null;
                            ButtonGUI.gui.tgtOBtn = null;

                            OCard.reloadCurrentCard();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        } else if (cmd.equalsIgnoreCase("Paste Button")) {
            if (AuthTool.tool.getClass() == ButtonTool.class) {
                Toolkit kit = Toolkit.getDefaultToolkit();
                Clipboard clip = kit.getSystemClipboard();

                String str = null;
                try {
                    str = (String) clip.getData(DataFlavor.stringFlavor);
                } catch (UnsupportedFlavorException | IOException e) {
                    e.printStackTrace();
                }
                if (str != null && str.startsWith("<buttonclip>")) {
                    XMLInputFactory factory = XMLInputFactory.newInstance();
                    XMLStreamReader reader = null;
                    try {
                        reader = factory.createXMLStreamReader(new ByteArrayInputStream(str.getBytes()));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    OCardBase cdbase = PCARD.pc.stack.curCard;
                    if (PaintTool.editBackground) {
                        cdbase = PCARD.pc.stack.curCard.bg;
                    }
                    int newid = 1;
                    for (; newid < 32767; newid++) {
                        if (cdbase.GetPartbyId(newid) == null) {
                            break;
                        }
                    }
                    OButton btn = new OButton(cdbase, newid);
                    try {
                        // Read clipoption element
                        String iconpath = "";
                        String iconname = "";
                        String fontname = "";
                        String text = "";
                        while (reader.hasNext()) {
                            int eventType = reader.next();
                            if (eventType == XMLStreamReader.START_ELEMENT) {
                                String elm = reader.getLocalName().intern();
                                switch (elm) {
                                    case "iconpath" -> iconpath = reader.getElementText();
                                    case "iconname" -> iconname = reader.getElementText();
                                    case "fontname" -> fontname = reader.getElementText();
                                    case "text" -> text = reader.getElementText();
                                }
                            } else if (eventType == XMLStreamReader.END_ELEMENT) {
                                String elm = reader.getLocalName();
                                if (elm.equals("clipoption")) {
                                    break;
                                }
                            }
                        }

                        reader = btn.readXML(reader);
                        btn.setText(text);

                        btn.textFont = fontname;
                        boolean isFound = false;
                        for (int i = 0; i < PCARD.pc.stack.fontList.size(); i++) {
                            OStack.fontClass fontinfo = PCARD.pc.stack.fontList.get(i);
                            if (fontinfo.name.equals(fontname)) {
                                isFound = true;
                                break;
                            }
                        }
                        if (!isFound) {
                            int newfontid = 0;
                            for (; newfontid < 32767; newfontid++) {
                                boolean isUsed = false;
                                for (int i = 0; i < PCARD.pc.stack.fontList.size(); i++) {
                                    OStack.fontClass fontinfo = PCARD.pc.stack.fontList.get(i);
                                    if (fontinfo.id == newfontid) {
                                        isUsed = true;
                                        break;
                                    }
                                }
                                if (!isUsed) break;
                            }
                            OStack.fontClass fontinfo = new fontClass(newfontid, fontname);
                            PCARD.pc.stack.fontList.add(fontinfo);
                        }

                        if (btn.icon != 0) {
                            String path = PCARD.pc.stack.rsrc.getFilePathAll(btn.icon, "icon");
                            // String path = PCARD.pc.stack.file.getParent();
                            /*if(fname!=null){
                                path += File.separatorChar + fname;
                            }*/
                            if (!iconpath.equals(path)) {
                                // If copying from another stack, also copy icon resources
                                if (PCARD.pc.stack.rsrc.getNameAll(btn.icon, "icon") != null) {
                                    // Icon already exists, use a different ID
                                    btn.icon = PCARD.pc.stack.rsrc.getNewResourceId("icon", btn.icon);
                                }
                                if (iconpath != null && !iconpath.isEmpty() && new File(iconpath).exists()) {
                                    // Copy file
                                    String srcfname = new File(iconpath).getName();
                                    String ext = "";
                                    if (srcfname.lastIndexOf(".") >= 0) {
                                        ext = srcfname.substring(srcfname.lastIndexOf("."));
                                    }
                                    String newFileName = "ICON_" + btn.icon + ext;
                                    Path newFilePath = PCARDFrame.pc.stack.file.getParent().resolve(newFileName);
                                    try (
                                            FileChannel srcChannel = new FileInputStream(iconpath).getChannel();
                                            FileChannel destChannel = new FileOutputStream(newFilePath.toFile()).getChannel()) {
                                        srcChannel.transferTo(0, srcChannel.size(), destChannel);
                                    }
                                    // Add to resources
                                    PCARDFrame.pc.stack.rsrc.addResource(btn.icon, "icon", iconname, newFileName);
                                }
                            }
                        }

                        cdbase.btnList.add(btn);
                        cdbase.partsList.add(btn);

                        OCard.reloadCurrentCard();

                        ButtonGUI.gui.tgtOBtn = btn;
                        ButtonGUI.gui.target = btn.getComponent();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        } else if (cmd.equalsIgnoreCase("Cut Field") || cmd.equalsIgnoreCase("Copy Field")) {
            if (AuthTool.tool.getClass() == FieldTool.class) {
                if (FieldGUI.gui.tgtOFld != null) {
                    // Get XML text
                    XMLOutputFactory factory = XMLOutputFactory.newInstance();
                    StringWriter stringWriter = new StringWriter();
                    try {
                        XMLStreamWriter writer = factory.createXMLStreamWriter(stringWriter);

                        writer.writeStartElement("fieldclip");
                        writer.writeCharacters("\n\t");

                        {
                            writer.writeStartElement("clipoption");
                            writer.writeCharacters("\n\t\t");

                            writer.writeStartElement("fontname");
                            writer.writeCharacters(FieldGUI.gui.tgtOFld.textFont);
                            writer.writeEndElement();
                            writer.writeCharacters("\n\t\t");

                            writer.writeStartElement("text");
                            writer.writeCharacters(FieldGUI.gui.tgtOFld.getText());
                            writer.writeEndElement();
                            writer.writeCharacters("\n\t");
                            writer.writeEndElement();
                            writer.writeCharacters("\n\t\t");
                        }

                        // Convert field info to XML
                        FieldGUI.gui.tgtOFld.writeXML(writer);

                        // Close fieldclip tag
                        writer.writeEndElement();

                        writer.close();

                        // Copy to clipboard
                        {
                            Toolkit kit = Toolkit.getDefaultToolkit();
                            Clipboard clip = kit.getSystemClipboard();

                            StringSelection ss = new StringSelection(stringWriter.toString());
                            clip.setContents(ss, ss);
                        }

                        if (cmd.equalsIgnoreCase("Cut Field")) {
                            // Delete on cut
                            ((OCardBase) FieldGUI.gui.tgtOFld.parent).partsList.remove(FieldGUI.gui.tgtOFld);
                            ((OCardBase) FieldGUI.gui.tgtOFld.parent).fldList.remove(FieldGUI.gui.tgtOFld);
                            if (FieldGUI.gui.tgtOFld.getComponent() != null) {
                                PCARDFrame.pc.mainPane.remove(FieldGUI.gui.tgtOFld.getComponent());
                            }
                            FieldGUI.gui.target = null;
                            FieldGUI.gui.tgtOFld = null;

                            OCard.reloadCurrentCard();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        } else if (cmd.equalsIgnoreCase("Paste Field")) {
            if (AuthTool.tool.getClass() == FieldTool.class) {
                Toolkit kit = Toolkit.getDefaultToolkit();
                Clipboard clip = kit.getSystemClipboard();

                String str = null;
                try {
                    str = (String) clip.getData(DataFlavor.stringFlavor);
                } catch (UnsupportedFlavorException | IOException e) {
                    e.printStackTrace();
                }
                if (str != null && str.startsWith("<fieldclip>")) {
                    XMLInputFactory factory = XMLInputFactory.newInstance();
                    XMLStreamReader reader = null;
                    try {
                        reader = factory.createXMLStreamReader(new ByteArrayInputStream(str.getBytes()));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    OCardBase cdbase = PCARD.pc.stack.curCard;
                    if (PaintTool.editBackground) {
                        cdbase = PCARD.pc.stack.curCard.bg;
                    }
                    int newid = 1;
                    for (; newid < 32767; newid++) {
                        if (cdbase.GetPartbyId(newid) == null) {
                            break;
                        }
                    }
                    OField fld = new OField(cdbase, newid);
                    try {
                        // Read clipoption element
                        String fontname = "";
                        String text = "";
                        while (reader.hasNext()) {
                            int eventType = reader.next();
                            if (eventType == XMLStreamReader.START_ELEMENT) {
                                String elm = reader.getLocalName().intern();
                                if (elm.equals("fontname")) {
                                    fontname = reader.getElementText();
                                } else if (elm.equals("text")) {
                                    text = reader.getElementText();
                                }
                            } else if (eventType == XMLStreamReader.END_ELEMENT) {
                                String elm = reader.getLocalName();
                                if (elm.equals("clipoption")) {
                                    break;
                                }
                            }
                        }

                        reader = fld.readXML(reader);
                        fld.setText(text);

                        fld.textFont = fontname;
                        boolean isFound = false;
                        for (int i = 0; i < PCARD.pc.stack.fontList.size(); i++) {
                            OStack.fontClass fontinfo = PCARD.pc.stack.fontList.get(i);
                            if (fontinfo.name.equals(fontname)) {
                                isFound = true;
                                break;
                            }
                        }
                        if (!isFound) {
                            int newfontid = 0;
                            for (; newfontid < 32767; newfontid++) {
                                boolean isUsed = false;
                                for (int i = 0; i < PCARD.pc.stack.fontList.size(); i++) {
                                    OStack.fontClass fontinfo = PCARD.pc.stack.fontList.get(i);
                                    if (fontinfo.id == newfontid) {
                                        isUsed = true;
                                        break;
                                    }
                                }
                                if (!isUsed) break;
                            }
                            OStack.fontClass fontinfo = new fontClass(newfontid, fontname);
                            PCARD.pc.stack.fontList.add(fontinfo);
                        }

                        cdbase.fldList.add(fld);
                        cdbase.partsList.add(fld);

                        OCard.reloadCurrentCard();

                        FieldGUI.gui.tgtOFld = fld;
                        FieldGUI.gui.target = fld.getComponent();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        } else if (cmd.equalsIgnoreCase("Background")) {
            PaintTool.editBackground = !PaintTool.editBackground;
            GMenu.changeSelected("Edit", "Background", PaintTool.editBackground);

            ButtonGUI.gui.tgtOBtn = null;
            ButtonGUI.gui.target = null;
            FieldGUI.gui.tgtOFld = null;
            FieldGUI.gui.target = null;

            OCard.reloadCurrentCard();

            String titleName = PCARD.pc.stack.name;
            if (titleName.length() > 5 && titleName.endsWith(".xstk")) {
                titleName = titleName.substring(0, titleName.length() - 5);
            }
            if (PaintTool.editBackground) {
                PCARD.pc.setTitle("// / " + titleName + " // /");
            } else {
                PCARD.pc.setTitle(titleName);
            }
        } else if (cmd.equalsIgnoreCase("Open Stack…")) {
            /*FileDialog fd = new FileDialog(PCARD.pc , rb.getString("dialog.Open_Stack") , FileDialog.LOAD);
            fd.setVisible(true);
            if(fd.getFile()!=null){
                String path = fd.getDirectory()+fd.getFile();
                TTalk.CallMessage("open stack \""+path+"\"", "", null, false, true);
            }*/

            JFileChooser chooser = new JFileChooser();
            if (PCARD.pc.stack != null && PCARD.pc.stack.file != null) {
                chooser.setCurrentDirectory(PCARD.pc.stack.file.getParent().getParent().toFile());
            }
            chooser.setDialogTitle(rb.getString("dialog.Open_Stack"));
            chooser.setDialogType(JFileChooser.OPEN_DIALOG);
            chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
            int selected = chooser.showOpenDialog(PCARD.pc);
            if (selected == JFileChooser.APPROVE_OPTION) {
                PCARD.pc.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                String path = chooser.getSelectedFile().getPath();
                String fname = new File(path).getName();
                String parentpath = new File(new File(path).getParent()).getPath();
                if (fname.length() > 5 && fname.endsWith(".hzb")) {
                    // ok
                } else if (parentpath.length() > 5 && parentpath.endsWith(".hzb")) {
                    // When opening files in .hzb, parent opens
                    path = parentpath;
                }

                TTalk.CallMessage("open stack \"" + path + "\"", "", null, false, true);
            }
        } else if (cmd.equalsIgnoreCase("New Stack…")) {
            /*FileDialog fd = new FileDialog(PCARD.pc , rb.getString("dialog.New_Stack") , FileDialog.SAVE);
            fd.setVisible(true);
            if(fd.getFile()!=null){
                String path = fd.getDirectory()+fd.getFile();*/
            // MacOSX standard file dialog shows unnecessary overwrite confirmation, so make custom one
            JFileChooser chooser = new JFileChooser();
            if (PCARD.pc.stack != null && PCARD.pc.stack.file != null) {
                chooser.setCurrentDirectory(PCARD.pc.stack.file.getParent().getParent().toFile());
            }
            chooser.setDialogTitle(rb.getString("dialog.New_Stack"));
            chooser.setDialogType(JFileChooser.SAVE_DIALOG);
            chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            chooser.setFileFilter(new FileFilter() {
                @Override
                public boolean accept(File f) {
                    // Hide items with hzb extension
                    String name = f.getName();
                    int n = name.lastIndexOf('.');
                    if (n > 0) {
                        String ext = name.substring(n);
                        if (ext.equalsIgnoreCase(".hzb")) {
                            return false;
                        }
                    }
                    if (f.list() != null && f.list().length != 0) {
                        return false;
                    }
                    return true;
                }

                @Override
                public String getDescription() {
                    return PCARD.AppName + " stack(*.hzb)";
                }
            });

            int selected = chooser.showSaveDialog(PCARD.pc);
            if (selected == JFileChooser.APPROVE_OPTION) {
                try {
                    // Start progress display
                    PCARD.pc.stack.barDialog = new JDialog(PCARD.pc);
                    PCARD.pc.stack.barDialog.setUndecorated(true);
                    PCARD.pc.stack.barDialog.getContentPane().setLayout(new BorderLayout());
                    PCARD.pc.stack.bar = new JProgressBar();
                    PCARD.pc.stack.bar.setStringPainted(true);
                    PCARD.pc.stack.bar.setSize(new Dimension(PCARD.pc.getWidth(), 20));
                    PCARD.pc.stack.bar.setString("");
                    PCARD.pc.stack.bar.setValue(0);
                    PCARD.pc.stack.barOffset = 0;
                    PCARD.pc.stack.barDialog.add("Center", PCARD.pc.stack.bar);
                    PCARD.pc.stack.barDialog.setBounds(PCARD.pc.getBounds());
                    PCARD.pc.stack.barDialog.setVisible(true);
                    PCARD.pc.stack.bar.paintImmediately(PCARD.pc.stack.bar.getBounds());
                    PCARD.pc.stack.bar.setString("Make a new stack");

                    String path = chooser.getSelectedFile().getPath();

                    // Add hzb extension
                    int n = path.lastIndexOf('.');
                    if (n > 0) {
                        String ext = path.substring(n);
                        if (!ext.equalsIgnoreCase(".hzb")) {
                            path += ".hzb";
                        }
                    } else {
                        path += ".hzb";
                    }

                    Path f = Path.of(path);
                    if (Files.exists(f)) {
                        // Overwrite confirmation
                        new GDialog(PCARDFrame.pc, rb.getString("dialog.This_file_name_already_exists._If_you_continue,_the_existing_file_will_be_replaced."),
                                null, rb.getString("dialog.Cancel"), "OK", null);
                        if (GDialog.clicked.equals(rb.getString("dialog.Cancel"))) {
                            PCARD.pc.stack.barDialog.remove(PCARD.pc.stack.bar);
                            PCARD.pc.stack.barDialog.dispose();
                            return;
                        }
                        int j = 1;
                        Path renameStr;
                        while (true) {
                            renameStr = Path.of(System.getProperty("java.io.tmpdir"), PCARD.AppName + "_renamed" + j);
                            if (!Files.exists(renameStr)) {
                                break;
                            }
                            j++;
                        }
                        Files.move(f, renameStr);
                    }

                    PCARD.pc.setTitle("");

                    Files.createDirectory(f); // Create directory
                    if (Files.exists(f)) {
                        PCARD.pc.stack.bar.setValue(10);
                        PCARD.pc.stack.bar.paintImmediately(PCARD.pc.stack.bar.getBounds());

                        Path xmlFilePath = f.getParent().resolve("_stack.xml");

                        // Create stack instance
                        OStack newStack = new OStack(PCARD.pc);
                        newStack.file = xmlFilePath;
                        newStack.rsrc = new Rsrc(newStack);
                        newStack.scriptList = new ArrayList<>();
                        newStack.width = 640;
                        newStack.height = 480;

                        // Create card and background instances
                        OCard cd = new OCard(newStack);
                        OBackground bg = new OBackground(newStack);

                        cd.id = (int) (Math.random() * 9000 + 1000);
                        bg.id = (int) (Math.random() * 1000 + cd.id + 1);
                        cd.bgid = bg.id;

                        newStack.AddNewCard(cd.id);
                        // newStack.cdCacheList.add(cd);
                        newStack.AddNewBg(bg);
                        newStack.firstCard = cd.id;
                        newStack.firstBg = bg.id;

                        newStack.createdByVersion = PCARD.longVersion;
                        newStack.lastCompactedVersion = PCARD.longVersion;
                        newStack.firstEditedVersion = PCARD.longVersion;
                        newStack.userLevel = 5;

                        for (int i = 0; i < 40; i++) {
                            newStack.Pattern[i] = "PAT_" + (i + 1) + ".png";

                            Path ifile = Path.of(".", "resource", "PAT_" + (i + 1) + ".png");
                            Path ofile = f.getParent().resolve("PAT_" + (i + 1) + ".png");
                            BufferedImage bi = ImageIO.read(ifile.toFile());
                            ImageIO.write(bi, "png", ofile.toFile());
                        }

                        PCARD.pc.stack.bar.setValue(30);
                        PCARD.pc.stack.bar.paintImmediately(PCARD.pc.stack.bar.getBounds());

                        int fontid = Integer.parseInt(rb.getString("menu.fontid"));
                        String fontname = rb.getString("menu.fontname");
                        OStack.fontClass fontClass = new fontClass(fontid, fontname);
                        newStack.fontList.add(fontClass);

                        PCARD.pc.stack.bar.setValue(50);
                        PCARD.pc.stack.bar.paintImmediately(PCARD.pc.stack.bar.getBounds());

                        // XML writing
                        XMLwrite xmlWrite = new XMLwrite(newStack);
                        xmlWrite.saveStackNow();
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        newStack.clean();

                        PCARD.pc.stack.bar.setValue(80);
                        PCARD.pc.stack.bar.paintImmediately(PCARD.pc.stack.bar.getBounds());

                        PCARD.pc.stack.barDialog.remove(PCARD.pc.stack.bar);
                        PCARD.pc.stack.barDialog.dispose();

                        // Open XML
                        TTalk.CallMessage("open stack \"" + f + "\"", "", null, false, true);
                    } else {
                        new GDialog(PCARDFrame.pc, rb.getString("dialog.Can't_create_a_new_folder."),
                                null, "OK", null, null);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else if (cmd.equalsIgnoreCase("New Card")) {
            if (PCARD.pc.stack.curCard == null) return;

            // Create card instance
            OCard cd = new OCard(PCARD.pc.stack);

            cd.id = (int) (Math.random() * 100 + 1 + PCARD.pc.stack.GetMaxCardId());
            cd.bgid = PCARD.pc.stack.curCard.bg.id;
            cd.changed = true;

            PCARD.pc.stack.AddNewCard(PCARD.pc.stack.GetNumberof(PCARD.pc.stack.curCard), cd.id);
            TTalk.CallMessage("go cd id " + cd.id, "", null, false, true);
        } else if (cmd.equalsIgnoreCase("New Background")) {
            if (PCARD.pc.stack.curCard == null) return;

            // Create background instance
            OBackground bg = new OBackground(PCARD.pc.stack);
            bg.id = (int) (Math.random() * 100 + 1 + PCARD.pc.stack.GetMaxCardId());
            bg.changed = true;
            PCARD.pc.stack.AddNewBg(bg);

            // Create card instance
            OCard cd = new OCard(PCARD.pc.stack);
            cd.id = (int) (Math.random() * 100 + 1 + PCARD.pc.stack.GetMaxCardId());
            cd.bgid = bg.id;
            cd.changed = true;
            PCARD.pc.stack.AddNewCard(PCARD.pc.stack.GetNumberof(PCARD.pc.stack.curCard), cd.id);

            TTalk.CallMessage("go cd id " + cd.id, "", null, false, true);
        } else if (cmd.equalsIgnoreCase("Delete Card")) {
            if (1 == PCARD.pc.stack.cardIdList.size()) {
                new GDialog(PCARDFrame.pc, rb.getString("dialog.At_least_one_card_is_required."),
                        null, "OK", null, null);
                return;
            }
            int previd = 0;
            for (int i = 0; i < PCARD.pc.stack.cardIdList.size(); i++) {
                if (PCARD.pc.stack.cardIdList.get(i) == PCARD.pc.stack.curCard.id) {
                    if (i > 0) {
                        previd = PCARD.pc.stack.cardIdList.get(i - 1);
                    } else {
                        // For the first card
                        previd = PCARD.pc.stack.cardIdList.get(i + 1);
                        PCARD.pc.stack.firstCard = previd;
                        PCARD.pc.stack.firstBg = PCARD.pc.stack.GetCardbyId(previd).bgid;
                    }
                    // Delete card
                    PCARD.pc.stack.cardIdList.remove(i);

                }
            }
            for (int i = 0; i < PCARD.pc.stack.cdCacheList.size(); i++) {
                if (PCARD.pc.stack.cdCacheList.get(i).id == PCARD.pc.stack.curCard.id) {
                    // Delete card
                    PCARD.pc.stack.cdCacheList.remove(i);
                }
            }
            int bgid = PCARD.pc.stack.curCard.bgid;
            boolean useBg = false;
            for (int i = 0; i < PCARD.pc.stack.cdCacheList.size(); i++) {
                if (PCARD.pc.stack.cdCacheList.get(i).bgid == bgid) {
                    useBg = true;
                    break;
                }
            }
            if (!useBg) {
                // Delete because bg reference count is 0
                for (int j = 0; j < PCARD.pc.stack.bgCacheList.size(); j++) {
                    if (PCARD.pc.stack.bgCacheList.get(j).id == bgid) {
                        PCARD.pc.stack.bgCacheList.remove(j);
                        break;
                    }
                }
            }

            if (previd != 0) {
                TTalk.CallMessage("go cd id " + previd, "", null, false, true);
            }
        } else if (cmd.equalsIgnoreCase("Icon…")) {
            new ResEdit(PCARD.pc, "icon", null);
        } else if (cmd.equalsIgnoreCase("Sound…")) {
            new ResEdit(PCARD.pc, "sound", null);
        } else if (cmd.equalsIgnoreCase("Resource…")) {
            new ResEditIndex(PCARD.pc);
        } else if (cmd.equalsIgnoreCase("Clear This Menu")) {
            {
                File recentFile = new File("resource_trash" + File.separatorChar + "recent.txt");
                FileOutputStream fos = null;
                try {
                    fos = new FileOutputStream(recentFile);
                    fos.write("".getBytes());
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            GMenu.BuildRecentMenu((JMenu) PCARD.pc.getJMenuBar().getComponent(0));
        } else if (cmd.equalsIgnoreCase("Quit HyperCard") || cmd.equalsIgnoreCase("Quit")) {
            if (PCARD.pc.stack != null) {
                TTalk.doScriptforMenu("closeCard");
                if (PCARD.pc.stack.curCard != null && PCARD.pc.stack.curCard.bg != null) {
                    TTalk.doScriptforMenu("closeBackground");
                }
                TTalk.doScriptforMenu("closeStack");

                // Waiting for stack save
                for (int j = 0; j < 100; j++) {
                    boolean flag = false;
                    if (PCARD.pc.stack.changed) {
                        flag = true;
                    }
                    for (int i = 0; i < PCARD.pc.stack.cdCacheList.size(); i++) {
                        if (PCARD.pc.stack.cdCacheList.get(i).changed) {
                            flag = true;
                            break;
                        }
                    }
                    for (int i = 0; i < PCARD.pc.stack.bgCacheList.size(); i++) {
                        if (PCARD.pc.stack.bgCacheList.get(i).changed) {
                            flag = true;
                            break;
                        }
                    }
                    if (flag) {
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                        }
                    } else {
                        break;
                    }
                }
            }

            // End
            System.exit(0);
        } else {
            for (int i = 0; i < GMenu.recentsMenuName.length; i++) {
                if (cmd.equalsIgnoreCase(GMenu.recentsMenuName[i])) {
                    TTalk.CallMessage("open stack \"" + GMenu.recentsMenuPath[i] + "\"", "", null, false, true);
                    return;
                }
            }
            // Does not match any menu
            {
                GMenuPaint.doMenu(cmd);
            }
        }
    }
}
