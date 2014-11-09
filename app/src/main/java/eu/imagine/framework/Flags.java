package eu.imagine.framework;

/**
 * Enum for flags that can be set.
 */
public enum Flags {
    /**
     * Enables OpenCV detection part of framework. DEFAULT: true.
     */
    RUN_OPENCV,
    /**
     * Enable OpenGL ES render part of framework. Note: set automatically if
     * only homography is required, does NOT need to be set additionally!
     * DEFAULT: true.
     */
    RUN_RENDERER,
    /**
     * Set the framework to only detect the markers and pass them to any
     * registered listeners. INFO: Disables any OpenGL ES rendering options!
     */
    ONLY_HOMOGRAPHY,
    /**
     * Allow duplicate markers to be detected and rendered.
     */
    ALLOW_DUPLICATE_MARKERS,
    /**
     * Allow markers to be detected where the Hamming
     * encoding is uncertain. Normally, these are discarded.
     */
    ALLOW_UNCERTAIN_HAMMING,
    /**
     * Enables linear pipeline for detection, allowing debug views to be
     * drawn to output. NOTE: This is REQUIRED for all debug flags that
     * affect the drawing of a single output frame! (DEBUG_PREP_FRAME,
     * DEBUG_CONTOURS, DEBUG_POLY, DEBUG_DRAW_MARKERS, DEBUG_DRAW_MARKER_ID,
     * DEBUG_DRAW_SAMPLING)
     */
    DEBUG_FRAME,
    /**
     * Enable timing information output on OpenCV and OpenGL frames.
     */
    DEBUG_FRAME_LOGGING,
    /**
     * Enable debug logging for all debug messages posted via Messenger.
     */
    DEBUG_LOGGING,
    /**
     * Use Canny edge detection to base contours off. NOTE: Currently not
     * working correctly due to double detection of markers.
     */
    USE_CANNY,
    /**
     * Use the adaptive thresholding for creating the initial binary image.
     * Slower but less sensitive to luminance variations.
     */
    USE_ADAPTIVE,
    /**
     * Show result of binarization. WARNING: No marker detection done!
     */
    DEBUG_PREP_FRAME,
    /**
     * Show result of contour detection. WARNING: No marker detection done!
     */
    DEBUG_CONTOURS,
    /**
     * Show all detected markers' border. Draws a red rectangle around the
     * location of the detected marker.
     */
    DEBUG_POLY,
    /**
     * Draws dewarped detected marker signatures in top left corner.
     */
    DEBUG_DRAW_MARKERS,
    /**
     * Draws corrected and detected marker ID in top left corner.
     */
    DEBUG_DRAW_MARKER_ID,
    /**
     * Show sampling detection results on top of drawn markers in top left
     * corner. INFO: To see results MUST be used with DEBUG_DRAW_MARKERS!
     */
    DEBUG_DRAW_SAMPLING
}
