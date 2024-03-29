package com.aj.processor.app;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.aj.processor.app.XML.Process.Components.Node;
import com.aj.processor.app.XML.Process.PComponent;

import org.opencv.android.JavaCameraView;
import org.opencv.core.Point;




import java.util.ArrayList;
import java.util.Random;

import eu.imagine.framework.Detector;
import eu.imagine.framework.Entity;
import eu.imagine.framework.Flags;
import eu.imagine.framework.HomographyListener;
import eu.imagine.framework.Marker;
import eu.imagine.framework.MarkerPatternHelper;
import eu.imagine.framework.Messenger;
import eu.imagine.framework.OpenCVInterface;
import eu.imagine.framework.Trackable;

/**
 * Created with IntelliJ IDEA.
 * User: tamino
 * Date: 9/15/13
 * Time: 2:02 PM
 */
public class MainInterface {

    private static MainInterface instance;

    public final static int MAININTERFACE_STATE_NODEUPDATE = 0x0001;
    public final static int MAININTERFACE_STATE_NODEHIDE = 0x0002;



    public static MainInterface getInstance(){
        return instance;
    }



    // Allow debug logging:
    public static boolean DEBUG_LOGGING = false;
    public static boolean DEBUG_FRAME_LOGGING = false;
    public boolean ONLY_HOMOGRAPHY = false;
    public boolean ALLOW_DUPLICATE_MARKERS = false;
    public boolean RUN_OPENCV = true;
    public boolean RUN_RENDERER = true;
    public boolean DEBUG_FRAME = false;

    // Linking variables to sub classes:
    private Messenger log;
    private final String TAG = "MainInterface";
    private OpenCVInterface opencv;
    private RenderInterface render;
    private OpenGLSurfaceView renderView;

    private NodeView nodeView;

    //handler to retreive data/objects from the render thread (GLThread)
    private Handler glThreadHandler;


    private Activity mainActivity;
    private final Object synLock = new Object();

    // Store markers per frame:
    private ArrayList<Entity> allTrackings;
    private boolean newTrackings = false;

    private ArrayList<Trackable> detectedTrackables;
    private boolean updatedData = false;

    // Store Homography listeners:
    private ArrayList<HomographyListener> listeners;

    // Values:
    public float[][] camMatrix;
    public float[] distCoef;

    // Detector values
    public int threshold = 100;



    //is openCV running ?
    private boolean is_CV_running = true;

    public MainInterface(Activity mainActivity,
                         float[][] camMatrix,
                         float[] distortionCoefficients) {

        instance = this;

        this.log = Messenger.getInstance();
        log.log(TAG, "Constructing framework.");
        this.mainActivity = mainActivity;
        this.listeners = new ArrayList<HomographyListener>();
        this.allTrackings = new ArrayList<Entity>();
        this.detectedTrackables = new ArrayList<Trackable>();
        // Set camera matrix:
        this.camMatrix = camMatrix;
        this.distCoef = distortionCoefficients;

        //attach handler object to main thread aka UIThread...
        glThreadHandler = new Handler(Looper.getMainLooper()) {
            /*
             * handleMessage() defines the operations to perform when
             * the Handler receives a new Message to process.
             */
            @Override
            public void handleMessage(Message inputMessage){
                //receive a message and process it...

                // Gets the task from the incoming Message object.
                NodeTask nodeTask = (NodeTask) inputMessage.obj;


                switch (inputMessage.what) {
                    // The decoding is done
                    case MAININTERFACE_STATE_NODEUPDATE:
                        //get the PComponent and retrieve the data and set it onto the ui...
                        PComponent pc = nodeTask.getDataNode();
                        if(pc != null) {
                            if (pc.hasNode()) {
                                Node n = pc.getNode();
                                if (n != null) {
                                    if (n.hasName()) {
                                        String name = n.getName();
                                        if (name != null) {
                                            nodeView.setNodeName(name);
                                        }
                                    }
                                    if (n.hasID()) {
                                        String id = n.getID();
                                        if (id != null) {
                                            nodeView.setNodeID(id);
                                        }
                                    }
                                    if (n.hasDescription()) {
                                        String desc = n.getDescription();
                                        if (desc != null) {
                                            nodeView.setDescription(desc);
                                        }
                                    }
                                    if (n.hasStaffAssignmentRule()) {
                                        String staff = n.getStaffAssignmentRule();
                                        if (staff != null) {
                                            nodeView.setStaff(staff);
                                        }
                                    }

                                }
                            }
                        }
                        nodeView.setAlpha(1.0f);
                        break;

                    case MAININTERFACE_STATE_NODEHIDE:
                        nodeView.setAlpha(0.0f);
                        break;
                    default:
                        super.handleMessage(inputMessage);
                }

            }
        };
    }



    public void onCreate(ViewGroup groupView) {
        log.pushTimer(this, "start");
        // Create OpenCV part:
        if (RUN_OPENCV) {
            opencv = new OpenCVInterface(this, this.mainActivity);
            JavaCameraView cameraView = new JavaCameraView(mainActivity,
                    JavaCameraView.CAMERA_ID_ANY);
            cameraView.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup
                    .LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            cameraView.enableFpsMeter();
            cameraView.setVisibility(JavaCameraView.GONE);
            groupView.addView(cameraView);
            opencv.onCreate(cameraView);
            is_CV_running = true;
        }
        // Create OpenGL render part:
        if (RUN_RENDERER) {
            //render = new RenderInterface(this);
            //CHANGE
            //GLSurfaceView renderView = new GLSurfaceView(mainActivity.getApplicationContext());
            renderView = new OpenGLSurfaceView(mainActivity.getApplicationContext());
            renderView.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup
                    .LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            groupView.addView(renderView);
            //retrieve marker when needed...
            renderView.setMaininterFace(this);
            //render.onCreate(renderView);





            nodeView = new NodeView(mainActivity.getApplicationContext());
            nodeView.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup
                    .LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            groupView.addView(nodeView);

            nodeView.setAlpha(0.0f);


            renderView.setNodeView(nodeView);

        }
        // Set layout things:
        mainActivity.getWindow().addFlags(WindowManager.LayoutParams
                .FLAG_KEEP_SCREEN_ON);
        log.log(TAG, "Framework created in " + log.popTimer(this).time
                + "ms.");
    }


    //checking syncronization... might fix LibC crash...
    public void toggleOpenCV(){
        Debugger.error(TAG,"MainInterface:toggleOpenCV()");
        if (is_CV_running) {
            Debugger.error(TAG,"MainInterface:toggleOpenCV() is_CV_running");
            opencv.onPause();
            is_CV_running = false;
        } else {
            Debugger.error(TAG,"MainInterface:toggleOpenCV() !is_CV_running");
            opencv.onResume(this.mainActivity);
            is_CV_running = true;
        }

        Debugger.error(TAG,"MainInterface:toggleOpenCV() done!");
    }

    public void onResume() {
        if (RUN_OPENCV) {
            opencv.onResume(this.mainActivity);
            is_CV_running = true;
        }
        if (RUN_RENDERER) {
            renderView.onResume();
        }
    }

    public void onPause() {
        if (RUN_OPENCV) {
            opencv.onPause();
            is_CV_running = false;
        }
        if (RUN_RENDERER) {
            renderView.onPause();
        }
    }

    public void onDestroy() {
        if (RUN_OPENCV) {
            opencv.onDestroy();
            is_CV_running = false;
        }
        log.log(TAG, "Stopping.");
    }

    /**
     * Method for modifying the binary threshold for Detector. Only use after
     * checking with DEBUG_PREP_FRAME if the binarization actually is the
     * error! Used to set all 3 binarization methods, including Canny!
     *
     * @param value The value to set it to.
     */
    @SuppressWarnings("UnusedDeclaration")
    public void setBinaryThreshold(int value) {
        if (value >= 0 && value <= 255) {
            log.debug(TAG, "Setting theshold to "+value);
            this.threshold = value;
        }
        else
            this.threshold = 100;
    }

    /**
     * Add a homogrpahy listener. NOTE: Will only be notified if the
     * ONLY_HOMOGRAPHY flag has been set!
     *
     * @param homographyListener The object to register.
     */
    @SuppressWarnings("UnusedDeclaration")
    public void registerListener(HomographyListener homographyListener) {
        this.listeners.add(homographyListener);
    }

    /**
     * Remove a homography listener.
     *
     * @param homographyListener The object to remove.
     */
    @SuppressWarnings("UnusedDeclaration")
    public void removeListener(HomographyListener homographyListener) {
        this.listeners.remove(homographyListener);
    }

    /**
     * Add an entity to be detected and rendered. NOTE: References are used,
     * meaning that changes outside of the framework WILL have effects here
     * too!
     *
     * @param entity The entity to register.
     */
    @SuppressWarnings("UnusedDeclaration")
    public void registerEntity(Entity entity) {
        this.allTrackings.add(entity);
        newTrackings = true;
    }

    /**
     * Remove an entity from the list.
     *
     * @param entity Entity to remove.
     */
    @SuppressWarnings("UnusedDeclaration")
    public void removeEntity(Entity entity) {
        this.allTrackings.remove(entity);
    }

    /**
     * Function to create the marker for a given ID.
     *
     * @param ID The ID to encode into the marker.
     * @return The complete pattern that resembles the marker,
     *         including coded ID, coded direction, and borders.
     */
    @SuppressWarnings("UnusedDeclaration")
    public boolean[][] getMarker(int ID) {
        return MarkerPatternHelper.createMarker(ID);
    }

    /**
     * Helper method for converting an .obj file to the correct float value.
     *
     * @param data  The string containing the obj file.
     * @param color The color to show. If null, each face will be colored
     *              randomly.
     * @param scale Value to scale object by.
     * @return The complete float representation of the object,
     *         ready to be converted to the FloatBuffer required.
     */
    public float[] importOBJ(String data, float[] color, float scale) {
        Random rand = new Random();
        boolean randColors = color == null;
        String[] lines = data.split("\n");
        ArrayList<float[]> vertices = new ArrayList<float[]>();
        ArrayList<int[]> faces = new ArrayList<int[]>();
        // Extract vertice and face info:
        for (String line : lines) {
            // If starts with v --> vertice
            if (line.charAt(0) == 'v') {
                // Split for spaces
                String[] floats = line.substring(1).trim().split(" ");
                float[] vert = new float[3];
                // get three coordinates
                for (int i = 0; i < vert.length; i++)
                    vert[i] = Float.valueOf(floats[i].trim());
                // save
                vertices.add(vert);
            } else if (line.charAt(0) == 'f') {
                String[] ints = line.substring(1).trim().split(" ");
                int[] face = new int[3];
                for (int i = 0; i < face.length; i++)
                    face[i] = Integer.valueOf(ints[i].trim());
                faces.add(face);
            }
        }
        // Set together float:
        float[] retData = new float[faces.size() * 21];
        // For each face
        for (int i = 0; i < faces.size(); i++) {
            // Put each vertice
            int[] face = faces.get(i);
            // Check color:
            if (randColors)
                color = new float[]{rand.nextFloat(), rand.nextFloat(),
                        rand.nextFloat(), 1f};
            for (int j = 0; j < face.length; j++) {
                float[] vertice = vertices.get(face[j] - 1);
                // apply scale
                retData[i * 21 + j * 7 + 0] = vertice[0] * scale;
                retData[i * 21 + j * 7 + 1] = vertice[1] * scale;
                retData[i * 21 + j * 7 + 2] = vertice[2] * scale;
                // write color data
                retData[i * 21 + j * 7 + 3] = color[0];
                retData[i * 21 + j * 7 + 4] = color[1];
                retData[i * 21 + j * 7 + 5] = color[2];
                retData[i * 21 + j * 7 + 6] = color[3];
            }
        }

        return retData;
    }

    /**
     * Helper function for setting flags.
     *
     * @param value The flag to set.
     * @param bool  The value to set that flag at.
     */
    @SuppressWarnings("UnusedDeclaration")
    public void setFlag(Flags value, boolean bool) {
        switch (value) {
            case ALLOW_DUPLICATE_MARKERS:
                this.ALLOW_DUPLICATE_MARKERS = bool;
                break;
            case ALLOW_UNCERTAIN_HAMMING:
                MarkerPatternHelper.hammingDeforce = bool;
                break;
            case ONLY_HOMOGRAPHY:
                this.RUN_RENDERER = !bool;
                this.ONLY_HOMOGRAPHY = bool;
                break;
            case RUN_OPENCV:
                this.RUN_OPENCV = bool;
                break;
            case RUN_RENDERER:
                this.RUN_RENDERER = bool;
                break;
            case DEBUG_FRAME_LOGGING:
                MainInterface.DEBUG_FRAME_LOGGING = bool;
                break;
            case DEBUG_LOGGING:
                MainInterface.DEBUG_LOGGING = bool;
                break;
            case DEBUG_FRAME:
                this.DEBUG_FRAME = bool;
                break;
            case USE_CANNY:
                Detector.USE_CANNY = bool;
                break;
            case USE_ADAPTIVE:
                Detector.USE_ADAPTIVE = bool;
                break;
            case DEBUG_PREP_FRAME:
                Detector.DEBUG_PREP_FRAME = bool;
                break;
            case DEBUG_CONTOURS:
                Detector.DEBUG_CONTOURS = bool;
                break;
            case DEBUG_POLY:
                Detector.DEBUG_POLY = bool;
                break;
            case DEBUG_DRAW_MARKERS:
                Detector.DEBUG_DRAW_MARKERS = bool;
                break;
            case DEBUG_DRAW_SAMPLING:
                Detector.DEBUG_DRAW_SAMPLING = bool;
                break;
            case DEBUG_DRAW_MARKER_ID:
                Detector.DEBUG_DRAW_MARKER_ID = bool;
                break;
            default:
                log.log(TAG, "Failed to set flag " + value + "!");
                break;
        }
    }

    /**
     * Method that receives candidate markers. Here, they are filtered for
     * the markers we want and partnered with the entities we'll show.
     *
     * @param markerCandidates The list containing all detected markers.
     */
    public void updateList(ArrayList<Marker> markerCandidates) {
        synchronized (synLock) {

            updatedData = true;
            if (ONLY_HOMOGRAPHY) {
                for (HomographyListener listener : listeners) {
                    listener.receiveHomographies(markerCandidates);
                }
            }
            // Else case means we'll be rendering it,
            // so we filter the detected markers against the entity pairs we
            // want:
            else {
                // Remove old list:
                detectedTrackables.clear();
                // Now go through all trackables:
                for (Entity tracking : allTrackings) {
                    // Check if we want to render it:
                    if (!tracking.getVisibility())
                        continue;

                    for (Marker mark : markerCandidates)
                        // Add to rendering:
                        if (mark.getID() == tracking.getID()) {

                            //calculate center of the marker in image coords...
                            Point cp = mark.getCenterCoordsOnImage();
                            int x_coord = 0;
                            int y_coord = 0;
                            if(cp!=null){
                                x_coord = (int) cp.x;
                                y_coord = (int) cp.y;
                            }


                            detectedTrackables.add(new Trackable(mark.getID(),
                                    mark.getTranslation(),
                                    x_coord,
                                    y_coord));
                            // Simply continue with next entity if we don't
                            // want multiple renders:
                            if (!ALLOW_DUPLICATE_MARKERS)
                                break;
                        }
                }
            }
        }
    }

    /**
     * Method for the RenderInterface to check if an updated list has been
     * posted.
     *
     * @return True when a new list has been posted, false else.
     */

    protected boolean getRecognizedTrackablesStatus() {
        synchronized (synLock) {
            return updatedData;
        }
    }

    /**
     * Method for retrieving the entities to render with the respective
     * information.
     *
     * @return List containing all markers for now.
     */
    protected ArrayList<Trackable> getRecognizedTrackables() {
        synchronized (synLock) {
            updatedData = false;
            //noinspection unchecked
            return (ArrayList<Trackable>) detectedTrackables.clone();
        }
    }



    //do we have new processes in the list ?
    protected boolean getAllTrackingsStatus() {
        synchronized (synLock) {
            return newTrackings;
        }
    }

    //NEW retrieve all trackable links between IDs and XMLs so we can preload them during
    //openGL start...
    protected ArrayList<Entity> getAllTrackings() {
        synchronized (synLock) {
            newTrackings = false;
            //noinspection unchecked
            return (ArrayList<Entity>) allTrackings.clone();
        }
    }





    // Handle status messages from tasks
    public void handleState(NodeTask nodeTask, int state) {
        switch (state) {
            case MAININTERFACE_STATE_NODEUPDATE:
                /*
                 * Creates a message for the Handler
                 * with the state and the task object
                 */
                Message completeMessage =
                        glThreadHandler.obtainMessage(state, nodeTask);
                completeMessage.sendToTarget();
                break;
            case MAININTERFACE_STATE_NODEHIDE:
                Message completeMessage2 =
                        glThreadHandler.obtainMessage(state, nodeTask);
                completeMessage2.sendToTarget();
                break;
        }
    }
}
