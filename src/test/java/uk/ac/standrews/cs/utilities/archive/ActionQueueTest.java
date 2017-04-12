/*
 * Created on Dec 20, 2004 at 10:14:33 PM.
 */
package uk.ac.standrews.cs.utilities.archive;

import org.junit.Ignore;
import org.junit.Test;

/**
 * Test class for ActionQueue.
 *
 * @author graham
 */
public class ActionQueueTest {

    /**
     * Tests whether all of a set of queued actions are eventually completed.
     * It's not clear how to test timing issues given that we can't assume
     * anything about relative progress of threads.
     */
    @Test
    @Ignore
    public void testQueue() {

        // Single action, single queue slot, single thread.
        testQueue(1, 1000, 0, 1, 1, 10000);

        // Five actions, two threads.
        testQueue(5, 1000, 0, 3, 2, 10000);

        // Should result in some threads dying of boredom.
        testQueue(10, 3000, 500, 15, 15, 500);
    }

    /**
     * Queues a number of actions and waits for them all to complete.
     * Correctness condition is simply that the method completes.
     */
    private void testQueue(int number_of_actions, int action_delay_millis, int submission_delay, int queue_capacity, int number_of_threads, int timeout) {

        Semaphore[] semaphores = new Semaphore[number_of_actions];

        ActionQueue queue = new ActionQueue(queue_capacity, number_of_threads, timeout);

        // Create a number of actions.
        for (int i = 0; i < semaphores.length; i++) {

            // Create a semaphore on which the main thread will wait.
            Semaphore s = new Semaphore(0);
            semaphores[i] = s;

            // Queue an action that will wait for the given period.
            queue.enqueue(new TestAction(s, action_delay_millis));

            try {
                Thread.sleep(submission_delay);
            } catch (InterruptedException e) {/* no action */}
        }

        // Wait for all the actions to complete.
        for (Semaphore semaphore : semaphores) semaphore.semWait();
    }

    /**
     * Action waits for given period and signals completion.
     */
    private class TestAction implements Action {

        private Semaphore sync;
        private int delay_millis;

        TestAction(Semaphore sync, int delay_millis) {
            this.sync = sync;
            this.delay_millis = delay_millis;
        }

        // Delay for the specified time and then signal completion.
        public void performAction() {

            try {
                Thread.sleep(delay_millis);
            } catch (InterruptedException e) {/* no action */}

            sync.semSignal();
        }
    }
}