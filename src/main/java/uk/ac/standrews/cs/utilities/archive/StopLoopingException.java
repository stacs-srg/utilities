package uk.ac.standrews.cs.utilities.archive;


public class StopLoopingException extends Exception {

    private final Exception action_exception;

    public StopLoopingException(final Exception action_exception) {

        this.action_exception = action_exception;
    }

    public Exception getActionException() {

        return action_exception;
    }
}
