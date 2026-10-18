package com.github.hyperZebra;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.Serial;
import java.nio.file.Path;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;


public class GDialog extends JDialog implements ActionListener {

    @Serial
    private static final long serialVersionUID = 1L;
    static String clicked = "";
    static String inputText = "";
    static JTextField inputArea;
    static JButton defaultButton;

    GDialog(Frame owner, String text, String input, String b1, String b2, String b3) {
        super(owner, true);
        getContentPane().setLayout(new BorderLayout());

        // Add panel
        JPanel btmPanel = new JPanel();
        getContentPane().add("South", btmPanel);
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        getContentPane().add("North", topPanel);

        String[] texts2 = text.split("\r");
        StringBuilder text2 = new StringBuilder();
        for (String s : texts2) {
            text2.append(s).append("\n");
        }
        JTextArea area = new JTextArea(text2.toString());
        String[] texts = text2.toString().split("\n");
        area.setSize(380, 18 + 18 * texts.length);
//        area.setPreferredSize(new Dimension(380, 18+18*texts.length));
        area.setMargin(new Insets(16, 16, 2, 16));
        area.setLineWrap(true);
        area.setOpaque(false);
        area.setEditable(false);
        area.setFocusable(false);
        topPanel.add(area);

        if (b1 != null) {
            JButton btn1 = new JButton(b1);
            btn1.addActionListener(this);
            btmPanel.add(btn1);
        }
        if (b2 != null) {
            JButton btn2 = new JButton(b2);
            btn2.addActionListener(this);
            btmPanel.add(btn2);
        }
        if (b3 != null) {
            JButton btn3 = new JButton(b3);
            btn3.addActionListener(this);
            btmPanel.add(btn3);
            getRootPane().setDefaultButton(btn3);
            defaultButton = btn3;
        }

        if (input != null) {
            JTextField area2 = new JTextField(input);
            area2.setSize(350, 24);
            area2.setMargin(new Insets(16, 16, 16, 16));
            topPanel.add(area2);
            inputArea = area2;
        }

        int h = area.getPreferredSize().height;
        if (h > 500) h = 500;
        if (inputArea != null) h += inputArea.getPreferredSize().height;
        if (owner != null) {
            setBounds(owner.getX() + owner.getWidth() / 2 - 200, owner.getY() + owner.getHeight() / 2 - h / 2, 400, h + 48);
        } else {
            setBounds(0, 0, 400, h + 48);
            setLocationRelativeTo(null);
        }
        setUndecorated(true); // Hide title bar

        SwingUtilities.invokeLater(() -> {
            if (defaultButton != null) defaultButton.requestFocus();
        });

        setVisible(true);

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        clicked = e.getActionCommand();
        if (inputArea != null) inputText = inputArea.getText();
        inputArea = null;
        this.dispose();
    }
}


class GScrollDialog extends JDialog implements ActionListener {

    @Serial
    private static final long serialVersionUID = 1L;

    static String clicked = "";
    static String inputText = "";
    static JTextField inputArea;
    static JButton defaultButton;

    GScrollDialog(Frame owner, String text, String input, String b1, String b2, String b3) {
        super(owner, true);
        getContentPane().setLayout(new BorderLayout());

        // Add panel
        JPanel btmPanel = new JPanel();
        getContentPane().add("South", btmPanel);
        //JPanel topPanel = new JPanel();
        //topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));


        String[] texts2 = text.split("\r");
        StringBuilder text2 = new StringBuilder();
        for (String s : texts2) {
            text2.append(s).append("\n");
        }
        JTextArea area = new JTextArea(text2.toString());
        String[] texts = text2.toString().split("\n");
        area.setSize(380, 18 + 18 * texts.length);
        //area.setPreferredSize(new Dimension(380, 18 + 18 * texts.length));
        area.setMargin(new Insets(16, 16, 2, 16));
        area.setLineWrap(true);
        area.setOpaque(false);
        area.setEditable(false);
        area.setFocusable(false);
        //topPanel.add(area);
        JScrollPane scrollpane = new JScrollPane(area);
        getContentPane().add("North", scrollpane);
        scrollpane.setSize(new Dimension(380, 400));

        if (b1 != null) {
            JButton btn1 = new JButton(b1);
            btn1.addActionListener(this);
            btmPanel.add(btn1);
        }
        if (b2 != null) {
            JButton btn2 = new JButton(b2);
            btn2.addActionListener(this);
            btmPanel.add(btn2);
        }
        if (b3 != null) {
            JButton btn3 = new JButton(b3);
            btn3.addActionListener(this);
            btmPanel.add(btn3);
            getRootPane().setDefaultButton(btn3);
            defaultButton = btn3;
        }

        int h = area.getPreferredSize().height;
        if (h > 500) h = 500;
        if (inputArea != null) h += inputArea.getPreferredSize().height;
        if (owner != null) {
            setBounds(owner.getX() + owner.getWidth() / 2 - 200, owner.getY() + owner.getHeight() / 2 - h / 2, 400, h + 48);
        } else {
            setBounds(0, 0, 400, h + 48);
            setLocationRelativeTo(null);
        }
        setUndecorated(true); // Hide title bar

        SwingUtilities.invokeLater(() -> {
            if (defaultButton != null) defaultButton.requestFocus();
        });

        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        clicked = e.getActionCommand();
        if (inputArea != null) inputText = inputArea.getText();
        inputArea = null;
        this.dispose();
    }
}


class GUxAnswer extends JDialog implements ActionListener {

    @Serial
    private static final long serialVersionUID = 1L;

    static String clicked = "";
    static String inputText = "";
    static JTextField inputArea;
    static JButton defaultButton;

    GUxAnswer(Frame owner, String text, String input, int[] icons, int iconwait, int time, String[] buttons) {
        super(owner, true);
        getContentPane().setLayout(new BorderLayout());

        // Add panel
        JPanel btmPanel = new JPanel();
        getContentPane().add("South", btmPanel);
        JPanel topPanel = new JPanel();
        //topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        getContentPane().add("North", topPanel);

        JPanel topPanel2 = new JPanel();
        topPanel.add(topPanel2);

        // Load icon
        String fileName = PCARD.pc.stack.rsrc.getFilePathAll(icons[0], "icon");
        if (fileName != null) {
            Path path = (PCARD.pc.stack.file.getParent().resolve(fileName));

            JLabel label = new JLabel(new ImageIcon(path.toString()));
            topPanel2.add(label);
        }

        // Text
        String[] texts2 = text.split("\r");
        StringBuilder text2 = new StringBuilder();
        for (String s : texts2) {
            text2.append(s).append("\n");
        }
        JTextArea area = new JTextArea(text2.toString());
        String[] texts = text2.toString().split("\n");
        area.setSize(360, 18 + 18 * texts.length);
        //area.setPreferredSize(new Dimension(380, 18 + 18 * texts.length));
        area.setMargin(new Insets(16, 16, 2, 16));
        area.setLineWrap(true);
        area.setOpaque(false);
        area.setEditable(false);
        area.setFocusable(false);
        topPanel2.add(area);

        for (String button : buttons) {
            if (button != null) {
                JButton btn = new JButton(button);
                btn.addActionListener(this);
                btmPanel.add(btn);
                getRootPane().setDefaultButton(btn);
                defaultButton = btn;
            }
        }

        if (input != null) {
            JTextField area2 = new JTextField(input);
            area2.setSize(350, 24);
            area2.setMargin(new Insets(16, 16, 16, 16));
            topPanel.add(area2);
            inputArea = area2;
        }

        int h = area.getPreferredSize().height;
        if (h > 500) h = 500;
        if (inputArea != null) h += inputArea.getPreferredSize().height;
        if (owner != null) {
            setBounds(owner.getX() + owner.getWidth() / 2 - 200, owner.getY() + owner.getHeight() / 2 - h / 2, 400, h + 48);
        } else {
            setBounds(0, 0, 400, h + 48);
            setLocationRelativeTo(null);
        }
        setUndecorated(true); // Hide title bar

        SwingUtilities.invokeLater(() -> {
            if (defaultButton != null) defaultButton.requestFocus();
        });

        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        clicked = e.getActionCommand();
        if (inputArea != null) inputText = inputArea.getText();
        inputArea = null;
        this.dispose();
    }
}