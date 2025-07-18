/*
 * Copyright 2021 Systems Research Group, University of St Andrews:
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
package uk.ac.standrews.cs.utilities.stats;

import org.apache.commons.math3.exception.NotStrictlyPositiveException;
import org.junit.jupiter.api.Test;
import uk.ac.standrews.cs.utilities.Statistics;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

public class StatisticsTest {

    private static final double EPSILON = 0.0000001;

    @Test
    public void confidenceIntervalOfEmptyListThrowsException() {

        assertThrows(NotStrictlyPositiveException.class, () -> {
            assertTrue(Double.isNaN(Statistics.confidenceInterval(new ArrayList<>())));
        });
    }

    @Test
    public void confidenceIntervalOfSingleElementListThrowsException() {

        assertThrows(NotStrictlyPositiveException.class, () -> {
            assertTrue(Double.isNaN(Statistics.confidenceInterval(Collections.singletonList(1.0))));
        });
    }

    @Test
    public void confidenceIntervalsAt95PercentAreCorrect() {

        // Expected values calculated in Excel.

        assertEquals(12.7062047, Statistics.confidenceInterval(Arrays.asList(37.0, 39.0)), EPSILON);
        assertEquals(6.3531024, Statistics.confidenceInterval(Arrays.asList(51.0, 52.0)), EPSILON);
        assertEquals(0.0, Statistics.confidenceInterval(Arrays.asList(70.0, 70.0)), EPSILON);

        assertEquals(3.7945830, Statistics.confidenceInterval(Arrays.asList(37.0, 39.0, 36.0)), EPSILON);
        assertEquals(1.4342176, Statistics.confidenceInterval(Arrays.asList(51.0, 52.0, 51.0)), EPSILON);
        assertEquals(14.3421758, Statistics.confidenceInterval(Arrays.asList(70.0, 70.0, 60.0)), EPSILON);

        assertEquals(4.6844341, Statistics.confidenceInterval(Arrays.asList(37.0, 39.0, 36.0, 32.0)), EPSILON);
        assertEquals(38.4618748, Statistics.confidenceInterval(Arrays.asList(51.0, 52.0, 51.0, 3.0)), EPSILON);
        assertEquals(12.9922826, Statistics.confidenceInterval(Arrays.asList(70.0, 70.0, 60.0, 80.0)), EPSILON);

        assertEquals(3.3547914, Statistics.confidenceInterval(Arrays.asList(37.0, 39.0, 36.0, 32.0, 34.0)), EPSILON);
        assertEquals(27.0899102, Statistics.confidenceInterval(Arrays.asList(51.0, 52.0, 51.0, 3.0, 53.0)), EPSILON);
        assertEquals(8.7798903, Statistics.confidenceInterval(Arrays.asList(70.0, 70.0, 60.0, 80.0, 70.0)), EPSILON);
    }

    @Test
    public void confidenceIntervalsAt99PercentAreCorrect() {

        // Expected values calculated in Excel.

        final double confidence_level = 0.99;

        assertEquals(63.6567412, Statistics.confidenceInterval(Arrays.asList(37.0, 39.0), confidence_level), EPSILON);
        assertEquals(31.8283706, Statistics.confidenceInterval(Arrays.asList(51.0, 52.0), confidence_level), EPSILON);
        assertEquals(0.0, Statistics.confidenceInterval(Arrays.asList(70.0, 70.0), confidence_level), EPSILON);

        assertEquals(8.7528890, Statistics.confidenceInterval(Arrays.asList(37.0, 39.0, 36.0), confidence_level), EPSILON);
        assertEquals(3.3082811, Statistics.confidenceInterval(Arrays.asList(51.0, 52.0, 51.0), confidence_level), EPSILON);
        assertEquals(33.0828107, Statistics.confidenceInterval(Arrays.asList(70.0, 70.0, 60.0), confidence_level), EPSILON);

        assertEquals(8.5975857, Statistics.confidenceInterval(Arrays.asList(37.0, 39.0, 36.0, 32.0), confidence_level), EPSILON);
        assertEquals(70.5910803, Statistics.confidenceInterval(Arrays.asList(51.0, 52.0, 51.0, 3.0), confidence_level), EPSILON);
        assertEquals(23.8454124, Statistics.confidenceInterval(Arrays.asList(70.0, 70.0, 60.0, 80.0), confidence_level), EPSILON);

        assertEquals(5.5631490, Statistics.confidenceInterval(Arrays.asList(37.0, 39.0, 36.0, 32.0, 34.0), confidence_level), EPSILON);
        assertEquals(44.9223780, Statistics.confidenceInterval(Arrays.asList(51.0, 52.0, 51.0, 3.0, 53.0), confidence_level), EPSILON);
        assertEquals(14.5594264, Statistics.confidenceInterval(Arrays.asList(70.0, 70.0, 60.0, 80.0, 70.0), confidence_level), EPSILON);
    }
}
