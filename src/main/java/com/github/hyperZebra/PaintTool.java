package com.github.hyperZebra;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.MouseInfo;
import java.awt.Paint;
import java.awt.Point;
import java.awt.PointerInfo;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.TexturePaint;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import javax.imageio.ImageIO;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;


public class PaintTool {

    static boolean mouse;
    static float[] lastx = new float[2];
    static float[] lasty = new float[2];
    static long lastTime;
    static boolean canTablet = false;
    static PCARDFrame owner;

    // Paint properties
    static int brushSize = 3; // TODO: Make non-static
    static float lineSize = 1;
    static int alpha = 100;
    static boolean antialias = false;
    static boolean editBackground = false;
    static BufferedImage pat;
    boolean fill;
    static int smartSelectPercent = 5; // 5%
    // static boolean bit = false; // Zoomed display
    // static float bitLeft = 0;
    // static float bitTop = 0;

    public static void tabletinit() {
         /*if (JTabletExtension.checkCompatibility(PaintTool.owner, "1.2.0")) {
             canTablet = true;
             if(tabletListener==null){
                 tabletListener = new MyTabletListener();
             }
         }*/

        if (!PCARD.pc.paidle.isAlive()) {
            PCARD.pc.paidle = new PaintIdle();
            PCARD.pc.paidle.start();
        }
    }

    public static void mouseUp(int x, int y) {
        if (owner.bit > 1) {
            // Truncate decimals
            PaintTool.owner.bitLeft = (float) (int) PaintTool.owner.bitLeft;
            PaintTool.owner.bitTop = (float) (int) PaintTool.owner.bitTop;

            if (owner.getClass() == IconEditor.class) {
                // Use scrollbar
                ((IconEditor) owner).scrollpane.getHorizontalScrollBar().setValue(((IconEditor) owner).scrollpane.getHorizontalScrollBar().getValue() + (int) PaintTool.owner.bitLeft * PaintTool.owner.bit);
                ((IconEditor) owner).scrollpane.getVerticalScrollBar().setValue(((IconEditor) owner).scrollpane.getVerticalScrollBar().getValue() + (int) PaintTool.owner.bitTop * PaintTool.owner.bit);
                PaintTool.owner.bitLeft = 0;
                PaintTool.owner.bitTop = 0;
            }

            if (owner.tool.getClass() == EraserTool.class || owner.tool.getClass() == SelectTool.class) {
                x = (int) (((float) x + (float) owner.bit / 2) / owner.bit) + (int) owner.bitLeft;
                y = (int) (((float) y + (float) owner.bit / 2) / owner.bit) + (int) owner.bitTop;
            } else {
                x = x / owner.bit + (int) owner.bitLeft;
                y = y / owner.bit + (int) owner.bitTop;
            }

        }
        if (GUI.key[' '] > 0) { // space
            return;
        }
        if (owner.tool != null) {
            owner.tool.mouseUp(x, y);
            lastx[1] = lastx[0];
            lasty[1] = lasty[0];
            lastx[0] = x;
            lasty[0] = y;
            mouse = false;
        }
    }

    public static void mouseDown(int x, int y) {
        /*if(GUI.key[12]>0 && owner.bit>1 && owner.tool!=null&&owner.tool.getClass() == PencilTool.class) {
            lastX[0] = x;
            lastY[0] = y;
            return;
        }*/
        if (GUI.key[' '] > 0) { // space
            lastx[0] = x;
            lasty[0] = y;
            return;
        }
        if (owner.bit > 1) {
            if (owner.tool.getClass() == EraserTool.class || owner.tool.getClass() == SelectTool.class) {
                x = (int) (((float) x + (float) owner.bit / 2) / owner.bit) + (int) owner.bitLeft;
                y = (int) (((float) y + (float) owner.bit / 2) / owner.bit) + (int) owner.bitTop;
            } else {
                x = x / owner.bit + (int) owner.bitLeft;
                y = y / owner.bit + (int) owner.bitTop;
            }
        }
        if (owner.tool != null) {
            owner.tool.mouseDown(x, y);
            lastx[1] = lastx[0];
            lasty[1] = lasty[0];
            lastx[0] = x;
            lasty[0] = y;
            mouse = true;
        }
    }

    public static void mouseWithin(int x, int y) {
        /*if(GUI.key[12]>0 && owner.bit>1 && owner.tool!=null&&owner.tool.getClass() == PencilTool.class) {
            return;
        }*/
        if (GUI.key[' '] > 0) { // space
            if (GUI.key[' '] > 0) {
                PaintTool.owner.mainPane.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            }
            return;
        }
        if (owner.bit > 1) {
            x = x / owner.bit + (int) owner.bitLeft;
            y = y / owner.bit + (int) owner.bitTop;
        }
        if (owner.tool != null && owner.tool.mouseWithin(x, y)) {
            lastx[1] = lastx[0];
            lasty[1] = lasty[0];
            lastx[0] = x;
            lasty[0] = y;
        }
    }

    public static void mouseStillDown(int x, int y) {
        // if(GUI.key[12]>0 && owner.bit>1 && owner.tool!=null&&owner.tool.getClass() == PencilTool.class) {
        if (GUI.key[' '] > 0 && owner.bit > 1 && owner.tool != null) {
            PaintTool.owner.bitLeft += (PaintTool.lastx[0] - x) / PaintTool.owner.bit;
            PaintTool.owner.bitTop += (PaintTool.lasty[0] - y) / PaintTool.owner.bit;
            lastx[0] = x;
            lasty[0] = y;

            if (owner.getClass() == IconEditor.class) {
                // Use scrollbar
                if (PaintTool.owner.bitLeft + (float) ((IconEditor) owner).scrollpane.getHorizontalScrollBar().getValue() / owner.bit < 0) {
                    PaintTool.owner.bitLeft = (float) -((IconEditor) owner).scrollpane.getHorizontalScrollBar().getValue() / owner.bit;
                }
                if (PaintTool.owner.bitTop + (float) ((IconEditor) owner).scrollpane.getVerticalScrollBar().getValue() / owner.bit < 0) {
                    PaintTool.owner.bitTop = (float) -((IconEditor) owner).scrollpane.getVerticalScrollBar().getValue() / owner.bit;
                }
                if (PaintTool.owner.bitLeft + (float) PaintTool.owner.redoBuf.getWidth() / PaintTool.owner.bit > PaintTool.owner.redoBuf.getWidth() - (float) ((IconEditor) owner).scrollpane.getHorizontalScrollBar().getValue() / owner.bit) {
                    PaintTool.owner.bitLeft = (float) (PaintTool.owner.redoBuf.getWidth() * (PaintTool.owner.bit - 1)) / PaintTool.owner.bit - (float) ((IconEditor) owner).scrollpane.getHorizontalScrollBar().getValue() / owner.bit;
                }
                if (PaintTool.owner.bitTop + (float) PaintTool.owner.redoBuf.getHeight() / PaintTool.owner.bit > PaintTool.owner.redoBuf.getHeight() - (float) ((IconEditor) owner).scrollpane.getVerticalScrollBar().getValue() / owner.bit) {
                    PaintTool.owner.bitTop = (float) (PaintTool.owner.redoBuf.getHeight() * (PaintTool.owner.bit - 1)) / PaintTool.owner.bit - (float) ((IconEditor) owner).scrollpane.getVerticalScrollBar().getValue() / owner.bit;
                }
            } else {
                if (PaintTool.owner.bitLeft < 0) PaintTool.owner.bitLeft = 0;
                if (PaintTool.owner.bitTop < 0) PaintTool.owner.bitTop = 0;
                if (PaintTool.owner.bitLeft + (float) PaintTool.owner.redoBuf.getWidth() / PaintTool.owner.bit > PaintTool.owner.redoBuf.getWidth()) {
                    PaintTool.owner.bitLeft = (float) (PaintTool.owner.redoBuf.getWidth() * (PaintTool.owner.bit - 1)) / PaintTool.owner.bit;
                }
                if (PaintTool.owner.bitTop + (float) PaintTool.owner.redoBuf.getHeight() / PaintTool.owner.bit > PaintTool.owner.redoBuf.getHeight()) {
                    PaintTool.owner.bitTop = (float) (PaintTool.owner.redoBuf.getHeight() * (PaintTool.owner.bit - 1)) / PaintTool.owner.bit;
                }
            }

            PaintTool.owner.mainPane.repaint();
            return;
        }
        if (owner.bit > 1) {
            if (owner.tool.getClass() == EraserTool.class || owner.tool.getClass() == SelectTool.class) {
                x = (int) (((float) x + (float) owner.bit / 2) / owner.bit) + (int) owner.bitLeft;
                y = (int) (((float) y + (float) owner.bit / 2) / owner.bit) + (int) owner.bitTop;
            } else {
                x = x / owner.bit + (int) owner.bitLeft;
                y = y / owner.bit + (int) owner.bitTop;
            }
        }
        if (owner.tool != null && owner.tool.mouseStillDown(x, y)) {
            lastx[1] = lastx[0];
            lasty[1] = lasty[0];
            lastx[0] = x;
            lasty[0] = y;
        }
    }

    static public void setPattern(boolean invert) {
        BufferedImage patorg = PaintTool.owner.pat.patterns[PaintTool.owner.pat.pattern];
        if (patorg == null) {
            // If not available
            PaintTool.pat = new BufferedImage(8, 8, BufferedImage.TYPE_INT_ARGB);
            Graphics g1 = PaintTool.pat.getGraphics();
            g1.setColor(PaintTool.owner.fore.color);
            g1.fillRect(0, 0, 8, 8);
            return;
        }
        PaintTool.pat = new BufferedImage(patorg.getWidth(), patorg.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics g1 = PaintTool.pat.getGraphics();
        g1.drawImage(patorg, 0, 0, null);
        DataBuffer db = PaintTool.pat.getRaster().getDataBuffer();
        for (int h = 0; h < patorg.getHeight(); h++) {
            for (int w = 0; w < patorg.getWidth(); w++) {
                int c = db.getElem(0, h * patorg.getWidth() + w);
                int r = (c >> 16) & 0xFF;
                int g = (c >> 8) & 0xFF;
                int b = (c >> 0) & 0xFF;
                Color foreColor = PaintTool.owner.fore.color;
                Color backColor = PaintTool.owner.back.color;
                if (invert) {
                    foreColor = PaintTool.owner.back.color;
                    backColor = PaintTool.owner.fore.color;
                }
                int fr = foreColor.getRed();
                int fg = foreColor.getGreen();
                int fb = foreColor.getBlue();
                int fa = foreColor.getAlpha();
                int br = backColor.getRed();
                int bg = backColor.getGreen();
                int bb = backColor.getBlue();
                int ba = backColor.getAlpha();
                db.setElem(h * patorg.getWidth() + w, (((r * ba + (0xFF - r) * fa) / 0xFF) << 24) + (((r * br + (0xFF - r) * fr) / 0xFF) << 16) + (((g * bg + (0xFF - g) * fg) / 0xFF) << 8) + ((b * bb + (0xFF - b) * fb) / 0xFF));
            }
        }
    }

    static public void toCdPict() {
        if (PaintTool.owner != PCARDFrame.pc) return;
        if (PCARD.pc.stack.curCard == null || PaintTool.owner.mainImg == null) return;

        // Determine whether Card picture is completely transparent
        DataBuffer db = PaintTool.owner.mainImg.getRaster().getDataBuffer();
        int width = PaintTool.owner.mainImg.getWidth();
        boolean isTransparent = true;
        for (int h = 0; h < PaintTool.owner.mainImg.getHeight(); h++) {
            for (int w = 0; w < width; w++) {
                int c = db.getElem(0, h * width + w);
                if ((c & 0xFF000000) != 0) {
                    isTransparent = false;
                    break;
                }
            }
        }

        // Card picture
        if (isTransparent) {
            PaintTool.owner.stack.curCard.pict = null;
            PaintTool.owner.stack.curCard.bitmapName = null;
        } else {
            if (PaintTool.owner.stack.curCard.pict == null) {
                PaintTool.owner.stack.curCard.pict = new BufferedImage(PaintTool.owner.mainImg.getWidth(), PaintTool.owner.mainImg.getHeight(), BufferedImage.TYPE_INT_ARGB);
            }
            if (PaintTool.owner.stack.curCard.pict.getWidth() != PaintTool.owner.stack.width ||
                    PaintTool.owner.stack.curCard.pict.getHeight() != PaintTool.owner.stack.height) {
                // Resize
                PaintTool.owner.stack.curCard.pict = new BufferedImage(PaintTool.owner.stack.width, PaintTool.owner.stack.height, BufferedImage.TYPE_INT_ARGB);
            }
            Graphics2D g = (Graphics2D) PaintTool.owner.stack.curCard.pict.getGraphics();
            g.setColor(new Color(255, 255, 255));
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.CLEAR, 0.0f));
            g.fillRect(0, 0, PaintTool.owner.stack.width, PaintTool.owner.stack.height);
            g = (Graphics2D) PaintTool.owner.stack.curCard.pict.getGraphics();
            g.drawImage(PaintTool.owner.mainImg, 0, 0, PaintTool.owner);
        }

        // Determine whether Bg picture is completely transparent
        DataBuffer bgdb = PaintTool.owner.bgImg.getRaster().getDataBuffer();
        int bgwidth = PaintTool.owner.bgImg.getWidth();
        boolean isWhite = true;
        for (int h = 0; h < PaintTool.owner.bgImg.getHeight(); h++) {
            for (int w = 0; w < bgwidth; w++) {
                int c = bgdb.getElem(0, h * bgwidth + w);
                if ((c & 0x00FFFFFF) != 0x00FFFFFF) {
                    isWhite = false;
                    break;
                }
            }
        }

        // Bg picture
        if (isWhite) {
            PaintTool.owner.stack.curCard.bg.pict = null;
            PaintTool.owner.stack.curCard.bg.bitmapName = null;
        } else {
            if (PaintTool.owner.stack.curCard.bg.pict == null) {
                PaintTool.owner.stack.curCard.bg.pict = new BufferedImage(PaintTool.owner.mainImg.getWidth(), PaintTool.owner.mainImg.getHeight(), BufferedImage.TYPE_INT_ARGB);
            }
            if (PaintTool.owner.stack.curCard.bg.pict.getWidth() != PaintTool.owner.stack.width ||
                    PaintTool.owner.stack.curCard.bg.pict.getHeight() != PaintTool.owner.stack.height) {
                // Resize
                PaintTool.owner.stack.curCard.bg.pict = new BufferedImage(PaintTool.owner.stack.width, PaintTool.owner.stack.height, BufferedImage.TYPE_INT_ARGB);
            }
            Graphics2D g2 = (Graphics2D) PaintTool.owner.stack.curCard.bg.pict.getGraphics();
            g2.setColor(new Color(255, 255, 255));
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.CLEAR, 0.0f));
            g2.fillRect(0, 0, PaintTool.owner.stack.width, PaintTool.owner.stack.height);
            g2 = (Graphics2D) PaintTool.owner.stack.curCard.bg.pict.getGraphics();
            g2.drawImage(PaintTool.owner.bgImg, 0, 0, PaintTool.owner);
        }
    }

    static public void saveCdPictures() {
        // Copy to card picture
        PaintTool.toCdPict();

        // Save file (card)
        if (PCARDFrame.pc.stack.curCard != null && PCARDFrame.pc.stack.curCard.pict != null) {
            if (PCARDFrame.pc.stack.curCard.bitmapName == null ||
                    PCARDFrame.pc.stack.curCard.bitmapName.isEmpty() ||
                    !PCARDFrame.pc.stack.curCard.bitmapName.matches(".*\\.png")) {
                PCARDFrame.pc.stack.curCard.bitmapName = "BMAP_" + PCARDFrame.pc.stack.curCard.id + ".png";
                PCARDFrame.pc.stack.curCard.changed = true;
            }
            Path file = PCARDFrame.pc.stack.file.getParent().resolve(PCARDFrame.pc.stack.curCard.bitmapName);
            try {
                ImageIO.write(PCARDFrame.pc.stack.curCard.pict, "PNG", file.toFile());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Save file (bg)
        if (PCARDFrame.pc.stack.curCard != null &&
                PCARDFrame.pc.stack.curCard.bg != null &&
                PCARDFrame.pc.stack.curCard.bg.pict != null) {
            if (PCARDFrame.pc.stack.curCard.bg.bitmapName == null ||
                    PCARDFrame.pc.stack.curCard.bg.bitmapName.isEmpty() ||
                    !PCARDFrame.pc.stack.curCard.bg.bitmapName.matches(".*\\.png")) {
                PCARDFrame.pc.stack.curCard.bg.bitmapName = "BMAP_" + PCARDFrame.pc.stack.curCard.bg.id + ".png";
                PCARDFrame.pc.stack.curCard.bg.changed = true;
            }
            Path file2 = PCARDFrame.pc.stack.file.getParent().resolve(PCARDFrame.pc.stack.curCard.bg.bitmapName);
            try {
                ImageIO.write(PCARDFrame.pc.stack.curCard.bg.pict, "PNG", file2.toFile());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    public static void spoit(int x, int y) {
        DataBuffer db = PaintTool.owner.getSurface().getRaster().getDataBuffer();
        int c = db.getElem(x + y * PaintTool.owner.getSurface().getWidth());
        Color col = new Color((c >> 16) & 0xFF, (c >> 8) & 0xFF, (c) & 0xFF);
        if (paintGUI.right) {
            PaintTool.owner.back.color = col;
            PaintTool.owner.back.makeIcon(col);
        } else {
            PaintTool.owner.fore.color = col;
            PaintTool.owner.fore.makeIcon(col);
        }
    }
}


interface ToolInterface {

    void mouseUp(int x, int y);

    void mouseDown(int x, int y);

    boolean mouseWithin(int x, int y);

    boolean mouseStillDown(int x, int y);

    void clear();

    void end();

    String getName();
}

interface toolTabletInterface extends ToolInterface {

    void penUp(float x, float y, float pressure, boolean eraser);

    void penDown(float x, float y, float pressure, boolean eraser);

    boolean penStillDown(float x, float y, float pressure, boolean eraser);
}

interface ToolSelectInterface extends ToolInterface {

    BufferedImage getSelectedSurface(PCARDFrame owner);

    Rectangle getSelectedRect();

    Rectangle getMoveRect();

    boolean isMove();
}

/**
 * Pencil
 */
class PencilTool implements ToolInterface {

    boolean invert;
    boolean shift;
    int shiftx, shifty;

    @Override
    public String getName() {
        return "Pencil";
    }

    @Override
    public void mouseUp(int x, int y) {
        PaintTool.toCdPict();
        PaintTool.owner.mainPane.repaint();

        invert = false;
    }

    @Override
    public void mouseDown(int x, int y) {
        if (GUI.key[12] > 0) {
            PaintTool.spoit(x, y);
            return;
        }

        shift = (GUI.key[11] > 0);
        shiftx = x;
        shifty = y;

        if (GUI.key[14] > 0) { // cmd
            if (PaintTool.owner.bit > 1) {
                PaintTool.owner.bit = 1;
            } else {
                if (PCARDFrame.pc == PaintTool.owner) {
                    PaintTool.owner.bit = 8;
                } else if (PaintTool.owner.mainImg.getWidth() < 128 && PaintTool.owner.mainImg.getHeight() < 128) {
                    PaintTool.owner.bit = 8;
                } else if (PaintTool.owner.mainImg.getWidth() < 320 && PaintTool.owner.mainImg.getHeight() < 320) {
                    PaintTool.owner.bit = 4;
                } else {
                    PaintTool.owner.bit = 4; // 2;
                }
            }
            if (PaintTool.owner.bit > 1 && PCARDFrame.pc == PaintTool.owner) {
                PaintTool.owner.bitLeft = x - (float) PaintTool.owner.redoBuf.getWidth() / 2 / PaintTool.owner.bit;
                if (PaintTool.owner.bitLeft < 0) PaintTool.owner.bitLeft = 0;
                if (PaintTool.owner.bitLeft + (float) PaintTool.owner.redoBuf.getWidth() / PaintTool.owner.bit > PaintTool.owner.redoBuf.getWidth()) {
                    PaintTool.owner.bitLeft = (float) (PaintTool.owner.redoBuf.getWidth() * (PaintTool.owner.bit - 1)) / PaintTool.owner.bit;
                }
                PaintTool.owner.bitTop = y - (float) PaintTool.owner.redoBuf.getHeight() / 2 / PaintTool.owner.bit;
                if (PaintTool.owner.bitTop < 0) PaintTool.owner.bitTop = 0;
                if (PaintTool.owner.bitTop + (float) PaintTool.owner.redoBuf.getHeight() / PaintTool.owner.bit > PaintTool.owner.redoBuf.getHeight()) {
                    PaintTool.owner.bitTop = (float) (PaintTool.owner.redoBuf.getHeight() * (PaintTool.owner.bit - 1)) / PaintTool.owner.bit;
                }
            }
            if (PCARDFrame.pc != PaintTool.owner) { // IconEditor
                //PaintTool.owner.mainPane.setBounds(0, 0, PaintTool.owner.mainImg.getWidth()*PaintTool.owner.bit,
                //        PaintTool.owner.mainImg.getHeight()*PaintTool.owner.bit);
                PaintTool.owner.mainPane.setPreferredSize(new Dimension(PaintTool.owner.mainImg.getWidth() * PaintTool.owner.bit,
                        PaintTool.owner.mainImg.getHeight() * PaintTool.owner.bit));

                JViewport vp = (JViewport) PaintTool.owner.mainPane.getParent();
                ((JScrollPane) vp.getParent()).setViewportView(PaintTool.owner.mainPane);

                Dimension size = PaintTool.owner.getSize();
                int sw = size.width - 160;
                int sh = size.height - PaintTool.owner.getInsets().top;
                int rate = PaintTool.owner.bit;
                if (sw > PaintTool.owner.mainImg.getWidth() * rate + 20)
                    sw = PaintTool.owner.mainImg.getWidth() * rate + 20;
                if (sh > PaintTool.owner.mainImg.getHeight() * rate + 20)
                    sh = PaintTool.owner.mainImg.getHeight() * rate + 20;

                vp.getParent().setBounds(((size.width - 160) - sw) / 2, ((size.height - PaintTool.owner.getInsets().top) - sh) / 2, sw, sh);
            }

            //PaintTool.owner.getRootPane().repaint();
            PaintTool.owner.mainPane.repaint();
            return;
        }

        GMenuPaint.setUndo();

        PaintTool.lastx[0] = x;
        PaintTool.lasty[0] = y;

        DataBuffer db = PaintTool.owner.getSurface().getRaster().getDataBuffer();
        int c = db.getElem(x + y * PaintTool.owner.getSurface().getWidth());
        Color col = PaintTool.owner.fore.color;
        int c2 = (col.getAlpha() << 24) + (col.getRed() << 16) + (col.getGreen() << 8) + (col.getBlue());

        if (c == c2 || c - 1 == c2) {
            invert = true;
        }

        mouseStillDown(x, y);
    }

    @Override
    public boolean mouseWithin(int x, int y) {
        if (GUI.key[12] > 0) {
            PaintTool.owner.mainPane.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
        } else {
            PaintTool.owner.mainPane.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        }
        return true;
    }

    @Override
    public boolean mouseStillDown(int x, int y) {
        if (GUI.key[12] > 0) { // opt spoit
            return true;
        }
        if (GUI.key[14] > 0) { // cmd zoom
            return true;
        }

        if (shift && ((x - shiftx != 0) || (y - shifty != 0))) {
            if ((shiftx == -1) || (shifty != -1) && (Math.abs(x - shiftx) > Math.abs(y - shifty))) {
                shiftx = -1;
                y = shifty;
                PaintTool.lasty[0] = shifty;
            } else if ((shifty == -1) || (shiftx != -1)) {
                shifty = -1;
                x = shiftx;
                PaintTool.lastx[0] = shiftx;
            }
        }

        // Get back buffer
        Graphics2D g2;
        g2 = (Graphics2D) PaintTool.owner.getSurface().getGraphics();

        // Draw line on back buffer
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        float brushSize = 1;
        g2.setStroke(new BasicStroke(brushSize, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        if (this.invert) {
            g2.setColor(PaintTool.owner.back.color);
        } else {
            g2.setColor(PaintTool.owner.fore.color);
        }
        Line2D.Double line = new Line2D.Double(PaintTool.lastx[0], PaintTool.lasty[0], x, y);
        g2.draw(line);

        // Calculate clip size
        int left = 0, right = 0, top = 0, bottom = 0;
        if (x < PaintTool.lastx[0]) {
            left = (int) (x - brushSize / 2 - 1);
            right = (int) (PaintTool.lastx[0] + brushSize / 2 + 1);
        } else {
            left = (int) (PaintTool.lastx[0] - brushSize / 2 - 1);
            right = (int) (x + brushSize / 2 + 1);
        }
        if (y < PaintTool.lasty[0]) {
            top = (int) (y - brushSize / 2 - 1);
            bottom = (int) (PaintTool.lasty[0] + brushSize / 2 + 1);
        } else {
            top = (int) (PaintTool.lasty[0] - brushSize / 2 - 1);
            bottom = (int) (y + brushSize / 2 + 1);
        }

        // Draw background screen
        Graphics2D g3;
        g3 = (Graphics2D) PaintTool.owner.mainPane.getGraphics();
        g3.setClip(new Rectangle(left, top, right - left, bottom - top));
        if (!PaintTool.editBackground) {
            //g3.setColor(new Color(255,128,128));
            //g3.fillRect(0,0,640,480);
            //g3.drawImage(PaintTool.owner.bgImg, 0, 0, PaintTool.owner.mainPane);
        }

        Graphics2D g4 = (Graphics2D) PaintTool.owner.mainPane.getGraphics();
        g4.setClip(new Rectangle(left, top, right - left, bottom - top));

        // Draw line segment screen
        // g4.drawImage(PaintTool.owner.getSurface(), 0, 0, PaintTool.owner.mainPane);
        if (PaintTool.owner.bit > 1)
            PaintTool.owner.mainPane.repaint(
                    (left - (int) PaintTool.owner.bitLeft) * PaintTool.owner.bit,
                    (top - (int) PaintTool.owner.bitTop) * PaintTool.owner.bit,
                    (right - left) * PaintTool.owner.bit, (bottom - top) * PaintTool.owner.bit);
        else
            PaintTool.owner.mainPane.repaint(left, top, right - left, bottom - top);

        return true;
    }

    @Override
    public void clear() {

    }

    @Override
    public void end() {
        // TabletManager.getDefaultManager().removeTabletListener(PaintTool.owner.mainPane, PaintTool.tabletListener);
    }
}

/**
 * Brush
 */
class BrushTool implements toolTabletInterface {

    Cursor cursor;
    boolean shift;
    float shiftX, shiftY;

    @Override
    public String getName() {
        return "Brush";
    }

    public BrushTool() {
        PaintTool.tabletinit();

        if (PaintTool.canTablet) {
            //TabletManager.getDefaultManager().addTabletListener(PaintTool.owner.mainPane, PaintTool.tabletListener);
            //PaintTool.tabletListener.tool = this;
        }
        //PaintTool.owner.bit = 1;
        //PaintTool.owner.mainPane.repaint();
    }

    @Override
    public void mouseUp(int x, int y) {
//        if (PaintTool.tabletListener.in_stroke) {
//            return;
//        }
        penUp(x, y, 0.5f, false);
    }

    @Override
    public void penUp(float x, float y, float pressure, boolean eraser) {
        if (PaintTool.alpha != 100 || eraser) {
            // Write to surface
            Graphics2D g = (Graphics2D) PaintTool.owner.getSurface().getGraphics();
            if (eraser) {
                DataBuffer srcdb = PaintTool.owner.redoBuf.getRaster().getDataBuffer();
                DataBuffer tgtdb = PaintTool.owner.getSurface().getRaster().getDataBuffer();
                int width = PaintTool.owner.getSurface().getWidth();
                for (int h = 0; h < PaintTool.owner.getSurface().getHeight(); h++) {
                    for (int w = 0; w < width; w++) {
                        int src = srcdb.getElem(0, h * width + w);
                        int tgt = tgtdb.getElem(0, h * width + w);
                        int c = tgt;
                        if ((src & 0xFF000000) != 0) {
                            int a = (c & 0xFF000000) >> 24;
                            a = (a * (0xFF - ((src & 0xFF000000) >> 24))) / 0xFF;
                            c = (a << 24) + tgt & 0x00FFFFFF;
                        }
                        tgtdb.setElem(0, h * width + w, c);
                    }
                }
            } else {
                g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, PaintTool.alpha / 100.0F));
                g.drawImage(PaintTool.owner.redoBuf, 0, 0, PaintTool.owner.mainPane);
            }
        }

        PaintTool.toCdPict();
        PaintTool.owner.mainPane.repaint();
    }

    @Override
    public void mouseDown(int x, int y) {
//        if (PaintTool.tabletListener.in_stroke) {
//            return;
//        }
        penDown(x, y, 0.501f, false);
    }

    @Override
    public void penDown(float x, float y, float pressure, boolean eraser) {
        if (GUI.key[12] > 0) {
            PaintTool.spoit((int) x, (int) y);
            return;
        }
        shift = (GUI.key[11] > 0);
        shiftX = x;
        shiftY = y;

        GMenuPaint.setUndo();

        PaintTool.setPattern(GUI.key[14] > 0);

        if (PaintTool.alpha != 100 || eraser) {
            Graphics2D g2 = (Graphics2D) PaintTool.owner.redoBuf.getGraphics();
            g2.setColor(new Color(255, 255, 255));
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.CLEAR, 0.0f));
            g2.fillRect(0, 0, PaintTool.owner.redoBuf.getWidth(), PaintTool.owner.redoBuf.getHeight());
        }

        PaintTool.lastx[0] = x;
        PaintTool.lasty[0] = y;
        penStillDown(x, y, pressure, eraser);
    }

    @Override
    public boolean mouseWithin(int x, int y) {
//        if (PaintTool.tabletListener.in_stroke) {
//            return false;
//        }
        return penWithin(x, y, 0.5f);
    }

    public static boolean penWithin(float x, float y, float pressure) {
        return true;
    }

    @Override
    public boolean mouseStillDown(int x, int y) {
//        if (PaintTool.tabletListener.in_stroke) {
//            return false;
//        }
        if (GUI.key[12] > 0) {
            PaintTool.owner.mainPane.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
        } else {
            PaintTool.owner.mainPane.setCursor(cursor);
        }
        return penStillDown(x, y, 0.5f, false);
    }

    @Override
    public boolean penStillDown(float x, float y, float pressure, boolean eraser) {
//        if (PaintTool.antialias && pressure == 0.5f && Math.pow(x - PaintTool.lastX[0], 2) + Math.pow(y - PaintTool.lastY[0], 2) < PaintTool.brushSize * 15) {
//            if (PaintTool.owner.bit == 1 && PaintTool.lastTime + 500 >= System.currentTimeMillis()) {
//                return false; // Don't draw short lines to make anti-aliasing look cleaner
//            }
//        }

        if (shift && ((x - shiftX != 0) || (y - shiftY != 0))) {
            if ((shiftX == -1) || (shiftY != -1) && (Math.abs(x - shiftX) > Math.abs(y - shiftY))) {
                shiftX = -1;
                y = shiftY;
                PaintTool.lasty[0] = (int) shiftY;
                PaintTool.lasty[1] = (int) shiftY;
            } else if ((shiftY == -1) || (shiftX != -1)) {
                shiftY = -1;
                x = shiftX;
                PaintTool.lastx[0] = (int) shiftX;
                PaintTool.lastx[1] = (int) shiftX;
            }
        }

        // Get back buffer
        Graphics2D g2;
        if (PaintTool.alpha == 100 && !eraser) {
            g2 = (Graphics2D) PaintTool.owner.getSurface().getGraphics();
        } else {
            g2 = (Graphics2D) PaintTool.owner.redoBuf.getGraphics();
        }

        // Draw line on back buffer
        if (PaintTool.antialias) {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        } else {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        }
        float brushSize = PaintTool.brushSize * pressure * 2.0f;
        g2.setStroke(new BasicStroke(brushSize, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        if (GUI.key[14] > 0) g2.setColor(PaintTool.owner.back.color);
        else g2.setColor(PaintTool.owner.fore.color);
        if (eraser) {
            g2.setColor(Color.WHITE);
        }
        if (PaintTool.owner.pat.pattern != 11 && !eraser) {
            Rectangle2D.Double r = new Rectangle2D.Double(0, 0, PaintTool.pat.getWidth(), PaintTool.pat.getHeight());
            g2.setPaint(new TexturePaint(PaintTool.pat, r));
        }
        Line2D.Double line = new Line2D.Double(PaintTool.lastx[0], PaintTool.lasty[0], x, y);
        g2.draw(line);

        // Calculate clip size
        int left = 0, right = 0, top = 0, bottom = 0;
        if (x < PaintTool.lastx[0]) {
            left = (int) (x - brushSize / 2 - 1);
            right = (int) (PaintTool.lastx[0] + brushSize / 2 + 1);
        } else {
            left = (int) (PaintTool.lastx[0] - brushSize / 2 - 1);
            right = (int) (x + brushSize / 2 + 1);
        }
        if (y < PaintTool.lasty[0]) {
            top = (int) (y - brushSize / 2 - 1);
            bottom = (int) (PaintTool.lasty[0] + brushSize / 2 + 1);
        } else {
            top = (int) (PaintTool.lasty[0] - brushSize / 2 - 1);
            bottom = (int) (y + brushSize / 2 + 1);
        }

        // Draw background screen
        Graphics2D g3;
        if (PaintTool.alpha < 100) {
            g3 = (Graphics2D) PaintTool.owner.getSurface().getGraphics();
        } else {
            g3 = (Graphics2D) PaintTool.owner.mainPane.getGraphics();
        }
        g3.setClip(new Rectangle(left, top, right - left, bottom - top));
        if (!PaintTool.editBackground) {
            //g3.setColor(new Color(255,128,128));
            //g3.fillRect(0,0,640,480);
            //g3.drawImage(PaintTool.owner.bgImg, 0, 0, PaintTool.owner.mainPane);
        }

        Graphics2D g4 = (Graphics2D) PaintTool.owner.mainPane.getGraphics();
        g4.setClip(new Rectangle(left, top, right - left, bottom - top));


        // Reflect transparency and draw line segment on screen
        if (PaintTool.alpha < 100 || eraser) {
            if (PaintTool.owner.bit > 1) {
                AffineTransform af = new AffineTransform();
                af.translate(-((int) PaintTool.owner.bitLeft) * PaintTool.owner.bit/*+4*/, -((int) PaintTool.owner.bitTop) * PaintTool.owner.bit/*+4*/);
                af.scale(PaintTool.owner.bit, PaintTool.owner.bit);
                g4.transform(af);
            }

            g4.setClip(new Rectangle(left, top, right - left, bottom - top));
            g4.drawImage(PaintTool.owner.bgImg, 0, 0, PaintTool.owner.mainPane);
            g4.drawImage(PaintTool.owner.getSurface(), 0, 0, PaintTool.owner.mainPane);
            if (eraser) {
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
            } else {
                g4.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, PaintTool.alpha / 100.0F));
            }
            g4.drawImage(PaintTool.owner.redoBuf, 0, 0, PaintTool.owner.mainPane);

            if (PaintTool.owner.bit > 2) {
                Graphics g5 = PaintTool.owner.mainPane.getGraphics();
                MyPanel.bordersDraw(g5, PaintTool.owner.bit, PaintTool.owner.mainPane.getWidth(), PaintTool.owner.mainPane.getHeight());
            }

            PaintTool.lastTime = System.currentTimeMillis();
            return true;
        }

        // Draw line segment screen
        // g4.drawImage(PaintTool.owner.getSurface(), 0, 0, PaintTool.owner.mainPane);
        if (PaintTool.owner.bit > 1)
            PaintTool.owner.mainPane.repaint(
                    (left - (int) PaintTool.owner.bitLeft) * PaintTool.owner.bit,
                    (top - (int) PaintTool.owner.bitTop) * PaintTool.owner.bit,
                    (right - left) * PaintTool.owner.bit, (bottom - top) * PaintTool.owner.bit);
        else
            PaintTool.owner.mainPane.repaint(left, top, right - left, bottom - top);

        // Draw border when zoomed display
        if (PaintTool.owner.bit > 2) {
            Graphics g5 = PaintTool.owner.mainPane.getGraphics();
            MyPanel.bordersDraw(g5, PaintTool.owner.bit, PaintTool.owner.mainPane.getWidth(), PaintTool.owner.mainPane.getHeight());
        }

        PaintTool.lastTime = System.currentTimeMillis();

        return true;
    }

    @Override
    public void clear() {
    }

    @Override
    public void end() {
        //TabletManager.getDefaultManager().removeTabletListener(PaintTool.owner.mainPane, PaintTool.tabletListener);
    }
}


/**
 * Eraser
 */
class EraserTool implements toolTabletInterface {

    Cursor cursor;
    boolean shift;
    float shiftx, shifty;

    public EraserTool() {
        PaintTool.tabletinit();

        if (PaintTool.canTablet) {
            //TabletManager.getDefaultManager().addTabletListener(PaintTool.owner.mainPane, PaintTool.tabletListener);
            //PaintTool.tabletListener.tool = this;
        }
    }

    @Override
    public String getName() {
        return "Eraser";
    }

    @Override
    public void mouseUp(int x, int y) {
//        if (PaintTool.tabletListener.in_stroke) {
//            return;
//        }
        penUp(x, y, 0.5f, false);
    }

    @Override
    public void penUp(float x, float y, float pressure, boolean eraser) {

        // Write to surface
        if (true) { // eraser
            DataBuffer srcdb = PaintTool.owner.redoBuf.getRaster().getDataBuffer();
            DataBuffer tgtdb = PaintTool.owner.getSurface().getRaster().getDataBuffer();
            int width = PaintTool.owner.getSurface().getWidth();
            for (int h = 0; h < PaintTool.owner.getSurface().getHeight(); h++) {
                for (int w = 0; w < width; w++) {
                    int src = srcdb.getElem(0, h * width + w);
                    int tgt = tgtdb.getElem(0, h * width + w);
                    int c = tgt;
                    if ((src & 0xFF00_0000) != 0) {
                        //int a = (c & 0xFF00_0000)>>24;
                        //a = (a * (0xFF - ((src & 0xFF00_0000) >> 24))) / 0xFF;
                        //c = (a << 24) + tgt & 0x00FF_FFFF;
                        if (PaintTool.editBackground) {
                            c = 0xFFFF_FFFF;
                        } else {
                            c = 0x00FF_FFFF;
                        }
                    }
                    tgtdb.setElem(0, h * width + w, c);
                }
            }
        }

        PaintTool.toCdPict();
        PaintTool.owner.mainPane.repaint();
    }

    @Override
    public void mouseDown(int x, int y) {
        /*if(PaintTool.tabletListener.in_stroke){
            return;
        }*/
        penDown(x, y + 0.5f, 0.5f, false);
    }

    @Override
    public void penDown(float x, float y, float pressure, boolean eraser) {
        GMenuPaint.setUndo();
        shift = (GUI.key[11] > 0);
        shiftx = x;
        shifty = y;

        if (true) { // eraser
            Graphics2D g2 = (Graphics2D) PaintTool.owner.redoBuf.getGraphics();
            g2.setColor(new Color(255, 255, 255));
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.CLEAR, 0.0f));
            g2.fillRect(0, 0, PaintTool.owner.redoBuf.getWidth(), PaintTool.owner.redoBuf.getHeight());
        }

        PaintTool.lastx[0] = x;
        PaintTool.lasty[0] = y;
        penStillDown(x, y, pressure, eraser);
    }

    @Override
    public boolean mouseWithin(int x, int y) {
//        if (PaintTool.tabletListener.in_stroke) {
//            return false;
//        }
        return penWithin(x, y, 0.5f);
    }

    public static boolean penWithin(float x, float y, float pressure) {
        return true;
    }

    @Override
    public boolean mouseStillDown(int x, int y) {
//        if (PaintTool.tabletListener.in_stroke) {
//            return false;
//        }
        return penStillDown(x, y + 0.5f, 0.5f, false);
    }

    @Override
    public boolean penStillDown(float x, float y, float pressure, boolean eraser) {
        if (shift && ((x - shiftx != 0) || (y - shifty != 0))) {
            if ((shiftx == -1) || (shifty != -1) && (Math.abs(x - shiftx) > Math.abs(y - shifty))) {
                shiftx = -1;
                y = shifty;
                PaintTool.lasty[0] = (int) shifty;
            } else if ((shifty == -1) || (shiftx != -1)) {
                shifty = -1;
                x = shiftx;
                PaintTool.lastx[0] = (int) shiftx;
            }
        }

        // Get back buffer
        Graphics2D g2;
        if (true) { // eraser
            g2 = (Graphics2D) PaintTool.owner.redoBuf.getGraphics();
        }

        // Draw line on back buffer
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        float brushSize = 16.0f / PaintTool.owner.bit;
        g2.setStroke(new BasicStroke(brushSize, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER));

        if (PaintTool.editBackground) {
            g2.setColor(Color.WHITE);
        } else {
            Rectangle2D.Double r = new Rectangle2D.Double(0, 0, PaintTool.owner.bgImg.getWidth(), PaintTool.owner.bgImg.getHeight());
            g2.setPaint(new TexturePaint(PaintTool.owner.bgImg, r));
        }
        Line2D.Double line;
        for (int i = 1; i <= 16; i++) {
            line = new Line2D.Double(
                    (PaintTool.lastx[0] * i + x * (16 - i)) / 16,
                    (PaintTool.lasty[0] * i + y * (16 - i)) / 16 - brushSize / 2,
                    (PaintTool.lastx[0] * i + x * (16 - i)) / 16,
                    (PaintTool.lasty[0] * i + y * (16 - i)) / 16 + brushSize / 2);
            g2.draw(line);
        }

        // Calculate clip size
        int left = 0, right = 0, top = 0, bottom = 0;
        if (x < PaintTool.lastx[0]) {
            left = (int) (x - brushSize / 2 - 4);
            right = (int) (PaintTool.lastx[0] + brushSize / 2 + 4);
        } else {
            left = (int) (PaintTool.lastx[0] - brushSize / 2 - 4);
            right = (int) (x + brushSize / 2 + 4);
        }
        if (y < PaintTool.lasty[0]) {
            top = (int) (y - brushSize / 2 - 4);
            bottom = (int) (PaintTool.lasty[0] + brushSize / 2 + 4);
        } else {
            top = (int) (PaintTool.lasty[0] - brushSize / 2 - 4);
            bottom = (int) (y + brushSize / 2 + 4);
        }

        // Draw background screen
        Graphics2D g3;
        g3 = (Graphics2D) PaintTool.owner.mainPane.getGraphics();
        g3.setClip(new Rectangle(left, top, right - left, bottom - top));
        if (!PaintTool.editBackground) {
            // g3.setColor(new Color(255,128,128));
            // g3.fillRect(0,0,640,480);
            // g3.drawImage(PaintTool.owner.bgImg, 0, 0, PaintTool.owner.mainPane);
        }

        Graphics2D g4 = (Graphics2D) PaintTool.owner.mainPane.getGraphics();
        g4.setClip(new Rectangle(left, top, right - left, bottom - top));

        // Reflect transparency and draw line segment on screen
        if (true) {
            if (PaintTool.owner.bit > 1) {
                AffineTransform af = new AffineTransform();
                af.translate(-((int) PaintTool.owner.bitLeft) * PaintTool.owner.bit/*+4*/, -((int) PaintTool.owner.bitTop) * PaintTool.owner.bit/*+4*/);
                af.scale(PaintTool.owner.bit, PaintTool.owner.bit);
                g4.transform(af);
            }

            g4.setClip(new Rectangle(left, top, right - left, bottom - top));
            g4.drawImage(PaintTool.owner.bgImg, 0, 0, PaintTool.owner.mainPane);
            g4.drawImage(PaintTool.owner.getSurface(), 0, 0, PaintTool.owner.mainPane);
//            if (eraser) {
//                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
//            } else {
//                g4.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, PaintTool.alpha / 100.0F));
//            }
            g4.drawImage(PaintTool.owner.redoBuf, 0, 0, PaintTool.owner.mainPane);

            // Draw border when zoomed display
            if (PaintTool.owner.bit > 2) {
                Graphics g5 = PaintTool.owner.mainPane.getGraphics();
                MyPanel.bordersDraw(g5, PaintTool.owner.bit, PaintTool.owner.mainPane.getWidth(), PaintTool.owner.mainPane.getHeight());
            }

            PaintTool.lastTime = System.currentTimeMillis();
        }

        return true;
    }

    @Override
    public void clear() {

    }

    @Override
    public void end() {
        //TabletManager.getDefaultManager().removeTabletListener(PaintTool.owner.mainPane, PaintTool.tabletListener);
    }
}


/**
 * Paint Bucket
 */
class PaintBucketTool implements ToolInterface {

    ArrayList<Point> pointList;

    @Override
    public String getName() {
        return "PaintBucket";
    }

    public static void gradfill(BufferedImage img, Color color1, Color color2, double angle) {
        // Get the region of the img's opaque part with angle correction
        DataBuffer buffer = img.getRaster().getDataBuffer();
        Point topPoint = null;
        Point bottomPoint = null;
        int width = img.getWidth();
        int height = img.getHeight();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if ((0xFF00_0000 & buffer.getElem(0, x + y * width)) != 0) {
                    //System.out.println((0xFF00_0000 & buffer.getElem(0, x + y * width)) + " " + (buffer.getElem(0, x + y * width)));
                    if (topPoint == null) {
                        topPoint = new Point(x, y);
                        bottomPoint = new Point(x, y);
                    }
                    if (x * Math.sin(angle) + y * Math.cos(angle) < topPoint.x * Math.sin(angle) + topPoint.y * Math.cos(angle)) {
                        topPoint.x = x;
                        topPoint.y = y;
                    }
                    if (x * Math.sin(angle) + y * Math.cos(angle) > bottomPoint.x * Math.sin(angle) + bottomPoint.y * Math.cos(angle)) {
                        bottomPoint.x = x;
                        bottomPoint.y = y;
                    }
                }
            }
        }

        // Fill opaque part with gradient
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                // System.out.println(buffer.getElem(0, x+y*width));
                if ((0xFF000000 & buffer.getElem(0, x + y * width)) != 0) {
                    int c = 0;
                    double percent;
                    percent = (x * Math.sin(angle) + y * Math.cos(angle) - (topPoint.x * Math.sin(angle) + topPoint.y * Math.cos(angle)))
                            / (bottomPoint.x * Math.sin(angle) + bottomPoint.y * Math.cos(angle) - (topPoint.x * Math.sin(angle) + topPoint.y * Math.cos(angle)));
                    c = ((int) (color1.getRed() * percent + color2.getRed() * (1.0 - percent))) << 16;
                    c += ((int) (color1.getGreen() * percent + color2.getGreen() * (1.0 - percent))) << 8;
                    c += ((int) (color1.getBlue() * percent + color2.getBlue() * (1.0 - percent)));
                    buffer.setElem(0, x + y * width, 0xFF000000 + c);
                }
            }
        }
    }

    private void seedFillH(BufferedImage surface, BufferedImage newSurface, int px, int py, int srcColor, int newColor) {
        DataBuffer buffer = surface.getRaster().getDataBuffer();
        DataBuffer newBuffer = newSurface.getRaster().getDataBuffer();
        int width = surface.getWidth();
        int height = surface.getHeight();

        // Check left
        int lx;
        for (lx = -1; px + lx >= 0; lx--) {
//            System.out.println("<srcColor:" + srcColor);
//            System.out.println("<getElem(" + (px + lx) + "," + py + "):" + buffer.getElem(0, px + lx + py * width));
            if ((0x00FFFFFF & buffer.getElem(0, px + lx + py * width)) != (0x00FFFFFF & srcColor)) {
                break;
            }
        }
        lx++;

        // Check right
        int rx;
        for (rx = 1; px + rx < width; rx++) {
//            System.out.println(">srcColor:" + srcColor);
//            System.out.println(">getElem(" + (px + rx) + "," + py + "):" + buffer.getElem(0, px + lx + py * width));
            if ((0x00FFFFFF & buffer.getElem(0, px + rx + py * width)) != (0x00FFFFFF & srcColor)) {
                break;
            }
        }
        rx--;

        // Paint that line
        for (int x = px + lx; x <= px + rx; x++) {
            newBuffer.setElem(0, x + py * width, newColor);
        }

        // Search for the line above
        if (py - 1 >= 0) {
            for (int x = px + lx; x <= px + rx; x++) {
                // Search for the right edge
                if ((0x00FF_FFFF & buffer.getElem(0, x + (py - 1) * width)) == (0x00FF_FFFF & srcColor)) {
                    if (x == px + rx ||
                            (0x00FF_FFFF & buffer.getElem(0, (x + 1) + (py - 1) * width)) != (0x00FF_FFFF & srcColor)) {
                        if (newBuffer.getElem(0, x + (py - 1) * width) != newColor) {
                            // Not registered, so register it
                            pointList.add(new Point(x, py - 1));
                            newBuffer.setElem(0, x + (py - 1) * width, newColor);
                        }
                    }
                }
            }
        }

        // Search for the line below
        if (py + 1 < height) {
            for (int x = px + lx; x < px + rx; x++) {
                // Search for the right edge
                if ((0x00FF_FFFF & buffer.getElem(0, x + (py + 1) * width)) == (0x00FF_FFFF & srcColor)) {
                    if (x + 1 == px + rx ||
                            (0x00FF_FFFF & buffer.getElem(0, (x + 1) + (py + 1) * width)) != (0x00FF_FFFF & srcColor)) {
                        if (newBuffer.getElem(0, x + (py + 1) * width) != newColor) {
                            // Not registered, so register it
                            pointList.add(new Point(x, py + 1));
                            newBuffer.setElem(0, x + (py + 1) * width, newColor);
                        }
                    }
                }
            }
        }
    }

    private void seedfill(BufferedImage surface, BufferedImage newSurface, int px, int py) {
        DataBuffer buffer = surface.getRaster().getDataBuffer();
        int width = surface.getWidth();
        int height = surface.getHeight();
        int srcColor = buffer.getElem(0, px + py * width);
        //Color c = PaintTool.owner.stack.toolbar.fore.color;
        int newColor = 0xFF00_0000 /* + (c.getRed() << 16) + (c.getGreen() << 8) + c.getBlue() */;

        // Reset list
        pointList = new ArrayList<>();

        // Make back buffer transparent
        Graphics2D g = (Graphics2D) newSurface.getGraphics();
        g.setColor(new Color(255, 255, 255));
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.CLEAR, 0.0f));
        Rectangle2D.Double rect = new Rectangle2D.Double(0, 0, width, height);
        g.fill(rect);

        // Make transparent parts of surface #FFFFFF
        DataBuffer dbuffer = surface.getRaster().getDataBuffer();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if ((dbuffer.getElem(x + y * width) & -0xFF00_0000) == 0) {
                    dbuffer.setElem(x + y * width, 0x00FF_FFFF);
                }
            }
        }

        seedFillH(surface, newSurface, px, py, srcColor, newColor);

        while (!pointList.isEmpty()) {
            Point p = pointList.get(0);
            pointList.remove(0);
            seedFillH(surface, newSurface, p.x, p.y, srcColor, newColor);
        }

        // Apply pattern
        PaintTool.setPattern(false);
        DataBuffer fillBuf = newSurface.getRaster().getDataBuffer();
        DataBuffer patBuf = PaintTool.pat.getRaster().getDataBuffer();
        int patW = PaintTool.pat.getWidth();
        int patH = PaintTool.pat.getHeight();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int c1 = fillBuf.getElem(0, x + y * width);
                if ((c1 & 0xFF00_0000) != 0) {
                    int c2 = patBuf.getElem(x % patW + (y % patH) * patW);
                    //c2 = (c2 & 0x00FF_FFFF) + ((((c1 & 0xFF00_0000) >> 24) * ((c2 & 0xFF00_0000) >> 24)) / 0xFF) << 24;
                    fillBuf.setElem(0, x + y * width, c2);
                }
            }
        }

        // Apply gradient (cannot be used with pattern)
        if (PaintTool.owner.grad.use) {
            gradfill(newSurface, PaintTool.owner.grad.color1, PaintTool.owner.grad.color2, PaintTool.owner.grad.angle);
        }

        // Reflect to surface
        Graphics2D g2 = (Graphics2D) surface.getGraphics();
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, PaintTool.alpha / 100.0F));
        g2.drawImage(newSurface, 0, 0, PaintTool.owner.mainPane);

        // Reflect to front screen
        Graphics2D g3 = (Graphics2D) PaintTool.owner.mainPane.getGraphics();
        g3.drawImage(PaintTool.owner.bgImg, 0, 0, PaintTool.owner.mainPane);
        if (!PaintTool.editBackground) {
            g3.drawImage(PaintTool.owner.mainImg, 0, 0, PaintTool.owner.mainPane);
        }
    }

    @Override
    public void mouseUp(int x, int y) {
        if (GUI.key[12] > 1) {
            PaintTool.spoit(x, y);
            return;
        }

        GMenuPaint.setUndo();

        seedfill(PaintTool.owner.getSurface(), PaintTool.owner.redoBuf, x, y);

        PaintTool.toCdPict();
        PaintTool.owner.mainPane.repaint();
    }

    @Override
    public void mouseDown(int x, int y) {
    }

    @Override
    public boolean mouseWithin(int x, int y) {
        if (GUI.key[12] > 0) {
            PaintTool.owner.mainPane.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
        } else {
            PaintTool.owner.mainPane.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        }
        return true;
    }

    @Override
    public boolean mouseStillDown(int x, int y) {
        return true;
    }

    @Override
    public void clear() {

    }

    @Override
    public void end() {

    }
}


/**
 * Rectangle
 */
class RectTool implements ToolInterface {

    private Rectangle srcRect;
    private Rectangle lastClipRect = new Rectangle();

    @Override
    public String getName() {
        return "Rect";
    }

    private void strokeDraw(Rectangle rect) {
        float lineSize = PaintTool.lineSize;

        Rectangle clipRect = (Rectangle) rect.clone();
        if (clipRect.width < 0) {
            clipRect.width *= -1;
            clipRect.x -= clipRect.width;
        }
        if (clipRect.height < 0) {
            clipRect.height *= -1;
            clipRect.y -= clipRect.height;
        }
        clipRect.x -= (int) lineSize + 2;
        clipRect.y -= (int) lineSize + 2;
        clipRect.width += 2 * (int) lineSize + 3;
        clipRect.height += 2 * (int) lineSize + 3;

        // Get back buffer
        Graphics2D g2 = (Graphics2D) PaintTool.owner.redoBuf.getGraphics();
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.CLEAR, 0.0f));
        g2.fill(clipRect.union(lastClipRect));
        g2 = (Graphics2D) PaintTool.owner.redoBuf.getGraphics();

        // Write rectangle on back buffer
        if (PaintTool.antialias) {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        } else {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        }

        // Line type
        g2.setColor(PaintTool.owner.fore.color);
        if (PaintTool.antialias) {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        } else {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        }
        g2.setStroke(new BasicStroke(lineSize, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER));

        if (PaintTool.owner.fill) {
            // Pattern
            Rectangle2D.Double r = new Rectangle2D.Double(0, 0, PaintTool.pat.getWidth(), PaintTool.pat.getHeight());
            g2.setPaint(new TexturePaint(PaintTool.pat, r));
            g2.fill(rect);
        }

        // Draw rectangle
        g2.draw(rect);

        Graphics2D g4 = (Graphics2D) PaintTool.owner.mainPane.getGraphics();

        // Zoomed display
        if (PaintTool.owner.bit > 1) {
            AffineTransform af = new AffineTransform();
            af.translate(-((int) PaintTool.owner.bitLeft) * PaintTool.owner.bit, -((int) PaintTool.owner.bitTop) * PaintTool.owner.bit);
            af.scale(PaintTool.owner.bit, PaintTool.owner.bit);
            g4.transform(af);
        }

        // Reflect transparency and draw rectangle on screen
        g4.setClip(clipRect.union(lastClipRect));
        g4.drawImage(PaintTool.owner.bgImg, 0, 0, PaintTool.owner.mainPane);
        g4.drawImage(PaintTool.owner.getSurface(), 0, 0, PaintTool.owner.mainPane);
        {
            g4.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, PaintTool.alpha / 100.0F));
        }
        g4.drawImage(PaintTool.owner.redoBuf, 0, 0, PaintTool.owner.mainPane);

        // Draw border when zoomed display
        if (PaintTool.owner.bit > 2) {
            Graphics g5 = PaintTool.owner.mainPane.getGraphics();
            MyPanel.bordersDraw(g5, PaintTool.owner.bit, PaintTool.owner.mainPane.getWidth(), PaintTool.owner.mainPane.getHeight());
        }

        lastClipRect = clipRect;
    }

    private Rectangle setSelection(int x, int y) {
        //Graphics2D g3 = (Graphics2D) PaintTool.owner.mainPane.getGraphics();

        Rectangle r = (Rectangle) srcRect.clone();

        if (GUI.key[11] > 1) { // Square with Shift
            if (Math.abs(r.x - x) < Math.abs(r.y - y)) {
                if ((r.x - x) < 0 == (r.y - y) < 0)
                    y = (x - r.x) + r.y;
                else
                    y = (r.x - x) + r.y;
            } else {
                if ((r.x - x) < 0 == (r.y - y) < 0)
                    x = (y - r.y) + r.x;
                else
                    x = (r.y - y) + r.x;
            }
        }

        if (x < r.x) {
            r.width = r.x - x;
            r.x = x;
        } else {
            r.width = x - r.x;
        }
        if (y < r.y) {
            r.height = r.y - y;
            r.y = y;
        } else {
            r.height = y - r.y;
        }

        strokeDraw(r);

        return r;
    }

    @Override
    public void mouseUp(int x, int y) {
        srcRect = setSelection(x, y);

        // Reflect to surface
        Graphics2D g = PaintTool.owner.getSurface().createGraphics();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, PaintTool.alpha / 100.0F));
        g.drawImage(PaintTool.owner.redoBuf, 0, 0, PaintTool.owner.mainPane);

        PaintTool.owner.mainPane.repaint();
    }

    @Override
    public void mouseDown(int x, int y) {
        GMenuPaint.setUndo();

        PaintTool.setPattern(GUI.key[14] > 0);

        // Create new selection area
        srcRect = new Rectangle(x, y, 0, 0);
    }

    @Override
    public boolean mouseWithin(int x, int y) {
        return false;
    }

    @Override
    public boolean mouseStillDown(int x, int y) {
        {
            if (x == srcRect.x + srcRect.width && y == srcRect.y + srcRect.height) {
                return false;
            }
            setSelection(x, y);
        }
        return true;
    }

    @Override
    public void clear() {
    }

    @Override
    public void end() {
    }
}


/**
 * Ellipse
 */
class OvalTool implements ToolInterface {

    private Rectangle srcRect;
    private Rectangle lastClipRect = new Rectangle();

    @Override
    public String getName() {
        return "Oval";
    }

    private void strokeDraw(Rectangle rect) {
        float lineSize = PaintTool.lineSize;

        Rectangle clipRect = (Rectangle) rect.clone();
        if (clipRect.width < 0) {
            clipRect.width *= -1;
            clipRect.x -= clipRect.width;
        }
        if (clipRect.height < 0) {
            clipRect.height *= -1;
            clipRect.y -= clipRect.height;
        }
        clipRect.x -= (int) lineSize + 1;
        clipRect.y -= (int) lineSize + 1;
        clipRect.width += 2 * (int) lineSize + 2;
        clipRect.height += 2 * (int) lineSize + 2;

        // Get back buffer
        Graphics2D g2 = (Graphics2D) PaintTool.owner.redoBuf.getGraphics();
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.CLEAR, 0.0f));
        g2.fill(clipRect.union(lastClipRect));
        g2 = (Graphics2D) PaintTool.owner.redoBuf.getGraphics();

        // Draw ellipse on back buffer
        if (PaintTool.antialias) {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        } else {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        }

        // Line type
        g2.setColor(PaintTool.owner.fore.color);
        if (PaintTool.antialias) {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        } else {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        }
        if (PaintTool.owner.fill) {
            // Pattern
            Paint savePaint = g2.getPaint();
            Rectangle2D.Double r = new Rectangle2D.Double(0, 0, PaintTool.pat.getWidth(), PaintTool.pat.getHeight());
            g2.setPaint(new TexturePaint(PaintTool.pat, r));
            g2.fill(new Ellipse2D.Float(rect.x, rect.y, rect.width, rect.height));
            g2.setPaint(savePaint);
        }

        // Draw ellipse
        g2.setStroke(new BasicStroke(lineSize, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        g2.draw(new Ellipse2D.Float(rect.x, rect.y, rect.width, rect.height));


        Graphics2D g4 = (Graphics2D) PaintTool.owner.mainPane.getGraphics();

        // Zoomed display
        if (PaintTool.owner.bit > 1) {
            AffineTransform af = new AffineTransform();
            af.translate(-((int) PaintTool.owner.bitLeft) * PaintTool.owner.bit, -((int) PaintTool.owner.bitTop) * PaintTool.owner.bit);
            af.scale(PaintTool.owner.bit, PaintTool.owner.bit);
            g4.transform(af);
        }

        // Reflect transparency and draw ellipse on screen
        g4.setClip(clipRect.union(lastClipRect));
        g4.drawImage(PaintTool.owner.bgImg, 0, 0, PaintTool.owner.mainPane);
        g4.drawImage(PaintTool.owner.getSurface(), 0, 0, PaintTool.owner.mainPane);
        {
            g4.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, PaintTool.alpha / 100.0F));
        }
        g4.drawImage(PaintTool.owner.redoBuf, 0, 0, PaintTool.owner.mainPane);

        // Draw border when zoomed display
        if (PaintTool.owner.bit > 2) {
            Graphics g5 = PaintTool.owner.mainPane.getGraphics();
            MyPanel.bordersDraw(g5, PaintTool.owner.bit, PaintTool.owner.mainPane.getWidth(), PaintTool.owner.mainPane.getHeight());
        }

        lastClipRect = clipRect;

    }

    private Rectangle setSelection(int x, int y) {
        // Graphics2D g3 = (Graphics2D) PaintTool.owner.mainPane.getGraphics();

        Rectangle r = (Rectangle) srcRect.clone();

        if (GUI.key[11] > 1) { // Square with Shift
            if (Math.abs(r.x - x) < Math.abs(r.y - y)) {
                if ((r.x - x) < 0 == (r.y - y) < 0)
                    y = (x - r.x) + r.y;
                else
                    y = (r.x - x) + r.y;
            } else {
                if ((r.x - x) < 0 == (r.y - y) < 0)
                    x = (y - r.y) + r.x;
                else
                    x = (r.y - y) + r.x;
            }
        }

        if (x < r.x) {
            r.width = r.x - x;
            r.x = x;
        } else {
            r.width = x - r.x;
        }
        if (y < r.y) {
            r.height = r.y - y;
            r.y = y;
        } else {
            r.height = y - r.y;
        }


        strokeDraw(r);

        return r;
    }

    @Override
    public void mouseUp(int x, int y) {
        {
            srcRect = setSelection(x, y);

            // Reflect to surface
            Graphics2D g = PaintTool.owner.getSurface().createGraphics();
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, PaintTool.alpha / 100.0F));
            g.drawImage(PaintTool.owner.redoBuf, 0, 0, PaintTool.owner.mainPane);

            PaintTool.owner.mainPane.repaint();
        }
    }

    @Override
    public void mouseDown(int x, int y) {
        {
            GMenuPaint.setUndo();

            PaintTool.setPattern(GUI.key[14] > 0);

            // Create new selection area
            srcRect = new Rectangle(x, y, 0, 0);
        }
    }

    @Override
    public boolean mouseWithin(int x, int y) {
        return false;
    }

    @Override
    public boolean mouseStillDown(int x, int y) {
        {
            if (x == srcRect.x + srcRect.width && y == srcRect.y + srcRect.height) {
                return false;
            }
            setSelection(x, y);
        }
        return true;
    }

    @Override
    public void clear() {
    }

    @Override
    public void end() {
    }
}


// -------------------
// Line
// -------------------
class LineTool implements ToolInterface {

    private Rectangle srcRect;
    private Rectangle lastClipRect = new Rectangle();

    @Override
    public String getName() {
        return "Line";
    }

    private void strokeDraw(Rectangle rect) {
        float lineSize = PaintTool.lineSize;

        Rectangle clipRect = (Rectangle) rect.clone();
        if (clipRect.width < 0) {
            clipRect.width *= -1;
            clipRect.x -= clipRect.width;
        }
        if (clipRect.height < 0) {
            clipRect.height *= -1;
            clipRect.y -= clipRect.height;
        }
        clipRect.x -= (int) lineSize + 1;
        clipRect.y -= (int) lineSize + 1;
        clipRect.width += 2 * (int) lineSize + 2;
        clipRect.height += 2 * (int) lineSize + 2;

        // Get back buffer
        Graphics2D g2 = (Graphics2D) PaintTool.owner.redoBuf.getGraphics();
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.CLEAR, 0.0f));
        g2.fill(clipRect.union(lastClipRect));
        g2 = (Graphics2D) PaintTool.owner.redoBuf.getGraphics();

        // Draw line on back buffer

        // Line type
        g2.setColor(PaintTool.owner.fore.color);
        if (PaintTool.antialias) {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        } else {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        }
        g2.setStroke(new BasicStroke(lineSize, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        // Draw line
        g2.drawLine(rect.x, rect.y, rect.x + rect.width, rect.y + rect.height);


        Graphics2D g4 = (Graphics2D) PaintTool.owner.mainPane.getGraphics();

        // Zoomed display
        if (PaintTool.owner.bit > 1) {
            AffineTransform af = new AffineTransform();
            af.translate(-((int) PaintTool.owner.bitLeft) * PaintTool.owner.bit, -((int) PaintTool.owner.bitTop) * PaintTool.owner.bit);
            af.scale(PaintTool.owner.bit, PaintTool.owner.bit);
            g4.transform(af);
        }

        // Reflect transparency and draw line segment on screen
        g4.setClip(clipRect.union(lastClipRect));
        g4.drawImage(PaintTool.owner.bgImg, 0, 0, PaintTool.owner.mainPane);
        g4.drawImage(PaintTool.owner.getSurface(), 0, 0, PaintTool.owner.mainPane);
        {
            g4.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, PaintTool.alpha / 100.0F));
        }
        g4.drawImage(PaintTool.owner.redoBuf, 0, 0, PaintTool.owner.mainPane);

        // Draw border when zoomed display
        if (PaintTool.owner.bit > 2) {
            Graphics g5 = PaintTool.owner.mainPane.getGraphics();
            MyPanel.bordersDraw(g5, PaintTool.owner.bit, PaintTool.owner.mainPane.getWidth(), PaintTool.owner.mainPane.getHeight());
        }

        PaintTool.lastTime = System.currentTimeMillis();

        lastClipRect = clipRect;
    }

    private Rectangle setSelection(int x, int y) {
        if (srcRect == null) return new Rectangle(x, y, 0, 0);

        Rectangle r = (Rectangle) srcRect.clone();

        if (GUI.key[11] > 1) { // 45-degree units with Shift
            if (Math.abs(r.x - x) < Math.abs(r.y - y) / 2) {
                x = r.x;
            } else if (Math.abs(r.x - x) < Math.abs(r.y - y)) {
                if ((r.x - x) < 0 == (r.y - y) < 0)
                    y = (x - r.x) + r.y;
                else
                    y = (r.x - x) + r.y;
            } else if (Math.abs(r.x - x) / 2 > Math.abs(r.y - y)) {
                y = r.y;
            } else {
                if ((r.x - x) < 0 == (r.y - y) < 0)
                    x = (y - r.y) + r.x;
                else
                    x = (r.y - y) + r.x;
            }
        }

        r.width = x - r.x;
        r.height = y - r.y;

        strokeDraw(r);

        return r;
    }

    @Override
    public void mouseUp(int x, int y) {
        srcRect = setSelection(x, y);

        Graphics2D g = PaintTool.owner.getSurface().createGraphics();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, PaintTool.alpha / 100.0F));
        g.drawImage(PaintTool.owner.redoBuf, 0, 0, PaintTool.owner.mainPane);

        /* // Reflect to surface
        Graphics2D g3 = PaintTool.owner.getSurface().createGraphics();

        // Line type
        g3.setColor(PaintTool.owner.fore.color);
        if(PaintTool.antialias){
            g3.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        }else{
            g3.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        }
        float lineSize = PaintTool.lineSize;
        g3.setStroke(new BasicStroke(lineSize, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        // Draw line
        g3.drawLine(srcRect.x, srcRect.y, srcRect.x+srcRect.width, srcRect.y+srcRect.height);
        */
        PaintTool.owner.mainPane.repaint();
    }

    @Override
    public void mouseDown(int x, int y) {
        {
            GMenuPaint.setUndo();

            PaintTool.setPattern(GUI.key[14] > 0);

            // Create new selection area
            srcRect = new Rectangle(x, y, 0, 0);

            lastClipRect = new Rectangle(x, y, 0, 0);
        }
    }

    @Override
    public boolean mouseWithin(int x, int y) {
        return false;
    }

    @Override
    public boolean mouseStillDown(int x, int y) {
        {
            if (x == srcRect.x + srcRect.width && y == srcRect.y + srcRect.height) {
                return false;
            }
            setSelection(x, y);
        }
        return true;
    }

    @Override
    public void clear() {
    }

    @Override
    public void end() {
    }
}


// -------------------
// Selection
// -------------------
class SelectTool implements ToolSelectInterface {
    // srcRect: Selected area
    // moveRect: Current floating position
    // redoBuf: Floating selection of selected area (0,0,width,height)
    // move: If true, there is a floating selection

    Rectangle srcRect;
    Rectangle moveRect;
    boolean move = false;
    boolean shift;
    int shiftx, shifty;

    @Override
    public String getName() {
        return "Select";
    }

    private void strokeDraw() {
        Rectangle rect = moveRect;
        if (rect == null) rect = srcRect;
        if (rect == null) return;
        strokeDraw(rect);
    }

    private static void strokeDraw(Rectangle rect) {
        rect = (Rectangle) rect.clone();

        if (PaintTool.owner.bit > 1) {
            rect.x = (rect.x - (int) PaintTool.owner.bitLeft) * PaintTool.owner.bit;
            rect.y = (rect.y - (int) PaintTool.owner.bitTop) * PaintTool.owner.bit;
            rect.width *= PaintTool.owner.bit;
            rect.height *= PaintTool.owner.bit;
        }

        rect.width--;
        rect.height--;

        Graphics2D g3 = (Graphics2D) PaintTool.owner.mainPane.getGraphics();
        g3.setColor(Color.WHITE);
        g3.draw(rect);
        float[] dash = {4.0f, 2.0f};
        int i = (((int) System.currentTimeMillis() / 100) & 0xFFFF) % 6;
        g3.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dash, i));
        g3.setColor(Color.BLACK);
        g3.draw(rect);
    }

    private Rectangle setSelection(int x, int y) {
        if (srcRect == null) return null;

        // Graphics2D g3 = (Graphics2D) PaintTool.owner.mainPane.getGraphics();
        PaintTool.owner.mainPane.paintImmediately(new Rectangle(0, 0, PaintTool.owner.mainPane.getWidth(), PaintTool.owner.mainPane.getHeight()));

        Rectangle r = (Rectangle) srcRect.clone();
        if (x < r.x) {
            r.width = r.x - x;
            r.x = x;
        } else {
            r.width = x - r.x;
        }
        if (y < r.y) {
            r.height = r.y - y;
            r.y = y;
        } else {
            r.height = y - r.y;
        }
        strokeDraw(r);

        try {
            Thread.sleep(30);
        } catch (InterruptedException e) {
        }

        return r;
    }

    void viewSelection() {
        // Draw partial temporary buffer
        // Graphics2D g4 = (Graphics2D) PaintTool.owner.mainPane.getGraphics();
        // g4.drawImage(PaintTool.owner.redoBuf, moveRect.x, moveRect.y, moveRect.x+moveRect.width, moveRect.y+moveRect.height,
        //        0, 0, moveRect.width, moveRect.height, PaintTool.owner.mainPane);
        // MyPanel.mainPaneDraw(g4, PaintTool.owner.redoBuf, moveRect.x, moveRect.y, moveRect.width, moveRect.height);
        PaintTool.owner.mainPane.repaint();

        // Display selection area dashed line
        strokeDraw();
    }

    @Override
    public void mouseUp(int x, int y) {
        if (move == false && srcRect != null) {
            GMenuPaint.setUndo();

            srcRect = setSelection(x, y);

            moveRect = (Rectangle) srcRect.clone();
            move = true;

            // Move selection area to temporary buffer
            Graphics2D g = (Graphics2D) PaintTool.owner.redoBuf.getGraphics();
            g.setColor(new Color(255, 255, 255));
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.CLEAR, 0.0f));
            g.fillRect(0, 0, PaintTool.owner.redoBuf.getWidth(), PaintTool.owner.redoBuf.getHeight());
            g.setComposite(AlphaComposite.Src);
            g.drawImage(PaintTool.owner.getSurface(), 0, 0, srcRect.width, srcRect.height,
                    srcRect.x, srcRect.y, srcRect.x + srcRect.width, srcRect.y + srcRect.height, PaintTool.owner.mainPane);

            // Make the moved part transparent
            Graphics2D g2 = (Graphics2D) PaintTool.owner.getSurface().getGraphics();
            g2.setColor(new Color(255, 255, 255));
            if (!PaintTool.editBackground) {
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.CLEAR, 0.0f));
            }
            g2.fillRect(srcRect.x, srcRect.y, srcRect.width, srcRect.height);

            // Reflect to front screen
            /*Graphics2D g3 = (Graphics2D) PaintTool.owner.mainPane.getGraphics();
            g3.setColor(new Color(255,255,255));
            g3.fillRect(0, 0, PaintTool.owner.mainPane.getWidth(), PaintTool.owner.mainPane.getHeight());
            g3.drawImage(PaintTool.owner.bgImg, 0, 0, PaintTool.owner.mainPane);
            if(!PaintTool.editBackground){
                g3.drawImage(PaintTool.owner.mainImg, 0, 0, PaintTool.owner.mainPane);
            }*/
            if (PaintTool.owner.bit > 1) {
                PaintTool.owner.mainPane.repaint();
            } else {
                PaintTool.owner.mainPane.repaint(moveRect);
            }

            viewSelection();
        } else {
            mouseStillDown(x, y);
        }
    }

    @Override
    public void mouseDown(int x, int y) {
        if (move == true && moveRect.contains(new Point(x, y))) {
            // Start moving selection area
            shift = (GUI.key[11] > 0);
            shiftx = x;
            shifty = y;

            if (GUI.key[12] > 0) {
                Graphics2D g = (Graphics2D) PaintTool.owner.getSurface().getGraphics();
                g.drawImage(PaintTool.owner.redoBuf, moveRect.x, moveRect.y, moveRect.x + moveRect.width, moveRect.y + moveRect.height,
                        0, 0, srcRect.width, srcRect.height, PaintTool.owner.mainPane);
            }
        } else {
            // Draw floating area to actual area
            GMenuPaint.setUndo();
            end();

            // Create new selection area
            srcRect = new Rectangle(x, y, 0, 0);

            moveRect = null;
            move = false;
        }
    }

    @Override
    public boolean mouseWithin(int x, int y) {

        if (GUI.key[20] > 0 || GUI.key[21] > 0) { // BACKSPACE or DEL
            clear();
            PaintTool.owner.mainPane.repaint();
            return false;
        }

        strokeDraw();
        if (move == true && moveRect.contains(new Point(x, y))) {
            PaintTool.owner.mainPane.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        } else {
            PaintTool.owner.mainPane.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
        }

        return false;
    }

    @Override
    public boolean mouseStillDown(int x, int y) {
        if (move == true) {
            // Move selection area

            if (shift && ((x - shiftx != 0) || (y - shifty != 0))) {
                if ((shiftx == -1) || (shifty != -1) && (Math.abs(x - shiftx) > Math.abs(y - shifty))) {
                    shiftx = -1;
                    y = shifty;
                    PaintTool.lasty[0] = shifty;
                } else if ((shifty == -1) || (shiftx != -1)) {
                    shifty = -1;
                    x = shiftx;
                    PaintTool.lastx[0] = shiftx;
                }
            }

            moveRect.x = (int) (moveRect.x + x - PaintTool.lastx[0]);
            moveRect.y = (int) (moveRect.y + y - PaintTool.lasty[0]);

            // Reflect to front screen
            /*Graphics2D g3 = (Graphics2D) PaintTool.owner.mainPane.getGraphics();
            g3.drawImage(PaintTool.owner.bgImg, 0, 0, PaintTool.owner.mainPane);
            if(!PaintTool.editBackground){
                g3.drawImage(PaintTool.owner.mainImg, 0, 0, PaintTool.owner.mainPane);
            }*/
            if (PaintTool.owner.bit > 1) {
                PaintTool.owner.mainPane.repaint(
                        (moveRect.x - (int) PaintTool.owner.bitLeft) * PaintTool.owner.bit,
                        (moveRect.y - (int) PaintTool.owner.bitTop) * PaintTool.owner.bit,
                        (moveRect.width) * PaintTool.owner.bit, (moveRect.height) * PaintTool.owner.bit);
            } else {
                PaintTool.owner.mainPane.repaint(moveRect);
            }

            viewSelection();


            // When pointer goes outside the window
           /*PointerInfo pointerInfo = MouseInfo.getPointerInfo();
           Point p = pointerInfo.getLocation();
           Rectangle r = new Rectangle(PaintTool.owner.getLocationOnScreen().x,
                   PaintTool.owner.getLocationOnScreen().y,
                   PaintTool.owner.getWidth(), PaintTool.owner.getHeight());
           if(!r.contains(p) && move==true ){
                // Drag and drop to file
                // Create drag source
                DragSource dragSource = new DragSource();
                DragGestureRecognizer dgr =
                dragSource.createDefaultDragGestureRecognizer(
                            PaintTool.owner.mainPane,
                            DnDConstants.ACTION_COPY_OR_MOVE,
                            dselection);
           }*/
        } else {
            if (srcRect != null && x == srcRect.x + srcRect.width && y == srcRect.y + srcRect.height) {
                return false;
            }
            setSelection(x, y);
        }
        return true;
    }

    @Override
    public void clear() {
        move = false;
        moveRect = null;
        srcRect = null;
    }

    @Override
    public void end() {
        if (move) {
            Graphics2D g = (Graphics2D) PaintTool.owner.getSurface().getGraphics();
            BufferedImage newimg = MyPanel.makeBlendImage(PaintTool.owner.redoBuf);
            g.drawImage(newimg, moveRect.x, moveRect.y, moveRect.x + moveRect.width, moveRect.y + moveRect.height,
                    0, 0, srcRect.width, srcRect.height, null);

            // When pasting, the buffer size changes, so make a new one
            PaintTool.owner.redoBuf = new BufferedImage(PaintTool.owner.mainImg.getWidth(), PaintTool.owner.mainImg.getHeight(),
                    BufferedImage.TYPE_INT_ARGB);
        }

        PaintTool.owner.blendMode = 0;
        PaintTool.owner.blendLevel = 100;
        if (PaintBlendDialog.dialog != null) {
            PaintBlendDialog.dialog.dispose();
        }

        PaintTool.owner.mainPane.repaint();
    }

    @Override
    public BufferedImage getSelectedSurface(PCARDFrame owner) {
        if (move && srcRect.width > 0) {
            return owner.redoBuf;
        }
        return null;
    }

    @Override
    public Rectangle getSelectedRect() {
        return srcRect;
    }

    @Override
    public Rectangle getMoveRect() {
        Rectangle rect = (Rectangle) moveRect.clone();
        rect.x += srcRect.x;
        rect.y += srcRect.y;
        return rect;
    }

    @Override
    public boolean isMove() {
        return move;
    }
}

/*class DragSelection implements Transferable, DragGestureListener
{

    @Override
    public void dragGestureRecognized(DragGestureEvent e) {
        // If Copy/Move action, start dragging
        if((e.getDragAction()|DnDConstants.ACTION_COPY_OR_MOVE)!= 0) {
            try{
                e.startDrag(DragSource.DefaultCopyDrop, this, null);
            }catch(Exception e1){
                e1.printStackTrace();
            }
        }
    }

    @Override
    public Object getTransferData(DataFlavor e) {
        ArrayList<File> filelist = new ArrayList<File>();
        File file = new File("tmp.png");
        try {
            if(!ImageIO.write(PaintTool.owner.redoBuf, "PNG", file)){
                System.out.println("Image output error");
            }
            else{
                filelist.add(file);
            }
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        return filelist;
    }

    @Override
    public DataFlavor[] getTransferDataFlavors() {
        return new DataFlavor[] {DataFlavor.javaFileListFlavor};
    }

    @Override
    public boolean isDataFlavorSupported(DataFlavor flavor) {
        return flavor.equals(DataFlavor.javaFileListFlavor);
    }
}*/

// -------------------
// Lasso
// -------------------
class LassoTool implements ToolSelectInterface {
    // srcbits: Selected area
    // movePoint: Current floating movement amount
    // redoBuf: Floating selection of selected area (0,0,width,height)
    // move: If true, there is a floating selection

    ArrayList<Point> srcPoints = new ArrayList<>();
    BufferedImage srcbits;
    Point movePoint;
    boolean move = false;
    boolean shift;
    int shiftx, shifty;

    @Override
    public String getName() {
        return "Lasso";
    }

    private void strokeDraw() {
        if (move == false) {
            strokeDraw(srcPoints);
            return;
        }
        strokebitsDraw();
    }

    private void strokebitsDraw() {
        if (srcbits == null) return;

        Graphics2D g3 = (Graphics2D) PaintTool.owner.mainPane.getGraphics();

        if (PaintTool.owner.bit > 1) {
            AffineTransform af = new AffineTransform();
            af.translate(-((int) PaintTool.owner.bitLeft) * PaintTool.owner.bit + (double) PaintTool.owner.bit / 2, -((int) PaintTool.owner.bitTop) * PaintTool.owner.bit + (double) PaintTool.owner.bit / 2);
            af.scale(PaintTool.owner.bit, PaintTool.owner.bit);
            g3.transform(af);
        }

        DataBuffer srcbuf = srcbits.getRaster().getDataBuffer();
        int width = srcbits.getWidth();
        int height = srcbits.getHeight();
        int i = ((int) System.currentTimeMillis() / 100) % 8;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int c = srcbuf.getElem(0, x + y * width);
                if ((c & 0xFF000000) != 0) {
                    // Included in floating area and one of the surrounding 4 is outside the area
                    int c1 = 0x00FFFFFF;
                    if (x >= 1) c1 = srcbuf.getElem(0, x - 1 + y * width);
                    int c2 = 0x00FFFFFF;
                    if (x < width - 1) c2 = srcbuf.getElem(0, x + 1 + y * width);
                    int c3 = 0x00FFFFFF;
                    if (y >= 1) c3 = srcbuf.getElem(0, x + (y - 1) * width);
                    int c4 = 0x00FFFFFF;
                    if (y < height - 1) c4 = srcbuf.getElem(0, x + (y + 1) * width);
                    if ((c1 & 0xFF000000) == 0 || (c2 & 0xFF000000) == 0 || (c3 & 0xFF000000) == 0 || (c4 & 0xFF000000) == 0) {
                        if ((x + y + i) % 4 != 0) continue;
                        if ((x + y + i) % 8 < 4) g3.setColor(Color.WHITE);
                        else g3.setColor(Color.BLACK);
                        g3.drawLine(x + movePoint.x, y + movePoint.y, x + movePoint.x, y + movePoint.y);
                    }
                }
            }
        }
    }

    private static void strokeDraw(ArrayList<Point> list) {
        Graphics2D g3 = (Graphics2D) PaintTool.owner.mainPane.getGraphics();

        if (PaintTool.owner.bit > 1) {
            AffineTransform af = new AffineTransform();
            af.translate(-((int) PaintTool.owner.bitLeft) * 8 + (double) PaintTool.owner.bit / 2, -((int) PaintTool.owner.bitTop) * 8 + (double) PaintTool.owner.bit / 2);
            af.scale(PaintTool.owner.bit, PaintTool.owner.bit);
            g3.transform(af);
        }

        BasicStroke bs = new BasicStroke(0.1f);
        float[] dash = {4.0f, 2.0f};
        int i = (((int) System.currentTimeMillis() / 100) & 0xFFFF) % 6;
        BasicStroke bs2 = new BasicStroke(0.1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dash, i);
        for (int j = 0; j < list.size() - 1; j++) {
            Point p1 = list.get(j);
            Point p2 = list.get(j + 1);

            g3.setStroke(bs);
            g3.setColor(Color.WHITE);
            g3.drawLine(p1.x, p1.y, p2.x, p2.y);
            g3.setStroke(bs2);
            g3.setColor(Color.BLACK);
            g3.drawLine(p1.x, p1.y, p2.x, p2.y);
        }
    }

    private void makesrcbits() {
        int len = srcPoints.size();
        int[] xPoints = new int[len];
        int[] yPoints = new int[len];
        for (int j = 0; j < len; j++) {
            Point p = srcPoints.get(j);
            xPoints[j] = p.x;
            yPoints[j] = p.y;
        }

        // Prepare mask buffer
        BufferedImage srcsrcbits = new BufferedImage(PaintTool.owner.mainImg.getWidth(), PaintTool.owner.mainImg.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = (Graphics2D) srcsrcbits.getGraphics();
        g.setColor(new Color(255, 255, 255));
        // g.setComposite(AlphaComposite.getInstance(AlphaComposite.CLEAR, 0.0f));
        g.fillRect(0, 0, srcsrcbits.getWidth(), srcsrcbits.getHeight());

        // Draw polygon
        g = (Graphics2D) srcsrcbits.getGraphics();
        g.setColor(Color.BLACK);
        g.fillPolygon(xPoints, yPoints, srcPoints.size());

        // Draw surface() contained in that shape to srcsrcbits
        DataBuffer mskbuf = srcsrcbits.getRaster().getDataBuffer();
        DataBuffer imgbuf = PaintTool.owner.getSurface().getRaster().getDataBuffer();
        int width = srcsrcbits.getWidth();
        int height = srcsrcbits.getHeight();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (0xFFFFFFFF != mskbuf.getElem(x + y * width)) {
                    int v = imgbuf.getElem(x + y * width);
                    if ((v & 0xFF000000) == 0) v = 0xFFFFFFFF;
                    mskbuf.setElem(x + y * width, v);
                }
            }
        }

        //
        srcbits = new BufferedImage(PaintTool.owner.mainImg.getWidth(), PaintTool.owner.mainImg.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D srcg = (Graphics2D) srcbits.getGraphics();
        srcg.setColor(new Color(255, 255, 255));
        srcg.setComposite(AlphaComposite.getInstance(AlphaComposite.CLEAR, 0.0f));
        srcg.fillRect(0, 0, srcbits.getWidth(), srcbits.getHeight());
        srcg = (Graphics2D) srcbits.getGraphics();
        srcg.drawImage(srcsrcbits, 0, 0, null);

        // Transparentize surrounding white
        pointList = new ArrayList<>();

        for (int x = 0; x < width; x++) {
            seedfillH(srcsrcbits, srcbits, x, 0, 0xFFFFFFFF, 0x00FFFFFF);
            seedfillH(srcsrcbits, srcbits, x, height - 1, 0xFFFFFFFF, 0x00FFFFFF);
        }
        for (int y = 0; y < height; y++) {
            seedfillH(srcsrcbits, srcbits, 0, y, 0xFFFFFFFF, 0x00FFFFFF);
            seedfillH(srcsrcbits, srcbits, width - 1, y, 0xFFFFFFFF, 0x00FFFFFF);
        }

        while (!pointList.isEmpty()) {
            Point p = pointList.get(0);
            pointList.remove(0);
            seedfillH(srcsrcbits, srcbits, p.x, p.y, 0xFFFFFFFF, 0x00FFFFFF);
        }

        // Clear
        srcPoints = new ArrayList<>();
    }

    ArrayList<Point> pointList;

    private void seedfillH(BufferedImage surface, BufferedImage newSurface, int px, int py, int srcColor, int newColor) {
        DataBuffer buffer = surface.getRaster().getDataBuffer();
        DataBuffer newBuffer = newSurface.getRaster().getDataBuffer();
        int width = surface.getWidth();
        int height = surface.getHeight();

        // Check left
        int lx;
        for (lx = 0; px + lx >= 0; lx--) {
            // System.out.println("<srcColor:"+srcColor);
            // System.out.println("<getElem("+(px+lx)+","+py+"):"+buffer.getElem(0, px+lx+py*width));
            if (buffer.getElem(0, px + lx + py * width) != srcColor) {
                break;
            }
        }
        lx++;

        // Check right
        int rx;
        for (rx = 0; px + rx < width; rx++) {
            // System.out.println(">srcColor:"+srcColor);
            // System.out.println(">getElem("+(px+rx)+","+py+"):"+buffer.getElem(0, px+lx+py*width));
            if (buffer.getElem(0, px + rx + py * width) != srcColor) {
                break;
            }
        }
        rx--;

        // Paint that line
        for (int x = px + lx; x <= px + rx; x++) {
            if (buffer.getElem(0, x + py * width) == srcColor) {
                newBuffer.setElem(0, x + py * width, newColor);
            }
        }

        // Search for the line above
        if (py - 1 >= 0) {
            for (int x = px + lx; x <= px + rx; x++) {
                // Search for the right edge
                if (buffer.getElem(0, x + (py - 1) * width) == srcColor) {
                    if (x == px + rx ||
                            buffer.getElem(0, (x + 1) + (py - 1) * width) != srcColor) {
                        if (newBuffer.getElem(0, x + (py - 1) * width) != newColor) {
                            // Not registered, so register it
                            pointList.add(new Point(x, py - 1));
                            newBuffer.setElem(0, x + (py - 1) * width, newColor);
                        }
                    }
                }
            }
        }

        // Search for the line below
        if (py + 1 < height) {
            for (int x = px + lx; x < px + rx; x++) {
                // Search for the right edge
                if (buffer.getElem(0, x + (py + 1) * width) == srcColor) {
                    if (x + 1 == px + rx ||
                            buffer.getElem(0, (x + 1) + (py + 1) * width) != srcColor) {
                        if (newBuffer.getElem(0, x + (py + 1) * width) != newColor) {
                            // Not registered, so register it
                            pointList.add(new Point(x, py + 1));
                            newBuffer.setElem(0, x + (py + 1) * width, newColor);
                        }
                    }
                }
            }
        }
    }


    private void setSelection(int x, int y) {
        // PaintTool.owner.mainPane.paintImmediately(new Rectangle(0,0,PaintTool.owner.mainPane.getWidth(),PaintTool.owner.mainPane.getHeight()));

        if (srcPoints.isEmpty() ||
                srcPoints.get(srcPoints.size() - 1).x != x ||
                srcPoints.get(srcPoints.size() - 1).y != y) {
            srcPoints.add(new Point(x, y));
        }

        strokeDraw();

        try {
            Thread.sleep(30);
        } catch (InterruptedException e) {
        }

        strokeDraw();
    }

    void viewSelection() {
        PaintTool.owner.mainPane.repaint();

        // Display selection area dashed line
        strokeDraw();
    }

    @Override
    public void mouseUp(int x, int y) {
        if (move == false) {
            GMenuPaint.setUndo();

            makesrcbits();

            movePoint = new Point(0, 0);
            move = true;

            // Move selection area to temporary buffer
            Graphics2D g = (Graphics2D) PaintTool.owner.redoBuf.getGraphics();
            g.setColor(new Color(255, 255, 255));
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.CLEAR, 0.0f));
            g.fillRect(0, 0, PaintTool.owner.redoBuf.getWidth(), PaintTool.owner.redoBuf.getHeight());
            g.setComposite(AlphaComposite.Src);

            DataBuffer mskbuf = srcbits.getRaster().getDataBuffer();
            DataBuffer movbuf = PaintTool.owner.redoBuf.getRaster().getDataBuffer();
            DataBuffer surbuf = PaintTool.owner.getSurface().getRaster().getDataBuffer();
            int width = srcbits.getWidth();
            int height = srcbits.getHeight();
            for (int v = 0; v < height; v++) {
                for (int h = 0; h < width; h++) {
                    int c = mskbuf.getElem(0, h + v * width);
                    if ((c & 0xFF000000) != 0) {
                        movbuf.setElem(h + v * width, surbuf.getElem(0, h + v * width));
                        // Make the moved part transparent
                        if (!PaintTool.editBackground) {
                            surbuf.setElem(h + v * width, 0x00FFFFFF);
                        } else {
                            surbuf.setElem(h + v * width, 0xFFFFFFFF);
                        }
                    }
                }
            }

            // Reflect to front screen
            PaintTool.owner.mainPane.repaint();

            viewSelection();
        } else {
            mouseStillDown(x, y);
        }
    }

    @Override
    public void mouseDown(int x, int y) {
        if (move == true && srcbits != null) {
            DataBuffer mskbuf = srcbits.getRaster().getDataBuffer();
            if ((x - movePoint.x) >= 0 && (x - movePoint.x) < srcbits.getWidth() &&
                    (y - movePoint.y) >= 0 && (y - movePoint.y) < srcbits.getHeight()) {
                // Start moving selection area
                shift = (GUI.key[11] > 0);
                shiftx = x;
                shifty = y;

                int c = mskbuf.getElem(0, (x - movePoint.x) + (y - movePoint.y) * srcbits.getWidth());
                if ((c & 0xFF000000) != 0) {
                    if (GUI.key[12] > 0) {
                        Graphics2D g = (Graphics2D) PaintTool.owner.getSurface().getGraphics();
                        g.drawImage(PaintTool.owner.redoBuf, movePoint.x, movePoint.y, null);
                    }
                    return;
                }
            }
        }

        {
            // Draw floating area to actual area
            GMenuPaint.setUndo();
            end();

            // Create new selection area
            clear();

            srcPoints.add(new Point(x, y));
        }
    }

    @Override
    public boolean mouseWithin(int x, int y) {

        if (GUI.key[20] > 0 || GUI.key[21] > 0) { // BACKSPACE or DEL
            clear();
            PaintTool.owner.mainPane.repaint();
            return false;
        }

        strokeDraw();
        if (move == true && srcbits != null) {
            DataBuffer mskbuf = srcbits.getRaster().getDataBuffer();
            if ((x - movePoint.x) >= 0 && (x - movePoint.x) < srcbits.getWidth() &&
                    (y - movePoint.y) >= 0 && (y - movePoint.y) < srcbits.getHeight()) {
                int c = mskbuf.getElem(0, (x - movePoint.x) + (y - movePoint.y) * srcbits.getWidth());
                if ((c & 0xFF000000) != 0) {
                    PaintTool.owner.mainPane.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                    return false;
                }
            }
        }

        PaintTool.owner.mainPane.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));

        return false;
    }

    @Override
    public boolean mouseStillDown(int x, int y) {
        if (move == true) {
            // Move selection area

            if (shift && ((x - shiftx != 0) || (y - shifty != 0))) {
                if ((shiftx == -1) || (shifty != -1) && (Math.abs(x - shiftx) > Math.abs(y - shifty))) {
                    shiftx = -1;
                    y = shifty;
                    PaintTool.lasty[0] = shifty;
                } else if ((shifty == -1) || (shiftx != -1)) {
                    shifty = -1;
                    x = shiftx;
                    PaintTool.lastx[0] = shiftx;
                }
            }

            movePoint.x = (int) (movePoint.x + x - PaintTool.lastx[0]);
            movePoint.y = (int) (movePoint.y + y - PaintTool.lasty[0]);

            Rectangle rect = new Rectangle(movePoint.x, movePoint.y, x - (int) PaintTool.lastx[0], y - (int) PaintTool.lastx[0]);
            if (rect.width < 0) {
                rect.x += rect.width;
                rect.width = -rect.width;
            }
            if (rect.height < 0) {
                rect.y += rect.height;
                rect.height = -rect.height;
            }

            if (PaintTool.owner.bit > 1) {
                PaintTool.owner.mainPane.repaint(
                        (rect.x - (int) PaintTool.owner.bitLeft) * PaintTool.owner.bit,
                        (rect.y - (int) PaintTool.owner.bitTop) * PaintTool.owner.bit,
                        (rect.width) * PaintTool.owner.bit, (rect.height) * PaintTool.owner.bit);
            } else {
                PaintTool.owner.mainPane.repaint(rect);
            }

            viewSelection();
        } else {
            // Expand selection area
            setSelection(x, y);
        }
        return true;
    }

    @Override
    public void clear() {
        srcPoints = new ArrayList<>();
        srcbits = null;
        move = false;
    }

    @Override
    public void end() {
        srcbits = null;
        if (move) {
            Graphics2D g = (Graphics2D) PaintTool.owner.getSurface().getGraphics();
            BufferedImage newimg = MyPanel.makeBlendImage(PaintTool.owner.redoBuf);
            g.drawImage(newimg, movePoint.x, movePoint.y, null);

            // When rotated, the buffer size changes, so make a new one
            PaintTool.owner.redoBuf = new BufferedImage(PaintTool.owner.mainImg.getWidth(), PaintTool.owner.mainImg.getHeight(), BufferedImage.TYPE_INT_ARGB);
        }

        PaintTool.owner.blendMode = 0;
        PaintTool.owner.blendLevel = 100;
        if (PaintBlendDialog.dialog != null) {
            PaintBlendDialog.dialog.dispose();
        }

        PaintTool.owner.mainPane.repaint();
    }

    @Override
    public BufferedImage getSelectedSurface(PCARDFrame owner) {
        if (move) {
            return owner.redoBuf;
        }
        return null;
    }

    @Override
    public Rectangle getSelectedRect() {
        return makeSelectedRect(srcbits);
    }

    public static Rectangle makeSelectedRect(BufferedImage in_srcbits) {
        int width = PaintTool.owner.redoBuf.getWidth();
        int height = PaintTool.owner.redoBuf.getHeight();
        int left = width;
        int top = height;
        int right = 0;
        int bottom = 0;

        DataBuffer mskbuf = in_srcbits.getRaster().getDataBuffer();
        for (int v = 0; v < height; v++) {
            for (int h = 0; h < width; h++) {
                int c = mskbuf.getElem(0, h + v * width);
                if ((c & 0xFF000000) != 0) {
                    if (left > h) left = h;
                    if (right < h) right = h;
                    if (top > v) top = v;
                    if (bottom < v) bottom = v;
                }
            }
        }

        Rectangle srcRect = new Rectangle(left, top, right - left + 1, bottom - top + 1);

        return srcRect;
    }

    @Override
    public Rectangle getMoveRect() {
        Rectangle rect = makeSelectedRect(srcbits);
        rect.x += movePoint.x;
        rect.y += movePoint.y;
        return rect;
    }

    @Override
    public boolean isMove() {
        return move;
    }
}


// -------------------
// Smart Select
// -------------------
class SmartSelectTool implements ToolSelectInterface {
    // srcbits: Selected area
    // movePoint: Current floating movement amount
    // redoBuf: Floating selection of selected area (0,0,width,height)
    // move: If true, there is a floating selection

    // cmd+click outside selection area adds to selection area

    ArrayList<Point> pointList; // For seedFill
    ArrayList<Point> srcPoints = new ArrayList<>();
    BufferedImage srcbits;
    Point movePoint;
    Rectangle tmpRect;
    boolean move = false;
    boolean shift;
    int shiftx, shifty;

    @Override
    public String getName() {
        return "MagicWand";
    }

    private void borderDraw() {
        if (srcbits == null) return;

        /* // Reflect transparency and draw line segment on screen
        Graphics2D g4 = (Graphics2D) PaintTool.owner.redoBuf.getGraphics();
        g4.drawImage(PaintTool.owner.bgImg, 0, 0, PaintTool.owner.mainPane);
        g4.drawImage(PaintTool.owner.getSurface(), 0, 0, PaintTool.owner.mainPane);
        {
            g4.setComposite( AlphaComposite.getInstance(AlphaComposite.SRC_OVER,PaintTool.alpha/100.0F) );
        }
        g4.drawImage(srcbits, movePoint.x, movePoint.y, PaintTool.owner.mainPane);

        // Draw source image of selection area
        Graphics2D g6 = (Graphics2D) PaintTool.owner.mainPane.getGraphics();

        // Zoomed display
        if(PaintTool.owner.bit>1){
            AffineTransform af = new AffineTransform();
            af.translate(-((int)PaintTool.owner.bitLeft)*PaintTool.owner.bit, -((int)PaintTool.owner.bitTop)*PaintTool.owner.bit);
            af.scale(PaintTool.owner.bit, PaintTool.owner.bit);
            g6.transform(af);
        }

        g6.drawImage(PaintTool.owner.redoBuf, 0, 0, PaintTool.owner.mainPane);

        // Draw border when zoomed display
        if(PaintTool.owner.bit>2){
            Graphics g5 = PaintTool.owner.mainPane.getGraphics();
            MyPanel.bordersDraw(g5, PaintTool.owner.bit, PaintTool.owner.mainPane.getWidth(), PaintTool.owner.mainPane.getHeight());
        }*/

        // Draw background and floating parts
        // PaintTool.owner.mainPane.repaint();

        // Dashed line drawing is directly on screen
        Graphics2D g3 = (Graphics2D) PaintTool.owner.mainPane.getGraphics();

        if (PaintTool.owner.bit > 1) {
            AffineTransform af = new AffineTransform();
            af.translate(-((int) PaintTool.owner.bitLeft) * PaintTool.owner.bit + (double) PaintTool.owner.bit / 2, -((int) PaintTool.owner.bitTop) * PaintTool.owner.bit + (double) PaintTool.owner.bit / 2);
            af.scale(PaintTool.owner.bit, PaintTool.owner.bit);
            g3.transform(af);
        }

        // Draw selection dashed line
        if (srcbits == null) return;
        WritableRaster raster = srcbits.getRaster();
        if (raster == null) return;
        DataBuffer srcbuf = raster.getDataBuffer();
        int width = srcbits.getWidth();
        int height = srcbits.getHeight();
        int i = ((int) System.currentTimeMillis() / 200) % 8;
        // Color color = Color.WHITE;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int c = srcbuf.getElem(0, x + y * width);
                if ((c & 0xFF000000) != 0) {
                    // Included in floating area and one of the surrounding 4 is outside the area
                    int c1 = 0x00FFFFFF;
                    if (x >= 1) c1 = srcbuf.getElem(0, x - 1 + y * width);
                    int c2 = 0x00FFFFFF;
                    if (x < width - 1) c2 = srcbuf.getElem(0, x + 1 + y * width);
                    int c3 = 0x00FFFFFF;
                    if (y >= 1) c3 = srcbuf.getElem(0, x + (y - 1) * width);
                    int c4 = 0x00FFFFFF;
                    if (y < height - 1) c4 = srcbuf.getElem(0, x + (y + 1) * width);
                    if ((c1 & 0xFF000000) == 0 || (c2 & 0xFF000000) == 0 || (c3 & 0xFF000000) == 0 || (c4 & 0xFF000000) == 0) {
                        if ((x + y + i) % 4 == 0) {
                            if ((x + y + i) % 8 == 0) g3.setColor(Color.WHITE);
                            else g3.setColor(Color.BLACK);
                            g3.drawLine(x + movePoint.x, y + movePoint.y, x + movePoint.x, y + movePoint.y);
                        } else {
                            /*if(!isNear(c,color,3)){
                                color = new Color((c>>16)&0xFF, (c>>8)&0xFF, (c>>0)&0xFF);
                            }
                            g3.setColor(color);*/
                        }
                    } else {
                        /*if(!isNear(c,color,3)){
                            color = new Color((c>>16)&0xFF, (c>>8)&0xFF, (c>>0)&0xFF);
                        }
                        g3.setColor(color);*/
                    }
                    // g3.drawLine(x+movePoint.x, y+movePoint.y, x+movePoint.x, y+movePoint.y);
                }
            }
        }
    }

    private static boolean isNear(int argb, Color color, int near) {
        if (Math.abs(color.getRed() - ((argb >> 16) & 0xFF)) > near) {
            return false;
        }
        if (Math.abs(color.getGreen() - ((argb >> 8) & 0xFF)) > near) {
            return false;
        }
        if (Math.abs(color.getBlue() - ((argb >> 0) & 0xFF)) > near) {
            return false;
        }
        if (Math.abs(color.getAlpha() - ((argb >> 24) & 0xFF)) > near) {
            return false;
        }
        return true;
    }

    private void seedfillH(BufferedImage surface, BufferedImage newSurface, int px, int py, ArrayList<Color> srcColors) {
        DataBuffer buffer = surface.getRaster().getDataBuffer();
        DataBuffer newBuffer = newSurface.getRaster().getDataBuffer();
        int width = surface.getWidth();
        int height = surface.getHeight();
        int near = PaintTool.smartSelectPercent * 256 / 100;

        // Check left
        int lx;
        for (lx = 0; px + lx >= 0; lx--) {
            // System.out.println("<srcColor:"+srcColor);
            // System.out.println("<getElem("+(px+lx)+","+py+"):"+buffer.getElem(0, px+lx+py*width));
            boolean isMatch = false;
            for (Color srcColor : srcColors) {
                if (isNear(buffer.getElem(0, px + lx + py * width), srcColor, near)) {
                    isMatch = true;
                    break;
                }
            }
            if (!isMatch) break;
        }
        lx++;

        // Check right
        int rx;
        for (rx = 0; px + rx < width; rx++) {
            // System.out.println(">srcColor:"+srcColor);
            // System.out.println(">getElem("+(px+rx)+","+py+"):"+buffer.getElem(0, px+lx+py*width));
            boolean isMatch = false;
            for (Color srcColor : srcColors) {
                if (isNear(buffer.getElem(0, px + rx + py * width), srcColor, near)) {
                    isMatch = true;
                    break;
                }
            }
            if (!isMatch) break;
        }
        rx--;

        // Paint that line
        for (int x = px + lx; x <= px + rx; x++) {
            int c = buffer.getElem(0, x + py * width);
            if (c == 0) c = 1;
            newBuffer.setElem(0, x + py * width, c);
        }

        // Search for the line above
        if (py - 1 >= 0) {
            for (int x = px + lx; x <= px + rx; x++) {
                // Search for the right edge
                boolean isMatch = false;
                for (Color color : srcColors) {
                    if (isNear(buffer.getElem(0, x + (py - 1) * width), color, near)) {
                        isMatch = true;
                        break;
                    }
                }
                if (isMatch) {
                    boolean isMatch2 = false;
                    if (x < px + rx) {
                        for (Color srcColor : srcColors) {
                            if (isNear(buffer.getElem(0, (x + 1) + (py - 1) * width), srcColor, near)) {
                                isMatch2 = true;
                                break;
                            }
                        }
                    }
                    if (x == px + rx || !isMatch2) {
                        if (newBuffer.getElem(0, x + (py - 1) * width) == 0x00000000) {
                            // Not registered, so register it
                            pointList.add(new Point(x, py - 1));
                            int c = buffer.getElem(0, x + (py - 1) * width);
                            if (c == 0) c = 1;
                            newBuffer.setElem(0, x + (py - 1) * width, c);
                        }
                    }
                }
            }
        }

        // Search for the line below
        if (py + 1 < height) {
            for (int x = px + lx; x <= px + rx; x++) {
                // Search for the right edge
                boolean isMatch = false;
                for (Color color : srcColors) {
                    if (isNear(buffer.getElem(0, x + (py + 1) * width), color, near)) {
                        isMatch = true;
                        break;
                    }
                }
                if (isMatch) {
                    boolean isMatch2 = false;
                    if (x < px + rx) {
                        for (Color srcColor : srcColors) {
                            if (isNear(buffer.getElem(0, (x + 1) + (py + 1) * width), srcColor, near)) {
                                isMatch2 = true;
                                break;
                            }
                        }
                    }
                    if (x == px + rx || !isMatch2) {
                        if (newBuffer.getElem(0, x + (py + 1) * width) == 0x00000000) {
                            // Not registered, so register it
                            pointList.add(new Point(x, py + 1));
                            int c = buffer.getElem(0, x + (py + 1) * width);
                            if (c == 0) c = 1;
                            newBuffer.setElem(0, x + (py + 1) * width, c);
                        }
                    }
                }
            }
        }
    }

    private void makesrcbits() {
        boolean allScreen = false;
        if (GUI.key[12] > 0) { // opt
            allScreen = true;
        }

        // Takes time, so change cursor to clock
        PaintTool.owner.mainPane.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        // Enumerate included colors
        ArrayList<Color> srcColors = new ArrayList<>();
        int width = PaintTool.owner.getSurface().getWidth();
        int near = PaintTool.smartSelectPercent * 256 / 100;
        if (srcPoints.size() > 512) {
            near += 3;
        }
        for (Point point : srcPoints) {
            int d = PaintTool.owner.getSurface().getRaster().getDataBuffer().
                    getElem(point.x + point.y * width);
            Color c = new Color((d >> 16) & 0xFF, (d >> 8) & 0xFF, (d >> 0) & 0xFF, (d >> 24) & 0xFF);
            boolean isFound = false;
            for (int i = srcColors.size() - 1; i >= 0; i--) {
                if (isNear(d, srcColors.get(i), near / 2)) {
                    isFound = true;
                    break;
                }
            }
            if (!isFound) srcColors.add(c);
        }

        // Reset fill candidate list
        pointList = new ArrayList<>();

        if (allScreen) {
            // Search for all similar colors
            DataBuffer buffer = PaintTool.owner.getSurface().getRaster().getDataBuffer();
            DataBuffer newBuffer = srcbits.getRaster().getDataBuffer();
            int width1 = PaintTool.owner.getSurface().getWidth();
            int height1 = PaintTool.owner.getSurface().getHeight();
            int near1 = PaintTool.smartSelectPercent * 256 / 100;
            // Color color = Color.WHITE;
            int lasti = 0;
            for (int y = 0; y < height1; y++) {
                for (int x = 0; x < width1; x++) {
                    boolean isMatch = false;
                    if (isNear(buffer.getElem((x) + y * width), srcColors.get(lasti), near1)) {
                        isMatch = true;
                    } else {
                        for (int i = 0; i < srcColors.size(); i++) {
                            if (isNear(buffer.getElem((x) + y * width), srcColors.get(i), near1)) {
                                isMatch = true;
                                lasti = i;
                                break;
                            }
                        }
                    }
                    if (isMatch) {
                        int c = buffer.getElem(0, x + y * width);
                        newBuffer.setElem(0, x + y * width, c);
                    }
                }
            }
        } else {
            // Search for adjacent similar colors
            for (Point srcPoint : srcPoints) {
                seedfillH(PaintTool.owner.getSurface(), srcbits,
                        srcPoint.x, srcPoint.y, srcColors);

                while (!pointList.isEmpty()) {
                    Point p = pointList.get(0);
                    pointList.remove(0);
                    seedfillH(PaintTool.owner.getSurface(), srcbits,
                            p.x, p.y, srcColors);
                }
            }
        }

        // Color list is no longer needed, so clear it
        srcPoints = new ArrayList<>();

        // Restore cursor
        TBCursor.changeCursor(PaintTool.owner);
    }

    private void addSelection(int x, int y) {
        boolean isFound = false;
        if (x < 0 || y < 0 || x >= PaintTool.owner.mainPane.getWidth() || y >= PaintTool.owner.mainPane.getHeight()) {
            return;
        }
        for (Point srcPoint : srcPoints) {
            if (x == srcPoint.x && y == srcPoint.y) {
                isFound = true;
                break;
            }

        }
        if (!isFound) {
            srcPoints.add(new Point(x, y));

            Graphics2D g3 = (Graphics2D) PaintTool.owner.mainPane.getGraphics();
            if (PaintTool.owner.bit > 1) {
                AffineTransform af = new AffineTransform();
                af.translate(-((int) PaintTool.owner.bitLeft) * PaintTool.owner.bit + (double) PaintTool.owner.bit / 2, -((int) PaintTool.owner.bitTop) * PaintTool.owner.bit + (double) PaintTool.owner.bit / 2);
                af.scale(PaintTool.owner.bit, PaintTool.owner.bit);
                g3.transform(af);
            }
            g3.setColor(Color.BLACK);
            g3.drawLine(x, y, x, y);
        }
    }

    void viewSelection() {
        // PaintTool.owner.mainPane.repaint();

        // Display selection area
        borderDraw();
    }

    @Override
    public void mouseUp(int x, int y) {
        if (move == false && srcbits != null) {
            GMenuPaint.setUndo();

            makesrcbits();

            movePoint = new Point(0, 0);
            move = true;

            // Move selection area to temporary buffer
            Graphics2D g = (Graphics2D) PaintTool.owner.redoBuf.getGraphics();
            g.setColor(new Color(255, 255, 255));
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.CLEAR, 0.0f));
            g.fillRect(0, 0, PaintTool.owner.redoBuf.getWidth(), PaintTool.owner.redoBuf.getHeight());
            g.setComposite(AlphaComposite.Src);

            DataBuffer mskbuf = srcbits.getRaster().getDataBuffer();
            DataBuffer movbuf = PaintTool.owner.redoBuf.getRaster().getDataBuffer();
            DataBuffer surbuf = PaintTool.owner.getSurface().getRaster().getDataBuffer();
            int width = srcbits.getWidth();
            int height = srcbits.getHeight();
            for (int v = 0; v < height; v++) {
                for (int h = 0; h < width; h++) {
                    int c = mskbuf.getElem(0, h + v * width);
                    if ((c & 0xFF000000) != 0) {
                        movbuf.setElem(h + v * width, surbuf.getElem(0, h + v * width));
                        // Make the moved part transparent
                        if (!PaintTool.editBackground) {
                            surbuf.setElem(h + v * width, 0x00FFFFFF);
                        } else {
                            surbuf.setElem(h + v * width, 0xFFFFFFFF);
                        }
                    }
                }
            }

            // Reflect to front screen
            PaintTool.owner.mainPane.repaint();

            viewSelection();
        } else {
            mouseStillDown(x, y);
        }
    }

    @Override
    public void mouseDown(int x, int y) {
        if (move == true) {
            DataBuffer mskbuf = srcbits.getRaster().getDataBuffer();
            if ((x - movePoint.x) >= 0 && (x - movePoint.x) < PaintTool.owner.redoBuf.getWidth() &&
                    (y - movePoint.y) >= 0 && (y - movePoint.y) < PaintTool.owner.redoBuf.getHeight()) {
                // Start moving selection area
                shift = (GUI.key[11] > 0);
                shiftx = x;
                shifty = y;

                int c = mskbuf.getElem(0, (x - movePoint.x) + (y - movePoint.y) * PaintTool.owner.redoBuf.getWidth());
                if ((c & 0xFF000000) != 0) {
                    if (GUI.key[12] > 0) {
                        Graphics2D g = (Graphics2D) PaintTool.owner.getSurface().getGraphics();
                        g.drawImage(PaintTool.owner.redoBuf, movePoint.x, movePoint.y, null);
                    }
                    return;
                }
            }
        }

        {
            if (GUI.key[14] > 1 && srcbits != null && srcbits.getWidth() > 1) { // cmd
                this_end();
                move = false;
                tmpRect = null;
            } else if (move) {
                // Draw floating area to actual area
                GMenuPaint.setUndo();
                this_end();

                // Create new selection area
                clear();
            } else {
                // Create new selection area
                clear();

                // Prepare mask buffer
                srcbits = new BufferedImage(PaintTool.owner.mainImg.getWidth(), PaintTool.owner.mainImg.getHeight(), BufferedImage.TYPE_INT_ARGB);
                Graphics2D g = (Graphics2D) srcbits.getGraphics();
                g.setColor(new Color(255, 255, 255));
                g.setComposite(AlphaComposite.getInstance(AlphaComposite.CLEAR, 0.0f));
                g.fillRect(0, 0, srcbits.getWidth(), srcbits.getHeight());
            }

            addSelection(x, y);
        }
    }

    @Override
    public boolean mouseWithin(int x, int y) {

        if (GUI.key[20] > 0 || GUI.key[21] > 0) { // BACKSPACE or DEL
            clear();
            PaintTool.owner.mainPane.repaint();
            return false;
        }

        boolean isWithin = false;
        if (move == true && srcbits != null) {
            DataBuffer mskbuf = srcbits.getRaster().getDataBuffer();
            if ((x - movePoint.x) >= 0 && (x - movePoint.x) < srcbits.getWidth() &&
                    (y - movePoint.y) >= 0 && (y - movePoint.y) < srcbits.getHeight()) {
                int c = mskbuf.getElem(0, (x - movePoint.x) + (y - movePoint.y) * srcbits.getWidth());
                if ((c & 0xFF000000) != 0) {
                    PaintTool.owner.mainPane.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                    isWithin = true;
                }
            }
        }

        if (!isWithin) {
            PaintTool.owner.mainPane.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
        }

        viewSelection();

        return false;
    }

    @Override
    public boolean mouseStillDown(int x, int y) {
        if (move == true) {
            // Move selection area

            if (shift && ((x - shiftx != 0) || (y - shifty != 0))) {
                if ((shiftx == -1) || (shifty != -1) && (Math.abs(x - shiftx) - 1 > Math.abs(y - shifty))) {
                    shiftx = -1;
                    y = shifty;
                    PaintTool.lasty[0] = shifty;
                } else if ((shifty == -1) || (shiftx != -1)) {
                    shifty = -1;
                    x = shiftx;
                    PaintTool.lastx[0] = shiftx;
                }
            }

            movePoint.x = (int) (movePoint.x + x - PaintTool.lastx[0]);
            movePoint.y = (int) (movePoint.y + y - PaintTool.lasty[0]);

            /*if(tmpRect==null){
                tmpRect = getSelectedRect();
            }
            Rectangle rect = new Rectangle(movePoint.x-(int)Math.abs(x - PaintTool.lastX[0])-1,
                    movePoint.y-(int)Math.abs(y - PaintTool.lastY[0])-1,
                    (int)Math.abs(x - PaintTool.lastX[0])*2+2,
                    (int)Math.abs(y - PaintTool.lastY[0])*3+2);
            rect.width+=tmpRect.width;
            rect.height+=tmpRect.height;

            if(PaintTool.owner.bit>1){
                PaintTool.owner.mainPane.repaint(
                        (rect.x-(int)PaintTool.owner.bitLeft)*PaintTool.owner.bit,
                        (rect.y-(int)PaintTool.owner.bitTop)*PaintTool.owner.bit,
                        (rect.width)*PaintTool.owner.bit, (rect.height)*PaintTool.owner.bit);
            }else{
                PaintTool.owner.mainPane.repaint(rect);
            }*/
            PaintTool.owner.mainPane.repaint();

            viewSelection();
        } else if (srcbits != null) {
            // Expand selection color
            for (int i = 0; i < 8; i++) {
                addSelection((int) ((PaintTool.lastx[0] * i + x * (8 - i)) / 8), (int) ((PaintTool.lasty[0] * i + y * (8 - i)) / 8));
            }
            // addSelection(x,y);
        }
        return true;
    }

    @Override
    public void clear() {
        srcbits = null; // new BufferedImage(1,1,BufferedImage.TYPE_INT_ARGB);
        srcPoints = new ArrayList<>();
        move = false;
        tmpRect = null;
    }

    @Override
    public void end() {
        this_end();
        srcbits = null;
    }

    private void this_end() {
        if (move) {
            Graphics2D g = (Graphics2D) PaintTool.owner.getSurface().getGraphics();
            BufferedImage newimg = MyPanel.makeBlendImage(PaintTool.owner.redoBuf);
            g.drawImage(newimg, movePoint.x, movePoint.y, null);

            // When rotated, the buffer size changes, so make a new one
            PaintTool.owner.redoBuf = new BufferedImage(PaintTool.owner.mainImg.getWidth(), PaintTool.owner.mainImg.getHeight(), BufferedImage.TYPE_INT_ARGB);
            move = false;
        }

        PaintTool.owner.blendMode = 0;
        PaintTool.owner.blendLevel = 100;
        if (PaintBlendDialog.dialog != null) {
            PaintBlendDialog.dialog.dispose();
        }

        PaintTool.owner.mainPane.repaint();
    }

    @Override
    public BufferedImage getSelectedSurface(PCARDFrame owner) {
        if (move) {
            return /*srcbits; // PaintTool.*/owner.redoBuf;
        }
        return null;
    }

    @Override
    public Rectangle getSelectedRect() {
        return LassoTool.makeSelectedRect(srcbits);
    }

    @Override
    public Rectangle getMoveRect() {
        Rectangle rect = LassoTool.makeSelectedRect(srcbits);
        rect.x += movePoint.x;
        rect.y += movePoint.y;
        return rect;
    }

    @Override
    public boolean isMove() {
        return move;
    }
}


// -------------------
// Text
// -------------------
class TypeTool implements ToolInterface {

    MyTextArea area;
    Rectangle typeRect;
    boolean type = false;

    @Override
    public String getName() {
        return "Type";
    }

    void viewRect() {
        // Draw partial temporary buffer
        Graphics2D g4 = (Graphics2D) PaintTool.owner.mainPane.getGraphics();
        g4.drawImage(PaintTool.owner.redoBuf, typeRect.x, typeRect.y, typeRect.x + typeRect.width, typeRect.y + typeRect.height,
                0, 0, typeRect.width, typeRect.height, PaintTool.owner.mainPane);

        // Display selection area
        // strokeDraw();
    }

    @Override
    public void mouseUp(int x, int y) {
        if (type == false) {
            GMenuPaint.setUndo();

            // setRect(x,y);

            type = true;

            // Clear temporary buffer
            Graphics2D g = (Graphics2D) PaintTool.owner.redoBuf.getGraphics();
            g.setColor(new Color(255, 255, 255));
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.CLEAR, 0.0f));
            g.fillRect(0, 0, PaintTool.owner.redoBuf.getWidth(), PaintTool.owner.redoBuf.getHeight());

            Rectangle rect = (Rectangle) typeRect.clone();
            if (PaintTool.owner.bit > 1) {
                rect.x = (int) (rect.x - PaintTool.owner.bitLeft) * PaintTool.owner.bit;
                rect.y = (int) (rect.y - PaintTool.owner.bitTop) * PaintTool.owner.bit;
                rect.width *= PaintTool.owner.bit;
                rect.height *= PaintTool.owner.bit;
            }

            area = new MyTextArea("");
            area.fldData = new OField(null, 0);
            area.setBounds(rect);
            area.setOpaque(false);
            area.fldData.textFont = PCARD.pc.textFont;
            area.fldData.textSize = Math.min(512, PCARD.pc.textSize * PaintTool.owner.bit);
            area.fldData.textStyle = PCARD.pc.textStyle;
            // area.fldData.textAlign = PCARD.pc.textAlign;
            area.setFont(new Font(PCARD.pc.textFont, PCARD.pc.textStyle, area.fldData.textSize));
            area.setForeground(PaintTool.owner.fore.color);
            area.getDocument().addDocumentListener(new TypeToolListener());

            PaintTool.owner.mainPane.add(area);
            area.requestFocus();

            viewRect();
        } else {
            mouseStillDown(x, y);
        }
    }

    @Override
    public void mouseDown(int x, int y) {
        if (type == true && typeRect.contains(new Point(x, y))) {

        } else {
            // Draw floating area to actual area
            GMenuPaint.setUndo();
            end();

            // Create new selection area
            typeRect = new Rectangle(x, y, 1, PCARD.pc.textSize + 5);
            type = false;
        }
    }

    @Override
    public boolean mouseWithin(int x, int y) {
        // strokeDraw();
        return false;
    }

    @Override
    public boolean mouseStillDown(int x, int y) {
        if (type == true) {
            // Move
            // typeRect.x += x - PaintTool.lastX[0];
            // typeRect.y += y - PaintTool.lastY[0];

            // Reflect to front screen
            Graphics2D g3 = (Graphics2D) PaintTool.owner.mainPane.getGraphics();
            g3.drawImage(PaintTool.owner.bgImg, 0, 0, PaintTool.owner.mainPane);
            if (!PaintTool.editBackground) {
                g3.drawImage(PaintTool.owner.mainImg, 0, 0, PaintTool.owner.mainPane);
            }

            viewRect();
        } else {
            /*if(x==typeRect.x+typeRect.width&&y==typeRect.y+typeRect.height){
                return false;
            }
            setRect(x,y);*/
        }
        return true;
    }

    @Override
    public void clear() {
        type = false;
        typeRect = null;
        if (area != null) {
            PaintTool.owner.mainPane.remove(area);
            area = null;
        }
    }

    @Override
    public void end() {
        if (type) {
            // Draw textarea on back buffer
            Graphics2D redog = PaintTool.owner.redoBuf.createGraphics();
            area.setCaretColor(new Color(0, 0, 0, 0));

            if (PaintTool.owner.bit > 1) {
                area.fldData.textSize = PCARD.pc.textSize;
                area.setFont(new Font(PCARD.pc.textFont, PCARD.pc.textStyle, area.fldData.textSize));
                Rectangle rect = area.getBounds();
                rect.x = (int) ((float) rect.x / PaintTool.owner.bit + PaintTool.owner.bitLeft);
                rect.y = (int) ((float) rect.y / PaintTool.owner.bit + PaintTool.owner.bitTop);
                area.setBounds(rect);
            }
            area.fldData.width += 2;
            area.fldData.style = 1;
            area.fldData.useMyDraw = true;
            area.fldData.color = PaintTool.owner.fore.color;
            area.paint(redog);

            Graphics2D g = PaintTool.owner.getSurface().createGraphics();
            // Transparency
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, PaintTool.alpha / 100.0F));
            // Reflect to surface
            g.drawImage(PaintTool.owner.redoBuf, typeRect.x, typeRect.y, null);

        }
        if (area != null) {
            PaintTool.owner.mainPane.remove(area);
            PaintTool.owner.mainPane.repaint();
            area = null;
        }
    }


    class TypeToolListener implements DocumentListener {

        @Override
        public void changedUpdate(DocumentEvent e) {
            FontMetrics fo = TypeTool.this.area.getFontMetrics(TypeTool.this.area.getFont());
            int width = fo.stringWidth(TypeTool.this.area.getText());
            Rectangle r = TypeTool.this.area.getBounds();
            r.width = width;
            r.height = TypeTool.this.area.getLineCount() * TypeTool.this.area.getLineHeight() + 5;
            area.setBounds(r);
            area.fldData.width = r.width + 1;
            area.fldData.height = r.height;
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
}


class PaintIdle extends Thread {

    @Override
    public void run() {
        this.setName("Paint idle");

        while (true) {
            if (PaintTool.owner == null || PaintTool.owner.tool == null || PaintTool.owner.isVisible() == false) {
                try {
                    sleep(1000);
                } catch (InterruptedException e) {
                    // Thread.currentThread().interrupt();
                }
                continue;
            }

            if (PaintTool.lastTime + 100 <= System.currentTimeMillis() && PaintTool.owner.bit == 1) {
                // If waited more than 0.1 seconds
                if (PaintTool.mouse) {
                    PaintTool.mouseStillDown((int) PaintTool.lastx[0], (int) PaintTool.lasty[0]);
                } else {
                    PaintTool.mouseWithin((int) PaintTool.lastx[0], (int) PaintTool.lasty[0]);
                }
                // PaintTool.lastTime = 0;
            } else if (!PaintTool.mouse) {
                PointerInfo pointerInfo = MouseInfo.getPointerInfo();
                PaintTool.mouseWithin(pointerInfo.getLocation().x - PaintTool.owner.mainPane.getX() - PaintTool.owner.getLocationOnScreen().x,
                        pointerInfo.getLocation().y - PaintTool.owner.mainPane.getY() - PaintTool.owner.getLocationOnScreen().y - PaintTool.owner.getInsets().top);
            }
            GUI.keyEventCheck();

            try {
                sleep(100); // 10 times per 1000msec
            } catch (InterruptedException e) {
                // Thread.currentThread().interrupt();
            }
            /*if (Thread.currentThread().isInterrupted()) {
                break;
            }*/
        }
    }
}


/*class MyTabletListener implements TabletListener
{
    toolTabletInterface tool;
    boolean in_stroke = false;

    @Override
    public void cursorDragged(TabletEvent event) {
        if(event.getPressure() == 0.0) {
            in_stroke = false;
            return;
        }
        boolean eraser = (event.getDevice().getType() == cello.jtablet.TabletDevice.Type.ERASER);

        if(!in_stroke){
            tool.penDown(event.getFloatX(), event.getFloatY(), event.getPressure(), eraser);
            in_stroke = true;
        }
        tool.penStillDown(event.getFloatX(), event.getFloatY(), event.getPressure(), eraser);
        PaintTool.lastX[1] = PaintTool.lastX[0];
        PaintTool.lastY[1] = PaintTool.lastY[0];
        PaintTool.lastX[0] = event.getFloatX();
        PaintTool.lastY[0] = event.getFloatY();
    }

    @Override
    public void cursorEntered(TabletEvent arg0) {
    }

    @Override
    public void cursorExited(TabletEvent arg0) {
    }

    @Override
    public void cursorGestured(TabletEvent arg0) {
    }

    @Override
    public void cursorMoved(TabletEvent arg0) {
    }

    @Override
    public void cursorPressed(TabletEvent arg0) {
    }

    @Override
    public void cursorReleased(TabletEvent event) {
        if(in_stroke){
            boolean eraser = (event.getDevice().getType() == cello.jtablet.TabletDevice.Type.ERASER);
            tool.penUp(event.getFloatX(), event.getFloatY(), event.getPressure(), eraser);
            in_stroke = false;
        }
    }

    @Override
    public void cursorScrolled(TabletEvent arg0) {
    }

    @Override
    public void levelChanged(TabletEvent arg0) {
    }
}*/