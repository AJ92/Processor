package com.aj.processor.app;

import com.aj.processor.app.XML.Process.PComponent;

/**
 * Created by AJ on 24.12.2014.
 */
public class NodeTask {

    private MainInterface mainInterfaceInstance;
    private PComponent dataNode;

    public NodeTask(){
        // Gets a handle to the object that creates the thread pools
        mainInterfaceInstance = MainInterface.getInstance();
    }

    public void setDataNode(PComponent pc){
        dataNode = pc;
    }

    public PComponent getDataNode(){
        return dataNode;
    }




    public void handleTaskState(int state) {
        //0x0000 is not used...
        int outState = 0x0000;
        // Converts the decode state to the overall state.
        switch(state) {
            case OpenGLEngine.OPENGLENGINE_STATE_NODEUPDATE:
                outState = MainInterface.MAININTERFACE_STATE_NODEUPDATE;
                break;
            case OpenGLEngine.OPENGLENGINE_STATE_NODEHIDE:
                outState = MainInterface.MAININTERFACE_STATE_NODEHIDE;
                break;

        }

        handleState(outState);
    }


    // Passes the state to PhotoManager
    private void handleState(int state) {
        /*
         * Passes a handle to this task and the
         * current state to the class that created
         * the thread pools
         */
        if(mainInterfaceInstance != null) {
            mainInterfaceInstance.handleState(this, state);
        }
    }
}
