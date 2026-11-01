/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package com.github.hyperZebra;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.io.Serial;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import quicktime.QTSession;
import quicktime.app.view.QTComponent;
import quicktime.app.view.QTFactory;
import quicktime.io.OpenFile;
import quicktime.io.OpenMovieFile;
import quicktime.io.QTFile;
import quicktime.std.movies.Movie;


/**
 * A movie (or picture) window of the "Movie" and "Picture" XCMDs, played with QuickTime.
 * <p>
 * The window is drawn over the card window, mouse events go through it.
 * Classic Mac movies whose 'moov' resource is in the resource fork ("&lt;name&gt;.rsrc")
 * are played from a copy of the data fork with the movie resource appended.
 */
final class TMovie {

    private static final Logger logger = System.getLogger(TMovie.class.getName());

    /** opened movies */
    private static final List<TMovie> movies = new CopyOnWriteArrayList<>();

    private static boolean sessionOpened;

    /** the window object */
    OWindow window;
    /** the window name */
    String name;
    private Movie movie;
    private QTComponent comp;
    private BufferedImage still;
    private final Holder holder = new Holder();

    private int x, y;
    /** size in card coordinates */
    private int width = 1, height = 1;
    private boolean visible;
    private boolean loop;
    private boolean closeOnFinish;
    private int startTime;
    private int endTime = -1;
    private int callBackTime = -1;
    private String callBackMsg;
    private boolean callBackDone;
    private float volume = 1f;
    private boolean mute;
    private float lastRate;

    /** a container which lets mouse events go to the card */
    private class Holder extends JPanel {

        @Serial
        private static final long serialVersionUID = 1L;

        Holder() {
            super(new BorderLayout());
            setOpaque(false);
        }

        @Override
        public boolean contains(int x, int y) {
            return false;
        }

        @Override
        protected void paintComponent(Graphics g) {
            if (still != null) g.drawImage(still, 0, 0, getWidth(), getHeight(), null);
        }
    }

    private TMovie(String name) {
        this.name = name;
    }

    // ---- opening

    /**
     * "Movie" XCMD: Movie name, windowStyle, loc, "visible"|"invisible", "floating"
     * @return error message, empty if succeeded
     */
    static String openMovie(String[] params) {
        String ref = HyperTint.param(params, 0);
        if (ref.isEmpty()) return "No movie specified";
        Path file = findMovie(ref);
        if (file == null) {
            return "Can't find movie " + ref;
        }
        String name = MacFiles.leafName(ref);
        OWindow existing = findWindow(name);
        if (existing != null && existing.movie != null) {
            return "";
        }
        TMovie m = new TMovie(name);
        try {
            m.load(file);
        } catch (Exception e) {
            logger.log(Level.WARNING, "cannot open movie " + file + ": " + e, e);
            return "Can't open movie " + ref;
        }
        m.setLocation(HyperTint.param(params, 2));
        m.visible = !HyperTint.param(params, 3).equalsIgnoreCase("invisible");
        m.attach();
        return "";
    }

    /**
     * "Picture" XCMD: Picture name, "resource"|"file", windowStyle, visible, ...
     * @return error message, empty if succeeded
     */
    static String openPicture(String[] params) {
        String ref = HyperTint.param(params, 0);
        BufferedImage bi = HyperTint.picture(ref, "picture");
        if (bi == null) return "Can't find picture " + ref;
        OWindow existing = findWindow(ref);
        if (existing != null && existing.movie != null) existing.movie.close();
        TMovie m = new TMovie(ref);
        m.still = bi;
        m.width = bi.getWidth();
        m.height = bi.getHeight();
        m.holder.setSize(bi.getWidth(), bi.getHeight());
        m.visible = HyperTint.param(params, 3).equalsIgnoreCase("true");
        m.attach();
        return "";
    }

    private static OWindow findWindow(String name) {
        for (OWindow w : OWindow.list) {
            if (w.name != null && w.name.equalsIgnoreCase(name)) return w;
        }
        return null;
    }

    private static Path findMovie(String ref) {
        List<String> dirs = new ArrayList<>();
        String documents = TTalk.getGlobalValue("documents");
        if (documents != null) {
            for (String line : documents.split("[\\r\\n]")) if (!line.isBlank()) dirs.add(line.trim());
        }
        dirs.addAll(MacFiles.stacksSearchPath());
        return MacFiles.find(ref, dirs);
    }

    /** opens the QuickTime session once */
    static synchronized void ensureSession() throws Exception {
        if (!sessionOpened) {
            QTSession.open();
            sessionOpened = true;
        }
    }

    private void load(Path file) throws Exception {
        ensureSession();
        Movie newMovie = openFile(file);
        Component newComp = null;
        QTComponent qc = QTFactory.makeQTComponent(newMovie);
        newComp = qc.asComponent();
        Movie old = movie;
        Component oldComp = comp != null ? comp.asComponent() : null;
        movie = newMovie;
        comp = qc;
        int w = Math.max(1, (int) newMovie.getBox().getWidth());
        int h = Math.max(1, (int) newMovie.getBox().getHeight());
        width = w;
        height = h;
        Component c = newComp;
        Runnable r = () -> {
            if (oldComp != null) holder.remove(oldComp);
            holder.add(c, BorderLayout.CENTER);
            holder.setSize(w, h);
            holder.validate();
        };
        if (SwingUtilities.isEventDispatchThread()) r.run();
        else SwingUtilities.invokeAndWait(r);
        if (old != null) {
            try {
                old.stop();
            } catch (Exception e) {
                logger.log(Level.TRACE, e.toString());
            }
        }
        startTime = 0;
        endTime = -1;
        callBackTime = -1;
        callBackMsg = null;
        applyVolume();
    }

    /** opens a movie file, the movie resource may be in the resource fork */
    private static Movie openFile(Path file) throws Exception {
        if (hasMoovAtom(file)) {
            return Movie.fromFile(OpenMovieFile.asRead(new QTFile(file.toFile())));
        }
        Path rsrc = MacFiles.resourceFork(file);
        byte[] moov = rsrc != null ? moovResource(Files.readAllBytes(rsrc)) : null;
        if (moov == null) {
            throw new IOException("no movie resource: " + file);
        }
        long dataLength = Files.size(file);
        Path dir = Path.of(System.getProperty("java.io.tmpdir"), "hyperzebra-movies");
        Files.createDirectories(dir);
        Path flat = dir.resolve(Integer.toHexString(file.toAbsolutePath().toString().hashCode()) + ".mov");
        if (!Files.exists(flat) || Files.size(flat) != dataLength + moov.length) {
            Path tmp = Files.createTempFile(dir, "movie", ".tmp");
            try (OutputStream os = Files.newOutputStream(tmp)) {
                Files.copy(file, os);
                os.write(moov);
            }
            Files.move(tmp, flat, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        }
        return Movie.fromDataFork(OpenFile.asRead(new QTFile(flat.toFile())), (int) dataLength, 0, null);
    }

    /** @return true if the data fork has a 'moov' atom at the top level */
    private static boolean hasMoovAtom(Path file) {
        try (RandomAccessFile f = new RandomAccessFile(file.toFile(), "r")) {
            long p = 0, length = f.length();
            while (p + 8 <= length) {
                f.seek(p);
                long size = f.readInt() & 0xffff_ffffL;
                int type = f.readInt();
                if (type == 0x6d6f6f76) return true; // moov
                if (size == 1) size = f.readLong();
                if (size < 8) return false;
                p += size;
            }
        } catch (IOException e) {
            logger.log(Level.TRACE, e.toString());
        }
        return false;
    }

    private static int u2(byte[] b, int o) {
        return ((b[o] & 0xff) << 8) | (b[o + 1] & 0xff);
    }

    private static int u4(byte[] b, int o) {
        return ((b[o] & 0xff) << 24) | ((b[o + 1] & 0xff) << 16) | ((b[o + 2] & 0xff) << 8) | (b[o + 3] & 0xff);
    }

    /** @return the first 'moov' resource of the resource fork, null if none */
    static byte[] moovResource(byte[] b) {
        if (b.length < 16) return null;
        int dataOffset = u4(b, 0), mapOffset = u4(b, 4);
        int typeList = u2(b, mapOffset + 24) + mapOffset;
        int types = u2(b, typeList) + 1;
        for (int i = 0; i < types; i++) {
            int o = typeList + 2 + i * 8;
            if (b[o] == 'm' && b[o + 1] == 'o' && b[o + 2] == 'o' && b[o + 3] == 'v') {
                int ref = u2(b, o + 6) + typeList;
                int off = u4(b, ref + 4) & 0xffffff;
                int len = u4(b, dataOffset + off);
                byte[] r = new byte[len];
                System.arraycopy(b, dataOffset + off + 4, r, 0, len);
                return r;
            }
        }
        return null;
    }

    // ---- window

    private void setLocation(String loc) {
        int[] p = HyperTint.numbers(loc);
        if (p.length >= 2) {
            x = p[0];
            y = p[1];
        } else {
            // "Card" etc.: center on the card
            int cw = PCARD.pc.stack != null ? PCARD.pc.stack.width : 512;
            int ch = PCARD.pc.stack != null ? PCARD.pc.stack.height : 342;
            x = (cw - width) / 2;
            y = (ch - height) / 2;
        }
    }

    private void attach() {
        window = new OWindow(this);
        movies.add(this);
        Runnable r = () -> {
            JLayeredPane lp = PCARD.pc.getLayeredPane();
            lp.add(holder, JLayeredPane.MODAL_LAYER);
            layout();
        };
        SwingUtilities.invokeLater(r);
        Watcher.ensure();
    }

    private void layout() {
        Runnable r = () -> {
            if (PCARD.pc.mainPane == null) return;
            Point o = SwingUtilities.convertPoint(PCARD.pc.mainPane, 0, 0, PCARD.pc.getLayeredPane());
            int z = Math.max(1, PCARD.pc.zoom);
            holder.setBounds(o.x + x * z, o.y + y * z, width * z, height * z);
            holder.setVisible(visible);
            holder.validate();
            holder.repaint();
            logger.log(Level.DEBUG, "movie " + name + ": holder " + holder.getBounds() + " visible=" + holder.isVisible() + " parent=" + (holder.getParent() != null) + " children=" + java.util.Arrays.stream(holder.getComponents()).map(c -> c.getClass().getSimpleName() + c.getBounds()).toList());
        };
        if (SwingUtilities.isEventDispatchThread()) r.run();
        else SwingUtilities.invokeLater(r);
    }

    void close() {
        logger.log(Level.DEBUG, "movie " + name + ": close");
        movies.remove(this);
        OWindow.list.remove(window);
        try {
            if (movie != null) movie.stop();
        } catch (Exception e) {
            logger.log(Level.TRACE, e.toString());
        }
        SwingUtilities.invokeLater(() -> {
            if (holder.getParent() != null) {
                java.awt.Container p = holder.getParent();
                p.remove(holder);
                p.repaint();
            }
            holder.removeAll();
        });
    }

    /** "send xxx to window" */
    void command(String cmd) {
        String c = cmd.trim().toLowerCase(Locale.ROOT);
        try {
            if (c.equals("play") || c.equals("start")) {
                setRate(1);
            } else if (c.equals("stop") || c.equals("pause")) {
                setRate(0);
            }
            // movieIdle etc.: the movie is tasked by itself
        } catch (Exception e) {
            logger.log(Level.DEBUG, e.toString());
        }
    }

    private int duration() throws Exception {
        return movie != null ? movie.getDuration() : 0;
    }

    private int end() throws Exception {
        int d = duration();
        return endTime >= 0 && endTime < d ? endTime : d;
    }

    private synchronized void setRate(float rate) throws Exception {
        if (movie == null) return;
        if (rate != 0) {
            int t = movie.getTime();
            if (t >= end() || t < startTime) movie.setTimeValue(startTime);
            callBackDone = false;
            // start() prerolls and draws, setRate() alone does not draw frames
            movie.start();
            if (rate != 1) movie.setRate(rate);
        } else {
            movie.stop();
        }
        lastRate = rate;
    }

    private void applyVolume() {
        try {
            if (movie != null) movie.setVolume(mute ? 0f : volume);
        } catch (Exception e) {
            logger.log(Level.TRACE, e.toString());
        }
    }

    private static boolean bool(String v) {
        return v.trim().equalsIgnoreCase("true");
    }

    private static int integer(String v) {
        int[] n = HyperTint.numbers(v.trim());
        return n.length > 0 ? n[0] : 0;
    }

    /** @return true if the property is of the movie window */
    boolean setProperty(String prop, String value) {
        logger.log(Level.DEBUG, "movie " + name + ": set " + prop + " to " + value);
        try {
            switch (prop) {
                case "rate" -> setRate(value.trim().isEmpty() ? 0 : Float.parseFloat(value.trim()));
                case "currtime" -> {
                    if (movie != null) movie.setTimeValue(integer(value));
                    callBackDone = false;
                }
                case "starttime" -> startTime = integer(value);
                case "endtime" -> endTime = value.isBlank() ? -1 : integer(value);
                case "loop" -> loop = bool(value);
                case "closeonfinish" -> closeOnFinish = bool(value);
                case "audiolevel" -> {
                    volume = Math.max(0, Math.min(integer(value), 256)) / 256f;
                    applyVolume();
                }
                case "mute" -> {
                    mute = bool(value);
                    applyVolume();
                }
                case "visible" -> {
                    visible = bool(value);
                    layout();
                }
                case "loc", "topleft" -> {
                    int[] p = HyperTint.numbers(value);
                    if (p.length >= 2) {
                        if (prop.equals("loc")) {
                            x = p[0] - width / 2;
                            y = p[1] - height / 2;
                        } else {
                            x = p[0];
                            y = p[1];
                        }
                        layout();
                    }
                }
                case "rect" -> {
                    int[] r = HyperTint.numbers(value);
                    if (r.length == 4) {
                        x = r[0];
                        y = r[1];
                        width = r[2] - r[0];
                        height = r[3] - r[1];
                        layout();
                    }
                }
                case "callbacktime" -> {
                    callBackTime = value.isBlank() ? -1 : integer(value);
                    callBackDone = false;
                }
                case "callbackmsg" -> callBackMsg = value;
                case "movie" -> {
                    Path file = findMovie(value.trim());
                    if (file != null) {
                        boolean wasPlaying = movie != null && movie.getRate() != 0;
                        load(file);
                        if (wasPlaying) setRate(1);
                    }
                }
                case "windowname", "name" -> {
                    name = value.trim();
                    if (window != null) window.name = name;
                }
                default -> {
                    // timeFormat, dontErase, fastIdle, movieCursorID, badge, controllerVisible,
                    // seeAllFrames, passMovieClick, ...
                    return knownProperty(prop);
                }
            }
        } catch (Exception e) {
            logger.log(Level.DEBUG, "movie " + name + " " + prop + ": " + e);
        }
        return true;
    }

    private static boolean knownProperty(String prop) {
        return switch (prop) {
            case "timeformat", "dontErase", "dontrase", "donterase", "fastidle", "moviecursorid", "badge",
                 "controllervisible", "seeallframes", "passmovieclick", "hilite", "palette", "volume",
                 "growbox", "closebox", "zoombox", "dithered", "scale", "nodither" -> true;
            default -> false;
        };
    }

    /** @return the value, null if it is not a property of the movie window */
    String getProperty(String prop) {
        try {
            return switch (prop) {
                case "rate" -> movie != null ? format(movie.getRate()) : "0";
                case "currtime" -> movie != null ? Integer.toString(movie.getTime()) : "0";
                case "duration" -> Integer.toString(duration());
                case "timescale" -> movie != null ? Integer.toString(movie.getTimeScale()) : "600";
                case "starttime" -> Integer.toString(startTime);
                case "endtime" -> Integer.toString(end());
                case "loop" -> Boolean.toString(loop);
                case "closeonfinish" -> Boolean.toString(closeOnFinish);
                case "audiolevel" -> Integer.toString(Math.round(volume * 256));
                case "mute" -> Boolean.toString(mute);
                case "visible" -> Boolean.toString(visible);
                case "loc" -> (x + width / 2) + "," + (y + height / 2);
                case "topleft" -> x + "," + y;
                case "rect" -> x + "," + y + "," + (x + width) + "," + (y + height);
                case "width" -> Integer.toString(width);
                case "height" -> Integer.toString(height);
                case "callbacktime" -> callBackTime < 0 ? "" : Integer.toString(callBackTime);
                case "callbackmsg" -> callBackMsg != null ? callBackMsg : "";
                case "name", "windowname" -> name;
                default -> knownProperty(prop) ? "" : null;
            };
        } catch (Exception e) {
            logger.log(Level.DEBUG, "movie " + name + " " + prop + ": " + e);
            return "";
        }
    }

    private static String format(float f) {
        return f == (int) f ? Integer.toString((int) f) : Float.toString(f);
    }

    private static final Pattern START_TIME_MSG = Pattern.compile(
            "(?i)\\s*set\\s+(?:the\\s+)?starttime\\s+of\\s+window\\s+\"?([^\"]+?)\"?\\s+to\\s+\"?(\\d+)\"?\\s*");

    /** called periodically: loops, call backs, finishing */
    private synchronized void watch() throws Exception {
        if (movie == null || lastRate == 0) return;
        int t = movie.getTime();
        if (callBackTime >= 0 && !callBackDone && t >= callBackTime) {
            callBackDone = true;
            if (callBackMsg != null && !callBackMsg.isBlank()) {
                Matcher m = START_TIME_MSG.matcher(callBackMsg);
                if (m.matches() && m.group(1).equalsIgnoreCase(name)) {
                    startTime = Integer.parseInt(m.group(2));
                } else {
                    TTalk.CallMessage(callBackMsg, "", null, false, true);
                }
            }
        }
        int end = end();
        if (t >= end || movie.isDone()) {
            if (loop) {
                movie.setTimeValue(startTime);
                callBackDone = false;
                if (movie.getRate() == 0) {
                    movie.start();
                    if (lastRate != 1) movie.setRate(lastRate);
                }
            } else {
                movie.stop();
                lastRate = 0;
                if (closeOnFinish) close();
            }
        }
    }

    /** a thread watching playing movies */
    private static final class Watcher extends Thread {

        private static Watcher watcher;

        static synchronized void ensure() {
            if (watcher == null) {
                watcher = new Watcher();
                watcher.start();
            }
        }

        Watcher() {
            super("TMovie-watcher");
            setDaemon(true);
        }

        @Override
        public void run() {
            while (true) {
                for (TMovie m : movies) {
                    try {
                        m.watch();
                    } catch (Exception e) {
                        logger.log(Level.TRACE, e.toString());
                    }
                }
                try {
                    Thread.sleep(20);
                } catch (InterruptedException e) {
                    return;
                }
            }
        }
    }

    /** closes all movie windows */
    static void closeAll() {
        for (TMovie m : movies) m.close();
    }
}
