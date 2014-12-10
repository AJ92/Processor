package com.aj.processor.app;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;


import com.aj.processor.app.XML.Process.Components.Node;
import com.aj.processor.app.XML.Process.Components.StructuralNodeData;
import com.aj.processor.app.XML.Process.PComponent;
import com.aj.processor.app.XML.XMLLoadTaskAsync;
import com.aj.processor.app.XML.XMLProcessLoadedListener;
import com.aj.processor.app.graphics.camera.Camera;
import com.aj.processor.app.graphics.model.Components.Mesh;
import com.aj.processor.app.graphics.model.Line;
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

import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;

import eu.imagine.framework.Messenger;
import eu.imagine.framework.Trackable;

import com.aj.processor.app.XML.Process.Process;

/**
 * Handles the rendering of the objects.
 */
public class OpenGLEngine implements GLSurfaceView.Renderer, XMLProcessLoadedListener {


    private final String TAG = "OpenGLEngine";

    private MainInterface mainInterface;
    private ArrayList<Trackable> toRender;

    private int programLine;
    private int programSimple;

    //simple
    private int locMVPMatrixSimple;
    private int locPositionSimple;
    private int locTexCoordSimple;
    private int locNormalSimple;
    private int locTextureSimple;

    //line
    private int locMVPMatrixLine;
    private int locPositionLine;
    private int locColorLine;

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


    private boolean process_loaded = false;
    private Process test_process;

    private Camera default_cam;


    private CompositeObject test_marker_co;
    private CompositeObject test_marker_text_co;

    private Matrix4x4 ortho_m = new Matrix4x4();


    private Line x_dir;
    private Line y_dir;
    private Line z_dir;

    private ArrayList<Line> lines = new ArrayList<Line>();

    private Positation gizmo_posi = new Positation();


    //all the matrices we need... pre init them to identity
    private Matrix4x4 p_m = new Matrix4x4();
    private Matrix4x4 v_m = new Matrix4x4();
    private Matrix4x4 m_m = new Matrix4x4();
    private Matrix4x4 vm_m = new Matrix4x4();
    private Matrix4x4 pv_m = new Matrix4x4();
    private Matrix4x4 pvm_m = new Matrix4x4();


    private float render_mat[] = new float[16];




    //input from surfaceView
    private float angle_x = 0.0f;
    private float angle_y = 0.0f;

    private float pos_x = 0.0f;
    private float pos_y = 0.0f;

    private float scale = 1.0f;




    private Messenger log;

    /**
     * Constructor.
     *
     * @param mainInterface Pointer to MainInterface.
     */

    public void setMainInterFace(MainInterface mainInterface){
        this.mainInterface = mainInterface;
    }

    protected OpenGLEngine() {
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


        //TODO:
        //JUST A TEST CHECK...
        if(process_loaded){
            if(ow != null) {
                loadProcessIntoObjectWorld(test_process);
                process_loaded = false;
            }
        }

        if(default_cam != null) {
            //create the projection matrix...
            p_m.set_to_identity();
            p_m = p_m.perspective(default_cam.getFOV(), (float) (window_size_x) / (float) (window_size_y),
                    default_cam.getZNEAR(), default_cam.getZFAR());


            //retrieve the camera matrix
            default_cam.clear_rotation_global();
            default_cam.clear_rotation_local();
            default_cam.add_rotation_local(angle_x,0.0,1.0,0.0);
            default_cam.add_rotation_local(angle_y,1.0,0.0,0.0);





            // Clear Buffers:
            GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);


            // ------------------ get new markerlist if needed --------
            if(mainInterface != null) {
                if (mainInterface.getListUpdateStatus()) {
                    this.toRender = mainInterface.getList();
                    if (toRender == null) {
                        log.log(TAG, "Error getting list!");
                        toRender = new ArrayList<Trackable>();
                    } else
                        log.debug(TAG, "Updated list – found " + this.toRender.size() + " " + "trackables.");
                }
            }




            // ------------------------ RENDER ------------------------


            //      ------------------- simple shader -----------------
            // render all objects in ObjectWorld ow!!!
            if (ow != null) {



                for (Trackable trackable : toRender) {

                    //get the screen pos from the Trackable object
                    int x_pos = trackable.getX();
                    int y_pos = trackable.getY();

                    Vector3 space_pos = space_pos = touch_to_space(default_cam, x_pos, y_pos);


                    //scale the pos (move away from origin)
                    Vector3 space_pos_scaled = space_pos.multiply(default_cam.getZNEAR() + (3.0 * scale));
                    //move away from camera pos (the position we want to render our model at...)
                    Vector3 final_space_pos = default_cam.getPosition().add(space_pos_scaled);

                    default_cam.set_position(default_cam.getPosition()
                            .subtract(final_space_pos)
                            .add(new Vector3(-this.pos_x / 100.0, this.pos_y / 100.0, 0.0)));


                    v_m = default_cam.get_view_matrix();
                    pv_m = p_m.multiply(v_m);

                    if(ow.getStoreMode() == ObjectWorld.store_mode_simple) {




                        //MODELS
                        GLES20.glEnableVertexAttribArray(locPositionSimple);
                        GLES20.glEnableVertexAttribArray(locTexCoordSimple);
                        GLES20.glEnableVertexAttribArray(locNormalSimple);
                        for (CompositeObject co : ow.getCompositeObjectsModels()) {

                            if (co.hasModel()) {

                                Positation posi = co.getPositation();

                                m_m = posi.get_model_matrix();
                                //vm_m = v_m.multiply(m_m);
                                pvm_m = pv_m.multiply(m_m);


                                drawModel(co.getModel());
                            }
                        }
                        GLES20.glDisableVertexAttribArray(locPositionSimple);
                        GLES20.glDisableVertexAttribArray(locTexCoordSimple);
                        GLES20.glDisableVertexAttribArray(locNormalSimple);
                        //MODELS DONE


                        //LINES
                        GLES20.glEnableVertexAttribArray(locPositionLine);
                        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
                        for (CompositeObject co : ow.getCompositeObjectsModels()) {
                            if (co.hasLine()) {
                                Positation posi = co.getPositation();
                                pvm_m = pv_m.multiply(posi.get_model_matrix());
                                drawLine(co.getLine());
                            }
                        }
                        GLES20.glDisableVertexAttribArray(locPositionLine);
                        //LINES DONE
                    }

                }



            }




            //      ------------------- line shader only for gizmo atm-----------------

            GLES20.glEnableVertexAttribArray(locPositionLine);
            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);

            //render gizmo

            //convert it to space coords
            Vector3 space_pos = touch_to_space(default_cam, 100, window_size_y - 100);
            //scale the pos (move away from origin)
            Vector3 space_pos_scaled = space_pos.multiply( default_cam.getZNEAR() + 6.0);
            //move away from camera pos (the position we want to render our model at...)
            Vector3 final_space_pos = default_cam.getPosition().add(space_pos_scaled);

            gizmo_posi.set_position(final_space_pos);


            v_m = default_cam.get_view_matrix();
            pv_m = p_m.multiply(v_m);

            pvm_m = pv_m.multiply(gizmo_posi.get_model_matrix());

            //gizmo
            drawLine(x_dir);
            drawLine(y_dir);
            drawLine(z_dir);

            GLES20.glDisableVertexAttribArray(locPositionLine);




            if (MainInterface.DEBUG_FRAME_LOGGING) {
                log.debug(TAG, "OpenGL rendered frame in " + log.popTimer(this).time + "ms.");
            }
        }
    }

    /**
     * Called whenever the draw surface is changed – most notably on creation
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
        GLES20.glUseProgram(programSimple);

        // Pass in the position information

        ArrayList<Mesh> mesh_list = mdl.get_meshs();

        for(Mesh mesh: mesh_list) {

            if(mesh.isLoaded()) {

                GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mesh.get_vertex_vbo());
                //actually if once enabled we dont need to enable again.... but safety first xD
                //GLES20.glEnableVertexAttribArray(locPositionSimple);
                GLES20.glVertexAttribPointer(locPositionSimple, 3, GLES20.GL_FLOAT, false, 0, 0);

                GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mesh.get_texcoord_vbo());
                //GLES20.glEnableVertexAttribArray(locTexCoordSimple);
                GLES20.glVertexAttribPointer(locTexCoordSimple, 3, GLES20.GL_FLOAT, false, 0, 0);

                GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mesh.get_normal_vbo());
                //GLES20.glEnableVertexAttribArray(locNormalSimple);
                GLES20.glVertexAttribPointer(locNormalSimple, 3, GLES20.GL_FLOAT, false, 0, 0);

                // Set the active texture unit to texture unit 0.
                GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
                // Bind the texture to this unit.
                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mesh.get_material().get_diffuse_map_texture());
                // Tell the texture uniform sampler to use this texture in the shader by binding to texture unit 0.
                GLES20.glUniform1i(locTextureSimple, 0);


                // This multiplies the view matrix by the model matrix, and stores the result in the MVP matrix
                // (which currently contains model * view).
                //Matrix.multiplyMM(mMVPMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);

                // This multiplies the modelview matrix by the projection matrix, and stores the result in the MVP matrix
                // (which now contains model * view * projection).
                //Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVPMatrix, 0);


                //TRANSPOSE

                render_mat = pvm_m.getFloatArray(true);


                GLES20.glUniformMatrix4fv(locMVPMatrixSimple, 1, false, render_mat, 0);
                // The (1+data.capacity() / 8) tells us how many vertices we need to
                // draw

                if(mesh.get_triangle_count() == 0){
                    return;
                }

                GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, mesh.get_triangle_count()*3);
            }
        }
    }


    private void drawLine(Line line){

        GLES20.glUseProgram(programLine);

        // Enable a handle to the triangle vertices
        //GLES20.glEnableVertexAttribArray(locPositionLine);

        // Prepare the triangle coordinate data
        GLES20.glVertexAttribPointer(locPositionLine, line.getCoordsPerVertex(),
                GLES20.GL_FLOAT, false,
                line.getVertexStride(), line.getVertexBuffer());

        // Set color for drawing the triangle
        GLES20.glUniform4fv(locColorLine, 1, line.getColor(), 0);

        render_mat = pvm_m.getFloatArray(true);


        GLES20.glUniformMatrix4fv(locMVPMatrixLine, 1, false, render_mat, 0);


        GLES20.glLineWidth(5f);

        // Draw the triangle
        GLES20.glDrawArrays(GLES20.GL_LINES, 0, line.getVertexCount());
    }


    @Override
    public void onXMLProcessLoaded(Process p) {
        test_process = p;
        process_loaded = true;
    }

    private void loadProcessIntoObjectWorld(Process p){
        //create the objects we need and connect them with lines and so on...

    }

    private void createBasicAssets(){

        //create basic camera...
        Camera cam = new Camera();
        cam.set_position(0.0,0.0,0.0);
        cam.setZFAR(300.0);
        default_cam = cam;


        //creates it's own ModelLoader
        //simple storage mode is a linear unsorted list of models!!!
        ow = new ObjectWorld(ObjectWorld.store_mode_simple);


        //TEST XML PARSER (after ow construction... cause we need ow...)
        XMLLoadTaskAsync xmllta = new XMLLoadTaskAsync();
        // add the renderer as listener so once xml and the process is ready we can
        //construct a 3D representation of it
        xmllta.addXMLProcessLoadedListener(this);
        //xmllta.retreiveXMLFromAssets("test.xml");
        xmllta.retreiveXMLFromAssets("frage20l.xml");



        for(int y = 0; y < 10; y++) {
            for(int x = 0; x < 10; x++) {
                test_marker_co = ow.loadModelObject("box", "box.obj", true);
                test_marker_co.getPositation().set_position(x * 4.0,- y * 4.0, 0.0);

                test_marker_text_co = ow.loadModelObject("test_text", "text_model.obj", false);
                //crazy chain...
                test_marker_text_co.getModel().get_meshs().get(0).get_material().setDiffuseText(
                        "FF00FF_TEXT_BG.png", "Node", 100.0f, 0, 255, 0
                );

                //move to the right
                test_marker_text_co.getPositation().set_position(0.5f + x * 4.0, 0.0f - y * 4.0, 1.02f);

                //shadow
                CompositeObject test_marker_text_co2 = ow.loadModelObject("test_text", "text_model.obj", false);
                //crazy chain...
                test_marker_text_co2.getModel().get_meshs().get(0).get_material().setDiffuseText(
                        "FF00FF_TEXT_BG.png", "Node", 100.0f, 0, 0, 0
                );

                //move to the right
                test_marker_text_co2.getPositation().set_position(0.51f + x * 4.0, 0.01f - y * 4.0, 1.01f);
            }
        }


        //gizmo
        x_dir = new Line(
                0.0f,0.0f,0.0f,
                0.5f,0.0f,0.0f,
                1.0f,0.0f,0.0f,1.0f
        );

        y_dir = new Line(
                0.0f,0.0f,0.0f,
                0.0f,0.5f,0.0f,
                0.0f,1.0f,0.0f,1.0f
        );

        z_dir = new Line(
                0.0f,0.0f,0.0f,
                0.0f,0.0f,0.5f,
                0.0f,0.0f,1.0f,1.0f
        );


        Log.e(TAG,"assets LOADED !!!");

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
        double touch_x = ((double)(x)/((double)(window_size_x)*0.5)-1.0);
        double touch_y = -((double)(y)/((double)(window_size_y)*0.5)-1.0);

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
        projected_pos_normalized.set_z(-projected_pos_normalized.z());
        return projected_pos_normalized;
    }

    public void setAngleX(float angle_x){
        this.angle_x = angle_x;
    }

    public void setAngleY(float angle_y){
        this.angle_y = angle_y;
    }

    public void setPosX(float pos_x){
        this.pos_x = pos_x;
    }

    public void setPosY(float pos_y){
        this.pos_y = pos_y;
    }

    public void setScale(float scale){
        this.scale = scale;
    }

    public float getAngleX(){
        return this.angle_x;
    }

    public float getAngleY(){
        return this.angle_y;
    }

    public float getPosX(){
        return this.pos_x;
    }

    public float getPosY(){
        return this.pos_y;
    }

    public float getScale() {
        return this.scale;
    }


    /**
     * Method for creating the shader programs we need to render our geometry...
     * No magic involved
     */
    private void createShaders() {

        //---------------LINE SHADER-------------------------
        //load shader and create a program out of it...
        Shader vertex_shader_line = new Shader("shaders/line.vsh",GLES20.GL_VERTEX_SHADER);
        if(!vertex_shader_line.isCreated()){
            Log.e(TAG, vertex_shader_line.getError());
        }
        Shader fragment_shader_line = new Shader("shaders/line.fsh",GLES20.GL_FRAGMENT_SHADER);
        if(!fragment_shader_line.isCreated()){
            Log.e(TAG, fragment_shader_line.getError());
        }



        // Create a program object and store the handle to it.
        programLine = GLES20.glCreateProgram();

        if (programLine != 0) {
            // Bind the vertex shader to the program.
            GLES20.glAttachShader(programLine, vertex_shader_line.getShaderId());

            // Bind the fragment shader to the program.
            GLES20.glAttachShader(programLine, fragment_shader_line.getShaderId());

            // Bind attribute
            GLES20.glBindAttribLocation(programLine, 0, "a_Position");

            // Link the two shaders together into a program.
            GLES20.glLinkProgram(programLine);

            // Get the link status.
            final int[] linkStatus = new int[1];
            GLES20.glGetProgramiv(programLine, GLES20.GL_LINK_STATUS, linkStatus, 0);

            // If the link failed, delete the program.
            if (linkStatus[0] == 0) {
                GLES20.glDeleteProgram(programLine);
                programLine = 0;
            }
        }

        if (programLine == 0) {
            throw new RuntimeException("Error creating simple shader program.");
        }

        // Set program handles. These will later be used to pass in values to the program.
        locMVPMatrixLine = GLES20.glGetUniformLocation(programLine, "u_MVPMatrix");
        locPositionLine = GLES20.glGetAttribLocation(programLine, "a_Position");



        locColorLine = GLES20.glGetUniformLocation(programLine, "u_Color");

        // Tell OpenGL to use this program when rendering.
        GLES20.glUseProgram(programLine);










        //---------------SIMPLE SHADER-----------------------


        //load shader and create a program out of it...
        Shader vertex_shader_simple = new Shader("shaders/simple.vsh",GLES20.GL_VERTEX_SHADER);
        if(!vertex_shader_simple.isCreated()){
            Log.e(TAG, vertex_shader_simple.getError());
        }
        Shader fragment_shader_simple = new Shader("shaders/simple.fsh",GLES20.GL_FRAGMENT_SHADER);
        if(!fragment_shader_simple.isCreated()){
            Log.e(TAG, fragment_shader_simple.getError());
        }



        // Create a program object and store the handle to it.
        programSimple = GLES20.glCreateProgram();

        if (programSimple != 0) {
            // Bind the vertex shader to the program.
            GLES20.glAttachShader(programSimple, vertex_shader_simple.getShaderId());

            // Bind the fragment shader to the program.
            GLES20.glAttachShader(programSimple, fragment_shader_simple.getShaderId());

            // Bind attributes
            GLES20.glBindAttribLocation(programSimple, 0, "a_Position");
            GLES20.glBindAttribLocation(programSimple, 1, "a_TexCoord");
            GLES20.glBindAttribLocation(programSimple, 2, "a_Normal");

            // Link the two shaders together into a program.
            GLES20.glLinkProgram(programSimple);

            // Get the link status.
            final int[] linkStatus = new int[1];
            GLES20.glGetProgramiv(programSimple, GLES20.GL_LINK_STATUS, linkStatus, 0);

            // If the link failed, delete the program.
            if (linkStatus[0] == 0) {
                GLES20.glDeleteProgram(programSimple);
                programSimple = 0;
            }
        }

        if (programSimple == 0) {
            throw new RuntimeException("Error creating simple shader program.");
        }

        // Set program handles. These will later be used to pass in values to the program.
        locMVPMatrixSimple = GLES20.glGetUniformLocation(programSimple, "u_MVPMatrix");
        locPositionSimple = GLES20.glGetAttribLocation(programSimple, "a_Position");
        locTexCoordSimple = GLES20.glGetAttribLocation(programSimple, "a_TexCoord");
        locNormalSimple = GLES20.glGetAttribLocation(programSimple, "a_Normal");

        locTextureSimple = GLES20.glGetUniformLocation(programSimple, "tex_sampler");


        // Tell OpenGL to use this program when rendering.
        GLES20.glUseProgram(programSimple);
    }

}
