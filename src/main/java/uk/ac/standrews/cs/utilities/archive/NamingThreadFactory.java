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
package uk.ac.standrews.cs.utilities.archive;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;

/**
 * A wrapper around {@link Executors#defaultThreadFactory()} that names the threads using a given prefix concatenated with an atomically increasing integer starting from <code>0</code>.
 *
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 */
@SuppressWarnings("unused")
public final class NamingThreadFactory implements ThreadFactory {

    private static final UncaughtExceptionHandler PRINT_UNCAUGHT_EXCEPTIONS = (t, e) -> e.printStackTrace();

    private final AtomicLong sequence_number;
    private final String naming_prefix;
    private final boolean debug;

    /**
     * Instantiates a new naming thread factory.
     *
     * @param naming_prefix the naming prefix to be given to generated threads
     */
    public NamingThreadFactory(final String naming_prefix) {

        this(naming_prefix, false);
    }

    /**
     * Instantiates a new naming thread factory.
     *
     * @param naming_prefix the naming prefix to be given to generated threads
     * @param debug         whether to print out the stack trace of uncaught exceptions within a created thread
     */
    @SuppressWarnings("WeakerAccess")
    public NamingThreadFactory(final String naming_prefix, final boolean debug) {

        this.naming_prefix = naming_prefix;
        this.debug = debug;
        sequence_number = new AtomicLong(0);
    }

    @Override
    public Thread newThread(final Runnable arg0) {

        final Thread new_thread = Executors.defaultThreadFactory().newThread(arg0);
        final String name = generateName();

        if (debug) {
            new_thread.setUncaughtExceptionHandler(PRINT_UNCAUGHT_EXCEPTIONS);
        }

        new_thread.setName(name);
        return new_thread;
    }

    private String generateName() {

        return naming_prefix + sequence_number.getAndIncrement();
    }
}
