package eu.imagine.framework;

import android.util.Log;

import com.aj.processor.app.MainInterface;

import java.util.*;

/**
 * This class implements a thread safe timer for multiple objects and
 * standardized logging functions for the framework.
 */
public class Messenger {

    /**
     * Variable that stores singleton instance.
     */
    private static Messenger INSTANCE;
    /**
     * The stack with which the TimerResult objects are managed.
     */
    private HashMap<Object, Stack<TimerResult>> timers;
    private ArrayList<MessageInterface> listeners;

    /**
     * Method to return the singleton instance of Messenger.
     *
     * @return Instance of Messenger.
     */
    public synchronized static Messenger getInstance() {
        if (INSTANCE == null)
            INSTANCE = new Messenger();
        return INSTANCE;
    }

    /**
     * Private constructor that prepares class. To get an instance of
     * Messenger, ust the getInstance Method.
     */
    private Messenger() {
        this.timers = new HashMap<Object, Stack<TimerResult>>();
        this.listeners = new ArrayList<MessageInterface>();
    }

    /**
     * Method for logging a message to the Android log.
     *
     * @param tag     The tag used when logging after the internal tag.
     * @param content The content of the message to log.
     */
    public synchronized void log(final String tag, final String content) {
        sendMessage("Messenger|"+tag+" :log: "+content);
        Log.i("Messenger|" + tag, content);
    }

    /**
     * Method for logging only debugging content. DEBUG_LOGGING is set in
     * MainInterface.
     *
     * @param tag     The tag used after the internal tag.
     * @param content The content of the message to log.
     */
    public synchronized void debug(final String tag, final String content) {
        sendMessage("Messenger|"+tag+" :debug: "+content);
        if (MainInterface.DEBUG_LOGGING)
            Log.d("Messenger|" + tag, content);
    }

    /**
     * Method for placing a timer object on the stack with the given label.
     *
     * @param object Object reference to allow the correct stack to be used.
     * @param label  The label to remember for the timer.
     */
    public synchronized void pushTimer(Object object, final String label) {
        // Make sure that we don't keep too many objects:
        if (timers.size() > 32) {
            log("Messenger", "WARNING: Excessive amount of stacks required " +
                    "for timer function – possible memory leak!");
            Iterator<Map.Entry<Object, Stack<TimerResult>>> it = timers.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<Object, Stack<TimerResult>> pairs = it.next();
                if ((pairs.getValue()).isEmpty()) {
                    it.remove();
                }
            }
        }
        TimerResult data = new TimerResult(System.currentTimeMillis(), label);
        if (timers.containsKey(object)) {
            timers.get(object).push(data);
        } else {
            Stack<TimerResult> stack = new Stack<TimerResult>();
            stack.push(data);
            timers.put(object, stack);
        }
    }

    /**
     * Method for reading a timer object. Returns a TimerResult object which
     * contains the time spent between push and pop,
     * and the label placed originally.
     *
     * @param object Reference to object from which we will use the timer
     *               stack.
     * @return The TimerResult object containing the time difference and the
     *         label. If the stack is empty (meaning more pops than pushes
     *         were done), an object with time "-1" and label "EMPTY STACK" are
     *         returned.
     */
    public synchronized TimerResult popTimer(Object object) {
        if (timers.containsKey(object)) {
            Stack<TimerResult> stack = timers.get(object);
            if (stack.isEmpty())
                return new TimerResult(-1, "EMPTY STACK");
            TimerResult poped = stack.pop();
            poped.time = System.currentTimeMillis() - poped.time;
            return poped;
        } else {
            return new TimerResult(-1, "OBJECT HAS NO STACK");
        }
    }

    /**
     * Sends a string to all listeners.
     * @param msg The complete message to send.
     */
    private void sendMessage(String msg) {
        for (MessageInterface listener : listeners)
            listener.notify(msg);
    }

    /**
     * Method for registering a listener for log and debug messages.
     * @param object The object to register.
     */
    @SuppressWarnings("UnusedDeclaration")
    public void registerListener(MessageInterface object) {
        listeners.add(object);
    }

    /**
     * Method for removing a listener for log and debug messages.
     * @param object The object to remove.
     */
    @SuppressWarnings("UnusedDeclaration")
    public void removeListener(MessageInterface object) {
        listeners.remove(object);
    }
}
