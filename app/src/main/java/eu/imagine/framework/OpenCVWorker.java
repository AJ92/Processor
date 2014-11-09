package eu.imagine.framework;

import com.aj.processor.app.MainInterface;

/**
 * A single thread for detecting markers.
 */
@SuppressWarnings("FieldCanBeLocal")
class OpenCVWorker extends Thread {

    private TransportContainer in;
    private OpenCVInterface cvInterface;
    private Detector det;

    protected OpenCVWorker(OpenCVInterface cvInterface,
                           MainInterface mainInterface) {
        super();
        super.setPriority(Thread.MAX_PRIORITY);
        this.det = new Detector(mainInterface);
        this.cvInterface = cvInterface;
    }

    @Override
    public void run() {
        while (!this.isInterrupted()) {
            in = cvInterface.getTransport();
            if (in == null)
                continue;
            // Call detect in Detector
            det.detect(in.gray, in.rgba);
        }
    }
}
