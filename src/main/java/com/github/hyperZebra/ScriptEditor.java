package com.github.hyperZebra;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Insets;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.Serial;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.JViewport;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;


public class ScriptEditor extends JFrame {

    /**
     *
     */
    @Serial
    private static final long serialVersionUID = 1L;
    // private OStack parent = null;
    static JTabbedPane tabPane = null;
    String FindStr = "";
    String ReplaceStr = "";
    // static private ScriptArea area;

    public ScriptEditor(Frame owner) {
        super();

        // frame
        setBounds(owner.getX() + owner.getWidth() / 2 - 320, owner.getY() + owner.getHeight() / 2 - 320, 640, 640);
        setTitle(PCARD.pc.intl.getDialogText("Script Editor"));
        getContentPane().setLayout(new BorderLayout());
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                maybeExit();
                while (tabPane.getComponentCount() > 0) {
                    tabPane.remove(tabPane.getComponent(0));
                }
                TTalk.tracemode = 0;
                TTalk.stepflag = true;
            }

            @Override
            public void windowOpened(WindowEvent e) {
                // area.checkIndentCall();
            }
        });

        // tabpane
        tabPane = new JTabbedPane();
        getContentPane().add(tabPane);

        // menu
        new SEMenu(this);
    }

    public static void openScriptEditor(Frame owner, OObject obj) {
        openScriptEditor(owner, obj, 0);
    }

    private static Frame tmp_owner;
    private static OObject tmp_obj;
    private static int tmp_line;

    public static void openScriptEditor(Frame owner, OObject obj, int line) {
        tmp_owner = owner;
        tmp_obj = obj;
        tmp_line = line;

        SwingUtilities.invokeLater(() -> openScriptEditor1(tmp_owner, tmp_obj, tmp_line));
    }

    private static void openScriptEditor1(Frame owner, OObject obj, int line) {
        // System.out.println("openScriptEditor");

        ArrayList<String> scriptList = obj.scriptList;
        if (PCARD.pc.stack.scriptEditor == null) {
            PCARD.pc.stack.scriptEditor = new ScriptEditor(owner);
        }

        // 2重オープンチェック
        if (tabPane != null) {
            for (int i = 0; i < tabPane.getComponentCount(); i++) {
                if (!tabPane.getComponent(i).getName().equals("JScrollPane")) {
                    continue;
                }
                JScrollPane scrPane = (JScrollPane) tabPane.getComponent(i);
                if (scrPane.getViewport().getComponentCount() < 1) {
                    continue;
                }
                if (scrPane.isValid() == false) {
                    PCARD.pc.stack.scriptEditor.dispose();
                    PCARD.pc.stack.scriptEditor = new ScriptEditor(owner);
                    return;// 壊れている
                }
                ScriptArea area = (ScriptArea) scrPane.getViewport().getComponent(0);
                if (area.compareObject(obj) == true) {
                    if (line > 0) {
                        // 指定行にジャンプ
                        String[] newAry = area.getText().split("\n");
                        int selStart = 0;
                        int selEnd = 0;
                        for (int j = 0; j < newAry.length; j++) {
                            if (line == j) {
                                selEnd = selStart + newAry[j].length();
                                break;
                            }
                            selStart += newAry[j].length() + 1;
                        }
                        area.setSelectionStart(selStart);
                        if (selEnd > 0) area.setSelectionEnd(selEnd);
                    }

                    PCARD.pc.stack.scriptEditor.setVisible(true);
                    PCARD.pc.stack.scriptEditor.toFront();
                    tabPane.setSelectedIndex(i);
                    area.requestFocus();

                    tabPane.setVisible(true);
                    scrPane.setVisible(true);
                    area.setVisible(true);
                    return;
                }
            }
        }

        JScrollPane scrollpane = new JScrollPane();
        scrollpane.setName("JScrollPane");
        scrollpane.setBounds(0, 0, 640, 640);
        scrollpane.setPreferredSize(new Dimension(640, 640));
        scrollpane.getVerticalScrollBar().setValue(0);
        scrollpane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollpane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        tabPane.addTab(obj.getShortName(), scrollpane);

        StringBuilder text = new StringBuilder();
        for (int i = 0; i < scriptList.size(); i++) {
            text.append(scriptList.get(i));
            if (i < scriptList.size() - 1) text.append("\n");
        }
        if (obj.objectType.equals("button") && (text.isEmpty())) {
            text = new StringBuilder("on mouseUp\n  \nend mouseUp");
            line = 1;
        }

        // area
        ScriptArea area = new ScriptArea(PCARD.pc.stack.scriptEditor, text.toString());
        area.checkIndentCall();
        area.setObject(obj);
        area.setMargin(new Insets(2, 2, 2, 2));
        area.getPreferredSize();
        area.setOpaque(false);
        area.setCaretPosition(0);
        // scrollpane.add(area);
        scrollpane.setViewportView(area);


        if (tabPane.getComponentCount() > 2) {
            for (int i = 0; i < tabPane.getComponentCount(); i++) {
                JScrollPane scrPane = (JScrollPane) tabPane.getComponent(i);
                if (scrPane.getViewport().getComponentCount() >= 1) {
                    ScriptArea a = (ScriptArea) scrPane.getViewport().getComponent(0);
                    if (a != null) tabPane.setTitleAt(i, a.object.getShortShortName());
                }
            }
        }

        if (line > 0) {
            // 指定行にジャンプ
            String[] newAry = area.getText().split("\n");
            int selStart = 0;
            int selEnd = 0;
            for (int j = 0; j < newAry.length; j++) {
                if (line == j) {
                    int spacing = 0;
                    while (spacing < newAry[j].length() && (newAry[j].charAt(spacing) == ' ' || newAry[j].charAt(spacing) == '　'))
                        spacing++;
                    selStart += spacing;
                    selEnd = selStart + newAry[j].length() - spacing;
                    break;
                }
                selStart += newAry[j].length() + 1;
            }
            area.setSelectionStart(selStart);
            if (selEnd > 0) area.setSelectionEnd(selEnd);
        }

        PCARD.pc.stack.scriptEditor.toFront();

        try {
            PCARD.pc.stack.scriptEditor.setVisible(true);
        } catch (Exception e) {
            System.out.println("Error: PCARD.pc.stack.scriptEditor.setVisible(true);");
            SwingUtilities.invokeLater(() -> PCARD.pc.stack.scriptEditor.setVisible(true));
        }

        try {
            tabPane.setSelectedComponent(scrollpane);
        } catch (Exception e) {
            System.out.println("Error: tabPane.setSelectedComponent(scrollpane);");
        }

        area.requestFocus();
    }

    static void maybeExit() {
        for (int i = 0; i < tabPane.getComponentCount(); i++) {
            if (!tabPane.getComponent(i).getName().equals("JScrollPane")) {
                continue;
            }
            JScrollPane scrPane = (JScrollPane) tabPane.getComponent(i);
            if (scrPane.getViewport().getComponentCount() == 0) return;
            ScriptArea area = (ScriptArea) scrPane.getViewport().getComponent(0);
            if (false == saveAlert(area)) {
                return;// クローズキャンセル
            }
        }
        PCARD.pc.stack.scriptEditor.setVisible(false);
        // PCARD.pc.stack.scriptEditor.dispose();
        // PCARD.pc.stack.scriptEditor = null;
    }

    static boolean saveAlert(ScriptArea area) {
        if (area == null || area.saved) {
            return true;
        }
        java.awt.Toolkit.getDefaultToolkit().beep();
        Object[] options = {PCARD.pc.intl.getDialogText("Save"),
                PCARD.pc.intl.getDialogText("Discard"),
                PCARD.pc.intl.getDialogText("Cancel")};
        int retValue = JOptionPane.showOptionDialog(PCARD.pc.stack.scriptEditor,
                PCARD.pc.intl.getDialogText("Script is not saved."),
                "Exit Options",
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.WARNING_MESSAGE, null, options, options[0]);
        if (retValue == JOptionPane.YES_OPTION) {
            // 保存する
            saveScript(area);
        } else if (retValue == JOptionPane.NO_OPTION) {
            // 保存しない
        } else if (retValue == JOptionPane.CANCEL_OPTION) {
            return false;
        }
        return true;
    }

    static void saveScript(ScriptArea area) {
        String allStr = "";
        try {
            allStr = area.getDocument().getText(0, area.getDocument().getLength());
        } catch (BadLocationException e) {
            e.printStackTrace();
        }

        /*OObject obj = null;
        // カード(bg)を開いているなら、メモリ上にあるオブジェクトの情報を書き換える
        if(area.ParentId==0 || area.compareParent(PCARD.pc.stack.curCard)){
            obj = area.getObject(PCARD.pc.stack.curCard);
        } else {
            obj = area.getObjectOtherCard();
        }*/
        OObject obj = area.object;

        if (obj != null) {
            obj.setScript(allStr);
            area.savedScript();
        } else {
            System.out.println("スクリプトの保存に失敗しました");
        }
    }


    static void setTracemode() {
        if (PCARD.pc.stack.scriptEditor == null) {
            SwingUtilities.invokeLater(ScriptEditor::setTracemode);
            return;
        }
        JMenuBar mb = PCARD.pc.stack.scriptEditor.getJMenuBar();
        for (int i = 0; i < mb.getMenuCount(); i++) {
            if (mb.getMenu(i).getText().equals(PCARD.pc.intl.getText("Debug"))) {
                // すでにdebugメニューがある
                return;
            }
        }

        // debugメニューを作成
        SEMenuListener listener = new SEMenuListener();
        int s = InputEvent.CTRL_DOWN_MASK;
        if (System.getProperty("os.name").toLowerCase().startsWith("mac os x")) {
            s = InputEvent.META_DOWN_MASK;
        }
        JMenu m = new JMenu(PCARD.pc.intl.getText("Debug"));
        mb.add(m);
        JMenuItem mi;
        m.add(mi = new JMenuItem(PCARD.pc.intl.getText("Step")));
        mi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_7, s));
        mi.addActionListener(listener);
        m.addSeparator();

        m.add(mi = new JMenuItem(PCARD.pc.intl.getText("Trace")));
        mi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_5, s));
        mi.addActionListener(listener);

        m.add(mi = new JMenuItem(PCARD.pc.intl.getText("Run")));
        mi.addActionListener(listener);
        m.addSeparator();

        m.add(mi = new JMenuItem(PCARD.pc.intl.getText("Variable Watcher")));
        mi.addActionListener(listener);
    }
}

class seListener implements DocumentListener {

    ScriptArea area;

    seListener(ScriptArea inarea) {
        super();
        area = inarea;
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        area.changedScript();
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        String changeStr = area.getText().substring(e.getOffset(), e.getOffset() + e.getLength());
        if (changeStr.equals("\n")) {
            area.checkIndentCall();
        }
        if (changeStr.contains("\t")) {
            area.checkIndentCall();
            return;
        }
        area.changedScript();
        area.new checkWordsThread().start();
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        area.changedScript();
        area.new checkWordsThread().start();
    }
}

class ScriptArea extends JTextPane {

    /**
     *
     */
    @Serial
    private static final long serialVersionUID = 1L;
    OObject object;
    //
    boolean saved = true;
    // UndoManager undo = null;
    ScriptEditor parent = null;
    // String ParentType;
    // int ParentId;
    // String ObjType;
    // int ObjId;
    seListener seListener = null;

    public ScriptArea(ScriptEditor se, String text) {
        super();
        parent = se;
        // undo = new UndoManager();
        undobuf.add(text);

        setFont(new Font(PCARD.scriptFont, 0, PCARD.scriptFontSize));

        StyleContext scontext = new StyleContext();
        DefaultStyledDocument doc = new DefaultStyledDocument(scontext);
        setDocument(doc);

        try {
            doc.insertString(0, text, scontext.getStyle(StyleContext.DEFAULT_STYLE));
        } catch (BadLocationException e) {
            e.printStackTrace();
        }

        // getDocument().addUndoableEditListener(undo);
        // getDocument().addUndoableEditListener(new UndoableEditListener() {
        // public void undoableEditHappened(UndoableEditEvent e) {
        // 行われた編集(文字の追加や削除)をUndoManagerに登録
                /*if (checkThreadCnt==0) {
                    sub.addEdit(e.getEdit());
                    sub.end();
                    undo.addEdit(sub);
                    sub = new UndoManager();
                    sub.setLimit(10000);
                }
                else{
                    sub.addEdit(e.getEdit());
                }*/

                /*
                 * http:// webcache.googleusercontent.com/search?q=cache:fm8Klx9_kTAJ:gline.zapto.org/log/read.php/tech/1227234261/101-200+UndoableEditEvent+色の変更は&cd=2&hl=ja&ct=clnk&gl=jp&client=safari&source=www.google.co.jp
                 * javax.swing.undoではまったのでメモ。
                    例えば、文字列の置換のような削除、挿入という複数の処理を１回で元に戻したい場合は
                    UndoManagerを入れ子にする。

                    UndoManagerを２つ用意して、基本はサブに追加する。一塊の処理が終わったらend()を呼んでメインに追加する。

                    void undoableEditHappened(UndoableEditEvent e) {
                    　　sub.addEdit(e.getEdit());
                    　　if (!compound) {
                    　　　　sub.end();
                    　　　　main.addEdit(sub);
                    　　　　sub = new UndoManager();
                    　　}
                    }
                 */
        // }
        // });

        addFocusListener(new SAFocusListener());
        seListener = new seListener(this);
        getDocument().addDocumentListener(seListener);
    }

    static boolean flag;

    void checkIndentCall() {
        SwingUtilities.invokeLater(() -> {
            if (flag == false) {
                flag = true;
                checkIndent();
                new checkWordsThread().start();
                flag = false;
            }
        });
    }

    static int checkThreadCnt = 0;

    class checkWordsThread extends Thread {

        @Override
        public void run() {
            if (checkThreadCnt > 0) return;
            checkThreadCnt++;
            setPriority(Thread.MIN_PRIORITY);
            checkWords();
            checkThreadCnt--;
        }
    }

    void checkIndent() {
        String script = getText();

        int selectedStartLine = (script.substring(0, getSelectionStart()) + " ").split("\n").length - 1;
        int selectedEndLine = (script.substring(0, getSelectionEnd()) + " ").split("\n").length - 1;
        int selectedStartChar = getSelectionStart() - script.substring(0, getSelectionStart()).lastIndexOf("\n") - 1;
        int selectedEndChar = getSelectionEnd() - script.substring(0, getSelectionEnd()).lastIndexOf("\n") - 1;

        // タブ文字を消す
        if (script.contains("\t")) {
            script = script.substring(0, script.indexOf("\t")) + script.substring(script.indexOf("\t") + 1);
            selectedStartChar--;
            selectedEndChar--;
        }

        // インデントを解析
        StringBuilder newScript = new StringBuilder();
        String[] scrAry = (script + " ").split("\n");
        ArrayList<String> nestAry = new ArrayList<>();
        int indent = 0;
        int nextIndent = 0;
        int i;
        String[] lastwords = null;
        for (i = 0; i < scrAry.length; i++) {
            int spacing = 0;
            while (spacing < scrAry[i].length() && (scrAry[i].charAt(spacing) == ' ' || scrAry[i].charAt(spacing) == '　'))
                spacing++;

            ArrayList<String> stringList = new ArrayList<>();
            ArrayList<TTalk.wordType> typeList = new ArrayList<>();
            checkWordsLine(scrAry[i], stringList, typeList, true, false);
            for (int j = 0; j < typeList.size(); j++) {
                if (typeList.get(j) == TTalk.wordType.COMMENT) {
                    typeList.remove(j);
                    stringList.remove(j);
                    break;
                }
            }
            if (stringList.isEmpty()) stringList.add("");
            String[] words = new String[stringList.size()];
            stringList.toArray(words);

            // String[] words = scrAry[i].substring(spacing).split(" ");
            /*for(int j=0; j<words.length; j++){
                if(words[j].indexOf("--")==0){
                    words[j] = "";
                    if(j-1 >= 0) words[words.length-1] = words[j-1];
                    else words[words.length-1] = "";
                    break;
                }
                else if(words[j].indexOf("--")>=0){
                    words[j] = words[j].substring(0,words[j].indexOf("--"));
                    if(j-1 >= 0) words[words.length-1] = words[j-1];
                    else words[words.length-1] = "";
                    break;
                }
            }*/

            if (words[0].equalsIgnoreCase("on") && words.length >= 2) {
                if (indent == 0) {
                    nextIndent = 1;
                    if (!words[1].isEmpty()) nestAry.add("on " + words[1]);
                    else if (words.length >= 3 && !words[2].isEmpty()) nestAry.add("on " + words[2]);
                } else break;
            }
            if (words[0].equalsIgnoreCase("function") && words.length >= 2) {
                if (indent == 0) {
                    nextIndent = 1;
                    if (!words[1].isEmpty()) nestAry.add("on " + words[1]);
                    else if (words.length >= 3 && !words[2].isEmpty()) nestAry.add("on " + words[2]);
                } else break;
            }
            if (words[0].equalsIgnoreCase("repeat")) {
                if (indent >= 1) {
                    nextIndent = indent + 1;
                    nestAry.add("repeat");
                } else break;
            }
            if (words[0].equalsIgnoreCase("else")) {
                if (lastwords[0].equalsIgnoreCase("if") && !lastwords[lastwords.length - 1].equalsIgnoreCase("then") || lastwords[0].equalsIgnoreCase("else") && lastwords.length >= 2 && lastwords[1].equalsIgnoreCase("if")) {
                    //
                } else if (nestAry.get(nestAry.size() - 1).equalsIgnoreCase("then")) {
                    indent -= 1;
                    nextIndent -= 1;
                    nestAry.remove(nestAry.size() - 1);
                }
                // else break;
            }
            if (words[words.length - 1].equalsIgnoreCase("then") || words[words.length - 1].equalsIgnoreCase("else")) {
                if (indent >= 1) {
                    nextIndent = indent + 1;
                    nestAry.add("then");
                } else break;
            }
            if (words[0].equalsIgnoreCase("end")) {
                if (words[1].equalsIgnoreCase("repeat")) {
                    if (nestAry.get(nestAry.size() - 1).equalsIgnoreCase("repeat")) {
                        indent -= 1;
                        nextIndent -= 1;
                        nestAry.remove(nestAry.size() - 1);
                    } else break;
                } else if (words[1].equalsIgnoreCase("if")) {
                    if (nestAry.get(nestAry.size() - 1).equalsIgnoreCase("then")) {
                        indent -= 1;
                        nextIndent -= 1;
                        nestAry.remove(nestAry.size() - 1);
                    } else break;
                } else if (words.length >= 2 && !nestAry.isEmpty() && nestAry.get(nestAry.size() - 1).equalsIgnoreCase("on " + words[1])) {
                    indent -= 1;
                    nextIndent -= 1;
                    nestAry.remove(nestAry.size() - 1);
                } else if (words[1].isEmpty() && words.length >= 3 && nestAry.get(nestAry.size() - 1).equalsIgnoreCase("on " + words[2])) {
                    indent -= 1;
                    nextIndent -= 1;
                    nestAry.remove(nestAry.size() - 1);
                } else break;
            }

            lastwords = words;

            newScript.append("  ".repeat(Math.max(0, indent)));
            indent = nextIndent;

            newScript.append(scrAry[i].substring(spacing));
            if (i < scrAry.length - 1) newScript.append("\n");
        }

        for (; i < scrAry.length; i++) {
            int spacing = 0;
            while (spacing < scrAry[i].length() && (scrAry[i].charAt(spacing) == ' ' || scrAry[i].charAt(spacing) == '　'))
                spacing++;

            newScript.append("  ".repeat(Math.max(0, indent)));

            newScript.append(scrAry[i].substring(spacing));
            if (i < scrAry.length - 1) newScript.append("\n");
        }
        if (" ".isEmpty()) {
            newScript = new StringBuilder(newScript.substring(0, newScript.length() - 1));
        }

        {
            String[] newAry = (newScript + " ").split("\n");
            int selStart = 0;
            int j = 0;
            for (; j < newAry.length; j++) {
                if (selectedStartLine == j) break;
                selStart += newAry[j].length() + 1;
            }
            if (j < newAry.length && newAry[j].matches("^[ ]*$")) {
                selStart += newAry[j].length();
            } else {
                selStart += selectedStartChar;
            }

            int selEnd = 0;
            for (j = 0; j < newAry.length; j++) {
                if (selectedEndLine == j) break;
                selEnd += newAry[j].length() + 1;
            }
            selEnd += selectedEndChar;


            // テキストを設定
            getDocument().removeDocumentListener(seListener);
            setText(newScript.toString());
            getDocument().addDocumentListener(seListener);

            setSelectionStart(selStart);
            setSelectionEnd(selEnd);
        }
    }

    @SuppressWarnings("unchecked")
    void checkWords() {
        String text = getText();
        text = text.replace('\r', '\n');
        text = text.toLowerCase();
        String[] scriptAry = text.split("\n");

        ArrayList<String>[] stringList = new ArrayList[scriptAry.length];
        ArrayList<TTalk.wordType>[] typeList = new ArrayList[scriptAry.length];
        StringBuilder handler = new StringBuilder();
        TreeSet<String> varSet = new TreeSet<>();

        // 行の連結
        for (int i = scriptAry.length - 2; i >= 0; i--) {
            if (scriptAry[i].isEmpty()) continue;
            char c = scriptAry[i].charAt(scriptAry[i].length() - 1);
            if (c == '~' || c == 'ﾂ') {
                scriptAry[i] = scriptAry[i].substring(0, scriptAry[i].length() - 1) + scriptAry[i + 1];
                scriptAry[i + 1] = "";
            }
        }

        // 全ての行を解析
        for (int i = 0; i < scriptAry.length; i++) {
            stringList[i] = new ArrayList<>();
            typeList[i] = new ArrayList<>();
            checkWordsLine(scriptAry[i], stringList[i], typeList[i], true, true);
            checkWordsLine2(scriptAry[i], stringList[i], typeList[i], true, handler, varSet);
        }


        getDocument().removeDocumentListener(seListener);

        // 文字に色を付ける
        int start = 0;
        for (int i = 0; i < scriptAry.length; i++) {
            MutableAttributeSet attr = new SimpleAttributeSet();
            for (int j = 0; j < stringList[i].size(); j++) {
                if (typeList[i].get(j) == TTalk.wordType.STRING || typeList[i].get(j) == TTalk.wordType.CONST) {
                    StyleConstants.setForeground(attr, new Color(0, 0, 16));
                } else if (typeList[i].get(j) == TTalk.wordType.COMMENT) {
                    StyleConstants.setForeground(attr, new Color(0, 96, 0));
                } else if (typeList[i].get(j) == TTalk.wordType.XCMD || typeList[i].get(j) == TTalk.wordType.XFCN) {
                    StyleConstants.setForeground(attr, new Color(64, 0, 128));
                } else if (typeList[i].get(j) == TTalk.wordType.CMD || typeList[i].get(j) == TTalk.wordType.CMD_SUB || typeList[i].get(j) == TTalk.wordType.GLOBAL) {
                    StyleConstants.setForeground(attr, new Color(64, 0, 32));
                } else if (typeList[i].get(j) == TTalk.wordType.FUNC || typeList[i].get(j) == TTalk.wordType.OF_FUNC || typeList[i].get(j) == TTalk.wordType.THE_FUNC) {
                    StyleConstants.setForeground(attr, new Color(16, 0, 8));
                } else if (typeList[i].get(j) == TTalk.wordType.VARIABLE) {
                    StyleConstants.setForeground(attr, new Color(0, 64, 128));
                } else if (typeList[i].get(j) == TTalk.wordType.OBJECT || typeList[i].get(j) == TTalk.wordType.OF_OBJ) {
                    StyleConstants.setForeground(attr, new Color(16, 0, 0));
                } else if (typeList[i].get(j) == TTalk.wordType.CHUNK || typeList[i].get(j) == TTalk.wordType.OF_CHUNK) {
                    StyleConstants.setForeground(attr, new Color(0, 0, 0));
                } else if (typeList[i].get(j) == TTalk.wordType.X) {
                    StyleConstants.setForeground(attr, new Color(16, 16, 16));
                } else if (typeList[i].get(j) == TTalk.wordType.IF || typeList[i].get(j) == TTalk.wordType.THEN || typeList[i].get(j) == TTalk.wordType.ELSE || typeList[i].get(j) == TTalk.wordType.ENDIF) {
                    StyleConstants.setForeground(attr, new Color(128, 32, 0));
                } else if (typeList[i].get(j) == TTalk.wordType.ON_HAND || typeList[i].get(j) == TTalk.wordType.END_HAND || typeList[i].get(j) == TTalk.wordType.ON_FUNC || typeList[i].get(j) == TTalk.wordType.EXIT || typeList[i].get(j) == TTalk.wordType.PASS || typeList[i].get(j) == TTalk.wordType.RETURN) {
                    StyleConstants.setForeground(attr, new Color(128, 0, 0));
                } else if (typeList[i].get(j) == TTalk.wordType.REPEAT || typeList[i].get(j) == TTalk.wordType.END_REPEAT || typeList[i].get(j) == TTalk.wordType.EXIT_REP || typeList[i].get(j) == TTalk.wordType.NEXT_REP) {
                    StyleConstants.setForeground(attr, new Color(128, 64, 0));
                } else if (typeList[i].get(j) == TTalk.wordType.PROPERTY || typeList[i].get(j) == TTalk.wordType.OF_PROP) {
                    StyleConstants.setForeground(attr, new Color(0, 32, 0));
                } else {
                    StyleConstants.setForeground(attr, new Color(0, 0, 0));
                }
                if (start < 0) start = 0;
                if (start >= text.length()) start = text.length() - 10;

                int offset = text.substring(start).indexOf(stringList[i].get(j));
                if (offset > 0 && offset <= 64) start += offset;
                int len = stringList[i].get(j).length();
                if (start + len >= getDocument().getLength()) len = 0;
                ((DefaultStyledDocument) getDocument()).setCharacterAttributes(start, len, attr, false);
                start += len;
                // System.out.println(stringList[i].get(j)+"   "+len+":"+start);
            }
        }

        getDocument().addDocumentListener(seListener);
    }


    static void checkWordsLine(String script, ArrayList<String> stringList, ArrayList<TTalk.wordType> typeList, boolean isCmd, boolean isEditor) {
        StringBuilder str = new StringBuilder(16);
        boolean inFunc = false;
        ArrayList<TTalk.wordType> Brackets = new ArrayList<>();

        // 単語分割する。演算子、括弧、コメント、文字列の分別。
        for (int i = 0; i < script.length(); i++) {
            char code = script.charAt(i);
            if (code == '+' || code == '-' || code == '*' || code == '/' || code == '^' || code == '&'
                    || code == '=' || code == '<' || code == '>' || code == '≠' || code == '≤' || code == '≥') {
                if (code == '-' && i > 0 && script.codePointAt(i - 1) == '-') {
                    // コメント
                    typeList.remove(typeList.size() - 1);
                    stringList.remove(stringList.size() - 1);
                    // 行の終わりまでコメント
                    typeList.add(TTalk.wordType.COMMENT);
                    stringList.add(script.substring(i - 1));
                    break;
                }
                if (!str.isEmpty()) {
                    if (Character.isDigit(str.charAt(0))) typeList.add(TTalk.wordType.STRING);
                    else typeList.add(TTalk.wordType.X);
                    stringList.add(str.toString().toLowerCase().intern());
                    str.setLength(0);
                }
                typeList.add(TTalk.wordType.OPERATOR);
                stringList.add(String.valueOf(code).intern());
            } else if (code == '(') {
                if (!str.isEmpty() || !typeList.isEmpty() && typeList.get(typeList.size() - 1) == TTalk.wordType.X) {
                    if (!str.isEmpty()) {
                        if (Character.isDigit(str.charAt(0))) typeList.add(TTalk.wordType.STRING);
                        else typeList.add(TTalk.wordType.X);
                        stringList.add(str.toString().toLowerCase().intern());
                        str.setLength(0);
                    }
                    String funcstr = stringList.get(stringList.size() - 1);
                    if (funcstr.equals("cd") || funcstr.equals("card") ||
                            funcstr.equals("bg") || funcstr.equals("bkgnd") || funcstr.equals("background") ||
                            funcstr.equals("btn") || funcstr.equals("button") ||
                            funcstr.equals("fld") || funcstr.equals("field") ||
                            funcstr.equals("stack") ||
                            funcstr.equals("char") || funcstr.equals("character") ||
                            funcstr.equals("item") || funcstr.equals("word") ||
                            funcstr.equals("line") ||
                            funcstr.equals("window") ||
                            funcstr.equals("menu") ||
                            funcstr.equals("id") ||
                            funcstr.equals("or") || funcstr.equals("and") || funcstr.equals("not") ||
                            funcstr.equals("div") || funcstr.equals("mod") || funcstr.equals("is") || funcstr.equals("in") || funcstr.equals("within") || funcstr.equals("a") || funcstr.equals("an")) {
                        typeList.add(TTalk.wordType.LBRACKET);
                    } else if (funcstr.equals("of") && stringList.size() >= 2 && (
                            /*stringList.get(stringList.size()-2).matches("^[0-9]*$") ||
                            stringList.get(stringList.size()-2)=="char" ||
                            stringList.get(stringList.size()-2)=="character" ||
                            stringList.get(stringList.size()-2)=="item" ||
                            stringList.get(stringList.size()-2)=="word" ||
                            stringList.get(stringList.size()-2)=="line" ||*/
                            !TTalk.funcSet.contains(stringList.get(stringList.size() - 2)))) {
                        typeList.add(TTalk.wordType.LBRACKET);
                        Brackets.add(TTalk.wordType.LBRACKET);
                        inFunc = false;
                    } else if (isCmd && typeList.size() == 1) {
                        // 左側がコマンドとして認識される場合
                        typeList.add(TTalk.wordType.LBRACKET);
                        Brackets.add(TTalk.wordType.LBRACKET);
                        inFunc = false;
                    } else {
                        typeList.add(TTalk.wordType.LFUNC);
                        Brackets.add(TTalk.wordType.LFUNC);
                        inFunc = true;
                    }
                    stringList.add("(");
                } else {
                    typeList.add(TTalk.wordType.LBRACKET);
                    stringList.add("(");
                }
            } else if (code == ')') {
                if (!str.isEmpty()) {
                    if (Character.isDigit(str.charAt(0))) typeList.add(TTalk.wordType.STRING);
                    else typeList.add(TTalk.wordType.X);
                    stringList.add(str.toString().toLowerCase().intern());
                    str.setLength(0);
                }
                if (Brackets.isEmpty() || Brackets.get(Brackets.size() - 1) == TTalk.wordType.LBRACKET) {
                    typeList.add(TTalk.wordType.RBRACKET);
                    if (!Brackets.isEmpty()) Brackets.remove(Brackets.size() - 1);
                } else if (Brackets.get(Brackets.size() - 1) == TTalk.wordType.LFUNC) {
                    typeList.add(TTalk.wordType.RFUNC);
                    if (!Brackets.isEmpty()) Brackets.remove(Brackets.size() - 1);
                }
                if (Brackets.isEmpty() || Brackets.get(Brackets.size() - 1) == TTalk.wordType.LBRACKET) {
                    inFunc = false;
                } else {
                    inFunc = true;
                }
                stringList.add(")");
            } else if (code == ',') {
                if (!str.isEmpty()) {
                    if (Character.isDigit(str.charAt(0))) typeList.add(TTalk.wordType.STRING);
                    else typeList.add(TTalk.wordType.X);
                    stringList.add(str.toString().toLowerCase().intern());
                    str.setLength(0);
                }
                if (inFunc) {
                    typeList.add(TTalk.wordType.COMMA_FUNC);// 関数の引数指定
                } else {
                    typeList.add(TTalk.wordType.COMMA);// loc/rectあるいはglobal、はたまたハンドラの引数
                }
                stringList.add(",");
            } else if (code == '"') {
                if (!str.isEmpty()) {
                    typeList.add(TTalk.wordType.X);
                    stringList.add(str.toString().toLowerCase().intern());
                    str.setLength(0);
                }
                i++;
                while (i < script.length()) {
                    code = script.charAt(i);
                    if (code == '"') break;
                    str.append(code);
                    i++;
                }
                if (isEditor) {
                    typeList.add(TTalk.wordType.QUOTE);
                    stringList.add("\"");
                }

                typeList.add(TTalk.wordType.STRING);
                stringList.add(str.toString());
                str.setLength(0);

                if (isEditor) {
                    typeList.add(TTalk.wordType.QUOTE);
                    stringList.add("\"");
                }
            } else if (code == ' ' || code == '\t') {
                if (!str.isEmpty()) {
                    if (Character.isDigit(str.charAt(0))) typeList.add(TTalk.wordType.STRING);
                    else typeList.add(TTalk.wordType.X);
                    stringList.add(str.toString().toLowerCase().intern());
                    str.setLength(0);
                }
            } else if (i == script.length() - 1) {
                str.append(code);
                if (!str.isEmpty()) {
                    if (Character.isDigit(str.charAt(0))) typeList.add(TTalk.wordType.STRING);
                    else typeList.add(TTalk.wordType.X);
                    stringList.add(str.toString().toLowerCase().intern());
                    str.setLength(0);
                }
            } else {
                str.append(code);
            }
        }
    }


    static void checkWordsLine2(String script, ArrayList<String> stringList, ArrayList<TTalk.wordType> typeList, boolean isCmd, StringBuilder handler, TreeSet<String> varSet) {
        String command = "";


        // 演算子を特定
        for (int i = 0; i < typeList.size(); i++) {
            TTalk.wordType theType = typeList.get(i);

            if ((theType == TTalk.wordType.X || theType == TTalk.wordType.OPERATOR) &&
                    TTalk.operatorSet.contains(stringList.get(i)/*.toLowerCase()*/)) {
                switch (stringList.get(i)) {
                    case "div" -> typeList.set(i, TTalk.wordType.OPERATOR);
                    case "mod" -> typeList.set(i, TTalk.wordType.OPERATOR);
                    case "not" -> typeList.set(i, TTalk.wordType.OPERATOR);
                    case "and" -> typeList.set(i, TTalk.wordType.OPERATOR);
                    case "or" -> typeList.set(i, TTalk.wordType.OPERATOR);
                    case "contains" -> typeList.set(i, TTalk.wordType.OPERATOR);
                    case "within" -> typeList.set(i, TTalk.wordType.OPERATOR);
                    case "there" -> {
                        if (i <= typeList.size() - 1 && stringList.get(i + 1).equals("is")) {
                            if (i <= typeList.size() - 2 && (stringList.get(i + 2).equals("a") || stringList.get(i + 2).equals("an"))) {
                                typeList.set(i, TTalk.wordType.OPERATOR);
                                typeList.set(i + 1, TTalk.wordType.OPERATOR_SUB);
                                typeList.set(i + 2, TTalk.wordType.OPERATOR_SUB);
                            } else if (i <= typeList.size() - 3 && 0 == stringList.get(i + 2).compareToIgnoreCase("not")) {
                                if (0 == stringList.get(i + 3).compareToIgnoreCase("a") || 0 == stringList.get(i + 3).compareToIgnoreCase("an")) {
                                    typeList.set(i, TTalk.wordType.OPERATOR);
                                    typeList.set(i + 1, TTalk.wordType.OPERATOR_SUB);
                                    typeList.set(i + 2, TTalk.wordType.OPERATOR_SUB);
                                    typeList.set(i + 3, TTalk.wordType.OPERATOR_SUB);
                                }
                                // else throw new xTalkException("there is notの後にa/anが必要です");
                            }
                            // else throw new xTalkException("there isの後にa/anが必要です");
                        }
                    }
                    case "is" -> {
                        if (i < stringList.size() - 1 && (stringList.get(i + 1).equals("in") || stringList.get(i + 1).equals("a") || stringList.get(i + 1).equals("an"))) {
                            typeList.set(i, TTalk.wordType.OPERATOR);
                            typeList.set(i + 1, TTalk.wordType.OPERATOR_SUB);
                        } else if (i <= typeList.size() - 2 && stringList.get(i + 1).equals("not")) {
                            if (0 == stringList.get(i + 2).compareToIgnoreCase("in") || 0 == stringList.get(i + 2).compareToIgnoreCase("a") || 0 == stringList.get(i + 2).compareToIgnoreCase("an")) {
                                typeList.set(i, TTalk.wordType.OPERATOR);
                                typeList.set(i + 1, TTalk.wordType.OPERATOR_SUB);
                                typeList.set(i + 2, TTalk.wordType.OPERATOR_SUB);
                            } else { // is not
                                typeList.set(i, TTalk.wordType.OPERATOR);
                                typeList.set(i + 1, TTalk.wordType.OPERATOR_SUB);
                            }
                        } else typeList.set(i, TTalk.wordType.OPERATOR);
                    }
                    case "<" -> {
                        if (i < typeList.size() - 1 && (stringList.get(i + 1).equals("=") || stringList.get(i + 1).equals(">"))) {
                            typeList.set(i, TTalk.wordType.OPERATOR);
                            typeList.set(i + 1, TTalk.wordType.OPERATOR_SUB);
                        }
                    }
                    case ">" -> {
                        if (i < typeList.size() - 1 && stringList.get(i + 1).equals("=")) {
                            typeList.set(i, TTalk.wordType.OPERATOR);
                            typeList.set(i + 1, TTalk.wordType.OPERATOR_SUB);
                        }
                    }
                }
            }
            /*else if(theType==wordType.LFUNC) {
                if(i>=start+1 && (typeAry[i-1]==wordType.OPERATOR ||
                        stringList.get(i-1)=="to"))
                    typeAry[i]=wordType.LBRACKET;
            }*/
        }

        // 各単語の分別
        for (int i = 0; i < typeList.size(); i++) {
            TTalk.wordType type = typeList.get(i);
            if (type == TTalk.wordType.X) {
                String str = stringList.get(i);
                if (i == 0 && isCmd) {
                    // 行の最初の単語
                    switch (str) {
                        case "repeat" -> {
                            typeList.set(i, TTalk.wordType.REPEAT);
                            command = "repeat";
                            continue;
                        }
                        case "if" -> {
                            typeList.set(i, TTalk.wordType.IF);
                            continue;
                        }
                        case "else" -> {
                            typeList.set(i, TTalk.wordType.ELSE);
                            continue;
                        }
                        case "end" -> {
                            if (typeList.size() > i + 1) {
                                if (stringList.get(i + 1).equals("if")) {
                                    typeList.set(i, TTalk.wordType.ENDIF);
                                    i++;
                                    typeList.set(i, TTalk.wordType.ENDIF);
                                    continue;
                                } else if (stringList.get(i + 1).equals("repeat")) {
                                    typeList.set(i, TTalk.wordType.END_REPEAT);
                                    i++;
                                    typeList.set(i, TTalk.wordType.END_REPEAT);
                                    continue;
                                } else if (stringList.get(i + 1).equalsIgnoreCase(handler.toString())) {
                                    typeList.set(i, TTalk.wordType.END_HAND);
                                    i++;
                                    typeList.set(i, TTalk.wordType.END_HAND);
                                    varSet.clear();
                                    handler.delete(0, handler.length());
                                    continue;
                                }
                            }
                        }
                        case "on" -> {
                            if (typeList.size() > i + 1) {
                                typeList.set(i, TTalk.wordType.ON_HAND);
                                i++;
                                typeList.set(i, TTalk.wordType.ON_HAND);
                                handler.append(stringList.get(i));
                                continue;
                            }
                        }
                        case "function" -> {
                            if (typeList.size() > i + 1) {
                                typeList.set(i, TTalk.wordType.ON_FUNC);
                                i++;
                                typeList.set(i, TTalk.wordType.ON_FUNC);
                                handler.append(stringList.get(i));
                                continue;
                            }
                        }
                    }
                }
                if ((i == 0 && isCmd) || i >= 1 && (typeList.get(i - 1) == TTalk.wordType.THEN || typeList.get(i - 1) == TTalk.wordType.ELSE)) {
                    // コマンドをかけるところの最初の単語
                    if (PCARD.pc.stack.rsrc != null && PCARD.pc.stack.rsrc.getxcmdId(str, "command") > 0) {
                        typeList.set(i, TTalk.wordType.XCMD);
                    } else if (str.equals("global")) {
                        typeList.set(i, TTalk.wordType.GLOBAL);
                    } else if (TTalk.commandSet.contains(str)) {
                        typeList.set(i, TTalk.wordType.CMD);
                        command = str;
                    } else if (str.equals("return")) {
                        typeList.set(i, TTalk.wordType.RETURN);
                    } else if (str.equals("pass")) {
                        if (typeList.size() > i + 1 && stringList.get(i + 1).equalsIgnoreCase(handler.toString())) {
                            typeList.set(i, TTalk.wordType.PASS);
                            i++;
                            typeList.set(i, TTalk.wordType.PASS);
                        }
                    } else if (str.equals("exit")) {
                        if (typeList.size() > i + 1 && stringList.get(i + 1).equalsIgnoreCase(handler.toString())) {
                            typeList.set(i, TTalk.wordType.EXIT);
                            i++;
                            typeList.set(i, TTalk.wordType.EXIT);
                        } else if (typeList.size() > i + 2 && stringList.get(i + 1).equalsIgnoreCase("to") && stringList.get(i + 2).equalsIgnoreCase("hypercard")) {
                            typeList.set(i, TTalk.wordType.EXIT);
                            i++;
                            typeList.set(i, TTalk.wordType.EXIT);
                            i++;
                            typeList.set(i, TTalk.wordType.EXIT);
                        } else if (typeList.size() > i + 1 && stringList.get(i + 1).equalsIgnoreCase("repeat")) {
                            typeList.set(i, TTalk.wordType.EXIT_REP);
                            i++;
                            typeList.set(i, TTalk.wordType.EXIT_REP);
                        }
                    } else if (str.equals("next")) {
                        if (typeList.size() > i + 1 && stringList.get(i + 1).equalsIgnoreCase("repeat")) {
                            typeList.set(i, TTalk.wordType.NEXT_REP);
                            i++;
                            typeList.set(i, TTalk.wordType.NEXT_REP);
                        }
                    } else if (str.equals("if")) {
                        typeList.set(i, TTalk.wordType.IF);
                    } else if (str.equals("else")) {
                        typeList.set(i, TTalk.wordType.ELSE);
                    } else if (str.equals("then")) {
                        typeList.set(i, TTalk.wordType.THEN);
                    } else {
                        typeList.set(i, TTalk.wordType.USER_CMD);
                    }
                } else {
                    // コマンドが書けないところ
                    if (str.equals("else")) {
                        typeList.set(i, TTalk.wordType.ELSE);
                        continue;
                    } else if (str.equals("then")) {
                        typeList.set(i, TTalk.wordType.THEN);
                        continue;
                    } else if (PCARD.pc.stack.rsrc != null && PCARD.pc.stack.rsrc.getxcmdId(str, "function") > 0) {
                        typeList.set(i, TTalk.wordType.XFCN);
                        continue;
                    } else if (typeList.get(0) == TTalk.wordType.ON_HAND ||
                            typeList.get(0) == TTalk.wordType.ON_FUNC) {
                        // 引数
                        typeList.set(i, TTalk.wordType.VARIABLE);
                        varSet.add(str);
                        continue;
                    } else if (str.equals("it") || (i > 0 && !stringList.get(i - 1).equals("the")) && (varSet != null && varSet.contains(str))) {
                        typeList.set(i, TTalk.wordType.VARIABLE);
                        continue;
                    } else if (TTalk.constantSet.contains(str)) {
                        typeList.set(i, TTalk.wordType.CONST);
                        continue;
                    } else if (str.equals("of") && i >= 1) {
                        if (typeList.get(i - 1) == TTalk.wordType.OBJECT) {
                            typeList.set(i, TTalk.wordType.OF_OBJ);
                            continue;
                        } else if (typeList.get(i - 1) == TTalk.wordType.CHUNK) {
                            typeList.set(i, TTalk.wordType.OF_CHUNK);
                            continue;
                        } else if (typeList.get(i - 1) == TTalk.wordType.FUNC) {
                            typeList.set(i, TTalk.wordType.OF_FUNC);
                            continue;
                        } else if (typeList.get(i - 1) == TTalk.wordType.PROPERTY) {
                            typeList.set(i, TTalk.wordType.OF_PROP);
                            continue;
                        }
                        if (i >= 2) {
                            if (typeList.get(i - 2) == TTalk.wordType.OBJECT) {
                                typeList.set(i, TTalk.wordType.OF_OBJ);
                                continue;
                            } else if (typeList.get(i - 2) == TTalk.wordType.CHUNK) {
                                typeList.set(i, TTalk.wordType.OF_CHUNK);
                                continue;
                            }
                        }
                    } else if (typeList.get(0) == TTalk.wordType.GLOBAL) {
                        // グローバル変数
                        typeList.set(i, TTalk.wordType.VARIABLE);
                        varSet.add(str);
                        continue;
                    } else if (i >= 1 && typeList.get(i - 1) == TTalk.wordType.REPEAT && (str.equals("with") || str.equals("until") || str.equals("while"))) {
                        typeList.set(i, TTalk.wordType.REPEAT);
                        continue;
                    } else if (i >= 1 && typeList.get(i - 1) == TTalk.wordType.REPEAT && stringList.get(i - 1).equals("with")) {
                        // repeat変数
                        typeList.set(i, TTalk.wordType.VARIABLE);
                        varSet.add(str);
                        continue;
                    } else if (command.equals("put") && (str.equals("into") || str.equals("after") || str.equals("before"))) {
                        typeList.set(i, TTalk.wordType.CMD_SUB);
                        continue;
                    } else if (command.equals("add") && str.equals("to")) {
                        typeList.set(i, TTalk.wordType.CMD_SUB);
                        continue;
                    } else if (command.equals("subtract") && str.equals("from")) {
                        typeList.set(i, TTalk.wordType.CMD_SUB);
                        continue;
                    } else if (command.equals("divide") && str.equals("by")) {
                        typeList.set(i, TTalk.wordType.CMD_SUB);
                        continue;
                    } else if (command.equals("multiply") && str.equals("by")) {
                        typeList.set(i, TTalk.wordType.CMD_SUB);
                        continue;
                    } else if (command.equals("show") && str.equals("at")) {
                        typeList.set(i, TTalk.wordType.CMD_SUB);
                        continue;
                    } else if (command.equals("answer") && (str.equals("with") || str.equals("or"))) {
                        typeList.set(i, TTalk.wordType.CMD_SUB);
                        continue;
                    } else if (command.equals("ask") && str.equals("with")) {
                        typeList.set(i, TTalk.wordType.CMD_SUB);
                        continue;
                    } else if (command.equals("set") && str.equals("to")) {
                        typeList.set(i, TTalk.wordType.CMD_SUB);
                        continue;
                    } else if (command.equals("send") && str.equals("to")) {
                        typeList.set(i, TTalk.wordType.CMD_SUB);
                        continue;
                    } else if (command.equals("visual") && str.equals("effect")) {
                        typeList.set(i, TTalk.wordType.CMD_SUB);
                        continue;
                    } else if (command.equals("play") && str.equals("stop")) {
                        typeList.set(i, TTalk.wordType.CMD_SUB);
                        continue;
                    } else if (command.equals("pop") && str.equals("card")) {
                        typeList.set(i, TTalk.wordType.CMD_SUB);
                        continue;
                    } else if (command.equals("push") && str.equals("card")) {
                        typeList.set(i, TTalk.wordType.CMD_SUB);
                        continue;
                    } else if (command.equals("drag") && (str.equals("from") || str.equals("to") || str.equals("with"))) {
                        typeList.set(i, TTalk.wordType.CMD_SUB);
                        continue;
                    } else if (command.equals("click") && (str.equals("at") || str.equals("with"))) {
                        typeList.set(i, TTalk.wordType.CMD_SUB);
                        continue;
                    } else if ((command.equals("lock") || command.equals("unlock")) &&
                            (str.equals("screen") || str.equals("with") || str.equals("messages") || str.equals("recent") || str.equals("errordialogs"))) {
                        typeList.set(i, TTalk.wordType.CMD_SUB);
                        continue;
                    } else if (command.equals("wait") && (str.equals("until") || str.equals("while"))) {
                        typeList.set(i, TTalk.wordType.CMD_SUB);
                        continue;
                    } else if (command.equals("convert") && (str.equals("to"))) {
                        typeList.set(i, TTalk.wordType.CMD_SUB);
                        continue;
                    } else if (command.equals("open") && (str.equals("file"))) {
                        typeList.set(i, TTalk.wordType.CMD_SUB);
                        continue;
                    } else if (command.equals("read") && (str.equals("from") || str.equals("file") || str.equals("at") || str.equals("for") || str.equals("until"))) {
                        typeList.set(i, TTalk.wordType.CMD_SUB);
                        continue;
                    } else if (command.equals("repeat") && (str.equals("to") || str.equals("down"))) {
                        typeList.set(i, TTalk.wordType.REPEAT);
                        continue;
                    } else if (str.equals("cd") || str.equals("card")
                            || str.equals("bg") || str.equals("bkgnd") || str.equals("background")
                            || str.equals("btn") || str.equals("button")
                            || str.equals("fld") || str.equals("field")
                            || str.equals("stack")
                            || str.equals("window")
                            || str.equals("menu")
                            || str.equals("menubar") || str.equals("titlebar") || str.equals("msg") || str.equals("message")
                            || str.equals("hypercard")
                            || str.equals("me")
                            || (str.equals("id") && i + 1 < stringList.size() && !stringList.get(i + 1).equals("of"))) {
                        typeList.set(i, TTalk.wordType.OBJECT);
                        continue;
                    } else if (str.equals("char") || str.equals("character")
                            || str.equals("item") || str.equals("word")
                            || str.equals("line")) {
                        typeList.set(i, TTalk.wordType.CHUNK);
                        continue;
                    } else if (i >= 1 && typeList.get(i - 1) == TTalk.wordType.CMD_SUB &&
                            (command.equals("put") || command.equals("add") || command.equals("subtract") || command.equals("divide") || command.equals("multiply"))) {
                        // 変数
                        typeList.set(i, TTalk.wordType.VARIABLE);
                        varSet.add(str);
                        continue;
                    } else if (TTalk.propertySet.contains(str) ||
                            str.equals("long") || str.equals("short")) {
                        if ((i >= 1 && stringList.get(i - 1).equals("the")) ||
                                (i >= 1 && stringList.get(i - 1).equals("set")) ||
                                (i + 1 < stringList.size() && stringList.get(i + 1).equals("of"))) {
                            typeList.set(i, TTalk.wordType.PROPERTY);
                            continue;
                        }
                    }
                    if (TTalk.funcSet.contains(str)) {
                        if (str.equals("number") && i + 2 < stringList.size() && stringList.get(i + 1).equals("of") &&
                                !stringList.get(i + 2).matches("s$")) {
                            // number of xxsは関数だが、そうでない場合はプロパティ
                            typeList.set(i, TTalk.wordType.PROPERTY);
                        } else if (str.equals("number") && i - 1 > 0 && stringList.get(i - 1).equals("a")) {
                            typeList.set(i, TTalk.wordType.X);
                        } else {
                            typeList.set(i, TTalk.wordType.FUNC);
                        }
                    } else if (i + 1 < stringList.size() && !stringList.get(i).equals("of") && typeList.get(i + 1) == TTalk.wordType.LFUNC) {
                        typeList.set(i, TTalk.wordType.USER_FUNC);
                    }
                }
            }
        }

        for (int i = 0; i < typeList.size(); i++) {
            TTalk.wordType type = typeList.get(i);
            if (type == TTalk.wordType.X) {
                String str = stringList.get(i);
                if ((str.equals("first") || str.equals("last") || str.equals("prev") || str.equals("previous") || str.equals("next")) && i + 1 < stringList.size() && typeList.get(i + 1) == TTalk.wordType.OBJECT) {
                    typeList.set(i, TTalk.wordType.OBJECT);
                }
                if ((str.equals("first") || str.equals("last") || str.equals("middle") || str.equals("any")) && i + 1 < stringList.size() && typeList.get(i + 1) == TTalk.wordType.CHUNK) {
                    typeList.set(i, TTalk.wordType.CHUNK);
                }
            }
        }

        for (int i = 0; i < typeList.size(); i++) {
            TTalk.wordType type = typeList.get(i);
            if (type == TTalk.wordType.X) {
                String str = stringList.get(i);
                if (str.equals("the") && i + 1 < stringList.size() && typeList.get(i + 1) == TTalk.wordType.FUNC && (i + 2 >= stringList.size() || !stringList.get(i + 2).equals("of"))) {
                    typeList.set(i, TTalk.wordType.THE_FUNC);
                }
                if (str.equals("the") && i + 1 < stringList.size() && typeList.get(i + 1) == TTalk.wordType.PROPERTY) {
                    typeList.set(i, TTalk.wordType.PROPERTY);
                }
                if (str.equals("this") && i + 1 < stringList.size() && typeList.get(i + 1) == TTalk.wordType.OBJECT) {
                    typeList.set(i, TTalk.wordType.OBJECT);
                }
            }
        }
    }


    boolean compareObject(OObject obj) {
        return (obj == this.object);
    }
    /*
        if(ObjType.equals(obj.objectType) && ObjId == obj.id){
            if(obj.parent != null){
                if(ParentType.equals(obj.parent.objectType) && ParentId == obj.parent.id){
                    return true;
                }
            }else if(obj.objectType.equals("stack")){
                return true;// スタックごとにエディタウィンドウを持つので必ず一致
            }
        }
        return false;
    }*/

    // オブジェクトの親が一致するか
    /*boolean compareParent(OObject parent){
        if(ParentType!=null && ParentType.equals("background") && parent != null && parent.objectType.equals("card")){
            OCard cd = (OCard)parent;
            if(ParentId == cd.bg.id){
                return true;
            }
        } else {
            if(ParentType!=null && ParentType.equals(parent.objectType) && ParentId == parent.id){
                return true;
            }
        }
        return false;
    }*/

    void setObject(OObject obj) {
        this.object = obj;
    }
    /*    ObjType = obj.objectType;
        ObjId = obj.id;
        if(obj.parent != null){
            ParentType = obj.parent.objectType;
            ParentId = obj.parent.id;
        }
    }*/

    /*OObject getObject(OCard parent){
        if(ObjType.equals("stack")) return parent.stack;
        if(ObjType.equals("background") && ObjId == parent.bgid) return parent.bg;
        if(ObjType.equals("card") && ObjId == parent.id) return parent;
        if(ParentType.equals("bg")){
            OBackground bg = parent.bg;
            if(bg == null){
                bg = PCARD.pc.stack.GetBackgroundbyId(ParentId);
            }
            if(ObjType.equals("field") && ParentId == parent.bgid) return bg.GetFldbyId(ObjId);
            if(ObjType.equals("button") && ParentId == parent.bgid) return bg.GetBtnbyId(ObjId);
        }else{
            if(ObjType.equals("field") && ParentId == parent.id) return parent.GetFldbyId(ObjId);
            if(ObjType.equals("button") && ParentId == parent.id) return parent.GetBtnbyId(ObjId);
        }
        return null;
    }*/

    /*OObject getObjectOtherCard(){
        if(ParentType.equals("card")){
            OCard card = PCARD.pc.stack.GetCardbyId(ParentId);
            return getObject(card);
        } else {
            OCard card = PCARD.pc.stack.GetCardofBackgroundbyId(ParentId);
            return getObject(card);
        }
    }*/

    void changedScript() {
        saved = false;
        parent.getRootPane().putClientProperty("windowModified", Boolean.TRUE);
        if (!getText().equals(undobuf.get(undobuf.size() - 1))) {
            undobuf.add(getText());
            if (undobuf.size() >= 2) {
                // 単語単位でundobufに登録する
                String str1 = undobuf.get(undobuf.size() - 2);
                String str2 = undobuf.get(undobuf.size() - 1);
                boolean wordflag = false;
                int i1 = 0, i2 = 0;
                for (int i = 0; i + i1 < str1.length() && i + i2 < str2.length(); i++) {
                    if (str1.charAt(i + i1) == str2.charAt(i + i2)) continue;
                    if (/*str1.charAt(i+i1)=='\n' ||
                        str1.charAt(i+i1)==' ' ||*/
                            str2.charAt(i + i2) == '\n' ||
                                    str2.charAt(i + i2) == ' ') {
                        wordflag = true;
                    } else {
                        if (str1.length() - i1 > str2.length()) i1++;
                        if (str1.length() < str2.length() - i2) i2++;
                        i--;
                    }
                }
                if (wordflag == false) {
                    undobuf.remove(undobuf.size() - 2);
                }
            }
            if (undobuf.size() > 32) {
                undobuf.remove(0);
            }
            redobuf.clear();
            setCanUndo();
        }
    }

    void savedScript() {
        saved = true;
        parent.getRootPane().putClientProperty("windowModified", Boolean.FALSE);
    }

    static void setCanUndo() {
        SEMenu.undoMenu.setEnabled(true);
    }

    ArrayList<String> undobuf = new ArrayList<>();
    ArrayList<String> redobuf = new ArrayList<>();

    void undoStatus() {
        // SEMenu.undoMenu.setEnabled(undo.canUndo());
        // SEMenu.redoMenu.setEnabled(undo.canRedo());
        SEMenu.undoMenu.setEnabled(undobuf.size() > 1);
        SEMenu.redoMenu.setEnabled(!redobuf.isEmpty());
    }

    void undo() {
        if (undobuf.size() > 1) {
            String str1 = getText();
            String str2 = undobuf.get(undobuf.size() - 2);
            int pos = getCaretPosition() + str2.length() - str1.length();
            redobuf.add(str1);
            getDocument().removeDocumentListener(seListener);
            setText(str2);
            undobuf.remove(undobuf.size() - 1);
            getDocument().addDocumentListener(seListener);
            undoStatus();
            new checkWordsThread().start();
            if (pos >= 0 && pos < getText().length()) {
                setCaretPosition(pos);
            }
        }
    }

    void redo() {
        if (!redobuf.isEmpty()) {
            int pos = getCaretPosition();
            undobuf.add(getText());
            getDocument().removeDocumentListener(seListener);
            setText(redobuf.get(redobuf.size() - 1));
            redobuf.remove(redobuf.size() - 1);
            getDocument().addDocumentListener(seListener);
            undoStatus();
            new checkWordsThread().start();
            if (pos < getText().length()) {
                setCaretPosition(pos);
            }
        }
    }


    class SAFocusListener implements FocusListener {

        @Override
        public void focusGained(FocusEvent e) {
            if (saved) savedScript();
            else changedScript();
            undoStatus();
        }

        @Override
        public void focusLost(FocusEvent e) {
        }
    }
}


class SEMenu {

    /**
     *
     */
    static JMenuItem undoMenu = null;
    static JMenuItem redoMenu = null;

    public SEMenu(ScriptEditor scriptEditor) {
        ActionListener listener = null;

        listener = new SEMenuListener();

        // メニューバーの設定
        JMenuBar mb = new JMenuBar();
        scriptEditor.setJMenuBar(mb);

        JMenu m;
        JMenuItem mi;
        int s = InputEvent.CTRL_DOWN_MASK;
        if (System.getProperty("os.name").toLowerCase().startsWith("mac os x")) {
            s = InputEvent.META_DOWN_MASK;
        }
        int s_opt = s + InputEvent.ALT_DOWN_MASK;
        int s_shift = s + InputEvent.SHIFT_DOWN_MASK;

        // Fileメニュー
        m = new JMenu(PCARD.pc.intl.getText("File"));
        mb.add(m);
        m.add(mi = new JMenuItem(PCARD.pc.intl.getText("Close")));
        mi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W, s));
        mi.addActionListener(listener);
        m.add(mi = new JMenuItem(PCARD.pc.intl.getText("Save")));
        mi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, s));
        mi.addActionListener(listener);
        m.addSeparator();
        m.add(mi = new JMenuItem(PCARD.pc.intl.getText("Print…")));
        mi.setEnabled(false);

        // Editメニュー
        m = new JMenu(PCARD.pc.intl.getText("Edit"));
        mb.add(m);
        m.add(mi = new JMenuItem(PCARD.pc.intl.getText("Undo")));
        mi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, s));
        mi.addActionListener(listener);
        undoMenu = mi;
        m.add(mi = new JMenuItem(PCARD.pc.intl.getText("Redo")));
        mi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, s_shift));
        mi.addActionListener(listener);
        redoMenu = mi;
        m.addSeparator();
        m.add(mi = new JMenuItem(PCARD.pc.intl.getText("Cut")));
        mi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, s));
        mi.addActionListener(listener);
        m.add(mi = new JMenuItem(PCARD.pc.intl.getText("Copy")));
        mi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, s));
        mi.addActionListener(listener);
        m.add(mi = new JMenuItem(PCARD.pc.intl.getText("Paste")));
        mi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, s));
        mi.addActionListener(listener);
        m.add(mi = new JMenuItem(PCARD.pc.intl.getText("Select All")));
        mi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, s));
        mi.addActionListener(listener);
        m.addSeparator();
        m.add(mi = new JMenuItem(PCARD.pc.intl.getText("Find")));
        mi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, s));
        mi.addActionListener(listener);
        m.add(mi = new JMenuItem(PCARD.pc.intl.getText("Find Next")));
        mi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_G, s));
        mi.addActionListener(listener);
        m.add(mi = new JMenuItem(PCARD.pc.intl.getText("Find Prev")));
        mi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_G, s_shift));
        mi.addActionListener(listener);
        m.add(mi = new JMenuItem(PCARD.pc.intl.getText("Replace")));
        mi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, s));
        mi.addActionListener(listener);
        m.add(mi = new JMenuItem(PCARD.pc.intl.getText("Replace Next")));
        mi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T, s));
        mi.addActionListener(listener);
        m.add(mi = new JMenuItem(PCARD.pc.intl.getText("Replace Prev")));
        mi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T, s_shift));
        mi.addActionListener(listener);


        // Scriptメニュー
        m = new JMenu(PCARD.pc.intl.getText("Script"));
        mb.add(m);
        m.add(mi = new JMenuItem(PCARD.pc.intl.getText("Edit Card Script")));
        mi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_1, s_opt));
        mi.addActionListener(listener);
        m.add(mi = new JMenuItem(PCARD.pc.intl.getText("Edit Background Script")));
        mi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_2, s_opt));
        mi.addActionListener(listener);
        m.add(mi = new JMenuItem(PCARD.pc.intl.getText("Edit Stack Script")));
        mi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_3, s_opt));
        mi.addActionListener(listener);
        m.add(mi = new JMenuItem(PCARD.pc.intl.getText("Next Window")));
        mi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, s));
        mi.addActionListener(listener);
        m.addSeparator();
        m.add(mi = new JMenuItem(PCARD.pc.intl.getText("Comment")));
        mi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, s));
        mi.addActionListener(listener);
        m.add(mi = new JMenuItem(PCARD.pc.intl.getText("Uncomment")));
        mi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS, s));
        mi.addActionListener(listener);

    }
}

// メニュー動作
class SEMenuListener implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
        String in_cmd = e.getActionCommand();
        String cmd = PCARD.pc.intl.getEngText(in_cmd);
        JTabbedPane tabpane = ScriptEditor.tabPane;
        JViewport vp = (JViewport) ((JScrollPane) tabpane.getSelectedComponent()).getComponent(0);
        if (vp.getComponentCount() == 0) return;
        ScriptArea area = (ScriptArea) vp.getComponent(0);
        if (cmd.equals("Cut")) {
            area.cut();
        }
        if (cmd.equals("Copy")) {
            area.copy();
        }
        if (cmd.equals("Paste")) {
            area.paste();
        }
        if (cmd.equals("Select All")) {
            area.selectAll();
        }
        if (cmd.equals("Undo")) {
            area.undo();
            area.undoStatus();
        }
        if (cmd.equals("Redo")) {
            area.redo();
            area.undoStatus();
        }
        if (cmd.equals("Find")) {
            new FindDialog(area.parent);
            cmd = FindDialog.clicked;
        }
        if (cmd.equals("Find Next")) {
            String searchedStr = area.getText().substring(area.getSelectionEnd()).toLowerCase();
            if (searchedStr.contains(area.parent.FindStr.toLowerCase())) {
                area.setSelectionStart(area.getSelectionEnd() + searchedStr.indexOf(area.parent.FindStr.toLowerCase()));
                area.setSelectionEnd(area.getSelectionStart() + area.parent.FindStr.length());
            } else {
                java.awt.Toolkit.getDefaultToolkit().beep();
            }
        }
        if (cmd.equals("Find Prev")) {
            String searchedStr = area.getText().substring(0, area.getSelectionStart()).toLowerCase();
            if (searchedStr.lastIndexOf(area.parent.FindStr.toLowerCase()) >= 0) {
                area.setSelectionStart(searchedStr.lastIndexOf(area.parent.FindStr.toLowerCase()));
                area.setSelectionEnd(area.getSelectionStart() + area.parent.FindStr.length());
            } else {
                java.awt.Toolkit.getDefaultToolkit().beep();
            }
        }
        if (cmd.equals("Replace")) {
            new ReplaceDialog(area.parent);
            cmd = ReplaceDialog.clicked;
        }
        if (cmd.equals("Replace Next")) {
            int savStart = area.getSelectionStart();
            String searchedStr = area.getText().substring(area.getSelectionStart()).toLowerCase();
            if (searchedStr.contains(area.parent.FindStr.toLowerCase())) {
                String text = area.getText();
                text = text.substring(0, area.getSelectionStart() + searchedStr.indexOf(area.parent.FindStr.toLowerCase())) +
                        area.parent.ReplaceStr +
                        text.substring(area.getSelectionStart() + searchedStr.indexOf(area.parent.FindStr.toLowerCase()) + area.parent.FindStr.length());
                area.setText(text);

                area.setSelectionStart(savStart + searchedStr.indexOf(area.parent.FindStr.toLowerCase()));
                area.setSelectionEnd(area.getSelectionStart() + area.parent.ReplaceStr.length());
            } else {
                java.awt.Toolkit.getDefaultToolkit().beep();
            }
        }
        if (cmd.equals("Replace Prev")) {
            String searchedStr = area.getText().substring(0, area.getSelectionEnd());
            if (searchedStr.lastIndexOf(area.parent.FindStr.toLowerCase()) >= 0) {
                String text = area.getText();
                text = text.substring(0, searchedStr.lastIndexOf(area.parent.FindStr.toLowerCase())) +
                        area.parent.ReplaceStr +
                        text.substring(searchedStr.lastIndexOf(area.parent.FindStr.toLowerCase()) + area.parent.FindStr.length());
                area.setText(text);

                area.setSelectionStart(searchedStr.lastIndexOf(area.parent.FindStr.toLowerCase()));
                area.setSelectionEnd(area.getSelectionStart() + area.parent.ReplaceStr.length());
            } else {
                java.awt.Toolkit.getDefaultToolkit().beep();
            }
        }
        if (cmd.equals("Replace All")) {
            area.setSelectionStart(0);
            while (true) {
                int savStart = area.getSelectionStart();
                String searchedStr = area.getText().substring(area.getSelectionStart()).toLowerCase();
                if (searchedStr.contains(area.parent.FindStr.toLowerCase())) {
                    String text = area.getText();
                    text = text.substring(0, area.getSelectionStart() + searchedStr.indexOf(area.parent.FindStr.toLowerCase())) +
                            area.parent.ReplaceStr +
                            text.substring(area.getSelectionStart() + searchedStr.indexOf(area.parent.FindStr.toLowerCase()) + area.parent.FindStr.length());
                    area.setText(text);

                    area.setSelectionStart(savStart + searchedStr.indexOf(area.parent.FindStr.toLowerCase()));
                    area.setSelectionEnd(area.getSelectionStart() + area.parent.ReplaceStr.length());
                } else break;
            }
        }
        if (cmd.equals("Close")) {
            if (true == ScriptEditor.saveAlert(area)) {
                tabpane.remove(tabpane.getSelectedComponent());
                if (0 == tabpane.getComponentCount()) {
                    ScriptEditor.maybeExit();
                }
            }
        }
        if (cmd.equals("Save")) {
            ScriptEditor.saveScript(area);
        }

        {
            OObject obj = area.object;

            if (cmd.equals("Edit Card Script")) {
                OCard cd = PCARD.pc.stack.curCard;
                if (obj.objectType.equals("card")) cd = (OCard) obj;
                else if (obj.parent != null && obj.parent.objectType.equals("card")) cd = (OCard) obj.parent;
                ScriptEditor.openScriptEditor(PCARD.pc.stack.pcard, cd);
            }
            if (cmd.equals("Edit Background Script")) {
                OBackground bg = PCARD.pc.stack.curCard.bg;
                if (obj.objectType.equals("background")) bg = (OBackground) obj;
                else if (obj.objectType.equals("card")) bg = ((OCard) obj).bg;
                else if (obj.parent != null && obj.parent.objectType.equals("background"))
                    bg = (OBackground) obj.parent;
                ScriptEditor.openScriptEditor(PCARD.pc.stack.pcard, bg);
            }
            if (cmd.equals("Edit Stack Script")) {
                ScriptEditor.openScriptEditor(PCARD.pc.stack.pcard, PCARD.pc.stack);
            }
        }

        if (cmd.equals("Next Window")) {
            PCARD.pc.stack.pcard.toFront();
        }
        if (cmd.equals("Comment")) {
            String allScript = area.getText();
            int start = area.getSelectionStart();
            int end = area.getSelectionEnd();
            if (start == 0 && end == 0) return;
            if (end == 0) end = start;

            StringBuilder newScript = new StringBuilder(allScript.substring(0, start));
            String[] scrAry = (allScript.substring(start, end)).split("\n");
            for (int i = 0; i < scrAry.length; i++) {
                int spacing = 0;
                while (spacing < scrAry[i].length() && (scrAry[i].charAt(spacing) == ' ' || scrAry[i].charAt(spacing) == '　'))
                    spacing++;
                scrAry[i] = scrAry[i].substring(0, spacing) + "-- " + scrAry[i].substring(spacing);

                newScript.append(scrAry[i]);
                if (i < scrAry.length - 1) newScript.append("\n");
            }
            newScript.append(allScript.substring(end));
            area.setText(newScript.toString());
        }
        if (cmd.equals("Uncomment")) {
            String allScript = area.getText();
            int start = area.getSelectionStart();
            int end = area.getSelectionEnd();
            if (start == 0 && end == 0) return;
            if (end == 0) end = start;

            StringBuilder newScript = new StringBuilder(allScript.substring(0, start));
            String[] scrAry = (allScript.substring(start, end)).split("\n");
            for (int i = 0; i < scrAry.length; i++) {
                int spacing = 0;
                while (spacing < scrAry[i].length() && (scrAry[i].charAt(spacing) == ' ' || scrAry[i].charAt(spacing) == '　'))
                    spacing++;
                int comment = 0;
                while (spacing < scrAry[i].length() && (scrAry[i].charAt(spacing + comment) == ' ' || scrAry[i].charAt(spacing + comment) == '-'))
                    comment++;
                scrAry[i] = scrAry[i].substring(0, spacing) + scrAry[i].substring(spacing + comment);

                newScript.append(scrAry[i]);
                if (i < scrAry.length - 1) newScript.append("\n");
            }
            newScript.append(allScript.substring(end));
            area.setText(newScript.toString());
        }
        if (cmd.equals("Step")) {
            TTalk.stepflag = true;
        }
        if (cmd.equals("Trace")) {
            TTalk.tracemode = 1;
            TTalk.stepflag = true;
        }
        if (cmd.equals("Run")) {
            TTalk.tracemode = 0;
            TTalk.stepflag = true;
        }
        if (cmd.equals("Variable Watcher")) {
            VariableWatcher.watcherWindow.setVisible(true);
        }
    }
}

class FindDialog extends JDialog implements ActionListener {

    /**
     *
     */
    @Serial
    private static final long serialVersionUID = 1L;
    static String clicked = "";
    JTextField area;

    FindDialog(ScriptEditor owner) {
        super(owner, true);
        setTitle(PCARD.pc.intl.getDialogText("Find String"));
        getContentPane().setLayout(new BorderLayout());

        // パネルを追加する
        JPanel topPanel = new JPanel();
        // topPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        getContentPane().add("North", topPanel);
        JPanel btmPanel = new JPanel();
        btmPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        getContentPane().add("South", btmPanel);

        String text = owner.FindStr;
        area = new JTextField(text);
        area.setPreferredSize(new Dimension(380, 28));
        // area.setSize(380, 20);
        // area.setMargin(new Insets(16,16,16,16));
        // area.setLineWrap(true);
        // area.setBorder(new LineBorder(Color.BLACK));
        topPanel.add(area);

        JButton btn1 = new JButton(PCARD.pc.intl.getDialogText("Find Prev"));
        btn1.addActionListener(this);
        btmPanel.add(btn1);

        JButton btn2 = new JButton(PCARD.pc.intl.getDialogText("Find Next"));
        btn2.addActionListener(this);
        btmPanel.add(btn2);
        getRootPane().setDefaultButton(btn2);

        setBounds(owner.getX() + owner.getWidth() / 2 - 200, owner.getY() + owner.getHeight() / 2 - area.getPreferredSize().height / 2, 400, area.getPreferredSize().height + 80);
        setResizable(false);
        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        clicked = e.getActionCommand();
        ((ScriptEditor) getOwner()).FindStr = area.getText();
        this.dispose();
    }
}

class ReplaceDialog extends JDialog implements ActionListener {

    /**
     *
     */
    @Serial
    private static final long serialVersionUID = 1L;
    static String clicked = "";
    JTextField area1;
    JTextField area2;

    ReplaceDialog(ScriptEditor owner) {
        super(owner, true);
        setTitle(PCARD.pc.intl.getDialogText("Replace String"));
        getContentPane().setLayout(new BorderLayout());

        // パネルを追加する
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        topPanel.setPreferredSize(new Dimension(380, 60));
        getContentPane().add("North", topPanel);
        JPanel btmPanel = new JPanel();
        btmPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        getContentPane().add("South", btmPanel);

        String text1 = owner.FindStr;
        area1 = new JTextField(text1);
        area1.setPreferredSize(new Dimension(380, 28));
        // area1.setMargin(new Insets(8,16,8,16));
        // area1.setLineWrap(true);
        // area1.setBorder(new LineBorder(Color.BLACK));
        topPanel.add(area1);


        String text2 = owner.ReplaceStr;
        area2 = new JTextField(text2);
        area2.setPreferredSize(new Dimension(380, 28));
        // area2.setMargin(new Insets(8,16,8,16));
        // area2.setLineWrap(true);
        // area2.setBorder(new LineBorder(Color.BLACK));
        area2.transferFocus();
        topPanel.add(area2);
        topPanel.add(area1);

        // タブキーによる移動
        Set<KeyStroke>
                strokes = new HashSet<>(Collections.singletonList(KeyStroke.getKeyStroke("pressed TAB")));
        area1.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, strokes);
        strokes = new HashSet<>(Collections.singletonList(KeyStroke.getKeyStroke("pressed TAB")));
        area2.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, strokes);

        JButton btn3 = new JButton(PCARD.pc.intl.getDialogText("Replace All"));
        btn3.addActionListener(this);
        btmPanel.add(btn3);

        JButton btn1 = new JButton(PCARD.pc.intl.getDialogText("Replace Prev"));
        btn1.addActionListener(this);
        btmPanel.add(btn1);

        JButton btn2 = new JButton(PCARD.pc.intl.getDialogText("Replace Next"));
        btn2.addActionListener(this);
        btmPanel.add(btn2);
        getRootPane().setDefaultButton(btn2);

        setBounds(owner.getX() + owner.getWidth() / 2 - 200, owner.getY() + owner.getHeight() / 2 - area1.getPreferredSize().height, 400, area1.getPreferredSize().height * 2 + 110);
        setResizable(false);
        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        clicked = e.getActionCommand();
        ((ScriptEditor) getOwner()).FindStr = area1.getText();
        ((ScriptEditor) getOwner()).ReplaceStr = area2.getText();
        this.dispose();
    }
}