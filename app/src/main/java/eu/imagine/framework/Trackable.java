package eu.imagine.framework;

import java.nio.FloatBuffer;

/**
 * Class that holds the relationship between marker and object to be rendered.
 */
public class Trackable {

    private FloatBuffer FLOATBUFFER;
    private int ID;
    private float[] TRANSLATION;
    private int x_coord = 0;
    private int y_coord = 0;

    public Trackable(final int ID,
                     float[] translation,
                     int x,
                     int y) {
        this.ID = ID;
        this.TRANSLATION = translation;
        this.x_coord = x;
        this.y_coord = y;
    }

    public String toString() {
        return "Trackable | ID:" + ID;
    }

    public float[] getTRANSLATION() {
        return this.TRANSLATION;
    }

    public int getX(){
        return x_coord;
    }

    public int getY(){
        return y_coord;
    }
}
