package eu.imagine.framework;

/**
 * Class representation for a timer object that contains the label and time
 * spent between push and pop.
 */
public class TimerResult {
    /**
     * The time from when the timer was pushed to when it was popped.
     */
    public long time;
    /**
     * The label given on push. Can be used for identification purposes.
     */
    String label;

    TimerResult(long time, String label) {
        this.time = time;
        this.label = label;
    }
}
