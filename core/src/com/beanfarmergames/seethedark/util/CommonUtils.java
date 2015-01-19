package com.beanfarmergames.seethedark.util;

import com.badlogic.gdx.math.MathUtils;

public class CommonUtils {
    public static boolean doesRadianRangeContain(float startRadian, float rangeRadian, float testRadian) {
        assert (rangeRadian <= MathUtils.PI2);
        assert (rangeRadian >= rangeRadian);

        startRadian = startRadian % MathUtils.PI2;
        testRadian = testRadian % MathUtils.PI2;
        // @formatter:off
		/**
		 * 4PI           2PI           0             -2PI          -4PI
		 * |             |             |             |             |
		 * 
		 * Case 1:
		 *                  RR   SR       RR*  SR*
		 *                  |    |        |    |
		 * Case 2:                  
		 *             RR   SR       RR*  SR*
		 *             |    |        |    |
		 * Case 3:
		 *                      RR*  SR*      RR   SR
		 *                      |    |        |    |
		 */
		// @formatter:on
        if (testRadian <= 0) {
            testRadian += MathUtils.PI2;
        }
        if (startRadian < 0) {
            // Turn case 3 into case 1 or 2
            startRadian += MathUtils.PI2;
        }
        // Center
        if (startRadian <= testRadian && startRadian + rangeRadian >= testRadian) {
            return true;
        }
        // One Down
        if (startRadian - MathUtils.PI2 <= testRadian && startRadian + rangeRadian - MathUtils.PI2 >= testRadian) {
            return true;
        }
        // One Up
        if (startRadian + MathUtils.PI2 <= testRadian && startRadian + rangeRadian + MathUtils.PI2 >= testRadian) {
            return true;
        }

        return false;
    }
}
