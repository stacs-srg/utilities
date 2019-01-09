package uk.ac.standrews.cs.utilities.metrics;

public class CheckValues {
    public static double checkNullAndEmpty(String A, String B) {
        if (A.equals(B)) {
            return 1.0;
        }
        if (A.isEmpty() || B.isEmpty()) {
            return 0.0;
        }
        return -1;
    }
}
