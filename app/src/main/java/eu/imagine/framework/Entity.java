package eu.imagine.framework;

import java.nio.FloatBuffer;

/**
 * Created with IntelliJ IDEA.
 * User: tamino
 * Date: 9/18/13
 * Time: 2:40 PM
 *
 *
 * modified by AJ...
 */
public interface Entity {
    public int getID();

    public boolean getVisibility();

    public String getProcess();

    //no need
    //public FloatBuffer getFloatBuffer();
}
