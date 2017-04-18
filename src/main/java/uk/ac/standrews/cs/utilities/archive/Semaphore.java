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
 * Created on Dec 20, 2004 at 10:20:49 PM.
 */

package uk.ac.standrews.cs.utilities.archive;

/**
 * Semaphore implementation.
 *
 * @author graham
 */
@SuppressWarnings("WeakerAccess")
public class Semaphore {

    /**
     * Flag that allows all synchronisation to be disabled.
     */
    private static final boolean SYNCHRONISATION_DISABLED = false;

    private int semaphore_value;

    public Semaphore(int semaphore_value) {

        this.semaphore_value = semaphore_value;
    }

    /**
     * Wait operation, with specified timeout.
     *
     * @param idle_timeout time in milliseconds after which the operation returns anyway
     */
    public synchronized void semWait(int idle_timeout) {

        if (!SYNCHRONISATION_DISABLED) {

            semaphore_value--;

            // If necessary, block using Object.wait.
            if (semaphore_value < 0) {
                try {
                    wait(idle_timeout);
                } catch (InterruptedException e) {
                    System.out.println("timed-out, returning without waiting for semSignal");
                }
            }
        }
    }

    /**
     * Wait operation.
     */
    public synchronized void semWait() {

        // Zero indicates no timeout.
        semWait(0);
    }

    /**
     * Signal operation.
     */
    public synchronized void semSignal() {

        if (!SYNCHRONISATION_DISABLED) {

            // If necessary, resume a waiting thread using Object.notify.
            if (semaphore_value < 0) notify();

            semaphore_value++;
        }
    }

    /**
     * Returns the number of threads waiting on this semaphore
     *
     * @return the number of threads waiting on this semaphore
     */
    public synchronized int numberWaiting() {

        // Threads are waiting if the value is negative.
        return Math.max(-semaphore_value, 0);
    }
}