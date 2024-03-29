package com.aj.processor.app;

import eu.imagine.framework.Entity;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * Created with IntelliJ IDEA.
 * User: tamino
 * Date: 9/18/13
 * Time: 2:57 PM
 */
public class Tracking implements Entity {

    private boolean visible;
    private FloatBuffer FLOATBUFFER;
    private int ID;
    private String process;

    public Tracking(int ID, String process, boolean visible, float[] verticeData) {
        this.ID = ID;
        this.process = process;
        this.visible = visible;

        //no need
        /*
        // For performance reasons, we build the buffer here:
        this.FLOATBUFFER = ByteBuffer.allocateDirect(verticeData.length *
                4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        this.FLOATBUFFER.put(verticeData).position(0);
        */
    }

    @Override
    public int getID() {
        return ID;
    }

    @Override
    public boolean getVisibility() {
        return this.visible;
    }

    //new
    @Override
    public String getProcess(){
        return this.process;
    }

    //no need
    /*
    @Override
    public FloatBuffer getFloatBuffer() {
        return this.FLOATBUFFER;
    }
    */
}
