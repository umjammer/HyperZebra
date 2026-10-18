package com.github.hyperZebra;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Locale;
import javax.imageio.ImageIO;


public class HCData {

    public static boolean readDataFork(DataInputStream dis, OStack stack) {
        // System.out.println("==readDataFork==");

        boolean result = false;
        boolean isReadStack = false;
        int aligncnt = 0; // for debug
        int errCount = 0;

        try {
            while (dis.available() > 0) {
                int blockSize = readCode(dis, 2); // int blockSize = readCode(dis, 4); Only read 2 bytes to avoid drift
                int typeCode = readCode(dis, 4);

                if (stack.totalSize > 0) {
                    try {
                        stack.bar.setValue(stack.barOffset + (73 * (stack.totalSize - dis.available())) / stack.totalSize);
                        stack.bar.paintImmediately(stack.bar.getBounds());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

label:
                while (true) {
                    String typeStr = "";
                    typeStr += (char) (0xFF & (typeCode >> 24));
                    typeStr += (char) (0xFF & (typeCode >> 16));
                    typeStr += (char) (0xFF & (typeCode >> 8));
                    typeStr += (char) (0xFF & (typeCode >> 0));

                    switch (typeStr) {
                        case "STAK":
                            if (isReadStack) {
                                break label;
                            }
                            HCStackDebug.blockstart(typeStr);
                            isReadStack = true;
                            stack.bar.setString("Converting Stack Data...");
                            stack.readStackBlock(dis, blockSize);
                            break;
                        case "STBL":
                            stack.bar.setString("Converting Style Data...");
                            HCStackDebug.blockstart(typeStr);
                            stack.readStyleBlock(dis, blockSize);
                            break;
                        case "FTBL":
                            stack.bar.setString("Converting Font Data...");
                            HCStackDebug.blockstart(typeStr);
                            stack.readFontBlock(dis, blockSize);
                            break;
                        case "LIST":
                            stack.bar.setString("Converting List Data...");
                            HCStackDebug.blockstart(typeStr);
                            stack.readListBlock(dis, blockSize);
                            break;
                        case "PAGE":
                            stack.bar.setString("Converting Page Data...");
                            HCStackDebug.blockstart(typeStr);
                            stack.readPageBlock(dis, blockSize);
                            break;
                        case "CARD":
                            HCStackDebug.blockstart(typeStr);
                            stack.bar.setString("Converting Card Data...");
                            OCard cd = new OCard(PCARDFrame.pc.stack);
                            if (cd.readCardBlock(dis, blockSize)) {
                                // stack.cdCacheList.add(cd); // Registered by new OCard()
                            } else {
                                errCount++;
                            }
                            System.gc(); // Call GC as much as possible

                            break;
                        case "BKGD":
                            HCStackDebug.blockstart(typeStr);
                            stack.bar.setString("Converting Background Data...");
                            OBackground bg = new OBackground(PCARDFrame.pc.stack);
                            if (bg.readBackgroundBlock(dis, blockSize)) {
                                stack.AddNewBg(bg);
                            } else {
                                errCount++;
                            }
                            break;
                        case "MAST":
                            HCStackDebug.blockstart(typeStr);
                            OStack.readNullBlock(dis, blockSize);
                            break;
                        case "BMAP":
                            HCStackDebug.blockstart(typeStr);
                            stack.bar.setString("Converting Picture Data...");
                            stack.bar.paintImmediately(stack.bar.getBounds());
                            readPictureBlock(dis, blockSize, stack);
                            break;
                        case "FREE":
                            HCStackDebug.blockstart(typeStr);
                            OStack.readNullBlock(dis, blockSize);
                            break;
                        case "PRNT":
                            HCStackDebug.blockstart(typeStr);
                            OStack.readNullBlock(dis, blockSize);
                            break;
                        case "PRST":
                            HCStackDebug.blockstart(typeStr);
                            OStack.readNullBlock(dis, blockSize);
                            break;
                        case "PRFT":
                            HCStackDebug.blockstart(typeStr);
                            OStack.readNullBlock(dis, blockSize);
                            break;
                        case "TAIL":
                            HCStackDebug.blockstart(typeStr);
                            if (isReadStack) {
                                result = true;
                            }
                            break label;
                        default:
                            while (true) {
                                // Try to fix alignment
                                blockSize = (0x00FFFFFF & blockSize) << 8;
                                blockSize += (0x00FF & (typeCode >> 24));
                                // System.out.println("<blockSize:"+blockSize);

                                typeCode = typeCode << 8;
                                int read = readCode(dis, 1);
                                if (read == -1) throw new IOException();
                                typeCode += read;
                                // System.out.println("<typeCode:"+typeCode);

                                aligncnt++;
                                if (aligncnt == 32) {
                                    // System.out.println("!");
                                }

                                if ((typeCode & 0xFF000000) != 0x00000000) {
                                    break;
                                }
                            }
                            continue;
                    }
                    // HCStackDebug.debuginfo("<<end of "+typeStr+">>");
                    // HCStackDebug.debuginfo("size:"+Integer.toString(blockSize));
                    // System.out.println("blockSize:"+blockSize);
                    // System.out.println("typeStr:"+typeStr);
                    aligncnt = 0;
                    break;
                }
                if (result == true) {
                    break;
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Output debug info
        /*File f = new File("./debug_"+stack.name+".txt");
        try {
            FileOutputStream stream = new FileOutputStream(f);
            stream.write(HCStackDebug.allStr.toString().getBytes());
            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }*/

        if (result == true) {
            // Check number of cards
            // System.out.println(stack.cdCacheList.size()+","+stack.cardIdList.size());
            if (stack.cdCacheList.size() != stack.cardIdList.size()) {
                System.out.println("number of cards check error.");
                result = false;
            }
            if (errCount > 0) {
                System.out.println("errCount=" + errCount);
                result = false;
            }
        }

        // Start picture conversion
        new convertPictureThread().start();

        return result;
    }

    static int readCode(DataInputStream dis, int size) {
        byte[] opcode = new byte[size];
        for (int i = 0; i < size; i++) {
            try {
                int c = dis.read();
                if (c == -1 && size == 1) return -1;
                HCStackDebug.read(c);
                opcode[i] = (byte) c;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        int iop = 0;
        if (size == 1) {
            iop = (opcode[0]) & 0xff;
            // System.out.println("code1:"+opcode[0]);
        } else if (size == 2) {
            iop = (short) ((opcode[0] & 0xff) << 8) + (opcode[1] & 0xff);
            // System.out.println("code2:"+opcode[0]+" "+opcode[1]);
        } else if (size == 4) {
            iop = ((opcode[0] & 0xff) << 24) + ((opcode[1] & 0xff) << 16)
                    + ((opcode[2] & 0xff) << 8) + (opcode[3] & 0xff);
            // System.out.println("code4:"+opcode[0]+" "+opcode[1]+" "+opcode[2]+" "+opcode[3]);
        } else {
            // System.out.println("!!!");
        }
        return iop;
    }

    static String readStr(DataInputStream dis, int size) {
        StringBuilder str = new StringBuilder(size);
        // String debugStr = "";
        for (int i = 0; i < size; i++) {
            try {
                int v = dis.read();
                HCStackDebug.read(v);
                str.append((char) v);
                // debugStr += " "+v;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        // if(debugStr.length()<40){
        // System.out.println("str:"+debugStr);
        // }else{
        // System.out.println("str/:"+debugStr.substring(0,40));
        // System.out.println("/str"+debugStr.substring(debugStr.length()-40));
        // }
        return str.toString();
    }

    static resultStr readText(DataInputStream dis, int maxLen) {
        resultStr result = new resultStr();
        if (maxLen < 0) {
            result.str = "";
            return result;
        }
        byte[] b = new byte[maxLen];
        result.length_in_src = 0;

        int i = 0;
        int i2 = 0;
        try {
            for (; i + i2 < maxLen; i++) {
                int c = dis.read();
                HCStackDebug.read(c);
                result.length_in_src++;
                if (c < 0) break;
                if (c >= 0x00 && c <= 0x1f && c != 0x0a && c != 0x0d && c != 0x09 || c == 0x7F) {
                    i2++;
                    i--;
                    continue;
                }
                b[i] = (byte) c;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        byte[] raw = Arrays.copyOf(b, i);
        result.rawBytes = raw;
        result.str = decodeBytes(raw, 0, raw.length);
        return result;
    }

    static resultStr readTextToZero(DataInputStream dis, int maxLen) {
        resultStr result = new resultStr();
        if (maxLen < 0) {
            result.str = "";
            result.length_in_src = 0;
            return result;
        }
        byte[] b = new byte[maxLen];
        result.length_in_src = 0;

        int i = 0;
        try {
            for (; i < maxLen; i++) {
                int c = dis.read();
                HCStackDebug.read(c);
                result.length_in_src++;
                if (c <= 0) break;
                b[i] = (byte) c;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        byte[] raw = Arrays.copyOf(b, i);
        result.rawBytes = raw;
        result.str = decodeBytes(raw, 0, raw.length);
        return result;
    }

    static String decodeBytes(byte[] b, int offset, int len) {
        if (b == null || len <= 0) return "";
        String enc = (PCARD.pc != null && PCARD.pc.textEncoding != null) ? PCARD.pc.textEncoding : "Auto";
        String charsetName = null;
        if (!enc.equalsIgnoreCase("Auto")) {
            if (enc.equalsIgnoreCase("Shift_JIS") || enc.equalsIgnoreCase("SJIS")) {
                charsetName = "MS932";
            } else {
                charsetName = enc;
            }
        } else {
            int sjisMultiByteCount = 0;
            int isolatedKatakanaCount = 0;
            int invalidSjisCount = 0;
            for (int idx = offset; idx < offset + len; idx++) {
                int c = b[idx] & 0xFF;
                if (c < 0x80) continue;
                if ((c >= 0x81 && c <= 0x9F) || (c >= 0xE0 && c <= 0xEF)) {
                    if (idx + 1 < offset + len) {
                        int c2 = b[idx + 1] & 0xFF;
                        if ((c2 >= 0x40 && c2 <= 0x7E) || (c2 >= 0x80 && c2 <= 0xFC)) {
                            sjisMultiByteCount++;
                            idx++;
                        } else {
                            invalidSjisCount++;
                        }
                    } else {
                        invalidSjisCount++;
                    }
                } else if (c >= 0xA1 && c <= 0xDF) {
                    isolatedKatakanaCount++;
                } else {
                    invalidSjisCount++;
                }
            }
            if (invalidSjisCount > 0 || (sjisMultiByteCount == 0 && isolatedKatakanaCount > 0)) {
                charsetName = "MacRoman";
            } else {
                charsetName = "MS932";
            }
        }
        String str;
        try {
            str = new String(b, offset, len, Charset.forName(charsetName));
        } catch (Exception e) {
            str = new String(b, offset, len, Charset.forName("MS932"));
        }
        // Change line ending
        return str.replace('\r', '\n');
    }

    @SuppressWarnings("unchecked")
    private static void redecodeObject(OObject obj) {
        if (obj == null) return;
        if (obj.rawNameBytes != null) {
            obj.name = decodeBytes(obj.rawNameBytes, 0, obj.rawNameBytes.length);
        }
        if (obj.rawTextBytes != null) {
            obj.setTextInternal(decodeBytes(obj.rawTextBytes, 0, obj.rawTextBytes.length));
        }
        if (obj.rawScriptBytes != null && obj.scriptList != null) {
            String scriptStr = decodeBytes(obj.rawScriptBytes, 0, obj.rawScriptBytes.length);
            obj.scriptList.clear();
            Collections.addAll(obj.scriptList, scriptStr.split("\n"));
            obj.stringList = new ArrayList[obj.scriptList.size()];
            obj.typeList = new ArrayList[obj.scriptList.size()];
            obj.handlerList = null;
            obj.handlerLineList = null;
        }
    }

    public static void redecodeStack(OStack stack) {
        if (stack == null) return;
        if (stack.fontList != null) {
            for (OStack.fontClass fc : stack.fontList) {
                if (fc.rawNameBytes != null) {
                    fc.name = decodeBytes(fc.rawNameBytes, 0, fc.rawNameBytes.length);
                }
            }
        }
        redecodeObject(stack);
        if (stack.bgCacheList != null) {
            for (OBackground bg : stack.bgCacheList) {
                redecodeObject(bg);
                if (bg.partsList != null) {
                    for (OObject part : bg.partsList) {
                        redecodeObject(part);
                    }
                }
            }
        }
        if (stack.cdCacheList != null) {
            for (OCard cd : stack.cdCacheList) {
                redecodeObject(cd);
                if (cd.partsList != null) {
                    for (OObject part : cd.partsList) {
                        redecodeObject(part);
                    }
                }
                if (cd.bgfldList != null) {
                    for (OBgFieldData bgfld : cd.bgfldList) {
                        if (bgfld.rawTextBytes != null) {
                            bgfld.text = decodeBytes(bgfld.rawTextBytes, 0, bgfld.rawTextBytes.length);
                        }
                    }
                }
            }
        }
        OCard.reloadCurrentCard();
    }

    static String[] readPatterns(DataInputStream dis, String parentPath) {
        String[] patAry = new String[40];

        for (int i = 0; i < 40; i++) {
            BufferedImage bi = new BufferedImage(8, 8, BufferedImage.TYPE_INT_ARGB);
            DataBuffer db = bi.getRaster().getDataBuffer();
            try {
                for (int y = 0; y < 8; y++) {
                    int c = dis.read();
                    HCStackDebug.read(c);
                    for (int x = 0; x < 8; x++) {
                        if (((c >> (7 - x)) & 0x01) == 0) {
                            db.setElem(y * 8 + x, 0xFFFFFFFF);
                        } else {
                            db.setElem(y * 8 + x, 0xFF000000);
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            // Convert to PNG format and save to file
            String filename = "PAT_" + (i + 1) + ".png";
            File ofile = new File(parentPath + File.separatorChar + filename);
            try {
                ImageIO.write(bi, "png", ofile);
            } catch (IOException e) {
                e.printStackTrace();
            }
            patAry[i] = filename;
        }
        return patAry;
    }


    static ArrayList<Thread> threadList = new ArrayList<>();

    public static boolean readPictureBlock(DataInputStream dis, int blockSize, OStack stack) {
        // System.out.println("readPictureBlock");

        if (blockSize > 200000 || blockSize < 0) {
            return false;
        }

        // Read block data sequentially
        int bitmapId = HCData.readCode(dis, 4);
        // HCStackDebug.debuginfo("bitmapId:"+Integer.toString(bitmapId));
        // System.out.println("bitmapId:"+bitmapId);
        /*String filler =*/
        HCData.readStr(dis, 12);
        // System.out.println("filler:"+filler);

        int top = HCData.readCode(dis, 2);
        // HCStackDebug.debuginfo("top:"+Integer.toString(top));
        int left = HCData.readCode(dis, 2);
        // HCStackDebug.debuginfo("left:"+Integer.toString(left));
        int bottom = HCData.readCode(dis, 2);
        // HCStackDebug.debuginfo("bottom:"+Integer.toString(bottom));
        int right = HCData.readCode(dis, 2);
        // HCStackDebug.debuginfo("right:"+Integer.toString(right));

        int maskTop = HCData.readCode(dis, 2);
        // HCStackDebug.debuginfo("maskTop:"+Integer.toString(maskTop));
        int maskLeft = HCData.readCode(dis, 2);
        // HCStackDebug.debuginfo("maskLeft:"+Integer.toString(maskLeft));
        int maskBottom = HCData.readCode(dis, 2);
        // HCStackDebug.debuginfo("maskBottom:"+Integer.toString(maskBottom));
        int maskRight = HCData.readCode(dis, 2);
        // HCStackDebug.debuginfo("maskRight:"+Integer.toString(maskRight));

        int imgTop = HCData.readCode(dis, 2);
        // HCStackDebug.debuginfo("imgTop:"+Integer.toString(imgTop));
        int imgLeft = HCData.readCode(dis, 2);
        // HCStackDebug.debuginfo("imgLeft:"+Integer.toString(imgLeft));
        int imgBottom = HCData.readCode(dis, 2);
        // HCStackDebug.debuginfo("imgBottom:"+Integer.toString(imgBottom));
        int imgRight = HCData.readCode(dis, 2);
        // HCStackDebug.debuginfo("imgRight:"+Integer.toString(imgRight));

        /*String filler2 =*/
        HCData.readStr(dis, 8);
        // System.out.println("filler2:"+filler2);

        int maskSize = HCData.readCode(dis, 4);
        // HCStackDebug.debuginfo("maskSize:"+Integer.toString(maskSize));
        int imgSize = HCData.readCode(dis, 4);
        // HCStackDebug.debuginfo("imgSize:"+Integer.toString(imgSize));

        if (blockSize < 64 + maskSize + imgSize) {
            // System.out.println("!:");
            return false;
        }
        if (maskSize < 0 || imgSize < 0) {
            // System.out.println("!:");
            return false;
        }

        // Load mask into byte array
        byte[] mask = null;
        if (maskSize > 0) {
            mask = new byte[maskSize];
            try {
                // dis.read(mask);
                for (int i = 0; i < maskSize; i++) {
                    int v = dis.read();
                    HCStackDebug.read(v);
                    mask[i] = (byte) v;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Load image into byte array
        byte[] img = new byte[imgSize];
        try {
            // dis.read(img);
            for (int i = 0; i < imgSize; i++) {
                int v = dis.read();
                HCStackDebug.read(v);
                img[i] = (byte) v;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Load image in separate thread at the end
        Thread p = new saveThread(mask, img, bitmapId,
                top, left, right, bottom,
                maskLeft, maskTop, maskRight, maskBottom,
                imgLeft, imgTop, imgRight, imgBottom, stack);
        threadList.add(p);
        if (stack.GetCardbyId(stack.firstCard) != null &&
                stack.GetCardbyId(stack.firstCard).bitmapName != null &&
                stack.GetCardbyId(stack.firstCard).bitmapName.equals("BMAP_" + bitmapId + ".png")) {
            p.start();
        } else if (stack.GetBackgroundbyId(stack.firstBg) != null &&
                stack.GetBackgroundbyId(stack.firstBg).bitmapName != null &&
                stack.GetBackgroundbyId(stack.firstBg).bitmapName.equals("BMAP_" + bitmapId + ".png")) {
            p.start();
        }


        // Alignment adjustment
        int remainLength = blockSize - (64 + maskSize + imgSize);
        HCStackDebug.debuginfo("remainLength:" + remainLength);
        // System.out.println("remainLength:"+remainLength);
        /*String padding =*/
        HCData.readStr(dis, remainLength);
        // System.out.println("padding:"+padding);

        return true;
    }


    private static class convertPictureThread extends Thread {

        @Override
        public void run() {
            if (threadList != null) {
                Thread lastp = null;
                Thread lastp2 = null;
                for (Thread p : threadList) {
                    if (p != null && p.getState() == State.NEW) {
                        try {
                            p.start();
                        } catch (IllegalThreadStateException e) {
                            // already started
                        }
                    }
                    // Up to 3 parallel threads
                    while (lastp2 != null && lastp2.isAlive()) {
                        try {
                            Thread.sleep(20);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    lastp2 = lastp;
                    lastp = p;
                }
            }
        }
    }

    private static class saveThread extends Thread {

        byte[] mask;
        byte[] img;
        int bitmapId;
        int top;
        int left;
        int right;
        int bottom;
        int maskLeft;
        int maskTop;
        int maskRight;
        int maskBottom;
        int imgLeft;
        int imgTop;
        int imgRight;
        int imgBottom;
        OStack stack;

        saveThread(byte[] mask, byte[] img, int bitmapId,
                   int top, int left, int right, int bottom,
                   int maskLeft, int maskTop, int maskRight, int maskBottom,
                   int imgLeft, int imgTop, int imgRight, int imgBottom, OStack stack) {
            this.mask = mask;
            this.img = img;
            this.bitmapId = bitmapId;
            this.top = top;
            this.left = left;
            this.right = right;
            this.bottom = bottom;
            this.maskLeft = maskLeft;
            this.maskTop = maskTop;
            this.maskRight = maskRight;
            this.maskBottom = maskBottom;
            this.imgLeft = imgLeft;
            this.imgTop = imgTop;
            this.imgRight = imgRight;
            this.imgBottom = imgBottom;
            this.stack = stack;

            setName("BMAP_" + bitmapId + ".png");
        }

        @Override
        public void run() {
            // Is it a bitmapID registered in bg? (May be missed if stack data isn't fully read yet)
            boolean isBgPicture = false;
            for (int i = 0; stack.bgCacheList != null && i < stack.bgCacheList.size(); i++) {
                if (stack.bgCacheList.get(i).bitmapName != null && stack.bgCacheList.get(i).bitmapName.equals("BMAP_" + bitmapId + ".png")) {
                    isBgPicture = true;
                    break;
                }
            }

            BufferedImage maskBi = null;
            if (mask != null) {
                maskBi = readWOBA(mask, bitmapId, right, bottom, maskLeft, maskTop, maskRight, maskBottom, false);
            }
            BufferedImage mainBi = readWOBA(img, bitmapId, right, bottom, imgLeft, imgTop, imgRight, imgBottom, isBgPicture);

            if (isBgPicture) {
                Graphics2D g = mainBi.createGraphics();
                g.setColor(Color.WHITE);
                g.fillRect(0, 0, imgLeft, bottom);
                g.fillRect(imgRight, 0, right, bottom);
                g.fillRect(0, 0, right, imgTop);
                g.fillRect(0, imgBottom, right, bottom);
            } else {
                // Composite into image with alpha channel
                DataBuffer maindb = mainBi.getRaster().getDataBuffer();
                DataBuffer maskdb = (maskBi != null) ? maskBi.getRaster().getDataBuffer() : null;
                for (int y = top; y < bottom; y++) {
                    for (int x = left; x < right; x++) {
                        boolean inImg = (x >= imgLeft && x < imgRight && y >= imgTop && y < imgBottom);
                        boolean imgBlack = inImg && ((0x00FFFFFF & maindb.getElem(x + y * right)) == 0);
                        if (imgBlack) {
                            maindb.setElem(x + y * right, 0xFF000000);
                        } else {
                            boolean inMask = (x >= maskLeft && x < maskRight && y >= maskTop && y < maskBottom);
                            boolean maskOpaque = inMask && (maskdb == null || ((0x00FFFFFF & maskdb.getElem(x + y * right)) == 0));
                            if (maskOpaque) {
                                maindb.setElem(x + y * right, 0xFFFFFFFF);
                            } else {
                                maindb.setElem(x + y * right, 0x00FFFFFF);
                            }
                        }
                    }
                }
            }

            // Save to file (this takes time)
            String filename = "BMAP_" + bitmapId + ".png";
            Path file = PCARDFrame.pc.stack.file.getParent().resolve(filename);
            try {
                ImageIO.write(mainBi, "png", file.toFile());
            } catch (IOException e) {
                e.printStackTrace();
            }

            for (int i = 0; i < threadList.size(); i++) {
                Thread p = threadList.get(i);
                if (p == this) {
                    threadList.set(i, null);
                    break;
                }
            }
        }


        // Read HyperCard picture format (Wrath Of Bill Atkinson)
        private static BufferedImage readWOBA(byte[] img, int id, int cdWidth, int cdHeight,
                                              int left, int top, int right, int bottom, boolean isBgPicture) {
            BufferedImage bi = new BufferedImage(cdWidth, cdHeight, BufferedImage.TYPE_INT_ARGB);
            if (isBgPicture) {
                Graphics2D g = bi.createGraphics();
                g.setColor(Color.WHITE);
                g.fillRect(0, 0, cdWidth, cdHeight);
            } else {
                Graphics2D g = bi.createGraphics();
                g.setComposite(AlphaComposite.getInstance(AlphaComposite.CLEAR, 0.0f));
                g.fillRect(0, 0, cdWidth, cdHeight);
            }


            DataBuffer db = bi.getRaster().getDataBuffer();
            left = left / 32 * 32;
            right = (right + 31) / 32 * 32;
            // String debugStr = "";

            byte[] keepArray = new byte[] {(byte) 0xAA, 0x55, (byte) 0xAA, 0x55, (byte) 0xAA, 0x55, (byte) 0xAA, 0x55};
            int dh = 0;
            int dv = 0;
            int repeatInstructionCount = 0;
            int repeatInstructionIndex = 0;

            int i = 0;
            for (int y = top; y < bottom; y++) {
                // debugStr += "\n"+"line "+y;// ####

                int opcode = 0;
                int x = left;
                for (; i < img.length && x < right; /*i++*/) {
                    if (repeatInstructionCount > 0) {
                        i = repeatInstructionIndex;
                        repeatInstructionCount--;
                        // debugStr += "rep ";// ####
                    }
                    opcode = (0x00FF & img[i]);
                    i++;
                    // System.out.println("opcode("+i+")="+Integer.toHexString(opcode));
                    // debugStr += "["+Integer.toHexString(opcode)+"]";// ####

                    if (opcode <= 0x7F) {
                        int dataBytes = opcode >> 4;
                        int zeroBytes = opcode & 0x0F;

                        for (int j = 0; j < zeroBytes; j++) {
                            for (int k = 0; k < 8 && x < right; k++) {
                                db.setElem(x + y * cdWidth, 0xFFFFFFFF);
                                x++;
                            }
                        }
                        for (int j = 0; j < dataBytes && i < img.length; j++) {
                            for (int k = 0; k < 8; k++) {
                                db.setElem(x + y * cdWidth, (0x01 & (img[i] >> (7 - k))) != 0 ? 0xFF000000 : 0xFFFFFFFF);
                                x++;
                            }
                            // debugStr += Integer.toHexString((int)(0x00ff&img[i]))+" ";// ####
                            i++;
                        }
                    } else if (opcode >= 0x80 && opcode <= 0x8F) {
                        switch (opcode) {
                            case 0x80: // 1 row uncompressed
                                while (x < right && i < img.length) {
                                    for (int k = 0; k < 8; k++) {
                                        db.setElem(x + y * cdWidth, (0x01 & (img[i] >> (7 - k))) != 0 ? 0xFF000000 : 0xFFFFFFFF);
                                        x++;
                                    }
                                    // debugStr += Integer.toHexString((int)(0x00FF&img[i]))+" ";// ####
                                    i++;
                                }
                                break;
                            case 0x81: // 1 row white
                                while (x < right) {
                                    db.setElem(x + y * cdWidth, 0xFFFFFFFF);
                                    x++;
                                }
                                break;
                            case 0x82: // 1 row black
                                while (x < right) {
                                    db.setElem(x + y * cdWidth, 0xFF000000);
                                    x++;
                                }
                                break;
                            case 0x83: // 1 row same byte
                                while (x < right) {
                                    for (int k = 0; k < 8; k++) {
                                        db.setElem(x + y * cdWidth, (0x01 & (img[i] >> (7 - k))) != 0 ? 0xFF000000 : 0xFFFFFFFF);
                                        x++;
                                    }
                                }
                                // debugStr += Integer.toHexString(img[i])+" ";// ####
                                keepArray[y % 8] = img[i];
                                i++;
                                break;
                            case 0x84: // 1 row same byte, use kept array data
                                while (x < right) {
                                    for (int k = 0; k < 8; k++) {
                                        db.setElem(x + y * cdWidth, (0x01 & (keepArray[y % 8] >> (7 - k))) != 0 ? 0xFF000000 : 0xFFFFFFFF);
                                        x++;
                                    }
                                }
                                // debugStr += "keep "+Integer.toHexString((int)(0x00FF&keepArray[y%8]))+" ";// ####
                                break;
                            case 0x85: // Copy 1 row above
                                while (x < right) {
                                    for (int k = 0; k < 8; k++) {
                                        db.setElem(x + y * cdWidth, db.getElem(0, x + (y - 1) * cdWidth));
                                        x++;
                                    }
                                }
                                break;
                            case 0x86: // Copy 2 rows above
                                while (x < right) {
                                    for (int k = 0; k < 8; k++) {
                                        db.setElem(x + y * cdWidth, db.getElem(0, x + (y - 2) * cdWidth));
                                        x++;
                                    }
                                }
                                break;
                            case 0x87: // Copy 3 rows above
                                while (x < right) {
                                    for (int k = 0; k < 8; k++) {
                                        db.setElem(x + y * cdWidth, db.getElem(0, x + (y - 3) * cdWidth));
                                        x++;
                                    }
                                }
                                break;
                            case 0x88:
                                dh = 16;
                                dv = 0; // XOR after 16-bit right shift
                                break;
                            case 0x89:
                                dh = 0;
                                dv = 0; //
                                break;
                            case 0x8A:
                                dh = 0;
                                dv = 1; // XOR with 1 row above
                                break;
                            case 0x8B:
                                dh = 0;
                                dv = 2; // XOR with 2 rows above
                                break;
                            case 0x8C:
                                dh = 1;
                                dv = 0; // XOR after 1-bit right shift
                                break;
                            case 0x8D:
                                dh = 1;
                                dv = 1; // 1-bit right shift, XOR with 1 row above
                                break;
                            case 0x8E:
                                dh = 2;
                                dv = 2; // 2-bit right shift, XOR with 2 rows above
                                break;
                            case 0x8F:
                                dh = 8;
                                dv = 0; // XOR after 8-bit right shift
                                break;

                            default:
                                // System.out.println("!");
                                break;
                        }
                    } else if (opcode >= 0xA0 && opcode <= 0xBF) {
                        // Repeat next byte's opcode for lower 5 bits
                        repeatInstructionCount = (0x1F & opcode);
                        repeatInstructionIndex = i;
                    } else if (opcode >= 0xC0 && opcode <= 0xDF) {
                        // Data for lower 5 bits * 8
                        int count = (0x1F & opcode) * 8;
                        while (count > 0 && x < cdWidth && i < img.length) {
                            for (int k = 0; k < 8; k++) {
                                db.setElem(x + y * cdWidth, (0x01 & (img[i] >> (7 - k))) != 0 ? 0xFF000000 : 0xFFFFFFFF);
                                x++;
                            }
                            count--;
                            // debugStr += Integer.toHexString((int)(0x00FF&img[i]))+" ";// ####
                            i++;
                        }
                    } else if (opcode >= 0xE0 && opcode <= 0xFF) {
                        // Zeros for lower 5 bits * 16
                        int count = (0x1F & opcode) * 16;
                        while (count > 0 && x < cdWidth) {
                            for (int k = 0; k < 8; k++) {
                                db.setElem(x + y * cdWidth, 0xFFFFFFFF);
                                x++;
                            }
                            count--;
                        }
                    }
                }

                if (opcode >= 0x80 && opcode <= 0x87) {
                    // Don't execute dh, dv when rewriting 1 row
                    continue;
                }

                //
                // debugStr += " dh="+dh+" dv="+dv;// ####
                if (y < bottom) {
                    if (dh > 0) {
                        x = left + dh;
                        while (x < right) {
                            // int v = 0xFF000000|0x00FFFFFF&(db.getElem(0, x+y*cdWidth)^db.getElem(0, (x-dh)+y*cdWidth));
                            int a1 = db.getElem(0, x + y * cdWidth);
                            int a2 = db.getElem(0, (x - dh) + y * cdWidth);
                            int a3 = a1 ^ a2;
                            int a4 = 0x00FFFFFF & a3;
                            int a5 = 0xFF000000 | a4;
                            int v = a5;
                            v = v ^ (0x00FFFFFF);
                            db.setElem(x + y * cdWidth, v);
                            x++;
                        }
                    }

                    x = left;
                    if (dv > 0) {
                        while (x < right && y >= dv) {
                            db.setElem(x + y * cdWidth, 0x00FFFFFF ^ (0xFF000000 | 0x00FFFFFF & (db.getElem(0, x + y * cdWidth) ^ db.getElem(0, x + (y - dv) * cdWidth))));
                            x++;
                        }
                    }
                }

                // go next row
            }

            /*File f = new File("./woba_debug_"+id+".txt");
            try {
                FileOutputStream stream = new FileOutputStream(f);
                stream.write(debugStr.getBytes());
                stream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }*/

            return bi;
        }
    }
}


class resultStr {

    String str;
    int length_in_src;
    byte[] rawBytes;
}


class HCStackDebug {

    static StringBuilder allStr = new StringBuilder(10000);
    static String hexstr;
    static StringBuilder debugStr = new StringBuilder(100);
    static int all_counter;
    static int block_counter;

    public static void read(int code) {
        /*if(block_counter%16==0){
            allStr.append("\n");
            allStr.append(Integer.toHexString(all_counter));
            allStr.append(" ");
            allStr.append(Integer.toHexString(block_counter));
            allStr.append(" ");
            allStr.append(hexstr);
            hexstr = "";
            allStr.append(" ");
            allStr.append(debugStr);
            debugStr = new StringBuilder(100);
        }
        hexstr += " "+Integer.toHexString(code);
        all_counter+=1;
        block_counter+=1;*/
    }

    public static void debuginfo(String str) {
        /*debugStr.append(" ");
        debugStr.append(str);*/
    }

    public static void blockstart(String str) {
        /*block_counter = 0;
        debugStr.append("\n\nBLOCK<<");
        debugStr.append(" "+str);
        debugStr.append(">>");*/
    }
}