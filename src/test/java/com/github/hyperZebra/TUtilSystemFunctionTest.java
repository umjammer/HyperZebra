/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package com.github.hyperZebra;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;


/**
 * Unit tests for the pure math/string branches of
 * {@link TUtil#CallSystemFunction} and {@link TUtil#IEEE_Round}.
 *
 * @version 0.00 2026/11/02 initial version <br>
 */
class TUtilSystemFunctionTest {

    private static String call(String message, String... params) throws XTalkException {
        Result result = TUtil.CallSystemFunction(message, params, null, new MemoryData(), true);
        assertEquals(0, result.ret, message + " should be handled");
        return result.theResult;
    }

    // ---- trunc

    @Test
    void trunc() throws Exception {
        assertEquals("3", call("trunc", "3.7"));
        assertEquals("3", call("trunc", "3"));
        // truncation toward zero, not floor
        assertEquals("-3", call("trunc", "-3.7"));
        // infinity constant
        assertEquals("2147483647", call("trunc", "∞"));
    }

    // ---- round (IEEE / banker's rounding)

    @Test
    void round() throws Exception {
        assertEquals("2", call("round", "2.4"));
        assertEquals("3", call("round", "2.6"));
        // .5 rounds to the nearest even number
        assertEquals("2", call("round", "2.5"));
        assertEquals("4", call("round", "3.5"));
    }

    @Test
    void ieeeRound() {
        assertEquals(2, TUtil.IEEE_Round(2.4));
        assertEquals(3, TUtil.IEEE_Round(2.6));
        assertEquals(0, TUtil.IEEE_Round(0.5));
        assertEquals(2, TUtil.IEEE_Round(1.5));
        assertEquals(2, TUtil.IEEE_Round(2.5));
        assertEquals(4, TUtil.IEEE_Round(3.5));
        // negative halves truncate toward zero
        assertEquals(-2, TUtil.IEEE_Round(-2.5));
    }

    // ---- abs

    @Test
    void abs() throws Exception {
        assertEquals("4", call("abs", "-4"));
        assertEquals("4", call("abs", "4"));
        assertEquals("4.5", call("abs", "-4.5"));
    }

    // ---- min / max / sum / average

    @Test
    void minMax() throws Exception {
        assertEquals("1", call("min", "3,1,2"));
        assertEquals("3", call("max", "3,1,2"));
        assertEquals("1", call("min", "3", "1", "2"));
        assertEquals("3", call("max", "3", "1", "2"));
        assertEquals("-5", call("min", "-5,3"));
    }

    @Test
    void sumAverage() throws Exception {
        assertEquals("6", call("sum", "1,2,3"));
        // integral results are formatted as ints
        assertEquals("2", call("average", "1,2,3"));
        assertEquals("1.5", call("average", "1,2"));
    }

    // ---- transcendental functions

    @Test
    void mathFunctions() throws Exception {
        assertEquals("3.0", call("sqrt", "9"));
        assertEquals("0.0", call("sin", "0"));
        assertEquals("1.0", call("cos", "0"));
        assertEquals(Double.toString(Math.atan(1)), call("atan", "1"));
        assertEquals(Double.toString(Math.log(8) / Math.log(2)), call("log2", "8"));
        assertEquals(Double.toString(Math.pow(2, 10)), call("exp2", "10"));
    }

    // ---- string functions

    @Test
    void chartonumNumtochar() throws Exception {
        assertEquals("65", call("chartonum", "A"));
        assertEquals("0", call("chartonum", ""));
        // NOTE: known bug - numtochar computes theResult but never sets ret = 0,
        // so the caller sees "not found" and keeps searching up the object chain
        Result result = TUtil.CallSystemFunction("numtochar", new String[] {"65"}, null, new MemoryData(), true);
        assertEquals(1, result.ret);
        assertEquals("A", result.theResult);
        result = TUtil.CallSystemFunction("numtochar", new String[] {"0"}, null, new MemoryData(), true);
        assertEquals("", result.theResult);
        result = TUtil.CallSystemFunction("numtochar", new String[] {"65536"}, null, new MemoryData(), true);
        assertEquals("", result.theResult);
    }

    @Test
    void lengthOffset() throws Exception {
        assertEquals("5", call("length", "hello"));
        assertEquals("0", call("length", ""));
        // offset is 1-based, 0 when not found
        assertEquals("2", call("offset", "b", "abc"));
        assertEquals("1", call("offset", "a", "abc"));
        assertEquals("0", call("offset", "z", "abc"));
    }

    // ---- dispatch behavior

    @Test
    void messageIsCaseInsensitive() throws Exception {
        assertEquals("3", call("TRUNC", "3.7"));
    }

    @Test
    void unknownFunctionIsNotHandled() throws Exception {
        Result result = TUtil.CallSystemFunction("foobar", new String[] {"1"}, null, new MemoryData(), true);
        assertEquals(1, result.ret);
        assertNull(result.theResult);
    }

    @Test
    void missingArgumentsThrow() {
        assertThrows(XTalkException.class, () -> TUtil.CallSystemFunction("trunc", new String[0], null, new MemoryData(), true));
        assertThrows(XTalkException.class, () -> TUtil.CallSystemFunction("round", new String[0], null, new MemoryData(), true));
        assertThrows(XTalkException.class, () -> TUtil.CallSystemFunction("annuity", new String[] {"0.1"}, null, new MemoryData(), true));
    }

    @Test
    void nonNumericArgumentThrows() {
        assertThrows(XTalkException.class, () -> TUtil.CallSystemFunction("trunc", new String[] {"abc"}, null, new MemoryData(), true));
        assertThrows(XTalkException.class, () -> TUtil.CallSystemFunction("average", new String[] {"x,y"}, null, new MemoryData(), true));
    }

    @Test
    void offsetRequiresTwoArguments() throws Exception {
        Result result = TUtil.CallSystemFunction("offset", new String[] {"a"}, null, new MemoryData(), true);
        assertEquals(1, result.ret);
        assertNull(result.theResult);
    }
}
