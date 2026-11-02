/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package com.github.hyperZebra;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;


/**
 * Unit tests for the pure static helpers of {@link TTalk}:
 * comment/continuation stripping and chunk ordinals.
 *
 * @version 0.00 2026/11/02 initial version <br>
 */
class TTalkHelperTest {

    // ---- stripContinuation

    @Test
    void continuationCharacters() {
        assertEquals("put 1 ", TTalk.stripContinuation("put 1 ¬"));
        assertEquals("put 1 ", TTalk.stripContinuation("put 1 ~"));
        // halfwidth katakana form used on the Mac
        assertEquals("put ", TTalk.stripContinuation("put ﾂ"));
    }

    @Test
    void noContinuation() {
        assertNull(TTalk.stripContinuation("put 1"));
        assertNull(TTalk.stripContinuation(""));
        // continuation char must be at the end of the line
        assertNull(TTalk.stripContinuation("put 1 ¬ more"));
    }

    @Test
    void comments() {
        // trailing comment, no continuation
        assertNull(TTalk.stripContinuation("put 1 -- comment"));
        assertNull(TTalk.stripContinuation("-- comment"));
        // comment removed, continuation kept
        assertEquals("put 1 ", TTalk.stripContinuation("put 1 ¬ -- comment"));
    }

    @Test
    void dashesInsideQuotesAreNotComments() {
        assertNull(TTalk.stripContinuation("put \"a--b\""));
        assertEquals("put \"a--b\" ", TTalk.stripContinuation("put \"a--b\" ¬"));
    }

    // ---- chunkOrdinal

    @Test
    void numberedOrdinals() {
        assertEquals(1, TTalk.chunkOrdinal("char", "first", "abc"));
        assertEquals(2, TTalk.chunkOrdinal("char", "second", "abc"));
        assertEquals(3, TTalk.chunkOrdinal("char", "third", "abc"));
        assertEquals(10, TTalk.chunkOrdinal("char", "tenth", "abc"));
        // ordinals are case insensitive
        assertEquals(1, TTalk.chunkOrdinal("char", "FIRST", "abc"));
    }

    @Test
    void lastMiddleMid() {
        assertEquals(3, TTalk.chunkOrdinal("char", "last", "abc"));
        assertEquals(3, TTalk.chunkOrdinal("char", "middle", "abcde"));
        assertEquals(2, TTalk.chunkOrdinal("char", "mid", "abcd"));
    }

    @Test
    void items() {
        assertEquals(3, TTalk.chunkOrdinal("item", "last", "a,b,c"));
        // a trailing delimiter counts as one item less
        assertEquals(2, TTalk.chunkOrdinal("item", "last", "a,b,c,"));
    }

    @Test
    void linesAndWords() {
        assertEquals(3, TTalk.chunkOrdinal("line", "last", "a\rb\nc"));
        assertEquals(3, TTalk.chunkOrdinal("word", "last", "a b  c"));
        // no words at all
        assertEquals(0, TTalk.chunkOrdinal("word", "last", "   "));
    }

    @Test
    void unknownOrdinal() {
        assertEquals(0, TTalk.chunkOrdinal("char", "nope", "abc"));
        assertEquals(0, TTalk.chunkOrdinal("char", null, "abc"));
    }
}
