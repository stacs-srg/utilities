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
package uk.ac.standrews.cs.utilities.time;

import org.junit.Ignore;
import org.junit.Test;
import uk.ac.standrews.cs.utilities.archive.Diagnostic;
import uk.ac.standrews.cs.utilities.archive.StopLoopingException;
import uk.ac.standrews.cs.utilities.archive.UndefinedDiagnosticLevelException;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

/**
 * Tests for the {@link Timing} utility class.
 *
 * @author Graham Kirby (graham.kirby@st-andrews.ac.uk)
 */
@Ignore
public class TimingTests {

    private static final int TEST_TIMEOUT_1 = 50000;
    private static final int TEST_TIMEOUT_2 = 30000;

    /**
     * Checks that a repeated loop with fixed delay executes for the expected number of iterations.
     *
     * @throws Exception if the test fails
     */
    @Test(timeout = TEST_TIMEOUT_1)
    public void repeatShortLoopWithFixedDelay() throws Exception {

        final Duration overall_timeout = new Duration(10, TimeUnit.SECONDS);
        final Duration delay_between_repetitions = new Duration(2, TimeUnit.SECONDS);
        final Duration delay_within_loop = new Duration(0, TimeUnit.SECONDS);
        final boolean delay_is_fixed = true;
        final int expected_loops = 5;

        generalRepeatTest(overall_timeout, delay_between_repetitions, delay_within_loop, delay_is_fixed, expected_loops);
    }

    /**
     * Checks that a repeated loop with fixed delay executes for the expected number of iterations.
     *
     * @throws Exception if the test fails
     */
    @Test(timeout = TEST_TIMEOUT_1)
    public void repeatLongLoopWithFixedDelay() throws Exception {

        final Duration overall_timeout = new Duration(10, TimeUnit.SECONDS);
        final Duration delay_between_repetitions = new Duration(2, TimeUnit.SECONDS);
        final Duration delay_within_loop = new Duration(3, TimeUnit.SECONDS);
        final boolean delay_is_fixed = true;
        final int expected_loops = 2;

        generalRepeatTest(overall_timeout, delay_between_repetitions, delay_within_loop, delay_is_fixed, expected_loops);
    }

    /**
     * Checks that a repeated loop with variable delay executes for the expected number of iterations.
     *
     * @throws Exception if the test fails
     */
    @Test(timeout = TEST_TIMEOUT_1)
    public void repeatWithVariableDelay() throws Exception {

        final Duration overall_timeout = new Duration(10, TimeUnit.SECONDS);
        final Duration delay_between_repetitions = new Duration(2, TimeUnit.SECONDS);
        final Duration delay_within_loop = new Duration(3, TimeUnit.SECONDS);
        final boolean delay_is_fixed = false;
        final int expected_loops = 4;

        generalRepeatTest(overall_timeout, delay_between_repetitions, delay_within_loop, delay_is_fixed, expected_loops);
    }

    /**
     * Checks that a repeated loop reaching its timeout limit completes normally.
     *
     * @throws Exception if the test fails
     */
    @Test(timeout = TEST_TIMEOUT_2)
    public void repeatTerminatedByTimeout() throws Exception {

        final Duration overall_timeout = new Duration(10, TimeUnit.SECONDS);
        final Duration delay_between_repetitions = new Duration(1, TimeUnit.SECONDS);
        final boolean delay_is_fixed = true;

        final Callable<Void> action = () -> null;

        Timing.repeat(action, overall_timeout, delay_between_repetitions, delay_is_fixed, Diagnostic.FULL);
    }

    /**
     * Checks that a repeated loop that is interrupted terminates as expected.
     *
     * @throws Exception if the test fails
     */
    @Test(timeout = TEST_TIMEOUT_1, expected = InterruptedException.class)
    public void repeatTerminatedByInterruptedException() throws Exception {

        final Duration overall_timeout = new Duration(10, TimeUnit.SECONDS);
        final Duration delay_between_repetitions = new Duration(1, TimeUnit.SECONDS);
        final boolean delay_is_fixed = true;

        final Callable<Void> action = new Callable<Void>() {

            private int count = 0;

            @Override
            public Void call() throws Exception {

                if (count > 5) {
                    Thread.currentThread().interrupt();
                }
                count++;
                return null;
            }
        };

        Timing.repeat(action, overall_timeout, delay_between_repetitions, delay_is_fixed, Diagnostic.FULL);
    }

    /**
     * Checks that a repeated loop that throws an application-specific exception continues as expected.
     *
     * @throws Exception if the test fails
     */
    @Test(timeout = TEST_TIMEOUT_1)
    public void repeatNotTerminatedByApplicationException() throws Exception {

        final Duration overall_timeout = new Duration(10, TimeUnit.SECONDS);
        final Duration delay_between_repetitions = new Duration(1, TimeUnit.SECONDS);
        final boolean delay_is_fixed = true;

        final Callable<Void> action = new Callable<Void>() {

            private int count = 0;

            @Override
            public Void call() throws Exception {

                if (count > 5) {
                    throw new UndefinedDiagnosticLevelException();
                }
                count++;
                return null;
            }
        };

        Timing.repeat(action, overall_timeout, delay_between_repetitions, delay_is_fixed, Diagnostic.FULL);
    }

    /**
     * Checks that a repeated loop that throws a wrapped application-specific exception terminates as expected.
     *
     * @throws Exception if the test fails
     */
    @Test(timeout = TEST_TIMEOUT_1, expected = UndefinedDiagnosticLevelException.class)
    public void repeatTerminatedByApplicationException() throws Exception {

        final Duration overall_timeout = new Duration(10, TimeUnit.SECONDS);
        final Duration delay_between_repetitions = new Duration(1, TimeUnit.SECONDS);
        final boolean delay_is_fixed = true;

        final Callable<Void> action = new Callable<Void>() {

            private int count = 0;

            @Override
            public Void call() throws Exception {

                if (count > 5) {
                    throw new StopLoopingException(new UndefinedDiagnosticLevelException());
                }
                count++;
                return null;
            }
        };

        Timing.repeat(action, overall_timeout, delay_between_repetitions, delay_is_fixed, Diagnostic.FULL);
    }

    @Test(timeout = 12000)
    public void retryWorkingFirstTime() throws Exception {

        final Duration overall_timeout = new Duration(10, TimeUnit.SECONDS);
        final Duration delay_between_repetitions = new Duration(1, TimeUnit.SECONDS);
        final boolean delay_is_fixed = true;

        final Callable<Integer> action = () -> -7;

        final int result = Timing.retry(action, overall_timeout, delay_between_repetitions, delay_is_fixed, Diagnostic.FULL);
        assertThat(result, is(equalTo(-7)));
    }

    @Test(timeout = 15000)
    public void retryShortLoopWithFixedDelay() throws Exception {

        final Duration overall_timeout = new Duration(10, TimeUnit.SECONDS);
        final Duration delay_between_repetitions = new Duration(2, TimeUnit.SECONDS);
        final Duration delay_within_loop = new Duration(0, TimeUnit.SECONDS);
        final boolean delay_is_fixed = true;
        final int expected_loops = 5;

        generalRetryTest(overall_timeout, delay_between_repetitions, delay_within_loop, delay_is_fixed, expected_loops);
    }

    @Test(timeout = 15000)
    public void retryLongLoopWithFixedDelay() throws Exception {

        final Duration overall_timeout = new Duration(10, TimeUnit.SECONDS);
        final Duration delay_between_repetitions = new Duration(2, TimeUnit.SECONDS);
        final Duration delay_within_loop = new Duration(3, TimeUnit.SECONDS);
        final boolean delay_is_fixed = true;
        final int expected_loops = 2;

        generalRetryTest(overall_timeout, delay_between_repetitions, delay_within_loop, delay_is_fixed, expected_loops);
    }

    @Test(timeout = 15000)
    public void retryWithVariableDelay() throws Exception {

        final Duration overall_timeout = new Duration(10, TimeUnit.SECONDS);
        final Duration delay_between_repetitions = new Duration(2, TimeUnit.SECONDS);
        final Duration delay_within_loop = new Duration(3, TimeUnit.SECONDS);
        final boolean delay_is_fixed = false;
        final int expected_loops = 4;

        generalRetryTest(overall_timeout, delay_between_repetitions, delay_within_loop, delay_is_fixed, expected_loops);
    }

    @Test(timeout = 12000, expected = TimeoutException.class)
    public void retryTerminatedByTimeout() throws Exception {

        final Duration overall_timeout = new Duration(10, TimeUnit.SECONDS);
        final Duration delay_between_repetitions = new Duration(1, TimeUnit.SECONDS);
        final boolean delay_is_fixed = true;

        final Callable<Integer> action = () -> {

            throw new UndefinedDiagnosticLevelException();
        };

        Timing.retry(action, overall_timeout, delay_between_repetitions, delay_is_fixed, Diagnostic.FULL);
    }

    @Test(timeout = 15000, expected = InterruptedException.class)
    public void retryTerminatedByInterruptedException() throws Exception {

        final Duration overall_timeout = new Duration(10, TimeUnit.SECONDS);
        final Duration delay_between_repetitions = new Duration(1, TimeUnit.SECONDS);
        final boolean delay_is_fixed = true;

        final Callable<Integer> action = new Callable<Integer>() {

            private int count = 0;

            @Override
            public Integer call() throws Exception {

                count++;

                if (count > 5) {
                    throw new InterruptedException();
                } else {
                    throw new UndefinedDiagnosticLevelException();
                }
            }
        };

        Timing.retry(action, overall_timeout, delay_between_repetitions, delay_is_fixed, Diagnostic.FULL);
    }

    @Test(timeout = 15000, expected = UndefinedDiagnosticLevelException.class)
    public void retryTerminatedByApplicationException() throws Exception {

        final Duration overall_timeout = new Duration(10, TimeUnit.SECONDS);
        final Duration delay_between_repetitions = new Duration(1, TimeUnit.SECONDS);
        final boolean delay_is_fixed = true;

        final Callable<Integer> action = new Callable<Integer>() {

            private int count = 0;

            @Override
            public Integer call() throws Exception {

                count++;

                if (count > 5) {
                    throw new StopLoopingException(new UndefinedDiagnosticLevelException());
                } else {
                    throw new UndefinedDiagnosticLevelException();
                }
            }
        };

        Timing.retry(action, overall_timeout, delay_between_repetitions, delay_is_fixed, Diagnostic.FULL);
    }

    // -------------------------------------------------------------------------------------------------------

    private void sleepIfNotNull(final Duration delay_within_loop) throws InterruptedException {

        if (delay_within_loop != null) {
            delay_within_loop.sleep();
        }
    }

    private void generalRepeatTest(final Duration overall_timeout, final Duration delay_between_repetitions, final Duration delay_within_loop, final boolean delay_is_fixed, final int expected_loops) throws Exception {

        final AtomicInteger repeat_count = new AtomicInteger(0);
        final AtomicBoolean delay_violation = new AtomicBoolean(false);

        final Callable<Void> action = new Callable<Void>() {

            private Duration last_called = new Duration();

            @Override
            public Void call() throws Exception {

                repeat_count.incrementAndGet();

                sleepIfNotNull(delay_within_loop);

                final Duration elapsed = Duration.elapsed(last_called);
                last_called = Duration.elapsed();

                if (elapsed.compareTo(delay_between_repetitions) < 0) {
                    delay_violation.set(true);
                }
                return null;
            }
        };

        Timing.repeat(action, overall_timeout, delay_between_repetitions, delay_is_fixed, Diagnostic.FULL);

        assertThat(delay_violation.get(), is(false));
        assertThat(repeat_count.get(), is(equalTo(expected_loops)));
    }

    private void generalRetryTest(final Duration overall_timeout, final Duration delay_between_repetitions, final Duration delay_within_loop, final boolean delay_is_fixed, final int expected_loops) throws Exception {

        final AtomicInteger repeat_count = new AtomicInteger(0);
        final AtomicBoolean delay_violation = new AtomicBoolean(false);

        final Callable<Integer> action = new Callable<Integer>() {

            private Duration last_called = new Duration();

            @Override
            public Integer call() throws Exception {

                if (repeat_count.incrementAndGet() < expected_loops) {
                    throw new UndefinedDiagnosticLevelException();
                }

                sleepIfNotNull(delay_within_loop);

                final Duration elapsed = Duration.elapsed(last_called);
                last_called = Duration.elapsed();

                if (elapsed.compareTo(delay_between_repetitions) < 0) {
                    delay_violation.set(true);
                }
                return -7;
            }
        };

        final int result = Timing.retry(action, overall_timeout, delay_between_repetitions, delay_is_fixed, Diagnostic.FULL);

        assertThat(delay_violation.get(), is(false));
        assertThat(repeat_count.get(), is(equalTo(expected_loops)));
        assertThat(result, is(equalTo(-7)));
    }
}
