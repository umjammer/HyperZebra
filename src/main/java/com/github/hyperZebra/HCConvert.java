package com.github.hyperZebra;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.swing.JDialog;
import javax.swing.JProgressBar;

// import org.apache.tools.tar.TarEntry;
// import org.apache.tools.tar.TarInputStream;


public class HCConvert {

    public static boolean openHCStack(String path, OStack stack) {
        // System.out.println("openHCStack...");

        // Start progress display
        stack.barDialog = new JDialog(stack.pcard);
        stack.barDialog.setUndecorated(true);
        stack.barDialog.getContentPane().setLayout(new BorderLayout());
        stack.bar = new JProgressBar();
        stack.bar.setStringPainted(true);
        PCARD.pc.stack.bar.setSize(new Dimension(PCARD.pc.getWidth(), 20));
        stack.bar.setString("");
        stack.bar.setValue(0);
        stack.barOffset = 0;
        stack.barDialog.add("Center", stack.bar);
        stack.barDialog.setBounds(stack.pcard.getBounds());
        stack.barDialog.setVisible(true);
        stack.bar.paintImmediately(stack.bar.getBounds());
        stack.bar.setString("Make tar archive");

        // Create tar in Apple-Double format to read resources from Java
        File tarFile = MakeAppleDoubleTar(path);

        // Create directory
        if (!CreateStackFolder(stack, Paths.get(path))) {
            tarFile.delete();
            stack.barDialog.remove(stack.bar);
            stack.barDialog.dispose();
        }


        if (tarFile != null) {
            stack.bar.setString("readFromTar");
            // Extract data fork and resource fork from tar
            if (!readFromTar(tarFile, stack)) {
                System.out.println("Error occured at read data from tar file.");
            } else {
                // Read successful
                tarFile.delete();
                stack.barDialog.remove(stack.bar);
                stack.barDialog.dispose();

                stack.bar = null;
                stack.barDialog = null;

                stack.setName(new File(path).getName());

                return true;
            }
        } else { // Read directly without resource fork
            // System.out.println("Load File Direct");
            stack.bar.setString("Load File Direct");

            DataInputStream dis = null;
            try {
                FileInputStream fis = new FileInputStream(path);
                dis = new DataInputStream(fis);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            // Read data fork
            if (dis == null || !HCData.readDataFork(dis, stack)) {
                System.out.println("Error occured at reading data from file.");
                new GDialog(PCARDFrame.pc, PCARDFrame.pc.intl.getDialogText("Error occured at reading HyperCard stack data."),
                        null, "OK", null, null);
            } else {
                // If there's a file with a resource fork-like name, read resource fork
                // 1. File when decompressing AppleDoubleHeader
                String searchPath = new File(path).getParent() + File.separatorChar + "._" + new File(path).getName();
                File rsrcFile = new File(searchPath);
                if (!rsrcFile.exists()) {
                    searchPath = new File(path).getParent() + "__MACOSX" + File.separatorChar + new File(path).getName();
                    rsrcFile = new File(searchPath);
                }
                if (rsrcFile.exists()) {
                    DataInputStream rsrc_dis = null;
                    try {
                        rsrc_dis = new DataInputStream(new FileInputStream(rsrcFile));
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }

                    // Read as AppleDouble header file
                    HCResource.readAppleDoubleHeader(rsrc_dis, stack);
                } else {
                    System.out.println("Resource file is not found.");
                    new GDialog(PCARDFrame.pc, PCARDFrame.pc.intl.getDialogText("Resource data is not found."),
                            null, "OK", null, null);
                }
                // Success even without resource file
                stack.barDialog.remove(stack.bar);
                stack.barDialog.dispose();

                stack.bar = null;
                stack.barDialog = null;

                stack.setName(new File(path).getName());
                return true;
            }
        }

        // Read error
        if (tarFile != null) {
            tarFile.delete();
        }
        stack.barDialog.remove(stack.bar);
        stack.barDialog.dispose();
        return false;
    }


    public static boolean openMacBinaryStack(String path, OStack stack) {
        // System.out.println("openMacBinaryHCStack...");

        // Start progress display
        stack.barDialog = new JDialog(stack.pcard);
        stack.barDialog.setUndecorated(true);
        stack.bar = new JProgressBar();
        stack.bar.setStringPainted(true);
        PCARD.pc.stack.bar.setSize(new Dimension(PCARD.pc.getWidth(), 20));
        stack.bar.setString("");
        stack.bar.setValue(0);
        stack.barOffset = 0;
        stack.barDialog.add(stack.bar);
        stack.barDialog.setBounds(stack.pcard.getBounds());
        stack.barDialog.setVisible(true);
        stack.bar.paintImmediately(stack.bar.getBounds());

        // Create directory
        if (!CreateStackFolder(stack, Paths.get(path))) {
            stack.barDialog.remove(stack.bar);
            stack.barDialog.dispose();
        }

        // MacBinary has 128-byte header
        // + data fork (data fork length rounded up to 128 bytes)
        // + resource fork
        //
        // Decompressing lzh format files may result in MacBinary format.
        // Windows version of StuffItExpander has an option to convert to MacBinary after decompression

        FileInputStream fis = null;
        try {
            fis = new FileInputStream(path);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        byte[] header = new byte[128];
        try {
            fis.read(header, 0, 128);
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (header[65] == 'S' && header[66] == 'T' && header[67] == 'A' && header[68] == 'K') {
            int dataForkLen = ((0x00FF & header[83]) << 24) +
                    ((0x00FF & header[84]) << 16) +
                    ((0x00FF & header[85]) << 8) +
                    ((0x00FF & header[86]));
            // Read data fork portion into byte array
            if (dataForkLen > 100 * 1000 * 1000) {
                // Shouldn't be more than 100M
                dataForkLen = 10 * 1000 * 1000;// For now, set to 10M.
            }
            byte[] dataBytes = new byte[dataForkLen];
            try {
                fis.read(dataBytes, 0, dataForkLen);
            } catch (IOException e) {
                e.printStackTrace();
            }
            DataInputStream dis = new DataInputStream(new ByteArrayInputStream(dataBytes));

            // Read data fork
            if (HCData.readDataFork(dis, stack)) {
                dataBytes = null;

                int rsrcForkLen = ((0x00FF & header[87]) << 24) +
                        ((0x00FF & header[88]) << 16) +
                        ((0x00FF & header[89]) << 8) +
                        ((0x00FF & header[90]));

                byte[] rsrcBytes = null;
                if (rsrcForkLen > 0) { // Is there a resource fork?
                    // Skip to the next block after data fork
                    byte[] nullBytes = new byte[128 - ((dataForkLen - 1) % 128 + 1)];
                    try {
                        fis.read(nullBytes, 0, 128 - ((dataForkLen - 1) % 128 + 1));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    // Read resource fork portion into byte array
                    if (rsrcForkLen > 300 * 1000 * 1000) {
                        // Shouldn't be more than 300M
                        rsrcForkLen = 10 * 1000 * 1000;// For now, set to 10M.
                    }
                    rsrcBytes = new byte[rsrcForkLen];
                    try {
                        fis.read(rsrcBytes, 0, rsrcForkLen);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                if (rsrcForkLen == 0 || HCResource.readResourceFork(rsrcBytes, stack)) {
                    stack.barDialog.remove(stack.bar);
                    stack.barDialog.dispose();

                    stack.bar = null;
                    stack.barDialog = null;

                    stack.setName(new File(path).getName());
                    return true;
                } else {
                    System.out.println("Error occured at reading resource data from MacBinary file.");
                    new GDialog(PCARDFrame.pc, PCARDFrame.pc.intl.getDialogText("Error occured at reading MacBinary HyperCard stack data."),
                            null, "OK", null, null);
                }
            } else {
                System.out.println("Error occured at reading data from MacBinary file.");
                new GDialog(PCARDFrame.pc, PCARDFrame.pc.intl.getDialogText("Error occured at reading MacBinary HyperCard stack data."),
                        null, "OK", null, null);
            }
        }

        // Read error
        stack.barDialog.remove(stack.bar);
        stack.barDialog.dispose();
        return false;
    }


    private static boolean CreateStackFolder(OStack stack, Path path) {
        stack.bar.setValue(2);
        stack.bar.setString("Create a new folder");
        stack.bar.paintImmediately(stack.bar.getBounds());

        // Create directory
        Path newdir = Paths.get("/tmp", path.getFileName() + ".hzb");
        int i = 2;
        while (Files.exists(newdir)) {
            newdir = Paths.get("/tmp", path.getFileName() + " " + i + ".hzb");
            i++;
        }
        try {
            Files.createDirectory(newdir);
        } catch (IOException e) {
            new GDialog(PCARDFrame.pc, PCARDFrame.pc.intl.getDialogText("Can't create a new folder."),
                    null, "OK", null, null);
            return false;
        }
        stack.path = newdir.resolve("_stack.xml").toString();
        stack.file = Path.of(stack.path);

        return true;
    }


    public static File MakeAppleDoubleTar(String path) {
        // Must be on MacOSX
        String os = System.getProperty("os.name");
        if (os != null && os.startsWith("Mac OS X")) {
        } else {
//            new GDialog(PCARD.pc, PCARD.pc.intl.getDialogText("Converting from HyperCard Stack is need to run on MacOSX."),
//                    null, "OK", null, null);
            return null;
        }

        String srcpath = path;
        String tarpath = path;
        for (int i = 0; i < srcpath.length(); i++) {
            if (srcpath.charAt(i) >= 128 || srcpath.length() >= 100 - 2) { // If 2-byte characters or path is longer than tar can store
                String renameStr;
                int j = 1;
                while (true) {
                    renameStr = System.getProperty("java.io.tmpdir") + File.separatorChar + "hyperzebra_renamed" + j;
                    if (!new File(renameStr).exists()) {
                        break;
                    }
                    j++;
                }
                new File(srcpath).renameTo(new File(renameStr));
                srcpath = renameStr;

                {
                    // Creating a copy will lose the resource fork

                    // Create stack copy in temp directory as a workaround for Japanese paths
//                    srcpath = System.getProperty("java.io.tmpdir") + File.separatorChar + "hyperzebra_stack";
//
//                    try {
//                        FileChannel srcChannel = new FileInputStream(path).getChannel();
//                        FileChannel destChannel = new FileOutputStream(srcpath).getChannel();
//                        try {
//                            srcChannel.transferTo(0, srcChannel.size(), destChannel);
//                        } finally {
//                            srcChannel.close();
//                            destChannel.close();
//                        }
//                    } catch (FileNotFoundException e) {
//                        e.printStackTrace();
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
                }

                // Create tar archive in temp directory as a workaround for Japanese paths
                tarpath = System.getProperty("java.io.tmpdir") + File.separatorChar + "hyperzebra_work";
                break;
            }
        }

        String path2 = tarpath;
        File file = new File(path2 + ".tar");
        int i = 1;
        while (file.exists()) {
            path2 = tarpath + " " + i;
            file = new File(path2 + ".tar");
            i++;
        }

        // Save with Apple's tar
        try {
            ProcessBuilder pb = new ProcessBuilder("tar", "-cvf", path2 + ".tar", srcpath);
            // ProcessBuilder pb = new ProcessBuilder("tar", "-cvf", path+".tar", path);
            Process p = pb.start();
            p.waitFor();
            InputStream is = p.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            boolean isSuccess = false;
            for (; ; ) {
                String line = br.readLine();
                if (line == null) break;
                // System.out.println("tar>"+line);
                if (line.startsWith("a " + srcpath)) {
                    isSuccess = true;
                    break;
                }
            }
            if (!isSuccess) {
                // Not successful
                // System.out.println("Error occured at creating tar archive.");
            }
        } catch (IOException | InterruptedException e1) {
            e1.printStackTrace();
            file = null;
        }

        if (!srcpath.equals(path)) {
            // Restore if file was moved
            new File(srcpath).renameTo(new File(path));
        }

        if (file != null) {
            File ofile = new File(path2 + ".tar");
            if (ofile.exists()) {
                return file;
            }
        }
        return null;
    }

    static public String getConvertPath(String inPath) {
        byte[] sjisByte = null;
        try {
            sjisByte = inPath.getBytes("SJIS");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        String sjisPath = "";
        sjisPath = new String(sjisByte, StandardCharsets.UTF_8);

        return sjisPath;
    }

    public static boolean readFromTar(File tarFile, OStack stack) {
        // System.out.println("readFromTar...");
        FileInputStream fis;
        MyTarInputStream tin = null;
        try {
            fis = new FileInputStream(tarFile);
            tin = new MyTarInputStream(fis);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        // Handle tar with Apache ant library
        if (tin == null) return false;
        try {
            MyTarEntry tarEnt = tin.getNextEntry();
            while (tarEnt != null) {
                String name = tarEnt.getName();
                int size = tarEnt.getSize();
                ByteArrayOutputStream bos = new ByteArrayOutputStream(size);
                tin.copyEntryContents(bos);
                byte[] data = bos.toByteArray();
                DataInputStream dis = new DataInputStream(new ByteArrayInputStream(data));

                if (name.contains("._")) {
                    // Apple Double Header File (includes resource fork)
                    if (!HCResource.readAppleDoubleHeader(dis, stack)) {
                        System.out.println("Error occured at reading resouce fork from tar file.");
                    }
                    stack.barOffset += 25;
                } else if (!name.contains("PaxHeader")) {
                    // Data fork
                    if (!HCData.readDataFork(dis, stack)) {
                        System.out.println("Error occured at reading data fork from tar file.");
                        new GDialog(PCARDFrame.pc, PCARDFrame.pc.intl.getDialogText("Error occured at reading HyperCard stack data."),
                                null, "OK", null, null);
                    }
                    stack.barOffset += 73;
                }

                dis.close();
                tarEnt = tin.getNextEntry();
            }
            tin.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return true;
    }

    static class MyTarInputStream {

        private final FileInputStream fis;
        private int size;

        MyTarInputStream(FileInputStream fis) {
            this.fis = fis;
        }

        public void copyEntryContents(ByteArrayOutputStream bos) throws IOException {
            for (int i = 0; i < size; i++) {
                bos.write(fis.read());
            }
            // Padding
            if (512 - ((size - 1) % 512 + 1) > 0) {
                for (int i = 0; i < 512 - ((size - 1) % 512 + 1); i++) {
                    fis.read();
                }
            }
        }

        public MyTarEntry getNextEntry() throws IOException {
            // Read 512-byte header
            StringBuilder name = new StringBuilder(100);
            for (int i = 0; i < 100; i++) {
                name.append((char) fis.read());
            }
            for (int i = 0; i < 24; i++) {
                fis.read();
            }
            if (name.toString().isEmpty()) {
                return null; // Seems to be end
            }

            StringBuilder sizeStrbldr = new StringBuilder(12);
            int j;
            for (j = 0; j < 11; j++) {
                char c = (char) fis.read();
                if (c != '0') {
                    sizeStrbldr.append(c);
                    j++;
                    break;
                }
            }
            for (; j < 11; j++) {
                char c = (char) fis.read();
                sizeStrbldr.append(c);
                if (c <= 0) {
                    return null;
                }
            }
            String sizeStr = sizeStrbldr.toString();
            if (sizeStr.isEmpty()) {
                return null; // Seems to be end
            }
            size = Integer.valueOf(sizeStr, 8);
            for (int i = 0; i < 512 - 100 - 24 - 11; i++) {
                fis.read();
            }

            // Create MyTarEntry class and return name and size
            MyTarEntry entry = new MyTarEntry(name.toString(), size);
            return entry;
        }

        public void close() throws IOException {
            fis.close();
        }
    }

    static class MyTarEntry {

        String name;
        int size;

        MyTarEntry(String name, int size) {
            this.name = name;
            this.size = size;
        }

        public String getName() {
            return name;
        }

        public int getSize() {
            return size;
        }
    }
}
