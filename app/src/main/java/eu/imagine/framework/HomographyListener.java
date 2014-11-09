package eu.imagine.framework;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: tamino
 * Date: 9/17/13
 * Time: 2:13 PM
 */
public interface HomographyListener {
    public void receiveHomographies(ArrayList<Marker> foundMarkers);
}
