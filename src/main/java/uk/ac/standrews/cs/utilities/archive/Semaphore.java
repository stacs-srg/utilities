/*
 * Created on Dec 20, 2004 at 10:20:49 PM.
 */

package uk.ac.standrews.cs.utilities.archive;

/**
 * Semaphore implementation.
 *
 * @author graham
 */
public class Semaphore {

    /**
     * Flag that allows all synchronisation to be disabled.
     */
    protected static boolean SYNCHRONISATION_DISABLED = false;

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