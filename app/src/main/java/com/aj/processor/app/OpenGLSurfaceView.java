package com.aj.processor.app;


import android.content.Context;
import android.graphics.PixelFormat;
import android.opengl.GLSurfaceView;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

/**
 * Created by AJ on 14.11.2014.
 */
public class OpenGLSurfaceView extends GLSurfaceView {

    private final OpenGLEngine renderer;
    private MainInterface mainInterface;
    private ScaleGestureDetector sgd;
    private String tag = "OpenGLSurfaceView";

    public OpenGLSurfaceView(Context context) {
        super(context);

        // Create an OpenGL ES 2.0 context.
        setEGLContextClientVersion(2);

        setEGLContextClientVersion(2);
        setEGLConfigChooser(8, 8, 8, 8, 16, 0);

        renderer = new OpenGLEngine();
        setRenderer(renderer);


        getHolder().setFormat(PixelFormat.TRANSLUCENT);
        setZOrderMediaOverlay(true);



        // Render the view only when there is a change in the drawing data
        setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);

        sgd = new ScaleGestureDetector(context, new ScaleListener());
    }

    private final float TOUCH_SCALE_FACTOR = 180.0f / 640;
    private float mPreviousX;
    private float mPreviousY;

    private float mPreviousScale = 1.0f;


    private int rotMode = 0x0001;
    private int moveMode = 0x0002;

    private int interactMode = rotMode;

    private float max_scale = 8.0f;
    private float min_scale = 0.3f;

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        // MotionEvent reports input details from the touch screen
        // and other input controls. In this case, you are only
        // interested in events where the touch position changed.

        float x = e.getX();
        float y = e.getY();

        //detect pinch gesture...
        sgd.onTouchEvent(e);
        scale_renderer(mPreviousScale);

        switch (e.getAction()) {
            case MotionEvent.ACTION_MOVE:

                float dx = x - mPreviousX;
                float dy = y - mPreviousY;

                if(interactMode == rotMode) {
                    rotate_renderer(dx, dy);
                }
                else if(interactMode == moveMode){
                    pos_renderer(dx, dy);
                }

                requestRender();
                break;
            case MotionEvent.ACTION_UP:

                //left lower corner (toogle capturing of markers...)
                if(x < 100.0f){
                    if(y > (getHeight() - 100.0f)){
                        //stop capturing... or start again...
                        if(mainInterface != null){
                            mainInterface.toggleOpenCV();
                        }
                        //reset renderer pos...
                        pos_renderer(0.0f, 0.0f);
                        scale_renderer(1.0f);
                    }
                }

                //right lower corner (toogle move or rotate...)
                if(x > (getWidth() - 100.0f)){
                    if(y > (getHeight() - 100.0f)){
                        if(interactMode == moveMode){
                            interactMode = rotMode;
                            Log.e(tag,"switched interaction mode to rotate");
                        }
                        else{
                            interactMode = moveMode;
                            Log.e(tag,"switched interaction mode to move");
                        }
                    }
                }
                requestRender();
                break;
        }

        mPreviousX = x;
        mPreviousY = y;
        return true;
    }

    private void rotate_renderer(float dx, float dy){
        renderer.setAngleX(renderer.getAngleX() + (dx * TOUCH_SCALE_FACTOR) );
        renderer.setAngleY(renderer.getAngleY() + (dy * TOUCH_SCALE_FACTOR) );
    }

    private void scale_renderer(float scl){
        renderer.setScale(scl);
    }

    private void pos_renderer(float x, float y){
        renderer.setPosX(renderer.getPosX() + x);
        renderer.setPosY(renderer.getPosY() + y);
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
}
