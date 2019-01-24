package uk.ac.standrews.cs.utilities.stats;

import org.apache.commons.math3.exception.NotStrictlyPositiveException;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;

public class StatisticsTest {

    private static final double EPSILON = 0.0000001;

    @Test(expected = NotStrictlyPositiveException.class)
    public void confidenceIntervalOfEmptyListThrowsException() {

        assertTrue(Double.isNaN(ConfidenceIntervals.calculateConfidenceInterval(new ArrayList<>())));
    }

    @Test(expected = NotStrictlyPositiveException.class)
    public void confidenceIntervalOfSingleElementListThrowsException() {

        assertTrue(Double.isNaN(ConfidenceIntervals.calculateConfidenceInterval(Collections.singletonList(1.0))));
    }

    @Test
    public void confidenceIntervalsAt95PercentAreCorrect() {

        // Expected values calculated in Excel.

        assertEquals(12.7062047, ConfidenceIntervals.calculateConfidenceInterval(Arrays.asList(37.0, 39.0)), EPSILON);
        assertEquals(6.3531024, ConfidenceIntervals.calculateConfidenceInterval(Arrays.asList(51.0, 52.0)), EPSILON);
        assertEquals(0.0, ConfidenceIntervals.calculateConfidenceInterval(Arrays.asList(70.0, 70.0)), EPSILON);

        assertEquals(3.7945830, ConfidenceIntervals.calculateConfidenceInterval(Arrays.asList(37.0, 39.0, 36.0)), EPSILON);
        assertEquals(1.4342176, ConfidenceIntervals.calculateConfidenceInterval(Arrays.asList(51.0, 52.0, 51.0)), EPSILON);
        assertEquals(14.3421758, ConfidenceIntervals.calculateConfidenceInterval(Arrays.asList(70.0, 70.0, 60.0)), EPSILON);

        assertEquals(4.6844341, ConfidenceIntervals.calculateConfidenceInterval(Arrays.asList(37.0, 39.0, 36.0, 32.0)), EPSILON);
        assertEquals(38.4618748, ConfidenceIntervals.calculateConfidenceInterval(Arrays.asList(51.0, 52.0, 51.0, 3.0)), EPSILON);
        assertEquals(12.9922826, ConfidenceIntervals.calculateConfidenceInterval(Arrays.asList(70.0, 70.0, 60.0, 80.0)), EPSILON);

        assertEquals(3.3547914, ConfidenceIntervals.calculateConfidenceInterval(Arrays.asList(37.0, 39.0, 36.0, 32.0, 34.0)), EPSILON);
        assertEquals(27.0899102, ConfidenceIntervals.calculateConfidenceInterval(Arrays.asList(51.0, 52.0, 51.0, 3.0, 53.0)), EPSILON);
        assertEquals(8.7798903, ConfidenceIntervals.calculateConfidenceInterval(Arrays.asList(70.0, 70.0, 60.0, 80.0, 70.0)), EPSILON);
    }

    @Test
    public void confidenceIntervalsAt99PercentAreCorrect() {

        // Expected values calculated in Excel.

        final double confidence_level = 0.99;

        assertEquals(63.6567412, ConfidenceIntervals.calculateConfidenceInterval(Arrays.asList(37.0, 39.0), confidence_level), EPSILON);
        assertEquals(31.8283706, ConfidenceIntervals.calculateConfidenceInterval(Arrays.asList(51.0, 52.0), confidence_level), EPSILON);
        assertEquals(0.0, ConfidenceIntervals.calculateConfidenceInterval(Arrays.asList(70.0, 70.0), confidence_level), EPSILON);

        assertEquals(8.7528890, ConfidenceIntervals.calculateConfidenceInterval(Arrays.asList(37.0, 39.0, 36.0), confidence_level), EPSILON);
        assertEquals(3.3082811, ConfidenceIntervals.calculateConfidenceInterval(Arrays.asList(51.0, 52.0, 51.0), confidence_level), EPSILON);
        assertEquals(33.0828107, ConfidenceIntervals.calculateConfidenceInterval(Arrays.asList(70.0, 70.0, 60.0), confidence_level), EPSILON);

        assertEquals(8.5975857, ConfidenceIntervals.calculateConfidenceInterval(Arrays.asList(37.0, 39.0, 36.0, 32.0), confidence_level), EPSILON);
        assertEquals(70.5910803, ConfidenceIntervals.calculateConfidenceInterval(Arrays.asList(51.0, 52.0, 51.0, 3.0), confidence_level), EPSILON);
        assertEquals(23.8454124, ConfidenceIntervals.calculateConfidenceInterval(Arrays.asList(70.0, 70.0, 60.0, 80.0), confidence_level), EPSILON);

        assertEquals(5.5631490, ConfidenceIntervals.calculateConfidenceInterval(Arrays.asList(37.0, 39.0, 36.0, 32.0, 34.0), confidence_level), EPSILON);
        assertEquals(44.9223780, ConfidenceIntervals.calculateConfidenceInterval(Arrays.asList(51.0, 52.0, 51.0, 3.0, 53.0), confidence_level), EPSILON);
        assertEquals(14.5594264, ConfidenceIntervals.calculateConfidenceInterval(Arrays.asList(70.0, 70.0, 60.0, 80.0, 70.0), confidence_level), EPSILON);
    }
}
