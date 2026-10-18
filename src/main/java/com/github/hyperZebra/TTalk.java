package com.github.hyperZebra;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.PointerInfo;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serial;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;

import vavi.util.Debug;


class Result {

    int ret;
    String theResult;
}

class ObjResult {

    int cnt;
    OObject obj;
}

class Message {

    String message;
    String param;
    OObject object;
    boolean idle;
    boolean do_script;
}

class MemoryData {

    TreeSet<String> treeset;
    ArrayList<String> nameList;
    ArrayList<String> valueList;
    String message;
    String[] params; // Hold arguments for now
}

interface TalkObject {

    // ----getter-----
    String getStr();

    StringBuilder getStrBldr();

    double getNum();

    int getInt();

    // ----setter-----
    void setStr(String str);

    void setStrBldr(StringBuilder bldr);

    void setNum(Double num);

    void setNum(int in);
}

/*class BasicTalkObject implements TalkObject {
    // Basic object that holds only String
    private String str;

    // ----getter-----
    public String getStr(){
        if(str!=null) return str;
        else return "";
    }
    public double getNum(){
        if(str!=null) return Double.valueOf(str);
        else return 0;
    }
    public int getInt(){
        if(str!=null) return Integer.valueOf(str);
        else return 0;
    }

    // ----setter-----
    public void setStr(String str){
        this.str = str;
    }
    public void setNum(Double num){
        this.str = Double.toString(num);
    }
    public void setNum(int in){
        this.str = Integer.toString(in);
    }
}*/

final class VariableObj implements TalkObject {

    // Variable object that also holds numeric type
    private StringBuilder str;
    private double num;
    private boolean num_flag;

    // ----getter-----
    @Override
    public String getStr() {
        if (num_flag) return Double.toString(num);
        else return str.toString();
    }

    @Override
    public StringBuilder getStrBldr() {
        if (num_flag) return new StringBuilder(Double.toString(num));
        else return str;
    }

    @Override
    public double getNum() {
        if (num_flag) return num;
        else return num;
    }

    @Override
    public int getInt() {
        if (num_flag) return (int) num;
        else return (int) num;
    }

    // ----setter-----
    @Override
    public void setStr(String str) {
        this.str = new StringBuilder(str);
        this.num_flag = false;
    }

    @Override
    public void setStrBldr(StringBuilder bldr) {
        this.str = bldr;
        this.num_flag = false;
    }

    @Override
    public void setNum(Double num) {
        this.num = num;
        this.num_flag = true;
    }

    @Override
    public void setNum(int in) {
        this.num = in;
        this.num_flag = true;
    }
}

enum chunkType {BYTE, CHAR, ITEM, WORD, LINE}
/*
final class ChunkObj implements TalkObject {
    // Object that can be read/written while holding chunk specification
    // Nest ChunkObj for multiple chunk specifications
    private TalkObject tobj;
    private int[] splitSets; // Array of number of chunks + 1.
                              // Offsets to each delimiter are stored.
                              // [0] is always 0, and [splitSet.length] contains the total length.
    private int chunk_s;
    private int chunk_e;
    private chunkType chunktype;

    ChunkObj(TalkObject t, chunkType ct, int s, int e){
        tobj = t;
        chunktype = ct;
        chunk_s = s;
        chunk_e = e;
    }

    public void setChunk(chunkType ct, int s, int e){
        if(chunktype != ct){
            chunktype = ct;
            splitSets = null;
        }
        chunk_s = s;
        chunk_e = e;
    }

    private void makeCache(){
        if(chunktype == chunkType.BYTE || chunktype == chunkType.CHAR){
            return; // No cache needed
        }

        if(chunktype == chunkType.ITEM || chunktype == chunkType.LINE){
            String splitChar = "\n";
            if(chunktype == chunkType.ITEM){
                splitChar = TTalk.itemDelimiter;
            }
            String[] data = tobj.getStr().split(splitChar);
            splitSets = new int[data.length+1];
            splitSets[0] = 0;
            for(int i=0; i<data.length; i++){
                splitSets[i+1] = splitSets[i] + data[i].length()+1;
            }
        }
        else if(chunktype == chunkType.WORD ){
            char[] data = tobj.getStr().toCharArray();
            boolean in_quote = false;
            int cnt = 1;
            // Count the number of array elements
            for(int i=0; i<data.length; i++){
                if(data[i] == '\"'){
                    in_quote = !in_quote;
                }
                else if((data[i] == ' ' || data[i] == '\n') && !in_quote){
                    cnt++;
                }
            }
            // Create array
            splitSets = new int[cnt+1];
            // Assign to array
            cnt = 1;
            splitSets[0] = 0;
            int i=0;
            for(; i<data.length; i++){
                if(data[i] == '\"'){
                    in_quote = !in_quote;
                }
                else if((data[i] == ' ' || data[i] == '\n') && !in_quote){
                    splitSets[cnt] = i;
                    cnt++;
                }
            }
            splitSets[cnt] = i;
        }
    }

    // ----getter-----
    public String getStr(){
        if(chunktype == chunkType.BYTE){
            return Integer.toString(tobj.getStr().getBytes()[chunk_s]);
        }
        else if(chunktype == chunkType.CHAR){
            return tobj.getStr().substring(chunk_s,chunk_e);
        }
        else{
            if(splitSets==null) makeCache();
            return tobj.getStr().substring(splitSets[chunk_s],splitSets[chunk_e]);
        }
    }
    public StringBuilder getStrBldr(){
        return new StringBuilder(getStr());
    }
    public double getNum(){
        return Double.valueOf(getStr());
    }
    public int getInt(){
        return Integer.valueOf(getStr());
    }

    // ----setter-----
    public void setStr(String str){
        if(chunktype == chunkType.BYTE){
            Integer.toString(tobj.setStr().getBytes()[chunk_s]);
        }
        else if(chunktype == chunkType.CHAR){
            return tobj.setStr().substring(chunk_s,chunk_e);
        }
        else{
            return tobj.setStr().substring(splitSets[chunk_s],splitSets[chunk_e]);
            splitSets = null;
        }
    }
    public void setStrBldr(StringBuilder bldr) {
        this.str = bldr;
    }
    public void setNum(Double num){
        setStr(Double.toString(num));
    }
    public void setNum(int in){
        setStr(Integer.toString(in));
    }

}*/

// Would also like a BytObj that holds byte[]
//  Whether to reference with byte of or handle in byte units with char of.

// Getting confused. Let me organize.
//  Initially holds String
//  When referenced as a number, convert to number each time
//  When a number is assigned, a new numeric type is created.
//  Repeated assignments of numbers and characters increase unused objects, so GC do your best
//
//  When a variable is referenced with line of, create and hold data as an array, and return one of the array elements
//    item of line of etc. is complicated. Should we create a child that holds offset?
//    At that time, whether to leave StringBuilder type as null or hold combination information depends on implementation.
//    Discard array data when StringBuilder side is changed
//  When a part is referenced with line of, create only the data as an array and return it, discarding the data.
//    Parts might also hold array data, but fields need all data when displaying anyway, so it is a waste of space
//      Local variables are quickly deleted so it should not be a waste of space
//  When assigned to a variable with line of, the variable sets StringBuilder type to null and holds data as an array
//  When assigned to a part with line of, create an array, perform assignment, then perform combination and assign to StringBuilder.
//  When all data is referenced from a variable assigned with line of, combine and return the data in the array.
//    At that time, whether to leave StringBuilder type as null or hold combination information depends on implementation.
//  For word of, there are various split characters so recovery is complicated, so also hold an array of just split characters.

class OpenFile {

    FileInputStream istream;
    FileOutputStream ostream;
    String path;
}

public class TTalk extends Thread {

    static TTalk talk;
    static DecimalFormat numFormat = new DecimalFormat("0.######");
    static boolean idle = true;
    static String itemDelimiter = ",";
    static ArrayList<Message> messageList = new ArrayList<>();
    static TSound tsound = new TSound();
    static boolean stop = false;
    static int tracemode = 0;
    static boolean stepflag;
    static MemoryData globalData = new MemoryData();
    static ArrayList<String> pushCardList = new ArrayList<>();
    static Result lastResult;
    static int fastLockScreen = 0;
    static boolean lockErrorDialogs = false;
    static long lastErrorTime;
    static boolean lockMessages = false;
    static int dragspeed = 0;
    static int wait = 1; // Wait to return to old speed in 2.x
    static ArrayList<OpenFile> openFileList = new ArrayList<>();


    enum wordType {
        X,
        VARIABLE,
        GLOBAL,
        OBJECT,
        STRING,
        CONST,
        CHUNK,
        PROPERTY,
        CMD, CMD_SUB, USER_CMD, XCMD,
        FUNC, USER_FUNC, XFCN,
        OPERATOR, QUOTE,
        LBRACKET, RBRACKET,
        LFUNC, RFUNC,
        COMMA, COMMA_FUNC,
        OF_FUNC, OF_PROP, OF_OBJ, OF_CHUNK,
        ON_HAND, END_HAND, ON_FUNC/*, END_FUNC*/, EXIT, PASS, RETURN,
        IF, ELSE, THEN, ENDIF,
        REPEAT, END_REPEAT, EXIT_REP, NEXT_REP,
        THE_FUNC,
        COMMENT,
        NOP, OPERATOR_SUB
    }

    static TreeSet<String> operatorSet; // Tree for speeding up
    static TreeSet<String> constantSet; // Tree for speeding up
    static TreeSet<String> commandSet; // Used by script editor
    static TreeSet<String> funcSet; // Used by script editor
    static TreeSet<String> propertySet; // Used by script editor

    private static void init() {
        operatorSet = new TreeSet<>();
        operatorSet.add("div");
        operatorSet.add("mod");
        operatorSet.add("not");
        operatorSet.add("and");
        operatorSet.add("or");
        operatorSet.add("contains");
        operatorSet.add("within");
        operatorSet.add("there");
        operatorSet.add("is");
        operatorSet.add("<");
        operatorSet.add(">");

        constantSet = new TreeSet<>();
        constantSet.add("down");
        constantSet.add("empty");
        constantSet.add("false");
        constantSet.add("formfeed");
        constantSet.add("linefeed");
        constantSet.add("pi");
        constantSet.add("quote");
        constantSet.add("comma");
        constantSet.add("return");
        constantSet.add("space");
        constantSet.add("tab");
        constantSet.add("true");
        constantSet.add("up");
        constantSet.add("zero");
        constantSet.add("one");
        constantSet.add("two");
        constantSet.add("three");
        constantSet.add("four");
        constantSet.add("five");
        constantSet.add("six");
        constantSet.add("seven");
        constantSet.add("eight");
        constantSet.add("nine");

        commandSet = new TreeSet<>();
        commandSet.add("add");
        commandSet.add("answer");
        commandSet.add("arrowkey");
        commandSet.add("ask");
        commandSet.add("beep");
        commandSet.add("choose");
        commandSet.add("click");
        commandSet.add("close");
        commandSet.add("commandkeydown");
        commandSet.add("controlkey");
        commandSet.add("convert");
        commandSet.add("copy");
        commandSet.add("create");
        commandSet.add("debug");
        commandSet.add("delete");
        commandSet.add("dial");
        commandSet.add("disable");
        commandSet.add("divide");
        commandSet.add("do");
        commandSet.add("domenu");
        commandSet.add("drag");
        commandSet.add("edit");
        commandSet.add("enable");
        commandSet.add("enterinfield");
        commandSet.add("enterkey");
        commandSet.add("export");
        commandSet.add("find");
        commandSet.add("get");
        commandSet.add("global");// Special handling
        commandSet.add("go");
        commandSet.add("help");
        commandSet.add("hide");
        commandSet.add("import");
        commandSet.add("keydown");
        commandSet.add("lock");
        commandSet.add("mark");
        commandSet.add("multiply");
        commandSet.add("open");
        commandSet.add("play");
        commandSet.add("pop");
        commandSet.add("print");
        commandSet.add("push");
        commandSet.add("put");
        commandSet.add("read");
        commandSet.add("reply");
        commandSet.add("request");
        commandSet.add("reset");
        commandSet.add("returninfield");
        commandSet.add("returnkey");
        commandSet.add("save");
        commandSet.add("select");
        commandSet.add("send");
        commandSet.add("set");
        commandSet.add("show");
        commandSet.add("sort");
        commandSet.add("start");
        commandSet.add("stop");
        commandSet.add("subtract");
        commandSet.add("tabkey");
        commandSet.add("type");
        commandSet.add("unlock");
        commandSet.add("unmark");
        commandSet.add("visual");
        commandSet.add("wait");
        commandSet.add("write");

        commandSet.add("flash");// add
        commandSet.add("about");// add

        funcSet = new TreeSet<>();
        funcSet.add("abs");//
        funcSet.add("annuity");//
        funcSet.add("atan");//
        funcSet.add("average");//
        funcSet.add("chartonum");//
        funcSet.add("clickchunk");
        funcSet.add("clickh");//
        funcSet.add("clickline");//
        funcSet.add("clickloc");//
        funcSet.add("clicktext");
        funcSet.add("clickv");//
        funcSet.add("cmdkey");//
        funcSet.add("commandkey");//
        funcSet.add("compound");//
        funcSet.add("cos");//
        funcSet.add("date");//
        funcSet.add("diskspace");//
        funcSet.add("exp");//
        funcSet.add("exp1");//
        funcSet.add("exp2");//
        funcSet.add("foundchunk");//
        funcSet.add("foundfield");//
        funcSet.add("foundline");//
        funcSet.add("foundtext");//
        funcSet.add("heapspace");//
        funcSet.add("length");//
        funcSet.add("ln");//
        funcSet.add("ln1");//
        funcSet.add("log2");//
        funcSet.add("max");//
        funcSet.add("menus");//
        funcSet.add("min");//
        funcSet.add("mouse");//
        funcSet.add("mouseclick");//
        funcSet.add("mouseh");//
        funcSet.add("mouseloc");//
        funcSet.add("mousev");//
        funcSet.add("number");
        funcSet.add("numtochar");//
        funcSet.add("offset");//
        funcSet.add("optionkey");//
        funcSet.add("param");//
        funcSet.add("paramcount");//
        funcSet.add("params");//
        funcSet.add("programs");
        funcSet.add("random");//
        funcSet.add("result");//
        funcSet.add("round");//
        funcSet.add("screenrect");//
        funcSet.add("seconds");//
        funcSet.add("selectedbutton");
        funcSet.add("selectedchunk");
        funcSet.add("selectedfield");//
        funcSet.add("selectedline");//
        funcSet.add("selectedloc");
        funcSet.add("selectedtext");//
        funcSet.add("selection");
        funcSet.add("shiftkey");//
        funcSet.add("sin");//
        funcSet.add("sound");//
        funcSet.add("sqrt");//
        funcSet.add("stacks");//
        funcSet.add("stackspace");//
        funcSet.add("sum");//
        funcSet.add("systemversion");//
        funcSet.add("tan");//
        funcSet.add("target");//
        funcSet.add("ticks");//
        funcSet.add("time");//
        funcSet.add("tool");//
        funcSet.add("trunc");//
        funcSet.add("value");//
        funcSet.add("windows");//

        funcSet.add("systemname");// add
        funcSet.add("javaversion");// add
        funcSet.add("javavmversion");// add
        funcSet.add("controlkey");// add

        propertySet = new TreeSet<>();
        propertySet.add("address");
        propertySet.add("autohilite");
        propertySet.add("autoselect");
        propertySet.add("autotab");
        propertySet.add("blindtyping");
        propertySet.add("botright");
        propertySet.add("bottom");// set
        propertySet.add("bottomright");
        propertySet.add("brush");
        propertySet.add("cantabort");// set
        propertySet.add("cantdelete");// set
        propertySet.add("cantmodify");// set
        propertySet.add("cantpeek");// set
        propertySet.add("centered");
        propertySet.add("checkmark");
        propertySet.add("cmdchar");
        propertySet.add("commandchar");
        propertySet.add("cursor");// set
        propertySet.add("debugger");
        propertySet.add("dialingtime");
        propertySet.add("dialingvolume");
        propertySet.add("dontsearch");
        propertySet.add("dontwrap");
        propertySet.add("dragspeed");
        propertySet.add("editbkgnd");
        propertySet.add("enabled");// set
        propertySet.add("environment");
        propertySet.add("family");
        propertySet.add("filled");
        propertySet.add("fixedlineheight");
        propertySet.add("freesize");
        propertySet.add("grid");
        propertySet.add("height");// set
        propertySet.add("highlight");// set
        propertySet.add("hilight");// set
        propertySet.add("hilite");// set
        propertySet.add("highlite");// set
        propertySet.add("icon");// set
        propertySet.add("id");
        propertySet.add("itemdelimiter");// set
        propertySet.add("language");
        propertySet.add("left");// set
        propertySet.add("linesize");
        propertySet.add("loc");// set
        propertySet.add("location");// set
        propertySet.add("lockerrordialogs");// set
        propertySet.add("lockmessages");// set
        propertySet.add("lockrecent");// set
        propertySet.add("lockscreen");// set
        propertySet.add("locktext");
        propertySet.add("longwindowtitles");
        propertySet.add("markchar");
        propertySet.add("marked");
        propertySet.add("menumessage");
        propertySet.add("menumsg");
        propertySet.add("messagewatcher");
        propertySet.add("multiple");
        propertySet.add("multiplelines");
        propertySet.add("multispace");
        propertySet.add("name");// set
        propertySet.add("numberformat");
        propertySet.add("owner");
        propertySet.add("partnumber");
        propertySet.add("pattern");
        propertySet.add("polysides");
        propertySet.add("powerkeys");
        propertySet.add("printmargins");
        propertySet.add("printtextalign");
        propertySet.add("printtextfont");
        propertySet.add("printtextheight");
        propertySet.add("printtextsize");
        propertySet.add("printtextstyle");
        propertySet.add("rect");// set
        propertySet.add("rectangle");// set
        propertySet.add("reporttemplates");
        propertySet.add("right");// set
        propertySet.add("script");// set
        propertySet.add("scripteditor");
        propertySet.add("scriptinglanguage");
        propertySet.add("scripttextfont");// get,set
        propertySet.add("scripttextsize");// get,set
        propertySet.add("scroll");// set
        propertySet.add("sharedhilite");
        propertySet.add("sharedtext");
        propertySet.add("showlines");
        propertySet.add("showname");
        propertySet.add("showpict");// set
        propertySet.add("size");
        propertySet.add("stacksinuse");
        propertySet.add("style");
        propertySet.add("suspended");
        propertySet.add("textalign");
        propertySet.add("textarrows");
        propertySet.add("textfont");// set
        propertySet.add("textheight");// set
        propertySet.add("textsize");// set
        propertySet.add("textstyle");// set
        propertySet.add("titelwidth");
        propertySet.add("top");// set
        propertySet.add("topleft");// set
        propertySet.add("tracedelay");
        propertySet.add("userlevel");// set
        propertySet.add("usermodify");
        propertySet.add("variablewatcher");
        propertySet.add("version");
        propertySet.add("visible");// set
        propertySet.add("widemargins");
        propertySet.add("width");// set
        propertySet.add("zoomed");// set

        propertySet.add("text");// (add) set
        propertySet.add("systemlang");// add get
        propertySet.add("blendingmode");// add set
        propertySet.add("blendinglevel");// add set
        propertySet.add("color");// add set
        propertySet.add("backColor");// add set
        propertySet.add("selectedfield");//
        propertySet.add("selectedline");//
        propertySet.add("selectedtext");//

        propertySet.add("audiolevel");// xcmd set
        propertySet.add("currtime");// xcmd set
        propertySet.add("scale");// xcmd set

        globalData.treeset = new TreeSet<>();
        globalData.nameList = new ArrayList<>();
        globalData.valueList = new ArrayList<>();
    }

    // Thread to execute received messages
    @Override
    public void run() {
        talk = this;
        init();
        this.setName("scriptEngine");
        // setPriority(MAX_PRIORITY);

        while (true) {
            Message msg = null;
            synchronized (messageList) {
                if (!messageList.isEmpty()) {
                    msg = messageList.get(0);
                    messageList.remove(0);
                }
            }
            if (msg != null) {
                try {
                    if (msg.do_script) {
                        doScriptforMenu(msg.message);
                    } else ReceiveMessage(msg.message, msg.param, msg.object, null, msg.idle);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                /*if(idle==false)*/
                if (PCARD.pc.tool == null && AuthTool.tool == null) {
                    returnToIdle();
                }
                TTalk.TalkSleep(50);
            }
        }
    }

    private static void TalkSleep(int time) {
        if (TXcmd.terazzaAry != null) {
            long startTime = (int) new Date().getTime();
            while (true) {
                // Execute animation during sleep
                TXcmd.TerazzaAnime();

                // Check remaining time
                int left = time - (int) ((new Date().getTime()) - startTime);
                if (left <= 0) {
                    break;
                }

                // Sleep in increments of 30
                int t = Math.min(left, 30);
                try {
                    Thread.sleep(t);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                if (Thread.currentThread().isInterrupted()) {
                    break;
                }
            }
        } else if (time > 0) {
            try {
                Thread.sleep(time);
            } catch (InterruptedException e) {
            }
        }
    }

    // Execute internal script when running a menu
    public static String doScriptforMenu(String script) throws xTalkException {
        if (Thread.currentThread() != talk) {
            CallMessage(script, "", null, false, true);
            return "";
        }
        stop = false;
        Result result = null;
        MemoryData memData = new MemoryData();
        memData.treeset = new TreeSet<>();
        memData.nameList = new ArrayList<>();
        memData.valueList = new ArrayList<>();
        result = talk.doScriptLine(script, /*PCARD.pc.stack.curCard*/null, null, memData, 0);

        if (result == null) return "";
        return result.theResult;
    }

    // Send message to script thread
    public static void CallMessage(String message, OObject object) {
        // System.out.println("CallMessage "+message+" "+object.getShortName());
        CallMessage(message, "", object, false, false);
    }

    public static void CallMessage(String message, String param, OObject object, boolean idle, boolean do_script) {
        Message msg = new Message();
        msg.message = message;
        msg.object = object;
        msg.param = param;
        msg.idle = idle;
        msg.do_script = do_script;

        synchronized (messageList) {
            messageList.add(msg);
        }
    }

    private void ReceiveMessage(String message, String param, OObject object, MemoryData memData, boolean idlenow)
            throws xTalkException {
        boolean changeIdle = false;
        if (idlenow == false && idle == true) {
            // Set to busy state
            idle = false;
            changeIdle = true;
            GUI.mouseClicked = false;
        }

        String[] params;
        if (!param.isEmpty()) {
            params = new String[1];
            params[0] = param;
        } else params = new String[0];
        lastResult = ReceiveMessage(message, params, object, object, memData);

        if (changeIdle) {
            // Return to idle state
            returnToIdle();
        }
    }

    private static void returnToIdle() {
        if (!idle && !GUI.mouseDown) {
            // Call GC as much as possible
            SwingUtilities.invokeLater(System::gc);
        }
        idle = true;
        TTalk.stop = false;
        TTalk.tracemode = 0;
        if (PCARD.pc.stack == null) return;

        if (PCARD.lockedScreen) {
            OCard.reloadCurrentCard();
            PCARD.lockedScreen = false;
        }
        fastLockScreen = 0;
        PCARD.visual = 0;
        numFormat = new DecimalFormat("0.######");
        itemDelimiter = ",";
        dragspeed = 0;
        Cursor cr = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
        PCARD.pc.setCursor(cr);
        PCARD.pc.stack.mouseWithinCheck();

        if (VariableWatcher.watcherWindow.isVisible() && TTalk.globalData.nameList.size() != VariableWatcher.rowSize) {
            VariableWatcher.watcherWindow.setTable(TTalk.globalData, null);
        }

        // Close files
        for (OpenFile openFile : openFileList) {
            try {
                if (openFile.istream != null) {
                    openFile.istream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                if (openFile.ostream != null) {
                    openFile.ostream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        openFileList.clear();
    }


    private Result ReceiveMessage(String message, String[] params, OObject object, OObject target, MemoryData memData)
            throws xTalkException {
        // System.out.println("ReceiveMessage "+message+" "+object.getShortName());
        Result result = new Result();
        return ReceiveMessage(message, params, object, target, memData, result, false, false, false, 0);
    }

    String[] messageRingArray = new String[128];
    OObject[] objectRingArray = new OObject[128];
    int ringCnt;

    // Receive message
    private Result ReceiveMessage(String message, String[] params, OObject object, OObject target, MemoryData memData, Result result, boolean isFunc, boolean isDynamic, boolean isBgroot, int usingCnt)
            throws xTalkException {
        // long timestart = System.currentTimeMillis();


        result.ret = 1;

        if (object == null) {
            object = PCARD.pc.stack.curCard;
        }

        if (object == null || object.scriptList == null) return result;

        // Pick up only handler lines and save to list
        if (object.handlerLineList == null) {
            object.handlerList = new TreeSet<>();
            object.handlerLineList = new ArrayList<>();
            for (int line = 0; line < object.scriptList.size(); line++) {
                String str = object.scriptList.get(line);
                while (str.substring(0, 0).equals(" ")) {
                    str = str.substring(1);
                }
                String[] words = str.split(" ");
                if (words.length <= 1) {
                    continue;
                }
                if (0 == words[0].compareToIgnoreCase("on") ||
                        0 == words[0].compareToIgnoreCase("function") ||
                        0 == words[0].compareToIgnoreCase("end")) {
                    if (0 == words[0].compareToIgnoreCase("on") ||
                            0 == words[0].compareToIgnoreCase("function")) {
                        if (words[1].contains("--")) {
                            words[1] = words[1].substring(0, words[1].indexOf("--"));
                        }
                        object.handlerList.add(words[1].toLowerCase().intern());
                    }
                    object.handlerLineList.add(line);
                }
            }
        }

        // Find handler
        if (object.handlerList.contains(message.toLowerCase().intern())) {
            // For messageWatcher
            messageRingArray[ringCnt % 128] = message;
            objectRingArray[ringCnt % 128] = object;
            ringCnt++;
            if (MessageWatcher.watcherWindow != null && MessageWatcher.watcherWindow.isVisible()) {
                MessageWatcher.watcherWindow.setTable(messageRingArray, objectRingArray, ringCnt);
            }

            int start = 0;
            for (int i = 0; object.handlerLineList != null && i < object.handlerLineList.size(); i++) {
                int line = object.handlerLineList.get(i);
                String str = object.scriptList.get(line);
                while (str.substring(0, 0).equals(" ")) {
                    str = str.substring(1);
                }
                String[] words = str.split(" ");
                if (words.length <= 1) {
                    line++;
                    continue;
                }

                if (words[1].contains("--")) {
                    words[1] = words[1].substring(0, words[1].indexOf("--"));
                }

                if (0 == words[1].compareToIgnoreCase(message)) {
                    if (!isFunc && 0 == words[0].compareToIgnoreCase("on")) {
                        start = line + 1;
                    }
                    if (isFunc && 0 == words[0].compareToIgnoreCase("function")) {
                        start = line + 1;
                    }
                    if (start > 0 && 0 == words[0].compareToIgnoreCase("end")) {
                        MemoryData memData2 = new MemoryData();
                        memData2.treeset = new TreeSet<>();
                        memData2.nameList = new ArrayList<>();
                        memData2.valueList = new ArrayList<>();

                        // Arguments
                        String[] handlerWords = object.scriptList.get(start - 1).split(" ");
                        if (handlerWords.length >= 3) { // on handler param
                            StringBuilder paramStr = new StringBuilder();
                            for (int k = 2; k < handlerWords.length; k++) paramStr.append(handlerWords[k]);
                            String[] paramNames = paramStr.toString().split(",");
                            for (int j = 0; j < paramNames.length; j++) {
                                if (paramNames[j].startsWith("--")) break;
                                if (paramNames[j].contains("--")) {
                                    paramNames[j] = paramNames[j].substring(0, paramNames[j].indexOf("--"));
                                }
                                if (j >= params.length) setVariable(memData2, paramNames[j], "");
                                else setVariable(memData2, paramNames[j], params[j]);
                            }
                        }

                        // Set message name and params in memData (for param(i) and the params)
                        memData2.message = message;
                        memData2.params = params;

                        // timeIsMoney("ReceiveMessage2-1:",timestart,14);
                        try {
                            result = doScript(object, target, message, object.scriptList, start, line - 1, memData2, params, 0);
                        } catch (Exception err) {
                            if (!object.name.isEmpty())
                                System.out.println(object.objectType + " '" + object.name + "' " + err);
                            else
                                System.out.println(object.objectType + " id " + object.id + " " + err);
                            err.printStackTrace();
                            result.ret = -1;
                            break;
                        }
                    }
                }
                line++;
            }
        }

        // timeIsMoney("ReceiveMessage2-2:",timestart,15);
        if (result.ret == 1) {
            if (object.parent != null) {
                // Normal pass: send to parent
                /*if(object.objectType.equals("background")&&isBgroot==false){
                    // Background goes through the card once
                    System.out.println("!1");
                    result=ReceiveMessage(message, params, ((OBackground)object).viewCard, target, isFunc, isDynamic, true);
                }
                else*/
                if (object.parent.objectType.equals("background") && !object.objectType.equals("card")) {
                    // Background components go through the card once
                    result = ReceiveMessage(message, params, ((OBackground) object.parent).viewCard, target, memData, result, isFunc, isDynamic, isBgroot, usingCnt);
                } else {
                    result = ReceiveMessage(message, params, object.parent, target/*object.parent*/, memData, result, isFunc, isDynamic, isBgroot, usingCnt);
                }
            } else if (!isDynamic && target != null && (
                    target.parent != null && target.parent.objectType.equals("background") ||
                            target.objectType.equals("background") ||
                            target.objectType.equals("stack") ||
                            target.objectType.equals("card") && target != PCARD.pc.stack.curCard)) {
                // Dynamic pass: send to current card
                result = ReceiveMessage(message, params, PCARD.pc.stack.curCard, target, memData, result, isFunc, true, isBgroot, usingCnt);
            } else if (!isDynamic && target != null && target.parent != null && target.parent.objectType.equals("card") && target.parent != PCARD.pc.stack.curCard) {
                // Dynamic pass: send to current card
                result = ReceiveMessage(message, params, PCARD.pc.stack.curCard, target, memData, result, isFunc, true, isBgroot, usingCnt);
            } else {
                OStack stack = null;
                if (object.objectType.equals("stack")) stack = (OStack) object;
                else if (object.parent.objectType.equals("stack")) stack = (OStack) object.parent;
                else if (object.parent.parent.equals("stack")) stack = (OStack) object.parent.parent;
                if (stack != null && stack.usingStacks != null && usingCnt < stack.usingStacks.size() &&
                        stack.usingStacks.get(usingCnt) != null && stack.usingStacks.get(usingCnt).scriptList != null) {
                    usingCnt++;
                    result = ReceiveMessage(message, params, stack.usingStacks.get(usingCnt - 1), target, memData, result, isFunc, true, isBgroot, usingCnt);
                } else if (!commandSet.contains(message)) {
                    result = TUtil.CallSystemMessage(message, params, target, memData, isFunc);
                }
            }
        }

        return result;// 0 if found, 1 if not found so look for next object
    }


    // mode=0 normal    mode=1 internal call so no error
    private Result doScript(OObject object, OObject target, String message, String script, MemoryData memData, String[] parentparams, int mode)
            throws xTalkException {
        ArrayList<String> scriptList = new ArrayList<>();
        while (script.contains("\r")) { // Convert return (CR) to (LF)
            script = script.substring(0, script.indexOf("\r")) + "\n" + script.substring(script.indexOf("\r") + 1);
        }
        String[] scrAry = script.split("\n");

        Collections.addAll(scriptList, scrAry);

        return doScript(object, target, message, scriptList, 0, scriptList.size() - 1, memData, parentparams, mode);
    }


    // Execute handler body
    // object may be null
    // message may be null
    // -1 . error/exit to HyperCard
    // 0 .. normal end
    // 1 .. pass message
    // 2 .. exit repeat
    // 3 .. exit handle
    private Result doScript(OObject object, OObject target, String message, ArrayList<String> scriptList, int start, int end,
                            MemoryData memData, String[] parentparams, int mode)
            throws xTalkException {
        if (TTalk.stop) {
            throw new xTalkException("user abort.");
        }
        if (TTalk.tracemode >= 1) {
            ScriptEditor.openScriptEditor(PCARD.pc, object, start);
            boolean watcher = false;
            if (VariableWatcher.watcherWindow.isVisible()) {
                watcher = true;
                VariableWatcher.watcherWindow.setTable(TTalk.globalData, memData);
            }
            if (TTalk.tracemode == 2) {
                while (stepflag == false) {
                    try {
                        Thread.sleep(20);
                    } catch (InterruptedException e) {
                    }
                    if (watcher == false && VariableWatcher.watcherWindow.isVisible()) {
                        watcher = true;
                        VariableWatcher.watcherWindow.setTable(TTalk.globalData, memData);
                    }
                }
                stepflag = false;
            }
        }

        int line = start;
        Result result = new Result();

        /*StringBuilder scr = new StringBuilder(128);
        scr.append("+ ");
        for(int i=start; i<=end; i++){
            scr.append(scriptList.get(i));
            scr.append(" ");
        }
        System.out.println(scr.toString());*/

        result.ret = 0;

        if (object != null && object.wrapFlag == false && object.scriptList == scriptList) {
            for (int i = 0; i < scriptList.size(); i++) {
                String str = scriptList.get(i);
                while (!str.isEmpty() &&
                        (str.charAt(str.length() - 1) == 'ﾂ' ||
                                str.charAt(str.length() - 1) == '~')) {
                    i++;
                    if (i > scriptList.size()) break;
                    str = str.substring(0, str.length() - 1) + " " + scriptList.get(i);
                    scriptList.set(i - 1, "");
                    scriptList.set(i, str);
                }
            }
            object.wrapFlag = true;
        }

        try { //
label:
            while (line <= end) {
                /*String str=scriptList.get(line);
                while(str.length()>0 &&
                        (str.charAt(str.length()-1)=='ﾂ' ||
                        str.charAt(str.length()-1)=='~') )
                {
                    line++;
                    if(line>end) break;
                    str = str.substring(0,str.length()-1)+" "+scriptList.get(line);
                }
                int spacing1=0;
                while(spacing1<str.length() && (str.charAt(spacing1)==' ' || str.charAt(spacing1)=='　')) spacing1++;
                String[] words = str.substring(spacing1).split(" ");
                if(words.length==0) continue;*/
                ArrayList<String> sList;
                ArrayList<wordType> tList;
                if (object == null || scriptList != object.scriptList || object.stringList == null || object.stringList[line] == null) {
                    String str = scriptList.get(line);
                    /*while(str.length()>0 &&
                            (str.charAt(str.length()-1)=='ﾂ' ||
                            str.charAt(str.length()-1)=='~') )
                    {
                        line++;
                        if(line>end) break;
                        str = str.substring(0,str.length()-1)+" "+scriptList.get(line);
                    }*/
                    // Parse
                    sList = new ArrayList<>();
                    tList = new ArrayList<>();
                    fullresolution(str, sList, tList, memData.treeset, true);
                    if (object != null) {
                        object.stringList[line] = sList;
                        object.typeList[line] = tList;
                    }
                } else {
                    sList = object.stringList[line];
                    tList = object.typeList[line];
                }
                if (sList.isEmpty()) {
                    line++;
                    continue;
                }

                String str0 = sList.get(0);
                switch (str0) {
                    case "repeat" -> {
                        start = line;
                        line++;
                        int nest = 0;
                        while (line <= end) {
                            String str2 = scriptList.get(line);
                            int spacing = 0;
                            while (spacing < str2.length() && (str2.charAt(spacing) == ' ' || str2.charAt(spacing) == '　'))
                                spacing++;
                            String[] words2 = str2.substring(spacing).split(" ");
                            if (words2.length == 0) {
                                line++;
                                continue;
                            }
                            if (0 == words2[0].compareToIgnoreCase("repeat")) {
                                nest++;
                            }
                            if (0 == words2[0].compareToIgnoreCase("end") && 0 == words2[1].compareToIgnoreCase("repeat")) {
                                if (nest == 0) break;
                                nest--;
                            }
                            line++;
                        }
                        if (line > end + 1) throw new xTalkException("end repeat not found");

                        if (sList.size() <= 1 || sList.get(1).startsWith("--") || 0 == sList.get(1).compareToIgnoreCase("forever")) {
                            // repeat [forever]
                            while (true) {
                                result = doScript(object, target, message, object.scriptList, start + 1, line - 1, memData, parentparams, 0);
                                if (result.ret != 0)
                                    break;
                            }
                        } else if (0 == sList.get(1).compareToIgnoreCase("with")) {
                            // repeat with xxxx=x to xx / down to
                            int next;
                            boolean down = false;
                            String mem;
                            StringBuilder rstart = new StringBuilder();
                            StringBuilder rend = new StringBuilder();
                            if (sList.size() >= 3 && sList.get(2).contains("=")) {
                                String[] words2 = sList.get(2).split("=");
                                mem = words2[0];
                                if (words2.length >= 2) rstart = new StringBuilder(words2[1]);
                                next = 3;
                            } else if (sList.size() >= 4 && sList.get(3).contains("=")) {
                                mem = sList.get(2);
                                String[] words2 = sList.get(3).split("=");
                                if (words2.length >= 1) rstart = new StringBuilder(words2[1]);
                                next = 4;
                            } else {
                                throw new xTalkException("repeat with arguments are unknown");
                            }
                            while (true) {
                                if (next >= sList.size() || 0 == sList.get(next).compareToIgnoreCase("to"))
                                    break;
                                if (0 == sList.get(next).compareToIgnoreCase("down")) {
                                    next++;
                                    if (next < sList.size() && 0 == sList.get(next).compareToIgnoreCase("to")) {
                                        down = true;
                                        break;
                                    } else {
                                        throw new xTalkException("to is required after down");
                                    }
                                }
                                rstart.append(" ").append(sList.get(next));
                                next++;
                            }
                            next++;
                            while (true) {
                                if (next >= sList.size() || sList.get(next).startsWith("--"))
                                    break;
                                if (sList.get(next).contains(" ")) {
                                    rend.append(" \"").append(sList.get(next)).append("\"");
                                } else {
                                    rend.append(" ").append(sList.get(next));
                                }
                                next++;
                            }
                            String startStr = Evalution(rstart.toString(), memData, object, target);
                            setVariable(memData, mem, startStr);
                            int cnt = 0;
                            if (down == false) {
                                while (0 == Evalution(mem + "<=" + rend, memData, object, target).compareTo("true")) {
                                    result = doScript(object, target, message, scriptList, start + 1, line - 1, memData, parentparams, 0);
                                    if (result.ret != 0)
                                        break;
                                    cnt++;
                                    // System.out.println("cnt="+cnt);
                                    setVariable(memData, mem, Integer.toString(Integer.parseInt(startStr) + cnt));
                                }
                            } else {
                                while (0 == Evalution(mem + ">=" + rend, memData, object, target).compareTo("true")) {
                                    result = doScript(object, target, message, scriptList, start + 1, line - 1, memData, parentparams, 0);
                                    if (result.ret != 0)
                                        break;
                                    cnt++;
                                    setVariable(memData, mem, Integer.toString(Integer.parseInt(startStr) - cnt));
                                }
                            }
                        } else if (0 == sList.get(1).compareToIgnoreCase("while")) {
                            // repeat while
                            StringBuilder prm = new StringBuilder();
                            int next = 2;
                            while (true) {
                                if (next >= sList.size() || sList.get(next).startsWith("--"))
                                    break;
                                prm.append(" ").append(sList.get(next));
                                next++;
                            }
                            while (0 == Evalution(prm.toString(), memData, object, target).compareTo("true")) {
                                result = doScript(object, target, message, scriptList, start + 1, line - 1, memData, parentparams, 0);
                                if (result.ret != 0)
                                    break;
                            }
                        } else if (0 == sList.get(1).compareToIgnoreCase("until")) {
                            // repeat until
                            StringBuilder prm = new StringBuilder();
                            int next = 2;
                            while (true) {
                                if (next >= sList.size() || sList.get(next).startsWith("--"))
                                    break;
                                prm.append(" ").append(sList.get(next));
                                next++;
                            }
                            while (0 == Evalution(prm.toString(), memData, object, target).compareTo("false")) {
                                result = doScript(object, target, message, scriptList, start + 1, line - 1, memData, parentparams, 0);
                                if (result.ret != 0)
                                    break;
                            }
                        } else {
                            // repeat [for] x [times]
                            // repeat until
                            StringBuilder prm = new StringBuilder();
                            int next = 1;
                            if (next < sList.size() && 0 == sList.get(next).compareToIgnoreCase("for"))
                                next++;
                            while (true) {
                                if (next >= sList.size() || sList.get(next).startsWith("--") || 0 == sList.get(next).compareToIgnoreCase("times"))
                                    break;
                                prm.append(" ").append(sList.get(next));
                                next++;
                            }
                            int cnt = 0;
                            while (0 == Evalution(cnt + "<" + prm, memData, object, target).compareTo("true")) {
                                result = doScript(object, target, message, scriptList, start + 1, line - 1, memData, parentparams, 0);
                                if (result.ret != 0)
                                    break;
                                cnt++;
                            }
                        }
                        if (result.ret == -1 || result.ret == 1 || result.ret == 3)
                            break label;
                        result.ret = 0;
                    }
                    case "if" -> {
                        // if syntax
                        // ArrayList<String> stringList = new ArrayList<String>();
                        // ArrayList<wordType> typeList = new ArrayList<wordType>();
                        String nstr = "";

                        // Parse
                        // resolution(str, stringList, typeList,false);

                        int next = -1;
                        String prm = "";// String of if condition

                        int iftype = 0;
                        int iftype2 = 0;
                        int endi = 0;
                        for (int i = 1; i < sList.size(); i++, endi = i) {

                            if (0 == sList.get(i).compareToIgnoreCase("then")) {
                                if (i == sList.size() - 1) { // then at end of line
                                    iftype = 1;
                                    next = 0;
                                    break; // Multi-line then
                                } else {
                                    iftype = 2;
                                    next = i;
                                    break; // One-line then
                                }
                            }
                        }
                        prm = combine(1, endi, sList, tList);
                        ArrayList<String> nstringList = new ArrayList<>();
                        ArrayList<wordType> ntypeList = new ArrayList<>();
                        if (iftype == 0) {
                            iftype2 = 1;
                            // Find then on second line

                            if (line + 1 <= end) {
                                line++;
                                nstr = scriptList.get(line);
                            }
                            resolution(nstr, nstringList, ntypeList, false);

                            if (!nstringList.isEmpty() && 0 == nstringList.get(0).compareToIgnoreCase("then")) {
                                if (1 == nstringList.size()) { // then at end of line
                                    iftype = 1;
                                    next = 0; // Multi-line then (if on second line)

                                } else {
                                    iftype = 2;
                                    next = 0; // One-line then (if on second line)

                                }
                            } else throw new xTalkException("if statement requires then");
                        }
                        if (iftype2 == 1 && iftype == 2) {
                            sList = nstringList;
                            tList = ntypeList;
                        }
                    /*if(iftype2==2){
                        stringList = nstringList;
                        typeList = ntypeList;
                        line++;
                    }*/
                        // Whether if condition is met
                        boolean doif = false;
                        if (0 == Evalution(prm, memData, object, target).compareTo("true")) {
                            doif = true;
                        }
                        String doStr = "";
                        boolean thenthen = false;
                        int elseiftype = 0;
                        boolean doifever = false;
                        // Loop from here for consecutive else processing
                        while (true) {
                            // Execute then (else)
                            boolean else_works = false;
                            if (iftype == 2) {
                                // then is not at end of line
                                // ->Execute this line (to else)
                                next++;
                                int nstart = next;
                                while (true) {
                                    if (next >= sList.size()) { // Reached end of line
                                        String n2str = "";
                                        ArrayList<String> n2stringList = new ArrayList<>();
                                        ArrayList<wordType> n2typeList = new ArrayList<>();
                                        int n2next = 0;
                                        if (line + 1 <= end) {
                                            n2str = scriptList.get(line + 1);
                                        }
                                        resolution(n2str, n2stringList, n2typeList, false);
                                        if (n2stringList.size() > n2next && 0 == n2stringList.get(n2next).compareToIgnoreCase("else")) {
                                            // Next line starts with else
                                            if (sList.get(nstart).equalsIgnoreCase("if")) {
                                                int next2 = nstart;
                                                for (; next2 < sList.size(); next2++) {
                                                    if (sList.get(next2).equalsIgnoreCase("then"))
                                                        break;
                                                }
                                                prm = combine(nstart + 1, next2, sList, tList);
                                                if (0 == Evalution(prm, memData, object, target).compareTo("true")) {
                                                    doif = true;
                                                } else doif = false;
                                                doStr += combine(next2 + 1, next, sList, tList);
                                            } else {
                                                doStr += combine(nstart, next, sList, tList);
                                            }
                                            nstart = -1;
                                            line++;
                                            next = n2next;
                                            sList = n2stringList;
                                            tList = n2typeList;
                                            else_works = true;
                                            break;
                                        } else if (0 == sList.get(sList.size() - 1).compareToIgnoreCase("else") ||
                                                0 == sList.get(sList.size() - 1).compareToIgnoreCase("then")) {
                                            // End of this line is else or then
                                            iftype = 1;
                                            thenthen = true;
                                            break;
                                        } else {
                                            // At end of line, next line is not else so end
                                            break;
                                        }
                                    } else if (0 == sList.get(next).compareToIgnoreCase("else")) {
                                        // else in the middle of sentence
                                        else_works = true;
                                        if (sList.get(nstart).equalsIgnoreCase("if")) {
                                            int next2 = nstart + 1;
                                            while (next2 < sList.size() && !sList.get(next2).equalsIgnoreCase("then")) {
                                                next2++;
                                            }
                                            prm = combine(nstart + 1, next2, sList, tList);
                                            if (0 == Evalution(prm, memData, object, target).compareTo("true")) {
                                                doif = true;
                                            } else doif = false;
                                        }
                                        break;
                                    }
                                    // doStr += " "+stringList.get(next);
                                    next++;
                                }
                                if (thenthen) continue;
                                if (nstart != -1) {
                                    doStr += combine(nstart, next, sList, tList);
                                }
                                if (doif && doifever == false) {
                                    result = doScript(object, target, message, doStr, memData, parentparams, 1);
                                    if (result.ret != 0) {
                                        return result;
                                    }
                                    doifever = true;
                                }
                            } else if (iftype == 1) {
                                // then is at end of line
                                // ->Execute from next line to end if/else
                                if (iftype2 == 1) {
                                    // line++;
                                }
                                line++;
                                int nest = 0;
                                int ifdostart = line;
                                // System.out.println("===="+elseiftype);
                                while (line <= end) {
                                    String str2 = scriptList.get(line);
                                    int spacing = 0;
                                    while (spacing < str2.length() && (str2.charAt(spacing) == ' ' || str2.charAt(spacing) == '　'))
                                        spacing++;
                                    if (str2.lastIndexOf("\"") >= 0 && str2.lastIndexOf("\"") + 2 < str2.length()) {
                                        str2 = str2.substring(0, str2.lastIndexOf("\"") + 1) + " " + str2.substring(str2.lastIndexOf("\"") + 1);
                                    }
                                    String[] words2 = str2.substring(spacing).split(" ");
                                    if (words2.length < 1) {
                                        line++;
                                        continue;
                                    }
                                    if (words2[0].startsWith("--")) {
                                        line++;
                                        continue;
                                    }
                                    for (int k = 1; k < words2.length; k++) {
                                        if (words2[k].startsWith("--")) {
                                            words2[words2.length - 1] = words2[k - 1];
                                            break;
                                        }
                                    }
                                    if (0 == words2[0].compareToIgnoreCase("else") && elseiftype == 0) {
                                        String str3 = scriptList.get(line - 1);
                                        String[] words3 = str3.split(" ");
                                        for (int k = 1; k < words3.length; k++) {
                                            if (words3[k].startsWith("--")) {
                                                words3[words3.length - 1] = words3[k - 1];
                                                break;
                                            }
                                        }
                                        if (words3.length > 0 && 0 != words3[words3.length - 1].compareToIgnoreCase("then") &&
                                                scriptList.get(line - 1).contains(" then ")) {
                                            //
                                            // System.out.println(nest+"*"+str2);
                                            if (0 == words2[words2.length - 1].compareToIgnoreCase("else") ||
                                                    0 == words2[words2.length - 1].compareToIgnoreCase("then")) {
                                                // System.out.println(nest+">"+str2);
                                                nest++;
                                            }
                                        } else if (nest == 0) {
                                            // System.out.println("continues with else:"+str2);
                                            // Continues with else
                                            // next = 0;
                                        /*String n2str = "";
                                        ArrayList<String> n2stringList = new ArrayList<String>();
                                        ArrayList<wordType> n2typeList = new ArrayList<wordType>();
                                        if(ifdostart+1<=end){
                                            n2str=scriptList.get(ifdostart);
                                        }
                                        resolution(n2str, n2stringList, n2typeList,false);
                                        {
                                            if(n2stringList.size()>0 && n2stringList.get(0).equalsIgnoreCase("if")){
                                                int next2 = 1;
                                                for(;next2<n2stringList.size();next2++){
                                                    if(n2stringList.get(next2).equalsIgnoreCase("then"))
                                                        break;
                                                }
                                                prm = combine(1, next2, n2stringList, n2typeList);
                                                if(0==Evalution(prm,memData, object, target).compareTo("true")){
                                                    doif=true;
                                                }else doif = false;
                                            }
                                        }*/
                                        /*sList.clear();
                                        tList.clear();*/
                                            sList = new ArrayList<>();
                                            tList = new ArrayList<>();
                                            resolution(str2, sList, tList, false);
                                            else_works = true;
                                            break;
                                        }
                                    }
                                    if (0 == words2[words2.length - 1].compareToIgnoreCase("then") ||
                                            (0 != words2.length - 1) && 0 == words2[words2.length - 1].compareToIgnoreCase("else")) {
                                        if (0 == words2[0].compareToIgnoreCase("else") &&
                                                0 == words2[words2.length - 1].compareToIgnoreCase("then")) {
                                            // System.out.println(nest+" "+str2);
                                        } else {
                                            // System.out.println(nest+">"+str2);
                                            nest++;
                                        }
                                    } else if (0 == words2[0].compareToIgnoreCase("else") &&
                                            (0 != words2.length - 1) && 0 != words2[words2.length - 1].compareToIgnoreCase("then")) {
                                        if (scriptList.get(line - 1).contains(" then ")) {
                                            //
                                            // System.out.println(nest+"#"+str2);
                                        } else {
                                            if (nest == 0) {
                                                // System.out.println("ends with else"+str2);
                                                // Ends with else
                                            /*sList.clear();
                                            tList.clear();*/
                                                sList = new ArrayList<>();
                                                tList = new ArrayList<>();
                                                resolution(str2, sList, tList, false);
                                                else_works = true;
                                                break;
                                            }
                                            nest--;
                                            // System.out.println(nest+"<"+str2);
                                        }
                                    } else if (0 == words2[0].compareToIgnoreCase("end") && 0 == words2[1].compareToIgnoreCase("if")) {
                                        if (nest == 0) {
                                            // End with end if
                                            // System.out.println("ends with end if"+str2);
                                            break;
                                        }
                                        nest--;
                                        // System.out.println(nest+"<"+str2);
                                    }
                                    // else System.out.println(nest+" "+str2);

                                    // //doStr += "\n"+str2;

                                    line++;
                                    elseiftype = 0;
                                }
                                if (doif && doifever == false) {
                                    if (!doStr.isEmpty()) {
                                        result = doScript(object, target, message, doStr, memData, parentparams, 1);
                                        if (result.ret != 0) {
                                            return result;
                                        }
                                    }
                                    result = doScript(object, target, message, object.scriptList, ifdostart, line - 1, memData, parentparams, 0);
                                    if (result.ret != 0) {
                                        return result;
                                    }
                                    doifever = true;
                                }
                            }
                            if (else_works == false || line > end) {
                                break;
                            } else {
                                doStr = "";
                                // else judgment
                                if (next + 1 == sList.size()) { // else at end of line
                                    iftype = 1;
                                    next = 0;
//                                    System.out.println("iftype=1;next=0;");
                                    if (line + 1 <= end) {
                                        // line++;
                                        // str=scriptList.get(line);
                                    }
//                                    sList.clear();
//                                    tList.clear();
                                    sList = new ArrayList<>();
                                    tList = new ArrayList<>();
                                    resolution(scriptList.get(line), sList, tList, false);
                                    doif = true;
                                } else if (sList.get(next + 1).equalsIgnoreCase("if") &&
                                        sList.get(sList.size() - 1).equalsIgnoreCase("then")) { // else if ... to ... then
                                    iftype = 1;
//                                    System.out.println("iftype=1;");
                                    elseiftype = 1;
                                    prm = combine(next + 2, sList.size() - 1, sList, tList);
                                    doif = false;
                                    if (0 == Evalution(prm, memData, object, target).compareTo("true")) {
                                        doif = true;
                                    }
                                } else {
                                    iftype = 2;
//                                    System.out.println("iftype=2;");
                                    doif = true;
                                    // ####next++;
                                }
                                // doif = !doifever;
                            }
                        }
                    }
                    case "exit" -> {
                        if (sList.size() >= 2 && 0 == sList.get(1).compareToIgnoreCase("repeat")) {
                            result.ret = 2;
                            return result;
                        }
                        if (sList.size() >= 2 && 0 == sList.get(1).compareToIgnoreCase(message)) {
                            result.ret = 3;
                            return result;
                        }
                        if (sList.size() >= 2 && 0 == sList.get(1).compareToIgnoreCase("to")) {
                            if (sList.size() >= 3 && 0 == sList.get(2).compareToIgnoreCase("hypercard")) {
                                result.ret = -1;
                                return result;
                            }
                        }
                        throw new xTalkException("exit is unknown");
                    }
                    case "next" -> {
                        if (sList.size() >= 2 && 0 == sList.get(1).compareToIgnoreCase("repeat")) {
                            result.ret = 0;
                            return result;
                        }
                        throw new xTalkException("next is unknown");
                    }
                    case "pass" -> {
                        if (sList.size() >= 2 && 0 == sList.get(1).compareToIgnoreCase(message)) {
                            result.ret = 1;
                            return result;
                        }
                        throw new xTalkException("pass is unknown");
                    }
                    case "return" -> {
                        result.theResult = "";
                        if (sList.size() >= 2) {
                            StringBuilder retstr = new StringBuilder();
                            for (int j = 1; j < sList.size(); j++) {
                                retstr.append(sList.get(j)).append(" ");
                            }
                            result.theResult = Evalution(retstr.toString(), memData, object, target);
                        }
                        result.ret = 0;
                        return result;
                    }
                    case "end" -> throw new xTalkException("end is in an invalid position");
                    default -> {
                        // Execute normal statement
                        if (TTalk.stop) {
                            throw new xTalkException("user abort.");
                        }
                        if (TTalk.tracemode == 1 && object != null) {
                            ScriptEditor.openScriptEditor(PCARD.pc, object, line);
                        }
//                        if (object == null || scriptList != object.scriptList) {
//                            // System.out.println(">> "+str);
//                            result = doScriptLine(str, object, target, memData);
//                        } else {
//                            if (object.stringList == null || object.stringList[line] == null) {
//                                // Parse
//                                // object.stringList[line] = new ArrayList<String>();
//                                // object.typeList[line]  = new ArrayList<wordType>();
//                                // fullresolution(str, object.stringList[line], object.typeList[line], true);
//                            }
//                            // System.out.println(">> "+str);
//
//                            ArrayList<String> stringList = new ArrayList<String>(object.stringList[line]);
//
//                            wordType[] typeAry = new wordType[object.typeList[line].size()];
//                            for (int i = 0; i < object.typeList[line].size(); i++) { // Move from list to array
//                                typeAry[i] = object.typeList[line].get(i);
//                            }
//
//                            // Execute
//                            result = CommandExec(stringList, typeAry, object, target, memData);
//                        }


                        ArrayList<String> stringList = new ArrayList<>(sList);

                        wordType[] typeAry = new wordType[tList.size()];
                        tList.toArray(typeAry);
//                        wordType[] typeAry = new wordType[tList.size()];
//                        for (int i = 0; i < tList.size(); i++) { // Move from list to array
//                            typeAry[i] = tList.get(i);
//                        }

                        // Execute
                        result = CommandExec(stringList, typeAry, object, target, memData, line);
                        lastResult = result;
                    }
                }
                line++;
            }
        } catch (xTalkException e) {
            e.printStackTrace();

            // Processing when script error occurs
            String str = "";
            if (line < scriptList.size()) str = scriptList.get(line);
            while (str.length() > 1 && str.charAt(0) == ' ') str = str.substring(1);
            System.out.println("Error occurred at:" + str);

            if (mode == 1) { // Internal call so do not show dialog here, throw to upper caller
                throw new xTalkException(e.getMessage());
            }

            if (object != null && mode == 0) System.out.println("line " + line + " of " + object.getShortName());

            messageList.clear();

            lastErrorTime = System.currentTimeMillis();

            if (TTalk.stop) { // Force stop by cmd+. or stop by error
                throw new xTalkException(e.getMessage());
            }

            if (lockErrorDialogs) {
                throw new xTalkException(e.getMessage());
            }

            Toolkit.getDefaultToolkit().beep();

            if (object != null && object.scriptList != null && !object.scriptList.isEmpty()) {
                Object[] options = {"OK", "Open Script"};
                String msg = e.getMessage();
                int retValue = JOptionPane.showOptionDialog(PCARD.pc,
                        "Script Error: " + msg + "\n\n"
                                + "line " + line + " of " + object.getShortName(),
                        "Script Error",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE, null, options, options[0]);
                if (retValue == JOptionPane.YES_OPTION) {
                } else if (retValue == JOptionPane.NO_OPTION) {
                    ScriptEditor.openScriptEditor(PCARD.pc, object, line);
                }
            } else {
                Object[] options = {"OK"};
                JOptionPane.showOptionDialog(PCARD.pc,
                        "Script Error: ",
                        "Exit Options",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE, null, options, options[0]);
            }

            TTalk.stop = true; // Do not generate unnecessary dialogs

            throw new xTalkException(e.getMessage());
        }

        return result;
    }

    private static String combine(int start, int end, ArrayList<String> stringList, ArrayList<wordType> typeList) {
        StringBuilder str = new StringBuilder(32);

        for (int i = start; i < end; i++) {
            if (typeList.get(i) == wordType.STRING) {
                str.append("\"");
                str.append(stringList.get(i));
                str.append("\" ");
            } else {
                str.append(stringList.get(i));
                str.append(" ");
            }
        }

        return str.toString();
    }

    private static void resolution(String script, ArrayList<String> stringList, ArrayList<wordType> typeList, boolean isCmd) {
        ScriptArea.checkWordsLine(script, stringList, typeList, isCmd, false);
        for (int j = 0; j < typeList.size(); j++) {
            if (typeList.get(j) == TTalk.wordType.COMMENT) {
                typeList.remove(j);
                stringList.remove(j);
                break;
            }
        }
    }

    private static void fullresolution(String script, ArrayList<String> stringList, ArrayList<wordType> typeList, TreeSet<String> treeSet, boolean isCmd) {
        resolution(script, stringList, typeList, isCmd);

        StringBuilder handler = new StringBuilder();
        TreeSet<String> varSet;
        if (treeSet != null) varSet = treeSet;
        else varSet = new TreeSet<>();
        ScriptArea.checkWordsLine2(script, stringList, typeList, isCmd, handler, varSet);
    }

    // Parse script into word units
    /*private static void resolution(String script, ArrayList<String> stringList, ArrayList<wordType> typeList, boolean isCmd)
    {
        // long timestart = System.currentTimeMillis();
        StringBuilder str=new StringBuilder(16);

        for(int i=0; i<script.length(); i++) {
            char code = script.charAt(i);
            if(code=='+' || code=='-' || code=='*' || code=='/' || code=='^' || code=='&'
                || code=='=' || code=='<' || code=='>' || code=='≠' || code=='≤' || code=='≥') {
                if(code=='-' && i>0 && script.codePointAt(i-1)=='-')
                {
                    typeList.remove(typeList.size()-1);
                    stringList.remove(stringList.size()-1);
                    break;
                }
                if(str.length()>0) {
                    if(Character.isDigit(str.charAt(0))) typeList.add(wordType.STRING);
                    else typeList.add(wordType.X);
                    stringList.add(str.toString().toLowerCase().intern());
                    str.setLength(0);
                }
                typeList.add(wordType.OPERATOR);
                stringList.add(String.valueOf((char)code).intern());
            } else if(code=='(') {
                if(str.length()>0 || typeList.size()>0 && typeList.get(typeList.size()-1) == wordType.X) {
                    if(str.length()>0){
                        if(Character.isDigit(str.charAt(0))) typeList.add(wordType.STRING);
                        else typeList.add(wordType.X);
                        stringList.add(str.toString().toLowerCase().intern());
                        str.setLength(0);
                    }
                    String funcstr = stringList.get(stringList.size()-1);
                    if(funcstr=="cd" || funcstr=="card" ||
                            funcstr=="bg" || funcstr=="bkgnd" ||funcstr=="background" ||
                            funcstr=="btn" || funcstr=="button" ||
                            funcstr=="fld" || funcstr=="field" ||
                            funcstr=="stack"||
                            funcstr=="char" || funcstr=="character"||
                            funcstr=="item" || funcstr=="word" ||
                            funcstr=="line" ||
                            funcstr=="window" ||
                            funcstr=="id")
                    {
                        typeList.add(wordType.LBRACKET);
                    }
                    else if(funcstr=="of" && stringList.size() >= 2 && (
                            stringList.get(stringList.size()-2).matches("^[0-9]*$") ||
                            stringList.get(stringList.size()-2)=="char" ||
                            stringList.get(stringList.size()-2)=="character" ||
                            stringList.get(stringList.size()-2)=="item" ||
                            stringList.get(stringList.size()-2)=="word" ||
                            stringList.get(stringList.size()-2)=="line") )
                    {
                        typeList.add(wordType.LBRACKET);
                    }
                    else if(isCmd && typeList.size()==1)
                    {
                        // When left side is recognized as command
                        typeList.add(wordType.LBRACKET);
                    }
                    else {
                        typeList.add(wordType.LFUNC);
                    }
                    stringList.add("(");
                }
                else {
                    typeList.add(wordType.LBRACKET);
                    stringList.add("(");
                }
            } else if(code==')') {
                if(str.length()>0) {
                    if(Character.isDigit(str.charAt(0))) typeList.add(wordType.STRING);
                    else typeList.add(wordType.X);
                    stringList.add(str.toString().toLowerCase().intern());
                    str.setLength(0);
                }
                typeList.add(wordType.RBRACKET);
                stringList.add(")");
            } else if(code==',') {
                if(str.length()>0) {
                    if(Character.isDigit(str.charAt(0))) typeList.add(wordType.STRING);
                    else typeList.add(wordType.X);
                    stringList.add(str.toString().toLowerCase().intern());
                    str.setLength(0);
                }
                typeList.add(wordType.COMMA);
                stringList.add(",");
            } else if(code=='"') {
                if(str.length()>0) {
                    typeList.add(wordType.X);
                    stringList.add(str.toString().toLowerCase().intern());
                    str.setLength(0);
                }
                i++;
                while(i<script.length()) {
                    code = script.charAt(i);
                    if(code=='"') break;
                    str.append(code);
                    i++;
                }
                typeList.add(wordType.STRING);
                stringList.add(str.toString());
                str.setLength(0);
            } else if (code==' ' || code=='\t') {
                if(str.length()>0) {
                    if(Character.isDigit(str.charAt(0))) typeList.add(wordType.STRING);
                    else typeList.add(wordType.X);
                    stringList.add(str.toString().toLowerCase().intern());
                    str.setLength(0);
                }
            }
            else if(i==script.length()-1) {
                str.append(code);
                if(str.length()>0) {
                    if(Character.isDigit(str.charAt(0))) typeList.add(wordType.STRING);
                    else typeList.add(wordType.X);
                    stringList.add(str.toString().toLowerCase().intern());
                    str.setLength(0);
                }
            } else {
                str.append(code);
            }
        }
        // timeIsMoney("resolution:",timestart,18);
    }*/

    private Result doScriptLines(String script, OObject object, OObject target,
                                 MemoryData memData)
            throws xTalkException {
        Result ret = null;
        String[] scripts = script.split("\n");

        for (String s : scripts) {
            ret = doScriptLine(s, object, target, memData, 0);
        }

        return ret;
    }

    private Result doScriptLine(String script, OObject object, OObject target,
                                MemoryData memData, int startline)
            throws xTalkException {
        // long timestart = System.currentTimeMillis();
        ArrayList<String> stringList = new ArrayList<>();
        ArrayList<wordType> typeList = new ArrayList<>();

        // 1. Parse
        fullresolution(script, stringList, typeList, memData.treeset, true);

        wordType[] typeAry = new wordType[typeList.size()];
        for (int i = 0; i < typeList.size(); i++) {
            typeAry[i] = typeList.get(i);
        }

        // timeIsMoney("CommandExec:",timestart,19);
        // 2. Execute
        return CommandExec(stringList, typeAry, object, target, memData, startline);
    }

    @SuppressWarnings("unchecked")
    private Result CommandExec(
            ArrayList<String> stringList, wordType[] typeAry, OObject object, OObject target,
            MemoryData memData, int startline)
            throws xTalkException {
        // long timestart = System.currentTimeMillis();
        Result result = new Result();

        if (stringList.isEmpty()) return result;
        String str = stringList.get(0)/*.toLowerCase().intern()*/;
        if (stringList.size() == 1) {
            // System.out.println("do-"+str);
        } else {
            // System.out.println("do-"+str+stringList.get(1));
        }

        if (TTalk.stop) {
            throw new xTalkException("user abort.");
        }
        if (TTalk.tracemode >= 1 && startline > 0) {
            ScriptEditor.openScriptEditor(PCARD.pc, object, startline);
            boolean watcher = false;
            if (VariableWatcher.watcherWindow.isVisible()) {
                watcher = true;
                VariableWatcher.watcherWindow.setTable(TTalk.globalData, memData);
            }
            if (TTalk.tracemode == 2) {
                while (stepflag == false) {
                    try {
                        Thread.sleep(20);
                    } catch (InterruptedException e) {
                    }
                    if (watcher == false && VariableWatcher.watcherWindow.isVisible()) {
                        watcher = true;
                        VariableWatcher.watcherWindow.setTable(TTalk.globalData, memData);
                    }
                    if (!PCARD.pc.stack.scriptEditor.isVisible()) {
                        break;
                    }
                }
                stepflag = false;
            }
        }

        boolean inScript = true;
        if (commandSet.contains(str)) {
            // Whether handler is included from object to its parent
            inScript = false;
            OObject obj = object;
            while (obj != null && obj.handlerList != null) {
                if (obj.handlerList.contains(str)) {
                    inScript = true;
                    break;
                }
                obj = obj.parent;
            }
        }

        if (inScript) {
            // Handlers included in script
            int o = 0;
            for (int i = 1; i < stringList.size(); i++) {
                if (typeAry[i] == wordType.COMMA || i == stringList.size() - 1) {
                    o++;
                }
            }
            String[] paramAry = new String[o];
            o = 0;
            int j = 1;
            boolean flag = false;
            if (stringList.size() == 1) j = 0;
            else {
                int nest = 0;
                for (int i = 1; i <= stringList.size(); i++) {
                    if (i < stringList.size() && (typeAry[i] == wordType.LFUNC || typeAry[i] == wordType.LBRACKET)) {
                        nest++;
                    }
                    if (i < stringList.size() && (typeAry[i] == wordType.RBRACKET || typeAry[i] == wordType.RFUNC)) {
                        nest--;
                    }
                    if (i == stringList.size() || (typeAry[i] == wordType.COMMA && nest == 0)) {
                        if (j < typeAry.length && typeAry[j] == wordType.OPERATOR && 0 != stringList.get(j).compareTo("-")) {
                            // If an expression is entered in the msg box, do not execute it as a command
                            flag = true;
                            result.ret = 1;
                            break;
                        }
                        /*if(stringList.get(0).equals("Movie")){
                            System.out.println("param "+stringList.get(j)+" :"+j+" "+i);
                        }*/

                        paramAry[o] = Evalution(stringList, typeAry, j, i - 1, memData, object, target);
                        j = i + 1;
                        o++;
                    }
                }
            }
            // By default, send message to self
            // For background and stack, send message to own child card
            OObject sendObject = object;
            if (object != null && object.objectType.equals("background")) {
                sendObject = ((OBackground) object).viewCard;
                if (sendObject == null && ((OStack) object.parent).curCard.bg == object) {
                    sendObject = ((OStack) object.parent).curCard;
                }
            } else if (object != null && object.objectType.equals("stack")) {
                sendObject = ((OStack) object).curCard;
            }
            if (sendObject == null) sendObject = object;
            if (!flag) result = ReceiveMessage(stringList.get(0), paramAry, sendObject, target, memData);
            if (result.ret == 1) {
                // Evaluate value (for msg box)
                result.theResult = Evalution(stringList, typeAry, 0, stringList.size() - 1, memData, object, target);
                result.ret = 0;
            }
        }

        switch (str) {
            case "global" -> {
                for (int i = 1; i < stringList.size(); i++) {
                    if (stringList.get(i).equals(",")) continue;
                    setGlobalVariable(stringList.get(i), null);
                }
            }
            case "put" -> {
                if (0 == stringList.get(stringList.size() - 2).compareToIgnoreCase("into") && (0 == stringList.get(stringList.size() - 1).compareToIgnoreCase("message") || 0 == stringList.get(stringList.size() - 1).compareToIgnoreCase("msg"))) {
                    String value = Evalution(stringList, typeAry, 1, stringList.size() - 3, memData, object, target);
                    GMsg.setText(value);
                } else if (stringList.size() >= 2) {
                    int next = 2;
                    String dirStr = "";
                    while (next < stringList.size()) {
                        dirStr = stringList.get(next);
                        if (0 == dirStr.compareToIgnoreCase("into") ||
                                0 == dirStr.compareToIgnoreCase("before") ||
                                0 == dirStr.compareToIgnoreCase("after")) {
                            break;
                        }
                        next++;
                    }
                    if (next + 1 >= stringList.size()) {
                        // put xx
                        String value = Evalution(stringList, typeAry, 1, stringList.size() - 1, memData, object, target);
                        GMsg.setText(value);
                    } else if (0 == dirStr.compareToIgnoreCase("after")) {
                        StringBuilder doStr = new StringBuilder();
                        for (int k = 1; k < next; k++) {
                            if (typeAry[k] == wordType.STRING)
                                doStr.append(" \"").append(stringList.get(k)).append("\"");
                            else doStr.append(" ").append(stringList.get(k));
                        }
                        StringBuilder toStr = new StringBuilder();
                        for (int k = next + 1; k < stringList.size(); k++) {
                            if (typeAry[k] == wordType.STRING)
                                toStr.append(" \"").append(stringList.get(k)).append("\"");
                            else toStr.append(" ").append(stringList.get(k));
                        }
                        doStr = new StringBuilder("put " + toStr + " & " + doStr + " into" + toStr);
                        doScript(object, target, "", doStr.toString(), memData, null, 1);
                    } else if (0 == dirStr.compareToIgnoreCase("before")) {
                        StringBuilder doStr = new StringBuilder();
                        for (int k = 1; k < next; k++) {
                            if (typeAry[k] == wordType.STRING)
                                doStr.append(" \"").append(stringList.get(k)).append("\"");
                            else doStr.append(" ").append(stringList.get(k));
                        }
                        StringBuilder toStr = new StringBuilder();
                        for (int k = next + 1; k < stringList.size(); k++) {
                            if (typeAry[k] == wordType.STRING)
                                toStr.append(" \"").append(stringList.get(k)).append("\"");
                            else toStr.append(" ").append(stringList.get(k));
                        }
                        doStr = new StringBuilder("put " + doStr + " & " + toStr + " into" + toStr);
                        doScript(object, target, "", doStr.toString(), memData, null, 1);
                    } else { // put into
                        if (next + 2 < stringList.size() && (
                                0 == stringList.get(next + 1).compareToIgnoreCase("char") || 0 == stringList.get(next + 1).compareToIgnoreCase("character")
                                        || 0 == stringList.get(next + 1).compareToIgnoreCase("item") || 0 == stringList.get(next + 1).compareToIgnoreCase("word")
                                        || 0 == stringList.get(next + 1).compareToIgnoreCase("line") || 0 == stringList.get(next + 1).compareToIgnoreCase("last")
                                        || 0 == stringList.get(next + 1).compareToIgnoreCase("first"))) {
                            String value = Evalution(stringList, typeAry, 1, next - 1, memData, object, target);
                            if (0 == stringList.get(next + 1).compareToIgnoreCase("last") || 0 == stringList.get(next + 1).compareToIgnoreCase("first")) {
                                stringList.set(next + 1, stringList.get(next + 2));
                                // Will be overwritten so make a copy
                                ArrayList<String> cpstringList = (ArrayList<String>) stringList.clone();
                                wordType[] cptypeAry = typeAry.clone();
                                String value1 = Evalution(cpstringList, cptypeAry, next + 4, cpstringList.size() - 1, memData, object, target);
                                stringList.set(next + 2, Integer.toString(getNumberOfChunk(cpstringList.get(next + 2) + "s", value1)));
                            }
                            int next2 = next + 1;
                            int nest = 0;
                            while (next2 < stringList.size()) {
                                if (nest == 0 && 0 == stringList.get(next2).compareToIgnoreCase("of")) {
                                    break;
                                }
                                if (typeAry[next2] == wordType.LBRACKET || typeAry[next2] == wordType.LFUNC) {
                                    nest++;
                                }
                                if (typeAry[next2] == wordType.RBRACKET || typeAry[next2] == wordType.RFUNC) {
                                    nest--;
                                }

                                next2++;
                            }
                            String chunkStart = "";
                            String chunkEnd = null;
                            int next3 = next;
                            while (next3 < next2) {
                                if (0 == stringList.get(next3).compareToIgnoreCase("to")) {
                                    chunkEnd = Evalution(stringList, typeAry, next3 + 1, next2 - 1, memData, object, target);
                                    break;
                                }
                                next3++;
                            }
                            chunkStart = Evalution(stringList, typeAry, next + 2, next3 - 1, memData, object, target);

                            if (next2 + 1 < stringList.size() && (
                                    0 == stringList.get(next2 + 1).compareToIgnoreCase("item") || 0 == stringList.get(next2 + 1).compareToIgnoreCase("word")
                                            || 0 == stringList.get(next2 + 1).compareToIgnoreCase("line"))) {
                                int next4 = next2 + 1;
                                while (next4 < stringList.size()) {
                                    if (0 == stringList.get(next4).compareToIgnoreCase("of"))
                                        break;
                                    next4++;
                                }
                                String chunkStart2 = "";
                                String chunkEnd2 = null;
                                int next5 = next2;
                                while (next5 < next4) {
                                    if (0 == stringList.get(next5).compareToIgnoreCase("to")) {
                                        chunkEnd2 = Evalution(stringList, typeAry, next5 + 1, next4 - 1, memData, object, target);
                                        break;
                                    }
                                    next5++;
                                }
                                chunkStart2 = Evalution(stringList, typeAry, next2 + 2, next5 - 1, memData, object, target);
                                OObject obj = null;
                                try {
                                    obj = getObject(stringList, typeAry, next4 + 1, stringList.size() - 1, memData, object, target);
                                } catch (Exception e) {
                                }
                                if (obj != null) {
                                    setChunkObject2(stringList.get(next + 1), chunkStart, chunkEnd, stringList.get(next2 + 1), chunkStart2, chunkEnd2, value, obj);
                                } else {
                                    setChunkVariable2(stringList.get(next + 1), chunkStart, chunkEnd, stringList.get(next2 + 1), chunkStart2, chunkEnd2, value, stringList.get(next4 + 1), memData);
                                }
                            } else {
                                OObject obj = null;
                                try {
                                    obj = getObject(stringList, typeAry, next2 + 1, stringList.size() - 1, memData, object, target);
                                } catch (Exception e) {
                                }
                                if (obj != null) {
                                    setChunkObject(stringList.get(next + 1), chunkStart, chunkEnd, value, obj);
                                } else {
                                    setChunkVariable(stringList.get(next + 1), chunkStart, chunkEnd, value, stringList.get(next2 + 1), memData);
                                }
                            }
                        } else {
                            ArrayList<String> stL = new ArrayList<>(stringList);
                            wordType[] tyA = new wordType[typeAry.length];
                            System.arraycopy(typeAry, 0, tyA, 0, typeAry.length);

                            OObject obj;
                            if (stringList.get(next + 1).equalsIgnoreCase("the") && next + 2 < stringList.size()) {
                                String str1 = "the " + stringList.get(next + 2);
                                String res = Evalution(str1, memData, object, target);

                            /*ArrayList<String> tmpStrList = new ArrayList<String>();
                            ArrayList<wordType> newTypeList = new ArrayList<wordType>();
                            resolution(res, tmpStrList, newTypeList, false);
                            wordType[] tmpTypeAry = new wordType[newTypeList.size()];
                            for(int i=0; i<tmpTypeAry.length; i++){
                                tmpTypeAry[i] = newTypeList.get(i);
                            }*/

                                String newStr = "put empty into " + res;
                                return doScriptLine(newStr, object, target, memData, 0);
                            } else {
                                obj = getObject(stL, tyA, next + 1, stringList.size() - 1, memData, object, target);
                            }
                            if (obj != null) {
                                String value = Evalution(stringList, typeAry, 1, next - 1, memData, object, target);
                                TUtil.SetProperty(obj, "text", value);
                            } else {
                                // Variable
                                String value = Evalution(stringList, typeAry, 1, stringList.size() - 3, memData, object, target);
                                setVariable(memData, stringList.get(stringList.size() - 1), value);
                            }
                        }
                    }
                } else {
                    //
                }
            }
            case "set" -> {
                // set [the] xx [of xx xx] to xx xx
                String property = "";
                OObject obj = null;
                int next = 1;
                if (next >= stringList.size()) throw new xTalkException("set is unknown");
                if (0 == stringList.get(next).compareToIgnoreCase("the"))
                    next++;
                if (next >= stringList.size()) throw new xTalkException("set is unknown");
                property = stringList.get(next);
                next++;
                if (next >= stringList.size()) throw new xTalkException("set is unknown");
                if (0 == stringList.get(next).compareToIgnoreCase("of")) {
                    next++;
                    if (next >= stringList.size()) throw new xTalkException("set is unknown");
                    int end = next;
                    while (end < stringList.size()) {
                        if (0 == stringList.get(end).compareToIgnoreCase("to"))
                            break;
                        end++;
                    }
                    obj = getObject(stringList, typeAry, next, end - 1, memData, object, target);
                    if (obj == null) {
                        throw new xTalkException("object to change with set is unknown");
                    }
                    next = end;
                }
                if (0 == stringList.get(next).compareToIgnoreCase("to")) {
                    StringBuilder value = new StringBuilder();
                    StringBuilder param = new StringBuilder();
                    next++;
                    while (next < stringList.size()) {
                        if (stringList.get(next).equals(",") && typeAry[next] == wordType.COMMA) {
                            if (property.equalsIgnoreCase("topleft") || property.equalsIgnoreCase("scroll") ||
                                    property.equalsIgnoreCase("loc") || property.equalsIgnoreCase("location") ||
                                    property.equalsIgnoreCase("rect") || property.equalsIgnoreCase("rectangle")) {
                                // For loc, rect, split by comma
                                param.append(Evalution(value.toString(), memData, object, target)).append(",");
                                value = new StringBuilder();
                                next++;
                                continue;
                            }
                        }
                        if (typeAry[next] == wordType.STRING) {
                            value.append("\"").append(stringList.get(next)).append("\" ");
                        } else {
                            value.append(stringList.get(next)).append(" ");
                        }
                        next++;
                    }
                    param.append(Evalution(value.toString(), memData, object, target));
                    TUtil.SetProperty(obj, property, param.toString());
                }
            }
            case "go" -> {
                int next = 1;
                if (next >= stringList.size()) throw new xTalkException("go is unknown");
                if (0 == stringList.get(next).compareToIgnoreCase("to"))
                    next++;
                int id = 0;
                if (0 == stringList.get(next).compareToIgnoreCase("next")) {
                    int i;
                    for (i = 0; i < PCARD.pc.stack.cardIdList.size(); i++) {
                        if (PCARD.pc.stack.curCard != null && PCARD.pc.stack.curCard.id == PCARD.pc.stack.cardIdList.get(i))
                            break;
                    }
                    if (i == PCARD.pc.stack.cardIdList.size()) i = 0;
                    else {
                        i++;
                        if (i >= PCARD.pc.stack.cardIdList.size()) i = 0;
                    }
                    id = PCARD.pc.stack.cardIdList.get(i);
                } else if (0 == stringList.get(next).compareToIgnoreCase("prev") || 0 == stringList.get(next).compareToIgnoreCase("previous")) {
                    int i;
                    for (i = 0; i < PCARD.pc.stack.cardIdList.size(); i++) {
                        if (PCARD.pc.stack.curCard.id == PCARD.pc.stack.cardIdList.get(i))
                            break;
                    }
                    if (i == 0) i = PCARD.pc.stack.cardIdList.size() - 1;
                    else i--;
                    id = PCARD.pc.stack.cardIdList.get(i);
                } else if (0 == stringList.get(next).compareToIgnoreCase("first")) {
                    id = PCARD.pc.stack.cardIdList.get(0);
                } else if (0 == stringList.get(next).compareToIgnoreCase("last")) {
                    id = PCARD.pc.stack.cardIdList.get(PCARD.pc.stack.cardIdList.size() - 1);
                } else if (0 == stringList.get(next).compareToIgnoreCase("home")) {
                    if (PCARD.pc.tool != null) {
                        PCARD.pc.tool.end();
                        PCARD.pc.tool = null;
                        PaintTool.saveCdPictures();
                        {
                            // Paint buffer
                            PCARD.pc.mainImg = null;
                            PCARD.pc.bgImg = null;
                            PCARD.pc.undoBuf = null;
                            PCARD.pc.redoBuf = null;
                        }
                        lockMessages = true;
                    }

                    if (!lockMessages) {
                        talk.ReceiveMessage("closeCard", "", PCARD.pc.stack.curCard, null, false);
                        if (PCARD.pc.stack != null && PCARD.pc.stack.curCard != null &&
                                PCARD.pc.stack.curCard.bg != null) {
                            talk.ReceiveMessage("closeBackground", "", PCARD.pc.stack.curCard.bg, null, false);
                        }
                        talk.ReceiveMessage("closeStack", "", PCARD.pc.stack, null, false);
                    }

                    if (PCARD.pc.stack.curCard != null) {
                        PCARD.pc.stack.curCard.removeData();
                        if (PCARD.pc.stack.curCard.bg != null) PCARD.pc.stack.curCard.bg.removeData();
                    }
                    PCARD.pc.stack.clean();
                    PCARD.pc.emptyWindow();
                    result.ret = -1;
                    return result;
                } else if (0 == stringList.get(next).compareToIgnoreCase("cd") || 0 == stringList.get(next).compareToIgnoreCase("card") ||
                        next + 1 < stringList.size() && (0 == stringList.get(next + 1).compareToIgnoreCase("cd") || 0 == stringList.get(next + 1).compareToIgnoreCase("card"))) {
                    OObject obj = getObject(stringList, typeAry, next, stringList.size() - 1, memData, object, target);
                    if (obj != null) id = obj.id;
                }
                if (id == 0) {
                    result.theResult = "card not found";
                    return result;
                }

                // ##### Is it OK to do this?
                messageList.clear();

                // Cleanup after paint
                if (PCARD.pc.tool != null) {
                    PCARD.pc.tool.end();
                    PaintTool.saveCdPictures();
                    {
                        // Paint buffer
                        PCARD.pc.mainImg = null;
                        PCARD.pc.bgImg = null;
                        PCARD.pc.undoBuf = null;
                        PCARD.pc.redoBuf = null;
                    }
                    lockMessages = true;
                }

                if (!lockMessages) {
                    talk.ReceiveMessage("closeCard", new String[0], PCARD.pc.stack.curCard, PCARD.pc.stack.curCard, null);
                }
                OCard cd = OCard.getOCard(PCARD.pc.stack, id, true);// Read only data

                if (!lockMessages) {
                    if (cd.bgid != PCARD.pc.stack.curCard.bgid) {
                        talk.ReceiveMessage("closeBackground", new String[0], PCARD.pc.stack.curCard, PCARD.pc.stack.curCard, null);
                    }
                }
                boolean saveLockedScreen = PCARD.lockedScreen;
                if (!PCARD.lockedScreen) {
                    if (PCARD.visual > 0) VEffect.setOldOff();
                    // Prevent screen from being rewritten on main thread
                    PCARD.lockedScreen = true;
                    fastLockScreen = 0;
                }
                PCARD.pc.stack.curCard.removeData();
                if (PCARD.pc.stack.curCard.bg != null) PCARD.pc.stack.curCard.bg.removeData();
                PCARD.pc.stack.curCard.bg = null;
                for (int i = PCARD.pc.mainPane.getComponentCount() - 1; i >= 0; i--) {
                    // PCARD.pc.mainPane.remove(PCARD.pc.mainPane.getComponent(i));
                }
                cd = OCard.getOCard(PCARD.pc.stack, id, false);// Read both components and pictures

                cd.bg = OBackground.getOBackground(PCARD.pc.stack, cd, cd.bgid, false);// new OBackground(PCARD.pc.stack, cd, cd.bgid, false);

                cd.parent = cd.bg;
                if (PCARD.pc.stack.curCard.label != null) {
                    PCARD.pc.mainPane.remove(PCARD.pc.stack.curCard.label);
                    PCARD.pc.stack.curCard.label = null;
                    PCARD.pc.stack.curCard.pict = null;
                }
                PCARD.pc.stack.curCard = cd;
                if (!saveLockedScreen) {
                    VEffect.visualEffect(PCARD.visual, PCARD.toVisual, PCARD.visSpd);
                    PCARD.visual = 0;
                    PCARD.toVisual = 0;
                    PCARD.visSpd = 3;
                }
                if (!lockMessages) {
                    if (cd.bgid != PCARD.pc.stack.curCard.bgid) {
                        talk.ReceiveMessage("openBackground", new String[0], PCARD.pc.stack.curCard, PCARD.pc.stack.curCard, null);
                    }
                    talk.ReceiveMessage("openCard", new String[0], PCARD.pc.stack.curCard, PCARD.pc.stack.curCard, null);
                }

                if (PCARD.pc.tool != null) {
                    // Start paint
                    String toolName = PCARD.pc.tool.getName();
                    PCARD.pc.tool = null;
                    TBButtonListener.ChangeTool(toolName, null);
                } else if (AuthTool.tool != null) {
                    // Start authoring
                    if (AuthTool.tool.getClass() == ButtonTool.class) {
                        TBButtonListener.ChangeTool("button", null);
                    } else if (AuthTool.tool.getClass() == FieldTool.class) {
                        TBButtonListener.ChangeTool("field", null);
                    }
                }
                // System.out.println("curCard:"+PCARD.pc.stack.curCard.getShortName());
            }
            case "visual" -> {
                int next = 1;
                if (next >= stringList.size()) throw new xTalkException("visual is unknown");
                if (0 == stringList.get(next).compareToIgnoreCase("effect"))
                    next++;
                int mode;
                mode = TUtil.getVisualMode(stringList, next, stringList.size() - 1);
                PCARD.visual = mode & 0xFF;
                PCARD.toVisual = (mode >> 8) & 0xFF;
                PCARD.visSpd = (mode >> 16) & 0xFF;
            }
            case "lock" -> {
                int next = 1;
                if (next >= stringList.size()) throw new xTalkException("lock is unknown");
                if (0 == stringList.get(next).compareToIgnoreCase("screen")) {
                    if (fastLockScreen != object.id) {
                        VEffect.setOldOff();
                    }
                    PCARD.lockedScreen = true;
                } else if (0 == stringList.get(next).compareToIgnoreCase("messages")) {
                    lockMessages = true;
                }
            }
            case "unlock" -> {
                int next = 1;
                if (next >= stringList.size()) throw new xTalkException("unlock is unknown");
                if (0 == stringList.get(next).compareToIgnoreCase("screen")) {
                    int vis = 0, tovis = 0, spd = 3;
                    next++;
                    if (next < stringList.size() && 0 == stringList.get(next).compareToIgnoreCase("with")) {
                        next++;
                        if (next < stringList.size() && 0 == stringList.get(next).compareToIgnoreCase("visual")) {
                            next++;
                            if (next < stringList.size() && 0 == stringList.get(next).compareToIgnoreCase("effect"))
                                next++;
                        }
                        if (next < stringList.size()) {
                            int mode = TUtil.getVisualMode(stringList, next, stringList.size() - 1);
                            vis = mode & 0xFF;
                            tovis = (mode >> 8) & 0xFF;
                            spd = (mode >> 16) & 0xFF;
                        }
                    }
                    PCARD.lockedScreen = false;
                    if (vis == 0) fastLockScreen = object.id;
                    VEffect.visualEffect(vis, tovis, spd);
                } else if (0 == stringList.get(next).compareToIgnoreCase("messages")) {
                    lockMessages = false;
                }
            }
            case "answer" -> {
                // answer
                int next = 1;
                if (next >= stringList.size()) throw new xTalkException("answer is unknown");
                if (stringList.get(next).equals("file")) {
                    // File open dialog
                    next++;
                    int j;
                    for (j = next; next < stringList.size() - 1; j++) {
                        if (stringList.get(j).equals("of") && stringList.get(j + 1).equals("type")) {
                            break;
                        }
                    }
                    String answerStr = Evalution(stringList, typeAry, next, j - 1, memData, object, target);
                    String type = null;
                    if (j + 2 < stringList.size()) {
                        type = Evalution(stringList, typeAry, j + 2, stringList.size() - 1, memData, object, target);
                    }
                    JFileChooser chooser = new JFileChooser();
                    if (type != null) {
                        class AnswerFileFilter extends FileFilter {

                            final String type;
                            String type2;

                            AnswerFileFilter(String type) {
                                this.type = type;
                                if (type.equals("TEXT")) type2 = "txt";
                                if (type.equals("PICT")) type2 = "png";
                                if (type.equals("STAK")) type2 = "xml";
                            }

                            @Override
                            public boolean accept(File f) {
                                if (f.isDirectory()) {
                                    return false;
                                }

                                String ext = getExtension(f);
                                if (ext == null || ext.equalsIgnoreCase(type) || ext.equalsIgnoreCase(type2)) {
                                    return true;
                                }
                                return false;
                            }

                            @Override
                            public String getDescription() {
                                return type + "file";
                            }

                            private static String getExtension(File f) {
                                String ext = null;
                                String filename = f.getName();
                                int dotIndex = filename.lastIndexOf('.');

                                if ((dotIndex > 0) && (dotIndex < filename.length() - 1)) {
                                    ext = filename.substring(dotIndex + 1).toLowerCase();
                                }

                                return ext;
                            }
                        }
                        chooser.addChoosableFileFilter(new AnswerFileFilter(type));
                    }
                    if (PCARD.pc.stack != null && PCARD.pc.stack.file != null) {
                        chooser.setCurrentDirectory(PCARD.pc.stack.file.getParent().getParent().toFile());
                    }
                    chooser.setDialogTitle(answerStr);
                    chooser.setDialogType(JFileChooser.OPEN_DIALOG);
                    chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                    int selected = chooser.showOpenDialog(PCARD.pc);
                    if (selected == JFileChooser.APPROVE_OPTION) {
                        String path = chooser.getSelectedFile().getPath();

                        setVariable(memData, "it", path);
                    }
                } else {
                    // Show answer dialog box
                    String answerStr = "";
                    String btn1 = "OK";
                    String btn2 = null;
                    String btn3 = null;
                    int i;
                    for (i = next + 1; i < stringList.size(); i++) {
                        if (0 == stringList.get(i).compareToIgnoreCase("with")) break;
                    }
                    answerStr = Evalution(stringList, typeAry, next, i - 1, memData, object, target);
                    next = i;
                    if (next < stringList.size() && 0 == stringList.get(next).compareToIgnoreCase("with")) {
                        next++;
                        if (next < stringList.size()) btn1 = stringList.get(next);
                        next++;
                        if (next + 1 < stringList.size() && 0 == stringList.get(next).compareToIgnoreCase("or")) {
                            btn2 = btn1;
                            next++;
                            btn1 = stringList.get(next);
                            next++;
                        }
                        if (next + 1 < stringList.size() && 0 == stringList.get(next).compareToIgnoreCase("or")) {
                            btn3 = btn2;
                            btn2 = btn1;
                            next++;
                            btn1 = stringList.get(next);
                        }
                    }
                    new GDialog(PCARD.pc, answerStr, null, btn3, btn2, btn1);
                    setVariable(memData, "it", GDialog.clicked);
                }
            }
            case "add" -> {
                int next = 1;
                while (next < stringList.size()) {
                    if (0 == stringList.get(next).compareToIgnoreCase("to"))
                        break;
                    next++;
                }
                if (next >= stringList.size()) throw new xTalkException("add is unknown");
                StringBuilder newStr = new StringBuilder();
                newStr.append("put");
                for (int i = next + 1; i < stringList.size(); i++) {
                    if (typeAry[i] == wordType.STRING) {
                        newStr.append(" \"").append(stringList.get(i)).append("\"");
                    } else {
                        newStr.append(" ").append(stringList.get(i));
                    }
                }
                newStr.append(" +");
                for (int i = 1; i < next; i++) {
                    if (typeAry[i] == wordType.STRING) {
                        newStr.append(" \"").append(stringList.get(i)).append("\"");
                    } else {
                        newStr.append(" ").append(stringList.get(i));
                    }
                }
                newStr.append(" into");
                for (int i = next + 1; i < stringList.size(); i++) {
                    if (typeAry[i] == wordType.STRING) {
                        newStr.append(" \"").append(stringList.get(i)).append("\"");
                    } else {
                        newStr.append(" ").append(stringList.get(i));
                    }
                }
                // doScript(object, target, "", newStr, memData,1);
                doScriptLine(newStr.toString(), object, target, memData, 0);
            }
            case "subtract" -> {
                int next = 1;
                while (next < stringList.size()) {
                    if (0 == stringList.get(next).compareToIgnoreCase("from"))
                        break;
                    next++;
                }
                if (next >= stringList.size()) throw new xTalkException("subtract is unknown");
                StringBuilder newStr = new StringBuilder();
                newStr.append("put");
                for (int i = next + 1; i < stringList.size(); i++) {
                    if (typeAry[i] == wordType.STRING) {
                        newStr.append(" \"").append(stringList.get(i)).append("\"");
                    } else {
                        newStr.append(" ").append(stringList.get(i));
                    }
                }
                newStr.append(" -");
                for (int i = 1; i < next; i++) {
                    if (typeAry[i] == wordType.STRING) {
                        newStr.append(" \"").append(stringList.get(i)).append("\"");
                    } else {
                        newStr.append(" ").append(stringList.get(i));
                    }
                }
                newStr.append(" into");
                for (int i = next + 1; i < stringList.size(); i++) {
                    if (typeAry[i] == wordType.STRING) {
                        newStr.append(" \"").append(stringList.get(i)).append("\"");
                    } else {
                        newStr.append(" ").append(stringList.get(i));
                    }
                }
                // doScript(object, target, "", newStr, memData,1);
                doScriptLine(newStr.toString(), object, target, memData, 0);
            }
            case "multiply" -> {
                int next = 1;
                while (next < stringList.size()) {
                    if (0 == stringList.get(next).compareToIgnoreCase("by"))
                        break;
                    next++;
                }
                if (next >= stringList.size()) throw new xTalkException("multiply is unknown");
                StringBuilder newStr = new StringBuilder();
                newStr.append("put");
                for (int i = next + 1; i < stringList.size(); i++) {
                    if (typeAry[i] == wordType.STRING) {
                        newStr.append(" \"").append(stringList.get(i)).append("\"");
                    } else {
                        newStr.append(" ").append(stringList.get(i));
                    }
                }
                newStr.append(" *");
                for (int i = 1; i < next; i++) {
                    if (typeAry[i] == wordType.STRING) {
                        newStr.append(" \"").append(stringList.get(i)).append("\"");
                    } else {
                        newStr.append(" ").append(stringList.get(i));
                    }
                }
                newStr.append(" into");
                for (int i = 1; i < next; i++) {
                    if (typeAry[i] == wordType.STRING) {
                        newStr.append(" \"").append(stringList.get(i)).append("\"");
                    } else {
                        newStr.append(" ").append(stringList.get(i));
                    }
                }
                // doScript(object, target, "", newStr, memData,1);
                doScriptLine(newStr.toString(), object, target, memData, 0);
            }
            case "divide" -> {
                int next = 1;
                while (next < stringList.size()) {
                    if (0 == stringList.get(next).compareToIgnoreCase("by"))
                        break;
                    next++;
                }
                if (next >= stringList.size()) throw new xTalkException("divide is unknown");
                StringBuilder newStr = new StringBuilder();
                newStr.append("put");
                for (int i = next + 1; i < stringList.size(); i++) {
                    if (typeAry[i] == wordType.STRING) {
                        newStr.append(" \"").append(stringList.get(i)).append("\"");
                    } else {
                        newStr.append(" ").append(stringList.get(i));
                    }
                }
                newStr.append(" /");
                for (int i = 1; i < next; i++) {
                    if (typeAry[i] == wordType.STRING) {
                        newStr.append(" \"").append(stringList.get(i)).append("\"");
                    } else {
                        newStr.append(" ").append(stringList.get(i));
                    }
                }
                newStr.append(" into");
                for (int i = 1; i < next; i++) {
                    if (typeAry[i] == wordType.STRING) {
                        newStr.append(" \"").append(stringList.get(i)).append("\"");
                    } else {
                        newStr.append(" ").append(stringList.get(i));
                    }
                }
                // doScript(object, target, "", newStr, memData,1);
                doScriptLine(newStr.toString(), object, target, memData, 0);
            }
            case "get" -> {
                int next = 1;
                if (next >= stringList.size()) throw new xTalkException("get is unknown");
                // Variable it
                String value = Evalution(stringList, typeAry, next, stringList.size() - 1, memData, object, target);
                setVariable(memData, "it", value);
            /*String newStr="";
            newStr += "put";
            for(int i=next; i<stringList.size(); i++){
                newStr += " "+stringList.get(i);
            }
            newStr += " into it";
            doScript(object, target, "", newStr, memData);*/
            }
            case "show" -> {
                StringBuilder objStr = new StringBuilder();
                int next = 1;
                if (next >= stringList.size()) throw new xTalkException("show is unknown");
                if (stringList.get(next).equalsIgnoreCase("all") &&
                        next + 1 < stringList.size() &&
                        (stringList.get(next + 1).equalsIgnoreCase("cds") || stringList.get(next + 1).equalsIgnoreCase("cards"))) {
                    // show all cds
                    StringBuilder innerScript = new StringBuilder();
                    if (!lockMessages) {
                        innerScript.append("lock messages\n");
                    }
                    for (int i = 0; i < PCARD.pc.stack.cardIdList.size(); i++) {
                        innerScript.append("go cd ").append(i + 1).append("\n");
                        if (!PCARD.lockedScreen) innerScript.append("wait 1 tick\n");
                    }
                    innerScript.append("go cd 1\n");
                    innerScript.append("unlock messages\n");
                    // if(!lockMessages){
                    // doScript(object, target, "", innerScript, memData,1);
                    doScriptLines(innerScript.toString(), object, target, memData);
                    // }
                } else if (stringList.get(next).equalsIgnoreCase("groups")) {
                    // Underline group selection style of field
                    // TODO:
                } else {
                    while (next < stringList.size()) {
                        if (0 == stringList.get(next).compareToIgnoreCase("at")) {
                            StringBuilder newStr = new StringBuilder();
                            newStr.append("set loc of");
                            for (int i = 1; i < next; i++) {
                                objStr.append(" ").append(stringList.get(i));
                            }
                            newStr.append(objStr).append(" to ");
                            for (int i = next + 1; i < stringList.size(); i++) {
                                if (typeAry[i] == wordType.STRING) {
                                    newStr.append(" \"").append(stringList.get(i)).append("\"");
                                } else {
                                    newStr.append(" ").append(stringList.get(i));
                                }
                            }
                            // doScript(object, target, "", newStr, memData,1);
                            doScriptLine(newStr.toString(), object, target, memData, 0);
                            break;
                        }
                        next++;
                    }
                    StringBuilder newStr = new StringBuilder();
                    newStr.append("set visible of");
                    if (next < stringList.size()) {
                        newStr.append(" ").append(objStr);
                    } else {
                        next = 1;
                        for (int i = next; i < stringList.size(); i++) {
                            if (typeAry[i] == wordType.STRING) {
                                newStr.append(" \"").append(stringList.get(i)).append("\"");
                            } else {
                                newStr.append(" ").append(stringList.get(i));
                            }
                        }
                    }
                    newStr.append(" to true");
                    // doScript(object, target, "", newStr, memData,1);
                    doScriptLine(newStr.toString(), object, target, memData, 0);
                }
            }
            case "hide" -> {
                int next = 1;
                if (next >= stringList.size()) throw new xTalkException("hide is unknown");
                StringBuilder newStr = new StringBuilder();
                newStr.append("set visible of");
                for (int i = next; i < stringList.size(); i++) {
                    if (typeAry[i] == wordType.STRING) {
                        newStr.append(" \"").append(stringList.get(i)).append("\"");
                    } else {
                        newStr.append(" ").append(stringList.get(i));
                    }
                }
                newStr.append(" to false");
                // doScript(object, target, "", newStr, memData,1);
                doScriptLine(newStr.toString(), object, target, memData, 0);
            }
            case "enable" -> {
                int next = 1;
                if (next >= stringList.size()) throw new xTalkException("enable is unknown");
                StringBuilder newStr = new StringBuilder();
                newStr.append("set enabled of");
                for (int i = next; i < stringList.size(); i++) {
                    if (typeAry[i] == wordType.STRING) {
                        newStr.append(" \"").append(stringList.get(i)).append("\"");
                    } else {
                        newStr.append(" ").append(stringList.get(i));
                    }
                }
                newStr.append(" to true");
                // doScript(object, target, "", newStr, memData,1);
                doScriptLine(newStr.toString(), object, target, memData, 0);
            }
            case "disable" -> {
                int next = 1;
                if (next >= stringList.size()) throw new xTalkException("disable is unknown");
                StringBuilder newStr = new StringBuilder();
                newStr.append("set enabled of");
                for (int i = next; i < stringList.size(); i++) {
                    if (typeAry[i] == wordType.STRING) {
                        newStr.append(" \"").append(stringList.get(i)).append("\"");
                    } else {
                        newStr.append(" ").append(stringList.get(i));
                    }
                }
                newStr.append(" to false");
                // doScript(object, target, "", newStr, memData,1);
                doScriptLine(newStr.toString(), object, target, memData, 0);
            }
            case "wait" -> {
                int next = 1;
                if (next >= stringList.size()) throw new xTalkException("wait is unknown");
                if (0 == stringList.get(next).compareToIgnoreCase("until")) {
                    while (true) {
                        ArrayList<String> stL = new ArrayList<>(stringList);
                        wordType[] tyA = new wordType[typeAry.length];
                        System.arraycopy(typeAry, 0, tyA, 0, typeAry.length);
                        String cond = Evalution(stL, tyA, next + 1, stringList.size() - 1, memData, object, target);
                        if (0 == cond.compareToIgnoreCase("true"))
                            break;
                        TTalk.TalkSleep(10);

                        if (TTalk.stop == true) {
                            throw new xTalkException("user abort.");
                        }
                    }
                } else if (0 == stringList.get(next).compareToIgnoreCase("while")) {
                    while (true) {
                        ArrayList<String> stL = new ArrayList<>(stringList);
                        wordType[] tyA = new wordType[typeAry.length];
                        System.arraycopy(typeAry, 0, tyA, 0, typeAry.length);
                        String cond = Evalution(stL, tyA, next + 1, stringList.size() - 1, memData, object, target);
                        if (0 == cond.compareToIgnoreCase("false"))
                            break;
                        TTalk.TalkSleep(10);
                        if (TTalk.stop == true) {
                            throw new xTalkException("user abort.");
                        }
                    }
                } else if (0 == stringList.get(stringList.size() - 1).compareToIgnoreCase("seconds") ||
                        0 == stringList.get(stringList.size() - 1).compareToIgnoreCase("second")) {
                    if (stringList.get(next).equalsIgnoreCase("for")) next++;
                    String secs = Evalution(stringList, typeAry, next, stringList.size() - 2, memData, object, target);
                    TTalk.TalkSleep(Integer.parseInt(secs) * 1000);
                } else if (0 == stringList.get(stringList.size() - 1).compareToIgnoreCase("ticks") ||
                        0 == stringList.get(stringList.size() - 1).compareToIgnoreCase("tick")) {
                    if (stringList.get(next).equalsIgnoreCase("for")) next++;
                    String ticks = Evalution(stringList, typeAry, next, stringList.size() - 2, memData, object, target);
                    TTalk.TalkSleep(Integer.parseInt(ticks) * 1000 / 60);
                } else {
                    if (stringList.get(next).equalsIgnoreCase("for")) next++;
                    String ticks = Evalution(stringList, typeAry, next, stringList.size() - 1, memData, object, target);
                    if (!ticks.isEmpty()) {
                        TTalk.TalkSleep(Integer.parseInt(ticks) * 1000 / 60);
                    }
                }
            }
            case "do" -> {
                int next = 1;
                if (next >= stringList.size()) throw new xTalkException("do is unknown");
                String newStr = "";
                newStr = Evalution(stringList, typeAry, next, stringList.size() - 1, memData, object, target);
                doScript(object, target, "", newStr, memData, null, 0);
            }
            case "send" -> {
                // String newStr="";
                OObject obj = null;
                int next = 1;
                if (next >= stringList.size()) throw new xTalkException("send is unknown");
                while (0 != stringList.get(next).compareToIgnoreCase("to")) {
                    // newStr += stringList.get(next)+" ";
                    next++;
                    if (next >= stringList.size()) {
                        // throw new xTalkException("send requires to");
                        break;
                    }
                }
                if (next < stringList.size()) {
                    String newStr2 = Evalution(stringList, typeAry, 1, next - 1, memData, object, target);
                    next++;
                    if (next >= stringList.size()) throw new xTalkException("send to is unknown");
                    obj = getObject(stringList, typeAry, next, stringList.size() - 1, memData, object, target);
                    if (obj == null) {
                        // Parse and send variable value
                        String v = Evalution(stringList, typeAry, next, stringList.size() - 1, memData, object, target);
                        ArrayList<String> vList = new ArrayList<>();
                        ArrayList<wordType> tList = new ArrayList<>();
                        resolution(v, vList, tList, false);
                        wordType[] typeAry2 = new wordType[tList.size()];
                        for (int i = 0; i < tList.size(); i++) {
                            typeAry2[i] = tList.get(i);
                        }
                        obj = getObject(vList, typeAry2, 0, vList.size() - 1, memData, object, target);
                        if (obj == null) {
                            throw new xTalkException("object to send message with send is unknown");
                        }
                    }
                    if (obj.objectType.equals("window")) {
                        ((OWindow) obj).Command(newStr2);
                    } else {
                        doScript(obj, obj, "", newStr2, memData, null, 0);
                    }
                } else {
                    String newStr2 = Evalution(stringList, typeAry, 1, next - 1, memData, object, target);
                    doScript(object, object, "", newStr2, memData, null, 0);
                }
            }
            case "edit" -> {
                StringBuilder newStr = new StringBuilder();
                OObject obj = null;
                int next = 1;
                if (next >= stringList.size()) throw new xTalkException("edit is unknown");

                if (stringList.get(next).equalsIgnoreCase("picture")) {
                    // picture
                    if (ResEdit.nulleditor == null || ResEdit.nulleditor.child.pcard == null) {
                        ResEdit.nulleditor = new ResEdit(null, "icon", null);
                    }
                    ResEdit.nulleditor.child.pcard.stack.rsrc.addResource(
                            ResEdit.nulleditor.child.rsrcAry.length + 1, "icon", new File(stringList.get(2)).getName(), stringList.get(2));
                    ResEdit.nulleditor.child.open(ResEdit.nulleditor.child.pcard, 0);
//                    Rsrc.RsrcClass[] newAry = new Rsrc.RsrcClass[ResEdit.nulleditor.child.rsrcAry.length + 1];
//                    for (int i = 0; i < newAry.length - 1; i++) {
//                        newAry[i] = ResEdit.nulleditor.child.rsrcAry[i];
//                    }
//                    newAry[newAry.length - 1] = ResEdit.nulleditor.child.pcard.stack.rsrc.
//                            new RsrcClass(newAry.length, "icon", stringList.get(2), stringList.get(2), "0", "0", null);
//                    ResEdit.nulleditor.child.rsrcAry = newAry;
                } else {
                    // script
                    while (0 != stringList.get(next).compareToIgnoreCase("of")) {
                        if (!newStr.isEmpty()) newStr.append(" ");
                        newStr.append(stringList.get(next));
                        next++;
                        if (next >= stringList.size()) throw new xTalkException("edit requires of");
                    }
                    next++;
                    if (next >= stringList.size()) throw new xTalkException("edit of is unknown");
                    obj = getObject(stringList, typeAry, next, stringList.size() - 1, memData, object, target);
                    if (obj == null) throw new xTalkException("object to edit with edit is unknown");
                    if (newStr.toString().equalsIgnoreCase("script")) {
                        ScriptEditor.openScriptEditor(PCARD.pc, obj);
                    } else {
                        throw new xTalkException(newStr + " cannot be edited");
                    }
                }
            }
            case "play" -> {
                int next = 1;
                if (next >= stringList.size())
                    throw new xTalkException("play is unknown");
                if (0 == stringList.get(next).compareToIgnoreCase("stop")) {
                    // play stop
                    tsound.PlayStop();
                } else {
                    Pattern p = Pattern.compile("([a-grA-GR]{0,1})([#b]{0,1})([0-7]{0,1})([whqestx]{0,1})([3]{0,1}[\\.]*)([pf]{0,2})[ ]*");
                    String[] neiro = new String[7];
                    for (int i = 0; i < 7; i++) {
                        neiro[i] = "";
                    }

                    String soundRsrc = "";
                    int tempo = 120;
                    int vol = 100;
                    while (next < stringList.size()) {
                        if (next > 1 && 0 == stringList.get(next).compareToIgnoreCase("tempo")) {
                            if (soundRsrc.isEmpty())
                                soundRsrc = Evalution(stringList, typeAry, 1, next - 1, memData, object, target);
                            String tempoStr = getVariable(memData, stringList.get(next + 1));
                            if (tempoStr == null) tempoStr = stringList.get(next + 1);
                            tempo = Integer.parseInt(tempoStr);
                            next++;
                            // System.out.println("soundRsrc"+soundRsrc);
                        } else if (next > 1) {
                            String noteStr = getVariable(memData, stringList.get(next));
                            if (noteStr == null) noteStr = stringList.get(next);
                            Matcher m = p.matcher(noteStr);
                            boolean flag = false;
                            while (m.find()) {
                                int allLength = 0;
                                for (int i = 0; i < 7; i++) {
                                    neiro[i] = m.group(i);
                                    allLength += neiro[i].length();
                                }
                                if (allLength == 0) break;
                                if (soundRsrc.isEmpty())
                                    soundRsrc = Evalution(stringList, typeAry, 1, next - 1, memData, object, target);
                                if (neiro[6].contains("p")) vol /= 2;
                                if (neiro[6].contains("f")) {
                                    vol *= 2;
                                    if (vol > 100) vol = 100;
                                }
                                tsound.Play(PCARD.pc.stack, soundRsrc, neiro, tempo, vol);
                                flag = true;
                            }
                            if (flag == false && !soundRsrc.isEmpty()) {
                                throw new xTalkException("play of " + stringList.get(next) + " is unknown");
                            }
                        }
                        next++;
                    }
                    if (soundRsrc.isEmpty()) {
                        soundRsrc = Evalution(stringList, typeAry, 1, next - 1, memData, object, target);
                        boolean playres = tsound.Play(PCARD.pc.stack, soundRsrc, neiro, tempo, vol);
                        if (!playres) {
                            result.theResult = "play sound error";
                            result.ret = 0;
                        }
                    }
                }
            }
            case "push" -> {
                int next = 1;
                if (next >= stringList.size()) throw new xTalkException("push is unknown");
                if (stringList.get(next).equalsIgnoreCase("cd") ||
                        stringList.get(next).equalsIgnoreCase("card")) {
                    pushCardList.add(PCARD.pc.stack.curCard.getShortName());
                } else {
                    if (next >= stringList.size())
                        throw new xTalkException("push " + stringList.get(next) + " is unknown");
                }
            }
            case "pop" -> {
                int next = 1;
                if (next >= stringList.size()) throw new xTalkException("pop is unknown");
                if (stringList.get(next).equalsIgnoreCase("cd") ||
                        stringList.get(next).equalsIgnoreCase("card")) {
                    if (!pushCardList.isEmpty()) {
                        String doStr = "go " + pushCardList.get(pushCardList.size() - 1);
                        pushCardList.remove(pushCardList.size() - 1);
                        doScript(object, target, "", doStr, memData, null, 1);
                    }
                } else {
                    if (next >= stringList.size())
                        throw new xTalkException("push " + stringList.get(next) + " is unknown");
                }
            }
            case "click" -> {
                int next = 1;
                if (next < stringList.size() && stringList.get(next).equalsIgnoreCase("at")) {
                    next++;
                    if (next >= stringList.size()) throw new xTalkException("no coordinates for click at");
                    StringBuilder value = new StringBuilder();
                    StringBuilder param = new StringBuilder();
                    while (next < stringList.size() && !stringList.get(next).equalsIgnoreCase("with")) {
                        if (stringList.get(next).equals(",")) {
                            // Split by comma
                            param.append(Evalution(value.toString(), memData, object, target)).append(",");
                            value = new StringBuilder();
                            next++;
                            continue;
                        }
                        value.append(stringList.get(next)).append(" ");
                        next++;
                    }
                    param.append(Evalution(value.toString(), memData, object, target));
                    String[] params = param.toString().split(",");
                    if (2 != params.length) throw new xTalkException("click at coordinates are unknown");
                    int x = 0, y = 0;
                    try {
                        x = Integer.parseInt(params[0]);
                        y = Integer.parseInt(params[1]);
                    } catch (Exception e) {
                        throw new xTalkException("click at coordinates are unknown");
                    }

                    if (PCARD.pc.tool == null && AuthTool.tool == null) {
                        // browse tool
                        OObject obj = null;
                        for (int i = 0; i < PCARD.pc.mainPane.getComponentCount(); i++) {
                            Component c = PCARD.pc.mainPane.getComponent(i);
                            if (c.isVisible() &&
                                    x >= c.getX() && y >= c.getY() && x < c.getX() + c.getWidth() && y < c.getY() + c.getHeight() && // Using contains does not respond to transparent buttons
                                    c.isEnabled()) {
                                if (c.getClass() == MyPopup.class) obj = ((MyPopup) c).btnData;
                                if (c.getClass() == MyCheck.class) obj = ((MyCheck) c).btnData;
                                if (c.getClass() == MyRadio.class) obj = ((MyRadio) c).btnData;
                                if (c.getClass() == RoundButton.class) obj = ((RoundButton) c).btnData;
                                if (c.getClass() == RectButton.class) obj = ((RectButton) c).btnData;
                                if (c.getClass() == RoundedCornerButton.class) obj = ((RoundedCornerButton) c).btnData;
                                if (c.getClass() == MyButton.class) obj = ((MyButton) c).btnData;
                                if (c.getClass() == MyTextArea.class) obj = ((MyTextArea) c).fldData;
                                if (c.getClass() == MyScrollPane.class) obj = ((MyScrollPane) c).fldData;
                                // if(c.getClass()==MyLabel.class) obj = ((MyLabel)c).cd;

                                if (obj != null) {
                                    GUI.clickH = x;
                                    GUI.clickV = y;
                                    GUI.mouseClicked = true;
                                    talk.ReceiveMessage("mouseDown", "", obj, null, false);
                                    talk.ReceiveMessage("mouseUp", "", obj, null, false);
                                    break;
                                }
                            }
                        }
                        if (obj == null && x >= 0 && y >= 0 && x < PCARD.pc.stack.width && y < PCARD.pc.stack.height) {
                            GUI.clickH = x;
                            GUI.clickV = y;
                            GUI.mouseClicked = true;
                            talk.ReceiveMessage("mouseDown", "", PCARD.pc.stack.curCard, null, false);
                            talk.ReceiveMessage("mouseUp", "", PCARD.pc.stack.curCard, null, false);
                        }
                    } else {
                        // auth or paint tool
                        if (x < 0 || x > PCARD.pc.getWidth() || y < 0 || y > PCARD.pc.getHeight()) {
                            result.theResult = "Error: out of card window";
                            return result;
                        }
                        int j;
                        for (j = 2; j < stringList.size(); j++) {
                            if (stringList.get(j).equalsIgnoreCase("with")) {
                                break;
                            }
                        }
                        String withKey = "";
                        if (j + 1 < stringList.size()) {
                            withKey = stringList.get(j + 1).toLowerCase();
                        }
                        Robot rb = null;
                        try {
                            rb = new Robot();
                        } catch (AWTException e) {
                            e.printStackTrace();
                        }
                        x = x * PCARD.pc.zoom + PCARD.pc.getX();
                        y = y * PCARD.pc.zoom + PCARD.pc.getY() + PCARD.pc.getInsets().top;
                        if (rb != null) {
                        /*BufferedImage bi = new BufferedImage(16,16, BufferedImage.TYPE_INT_ARGB);
                        Toolkit kit = Toolkit.getDefaultToolkit();
                        Cursor cr = kit.createCustomCursor(bi, new Point(0, 0), "none-cursor");
                        PCARD.pc.setCursor(cr);*/

                            if (withKey.contains("control")) rb.keyPress(KeyEvent.VK_CONTROL);
                            if (withKey.contains("shift")) rb.keyPress(KeyEvent.VK_SHIFT);
                            if (withKey.contains("option") || withKey.contains("alt")) rb.keyPress(KeyEvent.VK_ALT);
                            if (withKey.contains("command") || withKey.contains("cmd")) rb.keyPress(KeyEvent.VK_META);
                            if (!withKey.isEmpty()) rb.delay(100);
                            {
                                PointerInfo pointerInfo = MouseInfo.getPointerInfo();
                                int savex = pointerInfo.getLocation().x;
                                int savey = pointerInfo.getLocation().y;
                                rb.mouseMove(x, y);
                                rb.delay(10);
                                rb.mousePress(InputEvent.BUTTON1_DOWN_MASK);
                                rb.delay(20);
                                rb.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
                                rb.delay(20);
                                rb.mouseMove(savex, savey);
                            }
                            if (withKey.contains("control")) rb.keyRelease(KeyEvent.VK_CONTROL);
                            if (withKey.contains("shift")) rb.keyRelease(KeyEvent.VK_SHIFT);
                            if (withKey.contains("option") || withKey.contains("alt")) rb.keyRelease(KeyEvent.VK_ALT);
                            if (withKey.contains("command") || withKey.contains("cmd")) rb.keyRelease(KeyEvent.VK_META);

                            /*PCARD.pc.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));*/
                        }
                    }
                } else throw new xTalkException("click is unknown");
            }
            case "drag" -> {
                int next = 1;
                int sx = 0, sy = 0;
                int ex = 0, ey = 0;
                if (next < stringList.size() && stringList.get(next).equalsIgnoreCase("from")) {
                    next++;
                    if (next >= stringList.size()) throw new xTalkException("no coordinates for drag from");
                    StringBuilder value = new StringBuilder();
                    StringBuilder param = new StringBuilder();
                    while (next < stringList.size() && !stringList.get(next).equalsIgnoreCase("to")) {
                        if (stringList.get(next).equals(",")) {
                            // Split by comma
                            param.append(Evalution(value.toString(), memData, object, target)).append(",");
                            value = new StringBuilder();
                            next++;
                            continue;
                        }
                        value.append(stringList.get(next)).append(" ");
                        next++;
                    }
                    param.append(Evalution(value.toString(), memData, object, target));
                    String[] params = param.toString().split(",");
                    if (2 != params.length) throw new xTalkException("drag from coordinates are unknown");

                    try {
                        sx = Integer.parseInt(params[0]);
                        sy = Integer.parseInt(params[1]);
                    } catch (Exception e) {
                        throw new xTalkException("drag from coordinates are unknown");
                    }
                } else throw new xTalkException("from is missing");

                if (next < stringList.size() && stringList.get(next).equalsIgnoreCase("to")) {
                    next++;
                    if (next >= stringList.size()) throw new xTalkException("no coordinates for drag to");
                    StringBuilder value = new StringBuilder();
                    StringBuilder param = new StringBuilder();
                    while (next < stringList.size()) {
                        if (stringList.get(next).equals(",")) {
                            // Split by comma
                            param.append(Evalution(value.toString(), memData, object, target)).append(",");
                            value = new StringBuilder();
                            next++;
                            continue;
                        }
                        value.append(stringList.get(next)).append(" ");
                        next++;
                    }
                    param.append(Evalution(value.toString(), memData, object, target));
                    String[] params = param.toString().split(",");
                    if (2 != params.length) throw new xTalkException("drag to coordinates are unknown");

                    try {
                        ex = Integer.parseInt(params[0]);
                        ey = Integer.parseInt(params[1]);
                    } catch (Exception e) {
                        throw new xTalkException("drag to coordinates are unknown");
                    }
                } else throw new xTalkException("to is missing");

                {
                    int j;
                    for (j = next; j < stringList.size(); j++) {
                        if (stringList.get(j).equalsIgnoreCase("with")) {
                            break;
                        }
                    }
                    String withKey = "";
                    if (j + 1 < stringList.size()) {
                        withKey = stringList.get(j + 1).toLowerCase();
                    }
                    Robot rb = null;
                    try {
                        rb = new Robot();
                    } catch (AWTException e) {
                        e.printStackTrace();
                    }
                    sx = sx * PCARD.pc.zoom + PCARD.pc.getX();
                    sy = sy * PCARD.pc.zoom + PCARD.pc.getY() + PCARD.pc.getInsets().top;
                    ex = ex * PCARD.pc.zoom + PCARD.pc.getX();
                    ey = ey * PCARD.pc.zoom + PCARD.pc.getY() + PCARD.pc.getInsets().top;
                    if (rb != null) {
//                        BufferedImage bi = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
//                        Toolkit kit = Toolkit.getDefaultToolkit();
//                        Cursor cr = kit.createCustomCursor(bi, new Point(0, 0), "none-cursor");
//                        PCARD.pc.setCursor(cr);

                        if (withKey.contains("control")) rb.keyPress(KeyEvent.VK_CONTROL);
                        if (withKey.contains("shift")) rb.keyPress(KeyEvent.VK_SHIFT);
                        if (withKey.contains("option") || withKey.contains("alt")) rb.keyPress(KeyEvent.VK_ALT);
                        if (withKey.contains("command") || withKey.contains("cmd")) rb.keyPress(KeyEvent.VK_META);
                        if (!withKey.isEmpty()) rb.delay(100);
                        {
                            PointerInfo pointerInfo = MouseInfo.getPointerInfo();
                            int savex = pointerInfo.getLocation().x;
                            int savey = pointerInfo.getLocation().y;
                            rb.mouseMove(sx, sy);
                            rb.delay(10);
                            rb.mousePress(InputEvent.BUTTON1_DOWN_MASK);
                            rb.delay(20);
                            if (dragspeed > 0) {
                                double start = new Date().getTime();
                                double d = Math.sqrt((ex - sx) * (ex - sx) + (ey - sy) * (ey - sy));
                                while (true) {
                                    double sec = (new Date().getTime() - start) / 10.0 * 3 / 5 / 60;
                                    double per = sec * dragspeed / (d + 0.4);
                                    if (per >= 1) break;
                                    rb.mouseMove((int) (sx * (1 - per) + ex * per), (int) (sy * (1 - per) + ey * per));

                                    try {
                                        Thread.sleep(10);
                                    } catch (InterruptedException e) {
                                    }
                                }
                            }
                            rb.mouseMove(ex, ey);
                            rb.delay(20);
                            rb.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
                            rb.delay(20);
                            rb.mouseMove(savex, savey);
                        }
                        if (withKey.contains("control")) rb.keyRelease(KeyEvent.VK_CONTROL);
                        if (withKey.contains("shift")) rb.keyRelease(KeyEvent.VK_SHIFT);
                        if (withKey.contains("option") || withKey.contains("alt")) rb.keyRelease(KeyEvent.VK_ALT);
                        if (withKey.contains("command") || withKey.contains("cmd")) rb.keyRelease(KeyEvent.VK_META);

//                        PCARD.pc.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                    }
                }
            }
            case "reset" -> {
                int next = 1;
                if (next < stringList.size()) {
                    if (stringList.get(next).equalsIgnoreCase("menuBar")) {
                        // Reset menu bar to initial state
                        PCARD.pc.menu = new GMenu(PCARD.pc, 0);
                        GMenu.menuUpdate(PCARD.pc.menu.mb);
                    } else throw new xTalkException("reset is unknown");
                }
            }
            case "delete" -> {
                int next = 1;
                if (next >= stringList.size()) throw new xTalkException("delete is unknown");
                if (stringList.get(next).equalsIgnoreCase("menu")) {
                    // Delete menu
                    for (int i = 0; i < PCARDFrame.pc.menu.mb.getComponentCount(); i++) {
                        Component c = PCARDFrame.pc.menu.mb.getComponent(i);
                        if (c.getClass() == JMenu.class) {
                            JMenu menu = (JMenu) c;
                            String v = Evalution(stringList, typeAry, next + 1, stringList.size() - 1, memData, object, target);
                            if (menu.getText().equalsIgnoreCase(v)) {
                                PCARDFrame.pc.menu.mb.remove(menu);
                                // Reflect menu
                                GMenu.menuUpdate(PCARD.pc.menu.mb);
                                break;
                            }
                        }
                    }
                } else if (stringList.get(next).equalsIgnoreCase("menuItem")) {
                    // Delete menu item
                    for (int i = 0; i < PCARDFrame.pc.menu.mb.getComponentCount(); i++) {
                        Component c = PCARDFrame.pc.menu.mb.getComponent(i);
                        if (c.getClass() == JMenu.class) {
                            JMenu menu = (JMenu) c;
                            for (int j = 0; j < menu.getPopupMenu().getComponentCount(); j++) {
                                Component c2 = menu.getPopupMenu().getComponent(j);
                                if (c2.getClass() == JMenuItem.class) {
                                    JMenuItem mi = (JMenuItem) c2;
                                    if (mi.getText().equalsIgnoreCase(stringList.get(next))) {
                                        menu.remove(mi);
                                        // Reflect menu
                                        GMenu.menuUpdate(PCARD.pc.menu.mb);
                                        break;
                                    }
                                }
                            }
                        }
                    }
                } else {
                    StringBuilder doStr = new StringBuilder("put empty into");
                    for (; next < stringList.size(); next++) {
                        doStr.append(" ").append(stringList.get(next));
                    }
                    doScriptLine(doStr.toString(), object, target, memData, 0);
//                    doScript(object, target, "", doStr, memData,1);
                }
            }
            case "sort" -> {
                int next = 1;
                if (next >= stringList.size()) throw new xTalkException("sort is unknown");
                String splitStr = "\n";
                if (next + 1 <= stringList.size() && stringList.get(next).equalsIgnoreCase("items") &&
                        stringList.get(next + 1).equalsIgnoreCase("of")) {
                    splitStr = ",";
                    next += 2;
                } else if (next + 1 <= stringList.size() && stringList.get(next).equalsIgnoreCase("lines") &&
                        stringList.get(next + 1).equalsIgnoreCase("of")) {
                    next += 2;
                }
                int j = next;
                boolean isAscending = true; // Sort direction

                for (; j < stringList.size(); j++) {
                    if (stringList.get(j).equalsIgnoreCase("descending")) {
                        isAscending = false;
                        break;
                    } else if (stringList.get(j).equalsIgnoreCase("ascending")) {
                        break;
                    }
                }
                OObject obj = getObject(stringList, typeAry, next, j - 1, memData, object, target);
                String oldValue = "";
                if (obj != null) {
                    oldValue = obj.getText();
                } else {
                    oldValue = getVariable(memData, stringList.get(next));
                }

                StringBuilder newValue = new StringBuilder();
                {
                    String[] valAry = oldValue.split(splitStr);
                    for (int i = 0; i < valAry.length - 1; i++) {
                        for (int k = i + 1; k < valAry.length; k++) {
                            if (valAry[i].compareTo(valAry[k]) > 0 == isAscending) {
                                String v = valAry[i];
                                valAry[i] = valAry[k];
                                valAry[k] = v;
                            }
                        }
                    }
                    for (int i = 0; i < valAry.length; i++) {
                        newValue.append(valAry[i]);
                        if (i + 1 < valAry.length) newValue.append(splitStr);
                    }
                }

                if (obj != null) {
                    TUtil.SetProperty(obj, "text", newValue.toString());
                } else {
                    setVariable(memData, stringList.get(next), newValue.toString());
                }
            }
            case "ask" -> {
                // ask
                int next = 1;
                if (stringList.get(next).equals("file")) {
                    // File save dialog
                    next++;
                    int j;
                    for (j = next; next < stringList.size() - 1; j++) {
                        if (stringList.get(j).equals("with")) {
                            break;
                        }
                    }
                    String answerStr = Evalution(stringList, typeAry, next, j - 1, memData, object, target);
                    String filename = "";
                    if (j + 1 < stringList.size()) {
                        filename = Evalution(stringList, typeAry, j + 1, stringList.size() - 1, memData, object, target);
                    }

                    JFileChooser chooser;
                    if (PCARD.pc.stack.file != null) {
                        String filePath = PCARD.pc.stack.path;
                        chooser = new JFileChooser();
                        File parent = new File(new File(filePath).getParent());
                        chooser.setCurrentDirectory(parent);
                        chooser.setSelectedFile(new File(parent.getPath() + File.separatorChar + filename));
                    } else {
                        chooser = new JFileChooser(new File(filename));
                    }
                    chooser.setDialogTitle(answerStr);
                    int ret = chooser.showSaveDialog(PCARD.pc);
                    if (ret != JFileChooser.APPROVE_OPTION) {
                        // Do not save
                        result.theResult = "canceled";
                        return result;
                    }
                    String path = chooser.getSelectedFile().getPath();
                    setVariable(memData, "it", path);
                } else {
                    // Show dialog box with text input
                    String answerStr = "";
                    if (next >= stringList.size()) throw new xTalkException("ask is unknown");
                    if (stringList.get(next).equalsIgnoreCase("file")) throw new xTalkException("ask file not supported");
                    if (stringList.get(next).equalsIgnoreCase("password"))
                        throw new xTalkException("ask password not supported");
                    int i;
                    for (i = next + 1; i < stringList.size(); i++) {
                        if (0 == stringList.get(i).compareToIgnoreCase("with")) break;
                    }
                    answerStr = Evalution(stringList, typeAry, next, i - 1, memData, object, target);
                    next = i;
                    String defaultText = "";
                    if (next + 1 < stringList.size()) {
                        defaultText = Evalution(stringList, typeAry, next + 1, stringList.size() - 1, memData, object, target);
                    }
                    new GDialog(PCARD.pc, answerStr, defaultText, "Cancel", "OK", null);
                    setVariable(memData, "it", GDialog.inputText);
                }
            }
            case "domenu" -> {
                int next = 1;
                if (next >= stringList.size()) throw new xTalkException("domenu is unknown");

                // TODO Want to check if enabled and if it is in the menu at all
                GMenuBrowse.doMenu(stringList.get(next));
            }
            case "select" -> {
                OObject obj = null;
                String chunkType = "";
                String chunkStart = "";
                String chunkEnd = null;
                int next = 1;
                if (next >= stringList.size()) throw new xTalkException("select is unknown");
                if (typeAry[next] == wordType.FUNC) {
                    String newStr = Evalution(stringList, typeAry, next, stringList.size() - 1, memData, object, target);
                    if (!newStr.isEmpty()) {
                        for (int j = stringList.size() - 1; j >= next; j--) {
                            stringList.remove(1);
                        }
                        ArrayList<wordType> typeList = new ArrayList<>();
                        typeList.add(wordType.X);
                        resolution(newStr, stringList, typeList, true);
                        typeAry = new wordType[typeList.size()];
                        typeList.toArray(typeAry);
                    } else {
                        result.theResult = "";
                        result.ret = 0;
                        return result;
                    }
                }
                if (stringList.get(next).equalsIgnoreCase("char") ||
                        stringList.get(next).equalsIgnoreCase("character") ||
                        stringList.get(next).equalsIgnoreCase("item") ||
                        stringList.get(next).equalsIgnoreCase("word") ||
                        stringList.get(next).equalsIgnoreCase("line")) {
                    chunkType = stringList.get(next);
                    int nest = 0;
                    while (nest != 0 || (0 != stringList.get(next).compareToIgnoreCase("to") &&
                            0 != stringList.get(next).compareToIgnoreCase("of"))) {
                        next++;
                        if (next >= stringList.size()) break;
                        if (typeAry[next] == wordType.LFUNC || typeAry[next] == wordType.LBRACKET) {
                            nest++;
                        }
                        if (typeAry[next] == wordType.RFUNC || typeAry[next] == wordType.RBRACKET) {
                            nest--;
                        }
                    }
                    if (next < stringList.size()) {
                        chunkStart = Evalution(stringList, typeAry, 2, next - 1, memData, object, target);
                    }
                    if (next < stringList.size() && 0 == stringList.get(next).compareToIgnoreCase("to")) {
                        int next2 = next;
                        while (0 != stringList.get(next).compareToIgnoreCase("of")) {
                            next++;
                            if (next >= stringList.size()) break;
                        }
                        if (next < stringList.size()) {
                            chunkEnd = Evalution(stringList, typeAry, next2 + 1, next - 1, memData, object, target);
                        }
                    }
                } else if (stringList.get(next).equalsIgnoreCase("empty")) {
                    if (target != null && target.getClass() == OField.class) {
                        OField fld = (OField) target;
                        fld.setSelectedLine(0);
                    } else {
                        // throw new xTalkException("cannot select target empty");
                    }
                    result.theResult = "";
                    result.ret = 0;
                    return result;
                } else {
                    throw new xTalkException("select object command is not supported");
                }
                while (0 != stringList.get(next).compareToIgnoreCase("of")) {
                    next++;
                    if (next >= stringList.size()) {
                        throw new xTalkException("select is unknown");
                    }
                }
                obj = getObject(stringList, typeAry, next + 1, stringList.size() - 1, memData, object, target);
                if (obj.objectType.equals("button")) {
                    if (chunkType.equalsIgnoreCase("line")) {
                        if (chunkEnd == null) {
                            if (chunkStart.isEmpty()) chunkStart = "0";
                            ((OButton) obj).setSelectedLine(Integer.parseInt(chunkStart));
                        } else throw new xTalkException("buttons can only be selected on one line");
                    } else throw new xTalkException("buttons can only be selected by line");
                } else if (obj.objectType.equals("field")) {
                    if (chunkType.equalsIgnoreCase("line") && chunkEnd == null) {
                        OField fld = (OField) obj;
                        fld.setSelectedLine(Integer.parseInt(chunkStart));
                        if (fld.autoSelect == false) {
                            String[] texts = fld.getText().split("\n");
                            int start = Integer.parseInt(chunkStart);
                            int chars = 0;
                            for (int j = 0; j < start && j < texts.length; j++) {
                                chars += texts[j].length() + 1;
                            }
                            fld.fld.setSelectionStart(chars);
                            if (start < texts.length) {
                                fld.fld.setSelectionEnd(chars + texts[start].length());
                            }
                            fld.fld.repaint();
                        }
                    } else {
                        throw new xTalkException("complex selection with select is not supported");
                    }
                } else throw new xTalkException("text other than fields and popup buttons cannot be selected");
            }
            case "choose" -> {
                int next = 2;
                if (next >= stringList.size()) throw new xTalkException("choose is unknown");

                if (stringList.get(next).equalsIgnoreCase("tool")) {
                    next = 1;
                    boolean ret = TBButtonListener.ChangeTool(stringList.get(next), null);
                    if (!ret) {
                        throw new xTalkException("tool\"" + stringList.get(next) + "\" does not exist");
                    }
                    PCARD.pc.mainPane.repaint();
                } else {
                    throw new xTalkException("choose is unknown");
                }
            }
            case "create" -> {
                int next = 1;
                if (next >= stringList.size()) throw new xTalkException("create is unknown");

                if (stringList.get(next).equalsIgnoreCase("menu")) {
                    next++;
                    if (next >= stringList.size()) throw new xTalkException("create menu is unknown");

                    JMenu m = new JMenu(stringList.get(next));
                    PCARD.pc.menu.mb.add(m);
                    PCARD.pc.getJMenuBar().add(m);
                } else if (stringList.get(next).equalsIgnoreCase("button") ||
                        stringList.get(next).equalsIgnoreCase("btn")) {
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
                    OButton obtn = null;
                    obtn = new OButton(cdbase, newid);
                    if (obtn != null) {
                        obtn.btn = new MyButton(obtn, "");
                        ((OCardBase) obtn.parent).partsList.add(obtn);
                        ((OCardBase) obtn.parent).btnList.add(obtn);

                        int left = PCARD.pc.stack.width / 2 - 128 / 2;
                        int top = PCARD.pc.stack.height / 2 - 32 / 2;
                        obtn.setRect(left, top, left + 128, top + 32);
                        obtn.style = 0;
                        obtn.setName(PCARD.pc.intl.getDialogText("New Button"));

                        OCard.reloadCurrentCard();
                        if (AuthTool.tool != null && AuthTool.tool.getClass() == ButtonTool.class) {
                            ButtonGUI.gui.tgtOBtn = obtn;
                            ButtonGUI.gui.target = obtn.getComponent();
                        }
                    }
                } else if (stringList.get(next).equalsIgnoreCase("field") ||
                        stringList.get(next).equalsIgnoreCase("fld")) {
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
                    OField ofld = null;
                    ofld = new OField(cdbase, newid);
                    if (ofld != null) {
                        ofld.fld = new MyTextArea("");
                        ((OCardBase) ofld.parent).partsList.add(ofld);
                        ((OCardBase) ofld.parent).fldList.add(ofld);

                        int left = PCARD.pc.stack.width / 2 - 128 / 2;
                        int top = PCARD.pc.stack.height / 2 - 128 / 2;
                        ofld.setRect(left, top, left + 128, top + 128);
                        ofld.style = 3;
                        ofld.enabled = false;

                        OCard.reloadCurrentCard();
                        if (AuthTool.tool != null && AuthTool.tool.getClass() == FieldTool.class) {
                            FieldGUI.gui.tgtOFld = ofld;
                            FieldGUI.gui.target = ofld.getComponent();
                        }
                    }
                }
            }
            case "open" -> {
                int next = 1;
                if (next >= stringList.size()) throw new xTalkException("open is unknown");

                if (stringList.get(next).equalsIgnoreCase("stack")) {
                    next = 2;
                    OStack saveStack = PCARD.pc.stack;
                    PCARD.pc.stack = new OStack(PCARD.pc);
                    String path = stringList.get(next);
                    if (!new File(path).exists()) {
                        path = convertFromMacPath(path);
                    }
                    boolean res = PCARD.pc.stack.openStackFileInThread(path, false);
                    if (res) {
                        saveStack.clean();
                    } else {
                        PCARD.pc.stack = saveStack;
                    }
                    result.theResult = res ? "true" : "false";
                    result.ret = 0;
                } else if (stringList.get(next).equals("file")) {
                    next++;
                    if (next >= stringList.size()) throw new xTalkException("open file is unknown");

                    String path = stringList.get(next);
                    if (!new File(path).exists()) {
                        path = convertFromMacPath(path);
                    }
                    if (!new File(path).getName().equals(path)) {
                        if (!new File(new File(path).getParent()).exists()) {
                            throw new xTalkException("Unknown directory. Cannot open file");
                        }
                    }

                    if (!new File(stringList.get(next)).exists()) {
                        // Create new
                        try {
                            if (!new File(stringList.get(next)).createNewFile()) {
                                throw new xTalkException("Could not create file");
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else if (new File(stringList.get(next)).isDirectory()) {
                        // Directory
                        throw new xTalkException("Cannot open directory");
                    }
                    for (OpenFile openFile : openFileList) {
                        if (openFile.path.equals(path)) {
                            throw new xTalkException("Already open");
                        }
                    }
                    OpenFile ofile = new OpenFile();
                    ofile.path = path;
                    openFileList.add(ofile);
                } else {
                    throw new xTalkException("open is unknown");
                }
            }
            case "close" -> {
                int next = 1;
                if (next >= stringList.size()) throw new xTalkException("close is unknown");

                if (stringList.get(next).equalsIgnoreCase("window")) {
                    next++;
                    if (next >= stringList.size()) throw new xTalkException("close window is unknown");

                    OObject obj = getObject(stringList, typeAry, next, stringList.size() - 1, memData, object, target);
                    if (obj != null) {
                        ((OWindow) obj).Close();
                    }
                } else if (stringList.get(next).equals("file")) {
                    next++;
                    if (next >= stringList.size()) throw new xTalkException("close file is unknown");

                    String path = stringList.get(next);
                    if (!new File(path).exists()) {
                        path = convertFromMacPath(path);
                    }
                    if (!new File(path).getName().equals(path)) {
                        if (!new File(new File(path).getParent()).exists()) {
                            throw new xTalkException("Unknown directory. Cannot close file");
                        }
                    }

                    if (!new File(stringList.get(next)).exists()) {
                        throw new xTalkException("File does not exist");
                    } else if (new File(stringList.get(next)).isDirectory()) {
                        // Directory
                        throw new xTalkException("Cannot close directory");
                    }
                    OpenFile ofile = null;
                    for (OpenFile openFile : openFileList) {
                        if (openFile.path.equals(path)) {
                            ofile = openFile;
                        }
                    }
                    if (ofile == null) throw new xTalkException("Not open");
                    if (ofile.istream != null) {
                        try {
                            ofile.istream.close();
                        } catch (IOException e) {
                        }
                    }
                    if (ofile.ostream != null) {
                        try {
                            ofile.ostream.close();
                        } catch (IOException e) {
                        }
                    }
                    ofile.path = null;
                    openFileList.remove(ofile);
                } else {
                    throw new xTalkException("close is unknown");
                }
            }
            case "read" -> {
                int next = 1;
                if (next >= stringList.size()) throw new xTalkException("read is unknown");
                if (!stringList.get(next).equals("from")) throw new xTalkException("read is unknown");
                next++;
                if (next >= stringList.size()) throw new xTalkException("read from is unknown");
                if (stringList.get(next).equals("file")) {
                    next++;
                    if (next >= stringList.size()) throw new xTalkException("read from file is unknown");

                    String path = stringList.get(next);
                    if (!new File(path).exists()) {
                        path = convertFromMacPath(path);
                    }

                    int offset = -1;
                    int forchars = -1;
                    int endChar = -1;
                    next++;
                    if (next < stringList.size()) {
                        if (stringList.get(next).equals("at")) {
                            next++;
                            if (next >= stringList.size()) throw new xTalkException("read from file at is unknown");
                            try {
                                offset = Integer.parseInt(stringList.get(next));
                            } catch (Exception e) {
                                throw new xTalkException("A number is required here");
                            }

                            next++;
                        }
                        if (next < stringList.size()) {
                            if (stringList.get(next).equals("until")) {
                                next++;
                                if (next >= stringList.size())
                                    throw new xTalkException("read from file until is unknown");
                                String tmp = Evalution(stringList, typeAry, next, stringList.size() - 1, memData, object, target);
                                if (tmp.length() == 1) {
                                    endChar = tmp.charAt(0);
                                } else if (tmp.equalsIgnoreCase("eof")) {
                                    endChar = -1;
                                } else {
                                    throw new xTalkException("Please specify a character here");
                                }
                            } else if (stringList.get(next).equals("for")) {
                                next++;
                                if (next >= stringList.size())
                                    throw new xTalkException("read from file for is unknown");
                                try {
                                    forchars = Integer.parseInt(stringList.get(next));
                                } catch (Exception e) {
                                    throw new xTalkException("A number is required here");
                                }
                            }
                        }
                    }

                    OpenFile ofile = null;
                    for (OpenFile openFile : openFileList) {
                        if (openFile.path.equals(path)) {
                            ofile = openFile;
                            break;
                        }
                    }
                    if (ofile == null) {
                        throw new xTalkException("File is not open");
                    }
                    if (ofile.istream == null) {
                        try {
                            ofile.istream = new FileInputStream(path);
                        } catch (FileNotFoundException e) {
                            throw new xTalkException("Cannot read from file");
                        }
                    }
                    try {
                        if (offset != -1) {
                            ofile.istream.reset();
                        }
                        offset = Math.min(ofile.istream.available(), offset);
                        for (int i = 0; i < offset; i++) {
                            ofile.istream.read();
                        }
                        forchars = Math.min(ofile.istream.available(), forchars);
                        if (forchars == -1) {
                            forchars = ofile.istream.available();
                        }
                        byte[] b = new byte[forchars];
                        int i;
                        for (i = 0; i < forchars; i++) {
                            ofile.istream.read(b, i, 1);
                            if (b[i] == endChar) {
                                // b[i] = 0;
                                break;
                            }
                        }
                        setVariable(memData, "it", new String(b));
                    } catch (IOException e) {
                        throw new xTalkException("An error occurred while reading from file");
                    }
                } else {
                    throw new xTalkException("read from is unknown");
                }
            }
            case "write" -> {
                int next = 1;
                if (next >= stringList.size()) throw new xTalkException("write is unknown");

                while (next < stringList.size() && !stringList.get(next).equals("to")) {
                    next++;
                }
                if (next >= stringList.size()) throw new xTalkException("write requires to file");
                String text = Evalution(stringList, typeAry, 1, next - 1, memData, object, target);

                next++;
                if (next >= stringList.size()) throw new xTalkException("write requires to file");
                if (stringList.get(next).equals("file")) {
                    next++;
                    if (next >= stringList.size()) throw new xTalkException("write to file is unknown");

                    int j = next;
                    while (j < stringList.size() && !stringList.get(j).equals("at")) {
                        next++;
                    }
                    String path = Evalution(stringList, typeAry, next, j - 1, memData, object, target);
                    if (!new File(path).exists()) {
                        path = convertFromMacPath(path);
                    }

                    int offset = -1;
                    next = j;
                    if (next < stringList.size() && stringList.get(next).equals("at")) {
                        next++;
                        if (next >= stringList.size()) throw new xTalkException("write to file at is unknown");
                        try {
                            offset = Integer.parseInt(stringList.get(next));
                        } catch (Exception e) {
                            throw new xTalkException("A number is required here");
                        }

                        next++;
                    }

                    OpenFile ofile = null;
                    for (OpenFile openFile : openFileList) {
                        if (openFile.path.equals(path)) {
                            ofile = openFile;
                            break;
                        }
                    }
                    if (ofile == null) {
                        throw new xTalkException("File is not open");
                    }
                    if (ofile.ostream == null && offset == -1) {
                        try {
                            ofile.ostream = new FileOutputStream(path);
                        } catch (FileNotFoundException e) {
                            throw new xTalkException("Cannot write to file");
                        }
                    }
                    try {
                        if (offset != -1) {
                            ofile.ostream = new FileOutputStream(path);
                        }
                        if (offset > 0) {
                            byte[] b = new byte[offset];
                            int i;
                            FileInputStream istream = new FileInputStream(path);
                            offset = Math.min(istream.available(), offset);
                            for (i = 0; i < offset; i++) {
                                istream.read(b, i, 1);
                            }
                            istream.close();
                            text = new String(b) + text;
                        }
                    } catch (IOException e) {
                        throw new xTalkException("An error occurred while preparing to write to file");
                    }
                    try {
                        ofile.ostream.write(text.getBytes());
                    } catch (IOException e) {
                        throw new xTalkException("An error occurred while writing to file");
                    }
                } else {
                    throw new xTalkException("write is unknown");
                }
            }
            case "convert" -> {
                int next = 2;
                if (next >= stringList.size()) throw new xTalkException("convert is unknown");

                for (next = 2; next < stringList.size(); next++) {
                    if (stringList.get(next).equalsIgnoreCase("to")) {
                        break;
                    }
                }
                if (next >= stringList.size()) throw new xTalkException("convert is missing to");

                String convStr = Evalution(stringList, typeAry, 1, next - 1, memData, object, target);

                Calendar calender = Calendar.getInstance();
                calender.set(Calendar.HOUR_OF_DAY, 0);
                calender.set(Calendar.MINUTE, 0);
                calender.set(Calendar.SECOND, 0);

                while (true) { // Dummy loop to break out of
                    Pattern p = Pattern.compile("^([0-9]{1,4})\\.([0-9]{2})([0-9]{2})$");
                    Matcher m = p.matcher(convStr);
                    if (m.find()) {
                        calender.set(Calendar.YEAR, Integer.parseInt(m.group(0)));
                        calender.set(Calendar.MONTH, Integer.parseInt(m.group(1)) - 1);
                        calender.set(Calendar.DAY_OF_MONTH, Integer.parseInt(m.group(2)));
                        break;
                    }
                    p = Pattern.compile("^(.*), (.*) ([0-9]{1,2}). ([0-9]){1,4}$");
                    m = p.matcher(convStr);
                    if (m.find()) {
                        calender.set(Calendar.YEAR, Integer.parseInt(m.group(3)));
                        int month = 0;
                        String[] month_ary = {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};
                        for (int i = 0; i < month_ary.length; i++) {
                            if (month_ary[i].equals(m.group(1))) {
                                month = i + 1;
                                calender.set(Calendar.MONTH, month - 1);
                                break;
                            }
                        }
                        calender.set(Calendar.DAY_OF_MONTH, Integer.parseInt(m.group(2)));
                        int dow = 0;
                        String[] youbi_ary = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
                        for (int i = 0; i < youbi_ary.length; i++) {
                            if (youbi_ary[i].equals(m.group(0))) {
                                dow = i + 1;
                                calender.set(Calendar.DAY_OF_WEEK, dow);
                                break;
                            }
                        }
                        break;
                    }
                    if (Locale.getDefault().equals(Locale.JAPANESE)) {
                        p = Pattern.compile("^([0-9]{1,4})年 ([0-9]{1,2})月 ([0-9]{1,2})日 (.*){3}$");
                        m = p.matcher(convStr);
                        if (m.find()) {
                            calender.set(Calendar.YEAR, Integer.parseInt(m.group(1)));
                            calender.set(Calendar.MONTH, Integer.parseInt(m.group(2)) - 1);
                            calender.set(Calendar.DAY_OF_MONTH, Integer.parseInt(m.group(3)));
                            int dow = 0;
                            String[] youbi_ary = {"日曜日", "月曜日", "火曜日", "水曜日", "木曜日", "金曜日", "土曜日"};
                            for (int i = 0; i < youbi_ary.length; i++) {
                                if (youbi_ary[i].equals(m.group(4))) {
                                    dow = i + 1;
                                    calender.set(Calendar.DAY_OF_WEEK, dow);
                                    break;
                                }
                            }
                            break;
                        }
                    }
                    p = Pattern.compile("^([0-9]{1,2}):([0-9]{1,2}) (AM|PM)$");
                    m = p.matcher(convStr);
                    if (m.find()) {
                        calender.set(Calendar.HOUR, Integer.parseInt(m.group(1)));
                        calender.set(Calendar.MINUTE, Integer.parseInt(m.group(2)));
                        calender.set(Calendar.SECOND, 0);
                        calender.set(Calendar.AM_PM, m.group(3).equals("AM") ? Calendar.AM : Calendar.PM);
                        break;
                    }
                    p = Pattern.compile("^([0-9]{1,2}):([0-9]{1,2}):([0-9]{1,2}) (AM|PM)$");
                    m = p.matcher(convStr);
                    if (m.find()) {
                        calender.set(Calendar.HOUR, Integer.parseInt(m.group(1)));
                        calender.set(Calendar.MINUTE, Integer.parseInt(m.group(2)));
                        calender.set(Calendar.SECOND, Integer.parseInt(m.group(3)));
                        calender.set(Calendar.AM_PM, m.group(4).equals("AM") ? Calendar.AM : Calendar.PM);
                        break;
                    }

                    throw new xTalkException("Cannot parse this date with convert");
                }

                if (next + 1 < stringList.size() &&
                        stringList.get(next + 1).equalsIgnoreCase("dateItems")) {
                    // dateitems
                    // Year Month Day Hour Minute Second Day of week
                    String dateItemStr = "";
                    dateItemStr += calender.get(Calendar.YEAR) + ",";
                    dateItemStr += calender.get(Calendar.MONTH) + 1 + ",";
                    dateItemStr += calender.get(Calendar.DAY_OF_MONTH) + ",";
                    dateItemStr += calender.get(Calendar.HOUR_OF_DAY) + ",";
                    dateItemStr += calender.get(Calendar.MINUTE) + ",";
                    dateItemStr += calender.get(Calendar.SECOND) + ",";
                    dateItemStr += calender.get(Calendar.DAY_OF_WEEK);
                    result.theResult = dateItemStr;
                    result.ret = 0;
                } else {
                    throw new xTalkException("Conversion with convert to is unknown");
                }
            }
            case "find" -> {
                int next = 1;
                if (next >= stringList.size()) throw new xTalkException("find is unknown");
                OField fld = null;
                int j = 2;
                for (; j < stringList.size(); j++) {
                    if (stringList.get(j).equalsIgnoreCase("in") && j + 1 < stringList.size()) {
                        OObject obj = getObject(stringList, typeAry, j + 1, stringList.size() - 1, memData, object, target);
                        if (obj != null && obj.objectType.equals("field")) {
                            fld = (OField) obj;
                        } else {
                            throw new xTalkException("field to search with find is unknown");
                        }
                        break;
                    }
                }
                next = 1;
                if (stringList.get(next).equalsIgnoreCase("string")) {
                    // TODO:
                    next++;
                } else if (stringList.get(next).equalsIgnoreCase("chars")) {
                    // TODO:
                    next++;
                } else if (stringList.get(next).equalsIgnoreCase("word")) {
                    // TODO:
                    next++;
                } else if (stringList.get(next).equalsIgnoreCase("whole")) {
                    // TODO:
                    next++;
                } else {

                }
                String findStr = Evalution(stringList, typeAry, next, j - 1, memData, object, target);
                findStr = findStr.toLowerCase();
                if (fld != null) {
                    String text = fld.getText().toLowerCase();
                    int offset = text.indexOf(findStr);
                    if (offset == -1) {
                        result.theResult = "not found";
                        result.ret = 0;
                    } else {
                        PCARD.pc.foundObject = fld.getShortName() + " of " + fld.parent.getShortName();
                        PCARD.pc.foundIndex = offset;
                        PCARD.pc.foundText = fld.getText().substring(offset, offset + findStr.length());
                        fld.fld.setSelectionStart(PCARD.pc.foundIndex);
                        fld.fld.setSelectionEnd(PCARD.pc.foundIndex + findStr.length());
                    }
                } else {
                    boolean isFound = false;
                    for (int i = 0; i < PCARD.pc.stack.cdCacheList.size(); i++) {
                        OCard card = PCARD.pc.stack.cdCacheList.get(i);
                        for (int k = 0; k < card.fldList.size(); k++) {
                            OField fld2 = card.fldList.get(k);
                            String text = fld2.getText().toLowerCase();
                            int offset = text.indexOf(findStr);
                            if (offset != -1) {
                                PCARD.pc.foundObject = fld2.getShortName() + " of " + card.getShortName();
                                PCARD.pc.foundIndex = offset;
                                PCARD.pc.foundText = fld2.getText().substring(offset, offset + findStr.length());
                                doScriptLine("go " + card.getShortName(), object, target, memData, 0);
                                fld2.fld.setSelectionStart(PCARD.pc.foundIndex);
                                fld2.fld.setSelectionEnd(PCARD.pc.foundIndex + findStr.length());
                                isFound = true;
                                break;
                            }
                        }
                        if (isFound) break;
                        for (int k = 0; k < card.bgfldList.size(); k++) {
                            OBgFieldData bgfld = card.bgfldList.get(k);
                            String text = bgfld.text.toLowerCase();
                            int offset = text.indexOf(findStr);
                            if (offset != -1) {
                                PCARD.pc.foundObject = "bg fld id " + bgfld.id + " of " + card.getShortName();
                                PCARD.pc.foundIndex = offset;
                                PCARD.pc.foundText = bgfld.text.substring(offset, offset + findStr.length());
                                doScriptLine("go " + card.getShortName(), object, target, memData, 0);
                                OField fld2 = card.GetBgFldbyId(bgfld.id);
                                fld2.fld.setSelectionStart(PCARD.pc.foundIndex);
                                fld2.fld.setSelectionEnd(PCARD.pc.foundIndex + findStr.length());
                                isFound = true;
                                break;
                            }
                        }
                        if (isFound) break;
                    }
                    if (!isFound) {
                        for (int i = 0; i < PCARD.pc.stack.bgCacheList.size(); i++) {
                            OBackground bg = PCARD.pc.stack.bgCacheList.get(i);
                            for (int k = 0; k < bg.fldList.size(); k++) {
                                OField fld2 = bg.fldList.get(k);
                                String text = fld2.getText().toLowerCase();
                                int offset = text.indexOf(findStr);
                                if (offset != -1) {
                                    PCARD.pc.foundObject = fld2.getShortName() + " of " + bg.getShortName();
                                    PCARD.pc.foundIndex = offset;
                                    PCARD.pc.foundText = fld2.getText().substring(offset, offset + findStr.length());
                                    for (int m = 0; m < PCARD.pc.stack.cdCacheList.size(); m++) {
                                        OCard card = PCARD.pc.stack.cdCacheList.get(m);
                                        if (card.bgid == bg.id) {
                                            doScriptLine("go " + card.getShortName(), object, target, memData, 0);
                                            break;
                                        }
                                    }
                                    fld2.fld.setSelectionStart(PCARD.pc.foundIndex);
                                    fld2.fld.setSelectionEnd(PCARD.pc.foundIndex + findStr.length());
                                    isFound = true;
                                    break;
                                }
                            }
                            if (isFound) break;
                        }
                    }
                    if (!isFound) {
                        result.theResult = "not found";
                        result.ret = 0;
                    }
                }
            }
            case "type" -> {
                int next = 1;
                if (next >= stringList.size()) throw new xTalkException("type is unknown");

                int j;
                for (j = next + 1; j < stringList.size(); j++) {
                    if (stringList.get(j).equalsIgnoreCase("with")) {
                        break;
                    }
                }
                String withKey = "";
                if (j + 1 < stringList.size()) {
                    withKey = stringList.get(j + 1).toLowerCase();
                }

                String tmpstr = stringList.get(next).toUpperCase();
                Robot rb = null;
                try {
                    rb = new Robot();
                } catch (AWTException e) {
                    e.printStackTrace();
                }
                if (rb != null) {
                    if (withKey.contains("control")) rb.keyPress(KeyEvent.VK_CONTROL);
                    if (withKey.contains("shift")) rb.keyPress(KeyEvent.VK_SHIFT);
                    if (withKey.contains("option") || withKey.contains("alt")) rb.keyPress(KeyEvent.VK_ALT);
                    if (withKey.contains("command") || withKey.contains("cmd")) rb.keyPress(KeyEvent.VK_META);
                    if (!withKey.isEmpty()) rb.delay(200);
                    for (int i = 0; i < tmpstr.length(); i++) {
                        rb.keyPress(tmpstr.charAt(i));
                        rb.delay(40);
                        rb.keyRelease(tmpstr.charAt(i));
                    }
                    if (withKey.contains("control")) rb.keyRelease(KeyEvent.VK_CONTROL);
                    if (withKey.contains("shift")) rb.keyRelease(KeyEvent.VK_SHIFT);
                    if (withKey.contains("option") || withKey.contains("alt")) rb.keyRelease(KeyEvent.VK_ALT);
                    if (withKey.contains("command") || withKey.contains("cmd")) rb.keyRelease(KeyEvent.VK_META);
                }
            }
            case "start" -> {
                int next = 1;
                if (next >= stringList.size()) throw new xTalkException("start is unknown");
                if (!stringList.get(next).equals("using")) throw new xTalkException("start is unknown");
                next++;
                if (next >= stringList.size()) throw new xTalkException("start using is unknown");
                if (!stringList.get(next).equals("stack"))
                    throw new xTalkException("Please specify a stack with start using");
                next++;
                if (next >= stringList.size()) throw new xTalkException("start using stack is unknown");

                String pathStr = Evalution(stringList, typeAry, next, stringList.size() - 1, memData, object, target);

                // Double using not allowed
                if (PCARD.pc.stack.usingStacks != null) {
                    for (int i = 0; i < PCARD.pc.stack.usingStacks.size(); i++) {
                        if (pathStr.equals(PCARD.pc.stack.usingStacks.get(i).path)) {
                            PCARD.pc.stack.usingStacks.remove(i);
                            break;
                        }
                    }
                }

                OStack ostack = new OStack(PCARD.pc);
                OStack.openStackFile(pathStr, true);
                if (PCARD.pc.stack.usingStacks != null && PCARD.pc.stack.usingStacks.size() <= 8) {
                    PCARD.pc.stack.usingStacks.add(ostack);
                }
            }
            case "stop" -> {
                int next = 1;
                if (next >= stringList.size()) throw new xTalkException("stop is unknown");
                if (!stringList.get(next).equals("using")) throw new xTalkException("stop is unknown");
                next++;
                if (next >= stringList.size()) throw new xTalkException("stop using is unknown");
                if (!stringList.get(next).equals("stack"))
                    throw new xTalkException("Please specify a stack with stop using");
                next++;
                if (next >= stringList.size()) throw new xTalkException("stop using stack is unknown");

                String pathStr = Evalution(stringList, typeAry, next, stringList.size() - 1, memData, object, target);


                for (int i = 0; i < PCARD.pc.stack.usingStacks.size(); i++) {
                    if (pathStr.equals(PCARD.pc.stack.usingStacks.get(i).path)) {
                        PCARD.pc.stack.usingStacks.remove(i);
                        break;
                    }
                }
            }
            case "debug" -> {
                int next = 1;
                if (next < stringList.size()) {
                    if (stringList.get(next).equalsIgnoreCase("sound")) {
                        next++;
                        if (next < stringList.size()) {
                            if (stringList.get(next).equalsIgnoreCase("on")) {
                                TSound.use = true;
                            } else if (stringList.get(next).equalsIgnoreCase("off")) {
                                TSound.use = false;
                            }
                        }
                    } else if (stringList.get(next).equalsIgnoreCase("checkpoint")) {
                        TTalk.tracemode = 2;
                        ScriptEditor.setTracemode();
                    } else if (stringList.get(next).equalsIgnoreCase("wait")) {
                        next++;
                        if (next < stringList.size()) {
                            TTalk.wait = Integer.parseInt(stringList.get(next));
                        }
                    }
                }
            }
            case "about" -> {
                int next = 1;
                if (next < stringList.size()) {
                    if (stringList.get(next).equalsIgnoreCase("this")) {
                        new GDialog(null, PCARD.AppName + " " + PCARD.longVersion,
                                null, "OK", null, null);
                    }
                }
            }
            case "beep" -> java.awt.Toolkit.getDefaultToolkit().beep();
            case "flash" -> {
                int next = 1;
                int cnt;
                if (next >= stringList.size()) cnt = 3;
                else cnt = Integer.parseInt(stringList.get(next));

                // Prepare inverted image
                BufferedImage off = new BufferedImage(PCARD.pc.stack.width, PCARD.pc.stack.height, BufferedImage.TYPE_INT_BGR);
                Graphics2D offg = off.createGraphics();

                for (int i = 0; i < cnt * 2; i++) {
                    PCARD.pc.mainPane.paintUnscaled(offg);
                    offg.setXORMode(Color.white);
                    offg.fillRect(0, 0, PCARD.pc.stack.width, PCARD.pc.stack.height);
                    PCARD.pc.mainPane.getGraphics().drawImage(off, 0, 0, PCARD.pc.stack.width, PCARD.pc.stack.height, PCARD.pc);

                    try {
                        sleep(50);
                    } catch (InterruptedException e) {
                    }
                }
                PCARD.pc.mainPane.repaint();
            }
        }
        /*
         * --add to
         * --answer [with]
         * --answer file [of type]
         * arrowkey
         * --ask [with]
         * ask password
         * --ask file [with]
         * --beep
         * --choose x tool
         * choose tool i
         * --click at [with]
         * --close file
         * close printing
         * --close window
         * controlKey
         * --convert [date] to dateitems
         * Other than convert [date] to dateitems
         * create menu
         * create stack
         * --create btn/fld
         * (debug)
         * --debug checkpoint
         * --debug sound on/off
         * --**debug wait <time>
         * --delete
         * (dial)
         * --disable
         * --divide
         * --do
         * --doMenu
         * --drag from
         * --edit script of
         * --** edit picture <filepath>
         * --enable
         * enterKey
         * --find in
         * functionKey
         * --get
         * --go [to]
         * help
         * (hide menubar)
         * hide titlebar
         * --hide
         * --lock screen
         * --lock messages
         * mark card
         * mark cards
         * --multiply
         * --open
         * --open file
         * open printing
         * open report printing
         * --palette
         * --picture
         * --play
         * --play stop
         * --pop card
         * print
         * --push card
         * --put
         * --put into
         * --put after
         * --put before
         * put // menu
         * --read from file until/at/for
         * --reset menubar
         * reset paint
         * returnKey
         * save stack x as
         * --select
         * --select chunk of field (line only)
         * select text of field
         * --select empty
         * --send to
         * --set to
         * show groups
         * --show [at]
         * show marked cards
         * show number cards
         * show menubar
         * --show all cards
         * --sort container
         * sort card
         * --start using stack
         * --stop using stack
         * --subtract from
         * tabkey
         * --type [with]
         * --unlock messages
         * --unlock screen
         * unmark
         * --visual [effect][to]
         * --wait [for] [seconds]
         * --wait until/while
         * --write to file [at]
         */

        // timeIsMoney("CommandExec:",timestart,20);
        return result;
    }

    private static String convertFromMacPath(String path) {
        // Convert : to separatorChar
        while (path.contains(":")) {
            int index = path.indexOf(":");
            path = path.substring(0, index) + File.separatorChar + path.substring(index + 1);
        }
        return path;
    }

    static String Evalution(
            String string, MemoryData memData, OObject object, OObject target)
            throws xTalkException {
        // long timestart = System.currentTimeMillis();
        if (string.isEmpty()) {
            return "";
        }

        ArrayList<String> stringList = new ArrayList<>();
        ArrayList<wordType> typeList = new ArrayList<>();

        fullresolution(string, stringList, typeList, memData.treeset, false);

        wordType[] typeAry = new wordType[typeList.size()];
        for (int i = 0; i < typeList.size(); i++) {
            typeAry[i] = typeList.get(i);
        }

        // timeIsMoney("Evalution1:",timestart,21);

        return Evalution(stringList, typeAry, 0, stringList.size() - 1, memData, object, target);
    }

    private static String Evalution(
            ArrayList<String> stringList, wordType[] typeAry, int start, int end,
            MemoryData memData, OObject object, OObject target)
            throws xTalkException {
        // Interpret/operate on words entered in the list
        // When the number of words decreases, keep the left side and set the right side to wordType.NOP

//        String hintstr = "";
//        for (int i = start; i <= end; i++) {
//            hintstr += stringList.get(i) + " ";
//        }
//        System.out.println("  " + hintstr);
//        String hintstr2 = "";
//        for (int i = start; i <= end; i++) {
//            if (typeList.get(i) == wordType.X) hintstr2 += "value ";
//            if (typeList.get(i) == wordType.STRING) hintstr2 += "STRING ";
//            if (typeList.get(i) == wordType.LFUNC) hintstr2 += "LFUNC ";
//            if (typeList.get(i) == wordType.LBRACKET) hintstr2 += "LBRACKET ";
//            if (typeList.get(i) == wordType.RBRACKET) hintstr2 += "RBRACKET ";
//            if (typeList.get(i) == wordType.OPERATOR) hintstr2 += "OP ";
//        }
//        System.out.println(hintstr2);

//        long timestart = System.currentTimeMillis();

        for (int i = end; i >= start; i--) {
            if (typeAry[i] == wordType.NOP) {
                end--;
            } else break;
        }
        if (start == end) {
            if (typeAry[start] == wordType.STRING
//                    || stringList.get(start).length()>=1 && Character.isDigit(stringList.get(start).charAt(0))
            ) {
                return stringList.get(start);
            }
        }

        // Since toLowerCase and intern have been done in advance, they can be compared with ==

        // 1. Parentheses
        for (int i = start; i <= end; i++) {
            if (typeAry[i] == wordType.LBRACKET) {
                // Find the corresponding closing parenthesis
                int depth = 0;
                int j = i + 1;
                for (; j <= end; j++) {
                    if (typeAry[j] == wordType.LBRACKET) depth++;
                    else if (typeAry[j] == wordType.LFUNC) depth++;
                    else if (typeAry[j] == wordType.RBRACKET || typeAry[j] == wordType.RFUNC) {
                        if (depth > 0) depth--;
                        else {
                            String value = Evalution(stringList, typeAry, i + 1, j - 1, memData, object, target);
                            stringList.set(i, value);
                            typeAry[i] = wordType.STRING;
                            for (int k = i + 1; k <= j; k++) {
                                typeAry[k] = wordType.NOP;
                            }
                            i = j;
                            break;
                        }
                    }
                }
                if (j > end)
                    throw new xTalkException("No matching parenthesis");
//            } else if (typeAry[i] == wordType.RBRACKET) {
//                // Enters here for functions, so temporary workaround
//                int j;
//                for (j = start; j <= end; j++) {
//                    if (typeAry[j] == wordType.LFUNC) break;
//                }
//                if (j > end)
//                    throw new xTalkException("No matching parenthesis");
            }
        }

        // Function call with parentheses
        for (int i = start; i <= end; i++) {
            if (typeAry[i] == wordType.LFUNC) {
                if (i > start /* && typeAry[i-1]==wordType.X */) {
                    int j = i + 1;
                    int nest = 1;
                    for (; j <= end; j++) {
                        if (typeAry[j] == wordType.LBRACKET)
                            nest++;
                        if (typeAry[j] == wordType.LFUNC)
                            nest++;
                        if (typeAry[j] == wordType.RBRACKET || typeAry[j] == wordType.RFUNC) {
                            nest--;
                            if (nest == 0) {
                                int o = 0;
                                for (int m = i + 1; m <= j; m++) {
                                    if ((typeAry[m] == wordType.COMMA_FUNC || typeAry[m] == wordType.COMMA) || m == j) {
                                        o++;
                                    }
                                }
                                String[] paramAry = new String[o];
                                int n = i + 1;
                                o = 0;
                                for (int m = i + 1; m <= j; m++) {
                                    if ((typeAry[m] == wordType.COMMA_FUNC || typeAry[m] == wordType.COMMA) || m == j) {
                                        paramAry[o] = Evalution(stringList, typeAry, n, m - 1, memData, object, target);
                                        o++;
                                        n = m + 1;
                                    }
                                }
                                int offset = 0;
                                String funcName = stringList.get(i - 1);
                                if (funcName.equalsIgnoreCase("of")) {
                                    offset = -1;
                                    funcName = stringList.get(i - 2);
                                    if (i - 3 >= start && stringList.get(i - 3).equals("the")) {
                                        offset = -2;
                                    }
                                }
                                Result funcres = talk.ReceiveMessage(funcName, paramAry, object, target, memData, new Result(), true, false, false, 0);
                                if (funcres != null && funcres.theResult != null) {
                                    stringList.set(i - 1 + offset, funcres.theResult);
                                    typeAry[i - 1 + offset] = wordType.STRING;
                                } else {
                                    stringList.set(i - 1 + offset, "");
                                    typeAry[i - 1 + offset] = wordType.STRING;
                                }
                                for (int k = i + offset; k <= j; k++) {
                                    typeAry[k] = wordType.NOP;
                                }
                                break;
                            }
                        }
                    }
                    if (j > end) throw new xTalkException("( is invalid");
                }
            }
        }

        // HyperTalk constants
        for (int i = start; i <= end; i++) {
            if (typeAry[i] == wordType.CONST) {
                String str = stringList.get(i) /* .toLowerCase() */;
                /* if(constantSet.contains(str)) */
                {
                    switch (str) {
                        case "down" -> {
                            stringList.set(i, "down");
                            typeAry[i] = wordType.STRING;
                        }
                        case "empty" -> {
                            stringList.set(i, "");
                            typeAry[i] = wordType.STRING;
                        }
                        case "false" -> {
                            stringList.set(i, "false");
                            typeAry[i] = wordType.STRING;
                        }
                        case "formfeed" -> {
                            stringList.set(i, "\f");
                            typeAry[i] = wordType.STRING;
                        }
                        case "carriagereturn" -> {
                            stringList.set(i, "\r");
                            typeAry[i] = wordType.STRING;  // Add
                        }
                        case "linefeed" -> {
                            stringList.set(i, "\n");
                            typeAry[i] = wordType.STRING;
                        }
                        case "pi" -> {
                            stringList.set(i, "3.14159265358979323846");
                            typeAry[i] = wordType.STRING;
                        }
                        case "quote" -> {
                            stringList.set(i, "\"");
                            typeAry[i] = wordType.STRING;
                        }
                        case "return" -> {
                            // stringList.set(i,"\r");
                            stringList.set(i, "\n");
                            typeAry[i] = wordType.STRING;  // Change newline code
                        }
                        case "space" -> {
                            stringList.set(i, " ");
                            typeAry[i] = wordType.STRING;
                        }
                        case "tab" -> {
                            stringList.set(i, "\t");
                            typeAry[i] = wordType.STRING;
                        }
                        case "comma" -> {
                            stringList.set(i, ",");
                            typeAry[i] = wordType.STRING;
                        }
                        case "true" -> {
                            stringList.set(i, "true");
                            typeAry[i] = wordType.STRING;
                        }
                        case "up" -> {
                            stringList.set(i, "up");
                            typeAry[i] = wordType.STRING;
                        }
                        case "zero" -> {
                            stringList.set(i, "0");
                            typeAry[i] = wordType.STRING;
                        }
                        case "one" -> {
                            stringList.set(i, "1");
                            typeAry[i] = wordType.STRING;
                        }
                        case "two" -> {
                            stringList.set(i, "2");
                            typeAry[i] = wordType.STRING;
                        }
                        case "three" -> {
                            stringList.set(i, "3");
                            typeAry[i] = wordType.STRING;
                        }
                        case "four" -> {
                            stringList.set(i, "4");
                            typeAry[i] = wordType.STRING;
                        }
                        case "five" -> {
                            stringList.set(i, "5");
                            typeAry[i] = wordType.STRING;
                        }
                        case "six" -> {
                            stringList.set(i, "6");
                            typeAry[i] = wordType.STRING;
                        }
                        case "seven" -> {
                            stringList.set(i, "7");
                            typeAry[i] = wordType.STRING;
                        }
                        case "eight" -> {
                            stringList.set(i, "8");
                            typeAry[i] = wordType.STRING;
                        }
                        case "nine" -> {
                            stringList.set(i, "9");
                            typeAry[i] = wordType.STRING;
                        }
                    }
                }
            }
        }

        // Variable
        for (int i = start; i <= end; i++) {
            if (typeAry[i] == wordType.VARIABLE ||
                    typeAry[i] == wordType.X && ((i - 1 < start) || (!stringList.get(i - 1).equals("the")))) {
                String str = getVariable(memData, stringList.get(i));
                if (str != null) {
                    stringList.set(i, str);
                    typeAry[i] = wordType.STRING;
                }
            }
        }

        // Property
        for (int i = end; i >= start; i--) {
            // for(int i=start; i<=end; i++) {   // There is a of the x etc. so look from the back
            if (typeAry[i] == wordType.PROPERTY) {
                if (stringList.get(i).equals("the") &&
                        ((i + 1 <= end && 0 != stringList.get(i + 1).compareToIgnoreCase("target")) &&
                                (i + 2 > end || 0 != stringList.get(i + 2).compareToIgnoreCase("of")) &&
                                        (((0 == stringList.get(i + 1).compareToIgnoreCase("short") ||
                                                0 == stringList.get(i + 1).compareToIgnoreCase("long")) &&
                                                (i + 3 > end || 0 != stringList.get(i + 3).compareToIgnoreCase("of"))
                                        ) || (
                                                (0 != stringList.get(i + 1).compareToIgnoreCase("short") &&
                                                        0 != stringList.get(i + 1).compareToIgnoreCase("long")) &&
                                                        (i + 3 > end || 0 != stringList.get(i + 3).compareToIgnoreCase("of"))
                                        ))
                        )) {
                    if (i > end - 1) throw new xTalkException("No property name after the");
                    String str = stringList.get(i + 1);
                    if (str.equalsIgnoreCase("short") || str.equalsIgnoreCase("long")) {
                        str += " " + stringList.get(i + 2);
                        typeAry[i + 2] = wordType.NOP;
                    }
                    stringList.set(i, TUtil.CallSystemFunction(str, null, target, memData, true).theResult);
                    typeAry[i] = wordType.STRING;
                    typeAry[i + 1] = wordType.NOP;
                } else if (i <= end - 1 && stringList.get(i + 1).equals("of") && (typeAry[i + 1] == wordType.X || typeAry[i + 1] == wordType.CHUNK || typeAry[i + 1] == wordType.OBJECT || typeAry[i + 1] == wordType.OF_PROP) &&
                        /*stringList.get(i)!="number" &&*/ !stringList.get(i).equals("chars") &&
                        !stringList.get(i).equals("characters") && !stringList.get(i).equals("items") &&
                        !stringList.get(i).equals("words") && !stringList.get(i).equals("lines") &&
                        !stringList.get(i).equals("cds") && !stringList.get(i).equals("cards") &&
                        !stringList.get(i).equals("btns") && !stringList.get(i).equals("buttons") &&
                        !stringList.get(i).equals("flds") && !stringList.get(i).equals("fields") &&
                        !stringList.get(i).equals("char") && !stringList.get(i).equals("character") &&
                        !stringList.get(i).equals("item") && !stringList.get(i).equals("word") &&
                        !stringList.get(i).equals("line")
                ) {
                    if (i + 1 > end - 1) throw new xTalkException("No object name after of");
                    String str = stringList.get(i);
                    ObjResult objres = null;
                    try {
                        objres = getObjectfromList(stringList, typeAry, i + 2, object, target);
                    } catch (xTalkException e) {
                    }
                    if (objres != null && objres.obj != null) {

//                        String objstr = "";
//                        for (int k = i + 2; k < i + 2 + objres.cnt; k++) objstr += stringList.get(k);

                        int offset = 0;
                        if (i > start && (0 == stringList.get(i - 1).compareToIgnoreCase("short") || 0 == stringList.get(i - 1).compareToIgnoreCase("long"))) {
                            str = stringList.get(i - 1) + " " + str;
                            i--;
                            offset++;
                        }
                        if (i > start && 0 == stringList.get(i - 1).compareToIgnoreCase("the")) {
                            i--;
                            offset++;
                        }
                        stringList.set(i, TUtil.getProperty(str, objres.obj, target, memData));
                        typeAry[i] = wordType.STRING;
//                        for (int j = i + 1; j < i + 2 + objres.cnt && j <= end; j++) {
                        for (int j = i + 1; j <= i + 1 + offset + objres.cnt && j <= end; j++) {
                            typeAry[j] = wordType.NOP;
                        }
                    }
                }
            }
        }

        // Function called with the
        for (int i = start; i <= end; i++) {
            if (typeAry[i] == wordType.THE_FUNC) {
                if (i > end - 1) throw new xTalkException("No function name after the");
                String str = stringList.get(i + 1);
                stringList.set(i, TUtil.CallSystemFunction(str, new String[0], target, memData, true).theResult);
                typeAry[i] = wordType.STRING;
                typeAry[i + 1] = wordType.NOP;
            }
        }

        // Chunk expression
        for (int i = end; i >= start; i--) {
            if (typeAry[i] == wordType.CHUNK) {
                String str = stringList.get(i)/*.toLowerCase()*/;
                if (str.equals("char") || str.equals("character") ||
                        str.equals("item") || str.equals("word") ||
                        str.equals("line")) {
                    if (i > start && (stringList.get(i - 1).equals("any") ||
                            stringList.get(i - 1).equals("last") || stringList.get(i - 1).equals("first"))) {
                        continue;
                    }
                    String chunkStart = "";
                    String chunkEnd = null;
                    int j;
                    for (j = i + 2; j < end; j++) {
                        if (typeAry[j] == wordType.NOP) continue;
                        String str2 = stringList.get(j);
                        if (0 == str2.compareToIgnoreCase("to")) {
                            chunkStart = Evalution(stringList, typeAry, i + 1, j - 1, memData, object, target);
                            for (int k = j + 1; k < end; k++) {
                                str2 = stringList.get(k);
                                if (0 == str2.compareToIgnoreCase("of") && typeAry[k] != wordType.NOP) {
                                    chunkEnd = Evalution(stringList, typeAry, j + 1, k - 1, memData, object, target);
                                    j = k;
                                    break;
                                }
                            }
                            break;
                        } else if (0 == str2.compareToIgnoreCase("of")) {
                            chunkStart = Evalution(stringList, typeAry, i + 1, j - 1, memData, object, target);
                            break;
                        }
                    }
                    int k;
                    for (k = j + 2; k <= end; k++) {
                        if (typeAry[k] != wordType.NOP && typeAry[k] != wordType.STRING && typeAry[k] != wordType.X && typeAry[k] != wordType.OBJECT && typeAry[k] != wordType.OF_OBJ) {
                            break;
                        }
                    }
                    if (k - 1 > end) k = end + 1;
                    if (j + 1 > end) j = end - 1;
                    if (j <= i) break;// ####
                    String value = Evalution(stringList, typeAry, j + 1, k - 1, memData, object, target);
                    stringList.set(i, getChunk(str, chunkStart, chunkEnd, value));
                    typeAry[i] = wordType.STRING;
                    for (int m = i + 1; m <= k - 1; m++) {
//                        stringList.set(m, "");
                        typeAry[m] = wordType.NOP;
                    }
                }
            }
        }

        // Chunk expression 2 (any, last)
        for (int i = end; i >= start + 1; i--) {
            if (typeAry[i] == wordType.CHUNK) {
                String str = stringList.get(i)/*.toLowerCase() */;
                if (str.equals("char") || str.equals("character") ||
                        str.equals("item") || str.equals("word") ||
                        str.equals("line")) {
                    if (0 == stringList.get(i - 1).compareToIgnoreCase("any") ||
                            0 == stringList.get(i - 1).compareToIgnoreCase("last") ||
                            0 == stringList.get(i - 1).compareToIgnoreCase("first")) {
                        int k;
                        for (k = i + 2; k <= end; k++) {
                            if (typeAry[k] != wordType.NOP && typeAry[k] != wordType.STRING && typeAry[k] != wordType.OBJECT && typeAry[k] != wordType.X) {
                                k--;
                                break;
                            }
                        }
                        if (k > end) k = end;
                        String value = Evalution(stringList, typeAry, i + 2, k, memData, object, target);
                        if (0 == stringList.get(i - 1).compareToIgnoreCase("last")) {
                            int chunk = getNumberOfChunk(str + "s", value);
                            if (chunk <= 0) stringList.set(i - 1, "");
                            else stringList.set(i - 1, getChunk(str, Integer.toString(chunk), "", value));
                        } else if (0 == stringList.get(i - 1).compareToIgnoreCase("first")) {
                            stringList.set(i - 1, getChunk(str, "1", "", value));
                        } else if (0 == stringList.get(i - 1).compareToIgnoreCase("any")) {
                            int number = getNumberOfChunk(str + "s", value);
                            if (number <= 0) stringList.set(i - 1, "");
                            else
                                stringList.set(i - 1, getChunk(str, Integer.toString((int) (number * Math.random()) + 1), "", value));
                        }
                        typeAry[i - 1] = wordType.STRING;
                        for (int m = i; m <= k; m++) {
                            typeAry[m] = wordType.NOP;
                        }
                    }
                }
            }
        }

        // Chunk expression 3 (number of xxxs)
        for (int i = end; i >= start + 2; i--) {
            if (typeAry[i] == wordType.X) {
                String str = stringList.get(i)/*.toLowerCase()*/;
                if (str.equals("chars") || str.equals("characters") ||
                        str.equals("items") || str.equals("words") ||
                        str.equals("lines")) {
                    if (0 == stringList.get(i - 1).compareToIgnoreCase("of") &&
                            0 == stringList.get(i - 2).compareToIgnoreCase("number") &&
                            i + 1 <= end &&
                            (0 == stringList.get(i + 1).compareToIgnoreCase("of") ||
                                    0 == stringList.get(i + 1).compareToIgnoreCase("in"))) {
                        int k;
                        for (k = i + 2; k <= end; k++) {
                            if (typeAry[k] != wordType.NOP && typeAry[k] != wordType.STRING && typeAry[k] != wordType.X && typeAry[k] != wordType.OBJECT) {
                                k--;
                                break;
                            }
                        }
                        if (k > end) k = end;
                        String value = Evalution(stringList, typeAry, i + 2, k, memData, object, target);
                        if (i - 3 >= start && stringList.get(i - 3).equalsIgnoreCase("the")) {
                            i--;
                        }
                        stringList.set(i - 2, Integer.toString(getNumberOfChunk(str, value)));
                        typeAry[i - 2] = wordType.STRING;
                        for (int m = i - 1; m <= k; m++) {
                            typeAry[m] = wordType.NOP;
                        }
                    }
                } else if (str.equals("btns") || str.equals("buttons") ||
                        str.equals("flds") || str.equals("fields") ||
                        str.equals("cds") || str.equals("cards") ||
                        str.equals("bgs") || str.equals("bkgnds") || str.equals("backgrounds")) {
                    int offset = 0;
                    boolean bg_flag = false;
                    if (0 == stringList.get(i - 1).compareToIgnoreCase("cd") || 0 == stringList.get(i - 1).compareToIgnoreCase("card")) {
                        offset = 1;
                    } else if (0 == stringList.get(i - 1).compareToIgnoreCase("bg") || 0 == stringList.get(i - 1).compareToIgnoreCase("bkgnd") || 0 == stringList.get(i - 1).compareToIgnoreCase("background")) {
                        bg_flag = true;
                        offset = 1;
                    }
                    if (i - 2 - offset >= start &&
                            0 == stringList.get(i - 1 - offset).compareToIgnoreCase("of") &&
                            0 == stringList.get(i - 2 - offset).compareToIgnoreCase("number")) {
                        // parent
                        OObject parent = PCARD.pc.stack.curCard;
                        int endoff = 0;
//                        if (object.objectType.equals("card")) parent = object;
//                        else if (object.parent != null && object.parent.objectType.equals("card"))
//                            parent = object.parent;
//                        else if (object.objectType.equals("background")) parent = ((OBackground) object).viewCard;
                        if (0 == stringList.get(i + 1 - offset).compareToIgnoreCase("of") ||
                                0 == stringList.get(i + 1 - offset).compareToIgnoreCase("in")) {
                            ObjResult oResult = getObjectfromList(stringList, typeAry, (i + 1 - offset) + 1, object, target);
                            parent = oResult.obj;
                            endoff = oResult.cnt + 1;
                        }
                        System.out.println("number of 's parent:" + parent.getShortName());
                        if (parent != null) {
                            // search
                            int number = -1;
                            switch (str) {
                                case "btns", "buttons" -> {
                                    if (bg_flag) {
                                        if (!parent.objectType.equals("background")) parent = ((OCard) parent).bg;
                                    }
                                    number = ((OCardBase) parent).btnList.size();
                                }
                                case "flds", "fields" -> {
                                    if (bg_flag) {
                                        if (!parent.objectType.equals("background")) parent = ((OCard) parent).bg;
                                    }
                                    number = ((OCardBase) parent).fldList.size();
                                }
                                case "cds", "cards" -> {
                                    if (bg_flag) {
                                        if (!parent.objectType.equals("background")) parent = ((OCard) parent).bg;
                                        OStack st = (parent instanceof OBackground) ? ((OBackground) parent).stack : (OStack) parent;
                                        number = 0;
                                        for (int k = 0; k < st.cardIdList.size(); k++) {
                                            OCard cd = st.GetCardbyId(st.cardIdList.get(k));
                                            if (cd.bgid == parent.id) {
                                                number++;
                                            }
                                        }
                                    } else {
                                        Debug.println("parent: " + parent.getClass());
                                        if (parent instanceof OCard) {
                                            number = ((OCard) parent).stack.cardIdList.size();
                                        } else {
                                            number = ((OStack) parent).cardIdList.size();
                                        }
                                    }
                                }
                                case "bgs", "bkgnds", "backgrounds" -> {
                                    OStack st = (parent instanceof OCard) ? ((OCard) parent).stack : (OStack) parent;
                                    ArrayList<Integer> list = new ArrayList<>();
                                    for (int k = 0; k < st.cardIdList.size(); k++) {
                                        OCard cd = st.GetCardbyId(st.cardIdList.get(k));
                                        int j;
                                        for (j = 0; j < list.size(); j++) {
                                            if (cd.bgid == list.get(j)) {
                                                break;
                                            }
                                        }
                                        if (j >= list.size()) continue;
                                        list.add(cd.bgid);
                                    }
                                    number = list.size();
                                }
                            }
                            if (number == -1)
                                throw new xTalkException("Cannot count with number of");
                            if (i - 3 - offset >= start && stringList.get(i - 3 - offset).equalsIgnoreCase("the")) {
                                offset++;
                            }
                            stringList.set(i - 2 - offset, Integer.toString(number));
                            typeAry[i - 2 - offset] = wordType.STRING;
                            for (int m = i - 2 - offset + 1; m <= i + endoff; m++) {
                                typeAry[m] = wordType.NOP;
                            }
                        }
                    }
                }
            }
        }

        // Function called with of
        for (int i = end; i >= start; i--) {
            if (typeAry[i] == wordType.FUNC) {
                // number of/function call using of
                char c = ' ';
                if (i + 2 <= end) {
                    c = stringList.get(i + 2).charAt(stringList.get(i + 2).length() - 1);
                }
                if (stringList.get(i).equals("number") &&
                        stringList.get(i + 1).equals("of") &&
                        (c == 's' || c == 'S') && (i + 2 > end || !stringList.get(i + 2).equals("chars") &&
                        !stringList.get(i + 2).equals("characters") && !stringList.get(i + 2).equals("items") && !stringList.get(i + 2).equals("words") && !stringList.get(i + 2).equals("lines"))
                ) {
                    // number of xxxs
                    ObjResult objres2 = null;
                    int next = i + 2;
                    if (i + 4 > end - 1) {

                    } else {
                        next = i + 3;
                        if (0 != stringList.get(next).compareToIgnoreCase("of") && 0 != stringList.get(next).compareToIgnoreCase("in"))
                            next++;
                        objres2 = getObjectfromList(stringList, typeAry, next + 1, object, target);
                        if (objres2.obj == null) {
                            objres2.obj = PCARD.pc.stack.curCard;
                            objres2.cnt = 0;
                            next = i + 2;
                        }
                    }
                    if (0 == stringList.get(i + 2).compareToIgnoreCase("cds") || 0 == stringList.get(i + 2).compareToIgnoreCase("cards")) {
                        if (objres2 != null && objres2.obj.objectType.equals("stack")) {
                            OStack st = (OStack) objres2.obj;
                            stringList.set(i, Integer.toString(st.cardIdList.size()));
                            typeAry[i] = wordType.STRING;
                        } else if (objres2 != null && objres2.obj.objectType.equals("background")) {
                            OBackground bg = (OBackground) objres2.obj;
                            int cnt = 0;
                            for (int j = 0; j < bg.stack.cardIdList.size(); j++) {
                                if (OCard.getOCard(bg.stack, bg.stack.cardIdList.get(j), true).bgid == bg.id) {
                                    cnt++;
                                }
                            }
                            stringList.set(i, Integer.toString(cnt));
                            typeAry[i] = wordType.STRING;
                        } else throw new xTalkException("Please specify a background or stack here");
                    } else if (0 == stringList.get(i + 2).compareToIgnoreCase("btns") || 0 == stringList.get(i + 2).compareToIgnoreCase("buttons") ||
                            0 == stringList.get(i + 3).compareToIgnoreCase("btns") || 0 == stringList.get(i + 3).compareToIgnoreCase("buttons")) {
                        if (objres2 == null || objres2.obj.objectType.equals("card") || objres2.obj.objectType.equals("background")) {
                            OCardBase cdbs;
                            if (objres2 == null) cdbs = PCARD.pc.stack.curCard;
                            else cdbs = (OCardBase) objres2.obj;
                            stringList.set(i, Integer.toString(cdbs.btnList.size()));
                            typeAry[i] = wordType.STRING;
                            if (0 == stringList.get(i + 3).compareToIgnoreCase("btns") || 0 == stringList.get(i + 3).compareToIgnoreCase("buttons")) {
                                next++;
                            }
                        } else throw new xTalkException("Please specify a card or background here");
                    } else if (0 == stringList.get(i + 2).compareToIgnoreCase("flds") || 0 == stringList.get(i + 2).compareToIgnoreCase("fields") ||
                            0 == stringList.get(i + 3).compareToIgnoreCase("flds") || 0 == stringList.get(i + 3).compareToIgnoreCase("fields")) {
                        if (objres2 == null || objres2.obj.objectType.equals("card") || objres2.obj.objectType.equals("background")) {
                            OCardBase cdbs;
                            if (objres2 == null) cdbs = PCARD.pc.stack.curCard;
                            else cdbs = (OCardBase) objres2.obj;
                            stringList.set(i, Integer.toString(cdbs.fldList.size()));
                            typeAry[i] = wordType.STRING;
                            if (0 == stringList.get(i + 3).compareToIgnoreCase("flds") || 0 == stringList.get(i + 3).compareToIgnoreCase("fields")) {
                                next++;
                            }
                        } else throw new xTalkException("Please specify a card or background here");
                    } else if (0 == stringList.get(i + 2).compareToIgnoreCase("parts") ||
                            0 == stringList.get(i + 3).compareToIgnoreCase("parts")) {
                        if (objres2 == null || objres2.obj.objectType.equals("card") || objres2.obj.objectType.equals("background")) {
                            OCardBase cdbs;
                            if (objres2 == null) cdbs = PCARD.pc.stack.curCard;
                            else cdbs = (OCardBase) objres2.obj;
                            stringList.set(i, Integer.toString(cdbs.btnList.size() + cdbs.fldList.size()));
                            typeAry[i] = wordType.STRING;
                        } else throw new xTalkException("Please specify a card or background here");
                    }
                    int cnt = 0;
                    if (objres2 != null) cnt += objres2.cnt;
                    for (int j = i + 1; j <= next + cnt && j <= end; j++) {
                        typeAry[j] = wordType.NOP;
                    }
                } else if (!stringList.get(i).equals("number") &&
                        !stringList.get(i).equals("char") && !stringList.get(i).equals("character") &&
                        !stringList.get(i).equals("item") && !stringList.get(i).equals("word") &&
                        !stringList.get(i).equals("line") &&
                        !stringList.get(i).equals("short") &&
                        !stringList.get(i).equals("long")) {
                    // Function call
                    String[] paramAry = new String[1];
                    paramAry[0] = stringList.get(i + 2);
                    Result funcres = TUtil.CallSystemFunction(stringList.get(i), paramAry, target, memData, true);
                    int offset = 0;
                    if (funcres != null && funcres.theResult != null) {
                        if (i - 1 >= start && stringList.get(i - 1).equalsIgnoreCase("the")) {
                            i--;
                            offset++;
                        }
                        stringList.set(i, funcres.theResult);
                        typeAry[i] = wordType.STRING;
                    } else throw new xTalkException("This function is unknown");
                    for (int j = i + 1; j <= i + 2 + offset && j <= end; j++) {
                        typeAry[j] = wordType.NOP;
                    }
                }
            }
        }

//        // Tried moving to the front to allow item random of 2 of x
//        // Property (or function)
//        for (int i = start; i <= end; i++) {
//            if (typeAry[i] == wordType.X) {
//                if (0 == stringList.get(i).compareToIgnoreCase("the") &&
//                        (
//                                (0 != stringList.get(i + 1).compareToIgnoreCase("target")) &&
//                                        (i + 2 > end || 0 != stringList.get(i + 2).compareToIgnoreCase("of")) ||
//                                        (
//                                                (0 == stringList.get(i + 1).compareToIgnoreCase("short") ||
//                                                        0 == stringList.get(i + 1).compareToIgnoreCase("long")) &&
//                                                        (i + 3 > end || 0 != stringList.get(i + 3).compareToIgnoreCase("of"))
//                                        )
//                        )) {
//                    if (i > end - 1) throw new xTalkException("No property name after the");
//                    String str = stringList.get(i + 1);
//                    if (str.equalsIgnoreCase("short") || str.equalsIgnoreCase("long")) {
//                        str += " " + stringList.get(i + 2);
//                        typeAry[i + 2] = wordType.NOP;
//                    }
//                    stringList.set(i, TUtil.getProperty(str, null));
//                    typeAry[i] = wordType.STRING;
//                    typeAry[i + 1] = wordType.NOP;
//                } else if (i <= end - 1 && 0 == stringList.get(i + 1).compareToIgnoreCase("of") && typeAry[i + 1] == wordType.X) {
//                    if (i + 1 > end - 1) throw new xTalkException("No object name after of");
//                    String str = stringList.get(i);
//                    ObjResult objres = null;
//                    objres = getObjectfromList(stringList, typeAry, i + 2, object, target);
//                    if (objres != null && objres.obj != null) {
//                        //
//                        String objstr = "";
//                        for (int k = i + 2; k < i + 2 + objres.cnt; k++) objstr += stringList.get(k);
//                        //
//                        int offset = 0;
//                        if (i > start && (0 == stringList.get(i - 1).compareToIgnoreCase("short") || 0 == stringList.get(i - 1).compareToIgnoreCase("long"))) {
//                            str = stringList.get(i - 1) + " " + str;
//                            i--;
//                            offset++;
//                        }
//                        if (i > start && 0 == stringList.get(i - 1).compareToIgnoreCase("the")) {
//                            i--;
//                            offset++;
//                        }
//                        stringList.set(i, TUtil.getProperty(str, objres.obj));
//                        typeAry[i] = wordType.STRING;
//                        // for(int j=i+1; j<i+2+objres.cnt && j<=end; j++){
//                        for (int j = i + 1; j <= i + 1 + offset + objres.cnt && j <= end; j++) {
//                            typeAry[j] = wordType.NOP;
//                        }
//                    } else {
//                        // number of/function call using of
//                        if (0 == str.compareToIgnoreCase("number")) {
//                            // number of
//                            ObjResult objres2 = null;
//                            int next = i + 2;
//                            if (i + 4 > end - 1) {
//
//                            } else {
//                                next = i + 3;
//                                if (0 != stringList.get(next).compareToIgnoreCase("of")) next++;
//                                objres2 = getObjectfromList(stringList, typeAry, next + 1, object, target);
//                                if (objres2.obj == null) {
//                                    objres2.obj = PCARD.pc.stack.curCard;
//                                    objres2.cnt = 0;
//                                    next = i + 2;
//                                }
//                            }
//                            if (0 == stringList.get(i + 2).compareToIgnoreCase("cds") || 0 == stringList.get(i + 2).compareToIgnoreCase("cards")) {
//                                if (objres2 != null && objres2.obj.objectType == "stack") {
//                                    OStack st = (OStack) objres2.obj;
//                                    stringList.set(i, Integer.toString(st.cardIdList.size()));
//                                    typeAry[i] = wordType.STRING;
//                                } else if (objres2 != null && objres2.obj.objectType == "background") {
//                                    OBackground bg = (OBackground) objres2.obj;
//                                    int cnt = 0;
//                                    for (int j = 0; j < bg.stack.cardIdList.size(); j++) {
//                                        if (OCard.getOCard(bg.stack, bg.stack.cardIdList.get(j), true).bgid == bg.id) {
//                                            cnt++;
//                                        }
//                                    }
//                                    stringList.set(i, Integer.toString(cnt));
//                                    typeAry[i] = wordType.STRING;
//                                } else throw new xTalkException("Please specify a background or stack here");
//                            } else if (0 == stringList.get(i + 2).compareToIgnoreCase("btns") || 0 == stringList.get(i + 2).compareToIgnoreCase("buttons") ||
//                                    0 == stringList.get(i + 3).compareToIgnoreCase("btns") || 0 == stringList.get(i + 3).compareToIgnoreCase("buttons")) {
//                                if (objres2 == null || objres2.obj.objectType.equals("card") || objres2.obj.objectType.equals("background")) {
//                                    OCardBase cdbs;
//                                    if (objres2 == null) cdbs = (OCardBase) PCARD.pc.stack.curCard;
//                                    else cdbs = (OCardBase) objres2.obj;
//                                    stringList.set(i, Integer.toString(cdbs.btnList.size()));
//                                    typeAry[i] = wordType.STRING;
//                                } else throw new xTalkException("Please specify a card or background here");
//                            } else if (0 == stringList.get(i + 2).compareToIgnoreCase("flds") || 0 == stringList.get(i + 2).compareToIgnoreCase("fields") ||
//                                    0 == stringList.get(i + 3).compareToIgnoreCase("flds") || 0 == stringList.get(i + 3).compareToIgnoreCase("fields")) {
//                                if (objres2 == null || objres2.obj.objectType.equals("card") || objres2.obj.objectType.equals("background")) {
//                                    OCardBase cdbs;
//                                    if (objres2 == null) cdbs = (OCardBase) PCARD.pc.stack.curCard;
//                                    else cdbs = (OCardBase) objres2.obj;
//                                    stringList.set(i, Integer.toString(cdbs.fldList.size()));
//                                    typeAry[i] = wordType.STRING;
//                                } else throw new xTalkException("Please specify a card or background here");
//                            } else if (0 == stringList.get(i + 2).compareToIgnoreCase("parts") ||
//                                    0 == stringList.get(i + 3).compareToIgnoreCase("parts")) {
//                                if (objres2 == null || objres2.obj.objectType.equals("card") || objres2.obj.objectType.equals("background")) {
//                                    OCardBase cdbs;
//                                    if (objres2 == null) cdbs = (OCardBase) PCARD.pc.stack.curCard;
//                                    else cdbs = (OCardBase) objres2.obj;
//                                    stringList.set(i, Integer.toString(cdbs.btnList.size() + cdbs.fldList.size()));
//                                    typeAry[i] = wordType.STRING;
//                                } else throw new xTalkException("Please specify a card or background here");
//                            }
//                            int cnt = 0;
//                            if (objres2 != null) cnt += objres2.cnt;
//                            for (int j = i + 1; j <= next + cnt && j <= end; j++) {
//                                typeAry[j] = wordType.NOP;
//                            }
//                        } else {
//                            // Function call
//                            String[] paramAry = new String[1];
//                            paramAry[0] = stringList.get(i + 2);
//                            Result funcres = TUtil.CallSystemMessage(str, paramAry, memData, true);
//                            int offset = 0;
//                            if (funcres != null && funcres.theResult != null) {
//                                if (i - 1 >= start && stringList.get(i - 1).equalsIgnoreCase("the")) {
//                                    i--;
//                                    offset++;
//                                }
//                                stringList.set(i, funcres.theResult);
//                                typeAry[i] = wordType.STRING;
//                            } else throw new xTalkException("This function is unknown");
//                            for (int j = i + 1; j <= i + 2 + offset && j <= end; j++) {
//                                typeAry[j] = wordType.NOP;
//                            }
//                        }
//                    }
//                }
//            }
//        }

        // 2a.there is a
        for (int i = start; i <= end; i++) {
            if (typeAry[i] == wordType.OPERATOR) {
                if (stringList.get(i).equals("there") &&
                        i + 1 <= end && typeAry[i + 1] == wordType.OPERATOR_SUB &&
                        0 == stringList.get(i + 1).compareToIgnoreCase("is") &&
                        i + 2 <= end && typeAry[i + 2] == wordType.OPERATOR_SUB &&
                        0 == stringList.get(i + 2).compareToIgnoreCase("a")) {
                    int j = i;
                    ObjResult objres = null;
                    try {
                        objres = getObjectfromList(stringList, typeAry, i + 3, object, target);
                    } catch (xTalkException e) {
                    }
                    if (objres != null && objres.obj != null) {
                        stringList.set(j, "true");
                        typeAry[j] = wordType.STRING;
                        for (int k = j + 1; k < j + 3 + objres.cnt; k++) {
                            typeAry[k] = wordType.NOP;
                        }
                    } else {
                        stringList.set(j, "false");
                        typeAry[j] = wordType.STRING;
                        for (int k = j + 1; k <= end; k++) {
                            if (typeAry[k] == wordType.OPERATOR) break;
                            typeAry[k] = wordType.NOP;
                        }
                    }
                } else if (stringList.get(i).equals("there") &&
                        i + 1 <= end && typeAry[i + 1] == wordType.OPERATOR_SUB &&
                        0 == stringList.get(i + 1).compareToIgnoreCase("is") &&
                        i + 2 <= end && typeAry[i + 2] == wordType.OPERATOR_SUB &&
                        0 == stringList.get(i + 2).compareToIgnoreCase("not") &&
                        i + 3 <= end && typeAry[i + 3] == wordType.OPERATOR_SUB &&
                        0 == stringList.get(i + 3).compareToIgnoreCase("a")) {
                    int j = i;
                    ObjResult objres = null;
                    try {
                        objres = getObjectfromList(stringList, typeAry, i + 4, object, target);
                    } catch (xTalkException e) {
                    }
                    if (objres != null && objres.obj != null) {
                        stringList.set(j, "false");
                        typeAry[j] = wordType.STRING;
                        for (int k = j + 1; k < j + 4 + objres.cnt; k++) {
                            typeAry[k] = wordType.NOP;
                        }
                    } else {
                        stringList.set(j, "true");
                        typeAry[j] = wordType.STRING;
                        for (int k = j + 1; k <= end; k++) {
                            if (typeAry[k] == wordType.OPERATOR) break;
                            typeAry[k] = wordType.NOP;
                        }
                    }
                }
            }
        }

        // Container
        for (int i = start; i <= end; i++) {
            if (typeAry[i] == wordType.OBJECT && (i <= end - 1 && (typeAry[i + 1] == wordType.X || typeAry[i + 1] == wordType.STRING || typeAry[i + 1] == wordType.OBJECT || typeAry[i + 1] == wordType.OF_OBJ) ||
                    stringList.get(i).equals("me") ||
                    stringList.get(i).equals("target") ||
                    stringList.get(i).equals("msg") ||
                    stringList.get(i).equals("message"))) {
                ObjResult objres = null;
                objres = getObjectfromList(stringList, typeAry, i, object, target);
                if (objres != null && objres.obj != null) {
                    if (i + 1 <= end && stringList.get(i).equalsIgnoreCase("the") && stringList.get(i + 1).equalsIgnoreCase("target")) {
                        // Object name for target
                        stringList.set(i, objres.obj.getShortName());
                        typeAry[i] = wordType.STRING;
                        for (int j = i + 1; j < i + objres.cnt && j <= end; j++) {
                            typeAry[j] = wordType.NOP;
                        }
                    } else if (0 == objres.obj.objectType.compareTo("button") || 0 == objres.obj.objectType.compareTo("field")) {
                        stringList.set(i, objres.obj.getText());
                        typeAry[i] = wordType.STRING;
                        for (int j = i + 1; j < i + objres.cnt && j <= end; j++) {
                            typeAry[j] = wordType.NOP;
                        }
                    } else if (0 == objres.obj.objectType.compareTo("card"))
                        throw new xTalkException("Cards cannot be used as containers");
                    else if (0 == objres.obj.objectType.compareTo("background"))
                        throw new xTalkException("Backgrounds cannot be used as containers");
                    else if (0 == objres.obj.objectType.compareTo("stack"))
                        throw new xTalkException("Stacks cannot be used as containers");
                }
            }
        }

        // 2b. Negative sign (is within not here?)
        for (int i = start; i <= end; i++) {
            if (typeAry[i] == wordType.OPERATOR) {
                if (stringList.get(i).equals("-")) {
                    if (i == start || typeAry[i - 1] == wordType.OPERATOR || typeAry[i - 1] == wordType.OPERATOR_SUB) {
                        if (i > end - 1) throw new xTalkException("No value after -");
                        String str = Evalution(stringList, typeAry, i + 1, i + 1, memData, object, target);
                        try {
                            double value = -1.0 * (Double.parseDouble(str));
                            typeAry[i] = wordType.STRING;
                            stringList.set(i, numFormat.format(value));
                            typeAry[i + 1] = wordType.NOP;
                        } catch (Exception err) {
                            throw new xTalkException("Value after - is invalid. " + err);
                        }
                    }
                } else if (stringList.get(i).equals("not")) {
                    if (i == start || (typeAry[i - 1] != wordType.OPERATOR && typeAry[i - 1] != wordType.OPERATOR_SUB) ||
                            (0 == stringList.get(i - 1).compareToIgnoreCase("and") || 0 == stringList.get(i - 1).compareToIgnoreCase("or") || 0 == stringList.get(i - 1).compareToIgnoreCase("&"))) {
                        if (i > end - 1) {
                            throw new xTalkException("No value after not");
                        }
                        String value = Evalution(stringList, typeAry, i + 1, i + 1, memData, object, target);
                        if (value.compareToIgnoreCase("true") == 0) value = "false";
                        else if (value.compareToIgnoreCase("false") == 0) value = "true";
                        else throw new xTalkException("No boolean value (true/false) after not");
                        typeAry[i] = wordType.STRING;
                        stringList.set(i, value);
                        typeAry[i + 1] = wordType.NOP;
                    }
                }
//                else if (stringList.get(i) == "is" &&
//                        i + 1 <= end && typeAry[i + 1] == wordType.OPERATOR &&
//                        stringList.get(i + 1) == "within") {
//                    String str1 = "";
//                    int j = i - 1;
//                    for (; j >= start; j--) {
//                        if (typeAry[j] == wordType.STRING || typeAry[j] == wordType.X) {
//                            str1 = Evalution(stringList, typeAry, j, j, memData, object, target);
//                            break;
//                        }
//                    }
//                    Point p = null;
//                    try {
//                        String[] strAry = str1.split(",");
//                        p = new Point(Integer.valueOf(strAry[0]),
//                                Integer.valueOf(strAry[1]));
//                    } catch (Exception err) {
//                        throw new xTalkException("within operator point specification is invalid. " + err);
//                    }
//
//                    Rectangle r = null;
//                    try {
//                        String[] strAry = stringList.get(i + 2).split(",");
//                        r = new Rectangle(Integer.valueOf(strAry[0]),
//                                Integer.valueOf(strAry[1]),
//                                Integer.valueOf(strAry[2]),
//                                Integer.valueOf(strAry[3]));
//                    } catch (Exception err) {
//                        throw new xTalkException("within operator rectangle area specification is invalid. " + err);
//                    }
//
//                    typeAry[j] = wordType.STRING;
//                    if (r.contains(p)) {
//                        stringList.set(j, "true");
//                    } else {
//                        stringList.set(j, "false");
//                    }
//
//                    typeAry[i] = wordType.NOP;
//                    typeAry[i + 1] = wordType.NOP;
//                    typeAry[i + 2] = wordType.NOP;
//                } else if (stringList.get(i) == "is" &&
//                        i + 1 <= end && typeAry[i + 1] == wordType.OPERATOR_SUB &&
//                        0 == stringList.get(i + 1).compareToIgnoreCase("not") &&
//                        i + 2 <= end && typeAry[i + 2] == wordType.OPERATOR &&
//                        0 == stringList.get(i + 2).compareToIgnoreCase("within")) {
//                    String str1 = "";
//                    int j = i - 1;
//                    for (; j >= start; j--) {
//                        if (typeAry[j] == wordType.STRING || typeAry[j] == wordType.X) {
//                            str1 = Evalution(stringList, typeAry, j, j, memData, object, target);
//                            break;
//                        }
//                    }
//                    Point p = null;
//                    try {
//                        String[] strAry = str1.split(",");
//                        p = new Point(Integer.valueOf(strAry[0]),
//                                Integer.valueOf(strAry[1]));
//                    } catch (Exception err) {
//                        throw new xTalkException("within operator point specification is invalid. " + err);
//                    }
//
//                    Rectangle r = null;
//                    try {
//                        String[] strAry = stringList.get(i + 3).split(",");
//                        r = new Rectangle(Integer.valueOf(strAry[0]),
//                                Integer.valueOf(strAry[1]),
//                                Integer.valueOf(strAry[2]),
//                                Integer.valueOf(strAry[3]));
//                    } catch (Exception err) {
//                        throw new xTalkException("within operator rectangle area specification is invalid. " + err);
//                    }
//
//                    typeAry[j] = wordType.STRING;
//                    if (!r.contains(p)) {
//                        stringList.set(j, "true");
//                    } else {
//                        stringList.set(j, "false");
//                    }
//
//                    typeAry[i] = wordType.NOP;
//                    typeAry[i + 1] = wordType.NOP;
//                    typeAry[i + 2] = wordType.NOP;
//                    typeAry[i + 3] = wordType.NOP;
//                }
            }
        }

        // 3. Exponentiation
        for (int i = end; i >= start; i--) {
            if (typeAry[i] == wordType.OPERATOR) {
                if (stringList.get(i).equals("^")) {
                    if (i > end - 1) throw new xTalkException("^ operator is invalid");
                    try {
                        String str1 = "";
                        int j = i - 1;
                        for (; j >= start; j--) {
                            if (typeAry[j] == wordType.STRING || typeAry[j] == wordType.X) {
                                str1 = Evalution(stringList, typeAry, j, j, memData, object, target);
                                break;
                            }
                        }
                        String str2 = Evalution(stringList, typeAry, i + 1, i + 1, memData, object, target);
                        if (str1.isEmpty()) str1 = "0";
                        if (str2.isEmpty()) str2 = "0";
                        if (str2.equals("∞")) str1 = "Infinity";
                        if (str2.equals("∞")) str2 = "Infinity";
                        double value = Math.pow(Double.parseDouble(str1), Double.parseDouble(str2));
                        typeAry[j] = wordType.STRING;
                        stringList.set(j, numFormat.format(value));
                        typeAry[i] = wordType.NOP;
                        typeAry[i + 1] = wordType.NOP;
                    } catch (Exception err) {
                        throw new xTalkException("^ operator is invalid. " + err);
                    }
                }
            }
        }

        // 4. Number multiplication/division
        for (int i = start; i <= end; i++) {
            if (typeAry[i] == wordType.OPERATOR) {
                if (stringList.get(i).equals("*") ||
                        stringList.get(i).equals("/") ||
                        stringList.get(i).equals("div") ||
                        stringList.get(i).equals("mod")) {
                    if (i > end - 1) throw new xTalkException(stringList.get(i) + " operator is invalid");
                    String str1 = "";
                    String str2 = "";
                    try {
                        int j = i - 1;
                        for (; j >= start; j--) {
                            if (typeAry[j] == wordType.STRING || typeAry[j] == wordType.X) {
                                str1 = Evalution(stringList, typeAry, j, j, memData, object, target);
                                break;
                            }
                        }
                        str2 = Evalution(stringList, typeAry, i + 1, i + 1, memData, object, target);
                        double value = 0.0;
                        if (str1.isEmpty()) str1 = "0";
                        if (str2.isEmpty()) str2 = "0";
                        if (str1.equals("∞") || str2.equals("∞")) {
                            stringList.set(j, "Infinity");
                        } else if (str1.equals("♾") || str2.equals("♾")) {
                            stringList.set(j, "Infinity");
                        } else {
                            if (0 == stringList.get(i).compareTo("*"))
                                value = Double.parseDouble(str1) * Double.parseDouble(str2);
                            else if (0 == stringList.get(i).compareTo("/"))
                                value = Double.parseDouble(str1) / Double.parseDouble(str2);
                            else if (0 == stringList.get(i).compareToIgnoreCase("div"))
                                value = (int) (Double.parseDouble(str1) / Double.parseDouble(str2));
                            else if (0 == stringList.get(i).compareToIgnoreCase("mod"))
                                value = Double.parseDouble(str1) % Double.parseDouble(str2);
                            if (numFormat.format(value).equals("∞")) stringList.set(j, "Infinity");
                            if (numFormat.format(value).equals("-∞")) stringList.set(j, "-Infinity");
                            stringList.set(j, numFormat.format(value));
                        }
                        typeAry[j] = wordType.STRING;
                        typeAry[i] = wordType.NOP;
                        typeAry[i + 1] = wordType.NOP;
                    } catch (Exception err) {
                        throw new xTalkException(stringList.get(i) + " operator is invalid. " + err);
                    }
                }
            }
        }

        // 5. Number addition/subtraction
        for (int i = start; i <= end; i++) {
            if (typeAry[i] == wordType.OPERATOR) {
                if (stringList.get(i).equals("+") ||
                        stringList.get(i).equals("-")) {
                    if (i > end - 1) throw new xTalkException(stringList.get(i) + " operator is invalid");
                    try {
                        String str1 = "";
                        int j = i - 1;
                        for (; j >= start; j--) {
                            if (typeAry[j] == wordType.STRING || typeAry[j] == wordType.X) {
                                str1 = Evalution(stringList, typeAry, j, j, memData, object, target);
                                break;
                            }
                        }
                        String str2 = Evalution(stringList, typeAry, i + 1, i + 1, memData, object, target);
                        double value = 0.0;
                        if (str1.isEmpty()) str1 = "0";
                        if (str2.isEmpty()) str2 = "0";
                        if (str1.equals("∞") || str2.equals("∞")) {
                            stringList.set(j, "Infinity");
                        } else if (str1.equals("♾") || str2.equals("♾")) {
                            stringList.set(j, "Infinity");
                        } else {
                            if (0 == stringList.get(i).compareTo("+"))
                                value = Double.parseDouble(str1) + Double.parseDouble(str2);
                            else if (0 == stringList.get(i).compareTo("-"))
                                value = Double.parseDouble(str1) - Double.parseDouble(str2);
                            stringList.set(j, numFormat.format(value));
                        }
                        typeAry[j] = wordType.STRING;
                        typeAry[i] = wordType.NOP;
                        typeAry[i + 1] = wordType.NOP;
                    } catch (Exception err) {
                        throw new xTalkException(stringList.get(i) + " operator is invalid. " + err);
                    }
                }
            }
        }

        // 6. Text concatenation
        for (int i = start; i <= end; i++) {
            if (typeAry[i] == wordType.OPERATOR) {
                if (stringList.get(i).equals("&")) {
                    if (i > end - 1) {
                        throw new xTalkException(stringList.get(i) + " operator is invalid");
                    }
                    String str1 = "";
                    int j = i - 1;
                    for (; j >= start; j--) {
                        if (typeAry[j] == wordType.STRING || typeAry[j] == wordType.X) {
                            str1 = Evalution(stringList, typeAry, j, j, memData, object, target);
                            break;
                        }
                    }
                    String str2 = "";
                    if (stringList.get(i + 1).equals("&")) {
                        try {
                            str2 = Evalution(stringList, typeAry, i + 2, i + 2, memData, object, target);
                        } catch (Exception err) {
                            throw new xTalkException(stringList.get(i) + " operator is invalid. " + err);
                        }
                        typeAry[i + 2] = wordType.NOP;
                        typeAry[j] = wordType.STRING;
                        stringList.set(j, str1 + " " + str2);
                    } else {
                        try {
                            str2 = Evalution(stringList, typeAry, i + 1, i + 1, memData, object, target);
                        } catch (Exception err) {
                            throw new xTalkException(stringList.get(i) + " operator is invalid. " + err);
                        }
                        typeAry[j] = wordType.STRING;
                        stringList.set(j, str1 + str2);
                    }
                    typeAry[i] = wordType.NOP;
                    typeAry[i + 1] = wordType.NOP;
                }
            }
        }

        // within
        for (int i = start; i <= end; i++) {
            if (typeAry[i] == wordType.OPERATOR) {
                if (stringList.get(i).equals("is") &&
                        i + 1 <= end && typeAry[i + 1] == wordType.OPERATOR &&
                        stringList.get(i + 1).equals("within")) {
                    String str1 = "";
                    int j = i - 1;
                    for (; j >= start; j--) {
                        if (typeAry[j] == wordType.STRING || typeAry[j] == wordType.X) {
                            str1 = Evalution(stringList, typeAry, j, j, memData, object, target);
                            break;
                        }
                    }
                    Point p = null;
                    try {
                        String[] strAry = str1.split(",");
                        p = new Point(Integer.parseInt(strAry[0]),
                                Integer.parseInt(strAry[1]));
                    } catch (Exception err) {
                        throw new xTalkException("within operator point specification is invalid. " + err);
                    }

                    Rectangle r = null;
                    try {
                        String[] strAry = stringList.get(i + 2).split(",");
                        r = new Rectangle(Integer.parseInt(strAry[0]),
                                Integer.parseInt(strAry[1]),
                                Integer.parseInt(strAry[2]),
                                Integer.parseInt(strAry[3]));
                    } catch (Exception err) {
                        throw new xTalkException("within operator rectangle area specification is invalid. " + err);
                    }

                    typeAry[j] = wordType.STRING;
                    if (r.contains(p)) {
                        stringList.set(j, "true");
                    } else {
                        stringList.set(j, "false");
                    }

                    typeAry[i] = wordType.NOP;
                    typeAry[i + 1] = wordType.NOP;
                    typeAry[i + 2] = wordType.NOP;
                } else if (stringList.get(i).equals("is") &&
                        i + 1 <= end && typeAry[i + 1] == wordType.OPERATOR_SUB &&
                        0 == stringList.get(i + 1).compareToIgnoreCase("not") &&
                        i + 2 <= end && typeAry[i + 2] == wordType.OPERATOR &&
                        0 == stringList.get(i + 2).compareToIgnoreCase("within")) {
                    String str1 = "";
                    int j = i - 1;
                    for (; j >= start; j--) {
                        if (typeAry[j] == wordType.STRING || typeAry[j] == wordType.X) {
                            str1 = Evalution(stringList, typeAry, j, j, memData, object, target);
                            break;
                        }
                    }
                    Point p = null;
                    try {
                        String[] strAry = str1.split(",");
                        p = new Point(Integer.parseInt(strAry[0]),
                                Integer.parseInt(strAry[1]));
                    } catch (Exception err) {
                        throw new xTalkException("within operator point specification is invalid. " + err);
                    }

                    Rectangle r = null;
                    try {
                        String[] strAry = stringList.get(i + 3).split(",");
                        r = new Rectangle(Integer.parseInt(strAry[0]),
                                Integer.parseInt(strAry[1]),
                                Integer.parseInt(strAry[2]),
                                Integer.parseInt(strAry[3]));
                    } catch (Exception err) {
                        throw new xTalkException("within operator rectangle area specification is invalid. " + err);
                    }

                    typeAry[j] = wordType.STRING;
                    if (!r.contains(p)) {
                        stringList.set(j, "true");
                    } else {
                        stringList.set(j, "false");
                    }

                    typeAry[i] = wordType.NOP;
                    typeAry[i + 1] = wordType.NOP;
                    typeAry[i + 2] = wordType.NOP;
                    typeAry[i + 3] = wordType.NOP;
                }
            }
        }

        // 7. Number or text comparison
        for (int i = start; i <= end; i++) {
            if (typeAry[i] == wordType.OPERATOR) {
                String opr = stringList.get(i);
                switch (opr) {
                    case "<", ">", "≤", "≥" -> {
                        if (i > end - 1) {
                            throw new xTalkException("Comparison operator is invalid");
                        }
                        String str1 = "";
                        int j = i - 1;
                        for (; j >= start; j--) {
                            if (typeAry[j] == wordType.STRING || typeAry[j] == wordType.X) {
                                str1 = Evalution(stringList, typeAry, j, j, memData, object, target);
                                break;
                            }
                        }
                        if (typeAry[i + 1] == wordType.OPERATOR_SUB) {
                            if (i + 1 > end - 1) throw new xTalkException("Comparison operator is invalid");
                            if (opr.equals("<") && stringList.get(i + 1).equals(">")) continue;
                            opr = opr + stringList.get(i + 1);
                            typeAry[i] = wordType.NOP;
                            i++;
                        }
                        String str2 = Evalution(stringList, typeAry, i + 1, i + 1, memData, object, target);
                        try {
                            if (str1.isEmpty()) str1 = "0";
                            if (str2.isEmpty()) str2 = "0";
                            boolean value = false;
                            if (0 == opr.compareTo("<"))
                                value = Double.parseDouble(str1) < Double.parseDouble(str2);
                            else if (0 == opr.compareTo("<=") || 0 == opr.compareTo("≤"))
                                value = Double.parseDouble(str1) <= Double.parseDouble(str2);
                                // else if(0==opr.compareTo("<>") || 0==opr.compareTo("≠"))
                                //    value=Double.valueOf(str1) != Double.valueOf(str2);
                            else if (0 == opr.compareTo(">"))
                                value = Double.parseDouble(str1) > Double.parseDouble(str2);
                            else if (0 == opr.compareTo(">=") || 0 == opr.compareTo("≥"))
                                value = Double.parseDouble(str1) >= Double.parseDouble(str2);
                            typeAry[j] = wordType.STRING;
                            stringList.set(j, value ? "true" : "false");
                            typeAry[i] = wordType.NOP;
                            typeAry[i + 1] = wordType.NOP;
                        } catch (Exception err) {
                            boolean value = false;
                        /*if(0==opr.compareTo("<"))
                            value=str1.compareTo(str2)<0;
                        else if(0==opr.compareTo("<=") || 0==opr.compareTo("≤"))
                            value=str1.compareTo(str2)<=0;
                        else*/
                            if (0 == opr.compareTo("<>") || 0 == opr.compareTo("≠"))
                                value = str1.compareTo(str2) != 0;
                        /*else if(0==opr.compareTo(">"))
                            value=str1.compareTo(str2)>0;
                        else if(0==opr.compareTo(">=") || 0==opr.compareTo("≥"))
                            value=str1.compareTo(str2)>=0;*/
                            else throw new xTalkException("Cannot compare non-numbers: " + str1 + " and " + str2);
                            typeAry[j] = wordType.STRING;
                            stringList.set(j, value ? "true" : "false");
                            typeAry[i] = wordType.NOP;
                            typeAry[i + 1] = wordType.NOP;
                        }
                    }
                    case "is" -> {
                        if (i > end - 1) continue;
                        if (0 == stringList.get(i + 1).compareToIgnoreCase("in")) {
                            if (i + 1 > end - 1) throw new xTalkException("is in operator is invalid");
                            String str1 = "";
                            int j = i - 1;
                            for (; j >= start; j--) {
                                if (typeAry[j] == wordType.STRING || typeAry[j] == wordType.X) {
                                    str1 = Evalution(stringList, typeAry, j, j, memData, object, target);
                                    break;
                                }
                            }
                            String str2 = Evalution(stringList, typeAry, i + 2, i + 2, memData, object, target);
                            boolean value = false;
                            value = str2.toLowerCase().contains(str1.toLowerCase());
                            typeAry[j] = wordType.STRING;
                            stringList.set(j, value ? "true" : "false");
                            typeAry[i] = wordType.NOP;
                            typeAry[i + 1] = wordType.NOP;
                            typeAry[i + 2] = wordType.NOP;
                        } else if (typeAry[i + 1] != wordType.STRING &&
                                (0 == stringList.get(i + 1).compareToIgnoreCase("a") ||
                                        0 == stringList.get(i + 1).compareToIgnoreCase("an"))) {
                            // is a
                            if (i + 2 > end) throw new xTalkException("is a operator is invalid");
                            String str1 = "";
                            int j = i - 1;
                            for (; j >= start; j--) {
                                if (typeAry[j] == wordType.STRING || typeAry[j] == wordType.X) {
                                    str1 = Evalution(stringList, typeAry, j, j, memData, object, target);
                                    break;
                                }
                            }
                            boolean value = false;
                            if (stringList.get(i + 2).equalsIgnoreCase("number")) {
                                value = str1.matches("^[-]{0,1}[0-9]{1,99}[\\.]{0,1}[0-9]{0,99}$");
                            } else if (stringList.get(i + 2).equalsIgnoreCase("integer")) {
                                value = str1.matches("^[-]{0,1}[0-9]{1,99}$");
                            } else if (stringList.get(i + 2).equalsIgnoreCase("point")) {
                                value = str1.matches("^[-]{0,1}[0-9]{1,99},[-]{0,1}[0-9]{1,99}$");
                            } else if (stringList.get(i + 2).equalsIgnoreCase("rect") || stringList.get(i + 2).equalsIgnoreCase("rectangle")) {
                                value = str1.matches("^[-]{0,1}[0-9]{1,99},[-]{0,1}[0-9]{1,99},[-]{0,1}[0-9]{1,99},[-]{0,1}[0-9]{1,99}$");
                            } else if (stringList.get(i + 2).equalsIgnoreCase("date")) {
                                value = true;// **
                            } else if (stringList.get(i + 2).equalsIgnoreCase("logical")) {
                                value = (str1.equalsIgnoreCase("true") || str1.equalsIgnoreCase("false"));
                            } else throw new xTalkException(stringList.get(i + 2) + " cannot be used with is a operator");
                            typeAry[j] = wordType.STRING;
                            stringList.set(j, value ? "true" : "false");
                            typeAry[i] = wordType.NOP;
                            typeAry[i + 1] = wordType.NOP;
                            typeAry[i + 2] = wordType.NOP;
                        } else if (0 == stringList.get(i + 1).compareToIgnoreCase("not")) {
                            if (i > end - 2) continue;
                            if (0 == stringList.get(i + 2).compareToIgnoreCase("in")) {
                                if (i + 2 > end - 1) throw new xTalkException("is not in operator is invalid");
                                String str1 = "";
                                int j = i - 1;
                                for (; j >= start; j--) {
                                    if (typeAry[j] == wordType.STRING || typeAry[j] == wordType.X) {
                                        str1 = Evalution(stringList, typeAry, j, j, memData, object, target);
                                        break;
                                    }
                                }
                                String str2 = Evalution(stringList, typeAry, i + 3, i + 3, memData, object, target);
                                boolean value = false;
                                value = !str2.toLowerCase().contains(str1.toLowerCase());
                                typeAry[j] = wordType.STRING;
                                stringList.set(j, value ? "true" : "false");
                                typeAry[i] = wordType.NOP;
                                typeAry[i + 1] = wordType.NOP;
                                typeAry[i + 2] = wordType.NOP;
                                typeAry[i + 3] = wordType.NOP;
                            } else {
                                if (i > end - 2) continue;
                                if (0 == stringList.get(i + 2).compareToIgnoreCase("a") ||
                                        0 == stringList.get(i + 2).compareToIgnoreCase("an")) {
                                    // is not a/an
                                    if (i + 2 > end) throw new xTalkException("is not a operator is invalid");
                                    String str1 = "";
                                    int j = i - 1;
                                    for (; j >= start; j--) {
                                        if (typeAry[j] == wordType.STRING || typeAry[j] == wordType.X) {
                                            str1 = Evalution(stringList, typeAry, j, j, memData, object, target);
                                            break;
                                        }
                                    }
                                    boolean value = false;
                                    if (stringList.get(i + 3).equalsIgnoreCase("number")) {
                                        value = str1.matches("^[-]{0,1}[0-9]{1,99}[\\.]{0,1}[0-9]{0,99}$");
                                    } else if (stringList.get(i + 3).equalsIgnoreCase("integer")) {
                                        value = str1.matches("^[-]{0,1}[0-9]{1,99}$");
                                    } else if (stringList.get(i + 3).equalsIgnoreCase("point")) {
                                        value = str1.matches("^[-]{0,1}[0-9]{1,99},[-]{0,1}[0-9]{1,99}$");
                                    } else if (stringList.get(i + 3).equalsIgnoreCase("rect") || stringList.get(i + 3).equalsIgnoreCase("rectangle")) {
                                        value = str1.matches("^[-]{0,1}[0-9]{1,99},[-]{0,1}[0-9]{1,99},[-]{0,1}[0-9]{1,99},[-]{0,1}[0-9]{1,99}$");
                                    } else if (stringList.get(i + 3).equalsIgnoreCase("date")) {
                                        value = true;// **
                                    } else if (stringList.get(i + 3).equalsIgnoreCase("logical")) {
                                        value = (str1.equalsIgnoreCase("true") || str1.equalsIgnoreCase("false"));
                                    } else
                                        throw new xTalkException(stringList.get(i + 3) + " cannot be used with is not a operator");
                                    typeAry[j] = wordType.STRING;
                                    stringList.set(j, !value ? "true" : "false");
                                    typeAry[i] = wordType.NOP;
                                    typeAry[i + 1] = wordType.NOP;
                                    typeAry[i + 2] = wordType.NOP;
                                    typeAry[i + 3] = wordType.NOP;
                                }
                            }
                        }
                    }
                    case "contains" -> {
                        if (i + 1 > end) throw new xTalkException("contains operator is invalid");
                        String str1 = "";
                        int j = i - 1;
                        for (; j >= start; j--) {
                            if (typeAry[j] == wordType.STRING || typeAry[j] == wordType.X) {
                                str1 = Evalution(stringList, typeAry, j, j, memData, object, target);
                                break;
                            }
                        }
                        String str2 = Evalution(stringList, typeAry, i + 1, i + 1, memData, object, target);
                        boolean value = false;
                        value = str1.toLowerCase().contains(str2.toLowerCase());
                        typeAry[j] = wordType.STRING;
                        stringList.set(j, value ? "true" : "false");
                        typeAry[i] = wordType.NOP;
                        typeAry[i + 1] = wordType.NOP;
                    }
                }
            }
        }

        // 8. Number or text comparison
        for (int i = start; i <= end; i++) {
            if (typeAry[i] == wordType.OPERATOR) {
                String opr = stringList.get(i);
                if (opr.equals("≠")) {
                    if (i > end - 1) throw new xTalkException("Comparison operator is invalid");
                    String str1 = "";
                    int j = i - 1;
                    for (; j >= start; j--) {
                        if (typeAry[j] == wordType.STRING || typeAry[j] == wordType.X) {
                            str1 = Evalution(stringList, typeAry, j, j, memData, object, target);
                            break;
                        }
                    }
                    String str2 = Evalution(stringList, typeAry, i + 1, i + 1, memData, object, target);
                    boolean value = false;
                    try {
                        value = !Double.valueOf(str1).equals(Double.valueOf(str2));
                    } catch (Exception err) {
                        value = (0 != str1.compareToIgnoreCase(str2));
                    }
                    value = (0 != str1.compareToIgnoreCase(str2));
                    typeAry[j] = wordType.STRING;
                    stringList.set(j, value ? "true" : "false");
                    typeAry[i] = wordType.NOP;
                    typeAry[i + 1] = wordType.NOP;
                } else if (opr.equals("is") &&
                        stringList.get(i + 1).equals("not") ||
                        opr.equals("<") &&
                                stringList.get(i + 1).equals(">")) {
                    if (i + 1 > end - 1) throw new xTalkException("Comparison operator is invalid");
                    String str1 = "";
                    int j = i - 1;
                    for (; j >= start; j--) {
                        if (typeAry[j] == wordType.STRING || typeAry[j] == wordType.X) {
                            str1 = Evalution(stringList, typeAry, j, j, memData, object, target);
                            break;
                        }
                    }
                    String str2 = Evalution(stringList, typeAry, i + 2, i + 2, memData, object, target);
                    boolean value = false;
                    try {
                        value = (0 != Double.compare(Double.parseDouble(str1), Double.parseDouble(str2)));
                    } catch (Exception err) {
                        value = (0 != str1.compareToIgnoreCase(str2));
                    }
                    typeAry[j] = wordType.STRING;
                    stringList.set(j, value ? "true" : "false");
                    typeAry[i] = wordType.NOP;
                    typeAry[i + 1] = wordType.NOP;
                    typeAry[i + 2] = wordType.NOP;
                } else if (opr.equals("=") ||
                        opr.equals("is")) {
                    if (i > end - 1)
                        throw new xTalkException("Comparison operator is invalid");
                    String str1 = "";
                    int j = i - 1;
                    for (; j >= start; j--) {
                        if (typeAry[j] == wordType.STRING || typeAry[j] == wordType.X || typeAry[j] == wordType.VARIABLE) {
                            str1 = Evalution(stringList, typeAry, j, j, memData, object, target);
                            break;
                        }
                    }
                    String str2 = Evalution(stringList, typeAry, i + 1, i + 1, memData, object, target);
                    {
                        boolean value = false;
                        try {
                            value = (0 == Double.compare(Double.parseDouble(str1), Double.parseDouble(str2)));
                        } catch (Exception err) {
                            value = (0 == str1.compareToIgnoreCase(str2));
                        }
                        typeAry[j] = wordType.STRING;
                        stringList.set(j, value ? "true" : "false");
                        typeAry[i] = wordType.NOP;
                        typeAry[i + 1] = wordType.NOP;
                    }
                }
            }
        }

        // 9. and operator
        for (int i = start; i <= end; i++) {
            if (typeAry[i] == wordType.OPERATOR) {
                if (stringList.get(i).equals("and")) {
                    if (i > end - 1) throw new xTalkException("and operator is invalid");
                    String str1 = "";
                    int j = i - 1;
                    for (; j >= start; j--) {
                        if (typeAry[j] == wordType.STRING || typeAry[j] == wordType.X) {
                            str1 = Evalution(stringList, typeAry, j, j, memData, object, target);
                            break;
                        }
                    }
                    String str2 = Evalution(stringList, typeAry, i + 1, i + 1, memData, object, target);
                    {
                        boolean value1 = false;
                        boolean value2 = false;
                        if (0 == str1.compareToIgnoreCase("true")) value1 = true;
                        else if (0 == str1.compareToIgnoreCase("false")) value1 = false;
                        else throw new xTalkException("and operation requires boolean value (true/false)");
                        if (0 == str2.compareToIgnoreCase("true")) value2 = true;
                        else if (0 == str2.compareToIgnoreCase("false")) value2 = false;
                        else throw new xTalkException("and operation requires boolean value (true/false)");
                        typeAry[j] = wordType.STRING;
                        stringList.set(j, (value1 && value2) ? "true" : "false");
                        typeAry[i] = wordType.NOP;
                        typeAry[i + 1] = wordType.NOP;
                    }
                }
            }
        }

        // 10. or operator
        for (int i = start; i <= end; i++) {
            if (typeAry[i] == wordType.OPERATOR) {
                if (stringList.get(i).equals("or")) {
                    if (i > end - 1) throw new xTalkException("or operator is invalid");
                    String str1 = "";
                    int j = i - 1;
                    for (; j >= start; j--) {
                        if (typeAry[j] == wordType.STRING || typeAry[j] == wordType.X) {
                            str1 = Evalution(stringList, typeAry, j, j, memData, object, target);
                            break;
                        }
                    }
                    String str2 = Evalution(stringList, typeAry, i + 1, i + 1, memData, object, target);
                    {
                        boolean value1 = false;
                        boolean value2 = false;
                        if (0 == str1.compareToIgnoreCase("true")) value1 = true;
                        else if (0 == str1.compareToIgnoreCase("false")) value1 = false;
                        else throw new xTalkException("or operation requires boolean value (true/false)");
                        if (0 == str2.compareToIgnoreCase("true")) value2 = true;
                        else if (0 == str2.compareToIgnoreCase("false")) value2 = false;
                        else throw new xTalkException("or operation requires boolean value (true/false)");
                        typeAry[j] = wordType.STRING;
                        stringList.set(j, (value1 || value2) ? "true" : "false");
                        typeAry[i] = wordType.NOP;
                        typeAry[i + 1] = wordType.NOP;
                    }
                }
            }
        }

        // Are values being concatenated without an operator?
        for (int i = start + 1; i <= end; i++) {
            /*if(typeAry[i-1]==wordType.X && typeAry[i]==wordType.X) {
                throw new xTalkException(stringList.get(i-1)+" "+stringList.get(i)+" is unknown");
            }*/
            if (typeAry[i] != wordType.NOP) {
                throw new xTalkException(stringList.get(i) + " is unknown");
            }
        }

        // timeIsMoney("Evalution2:",timestart,22);

        if (start < typeAry.length && (typeAry[start] == wordType.STRING || typeAry[start] == wordType.X)) {
            return stringList.get(start);
        } else return "";
    }

    static void setVariable(
            MemoryData memData,
            String name, String value)
            throws xTalkException {
        // long timestart = System.currentTimeMillis();

        /*if(name.equalsIgnoreCase("TIMEX")){
            System.out.println("setVariable:"+name+"="+value);
        }*/
        // System.out.println("setVariable:"+name+"="+value);

        String str = name.toLowerCase();

        if (memData.treeset.contains(str)) {
            // Assign to local variable
            for (int i = 0; i < memData.nameList.size(); i++) {
                if (str.equals(memData.nameList.get(i))) {
                    memData.valueList.set(i, value);
                    // timeIsMoney("setVariable:",timestart,23);
                    return;
                }
            }
        }

        if (globalData.treeset.contains(str)) {
            // Assign to global variable
            for (int i = 0; i < globalData.nameList.size(); i++) {
                if (str.equals(globalData.nameList.get(i))) {
                    globalData.valueList.set(i, value);
                    // timeIsMoney("setVariable:",timestart,23);
                    return;
                }
            }
        }

        // Declare and assign local variable
        if (true == Character.isLetter(name.charAt(0))) {
            if (true == name.matches("^[A-Za-z0-9_]*$")) {
                memData.nameList.add(str);
                memData.valueList.add(value);
                memData.treeset.add(str);
            } else throw new xTalkException("Variable name is not alphanumeric: " + name);
        } else throw new xTalkException("Variable name does not start with alphabetic character: " + name);
        // timeIsMoney("setVariable:",timestart,23);

    }

    // Get variable value
    private static String getVariable(MemoryData memData, String str) {
        // long timestart = System.currentTimeMillis();
        if (memData == null) return null;
        // String str=name.toLowerCase();

        if (!memData.treeset.contains(str) && !globalData.treeset.contains(str)) {
            if (str.equals("it")) {
                return "";
            }
            return null;
        }

        for (int i = 0; i < memData.nameList.size(); i++) {
            if (str.equals(memData.nameList.get(i))) {
                // timeIsMoney("getVariable:",timestart,24);
                return memData.valueList.get(i);
            }
        }

        for (int i = 0; i < globalData.nameList.size(); i++) {
            if (str.equals(globalData.nameList.get(i))) {
                // timeIsMoney("getVariable:",timestart,24);
                return globalData.valueList.get(i);
            }
        }

        // timeIsMoney("getVariable:",timestart,24);
        return null;
    }

    // Global variable declaration, assignment
    private static void setGlobalVariable(
            String name, String value)
            throws xTalkException {
        String str = name.toLowerCase();

        // Assign to global variable
        if (globalData.treeset.contains(str)) {
            for (int i = 0; i < globalData.nameList.size(); i++) {
                if (str.equals(globalData.nameList.get(i))) {
                    if (value != null) globalData.valueList.set(i, value);
                    return;
                }
            }
        }

        // Declare and assign global variable
        if (true == Character.isLetter(name.charAt(0))) {
            if (true == name.matches("^[A-Za-z0-9_]*$")) {
                globalData.nameList.add(str);
                globalData.valueList.add(Objects.requireNonNullElse(value, ""));
                globalData.treeset.add(str);
            } else throw new xTalkException("Variable name is not alphanumeric: " + name);
        } else throw new xTalkException("Variable name does not start with alphabetic character: " + name);
    }

    private static ObjResult getObjectfromList(ArrayList<String> stringList, wordType[] typeAry, int start,
                                               OObject object, OObject target)
            throws xTalkException {
        // long timestart = System.currentTimeMillis();
        ObjResult objres = new ObjResult();
        objres.cnt = 0;
        objres.obj = null;

        int next = start;

        if (next >= stringList.size()) return null;// throw new xTalkException("Object is unknown");
        String str = stringList.get(next);
        if (0 == str.compareToIgnoreCase("HyperCard")) {
            objres.cnt = 1;
            objres.obj = OHyperCard.hc;
            return objres;
        }
        if (0 == str.compareToIgnoreCase("me")) {
            objres.cnt = 1;
            objres.obj = object;
            return objres;
        }
        if (0 == str.compareToIgnoreCase("target")) {
            objres.cnt = 1;
            objres.obj = target;
            return objres;
        }
        if (0 == str.compareToIgnoreCase("menubar")) {
            objres.cnt = 1;
            objres.obj = OMenubar.menubar;
            return objres;
        }
        if (0 == str.compareToIgnoreCase("msg")) {
            objres.cnt = 1;
            objres.obj = OMsg.msg;
            return objres;
        }
        next++;
        if (next >= stringList.size()) return null;// throw new xTalkException("Object is unknown");
        String str2 = stringList.get(next);
        if (0 == str.compareToIgnoreCase("the") && 0 == str2.compareToIgnoreCase("target")) {
            objres.cnt = 2;
            objres.obj = target;
            return objres;
        }
        if (0 == str.compareToIgnoreCase("this")) {
            if (0 == str2.compareToIgnoreCase("cd") || 0 == str2.compareToIgnoreCase("card")) {
                objres.cnt = 2;
                objres.obj = PCARD.pc.stack.curCard;
                return objres;
            }
            if (0 == str2.compareToIgnoreCase("bg") || 0 == str2.compareToIgnoreCase("bkgnd") || 0 == str2.compareToIgnoreCase("background")) {
                objres.cnt = 2;
                objres.obj = PCARD.pc.stack.curCard.bg;
                return objres;
            }
            if (0 == str2.compareToIgnoreCase("stack")) {
                objres.cnt = 2;
                objres.obj = PCARD.pc.stack;
                return objres;
            }
        }
        if (0 == str.compareToIgnoreCase("next")) {
            if (0 == str2.compareToIgnoreCase("cd") || 0 == str2.compareToIgnoreCase("card")) {
                objres.cnt = 2;
                int id;
                int i;
                for (i = 0; i < PCARD.pc.stack.cardIdList.size(); i++) {
                    if (PCARD.pc.stack.curCard != null && PCARD.pc.stack.curCard.id == PCARD.pc.stack.cardIdList.get(i))
                        break;
                }
                if (i == PCARD.pc.stack.cardIdList.size()) i = 0;
                else {
                    i++;
                    if (i >= PCARD.pc.stack.cardIdList.size()) i = 0;
                }
                id = PCARD.pc.stack.cardIdList.get(i);
                objres.obj = PCARD.pc.stack.GetCardbyId(id);
                return objres;
            }
        }
        if (0 == str.compareToIgnoreCase("prev") || 0 == str.compareToIgnoreCase("previous")) {
            if (0 == str2.compareToIgnoreCase("cd") || 0 == str2.compareToIgnoreCase("card")) {
                objres.cnt = 2;
                int id;
                int i;
                for (i = 0; i < PCARD.pc.stack.cardIdList.size(); i++) {
                    if (PCARD.pc.stack.curCard != null && PCARD.pc.stack.curCard.id == PCARD.pc.stack.cardIdList.get(i))
                        break;
                }
                if (i == 0) i = PCARD.pc.stack.cardIdList.size() - 1;
                else {
                    i--;
                }
                id = PCARD.pc.stack.cardIdList.get(i);
                objres.obj = PCARD.pc.stack.GetCardbyId(id);
                return objres;
            }
        }
        if (0 == str.compareToIgnoreCase("window")) {
            for (int i = 0; i < OWindow.list.size(); i++) {
                if (!str2.isEmpty() &&
                        OWindow.list.get(i).name != null && OWindow.list.get(i).name.equalsIgnoreCase(str2)) {
                    objres.cnt = 2;
                    objres.obj = OWindow.list.get(i);
                    break;
                }
            }
            if (str2.equalsIgnoreCase("message") || str2.equalsIgnoreCase("msg")) {
                objres.cnt = 2;
                objres.obj = OMsg.msg;
            }
            return objres;
        } else if (0 == str2.compareToIgnoreCase("window")) {
            if (0 == str.compareToIgnoreCase("cd") || 0 == str.compareToIgnoreCase("card")) {
                objres.cnt = 2;
                objres.obj = OWindow.cdwindow;
            } else if (0 == str.compareToIgnoreCase("msg") || 0 == str.compareToIgnoreCase("message")) {
                objres.cnt = 2;
                objres.obj = OWindow.msgwindow;
            }
            return objres;
        } else if (0 == str.compareToIgnoreCase("menu")) {
            if (str2.matches("[0-9]*")) {
                int i = Integer.parseInt(str2);
                if (i <= PCARD.pc.getJMenuBar().getComponentCount()) {
                    objres.cnt = 2;
                    objres.obj = new OMenu((JMenu) PCARD.pc.getJMenuBar().getComponent(i));
                }
                return objres;
            } else {
                for (int i = 0; i < PCARD.pc.getJMenuBar().getComponentCount(); i++) {
                    JMenu menu = (JMenu) PCARD.pc.getJMenuBar().getComponent(i);
                    if (menu.getText().equalsIgnoreCase(str2)) {
                        objres.cnt = 2;
                        objres.obj = new OMenu(menu);
                    }
                }
                return objres;
            }
        } else if (0 == str.compareToIgnoreCase("folder")) {
            String path = "";
            path = str2.replace(':', File.separatorChar);
            objres.cnt = 2;
            if (new File(path).isDirectory()) {
                objres.obj = new OObject();
            }
            return objres;
        }

        for (int i = start; i < stringList.size() - 1; i++) {
            if (stringList.get(i).equalsIgnoreCase("first")) {
                if (stringList.get(i + 1).equalsIgnoreCase("card") ||
                        stringList.get(i + 1).equalsIgnoreCase("background") ||
                        stringList.get(i + 1).equalsIgnoreCase("button") ||
                        stringList.get(i + 1).equalsIgnoreCase("field")) {
                    String s = "";
                    if (stringList.get(i).equalsIgnoreCase("first")) s = "1";
                    stringList.set(i, stringList.get(i + 1));
                    stringList.set(i + 1, s);
                }
            }
        }

        // button
        if (0 == str.compareToIgnoreCase("btn") || 0 == str.compareToIgnoreCase("button") ||
                0 == str2.compareToIgnoreCase("btn") || 0 == str2.compareToIgnoreCase("button")) {
            boolean bg = false;
            // Sometimes need the card where the object exists rather than the current card, but no standard is known part1
            OCardBase cdbase;
            /*if(object.objectType.equals("card")) cdbase=(OCardBase)object;
            else if(object.objectType.equals("background")) cdbase=((OBackground)object).viewCard;
            else if(object.objectType.equals("button")) cdbase=((OButton)object).card;
            else if(object.objectType.equals("field")) cdbase=((OField)object).card;
            else*/
            cdbase = PCARD.pc.stack.curCard;
            if (0 == str.compareToIgnoreCase("cd") || 0 == str.compareToIgnoreCase("card")) {
                // cdbase=PCARD.pc.stack.curCard;
                next++;
            } else if (0 == str.compareToIgnoreCase("bg") || 0 == str.compareToIgnoreCase("bkgnd") || 0 == str.compareToIgnoreCase("background")) {
                bg = true;
                cdbase = PCARD.pc.stack.curCard.bg;
                // cdbase=PCARD.pc.stack.curCard;
                next++;
            }
            if (next >= stringList.size()) throw new xTalkException("button is unknown");
            String str3 = stringList.get(next);
            int id = 0;
            int number = 0;
            String name = "";
            if (0 == str3.compareToIgnoreCase("id")) {
                next++;
                String str4 = stringList.get(next);
                id = Integer.parseInt(str4);
            } else {
                try {
                    number = Integer.parseInt(str3);
                } catch (Exception e) {
                    name = str3;
                }
            }
            // Object specification on other card
            next++;
            while (next < stringList.size() && typeAry[next] == wordType.NOP) {
                next++;
            }
            if (next < stringList.size()) {
                String str5 = stringList.get(next);
                if (0 == str5.compareToIgnoreCase("of")) {
                    next++;
                    if (next >= stringList.size()) throw new xTalkException("object is unknown");
                    ObjResult objres2;
                    objres2 = getObjectfromList(stringList, typeAry, next, object, target);
                    next += objres2.cnt/*-1*/;
                    if (objres2.obj == null) throw new xTalkException("object is unknown");
                    if (0 != objres2.obj.objectType.compareTo("card") && 0 != objres2.obj.objectType.compareTo("background"))
                        throw new xTalkException("Please specify a card or background here");
                    cdbase = (OCardBase) objres2.obj;
                    if (bg == true && 0 == cdbase.objectType.compareTo("card")) {
                        if (((OCard) cdbase).bg != null) {
                            cdbase = ((OCard) cdbase).bg;
                        } else {
                            cdbase = new OBackground(cdbase.stack, (OCard) cdbase, ((OCard) cdbase).bgid, true);
                        }
                        if (cdbase == null) throw new xTalkException("No background");
                    }
                }
            }
            // Search by id/number/name from cdbase.btnList
            if (id != 0) {
                for (int i = 0; i < cdbase.btnList.size(); i++) {
                    if (id == cdbase.btnList.get(i).id) {
                        objres.cnt = next - start/*+1*/;
                        objres.obj = cdbase.btnList.get(i);
                        // System.out.println("getObjectFromList:"+(System.currentTimeMillis()-timestart));
                        return objres;
                    }
                }
                throw new xTalkException("No button with id " + id);
            } else if (number != 0) {
                if (number < 0) throw new xTalkException("No button at number " + number);
                if (number > cdbase.btnList.size()) throw new xTalkException("No button at number " + number);
                objres.cnt = next - start/*+1*/;
                objres.obj = cdbase.btnList.get(number - 1);
                // /System.out.println("getObjectFromList:"+(System.currentTimeMillis()-timestart));
                return objres;
            } else {
                for (int i = 0; i < cdbase.btnList.size(); i++) {
                    if (id == cdbase.btnList.get(i).name.compareToIgnoreCase(name)) {
                        objres.cnt = next - start/*+1*/;
                        objres.obj = cdbase.btnList.get(i);
                        // System.out.println("getObjectFromList:"+(System.currentTimeMillis()-timestart));
                        return objres;
                    }
                }
                // As an exception, the me object can be referenced by name even after card movement
                if (object != null && object.objectType.equals("button")) {
                    if (bg && object.parent.objectType.equals("background") ||
                            !bg && object.parent.objectType.equals("card")) {
                        if (object.name.equalsIgnoreCase(name)) {
                            objres.cnt = next - start/*+1*/;
                            objres.obj = object;
                            // System.out.println("getObjectFromList:"+(System.currentTimeMillis()-timestart));
                            return objres;
                        }
                    }
                }
                if (target != null && target.objectType.equals("button")) {
                    if (bg && target.parent.objectType.equals("background") ||
                            !bg && target.parent.objectType.equals("card")) {
                        if (target.name.equalsIgnoreCase(name)) {
                            objres.cnt = next - start/*+1*/;
                            objres.obj = target;
                            // System.out.println("getObjectFromList:"+(System.currentTimeMillis()-timestart));
                            return objres;
                        }
                    }
                }
                throw new xTalkException("No button with name \"" + name + "\"");
            }
        }
        // field
        if (0 == str.compareToIgnoreCase("fld") || 0 == str.compareToIgnoreCase("field") ||
                0 == str2.compareToIgnoreCase("fld") || 0 == str2.compareToIgnoreCase("field")) {
            boolean bg = false;
            OCardBase cdbase = PCARD.pc.stack.curCard.bg;
            if (0 == str.compareToIgnoreCase("cd") || 0 == str.compareToIgnoreCase("card")) {
                cdbase = PCARD.pc.stack.curCard;
                next++;
            } else if (0 == str.compareToIgnoreCase("bg") || 0 == str.compareToIgnoreCase("bkgnd") || 0 == str.compareToIgnoreCase("background")) {
                bg = true;
                // cdbase=PCARD.pc.stack.curCard.bg;
                cdbase = PCARD.pc.stack.curCard;
                next++;
            }
            if (next >= stringList.size()) throw new xTalkException("field is unknown");
            String str3 = stringList.get(next);
            int id = 0;
            int number = 0;
            String name = "";
            if (0 == str3.compareToIgnoreCase("id")) {
                next++;
                String str4 = stringList.get(next);
                id = Integer.parseInt(str4);
            } else {
                try {
                    number = Integer.parseInt(str3);
                } catch (Exception e) {
                    name = str3;
                }
            }
            // Object specification on other card
            next++;
            while (next < stringList.size() && typeAry[next] == wordType.NOP) {
                next++;
            }
            if (next < stringList.size()) {
                String str5 = stringList.get(next);
                if (0 == str5.compareToIgnoreCase("of")) {
                    next++;
                    if (next >= stringList.size()) throw new xTalkException("object is unknown");
                    ObjResult objres2;
                    objres2 = getObjectfromList(stringList, typeAry, next, object, target);
                    next += objres2.cnt/*-1*/;
                    if (objres2.obj == null) throw new xTalkException("object is unknown");
                    if (0 != objres2.obj.objectType.compareTo("card") && 0 != objres2.obj.objectType.compareTo("background"))
                        throw new xTalkException("Please specify a card or background here");
                    cdbase = (OCardBase) objres2.obj;
                }
            }

            // Get bg from cd (otherwise cannot get field content that differs per card)
            // Method from the era when bg was not expanded in memory
            /*if(bg == true && 0==cdbase.objectType.compareTo("card")) {
                if(((OCard)cdbase).bg != null) {
                    cdbase = (OCardBase) ((OCard)cdbase).bg;
                } else {
                    cdbase = (OCardBase) new OBackground(cdbase.stack, (OCard)cdbase, ((OCard)cdbase).bgid, true);
                }
                if(cdbase == null) throw new xTalkException("No background");
            }*/

            // Get bg from cd (otherwise cannot get field content that differs per card)
            if (bg == true && 0 == cdbase.objectType.compareTo("card")) {
                OCard ocd = (OCard) cdbase;
                cdbase = ocd.getBg();
            }

            // Search by id/number/name from cdbase.fldList
            if (id != 0) {
                for (int i = 0; i < cdbase.fldList.size(); i++) {
                    if (id == cdbase.fldList.get(i).id) {
                        objres.cnt = next - start/*+1*/;
                        objres.obj = cdbase.fldList.get(i);
                        // System.out.println("getObjectFromList:"+(System.currentTimeMillis()-timestart));
                        return objres;
                    }
                }
                throw new xTalkException("No field with id " + id);
            } else if (number != 0) {
                if (number < 0) throw new xTalkException("No field at number " + number);
                if (number > cdbase.fldList.size()) throw new xTalkException("No field at number " + number);
                objres.cnt = next - start/*+1*/;
                objres.obj = cdbase.fldList.get(number - 1);
                // System.out.println("getObjectFromList:"+(System.currentTimeMillis()-timestart));
                return objres;
            } else {
                for (int i = 0; i < cdbase.fldList.size(); i++) {
                    if (0 == cdbase.fldList.get(i).name.compareToIgnoreCase(name)) {
                        objres.cnt = next - start/*+1*/;
                        objres.obj = cdbase.fldList.get(i);
                        // System.out.println("getObjectFromList:"+(System.currentTimeMillis()-timestart));
                        return objres;
                    }
                }
                // As an exception, the me object can be referenced by name even after card movement
                if (object != null && object.objectType.equals("field")) {
                    if (bg && object.parent.objectType.equals("background") ||
                            !bg && object.parent.objectType.equals("card")) {
                        if (object.name.equalsIgnoreCase(name)) {
                            objres.cnt = next - start/*+1*/;
                            objres.obj = object;
                            // System.out.println("getObjectFromList:"+(System.currentTimeMillis()-timestart));
                            return objres;
                        }
                    }
                }
                throw new xTalkException("No field with name \"" + name + "\"");
            }
        }
        // cd
        if (0 == str.compareToIgnoreCase("cd") || 0 == str.compareToIgnoreCase("card")) {
            String str3 = stringList.get(next);
            int id = 0;
            int number = 0;
            String name = "";
            if (0 == str3.compareToIgnoreCase("id")) {
                next++;
                String str4 = stringList.get(next);
                id = Integer.parseInt(str4);
            } else {
                try {
                    number = Integer.parseInt(str3);
                } catch (Exception e) {
                    name = str3;
                }
            }
            // Search by id/number/name
            if (id != 0) {
                objres.cnt = next - start + 1;
                objres.obj = PCARD.pc.stack.GetCardbyId(id);
                if (objres.obj == null) throw new xTalkException("No card with id " + id);
                // System.out.println("getObjectFromList:"+(System.currentTimeMillis()-timestart));
                return objres;
            } else if (number != 0) {
                objres.cnt = next - start + 1;
                objres.obj = PCARD.pc.stack.GetCardbyNum(number);
                if (objres.obj == null) throw new xTalkException("No card at number " + number);
                // System.out.println("getObjectFromList:"+(System.currentTimeMillis()-timestart));
                return objres;
            } else {
                objres.cnt = next - start + 1;
                objres.obj = PCARD.pc.stack.GetCard(name);
                if (objres.obj == null) throw new xTalkException("No card with name " + name);
                // System.out.println("getObjectFromList:"+(System.currentTimeMillis()-timestart));
                return objres;
            }
        }

        // String errstr="";
        // for(int i=start; i<start+objres.cnt; i++){
        //    errstr+=stringList.get(i);
        // }
        // timeIsMoney("getObjectFromList:",timestart,25);
        // System.out.println("objstr;"+errstr+" cnt="+objres.cnt);
        return objres;
    }

    private static int getNumberOfChunk(String mode, String source) {
        if (0 == mode.compareTo("chars") || 0 == mode.compareTo("characters")) {
            return source.length();
        }
        if (0 == mode.compareTo("items")) {
            String[] strAry = source.split(itemDelimiter);
            return strAry.length;
        }
        if (0 == mode.compareTo("words")) { // Need to handle newlines and "" grouping
            String[] strAry = source.split(" ");
            return strAry.length;
        }
        if (0 == mode.compareTo("lines")) {
            while (source.contains("\r")) {
                int offset = 0;
                if (source.length() > source.indexOf("\r") + 1 && source.charAt(source.indexOf("\r") + 1) == '\n')
                    offset = 1;
                source = source.substring(0, source.indexOf("\r")) + "\n" + source.substring(source.indexOf("\r") + 1 + offset);
            }
            String[] strAry = source.split("\n");
            return strAry.length;
        }
        return 0;
    }

    private static String getChunk(String mode, String chunkStart, String chunkEnd, String source) {
        // long timestart = System.currentTimeMillis();
        int start = 0;
        try {
            start = Integer.parseInt(chunkStart);
        } catch (Exception e) {
        }
        int end = start;
        if (chunkEnd != null) try {
            end = Integer.parseInt(chunkEnd);
        } catch (Exception e) {
        }
        if (end < start) return "";

        if (0 == mode.compareTo("char") || 0 == mode.compareTo("character")) {
            StringBuilder value = new StringBuilder();
            for (int i = start - 1; i <= end - 1; i++) {
                if (i >= source.length()) break;
                if (i < 0) continue;
                value.append(source.charAt(i));
            }
            // System.out.println("getChunk:"+(System.currentTimeMillis()-timestart));
            return value.toString();
        }
        if (0 == mode.compareTo("item")) {
            StringBuilder value = new StringBuilder();
            String[] strAry = source.split(itemDelimiter);
            if (start - 1 < strAry.length && start >= 1) value = new StringBuilder(strAry[start - 1]);
            else return "";
            for (int i = start; i <= end - 1; i++) {
                if (i >= strAry.length) break;
                value.append(itemDelimiter).append(strAry[i]);
            }
            // System.out.println("getChunk:"+(System.currentTimeMillis()-timestart));
            return value.toString();
        }
        if (0 == mode.compareTo("word")) { // Need to handle newlines and "" grouping
            StringBuilder value = new StringBuilder();
            String[] strAry = source.split(" ");
            if (start - 1 < strAry.length && start >= 1) value = new StringBuilder(strAry[start - 1]);
            else return "";
            for (int i = start; i <= end - 1; i++) {
                if (i >= strAry.length) break;
                value.append(" ").append(strAry[i]);
            }
            // System.out.println("getChunk:"+(System.currentTimeMillis()-timestart));
            return value.toString();
        }
        if (0 == mode.compareTo("line")) {
            StringBuilder value = new StringBuilder();
            while (source.contains("\r")) {
                int offset = 0;
                if (source.length() > source.indexOf("\r") + 1 && source.charAt(source.indexOf("\r") + 1) == '\n')
                    offset = 1;
                source = source.substring(0, source.indexOf("\r")) + "\n" + source.substring(source.indexOf("\r") + 1 + offset);
            }
            String[] strAry = source.split("\n");
            if (start - 1 < strAry.length && start >= 1) value = new StringBuilder(strAry[start - 1]);
            else return "";
            for (int i = start; i <= end - 1; i++) {
                if (i >= strAry.length) break;
                value.append("\n").append(strAry[i]);
            }
            int indexof = value.indexOf("\r");
            if (indexof >= 0) {
                String value2 = value.substring(0, indexof);
                if (value.length() > indexof) value2 += value.substring(indexof + 1, value.length());
                // System.out.println("getChunk:"+(System.currentTimeMillis()-timestart));
                return value2;
            } else {
                // System.out.println("getChunk:"+(System.currentTimeMillis()-timestart));
                return value.toString();
            }
        }
        // timeIsMoney("getChunk:",timestart,26);
        return "";
    }

    private static void setChunkVariable(String mode, String chunkStart, String chunkEnd, String setstr, String name,
                                         MemoryData memData)
            throws xTalkException {
        // long timestart = System.currentTimeMillis();
        int start = 0;
        try {
            start = Integer.parseInt(chunkStart);
        } catch (Exception e) {
        }
        int end = start;
        if (chunkEnd != null) try {
            end = Integer.parseInt(chunkEnd);
        } catch (Exception e) {
        }

        String oldvalue = getVariable(memData, name);
        if (oldvalue == null) oldvalue = "";

        if (end <= 0) {
            // System.out.println("!");
            end = 1;
        }

        StringBuilder value = new StringBuilder();
        if (0 == mode.compareTo("char") || 0 == mode.compareTo("character")) {
            if (!oldvalue.isEmpty())
                value = new StringBuilder(oldvalue.substring(0, start - 1));
            value.append(setstr);
            if (oldvalue.length() > end)
                value.append(oldvalue.substring(end));
        } else {
            String delimiter = " ";
            if (0 == mode.compareTo("item")) {
                delimiter = itemDelimiter;
            }
            if (0 == mode.compareTo("word")) { // Need to handle newlines and "" grouping
                delimiter = " ";
            }
            if (0 == mode.compareTo("line")) {
                while (oldvalue.contains("\r")) {
                    int offset = 0;
                    if (oldvalue.length() > oldvalue.indexOf("\r") + 1 && oldvalue.charAt(oldvalue.indexOf("\r") + 1) == '\n')
                        offset = 1;
                    oldvalue = oldvalue.substring(0, oldvalue.indexOf("\r")) + "\n" + oldvalue.substring(oldvalue.indexOf("\r") + 1 + offset);
                }
                delimiter = "\n";
            }
            String[] strAry = oldvalue.split(delimiter);

            for (int i = 0; i < start - 1; i++) {
                if (i < strAry.length) value.append(strAry[i]).append(delimiter);
                else value.append(delimiter);
            }
            value.append(setstr);
            for (int i = end; i < strAry.length; i++) {
                value.append(delimiter).append(strAry[i]);
            }
        }

        setVariable(memData, name, value.toString());
        // timeIsMoney("setChunkVariable:",timestart,27);
    }

    private static void setChunkVariable2(String mode, String chunkStart, String chunkEnd, String mode2, String chunkStart2, String chunkEnd2,
                                          String setstr, String name, MemoryData memData)
            throws xTalkException {
        // long timestart = System.currentTimeMillis();
        int start = 0;
        try {
            start = Integer.parseInt(chunkStart);
        } catch (Exception e) {
        }
        int end = start;
        if (chunkEnd != null) try {
            end = Integer.parseInt(chunkEnd);
        } catch (Exception e) {
        }
        int start2 = 0;
        try {
            start2 = Integer.parseInt(chunkStart2);
        } catch (Exception e) {
        }
        int end2 = start2;
        if (chunkEnd != null) try {
            end2 = Integer.parseInt(chunkEnd);
        } catch (Exception e) {
        }

        String oldvalue = getVariable(memData, name);
        if (oldvalue == null) oldvalue = "";

        String delimiter2 = " ";
        if (0 == mode2.compareTo("item")) {
            delimiter2 = itemDelimiter;
        }
        if (0 == mode2.compareTo("word")) { // Need to handle newlines and "" grouping
            delimiter2 = " ";
        }
        if (0 == mode2.compareTo("line")) {
            while (oldvalue.contains("\r")) {
                int offset = 0;
                if (oldvalue.length() > oldvalue.indexOf("\r") + 1 && oldvalue.charAt(oldvalue.indexOf("\r") + 1) == '\n')
                    offset = 1;
                oldvalue = oldvalue.substring(0, oldvalue.indexOf("\r")) + "\n" + oldvalue.substring(oldvalue.indexOf("\r") + 1 + offset);
            }
            delimiter2 = "\n";
        }
        String[] strAry2 = oldvalue.split(delimiter2);

        StringBuilder value = new StringBuilder();
        for (int i = 0; i < start2 - 1; i++) {
            if (i < strAry2.length) value.append(strAry2[i]).append(delimiter2);
            else value.append(delimiter2);
        }

        String text2 = "";
        if (start2 - 1 >= 0 && start2 - 1 < strAry2.length) text2 = strAry2[start2 - 1];
        if (0 == mode.compareTo("char") || 0 == mode.compareTo("character")) {
            if (start - 1 > 0 && end >= 0)
                value.append(text2, 0, start - 1).append(setstr).append(text2.substring(end));
            else value.append(text2, 0, 0).append(setstr).append(text2);
        } else {
            String delimiter = " ";
            if (0 == mode.compareTo("item")) {
                delimiter = itemDelimiter;
            }
            if (0 == mode.compareTo("word")) { // Need to handle newlines and "" grouping
                delimiter = " ";
            }
            if (0 == mode.compareTo("line")) {
                delimiter = "\n";
            }
            String[] strAry = text2.split(delimiter);

            for (int i = 0; i < start - 1; i++) {
                if (i < strAry.length) value.append(strAry[i]).append(delimiter);
                else value.append(delimiter);
            }
            value.append(setstr);
            for (int i = end; i < strAry.length; i++) {
                value.append(delimiter).append(strAry[i]);
            }
        }

        for (int i = end2; i < strAry2.length; i++) {
            value.append(delimiter2).append(strAry2[i]);
        }

        setVariable(memData, name, value.toString());
        // timeIsMoney("setChunkVariable2:",timestart,28);
    }

    private static void setChunkObject(String mode, String chunkStart, String chunkEnd, String setstr, OObject obj) {
        int start = 0;
        // long timestart = System.currentTimeMillis();
        try {
            start = Integer.parseInt(chunkStart);
        } catch (Exception e) {
        }
        int end = start;
        if (chunkEnd != null) try {
            end = Integer.parseInt(chunkEnd);
        } catch (Exception e) {
        }

        OButton btn = null;
        OField fld = null;
        String text = "";
        if (0 == obj.objectType.compareTo("button")) {
            btn = (OButton) obj;
            text = btn.getText();
        } else if (0 == obj.objectType.compareTo("field")) {
            fld = (OField) obj;
            text = fld.getText();
        } else return;

        StringBuilder value = new StringBuilder();
        if (0 == mode.compareTo("char") || 0 == mode.compareTo("character")) {
            value = new StringBuilder(text.substring(0, start - 1) + setstr + text.substring(end));
        } else {
            String delimiter = " ";
            if (0 == mode.compareTo("item")) {
                delimiter = itemDelimiter;
            }
            if (0 == mode.compareTo("word")) { // Need to handle newlines and "" grouping
                delimiter = " ";
            }
            if (0 == mode.compareTo("line")) {
                while (text.contains("\r")) {
                    int offset = 0;
                    if (text.length() > text.indexOf("\r") + 1 && text.charAt(text.indexOf("\r") + 1) == '\n')
                        offset = 1;
                    text = text.substring(0, text.indexOf("\r")) + "\n" + text.substring(text.indexOf("\r") + 1 + offset);
                }
                delimiter = "\n";
            }
            String[] strAry = text.split(delimiter);

            for (int i = 0; i < start - 1; i++) {
                if (i < strAry.length) value.append(strAry[i]).append(delimiter);
                else value.append(delimiter);
            }
            value.append(setstr);
            for (int i = end; i < strAry.length; i++) {
                value.append(delimiter).append(strAry[i]);
            }
        }

        if (btn != null) {
            btn.setText(value.toString());
        } else if (fld != null) {
            fld.setText(value.toString());
        }
        // timeIsMoney("setChunkVariable2:",timestart,29);
    }

    private static void setChunkObject2(String mode, String chunkStart, String chunkEnd, String mode2, String chunkStart2, String chunkEnd2, String setstr, OObject obj) {
        int start = 0;
        // long timestart = System.currentTimeMillis();
        try {
            start = Integer.parseInt(chunkStart);
        } catch (Exception e) {
        }
        int end = start;
        if (chunkEnd != null) try {
            end = Integer.parseInt(chunkEnd);
        } catch (Exception e) {
        }
        int start2 = 0;
        try {
            start2 = Integer.parseInt(chunkStart2);
        } catch (Exception e) {
        }
        int end2 = start2;
        if (chunkEnd != null) try {
            end2 = Integer.parseInt(chunkEnd);
        } catch (Exception e) {
        }

        OButton btn = null;
        OField fld = null;
        String text = "";
        if (0 == obj.objectType.compareTo("button")) {
            btn = (OButton) obj;
            text = btn.getText();
        } else if (0 == obj.objectType.compareTo("field")) {
            fld = (OField) obj;
            text = fld.getText();
        } else return;

        String delimiter2 = " ";
        if (0 == mode2.compareTo("item")) {
            delimiter2 = itemDelimiter;
        }
        if (0 == mode2.compareTo("word")) { // Need to handle newlines and "" grouping
            delimiter2 = " ";
        }
        if (0 == mode2.compareTo("line")) {
            while (text.contains("\r")) {
                int offset = 0;
                if (text.length() > text.indexOf("\r") + 1 && text.charAt(text.indexOf("\r") + 1) == '\n') offset = 1;
                text = text.substring(0, text.indexOf("\r")) + "\n" + text.substring(text.indexOf("\r") + 1 + offset);
            }
            delimiter2 = "\n";
        }
        String[] strAry2 = text.split(delimiter2);

        StringBuilder value = new StringBuilder();
        for (int i = 0; i < start2 - 1; i++) {
            if (i < strAry2.length) value.append(strAry2[i]).append(delimiter2);
            else value.append(delimiter2);
        }

        String text2 = "";
        if (start2 - 1 >= 0 && start2 - 1 < strAry2.length) text2 = strAry2[start2 - 1];
        if (0 == mode.compareTo("char") || 0 == mode.compareTo("character")) {
            value.append(text2, 0, start - 1).append(setstr).append(text2.substring(end));
        } else {
            String delimiter = " ";
            if (0 == mode.compareTo("item")) {
                delimiter = itemDelimiter;
            }
            if (0 == mode.compareTo("word")) { // Need to handle newlines and "" grouping
                delimiter = " ";
            }
            if (0 == mode.compareTo("line")) {
                delimiter = "\n";
            }
            String[] strAry = text2.split(delimiter);

            for (int i = 0; i < start - 1; i++) {
                if (i < strAry.length) value.append(strAry[i]).append(delimiter);
                else value.append(delimiter);
            }
            value.append(setstr);
            for (int i = end; i < strAry.length; i++) {
                value.append(delimiter).append(strAry[i]);
            }
        }

        for (int i = end2; i < strAry2.length; i++) {
            value.append(delimiter2).append(strAry2[i]);
        }

        if (btn != null) {
            btn.setText(value.toString());
        } else if (fld != null) {
            fld.setText(value.toString());
        }
        // timeIsMoney("setChunkObject2:",timestart,30);
    }

    private static OObject getObject(ArrayList<String> stringList, wordType[] typeAry, int start, int end, MemoryData memData, OObject object, OObject target)
            throws xTalkException {
        // long timestart = System.currentTimeMillis();
        OObject obj = null;
        StringBuilder errStr = new StringBuilder(48);
        for (int i = start; i <= end; i++) {
            if (typeAry[i] == wordType.STRING) errStr.append("\"");
            errStr.append(stringList.get(i));
            if (typeAry[i] == wordType.STRING) errStr.append("\"");
            errStr.append(" ");
        }
        // System.out.println("getObject"+errStr);
        int next = start;

        if (next >= stringList.size()) return null;

        if (0 == stringList.get(next).compareToIgnoreCase("me")) {
            obj = object;
            // next++;
        } else if (0 == stringList.get(next).compareToIgnoreCase("target")) {
            obj = target;
            // next++;
        } else if (0 == stringList.get(next).compareToIgnoreCase("titlebar")) {
            obj = OTitlebar.titlebar;
            // next++;
        } else if (0 == stringList.get(next).compareToIgnoreCase("menubar")) {
            obj = OMenubar.menubar;
            // next++;
        } else if (0 == stringList.get(next).compareToIgnoreCase("tool") &&
                next + 1 <= end && 0 == stringList.get(next + 1).compareToIgnoreCase("window")) {
            obj = OToolWindow.toolwindow;
            // next++;
        } else if ((0 == stringList.get(next).compareToIgnoreCase("cd") || 0 == stringList.get(next).compareToIgnoreCase("card")) &&
                next + 1 <= end && 0 == stringList.get(next + 1).compareToIgnoreCase("window")) {
            obj = OWindow.cdwindow;
            // next++;
        } else if (0 == stringList.get(next).compareToIgnoreCase("msg") || 0 == stringList.get(next).compareToIgnoreCase("message")) {
            obj = OMsg.msg;
            // next++;
        } else if (0 == stringList.get(next).compareToIgnoreCase("the") &&
                next + 1 <= end && 0 == stringList.get(next + 1).compareToIgnoreCase("target")) {
            obj = target;
            next++;// next+=2;
        } else if (0 == stringList.get(next).compareToIgnoreCase("window")) {
            String str = Evalution(stringList, typeAry, next + 1, end, memData, object, target);
            for (int i = 0; i < OWindow.list.size(); i++) {
                if (!str.isEmpty() && str.equalsIgnoreCase(OWindow.list.get(i).name)) {
                    obj = OWindow.list.get(i);
                    next++;
                }
            }
            if (obj == null && str.equalsIgnoreCase("message")) {
                obj = OMsg.msg;
            }
        } else if (0 == stringList.get(next).compareToIgnoreCase("menu")) {
            int j;
            for (j = next + 1; j <= end; j++) {
                if (stringList.get(j).equalsIgnoreCase("with")) {
                    break;
                }
            }
            j--;
            String str = Evalution(stringList, typeAry, next + 1, j, memData, object, target);

            for (int i = 0; i < PCARD.pc.menu.mb.getComponentCount(); i++) {
                Component c = PCARD.pc.menu.mb.getComponent(i);
                if (c.getClass() == JMenu.class) {
                    JMenu menu = (JMenu) c;
                    if (str.equalsIgnoreCase(menu.getText()) ||
                            str.equalsIgnoreCase(PCARD.pc.intl.getEngText(menu.getText()))) {
                        obj = new OMenu(menu);
                        break;
                    }
                }
            }
        } else if ((0 == stringList.get(next).compareToIgnoreCase("cd") || 0 == stringList.get(next).compareToIgnoreCase("card")) &&
                next + 1 <= end && 0 == stringList.get(next + 1).compareToIgnoreCase("picture")) {
            obj = PCARD.pc.stack.curCard.picture;
            next++;// next+=2;
        } else if ((0 == stringList.get(next).compareToIgnoreCase("bg") || 0 == stringList.get(next).compareToIgnoreCase("bkgnd") || 0 == stringList.get(next).compareToIgnoreCase("background")) &&
                next + 1 <= end && 0 == stringList.get(next + 1).compareToIgnoreCase("picture")) {
            obj = PCARD.pc.stack.curCard.bg.picture;
            next++;// next+=2;
        } else if (0 == stringList.get(next).compareToIgnoreCase("picture") &&
                next + 1 <= end && 0 == stringList.get(next + 1).compareToIgnoreCase("of") &&
                next + 2 <= end) {
            ObjResult result = getObjectfromList(stringList, typeAry, next + 2, object, target);
            if (result != null) {
                if (result.obj.objectType.equals("card") || result.obj.objectType.equals("background")) {
                    obj = ((OCardBase) result.obj).picture;
                    next++;// next+=2;
                    next += result.cnt;
                }
            }
        } else if (0 == stringList.get(next).compareToIgnoreCase("this")) {
            next++;
            if (next > end) throw new xTalkException("Object " + errStr + " is unknown");
            if (0 == stringList.get(next).compareToIgnoreCase("card") || 0 == stringList.get(next).compareToIgnoreCase("cd")) {
                obj = PCARD.pc.stack.curCard;
                next++;
            } else if (0 == stringList.get(next).compareToIgnoreCase("background") || 0 == stringList.get(next).compareToIgnoreCase("bkgnd") || 0 == stringList.get(next).compareToIgnoreCase("bg")) {
                obj = PCARD.pc.stack.curCard.bg;
                next++;
            } else if (0 == stringList.get(next).compareToIgnoreCase("stack")) {
                obj = PCARD.pc.stack;
                next++;
            }
        } else if (0 == stringList.get(next).compareToIgnoreCase("next")) {
            next++;
            if (next > end) throw new xTalkException("Object " + errStr + " is unknown");
            if (0 == stringList.get(next).compareToIgnoreCase("card") || 0 == stringList.get(next).compareToIgnoreCase("cd")) {
                int id;
                int i;
                for (i = 0; i < PCARD.pc.stack.cardIdList.size(); i++) {
                    if (PCARD.pc.stack.curCard != null && PCARD.pc.stack.curCard.id == PCARD.pc.stack.cardIdList.get(i))
                        break;
                }
                if (i == PCARD.pc.stack.cardIdList.size()) i = 0;
                else {
                    i++;
                    if (i >= PCARD.pc.stack.cardIdList.size()) i = 0;
                }
                id = PCARD.pc.stack.cardIdList.get(i);
                obj = PCARD.pc.stack.GetCardbyId(id);
                next++;
            }
        } else if (0 == stringList.get(next).compareToIgnoreCase("last")) {
            next++;
            if (next > end) throw new xTalkException("Object " + errStr + " is unknown");
            if (0 == stringList.get(next).compareToIgnoreCase("card") || 0 == stringList.get(next).compareToIgnoreCase("cd")) {
                obj = PCARD.pc.stack.GetCardbyNum(PCARD.pc.stack.cardIdList.size());
                next++;
            }
        } else if (stringList.size() - start >= 2) {
            // Sometimes need the card where the object exists rather than the current card, but what is the standard? When not found?
            OCardBase cdbase;
            /*if(object.objectType.equals("card")) cdbase=(OCardBase)object;
            else if(object.objectType.equals("background")) cdbase=((OBackground)object).viewCard;
            else if(object.objectType.equals("button")) cdbase=((OButton)object).card;
            else if(object.objectType.equals("field")) cdbase=((OField)object).card;
            else*/
            cdbase = PCARD.pc.stack.curCard;

            boolean bg_flag = false;
            // boolean cdbg_flag = false;// Search both card and BG
            int flag = 0; // 1 is button, 2 is field
            for (int i = next; i <= end; i++) {
                // Parentheses
                if (typeAry[i] == wordType.LBRACKET) {
                    int nest = 0;
                    for (int j = i + 1; j <= end; j++) {
                        if (typeAry[j] == wordType.RBRACKET || typeAry[j] == wordType.RFUNC) {
                            if (nest == 0) {
                                Evalution(stringList, typeAry, i, j, memData, object, target);
                                for (int k = i; k <= j; k++) {
                                    if (typeAry[k] == wordType.NOP) stringList.set(k, "");
                                }
                                break;
                            }
                            nest--;
                        }
                        if (typeAry[j] == wordType.LBRACKET || typeAry[j] == wordType.LFUNC) {
                            nest++;
                        }
                    }
                }
            }
            for (int i = next; i <= end; i++) {
                // For other cards
                if (0 == stringList.get(i).compareToIgnoreCase("of") &&
                        (!stringList.get(i - 1).matches("[0-9]*") ||
                                i - 2 >= start &&
                                        !stringList.get(i - 2).equalsIgnoreCase("char") && !stringList.get(i - 2).equalsIgnoreCase("item") &&
                                        !stringList.get(i - 2).equalsIgnoreCase("line") && !stringList.get(i - 2).equalsIgnoreCase("word") &&
                                        !stringList.get(i - 2).equalsIgnoreCase("character"))) {
                    if (i + 1 > end) {
                        throw new xTalkException("Object" + errStr + " is unknown");
                    }
                    // if(0==stringList.get(i+1).compareToIgnoreCase("this")){
                    // if(i+2>end) throw new xTalkException("Object"+errStr+" is unknown");
                    if (0 == stringList.get(i + 1).compareToIgnoreCase("card") || 0 == stringList.get(i + 1).compareToIgnoreCase("cd")) {
                        if (i + 2 <= end && 0 == stringList.get(i + 2).compareToIgnoreCase("id")) {
                            if (i + 3 > end) throw new xTalkException("Object" + errStr + " is unknown");
                            int j;
                            for (j = i + 3; j < end; j++) {
                                if (0 == stringList.get(j).compareToIgnoreCase("of")) {
                                    j--;
                                    break;
                                }
                            }
                            String str = Evalution(stringList, typeAry, i + 3, j, memData, object, target);
                            try {
                                cdbase = PCARD.pc.stack.GetCardbyId(Integer.parseInt(str));
                            } catch (Exception err) {
                                throw new xTalkException("Object" + errStr + " is unknown");
                            }
                        } else {
                            int j;
                            for (j = i + 2; j < end; j++) {
                                if (0 == stringList.get(j).compareToIgnoreCase("of")) {
                                    j--;
                                    break;
                                }
                            }
                            String str = Evalution(stringList, typeAry, i + 2, j, memData, object, target);
                            try {
                                cdbase = PCARD.pc.stack.GetCardbyNum(Integer.parseInt(str));
                            } catch (Exception err) {
                                cdbase = PCARD.pc.stack.GetCard(str);
                            }
                        }
                        if (cdbase != null) {
                            end = i - 1;
                            break;
                        } else throw new xTalkException("Object" + errStr + " is unknown");
                    } else if (0 == stringList.get(i + 1).compareToIgnoreCase("background") || 0 == stringList.get(i + 1).compareToIgnoreCase("bkgnd") || 0 == stringList.get(i + 1).compareToIgnoreCase("bg")) {
                        if (i + 2 <= end && 0 == stringList.get(i + 2).compareToIgnoreCase("id")) {
                            if (i + 3 > end) throw new xTalkException("Object" + errStr + " is unknown");
                            int j;
                            for (j = i + 3; j < end; j++) {
                                if (0 == stringList.get(j).compareToIgnoreCase("of")) {
                                    j--;
                                    break;
                                }
                            }
                            String str = Evalution(stringList, typeAry, i + 3, j, memData, object, target);
                            try {
                                cdbase = PCARD.pc.stack.GetBackgroundbyId(Integer.parseInt(str));
                            } catch (Exception err) {
                                throw new xTalkException("Object" + errStr + " is unknown");
                            }
                        } else {
                            int j;
                            for (j = i + 2; j < end; j++) {
                                if (0 == stringList.get(j).compareToIgnoreCase("of")) {
                                    j--;
                                    break;
                                }
                            }
                            String str = Evalution(stringList, typeAry, i + 2, j, memData, object, target);
                            try {
                                cdbase = PCARD.pc.stack.GetBackgroundbyNum(Integer.parseInt(str));
                            } catch (Exception err) {
                                cdbase = PCARD.pc.stack.GetBackground(str);
                            }
                        }
                        if (cdbase != null) {
                            end = i - 1;
                            break;
                        } else throw new xTalkException("Object" + errStr + " is unknown");
                    } else throw new xTalkException("Object" + errStr + " is unknown");
                    // }
                }
            }

            // Objects on Card
            if (0 == stringList.get(next).compareToIgnoreCase("cd") || 0 == stringList.get(next).compareToIgnoreCase("card") ||
                    (0 != stringList.get(next).compareToIgnoreCase("bg") && 0 != stringList.get(next).compareToIgnoreCase("bkgnd") && 0 != stringList.get(next).compareToIgnoreCase("background"))) {
                if (0 == stringList.get(next).compareToIgnoreCase("cd") || 0 == stringList.get(next).compareToIgnoreCase("card")) {
                    next++;
                } else if (0 == stringList.get(next).compareToIgnoreCase("btn") || 0 == stringList.get(next).compareToIgnoreCase("button")) {
                    // cdbg_flag = true;
                } else if (0 == stringList.get(next).compareToIgnoreCase("fld") || 0 == stringList.get(next).compareToIgnoreCase("field")) {
                    // cdbg_flag = true;
                    bg_flag = true;
                } else {
                    return null;// Not an object
                }
                if (next > end) throw new xTalkException("Object " + errStr + " is unknown");
                if (typeAry[next] != wordType.STRING && (0 == stringList.get(next).compareToIgnoreCase("button") || 0 == stringList.get(next).compareToIgnoreCase("btn"))) {
                    flag = 1;
                    next++;
                } else if (typeAry[next] != wordType.STRING && (0 == stringList.get(next).compareToIgnoreCase("field") || 0 == stringList.get(next).compareToIgnoreCase("fld"))) {
                    flag = 2;
                    next++;
                } else {
                    // The Card itself
                    if (0 == stringList.get(next).compareToIgnoreCase("id")) {
                        next++;
                        if (next > end) throw new xTalkException("Object " + errStr + " is unknown");
                        int j;
                        for (j = next; j < end; j++) {
                            if (0 == stringList.get(j).compareToIgnoreCase("of") &&
                                    (!stringList.get(j - 1).matches("[0-9]*"))) break;
                        }
                        String str = Evalution(stringList, typeAry, next, j, memData, object, target);
                        try {
                            obj = PCARD.pc.stack.GetCardbyId(Integer.parseInt(str));
                        } catch (Exception err) {
                            throw new xTalkException("Object" + errStr + " is unknown");
                        }
                    } else {
                        int j;
                        for (j = next; j < end; j++) {
                            if (0 == stringList.get(j).compareToIgnoreCase("of") &&
                                    (!stringList.get(j - 1).matches("[0-9]{1,99}"))) {
                                j--;
                                break;
                            }
                        }
                        String str = Evalution(stringList, typeAry, next, j, memData, object, target);

                        if (cdbase != null && cdbase.objectType.equals("background"))
                            obj = PCARD.pc.stack.GetCardofBg(cdbase, str);
                        else obj = PCARD.pc.stack.GetCard(str);
                        if (obj == null) {
                            try {
                                if (cdbase != null && cdbase.objectType.equals("background"))
                                    obj = PCARD.pc.stack.GetCardofBgbyNum(cdbase, Integer.parseInt(str));
                                else obj = PCARD.pc.stack.GetCardbyNum(Integer.parseInt(str));
                            } catch (Exception err) {
                            }
                        }
                    }
                }
            }
            // Objects on Bg
            if (next <= end && (0 == stringList.get(next).compareToIgnoreCase("bg") || 0 == stringList.get(next).compareToIgnoreCase("bkgnd") || 0 == stringList.get(next).compareToIgnoreCase("background"))) {
                bg_flag = true;
                next++;
                if (next > end) throw new xTalkException("Object " + errStr + " is unknown");
                if (0 == stringList.get(next).compareToIgnoreCase("button") || 0 == stringList.get(next).compareToIgnoreCase("btn")) {
                    flag = 1;
                    // cdbase = PCARD.pc.stack.curCard.bg;
                    next++;
                } else if (0 == stringList.get(next).compareToIgnoreCase("field") || 0 == stringList.get(next).compareToIgnoreCase("fld")) {
                    flag = 2;
                    // cdbase = PCARD.pc.stack.curCard.bg;
                    next++;
                } else {
                    // The Bg itself
                    if (0 == stringList.get(next).compareToIgnoreCase("id")) {
                        next++;
                        if (next > end) throw new xTalkException("Object " + errStr + " is unknown");
                        int j;
                        for (j = next; j < end; j++) {
                            if (0 == stringList.get(j).compareToIgnoreCase("of") &&
                                    (!stringList.get(j - 1).matches("[0-9]*"))) break;
                        }
                        String str = Evalution(stringList, typeAry, next, j, memData, object, target);
                        try {
                            obj = PCARD.pc.stack.GetBackgroundbyId(Integer.parseInt(str));
                        } catch (Exception err) {
                            throw new xTalkException("Object" + errStr + " is unknown");
                        }
                    } else {
                        int j;
                        for (j = next; j < end; j++) {
                            if (0 == stringList.get(j).compareToIgnoreCase("of") &&
                                    (!stringList.get(j - 1).matches("[0-9]*"))) break;
                        }
                        String str = Evalution(stringList, typeAry, next, j, memData, object, target);
                        try {
                            obj = PCARD.pc.stack.GetBackgroundbyNum(Integer.parseInt(str));
                        } catch (Exception err) {
                            obj = PCARD.pc.stack.GetBackground(str);
                        }
                    }
                }
            }
            if (obj == null && flag == 1) {
                // btn
                if (next > end) throw new xTalkException("Object " + errStr + " is unknown");
                if (0 == stringList.get(next).compareToIgnoreCase("id")) {
                    next++;
                    if (next > end) throw new xTalkException("Object " + errStr + " is unknown");
                    int j;
                    for (j = next; j < end; j++) {
                        if (0 == stringList.get(j).compareToIgnoreCase("of") &&
                                (!stringList.get(j - 1).matches("[0-9]*"))) {
                            j--;
                            break;
                        }
                    }
                    String str = Evalution(stringList, typeAry, next, j, memData, object, target);
                    // System.out.println("* "+str);
                    try {
                        if (!bg_flag) obj = cdbase.GetBtnbyId(Integer.parseInt(str));
                        else obj = cdbase.GetBgBtnbyId(Integer.parseInt(str));
                    } catch (Exception err) {
                        throw new xTalkException("Object" + errStr + " is unknown");
                    }
                } else {
                    int j;
                    for (j = next; j < end; j++) {
                        if (0 == stringList.get(j).compareToIgnoreCase("of") &&
                                (!stringList.get(j - 1).matches("[0-9]*"))) {
                            j--;
                            break;
                        }
                    }
                    String str = Evalution(stringList, typeAry, next, j, memData, object, target);
                    try {
                        if (!bg_flag) obj = cdbase.GetBtnbyNum(Integer.parseInt(str));
                        else obj = cdbase.GetBgBtnbyNum(Integer.parseInt(str));
                    } catch (Exception err) {
                        if (!bg_flag) {
                            obj = cdbase.GetBtn(str);
                            /*if(obj==null && cdbg_flag){
                                cdbase=PCARD.pc.stack.GetBackgroundbyId(((OCard)cdbase).bgid);
                                obj=cdbase.GetBtn(str);
                            }*/
                        } else obj = cdbase.GetBgBtn(str);
                    }
                    if (obj == null) {
                        if (object.objectType.equals("button") &&
                                (bg_flag && object.parent.objectType.equals("background")) ||
                                (!bg_flag && object.parent.objectType.equals("card"))) {
                            if (object.name.equalsIgnoreCase(str)) {
                                obj = object;
                            }
                        }
                    }
                    if (obj == null && target != null) {
                        if (target.objectType.equals("button") &&
                                (bg_flag && target.parent.objectType.equals("background")) ||
                                (!bg_flag && target.parent.objectType.equals("card"))) {
                            if (target.name.equalsIgnoreCase(str)) {
                                obj = target;
                            }
                        }
                    }
                }
            }
            if (obj == null && flag == 2) {
                // fld
                if (next > end) throw new xTalkException("Object " + errStr + " is unknown");
                if (0 == stringList.get(next).compareToIgnoreCase("id")) {
                    next++;
                    if (next > end) throw new xTalkException("Object " + errStr + " is unknown");
                    int j;
                    for (j = next; j < end; j++) {
                        if (0 == stringList.get(j).compareToIgnoreCase("of") &&
                                (!stringList.get(j - 1).matches("[0-9]*"))) {
                            j--;
                            break;
                        }
                    }
                    String str = Evalution(stringList, typeAry, next, j, memData, object, target);
                    try {
                        if (!bg_flag) obj = cdbase.GetFldbyId(Integer.parseInt(str));
                        else obj = cdbase.GetBgFldbyId(Integer.parseInt(str));
                    } catch (Exception err) {
                        throw new xTalkException("Object" + errStr + " is unknown");
                    }
                } else {
                    int j;
                    for (j = next; j < end; j++) {
                        if (0 == stringList.get(j).compareToIgnoreCase("of") &&
                                ((!stringList.get(j - 1).matches("[0-9]*")) ||
                                        j - 1 == next)) {
                            j--;
                            break;
                        }
                    }
                    String str = Evalution(stringList, typeAry, next, j, memData, object, target);
                    try {
                        if (!bg_flag) obj = cdbase.GetFldbyNum(Integer.parseInt(str));
                        else obj = cdbase.GetBgFldbyNum(Integer.parseInt(str));
                    } catch (Exception err) {
                        if (!bg_flag) obj = cdbase.GetFld(str);
                        else obj = cdbase.GetBgFld(str);
                    }
                }
            }
        }

        // timeIsMoney("getObject:",timestart,31);
        if (next != end && obj == null) {
            throw new xTalkException("Object" + errStr + " is unknown");
        }
        return obj;
    }

    static timeClass[] timeList = new timeClass[32];

    static void timeIsMoney(String name, long start, int index) {
        long time = System.currentTimeMillis() - start;
        if (timeList[index] == null) {
            timeList[index] = new timeClass();
            timeList[index].name = "";
            timeList[index].time = 0;
        }
        if (timeList[index].name.equals(name) || timeList[index].name.isEmpty()) {
            timeList[index].time += time;
        }
    }
}

class timeClass {

    long time;
    String name;
}

class xTalkException extends Exception {

    @Serial
    private static final long serialVersionUID = 1L;

    public xTalkException(String msg) {
        super(msg);
    }

    public xTalkException(String msg, int line) {
        super(msg + "(line " + line + ")");
    }
}