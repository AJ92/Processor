package eu.imagine.framework;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;

/**
 * Created with IntelliJ IDEA.
 * User: tamino
 * Date: 9/23/13
 * Time: 2:20 PM
 */
class ConvertHelper {

    private static ConvertHelper INSTANCE;
    private Messenger log;
    private final String TAG = "Converter";

    protected static ConvertHelper getInstance() {
        if (INSTANCE == null)
            INSTANCE = new ConvertHelper();
        return INSTANCE;
    }

    private ConvertHelper() {
        log = Messenger.getInstance();
    }

    /**
     * @param array
     * @return
     */
    protected Mat float2ToMatFloat(final float[][] array) {
        Mat retMat = new Mat(array.length, array[0].length, CvType.CV_32F);
        for (int row = 0; row < array.length; row++)
            for (int column = 0; column < array[row].length; column++) {
                retMat.put(row, column, array[row][column]);
            }
        return retMat;
    }

    /**
     * @param array
     * @return
     */
    protected MatOfDouble float1ToMatDouble(final float[] array) {
        MatOfDouble retMat = new MatOfDouble();
        for (int i = 0; i < array.length; i++)
            retMat.put(0, i, array[i]);
        return retMat;
    }

    /**
     * @param mat
     * @return
     */
    protected float[][] matFloatToFloat2(Mat mat) {
        int height = (int) mat.size().height;
        int width = (int) mat.size().width;
        float[][] retFloat = new float[height][width];
        for (int row = 0; row < height; row++)
            for (int column = 0; column < width; column++) {
                retFloat[row][column] = (float) mat.get(row, column)[0];
            }
        return retFloat;
    }

    /**
     * @param mat
     * @return
     */
    protected float[] matToFloat1(Mat mat) {
        int width = (int) mat.size().width;
        int height = (int) mat.size().height;
        if (width > height && height == 1) {
            float[] retFloat = new float[width];
            for (int column = 0; column < width; column++)
                retFloat[column] = (float) mat.get(0, column)[0];
            return retFloat;
        } else if (height > width && width == 1) {
            float[] retFloat = new float[height];
            for (int row = 0; row < height; row++)
                retFloat[row] = (float) mat.get(row, 0)[0];
            return retFloat;
        }
        log.debug(TAG, "matToFloat1: mat not one dimensional! Returning " +
                "NULL!");
        return null;
    }
}
