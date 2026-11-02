/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package com.github.hyperZebra;

import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * Unit tests for the parameter parsers of {@link TXcmd}
 * (sizes, rects, colors, ports) and built-in XCMD detection.
 *
 * @version 0.00 2026/11/02 initial version <br>
 */
class TXcmdParserTest {

    // ---- isBuiltin

    @Test
    void isBuiltin() {
        assertTrue(TXcmd.isBuiltin("movie"));
        assertTrue(TXcmd.isBuiltin("MOVIE"));
        assertTrue(TXcmd.isBuiltin("xabout"));
        // handled by HyperTint
        assertTrue(TXcmd.isBuiltin("hypertint"));
        assertTrue(TXcmd.isBuiltin("htlock"));
        assertFalse(TXcmd.isBuiltin("foobar"));
        assertFalse(TXcmd.isBuiltin(""));
    }

    // ---- getSize

    @Test
    void getSizeFromPoint() {
        Point p = new Point();
        assertTrue(TXcmd.getSize("10,20", p));
        assertEquals(10, p.x);
        assertEquals(20, p.y);

        assertTrue(TXcmd.getSize("-1,-2", p));
        assertEquals(-1, p.x);
        assertEquals(-2, p.y);
    }

    @Test
    void getSizeFromRect() {
        Point p = new Point();
        assertTrue(TXcmd.getSize("0,0,100,50", p));
        assertEquals(100, p.x);
        assertEquals(50, p.y);

        assertTrue(TXcmd.getSize("10,20,30,45", p));
        assertEquals(20, p.x);
        assertEquals(25, p.y);
    }

    @Test
    void getSizeRejectsGarbage() {
        Point p = new Point();
        assertFalse(TXcmd.getSize("abc", p));
        assertFalse(TXcmd.getSize("10", p));
        assertFalse(TXcmd.getSize("a,b", p));
    }

    // ---- getRect

    @Test
    void getRectFromTwoPoints() {
        Rectangle r = new Rectangle();
        assertTrue(TXcmd.getRect("1,2,3,4", r));
        assertEquals(1, r.x);
        assertEquals(2, r.y);
        assertEquals(2, r.width);
        assertEquals(2, r.height);
    }

    @Test
    void getRectFromLocationOnly() {
        Rectangle r = new Rectangle();
        assertTrue(TXcmd.getRect("7,9", r));
        assertEquals(7, r.x);
        assertEquals(9, r.y);
    }

    @Test
    void getRectAcceptsDecimals() {
        Rectangle r = new Rectangle();
        assertTrue(TXcmd.getRect("1.5,2.5,3.5,4.5", r));
        assertEquals(1, r.x);
        assertEquals(2, r.y);
        assertEquals(2, r.width);
        assertEquals(2, r.height);
    }

    @Test
    void getRectRejectsGarbage() {
        Rectangle r = new Rectangle();
        assertFalse(TXcmd.getRect("abc", r));
        assertFalse(TXcmd.getRect("1,2,3", r));
    }

    // ---- getColor

    @Test
    void getColorFromRGB() {
        // HyperTalk rgb values are 0-65535
        assertEquals(new Color(255, 0, 0), TXcmd.getColor("65535,0,0"));
        assertEquals(new Color(128, 64, 32), TXcmd.getColor("32896,16384,8192"));
    }

    @Test
    void getColorFromHex() {
        // NOTE: only the first hex digit of each component is used
        assertEquals(new Color(15, 0, 0), TXcmd.getColor("#FF0000"));
        assertEquals(new Color(0, 15, 1), TXcmd.getColor("#00FF11"));
    }

    @Test
    void getColorByName() {
        assertEquals(Color.WHITE, TXcmd.getColor("white"));
        assertEquals(Color.BLACK, TXcmd.getColor("BLACK"));
        assertEquals(Color.RED, TXcmd.getColor("red"));
        assertEquals(Color.GREEN, TXcmd.getColor("green"));
        assertEquals(Color.BLUE, TXcmd.getColor("blue"));
        assertEquals(Color.YELLOW, TXcmd.getColor("yellow"));
        assertEquals(Color.ORANGE, TXcmd.getColor("orange"));
        assertEquals(Color.GRAY, TXcmd.getColor("gray"));
        assertEquals(Color.PINK, TXcmd.getColor("pink"));
    }

    @Test
    void unknownColorFallsBackToBlack() {
        assertEquals(Color.BLACK, TXcmd.getColor("banana"));
    }

    // ---- getPort

    @Test
    void getPortNumber() {
        assertEquals(3, TXcmd.getPort("3"));
        assertEquals(0, TXcmd.getPort("0"));
    }

    @Test
    void getPortNamed() {
        assertEquals(-1, TXcmd.getPort("cd"));
        assertEquals(-1, TXcmd.getPort("CD"));
        assertEquals(-1, TXcmd.getPort("card"));
        assertEquals(-1, TXcmd.getPort("direct"));
        assertEquals(-1, TXcmd.getPort("dir"));
        assertEquals(-2, TXcmd.getPort("cd buf"));
        assertEquals(-3, TXcmd.getPort("pri"));
        assertEquals(-4, TXcmd.getPort("cd alpha"));
    }

    @Test
    void unknownPortIsZero() {
        assertEquals(0, TXcmd.getPort("foobar"));
    }
}
