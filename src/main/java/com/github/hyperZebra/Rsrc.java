package com.github.hyperZebra;

import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;


public class Rsrc {

    Map<String, RsrcClass> rsrcIdMap = new HashMap<>();
    private final Map<String, RsrcClass> rsrcNameMap = new HashMap<>();
    private final Map<String, xcmdClass> xcmdNameMap = new HashMap<>();
    List<addcolorClass> addcolorList = new ArrayList<>();
    List<PlteClass> plteList = new ArrayList<>();
    OStack ownerstack;


    public Rsrc(OStack owner) {
        this.ownerstack = owner;
    }

    // このスタック内のリソースを検索
    public int getRsrcId1(String name, String type) {
        RsrcClass r = rsrcNameMap.get(type + name.toLowerCase());
        if (r != null) return r.id;
        return 0;
    }

    public String getFileName1(int id, String type) {
        RsrcClass r = rsrcIdMap.get(type + id);
        if (r != null) return r.filename;
        return null;
    }

    public String getName1(int id, String type) {
        RsrcClass r = rsrcIdMap.get(type + id);
        if (r != null) return r.name;
        return null;
    }

    public Point getHotSpot1(int id) {
        RsrcClass r = rsrcIdMap.get("cursor" + id);
        Point p = new Point(r.hotsporleft, r.hotsportop);
        return p;
    }

    public RsrcClass getResource1(int id, String type) {
        RsrcClass r = rsrcIdMap.get(type + id);
        return r;
    }

    // usingしているスタックすべてのリソースを検索
    public int getRsrcIdAll(String name, String type) {
        RsrcClass r = rsrcNameMap.get(type + name.toLowerCase());
        if (r != null) return r.id;
        for (int i = ownerstack.usingStacks.size() - 1; i >= 0; i--) {
            r = ownerstack.usingStacks.get(i).rsrc.rsrcNameMap.get(type + name.toLowerCase());
            if (r != null) return r.id;
        }
        return 0;
    }

    public String getFilePathAll(int id, String type) {
        RsrcClass r = rsrcIdMap.get(type + id);
        if (r != null) {
            if (this.ownerstack.file != null) {
                return ownerstack.file.getParent().resolve(r.filename).toString();
            } else {
                return r.filename;
            }
        }
        for (int i = ownerstack.usingStacks.size() - 1; i >= 0; i--) {
            r = ownerstack.usingStacks.get(i).rsrc.rsrcIdMap.get(type + id);
            if (r != null) {
                Path parentpath = ownerstack.usingStacks.get(i).file.getParent();
                return parentpath.resolve(r.filename).toString();
            }
        }
        return null;
    }

    public String getNameAll(int id, String type) {
        RsrcClass r = rsrcIdMap.get(type + id);
        if (r != null) return r.name;
        for (int i = ownerstack.usingStacks.size() - 1; i >= 0; i--) {
            r = ownerstack.usingStacks.get(i).rsrc.rsrcNameMap.get(type + id);
            if (r != null) return r.name;
        }
        return null;
    }

    public Point getHotSpotAll(int id) {
        RsrcClass r = rsrcIdMap.get("cursor" + id);
        if (r != null) {
            Point p = new Point(r.hotsporleft, r.hotsportop);
            return p;
        }
        for (int i = ownerstack.usingStacks.size() - 1; i >= 0; i--) {
            r = ownerstack.usingStacks.get(i).rsrc.rsrcNameMap.get("cursor" + id);
            if (r != null) return new Point(r.hotsporleft, r.hotsportop);
        }
        return null;
    }

    public RsrcClass getResourceAll(int id, String type) {
        RsrcClass r = rsrcIdMap.get(type + id);
        if (r != null) return r;
        for (int i = ownerstack.usingStacks.size() - 1; i >= 0; i--) {
            r = ownerstack.usingStacks.get(i).rsrc.rsrcIdMap.get(type + id);
            if (r != null) return r;
        }
        return null;
    }

    public XMLStreamReader readXML(XMLStreamReader reader) throws Exception {
        String idStr = "0", typeStr = "", nameStr = "", fnameStr = "", leftStr = "0", topStr = "0";
        FontInfo info = null;
        while (reader.hasNext()) {
            try {
                int eventType = reader.next();
                if (eventType == XMLStreamConstants.START_ELEMENT) {
                    String elm = reader.getLocalName();
                    switch (elm) {
                        case "id" -> idStr = reader.getElementText();
                        case "type" -> typeStr = reader.getElementText();
                        case "name" -> nameStr = reader.getElementText();
                        case "file" -> fnameStr = reader.getElementText();
                        case "left" -> leftStr = reader.getElementText();
                        case "top" -> topStr = reader.getElementText();
                        case "hotspot" -> {
                        }
                        case "fontinfo" -> {
                            info = new FontInfo();
                            reader = readFontInfoXML(reader, info);
                        }
                        default -> {
                            System.out.println("Local Name: " + reader.getLocalName());
                            System.out.println("Element Text: " + reader.getElementText());
                        }
                    }
                }
                if (eventType == XMLStreamConstants.END_ELEMENT) {
                    String elm = reader.getLocalName();
                    if (elm.equals("media") || elm.equals("resource")) {
                        addResource(Integer.parseInt(idStr), typeStr, nameStr, fnameStr, leftStr, topStr, info);
                        break;
                    }
                }
            } catch (Exception ex) {
                System.err.println(ex.getMessage());
            }
        }
        return reader;
    }

    public static XMLStreamReader readFontInfoXML(XMLStreamReader reader, FontInfo info) throws Exception {
        int locCount = 0;
        while (reader.hasNext()) {
            try {
                int eventType = reader.next();
                if (eventType == XMLStreamConstants.START_ELEMENT) {
                    String elm = reader.getLocalName();
                    switch (elm) {
                        case "firstChar" -> info.firstChar = (char) Integer.parseInt(reader.getElementText());
                        case "lastChar" -> info.lastChar = (char) Integer.parseInt(reader.getElementText());
                        case "fontType" -> info.fontType = (char) Integer.parseInt(reader.getElementText());
                        case "widMax" -> info.widMax = (char) Integer.parseInt(reader.getElementText());
                        case "kernMax" -> info.kernMax = (char) Integer.parseInt(reader.getElementText());
                        case "nDescent" -> info.nDescent = (char) Integer.parseInt(reader.getElementText());
                        case "fRectWidth" -> info.fRectWidth = (char) Integer.parseInt(reader.getElementText());
                        case "fRectHeight" -> info.fRectHeight = (char) Integer.parseInt(reader.getElementText());
                        case "owTLoc" -> info.owTLoc = (char) Integer.parseInt(reader.getElementText());
                        case "ascent" -> info.ascent = (char) Integer.parseInt(reader.getElementText());
                        case "descent" -> info.descent = (char) Integer.parseInt(reader.getElementText());
                        case "leading" -> info.leading = (char) Integer.parseInt(reader.getElementText());
                        case "loc" -> {
                            if (info.locs == null) {
                                info.locs = new int[info.lastChar - info.firstChar + 2];
                                locCount = 0;
                            }
                            info.locs[locCount] = Integer.parseInt(reader.getElementText());
                            locCount++;
                        }
                        case "offset" -> {
                            if (info.offsets == null) {
                                info.offsets = new int[info.lastChar - info.firstChar + 2];
                                locCount = 0;
                            }
                            info.offsets[locCount] = Integer.parseInt(reader.getElementText());
                            locCount++;
                        }
                        case "width" -> {
                            if (info.widthes == null) {
                                info.widthes = new int[info.lastChar - info.firstChar + 2];
                                locCount = 0;
                            }
                            info.widthes[locCount] = Integer.parseInt(reader.getElementText());
                            locCount++;
                        }
                        default -> {
                            System.out.println("Local Name: " + reader.getLocalName());
                            System.out.println("Element Text: " + reader.getElementText());
                        }
                    }
                }
                if (eventType == XMLStreamConstants.END_ELEMENT) {
                    String elm = reader.getLocalName();
                    if (elm.equals("fontinfo")) {
                        break;
                    }
                }
            } catch (Exception ex) {
                System.err.println(ex.getMessage());
            }
        }
        return reader;
    }

    // XML保存
    public void writeXML(XMLStreamWriter writer) throws XMLStreamException {

        for (RsrcClass rsrcClass : rsrcIdMap.values()) {
            writer.writeCharacters("\t");
            RsrcClass rsrc = rsrcClass;

            rsrc.writeXMLOneRsrc(writer);
        }
    }

    static class RsrcClass {

        int id;
        String type;
        String name;
        String filename;
        int hotsporleft;
        int hotsportop;
        OptionInfo optionInfo;

        public RsrcClass(int id, String type, String name, String filename, String left, String top, OptionInfo optionInfo) {
            this.id = id;
            this.type = type;
            this.name = name;
            this.filename = filename;
            this.hotsporleft = Integer.parseInt(left);
            this.hotsportop = Integer.parseInt(top);
            this.optionInfo = optionInfo;
        }


        public void writeXMLOneRsrc(XMLStreamWriter writer) throws XMLStreamException {
            writer.writeStartElement("media");
            writer.writeCharacters("\n\t\t");
            {
                writer.writeStartElement("id");
                writer.writeCharacters(Integer.toString(this.id));
                writer.writeEndElement();
                writer.writeCharacters("\n\t\t");

                writer.writeStartElement("type");
                writer.writeCharacters(this.type);
                writer.writeEndElement();
                writer.writeCharacters("\n\t\t");

                writer.writeStartElement("name");
                writer.writeCharacters(this.name);
                writer.writeEndElement();
                writer.writeCharacters("\n\t\t");

                writer.writeStartElement("file");
                writer.writeCharacters(this.filename);
                writer.writeEndElement();
                writer.writeCharacters("\n\t");

                if (this.type.equals("cursor")) {
                    writer.writeCharacters("\t");
                    writer.writeStartElement("hotspot");
                    writer.writeCharacters("\n\t\t\t");
                    {
                        writer.writeStartElement("left");
                        writer.writeCharacters(Integer.toString(this.hotsporleft));
                        writer.writeEndElement();
                        writer.writeCharacters("\n\t\t\t");

                        writer.writeStartElement("top");
                        writer.writeCharacters(Integer.toString(this.hotsportop));
                        writer.writeEndElement();
                        writer.writeCharacters("\n\t\t");
                    }
                    writer.writeEndElement();
                    writer.writeCharacters("\n\t");
                }

                FontInfo fontinfo = (FontInfo) this.optionInfo;
                if (this.type.equals("font") && fontinfo != null) {
                    writer.writeCharacters("\t");
                    writer.writeStartElement("fontinfo");
                    writer.writeCharacters("\n\t\t\t");
                    {

                        writer.writeStartElement("fontType");
                        writer.writeCharacters(Integer.toString(fontinfo.fontType));
                        writer.writeEndElement();
                        writer.writeCharacters("\n\t\t");

                        writer.writeStartElement("firstChar");
                        writer.writeCharacters(Integer.toString(fontinfo.firstChar));
                        writer.writeEndElement();
                        writer.writeCharacters("\n\t\t");

                        writer.writeStartElement("lastChar");
                        writer.writeCharacters(Integer.toString(fontinfo.lastChar));
                        writer.writeEndElement();
                        writer.writeCharacters("\n\t\t");

                        writer.writeStartElement("widMax");
                        writer.writeCharacters(Integer.toString(fontinfo.widMax));
                        writer.writeEndElement();
                        writer.writeCharacters("\n\t\t");

                        writer.writeStartElement("kernMax");
                        writer.writeCharacters(Integer.toString(fontinfo.kernMax));
                        writer.writeEndElement();
                        writer.writeCharacters("\n\t\t");

                        writer.writeStartElement("nDescent");
                        writer.writeCharacters(Integer.toString(fontinfo.nDescent));
                        writer.writeEndElement();
                        writer.writeCharacters("\n\t\t");

                        writer.writeStartElement("fRectWidth");
                        writer.writeCharacters(Integer.toString(fontinfo.fRectWidth));
                        writer.writeEndElement();
                        writer.writeCharacters("\n\t\t");

                        writer.writeStartElement("fRectHeight");
                        writer.writeCharacters(Integer.toString(fontinfo.fRectHeight));
                        writer.writeEndElement();
                        writer.writeCharacters("\n\t\t");

                        writer.writeStartElement("owTLoc");
                        writer.writeCharacters(Integer.toString(fontinfo.owTLoc));
                        writer.writeEndElement();
                        writer.writeCharacters("\n\t\t");

                        writer.writeStartElement("ascent");
                        writer.writeCharacters(Integer.toString(fontinfo.ascent));
                        writer.writeEndElement();
                        writer.writeCharacters("\n\t\t");

                        writer.writeStartElement("descent");
                        writer.writeCharacters(Integer.toString(fontinfo.descent));
                        writer.writeEndElement();
                        writer.writeCharacters("\n\t\t");

                        writer.writeStartElement("leading");
                        writer.writeCharacters(Integer.toString(fontinfo.leading));
                        writer.writeEndElement();
                        writer.writeCharacters("\n\t\t");

                        for (int j = 0; fontinfo.locs != null && j < fontinfo.locs.length; j++) {
                            writer.writeStartElement("loc");
                            writer.writeCharacters(Integer.toString(fontinfo.locs[j]));
                            writer.writeEndElement();
                            writer.writeCharacters("\n\t\t");
                        }

                        for (int j = 0; fontinfo.offsets != null && j < fontinfo.offsets.length; j++) {
                            writer.writeStartElement("offset");
                            writer.writeCharacters(Integer.toString(fontinfo.offsets[j]));
                            writer.writeEndElement();
                            writer.writeCharacters("\n\t\t");
                        }

                        for (int j = 0; fontinfo.widthes != null && j < fontinfo.widthes.length; j++) {
                            writer.writeStartElement("width");
                            writer.writeCharacters(Integer.toString(fontinfo.widthes[j]));
                            writer.writeEndElement();
                            writer.writeCharacters("\n\t\t");
                        }
                    }
                    writer.writeEndElement();
                    writer.writeCharacters("\n\t");
                }
            }
            writer.writeEndElement();
            writer.writeCharacters("\n");
        }

    }

    static class OptionInfo {

    }


    public int getNewResourceId(String type) {
        return getNewResourceId(type, 1000);
    }

    public int getNewResourceId(String type, int baseid) {
        int id = baseid;
        while (true) {
            if (null == getFilePathAll(id, type) && id >= -32768 && id <= Integer.MAX_VALUE) {
                break;
            }
            id++;
            if (id < -32768 || id >= 32768) id = 1;
        }
        return id;
    }

    public void addResource(int id, String type, String name, String path) {
        RsrcClass rsrc = new RsrcClass(id, type, name, path, "0", "0", null);
        rsrcIdMap.put(type + id, rsrc);
        rsrcNameMap.put(type + name.toLowerCase(), rsrc);
        ownerstack.changed = true;
    }

    public void addResource(int id, String type, String name, String filename, String leftStr, String topStr) {
        RsrcClass rsrc = new RsrcClass(id, type, name, filename, leftStr, topStr, null);
        rsrcIdMap.put(type + id, rsrc);
        rsrcNameMap.put(type + name.toLowerCase(), rsrc);
        ownerstack.changed = true;
    }

    public void addResource(int id, String type, String name, String filename, String leftStr, String topStr, OptionInfo info) {
        RsrcClass rsrc = new RsrcClass(id, type, name, filename, leftStr, topStr, info);
        rsrcIdMap.put(type + id, rsrc);
        rsrcNameMap.put(type + name.toLowerCase(), rsrc);
        ownerstack.changed = true;
    }

    public void addResource(RsrcClass r) {
        rsrcIdMap.put(r.type + r.id, r);
        rsrcNameMap.put(r.type + r.name.toLowerCase(), r);
        ownerstack.changed = true;
    }

    public boolean deleteResource(String type, int id) throws IOException {
        RsrcClass r = rsrcIdMap.get(type + id);
        if (r == null) return false;
        Path FilePath = this.ownerstack.file.getParent().resolve(r.filename);
        // (new File(FilePath)).delete();
        Files.move(FilePath, Path.of("resource_trash", r.filename));
        if (null == rsrcIdMap.remove(type + r.id)) {
            System.out.println("delete resource error");
        }
        if (null == rsrcNameMap.remove(type + r.name.toLowerCase())) {
            System.out.println("delete resource error");
        }
        ownerstack.changed = true;
        return true;
    }


    public int getRsrcCount(String type) {
        Iterator<RsrcClass> it = rsrcIdMap.values().iterator();
        int i = 0;
        RsrcClass rsrc;
        while (it.hasNext()) {
            rsrc = it.next();
            if (rsrc.type.equals(type)) {
                i++;
            }
        }
        return i;
    }

    public int getRsrcCountAll(String type) {
        Iterator<RsrcClass> it = rsrcIdMap.values().iterator();
        int i = 0;
        RsrcClass rsrc;
        while (it.hasNext()) {
            rsrc = it.next();
            if (rsrc.type.equals(type)) {
                i++;
            }
        }
        for (int j = ownerstack.usingStacks.size() - 1; j >= 0; j--) {
            OStack rsrcstack = ownerstack.usingStacks.get(j);
            it = rsrcstack.rsrc.rsrcIdMap.values().iterator();
            while (it.hasNext()) {
                rsrc = it.next();
                if (rsrc.type.equals(type)) {
                    if ((rsrcstack.file.getParent().resolve(rsrc.filename)
                            .equals(Path.of(getFilePathAll(rsrc.id, type))))) {
                        i++;
                    }
                }
            }
        }
        return i;
    }

    public RsrcClass getRsrcByIndex(String type, int index) {
        Iterator<RsrcClass> it = rsrcIdMap.values().iterator();
        int i = 0;
        RsrcClass rsrc;
        while (it.hasNext()) {
            rsrc = it.next();
            if (rsrc.type.equals(type)) {
                if (i == index) {
                    return rsrc;
                }
                i++;
            }
        }
        return null;
    }


    // ----------------
    // for icon
    // ----------------
    public BufferedImage getImage(int id) {
        BufferedImage bi = null;
        String path = this.getFilePathAll(id, "icon");
        /*String path = "";
        if(this.ownerstack.file!=null){
            path = this.ownerstack.file.getParent()+File.separatorChar+filename;
        }else{
            path = filename;
        }*/
        try {
            bi = javax.imageio.ImageIO.read(new File(path));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return bi;
    }


    // ----------------
    // for PICT etc
    // ----------------
    public BufferedImage getImage(int id, String type) {
        BufferedImage bi = null;
        String path = this.getFilePathAll(id, type);
        /*String path = "";
        path = this.ownerstack.file.getParent()+File.separatorChar+filename;*/
        try {
            bi = javax.imageio.ImageIO.read(new File(path));
        } catch (IOException e) {
            // e.printStackTrace();
        }

        return bi;
    }

    // ----------------
    // for FONT
    // ----------------
    static class FontInfo extends OptionInfo {

        int fontType;
        char firstChar;
        char lastChar;
        int widMax;
        int kernMax;
        int nDescent;
        int fRectWidth;
        int fRectHeight;
        int owTLoc;
        int ascent;
        int descent;
        int leading;
        int[] locs;
        int[] offsets;
        int[] widthes;
    }

    public void addFontResource(int id, String type, String name, String path, FontInfo info) {
        RsrcClass rsrc = new RsrcClass(id, type, name, path, "0", "0", info);
        rsrcIdMap.put(type + id, rsrc);
        rsrcNameMap.put(type + name.toLowerCase(), rsrc);
    }

    // ----------------
    // XCMD
    // ----------------
    public int getxcmdId(String name, String type) {
        xcmdClass r = xcmdNameMap.get(type + name.toLowerCase());
        if (r != null) return r.id;
        return 0;
    }

    public void addXcmd(xcmdClass xcmd) {
        xcmdNameMap.put(xcmd.type + xcmd.name.toLowerCase(), xcmd);
    }

    public XMLStreamReader readXCMDXML(XMLStreamReader reader) throws Exception {
        String idStr = "0", typeStr = "", nameStr = "", pathStr = "", platformStr = "", sizeStr = "";
        int count = reader.getAttributeCount();
        for (int i = 0; i < count; i++) {
            String attr = reader.getAttributeLocalName(i);
            switch (attr) {
                case "id" -> idStr = reader.getAttributeValue(i);
                case "type" -> typeStr = reader.getAttributeValue(i);
                case "platform" -> platformStr = reader.getAttributeValue(i);
                case "name" -> nameStr = reader.getAttributeValue(i);
                case "size" -> sizeStr = reader.getAttributeValue(i);
                case "file" -> pathStr = reader.getAttributeValue(i);
            }
        }
        // xcmdList.add(new xcmdClass(idStr, typeStr, nameStr, pathStr, platformStr, sizeStr));
        xcmdClass xcmd = new xcmdClass(idStr, typeStr, nameStr, pathStr, platformStr, sizeStr);
        xcmdNameMap.put(typeStr + nameStr.toLowerCase(), xcmd);
        return reader;
    }


    public void writeXcmdXML(XMLStreamWriter writer) throws XMLStreamException {
        Iterator<xcmdClass> it = xcmdNameMap.values().iterator();
        xcmdClass xcmd;
        while (it.hasNext()) {
            writer.writeCharacters("\t");
            xcmd = it.next();
            writer.writeEmptyElement("externalcommand");
            writer.writeAttribute("type", xcmd.type);
            writer.writeAttribute("platform", xcmd.platform);
            writer.writeAttribute("id", Integer.toString(xcmd.id));
            writer.writeAttribute("size", Integer.toString(xcmd.size));
            writer.writeAttribute("name", xcmd.name);
            writer.writeAttribute("file", xcmd.filename);
            writer.writeCharacters("\n");
        }
    }


    static class xcmdClass {

        int id;
        String type; // command or function
        String name;
        String filename;
        String platform;
        int size;

        public xcmdClass(String id, String type, String name, String filename, String platform, String size) {
            this.id = Integer.parseInt(id);
            this.type = type;
            this.name = name;
            this.filename = filename;
            this.platform = platform;
            this.size = Integer.parseInt(size);
        }
    }


    // -------------
    // AddColor関連
    // -------------
    public XMLStreamReader readAddColorXML(XMLStreamReader reader, boolean isBg) throws Exception {
        String idStr = "0";
        addcolorClass addColorOwner = null;
        while (reader.hasNext()) {
            try {
                int eventType = reader.next();
                if (eventType == XMLStreamConstants.START_ELEMENT) {
                    String elm = reader.getLocalName();
                    if (elm.equals("id")) {
                        idStr = reader.getElementText();
                        addColorOwner = new addcolorClass(idStr, isBg);
                    } else if (elm.equals("addcolorobject")) {
                        reader = readAddColorObjXML(reader, addColorOwner);
                    } else {
                        System.out.println("Local Name: " + reader.getLocalName());
                        System.out.println("Element Text: " + reader.getElementText());
                    }
                }
                if (eventType == XMLStreamConstants.END_ELEMENT) {
                    String elm2 = reader.getLocalName();
                    if (elm2.equals("addcolorbackground")) {
                        break;
                    }
                    if (elm2.equals("addcolorcard")) {
                        break;
                    }
                }
            } catch (Exception ex) {
                System.err.println(ex.getMessage());
            }
        }
        addcolorList.add(addColorOwner);
        return reader;
    }


    public XMLStreamReader readAddColorObjXML(XMLStreamReader reader, addcolorClass parent) throws Exception {
        String typeStr = "", nameStr = "";
        boolean transparent = false;
        boolean visible = true;
        Rectangle rect = new Rectangle();
        int id = 0, bevel = 0, red = 0, green = 0, blue = 0;
        while (reader.hasNext()) {
            try {
                int eventType = reader.next();
                if (eventType == XMLStreamConstants.START_ELEMENT) {
                    String elm = reader.getLocalName();
                    switch (elm) {
                        case "type" -> typeStr = reader.getElementText();
                        case "id" -> id = Integer.parseInt(reader.getElementText());
                        case "top" -> rect.y = Integer.parseInt(reader.getElementText());
                        case "left" -> rect.x = Integer.parseInt(reader.getElementText());
                        case "bottom" -> rect.height = Integer.parseInt(reader.getElementText()) - rect.y;
                        case "right" -> rect.width = Integer.parseInt(reader.getElementText()) - rect.x;
                        case "transparent" -> {
                            transparent = XMLRead.bool(reader);
                            reader.next();
                        }
                        case "name" -> nameStr = reader.getElementText();
                        case "visible" -> {
                            visible = XMLRead.bool(reader);
                            reader.next();
                        }
                        case "rect" -> {
                        }
                        case "bevel" -> bevel = Integer.parseInt(reader.getElementText());
                        case "color" -> {
                        }
                        case "red" -> red = Integer.parseInt(reader.getElementText()) / 256;
                        case "green" -> green = Integer.parseInt(reader.getElementText()) / 256;
                        case "blue" -> blue = Integer.parseInt(reader.getElementText()) / 256;
                        default -> {
                            System.out.println("Local Name: " + reader.getLocalName());
                            System.out.println("Element Text: " + reader.getElementText());
                        }
                    }
                }
                if (eventType == XMLStreamConstants.END_ELEMENT) {
                    String elm2 = reader.getLocalName();
                    if (elm2.equals("addcolorobject")) {
                        break;
                    }
                }
            } catch (Exception ex) {
                System.err.println(ex.getMessage());
            }
        }
        if (typeStr.equals("picture")) {
            parent.addPictObject(nameStr, rect, transparent, visible);
        }
        if (typeStr.equals("rectangle")) {
            parent.addRectObject(rect, bevel, new Color(red, green, blue), visible);
        }
        if (typeStr.equals("button")) {
            parent.addBtnObject(id, bevel, new Color(red, green, blue), visible);
        }
        if (typeStr.equals("field")) {
            parent.addFldObject(id, bevel, new Color(red, green, blue), visible);
        }
        return reader;
    }


    public static void setEnabledACObj(int number, boolean enabled) {
        // addcolorClassを探す
        List<addcolorClass> cdList = PCARD.pc.stack.rsrc.addcolorList;
        addcolorClass accd = null;
        for (Rsrc.addcolorClass addcolorClass : cdList) {
            if (PCARD.pc.stack.curCard.id == addcolorClass.id) {
                accd = addcolorClass;
                break;
            }
        }
        if (accd == null) return;

        if (number < accd.objList.size()) {
            accd.objList.get(number).visible = enabled;
        }
    }

    static class addcolorClass {

        int id;
        boolean isBg;
        ArrayList<addcolorObjClass> objList = new ArrayList<>();

        public addcolorClass(String id, boolean isBg) {
            this.id = Integer.parseInt(id);
            this.isBg = isBg;
        }

        public void addPictObject(String name, Rectangle rect, boolean transparent, boolean visible) {
            objList.add(new PictObject(name, rect, transparent, visible));
        }

        public void addRectObject(Rectangle rect, int bevel, Color color, boolean visible) {
            objList.add(new RectObject(rect, bevel, color, visible));
        }

        public void addBtnObject(int id, int bevel, Color color, boolean visible) {
            objList.add(new BtnObject(id, bevel, color, visible));
        }

        public void addFldObject(int id, int bevel, Color color, boolean visible) {
            objList.add(new FldObject(id, bevel, color, visible));
        }

        public void writeAddColorXMLOne(XMLStreamWriter writer) throws XMLStreamException {
            if (isBg) {
                writer.writeStartElement("addcolorbackground");
            } else {
                writer.writeStartElement("addcolorcard");
            }
            writer.writeCharacters("\n\t\t");
            {
                writer.writeStartElement("id");
                writer.writeCharacters(Integer.toString(this.id));
                writer.writeEndElement();
                writer.writeCharacters("\n\t\t");

                for (Rsrc.addcolorObjClass addcolorObjClass : objList) {
                    writer.writeStartElement("addcolorobject");

                    addcolorObjClass obj = addcolorObjClass;
                    PictObject picto = null;
                    RectObject recto = null;
                    BtnObject btno = null;
                    FldObject fldo = null;

                    writer.writeStartElement("type");
                    if (obj.getClass() == PictObject.class) {
                        writer.writeCharacters("picture");
                        picto = (PictObject) obj;
                    } else if (obj.getClass() == RectObject.class) {
                        writer.writeCharacters("rectangle");
                        recto = (RectObject) obj;
                    } else if (obj.getClass() == BtnObject.class) {
                        writer.writeCharacters("button");
                        btno = (BtnObject) obj;
                    } else if (obj.getClass() == FldObject.class) {
                        writer.writeCharacters("field");
                        fldo = (FldObject) obj;
                    }
                    writer.writeEndElement();
                    writer.writeCharacters("\n\t\t\t");

                    int id = 0;
                    if (btno != null) id = btno.id;
                    else if (fldo != null) id = fldo.id;
                    if (id > 0) {
                        writer.writeStartElement("id");
                        writer.writeCharacters(Integer.toString(id));
                        writer.writeEndElement();
                        writer.writeCharacters("\n\t\t\t");
                    }

                    Rectangle rect = null;
                    if (picto != null) rect = picto.rect;
                    else if (recto != null) rect = recto.rect;
                    if (rect != null) {
                        writer.writeStartElement("rect");
                        writer.writeCharacters("\n\t\t\t\t");
                        {
                            writer.writeStartElement("left");
                            writer.writeCharacters(Integer.toString(rect.x));
                            writer.writeEndElement();
                            writer.writeCharacters("\n\t\t\t\t");

                            writer.writeStartElement("top");
                            writer.writeCharacters(Integer.toString(rect.y));
                            writer.writeEndElement();
                            writer.writeCharacters("\n\t\t\t\t");

                            writer.writeStartElement("right");
                            writer.writeCharacters(Integer.toString(rect.x + rect.width));
                            writer.writeEndElement();
                            writer.writeCharacters("\n\t\t\t\t");

                            writer.writeStartElement("bottom");
                            writer.writeCharacters(Integer.toString(rect.y + rect.height));
                            writer.writeEndElement();
                            writer.writeCharacters("\n\t\t\t");
                        }
                        writer.writeEndElement();
                        writer.writeCharacters("\n\t\t\t");
                    }

                    if (picto != null) {
                        writer.writeStartElement("name");
                        writer.writeCharacters(picto.name);
                        writer.writeEndElement();
                        writer.writeCharacters("\n\t\t\t");

                        writer.writeStartElement("transparent");
                        writer.writeCharacters(picto.transparent ? "true" : "false");
                        writer.writeEndElement();
                        writer.writeCharacters("\n\t\t\t");
                    }

                    if (picto == null) {
                        writer.writeStartElement("bevel");
                        writer.writeCharacters(Integer.toString(obj.bevel));
                        writer.writeEndElement();
                        writer.writeCharacters("\n\t\t\t");
                    }

                    writer.writeStartElement("visible");
                    writer.writeCharacters(" ");
                    writer.writeEmptyElement(obj.visible + " ");
                    writer.writeCharacters(" ");
                    writer.writeEndElement();
                    writer.writeCharacters("\n\t\t\t");

                    if (picto == null) {
                        writer.writeStartElement("color");
                        writer.writeCharacters("\n\t\t\t\t");
                        {
                            writer.writeStartElement("red");
                            writer.writeCharacters(Integer.toString(obj.color.getRed() * 256));
                            writer.writeEndElement();
                            writer.writeCharacters("\n\t\t\t\t");

                            writer.writeStartElement("green");
                            writer.writeCharacters(Integer.toString(obj.color.getGreen() * 256));
                            writer.writeEndElement();
                            writer.writeCharacters("\n\t\t\t\t");

                            writer.writeStartElement("blue");
                            writer.writeCharacters(Integer.toString(obj.color.getBlue() * 256));
                            writer.writeEndElement();
                            writer.writeCharacters("\n\t\t\t");
                        }
                        writer.writeEndElement();
                        writer.writeCharacters("\n\t\t\t");
                    }

                    writer.writeEndElement();
                    writer.writeCharacters("\n\t\t");
                }
            }
            writer.writeEndElement();
            writer.writeCharacters("\n");
        }
    }

    static class addcolorObjClass {

        int bevel;
        Color color;
        boolean visible;

        public addcolorObjClass(int bevel, Color color, boolean visible) {
            this.bevel = bevel;
            this.color = color;
            this.visible = visible;
        }
    }

    static class PictObject extends addcolorObjClass {

        String name;
        boolean transparent;
        Rectangle rect;

        public PictObject(String name, Rectangle rect, boolean transparent, boolean visible) {
            super(0, null, visible);
            this.name = name;
            this.rect = rect;
            this.transparent = transparent;
        }
    }

    static class RectObject extends addcolorObjClass {

        Rectangle rect;

        public RectObject(Rectangle rect, int bevel, Color color, boolean visible) {
            super(bevel, color, visible);
            this.rect = rect;
        }
    }

    static class BtnObject extends addcolorObjClass {

        int id;

        public BtnObject(int id, int bevel, Color color, boolean visible) {
            super(bevel, color, visible);
            this.id = id;
        }
    }

    static class FldObject extends addcolorObjClass {

        int id;

        public FldObject(int id, int bevel, Color color, boolean visible) {
            super(bevel, color, visible);
            this.id = id;
        }
    }


    // XML保存
    public void writeAddColorXML(XMLStreamWriter writer) throws XMLStreamException {
        List<addcolorClass> cdList = PCARD.pc.stack.rsrc.addcolorList;
        for (Rsrc.addcolorClass addcolorClass : cdList) {
            addcolorClass.writeAddColorXMLOne(writer);
        }
    }


    // -------------
    // PLTE関連
    // -------------
    public XMLStreamReader readPlteXML(XMLStreamReader reader) throws Exception {
        int id = 0;
        String name = "";
        int WindowDef = 0;
        boolean clearHilite = true;
        int btnType = 0;
        int pictId = 0;
        Point pictHV = new Point(0, 0);
        PlteClass plteOwner = null;

        while (reader.hasNext()) {
            try {
                int eventType = reader.next();
                if (eventType == XMLStreamConstants.START_ELEMENT) {
                    String elm = reader.getLocalName();
                    switch (elm) {
                        case "id" -> id = Integer.parseInt(reader.getElementText());
                        case "name" -> name = reader.getElementText();
                        case "windowdefinition" -> WindowDef = Integer.parseInt(reader.getElementText());
                        case "clearhilite" -> {
                            clearHilite = XMLRead.bool(reader);
                            reader.next();
                        }
                        case "buttontype" -> {
                            String typeStr = reader.getElementText();
                            if (typeStr.equals("solid")) {
                                btnType = 0;
                            } else if (typeStr.equals("frame")) {
                                btnType = 1;
                            }
                        }
                        case "pictureid" -> pictId = Integer.parseInt(reader.getElementText());
                        case "horizontaloffset" -> pictHV.x = Integer.parseInt(reader.getElementText());
                        case "verticaloffset" -> pictHV.y = Integer.parseInt(reader.getElementText());
                        case "paletteobject" -> {
                            if (plteOwner == null) {
                                plteOwner = new PlteClass(id, name, WindowDef, clearHilite, btnType, pictId, pictHV);
                            }
                            reader = readPlteObjXML(reader, plteOwner);
                        }
                        default -> {
                            System.out.println("Local Name: " + reader.getLocalName());
                            System.out.println("Element Text: " + reader.getElementText());
                        }
                    }
                }
                if (eventType == XMLStreamConstants.END_ELEMENT) {
                    String elm2 = reader.getLocalName();
                    if (elm2.equals("palette")) {
                        break;
                    }
                }
            } catch (Exception ex) {
                System.err.println(ex.getMessage());
            }
        }
        plteList.add(plteOwner);
        return reader;
    }


    public XMLStreamReader readPlteObjXML(XMLStreamReader reader, PlteClass parent) throws Exception {
        String message = "";
        Rectangle rect = new Rectangle();
        // int id=0;
        while (reader.hasNext()) {
            try {
                int eventType = reader.next();
                if (eventType == XMLStreamConstants.START_ELEMENT) {
                    String elm = reader.getLocalName();
                    switch (elm) {
                        case "id" ->  /*id =*/
                                Integer.valueOf(reader.getElementText());
                        case "top" -> rect.y = Integer.parseInt(reader.getElementText());
                        case "left" -> rect.x = Integer.parseInt(reader.getElementText());
                        case "bottom" -> rect.height = Integer.parseInt(reader.getElementText()) - rect.y;
                        case "right" -> rect.width = Integer.parseInt(reader.getElementText()) - rect.x;
                        case "message" -> message = reader.getElementText();
                        case "rect" -> {
                        }
                        default -> {
                            System.out.println("Local Name: " + reader.getLocalName());
                            System.out.println("Element Text: " + reader.getElementText());
                        }
                    }
                }
                if (eventType == XMLStreamConstants.END_ELEMENT) {
                    String elm2 = reader.getLocalName();
                    if (elm2.equals("paletteobject")) {
                        break;
                    }
                }
            } catch (Exception ex) {
                System.err.println(ex.getMessage());
            }
        }

        parent.addRectBtn(rect, message);

        return reader;
    }


    static class PlteClass {

        int id;
        String name;
        int windowDef;
        boolean clearHilite;
        int btnType;
        int pictId;
        Point pictHV;

        ArrayList<PlteBtnObject> objList = new ArrayList<>();

        public PlteClass(int id, String name, int windowDef, boolean clearHilite, int btnType, int pictId, Point pictHV) {
            this.id = id;
            this.name = name;
            this.windowDef = windowDef;
            this.clearHilite = clearHilite;
            this.btnType = btnType;
            this.pictId = pictId;
            this.pictHV = pictHV;
        }

        public void addRectBtn(Rectangle rect, String message) {
            objList.add(new PlteBtnObject(rect, message));
        }

        public void writePlteXMLOne(XMLStreamWriter writer) throws XMLStreamException {
            writer.writeStartElement("palette");
            writer.writeCharacters("\n\t\t");
            {
                writer.writeStartElement("id");
                writer.writeCharacters(Integer.toString(this.id));
                writer.writeEndElement();
                writer.writeCharacters("\n\t\t");

                writer.writeStartElement("name");
                writer.writeCharacters(this.name);
                writer.writeEndElement();
                writer.writeCharacters("\n\t\t");

                writer.writeStartElement("windowdefinition");
                writer.writeCharacters(Integer.toString(this.windowDef));
                writer.writeEndElement();
                writer.writeCharacters("\n\t\t");

                writer.writeStartElement("clearhilite");
                writer.writeCharacters(" ");
                writer.writeEmptyElement(this.clearHilite + " ");
                writer.writeCharacters(" ");
                writer.writeEndElement();
                writer.writeCharacters("\n\t\t");

                writer.writeStartElement("pictureid");
                writer.writeCharacters(Integer.toString(this.pictId));
                writer.writeEndElement();
                writer.writeCharacters("\n\t\t");

                writer.writeStartElement("verticaloffset");
                writer.writeCharacters(Integer.toString(this.pictHV.y));
                writer.writeEndElement();
                writer.writeCharacters("\n\t\t");

                writer.writeStartElement("horizontaloffset");
                writer.writeCharacters(Integer.toString(this.pictHV.x));
                writer.writeEndElement();
                writer.writeCharacters("\n\t\t");

                for (int i = 0; i < objList.size(); i++) {
                    writer.writeStartElement("paletteobject");
                    writer.writeCharacters("\n\t\t\t");

                    PlteBtnObject obj = objList.get(i);

                    {
                        writer.writeStartElement("id");
                        writer.writeCharacters(Integer.toString(i + 1));
                        writer.writeEndElement();
                        writer.writeCharacters("\n\t\t\t");
                    }

                    Rectangle rect = obj.rect;

                    {
                        writer.writeStartElement("rect");
                        writer.writeCharacters("\n\t\t\t\t");
                        {
                            writer.writeStartElement("left");
                            writer.writeCharacters(Integer.toString(rect.x));
                            writer.writeEndElement();
                            writer.writeCharacters("\n\t\t\t\t");

                            writer.writeStartElement("top");
                            writer.writeCharacters(Integer.toString(rect.y));
                            writer.writeEndElement();
                            writer.writeCharacters("\n\t\t\t\t");

                            writer.writeStartElement("right");
                            writer.writeCharacters(Integer.toString(rect.x + rect.width));
                            writer.writeEndElement();
                            writer.writeCharacters("\n\t\t\t\t");

                            writer.writeStartElement("bottom");
                            writer.writeCharacters(Integer.toString(rect.y + rect.height));
                            writer.writeEndElement();
                            writer.writeCharacters("\n\t\t\t");
                        }
                        writer.writeEndElement();
                        writer.writeCharacters("\n\t\t\t");
                    }

                    {
                        writer.writeStartElement("message");
                        writer.writeCharacters(obj.message);
                        writer.writeEndElement();
                        writer.writeCharacters("\n\t\t\t");
                    }

                    writer.writeEndElement();
                    writer.writeCharacters("\n\t\t");
                }
            }
            writer.writeEndElement();
            writer.writeCharacters("\n");
        }
    }

    // XML保存
    public void writePlteXml(XMLStreamWriter writer) throws XMLStreamException {
        List<PlteClass> plteList = PCARD.pc.stack.rsrc.plteList;
        for (PlteClass plteClass : plteList) {
            plteClass.writePlteXMLOne(writer);
        }
    }

    static class PlteBtnObject {

        Rectangle rect;
        String message;

        public PlteBtnObject(Rectangle rect, String message) {
            this.rect = rect;
            this.message = message;
        }
    }
}
