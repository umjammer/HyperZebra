/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package com.github.hyperZebra;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * Unit tests for the pure helpers of {@link HyperTint}.
 *
 * @version 0.00 2026/11/02 initial version <br>
 */
class HyperTintTest {

    // ---- numbers

    @Test
    void numbers() {
        assertArrayEquals(new int[] {1, 2, 3}, HyperTint.numbers("1,2,3"));
        // whitespace around items is tolerated
        assertArrayEquals(new int[] {1, 2}, HyperTint.numbers("1, 2"));
        // decimals are truncated
        assertArrayEquals(new int[] {1, 2}, HyperTint.numbers("1.9,2.1"));
    }

    @Test
    void numbersEmptyOrInvalid() {
        assertArrayEquals(new int[0], HyperTint.numbers(null));
        assertArrayEquals(new int[0], HyperTint.numbers(""));
        assertArrayEquals(new int[0], HyperTint.numbers("   "));
        // any bad item invalidates the whole list
        assertArrayEquals(new int[0], HyperTint.numbers("a"));
        assertArrayEquals(new int[0], HyperTint.numbers("1,x"));
    }

    // ---- param

    @Test
    void param() {
        assertEquals("a", HyperTint.param(new String[] {" a "}, 0));
        assertEquals("", HyperTint.param(null, 0));
        // out of bounds
        assertEquals("", HyperTint.param(new String[] {"a"}, 1));
        assertEquals("", HyperTint.param(new String[] {null}, 0));
    }

    // ---- handles

    @Test
    void handles() {
        assertTrue(HyperTint.handles("hypertint"));
        assertTrue(HyperTint.handles("HTLOCK"));
        assertTrue(HyperTint.handles("htchangepict"));
        assertTrue(HyperTint.handles("xcicon3"));
        // handled elsewhere (TXcmd)
        assertFalse(HyperTint.handles("movie"));
        assertFalse(HyperTint.handles("foobar"));
        assertFalse(HyperTint.handles(""));
    }
}
