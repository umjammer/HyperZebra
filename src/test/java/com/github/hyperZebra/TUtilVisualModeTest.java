/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package com.github.hyperZebra;

import java.util.ArrayList;
import java.util.Arrays;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;


/**
 * Unit tests for {@link TUtil#getVisualMode} (HyperTalk visual effect phrases).
 *
 * <p>The packed return value is {@code mode + (to << 8) + (spd << 16)},
 * with the default speed being 3 (normal).</p>
 *
 * @version 0.00 2026/11/02 initial version <br>
 */
class TUtilVisualModeTest {

    private static final int NORMAL_SPEED = 3 << 16;

    private static ArrayList<String> words(String phrase) {
        return new ArrayList<>(Arrays.asList(phrase.split("\\s+")));
    }

    @Test
    void emptyListReturnsZero() throws Exception {
        assertEquals(0, TUtil.getVisualMode(new ArrayList<>(), 0, 0));
        // start past the end of the list
        assertEquals(0, TUtil.getVisualMode(words("dissolve"), 1, 1));
    }

    @Test
    void simpleEffects() throws Exception {
        assertEquals(0 + NORMAL_SPEED, TUtil.getVisualMode(words("cut"), 0, 0));
        assertEquals(0 + NORMAL_SPEED, TUtil.getVisualMode(words("plain"), 0, 0));
        assertEquals(3 + NORMAL_SPEED, TUtil.getVisualMode(words("dissolve"), 0, 0));
        assertEquals(5 + NORMAL_SPEED, TUtil.getVisualMode(words("checkerboard"), 0, 0));
    }

    @Test
    void twoWordEffects() throws Exception {
        assertEquals(1 + NORMAL_SPEED, TUtil.getVisualMode(words("barn door open"), 0, 2));
        assertEquals(2 + NORMAL_SPEED, TUtil.getVisualMode(words("barn door close"), 0, 2));
        assertEquals(4 + NORMAL_SPEED, TUtil.getVisualMode(words("venetian blinds"), 0, 1));
        assertEquals(6 + NORMAL_SPEED, TUtil.getVisualMode(words("iris open"), 0, 1));
        assertEquals(7 + NORMAL_SPEED, TUtil.getVisualMode(words("iris close"), 0, 1));
    }

    @Test
    void directionalEffects() throws Exception {
        assertEquals(8 + NORMAL_SPEED, TUtil.getVisualMode(words("scroll left"), 0, 1));
        assertEquals(9 + NORMAL_SPEED, TUtil.getVisualMode(words("scroll right"), 0, 1));
        assertEquals(10 + NORMAL_SPEED, TUtil.getVisualMode(words("scroll up"), 0, 1));
        assertEquals(11 + NORMAL_SPEED, TUtil.getVisualMode(words("scroll down"), 0, 1));

        assertEquals(12 + NORMAL_SPEED, TUtil.getVisualMode(words("wipe left"), 0, 1));
        assertEquals(13 + NORMAL_SPEED, TUtil.getVisualMode(words("wipe right"), 0, 1));
        assertEquals(14 + NORMAL_SPEED, TUtil.getVisualMode(words("wipe up"), 0, 1));
        assertEquals(15 + NORMAL_SPEED, TUtil.getVisualMode(words("wipe down"), 0, 1));

        assertEquals(24 + NORMAL_SPEED, TUtil.getVisualMode(words("push left"), 0, 1));
        assertEquals(25 + NORMAL_SPEED, TUtil.getVisualMode(words("push right"), 0, 1));
        assertEquals(26 + NORMAL_SPEED, TUtil.getVisualMode(words("push up"), 0, 1));
        assertEquals(27 + NORMAL_SPEED, TUtil.getVisualMode(words("push down"), 0, 1));
    }

    @Test
    void zoomShrinkStretch() throws Exception {
        // zoom open/in share a mode, as do zoom close/out
        assertEquals(16 + NORMAL_SPEED, TUtil.getVisualMode(words("zoom open"), 0, 1));
        assertEquals(16 + NORMAL_SPEED, TUtil.getVisualMode(words("zoom in"), 0, 1));
        assertEquals(17 + NORMAL_SPEED, TUtil.getVisualMode(words("zoom close"), 0, 1));
        assertEquals(17 + NORMAL_SPEED, TUtil.getVisualMode(words("zoom out"), 0, 1));

        assertEquals(18 + NORMAL_SPEED, TUtil.getVisualMode(words("shrink to top"), 0, 2));
        assertEquals(19 + NORMAL_SPEED, TUtil.getVisualMode(words("shrink to bottom"), 0, 2));
        assertEquals(20 + NORMAL_SPEED, TUtil.getVisualMode(words("shrink to center"), 0, 2));

        assertEquals(21 + NORMAL_SPEED, TUtil.getVisualMode(words("stretch from top"), 0, 2));
        assertEquals(22 + NORMAL_SPEED, TUtil.getVisualMode(words("stretch from bottom"), 0, 2));
        assertEquals(23 + NORMAL_SPEED, TUtil.getVisualMode(words("stretch from center"), 0, 2));
    }

    @Test
    void toDestination() throws Exception {
        assertEquals(3 + (1 << 8) + NORMAL_SPEED, TUtil.getVisualMode(words("dissolve to black"), 0, 2));
        assertEquals(3 + (2 << 8) + NORMAL_SPEED, TUtil.getVisualMode(words("dissolve to white"), 0, 2));
        assertEquals(3 + (3 << 8) + NORMAL_SPEED, TUtil.getVisualMode(words("dissolve to grey"), 0, 2));
        // gray is an alias for grey
        assertEquals(3 + (3 << 8) + NORMAL_SPEED, TUtil.getVisualMode(words("dissolve to gray"), 0, 2));
        assertEquals(0 + (4 << 8) + NORMAL_SPEED, TUtil.getVisualMode(words("cut to inverse"), 0, 2));
    }

    @Test
    void speed() throws Exception {
        assertEquals(3 + (2 << 16), TUtil.getVisualMode(words("dissolve fast"), 0, 1));
        assertEquals(3 + (4 << 16), TUtil.getVisualMode(words("dissolve slow"), 0, 1));
        assertEquals(3 + (1 << 16), TUtil.getVisualMode(words("dissolve very fast"), 0, 2));
        assertEquals(3 + (5 << 16), TUtil.getVisualMode(words("dissolve very slow"), 0, 2));
    }

    @Test
    void fullPhrase() throws Exception {
        // mode 3, to black, fast
        assertEquals(3 + (1 << 8) + (2 << 16), TUtil.getVisualMode(words("dissolve to black fast"), 0, 3));
        // mode 0, to inverse, very slow
        assertEquals(0 + (4 << 8) + (5 << 16), TUtil.getVisualMode(words("cut to inverse very slow"), 0, 4));
    }

    @Test
    void caseInsensitive() throws Exception {
        assertEquals(3 + (1 << 8) + NORMAL_SPEED, TUtil.getVisualMode(words("DISSOLVE TO BLACK"), 0, 2));
    }

    @Test
    void startOffset() throws Exception {
        // effect phrase starts at index 1
        assertEquals(3 + NORMAL_SPEED, TUtil.getVisualMode(words("put dissolve"), 1, 1));
    }

    @Test
    void incompletePhrasesThrow() {
        assertThrows(XTalkException.class, () -> TUtil.getVisualMode(words("barn"), 0, 0));
        assertThrows(XTalkException.class, () -> TUtil.getVisualMode(words("barn door"), 0, 1));
        assertThrows(XTalkException.class, () -> TUtil.getVisualMode(words("venetian"), 0, 0));
        assertThrows(XTalkException.class, () -> TUtil.getVisualMode(words("iris"), 0, 0));
        assertThrows(XTalkException.class, () -> TUtil.getVisualMode(words("scroll sideways"), 0, 1));
        assertThrows(XTalkException.class, () -> TUtil.getVisualMode(words("shrink top"), 0, 1));
        assertThrows(XTalkException.class, () -> TUtil.getVisualMode(words("stretch top"), 0, 1));
        // "to" must be followed by a known destination
        assertThrows(XTalkException.class, () -> TUtil.getVisualMode(words("dissolve to purple"), 0, 2));
    }

    @Test
    void unknownLeadingWordFallsThroughToDefault() throws Exception {
        // an unrecognized leading word is not an error: mode stays 0
        assertEquals(0 + NORMAL_SPEED, TUtil.getVisualMode(words("foobar"), 0, 0));
    }
}
