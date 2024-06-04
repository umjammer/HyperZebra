package com.github.hyperZebra;

import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.Serial;
import java.util.ResourceBundle;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;


public class GFontDialog extends JDialog implements ActionListener, MouseListener {

    /**
     *
     */
    @Serial
    private static final long serialVersionUID = 1L;
    static String selectedFont = "";
    static int selectedSize;
    static int selectedStyle;
    static int selectedAlign;
    static MyTextArea sampleArea;
    PCARDFrame owner;

    ResourceBundle rb = ResourceBundle.getBundle("message");

    GFontDialog(PCARDFrame owner, String defaultFont, int defaultSize, int defaultStyle, int defaultAlign) {
        super(owner, true);
        this.owner = owner;
        getContentPane().setLayout(null);
        setTitle(PCARD.pc.intl.getDialogText("Font"));

        // サンプル表示
        sampleArea = new MyTextArea(rb.getString("font-dialog.sample"));
        sampleArea.fldData = new OField(null, 0);
        sampleArea.fldData.width = 400;
        sampleArea.fldData.dontWrap = true;
        sampleArea.setBounds(0, 0, 400, 96);
        sampleArea.setMargin(new Insets(0, 0, 0, 0));
        sampleArea.setEditable(false);
        sampleArea.setFocusable(false);
        setStyleSample(defaultFont, defaultSize, defaultStyle, defaultAlign);
        getContentPane().add(sampleArea);

        // フォント用スクロール
        JScrollPane scrl = new JScrollPane();
        scrl.setBounds(4, 96, 192, 156);
        scrl.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        scrl.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        getContentPane().add(scrl);

        // フォントリスト
        String[] fontFamilyNames = GraphicsEnvironment
                .getLocalGraphicsEnvironment()
                .getAvailableFontFamilyNames();
        StringBuilder fontStrings = new StringBuilder();
        for (String fontFamilyName : fontFamilyNames) {
            fontStrings.append(fontFamilyName).append("\n");
        }
        MyTextArea fontarea = new MyTextArea(fontStrings.toString());
        fontarea.setName("Font");
        fontarea.pr_scrl = scrl;
        fontarea.setMargin(new Insets(0, 0, 0, 0));
        fontarea.setOpaque(false);
        fontarea.setEditable(false);
        fontarea.addMouseListener(this);
        scrl.setViewportView(fontarea);

        // 行選択
        for (int i = 0; i < fontFamilyNames.length; i++) {
            if (defaultFont.equals(fontFamilyNames[i])) {
                lineSelect(i + 1, fontarea);
            }
        }

        // サイズ用スクロール
        JScrollPane sizeScrl = new JScrollPane();
        sizeScrl.setBounds(200, 96, 54, 156);
        sizeScrl.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        sizeScrl.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        getContentPane().add(sizeScrl);

        // サイズリスト
        StringBuilder sizeStrings = new StringBuilder();
        for (int i = 1; i <= 127; i++) {
            sizeStrings.append(i).append("\n");
        }
        MyTextArea sizearea = new MyTextArea(sizeStrings.toString());
        sizearea.setName("Size");
        sizearea.pr_scrl = scrl;
        sizearea.setMargin(new Insets(0, 0, 0, 0));
        sizearea.setOpaque(false);
        sizearea.setEditable(false);
        sizearea.addMouseListener(this);
        sizeScrl.setViewportView(sizearea);

        // 行選択
        lineSelect(defaultSize, sizearea);

        // パネル
        JPanel stylePanel = new JPanel();
        stylePanel.setBounds(254, 96, 140, 156);
        stylePanel.setLayout(new GridLayout(7, 1));
        getContentPane().add(stylePanel);

        // スタイル
        JCheckBox check = new JCheckBox(PCARD.pc.intl.getDialogText("Bold"));
        check.setSelected((defaultStyle & 1) > 0);
        check.addActionListener(this);
        stylePanel.add(check);

        check = new JCheckBox(PCARD.pc.intl.getDialogText("Italic"));
        check.setSelected((defaultStyle & 2) > 0);
        check.addActionListener(this);
        stylePanel.add(check);

        check = new JCheckBox(PCARD.pc.intl.getDialogText("Underline"));
        check.setSelected((defaultStyle & 4) > 0);
        check.addActionListener(this);
        stylePanel.add(check);

        check = new JCheckBox(PCARD.pc.intl.getDialogText("Outline"));
        check.setSelected((defaultStyle & 8) > 0);
        check.addActionListener(this);
        stylePanel.add(check);

        check = new JCheckBox(PCARD.pc.intl.getDialogText("Shadow"));
        check.setSelected((defaultStyle & 16) > 0);
        check.addActionListener(this);
        stylePanel.add(check);

        check = new JCheckBox(PCARD.pc.intl.getDialogText("Condensed"));
        check.setSelected((defaultStyle & 32) > 0);
        check.addActionListener(this);
        stylePanel.add(check);

        check = new JCheckBox(PCARD.pc.intl.getDialogText("Extend"));
        check.setSelected((defaultStyle & 64) > 0);
        check.addActionListener(this);
        stylePanel.add(check);

        selectedStyle = defaultStyle;

        if (defaultAlign != -1) {
            // パネル
            JPanel alignPanel = new JPanel();
            alignPanel.setBounds(0, 250, 400, 300);
            alignPanel.setLayout(new FlowLayout());
            getContentPane().add(alignPanel);

            ButtonGroup group = new ButtonGroup();

            JRadioButton radio = new JRadioButton(PCARD.pc.intl.getDialogText("Align Left"));
            radio.setName("Align Left");
            radio.setSelected(defaultAlign == 0);
            radio.addActionListener(this);
            group.add(radio);
            alignPanel.add(radio);

            radio = new JRadioButton(PCARD.pc.intl.getDialogText("Align Center"));
            radio.setName("Align Center");
            radio.setSelected(defaultAlign == 1);
            radio.addActionListener(this);
            group.add(radio);
            alignPanel.add(radio);

            radio = new JRadioButton(PCARD.pc.intl.getDialogText("Align Right"));
            radio.setName("Align Right");
            radio.setSelected(defaultAlign == 2);
            radio.addActionListener(this);
            group.add(radio);
            alignPanel.add(radio);

            selectedAlign = defaultAlign;
        }

        setBounds(owner.getX() + owner.getWidth() / 2 - 200, owner.getY() + owner.getHeight() / 2 - 160 + 12, 400, 320);

        setResizable(false);
        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String in_name = e.getActionCommand();
        String name = PCARD.pc.intl.getDialogEngText(in_name);
        switch (name) {
            case "Bold" -> selectedStyle = (selectedStyle & (~1)) |
                    (((JCheckBox) e.getSource()).isSelected() ? 1 : 0);
            case "Italic" -> selectedStyle = (selectedStyle & (~2)) |
                    (((JCheckBox) e.getSource()).isSelected() ? 2 : 0);
            case "Underline" -> selectedStyle = (selectedStyle & (~4)) |
                    (((JCheckBox) e.getSource()).isSelected() ? 4 : 0);
            case "Outline" -> selectedStyle = (selectedStyle & (~8)) |
                    (((JCheckBox) e.getSource()).isSelected() ? 8 : 0);
            case "Shadow" -> selectedStyle = (selectedStyle & (~16)) |
                    (((JCheckBox) e.getSource()).isSelected() ? 16 : 0);
            case "Condensed" -> selectedStyle = (selectedStyle & (~32)) |
                    (((JCheckBox) e.getSource()).isSelected() ? 32 : 0);
            case "Extend" -> selectedStyle = (selectedStyle & (~64)) |
                    (((JCheckBox) e.getSource()).isSelected() ? 64 : 0);
            case "Align Left" -> selectedAlign = 0;
            case "Align Center" -> selectedAlign = 1;
            case "Align Right" -> selectedAlign = 2;
        }
        setStyleSample(selectedFont, selectedSize, selectedStyle, selectedAlign);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
        MyTextArea area = (MyTextArea) e.getSource();
        {
            int line = (e.getY() /*+ area.pr_scrl.getVerticalScrollBar().getValue()*/ + area.getLineHeight() - 1) / area.getLineHeight();
            // System.out.println("getValue="+fld.pr_scrl.getVerticalScrollBar().getValue() );
            // System.out.println("line="+line);
            lineSelect(line, area);
            setStyleSample(selectedFont, selectedSize, selectedStyle, selectedAlign);
        }
    }

    private static void lineSelect(int line, MyTextArea area) {
        int cnt = 0;
        int i;
        for (i = 0; i < area.getText().length(); i++) {
            if (area.getText().charAt(i) == '\n' || i == 0) {
                cnt++;
                if (cnt == line) {
                    area.pr_selLine = line;
                    area.setSelectionStart(i);
                    area.getRootPane().repaint();
                    if (area.getName().equals("Font")) {
                        if (area.getText().split("\n").length >= line - 1) {
                            selectedFont = area.getText().split("\n")[line - 1];
                        }
                    } else {
                        selectedSize = line;
                    }
                }
                if (cnt == line + 1) {
                    break;
                }
            }
        }
        if (area.pr_selLine == line) {
            area.setSelectionEnd(i);
        }
    }

    private void setStyleSample(String font, int size, int style, int align) {

        sampleArea.fldData.textFont = font;
        sampleArea.fldData.textSize = size;
        sampleArea.fldData.textStyle = style;
        sampleArea.fldData.textAlign = align;

        int javaStyle = Font.PLAIN;
        if ((1 & style) > 0) {
            javaStyle |= Font.BOLD;
        } else if ((2 & style) > 0) {
            javaStyle |= Font.ITALIC;
        }
        Font ffont = new Font(font, javaStyle, size);
        sampleArea.setFont(ffont);

        if (owner.tool != null && owner.tool.getClass() == TypeTool.class) {
            TypeTool tool = (TypeTool) owner.tool;
            if (tool.area != null) {
                tool.area.fldData.textFont = font;
                tool.area.fldData.textSize = size;
                tool.area.fldData.textStyle = style;
                tool.area.fldData.textAlign = align;
                tool.area.setFont(ffont);
            }
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent arg0) {
    }

    @Override
    public void mouseExited(MouseEvent arg0) {
    }
}