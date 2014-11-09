package eu.imagine.framework;

import android.app.Activity;
import android.view.SurfaceView;
import android.view.View;

import com.aj.processor.app.MainInterface;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;

import java.util.concurrent.LinkedBlockingQueue;

/**
 * This is the interface which handles the management of the OpenCV
 * connection to the library on Android and the OpenCVWorkers.
 */
public class OpenCVInterface implements CameraBridgeViewBase
        .CvCameraViewListener2 {

    private MainInterface mainInterface;
    // OpenCV Android stuff:
    private CameraBridgeViewBase mOpenCvCameraView;

    // Vars that we need:
    private Messenger log;
    private final String TAG = "OpenCVInterface";
    protected LinkedBlockingQueue<TransportContainer> workerFeeder;
    private Detector det;
    private OpenCVWorker[] workers;
    @SuppressWarnings("FieldCanBeLocal")
    private final int PARALLEL_COUNT = 4;

    private BaseLoaderCallback mLoaderCallback;

    public OpenCVInterface(MainInterface mainInterface, Activity mainActivity) {
        this.mainInterface = mainInterface;
        mLoaderCallback = new BaseLoaderCallback(mainActivity) {
            @Override
            public void onManagerConnected(int status) {
                switch (status) {
                    case LoaderCallbackInterface.SUCCESS: {
                        mOpenCvCameraView.enableView();
                    }
                    break;
                    default: {
                        super.onManagerConnected(status);
                    }
                    break;
                }
            }
        };
    }

    /**
     * Starts the camera view. Notably also creates the worker threads.
     *
     * @param width  -  the width of the frames that will be delivered
     * @param height - the height of the frames that will be delivered
     */
    @Override
    public void onCameraViewStarted(int width, int height) {
        det = new Detector(mainInterface);
        workerFeeder = new LinkedBlockingQueue<TransportContainer>(1);

        workers = new OpenCVWorker[PARALLEL_COUNT];

        for (int i = 0; i < workers.length; i++) {
            workers[i] = new OpenCVWorker(this, mainInterface);
            workers[i].start();
        }
    }

    @Override
    public void onCameraViewStopped() {
        for (OpenCVWorker worker : workers) worker.interrupt();
    }

    /**
     * This is where the frame is processed by OpenCV.
     *
     * @param inputFrame The frame containing the rgba and gray mat.
     * @return The frame to show.
     */
    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        // If debug, do everything in line:
        if (mainInterface.DEBUG_FRAME) {
            return det.detect(inputFrame.gray(), inputFrame.rgba());
        }
        // Else put the task on multiple threads:
        else {
            try {
                workerFeeder.put(new TransportContainer(inputFrame.gray().clone(),
                        inputFrame.rgba().clone()));
            } catch (InterruptedException e) {
                log.log(TAG, "Error feeding!");
                return inputFrame.gray();
            }
            return inputFrame.rgba();
        }
    }

    public void onCreate(View cameraView) {
        this.log = Messenger.getInstance();
        mOpenCvCameraView = (CameraBridgeViewBase) cameraView;
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);
        log.log(TAG, "Created.");
    }

    public void onResume(Activity mainActivity) {
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_6, mainActivity,
                mLoaderCallback);
    }

    public void onPause() {
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    public void onDestroy() {
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    /**
     * Method for getting one TransportContainer to work on for the threads.
     *
     * @return The container with the mat data.
     */
    protected TransportContainer getTransport() {
        try {
            return workerFeeder.take();
        } catch (InterruptedException e) {
            log.log(TAG, "Error taking frame for worker!");
            return null;
        }
    }
}
