/*
 * Copyright 2018 Systems Research Group, University of St Andrews:
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

import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

@Ignore
public class TimeoutExecutorTests {

    /*
     * Test all sensible combinations of the following variables:
     * 
     * 1) result (Callable) / no result (Runnable)
     * 2) block / return immediately
     * 3) spends / doesn't spend time in queue
     * 4) include / don't include queue time in determining timeout
     * 5) does / doesn't time out
     */

    private static final int EXECUTOR_TEST_TIMEOUT = 60000;
    private static final Duration DURATION_1 = new Duration(1, TimeUnit.SECONDS);
    private static final Duration DURATION_2 = new Duration(2, TimeUnit.SECONDS);
    private static final Duration DURATION_3 = new Duration(3, TimeUnit.SECONDS);
    private static final Duration DURATION_4 = new Duration(4, TimeUnit.SECONDS);
    private static final Duration DURATION_6 = new Duration(6, TimeUnit.SECONDS);

    private TimeoutExecutor executor;

    @After
    public void shutdown() {

        executor.shutdown();
    }

    @Test(timeout = EXECUTOR_TEST_TIMEOUT)
    public void runnableBlocksDoesntQueueDoesntTimeOut() throws Exception {

        executor = TimeoutExecutor.makeTimeoutExecutor(2, DURATION_4, true, true, "");

        final CountDownLatch interrupted_latch = new CountDownLatch(1);
        final AtomicInteger result = new AtomicInteger();

        runTask(DURATION_2, null, false, null, null, interrupted_latch, result, 1, false);

        assertThat(interrupted_latch.getCount(), is(1L));
        assertThat(result.get(), is(equalTo(1)));
    }

    @Test(timeout = EXECUTOR_TEST_TIMEOUT)
    public void runnableBlocksDoesntQueueDoesTimeOut() throws Exception {

        executor = TimeoutExecutor.makeTimeoutExecutor(2, DURATION_4, true, true, "");
        final CountDownLatch timed_out_latch = new CountDownLatch(1);

        runTask(DURATION_6, null, false, null, timed_out_latch, null, null, 0, false);

        timed_out_latch.await();
    }

    @Test(timeout = EXECUTOR_TEST_TIMEOUT)
    public void runnableBlocksDoesQueueDoesIncludeQueueTimeDoesntTimeOut() throws Exception {

        executor = TimeoutExecutor.makeTimeoutExecutor(1, DURATION_4, true, true, "");

        runTwoTasksNoTimeout(DURATION_1, DURATION_1, true);
    }

    @Test(timeout = EXECUTOR_TEST_TIMEOUT)
    public void runnableBlocksDoesQueueDoesIncludeQueueTimeDoesTimeOut() throws Exception {

        executor = TimeoutExecutor.makeTimeoutExecutor(1, DURATION_4, true, true, "");

        runTwoTasksWithSecondTimingOut(DURATION_3, DURATION_3, true);
    }

    @Test(timeout = EXECUTOR_TEST_TIMEOUT)
    public void runnableBlocksDoesQueueDoesntIncludeQueueTimeDoesntTimeOut() throws Exception {

        executor = TimeoutExecutor.makeTimeoutExecutor(1, DURATION_4, true, false, "");

        runTwoTasksNoTimeout(DURATION_3, DURATION_3, true);
    }

    @Test(timeout = EXECUTOR_TEST_TIMEOUT)
    public void runnableBlocksDoesQueueDoesntIncludeQueueTimeDoesTimeOut() throws Exception {

        executor = TimeoutExecutor.makeTimeoutExecutor(1, DURATION_4, true, false, "");

        runTwoTasksWithSecondTimingOut(DURATION_3, DURATION_6, true);
    }

    @Test(timeout = EXECUTOR_TEST_TIMEOUT)
    public void runnableDoesntBlockDoesntQueueDoesntTimeOut() throws Exception {

        executor = TimeoutExecutor.makeTimeoutExecutor(2, DURATION_4, false, true, "");

        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicInteger result = new AtomicInteger();

        runTask(DURATION_2, null, false, latch, null, null, result, 1, false);

        assertThat(latch.getCount(), is(equalTo(1L)));
        latch.await();
        assertThat(result.get(), is(equalTo(1)));
    }

    @Test(timeout = EXECUTOR_TEST_TIMEOUT)
    public void runnableDoesntBlockDoesntQueueDoesTimeOut() throws Exception {

        executor = TimeoutExecutor.makeTimeoutExecutor(2, DURATION_4, false, true, "");

        final CountDownLatch interrupted_latch = new CountDownLatch(1);

        runTask(DURATION_6, null, false, null, null, interrupted_latch, null, 0, false);

        assertThat(interrupted_latch.getCount(), is(equalTo(1L)));
        interrupted_latch.await();
    }

    @Test(timeout = EXECUTOR_TEST_TIMEOUT)
    public void runnableDoesntBlockDoesQueueDoesIncludeQueueTimeDoesntTimeOut() throws Exception {

        executor = TimeoutExecutor.makeTimeoutExecutor(1, DURATION_4, false, true, "");

        runTwoTasksNoTimeout(DURATION_1, DURATION_1, false);
    }

    @Test(timeout = EXECUTOR_TEST_TIMEOUT)
    public void runnableDoesntBlockDoesQueueDoesIncludeQueueTimeDoesTimeOut() throws Exception {

        executor = TimeoutExecutor.makeTimeoutExecutor(1, DURATION_4, false, true, "");

        runTwoTasksWithSecondInterrupted(DURATION_3, DURATION_3, false);
    }

    @Test(timeout = EXECUTOR_TEST_TIMEOUT)
    public void runnableDoesntBlockDoesQueueDoesntIncludeQueueTimeDoesntTimeOut() throws Exception {

        executor = TimeoutExecutor.makeTimeoutExecutor(1, DURATION_4, false, false, "");

        runTwoTasksNoTimeout(DURATION_3, DURATION_3, false);
    }

    @Test(timeout = EXECUTOR_TEST_TIMEOUT)
    public void runnableDoesntBlockDoesQueueDoesntIncludeQueueTimeDoesTimeOut() throws Exception {

        executor = TimeoutExecutor.makeTimeoutExecutor(1, DURATION_4, false, false, "");

        runTwoTasksWithSecondInterrupted(DURATION_3, DURATION_6, false);
    }

    // -------------------------------------------------------------------------------------------------------

    @Test(timeout = EXECUTOR_TEST_TIMEOUT)
    public void callableDoesntQueueDoesntTimeOut() throws Exception {

        executor = TimeoutExecutor.makeTimeoutExecutor(2, DURATION_4, true, true, "");

        final int i = runCallable(DURATION_2, null, false, null, null, 2);

        assertThat(i, is(equalTo(2)));
    }

    @Test(timeout = EXECUTOR_TEST_TIMEOUT)
    public void callableDoesntQueueDoesTimeOut() throws Exception {

        executor = TimeoutExecutor.makeTimeoutExecutor(2, DURATION_4, true, true, "");
        final CountDownLatch timed_out_latch = new CountDownLatch(1);

        runCallable(DURATION_6, null, false, null, timed_out_latch, 0);

        timed_out_latch.await();
    }

    @Test(timeout = EXECUTOR_TEST_TIMEOUT)
    public void callableDoesQueueDoesIncludeQueueTimeDoesntTimeOut() throws Exception {

        executor = TimeoutExecutor.makeTimeoutExecutor(1, DURATION_6, true, true, "");

        runTwoCallablesNoTimeout(DURATION_1, DURATION_1);
    }

    @Test(timeout = EXECUTOR_TEST_TIMEOUT)
    public void callableDoesQueueDoesIncludeQueueTimeDoesTimeOut() throws Exception {

        executor = TimeoutExecutor.makeTimeoutExecutor(1, DURATION_4, true, true, "");

        runTwoCallablesWithSecondTimingOut(DURATION_3, DURATION_3);
    }

    @Test(timeout = EXECUTOR_TEST_TIMEOUT)
    public void callableDoesQueueDoesntIncludeQueueTimeDoesntTimeOut() throws Exception {

        executor = TimeoutExecutor.makeTimeoutExecutor(1, DURATION_6, true, false, "");

        runTwoCallablesNoTimeout(DURATION_4, DURATION_4);
    }

    @Test(timeout = EXECUTOR_TEST_TIMEOUT)
    public void callableDoesQueueDoesntIncludeQueueTimeDoesTimeOut() throws Exception {

        executor = TimeoutExecutor.makeTimeoutExecutor(1, DURATION_4, true, false, "");

        runTwoCallablesWithSecondTimingOut(DURATION_3, DURATION_6);
    }

    // -------------------------------------------------------------------------------------------------------

    private void countDownIfNotNull(final CountDownLatch task_completed_latch) {

        if (task_completed_latch != null) {
            task_completed_latch.countDown();
        }
    }

    private void setResultIfNotNull(final AtomicInteger result, final int desired_result) {

        if (result != null) {
            result.set(desired_result);
        }
    }

    private void syncOnLatch(final CountDownLatch latch, final boolean wait) throws InterruptedException {

        if (latch != null) {
            if (wait) {
                latch.await();
            } else {
                latch.countDown();
            }
        }
    }

    private void runTask(final Duration duration, final CountDownLatch sync_latch, final boolean wait_for_latch, final CountDownLatch task_completed_latch, final CountDownLatch timed_out_latch, final CountDownLatch interrupted_latch, final AtomicInteger result, final int desired_result,
                         final boolean new_thread) {

        if (new_thread) {
            new Thread(() -> runTask(duration, sync_latch, wait_for_latch, task_completed_latch, timed_out_latch, interrupted_latch, result, desired_result, false)).start();
        } else {

            try {
                syncOnLatch(sync_latch, wait_for_latch);

                executor.executeWithTimeout(() -> {

                    try {
                        syncOnLatch(sync_latch, !wait_for_latch);
                        duration.sleep();
                        setResultIfNotNull(result, desired_result);
                        countDownIfNotNull(task_completed_latch);
                    } catch (final InterruptedException e) {
                        countDownIfNotNull(interrupted_latch);
                    }
                });
            } catch (final Exception e) {
                if (e instanceof TimeoutException) {
                    countDownIfNotNull(timed_out_latch);
                }
            }
        }
    }

    private int runCallable(final Duration duration, final CountDownLatch sync_latch, final boolean wait_for_latch, final CountDownLatch completed_latch, final CountDownLatch timed_out_latch, final int desired_result) {

        try {
            syncOnLatch(sync_latch, wait_for_latch);

            final int result = executor.executeWithTimeout(() -> {

                syncOnLatch(sync_latch, !wait_for_latch);
                duration.sleep();
                return desired_result;
            });
            countDownIfNotNull(completed_latch);
            return result;

        } catch (final Exception e) {
            if (e instanceof TimeoutException) {
                countDownIfNotNull(timed_out_latch);
            }
            return 0;
        }
    }

    private void runCallableInThread(final Duration duration, final CountDownLatch sync_latch, final boolean wait_for_latch, final CountDownLatch completed_latch, final CountDownLatch timed_out_latch, final AtomicInteger result, final int desired_result) {

        new Thread(() -> {

            final int result_value = runCallable(duration, sync_latch, wait_for_latch, completed_latch, timed_out_latch, desired_result);
            setResultIfNotNull(result, result_value);
        }).start();
    }

    private void runTwoTasksNoTimeout(final Duration first_duration, final Duration second_duration, final boolean new_threads) throws InterruptedException {

        final CountDownLatch first_task_completed_latch = new CountDownLatch(1);
        final CountDownLatch second_task_completed_latch = new CountDownLatch(1);

        final AtomicInteger first_location = new AtomicInteger();
        final AtomicInteger second_location = new AtomicInteger();

        runTask(first_duration, null, false, first_task_completed_latch, null, null, first_location, 1, new_threads);
        runTask(second_duration, null, false, second_task_completed_latch, null, null, second_location, 1, new_threads);

        first_task_completed_latch.await();
        second_task_completed_latch.await();

        assertThat(first_location.get(), is(equalTo(1)));
        assertThat(second_location.get(), is(equalTo(1)));
    }

    private void runTasks(Duration first_duration, Duration second_duration, boolean new_threads, CountDownLatch latch1, CountDownLatch latch2) throws InterruptedException {

        final CountDownLatch sync_latch = new CountDownLatch(1);
        final CountDownLatch first_task_completed_latch = new CountDownLatch(1);

        runTask(first_duration, sync_latch, false, first_task_completed_latch, null, null, null, 0, new_threads);
        runTask(second_duration, sync_latch, true, null, latch1, latch2, null, 0, new_threads);

        first_task_completed_latch.await();
    }

    private void runTwoTasksWithSecondTimingOut(final Duration first_duration, final Duration second_duration, @SuppressWarnings("SameParameterValue") final boolean new_threads) throws InterruptedException {

        final CountDownLatch second_task_timed_out_latch = new CountDownLatch(1);

        runTasks(first_duration, second_duration, new_threads, second_task_timed_out_latch, null);
        second_task_timed_out_latch.await();
    }

    private void runTwoTasksWithSecondInterrupted(final Duration first_duration, final Duration second_duration, @SuppressWarnings("SameParameterValue") final boolean new_threads) throws InterruptedException {

        final CountDownLatch second_task_interrupted_latch = new CountDownLatch(1);

        runTasks(first_duration, second_duration, new_threads, null, second_task_interrupted_latch);
        second_task_interrupted_latch.await();
    }

    private void runTwoCallablesNoTimeout(final Duration first_duration, final Duration second_duration) throws InterruptedException {

        final CountDownLatch first_task_completed_latch = new CountDownLatch(1);
        final CountDownLatch second_task_completed_latch = new CountDownLatch(1);

        final AtomicInteger first_result = new AtomicInteger();
        final AtomicInteger second_result = new AtomicInteger();

        runCallableInThread(first_duration, null, false, first_task_completed_latch, null, first_result, 2);
        runCallableInThread(second_duration, null, false, second_task_completed_latch, null, second_result, 3);

        first_task_completed_latch.await();
        second_task_completed_latch.await();

        assertThat(first_result.get(), is(equalTo(2)));
        assertThat(second_result.get(), is(equalTo(3)));
    }

    private void runTwoCallablesWithSecondTimingOut(final Duration first_duration, final Duration second_duration) throws InterruptedException {

        final CountDownLatch sync_latch = new CountDownLatch(1);
        final CountDownLatch first_task_completed_latch = new CountDownLatch(1);
        final CountDownLatch second_task_timed_out_latch = new CountDownLatch(1);

        runCallableInThread(first_duration, sync_latch, false, first_task_completed_latch, null, null, 2);
        runCallableInThread(second_duration, sync_latch, true, null, second_task_timed_out_latch, null, 2);

        first_task_completed_latch.await();
        second_task_timed_out_latch.await();
    }
}
