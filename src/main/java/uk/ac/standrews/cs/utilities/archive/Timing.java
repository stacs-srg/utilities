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
package uk.ac.standrews.cs.utilities.archive;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeoutException;

/**
 * Timing and repetition-related utility methods.
 *
 * @author Graham Kirby (graham.kirby@st-andrews.ac.uk)
 */
@SuppressWarnings("WeakerAccess")
public final class Timing {

    // -------------------------------------------------------------------------------------------------------

    /**
     * Repeats a given void action until an overall timeout period is exceeded. A delay between repetitions can be specified in two different ways as explained below.
     * If the action throws {@link InterruptedException}, this is thrown in turn by this method. If the action requires to terminate the loop with an application-specific
     * exception, it should throw it wrapped in {@link StopLoopingException}. Other exceptions thrown by the action are caught and logged without terminating the loop.
     *
     * @param action          the action to be repeated
     * @param overall_timeout the overall timeout
     * @param loop_delay      the delay between repetitions
     * @param delay_is_fixed  true if the delay represents a fixed sleep period between the end of one repetition and the start of the next, false if it represents the minimum time between the start of one repetition and the start of the next
     * @param reporting_level the diagnostic reporting threshold
     * @throws Exception if the thread is interrupted, or if the action throws an exception that terminates the loop
     */
    @SuppressWarnings("WeakerAccess")
    public static void repeat(final Callable<Void> action, final Duration overall_timeout, final Duration loop_delay, final boolean delay_is_fixed, final Diagnostic reporting_level) throws Exception {

        try {
            generalLoop(action, overall_timeout, loop_delay, delay_is_fixed, false, reporting_level);

        } catch (final TimeoutException e) {
            // Timeout is normal exit reason, so don't throw.
        }
    }

    /**
     * Retries a given action until it succeeds or an overall timeout period is exceeded. A delay between repetitions can be specified in two different ways as explained below.
     * If the action throws {@link InterruptedException}, this is thrown in turn by this method. If the action requires to terminate the loop with an application-specific
     * exception, it should throw it wrapped in {@link StopLoopingException}. Other exceptions thrown by the action are caught and logged without terminating the loop.
     *
     * @param <T>             the result type of the action
     * @param action          the action to be repeated
     * @param overall_timeout the overall timeout
     * @param loop_delay      the delay between repetitions
     * @param delay_is_fixed  true if the delay represents a fixed sleep period between the end of one repetition and the start of the next, false if it represents the minimum time between the start of one repetition and the start of the next
     * @param reporting_level the diagnostic reporting threshold
     * @return the action result
     * @throws Exception {@link TimeoutException} if the action times out, {@link InterruptedException} if the action is interrupted, or an exception thrown by the action
     */
    public static <T> T retry(final Callable<T> action, final Duration overall_timeout, final Duration loop_delay, final boolean delay_is_fixed, final Diagnostic reporting_level) throws Exception {

        return generalLoop(action, overall_timeout, loop_delay, delay_is_fixed, true, reporting_level);
    }

    // -------------------------------------------------------------------------------------------------------

    private static <T> T generalLoop(final Callable<T> action, final Duration timeout_interval, final Duration loop_delay, final boolean delay_is_fixed, final boolean return_result, final Diagnostic reporting_level) throws Exception {

        // The start time for the entire method.
        final Duration start = Duration.elapsed();

        // The total elapsed time, initially zero.
        Duration elapsed_overall = new Duration();

        // The time at which the previous loop ended.
        Duration previous_loop_end = start;

        // Loop until interrupted.
        while (!Thread.currentThread().isInterrupted()) {

            // Check for the overall timeout being reached.
            if (elapsed_overall.compareTo(timeout_interval) >= 0) {
                throw new TimeoutException();
            }

            try {
                // Try calling the action.
                final T result = action.call();

                // We may or may not wish to return the result, thus terminating the loop.
                if (return_result) {
                    return result;
                }
            } catch (final Exception e) {

                // Deal with special cases: InterruptedException and StopLoopingException. Otherwise the exception is absorbed and the loop carries on.
                launderException(e, reporting_level);
            }

            // The time at which the current loop actually ended.
            final Duration current_loop_end = Duration.elapsed();

            if (delay_is_fixed) {
                loop_delay.sleep();
            } else {
                // The earliest time at which the current loop should finish, given the specified minimum loop delay.
                final Duration earliest_finish = previous_loop_end.add(loop_delay);

                // Calculate whether a further delay is required.
                final Duration delay_required = earliest_finish.subtract(current_loop_end);

                // The calculated delay may be negative, in which case this won't do anything.
                delay_required.sleep();
            }

            // Record the final end time for the loop, and update the overall elapsed time.
            previous_loop_end = Duration.elapsed();
            elapsed_overall = previous_loop_end.subtract(start);
        }

        // The loop has terminated so the thread must have been interrupted.
        throw new InterruptedException();
    }

    private static void launderException(final Exception e, final Diagnostic reporting_level) throws Exception {

        if (e instanceof InterruptedException) {
            throw e;
        }
        if (e instanceof StopLoopingException) {
            throw ((StopLoopingException) e).getActionException();
        }

        Diagnostic.trace("exception: " + e.getMessage(), reporting_level);
    }
}
