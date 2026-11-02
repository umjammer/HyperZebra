/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package com.github.hyperZebra;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;


/**
 * Unit tests for the HyperTalk object name formatting of {@link OObject}.
 *
 * @version 0.00 2026/11/02 initial version <br>
 */
class OObjectNameTest {

    private static OObject of(String type, int id, String name) {
        OObject obj = new OObject();
        obj.objectType = type;
        obj.id = id;
        obj.name = name;
        return obj;
    }

    // ---- getShortShortName

    @Test
    void shortShortNameAbbreviatesType() {
        assertEquals("btn \"Go Home\"", of("button", 5, "Go Home").getShortShortName());
        assertEquals("fld \"Name\"", of("field", 4, "Name").getShortShortName());
        assertEquals("cd id 2", of("card", 2, "").getShortShortName());
        assertEquals("bg id 1", of("background", 1, "").getShortShortName());
        assertEquals("stack id 3", of("stack", 3, "").getShortShortName());
    }

    @Test
    void shortShortNameTruncatesLongNames() {
        assertEquals("btn \"Super Bu...\"", of("button", 5, "Super Button").getShortShortName());
        // exactly 9 chars is not truncated
        assertEquals("btn \"123456789\"", of("button", 5, "123456789").getShortShortName());
    }

    @Test
    void shortShortNameUsesIdWhenNameless() {
        assertEquals("btn id 5", of("button", 5, "").getShortShortName());
    }

    // ---- getShortName

    @Test
    void shortNameCapitalizesType() {
        assertEquals("Button \"Go Home\"", of("button", 5, "Go Home").getShortName());
        assertEquals("Button id 5", of("button", 5, "").getShortName());
        assertEquals("Card \"Main\"", of("card", 2, "Main").getShortName());
    }

    @Test
    void shortNameQualifiesChildrenWithParent() {
        OObject card = of("card", 2, "Main");
        OObject button = of("button", 5, "Go Home");
        button.parent = card;
        assertEquals("Card Button \"Go Home\"", button.getShortName());

        OObject bg = of("background", 1, "Bkg");
        OObject field = of("field", 4, "Name");
        field.parent = bg;
        assertEquals("Background Field \"Name\"", field.getShortName());
    }

    @Test
    void shortNameDoesNotQualifyOtherParents() {
        OObject stack = of("stack", 3, "MyStack");
        OObject button = of("button", 5, "Go Home");
        button.parent = stack;
        assertEquals("Button \"Go Home\"", button.getShortName());
    }

    // ---- getLongName

    @Test
    void longNameChainsParent() {
        OObject card = of("card", 2, "Main");
        OObject button = of("button", 5, "Go Home");
        button.parent = card;
        // NOTE: getShortName() already qualifies the button with its parent card,
        // so the parent appears twice in the long name
        assertEquals("Card Button \"Go Home\" of Card \"Main\"", button.getLongName());
    }

    @Test
    void longNameWithoutParent() {
        assertEquals("Button \"Go Home\"", of("button", 5, "Go Home").getLongName());
    }

    // ---- CapitalType

    @Test
    void capitalType() {
        assertEquals("Button", of("button", 1, "").CapitalType());
        assertEquals("Background", of("background", 1, "").CapitalType());
        assertEquals("Stack", of("stack", 1, "").CapitalType());
    }
}
