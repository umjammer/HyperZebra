package com.github.hyperZebra;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.PointerInfo;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import javax.imageio.ImageIO;
import javax.swing.AbstractButton;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JToggleButton;
import javax.swing.border.EmptyBorder;

import com.github.hyperZebra.Rsrc.PlteBtnObject;


public class TUtil {

    static TUtil tutil = new TUtil();


    // HyperTalk built-in functions
    public static Result CallSystemFunction(String message, String[] params, OObject target, MemoryData memData, boolean isFunc)
            throws xTalkException {
        Result result = new Result();
        result.ret = 1; // Return 1 if not found

        message = message.toLowerCase();

        if (0 == message.compareTo("trunc")) {
            if (params.length < 1) throw new xTalkException("Argument is required");
            double v;
            try {
                v = Double.parseDouble(params[0]);
            } catch (Exception e) {
                if (params[0].equals("∞")) v = Double.MAX_VALUE;
                else throw new xTalkException("A number is required here");
            }
            result.theResult = Integer.toString((int) v);
            result.ret = 0;
        } else if (0 == message.compareTo("round")) {
            if (params.length < 1) throw new xTalkException("Argument is required");
            double v;
            try {
                v = Double.parseDouble(params[0]);
            } catch (Exception e) {
                if (params[0].equals("∞")) v = Double.MAX_VALUE;
                else throw new xTalkException("A number is required here");
            }
            result.theResult = Integer.toString(IEEE_Round(v));
            result.ret = 0;
        } else if (0 == message.compareTo("abs")) {
            if (params.length < 1) throw new xTalkException("Argument is required");
            double v;
            try {
                v = Double.parseDouble(params[0]);
            } catch (Exception e) {
                if (params[0].equals("∞")) v = Double.MAX_VALUE;
                else throw new xTalkException("A number is required here");
            }
            if (v - (int) v == 0) {
                result.theResult = Integer.toString((int) (v > 0 ? v : -v));
            } else {
                result.theResult = Double.toString(v > 0 ? v : -v);
            }
            result.ret = 0;
        } else if (0 == message.compareTo("min")) {
            if (params.length < 1) throw new xTalkException("Argument is required");
            double v;
            String[] vAry = params;
            if (params[0].contains(",")) vAry = params[0].split(",");
            try {
                v = Double.parseDouble(vAry[0]);
            } catch (Exception e) {
                if (params[0].equals("∞")) v = Double.MAX_VALUE;
                else throw new xTalkException("A number is required here");
            }
            for (int i = 1; i < vAry.length; i++) {
                double v2;
                try {
                    v2 = Double.parseDouble(vAry[i]);
                } catch (Exception e) {
                    if (params[0].equals("∞")) v2 = Double.MAX_VALUE;
                    else throw new xTalkException("A number is required here");
                }
                if (v2 < v) v = v2;
            }
            if (v - (int) v == 0) {
                result.theResult = Integer.toString((int) v);
            } else {
                result.theResult = Double.toString(v);
            }
            result.ret = 0;
        } else if (0 == message.compareTo("max")) {
            if (params.length < 1) throw new xTalkException("Argument is required");
            double v;
            String[] vAry = params;
            if (params[0].contains(",")) vAry = params[0].split(",");
            try {
                v = Double.parseDouble(vAry[0]);
            } catch (Exception e) {
                if (params[0].equals("∞")) v = Double.MAX_VALUE;
                else throw new xTalkException("A number is required here");
            }
            for (int i = 1; i < vAry.length; i++) {
                double v2;
                try {
                    v2 = Double.parseDouble(vAry[i]);
                } catch (Exception e) {
                    if (params[0].equals("∞")) v2 = Double.MAX_VALUE;
                    else throw new xTalkException("A number is required here");
                }
                if (v2 > v) v = v2;
            }
            if (v - (int) v == 0) {
                result.theResult = Integer.toString((int) v);
            } else {
                result.theResult = Double.toString(v);
            }
            result.ret = 0;
        } else if (0 == message.compareTo("average")) {
            if (params.length < 1) throw new xTalkException("Argument is required");
            double v;
            String[] vAry = params;
            if (params[0].contains(",")) vAry = params[0].split(",");
            try {
                v = Double.parseDouble(vAry[0]);
            } catch (Exception e) {
                throw new xTalkException("A number is required here");
            }
            for (int i = 1; i < vAry.length; i++) {
                double v2;
                try {
                    v2 = Double.parseDouble(vAry[i]);
                } catch (Exception e) {
                    throw new xTalkException("A number is required here");
                }
                v += v2;
            }
            v /= vAry.length;
            if (v - (int) v == 0) {
                result.theResult = Integer.toString((int) v);
            } else {
                result.theResult = Double.toString(v);
            }
            result.ret = 0;
        } else if (0 == message.compareTo("sum")) {
            if (params.length < 1) throw new xTalkException("Argument is required");
            double v;
            String[] vAry = params;
            if (params[0].contains(",")) vAry = params[0].split(",");
            try {
                v = Double.parseDouble(vAry[0]);
            } catch (Exception e) {
                if (params[0].equals("∞")) v = Double.MAX_VALUE;
                else throw new xTalkException("A number is required here");
            }
            for (int i = 1; i < vAry.length; i++) {
                double v2;
                try {
                    v2 = Double.parseDouble(vAry[i]);
                } catch (Exception e) {
                    if (params[0].equals("∞")) v2 = Double.MAX_VALUE;
                    else throw new xTalkException("A number is required here");
                }
                v += v2;
            }
            if (v - (int) v == 0) {
                result.theResult = Integer.toString((int) v);
            } else {
                result.theResult = Double.toString(v);
            }
            result.ret = 0;
        } else if (0 == message.compareTo("random")) {
            if (params.length < 1) throw new xTalkException("Argument is required");
            double v;
            try {
                v = Double.parseDouble(params[0]);
            } catch (Exception e) {
                if (params[0].equals("∞")) v = Double.MAX_VALUE;
                else throw new xTalkException("A number is required here");
            }
            result.theResult = Integer.toString((int) (v * Math.random() + 0.999999));
            result.ret = 0;
        } else if (0 == message.compareTo("sqrt")) {
            if (params.length < 1) throw new xTalkException("Argument is required");
            double v;
            try {
                v = Double.parseDouble(params[0]);
            } catch (Exception e) {
                if (params[0].equals("∞")) v = Double.MAX_VALUE;
                else throw new xTalkException("A number is required here");
            }
            result.theResult = Double.toString((Math.sqrt(v)));
            result.ret = 0;
        } else if (0 == message.compareTo("sin")) {
            if (params.length < 1) throw new xTalkException("Argument is required");
            double v;
            try {
                v = Double.parseDouble(params[0]);
            } catch (Exception e) {
                if (params[0].equals("∞")) v = Double.MAX_VALUE;
                else throw new xTalkException("A number is required here");
            }
            result.theResult = Double.toString((Math.sin(v)));
            result.ret = 0;
        } else if (0 == message.compareTo("cos")) {
            if (params.length < 1) throw new xTalkException("Argument is required");
            double v;
            try {
                v = Double.parseDouble(params[0]);
            } catch (Exception e) {
                if (params[0].equals("∞")) v = Double.MAX_VALUE;
                else throw new xTalkException("A number is required here");
            }
            result.theResult = Double.toString((Math.cos(v)));
            result.ret = 0;
        } else if (0 == message.compareToIgnoreCase("tan")) {
            if (params.length < 1) throw new xTalkException("Argument is required");
            double v;
            try {
                v = Double.parseDouble(params[0]);
            } catch (Exception e) {
                if (params[0].equals("∞")) v = Double.MAX_VALUE;
                else throw new xTalkException("A number is required here");
            }
            result.theResult = Double.toString((Math.tan(v)));
            result.ret = 0;
        } else if (0 == message.compareTo("atan")) {
            if (params.length < 1) throw new xTalkException("Argument is required");
            double v;
            try {
                v = Double.parseDouble(params[0]);
            } catch (Exception e) {
                if (params[0].equals("∞")) v = Double.MAX_VALUE;
                else throw new xTalkException("A number is required here");
            }
            result.theResult = Double.toString((Math.atan(v)));
            result.ret = 0;
        } else if (0 == message.compareTo("chartonum")) {
            if (params.length < 1) throw new xTalkException("Argument is required");
            char c = 0;
            if (!params[0].isEmpty()) c = params[0].charAt(0);
            result.theResult = Integer.toString(c);
            result.ret = 0;
        } else if (0 == message.compareTo("length")) {
            if (params.length < 1) throw new xTalkException("Argument is required");
            result.theResult = Integer.toString(params[0].length());
            result.ret = 0;
        } else if (0 == message.compareTo("value")) {
            if (params.length < 1) throw new xTalkException("Argument is required");
            result.theResult = TTalk.Evalution(params[0], memData, null, null);
            result.ret = 0;
        } else if (0 == message.compareTo("param")) {
            // param of i
            if (params.length < 1) throw new xTalkException("Argument is required");
            int i = Integer.parseInt(params[0]) - 1;
            if (i >= 0 && i < memData.params.length) {
                result.theResult = memData.params[i];
            }
            result.ret = 0;
        } else if (0 == message.compareTo("annuity")) {
            // Annual interest rate calculation (present value annuity factor)
            if (params.length < 2) throw new xTalkException("Two arguments are required");
            double rate = Double.parseDouble(params[0]);
            double periods = Double.parseDouble(params[1]);
            // double base = 0.0;
            // for(int i=0; i<(int)periods; i++){
            // base += 1.0;
            // base /= (1.0+rate);
            // }
            // base += (periods-(int)periods);
            // base /= 1.0+(rate*(periods-(int)periods));

            double base = (1 - 1 / Math.pow((1 + rate), periods)) / rate;
            result.theResult = Double.toString(base);
            result.ret = 0;
        } else if (0 == message.compareTo("compound")) {
            //
            if (params.length < 2) throw new xTalkException("Two arguments are required");
            double rate = Double.parseDouble(params[0]);
            double periods = Double.parseDouble(params[1]);
            double base = Math.pow((1 + rate), periods);
            result.theResult = Double.toString(base);
            result.ret = 0;
        } else if (0 == message.compareTo("exp")) {
            // Exponential
            if (params.length < 1) throw new xTalkException("Argument is required");
            double v = Double.parseDouble(params[0]);
            double base = Math.exp(v);
            result.theResult = Double.toString(base);
            result.ret = 0;
        } else if (0 == message.compareTo("exp1")) {
            // Exponential - 1
            if (params.length < 1) throw new xTalkException("Argument is required");
            double v = Double.parseDouble(params[0]);
            double base = Math.exp(v);
            result.theResult = Double.toString(base - 1.0);
            result.ret = 0;
        } else if (0 == message.compareTo("ln")) {
            // Natural logarithm
            if (params.length < 1) throw new xTalkException("Argument is required");
            double v = Double.parseDouble(params[0]);
            double base = Math.log(v);
            result.theResult = Double.toString(base);
            result.ret = 0;
        } else if (0 == message.compareTo("ln1")) {
            // Natural logarithm of number plus 1
            if (params.length < 1) throw new xTalkException("Argument is required");
            double v = Double.parseDouble(params[0]);
            double base = Math.log(1.0 + v);
            result.theResult = Double.toString(base);
            result.ret = 0;
        } else if (0 == message.compareTo("exp2")) {
            // 2 to the power of x
            if (params.length < 1) throw new xTalkException("Argument is required");
            double v = Double.parseDouble(params[0]);
            double base = Math.pow(2, v);
            result.theResult = Double.toString(base);
            result.ret = 0;
        } else if (0 == message.compareTo("log2")) {
            // 2 to the power of x
            if (params.length < 1) throw new xTalkException("Argument is required");
            double v = Double.parseDouble(params[0]);
            double base = Math.log(v) / Math.log(2);
            result.theResult = Double.toString(base);
            result.ret = 0;
        } else if (0 == message.compareTo("offset")) {
            if (params.length >= 2) {
                int off = params[1].indexOf(params[0]);
                if (off >= 0) result.theResult = Integer.toString(off + 1);
                else result.theResult = "0";
                result.ret = 0;
            }
        } else if (0 == message.compareTo("ticks")) {
            result.theResult = Integer.toString((int) new Date().getTime() / 10 * 3 / 5);
        } else if (0 == message.compareTo("seconds")) {
            result.theResult = Integer.toString((int) new Date().getTime() / 10 * 3 / 5 / 60);
        } else if (0 == message.compareTo("short version")) {
            result.theResult = PCARD.version;
        } else if (0 == message.compareTo("version")) {
            result.theResult = PCARD.version;
        } else if (0 == message.compareTo("environment")) {
            result.theResult = "development";
        } else if (0 == message.compareTo("systemname")) {
            result.theResult = System.getProperty("os.name");
        } else if (0 == message.compareTo("systemversion")) {
            String str = System.getProperty("os.version");
            while (str.indexOf(".") != str.lastIndexOf(".")) {
                str = str.substring(0, str.lastIndexOf(".")) + str.substring(str.lastIndexOf(".") + 1);
            }
            result.theResult = str;
        } else if (0 == message.compareTo("javaversion")) {
            result.theResult = System.getProperty("java.version");
        } else if (0 == message.compareTo("javavmversion")) {
            result.theResult = System.getProperty("java.vm.version");
        } else if (0 == message.compareTo("mouseclick")) {
            String v = GUI.mouseClicked ? "true" : "false";
            GUI.mouseClicked = false; // Returns to false once read
            result.theResult = v;
        } else if (0 == message.compareTo("mouse")) {
            result.theResult = GUI.mouseDown ? "Down" : "Up";
        } else if (0 == message.compareTo("mouseh")) {
            PointerInfo pointerInfo = MouseInfo.getPointerInfo();
            result.theResult = Integer.toString((pointerInfo.getLocation().x - PCARD.pc.mainPane.getX() - PCARD.pc.getLocationOnScreen().x) / PCARD.pc.zoom);
        } else if (0 == message.compareTo("mousev")) {
            PointerInfo pointerInfo = MouseInfo.getPointerInfo();
            result.theResult = Integer.toString((pointerInfo.getLocation().y - PCARD.pc.mainPane.getY() - PCARD.pc.getLocationOnScreen().y - PCARD.pc.getInsets().top) / PCARD.pc.zoom);
        } else if (0 == message.compareTo("mouseloc")) {
            // Mouse coordinates starting from top-left of card
            PointerInfo pointerInfo = MouseInfo.getPointerInfo();
            result.theResult = (pointerInfo.getLocation().x - PCARD.pc.mainPane.getX() - PCARD.pc.getLocationOnScreen().x) / PCARD.pc.zoom + "," +
                    (pointerInfo.getLocation().y - PCARD.pc.mainPane.getY() - PCARD.pc.getLocationOnScreen().y - PCARD.pc.getInsets().top) / PCARD.pc.zoom;
        } else if (0 == message.compareTo("clickh")) {
            result.theResult = Integer.toString(GUI.clickH);
        } else if (0 == message.compareTo("clickv")) {
            result.theResult = Integer.toString(GUI.clickV);
        } else if (0 == message.compareTo("clickloc")) {
            // Click coordinates starting from top-left of card
            result.theResult = GUI.clickH + "," + GUI.clickV;
        } else if (0 == message.compareTo("clickline")) {
            // Last clicked selectable line field and its line count
            result.theResult = GUI.clickLine;
        } else if (0 == message.compareTo("commandkey") || 0 == message.compareTo("cmdkey")) {
            if (GUI.key[14] >= 1) result.theResult = "Down";
            else result.theResult = "Up";
        } else if (0 == message.compareTo("optionkey")) {
            if (GUI.key[12] >= 1) result.theResult = "Down";
            else result.theResult = "Up";
        } else if (0 == message.compareTo("shiftkey")) {
            if (GUI.key[11] >= 1) result.theResult = "Down";
            else result.theResult = "Up";
        } else if (0 == message.compareTo("controlkey")) {
            if (GUI.key[13] >= 1) result.theResult = "Down";
            else result.theResult = "Up";
        } else if (0 == message.compareTo("heapspace")) {
            int freesize = (int) (Runtime.getRuntime().maxMemory() - (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()));
            result.theResult = Integer.toString(freesize);
        } else if (0 == message.compareTo("stackspace")) {
            // Stack size cannot be determined so fixed
            result.theResult = Integer.toString(128000);
        } else if (0 == message.compareTo("diskspace")) {
            try {
                result.theResult = Integer.toString((int) Files.getFileStore(PCARD.pc.stack.file).getUsableSpace());
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (0 == message.compareTo("screenrect")) {
            Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
            result.theResult = "0,0," + d.width + "," + d.height;
        } else if (0 == message.compareTo("windows")) {
            StringBuilder windowsStr = new StringBuilder();
            /*Frame[] frame = PCARD.getFrames();
            for(int i=0; i<frame.length; i++){
                if(GMsg.class == frame[i].getClass()){
                    if(!windowsStr.equals("")) windowsStr+="\n";
                    windowsStr += "Message";
                }
                if(!windowsStr.equals("")) windowsStr+="\n";
                windowsStr += frame[i].getName();
            }*/
            /*Window[] win = PCARD.getWindows();
            for(int i=0; i<win.length; i++){
                if(!windowsStr.equals("")) windowsStr+="\n";
                windowsStr += win[i].getName();
            }*/
            for (int i = 0; i < OWindow.list.size(); i++) {
                if (!windowsStr.isEmpty()) windowsStr.append("\n");
                windowsStr.append(OWindow.list.get(i).name);
            }
            result.theResult = windowsStr.toString();
        } else if (0 == message.compareTo("result")) {
            if (TTalk.lastResult != null && TTalk.lastResult.theResult != null) {
                result.theResult = TTalk.lastResult.theResult;
            } else result.theResult = "";
        } else if (0 == message.compareTo("itemdelimiter")) {
            result.theResult = TTalk.itemDelimiter;
        } else if (0 == message.compareTo("sound")) {
            String name = TTalk.tsound.name;
            if (name.isEmpty()) name = "done";
            result.theResult = name;
        } else if (0 == message.compareTo("long time")) {
            Calendar cal = Calendar.getInstance();
            int hrd = cal.get(Calendar.HOUR_OF_DAY);
            int hr = cal.get(Calendar.HOUR);
            String min = Integer.toString(cal.get(Calendar.MINUTE));
            if (min.length() == 1) min = "0" + min;
            String sec = Integer.toString(cal.get(Calendar.SECOND));
            if (sec.length() == 1) sec = "0" + sec;

            if (hrd >= 12) result.theResult = hr + ":" + min + ":" + sec + " PM";
            else result.theResult = hr + ":" + min + ":" + sec + " AM";
        } else if (0 == message.compareTo("time") || 0 == message.compareTo("short time")) {
            Calendar cal = Calendar.getInstance();
            int hrd = cal.get(Calendar.HOUR_OF_DAY);
            int hr = cal.get(Calendar.HOUR);
            String min = Integer.toString(cal.get(Calendar.MINUTE));
            if (min.length() == 1) min = "0" + min;

            if (hrd >= 12) result.theResult = hr + ":" + min + " PM";
            else result.theResult = hr + ":" + min + " AM";
        } else if (0 == message.compareTo("long date")) {
            Calendar cal = Calendar.getInstance();
            // Year
            int year = cal.get(Calendar.YEAR);

            // Month
            int month = cal.get(Calendar.MONTH) + 1;
            String monthStr = null;
            String[] month_ary = {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};
            if (Locale.getDefault().equals(Locale.JAPANESE)) {
                month_ary = null;
            } else {
                monthStr = month_ary[month];
            }

            // Day
            int date = cal.get(Calendar.DATE);

            // Day of week
            int dow = cal.get(Calendar.DAY_OF_WEEK);
            String[] youbi_ary = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
            if (Locale.getDefault().equals(Locale.JAPANESE)) {
                youbi_ary = new String[] {"日曜日", "月曜日", "火曜日", "水曜日", "木曜日", "金曜日", "土曜日"};
            }
            String youbiStr = youbi_ary[dow - 1];

            if (Locale.getDefault().equals(Locale.JAPANESE)) {
                result.theResult = year + "年 " + month + "月 " + date + "日 " + youbiStr;
            } else {
                result.theResult = youbiStr + ", " + monthStr + " " + date + ", " + year;
            }
        } else if (0 == message.compareTo("date") || 0 == message.compareTo("short date")) {
            Calendar cal = Calendar.getInstance();
            String year = Integer.toString(cal.get(Calendar.YEAR) % 100);
            if (year.length() == 1) year = "0" + year;
            String month = Integer.toString(cal.get(Calendar.MONTH) + 1);
            if (month.length() == 1) month = "0" + month;
            String date = Integer.toString(cal.get(Calendar.DATE));
            if (date.length() == 1) date = "0" + date;

            result.theResult = year + "." + month + date;
        } else if (0 == message.compareTo("menus")) {
            StringBuilder tmpStr = new StringBuilder();
            for (int i = 0; i < PCARD.pc.menu.mb.getComponentCount(); i++) {
                Component c = PCARD.pc.menu.mb.getComponent(i);
                if (c.getClass() == JMenu.class) {
                    JMenu menu = (JMenu) c;
                    if (!tmpStr.isEmpty()) tmpStr.append("\n");
                    tmpStr.append(menu.getText());
                }
            }
            result.theResult = tmpStr.toString();
        } else if (0 == message.compareTo("stacks")) {
            result.theResult = PCARD.pc.stack.path; // Only one can be started
        } else if (0 == message.compareTo("suspended")) {
            result.theResult = PCARD.pc.isFocused() ? "false" : "true";
        } else if (0 == message.compareTo("foundline")) {
            int offset = PCARD.pc.foundIndex;
            if (PCARD.pc.foundObject == null) {
                result.theResult = "";
            } else {
                String text = TTalk.Evalution(PCARD.pc.foundObject, memData, null, null);
                String[] lines = text.substring(offset).split("\n");
                result.theResult = "line " + (lines.length + 1) + " of " + PCARD.pc.foundObject;
            }
        } else if (0 == message.compareTo("foundtext")) {
            result.theResult = PCARD.pc.foundText;
        } else if (0 == message.compareTo("foundchunk")) {
            if (PCARD.pc.foundObject == null) {
                result.theResult = "";
            } else {
                result.theResult = "char " + PCARD.pc.foundIndex + " to " +
                        (PCARD.pc.foundIndex + PCARD.pc.foundText.length()) + " of " + PCARD.pc.foundObject;
            }
        } else if (0 == message.compareTo("foundfield")) {
            result.theResult = PCARD.pc.foundObject;
        } else if (0 == message.compareTo("paramcount")) {
            if (memData.params == null) result.theResult = "0";
            else result.theResult = Integer.toString(memData.params.length);
        } else if (0 == message.compareTo("params")) {
            StringBuilder str = new StringBuilder();
            if (memData.message != null) str.append(memData.message);
            if (memData.params != null) {
                for (int i = 0; i < memData.params.length; i++) {
                    if (i == 0) str.append(" \"").append(memData.params[i]).append("\"");
                    else str.append(",\"").append(memData.params[i]).append("\"");
                }
            }
            result.theResult = str.toString();
        } else if (0 == message.compareTo("target")) {
            if (target != null) result.theResult = target.getShortName();
            else result.theResult = "";
        } else if (0 == message.compareTo("tool")) {
            if (PCARD.pc.tool != null) {
                if (PCARD.pc.tool.getClass() == SelectTool.class) result.theResult = "select tool";
                else if (PCARD.pc.tool.getClass() == LassoTool.class) result.theResult = "lasso tool";
                else if (PCARD.pc.tool.getClass() == SmartSelectTool.class) result.theResult = "magicwand tool";
                else if (PCARD.pc.tool.getClass() == PencilTool.class) result.theResult = "pencil tool";
                else if (PCARD.pc.tool.getClass() == BrushTool.class) result.theResult = "brush tool";
                else if (PCARD.pc.tool.getClass() == PaintBucketTool.class) result.theResult = "bucket tool";
                else if (PCARD.pc.tool.getClass() == LineTool.class) result.theResult = "line tool";
                else if (PCARD.pc.tool.getClass() == RectTool.class) result.theResult = "rectangle tool";
                else if (PCARD.pc.tool.getClass() == OvalTool.class) result.theResult = "oval tool";
                else if (PCARD.pc.tool.getClass() == TypeTool.class) result.theResult = "text tool";
                else if (PCARD.pc.tool.getClass() == EraserTool.class) result.theResult = "eraser tool";
            } else if (AuthTool.tool != null) {
                if (AuthTool.tool.getClass() == ButtonTool.class) result.theResult = "button tool";
                else if (AuthTool.tool.getClass() == FieldTool.class) result.theResult = "field tool";
            } else result.theResult = "browse tool";
        } else if (0 == message.compareTo("selectedtext") ||
                0 == message.compareTo("selection")) {
            if (GUI.clickField != null) {
                result.theResult = GUI.clickField.getSelectedText();
            }
            // Search for field with input focus and return selected text
            // cd
            for (int i = 0; i < PCARD.pc.stack.curCard.fldList.size(); i++) {
                OField field = PCARD.pc.stack.curCard.fldList.get(i);
                JComponent compo = field.getComponent();
                if (compo != null && compo.isFocusOwner()) {
                    result.theResult = field.getSelectedText();
                }
            }
            // bg
            for (int i = 0; i < PCARD.pc.stack.curCard.bg.fldList.size(); i++) {
                OField field = PCARD.pc.stack.curCard.bg.fldList.get(i);
                JComponent compo = field.getComponent();
                if (compo != null && compo.isFocusOwner()) {
                    result.theResult = field.getSelectedText();
                }
            }
            if (result.theResult == null) {
                result.theResult = "";
            }
        } else if (0 == message.compareTo("numtochar")) {
            int i = Integer.parseInt(params[0]);
            if (i <= 0 || i > 65535) result.theResult = "";
            else {
                char c = (char) i;
                result.theResult = Character.toString(c);
            }
        }
        //
        // Properties are also treated like functions. These can be set.
        //
        else if (0 == message.compareTo("userlevel")) {
            result.theResult = Integer.toString(PCARD.userLevel);
            result.ret = 0;
        } else if (0 == message.compareTo("scripttextfont")) {
            result.theResult = PCARD.scriptFont;
            result.ret = 0;
        } else if (0 == message.compareTo("scripttextsize")) {
            result.theResult = Integer.toString(PCARD.scriptFontSize);
            result.ret = 0;
        } else if (0 == message.compareTo("systemlang")) {
            result.theResult = Locale.getDefault().getLanguage();
            result.ret = 0;
        } else if (0 == message.compareTo("dragspeed")) {
            result.theResult = Integer.toString(TTalk.dragspeed);
            result.ret = 0;
        } else if (0 == message.compareToIgnoreCase("checkHCFont")) {
            // Home stack function
            result.theResult = ""; // Anyway return empty
            result.ret = 0;
        } else {
            System.out.println("Undefined function/property: " + message);
        }

        return result;
    }


    // System messages and XCMD, XFCN
    public static Result CallSystemMessage(String message, String[] params, OObject target, MemoryData memData, boolean isFunc)
            throws xTalkException {
        Result result = new Result();
        result.ret = 1; // Return 1 if not found

        if (0 == message.compareToIgnoreCase("mouseDown")) {
            result.ret = 0;
        } else if (0 == message.compareToIgnoreCase("mouseUp")) {
            result.ret = 0;
        } else if (0 == message.compareToIgnoreCase("mouseLeave")) {
            result.ret = 0;
        } else if (0 == message.compareToIgnoreCase("mouseEnter")) {
            result.ret = 0;
        } else if (0 == message.compareToIgnoreCase("mouseWithin")) {
            result.ret = 0;
        } else if (0 == message.compareToIgnoreCase("mouseStillDown")) {
            result.ret = 0;
        } else if (0 == message.compareToIgnoreCase("mouseDoubleClick")) {
            result.ret = 0;
        } else if (0 == message.compareToIgnoreCase("startUp")) {
            result.ret = 0;
        } else if (0 == message.compareToIgnoreCase("openStack")) {
            result.ret = 0;
        } else if (0 == message.compareToIgnoreCase("closeStack")) {
            result.ret = 0;
        } else if (0 == message.compareToIgnoreCase("openCard")) {
            result.ret = 0;
        } else if (0 == message.compareToIgnoreCase("closeCard")) {
            result.ret = 0;
        } else if (0 == message.compareToIgnoreCase("openBackground")) {
            result.ret = 0;
        } else if (0 == message.compareToIgnoreCase("closeBackground")) {
            result.ret = 0;
        } else if (0 == message.compareToIgnoreCase("idle")) {
            result.ret = 0;
        } else if (0 == message.compareToIgnoreCase("keydown")) {
            result.ret = 0;
        } else if (0 == message.compareToIgnoreCase("commandkeydown")) {
            result.ret = 0;
        } else if (0 == message.compareTo("arrowKey")) {
            if (params[0].equals("left")) TTalk.doScriptforMenu("go prev");
            if (params[0].equals("right")) TTalk.doScriptforMenu("go next");
            result.ret = 0;
        } else if (0 == message.compareToIgnoreCase("picture")) {
            // Home stack XCMD
            String typeStr = "resource";
            if (params.length >= 2 && !params[1].isEmpty()) {
                typeStr = params[1];
            }
            String windowType = "rect";
            if (params.length >= 3 && !params[2].isEmpty()) {
                windowType = params[2];
            }
            boolean visible = true;
            if (params.length >= 4 && !params[3].isEmpty()) {
                visible = params[3].equalsIgnoreCase("true");
            }
            BufferedImage bi = null;
            if (typeStr.equalsIgnoreCase("resource")) {
                // Search for resource
                int rsrcid = PCARD.pc.stack.rsrc.getRsrcIdAll(params[0], "picture");
                bi = PCARD.pc.stack.rsrc.getImage(rsrcid, "picture");
            } else if (typeStr.equalsIgnoreCase("file")) {
                // Search for file
                try {
                    Path file = PCARD.pc.stack.file.getParent();
                    Path path = file.getParent().resolve(params[0]);
                    bi = ImageIO.read(path.toFile());
                    if (bi == null) {
                        bi = ImageIO.read(new File(params[0]));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (bi != null) {
                GPictWindow gpw = new GPictWindow(PCARD.pc, params[0], bi, windowType, visible);
                new OWindow(gpw);
            }
            result.ret = 0;
        } else if (0 == message.compareToIgnoreCase("palette")) {
            // Home stack XCMD
            // Search for PLTE resource
            for (int i = 0; i < PCARD.pc.stack.rsrc.plteList.size(); i++) {
                Rsrc.PlteClass plte = PCARD.pc.stack.rsrc.plteList.get(i);
                if (plte.name.equalsIgnoreCase(params[0])) {
                    BufferedImage bi = PCARD.pc.stack.rsrc.getImage(plte.pictId, "picture");
                    if (bi == null) {
                        bi = PCARD.pc.stack.rsrc.getImage(plte.id, "picture");
                    }
                    if (bi != null) {
                        // Offset
                        BufferedImage nbi = new BufferedImage(bi.getWidth() - plte.pictHV.x, bi.getHeight() - plte.pictHV.y, BufferedImage.TYPE_INT_ARGB);
                        nbi.getGraphics().drawImage(bi, -plte.pictHV.x, -plte.pictHV.y, PCARD.pc);
                        // Create window
                        GPictWindow gpw = new GPictWindow(PCARD.pc, params[0], nbi, "document", true);
                        new OWindow(gpw);
                        gpw.setResizable(false);
                        gpw.setLayout(null);
                        // Create button
                        for (int j = 0; j < plte.objList.size(); j++) {
                            PlteBtnObject obj = plte.objList.get(j);
                            AbstractButton btn;
                            if (plte.clearHilite) {
                                btn = new JButton();
                            } else {
                                btn = new JToggleButton();
                            }
                            btn.setBounds(obj.rect.x - plte.pictHV.x,
                                    obj.rect.y - plte.pictHV.y,
                                    obj.rect.width, obj.rect.height);
                            btn.setName(obj.message);
                            btn.setOpaque(true);
                            btn.setBorder(new EmptyBorder(0, 0, 0, 0));
                            BufferedImage iconbi = new BufferedImage(btn.getWidth(), btn.getHeight(), BufferedImage.TYPE_INT_ARGB);
                            iconbi.getGraphics().drawImage(nbi, -btn.getX(), -btn.getY(), PCARD.pc);
                            btn.setIcon(new ImageIcon(iconbi));
                            btn.addActionListener(sendMsgListener);
                            gpw.getContentPane().add(btn, 0);
                        }
                    } else {
                        result.theResult = "Error: PICT not found.";
                    }
                    break;
                }
            }
            result.ret = 0;
        } else {
            if (!isFunc && PCARD.pc.stack.rsrc.getxcmdId(message, "command") > 0 ||
                    isFunc && PCARD.pc.stack.rsrc.getxcmdId(message, "function") > 0) {
                return TXcmd.CallExternalCommand(message, params, target, memData, isFunc, result);
            } else {
                result = CallSystemFunction(message.toLowerCase(), params, target, memData, isFunc);
                if (result.theResult == null || result.theResult.isEmpty()) {
                    System.out.println("Undefined function: " + message);
                    throw new xTalkException(message + " is unknown");
                }
            }
        }

        return result;
    }


    static SendMsgListener sendMsgListener = new SendMsgListener();

    static class SendMsgListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            String msg = ((JComponent) e.getSource()).getName();
            try {
                TTalk.doScriptforMenu(msg);
            } catch (xTalkException e1) {
            }
        }
    }

    // Even rounding
    static int IEEE_Round(double v) {
        int i;
        i = (int) v;
        v -= i;
        if (v > 0.5) {
            i++;
        } else if (v == 0.5) {
            if (i % 2 == 1) i++;
        }
        return i;
    }

    public static String getProperty(
            String inProperty, OObject obj, OObject target, MemoryData memData) throws xTalkException {
        String prpty = inProperty.toLowerCase();
        OButton btn = null;
        OField fld = null;
        OCard cd = null;
        OBackground bg = null;
        // OStack stk=null;

        /*if(obj==null) {
            return CallSystemFunction(prpty, null, target, memData, true).theResult;
        }*/

        if (0 == obj.objectType.compareTo("button"))
            btn = (OButton) obj;
        if (0 == obj.objectType.compareTo("field"))
            fld = (OField) obj;
        if (0 == obj.objectType.compareTo("card"))
            cd = (OCard) obj;
        if (0 == obj.objectType.compareTo("background"))
            bg = (OBackground) obj;
        /*if(0==obj.objectType.compareTo("stack"))
            stk=(OStack)obj;*/

        if (0 == prpty.compareTo("icon")) {
            if (btn != null) {
                if (btn.iconURI != null) return btn.iconURI.toString();
                else return Integer.toString(btn.icon);
            } else return "";
        } else if (0 == prpty.compareTo("hilite") || 0 == prpty.compareTo("highlight") || 0 == prpty.compareTo("highlite") || 0 == prpty.compareTo("hilight")) {
            if (btn != null) {
                if (btn.btn != null) {
                    return (btn.btn.hilite) ? "true" : "false";
                } else {
                    return (btn.check_hilite) ? "true" : "false";
                }
            } else return "";
        } else if (0 == prpty.compareTo("enabled")) {
            if (btn != null) {
                return (btn.enabled) ? "true" : "false";
            } else if (fld != null) {
                return (fld.enabled) ? "true" : "false";
            } else return "";
        } else if (0 == prpty.compareTo("selectedtext")) {
            if (fld != null) return fld.getSelectedText();
            if (btn != null) return btn.getSelectedText();
            else return "";
        } else if (0 == prpty.compareTo("selectedline")) {
            if (fld != null) return "line " + fld.getSelectedLine() + " of " + obj.getShortName();
            if (btn != null) return "line " + btn.getSelectedLine() + " of " + obj.getShortName();
            else return "";
        } else if (0 == prpty.compareTo("selectedfield")) {
            // Search for field with input focus
            // cd
            for (int i = 0; i < PCARD.pc.stack.curCard.fldList.size(); i++) {
                OField field = PCARD.pc.stack.curCard.fldList.get(i);
                JComponent compo = field.getComponent();
                if (compo != null && compo.isFocusOwner()) {
                    return field.getShortName();
                }
            }
            // bg
            for (int i = 0; i < PCARD.pc.stack.curCard.bg.fldList.size(); i++) {
                OField field = PCARD.pc.stack.curCard.bg.fldList.get(i);
                JComponent compo = field.getComponent();
                if (compo != null && compo.isFocusOwner()) {
                    return field.getShortName();
                }
            }
            return "";
        }
        /*else if(0==prpty.compareTo("selectedbutton")){
            // Button selected within family
            return "";
        }*/
        else if (0 == prpty.compareTo("autohilite")) {
            if (btn != null) return (btn.btn.autoHilite) ? "true" : "false";
            else return "";
        } else if (0 == prpty.compareTo("left")) {
            return Integer.toString(obj.left);
        } else if (0 == prpty.compareTo("top")) {
            return Integer.toString(obj.top);
        } else if (0 == prpty.compareTo("right")) {
            return Integer.toString(obj.left + obj.width);
        } else if (0 == prpty.compareTo("bottom")) {
            return Integer.toString(obj.top + obj.height);
        } else if (0 == prpty.compareTo("topleft")) {
            return obj.left + "," + obj.top;
        } else if (0 == prpty.compareTo("loc") || 0 == prpty.compareTo("location")) {
            return obj.left + obj.width / 2 + "," + (obj.top + obj.height / 2);
        } else if (0 == prpty.compareTo("rect") || 0 == prpty.compareTo("rectangle")) {
            return obj.left + "," + obj.top + "," + (obj.left + obj.width) + "," + (obj.top + obj.height);
        } else if (0 == prpty.compareTo("width")) {
            return Integer.toString(obj.width);
        } else if (0 == prpty.compareTo("height")) {
            return Integer.toString(obj.height);
        } else if (0 == prpty.compareTo("visible")) {
            return (obj.getVisible() == true) ? "true" : "false";
        } else if (0 == prpty.compareTo("locktext")) {
            if (obj.objectType.equals("field")) {
                return (obj.enabled) ? "true" : "false";
            } else throw new xTalkException("lockText can only be referenced by fields");
        } else if (0 == prpty.compareTo("showpict")) {
            if (obj.objectType.equals("card") || obj.objectType.equals("background")) {
                return (((OCardBase) obj).showPict == true) ? "true" : "false";
            } else throw new xTalkException("showPict can only be referenced by cards and backgrounds");
        } else if (0 == prpty.compareTo("short name")) {
            if (!obj.name.isEmpty()) return obj.name;
            return obj.getShortName();
        } else if (0 == prpty.compareTo("name")) {
            return obj.getShortName();
        } else if (0 == prpty.compareTo("long name")) {
            return obj.getLongName();
        } else if (0 == prpty.compareTo("id")) {
            return Integer.toString(obj.id);
        } else if (0 == prpty.compareTo("long id")) {
            String sName = "";
            if (!obj.objectType.equals("card") && obj.parent != null) {
                if (obj.parent.objectType.equals("card") || obj.parent.objectType.equals("background")) {
                    sName += obj.parent.CapitalType() + " ";
                }
            }
            sName += obj.CapitalType() + " id " + obj.id;
            return sName;
        } else if (0 == prpty.compareTo("short id")) {
            return Integer.toString(obj.id);
        } else if (0 == prpty.compareTo("number")) {
            if (fld != null) return Integer.toString(((OCardBase) fld.parent).GetNumberof(fld));
            if (btn != null) return Integer.toString(((OCardBase) btn.parent).GetNumberof(btn));
            if (cd != null) return Integer.toString(cd.stack.GetNumberof(cd));
            if (bg != null) return Integer.toString(bg.stack.GetNumberof(bg));
            else {
                System.out.println("Undefined object property: " + prpty);
                return "";
            }
        } else if (0 == prpty.compareTo("script")) {
            StringBuilder str = new StringBuilder();
            for (int i = 0; i < obj.scriptList.size(); i++) {
                str.append(obj.scriptList.get(i));
                if (i + 1 < obj.scriptList.size()) str.append("\n");
            }
            return str.toString();
        } else if (0 == prpty.compareTo("freesize")) {
            if (obj.objectType.equals("stack")) {
                return "0";
            } else System.out.println("Undefined object property: " + prpty);
        } else if (0 == prpty.compareTo("style")) {
            if (fld != null) {
                return switch (fld.style) {
                    case 0 -> "plain";
                    case 1 -> "transparent";
                    case 2 -> "opaque";
                    case 3 -> "rectangle";
                    case 4 -> "shadow";
                    case 5 -> "scroll";
                    default -> "???";
                };
            } else if (btn != null) {
                return switch (btn.style) {
                    case 0 -> "standard";
                    case 1 -> "transparent";
                    case 2 -> "opaque";
                    case 3 -> "rectangle";
                    case 4 -> "shadow";
                    case 5 -> "roundRect";
                    case 6 -> "default";
                    case 7 -> "oval";
                    case 8 -> "popup";
                    case 9 -> "checkbox";
                    case 10 -> "radio";
                    default -> "???";
                };
            } else System.out.println("This object does not have property " + prpty);
        } else if (0 == prpty.compareTo("textstyle")) {
            String styleStr = "";
            if (fld != null) {
                if ((fld.textStyle & 1) > 0) styleStr += "bold,";
                if ((fld.textStyle & 2) > 0) styleStr += "italic,";
                if ((fld.textStyle & 4) > 0) styleStr += "underline,";
                if ((fld.textStyle & 8) > 0) styleStr += "outline,";
                if ((fld.textStyle & 16) > 0) styleStr += "shadow,";
                if ((fld.textStyle & 32) > 0) styleStr += "condensed,";
                if ((fld.textStyle & 64) > 0) styleStr += "extend,";
                if ((fld.textStyle & 128) > 0) styleStr += "group,";
                if (styleStr.isEmpty()) styleStr = "plain";
                else styleStr = styleStr.substring(0, styleStr.length() - 1);
                return styleStr;
            } else if (btn != null) {
                if ((btn.textStyle & 1) > 0) styleStr += "bold,";
                if ((btn.textStyle & 2) > 0) styleStr += "italic,";
                if ((btn.textStyle & 4) > 0) styleStr += "underline,";
                if ((btn.textStyle & 8) > 0) styleStr += "outline,";
                if ((btn.textStyle & 16) > 0) styleStr += "shadow,";
                if ((btn.textStyle & 32) > 0) styleStr += "condensed,";
                if ((btn.textStyle & 64) > 0) styleStr += "extend,";
                if ((btn.textStyle & 128) > 0) styleStr += "group,";
                if (styleStr.isEmpty()) styleStr = "plain";
                else styleStr = styleStr.substring(0, styleStr.length() - 1);
                return styleStr;
            } else System.out.println("This object does not have property " + prpty);
        } else if (0 == prpty.compareTo("cantmodify")) {
            if (obj.objectType.equals("stack")) {
                return (((OStack) obj).cantModify == true) ? "true" : "false";
            } else System.out.println("This object does not have property " + prpty);
        } else if (0 == prpty.compareTo("cantabort")) {
            if (obj.objectType.equals("stack")) {
                return (((OStack) obj).cantAbort == true) ? "true" : "false";
            } else System.out.println("This object does not have property " + prpty);
        } else if (0 == prpty.compareTo("blendingmode")) {
            if (obj.objectType.equals("button")) {
                if (((OButton) obj).blendMode == 0) return "copy";
                else if (((OButton) obj).blendMode == 1) return "blend";
                else return "";
            } else System.out.println("This object does not have property " + prpty);
        } else if (0 == prpty.compareTo("blendinglevel")) {
            if (obj.objectType.equals("button")) {
                return Integer.toString(((OButton) obj).blendLevel);
            } else System.out.println("This object does not have property " + prpty);
        } else if (0 == prpty.compareTo("color")) {
            if (obj.objectType.equals("button")) {
                Color col = ((OButton) obj).color;
                return col.getRed() * 256 + "," + col.getGreen() * 256 + "," + col.getBlue() * 256;
            }
            if (obj.objectType.equals("field")) {
                Color col = ((OField) obj).color;
                return col.getRed() * 256 + "," + col.getGreen() * 256 + "," + col.getBlue() * 256;
            } else System.out.println("This object does not have property " + prpty);
        } else if (0 == prpty.compareTo("backcolor")) {
            if (obj.objectType.equals("button")) {
                Color col = ((OButton) obj).bgColor;
                return col.getRed() * 256 + "," + col.getGreen() * 256 + "," + col.getBlue() * 256;
            }
            if (obj.objectType.equals("field")) {
                Color col = ((OField) obj).bgColor;
                return col.getRed() * 256 + "," + col.getGreen() * 256 + "," + col.getBlue() * 256;
            } else System.out.println("This object does not have property " + prpty);
        } else if (0 == prpty.compareTo("size")) {
            File parent = PCARD.pc.stack.file.getParent().toFile();
            int size = 0;
            File[] files = parent.listFiles();
            if (files != null) {
                for (File file : files) {
                    size = (int) (size + file.length());
                }
            }
            return Integer.toString(size);
        } else {
            System.out.println("Undefined object property: " + prpty);
        }
        return "";
    }

    public static void SetProperty(
            OObject obj, String inProperty, String value)
            throws xTalkException {
        String prpty = inProperty.toLowerCase();
        OButton btn = null;
        OField fld = null;
        OCard cd = null;
        /*OBackground bg=null;
        OStack stk=null;*/

        if (obj == null) {
            // HyperCard properties
            if (0 == prpty.compareTo("scripttextfont")) {
                PCARD.scriptFont = value;
            } else if (0 == prpty.compareTo("scripttextsize")) {
                try {
                    PCARD.scriptFontSize = Integer.parseInt(value);
                } catch (Exception err) {
                    throw new xTalkException("A number is required here");
                }
            } else if (0 == prpty.compareTo("textfont")) {
                PCARD.pc.textFont = value;
            } else if (0 == prpty.compareTo("textstyle")) {
                int style = 0;
                value = value.toLowerCase();
                if (value.contains("plain")) {
                    style = 0;
                } else {
                    if (value.contains("bold")) style |= 1;
                    if (value.contains("italic")) style |= 2;
                    if (value.contains("underline")) style |= 4;
                    if (value.contains("outline")) style |= 8;
                    if (value.contains("shadow")) style |= 16;
                    if (value.contains("condensed")) style |= 32;
                    if (value.contains("extend")) style |= 64;
                    if (value.contains("group")) style |= 128;
                }
                PCARD.pc.textStyle = style;
            } else if (0 == prpty.compareTo("textsize")) {
                try {
                    PCARD.pc.textSize = Integer.parseInt(value);
                } catch (Exception err) {
                    throw new xTalkException("A number is required here");
                }
            } else if (0 == prpty.compareTo("itemdelimiter")) {
                if (value.length() == 1) {
                    TTalk.itemDelimiter = value;
                } else if (value.isEmpty()) {
                    TTalk.itemDelimiter = ",";
                } else {
                    throw new xTalkException("Set one character for itemDelimiter");
                }
            } else if (0 == prpty.compareTo("userlevel")) {
                try {
                    PCARD.userLevel = Integer.parseInt(value);
                } catch (Exception err) {
                    throw new xTalkException("Specify a number from 1 to 5 for userLevel");
                }
            } else if (0 == prpty.compareTo("cursor")) {
                Cursor cr = null;
                        /*new Cursor(Cursor.CROSSHAIR_CURSOR) ,
                        new Cursor(Cursor.DEFAULT_CURSOR) ,
                        new Cursor(Cursor.HAND_CURSOR) ,
                        new Cursor(Cursor.MOVE_CURSOR) ,
                        new Cursor(Cursor.WAIT_CURSOR) ,
                        new Cursor(Cursor.TEXT_CURSOR) ,
                        new Cursor(Cursor.SW_RESIZE_CURSOR) ,
                        new Cursor(Cursor.SE_RESIZE_CURSOR) ,
                        new Cursor(Cursor.NW_RESIZE_CURSOR) ,
                        new Cursor(Cursor.NE_RESIZE_CURSOR) ,
                        new Cursor(Cursor.N_RESIZE_CURSOR) ,
                        new Cursor(Cursor.S_RESIZE_CURSOR) ,
                        new Cursor(Cursor.W_RESIZE_CURSOR) ,
                        new Cursor(Cursor.E_RESIZE_CURSOR) ,*/
                if (value.equalsIgnoreCase("hand") || value.equalsIgnoreCase("browse")) {
                    cr = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
                } else if (value.equalsIgnoreCase("watch")) {
                    cr = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR);
                } else if (value.equalsIgnoreCase("busy")) {
                    cr = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR);
                } else if (value.equalsIgnoreCase("arrow")) {
                    cr = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
                } else if (value.equalsIgnoreCase("cross")) {
                    cr = Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR);
                } else if (value.equalsIgnoreCase("ibeam")) {
                    cr = Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR);
                } else if (value.equalsIgnoreCase("none")) {
                    BufferedImage bi = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
                    Point hotSpot = new Point(0, 0);
                    String name = "none-cursor";
                    Toolkit kit = Toolkit.getDefaultToolkit();
                    cr = kit.createCustomCursor(bi, hotSpot, name);
                } else {
                    int rsrcid = PCARD.pc.stack.rsrc.getRsrcIdAll(value, "cursor");
                    if (rsrcid == 0 && value.matches("[0-9]*")) {
                        rsrcid = Integer.parseInt(value);
                    }
                    String fname = PCARD.pc.stack.rsrc.getFilePathAll(rsrcid, "cursor");
                    if (fname != null && !fname.isEmpty()) {
                        try {
                            BufferedImage bi = ImageIO.read(new File(fname/*PCARD.pc.stack.file.getParent()+File.separatorChar+fname*/));
                            Point hotSpot = PCARD.pc.stack.rsrc.getHotSpotAll(rsrcid);
                            String name = "none-cursor";
                            Toolkit kit = Toolkit.getDefaultToolkit();
                            cr = kit.createCustomCursor(bi, hotSpot, name);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        throw new xTalkException("Cursor " + value + " does not exist");
                    }
                }
                if (cr != null)
                    PCARD.pc.setCursor(cr);
            } else if (0 == prpty.compareTo("lockscreen")) {
                if (value.equalsIgnoreCase("true")) {
                    TTalk.doScriptforMenu("lock screen");
                } else if (value.equalsIgnoreCase("false")) {
                    TTalk.doScriptforMenu("unlock screen");
                }
            } else if (0 == prpty.compareTo("lockrecent")) {
                if (value.equalsIgnoreCase("true")) {

                } else if (value.equalsIgnoreCase("false")) {

                }
            } else if (0 == prpty.compareTo("lockmessages")) {
                if (value.equalsIgnoreCase("true")) {
                    TTalk.doScriptforMenu("lock messages");
                } else if (value.equalsIgnoreCase("false")) {
                    TTalk.doScriptforMenu("unlock messages");
                }
            } else if (0 == prpty.compareTo("lockerrordialogs")) {
                if (value.equalsIgnoreCase("true")) {
                    TTalk.lockErrorDialogs = true;
                } else if (value.equalsIgnoreCase("false")) {
                    TTalk.lockErrorDialogs = false;
                } else throw new xTalkException("A boolean value is required here");
            } else if (0 == prpty.compareTo("dragspeed")) {
                TTalk.dragspeed = Integer.parseInt(value);
            } else if (0 == prpty.compareTo("editbkgnd")) {
                if (value.equalsIgnoreCase("true")) {
                    PaintTool.editBackground = true;
                } else if (value.equalsIgnoreCase("false")) {
                    PaintTool.editBackground = false;
                } else throw new xTalkException("A boolean value is required here");
            } else throw new xTalkException("Property " + prpty + " is unknown");
            return;
        }

        if (0 == obj.objectType.compareTo("button"))
            btn = (OButton) obj;
        if (0 == obj.objectType.compareTo("field"))
            fld = (OField) obj;
        if (0 == obj.objectType.compareTo("card"))
            cd = (OCard) obj;
        /*if(0==obj.objectType.compareTo("background"))
            bg=(OBackground)obj;
        if(0==obj.objectType.compareTo("stack"))
            stk=(OStack)obj;*/

        if (0 == prpty.compareTo("icon")) {
            if (btn != null) {
                try {
                    btn.setIcon(Integer.parseInt(value));
                } catch (Exception err) {
                    if (PCARD.pc.stack.rsrc.getRsrcIdAll(value, "icon") != 0) {
                        btn.setIcon(PCARD.pc.stack.rsrc.getRsrcIdAll(value, "icon"));
                    } else {
                        try {
                            btn.setIconURI(new URI(value));
                        } catch (URISyntaxException e) {
                            btn.setIcon(0);
                        }
                    }
                }
            } else throw new xTalkException("Cannot set icon for non-buttons");
        } else if (0 == prpty.compareTo("visible")) {
            if (btn != null) {
                btn.setVisible(0 == value.compareToIgnoreCase("true"));
            } else if (fld != null) {
                fld.setVisible(0 == value.compareToIgnoreCase("true"));
            } else if (0 == obj.objectType.compareTo("titlebar")) {
                // PCARD.pc.setVisible(false);
                // PCARD.pc.setUndecorated(0!=value.compareToIgnoreCase("true"));
                // PCARD.pc.setVisible(true);
            } else if (0 == obj.objectType.compareTo("menubar")) {
                /*GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
                GraphicsDevice device = ge.getDefaultScreenDevice();  // Get GraphicsDevice

                if (device.isFullScreenSupported()) {
                    // Fullscreen!
                    device.setFullScreenWindow(PCARD.pc);
                }*/

                // Cannot hide menu with this, so create a window without menu and move there? Meaningless on Mac.
                /*if(0==value.compareToIgnoreCase("true")){
                    PCARD.pc.setJMenuBar(PCARD.pc.menu.mb);
                }else{
                    PCARD.pc.setJMenuBar(new JMenuBar());
                }*/
            } else if (0 == obj.objectType.compareTo("msg")) {
                boolean b = 0 == value.compareToIgnoreCase("true");
                GMsg.msg.setVisible(b);
                if (b) {
                    GMsg.msg.requestFocus();
                }
            } else if (0 == obj.objectType.compareTo("tool window")) {
                if (PCARD.pc.stack != null && PCARD.pc.toolbar != null) {
                    PCARD.pc.toolbar.tb.setBounds(Math.max(0, PCARD.pc.getX() - 78 - 2), PCARD.pc.getY() + 24, 78, 210);
                    PCARD.pc.toolbar.tb.setVisible(0 == value.compareToIgnoreCase("true"));
                }
            } else if (0 == obj.objectType.compareTo("picture")) {
                obj.setVisible(0 == value.compareToIgnoreCase("true"));
            } else if (0 == obj.objectType.compareTo("window")) {
                if (((OWindow) obj).frame != null) {
                    ((OWindow) obj).frame.setVisible(0 == value.compareToIgnoreCase("true"));
                } else if (((OWindow) obj).dlog != null) {
                    ((OWindow) obj).dlog.setVisible(0 == value.compareToIgnoreCase("true"));
                    if (((OWindow) obj).dlog == MessageWatcher.watcherWindow) {
                        MessageWatcher.watcherWindow.setTable(TTalk.talk.messageRingArray, TTalk.talk.objectRingArray, TTalk.talk.ringCnt);
                    }
                } else if (((OWindow) obj).gpw != null) {
                    ((OWindow) obj).gpw.setVisible(0 == value.compareToIgnoreCase("true"));
                } else {
                    throw new xTalkException("Cannot set visible for this object??");
                }
            } else throw new xTalkException("Cannot set visible for this object");
        } else if (0 == prpty.compareTo("enabled")) {
            if (btn != null) {
                btn.setEnabled(0 == value.compareToIgnoreCase("true"));
            } else if (fld != null) {
                fld.setEnabled(0 == value.compareToIgnoreCase("true"));
            } else if (obj.objectType.equals("menu")) {
                GMenu.changeMEnabled(value, 0 == value.compareToIgnoreCase("true"));
            } else throw new xTalkException("Cannot set enabled for this object");
        } else if (0 == prpty.compareTo("hilite") || 0 == prpty.compareTo("highlight") || 0 == prpty.compareTo("highlite") || 0 == prpty.compareTo("hilight")) {
            if (btn != null) {
                btn.setHilite(0 == value.compareToIgnoreCase("true"));
            } else if (fld != null) {
                fld.setHilite(0 == value.compareToIgnoreCase("true"));
            } else throw new xTalkException("Cannot set hilite for this object");
        } else if (0 == prpty.compareTo("text")) {
            if (btn != null) btn.setText(value);
            else if (fld != null) fld.setText(value);
            else if (obj.objectType.equals("menu")) {
                OMenu om = (OMenu) obj;
                om.menu.add(new JMenuItem(value));
            } else throw new xTalkException("Cannot store text for non-buttons and fields");
        } else if (0 == prpty.compareTo("name")) {
            if (btn != null) btn.setName(value);
            else if (fld != null) fld.setName(value);
            else obj.name = value;
        } else if (0 == prpty.compareTo("left")) {
            int v;
            try {
                double d = Double.parseDouble(value);
                v = (int) d;
            } catch (Exception err) {
                throw new xTalkException("A number is required here");
            }
            if (btn != null) btn.setTopLeft(v, btn.getTop());
            else if (fld != null) fld.setTopLeft(v, fld.getTop());
            else if (cd != null) PCARD.pc.stack.setTopLeft(v, PCARD.pc.stack.getTop());
            else if (0 == obj.objectType.compareTo("window")) {
                if (((OWindow) obj).frame != null) {
                    ((OWindow) obj).frame.setBounds(PCARD.pc.getX() + v, ((OWindow) obj).frame.getY(), ((OWindow) obj).frame.getWidth(), ((OWindow) obj).frame.getHeight());
                } else if (((OWindow) obj).gpw != null) {
                    ((OWindow) obj).gpw.setBounds(PCARD.pc.getX() + v, ((OWindow) obj).gpw.getY(), ((OWindow) obj).gpw.getWidth(), ((OWindow) obj).gpw.getHeight());
                } else {
                    throw new xTalkException("Cannot set left for this object??");
                }
            } else throw new xTalkException("Cannot set left for this object");
        } else if (0 == prpty.compareTo("top")) {
            int v;
            try {
                double d = Double.parseDouble(value);
                v = (int) d;
            } catch (Exception err) {
                throw new xTalkException("A number is required here");
            }
            if (btn != null) btn.setTopLeft(btn.getLeft(), v);
            else if (fld != null) fld.setTopLeft(fld.getLeft(), v);
            else if (cd != null) PCARD.pc.stack.setTopLeft(PCARD.pc.stack.getLeft(), v);
            else if (0 == obj.objectType.compareTo("window")) {
                if (((OWindow) obj).frame != null) {
                    ((OWindow) obj).frame.setBounds(((OWindow) obj).frame.getX(), PCARD.pc.getY() + PCARD.pc.getInsets().top + v, ((OWindow) obj).frame.getWidth(), ((OWindow) obj).frame.getHeight());
                } else if (((OWindow) obj).gpw != null) {
                    ((OWindow) obj).gpw.setBounds(((OWindow) obj).gpw.getX(), PCARD.pc.getY() + PCARD.pc.getInsets().top + v, ((OWindow) obj).gpw.getWidth(), ((OWindow) obj).gpw.getHeight());
                } else {
                    throw new xTalkException("Cannot set top for this object??");
                }
            } else throw new xTalkException("Cannot set top for this object");
        } else if (0 == prpty.compareTo("right")) {
            int v;
            try {
                double d = Double.parseDouble(value);
                v = (int) d;
            } catch (Exception err) {
                throw new xTalkException("A number is required here");
            }
            if (btn != null) btn.setTopLeft(v - btn.getWidth(), btn.getTop());
            else if (fld != null) fld.setTopLeft(v - fld.getWidth(), fld.getTop());
            else if (cd != null) PCARD.pc.stack.setTopLeft(v - PCARD.pc.stack.getWidth(), PCARD.pc.stack.getTop());
            else throw new xTalkException("Cannot set right for this object");
        } else if (0 == prpty.compareTo("bottom")) {
            int v;
            try {
                double d = Double.parseDouble(value);
                v = (int) d;
            } catch (Exception err) {
                throw new xTalkException("A number is required here");
            }
            if (btn != null) btn.setTopLeft(btn.getLeft(), v - btn.getHeight());
            else if (fld != null) fld.setTopLeft(fld.getLeft(), v - fld.getHeight());
            else if (cd != null) PCARD.pc.stack.setTopLeft(PCARD.pc.stack.getLeft(), v - PCARD.pc.stack.getHeight());
            else throw new xTalkException("Cannot set bottom for this object");
        } else if (0 == prpty.compareTo("topleft")) {
            int v1, v2;
            String[] strAry = value.split(",");
            if (strAry.length != 2) throw new xTalkException("Coordinates are required here");
            try {
                double d = Double.parseDouble(strAry[0]);
                v1 = (int) d;
                d = Double.parseDouble(strAry[1]);
                v2 = (int) d;
            } catch (Exception err) {
                throw new xTalkException("A number is required here");
            }
            if (btn != null) btn.setTopLeft(v1, v2);
            else if (fld != null) fld.setTopLeft(v1, v2);
            else if (cd != null) PCARD.pc.stack.setTopLeft(v1, v2);
            else if (0 == obj.objectType.compareTo("window")) {
                if (((OWindow) obj).frame != null) {
                    ((OWindow) obj).frame.setBounds(PCARD.pc.getX() + v1, PCARD.pc.getY() + PCARD.pc.getInsets().top + v2, ((OWindow) obj).frame.getWidth(), ((OWindow) obj).frame.getHeight());
                } else if (((OWindow) obj).gpw != null) {
                    ((OWindow) obj).gpw.setBounds(PCARD.pc.getX() + v1, PCARD.pc.getY() + PCARD.pc.getInsets().top + v2, ((OWindow) obj).gpw.getWidth(), ((OWindow) obj).gpw.getHeight());
                } else {
                    throw new xTalkException("Cannot set topleft for this object??");
                }
            } else throw new xTalkException("Cannot set topleft for this object");
        } else if (0 == prpty.compareTo("loc") || 0 == prpty.compareTo("location")) {
            int v1, v2;
            String[] strAry = value.split(",");
            if (strAry.length != 2) throw new xTalkException("Coordinates are required here");
            try {
                double d = Double.parseDouble(strAry[0]);
                v1 = (int) d;
                d = Double.parseDouble(strAry[1]);
                v2 = (int) d;
            } catch (Exception err) {
                throw new xTalkException("A number is required here");
            }
            if (btn != null) btn.setTopLeft(v1 - btn.getWidth() / 2, v2 - btn.getHeight() / 2);
            else if (fld != null) fld.setTopLeft(v1 - fld.getWidth() / 2, v2 - fld.getHeight() / 2);
            else if (cd != null)
                PCARD.pc.stack.setTopLeft(v1 - PCARD.pc.stack.getWidth() / 2, v2 - PCARD.pc.stack.getHeight() / 2);
            else if (0 == obj.objectType.compareTo("window")) {
                if (((OWindow) obj).frame == PCARD.pc) {
                    ((OWindow) obj).frame.setBounds(v1, v2, ((OWindow) obj).frame.getWidth(), ((OWindow) obj).frame.getHeight());
                } else if (((OWindow) obj).frame != null) {
                    ((OWindow) obj).frame.setBounds(PCARD.pc.getX() + v1, PCARD.pc.getY() + PCARD.pc.getInsets().top + v2, ((OWindow) obj).frame.getWidth(), ((OWindow) obj).frame.getHeight());
                } else if (((OWindow) obj).gpw != null) {
                    ((OWindow) obj).gpw.setBounds(PCARD.pc.getX() + v1, PCARD.pc.getY() + PCARD.pc.getInsets().top + v2, ((OWindow) obj).gpw.getWidth(), ((OWindow) obj).gpw.getHeight());
                }
            } else if (0 == obj.objectType.compareTo("msg")) {
                if (GMsg.msg != null) {
                    GMsg.msg.setBounds(PCARD.pc.getX() + v1, PCARD.pc.getY() + PCARD.pc.getInsets().top + v2, GMsg.msg.getWidth(), GMsg.msg.getHeight());
                }
            }
            /*else{
                throw new xTalkException("Cannot set loc for this object??");
            }*/
            /*else if(0==obj.objectType.compareTo("window")){
                if(((OWindow)obj).frame!=null){
                    ((OWindow)obj).frame.setBounds(PCARD.pc.getX()+v1-((OWindow)obj).frame.getWidth()/2, PCARD.pc.getY()+PCARD.pc.getInsets().top+v2-((OWindow)obj).frame.getHeight()/2, ((OWindow)obj).frame.getWidth(), ((OWindow)obj).frame.getHeight());
                }
                else if(((OWindow)obj).gpw!=null){
                    ((OWindow)obj).gpw.setBounds(PCARD.pc.getX()+v1-((OWindow)obj).gpw.getWidth()/2, PCARD.pc.getY()+PCARD.pc.getInsets().top+v2-((OWindow)obj).gpw.getHeight()/2, ((OWindow)obj).gpw.getWidth(), ((OWindow)obj).gpw.getHeight());
                }
                else{
                    throw new xTalkException("Cannot set loc for this object??");
                }
            }
            else if(0==obj.objectType.compareTo("msg")){
                if(GMsg.msg!=null){
                    GMsg.msg.setBounds(PCARD.pc.getX()+v1-GMsg.msg.getWidth()/2, PCARD.pc.getY()+PCARD.pc.getInsets().top+v2-GMsg.msg.getHeight()/2, GMsg.msg.getWidth(), GMsg.msg.getHeight());
                }
            }*/
            else throw new xTalkException("Cannot set loc for this object");
        } else if (0 == prpty.compareTo("rect") || 0 == prpty.compareTo("rectangle")) {
            int v1, v2, v3, v4;
            String[] strAry = value.split(",");
            if (strAry.length != 4) throw new xTalkException("Rectangle specification is required here");
            try {
                double d = Double.parseDouble(strAry[0]);
                v1 = (int) d;
                d = Double.parseDouble(strAry[1]);
                v2 = (int) d;
                d = Double.parseDouble(strAry[2]);
                v3 = (int) d;
                d = Double.parseDouble(strAry[3]);
                v4 = (int) d;
            } catch (Exception err) {
                throw new xTalkException("A number is required here");
            }
            if (btn != null) btn.setRect(v1, v2, v3, v4);
            else if (fld != null) fld.setRect(v1, v2, v3, v4);
            else if (cd != null) PCARD.pc.stack.setRect(v1, v2, v3, v4);
            else if (0 == obj.objectType.compareTo("window")) {
                if (((OWindow) obj).frame == PCARD.pc) {
                    int z = PCARD.pc.zoom;
                    ((OWindow) obj).frame.setBounds(v1, v2, (v3 - v1) * z, (v4 - v2) * z + PCARD.pc.getInsets().top + PCARD.pc.getJMenuBar().getHeight());
                } else if (((OWindow) obj).frame != null) {
                    ((OWindow) obj).frame.setBounds(PCARD.pc.getX() + v1, PCARD.pc.getY() + PCARD.pc.getInsets().top + v2, v3 - v1, v4 - v2);
                } else if (((OWindow) obj).gpw != null) {
                    ((OWindow) obj).gpw.setBounds(PCARD.pc.getX() + v1, PCARD.pc.getY() + PCARD.pc.getInsets().top + v2, v3 - v1, v4 - v2);
                } else {
                    throw new xTalkException("Cannot set rect for this object??");
                }
            } else throw new xTalkException("Cannot set rect for this object");
        } else if (0 == prpty.compareTo("width")) {
            int v;
            try {
                double d = Double.parseDouble(value);
                v = (int) d;
            } catch (Exception err) {
                throw new xTalkException("A number is required here");
            }
            if (btn != null)
                btn.setRect(btn.getLeft() + btn.getWidth() / 2 - v / 2, btn.getTop(), btn.getLeft() + btn.getWidth() / 2 + (v + 1) / 2, btn.getTop() + btn.getHeight());
            else if (fld != null)
                fld.setRect(fld.getLeft() + fld.getWidth() / 2 - v / 2, fld.getTop(), fld.getLeft() + fld.getWidth() / 2 + (v + 1) / 2, fld.getTop() + fld.getHeight());
            else if (cd != null)
                PCARD.pc.stack.setRect(PCARD.pc.stack.getLeft() + PCARD.pc.stack.getWidth() / 2 - v / 2, PCARD.pc.stack.getTop(), PCARD.pc.stack.getLeft() + PCARD.pc.stack.getWidth() / 2 + (v + 1) / 2, PCARD.pc.stack.getTop() + PCARD.pc.stack.getHeight());
        } else if (0 == prpty.compareTo("height")) {
            int v;
            try {
                double d = Double.parseDouble(value);
                v = (int) d;
            } catch (Exception err) {
                throw new xTalkException("A number is required here");
            }
            if (btn != null)
                btn.setRect(btn.getLeft(), btn.getTop() + btn.getHeight() / 2 - v / 2, btn.getLeft() + btn.getWidth(), btn.getTop() + btn.getHeight() / 2 + (v + 1) / 2);
            else if (fld != null)
                fld.setRect(fld.getLeft(), fld.getTop() + fld.getHeight() / 2 - v / 2, fld.getLeft() + fld.getWidth(), fld.getTop() + fld.getHeight() / 2 + (v + 1) / 2);
            else if (cd != null)
                PCARD.pc.stack.setRect(PCARD.pc.stack.getLeft(), PCARD.pc.stack.getTop() + PCARD.pc.stack.getHeight() / 2 - v / 2, PCARD.pc.stack.getLeft() + PCARD.pc.stack.getWidth(), PCARD.pc.stack.getTop() + PCARD.pc.stack.getHeight() / 2 + (v + 1) / 2);
        } else if (0 == prpty.compareTo("zoomed")) {
            if (0 == obj.objectType.compareTo("window")) {
                if (0 == obj.name.compareToIgnoreCase("cd")) {
                    if (0 == value.compareToIgnoreCase("true")) {
                        PCARD.pc.setLocationRelativeTo(null);
                    }
                } else throw new xTalkException("Cannot set zoomed for this window");
            } else throw new xTalkException("Cannot set zoomed for this object");
        } else if (0 == prpty.compareTo("cantpeek")) {
            if (obj != null && obj.objectType.equals("stack")) {
                ((OStack) obj).cantPeek = (0 == value.compareToIgnoreCase("true"));
                ((OStack) obj).changed = true;
            } else throw new xTalkException("cantpeek can only be set for stacks");
        } else if (0 == prpty.compareTo("cantabort")) {
            if (obj != null && obj.objectType.equals("stack")) {
                ((OStack) obj).cantAbort = (0 == value.compareToIgnoreCase("true"));
                ((OStack) obj).changed = true;
            } else throw new xTalkException("cantabort can only be set for stacks");
        } else if (0 == prpty.compareTo("cantdelete")) {
            if (obj != null && obj.objectType.equals("stack")) {
                ((OStack) obj).cantDelete = (0 == value.compareToIgnoreCase("true"));
                ((OStack) obj).changed = true;
            } else throw new xTalkException("cantdelete can only be set for stacks");
        } else if (0 == prpty.compareTo("cantmodify")) {
            if (obj != null && obj.objectType.equals("stack")) {
                ((OStack) obj).changed = true;
                if (PCARD.pc.stack.saveXML != null && PCARD.pc.stack.saveXML.saveThread != null) {
                    // Save whether cantmodify was set/removed
                    PCARD.pc.stack.saveXML.saveThread.interrupt();
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                    }
                }
                ((OStack) obj).cantModify = (0 == value.compareToIgnoreCase("true"));
            } else throw new xTalkException("cantmodify can only be set for stacks");
        } else if (0 == prpty.compareTo("script")) {
            if (obj != null) {
                obj.setScript(value);
            }
        } else if (0 == prpty.compareTo("showpict")) {
            if (obj != null) {
                if (obj.objectType.equals("card") || obj.objectType.equals("background")) {
                    ((OCardBase) obj).picture.setVisible(value.equalsIgnoreCase("true"));
                    // ((OCardBase)obj).stack.pcard.repaint();
                    ((OCardBase) obj).changed = true;
                } else throw new xTalkException("showPict can only be set for cards and backgrounds");
            }
        } else if (0 == prpty.compareTo("textsize")) {
            int v;
            try {
                double d = Double.parseDouble(value);
                v = (int) d;
            } catch (Exception err) {
                throw new xTalkException("A number is required here");
            }
            if (fld != null) fld.setTextSize(v);
        } else if (0 == prpty.compareTo("textheight")) {
            int v;
            try {
                double d = Double.parseDouble(value);
                v = (int) d;
            } catch (Exception err) {
                throw new xTalkException("A number is required here");
            }
            if (fld != null) fld.setTextHeight(v);
        } else if (0 == prpty.compareTo("audiolevel")) {
            // Window property for Movie XCMD (0-256)
            int v;
            if (value.isEmpty()) value = "0";
            try {
                v = Integer.parseInt(value);
            } catch (Exception err) {
                throw new xTalkException("A number is required here");
            }
            if (obj.objectType.equals("window") && ((OWindow) obj).mp3 != null) {
                ((OWindow) obj).mp3.setVolume(v);
            } else throw new xTalkException(obj.getShortName() + " cannot set audiolevel");
        } else if (0 == prpty.compareTo("timeformat")) {
            // Window property for Movie XCMD
        } else if (0 == prpty.compareTo("currtime")) {
            // Window property for Movie XCMD
            int v;
            if (value.isEmpty()) value = "0";
            try {
                v = Integer.parseInt(value);
            } catch (Exception err) {
                throw new xTalkException("A number is required here");
            }
            if (obj.objectType.equals("window") && ((OWindow) obj).mp3 != null) {
                ((OWindow) obj).mp3.setTime(v);
            } else throw new xTalkException(obj.getShortName() + " cannot set currTime");
        } else if (0 == prpty.compareTo("scroll")) {
            if (fld != null && fld.style == 5) {
                int v;
                try {
                    double d = Double.parseDouble(value);
                    v = (int) d;
                } catch (Exception err) {
                    throw new xTalkException("A number is required here");
                }
                fld.setScroll(v);
            } else if (cd != null) {
                int v1, v2;
                String[] strAry = value.split(",");
                if (strAry.length != 2) {
                    throw new xTalkException("Coordinates are required here");
                }
                try {
                    double d = Double.parseDouble(strAry[0]);
                    v1 = (int) d;
                    d = Double.parseDouble(strAry[1]);
                    v2 = (int) d;
                } catch (Exception err) {
                    throw new xTalkException("A number is required here");
                }
                PCARD.pc.stack.setScroll(v1, v2);
            }
        } else if (0 == prpty.compareTo("style")) {
            if (btn != null) {
                if (value.equalsIgnoreCase("standard")) btn.style = 0;
                else if (value.equalsIgnoreCase("transparent")) btn.style = 1;
                else if (value.equalsIgnoreCase("opaque")) btn.style = 2;
                else if (value.equalsIgnoreCase("rectangle")) btn.style = 3;
                else if (value.equalsIgnoreCase("shadow")) btn.style = 4;
                else if (value.equalsIgnoreCase("roundrect")) btn.style = 5;
                else if (value.equalsIgnoreCase("oval")) btn.style = 6;
                else if (value.equalsIgnoreCase("default")) btn.style = 7;
                else if (value.equalsIgnoreCase("popup")) btn.style = 8;
                else if (value.equalsIgnoreCase("radio")) btn.style = 9;
                else if (value.equalsIgnoreCase("checkbox")) btn.style = 10;
                else throw new xTalkException("This style is unknown");
                OCard.reloadCurrentCard();
            } else if (fld != null) {
                if (value.equalsIgnoreCase("standard")) fld.style = 0;
                else if (value.equalsIgnoreCase("transparent")) fld.style = 1;
                else if (value.equalsIgnoreCase("opaque")) fld.style = 2;
                else if (value.equalsIgnoreCase("rectangle")) fld.style = 3;
                else if (value.equalsIgnoreCase("shadow")) fld.style = 4;
                else if (value.equalsIgnoreCase("scroll")) fld.style = 5;
                else throw new xTalkException("This style is unknown");
                OCard.reloadCurrentCard();
            } else throw new xTalkException("Cannot set style for this object");
        } else if (prpty.equals("showname")) {
            if (btn != null) {
                btn.showName = value.equalsIgnoreCase("true");
            }
        } else if (prpty.equals("blendingmode")) {
            int v = 0;
            if (value.equalsIgnoreCase("copy")) v = 0;
            else if (value.equalsIgnoreCase("blend")) v = 1;
            if (btn != null) btn.setBlendMode(v);
        } else if (prpty.equals("blendinglevel")) {
            int v = 0;
            v = Integer.parseInt(value);
            if (v < 0 || v > 100) {
                throw new xTalkException(obj.getShortName() + "'s property " + prpty + " has invalid value");
            }
            if (btn != null) btn.setBlendLevel(v);
        } else if (prpty.equals("color")) {
            int r = 0, g = 0, b = 0;
            String[] strs = value.split(",");
            if (strs.length != 3) {
                throw new xTalkException("Specify color with r,g,b");
            }
            r = Integer.parseInt(strs[0]);
            g = Integer.parseInt(strs[1]);
            b = Integer.parseInt(strs[2]);
            if (r < 0 || r > 65535 || g < 0 || g > 65535 || b < 0 || b > 65535) {
                throw new xTalkException("Specify color in range 0 to 65535");
            }
            if (btn != null) {
                btn.setColor(new Color(r / 256, g / 256, b / 256));
            }
            if (fld != null) {
                fld.setColor(new Color(r / 256, g / 256, b / 256));
            }
            OCard.reloadCurrentCard();
        } else if (prpty.equals("backcolor")) {
            int r = 0, g = 0, b = 0;
            String[] strs = value.split(",");
            if (strs.length != 3) {
                throw new xTalkException("Specify color with r,g,b");
            }
            r = Integer.parseInt(strs[0]);
            g = Integer.parseInt(strs[1]);
            b = Integer.parseInt(strs[2]);
            if (r < 0 || r > 65535 || g < 0 || g > 65535 || b < 0 || b > 65535) {
                throw new xTalkException("Specify color in range 0 to 65535");
            }
            if (btn != null) {
                btn.setBgColor(new Color(r / 256, g / 256, b / 256));
            }
            if (fld != null) {
                fld.setBgColor(new Color(r / 256, g / 256, b / 256));
            }
            OCard.reloadCurrentCard();
        } else if (prpty.equals("scale")) {
            // Picture window properties
            int v = 0;
            v = Integer.parseInt(value);
            float vv = switch (v) {
                case -5 -> 1f / 32;
                case -4 -> 1f / 16;
                case -3 -> 1f / 8;
                case -2 -> 1f / 4;
                case -1 -> 1f / 2;
                case 1 -> 2;
                case 2 -> 4;
                case 3 -> 8;
                case 4 -> 16;
                case 5 -> 32;
                default -> 1;
            };
            if (0 == obj.objectType.compareTo("window")) {
                if (((OWindow) obj).gpw != null) {
                    ((OWindow) obj).gpw.label.scale = vv;
                    ((OWindow) obj).gpw.repaint();
                } else {
                    throw new xTalkException("Cannot set scale for this object");
                }
            } else throw new xTalkException("Cannot set scale for this object");
        } else {
            throw new xTalkException(obj.getShortName() + "'s property " + prpty + " is unknown");
        }
    }

    public static int getVisualMode(ArrayList<String> strList, int start, int end) throws xTalkException {
        int mode = 0;
        int to = 0;
        int spd = 3;
        int next = start;

        if (next >= strList.size()) return 0;
        if (0 == strList.get(next).compareToIgnoreCase("cut")) {
            mode = 0;
            next++;
        } else if (0 == strList.get(next).compareToIgnoreCase("plain")) {
            mode = 0;
            next++;
        } else if (0 == strList.get(next).compareToIgnoreCase("barn")) {
            next++;
            if (next < strList.size() && 0 == strList.get(next).compareToIgnoreCase("door")) {
                next++;
                if (next < strList.size() && 0 == strList.get(next).compareToIgnoreCase("open")) {
                    mode = 1;
                    next++;
                } else if (next < strList.size() && 0 == strList.get(next).compareToIgnoreCase("close")) {
                    mode = 2;
                    next++;
                } else throw new xTalkException("Visual effect is unknown");
            } else throw new xTalkException("Visual effect is unknown");
        } else if (0 == strList.get(next).compareToIgnoreCase("dissolve")) {
            mode = 3;
            next++;
        } else if (0 == strList.get(next).compareToIgnoreCase("venetian")) {
            next++;
            if (next < strList.size() && 0 == strList.get(next).compareToIgnoreCase("blinds")) {
                mode = 4;
                next++;
            } else throw new xTalkException("Visual effect is unknown");
        } else if (0 == strList.get(next).compareToIgnoreCase("checkerboard")) {
            mode = 5;
            next++;
        } else if (0 == strList.get(next).compareToIgnoreCase("iris")) {
            next++;
            if (next < strList.size() && 0 == strList.get(next).compareToIgnoreCase("open")) {
                mode = 6;
                next++;
            } else if (next < strList.size() && 0 == strList.get(next).compareToIgnoreCase("close")) {
                mode = 7;
                next++;
            } else throw new xTalkException("Visual effect is unknown");
        } else if (0 == strList.get(next).compareToIgnoreCase("scroll")) {
            next++;
            if (next < strList.size() && 0 == strList.get(next).compareToIgnoreCase("left")) {
                mode = 8;
                next++;
            } else if (next < strList.size() && 0 == strList.get(next).compareToIgnoreCase("right")) {
                mode = 9;
                next++;
            } else if (next < strList.size() && 0 == strList.get(next).compareToIgnoreCase("up")) {
                mode = 10;
                next++;
            } else if (next < strList.size() && 0 == strList.get(next).compareToIgnoreCase("down")) {
                mode = 11;
                next++;
            } else throw new xTalkException("Visual effect is unknown");
        } else if (0 == strList.get(next).compareToIgnoreCase("wipe")) {
            next++;
            if (next < strList.size() && 0 == strList.get(next).compareToIgnoreCase("left")) {
                mode = 12;
                next++;
            } else if (next < strList.size() && 0 == strList.get(next).compareToIgnoreCase("right")) {
                mode = 13;
                next++;
            } else if (next < strList.size() && 0 == strList.get(next).compareToIgnoreCase("up")) {
                mode = 14;
                next++;
            } else if (next < strList.size() && 0 == strList.get(next).compareToIgnoreCase("down")) {
                mode = 15;
                next++;
            } else throw new xTalkException("Visual effect is unknown");
        } else if (0 == strList.get(next).compareToIgnoreCase("zoom")) {
            next++;
            if (next < strList.size() && 0 == strList.get(next).compareToIgnoreCase("open")) {
                mode = 16;
                next++;
            } else if (next < strList.size() && 0 == strList.get(next).compareToIgnoreCase("in")) {
                mode = 16;
                next++;
            } else if (next < strList.size() && 0 == strList.get(next).compareToIgnoreCase("close")) {
                mode = 17;
                next++;
            } else if (next < strList.size() && 0 == strList.get(next).compareToIgnoreCase("out")) {
                mode = 17;
                next++;
            } else throw new xTalkException("Visual effect is unknown");
        } else if (0 == strList.get(next).compareToIgnoreCase("shrink")) {
            next++;
            if (next < strList.size() && 0 == strList.get(next).compareToIgnoreCase("to")) {
                next++;
                if (next < strList.size() && 0 == strList.get(next).compareToIgnoreCase("top")) {
                    mode = 18;
                    next++;
                } else if (next < strList.size() && 0 == strList.get(next).compareToIgnoreCase("bottom")) {
                    mode = 19;
                    next++;
                } else if (next < strList.size() && 0 == strList.get(next).compareToIgnoreCase("center")) {
                    mode = 20;
                    next++;
                } else throw new xTalkException("Visual effect is unknown");
            } else throw new xTalkException("Visual effect is unknown");
        } else if (0 == strList.get(next).compareToIgnoreCase("stretch")) {
            next++;
            if (next < strList.size() && 0 == strList.get(next).compareToIgnoreCase("from")) {
                next++;
                if (next < strList.size() && 0 == strList.get(next).compareToIgnoreCase("top")) {
                    mode = 21;
                    next++;
                } else if (next < strList.size() && 0 == strList.get(next).compareToIgnoreCase("bottom")) {
                    mode = 22;
                    next++;
                } else if (next < strList.size() && 0 == strList.get(next).compareToIgnoreCase("center")) {
                    mode = 23;
                    next++;
                } else throw new xTalkException("Visual effect is unknown");
            } else throw new xTalkException("Visual effect is unknown");
        } else if (0 == strList.get(next).compareToIgnoreCase("push")) {
            next++;
            if (next < strList.size() && 0 == strList.get(next).compareToIgnoreCase("left")) {
                mode = 24;
                next++;
            } else if (next < strList.size() && 0 == strList.get(next).compareToIgnoreCase("right")) {
                mode = 25;
                next++;
            } else if (next < strList.size() && 0 == strList.get(next).compareToIgnoreCase("up")) {
                mode = 26;
                next++;
            } else if (next < strList.size() && 0 == strList.get(next).compareToIgnoreCase("down")) {
                mode = 27;
                next++;
            } else throw new xTalkException("Visual effect is unknown");
        }

        if (next < strList.size() && 0 == strList.get(next).compareToIgnoreCase("to")) {
            next++;
            if (next < strList.size() && 0 == strList.get(next).compareToIgnoreCase("black")) {
                to = 1;
                next++;
            } else if (next < strList.size() && 0 == strList.get(next).compareToIgnoreCase("white")) {
                to = 2;
                next++;
            } else if (next < strList.size() && 0 == strList.get(next).compareToIgnoreCase("grey")) {
                to = 3;
                next++;
            } else if (next < strList.size() && 0 == strList.get(next).compareToIgnoreCase("gray")) {
                to = 3;
                next++;
            } else if (next < strList.size() && 0 == strList.get(next).compareToIgnoreCase("inverse")) {
                to = 4;
                next++;
            } else throw new xTalkException("Visual effect is unknown");
        }

        if (next < strList.size() && 0 == strList.get(next).compareToIgnoreCase("fast")) {
            spd = 2;
            next++;
        } else if (next < strList.size() && 0 == strList.get(next).compareToIgnoreCase("slow")) {
            spd = 4;
            next++;
        } else if (next < strList.size() && 0 == strList.get(next).compareToIgnoreCase("very")) {
            next++;
            if (next < strList.size() && 0 == strList.get(next).compareToIgnoreCase("fast")) {
                spd = 1;
                next++;
            } else if (next < strList.size() && 0 == strList.get(next).compareToIgnoreCase("slow")) {
                spd = 5;
                next++;
            }
        }

        return mode + (to << 8) + (spd << 16);
    }
}
