package com.github.hyperZebra;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.io.Serial;
import javax.swing.JPanel;


// Panel for displaying card picture and background picture
class MyPanel extends JPanel {

    @Serial
    private static final long serialVersionUID = 1L;
    PCARDFrame owner;

    MyPanel(PCARDFrame owner) {
        super();
        this.owner = owner;
    }

    public void paintUnscaled(Graphics g) {
        super.paint(g);
    }

    @Override
    public void paint(Graphics g) {
        if (owner != null && owner.zoom > 1) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.scale(owner.zoom, owner.zoom);
            super.paint(g2);
            g2.dispose();
        } else {
            super.paint(g);
        }
    }

    private static final StackWalker STACK_WALKER = StackWalker.getInstance();

    @Override
    public Graphics getGraphics() {
        Graphics g = super.getGraphics();
        if (g != null && owner != null && owner.zoom > 1) {
            boolean fromSwing = STACK_WALKER.walk(s -> s.skip(1).findFirst()
                    .map(f -> f.getClassName().startsWith("javax.swing.") || f.getClassName().startsWith("java.awt."))
                    .orElse(false));
            if (!fromSwing) {
                ((Graphics2D) g).scale(owner.zoom, owner.zoom);
            }
        }
        return g;
    }

    @Override
    public void paintImmediately(int x, int y, int w, int h) {
        int z = (owner != null && owner.zoom > 1) ? owner.zoom : 1;
        if (owner != null && owner.getRootPane() != null && owner.zoom > 1) {
            Point pt = javax.swing.SwingUtilities.convertPoint(this, x * z, y * z, owner.getRootPane());
            owner.getRootPane().paintImmediately(pt.x, pt.y, w * z, h * z);
        } else {
            super.paintImmediately(x * z, y * z, w * z, h * z);
        }
    }

    @Override
    public void paintImmediately(Rectangle r) {
        if (r == null) return;
        paintImmediately(r.x, r.y, r.width, r.height);
    }


    interface blendMode {

        int Null = -1;
        int Copy = 0;
        int Blend = 1;
        int Add = 2;
        int Subtract = 3;
        int Multiply = 4;
        int Screen = 5;
        int Darken = 6;
        int Lighten = 7;
        int Difference = 8;
        int Hue = 9;
        int Color = 10;
        int Saturation = 11;
        int Luminosity = 12;
        int AlphaChannel = 13;
    }

    static int getBlendMode(String str) {
        return switch (str) {
            case "Copy" -> blendMode.Copy;
            case "Blend" -> blendMode.Blend;
            case "Add" -> blendMode.Add;
            case "Subtract" -> blendMode.Subtract;
            case "Multiply" -> blendMode.Multiply;
            case "Screen" -> blendMode.Screen;
            case "Darken" -> blendMode.Darken;
            case "Lighten" -> blendMode.Lighten;
            case "Difference" -> blendMode.Difference;
            case "Hue" -> blendMode.Hue;
            case "Color" -> blendMode.Color;
            case "Saturation" -> blendMode.Saturation;
            case "Luminosity" -> blendMode.Luminosity;
            case "Alpha Channel" -> blendMode.AlphaChannel;
            default -> blendMode.Null;
        };
    }


    @Override
    protected void paintComponent(Graphics g) {
        // Main drawing section for paint tool
        if (owner.tool != null) {
            if (!PaintTool.editBackground) {
                mainPaneDraw(g, owner.bgImg);
            }
            if (owner.blendMode != blendMode.AlphaChannel) {
                mainPaneDraw(g, owner.getSurface());
            }

            AffineTransform saveAT = ((Graphics2D) g).getTransform();

            if (owner.tool != null && owner.redoBuf != null &&
                    owner.tool instanceof ToolSelectInterface && ((ToolSelectInterface) owner.tool).isMove()) {
                BufferedImage flowImage = owner.redoBuf;

                // Blend mode
                if (owner.blendMode != blendMode.Copy || owner.blendLevel != 100) {
                    Point offset = new Point(0, 0);
                    if (owner.tool.getClass() == SelectTool.class) {
                        SelectTool tool = (SelectTool) owner.tool;
                        offset.x = tool.moveRect.x;
                        offset.y = tool.moveRect.y;
                    } else if (owner.tool.getClass() == LassoTool.class) {
                        LassoTool tool = (LassoTool) owner.tool;
                        offset.x = tool.movePoint.x;
                        offset.y = tool.movePoint.y;
                    } else if (owner.tool.getClass() == SmartSelectTool.class) {
                        SmartSelectTool tool = (SmartSelectTool) owner.tool;
                        offset.x = tool.movePoint.x;
                        offset.y = tool.movePoint.y;
                    }

                    BufferedImage newImage = new BufferedImage(flowImage.getWidth(), flowImage.getHeight(), BufferedImage.TYPE_INT_ARGB);
                    Graphics2D newg = newImage.createGraphics();
                    newg.setComposite(AlphaComposite.getInstance(AlphaComposite.CLEAR, 0.0f));
                    newg.fillRect(0, 0, newImage.getWidth(), newImage.getHeight());

                    if (owner.blendMode == blendMode.AlphaChannel) {
                        BufferedImage sfcImage = new BufferedImage(owner.getSurface().getWidth(), owner.getSurface().getHeight(), BufferedImage.TYPE_INT_ARGB);
                        Graphics2D sfcg = sfcImage.createGraphics();
                        sfcg.setComposite(AlphaComposite.getInstance(AlphaComposite.CLEAR, 0.0f));
                        sfcg.fillRect(0, 0, sfcImage.getWidth(), sfcImage.getHeight());
                        sfcImage.createGraphics().drawImage(owner.getSurface(), 0, 0, null);

                        makeBlendImage(newImage, flowImage, sfcImage, offset, owner.blendMode, owner.blendLevel);

                        mainPaneDraw(g, sfcImage);
                    } else {
                        makeBlendImage(newImage, flowImage, owner.getSurface(), offset, owner.blendMode, owner.blendLevel);
                    }

                    flowImage = newImage;
                }

                // Draw selection area
                if (owner.tool.getClass() == SelectTool.class) {
                    SelectTool tl = (SelectTool) owner.tool;
                    if (tl.move) {
                        if (owner.selectaf != null) {
                            ((Graphics2D) g).setTransform(owner.selectaf);
                        }
                        mainPaneDraw(g, flowImage, tl.moveRect.x, tl.moveRect.y, tl.moveRect.width, tl.moveRect.height);
                    }
                } else if (owner.tool.getClass() == LassoTool.class) {
                    LassoTool tl = (LassoTool) owner.tool;
                    if (tl.move) {
                        if (owner.selectaf != null) {
                            ((Graphics2D) g).setTransform(owner.selectaf);
                        }
                        mainPaneDraw(g, flowImage, tl.movePoint.x, tl.movePoint.y, owner.redoBuf.getWidth(), owner.redoBuf.getHeight());
                    }
                } else if (owner.tool.getClass() == SmartSelectTool.class) {
                    SmartSelectTool tl = (SmartSelectTool) owner.tool;
                    if (tl.move) {
                        if (owner.selectaf != null) {
                            ((Graphics2D) g).setTransform(owner.selectaf);
                        }
                        mainPaneDraw(g, flowImage, tl.movePoint.x, tl.movePoint.y, owner.redoBuf.getWidth(), owner.redoBuf.getHeight());
                    }
                }
            }

            ((Graphics2D) g).setTransform(saveAT);

            if (owner.bit > 2) {
                bordersDraw(g, owner.bit, owner.bgImg.getWidth(), owner.bgImg.getHeight());
            }
        }

        /*if(PCARD.pc.stack!=null&&PCARD.pc.stack.curCard!=null
                &&PCARD.pc.stack.curCard.bg!=null&&PCARD.pc.stack.curCard.bg.label!=null
                &&PCARD.pc.tool==null){
            return;
        }*/
    }


    static void bordersDraw(Graphics g, int bit, int width, int height) {
        if (!PCARDFrame.useGrid) return;
        for (int h = 0/*bit-1*/; h < width * bit; h += bit * PCARDFrame.gridSize) {
            g.setColor(Color.GRAY);
            g.drawLine(h, 0, h, height * bit);
        }
        for (int v = 0/*bit-1*/; v < height * bit; v += bit * PCARDFrame.gridSize) {
            g.setColor(Color.GRAY);
            g.drawLine(0, v, width * bit, v);
        }
    }

    void mainPaneDraw(Graphics g, BufferedImage img) {
        if (owner.bit > 1) {
            // Magnified display
            g.drawImage(img, ((int) owner.bitLeft) * (-owner.bit),
                    ((int) owner.bitTop) * (-owner.bit),
                    ((int) owner.bitLeft) * (-owner.bit) + owner.bgImg.getWidth() * owner.bit,
                    ((int) owner.bitTop) * (-owner.bit) + owner.bgImg.getHeight() * owner.bit,
                    0, 0, owner.bgImg.getWidth(), owner.bgImg.getHeight(), null);
        } else {
            g.drawImage(img, 0, 0, null);
        }
    }

    public void mainPaneDraw(Graphics g, BufferedImage img, int x, int y, int width, int height) {
        if (owner.bit > 1) {
            // Magnified display
            g.drawImage(img, x * owner.bit - (int) owner.bitLeft * owner.bit,
                    y * owner.bit - (int) owner.bitTop * owner.bit,
                    x * owner.bit - (int) owner.bitLeft * owner.bit + width * owner.bit,
                    y * owner.bit - (int) owner.bitTop * owner.bit + height * owner.bit,
                    0, 0, width, height, null);
        } else {
            g.drawImage(img, x, y, x + width, y + height, 0, 0, width, height, null);
        }
    }


    public static BufferedImage makeBlendImage(BufferedImage flowImage) {
        if (PaintTool.owner.blendMode == blendMode.Copy && PaintTool.owner.blendLevel == 100) {
            return flowImage;
        }

        BufferedImage newImage = new BufferedImage(flowImage.getWidth(), flowImage.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D newg = newImage.createGraphics();
        newg.setComposite(AlphaComposite.getInstance(AlphaComposite.CLEAR, 0.0f));
        newg.fillRect(0, 0, newImage.getWidth(), newImage.getHeight());

        Point offset = new Point(0, 0);
        if (PaintTool.owner.tool.getClass() == SelectTool.class) {
            SelectTool tool = (SelectTool) PaintTool.owner.tool;
            offset.x = tool.moveRect.x;
            offset.y = tool.moveRect.y;
        } else if (PaintTool.owner.tool.getClass() == LassoTool.class) {
            LassoTool tool = (LassoTool) PaintTool.owner.tool;
            offset.x = tool.movePoint.x;
            offset.y = tool.movePoint.y;
        } else if (PaintTool.owner.tool.getClass() == SmartSelectTool.class) {
            SmartSelectTool tool = (SmartSelectTool) PaintTool.owner.tool;
            offset.x = tool.movePoint.x;
            offset.y = tool.movePoint.y;
        }

        makeBlendImage(newImage, flowImage, PaintTool.owner.getSurface(), offset,
                PaintTool.owner.blendMode, PaintTool.owner.blendLevel);

        return newImage;
    }


    private static void makeBlendImage(BufferedImage newImage,
                                       BufferedImage flowImage, BufferedImage surfaceImage, Point offset, int mode, int level) {
        if (flowImage.hasTileWriters()) {

            BufferedImage flowImage2 = new BufferedImage(flowImage.getWidth(), flowImage.getHeight(), BufferedImage.TYPE_INT_ARGB);
            Graphics2D newg = flowImage2.createGraphics();
            newg.setComposite(AlphaComposite.getInstance(AlphaComposite.CLEAR, 0.0f));
            newg.fillRect(0, 0, flowImage2.getWidth(), flowImage2.getHeight());
            flowImage2.createGraphics().drawImage(flowImage, 0, 0, null);
            flowImage = flowImage2;
        }

        switch (mode) {
            case blendMode.Copy:
                copyImage(newImage, flowImage, surfaceImage, offset, level);
                break;
            case blendMode.Blend:
                blendImage(newImage, flowImage, surfaceImage, offset, level);
                break;
            case blendMode.Add:
                addImage(newImage, flowImage, surfaceImage, offset, level);
                break;
            case blendMode.Subtract:
                subtractImage(newImage, flowImage, surfaceImage, offset, level);
                break;
            case blendMode.Multiply:
                multiplyImage(newImage, flowImage, surfaceImage, offset, level);
                break;
            case blendMode.Screen:
                screenImage(newImage, flowImage, surfaceImage, offset, level);
                break;
            case blendMode.Darken:
                darkenImage(newImage, flowImage, surfaceImage, offset, level);
                break;
            case blendMode.Lighten:
                lightenImage(newImage, flowImage, surfaceImage, offset, level);
                break;
            case blendMode.Difference:
                differenceImage(newImage, flowImage, surfaceImage, offset, level);
                break;
            case blendMode.Hue:
                hueImage(newImage, flowImage, surfaceImage, offset, level);
                break;
            case blendMode.Color:
                colorImage(newImage, flowImage, surfaceImage, offset, level);
                break;
            case blendMode.Saturation:
                saturationImage(newImage, flowImage, surfaceImage, offset, level);
                break;
            case blendMode.Luminosity:
                luminosityImage(newImage, flowImage, surfaceImage, offset, level);
                break;
            case blendMode.AlphaChannel:
                alphaImage(newImage, flowImage, surfaceImage, offset, level);
                break;
        }
    }


    private static void copyImage(BufferedImage newImage, BufferedImage flowImage,
                                  BufferedImage surfaceImage, Point offset, int level) {
        // Copy mode only changes colors

        // DataBuffer surfacebuf = surfaceImage.getRaster().getDataBuffer();
        DataBuffer srcbuf = flowImage.getRaster().getDataBuffer();
        DataBuffer newbuf = newImage.getRaster().getDataBuffer();
        int srcwidth = flowImage.getWidth();
        int srcheight = flowImage.getHeight();
        int width = newImage.getWidth();
        int height = newImage.getHeight();
        for (int v = 0; v < srcheight; v++) {
            if (v < 0 || v >= height) {
                continue;
            }
            for (int h = 0; h < srcwidth; h++) {
                if (h < 0 || h >= width) {
                    continue;
                }
                int c = srcbuf.getElem(h + v * srcwidth);
                int alpha = c & 0xFF000000;
                if (alpha != 0) {
                    int red = ((c >> 16) & 0xFF) * level / 100 + 255 * (100 - level) / 100;
                    int green = ((c >> 8) & 0xFF) * level / 100 + 255 * (100 - level) / 100;
                    int blue = ((c) & 0xFF) * level / 100 + 255 * (100 - level) / 100;
                    c = alpha + (red << 16) + (green << 8) + blue;
                    newbuf.setElem(h + v * width, c);
                }
            }
        }
    }


    private static void blendImage(BufferedImage newImage, BufferedImage flowImage,
                                   BufferedImage surfaceImage, Point offset, int level) {
        // Blend mode only changes alpha values

        // DataBuffer surfacebuf = surfaceImage.getRaster().getDataBuffer();
        DataBuffer srcbuf = flowImage.getRaster().getDataBuffer();
        DataBuffer newbuf = newImage.getRaster().getDataBuffer();
        int srcwidth = flowImage.getWidth();
        int srcheight = flowImage.getHeight();
        int width = newImage.getWidth();
        int height = newImage.getHeight();
        for (int v = 0; v < srcheight; v++) {
            if (v < 0 || v >= height) {
                continue;
            }
            for (int h = 0; h < srcwidth; h++) {
                if (h < 0 || h >= width) {
                    continue;
                }
                int c = srcbuf.getElem(h + v * srcwidth);
                int alpha = 0xFF & (c >> 24);
                if (alpha != 0) {
                    alpha = alpha * level / 100;
                    c = (alpha << 24) + (0x00FFFFFF & c);
                    newbuf.setElem(h + v * width, c);
                }
            }
        }
    }


    private static void addImage(BufferedImage newImage, BufferedImage flowImage,
                                 BufferedImage surfaceImage, Point offset, int level) {
        // Add mode is addition starting from black

        DataBuffer surfacebuf = surfaceImage.getRaster().getDataBuffer();
        DataBuffer srcbuf = flowImage.getRaster().getDataBuffer();
        DataBuffer newbuf = newImage.getRaster().getDataBuffer();
        int sfcwidth = surfaceImage.getWidth();
        int sfcheight = surfaceImage.getHeight();
        int srcwidth = flowImage.getWidth();
        int srcheight = flowImage.getHeight();
        int width = newImage.getWidth();
        int height = newImage.getHeight();
        for (int v = 0; v < srcheight; v++) {
            if (v + offset.y < 0 || v + offset.y >= height) {
                continue;
            }
            if (v + offset.y >= sfcheight) {
                continue;
            }
            for (int h = 0; h < srcwidth; h++) {
                int c = srcbuf.getElem(h + v * srcwidth);
                int alpha = c & 0xFF000000;
                if (alpha != 0) {
                    if (h + offset.x < 0 || h + offset.x >= width) {
                        continue;
                    }
                    int red = ((c >> 16) & 0xFF) * level / 100;
                    int green = ((c >> 8) & 0xFF) * level / 100;
                    int blue = ((c) & 0xFF) * level / 100;

                    if (h + offset.x >= sfcwidth) {
                        continue;
                    }
                    int sc = surfacebuf.getElem(h + offset.x + (v + offset.y) * sfcwidth);
                    int sfc_red, sfc_green, sfc_blue;
                    if ((sc & 0xFF000000) == 0) {
                        sc = 0x00FFFFFF;
                    }
                    sfc_red = ((sc >> 16) & 0xFF);
                    sfc_green = ((sc >> 8) & 0xFF);
                    sfc_blue = ((sc) & 0xFF);

                    red += sfc_red;
                    if (red > 255) red = 255;
                    green += sfc_green;
                    if (green > 255) green = 255;
                    blue += sfc_blue;
                    if (blue > 255) blue = 255;

                    c = alpha + (red << 16) + (green << 8) + blue;
                    newbuf.setElem(h + v * width, c);
                }
            }
        }
    }


    private static void subtractImage(BufferedImage newImage, BufferedImage flowImage,
                                      BufferedImage surfaceImage, Point offset, int level) {
        // Subtract mode is subtraction starting from white

        DataBuffer surfacebuf = surfaceImage.getRaster().getDataBuffer();
        DataBuffer srcbuf = flowImage.getRaster().getDataBuffer();
        DataBuffer newbuf = newImage.getRaster().getDataBuffer();
        int sfcwidth = surfaceImage.getWidth();
        int sfcheight = surfaceImage.getHeight();
        int srcwidth = flowImage.getWidth();
        int srcheight = flowImage.getHeight();
        int width = newImage.getWidth();
        int height = newImage.getHeight();
        for (int v = 0; v < srcheight; v++) {
            if (v + offset.y < 0 || v + offset.y >= height) {
                continue;
            }
            if (v + offset.y >= sfcheight) {
                continue;
            }
            for (int h = 0; h < srcwidth; h++) {
                int c = srcbuf.getElem(h + v * srcwidth);
                int alpha = c & 0xFF000000;
                if (alpha != 0) {
                    int red = (255 - ((c >> 16) & 0xFF)) * level / 100;
                    int green = (255 - ((c >> 8) & 0xFF)) * level / 100;
                    int blue = (255 - ((c) & 0xFF)) * level / 100;

                    if (h + offset.x < 0 || h + offset.x >= width) {
                        continue;
                    }
                    if (h + offset.x >= sfcwidth) {
                        continue;
                    }
                    int sc = surfacebuf.getElem(h + offset.x + (v + offset.y) * sfcwidth);
                    int sfc_red, sfc_green, sfc_blue;
                    if ((sc & 0xFF000000) == 0) {
                        sc = 0x00FFFFFF;
                    }
                    sfc_red = ((sc >> 16) & 0xFF);
                    sfc_green = ((sc >> 8) & 0xFF);
                    sfc_blue = ((sc) & 0xFF);

                    sfc_red -= red;
                    if (sfc_red < 0) sfc_red = 0;
                    sfc_green -= green;
                    if (sfc_green < 0) sfc_green = 0;
                    sfc_blue -= blue;
                    if (sfc_blue < 0) sfc_blue = 0;

                    c = alpha + (sfc_red << 16) + (sfc_green << 8) + sfc_blue;
                    newbuf.setElem(h + v * width, c);
                }
            }
        }
    }


    private static void multiplyImage(BufferedImage newImage, BufferedImage flowImage,
                                      BufferedImage surfaceImage, Point offset, int level) {
        // Multiply mode is multiplication starting from black

        DataBuffer surfacebuf = surfaceImage.getRaster().getDataBuffer();
        DataBuffer srcbuf = flowImage.getRaster().getDataBuffer();
        DataBuffer newbuf = newImage.getRaster().getDataBuffer();
        int sfcwidth = surfaceImage.getWidth();
        int sfcheight = surfaceImage.getHeight();
        int srcwidth = flowImage.getWidth();
        int srcheight = flowImage.getHeight();
        int width = newImage.getWidth();
        int height = newImage.getHeight();
        for (int v = 0; v < srcheight; v++) {
            if (v + offset.y < 0 || v + offset.y >= height) {
                continue;
            }
            if (v + offset.y >= sfcheight) {
                continue;
            }
            for (int h = 0; h < srcwidth; h++) {
                int c = srcbuf.getElem(h + v * srcwidth);
                int alpha = c & 0xFF000000;
                if (alpha != 0) {
                    int red = ((c >> 16) & 0xFF) * level / 100;
                    int green = ((c >> 8) & 0xFF) * level / 100;
                    int blue = ((c) & 0xFF) * level / 100;

                    if (h + offset.x < 0 || h + offset.x >= width) {
                        continue;
                    }
                    if (h + offset.x >= sfcwidth) {
                        continue;
                    }
                    int sc = surfacebuf.getElem(h + offset.x + (v + offset.y) * sfcwidth);
                    int sfc_red, sfc_green, sfc_blue;
                    if ((sc & 0xFF000000) == 0) {
                        sc = 0x00FFFFFF;
                    }
                    sfc_red = ((sc >> 16) & 0xFF);
                    sfc_green = ((sc >> 8) & 0xFF);
                    sfc_blue = ((sc) & 0xFF);

                    sfc_red = sfc_red * red / 255;
                    sfc_green = sfc_green * green / 255;
                    sfc_blue = sfc_blue * blue / 255;

                    c = alpha + (sfc_red << 16) + (sfc_green << 8) + sfc_blue;
                    newbuf.setElem(h + v * width, c);
                }
            }
        }
    }


    private static void screenImage(BufferedImage newImage, BufferedImage flowImage,
                                    BufferedImage surfaceImage, Point offset, int level) {
        // Screen mode is addition starting from level

        DataBuffer surfacebuf = surfaceImage.getRaster().getDataBuffer();
        DataBuffer srcbuf = flowImage.getRaster().getDataBuffer();
        DataBuffer newbuf = newImage.getRaster().getDataBuffer();
        int sfcwidth = surfaceImage.getWidth();
        int sfcheight = surfaceImage.getHeight();
        int srcwidth = flowImage.getWidth();
        int srcheight = flowImage.getHeight();
        int width = newImage.getWidth();
        int height = newImage.getHeight();
        for (int v = 0; v < srcheight; v++) {
            if (v + offset.y < 0 || v + offset.y >= height) {
                continue;
            }
            if (v + offset.y >= sfcheight) {
                continue;
            }
            for (int h = 0; h < srcwidth; h++) {
                int c = srcbuf.getElem(h + v * srcwidth);
                int alpha = c & 0xFF000000;
                if (alpha != 0) {
                    int red = ((c >> 16) & 0xFF) - 255 * level / 100;
                    int green = ((c >> 8) & 0xFF) - 255 * level / 100;
                    int blue = ((c) & 0xFF) - 255 * level / 100;

                    if (h + offset.x < 0 || h + offset.x >= width) {
                        continue;
                    }
                    if (h + offset.x >= sfcwidth) {
                        continue;
                    }
                    int sc = surfacebuf.getElem(h + offset.x + (v + offset.y) * sfcwidth);
                    int sfc_red, sfc_green, sfc_blue;
                    if ((sc & 0xFF000000) == 0) {
                        sc = 0x00FFFFFF;
                    }
                    sfc_red = ((sc >> 16) & 0xFF);
                    sfc_green = ((sc >> 8) & 0xFF);
                    sfc_blue = ((sc) & 0xFF);

                    sfc_red += red;
                    if (sfc_red < 0) sfc_red = 0;
                    if (sfc_red > 255) sfc_red = 255;
                    sfc_green += green;
                    if (sfc_green < 0) sfc_green = 0;
                    if (sfc_green > 255) sfc_green = 255;
                    sfc_blue += blue;
                    if (sfc_blue < 0) sfc_blue = 0;
                    if (sfc_blue > 255) sfc_blue = 255;

                    c = alpha + (sfc_red << 16) + (sfc_green << 8) + sfc_blue;
                    newbuf.setElem(h + v * width, c);
                }
            }
        }
    }


    private static void darkenImage(BufferedImage newImage, BufferedImage flowImage,
                                    BufferedImage surfaceImage, Point offset, int level) {
        // Darken mode keeps the darker one

        DataBuffer surfacebuf = surfaceImage.getRaster().getDataBuffer();
        DataBuffer srcbuf = flowImage.getRaster().getDataBuffer();
        DataBuffer newbuf = newImage.getRaster().getDataBuffer();
        int sfcwidth = surfaceImage.getWidth();
        int sfcheight = surfaceImage.getHeight();
        int srcwidth = flowImage.getWidth();
        int srcheight = flowImage.getHeight();
        int width = newImage.getWidth();
        int height = newImage.getHeight();
        for (int v = 0; v < srcheight; v++) {
            if (v + offset.y < 0 || v + offset.y >= height) {
                continue;
            }
            if (v + offset.y >= sfcheight) {
                continue;
            }
            for (int h = 0; h < srcwidth; h++) {
                int c = srcbuf.getElem(h + v * srcwidth);
                int alpha = c & 0xFF000000;
                if (alpha != 0) {
                    int red = ((c >> 16) & 0xFF);
                    int green = ((c >> 8) & 0xFF);
                    int blue = ((c) & 0xFF);

                    if (h + offset.x < 0 || h + offset.x >= width) {
                        continue;
                    }
                    if (h + offset.x >= sfcwidth) {
                        continue;
                    }
                    int sc = surfacebuf.getElem(h + offset.x + (v + offset.y) * sfcwidth);
                    int sfc_red, sfc_green, sfc_blue;
                    if ((sc & 0xFF000000) == 0) {
                        sc = 0x00FFFFFF;
                    }
                    sfc_red = ((sc >> 16) & 0xFF);
                    sfc_green = ((sc >> 8) & 0xFF);
                    sfc_blue = ((sc) & 0xFF);

                    if (sfc_red + sfc_green + sfc_blue < red + green + blue) {
                        c = alpha + (sfc_red << 16) + (sfc_green << 8) + sfc_blue;
                    } else {
                        c = alpha + (red << 16) + (green << 8) + blue;
                    }
                    newbuf.setElem(h + v * width, c);
                }
            }
        }
    }


    private static void lightenImage(BufferedImage newImage, BufferedImage flowImage,
                                     BufferedImage surfaceImage, Point offset, int level) {
        // Lighten mode keeps the lighter one

        DataBuffer surfacebuf = surfaceImage.getRaster().getDataBuffer();
        DataBuffer srcbuf = flowImage.getRaster().getDataBuffer();
        DataBuffer newbuf = newImage.getRaster().getDataBuffer();
        int sfcwidth = surfaceImage.getWidth();
        int sfcheight = surfaceImage.getHeight();
        int width = newImage.getWidth();
        int height = newImage.getHeight();
        for (int v = 0; v < height; v++) {
            if (v + offset.y < 0 || v + offset.y >= height) {
                continue;
            }
            if (v + offset.y >= sfcheight) {
                continue;
            }
            for (int h = 0; h < width; h++) {
                int c = srcbuf.getElem(h + v * width);
                int alpha = c & 0xFF000000;
                if (alpha != 0) {
                    int red = ((c >> 16) & 0xFF);
                    int green = ((c >> 8) & 0xFF);
                    int blue = ((c) & 0xFF);

                    if (h + offset.x < 0 || h + offset.x >= width) {
                        continue;
                    }
                    if (h + offset.x >= sfcwidth) {
                        continue;
                    }
                    int sc = surfacebuf.getElem(h + offset.x + (v + offset.y) * sfcwidth);
                    int sfc_red, sfc_green, sfc_blue;
                    if ((sc & 0xFF000000) == 0) {
                        sc = 0x00000000;
                    }
                    sfc_red = ((sc >> 16) & 0xFF);
                    sfc_green = ((sc >> 8) & 0xFF);
                    sfc_blue = ((sc) & 0xFF);

                    if (sfc_red + sfc_green + sfc_blue > red + green + blue) {
                        c = alpha + (sfc_red << 16) + (sfc_green << 8) + sfc_blue;
                    } else {
                        c = alpha + (red << 16) + (green << 8) + blue;
                    }
                    newbuf.setElem(h + v * width, c);
                }
            }
        }
    }


    private static void differenceImage(BufferedImage newImage, BufferedImage flowImage,
                                        BufferedImage surfaceImage, Point offset, int level) {
        // Difference mode is absolute difference

        DataBuffer surfacebuf = surfaceImage.getRaster().getDataBuffer();
        DataBuffer srcbuf = flowImage.getRaster().getDataBuffer();
        DataBuffer newbuf = newImage.getRaster().getDataBuffer();
        int sfcwidth = surfaceImage.getWidth();
        int sfcheight = surfaceImage.getHeight();
        int width = newImage.getWidth();
        int height = newImage.getHeight();
        for (int v = 0; v < height; v++) {
            if (v + offset.y < 0 || v + offset.y >= height) {
                continue;
            }
            if (v + offset.y >= sfcheight) {
                continue;
            }
            for (int h = 0; h < width; h++) {
                int c = srcbuf.getElem(h + v * width);
                int alpha = c & 0xFF000000;
                if (alpha != 0) {
                    int red = ((c >> 16) & 0xFF);
                    int green = ((c >> 8) & 0xFF);
                    int blue = ((c) & 0xFF);

                    if (h + offset.x < 0 || h + offset.x >= width) {
                        continue;
                    }
                    if (h + offset.x >= sfcwidth) {
                        continue;
                    }
                    int sc = surfacebuf.getElem(h + offset.x + (v + offset.y) * sfcwidth);
                    int sfc_red, sfc_green, sfc_blue;
                    if ((sc & 0xFF000000) == 0) {
                        sc = 0x00FFFFFF;
                    }
                    sfc_red = ((sc >> 16) & 0xFF);
                    sfc_green = ((sc >> 8) & 0xFF);
                    sfc_blue = ((sc) & 0xFF);

                    sfc_red = Math.abs(sfc_red - red);
                    sfc_green = Math.abs(sfc_green - green);
                    sfc_blue = Math.abs(sfc_blue - blue);

                    c = alpha + (sfc_red << 16) + (sfc_green << 8) + sfc_blue;
                    newbuf.setElem(h + v * width, c);
                }
            }
        }
    }


    private static void hueImage(BufferedImage newImage, BufferedImage flowImage,
                                 BufferedImage surfaceImage, Point offset, int level) {
        // Hue mode is hue only

        DataBuffer surfacebuf = surfaceImage.getRaster().getDataBuffer();
        DataBuffer srcbuf = flowImage.getRaster().getDataBuffer();
        DataBuffer newbuf = newImage.getRaster().getDataBuffer();
        int sfcwidth = surfaceImage.getWidth();
        int sfcheight = surfaceImage.getHeight();
        int width = newImage.getWidth();
        int height = newImage.getHeight();
        float[] hsb = new float[3];
        float[] sfc_hsb = new float[3];
        for (int v = 0; v < height; v++) {
            if (v + offset.y < 0 || v + offset.y >= height) {
                continue;
            }
            if (v + offset.y >= sfcheight) {
                continue;
            }
            for (int h = 0; h < width; h++) {
                int c = srcbuf.getElem(h + v * width);
                int alpha = c & 0xFF000000;
                if (alpha != 0) {
                    int red = ((c >> 16) & 0xFF);
                    int green = ((c >> 8) & 0xFF);
                    int blue = ((c) & 0xFF);

                    if (h + offset.x < 0 || h + offset.x >= width) {
                        continue;
                    }
                    if (h + offset.x >= sfcwidth) {
                        continue;
                    }
                    int sc = surfacebuf.getElem(h + offset.x + (v + offset.y) * sfcwidth);
                    int sfc_red, sfc_green, sfc_blue;
                    if ((sc & 0xFF000000) == 0) {
                        sc = 0x00FFFFFF;
                    }
                    sfc_red = ((sc >> 16) & 0xFF);
                    sfc_green = ((sc >> 8) & 0xFF);
                    sfc_blue = ((sc) & 0xFF);

                    Color.RGBtoHSB(red, green, blue, hsb);
                    Color.RGBtoHSB(sfc_red, sfc_green, sfc_blue, sfc_hsb);
                    sfc_hsb[0] = hsb[0]; // Assign hue only
                    int rgb = Color.HSBtoRGB(sfc_hsb[0], sfc_hsb[1], sfc_hsb[2]);

                    c = alpha + (0x00FFFFFF & rgb);
                    newbuf.setElem(h + v * width, c);
                }
            }
        }
    }


    private static void colorImage(BufferedImage newImage, BufferedImage flowImage,
                                   BufferedImage surfaceImage, Point offset, int level) {
        // Color mode is hue and saturation

        DataBuffer surfacebuf = surfaceImage.getRaster().getDataBuffer();
        DataBuffer srcbuf = flowImage.getRaster().getDataBuffer();
        DataBuffer newbuf = newImage.getRaster().getDataBuffer();
        int sfcwidth = surfaceImage.getWidth();
        int sfcheight = surfaceImage.getHeight();
        int width = newImage.getWidth();
        int height = newImage.getHeight();
        float[] hsb = new float[3];
        float[] sfc_hsb = new float[3];
        for (int v = 0; v < height; v++) {
            if (v + offset.y < 0 || v + offset.y >= height) {
                continue;
            }
            if (v + offset.y >= sfcheight) {
                continue;
            }
            for (int h = 0; h < width; h++) {
                int c = srcbuf.getElem(h + v * width);
                int alpha = c & 0xFF000000;
                if (alpha != 0) {
                    int red = ((c >> 16) & 0xFF);
                    int green = ((c >> 8) & 0xFF);
                    int blue = ((c) & 0xFF);

                    if (h + offset.x < 0 || h + offset.x >= width) {
                        continue;
                    }
                    if (h + offset.x >= sfcwidth) {
                        continue;
                    }
                    int sc = surfacebuf.getElem(h + offset.x + (v + offset.y) * sfcwidth);
                    int sfc_red, sfc_green, sfc_blue;
                    if ((sc & 0xFF000000) == 0) {
                        sc = 0x00FFFFFF;
                    }
                    sfc_red = ((sc >> 16) & 0xFF);
                    sfc_green = ((sc >> 8) & 0xFF);
                    sfc_blue = ((sc) & 0xFF);

                    Color.RGBtoHSB(red, green, blue, hsb);
                    Color.RGBtoHSB(sfc_red, sfc_green, sfc_blue, sfc_hsb);
                    sfc_hsb[0] = hsb[0]; // Assign hue
                    sfc_hsb[1] = hsb[1]; // Assign saturation
                    int rgb = Color.HSBtoRGB(sfc_hsb[0], sfc_hsb[1], sfc_hsb[2]);

                    c = alpha + (0x00FFFFFF & rgb);
                    newbuf.setElem(h + v * width, c);
                }
            }
        }
    }


    private static void saturationImage(BufferedImage newImage, BufferedImage flowImage,
                                        BufferedImage surfaceImage, Point offset, int level) {
        // Saturation mode is saturation

        DataBuffer surfacebuf = surfaceImage.getRaster().getDataBuffer();
        DataBuffer srcbuf = flowImage.getRaster().getDataBuffer();
        DataBuffer newbuf = newImage.getRaster().getDataBuffer();
        int sfcwidth = surfaceImage.getWidth();
        int sfcheight = surfaceImage.getHeight();
        int width = newImage.getWidth();
        int height = newImage.getHeight();
        float[] hsb = new float[3];
        float[] sfc_hsb = new float[3];
        for (int v = 0; v < height; v++) {
            if (v + offset.y < 0 || v + offset.y >= height) {
                continue;
            }
            if (v + offset.y >= sfcheight) {
                continue;
            }
            for (int h = 0; h < width; h++) {
                int c = srcbuf.getElem(h + v * width);
                int alpha = c & 0xFF000000;
                if (alpha != 0) {
                    int red = ((c >> 16) & 0xFF);
                    int green = ((c >> 8) & 0xFF);
                    int blue = ((c) & 0xFF);

                    if (h + offset.x < 0 || h + offset.x >= width) {
                        continue;
                    }
                    if (h + offset.x >= sfcwidth) {
                        continue;
                    }
                    int sc = surfacebuf.getElem(h + offset.x + (v + offset.y) * sfcwidth);
                    int sfc_red, sfc_green, sfc_blue;
                    if ((sc & 0xFF000000) == 0) {
                        sc = 0x00FFFFFF;
                    }
                    sfc_red = ((sc >> 16) & 0xFF);
                    sfc_green = ((sc >> 8) & 0xFF);
                    sfc_blue = ((sc) & 0xFF);

                    Color.RGBtoHSB(red, green, blue, hsb);
                    Color.RGBtoHSB(sfc_red, sfc_green, sfc_blue, sfc_hsb);
                    sfc_hsb[1] = hsb[1]; // Assign saturation
                    int rgb = Color.HSBtoRGB(sfc_hsb[0], sfc_hsb[1], sfc_hsb[2]);

                    c = alpha + (0x00FFFFFF & rgb);
                    newbuf.setElem(h + v * width, c);
                }
            }
        }
    }


    private static void luminosityImage(BufferedImage newImage, BufferedImage flowImage,
                                        BufferedImage surfaceImage, Point offset, int level) {
        // Luminosity mode is brightness

        DataBuffer surfacebuf = surfaceImage.getRaster().getDataBuffer();
        DataBuffer srcbuf = flowImage.getRaster().getDataBuffer();
        DataBuffer newbuf = newImage.getRaster().getDataBuffer();
        int sfcwidth = surfaceImage.getWidth();
        int sfcheight = surfaceImage.getHeight();
        int width = newImage.getWidth();
        int height = newImage.getHeight();
        float[] hsb = new float[3];
        float[] sfc_hsb = new float[3];
        for (int v = 0; v < height; v++) {
            if (v + offset.y < 0 || v + offset.y >= height) {
                continue;
            }
            if (v + offset.y >= sfcheight) {
                continue;
            }
            for (int h = 0; h < width; h++) {
                int c = srcbuf.getElem(h + v * width);
                int alpha = c & 0xFF000000;
                if (alpha != 0) {
                    int red = ((c >> 16) & 0xFF);
                    int green = ((c >> 8) & 0xFF);
                    int blue = ((c) & 0xFF);

                    if (h + offset.x < 0 || h + offset.x >= width) {
                        continue;
                    }
                    if (h + offset.x >= sfcwidth) {
                        continue;
                    }
                    int sc = surfacebuf.getElem(h + offset.x + (v + offset.y) * sfcwidth);
                    int sfc_red, sfc_green, sfc_blue;
                    if ((sc & 0xFF000000) == 0) {
                        sc = 0x00FFFFFF;
                    }
                    sfc_red = ((sc >> 16) & 0xFF);
                    sfc_green = ((sc >> 8) & 0xFF);
                    sfc_blue = ((sc) & 0xFF);

                    Color.RGBtoHSB(red, green, blue, hsb);
                    Color.RGBtoHSB(sfc_red, sfc_green, sfc_blue, sfc_hsb);
                    sfc_hsb[2] = hsb[2]; // Assign brightness
                    int rgb = Color.HSBtoRGB(sfc_hsb[0], sfc_hsb[1], sfc_hsb[2]);

                    c = alpha + (0x00FFFFFF & rgb);
                    newbuf.setElem(h + v * width, c);
                }
            }
        }
    }


    private static void alphaImage(BufferedImage newImage, BufferedImage flowImage,
                                   BufferedImage surfaceImage, Point offset, int level) {
        // Alpha channel mode applies density to surface alpha channel
        // Special handling needed as surface is modified

        DataBuffer surfacebuf = surfaceImage.getRaster().getDataBuffer();
        DataBuffer srcbuf = flowImage.getRaster().getDataBuffer();
        // DataBuffer newbuf = newImage.getRaster().getDataBuffer();
        int sfcwidth = surfaceImage.getWidth();
        int sfcheight = surfaceImage.getHeight();
        int width = newImage.getWidth();
        int height = newImage.getHeight();
        for (int v = 0; v < height; v++) {
            if (v + offset.y < 0 || v + offset.y >= height) {
                continue;
            }
            if (v + offset.y >= sfcheight) {
                continue;
            }
            for (int h = 0; h < width; h++) {
                int c = srcbuf.getElem(h + v * width);
                int alpha = c & 0xFF000000;
                if (alpha != 0) {
                    int red = ((c >> 16) & 0xFF);
                    int green = ((c >> 8) & 0xFF);
                    int blue = ((c) & 0xFF);

                    if (h + offset.x < 0 || h + offset.x >= width) {
                        continue;
                    }
                    if (h + offset.x >= sfcwidth) {
                        continue;
                    }
                    int sc = surfacebuf.getElem(h + offset.x + (v + offset.y) * sfcwidth);
                    if ((sc & 0xFF000000) == 0) {
                        continue;
                    }
                    int sfc_alpha = 255 - (red + green + blue) * level / 100 / 3;

                    c = (sfc_alpha << 24) + (0x00FFFFFF & sc);
                    surfacebuf.setElem(h + offset.x + (v + offset.y) * sfcwidth, c);
                }
            }
        }
    }
}
