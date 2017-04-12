/*
 * Created on Mar 1, 2005 at 4:56:16 PM.
 */
package uk.ac.standrews.cs.utilities.archive;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * @author http://www.merriampark.com/bigsqrt.htm#Source
 *         <p>
 *         Does what it says on the tin
 */
public class BigSquareRoot {

    private static BigDecimal ZERO = new BigDecimal("0");
    private static BigDecimal ONE = new BigDecimal("1");
    private static BigDecimal TWO = new BigDecimal("2");
    public static final int DEFAULT_MAX_ITERATIONS = 50;
    public static final int DEFAULT_SCALE = 10;

    private static BigDecimal error;
    private static int iterations;
    private static int scale = DEFAULT_SCALE;
    private static int maxIterations = DEFAULT_MAX_ITERATIONS;

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
        BigDecimal guess = ONE.movePointRight(length);
        return guess;
    }

    //----------------
    // Get square root
    //----------------

    public static BigDecimal sqrt(BigInteger n) {
        return sqrt(new BigDecimal(n));
    }

    public static BigDecimal sqrt(BigDecimal n) {

        // Make sure n is a positive number

        if (n.compareTo(ZERO) < 0) {
            throw new IllegalArgumentException();
        }
        if (n.compareTo(ZERO) == 0) {
            return ZERO;
        }

        BigDecimal initialGuess = getInitialApproximation(n);
        BigDecimal lastGuess = ZERO;
        BigDecimal guess = new BigDecimal(initialGuess.toString());

        // Iterate

        iterations = 0;
        boolean more = true;
        while (more) {
            lastGuess = guess;
            guess = n.divide(guess, scale, BigDecimal.ROUND_HALF_UP);
            guess = guess.add(lastGuess);
            guess = guess.divide(TWO, scale, BigDecimal.ROUND_HALF_UP);
            error = n.subtract(guess.multiply(guess));
            if (++iterations >= maxIterations) {
                more = false;
            } else if (lastGuess.equals(guess)) {
                more = error.abs().compareTo(ONE) >= 0;
            }
        }
        return guess;

    }
}
