/*
 * Copyright 2017 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 *
 * This file is part of the module utilities.
 *
 * utilities is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * utilities is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with utilities. If not, see
 * <http://www.gnu.org/licenses/>.
 */
/*
 * Created on Mar 1, 2005 at 4:56:16 PM.
 */
package uk.ac.standrews.cs.utilities.archive;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * @author http://www.merriampark.com/bigsqrt.htm#Source
 */
@SuppressWarnings("unused")
public class BigSquareRoot {

    private static final BigDecimal ZERO = new BigDecimal("0");
    private static final BigDecimal ONE = new BigDecimal("1");
    private static final BigDecimal TWO = new BigDecimal("2");
    private static final int DEFAULT_MAX_ITERATIONS = 50;
    private static final int DEFAULT_SCALE = 10;

    //--------------------------
    // Get initial approximation
    //--------------------------

    private static BigDecimal getInitialApproximation(BigDecimal n) {
        BigInteger integerPart = n.toBigInteger();
        int length = integerPart.toString().length();
        if ((length % 2) == 0) {
            length--;
        }
        length /= 2;
        return ONE.movePointRight(length);
    }

    //----------------
    // Get square root
    //----------------

    public static BigDecimal sqrt(BigInteger n) {
        return sqrt(new BigDecimal(n));
    }

    private static BigDecimal sqrt(BigDecimal n) {

        // Make sure n is a positive number

        if (n.compareTo(ZERO) < 0) {
            throw new IllegalArgumentException();
        }
        if (n.compareTo(ZERO) == 0) {
            return ZERO;
        }

        final BigDecimal initialGuess = getInitialApproximation(n);
        BigDecimal guess = new BigDecimal(initialGuess.toString());

        // Iterate

        int iterations = 0;
        boolean more = true;
        while (more) {
            final BigDecimal lastGuess = guess;
            int scale = DEFAULT_SCALE;
            guess = n.divide(guess, scale, BigDecimal.ROUND_HALF_UP);
            guess = guess.add(lastGuess);
            guess = guess.divide(TWO, scale, BigDecimal.ROUND_HALF_UP);
            BigDecimal error = n.subtract(guess.multiply(guess));
            if (++iterations >= DEFAULT_MAX_ITERATIONS) {
                more = false;
            } else if (lastGuess.equals(guess)) {
                more = error.abs().compareTo(ONE) >= 0;
            }
        }
        return guess;
    }
}
