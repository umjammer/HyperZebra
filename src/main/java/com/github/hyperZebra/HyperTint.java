/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package com.github.hyperZebra;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.Serial;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import javax.imageio.ImageIO;
import javax.swing.JComponent;
import javax.swing.JLayeredPane;
import javax.swing.SwingUtilities;


/**
 * An emulation of the HyperTint XCMD family (8 bit color pictures over the card)
 * and some other color picture XCMDs (xCIcon3).
 * <p>
 * The color screen is an overlay over the card pane, mouse events go through it.
 * While it is locked (HTLock true) the last shown screen stays, unlocking shows
 * the new screen with the visual effect given by HTVisual.
 */
final class HyperTint {

    private static final Logger logger = System.getLogger(HyperTint.class.getName());

    private HyperTint() {
    }

    /** the screen being drawn */
    private static BufferedImage canvas;
    /** the screen shown */
    private static BufferedImage shown;
    /** previous screen while a transition runs */
    private static BufferedImage transFrom;
    private static volatile float transProgress = 1f;
    private static String transEffect = "";
    private static Rectangle transRect;

    private static boolean active;
    private static boolean locked;

    private static String pendingEffect;
    private static Rectangle pendingRect;
    private static int pendingSpeed;

    private static Layer layer;

    /** the overlay component */
    static class Layer extends JComponent {

        @Serial
        private static final long serialVersionUID = 1L;

        @Override
        public boolean contains(int x, int y) {
            return false; // mouse events go to the card
        }

        /** @return the top left of the card in this component */
        Point cardOrigin() {
            if (PCARD.pc.mainPane == null) return new Point(0, 0);
            return SwingUtilities.convertPoint(PCARD.pc.mainPane, 0, 0, this);
        }

        @Override
        protected void paintComponent(Graphics g) {
            BufferedImage img = shown;
            if (!active || img == null) return;
            Graphics2D g2 = (Graphics2D) g.create();
            Point o = cardOrigin();
            g2.translate(o.x, o.y);
            int z = Math.max(1, PCARD.pc.zoom);
            g2.scale(z, z);
            BufferedImage from = transFrom;
            float t = transProgress;
            if (from != null && t < 1f) {
                g2.drawImage(from, 0, 0, null);
                paintTransition(g2, img, t);
            } else {
                g2.drawImage(img, 0, 0, null);
            }
            g2.dispose();
        }

        private void paintTransition(Graphics2D g2, BufferedImage to, float t) {
            int w = to.getWidth(), h = to.getHeight();
            Rectangle r = transRect != null ? transRect : new Rectangle(0, 0, w, h);
            String e = transEffect;
            Graphics2D g = (Graphics2D) g2.create();
            g.clipRect(r.x, r.y, r.width, r.height);
            if (e.contains("wipe left")) {
                int x = r.x + r.width - (int) (r.width * t);
                g.clipRect(x, r.y, r.width, r.height);
                g.drawImage(to, 0, 0, null);
            } else if (e.contains("wipe right")) {
                g.clipRect(r.x, r.y, (int) (r.width * t), r.height);
                g.drawImage(to, 0, 0, null);
            } else if (e.contains("wipe up")) {
                int y = r.y + r.height - (int) (r.height * t);
                g.clipRect(r.x, y, r.width, r.height);
                g.drawImage(to, 0, 0, null);
            } else if (e.contains("wipe down")) {
                g.clipRect(r.x, r.y, r.width, (int) (r.height * t));
                g.drawImage(to, 0, 0, null);
            } else {
                g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, t));
                g.drawImage(to, 0, 0, null);
            }
            g.dispose();
        }
    }

    private static synchronized void ensure() {
        int w = PCARD.pc.stack != null ? PCARD.pc.stack.width : 512;
        int h = PCARD.pc.stack != null ? PCARD.pc.stack.height : 342;
        if (canvas == null || canvas.getWidth() != w || canvas.getHeight() != h) {
            BufferedImage c = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
            Graphics g = c.getGraphics();
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, w, h);
            if (canvas != null) g.drawImage(canvas, 0, 0, null);
            g.dispose();
            canvas = c;
            shown = copy(c);
        }
        if (layer == null) {
            Runnable r = () -> {
                layer = new Layer();
                JLayeredPane lp = PCARD.pc.getLayeredPane();
                layer.setBounds(0, 0, Math.max(lp.getWidth(), 4096), Math.max(lp.getHeight(), 4096));
                lp.add(layer, JLayeredPane.PALETTE_LAYER);
            };
            if (SwingUtilities.isEventDispatchThread()) r.run();
            else try {
                SwingUtilities.invokeAndWait(r);
            } catch (Exception e) {
                logger.log(Level.ERROR, e.getMessage(), e);
            }
        }
    }

    private static BufferedImage copy(BufferedImage src) {
        BufferedImage c = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics g = c.getGraphics();
        g.drawImage(src, 0, 0, null);
        g.dispose();
        return c;
    }

    private static void repaint() {
        if (layer != null) layer.repaint();
    }

    /** the canvas was drawn */
    private static void changed() {
        active = true;
        if (!locked) {
            synchronized (HyperTint.class) {
                shown = copy(canvas);
            }
            repaint();
        }
    }

    // ---- pictures

    private static final Map<String, BufferedImage> cache = new LinkedHashMap<>(64, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, BufferedImage> eldest) {
            return size() > 48;
        }
    };

    /** @return the PICT (or cicn) resource of the name or id from the current and the used stacks */
    static BufferedImage picture(String nameOrId, String type) {
        if (nameOrId == null || nameOrId.isEmpty() || PCARD.pc.stack == null) return null;
        Rsrc rsrc = PCARD.pc.stack.rsrc;
        int id;
        try {
            id = Integer.parseInt(nameOrId.trim());
        } catch (NumberFormatException e) {
            id = rsrc.getRsrcIdAll(nameOrId.trim(), type);
            if (id == 0) return null;
        }
        String path = rsrc.getFilePathAll(id, type);
        logger.log(Level.DEBUG, "picture " + nameOrId + " -> " + id + " " + path);
        if (path == null) return null;
        synchronized (cache) {
            BufferedImage bi = cache.get(path);
            if (bi != null) return bi;
        }
        BufferedImage bi = type.equals("picture") ? readQuickTimePict(path) : null;
        if (bi == null) {
            try {
                bi = ImageIO.read(new File(path));
            } catch (Exception e) {
                logger.log(Level.WARNING, "cannot read " + path + ": " + e);
            }
        }
        if (bi != null) {
            synchronized (cache) {
                cache.put(path, bi);
            }
        }
        return bi;
    }

    /**
     * Reads a PICT with QuickTime, which decodes QuickTime compressed pictures (opcode 0x8200,
     * e.g. the "Graphics" codec) as well.
     * @return null if failed
     */
    static BufferedImage readQuickTimePict(String path) {
        try {
            TMovie.ensureSession();
            quicktime.std.image.GraphicsImporter gi = new quicktime.std.image.GraphicsImporter(new quicktime.io.QTFile(new File(path)));
            quicktime.qd.QDRect r = gi.getNaturalBounds();
            quicktime.qd.QDGraphics port = new quicktime.qd.QDGraphics(new quicktime.qd.QDRect(0, 0, r.getWidth(), r.getHeight()));
            gi.setGWorld(port, null);
            gi.draw();
            BufferedImage img = quicktime.internal.toolbox.QuickDrawManager.portImage(quicktime.QTObject.ID(port));
            if (img == null) return null;
            BufferedImage copy = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_RGB);
            Graphics g = copy.getGraphics();
            g.drawImage(img, 0, 0, null);
            g.dispose();
            port.disposeQTObject();
            gi.disposeQTObject();
            return copy;
        } catch (Throwable e) {
            logger.log(Level.DEBUG, "QuickTime cannot read " + path + ": " + e);
            return null;
        }
    }

    static int[] numbers(String s) {
        if (s == null || s.isBlank()) return new int[0];
        String[] items = s.split(",");
        int[] r = new int[items.length];
        for (int i = 0; i < items.length; i++) {
            try {
                r[i] = (int) Double.parseDouble(items[i].trim());
            } catch (NumberFormatException e) {
                return new int[0];
            }
        }
        return r;
    }

    static String param(String[] params, int i) {
        return params != null && i < params.length && params[i] != null ? params[i].trim() : "";
    }

    // ---- XCMDs

    /** @return true if the external command is handled here */
    static boolean handles(String name) {
        return switch (name.toLowerCase(Locale.ROOT)) {
            case "hypertint", "htudefpal", "htlock", "htchangepict", "htaddpict", "htremove", "htvisual",
                 "htsavepict", "httb1ts", "xcicon3" -> true;
            default -> false;
        };
    }

    static void call(String name, String[] params, Result result) {
        result.ret = 0;
        result.theResult = "";
        switch (name.toLowerCase(Locale.ROOT)) {
            case "hypertint" -> {
                ensure();
                active = true;
            }
            case "htudefpal", "htsavepict", "httb1ts" -> {
            }
            case "htremove" -> {
                active = false;
                locked = false;
                repaint();
            }
            case "htlock" -> {
                String v = param(params, 0).toLowerCase(Locale.ROOT);
                switch (v) {
                    case "true", "on" -> locked = true;
                    case "false", "forcefalse", "off" -> unlock();
                    default -> {
                        // bw, nobw, vbl, novbl, colormap... : display options
                    }
                }
            }
            case "htvisual" -> {
                pendingEffect = param(params, 0).toLowerCase(Locale.ROOT);
                int[] r = numbers(param(params, 2));
                pendingRect = r.length == 4 ? new Rectangle(r[0], r[1], r[2] - r[0], r[3] - r[1]) : null;
                int[] s = numbers(param(params, 3));
                pendingSpeed = s.length > 0 ? s[0] : 2;
            }
            case "htchangepict" -> {
                ensure();
                BufferedImage bi = picture(param(params, 0), "picture");
                if (bi == null) {
                    result.theResult = "Can't find picture " + param(params, 0);
                    logger.log(Level.INFO, result.theResult);
                    return;
                }
                synchronized (HyperTint.class) {
                    Graphics g = canvas.getGraphics();
                    g.drawImage(bi, 0, 0, null);
                    g.dispose();
                }
                changed();
            }
            case "htaddpict" -> {
                ensure();
                BufferedImage bi = picture(param(params, 0), "picture");
                if (bi == null) {
                    result.theResult = "Can't find picture " + param(params, 0);
                    logger.log(Level.INFO, result.theResult);
                    return;
                }
                int[] d = numbers(param(params, 1));
                int dx = d.length >= 2 ? d[0] : 0, dy = d.length >= 2 ? d[1] : 0;
                int[] s = param(params, 3).equalsIgnoreCase("srcRect") ? numbers(param(params, 4)) : new int[0];
                synchronized (HyperTint.class) {
                    Graphics g = canvas.getGraphics();
                    if (s.length == 4) {
                        int w = s[2] - s[0], h = s[3] - s[1];
                        g.drawImage(bi, dx, dy, dx + w, dy + h, s[0], s[1], s[2], s[3], null);
                    } else {
                        g.drawImage(bi, dx, dy, null);
                    }
                    g.dispose();
                }
                changed();
            }
            case "xcicon3" -> {
                ensure();
                BufferedImage bi = picture(param(params, 1), "cicn");
                if (bi == null) {
                    result.theResult = "Can't find cicn " + param(params, 1);
                    return;
                }
                int[] p = numbers(param(params, 0));
                if (p.length < 2) return;
                synchronized (HyperTint.class) {
                    Graphics g = canvas.getGraphics();
                    g.drawImage(bi, p[0] - bi.getWidth() / 2, p[1] - bi.getHeight() / 2, null);
                    g.dispose();
                }
                changed();
            }
        }
    }

    /** shows the drawn screen with the pending visual effect */
    private static void unlock() {
        locked = false;
        if (canvas == null) return;
        BufferedImage from;
        synchronized (HyperTint.class) {
            from = shown;
            shown = copy(canvas);
        }
        String effect = pendingEffect;
        pendingEffect = null;
        if (effect != null && !effect.isEmpty() && from != null && active && !"cut".equals(effect)) {
            transEffect = effect;
            transRect = pendingRect;
            transFrom = from;
            int ms = switch (Math.max(0, Math.min(pendingSpeed, 4))) {
                case 0 -> 600;
                case 1 -> 450;
                case 2 -> 330;
                case 3 -> 220;
                default -> 150;
            };
            long start = System.currentTimeMillis();
            while (true) {
                float t = (System.currentTimeMillis() - start) / (float) ms;
                if (t >= 1f) break;
                transProgress = t;
                repaint();
                try {
                    Thread.sleep(15);
                } catch (InterruptedException e) {
                    break;
                }
            }
            transProgress = 1f;
            transFrom = null;
        }
        repaint();
    }
}
