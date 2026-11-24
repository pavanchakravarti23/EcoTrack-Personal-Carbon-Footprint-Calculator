package com.ecotrack.utils;

import org.junit.Test;

import static org.junit.Assert.*;

public class InputValidatorTest {

    @Test
    public void acceptsValidZeroAndLargeBoundedNumbers() {
        assertEquals(0.0, InputValidator.parseNonNegativeDouble("0", "Field"), 0.0001);
        assertEquals(123.45, InputValidator.parseNonNegativeDouble(" 123.45 ", "Field"), 0.0001);
        assertEquals(100.0, InputValidator.parsePercentage("100", "Pct"), 0.0001);
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsEmptyField() {
        InputValidator.parseNonNegativeDouble("   ", "Distance");
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsNegativeNumber() {
        InputValidator.parseNonNegativeDouble("-5.2", "Distance");
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsExtremelyLargeOverflowValue() {
        InputValidator.parseNonNegativeDouble("999999999999", "Distance");
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsPercentageAbove100() {
        InputValidator.parsePercentage("105", "Recycling");
    }
}
