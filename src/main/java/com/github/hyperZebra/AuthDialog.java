package com.github.hyperZebra;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.Serial;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.ResourceBundle;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;


/**
 * Information dialog related code
 * <p>
 * The tool's own functionality is in ButtonGUI etc. in GUI.java
 */
public class AuthDialog extends JDialog {

    @Serial
    private static final long serialVersionUID = 1L;

    private static final Logger logger = System.getLogger(AuthDialog.class.getName());

    private static final ResourceBundle rb = ResourceBundle.getBundle("messages");

    static AuthDialog authDialog;
    static OObject object;

    private AuthDialog(Frame owner, String type, OObject obj) {
        super(owner, false);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                authDialog = null;
            }
        });
    }

    static void openAuthDialog(Frame owner, String type, OObject obj) {
        if (PCARD.pc.stack == null || PCARD.pc.stack.curCard == null) {
            return;
        }

        boolean newOpen = false;
        if (authDialog == null) {
            newOpen = true;
            authDialog = new AuthDialog(owner, type, obj);
        } else {
            authDialog.getContentPane().removeAll();
        }

        object = obj;

        //getContentPane().setLayout(new GridLayout(6, 1));
        authDialog.getContentPane().setLayout(new FlowLayout());

        JLabel label;
        JTextField jfield;
        JCheckBox check;
        JComboBox<?> popup;
        JButton jbtn;

        int w = 320;
        int h = 440;

        // button
        if (type.equals("button")) {
            OButton obtn = (OButton) obj;
            AuthButtonListener btnlistener = new AuthButtonListener();

            // Add panel
            {
                JPanel namePanel = new JPanel();
                authDialog.getContentPane().add(namePanel);

                label = new JLabel(rb.getString("dialog.Button_Name:"));
                namePanel.add(label);
                jfield = new JTextField(obtn.name);
                jfield.setName("name");
                jfield.setPreferredSize(new Dimension(w * 2 / 3, jfield.getPreferredSize().height));
                AuthTextListener listener = new AuthTextListener(jfield);
                jfield.getDocument().addDocumentListener(listener);
                namePanel.add(jfield);

                namePanel.setPreferredSize(new Dimension(w, jfield.getPreferredSize().height + 4));
            }

            // Add panel
            {
                JPanel infoPanel = new JPanel();
                infoPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
                authDialog.getContentPane().add(infoPanel);

                String typeStr = "";
                if (obtn.parent.objectType.equals("card")) typeStr = rb.getString("dialog.Card_button_");
                else typeStr = rb.getString("dialog.Background_button_");
                label = new JLabel(typeStr + rb.getString("dialog.ID:_") + obtn.id);
                label.setPreferredSize(new Dimension(w, label.getPreferredSize().height));
                infoPanel.add(label);
                label = new JLabel(typeStr + rb.getString("dialog.Number:_") + ((OCardBase) obtn.parent).GetNumberof(obtn));
                label.setPreferredSize(new Dimension(w, label.getPreferredSize().height));
                infoPanel.add(label);
                label = new JLabel(typeStr + rb.getString("dialog.Part_Number:_") + ((OCardBase) obtn.parent).GetNumberofParts(obtn));
                label.setPreferredSize(new Dimension(w, label.getPreferredSize().height));
                infoPanel.add(label);

                infoPanel.setPreferredSize(new Dimension(w, 24 * 3));
            }

            // Add panel
            {
                JPanel stylePanel = new JPanel();
                stylePanel.setLayout(new FlowLayout(FlowLayout.LEFT));
                authDialog.getContentPane().add(stylePanel);

                label = new JLabel(rb.getString("dialog.Style:"));
                stylePanel.add(label);
                popup = new JComboBox<Object>(new String[] {
                        rb.getString("dialog.Standard"),
                        rb.getString("dialog.Transparent"),
                        rb.getString("dialog.Opaque"),
                        rb.getString("dialog.Rectangle"),
                        rb.getString("dialog.Shadow"),
                        rb.getString("dialog.RoundRect"),
                        rb.getString("dialog.Default"),
                        rb.getString("dialog.Oval"),
                        rb.getString("dialog.Popup"),
                        rb.getString("dialog.CheckBox"),
                        rb.getString("dialog.Radio")});
                popup.setSelectedIndex(obtn.style);
                popup.setName("style");
                popup.setMaximumRowCount(17);
                popup.addActionListener(btnlistener);
                stylePanel.add(popup);

                stylePanel.setPreferredSize(new Dimension(w, popup.getPreferredSize().height + 4));
            }

            // Add panel
            {
                JPanel famiPanel = new JPanel();
                famiPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
                authDialog.getContentPane().add(famiPanel);

                label = new JLabel(rb.getString("dialog.Family:"));
                famiPanel.add(label);
                popup = new JComboBox<Object>(new String[] {
                        rb.getString("dialog.None"), "1", "2", "3",
                        "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16"});
                popup.setSelectedIndex(obtn.group);
                popup.setName("family");
                popup.setMaximumRowCount(17);
                popup.addActionListener(btnlistener);
                famiPanel.add(popup);

                famiPanel.setPreferredSize(new Dimension(w, popup.getPreferredSize().height + 4));
            }

            // Add panel
            {
                JPanel propPanel = new JPanel();
                propPanel.setLayout(new GridLayout(3, 2));
                authDialog.getContentPane().add(propPanel);

                check = new JCheckBox(rb.getString("dialog.Show_Name"));
                check.setSelected(obtn.showName);
                check.addActionListener(btnlistener);
                propPanel.add(check);
                check = new JCheckBox(rb.getString("dialog.Enabled"));
                check.setSelected(obtn.enabled);
                check.addActionListener(btnlistener);
                propPanel.add(check);
                check = new JCheckBox(rb.getString("dialog.Visible"));
                check.setSelected(obtn.getVisible());
                check.addActionListener(btnlistener);
                propPanel.add(check);
                check = new JCheckBox(rb.getString("dialog.Scale_Icon"));
                check.setSelected(obtn.getScaleIcon());
                check.addActionListener(btnlistener);
                propPanel.add(check);
                check = new JCheckBox(rb.getString("dialog.Auto_Hilite"));
                check.setSelected(obtn.getAutoHilite());
                check.addActionListener(btnlistener);
                propPanel.add(check);
                if (obtn.parent.objectType.equals("background")) {
                    check = new JCheckBox(rb.getString("dialog.Shared_Hilite"));
                    check.setSelected(obtn.sharedHighlight);
                    check.addActionListener(btnlistener);
                    propPanel.add(check);
                }

                propPanel.setPreferredSize(new Dimension(w, check.getPreferredSize().height * 3));
            }

            // Add panel
//            {
//                JPanel emptyPanel = new JPanel();
//                authDialog.getContentPane().add(emptyPanel);
//
//                emptyPanel.setPreferredSize(new Dimension(w, 8));
//            }

            // Add panel
            {
                JPanel colorPanel = new JPanel();
                authDialog.getContentPane().add(colorPanel);


                CPButton fore = new AuthColorButton(obtn.color, obtn, 0);
                colorPanel.add(fore);
                CPButton back = new AuthColorButton(obtn.bgColor, obtn, 1);
                colorPanel.add(back);

                colorPanel.setPreferredSize(new Dimension(w, 32));
            }

            // Add panel
            {
                JPanel buttonPanel = new JPanel();
                buttonPanel.setLayout(new GridLayout(3, 2));
                authDialog.getContentPane().add(buttonPanel);

                jbtn = new JButton(rb.getString("dialog.Font…"));
                jbtn.addActionListener(btnlistener);
                buttonPanel.add(jbtn);
                jbtn = new JButton(rb.getString("dialog.Icon…"));
                jbtn.addActionListener(btnlistener);
                buttonPanel.add(jbtn);
//                jbtn = new JButton(rb.getString("dialog.LinkTo…"));
//                jbtn.addActionListener(btnlistener);
//                jbtn.setEnabled(false);
//                buttonPanel.add(jbtn);
//                jbtn = new JButton(rb.getString("dialog.Effect…"));
//                jbtn.addActionListener(btnlistener);
//                jbtn.setEnabled(false);
//                buttonPanel.add(jbtn);
                jbtn = new JButton(rb.getString("dialog.Script…"));
                jbtn.addActionListener(btnlistener);
                buttonPanel.add(jbtn);
                jbtn = new JButton(rb.getString("dialog.Content…"));
                jbtn.addActionListener(btnlistener);
                buttonPanel.add(jbtn);

                buttonPanel.setPreferredSize(new Dimension(w, check.getPreferredSize().height * 3 + 8));
            }
        }

        // field
        if (type.equals("field")) {
            OField ofld = (OField) obj;

            JCheckBox multiplelinesbtn;
            JCheckBox fixedlineheightbtn;
            multiplelinesbtn = new JCheckBox(rb.getString("dialog.Multiple_lines"));
            fixedlineheightbtn = new JCheckBox(rb.getString("dialog.Fixed_line_height"));

            AuthFieldListener fldlistener = new AuthFieldListener(multiplelinesbtn, fixedlineheightbtn);

            // Add panel
            {
                JPanel namePanel = new JPanel();
                authDialog.getContentPane().add(namePanel);

                label = new JLabel(rb.getString("dialog.Field_Name:"));
                namePanel.add(label);
                jfield = new JTextField(ofld.name);
                jfield.setName("name");
                jfield.setPreferredSize(new Dimension(w * 2 / 3, jfield.getPreferredSize().height));
                AuthTextListener listener = new AuthTextListener(jfield);
                jfield.getDocument().addDocumentListener(listener);
                namePanel.add(jfield);

                namePanel.setPreferredSize(new Dimension(w, jfield.getPreferredSize().height + 4));
            }

            // Add panel
            {
                JPanel infoPanel = new JPanel();
                infoPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
                authDialog.getContentPane().add(infoPanel);

                String typeStr = "";
                if (ofld.parent.objectType.equals("card")) typeStr = rb.getString("dialog.Card_field_");
                else typeStr = rb.getString("dialog.Background_field_");
                label = new JLabel(typeStr + rb.getString("dialog.ID:_") + ofld.id);
                label.setPreferredSize(new Dimension(w, label.getPreferredSize().height));
                infoPanel.add(label);
                label = new JLabel(typeStr + rb.getString("dialog.Number:_") + ((OCardBase) ofld.parent).GetNumberof(ofld));
                label.setPreferredSize(new Dimension(w, label.getPreferredSize().height));
                infoPanel.add(label);
                label = new JLabel(typeStr + rb.getString("dialog.Part_Number:_") + ((OCardBase) ofld.parent).GetNumberofParts(ofld));
                label.setPreferredSize(new Dimension(w, label.getPreferredSize().height));
                infoPanel.add(label);

                infoPanel.setPreferredSize(new Dimension(w, 24 * 3));
            }

            // Add panel
            {
                JPanel stylePanel = new JPanel();
                stylePanel.setLayout(new FlowLayout(FlowLayout.LEFT));
                authDialog.getContentPane().add(stylePanel);

                label = new JLabel(rb.getString("dialog.Style:"));
                stylePanel.add(label);
                popup = new JComboBox<Object>(new String[] {
//                        rb.getString("dialog.Standard"),
                        rb.getString("dialog.Transparent"),
                        rb.getString("dialog.Opaque"),
                        rb.getString("dialog.Rectangle"),
                        rb.getString("dialog.Shadow"),
                        rb.getString("dialog.Scroll")});
                popup.setSelectedIndex(ofld.style - 1);
                popup.setName("style");
                popup.setMaximumRowCount(17);
                popup.addActionListener(fldlistener);
                stylePanel.add(popup);

                stylePanel.setPreferredSize(new Dimension(w, popup.getPreferredSize().height + 4));
            }

            // Add panel
            {
                JPanel propPanel = new JPanel();
                propPanel.setLayout(new GridLayout(6, 2));
                authDialog.getContentPane().add(propPanel);

                check = new JCheckBox(rb.getString("dialog.Lock_text"));
                check.setSelected(ofld.enabled);
                check.addActionListener(fldlistener);
                propPanel.add(check);
                check = new JCheckBox(rb.getString("dialog.Visible"));
                check.setSelected(ofld.getVisible());
                check.addActionListener(fldlistener);
                propPanel.add(check);
                check = new JCheckBox(rb.getString("dialog.Don't_wrap"));
                check.setSelected(ofld.dontWrap);
                check.addActionListener(fldlistener);
                propPanel.add(check);
                check = new JCheckBox(rb.getString("dialog.Auto_select"));
                check.setSelected(ofld.autoSelect);
                check.addActionListener(fldlistener);
                propPanel.add(check);
//                check = new JCheckBox(rb.getString("dialog.Multiple_lines"));
                multiplelinesbtn.setEnabled(ofld.autoSelect);
                multiplelinesbtn.setSelected(ofld.multipleLines);
                multiplelinesbtn.addActionListener(fldlistener);
                propPanel.add(multiplelinesbtn);
                check = new JCheckBox(rb.getString("dialog.Wide_margins"));
                check.setSelected(ofld.wideMargins);
                check.addActionListener(fldlistener);
                propPanel.add(check);
//                check = new JCheckBox(rb.getString("dialog.Fixed_line_height"));
                fixedlineheightbtn.setSelected(ofld.fixedLineHeight);
                fixedlineheightbtn.addActionListener(fldlistener);
                propPanel.add(fixedlineheightbtn);
                check = new JCheckBox(rb.getString("dialog.Show_lines"));
                check.setSelected(ofld.showLines);
                check.addActionListener(fldlistener);
                propPanel.add(check);
                check = new JCheckBox(rb.getString("dialog.Auto_tab"));
                check.setSelected(ofld.autoTab);
                check.addActionListener(fldlistener);
                propPanel.add(check);
                check = new JCheckBox(rb.getString("dialog.Don't_search"));
                check.setSelected(ofld.dontSearch);
                check.addActionListener(fldlistener);
                propPanel.add(check);
                if (ofld.parent.objectType.equals("background")) {
                    check = new JCheckBox(rb.getString("dialog.Shared_text"));
                    check.setSelected(ofld.sharedText);
                    check.addActionListener(fldlistener);
                    propPanel.add(check);
                }

                propPanel.setPreferredSize(new Dimension(w, check.getPreferredSize().height * 5));
            }

            // Add panel
//            {
//                JPanel emptyPanel = new JPanel();
//                authDialog.getContentPane().add(emptyPanel);
//
//                emptyPanel.setPreferredSize(new Dimension(w, 8));
//            }
            // Add panel
            {
                JPanel colorPanel = new JPanel();
                authDialog.getContentPane().add(colorPanel);


                CPButton fore = new AuthColorButton(ofld.color, ofld, 0);
                colorPanel.add(fore);
                CPButton back = new AuthColorButton(ofld.bgColor, ofld, 1);
                colorPanel.add(back);

                colorPanel.setPreferredSize(new Dimension(w, 32));
            }

            // Add panel
            {
                JPanel buttonPanel = new JPanel();
                buttonPanel.setLayout(new GridLayout(1, 2));
                authDialog.getContentPane().add(buttonPanel);

                jbtn = new JButton(rb.getString("dialog.Font…"));
                jbtn.addActionListener(fldlistener);
                buttonPanel.add(jbtn);
                jbtn = new JButton(rb.getString("dialog.Script…"));
                jbtn.addActionListener(fldlistener);
                buttonPanel.add(jbtn);

                buttonPanel.setPreferredSize(new Dimension(w, check.getPreferredSize().height * 1 + 4));
            }
        }

        // card
        if (type.equals("card")) {
            OCard ocard = (OCard) obj;
            AuthCardListener cardlistener = new AuthCardListener();

            // Add panel
            {
                JPanel namePanel = new JPanel();
                authDialog.getContentPane().add(namePanel);

                label = new JLabel(rb.getString("dialog.Card_Name:"));
                namePanel.add(label);
                jfield = new JTextField(ocard.name);
                jfield.setName("name");
                jfield.setPreferredSize(new Dimension(w * 2 / 3, jfield.getPreferredSize().height));
                AuthTextListener listener = new AuthTextListener(jfield);
                jfield.getDocument().addDocumentListener(listener);
                namePanel.add(jfield);

                namePanel.setPreferredSize(new Dimension(w, jfield.getPreferredSize().height + 4));
            }

            // Add panel
            {
                JPanel infoPanel = new JPanel();
                infoPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
                authDialog.getContentPane().add(infoPanel);

                String typeStr = "";
                typeStr = rb.getString("dialog.Card_");
                label = new JLabel(typeStr + rb.getString("dialog.ID:_") + ocard.id);
                label.setPreferredSize(new Dimension(w, label.getPreferredSize().height));
                infoPanel.add(label);
                label = new JLabel(typeStr + rb.getString("dialog.Number:_") + ocard.stack.getNumberOf(ocard)
                        + " / " + (ocard.stack.getCardListSize()));
                label.setPreferredSize(new Dimension(w, label.getPreferredSize().height));
                infoPanel.add(label);

                infoPanel.setPreferredSize(new Dimension(w, 24 * 2));
            }

            // Add panel
            {
                JPanel propPanel = new JPanel();
                propPanel.setLayout(new GridLayout(4, 1));
                authDialog.getContentPane().add(propPanel);

                check = new JCheckBox(rb.getString("dialog.Show_picture"));
                check.setSelected(ocard.showPict);
                check.addActionListener(cardlistener);
                propPanel.add(check);
                check = new JCheckBox(rb.getString("dialog.Marked"));
                check.setSelected(ocard.marked);
                check.addActionListener(cardlistener);
                propPanel.add(check);
                check = new JCheckBox(rb.getString("dialog.Don't_search"));
                check.setSelected(ocard.dontSearch);
                check.addActionListener(cardlistener);
                propPanel.add(check);
                check = new JCheckBox(rb.getString("dialog.Can't_delete"));
                check.setSelected(ocard.cantDelete);
                check.addActionListener(cardlistener);
                propPanel.add(check);

                propPanel.setPreferredSize(new Dimension(w, check.getPreferredSize().height * 4));
            }

            // Add panel
            {
                JPanel buttonPanel = new JPanel();
                // buttonPanel.setLayout(new GridLayout(1,1));
                authDialog.getContentPane().add(buttonPanel);

                jbtn = new JButton(rb.getString("dialog.Script…"));
                jbtn.addActionListener(cardlistener);
                buttonPanel.add(jbtn);

                buttonPanel.setPreferredSize(new Dimension(w, jbtn.getPreferredSize().height * 1 + 8));
            }
        }

        // background
        if (type.equals("background")) {
            OBackground obkgnd = (OBackground) obj;
            AuthBgListener bglistener = new AuthBgListener();

            // Add panel
            {
                JPanel namePanel = new JPanel();
                authDialog.getContentPane().add(namePanel);

                label = new JLabel(rb.getString("dialog.Background_Name:"));
                namePanel.add(label);
                jfield = new JTextField(obkgnd.name);
                jfield.setName("name");
                jfield.setPreferredSize(new Dimension(w * 1 / 2, jfield.getPreferredSize().height));
                AuthTextListener listener = new AuthTextListener(jfield);
                jfield.getDocument().addDocumentListener(listener);
                namePanel.add(jfield);

                namePanel.setPreferredSize(new Dimension(w, jfield.getPreferredSize().height + 4));
            }

            // Add panel
            {
                JPanel infoPanel = new JPanel();
                infoPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
                authDialog.getContentPane().add(infoPanel);

                String typeStr = "";
                typeStr = rb.getString("dialog.Background_");
                label = new JLabel(typeStr + rb.getString("dialog.ID:_") + obkgnd.id);
                label.setPreferredSize(new Dimension(w, label.getPreferredSize().height));
                infoPanel.add(label);
                label = new JLabel(typeStr + rb.getString("dialog.Number:_") + obkgnd.stack.getNumberOf(obkgnd)
                        + " / " + (obkgnd.stack.getBgListSize()));
                label.setPreferredSize(new Dimension(w, label.getPreferredSize().height));
                infoPanel.add(label);

                infoPanel.setPreferredSize(new Dimension(w, 24 * 2));
            }

            // Add panel
            {
                JPanel propPanel = new JPanel();
                propPanel.setLayout(new GridLayout(3, 1));
                authDialog.getContentPane().add(propPanel);

                check = new JCheckBox(rb.getString("dialog.Show_picture"));
                check.setSelected(obkgnd.showPict);
                check.addActionListener(bglistener);
                propPanel.add(check);
                check = new JCheckBox(rb.getString("dialog.Don't_search"));
                check.setSelected(obkgnd.dontSearch);
                check.addActionListener(bglistener);
                propPanel.add(check);
                check = new JCheckBox(rb.getString("dialog.Can't_delete"));
                check.setSelected(obkgnd.cantDelete);
                check.addActionListener(bglistener);
                propPanel.add(check);

                propPanel.setPreferredSize(new Dimension(w, check.getPreferredSize().height * 3));
            }

            // Add panel
            {
                JPanel buttonPanel = new JPanel();
                //buttonPanel.setLayout(new GridLayout(1,1));
                authDialog.getContentPane().add(buttonPanel);

                jbtn = new JButton(rb.getString("dialog.Script…"));
                jbtn.addActionListener(bglistener);
                buttonPanel.add(jbtn);

                buttonPanel.setPreferredSize(new Dimension(w, jbtn.getPreferredSize().height * 1 + 8));
            }
        }

        // stack
        if (type.equals("stack")) {
            OStack ostack = (OStack) obj;
            AuthStackListener stacklistener = new AuthStackListener();

            // Add panel
            {
                JPanel namePanel = new JPanel();
                authDialog.getContentPane().add(namePanel);

                label = new JLabel(rb.getString("dialog.Stack_Name:"));
                namePanel.add(label);
                jfield = new JTextField(ostack.name);
                jfield.setName("name");
                jfield.setPreferredSize(new Dimension(w * 2 / 3, jfield.getPreferredSize().height));
                AuthTextListener listener = new AuthTextListener(jfield);
                jfield.getDocument().addDocumentListener(listener);
                namePanel.add(jfield);

                namePanel.setPreferredSize(new Dimension(w, jfield.getPreferredSize().height + 4));
            }

            // Add panel
            {
                JPanel infoPanel = new JPanel();
                infoPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
                authDialog.getContentPane().add(infoPanel);

                JTextArea area = new JTextArea(rb.getString("dialog.Stack_Path:_") + ostack.file);
                area.setPreferredSize(new Dimension(w, 64));
                area.setLineWrap(true);
                infoPanel.add(area);

                infoPanel.setPreferredSize(new Dimension(w, 64 * 1));
            }

            // Add panel
            {
                JPanel buttonPanel = new JPanel();
                buttonPanel.setLayout(new GridLayout(1, 2));
                authDialog.getContentPane().add(buttonPanel);

                jbtn = new JButton(rb.getString("dialog.Script…"));
                jbtn.addActionListener(stacklistener);
                buttonPanel.add(jbtn);
                jbtn = new JButton(rb.getString("dialog.Size…"));
                jbtn.addActionListener(stacklistener);
                buttonPanel.add(jbtn);
                jbtn = new JButton(rb.getString("dialog.Card_List…"));
                jbtn.addActionListener(stacklistener);
                buttonPanel.add(jbtn);

                buttonPanel.setPreferredSize(new Dimension(w, 24 + 8));
            }
        }

        if (newOpen) {
            int left = 0;
            if (obj.left + obj.width / 2 > owner.getWidth() / 2) {
                left = owner.getX() + obj.left + PCARD.pc.toolbar.getTWidth() - w;
            } else {
                left = owner.getX() + obj.left + PCARD.pc.toolbar.getTWidth() + obj.width;
            }
            authDialog.setBounds(left, owner.getY() + owner.getHeight() / 2 - h / 2, w, h);
        }

        authDialog.getContentPane().repaint();
        authDialog.setVisible(true);
    }


    static class AuthButtonListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            String in_cmd = e.getActionCommand();
            if (in_cmd.equals("comboBoxChanged")) {
                in_cmd = ((JComboBox<?>) e.getSource()).getName();
            }
            String cmd = PCARD.getEngText("dialog", in_cmd);

            switch (cmd) {
                case "style" -> {
                    if (object.objectType.equals("button")) {
                        ((OButton) object).style = ((JComboBox<?>) e.getSource()).getSelectedIndex();

                        OCard.reloadCurrentCard();
                    }
                }
                case "family" -> {
                    if (object.objectType.equals("button")) {
                        if (((OButton) object).group >= 1 && ((OButton) object).radio != null) {
                            OButton.btnGroup[((OButton) object).group - 1].remove(((OButton) object).radio);
                        }
                        ((OButton) object).group = ((JComboBox<?>) e.getSource()).getSelectedIndex();
                        if (((OButton) object).group >= 1 && ((OButton) object).radio != null) {
                            OButton.btnGroup[((OButton) object).group - 1].add(((OButton) object).radio);
                        }
                    }
                }
                case "Show Name" -> {
                    if (object.objectType.equals("button")) {
                        ((OButton) object).setShowName(((JCheckBox) e.getSource()).isSelected());
                    }
                }
                case "Enabled" -> {
                    if (object.objectType.equals("button")) {
                        ((OButton) object).setEnabled(((JCheckBox) e.getSource()).isSelected());
                    }
                }
                case "Visible" -> {
                    if (object.objectType.equals("button")) {
                        object.setVisible(((JCheckBox) e.getSource()).isSelected());
                    }
                }
                case "Auto Hilite" -> {
                    if (object.objectType.equals("button")) {
                        ((OButton) object).setAutoHilite(((JCheckBox) e.getSource()).isSelected());
                    }
                }
                case "Shared Hilite" -> {
                    if (object.objectType.equals("button")) {
                        ((OButton) object).sharedHighlight = ((JCheckBox) e.getSource()).isSelected();
                    }
                }
                case "Scale Icon" -> {
                    if (object.objectType.equals("button")) {
                        ((OButton) object).setScaleIcon(((JCheckBox) e.getSource()).isSelected());
                        ((OButton) object).setIcon(((OButton) object).icon);
                    }
                }
                case "Font…" -> {
                    new GFontDialog(PCARD.pc, ((OButton) object).textFont, ((OButton) object).textSize, ((OButton) object).textStyle, ((OButton) object).textAlign);
                    ((OButton) object).textSize = GFontDialog.selectedSize;
                    ((OButton) object).textStyle = GFontDialog.selectedStyle;
                    ((OButton) object).textAlign = GFontDialog.selectedAlign;
                    ((OButton) object).setTextFont(GFontDialog.selectedFont);
                }
                case "Icon…" -> new ResEdit(PCARD.pc, "icon", object);
                case "LinkTo…", "Effect…" -> {
                }
                case "Script…" -> ScriptEditor.openScriptEditor(PCARD.pc, object);
                case "Content…" -> new TextEditor(PCARD.pc, object);
                default -> logger.log(Level.DEBUG, "AuthTool AuthButtonListener actionPerformed Error!!");
            }

            if (object.getClass() == OStack.class) {
                ((OStack) object).changed = true;
            } else if (object.getClass() == OCard.class || object.getClass() == OBackground.class) {
                ((OCardBase) object).changed = true;
            } else if (object.getClass() == OButton.class || object.getClass() == OField.class) {
                ((OCardBase) object.parent).changed = true;
            }
        }
    }

    static class AuthFieldListener implements ActionListener {

        JCheckBox multipleLinesBtn;
        JCheckBox fixedLineHeightBtn;

        AuthFieldListener(JCheckBox multipleLinesBtn, JCheckBox fixedLineHeightBtn) {
            this.multipleLinesBtn = multipleLinesBtn;
            this.fixedLineHeightBtn = fixedLineHeightBtn;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            String in_cmd = e.getActionCommand();
            if (in_cmd.equals("comboBoxChanged")) {
                in_cmd = ((JComboBox<?>) e.getSource()).getName();
            }
            String cmd = PCARD.getEngText("dialog", in_cmd);

            switch (cmd) {
                case "style" -> {
                    if (object.objectType.equals("field")) {
                        ((OField) object).style = ((JComboBox<?>) e.getSource()).getSelectedIndex() + 1;

                        OCard.reloadCurrentCard();
                    }
                }
                case "Visible" -> {
                    if (object.objectType.equals("field")) {
                        object.setVisible(((JCheckBox) e.getSource()).isSelected());
                    }
                }
                case "Locked text" -> {
                    if (object.objectType.equals("field")) {
                        object.enabled = ((JCheckBox) e.getSource()).isSelected();
                    }
                }
                case "Don't wrap" -> {
                    if (object.objectType.equals("field")) {
                        ((OField) object).dontWrap = ((JCheckBox) e.getSource()).isSelected();
                    }
                }
                case "Auto select" -> {
                    if (object.objectType.equals("field")) {
                        ((OField) object).autoSelect = ((JCheckBox) e.getSource()).isSelected();
                        multipleLinesBtn.setEnabled(((OField) object).autoSelect);
                        if (((OField) object).autoSelect) {
                            fixedLineHeightBtn.setEnabled(false);
                            fixedLineHeightBtn.setSelected(true);
                        } else {
                            fixedLineHeightBtn.setEnabled(true);
                        }
                    }
                }
                case "Multiple lines" -> {
                    if (object.objectType.equals("field")) {
                        ((OField) object).multipleLines = ((JCheckBox) e.getSource()).isSelected();
                    }
                }
                case "Wide margins" -> {
                    if (object.objectType.equals("field")) {
                        ((OField) object).wideMargins = ((JCheckBox) e.getSource()).isSelected();
                    }
                }
                case "Fixed line height" -> {
                    if (object.objectType.equals("field")) {
                        ((OField) object).fixedLineHeight = ((JCheckBox) e.getSource()).isSelected();
                    }
                }
                case "Show lines" -> {
                    if (object.objectType.equals("field")) {
                        ((OField) object).showLines = ((JCheckBox) e.getSource()).isSelected();
                    }
                }
                case "Auto tab" -> {
                    if (object.objectType.equals("field")) {
                        ((OField) object).autoTab = ((JCheckBox) e.getSource()).isSelected();
                    }
                }
                case "Don't search" -> {
                    if (object.objectType.equals("field")) {
                        ((OField) object).dontSearch = ((JCheckBox) e.getSource()).isSelected();
                    }
                }
                case "Shared Text" -> {
                    if (object.objectType.equals("field")) {
                        ((OField) object).sharedText = ((JCheckBox) e.getSource()).isSelected();
                    }
                }
                case "Font…" -> {
                    new GFontDialog(PCARD.pc, ((OField) object).textFont, ((OField) object).textSize, ((OField) object).textStyle, ((OField) object).textAlign);
                    ((OField) object).textSize = GFontDialog.selectedSize;
                    ((OField) object).textStyle = GFontDialog.selectedStyle;
                    ((OField) object).textAlign = GFontDialog.selectedAlign;
                    ((OField) object).setTextFont(GFontDialog.selectedFont);
                }
                case "Script…" -> ScriptEditor.openScriptEditor(PCARD.pc, object);
                default -> logger.log(Level.DEBUG, "AuthTool AuthFieldListener actionPerformed Error!!");
            }

            if (((OField) object).getComponent() != null) {
                ((OField) object).getComponent().paintImmediately(((OField) object).getComponent().getBounds());
            }
        }
    }

    static class AuthCardListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            String in_cmd = e.getActionCommand();
            if (in_cmd.equals("comboBoxChanged")) {
                in_cmd = ((JComboBox<?>) e.getSource()).getName();
            }
            String cmd = PCARD.getEngText("dialog", in_cmd);

            switch (cmd) {
                case "Show picture" -> {
                    if (object.objectType.equals("card")) {
                        ((OCard) object).showPict = ((JCheckBox) e.getSource()).isSelected();
                    }
                }
                case "Don't search" -> {
                    if (object.objectType.equals("card")) {
                        ((OCard) object).dontSearch = ((JCheckBox) e.getSource()).isSelected();
                    }
                }
                case "Can't delete" -> {
                    if (object.objectType.equals("card")) {
                        ((OCard) object).cantDelete = ((JCheckBox) e.getSource()).isSelected();
                    }
                }
                case "Marked" -> {
                    if (object.objectType.equals("card")) {
                        ((OCard) object).marked = ((JCheckBox) e.getSource()).isSelected();
                    }
                }
                case "Script…" -> ScriptEditor.openScriptEditor(PCARD.pc, object);
                default -> logger.log(Level.DEBUG, "AuthTool AuthCardListener actionPerformed Error!!");
            }

            if (((OCard) object).label != null) {
                ((OCard) object).label.paintImmediately(((OCard) object).label.getBounds());
            }
        }
    }

    static class AuthBgListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            String in_cmd = e.getActionCommand();
            if (in_cmd.equals("comboBoxChanged")) {
                in_cmd = ((JComboBox<?>) e.getSource()).getName();
            }
            String cmd = PCARD.getEngText("dialog", in_cmd);

            switch (cmd) {
                case "Show picture" -> {
                    if (object.objectType.equals("background")) {
                        ((OBackground) object).showPict = ((JCheckBox) e.getSource()).isSelected();
                    }
                }
                case "Can't delete" -> {
                    if (object.objectType.equals("background")) {
                        ((OBackground) object).cantDelete = ((JCheckBox) e.getSource()).isSelected();
                    }
                }
                case "Don't search" -> {
                    if (object.objectType.equals("card")) {
                        ((OBackground) object).dontSearch = ((JCheckBox) e.getSource()).isSelected();
                    }
                }
                case "Script…" -> ScriptEditor.openScriptEditor(PCARD.pc, object);
                default -> logger.log(Level.DEBUG, "AuthTool AuthBgListener actionPerformed Error!!");
            }

            if (((OBackground) object).label != null) {
                ((OBackground) object).label.paintImmediately(((OBackground) object).label.getBounds());
            }
        }
    }

    static class AuthStackListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            String in_cmd = e.getActionCommand();
            if (in_cmd.equals("comboBoxChanged")) {
                in_cmd = ((JComboBox<?>) e.getSource()).getName();
            }
            String cmd = PCARD.getEngText("dialog", in_cmd);

            switch (cmd) {
                case "Script…" -> ScriptEditor.openScriptEditor(PCARD.pc, object);
                case "Size…" -> new SizeDialog(AuthDialog.authDialog);
                case "Card List…" -> {
                    StringBuilder str = new StringBuilder();
                    for (int i = 0; i < PCARD.pc.stack.cardIdList.size(); i++) {
                        int id = PCARD.pc.stack.cardIdList.get(i);
                        OCard card = PCARD.pc.stack.getCardbyId(id);
                        str.append("card(").append(i).append(") id:").append(id).append(" name:").append(card.name).append(" btns:").append(card.btnList.size()).append(" flds:").append(card.fldList.size()).append("\n");
                    }
                    new GScrollDialog(null, str.toString(), null, null, null, "OK");
                }
                default -> logger.log(Level.DEBUG, "AuthTool AuthStackListener actionPerformed Error!!");
            }
        }
    }

    static class AuthTextListener implements DocumentListener {

        JTextField textField;

        public AuthTextListener(JTextField fld) {
            textField = fld;
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            if (textField.getName().equals("name")) {
                if (object.objectType.equals("button")) {
                    ((OButton) object).setName(textField.getText());
                } else {
                    object.name = textField.getText();
                }
            }
        }

        @Override
        public void insertUpdate(DocumentEvent e) {
            changedUpdate(e);
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            changedUpdate(e);
        }
    }

    static class SizeDialog extends JDialog implements ActionListener {

        @Serial
        private static final long serialVersionUID = 1L;
        JTextField widthField;
        JTextField heightField;
        JButton defaultButton;

        SizeDialog(JDialog owner) {
            super(owner, true);
            getContentPane().setLayout(new BorderLayout());

            // Add panel
            JPanel topPanel = new JPanel();
            topPanel.setLayout(new GridLayout(2, 1));
            topPanel.setPreferredSize(new Dimension(200, 80));
            getContentPane().add("Center", topPanel);

            {
                JPanel panel = new JPanel();
                panel.setLayout(new FlowLayout());
                //panel.setPreferredSize(new Dimension(320,32));

                JLabel label = new JLabel(rb.getString("dialog.Width:"));
                label.setPreferredSize(new Dimension(64, label.getPreferredSize().height));
                panel.add(label);

                JTextField area1 = new JTextField("" + PCARD.pc.stack.width);
                area1.setPreferredSize(new Dimension(64, area1.getPreferredSize().height));
                panel.add(area1);
                widthField = area1;
                topPanel.add(panel);
            }
            {
                JPanel panel = new JPanel();
                //panel.setPreferredSize(new Dimension(320,32));

                JLabel label = new JLabel(rb.getString("dialog.Height:"));
                label.setPreferredSize(new Dimension(64, label.getPreferredSize().height));
                panel.add(label);
                JTextField area2 = new JTextField("" + PCARD.pc.stack.height);
                area2.setPreferredSize(new Dimension(64, area2.getPreferredSize().height));
                panel.add(area2);
                heightField = area2;
                topPanel.add(panel);
            }

            // Add panel
            JPanel btmPanel = new JPanel();
            getContentPane().add("South", btmPanel);

            {
                JButton btn1 = new JButton("Cancel");
                btn1.addActionListener(this);
                btmPanel.add(btn1);
            }

            {
                JButton btn2 = new JButton("OK");
                btn2.addActionListener(this);
                btmPanel.add(btn2);
                getRootPane().setDefaultButton(btn2);
                defaultButton = btn2;
            }

            setBounds(owner.getX() + owner.getWidth() / 2 - 120, owner.getY() + owner.getHeight() / 2 - 120, 240, 160);
            setUndecorated(true); // Hide title bar

            SwingUtilities.invokeLater(() -> {
                if (defaultButton != null) defaultButton.requestFocus();
            });

            setVisible(true);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (e.getActionCommand().equals("OK")) {
                int width = 0;
                int height = 0;
                try {
                    width = Integer.parseInt(widthField.getText());
                    height = Integer.parseInt(heightField.getText());
                } catch (Exception e2) {

                }
                if (width <= 32 || height <= 32 || width * height >= 4000 * 3000) {
                    new GDialog(PCARD.pc, rb.getString("dialog.Illegal_size.")
                            , null, "OK", null, null);
                } else {
                    PCARD.pc.stack.width = width;
                    PCARD.pc.stack.height = height;
                    Rectangle r = PCARD.pc.getBounds();
                    PCARD.pc.setBounds(r.x, r.y, width, height + PCARD.pc.getInsets().top);
                    PCARD.pc.mainPane.setBounds(0, 0, width, height);
                }
            }
            this.dispose();
        }
    }
}


class AuthColorButton extends CPButton {

    @Serial
    private static final long serialVersionUID = 3564756897317769905L;

    int type;
    OObject obj;

    AuthColorButton(Color in_color, OObject obj, int type) {
        super(in_color, 0, 0, false);
        this.obj = obj;
        this.type = type;
    }

    @Override
    public void makeIcon(Color col) {
        super.makeIcon(col);

        if (obj.getClass() == OButton.class) {
            if (type == 0) ((OButton) obj).setColor(col);
            if (type == 1) ((OButton) obj).setBgColor(col);
        } else if (obj.getClass() == OField.class) {
            if (type == 0) ((OField) obj).setColor(col);
            if (type == 1) ((OField) obj).setBgColor(col);
        }
        OCard.reloadCurrentCard();
    }
}
