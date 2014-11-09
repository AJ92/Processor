package eu.imagine.framework;

import java.nio.FloatBuffer;

/**
 * Created with IntelliJ IDEA.
 * User: tamino
 * Date: 9/18/13
 * Time: 2:40 PM
 */
public interface Entity {
    public int getID();

    public boolean getVisibility();

    public FloatBuffer getFloatBuffer();
}
