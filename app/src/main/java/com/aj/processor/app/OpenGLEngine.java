package com.aj.processor.app;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;

import com.aj.processor.app.graphics.camera.Camera;
import com.aj.processor.app.graphics.model.Components.Mesh;
import com.aj.processor.app.graphics.model.Model;
import com.aj.processor.app.graphics.model.parser.format_obj.Loader_obj;
import com.aj.processor.app.graphics.object.CompositeObject;
import com.aj.processor.app.graphics.object.Positation;
import com.aj.processor.app.graphics.shader.Shader;
import com.aj.processor.app.graphics.world.ObjectWorld;
import com.aj.processor.app.mathematics.Matrix.Matrix4x4;
import com.aj.processor.app.mathematics.Vector.Vector3;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import java.io.IOException;
import java.nio.FloatBuffer;
import java.util.ArrayList;

import eu.imagine.framework.Messenger;
import eu.imagine.framework.Trackable;

/**
 * Handles the rendering of the objects.
 */
public class OpenGLEngine implements GLSurfaceView.Renderer {

    private final MainInterface mainInterface;
    private final String TAG = "OpenGLEngine";

    private ArrayList<Trackable> toRender;

    private int programHandle;

    private int mMVPMatrixHandle;
    private int mPositionHandle;
    private int mTexCoordHandle;
    private int mNormalHandle;

    private int mTextureUniformHandle;

    private float[] mModelMatrix = new float[16];
    private float[] mViewMatrix = new float[16];
    private float[] mProjectionMatrix = new float[16];
    private float[] mMVPMatrix = new float[16];

    private final int mPositionOffset = 0;
    private final int mPositionDataSize = 3;
    private final int mColorOffset = 3;
    private final int mColorDataSize = 4;
    private final int mBytesPerFloat = 4;
    private final int mStrideBytes = 7 * mBytesPerFloat;









    //---------------------- new and better stuff ----------------------------


    //window dimensions, will be used to place models and more...
    //coords are retreived from onSurfaceChanged...
    private int window_size_x = 0;
    private int window_size_y = 0;
    //our 3 dimensional world, that stores all the objects!
    private ObjectWorld ow;
    //DEFAULT MODEL to render in case we have broken models or so...
    private Model default_mdl;
    private CompositeObject default_co;
    private Camera default_cam;


    //all the matrices we need... pre init them to identity
    private Matrix4x4 p_m = new Matrix4x4();
    private Matrix4x4 v_m = new Matrix4x4();
    private Matrix4x4 m_m = new Matrix4x4();
    private Matrix4x4 vm_m = new Matrix4x4();
    private Matrix4x4 pvm_m = new Matrix4x4();


    private float render_mat[] = new float[16];



    private Messenger log;

    /**
     * Constructor.
     *
     * @param mainInterface Pointer to MainInterface.
     */
    protected OpenGLEngine(MainInterface mainInterface) {
        this.mainInterface = mainInterface;
        this.toRender = new ArrayList<Trackable>();


    }

    /**
     * Called when a surface is created.
     *
     * @param gl     The unused context.
     * @param config Unused configuration options.
     */
    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        // GLES20.glEnable(GLES20.GL_CULL_FACE);
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);


        final float eyeX = 0.0f;
        final float eyeY = 0.0f;
        final float eyeZ = 1.5f;
        final float lookX = 0.0f;
        final float lookY = 0.0f;
        final float lookZ = -5.0f;
        // Set our up vector.
        final float upX = 0.0f;
        final float upY = 1.0f;
        final float upZ = 0.0f;

        // Set the view matrix => camera position
        Matrix.setLookAtM(mViewMatrix, 0, eyeX, eyeY, eyeZ, lookX, lookY, lookZ, upX, upY, upZ);

        // Create shader prog:
        createShaders();

        this.log = Messenger.getInstance();

        //tests
        ow = new ObjectWorld();
        createBasicAssets();
    }



    /**
     * Rendering of a single frame. Here we update and render the detected
     * trackable list.
     *
     * @param gl Unused context.
     */
    @Override
    public void onDrawFrame(GL10 gl) {
        if (MainInterface.DEBUG_FRAME_LOGGING)
            log.pushTimer(this, "opengl frame");

        if(default_cam != null) {
            //create the projection matrix...
            p_m.set_to_identity();
            p_m = p_m.perspective(default_cam.getFOV(), (float) (window_size_x) / (float) (window_size_y),
                    default_cam.getZNEAR(), default_cam.getZFAR());

            //retrieve the camera matrix
            v_m = default_cam.get_view_matrix();



            // Clear Buffers:
            GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);

            // ------------------ get new markerlist if needed --------
            if (mainInterface.getListUpdateStatus()) {
                this.toRender = mainInterface.getList();
                if (toRender == null) {
                    log.log(TAG, "Error getting list!");
                    toRender = new ArrayList<Trackable>();
                } else
                    log.debug(TAG, "Updated list – found " + this.toRender.size() + " " + "trackables.");
            }


            //cam->getPosition() - touch_to_space(0,0)*near_

            // ------------------------ RENDER ------------------------
            if (!toRender.isEmpty()) {
                for (Trackable trackable : toRender) {

                    //get the screen pos from the Trackable object
                    int x_pos = trackable.getX();
                    int y_pos = trackable.getY();


                    //convert it to space coords
                    Vector3 space_pos = touch_to_space(default_cam,x_pos,y_pos);

                    //scale the pos (move away from origin)
                    Vector3 space_pos_scaled = space_pos.multiply( default_cam.getZNEAR() + 6.0);
                    //move away from camera pos (the position we want to render our model at...)
                    Vector3 final_space_pos = default_cam.getPosition().subtract(space_pos_scaled);

                    Positation posi = default_co.getPositation();
                    posi.set_position(final_space_pos);

                    m_m = posi.get_model_matrix();
                    vm_m = v_m.multiply(m_m);
                    pvm_m = (p_m.multiply(v_m)).multiply(m_m);

                    // Reset model matrix to identity
                    // Matrix.setIdentityM(mModelMatrix, 0);
                    // Matrix.multiplyMM(mModelMatrix, 0, trackable.getTRANSLATION(), 0, mModelMatrix, 0);
                    if (default_mdl != null) {
                        drawModel(default_mdl);
                    }
                }
            }


            if (MainInterface.DEBUG_FRAME_LOGGING) {
                log.debug(TAG, "OpenGL rendered frame in " + log.popTimer(this).time + "ms.");
            }
        }
    }

    /**
     * Called whenever the draw surface is changed – most notably on creation
     * . Notably takes care of setting up the correct perspective
     * transformation based on the camera calibration values.
     *
     * @param gl     Unused context.
     * @param width  Width in pixel of canvas.
     * @param height Height in pixel of canvas.
     */
    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {

        window_size_x = width;
        window_size_y = height;

        GLES20.glViewport(0, 0, width, height);

        /* no need
        float f_x = mainInterface.camMatrix[0][0];
        float f_y = mainInterface.camMatrix[1][1];
        float c_x = mainInterface.camMatrix[0][2];
        float c_y = mainInterface.camMatrix[1][2];

        float aspectRatio = ((float) width / (float) height) * (f_y / f_x);
        // Last number was originally 2f, but is better with 2.xf
        float fovY = 1f / (f_x / (float) height * 2.6f);
        float near = 0.1f;
        float far = 1000f;
        float frustum_height = near * fovY;
        float frustum_width = frustum_height * aspectRatio;

        float offset_x = (((float) width / 2f) - c_x) / (float) width *
                frustum_width * 2f;
        float offset_y = (((float) height / 2f) - c_y) / (float) height *
                frustum_height * 2f;

        Matrix.frustumM(mProjectionMatrix, 0, -frustum_width - offset_x, frustum_width - offset_x,
                -frustum_height - offset_y, frustum_height - offset_y, near, far);
        */
    }




    private void drawModel(Model mdl){
        // Pass in the position information

        ArrayList<Mesh> mesh_list = mdl.get_meshs();

        for(Mesh mesh: mesh_list) {

            if(mesh.isLoaded()) {

                GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mesh.get_vertex_vbo());
                //actually if once enabled we dont need to enable again.... but safety first xD
                GLES20.glEnableVertexAttribArray(mPositionHandle);
                GLES20.glVertexAttribPointer(mPositionHandle, 3, GLES20.GL_FLOAT, false, 0, 0);

                GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mesh.get_texcoord_vbo());
                GLES20.glEnableVertexAttribArray(mTexCoordHandle);
                GLES20.glVertexAttribPointer(mTexCoordHandle, 3, GLES20.GL_FLOAT, false, 0, 0);

                GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mesh.get_normal_vbo());
                GLES20.glEnableVertexAttribArray(mNormalHandle);
                GLES20.glVertexAttribPointer(mNormalHandle, 3, GLES20.GL_FLOAT, false, 0, 0);

                // Set the active texture unit to texture unit 0.
                GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
                // Bind the texture to this unit.
                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mesh.get_material().get_diffuse_map_texture());
                // Tell the texture uniform sampler to use this texture in the shader by binding to texture unit 0.
                GLES20.glUniform1i(mTextureUniformHandle, 0);


                // This multiplies the view matrix by the model matrix, and stores the result in the MVP matrix
                // (which currently contains model * view).
                //Matrix.multiplyMM(mMVPMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);

                // This multiplies the modelview matrix by the projection matrix, and stores the result in the MVP matrix
                // (which now contains model * view * projection).
                //Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVPMatrix, 0);


                //TRANSPOSE
                for (int f = 0; f < 4; f++) {
                    for (int g = 0; g < 4; g++) {
                        render_mat[f * 4 + g] = (float) (pvm_m.get_value(f*4+g));
                    }
                }

                GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, render_mat, 0);
                // The (1+data.capacity() / 8) tells us how many vertices we need to
                // draw

                if(mesh.get_triangle_count() == 0){
                    return;
                }

                GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, mesh.get_triangle_count()*3);

            }
        }
    }





    private void createBasicAssets(){
        //create basic camera...
        Camera cam = new Camera();
        cam.set_position(0.0,0.0,0.0);
        cam.setZFAR(300.0);
        default_cam = cam;

        //load default 3D object
        Loader_obj loader = new Loader_obj();
        Model mdl = new Model();
        loader.load_model_data(mdl,"box.obj");
        mdl.loadGLdata();
        default_mdl = mdl;

        //pack it with Positation into a CompositeObject
        Positation posi = new Positation();
        CompositeObject co = new CompositeObject();
        co.setModel(default_mdl);
        co.setPositation(posi);
        default_co = co;

    }


    public Vector3 touch_to_space(Camera cam, int x,int y){
        Matrix4x4 M_projection = new Matrix4x4();
        M_projection = M_projection.perspective(cam.getFOV(),(float)(window_size_x) / (float)(window_size_y),cam.getZNEAR(),cam.getZFAR());

        Matrix4x4 camera_view_projection_m = M_projection.multiply(cam.get_view_matrix());

        Matrix4x4 inv_cam_view_projection = camera_view_projection_m.inverted();
        /*
        double touch_x = -((double)(x)/((double)(window_size_x)*0.5)-1.0);
        double touch_y = ((double)(y)/((double)(window_size_y)*0.5)-1.0);
        */
        double touch_x = -((double)(x)/((double)(window_size_x)*0.5)-1.0);
        double touch_y = ((double)(y)/((double)(window_size_y)*0.5)-1.0);

        /*
        Log.e(TAG, Double.toString(touch_x) + "  " +
                Double.toString(touch_y));
        */
        Vector3 projected_pos_near = inv_cam_view_projection.multiply(new Vector3( cam.getZNEAR() * touch_x, cam.getZNEAR() * touch_y, cam.getZNEAR()) );
        Vector3 projected_pos_far  = inv_cam_view_projection.multiply(new Vector3( cam.getZFAR() * touch_x, cam.getZFAR() * touch_y, cam.getZFAR()) );
        Vector3 projected_pos = projected_pos_far.subtract(projected_pos_near);
        Vector3 projected_pos_normalized = projected_pos.normalized();
        projected_pos_normalized.set_x(-projected_pos_normalized.x());
        projected_pos_normalized.set_y(-projected_pos_normalized.y());
        return projected_pos_normalized;
    }


    /**
     * Method for creating the most basic of shaders.
     */
    private void createShaders() {
        /*
        final String vertexShader =
                "uniform mat4 u_MVPMatrix;      \n"
                        + "attribute vec4 a_Position;     \n"
                        + "attribute vec4 a_Color;        \n"
                        + "varying vec4 v_Color;          \n"
                        + "void main()                    \n"
                        + "{                              \n"
                        + "   v_Color = a_Color;          \n"
                        + "   gl_Position = u_MVPMatrix   \n"
                        + "               * a_Position;   \n"
                        + "}                              \n";

        final String fragmentShader =
                "precision mediump float;       \n"
                        + "varying vec4 v_Color;          \n"
                        + "void main()                    \n"
                        + "{                              \n"
                        + "   gl_FragColor = vec4(0.9,0.1,0.1,1.0);     \n"
                        + "}                              \n";

        // Load in the vertex shader.
        int vertexShaderHandle = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER);

        if (vertexShaderHandle != 0) {
            // Pass in the shader source.
            GLES20.glShaderSource(vertexShaderHandle, vertexShader);

            // Compile the shader.
            GLES20.glCompileShader(vertexShaderHandle);

            // Get the compilation status.
            final int[] compileStatus = new int[1];
            GLES20.glGetShaderiv(vertexShaderHandle, GLES20.GL_COMPILE_STATUS, compileStatus, 0);

            // If the compilation failed, delete the shader.
            if (compileStatus[0] == 0) {
                GLES20.glDeleteShader(vertexShaderHandle);
                vertexShaderHandle = 0;
            }
        }

        if (vertexShaderHandle == 0) {
            throw new RuntimeException("Error creating vertex shader.");
        }

        // Load in the fragment shader shader.
        int fragmentShaderHandle = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER);

        if (fragmentShaderHandle != 0) {
            // Pass in the shader source.
            GLES20.glShaderSource(fragmentShaderHandle, fragmentShader);

            // Compile the shader.
            GLES20.glCompileShader(fragmentShaderHandle);

            // Get the compilation status.
            final int[] compileStatus = new int[1];
            GLES20.glGetShaderiv(fragmentShaderHandle, GLES20.GL_COMPILE_STATUS, compileStatus, 0);

            // If the compilation failed, delete the shader.
            if (compileStatus[0] == 0) {
                GLES20.glDeleteShader(fragmentShaderHandle);
                fragmentShaderHandle = 0;
            }
        }

        if (fragmentShaderHandle == 0) {
            throw new RuntimeException("Error creating fragment shader.");
        }
        */



        //load shader and create a program out of it...
        Shader vertex_shader = new Shader("shaders/simple.vsh",GLES20.GL_VERTEX_SHADER);
        if(!vertex_shader.isCreated()){
            Log.e(TAG, vertex_shader.getError());
        }
        Shader fragment_shader = new Shader("shaders/simple.fsh",GLES20.GL_FRAGMENT_SHADER);
        if(!fragment_shader.isCreated()){
            Log.e(TAG, fragment_shader.getError());
        }



        // Create a program object and store the handle to it.
        programHandle = GLES20.glCreateProgram();

        if (programHandle != 0) {
            // Bind the vertex shader to the program.
            GLES20.glAttachShader(programHandle, vertex_shader.getShaderId());

            // Bind the fragment shader to the program.
            GLES20.glAttachShader(programHandle, fragment_shader.getShaderId());

            // Bind attributes
            GLES20.glBindAttribLocation(programHandle, 0, "a_Position");
            GLES20.glBindAttribLocation(programHandle, 1, "a_TexCoord");
            GLES20.glBindAttribLocation(programHandle, 2, "a_Normal");

            // Link the two shaders together into a program.
            GLES20.glLinkProgram(programHandle);

            // Get the link status.
            final int[] linkStatus = new int[1];
            GLES20.glGetProgramiv(programHandle, GLES20.GL_LINK_STATUS, linkStatus, 0);

            // If the link failed, delete the program.
            if (linkStatus[0] == 0) {
                GLES20.glDeleteProgram(programHandle);
                programHandle = 0;
            }
        }

        if (programHandle == 0) {
            throw new RuntimeException("Error creating program.");
        }

        // Set program handles. These will later be used to pass in values to the program.
        mMVPMatrixHandle = GLES20.glGetUniformLocation(programHandle, "u_MVPMatrix");
        mPositionHandle = GLES20.glGetAttribLocation(programHandle, "a_Position");
        mTexCoordHandle = GLES20.glGetAttribLocation(programHandle, "a_TexCoord");
        mNormalHandle = GLES20.glGetAttribLocation(programHandle, "a_Normal");

        mTextureUniformHandle = GLES20.glGetUniformLocation(programHandle, "tex_sampler");


        // Tell OpenGL to use this program when rendering.
        GLES20.glUseProgram(programHandle);
    }
}