/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package com.github.hyperZebra;

import java.io.StringReader;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * Unit tests for {@link XMLRead#bool(XMLStreamReader)}.
 *
 * @version 0.00 2026/11/02 initial version <br>
 */
class XMLReadBoolTest {

    /** reader positioned just inside the wrapper element, where bool() expects it */
    private static XMLStreamReader readerAt(String boolElement) throws Exception {
        XMLStreamReader reader = XMLInputFactory.newInstance()
                .createXMLStreamReader(new StringReader("<x>" + boolElement + "</x>"));
        reader.next(); // START_ELEMENT <x>
        return reader;
    }

    @Test
    void trueElement() throws Exception {
        assertTrue(XMLRead.bool(readerAt("<true/>")));
    }

    @Test
    void falseElement() throws Exception {
        assertFalse(XMLRead.bool(readerAt("<false/>")));
    }

    @Test
    void wrongElementThrows() throws Exception {
        assertThrows(XMLStreamException.class, () -> XMLRead.bool(readerAt("<foo/>")));
    }

    @Test
    void missingElementThrows() throws Exception {
        assertThrows(XMLStreamException.class, () -> XMLRead.bool(readerAt("")));
    }
}
