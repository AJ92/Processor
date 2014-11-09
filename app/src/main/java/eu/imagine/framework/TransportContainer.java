package eu.imagine.framework;

import org.opencv.core.Mat;

/**
 * Simple data class for passing both the rgba and gray mat to the worker
 * threads.
 */
class TransportContainer {

    protected Mat gray, rgba;

    protected TransportContainer(Mat gray, Mat rgba) {
        this.gray = gray;
        this.rgba = rgba;
    }
}
