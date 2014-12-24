package com.aj.processor.app;


import android.content.Context;
import android.graphics.PixelFormat;
import android.opengl.GLSurfaceView;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

import java.util.Calendar;

/**
 * Created by AJ on 14.11.2014.
 */

//TODO: create a callback from the openGL renderer to this view and try to set up NodeView
public class OpenGLSurfaceView extends GLSurfaceView {

    private final OpenGLEngine renderer;
    private MainInterface mainInterface;
    private NodeView nodeView;
    private ScaleGestureDetector sgd;
    private String TAG = "OpenGLSurfaceView";

    public OpenGLSurfaceView(Context context) {
        super(context);

        // Create an OpenGL ES 2.0 context.
        setEGLContextClientVersion(2);

        setEGLContextClientVersion(2);
        setEGLConfigChooser(8, 8, 8, 8, 16, 0);

        renderer = new OpenGLEngine();

        setRenderer(renderer);

        // old : TRANSLUCENT  possible fix for sigseg fault...
        getHolder().setFormat(PixelFormat.TRANSPARENT);
        setZOrderMediaOverlay(true);



        // Render the view all the time...
        setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);

        sgd = new ScaleGestureDetector(context, new ScaleListener());
    }

    private final float TOUCH_SCALE_FACTOR = 180.0f / 640;
    private float mPreviousX;
    private float mPreviousY;

    private float mPreviousScale = 1.0f;


    public final static int rotMode = 0x0001;
    public final static int moveMode = 0x0002;
    public final static int scaleMode = 0x0004;

    private int interactMode = rotMode;

    private float max_scale = 64.0f;
    private float min_scale = 0.3f;


    //recognize clicks
    private final int MAX_CLICK_DURATION = 400;
    private final int MAX_CLICK_DISTANCE = 5;
    private long startClickTime;

    private float x1;
    private float y1;
    private float x2;
    private float y2;




    @Override
    public boolean onTouchEvent(MotionEvent e) {
        // MotionEvent reports input details from the touch screen
        // and other input controls. In this case, you are only
        // interested in events where the touch position changed.

        float x = e.getX();
        float y = e.getY();

        if(interactMode == scaleMode) {
            //detect pinch gesture...
            sgd.onTouchEvent(e);
            scale_renderer(mPreviousScale);
        }

        switch (e.getAction()) {
            case MotionEvent.ACTION_MOVE:

                float dx = x - mPreviousX;
                float dy = y - mPreviousY;

                if (interactMode == rotMode) {
                    rotate_renderer(dx, dy);
                } else if (interactMode == moveMode) {
                    pos_renderer(dx, dy);
                }

                //not required we render continuously...
                //requestRender();
                break;


            case MotionEvent.ACTION_DOWN: {
                startClickTime = Calendar.getInstance().getTimeInMillis();
                x1 = e.getX();
                y1 = e.getY();
                break;
            }

            case MotionEvent.ACTION_UP:

                long clickDuration = Calendar.getInstance().getTimeInMillis() - startClickTime;
                x2 = e.getX();
                y2 = e.getY();
                float dx_click = x2 - x1;
                float dy_click = y2 - y1;

                if (clickDuration < MAX_CLICK_DURATION && dx_click < MAX_CLICK_DISTANCE && dy_click < MAX_CLICK_DISTANCE){
                    //Debugger.error(TAG, "CLICK recognized");
                    dispatchTouch(x2, y2);
                }

                //not required we render continuously...
                //requestRender();
        }

        mPreviousX = x;
        mPreviousY = y;
        return true;
    }

    //dispatch the touch click event to its functions...
    private void dispatchTouch(float x, float y){
        //left upper corner (toogle capturing of markers...)
        if(x < 100.0f){
            if(y < 100.0f){
                //stop capturing... or start again...
                if(mainInterface != null){
                    Log.e(TAG,"Toggle capturer");
                    mainInterface.toggleOpenCV();
                    Log.e(TAG,"Toggle capturer done !");
                    //reset renderer pos...
                    resetRenderer();
                    return;
                }
            }
        }

        //right lower corner (toogle move or rotate...)
        if(x < 200.0f){
            if(y > (getHeight() - 200.0f)){
                if(interactMode == moveMode){
                    interactMode = scaleMode;
                    Log.e(TAG,"switched interaction mode to scale");
                    set_renderer_interact_mode();
                    return;
                }
                else if(interactMode == scaleMode){
                    interactMode = rotMode;
                    Log.e(TAG,"switched interaction mode to rot");
                    set_renderer_interact_mode();
                    return;

                }
                else if(interactMode == rotMode){
                    interactMode = moveMode;
                    Log.e(TAG,"switched interaction mode to move");
                    set_renderer_interact_mode();
                    return;
                }
            }
        }

        //else
        rendererTouch(x,y);
    }


    private void set_renderer_interact_mode(){
        if(renderer != null){
            renderer.setInteractMode(this.interactMode);
        }
    }


    private void rotate_renderer(float dx, float dy){
        if(renderer != null){
            renderer.setAngleX(renderer.getAngleX() + (dx * TOUCH_SCALE_FACTOR) );
            renderer.setAngleY(renderer.getAngleY() + (dy * TOUCH_SCALE_FACTOR) );
        }
    }

    private void scale_renderer(float scl){
        if(renderer != null) {
            renderer.setScale(scl);
        }
    }

    private void pos_renderer(float x, float y){
        if(renderer != null) {
            renderer.setPosX(renderer.getPosX() + x);
            renderer.setPosY(renderer.getPosY() + y);
        }
    }


    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {



            mPreviousScale *= (1.0f / detector.getScaleFactor());
            mPreviousScale = Math.max(min_scale, Math.min(mPreviousScale, max_scale));
            return true;
        }
    }

    public void setMaininterFace(MainInterface mainInterface){
        if(renderer != null) {
            this.mainInterface = mainInterface;
            renderer.setMainInterFace(mainInterface);
        }
    }

    public void setNodeView(NodeView nv){
        this.nodeView = nv;
    }


    private void resetRenderer(){
        if(renderer != null) {
            //reset scale, rotate and pos of renderer...
            renderer.setPosX(0);
            renderer.setPosY(0);
            scale_renderer(1.0f);
            mPreviousScale = 1.0f;
            renderer.setAngleX(0.0f);
            renderer.setAngleY(0.0f);
        }
    }

    private void rendererTouch(float x, float y){
        if(renderer != null) {
            //redirect the touch to the renderer...
            renderer.onTouch(x,y);
        }
    }
}
