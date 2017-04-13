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

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Wrapper for fixed thread pool executor allowing actions to be executed subject to timeout.The method {@link #shutdown()} should be called before disposing of an instance, to avoid thread leakage.
 *
 * @author Graham Kirby (graham.kirby@st-andrews.ac.uk)
 */
public abstract class TimeoutExecutor {

    private ExecutorService first_stage_executor, second_stage_executor;
    protected Duration timeout;
    private final int thread_pool_size;
    private final String name;
    private static final AtomicInteger next_id = new AtomicInteger(0);

    // -------------------------------------------------------------------------------------------------------

    /**
     * Creates a timeout executor with the specified properties.
     *
     * @param thread_pool_size    the size of the thread pool used to execute each action
     * @param timeout             the timeout duration for the execution of each action
     * @param block               true if each call to execute an action should block until the action has completed
     * @param include_queued_time true if the time that an action spends queued before execution should be included when deciding timeouts
     * @return a timeout executor
     */
    public static TimeoutExecutor makeTimeoutExecutor(final int thread_pool_size, final Duration timeout, final boolean block, final boolean include_queued_time, final String name) {

        if (block) {
            if (include_queued_time) {
                return new BlockingIncludingQueuedTime(thread_pool_size, timeout, name);
            } else {
                return new BlockingNotIncludingQueuedTime(thread_pool_size, timeout, name);
            }
        } else {
            if (include_queued_time) {
                return new NonBlockingIncludingQueuedTime(thread_pool_size, timeout, name);
            } else {
                return new NonBlockingNotIncludingQueuedTime(thread_pool_size, timeout, name);
            }
        }
    }

    // -------------------------------------------------------------------------------------------------------

    private static Set<String> extant_executors = new HashSet<String>();

    private TimeoutExecutor(final int thread_pool_size, final Duration timeout, final String name) {

        this.thread_pool_size = thread_pool_size;
        this.timeout = timeout;
        initFirstStageExecutor(thread_pool_size);

        this.name = name + next_id.incrementAndGet();
        extant_executors.add(this.name);
        // System.out.println("\ncreated executor: " + this.name);
        // if (this.name.equals("ProcessManager kill executor5")) {
        // Diagnostic.printStackTrace();
        // System.out.println();
        // }
    }

    // -------------------------------------------------------------------------------------------------------

    /**
     * Executes the given action subject to the initialization parameters of this executor.
     *
     * @param action the action
     * @throws TimeoutException     if the action times out
     * @throws InterruptedException if the action is interrupted
     */
    public abstract void executeWithTimeout(Runnable action) throws TimeoutException, InterruptedException;

    /**
     * Executes the given action subject to the initialization parameters of this executor. If this executor is not set to block, an exception is thrown.
     *
     * @param <T>    the result type of the action
     * @param action the action
     * @return the result of the action
     * @throws Exception {@link TimeoutException} if the action times out, {@link InterruptedException} if the action is interrupted, {@link UnsupportedOperationException} if this executor was initialized with <code>block</code> set to false, or an exception thrown by the action
     */
    public abstract <T> T executeWithTimeout(final Callable<T> action) throws Exception;

    // -------------------------------------------------------------------------------------------------------

    public Duration getTimeout() {

        return timeout;
    }

    /**
     * Shuts down the executor and its thread pool(s).
     */
    public void shutdown() {

        // System.out.println("shutting down executor: " + name);

        first_stage_executor.shutdownNow();
        updateThreadCount(-thread_pool_size);
        if (second_stage_executor != null) {
            second_stage_executor.shutdownNow();
            updateThreadCount(-thread_pool_size);
        }
        extant_executors.remove(name);
        // System.out.println(">>>>>>>\nextant now:");
        // for (final String s : extant_executors) {
        // System.out.print(s + ", ");
        // }
        // System.out.println();
    }

    // -------------------------------------------------------------------------------------------------------

    protected abstract <T> T executeWithTimeout(final Callable<T> action, final ExecutorService executor, final boolean do_timeout) throws Exception;

    // -------------------------------------------------------------------------------------------------------

    private void initFirstStageExecutor(final int thread_pool_size) {

        first_stage_executor = Executors.newFixedThreadPool(thread_pool_size);

        updateThreadCount(thread_pool_size);
    }

    private static volatile int thread_count;

    private static synchronized void updateThreadCount(final int thread_pool_size) {

        thread_count += thread_pool_size;
        // System.out.println("thread count: " + thread_count);
    }

    protected void initSecondStageExecutor(final int thread_pool_size) {

        second_stage_executor = Executors.newFixedThreadPool(thread_pool_size);
        updateThreadCount(thread_pool_size);
    }

    protected void executeWithTimeoutIncludingQueuedTime(final Runnable runnable) throws TimeoutException, InterruptedException {

        final Callable<Object> action = Executors.callable(runnable);

        executeActionDerivedFromRunnable(action, true);
    }

    protected <T> T executeWithTimeoutIncludingQueuedTime(final Callable<T> action) throws Exception {

        return executeWithTimeout(action, first_stage_executor, true);
    }

    private void executeActionDerivedFromRunnable(final Callable<Object> action, final boolean do_timeout) throws TimeoutException, InterruptedException {

        try {
            executeWithTimeout(action, first_stage_executor, do_timeout);
        } catch (final Exception e) {
            if (e instanceof TimeoutException) {
                throw (TimeoutException) e;
            }
            if (e instanceof InterruptedException) {
                throw (InterruptedException) e;
            }
            throw new IllegalStateException("Exception not timeout or interrupted");
        }
    }

    protected void executeWithTimeoutNotIncludingQueuedTime(final Runnable runnable) throws TimeoutException, InterruptedException {

        final Callable<Object> action = Executors.callable(runnable);

        final Callable<Object> action_with_timeout = new Callable<Object>() {

            @Override
            public Object call() throws Exception {

                executeWithTimeout(action, second_stage_executor, true);
                return null;
            }
        };

        executeActionDerivedFromRunnable(action_with_timeout, false);
    }

    protected <T> T executeWithTimeoutNotIncludingQueuedTime(final Callable<T> action) throws Exception {

        final Callable<T> action_with_timeout = new Callable<T>() {

            @Override
            public T call() throws Exception {

                return executeWithTimeout(action, second_stage_executor, true);
            }
        };

        return executeWithTimeout(action_with_timeout, first_stage_executor, false);
    }

    // -------------------------------------------------------------------------------------------------------

    private static Exception launderThrowable(final Throwable t) {

        if (t instanceof Exception) {
            return (Exception) t;
        } else if (t instanceof Error) {
            System.err.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>> ERROR from execution exception");
            System.err.println(t.getClass().getName());
            t.printStackTrace();
            throw (Error) t;
        } else {
            throw new IllegalStateException("Throwable not exception or error", t);
        }
    }

    // -------------------------------------------------------------------------------------------------------

    private static final class BlockingNotIncludingQueuedTime extends TimeoutExecutor {

        private BlockingNotIncludingQueuedTime(final int thread_pool_size, final Duration timeout, final String name) {

            super(thread_pool_size, timeout, name);
            initSecondStageExecutor(thread_pool_size);
        }

        // -------------------------------------------------------------------------------------------------------

        @Override
        public void executeWithTimeout(final Runnable runnable) throws TimeoutException, InterruptedException {

            executeWithTimeoutNotIncludingQueuedTime(runnable);
        }

        @Override
        public <T> T executeWithTimeout(final Callable<T> action) throws Exception {

            return executeWithTimeoutNotIncludingQueuedTime(action);
        }

        // -------------------------------------------------------------------------------------------------------

        @Override
        protected <T> T executeWithTimeout(final Callable<T> action, final ExecutorService executor, final boolean do_timeout) throws Exception {

            final Future<T> task = executor.submit(action);

            try {
                if (do_timeout) {
                    return task.get(timeout.getLength(), timeout.getTimeUnit());
                } else {
                    return task.get();
                }
            } catch (final ExecutionException e) {
                throw launderThrowable(e.getCause());
            } finally {
                task.cancel(true);
            }
        }
    }

    // -------------------------------------------------------------------------------------------------------

    private static final class BlockingIncludingQueuedTime extends TimeoutExecutor {

        private BlockingIncludingQueuedTime(final int thread_pool_size, final Duration timeout, final String name) {

            super(thread_pool_size, timeout, name);
        }

        // -------------------------------------------------------------------------------------------------------

        @Override
        public void executeWithTimeout(final Runnable runnable) throws TimeoutException, InterruptedException {

            executeWithTimeoutIncludingQueuedTime(runnable);
        }

        @Override
        public <T> T executeWithTimeout(final Callable<T> action) throws Exception {

            return executeWithTimeoutIncludingQueuedTime(action);
        }

        // -------------------------------------------------------------------------------------------------------

        @Override
        protected <T> T executeWithTimeout(final Callable<T> action, final ExecutorService executor, final boolean ignored) throws Exception {

            final Future<T> task = executor.submit(action);

            try {
                return task.get(timeout.getLength(), timeout.getTimeUnit());
            } catch (final ExecutionException e) {
                throw launderThrowable(e.getCause());
            } finally {
                task.cancel(true);
            }
        }
    }

    // -------------------------------------------------------------------------------------------------------

    private static final class NonBlockingNotIncludingQueuedTime extends TimeoutExecutor {

        private ExecutorService timeout_watch_executor;

        private NonBlockingNotIncludingQueuedTime(final int thread_pool_size, final Duration timeout, final String name) {

            super(thread_pool_size, timeout, name);
            initSecondStageExecutor(thread_pool_size);
            timeout_watch_executor = Executors.newFixedThreadPool(thread_pool_size);
        }

        // -------------------------------------------------------------------------------------------------------

        @Override
        public void executeWithTimeout(final Runnable runnable) throws TimeoutException, InterruptedException {

            executeWithTimeoutNotIncludingQueuedTime(runnable);
        }

        @Override
        public <T> T executeWithTimeout(final Callable<T> action) throws Exception {

            throw new UnsupportedOperationException();
        }

        // -------------------------------------------------------------------------------------------------------
        private static AtomicInteger overall_count = new AtomicInteger(1);
        private static AtomicInteger live_count = new AtomicInteger(1);

        @Override
        protected <T> T executeWithTimeout(final Callable<T> action, final ExecutorService executor, final boolean do_timeout) throws Exception {

            if (do_timeout) {

                final CountDownLatch indirection_initialized = new CountDownLatch(1);
                final AtomicReference<Future<T>> future_indirection = new AtomicReference<Future<T>>();

                final Callable<T> action_with_timeout_watcher = new Callable<T>() {

                    @Override
                    public T call() throws Exception {

                        timeout_watch_executor.submit(new Runnable() {

                            @Override
                            public void run() {

                                try {
                                    indirection_initialized.await();
                                } catch (final InterruptedException e) {
                                    Diagnostic.trace("interrupted while waiting for latch indirection to be initialized");
                                }

                                Future<T> future = null;
                                try {
                                    future = future_indirection.get();
                                    future.get(timeout.getLength(), timeout.getTimeUnit());
                                } catch (final Exception e) {
                                    // Ignore.
                                } finally {
                                    future.cancel(true);
                                    live_count.decrementAndGet();
                                }
                            }
                        });
                        return action.call();
                    }
                };

                Future<T> future = executor.submit(action_with_timeout_watcher);
                future_indirection.set(future);
                indirection_initialized.countDown();

                return null;
            } else {

                final Future<T> task = executor.submit(action);

                try {
                    return task.get();
                } catch (final ExecutionException e) {
                    throw launderThrowable(e.getCause());
                } finally {
                    task.cancel(true);
                }
            }
        }
    }

    // -------------------------------------------------------------------------------------------------------

    private static final class NonBlockingIncludingQueuedTime extends TimeoutExecutor {

        private NonBlockingIncludingQueuedTime(final int thread_pool_size, final Duration timeout, final String name) {

            super(thread_pool_size, timeout, name);
        }

        // -------------------------------------------------------------------------------------------------------

        @Override
        public void executeWithTimeout(final Runnable runnable) throws TimeoutException, InterruptedException {

            executeWithTimeoutIncludingQueuedTime(runnable);
        }

        @Override
        public <T> T executeWithTimeout(final Callable<T> action) throws Exception {

            throw new UnsupportedOperationException();
        }

        // -------------------------------------------------------------------------------------------------------

        private static int count = 1;

        @Override
        protected <T> T executeWithTimeout(final Callable<T> action, final ExecutorService executor, final boolean ignored) throws Exception {

            final Future<T> task = executor.submit(action);

            System.out.println("NonBlockingIncludingQueuedTime thread count: " + count++);
            new Thread() {

                @Override
                public void run() {

                    try {
                        task.get(timeout.getLength(), timeout.getTimeUnit());
                    } catch (final Exception e) {
                        // Ignore.
                    } finally {
                        task.cancel(true);
                    }
                }
            }.start();

            return null;
        }
    }
}
