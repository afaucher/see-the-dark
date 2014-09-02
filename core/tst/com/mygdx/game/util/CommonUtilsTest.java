package com.mygdx.game.util;

import static org.junit.Assert.*;

import org.junit.Test;

import com.badlogic.gdx.math.MathUtils;

public class CommonUtilsTest {

    public static float EPSILON = 0.001f;

    private void testContains(float start, float range) {
        boolean contains = false;
        
        contains = CommonUtils.doesRadianRangeContain(start, range, start + range / 2);
        assertTrue(contains);
        contains = CommonUtils.doesRadianRangeContain(start, range, start);
        assertTrue(contains);
        contains = CommonUtils.doesRadianRangeContain(start, range, start + range);
        assertTrue(contains);
        // Up One
        contains = CommonUtils.doesRadianRangeContain(start, range, start + range - EPSILON + MathUtils.PI2);
        assertTrue(contains);
        contains = CommonUtils.doesRadianRangeContain(start, range, start + EPSILON + MathUtils.PI2);
        assertTrue(contains);
        // One Down
        contains = CommonUtils.doesRadianRangeContain(start, range, start + range - EPSILON - MathUtils.PI2);
        assertTrue(contains);
        contains = CommonUtils.doesRadianRangeContain(start, range, start + EPSILON - MathUtils.PI2);
        assertTrue(contains);
    }
    
    private void testDoesNotContains(float start, float range) {
        boolean contains = false;
        
        contains = CommonUtils.doesRadianRangeContain(start, range, start - EPSILON);
        assertFalse(contains);
        contains = CommonUtils.doesRadianRangeContain(start, range, start + range + EPSILON);
        assertFalse(contains);
        // Up One
        contains = CommonUtils.doesRadianRangeContain(start, range, start + range + EPSILON + MathUtils.PI2);
        assertFalse(contains);
        contains = CommonUtils.doesRadianRangeContain(start, range, start - EPSILON + MathUtils.PI2);
        assertFalse(contains);
        // One Down
        contains = CommonUtils.doesRadianRangeContain(start, range, start + range + EPSILON - MathUtils.PI2);
        assertFalse(contains);
        contains = CommonUtils.doesRadianRangeContain(start, range, start - EPSILON - MathUtils.PI2);
        assertFalse(contains);
    }

    @Test
    public void testBase() {
        testContains(0, 2);
        testDoesNotContains(0,2);
        testContains(-2, 2);
        testDoesNotContains(-2,2);
        testContains(-2, 4);
        testDoesNotContains(-2,4);
        testContains(MathUtils.PI2 - EPSILON, 2);
        testDoesNotContains(MathUtils.PI2 - EPSILON, 2);
        testContains(0, MathUtils.PI2);
    }

}
