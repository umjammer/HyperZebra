package com.github.hyperZebra;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.Serial;
import java.util.ResourceBundle;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JToggleButton;
import javax.swing.JWindow;
// import javax.swing.plaf.basic.BasicToolBarUI;


public class GToolBar {

    private static final ResourceBundle rb = ResourceBundle.getBundle("messages");

    toolBarListener toolBarListener = new toolBarListener();
    //JToolBar tb;
    JWindow tb;
    //CPButton fore;
    //CPButton back;
    //GradButton grad;
    //PatButton pat;
    TBButton selectToolButton;
    private boolean gvisible;

    GToolBar(PCARDFrame owner) {
        // JPanel panel = new JPanel();
        tb = new JWindow();

        // tb.setBounds(PCARD.pc.getX()-72,PCARD.pc.getY(),72,200);
        tb.setLayout(null);
        tb.setAlwaysOnTop(true);
        tb.addMouseMotionListener(toolBarListener);

        // panel.setLayout(null);
        // tb.setPreferredSize(new Dimension(80,120));
        // tb.setSize(new Dimension(80,80));
        // tb.add(panel);
        // panel.setMinimumSize(new Dimension(80,120));
        // panel.setMaximumSize(new Dimension(80,120));

        TBButton btn;
        ButtonGroup grp = new ButtonGroup();
        TBButtonListener listener = new TBButtonListener();

        JButton jbtn;
        tb.add(jbtn = new JButton());
        jbtn.setName("Close");
        jbtn.setBounds(4, 2, 12, 12);
        jbtn.setIcon(new ImageIcon("./resource/tb_close.png"));
        jbtn.setRolloverIcon(new ImageIcon("./resource/tb_close2.png"));
        jbtn.setContentAreaFilled(false);
        jbtn.setBorderPainted(false);
        jbtn.addActionListener(listener);

        tb.add(jbtn = new JButton());
        jbtn.setName("Bar");
        jbtn.setBounds(0, 0, 78, 16);
        jbtn.setIcon(new ImageIcon("./resource/tb_bar.png"));
        jbtn.setPressedIcon(new ImageIcon("./resource/tb_bar.png"));
        jbtn.setContentAreaFilled(false);
        jbtn.setBorderPainted(false);
        jbtn.addMouseMotionListener(toolBarListener);

        tb.add(btn = new TBButton(rb.getString("main.Browse"), 0, 0));
        grp.add(btn);
        btn.addActionListener(listener);
        btn.addMouseListener(listener);
        btn.setSelected(true);

        tb.add(btn = new TBButton(rb.getString("main.Button"), 0, 1));
        grp.add(btn);
        btn.addActionListener(listener);
        btn.addMouseListener(listener);

        tb.add(btn = new TBButton(rb.getString("main.Field"), 0, 2));
        grp.add(btn);
        btn.addActionListener(listener);
        btn.addMouseListener(listener);

        tb.add(btn = new TBButton(rb.getString("main.Select"), 1, 0));
        grp.add(btn);
        btn.addActionListener(listener);
        btn.addMouseListener(listener);
        selectToolButton = btn;

        tb.add(btn = new TBButton(rb.getString("main.Lasso"), 1, 1));
        grp.add(btn);
        btn.addActionListener(listener);
        btn.addMouseListener(listener);
        tb.add(btn = new TBButton(rb.getString("main.MagicWand"), 1, 2));
        grp.add(btn);
        btn.addActionListener(listener);
        btn.addMouseListener(listener);

        tb.add(btn = new TBButton(rb.getString("main.Pencil"), 2, 0));
        grp.add(btn);
        btn.addActionListener(listener);
        btn.addMouseListener(listener);

        tb.add(btn = new TBButton(rb.getString("main.Brush"), 2, 1));
        grp.add(btn);
        btn.addActionListener(listener);
        btn.addMouseListener(listener);

        tb.add(btn = new TBButton(rb.getString("main.Eraser"), 2, 2));
        grp.add(btn);
        btn.addActionListener(listener);
        btn.addMouseListener(listener);

        tb.add(btn = new TBButton(rb.getString("main.Line"), 3, 0));
        grp.add(btn);
        btn.addActionListener(listener);
        btn.addMouseListener(listener);

        // tb.add(new TBButton(rb.getString("main.SprayCan"),3,0));
        tb.add(btn = new TBButton(rb.getString("main.Rect"), 3, 1));
        grp.add(btn);
        btn.addActionListener(listener);
        btn.addMouseListener(listener);

        // tb.add(new TBButton(rb.getString("main.RoundRect"),3,2));
        tb.add(btn = new TBButton(rb.getString("main.Oval"), 3, 2));
        grp.add(btn);
        btn.addActionListener(listener);
        btn.addMouseListener(listener);

        tb.add(btn = new TBButton(rb.getString("main.PaintBucket"), 4, 0));
        grp.add(btn);
        btn.addActionListener(listener);
        btn.addMouseListener(listener);

        // tb.add(new TBButton(rb.getString("main.Curve"),4,2));
        tb.add(btn = new TBButton(rb.getString("main.Type"), 4, 1));
        grp.add(btn);
        btn.addActionListener(listener);
        btn.addMouseListener(listener);

        // tb.add(new TBButton(rb.getString("main.Polygon"),5,1));
        // tb.add(new TBButton(rb.getString("main.FreePolygon"),5,2));
        // tb.add(new TBButton(rb.getString("main.Spoit"),6,0));
        // tb.addSeparator();
        owner.fore = new CPButton(Color.BLACK, 5, 0, false);
        tb.add(owner.fore);
        owner.back = new CPButton(Color.WHITE, 5, 1, true);
        tb.add(owner.back);
        owner.pat = new PatButton(11, 6, 0);
        tb.add(owner.pat);
        owner.grad = new GradButton(Color.BLACK, Color.WHITE, 6, 1);
        tb.add(owner.grad);
        tb.add(owner.trans = new TransButton(rb.getString("main.Transparency"), 6, 2));
        owner.trans.addActionListener(listener);
        owner.trans.addMouseListener(listener);

        // panel.setPreferredSize(new Dimension(80,80));
        // panel.setSize(new Dimension(80,80));

        tb.addComponentListener(toolBarListener);

        // tb.pack();
        tb.setVisible(false);
        // owner.add(tb, BorderLayout.WEST);
    }

    int getTWidth() {
        return 0;
        /*if(tb==null) return 0;
        if(!tb.isVisible()) return 0;
        if (((BasicToolBarUI)tb.getUI()).isFloating()) return 0;
        if (tb.getOrientation()==JToolBar.HORIZONTAL) return 0;
        Rectangle r = tb.getBounds();
        return r.width;*/
    }

    int getTHeight() {
        return 0;
        /*if(tb==null) return 0;
        if(!tb.isVisible()) return 0;
        if (((BasicToolBarUI)tb.getUI()).isFloating()) return 0;
        if (tb.getOrientation()==JToolBar.VERTICAL) return 0;
        Rectangle r = tb.getBounds();
        return r.height;*/
    }

    void activate() {
        tb.setVisible(gvisible);
    }

    void deactivate() {
        gvisible = tb.isVisible();
        tb.setVisible(false);
    }
}


class toolBarListener implements ComponentListener, MouseMotionListener {

    @Override
    public void componentMoved(ComponentEvent e) {
        // PCARD.stack.setNewBounds();
    }

    @Override
    public void componentResized(ComponentEvent e) {
        // PCARD.pc.setNewBounds();
    }

    @Override
    public void componentHidden(ComponentEvent e) {
        // PCARD.pc.setNewBounds();
        GMenu.changeMenuName("Tool", "Hide ToolBar", "Show ToolBar");
    }

    @Override
    public void componentShown(ComponentEvent e) {
        // PCARD.pc.setNewBounds();
        GMenu.changeMenuName("Tool", "Show ToolBar", "Hide ToolBar");
    }

    boolean isDragStart;
    Point p = new Point(0, 0);

    @Override
    public void mouseDragged(MouseEvent e) {
        if (!isDragStart) {
            isDragStart = true;
            p.x = e.getX();
            p.y = e.getY();
        } else {
            JWindow tb = PCARD.pc.toolbar.tb;
            tb.setLocation(e.getXOnScreen() - p.x, e.getYOnScreen() - p.y);
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        isDragStart = false;
    }
}


class TBButton extends JToggleButton {

    private static final ResourceBundle rb = ResourceBundle.getBundle("messages");

    @Serial
    private static final long serialVersionUID = 1L;

    JPopupMenu popup = new JPopupMenu(); // Generate popup menu
    // Color color;

    TBButton(String in_text, int y, int x) {
        super();
        String text = PCARD.getEngText("main", in_text);
        setName(text);
        this.setBounds(x * 26, y * 28 + 16, 26, 26);
        setMargin(new Insets(0, 0, 0, 0));
        this.setFocusable(false);

        if (rb.getString("main.Brush").equals(in_text)) {
            int[] size = {1, 2, 3, 4, 6, 8, 16, 32};
            for (int j : size) {
                addPopupMenuItem("Brush " + j, e -> {
                            PaintTool.brushSize = Integer.parseInt(((JCheckBoxMenuItem) (e.getSource())).getText().split(" ")[1]);
                            TBCursor.changeCursor(PCARD.pc);

                            for (int i = 0; i < popup.getComponentCount(); i++) {
                                if (popup.getComponent(i) instanceof JCheckBoxMenuItem item) {
                                    item.setSelected(false);
                                }
                            }
                            ((JCheckBoxMenuItem) (e.getSource())).setSelected(true);
                        },
                        (j == PaintTool.brushSize));
            }
        }

        if (rb.getString("main.Line").equals(in_text)) {
            float[] size = {0, 0.5f, 1, 1.5f, 2, 3, 4, 5, 6, 8, 10, 12, 16};
            for (float v : size) {
                addPopupMenuItem("Line " + v, e -> {
                            PaintTool.lineSize = Float.parseFloat(((JCheckBoxMenuItem) (e.getSource())).getText().split(" ")[1]);
                            TBCursor.changeCursor(PCARD.pc);

                            for (int i = 0; i < popup.getComponentCount(); i++) {
                                if (popup.getComponent(i) instanceof JCheckBoxMenuItem item) {
                                    item.setSelected(false);
                                }
                            }
                            ((JCheckBoxMenuItem) (e.getSource())).setSelected(true);
                        },
                        (v == PaintTool.lineSize));
            }
        }

        if (rb.getString("main.Rect").equals(in_text) ||
                rb.getString("main.Oval").equals(in_text)) {
            addPopupMenuItem(rb.getString("main.Fill"), e -> {
                        PaintTool.owner.fill = true;

                        for (int i = 0; i < popup.getComponentCount(); i++) {
                            if (popup.getComponent(i) instanceof JCheckBoxMenuItem item) {
                                item.setSelected(false);
                            }
                        }
                        ((JCheckBoxMenuItem) (e.getSource())).setSelected(true);
                    },
                    PaintTool.owner.fill);
            addPopupMenuItem(rb.getString("main.Don't_Fill"), e -> {
                        PaintTool.owner.fill = false;

                        for (int i = 0; i < popup.getComponentCount(); i++) {
                            if (popup.getComponent(i) instanceof JCheckBoxMenuItem item) {
                                item.setSelected(false);
                            }
                        }
                        ((JCheckBoxMenuItem) (e.getSource())).setSelected(true);
                    },
                    !PaintTool.owner.fill);
        }

        if (rb.getString("main.MagicWand").equals(in_text)) {
            int[] size = {0, 1, 2, 3, 5, 7, 10, 15, 20, 25};
            for (int j : size) {
                addPopupMenuItem("Color " + j + " %", e -> {
                            PaintTool.smartSelectPercent = Integer.parseInt(((JCheckBoxMenuItem) (e.getSource())).getText().split(" ")[1]);
                            TBCursor.changeCursor(PCARD.pc);

                            for (int i = 0; i < popup.getComponentCount(); i++) {
                                if (popup.getComponent(i) instanceof JCheckBoxMenuItem item) {
                                    item.setSelected(false);
                                }
                            }
                            ((JCheckBoxMenuItem) (e.getSource())).setSelected(true);
                        },
                        (j == PaintTool.smartSelectPercent));
            }
        }

        try {
            BufferedImage bi = javax.imageio.ImageIO.read(new File("./resource/tb_button1.png"));
            BufferedImage bi2 = javax.imageio.ImageIO.read(new File("./resource/tb_" + PCARD.getEngText("main", text) + ".png"));
            bi.createGraphics().drawImage(bi2, 0, 0, this);
            ImageIcon icon = new ImageIcon(bi);
            setIcon(icon);

            bi = javax.imageio.ImageIO.read(new File("./resource/tb_button2.png"));
            bi.createGraphics().drawImage(bi2, 0, 0, this);
            icon = new ImageIcon(bi);
            setSelectedIcon(icon);

            setContentAreaFilled(false);
            setBorderPainted(false);
        } catch (IOException e) {
            //logger.log(Level.ERROR, e.getMessage(), e);
        }
    }

    // Add menu item
    private JMenuItem addPopupMenuItem(String name, ActionListener al, boolean check) {
        JCheckBoxMenuItem item = new JCheckBoxMenuItem(name);
        item.addActionListener(al);
        item.setSelected(check);
        // item.setBounds(0,0,getPreferredSize().width, getPreferredSize().height);
        popup.add(item);
        return item;
    }
}


class TBButtonListener implements ActionListener, MouseListener {

    private static final ResourceBundle rb = ResourceBundle.getBundle("messages");

    static String lastcmd = "Browse";

    @Override
    public void mouseClicked(MouseEvent arg0) {
        String in_cmd = ((JComponent) arg0.getSource()).getName();
        String cmd = PCARD.getText("main", in_cmd);
        if (javax.swing.SwingUtilities.isRightMouseButton(arg0) ||
                arg0.getClickCount() == 2) {
            if (rb.getString("main.Brush").equals(cmd)) {
                ((TBButton) arg0.getSource()).popup.show(arg0.getComponent(), arg0.getX(), arg0.getY());
            }
            if (rb.getString("main.Rect").equals(cmd)) {
                ((TBButton) arg0.getSource()).popup.show(arg0.getComponent(), arg0.getX(), arg0.getY());
            }
            if (rb.getString("main.Oval").equals(cmd)) {
                ((TBButton) arg0.getSource()).popup.show(arg0.getComponent(), arg0.getX(), arg0.getY());
            }
            if (rb.getString("main.Line").equals(cmd)) {
                ((TBButton) arg0.getSource()).popup.show(arg0.getComponent(), arg0.getX(), arg0.getY());
            }
            if (rb.getString("main.MagicWand").equals(cmd)) {
                ((TBButton) arg0.getSource()).popup.show(arg0.getComponent(), arg0.getX(), arg0.getY());
            }
            if (rb.getString("main.Transparency").equals(cmd)) {
                ((TransButton) arg0.getSource()).popup.show(arg0.getComponent(), arg0.getX(), arg0.getY());
            }
            if (rb.getString("main.Type").equals(cmd)) {
                new GFontDialog(PaintTool.owner, PCARD.pc.textFont, PCARD.pc.textSize, PCARD.pc.textStyle, -1);
                PCARD.pc.textFont = GFontDialog.selectedFont;
                PCARD.pc.textSize = GFontDialog.selectedSize * PaintTool.owner.bit;
                PCARD.pc.textStyle = GFontDialog.selectedStyle;
                // PCARD.pc.textAlign = GFontDialog.selectedAlign;
            }
        }

        if (arg0.getClickCount() == 2) {
            // Double click
            if (rb.getString("main.Eraser").equals(cmd)) {
                GMenuPaint.doMenu("Select All");
                GMenuPaint.doMenu("Clear Selection");
            }
            if (rb.getString("main.Select").equals(cmd)) {
                GMenuPaint.doMenu("Select All");
            }
            if (rb.getString("main.Lasso").equals(cmd)) {
                GMenuPaint.doMenu("choose lasso tool");
                PaintTool.mouseDown(0, 0);
                PaintTool.mouseStillDown(0, 1000);
                PaintTool.mouseStillDown(0, 10000);
                PaintTool.mouseStillDown(1000, 10000);
                PaintTool.mouseStillDown(10000, 10000);
                PaintTool.mouseStillDown(10000, 1000);
                PaintTool.mouseStillDown(10000, 0);
                PaintTool.mouseStillDown(1000, 0);
                PaintTool.mouseStillDown(0, 0);
                PaintTool.mouseUp(0, 0);
            }
            if (rb.getString("main.Pencil").equals(cmd)) {
                GMenuPaint.doMenu("FatBits");
            }
//            if (rb.getString("main.Button").equals(cmd)) {
//                try {
//                    GMenuBrowse.doMenu("New Button");
//                } catch (XTalkException e) {
//                    logger.log(Level.ERROR, e.getMessage(), e);
//                }
//            }
//            if (rb.getString("main.Field").equals(cmd)) {
//                try {
//                    GMenuBrowse.doMenu("New Field");
//                } catch (XTalkException e) {
//                    logger.log(Level.ERROR, e.getMessage(), e);
//                }
//            }
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        String in_cmd = ((JComponent) ae.getSource()).getName();
        String cmd = PCARD.getText("main", in_cmd);
        ChangeTool(cmd, ae);
//        if (((TBButton) ae.getSource()).getParent() == PCARD.pc.stack.toolbar.tb) {
//            PCARD.pc.toFront();
//        }
        if (rb.getString("main.Close").equals(cmd)) {
            PCARD.pc.toolbar.tb.setVisible(false);
        }
    }

    static public boolean ChangeTool(String cmd, ActionEvent ae) {

        // End processing of previous tool
        if (PCARD.pc.tool != null) {
            PCARD.pc.tool.end();

            PaintTool.saveCdPictures();
        }
        if (AuthTool.tool != null) {
            ButtonGUI.gui.removeListenerFromParts();
            FieldGUI.gui.removeListenerFromParts();

            AuthTool.tool.end();
        }

        // Change toolbar highlight
        if (PCARD.pc.stack != null) {
            for (int i = 0; i < PCARD.pc.toolbar.tb.getContentPane().getComponentCount(); i++) {
                JComponent component = (JComponent) PCARD.pc.toolbar.tb.getContentPane().getComponent(i);
                if (component.getClass() == TBButton.class) {
                    if (component.getName().equalsIgnoreCase(cmd)) {
                        ((TBButton) component).setSelected(true);
                    } else {
                        ((TBButton) component).setSelected(false);
                    }
                }
            }
        }

        if (rb.getString("main.Browse").equalsIgnoreCase(cmd) ||
                "Browse".equalsIgnoreCase(cmd) ||
                rb.getString("main.Button").equalsIgnoreCase(cmd) ||
                "Button".equalsIgnoreCase(cmd) ||
                rb.getString("main.Field").equalsIgnoreCase(cmd) ||
                "Field".equalsIgnoreCase(cmd)) {
            if (PCARD.pc.tool != null) {
                // Return from paint mode since browse tool was selected
                PCARD.pc.tool = null;
                PCARD.pc.bit = 1;
                PCARD.pc.getRootPane().removeMouseListener(paintGUI.gui);
                PCARD.pc.getRootPane().removeMouseMotionListener(paintGUI.gui);
                paintGUI.gui.removeListenerFromParts();

                //PCARD.pc.stack.curCard.pict.getGraphics().drawImage(PCARD.pc.mainImg, 0, 0, PCARD.pc);
                //PCARD.pc.stack.curCard.bg.pict.getGraphics().drawImage(PCARD.pc.bgImg, 0, 0, PCARD.pc);
                PCARD.pc.mainPane.repaint();

                //PCARD.pc.paIdle.interrupt();
                //PCARD.pc.pidle = new Pidle();
                //PCARD.pc.pidle.start();

                {
                    // Paint buffer
                    PCARD.pc.mainImg = null;
                    PCARD.pc.bgImg = null;
                    PCARD.pc.undoBuf = null;
                    PCARD.pc.redoBuf = null;
                }

                // This doesn't switch
                //PCARD.pc.setJMenuBar(PCARD.pc.menu.mb);

                GMenu.menuUpdate(PCARD.pc.menu.mb);
            }
        }

        {
            GMenu.changeSelected("Tool", PCARD.getText("main", lastcmd), true);
            GMenu.changeSelected("Tool", PCARD.getText("main", cmd), true);
            lastcmd = cmd;
        }

        if (rb.getString("main.Button").equalsIgnoreCase(cmd) ||
                "Button".equalsIgnoreCase(cmd)) {
            AuthTool.tool = new ButtonTool();
            TBCursor.changeCursor(PCARD.pc);

            GUI.removeAllListener();
            ButtonGUI.gui.addListenerToParts();
            PCARD.pc.mainPane.addMouseListener(ButtonGUI.gui);
            PCARD.pc.mainPane.addMouseMotionListener(ButtonGUI.gui);

            // Menu
            {
                GMenu.changeEnabled("Go", "Background", true);
                GMenu.changeEnabled("Objects", "Button Info…", true);
                GMenu.changeEnabled("Objects", "Field Info…", false);
                GMenu.changeEnabled("Objects", "Bring Closer", true);
                GMenu.changeEnabled("Objects", "Send Farther", true);

                GMenu.changeMenuName("Edit", "Cut", "Cut Button");
                GMenu.changeMenuName("Edit", "Copy", "Copy Button");
                GMenu.changeMenuName("Edit", "Paste", "Paste Button");
                GMenu.changeMenuName("Edit", "Delete", "Delete Button");
                GMenu.changeMenuName("Edit", "Cut Field", "Cut Button");
                GMenu.changeMenuName("Edit", "Copy Field", "Copy Button");
                GMenu.changeMenuName("Edit", "Paste Field", "Paste Button");
                GMenu.changeMenuName("Edit", "Delete Field", "Delete Button");
            }

            return true;
        } else if (rb.getString("main.Field").equalsIgnoreCase(cmd) ||
                "Field".equalsIgnoreCase(cmd)) {
            AuthTool.tool = new FieldTool();
            TBCursor.changeCursor(PCARD.pc);

            GUI.removeAllListener();
            FieldGUI.gui.addListenerToParts();
            PCARD.pc.mainPane.addMouseListener(FieldGUI.gui);
            PCARD.pc.mainPane.addMouseMotionListener(FieldGUI.gui);

            // Menu
            {
                GMenu.changeEnabled("Edit", "Background", true);
                GMenu.changeEnabled("Objects", "Button Info…", false);
                GMenu.changeEnabled("Objects", "Field Info…", true);
                GMenu.changeEnabled("Objects", "Bring Closer", true);
                GMenu.changeEnabled("Objects", "Send Farther", true);

                GMenu.changeMenuName("Edit", "Cut", "Cut Field");
                GMenu.changeMenuName("Edit", "Copy", "Copy Field");
                GMenu.changeMenuName("Edit", "Paste", "Paste Field");
                GMenu.changeMenuName("Edit", "Delete", "Delete Field");
                GMenu.changeMenuName("Edit", "Cut Button", "Cut Field");
                GMenu.changeMenuName("Edit", "Copy Button", "Copy Field");
                GMenu.changeMenuName("Edit", "Paste Button", "Paste Field");
                GMenu.changeMenuName("Edit", "Delete Button", "Delete Field");
            }

            return true;
        } else {
            // Except button and field tools
            if (AuthDialog.authDialog != null) AuthDialog.authDialog.dispose();
            AuthDialog.authDialog = null;
        }

        if (rb.getString("main.Browse").equalsIgnoreCase(cmd) ||
                "Browse".equalsIgnoreCase(cmd)) {
            AuthTool.tool = null;
            TBCursor.changeCursor(PCARD.pc);

            // Exit background edit mode
            {
                PaintTool.editBackground = false;
                String titleName = PCARD.pc.stack.name;
                if (titleName.length() > 5 && titleName.endsWith(".xstk")) {
                    titleName = titleName.substring(0, titleName.length() - 5);
                }
                PCARD.pc.setTitle(titleName);
            }

            OCard.reloadCurrentCard();

            // Menu
            {
                GMenu.changeEnabled("Edit", "Background", false);
                GMenu.changeSelected("Edit", "Background", false);
                GMenu.changeEnabled("Objects", "Button Info…", false);
                GMenu.changeEnabled("Objects", "Field Info…", false);
                GMenu.changeEnabled("Objects", "Bring Closer", false);
                GMenu.changeEnabled("Objects", "Send Farther", false);
            }

            return true;
        }

        //
        // Below is paint tool
        //

        if (rb.getString("main.Select").equalsIgnoreCase(cmd) ||
                "Select".equalsIgnoreCase(cmd)) {
            PCARD.pc.tool = new SelectTool();
            TBCursor.changeCursor(PCARD.pc);
        } else if (rb.getString("main.Lasso").equalsIgnoreCase(cmd) ||
                "Lasso".equalsIgnoreCase(cmd)) {
            PCARD.pc.tool = new LassoTool();
            TBCursor.changeCursor(PCARD.pc);
        } else if (rb.getString("main.MagicWand").equalsIgnoreCase(cmd) ||
                "MagicWand".equalsIgnoreCase(cmd)) {
            PCARD.pc.tool = new SmartSelectTool();
            TBCursor.changeCursor(PCARD.pc);
        } else if (rb.getString("main.Brush").equalsIgnoreCase(cmd) ||
                "Brush".equalsIgnoreCase(cmd)) {
            PCARD.pc.tool = new BrushTool();
            TBCursor.changeCursor(PCARD.pc);
        } else if (rb.getString("main.PaintBucket").equalsIgnoreCase(cmd) ||
                "PaintBucket".equalsIgnoreCase(cmd) ||
                "bucket".equalsIgnoreCase(cmd)) {
            PCARD.pc.tool = new PaintBucketTool();
            TBCursor.changeCursor(PCARD.pc);
        } else if (rb.getString("main.Transparency").equalsIgnoreCase(cmd) ||
                "Transparency".equalsIgnoreCase(cmd)) {
            if (ae != null) {
                ((TransButton) ae.getSource()).popup.show(((TransButton) ae.getSource()), /*((TBButton)arg0.getSource()).getLocation().x*/0, 0/*((TBButton)arg0.getSource()).getLocation().y*/);
            }
        } else if (rb.getString("main.Type").equalsIgnoreCase(cmd) ||
                "Type".equalsIgnoreCase(cmd) ||
                "text".equalsIgnoreCase(cmd)) {
            PCARD.pc.tool = new TypeTool();
            TBCursor.changeCursor(PCARD.pc);
        } else if (rb.getString("main.Pencil").equalsIgnoreCase(cmd) ||
                "Pencil".equalsIgnoreCase(cmd)) {
            PCARD.pc.tool = new PencilTool();
            TBCursor.changeCursor(PCARD.pc);
        } else if (rb.getString("main.Eraser").equalsIgnoreCase(cmd) ||
                "Eraser".equalsIgnoreCase(cmd)) {
            PCARD.pc.tool = new EraserTool();
            TBCursor.changeCursor(PCARD.pc);
        } else if (rb.getString("main.Rect").equalsIgnoreCase(cmd) ||
                "Rect".equalsIgnoreCase(cmd) ||
                "rectangle".equalsIgnoreCase(cmd)) {
            PCARD.pc.tool = new RectTool();
            TBCursor.changeCursor(PCARD.pc);
        } else if (rb.getString("main.Line").equalsIgnoreCase(cmd) ||
                "Line".equalsIgnoreCase(cmd)) {
            PCARD.pc.tool = new LineTool();
            TBCursor.changeCursor(PCARD.pc);
        } else if (rb.getString("main.Oval").equalsIgnoreCase(cmd) ||
                "Oval".equalsIgnoreCase(cmd)) {
            PCARD.pc.tool = new OvalTool();
            TBCursor.changeCursor(PCARD.pc);
        } else if (cmd.equals("DummyPaint")) {
            TBCursor.changeCursor(PCARD.pc);
        } else {
            return false; // Not found
        }

        if (PCARD.pc.stack.curCard != null && PCARD.pc.mainImg == null) {
            {
                // Paint buffer

                PCARD.pc.mainImg = new BufferedImage(PCARD.pc.stack.width, PCARD.pc.stack.height, BufferedImage.TYPE_INT_ARGB);

                PCARD.pc.bgImg = new BufferedImage(PCARD.pc.stack.width, PCARD.pc.stack.height, BufferedImage.TYPE_INT_ARGB);
                // If this is RGB, it won't be compatible with undoBuf
                Graphics g = PCARD.pc.bgImg.getGraphics();
                g.setColor(Color.WHITE);
                g.fillRect(0, 0, PCARD.pc.stack.width, PCARD.pc.stack.height);

                PCARD.pc.undoBuf = new BufferedImage(PCARD.pc.stack.width, PCARD.pc.stack.height, BufferedImage.TYPE_INT_ARGB);

                PCARD.pc.redoBuf = new BufferedImage(PCARD.pc.stack.width, PCARD.pc.stack.height, BufferedImage.TYPE_INT_ARGB);
            }

            // Move card picture to paint buffer
            PCARD.pc.mainImg.getGraphics().drawImage(PCARD.pc.stack.curCard.pict, 0, 0, PCARD.pc);
            if (PCARD.pc.stack.curCard.bg != null && PCARD.pc.stack.curCard.bg.pict != null) {
                PCARD.pc.bgImg.getGraphics().drawImage(PCARD.pc.stack.curCard.bg.pict, 0, 0, PCARD.pc);
            }
            PCARD.pc.mainPane.repaint();

            // Paint listener
            GUI.removeAllListener();
            paintGUI.gui.addListenerToParts();
            PCARD.pc.mainPane.addMouseListener(paintGUI.gui);
            PCARD.pc.mainPane.addMouseMotionListener(paintGUI.gui);

            // This doesn't switch
            // PCARD.pc.setJMenuBar(PCARD.pc.paintMenu.mb);

            GMenu.menuUpdate(PCARD.pc.paintMenu.mb);

            // PCARD.pc.pidle.interrupt();
//            PCARD.pc.paIdle = new PaintIdle();
//            PCARD.pc.paIdle.start();

            // Menu
            {
                GMenu.changeEnabled("Edit", "Background", true);
            }
        }

        // Don't pass focus to field
        if (PCARD.pc.stack.curCard != null) {
            for (int i = 0; i < PCARD.pc.stack.curCard.fldList.size(); i++) {
                PCARD.pc.stack.curCard.fldList.get(i).fld.setFocusable(false);
                PCARD.pc.stack.curCard.fldList.get(i).fld.setEditable(false);
            }
            for (int i = 0; i < PCARD.pc.stack.curCard.bg.fldList.size(); i++) {
                PCARD.pc.stack.curCard.bg.fldList.get(i).fld.setFocusable(false);
                PCARD.pc.stack.curCard.bg.fldList.get(i).fld.setEditable(false);
            }
        }

        return true;
    }
}


class TBCursor {

    static public void changeCursor(PCARDFrame frame) {
        if (frame.tool == null) {
            if (AuthTool.tool == null) {
                frame.mainPane.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            } else if (AuthTool.tool.getClass() == ButtonTool.class) {
                frame.mainPane.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            } else if (AuthTool.tool.getClass() == FieldTool.class) {
                frame.mainPane.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            }
        } else if (frame.tool.getClass() == BrushTool.class) {
            BufferedImage bi = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
            int i = PaintTool.brushSize;
            Graphics2D g2 = (Graphics2D) bi.getGraphics();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(0, 0, 0));
            g2.fillOval(16 - i / 2, 16 - i / 2, i, i);

            Point hotSpot = new Point(16, 16);
            String name = "brush-cursor";
            Toolkit kit = Toolkit.getDefaultToolkit();
            Cursor cr = kit.createCustomCursor(bi, hotSpot, name);
            frame.mainPane.setCursor(cr);
            if (PaintTool.owner != null && PaintTool.owner.tool != null) {
                ((BrushTool) PaintTool.owner.tool).cursor = cr;
            }
        } else if (frame.tool.getClass() == PaintBucketTool.class) {
            frame.mainPane.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        } else if (frame.tool.getClass() == SelectTool.class) {
            frame.mainPane.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
        } else if (frame.tool.getClass() == TypeTool.class) {
            frame.mainPane.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
        } else if (frame.tool.getClass() == PencilTool.class) {
            frame.mainPane.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        } else if (frame.tool.getClass() == LassoTool.class) {
            frame.mainPane.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        } else if (frame.tool.getClass() == SmartSelectTool.class) {
            frame.mainPane.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        } else if (frame.tool.getClass() == EraserTool.class) {
            BufferedImage bi = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
            int i = 16;
            Graphics2D g2 = (Graphics2D) bi.getGraphics();
            g2.setColor(Color.WHITE);
            g2.fillRect(16 - i / 2, 16 - i / 2, i, i);
            g2.setColor(Color.BLACK);
            g2.drawRect(16 - i / 2, 16 - i / 2, i, i);

            Point hotSpot = new Point(16, 16);
            String name = "eraser-cursor";
            Toolkit kit = Toolkit.getDefaultToolkit();
            Cursor cr = kit.createCustomCursor(bi, hotSpot, name);
            frame.mainPane.setCursor(cr);
            if (PaintTool.owner != null && PaintTool.owner.tool != null) {
                ((EraserTool) PaintTool.owner.tool).cursor = cr;
            }
        } else if (frame.tool.getClass() == RectTool.class) {
            frame.mainPane.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
        } else if (frame.tool.getClass() == LineTool.class) {
            frame.mainPane.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
        } else if (frame.tool.getClass() == OvalTool.class) {
            frame.mainPane.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
        }
    }
}
