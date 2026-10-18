package com.github.hyperZebra;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serial;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.MenuSelectionManager;
import javax.swing.RepaintManager;
import javax.swing.SwingUtilities;


class PCARDFrame extends JFrame {

    @Serial
    private static final long serialVersionUID = 1L;

    static PCARD pc = null;
    static boolean useGrid = true;
    static int gridSize = 1;
    int zoom = 2;
    String selectedFont = null;
    int fontSizeOffset = -2;
    String textEncoding = "Auto";
    MyPanel mainPane;
    OStack stack;
    static OStack home;
    int bit = 1;

    public static String getFontName(String origFont) {
        if (pc != null && pc.selectedFont != null && !pc.selectedFont.isEmpty()) {
            return pc.selectedFont;
        }
        return origFont;
    }

    public static int getFontSize(int origSize) {
        int offset = (pc != null) ? pc.fontSizeOffset : -2;
        return Math.max(6, origSize + offset);
    }
    float bitLeft;
    float bitTop;
    ToolInterface tool;
    CPButton fore;
    CPButton back;
    GradButton grad;
    PatButton pat;
    TransButton trans;
    boolean fill;
    AffineTransform selectaf;
    int blendMode;
    int blendLevel = 100;

    // Paint
    BufferedImage mainImg;
    BufferedImage bgImg;
    BufferedImage undoBuf;
    BufferedImage redoBuf;

    public void end() {
        if (mainImg != null) mainImg.flush();
        mainImg = null;
        if (bgImg != null) bgImg.flush();
        bgImg = null;
        if (undoBuf != null) undoBuf.flush();
        undoBuf = null;
        if (redoBuf != null) redoBuf.flush();
        redoBuf = null;
        pat = null;
        fore = null;
        back = null;
        grad = null;
        stack = null;
        mainPane = null;
        tool = null;
    }

    public BufferedImage getSurface() {
        if (PaintTool.editBackground) return bgImg;
        return mainImg;
    }

    public void setSurface(BufferedImage bi) {
        if (PaintTool.editBackground) bgImg = bi;
        mainImg = bi;
    }

    public static void setNewBounds() {
        Rectangle r = pc.getBounds();
        if (pc.toolbar == null || pc.toolbar.tb == null) return;
        int z = pc.zoom;
        pc.setBounds(r.x, r.y, pc.stack.width * z + pc.toolbar.getTWidth(), pc.stack.height * z + pc.toolbar.getTHeight() + pc.getInsets().top + pc.getJMenuBar().getHeight());
        if (pc.stack != null && pc.toolbar != null && pc.stack.scroll != null) {
            pc.mainPane.setBounds(pc.toolbar.getTWidth() - pc.stack.scroll.x * z, pc.toolbar.getTHeight() - pc.stack.scroll.y * z, pc.stack.width * z, pc.stack.height * z);
        }
    }
}


public class PCARD extends PCARDFrame /*implements MRJOpenDocumentHandler*/ {

    @Serial
    private static final long serialVersionUID = 1L;

    static Logger logger = Logger.getLogger(PCARD.class.getName());

    static String AppName = "HyperZebra";

    static String version = "3.0";
    static String longVersion = "3.0a5";
    GMenu menu;
    GMenu paintMenu;
    GMsg msg;
    International intl;
    static boolean useDoubleBuffer = false; // Can we save memory by turning off the buffer?
    Pidle pidle;
    PaintIdle paidle;
    DropTarget drop;
    MyDropTargetListener droplistener;
    GToolBar toolbar;


    // Properties
    static boolean lockedScreen = false;
    static int visual = 0;
    static int toVisual = 0; /* to black1 white2 grey3 inverse4 */
    static int visSpd = 3; /* very fast1 fast2 normal3 slow4 veryslow5 */
    static int editMode = 0; // 0:browse  1:outline emphasis
    static String scriptFont = "";
    static int scriptFontSize = 12;
    static int userLevel = 5;
    String textFont = "";
    int textSize = 12;
    int textStyle = 0;
    // int textAlign = 0;
    int foundIndex = 0;
    String foundText;
    String foundObject;

    public static void main(String[] args) {
        pc = new PCARD();

        Toolkit.getDefaultToolkit().getSystemEventQueue().push(new java.awt.EventQueue() {
            @Override
            protected void dispatchEvent(java.awt.AWTEvent event) {
                if (event instanceof java.awt.event.MouseEvent me && pc != null && pc.zoom > 1 && me.getSource() == pc && pc.mainPane != null && pc.mainPane.isShowing()) {
                    if (MenuSelectionManager.defaultManager().getSelectedPath().length == 0) {
                        java.awt.Point pt = SwingUtilities.convertPoint(pc.mainPane, 0, 0, pc);
                        if (me.getX() >= pt.x && me.getY() >= pt.y) {
                            int mx = me.getX() - pt.x;
                            int my = me.getY() - pt.y;
                            me.translatePoint(mx / pc.zoom - mx, my / pc.zoom - my);
                        }
                    }
                }
                super.dispatchEvent(event);
            }
        });

        RepaintManager.setCurrentManager(new RepaintManager() {
            @Override
            public void addDirtyRegion(JComponent c, int x, int y, int w, int h) {
                if (pc != null && pc.mainPane != null && pc.zoom > 1 && SwingUtilities.isDescendingFrom(c, pc.mainPane)) {
                    super.addDirtyRegion(pc.rootPane, 0, 0, pc.rootPane.getWidth(), pc.rootPane.getHeight());
                } else {
                    super.addDirtyRegion(c, x, y, w, h);
                }
            }
        });

        // Set window icon
        try {
            Image icon = ImageIO.read(new File("." + File.separatorChar + "resource" + File.separatorChar + "icon.png"));
            pc.setIconImage(icon);
        } catch (IOException e1) {
            logger.log(Level.INFO, "." + File.separatorChar + "resource" + File.separatorChar + "icon.png");
        }

        new OWindow(pc, true);
        pc.rootPane.setDoubleBuffered(true);
        pc.getRootPane().setOpaque(false);
        pc.setBackground(Color.WHITE);

        pc.setLayout(new BorderLayout());
        pc.mainPane = new MyPanel(pc);
        pc.mainPane.setDoubleBuffered(false);
//        pc.setContentPane(pc.mainPane /*, BorderLayout.CENTER */); // This makes the toolbar broken
        pc.add(pc.mainPane, BorderLayout.CENTER);

        Cursor cr = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
        pc.setCursor(cr);

        try {
            pc.intl = new International();
        } catch (Exception e) {
            logger.log(Level.INFO, e.getMessage(), e);
        }

        pc.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });

        OStack ostack = new OStack(pc);
        pc.stack = ostack;

        pc.msg = new GMsg(pc, "");
        new VariableWatcher();
        new MessageWatcher();

        pc.menu = new GMenu(pc, 0);
        pc.paintMenu = new GMenu(pc, 1);
        JMenuBar menubar = new JMenuBar();
        pc.setJMenuBar(menubar);
        GMenu.menuUpdate(PCARD.pc.menu.mb);

        TTalk.talk = new TTalk(); // There is a thread restart process in GUI.java in case TTalk terminates abnormally
        TTalk.talk.start();

        String homepath = "./home";
        if (new File(homepath).exists()) {
            // ostack.openStackFileInThread(homepath, true);
            try {
                TTalk.doScriptforMenu("start using stack " + "\"" + homepath + "\"");
            } catch (XTalkException e1) {
                logger.log(Level.INFO, e1.getMessage(), e1);
            }
            home = ostack;
        }

        String path = "./home";
        if (args.length > 0) {
            path = args[0];
        }

        if (path != null && new File(path).exists()) {
            OStack.openStackFile(path, false);
        } else if (home != null) {
            home.buildStackFile(false);
        } else {
            PCARD.failureOpenFile(path);
        }

        HyperZebra.installMacHandler();

        // Idle processing
        pc.pidle = new Pidle();
        pc.pidle.start();

        pc.paidle = new PaintIdle();
        pc.paidle.start();
    }

    static void successOpenFile() throws IOException {
        PCARD.lockedScreen = true;

        if (pc.drop != null) {
            pc.drop.removeDropTargetListener(pc.droplistener);
            pc.droplistener = null;
        }
        PaintTool.owner = PCARDFrame.pc;
        PCARDFrame.pc.stack.buildStackFile(false);

        // Add to recently used stacks
        if (PCARDFrame.pc.stack.path.startsWith("./home")) {
            return;
        }

        // First, read
        Path recentFile = Path.of("resource_trash", "recent.txt");
        String[] recents = new String[1];
        if (Files.exists(recentFile)) {
            recents = Files.readAllLines(recentFile).toArray(String[]::new);
        }

        // Add to the first line
        String path = PCARDFrame.pc.stack.path;
        if (new File(path).getName().equals("_stack.xml") ||
                new File(path).getName().equals("toc.xml")) {
            path = new File(path).getParent();
        }
        recents[0] = path;

        // Write to file
        {
            OutputStream fos = null;
            try {
                if (!Files.exists(recentFile.getParent())) {
                    Files.createDirectories(recentFile.getParent());
                }
                fos = Files.newOutputStream(recentFile); // TODO directory
                for (int i = 0; i < recents.length; i++) {
                    if (i == 0 || !recents[i].equals(path)) {
                        fos.write(recents[i].getBytes());
                        if (i < recents.length - 1) fos.write("\n".getBytes());
                    }
                }
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        GMenu.BuildRecentMenu((JMenu) PCARD.pc.getJMenuBar().getComponent(0));
    }

    static void failureOpenFile(String path) {
        if (pc.stack.curCard != null) {
            pc.stack.curCard.removeData();
            if (pc.stack.curCard.bg != null) pc.stack.curCard.bg.removeData();
        }
        pc.emptyWindow();
        if (!path.equals("./home")) {
            new GDialog(pc, PCARDFrame.pc.intl.getDialogText("Could't open the file."),
                    null, "OK", null, null);
        }
    }

    public void emptyWindow() {
        setTitle(PCARDFrame.pc.intl.getDialogText("Drop file here"));

        Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
        if (d != null) {
            setBounds(d.width / 2 - 400 / 2, d.height / 2 - 320 / 2 - 20, 400, 320 + 20);
        } else {
            setBounds(0, 0 - 320 - 20, 400, 320 + 20);
        }
        setVisible(true);
        // setLocationRelativeTo(null);
        droplistener = new MyDropTargetListener();
        drop = new DropTarget(pc.mainPane, droplistener);
    }

    private static class MyDropTargetListener extends DropTargetAdapter {

        /*
         * @see java.awt.dnd.DropTargetListener#drop(java.awt.dnd.DropTargetDropEvent)
         */
        @Override
        public void drop(DropTargetDropEvent e) {
            try {
                Transferable transfer = e.getTransferable();
                if (transfer.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                    if (pc.stack != null) {
                        pc.stack.clean();
                        pc.stack = new OStack(pc);
                    }
                    e.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
                    @SuppressWarnings("unchecked")
                    List<File> fileList =
                            (List<File>) (transfer.getTransferData(DataFlavor.javaFileListFlavor));
                    for (File file : fileList) {
                        OStack.openStackFile(file.toString(), false);
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    PCARD() {
        GUI.gui.addMouseListener(this);
        this.addKeyListener(GUI.gui);
        // this.addWindowListener(paintGUI.gui);
        this.addWindowListener(new PCARDWindowListener(this));
    }
}


class PCARDWindowListener implements WindowListener {

    PCARD owner;

    PCARDWindowListener(PCARD owner) {
        this.owner = owner;
    }

    @Override
    public void windowActivated(WindowEvent arg0) {
        if (owner != null && owner.toolbar != null) {
            owner.toolbar.activate();
        }
    }

    @Override
    public void windowClosed(WindowEvent arg0) {
    }

    @Override
    public void windowClosing(WindowEvent arg0) {
    }

    @Override
    public void windowDeactivated(WindowEvent arg0) {
        if (owner != null && owner.toolbar != null) {
            owner.toolbar.deactivate();
        }
    }

    @Override
    public void windowDeiconified(WindowEvent arg0) {
    }

    @Override
    public void windowIconified(WindowEvent arg0) {
    }

    @Override
    public void windowOpened(WindowEvent arg0) {
    }
}


class Pidle extends Thread {

    @Override
    public void run() {
        this.setName("idle thread");
        while (true) {
            if (PCARDFrame.pc.stack != null && PCARDFrame.pc.stack.curCard != null && PCARDFrame.pc.tool == null) {
                if (TTalk.idle && PCARD.pc.isActive()) {
                    if (System.currentTimeMillis() - TTalk.lastErrorTime > 10 * 1000) {
                        TTalk.CallMessage("idle", "", PCARDFrame.pc.stack.curCard, true, false);
                    }
                }
                GUI.keyEventCheck();
                // PCARD.pc.stack.pcard.requestFocusInWindow(); // This prevents typing in fields
            }
            try {
                sleep(100); // 10 times per 1000msec
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            if (Thread.currentThread().isInterrupted()) {
                break;
            }
        }
    }
}
