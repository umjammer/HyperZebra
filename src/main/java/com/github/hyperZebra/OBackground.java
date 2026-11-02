package com.github.hyperZebra;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import javax.imageio.ImageIO;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;


public class OBackground extends OCardBase {

    private static final Logger logger = System.getLogger(OBackground.class.getName());

    // Properties
    Boolean cantDelete = false;
    Boolean cantModify = false;
    Boolean dontSearch = false;
    // number (calculated from stack information)

    // Additional properties
    OCard viewCard;

    // toc.xml
    @SuppressWarnings("unused")
    private String filler1;

    @Override
    public void clean() {
        super.clean();
        viewCard = null;
    }

    // get
    public String getName() {
        return name;
    }

    public ArrayList<String> getScript() {
        return scriptList;
    }

    // set
    public void setName(String in) {
        name = in;
    }

    // Main
    public static OBackground getOBackground(OStack st, OCard cd, int bgId, boolean dataonly) throws XTalkException {
        //logger.log(Level.DEBUG, "bgid="+bgId);

        for (int i = 0; i < st.bgCacheList.size(); i++) {
            OBackground bg = st.bgCacheList.get(i);
            //logger.log(Level.DEBUG, "bg="+bg.id);
            if (st == cd.stack && bgId == bg.id) {
                if (dataonly) return bg;
                else {
                    // Unshared text
                    for (int j = 0; j < bg.fldList.size(); j++) {
                        OField fld = bg.fldList.get(j);
                        if (fld != null && fld.sharedText == false) {
                            fld.setTextInternal("");
                        }
                    }
                    for (int j = 0; j < cd.bgfldList.size(); j++) {
                        OField fld = bg.GetBgFldbyId(cd.bgfldList.get(j).id);
                        if (fld != null && fld.sharedText == false) {
                            fld.setTextInternal(cd.bgfldList.get(j).text);
                        }
                    }

                    buidParts(bg);
                    return buildOBackground(bg);
                }
            }
        }

        if (st.firstBg > 0 && bgId != st.firstBg) {
            // When converting HC stacks, bg may not exist, so use the first bg
            return getOBackground(st, cd, st.firstBg, dataonly);
        }

        return new OBackground(st, cd, bgId, dataonly);
    }

    OBackground(OStack st, OCard cd, int bgId, boolean dataonly) throws XTalkException {
        super(st);
        objectType = "background";
        parent = st;
        viewCard = cd;
        this.btnList = new ArrayList<>();
        this.fldList = new ArrayList<>();
        this.partsList = new ArrayList<>();
        this.scriptList = new ArrayList<>();

        id = bgId;
        {
            throw new XTalkException("No background data file with that id exists");
        }
    }


    public OBackground(OStack st) {
        super(st);

        objectType = "background";
        parent = st;
        this.btnList = new ArrayList<>();
        this.fldList = new ArrayList<>();
        this.partsList = new ArrayList<>();
        this.scriptList = new ArrayList<>();
    }


    @SuppressWarnings("unchecked")
    public XMLStreamReader readXML(XMLStreamReader reader) throws Exception {
        while (reader.hasNext()) {
            try {
                int eventType = reader.next();
                if (eventType == XMLStreamReader.START_ELEMENT) {
                    String elm = reader.getLocalName().intern();
                    switch (elm) {
                        case "id" -> id = Integer.parseInt(reader.getElementText());
                        case "filler1" -> filler1 = reader.getElementText();
                        case "bitmap" -> bitmapName = reader.getElementText();
                        case "cantDelete" -> {
                            cantDelete = XMLRead.bool(reader);
                            reader.next();
                        }
                        case "cantModify" -> {
                            cantModify = XMLRead.bool(reader);
                            reader.next();
                        }
                        case "showPict" -> {
                            showPict = XMLRead.bool(reader);
                            reader.next();
                        }
                        case "dontSearch" -> {
                            dontSearch = XMLRead.bool(reader);
                            reader.next();
                        }
                        // else if(elm.equals("owner")){ bgid = Integer.valueOf(reader.getElementText()); }
                        case "name" -> name = reader.getElementText();
                        case "script" -> {
                            String scrStr = reader.getElementText();
                            String[] scriptAry = scrStr.split("\n");
                            scriptList.addAll(Arrays.asList(scriptAry));
                            stringList = new ArrayList[scriptList.size()];
                            typeList = new ArrayList[scriptList.size()];
                        }
                        case "part" -> {
                            int partid = 0;
                            while (reader.hasNext()) {
                                int eventType2 = reader.next();
                                if (eventType2 == XMLStreamReader.START_ELEMENT) {
                                    String elm2 = reader.getLocalName();
                                    if (elm2.equals("id")) {
                                        partid = Integer.parseInt(reader.getElementText());
                                    } else if (elm2.equals("type")) {
                                        String type = reader.getElementText();
                                        if (type.equals("button")) {
                                            OButton btn = new OButton(this, partid);
                                            reader = btn.readXML(reader);
                                            btnList.add(btn);
                                            partsList.add(btn);
                                        } else if (type.equals("field")) {
                                            OField fld = new OField(this, partid);
                                            reader = fld.readXML(reader);
                                            fldList.add(fld);
                                            partsList.add(fld);
                                        } else logger.log(Level.DEBUG, "Part_Type: " + type);
                                        break;
                                    }
                                } else if (eventType2 == XMLStreamReader.END_ELEMENT) {
                                    String elm2 = reader.getLocalName();
                                    if (elm2.equals("part")) {
                                        break;
                                    }
                                }
                            }
                        }
                        case "content" -> {
                            int partid = 0;
                            String layer = "";
                            while (reader.hasNext()) {
                                int eventType2 = reader.next();
                                if (eventType2 == XMLStreamReader.START_ELEMENT) {
                                    String elm2 = reader.getLocalName();
                                    switch (elm2) {
                                        case "layer" -> layer = reader.getElementText();
                                        case "id" -> partid = Integer.parseInt(reader.getElementText());
                                        case "text" -> {
                                            if (layer.equals("background")) {
                                                OObject part = this.GetPartbyId(partid);
                                                if (part != null) {
                                                    part.setTextInternal(reader.getElementText());
                                                } else {
                                                    logger.log(Level.DEBUG, "No Part: " + layer + " part id " + partid);
                                                }
                                            } else {
                                                //
                                                logger.log(Level.DEBUG, "Local Name: " + reader.getLocalName());
                                            }
                                        }
                                        case "stylerun" -> {
                                            int styleid = 0;
                                            // int offset = 0;
                                            while (reader.hasNext()) {
                                                int eventType3 = reader.next();
                                                if (eventType3 == XMLStreamReader.START_ELEMENT) {
                                                    String elm3 = reader.getLocalName();
                                                    if (elm3.equals("offset")) { /*offset = Integer.valueOf(reader.getElementText());*/ } else if (elm3.equals("id")) {
                                                        styleid = Integer.parseInt(reader.getElementText());
                                                        // textStyle
                                                        if (layer.equals("card")) {
                                                            int style = 0;
                                                            for (int i = 0; i < this.stack.styleList.size(); i++) {
                                                                if (this.stack.styleList.get(i).id == styleid) {
                                                                    style = this.stack.styleList.get(i).style;
                                                                    break;
                                                                }
                                                            }
                                                            if (style != -1) {
                                                                OObject part = this.GetPartbyId(partid);
                                                                if (part.objectType.equals("button")) {
                                                                    OButton btn = (OButton) part;
                                                                    btn.textStyle = style;
                                                                } else if (part.objectType.equals("field")) {
                                                                    OField fld = (OField) part;
                                                                    fld.textStyle = style;
                                                                }
                                                            }
                                                        }
                                                    } else {
                                                        logger.log(Level.DEBUG, "Local Name: " + reader.getLocalName());
                                                        logger.log(Level.DEBUG, "Element Text: " + reader.getElementText());
                                                    }
                                                } else if (eventType3 == XMLStreamReader.END_ELEMENT) {
                                                    String elm3 = reader.getLocalName();
                                                    if (elm3.equals("stylerun")) {
                                                        break;
                                                    }
                                                }
                                            }
                                        }
                                        default -> {
                                            logger.log(Level.DEBUG, "Local Name: " + reader.getLocalName());
                                            logger.log(Level.DEBUG, "Element Text: " + reader.getElementText());
                                        }
                                    }
                                } else if (eventType2 == XMLStreamReader.END_ELEMENT) {
                                    String elm2 = reader.getLocalName();
                                    if (elm2.equals("content")) {
                                        break;
                                    }
                                }
                            }
                        }
                        case "true", "false" -> {
                            // dummy
                        }
                        default -> {
                            logger.log(Level.DEBUG, "Local Name: " + reader.getLocalName());
                            logger.log(Level.DEBUG, "Element Text: " + reader.getElementText());
                        }
                    }
                }
                if (eventType == XMLStreamReader.END_ELEMENT) {
                    String elm = reader.getLocalName();
                    if (elm.equals("background")) {
                        break;
                    }
                }
            } catch (Exception ex) {
                System.err.println(ex.getMessage());
            }
        }
        return reader;
    }


    public void writeXML(XMLStreamWriter writer) throws XMLStreamException {
        writer.writeStartElement("background");
        writer.writeCharacters("\n\t\t");

        writer.writeStartElement("id");
        writer.writeCharacters(Integer.toString(id));
        writer.writeEndElement();
        writer.writeCharacters("\n\t\t");

        writer.writeStartElement("bitmap");
        writer.writeCharacters(bitmapName);
        writer.writeEndElement();
        writer.writeCharacters("\n\t\t");

        writer.writeStartElement("cantDelete");
        writer.writeCharacters(" ");
        writer.writeEmptyElement(cantDelete.toString() + " ");
        writer.writeCharacters(" ");
        writer.writeEndElement();
        writer.writeCharacters("\n\t\t");

        writer.writeStartElement("showPict");
        writer.writeCharacters(" ");
        writer.writeEmptyElement(showPict.toString() + " ");
        writer.writeCharacters(" ");
        writer.writeEndElement();
        writer.writeCharacters("\n\t\t");

        writer.writeStartElement("dontSearch");
        writer.writeCharacters(" ");
        writer.writeEmptyElement(dontSearch.toString() + " ");
        writer.writeCharacters(" ");
        writer.writeEndElement();
        writer.writeCharacters("\n\t\t");

        for (OObject part : partsList) {
            if (part.getClass() == OButton.class) {
                ((OButton) part).writeXML(writer);
            } else if (part.getClass() == OField.class) {
                ((OField) part).writeXML(writer);
            }
        }

        for (OObject part : partsList) {
            if (!part.getText().isEmpty()) {
                writer.writeStartElement("content");
                writer.writeCharacters("\n\t\t\t");
                {
                    writer.writeStartElement("layer");
                    writer.writeCharacters("background");
                    writer.writeEndElement();
                    writer.writeCharacters("\n\t\t\t");

                    writer.writeStartElement("id");
                    writer.writeCharacters(Integer.toString(part.id));
                    writer.writeEndElement();
                    writer.writeCharacters("\n\t\t\t");

                    writer.writeStartElement("text");
                    writer.writeCharacters(part.getText());
                    writer.writeEndElement();
                    writer.writeCharacters("\n\t\t");

                    if (part.objectType.equals("field") &&
                            ((OField) part).styleList != null) {
                        for (int j = 0; j < ((OField) part).styleList.size(); j++) {
                            writer.writeStartElement("stylerun");
                            writer.writeCharacters("\n\t\t\t");
                            {
                                writer.writeStartElement("offset");
                                writer.writeCharacters(Integer.toString(((OField) part).styleList.get(j).textPosition));
                                writer.writeEndElement();
                                writer.writeCharacters("\n\t\t\t");
                            }
                            {
                                writer.writeStartElement("id");
                                writer.writeCharacters(Integer.toString(((OField) part).styleList.get(j).styleId));
                                writer.writeEndElement();
                                writer.writeCharacters("\n\t\t");
                            }
                            writer.writeEndElement();
                            writer.writeCharacters("\n\t\t");
                        }
                    }
                }
                writer.writeEndElement();
                writer.writeCharacters("\n\t\t");
            }
        }

        writer.writeStartElement("name");
        writer.writeCharacters(name);
        writer.writeEndElement();
        writer.writeCharacters("\n\t\t");

        writer.writeStartElement("script");
        for (int i = 0; i < scriptList.size(); i++) {
            writer.writeCharacters(scriptList.get(i));
            if (i < scriptList.size() - 1) {
                writer.writeCharacters("\n");
            }
        }
        writer.writeEndElement();
        writer.writeCharacters("\n\t");

        writer.writeEndElement();
        writer.writeCharacters("\n\t");
    }


    @SuppressWarnings("unchecked")
    public boolean readBackgroundBlock(DataInputStream dis, int blockSize) {
        //logger.log(Level.DEBUG, "====readBackgroundBlock====");

        if (blockSize > 2000000 || blockSize < 50) {
            return false;
        }

        int offset = 50;

        // Read block data sequentially
        id = HCData.readCode(dis, 4);
        //logger.log(Level.DEBUG, "id: " + id);
        if (id < 0 || id >= 2265535) {
            //logger.log(Level.DEBUG, "!");
        }
        /*String tygersStr =*/
        HCData.readStr(dis, 4);
        //logger.log(Level.DEBUG, "tygersStr: " + tygersStr);
        int bitmapId = HCData.readCode(dis, 4);
        //logger.log(Level.DEBUG, "bitmapId: " + bitmapId);
        if (bitmapId > 0) {
            bitmapName = "BMAP_" + bitmapId + ".png";
        }
        int flags = HCData.readCode(dis, 2);
        //logger.log(Level.DEBUG, "flags: " + flags);
        dontSearch = ((flags >> 11) & 0x01) != 0;
        showPict = !(((flags >> 13) & 0x01) != 0);
        cantDelete = ((flags >> 14) & 0x01) != 0;
        /*String tygers2Str =*/
        HCData.readStr(dis, 6);
        //logger.log(Level.DEBUG, "tygers2Str: " + tygers2Str);
        /*int nextBkgndId =*/
        HCData.readCode(dis, 4);
        //logger.log(Level.DEBUG, "nextBkgndId: " + nextBkgndId);
        /*int prevBkgndId =*/
        HCData.readCode(dis, 4);
        //logger.log(Level.DEBUG, "prevBkgndId: " + prevBkgndId);
        int numofParts = HCData.readCode(dis, 2);
        //logger.log(Level.DEBUG, "numofParts: " + numofParts);
        /*String tygers3Str =*/
        HCData.readStr(dis, 6);
        //logger.log(Level.DEBUG, "tygers3Str: " + tygers3Str);
        int numofContents = HCData.readCode(dis, 2);
        //logger.log(Level.DEBUG, "numofContents: " + numofContents);
        /*int scriptType =*/
        HCData.readCode(dis, 4);
        //logger.log(Level.DEBUG, "scriptType: " + scriptType);

        for (int i = 0; i < numofParts; i++) {
            //logger.log(Level.DEBUG, "==part "+i+"==");
            int dataLen = HCData.readCode(dis, 2);
            //logger.log(Level.DEBUG, "dataLen: " + dataLen);
            if (dataLen < 30) {
                logger.log(Level.DEBUG, "!");
                /*if(dataLen>=0){
                    dataLen = (dataLen<<8) + HCData.readCode(dis, 1);
                }*/
            }
            offset += dataLen;
            int pid = HCData.readCode(dis, 2);
            //logger.log(Level.DEBUG, "part id: " + pid);
            if ((pid < 0 || pid >= 32768) && pid < 6500) {
                logger.log(Level.DEBUG, "!");
            }
            int partType = HCData.readCode(dis, 1);
            //logger.log(Level.DEBUG, "partType: " + partType);
            if (partType == 1) {
                OButton btn = new OButton(this, pid);
                btn.readButtonBlock(dis, dataLen);
                btnList.add(btn);
                partsList.add(btn);
            } else if (partType == 2) {
                OField fld = new OField(this, pid);
                fld.readFieldBlock(dis, dataLen);
                fldList.add(fld);
                partsList.add(fld);
            } else return false;

            //logger.log(Level.DEBUG, "==end of part==");
        }

        for (int i = 0; i < numofContents; i++) {
            //logger.log(Level.DEBUG, "==content "+i+"==");

            int pid;
            { // Alignment adjustment
                pid = HCData.readCode(dis, 1);
                while (pid <= i) {
                    pid = (pid << 8) + HCData.readCode(dis, 1);
                }
            }
            // {
            //    pid = HCData.readCode(dis, 2);
            // }
            //logger.log(Level.DEBUG, "pid: " + pid);
            int contLen = HCData.readCode(dis, 2);
            int orgcontLen = contLen;
            //logger.log(Level.DEBUG, "contLen: " + contLen);
            // offset += contLen+4;
            if (pid >= 32768) {
                /*String padding =*/
                HCData.readStr(dis, contLen);
                continue;
            }
            int isStyledText = 0x0000FF & HCData.readCode(dis, 1);
            if (isStyledText < 128) {
                offset += contLen + 5;
                contLen -= 1;
            } else if (isStyledText >= 128) {
                int formattingLength = ((0x007F & isStyledText) << 8) + HCData.readCode(dis, 1);
                // offset += 2;
                //logger.log(Level.DEBUG, "formattingLength: " + formattingLength);
                if (formattingLength > 100) {
                    //logger.log(Level.DEBUG, "!");
                }
                for (int j = 0; j < formattingLength / 4; j++) {
                    styleClass styleC = new styleClass();
                    styleC.textPosition = HCData.readCode(dis, 2);
                    styleC.styleId = HCData.readCode(dis, 2);
                    for (OObject oObject : partsList) {
                        if (oObject.id == pid) {
                            if (oObject.objectType.equals("field")) {
                                OField fld = (OField) oObject;
                                if (fld.styleList == null) fld.styleList = new ArrayList<>();
                                fld.styleList.add(styleC);
                                break;
                            }
                        }
                    }
                }
                offset += contLen + 4;
                contLen -= formattingLength;
            }

            // Text
            resultStr contentResult;
            if (orgcontLen % 2 == 1) {
                //logger.log(Level.DEBUG, "readText(contLen+1=" + (contLen + 1) + ")");
                contentResult = HCData.readText(dis, contLen + 1);
            } else if (contLen > 0) {
                //logger.log(Level.DEBUG, "readText(contLen=" + (contLen) + ")");
                contentResult = HCData.readText(dis, contLen);
            } else {
                contentResult = new resultStr();
                contentResult.str = "";
                contentResult.length_in_src = 0;
            }
            //logger.log(Level.DEBUG, "contentResult:" + contentResult.str);
            //HCStackDebug.debuginfo("contentResult:" + contentResult.str);
            for (OObject oObject : partsList) {
                if (oObject.id == pid) {
                    oObject.setText(contentResult.str);
                    oObject.rawTextBytes = contentResult.rawBytes;
                    break;
                }
            }

            int remainLength = contLen - ((contentResult.length_in_src));
            //logger.log(Level.DEBUG, "content-remainLength:" + remainLength);
            if (remainLength < 0 || remainLength > 16) {
                //logger.log(Level.DEBUG, "!");
            }
            if (remainLength - 1 > 0) {
                /* String padding = */ HCData.readStr(dis, remainLength - 1);
                //logger.log(Level.DEBUG, "padding:" + padding);
            }
        }

        resultStr nameResult = HCData.readTextToZero(dis, blockSize - offset);
        name = nameResult.str;
        rawNameBytes = nameResult.rawBytes;
        //logger.log(Level.DEBUG, "name:" + name);

        resultStr scriptResult = HCData.readTextToZero(dis, blockSize - offset - nameResult.length_in_src);
        String scriptStr = scriptResult.str;
        rawScriptBytes = scriptResult.rawBytes;
        //logger.log(Level.DEBUG, "scriptStr:" + scriptStr);

        int remainLength = blockSize - (offset + (nameResult.length_in_src) + (scriptResult.length_in_src));
        //logger.log(Level.DEBUG, "remainLength: " + remainLength);
        if (remainLength > 100) {
            //logger.log(Level.DEBUG, "!");
        }
        if (remainLength < 0) {
            //logger.log(Level.DEBUG, "!");
        }
        if (remainLength - 1 > 0) {
            if (remainLength - 1 > 30 && nameResult.length_in_src == 0) {
                resultStr scriptResult2 = HCData.readText(dis, remainLength - 1);
                scriptStr = scriptResult2.str;
                rawScriptBytes = scriptResult2.rawBytes;
            } else {
                /* String padding = */ HCData.readStr(dis, remainLength - 1);
                //logger.log(Level.DEBUG, "padding:" + padding);
            }
            if (nameResult.length_in_src == 0 && scriptResult.length_in_src < 200) {
                name = scriptResult.str;
                rawNameBytes = scriptResult.rawBytes;
            }
        }
//        if ((nameResult.length_in_src + 1 + scriptResult.length_in_src + 1) % 2 != 0) {
//            String padding = HCData.readStr(dis, 1);
//            logger.log(Level.DEBUG, "padding:" + padding);
//        }

        // Script
        String[] scriptAry = scriptStr.split("\n");
        Collections.addAll(scriptList, scriptAry);
        stringList = new ArrayList[scriptList.size()];
        typeList = new ArrayList[scriptList.size()];

        return true;
    }

    private static OBackground buildOBackground(OBackground bg) {
        bg.picture = new OPicture(bg);

        // For backgrounds, must always draw white even if there's no picture
        bg.label = MyLabel.getMyBgLabel(bg);
        bg.label.setBounds(0, 0, bg.stack.width, bg.stack.height);
        PCARD.pc.mainPane.add(bg.label);

        // Load picture (pbm)
        if (bg.bitmapName != null) {
            try {
                Path path = bg.stack.file.getParent().resolve(bg.bitmapName);
                // Wait until loaded
                for (int i = 0; HCData.threadList != null && i < HCData.threadList.size(); i++) {
                    Thread p = HCData.threadList.get(i);
                    if (p != null && p.getName().equals(bg.bitmapName)) {
                        if (p.getState() == Thread.State.NEW) {
                            try {
                                p.start();
                            } catch (IllegalThreadStateException e) {
                                // already started
                            }
                        }
                        long lastLength = -1;
                        for (int j = 0; !Files.exists(path) && j < 50 || p.isAlive() || (Files.exists(path) && lastLength != Files.size(path)); j++) {
                            lastLength = Files.exists(path) ? Files.size(path) : -1;
                            try {
                                Thread.sleep(50);
                            } catch (InterruptedException e) {
                                logger.log(Level.ERROR, e.getMessage(), e);
                            }
                        }
                    }
                }
                if (Files.exists(path) && !Files.isDirectory(path)) {
                    bg.pict = ImageIO.read(Files.newInputStream(path));
                }
                if (bg.pict != null) {
                    //bg.showPict = true;
                }
            } catch (IOException e) {
                logger.log(Level.ERROR, e.getMessage(), e);
            }
            return bg;
        }

        // Load picture
        File file = new File(bg.stack.file.getParent() + "/resource/bgp" + bg.id + ".png");
        //bg.showPict = false;
        bg.label = MyLabel.getMyBgLabel(bg); // Background has nothing behind it, so label is required
        bg.label.setBounds(0, 0, bg.stack.width, bg.stack.height);
        PCARD.pc.mainPane.add(bg.label);
        try {
            if (file.exists()) {
                bg.pict = ImageIO.read(file);
                //bg.showPict = true;
            } else {
                file = new File(bg.stack.file.getParent() + "/resource/bgp" + bg.id + ".jpeg");
                bg.pict = ImageIO.read(file);
                //bg.showPict = true;
            }
        } catch (Exception err1) {
            //logger.log(Level.ERROR, err1.getMessage(), err1);
        }

        return bg;
    }
}


class OBgButtonData {

    OCardBase card;
    OButton btn;
    boolean checkHighlight = false; // Holds highlight information per card
    int id;

    public OBgButtonData(OCardBase cd, int btnId) {
        card = cd;

        id = btnId;
    }

    public OBgButtonData(OCardBase cd, String data, int btnId) {
        card = cd;

        readButtonData(data);
        id = btnId;
    }

    public void readButtonData(String indata) {
        String[] data = indata.split("\n");
        boolean hilite = false;

        for (String str : data) {
            if (str.length() >= 2 && str.charAt(0) == '#' && str.charAt(1) != '#') {
                String istr;

                istr = "#hilite:";
                if (str.startsWith(istr)) {
                    String tmpstr = str.substring(istr.length());
                    hilite = (tmpstr.compareTo("true") == 0);
                    checkHighlight = hilite;
                }
            }
        }
    }
}


class OBgFieldData {

    OCardBase card;
    OField fld;
    String text;
    byte[] rawTextBytes;
    int id;
    ArrayList<styleClass> styleList;

    public OBgFieldData(OCardBase cd, String data, int fldId) {
        card = cd;
        text = "";

        readFieldData(data);
        id = fldId;
    }

    public OBgFieldData(OCardBase cd, int fldId) {
        card = cd;
        text = "";

        id = fldId;
    }

    public void readFieldData(String indata) {
        String[] data = indata.split("\n");
        int isText = 0;

        for (String datum : data) {
            String str = datum;

            if (str.length() >= 2 && str.charAt(0) == '#' && str.charAt(1) != '#') {
                isText = 0;
                String istr;

                istr = "#text:";
                if (str.startsWith(istr)) {
                    str = str.substring(istr.length());
                    isText = 1;
                }
            }

            if (isText >= 1) {
                if (isText == 2) text += "\r\n";
                text += str;
                isText = 2;
            }
        }
    }
}
