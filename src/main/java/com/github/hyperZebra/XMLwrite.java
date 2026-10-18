package com.github.hyperZebra;

import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;


public class XMLwrite {

    OStack stack;
    saveThread saveThread;

    public XMLwrite(OStack stack) {
        this.stack = stack;

        saveThread = new saveThread();
        saveThread.start();
    }

    public void saveStackNow() {
        if (stack.cantModify) return;
        Path fname = stack.file;
        if (fname.getFileName().toString().endsWith(".xml")
                && Files.exists(stack.file) && !Files.isWritable(stack.file)) return;

        XMLOutputFactory factory = XMLOutputFactory.newInstance();

        StringWriter stringWriter = new StringWriter();
        try {
            XMLStreamWriter writer = factory.createXMLStreamWriter(stringWriter);

            writer.writeStartDocument("utf-8", "1.0");
            writer.writeCharacters("\n");
            // writer.writeDTD("<!DOCTYPE stackfile PUBLIC \"- // Apple, Inc. // DTD stackfile V 2.0 // EN\" \"\" >");
            writer.writeCharacters("\n");
            writer.writeStartElement("stackfile");
            writer.writeCharacters("\n\t");

            stack.writeXML(writer);
            stack.changed = false;
            stack.writeFontXML(writer);
            stack.writeStyleXML(writer);
            for (int i = 0; i < stack.bgCacheList.size(); i++) {
                stack.bgCacheList.get(i).writeXML(writer);
                stack.bgCacheList.get(i).changed = false;
            }
            if (stack.cardIdList.size() >= stack.cdCacheList.size()) {
                for (int i = 0; i < stack.cardIdList.size(); i++) {
                    int id = stack.cardIdList.get(i);
                    for (int j = 0; j < stack.cdCacheList.size(); j++) {
                        if (stack.cdCacheList.get(j).id == id) {
                            stack.cdCacheList.get(j).writeXML(writer);
                            stack.cdCacheList.get(j).changed = false;
                            break;
                        }
                    }
                }
            } else {
                for (int i = 0; i < stack.cdCacheList.size(); i++) {
                    stack.cdCacheList.get(i).writeXML(writer);
                    stack.cdCacheList.get(i).changed = false;
                } // This would disrupt the order
            }
            stack.rsrc.writeXML(writer);
            stack.rsrc.writePlteXml(writer);
            stack.rsrc.writeAddColorXML(writer);
            stack.rsrc.writeXcmdXML(writer);

            writer.writeEndDocument();

            writer.close();

            Path file = stack.file;
            if (!stack.file.getFileName().toString().endsWith(".xml")) {
                file = stack.file.getParent().resolve("_stack.xml");
            }
            if (!Files.exists(file) || Files.isWritable(file)) {
                OutputStream stream = Files.newOutputStream(file);
                stream.write(stringWriter.toString().getBytes(StandardCharsets.UTF_8));
                stream.close();
            }
        } catch (XMLStreamException | IOException e) {
            e.printStackTrace();
        }
    }

    class saveThread extends Thread {

        // Monitor changes when idle
        @Override
        public void run() {
            setName("saveXMLThread");
            setPriority(MIN_PRIORITY);

            if (!Files.exists(stack.file)) {
                // Create immediately if XML file doesn't exist
                saveStackNow();
            }

            while (true) {
                try {
                    sleep(1000); // Once per second
                } catch (InterruptedException e) {
                    // this.interrupt();
                }
                // if(isInterrupted()) break;
                if (PCARD.pc != stack.pcard) break;
                if (stack.cdCacheList == null) break;

                if (TTalk.idle) {
                    if (stack.changed) {
                        saveStackNow();
                        continue;
                    }
                    for (int i = 0; i < stack.cdCacheList.size(); i++) {
                        if (stack.cdCacheList.get(i).changed) {
                            saveStackNow();
                        }
                    }
                    for (int i = 0; i < stack.bgCacheList.size(); i++) {
                        if (stack.bgCacheList.get(i).changed) {
                            saveStackNow();
                        }
                    }
                }
            }
        }
    }
}
