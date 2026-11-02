package com.github.hyperZebra;

import java.awt.Rectangle;
import java.io.Serial;
import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.JTable;


public class MessageWatcher extends JDialog {

    @Serial
    private static final long serialVersionUID = -766099048104505721L;

    static MessageWatcher watcherWindow;
    static private JScrollPane scrollPane;

    private final String[] columnNames = {"Message", "Object"};

    public MessageWatcher() {
        super(/* PCARD.pc */);

        // Register object in window list
        setTitle("MessageWatcher");
        new OWindow(this);
        watcherWindow = this;

        // Prepare table
        String[][] tableData = {{"", ""}};

        JTable table = new JTable(tableData, columnNames);
        //table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setEnabled(false);
        table.setDefaultRenderer(Object.class, new MultiLineCellRenderer());

        // Scroll
        scrollPane = new JScrollPane(table);
        add(scrollPane);

        // Set window position and size
        setBounds(0, 0, 320, 240);
        setLocationRelativeTo(null);
    }

    public void setTable(String[] messageRingArray, OObject[] objectRingArray, int ringCnt) {
        int total = Math.min(ringCnt, messageRingArray.length);
        String[][] tabledata = new String[total][2];

        int i;
        for (i = 0; i < total; i++) {
            int j = (ringCnt + i) % messageRingArray.length;
            int i2 = total - i - 1;
            tabledata[i2][0] = messageRingArray[j];
            if (objectRingArray[j] != null) {
                tabledata[i2][1] = objectRingArray[j].getShortName();
            }
        }

        JTable table = new JTable(tabledata, columnNames);
        //table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setEnabled(false);
        table.setDefaultRenderer(Object.class, new MultiLineCellRenderer());

        Rectangle rect = scrollPane.getBounds();
        int scroll = scrollPane.getVerticalScrollBar().getValue();
        remove(scrollPane);
        scrollPane = new JScrollPane(table);
        add(scrollPane);
        scrollPane.getVerticalScrollBar().setValue(scroll);
//        scrollPane.removeAll();
//        scrollPane.add(table);
//        scrollPane.setViewportView(table);
        scrollPane.setBounds(rect);
        //repaint();
    }
}
