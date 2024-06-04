package com.github.hyperZebra;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Insets;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.Serial;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;


public class GMsg extends JDialog {

    /**
     *
     */
    @Serial
    private static final long serialVersionUID = 1L;
    static GMsg msg = null;
    static JTextField area;

    GMsg(Frame owner, String text) {
        super(owner);
        OWindow.msgwindow = new OWindow(this);
        /*getContentPane().*/
        setLayout(null);// new BorderLayout());
        // this.setBackground(Color.WHITE);

        // パネルを追加する
        // JPanel topPanel = new JPanel();
        // topPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        // getContentPane().add("North",topPanel);

        area = new JTextField(text);
        area.setPreferredSize(new Dimension(480, 28));
        area.setMargin(new Insets(0, 0, 0, 0));
        area.setBorder(new EmptyBorder(0, 8, 0, 0));
        area.setBounds(0, 0, 480, 28);
        area.setOpaque(false);
        // topPanel.add(area);
        /*getContentPane().*/
        add(area);

        setBounds(owner.getX() + owner.getWidth() / 2 - 240, owner.getY() + owner.getHeight(), 480, 48/*+PCARD.pc.getInsets().top*/);
        setResizable(false);
        setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        // setVisible(true);

        area.addKeyListener(new GMsgListener());

        addKeyListener(GUI.gui);// キー入力は共通で使う

        msg = this;

        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                msg.setSize(msg.getSize().width, 48 + msg.getInsets().top);
            }

            @Override
            public void componentShown(ComponentEvent e) {
                area.setCaretPosition(0);
                area.requestFocus();
                area.requestFocusInWindow();
            }
        });
    }

    public static void setText(String text) {
        StringBuilder newstr = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            String substr = text.substring(i, i + 1);
            if (0 != substr.compareTo("\n") && 0 != substr.compareTo("\r")) newstr.append(substr);
        }

        area.setText(newstr.toString());
        if (!msg.isVisible()) {
            msg.setVisible(true);
            PCARDFrame.pc.stack.pcard.setVisible(true);
        }
    }

    public static String getText() {
        return area.getText();
    }
}

class GMsgListener implements KeyListener {

    @Override
    public void keyPressed(KeyEvent e) {

        switch (e.getKeyCode()) {
            case KeyEvent.VK_ENTER:
                String str = GMsg.getText();
                if (str.contains("\n")) str = str.substring(0, str.indexOf("\n"));
                String res = null;
                try {
                    res = TTalk.doScriptforMenu(str);
                } catch (xTalkException e1) {
                    e1.printStackTrace();
                    Object[] options = {"OK"};
                    JOptionPane.showOptionDialog(PCARDFrame.pc.stack.pcard,
                            "Script Error: " + e1.getMessage(),
                            "Script Error",
                            JOptionPane.YES_OPTION,
                            JOptionPane.WARNING_MESSAGE, null, options, options[0]);
                }
                if (res != null && !res.isEmpty()) GMsg.setText(res);
                else GMsg.setText(str);
                PCARDFrame.pc.stack.pcard.toFront();
                break;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_ENTER:
                String str = GMsg.getText();
                if (str.contains("\n")) str = str.substring(0, str.indexOf("\n"));
                GMsg.setText(str);
                break;
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

}