package com.aj.processor.app;
import android.app.Activity;
import android.os.Bundle;
import android.view.ViewGroup;

import eu.imagine.framework.Flags;

import java.util.ArrayList;

public class ImagineActivity extends Activity {

    /**
     * Stores instance of framework for access.
     */
    private MainInterface framework;

    /*
    Orig:
    private float[][] cameraMatrix = new float[][]{
            new float[]{1251f, 0f, 639.5f},
            new float[]{0f, 1251f, 359.5f},
            new float[]{0f, 0f, 1f}
    };
    private float[] distortionCoefficients = new float[]{
            0.2610701252267455f, -2.229801972443634f, 0f, 0f, 4.354745457073879f
    };
    */

    // Camera matrix (here determined ahead of time)
    private float[][] cameraMatrix = new float[][]{
            new float[]{1280, 0, 640},
            new float[]{0, 1280, 360},
            new float[]{0, 0, 1}
    };
    // Distortion coefficients:
    private float[] distortionCoefficients = new float[]{
            0.2785042226314545f, -2.410807609558105f, 0, 0, 4.748225688934326f
    };

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.imagine);

        // Construct framework. This includes passing a reference to the
        // activity (here this), the viewgroup where it'll construct its
        // views, and the camera and distortioncoefficients.
        framework = new MainInterface(this, cameraMatrix, distortionCoefficients);

        // Import model
        //we dont need to import anything here...
        //float[] conv = framework.importOBJ(house, null, 0.75f);

        // Get and parse options to set
        Bundle options = getIntent().getExtras();
        if (options != null) {
            // Logging options
            framework.setFlag(Flags.DEBUG_LOGGING,
                    options.getBoolean("debugLog", false));
            framework.setFlag(Flags.DEBUG_FRAME_LOGGING,
                    options.getBoolean("frameDebug", false));

            // Detection settings
            framework.setFlag(Flags.ALLOW_DUPLICATE_MARKERS,
                    options.getBoolean("dupMarkers", false));
            framework.setFlag(Flags.ALLOW_UNCERTAIN_HAMMING,
                    options.getBoolean("hamming", false));

            // Visual debugging
            framework.setFlag(Flags.DEBUG_FRAME,
                    options.getBoolean("debugFrame",
                            false));
            framework.setFlag(Flags.DEBUG_PREP_FRAME,
                    options.getBoolean("prepFrame", false));
            framework.setFlag(Flags.DEBUG_CONTOURS,
                    options.getBoolean("contours", false));
            framework.setFlag(Flags.DEBUG_POLY,
                    options.getBoolean("poly", false));

            framework.setFlag(Flags.DEBUG_DRAW_MARKERS,
                    options.getBoolean("marker", false));
            framework.setFlag(Flags.DEBUG_DRAW_SAMPLING,
                    options.getBoolean("sample", false));
            framework.setFlag(Flags.DEBUG_DRAW_MARKER_ID,
                    options.getBoolean("marker_id", false));

            // Threshold:
            framework.setBinaryThreshold(options.getInt("threshold", 100));
            // Method switch
            switch (options.getInt("bin", 0)) {
                case 0:
                    // default is normal threshold
                    framework.setFlag(Flags.USE_CANNY, false);
                    framework.setFlag(Flags.USE_ADAPTIVE, false);
                    break;
                case 1:
                    framework.setFlag(Flags.USE_ADAPTIVE, true);
                    framework.setFlag(Flags.USE_CANNY, false);
                    break;
                case 2:
                    framework.setFlag(Flags.USE_CANNY, true);
                    framework.setFlag(Flags.USE_ADAPTIVE, false);
                    break;
                default:
                    // do nothing
            }

            //TODO: register marker values and XML documents...

            // register all trackables:
            //modified .... we need xmls here...

            //OLD VERSION
            /*
            ArrayList<Integer> tra = options.getIntegerArrayList("trackers");
            for (int value : tra)
                framework.registerEntity(new Tracking(value, true, conv));

            */


            //new version
            ArrayList<Integer> trackers = options.getIntegerArrayList("trackers");
            ArrayList<String> processes = options.getStringArrayList("processes");
            for (int i = 0; i < trackers.size(); i++)
                framework.registerEntity(new Tracking(trackers.get(i), processes.get(i), true, null));

        }

        // Call on create:
        framework.onCreate((ViewGroup) findViewById(R.id.group));
    }

    public void onResume() {
        super.onResume();
        // Call on resume:
        framework.onResume();
    }

    public void onPause() {
        super.onPause();
        // Call on pause:
        framework.onPause();
    }

    public void onDestroy() {
        super.onDestroy();
        // Call on destroy:
        framework.onDestroy();
    }


    //modified
    //one does not simply place models into code...
    /*
    String house = "# Blender v2.68 (sub 0) OBJ File: ''\n" +
            "# www.blender.org\n" +
            "mtllib untitled.mtl\n" +
            "o Text_Mesh\n" +
            "v 0.465750 -0.823500 0.000000\n" +
            "v 0.465750 -0.236250 0.000000\n" +
            "v 0.367925 -0.317479 0.000000\n" +
            "v 0.277521 -0.390333 0.000000\n" +
            "v 0.193711 -0.455063 0.000000\n" +
            "v 0.115667 -0.511917 0.000000\n" +
            "v 0.042560 -0.561146 0.000000\n" +
            "v -0.026438 -0.603000 -0.000000\n" +
            "v -0.092154 -0.637729 -0.000000\n" +
            "v -0.155417 -0.665583 -0.000000\n" +
            "v -0.217055 -0.686812 -0.000000\n" +
            "v -0.277896 -0.701667 -0.000000\n" +
            "v -0.338768 -0.710396 -0.000000\n" +
            "v -0.400500 -0.713250 -0.000000\n" +
            "v -0.469484 -0.709327 -0.000000\n" +
            "v -0.534437 -0.697802 -0.000000\n" +
            "v -0.594984 -0.679043 -0.000000\n" +
            "v -0.650750 -0.653417 -0.000000\n" +
            "v -0.701359 -0.621290 -0.000000\n" +
            "v -0.746437 -0.583031 -0.000000\n" +
            "v -0.785609 -0.539007 -0.000000\n" +
            "v -0.818500 -0.489583 -0.000000\n" +
            "v -0.844734 -0.435129 -0.000000\n" +
            "v -0.863938 -0.376010 -0.000000\n" +
            "v -0.875735 -0.312595 -0.000000\n" +
            "v -0.879750 -0.245250 -0.000000\n" +
            "v -0.878763 -0.214490 -0.000000\n" +
            "v -0.875792 -0.184042 -0.000000\n" +
            "v -0.870820 -0.153844 -0.000000\n" +
            "v -0.863833 -0.123833 -0.000000\n" +
            "v -0.854815 -0.093948 -0.000000\n" +
            "v -0.843750 -0.064125 -0.000000\n" +
            "v -0.830622 -0.034302 -0.000000\n" +
            "v -0.815417 -0.004417 -0.000000\n" +
            "v -0.798117 0.025594 -0.000000\n" +
            "v -0.778708 0.055792 -0.000000\n" +
            "v -0.757175 0.086240 -0.000000\n" +
            "v -0.733500 0.117000 -0.000000\n" +
            "v -0.456750 0.117000 -0.000000\n" +
            "v -0.493909 0.086292 -0.000000\n" +
            "v -0.526771 0.056021 -0.000000\n" +
            "v -0.555539 0.026156 -0.000000\n" +
            "v -0.580417 -0.003333 -0.000000\n" +
            "v -0.601607 -0.032479 -0.000000\n" +
            "v -0.619313 -0.061313 -0.000000\n" +
            "v -0.633737 -0.089865 -0.000000\n" +
            "v -0.645083 -0.118167 -0.000000\n" +
            "v -0.653555 -0.146250 -0.000000\n" +
            "v -0.659354 -0.174146 -0.000000\n" +
            "v -0.662685 -0.201885 -0.000000\n" +
            "v -0.663750 -0.229500 -0.000000\n" +
            "v -0.661306 -0.266348 -0.000000\n" +
            "v -0.654135 -0.301344 -0.000000\n" +
            "v -0.642480 -0.334231 -0.000000\n" +
            "v -0.626583 -0.364750 -0.000000\n" +
            "v -0.606686 -0.392645 -0.000000\n" +
            "v -0.583031 -0.417656 -0.000000\n" +
            "v -0.555861 -0.439527 -0.000000\n" +
            "v -0.525417 -0.458000 -0.000000\n" +
            "v -0.491941 -0.472816 -0.000000\n" +
            "v -0.455677 -0.483719 -0.000000\n" +
            "v -0.416866 -0.490449 -0.000000\n" +
            "v -0.375750 -0.492750 -0.000000\n" +
            "v -0.344708 -0.492000 -0.000000\n" +
            "v -0.315479 -0.489750 -0.000000\n" +
            "v -0.287719 -0.486000 -0.000000\n" +
            "v -0.261083 -0.480750 -0.000000\n" +
            "v -0.235229 -0.474000 -0.000000\n" +
            "v -0.209812 -0.465750 -0.000000\n" +
            "v -0.184490 -0.456000 -0.000000\n" +
            "v -0.158917 -0.444750 -0.000000\n" +
            "v -0.132750 -0.432000 -0.000000\n" +
            "v -0.105646 -0.417750 -0.000000\n" +
            "v -0.077260 -0.402000 -0.000000\n" +
            "v -0.047250 -0.384750 -0.000000\n" +
            "v 0.009534 -0.350066 0.000000\n" +
            "v 0.068208 -0.311656 0.000000\n" +
            "v 0.128320 -0.270105 0.000000\n" +
            "v 0.189417 -0.226000 0.000000\n" +
            "v 0.251044 -0.179926 0.000000\n" +
            "v 0.312750 -0.132469 0.000000\n" +
            "v 0.374081 -0.084215 0.000000\n" +
            "v 0.434583 -0.035750 0.000000\n" +
            "v 0.493805 0.012340 0.000000\n" +
            "v 0.551292 0.059469 0.000000\n" +
            "v 0.606591 0.105051 0.000000\n" +
            "v 0.659250 0.148500 0.000000\n" +
            "v 0.675000 0.148500 0.000000\n" +
            "v 0.675000 -0.823500 0.000000\n" +
            "v -0.879750 0.454500 -0.000000\n" +
            "v -0.879750 0.549000 -0.000000\n" +
            "v 0.069750 1.325250 0.000000\n" +
            "v 0.171000 1.325250 0.000000\n" +
            "v 0.171000 0.675000 0.000000\n" +
            "v 0.675000 0.675000 0.000000\n" +
            "v 0.675000 0.454500 0.000000\n" +
            "v 0.171000 0.454500 0.000000\n" +
            "v 0.171000 0.290250 0.000000\n" +
            "v -0.027000 0.290250 -0.000000\n" +
            "v -0.027000 0.454500 -0.000000\n" +
            "v -0.463500 0.679500 -0.000000\n" +
            "v -0.463500 0.675000 -0.000000\n" +
            "v -0.027000 0.675000 -0.000000\n" +
            "v -0.027000 1.030500 -0.000000\n" +
            "v 0.465750 -0.823500 -0.225000\n" +
            "v 0.465750 -0.236250 -0.225000\n" +
            "v 0.367925 -0.317479 -0.225000\n" +
            "v 0.277521 -0.390333 -0.225000\n" +
            "v 0.193711 -0.455063 -0.225000\n" +
            "v 0.115667 -0.511917 -0.225000\n" +
            "v 0.042560 -0.561146 -0.225000\n" +
            "v -0.026438 -0.603000 -0.225000\n" +
            "v -0.092154 -0.637729 -0.225000\n" +
            "v -0.155417 -0.665583 -0.225000\n" +
            "v -0.217055 -0.686812 -0.225000\n" +
            "v -0.277896 -0.701667 -0.225000\n" +
            "v -0.338768 -0.710396 -0.225000\n" +
            "v -0.400500 -0.713250 -0.225000\n" +
            "v -0.469484 -0.709327 -0.225000\n" +
            "v -0.534437 -0.697802 -0.225000\n" +
            "v -0.594984 -0.679043 -0.225000\n" +
            "v -0.650750 -0.653417 -0.225000\n" +
            "v -0.701359 -0.621290 -0.225000\n" +
            "v -0.746437 -0.583031 -0.225000\n" +
            "v -0.785609 -0.539007 -0.225000\n" +
            "v -0.818500 -0.489583 -0.225000\n" +
            "v -0.844734 -0.435129 -0.225000\n" +
            "v -0.863938 -0.376010 -0.225000\n" +
            "v -0.875735 -0.312595 -0.225000\n" +
            "v -0.879750 -0.245250 -0.225000\n" +
            "v -0.878763 -0.214490 -0.225000\n" +
            "v -0.875792 -0.184042 -0.225000\n" +
            "v -0.870820 -0.153844 -0.225000\n" +
            "v -0.863833 -0.123833 -0.225000\n" +
            "v -0.854815 -0.093948 -0.225000\n" +
            "v -0.843750 -0.064125 -0.225000\n" +
            "v -0.830622 -0.034302 -0.225000\n" +
            "v -0.815417 -0.004417 -0.225000\n" +
            "v -0.798117 0.025594 -0.225000\n" +
            "v -0.778708 0.055792 -0.225000\n" +
            "v -0.757175 0.086240 -0.225000\n" +
            "v -0.733500 0.117000 -0.225000\n" +
            "v -0.456750 0.117000 -0.225000\n" +
            "v -0.493909 0.086292 -0.225000\n" +
            "v -0.526771 0.056021 -0.225000\n" +
            "v -0.555539 0.026156 -0.225000\n" +
            "v -0.580417 -0.003333 -0.225000\n" +
            "v -0.601607 -0.032479 -0.225000\n" +
            "v -0.619313 -0.061313 -0.225000\n" +
            "v -0.633737 -0.089865 -0.225000\n" +
            "v -0.645083 -0.118167 -0.225000\n" +
            "v -0.653555 -0.146250 -0.225000\n" +
            "v -0.659354 -0.174146 -0.225000\n" +
            "v -0.662685 -0.201885 -0.225000\n" +
            "v -0.663750 -0.229500 -0.225000\n" +
            "v -0.661306 -0.266348 -0.225000\n" +
            "v -0.654135 -0.301344 -0.225000\n" +
            "v -0.642480 -0.334231 -0.225000\n" +
            "v -0.626583 -0.364750 -0.225000\n" +
            "v -0.606686 -0.392645 -0.225000\n" +
            "v -0.583031 -0.417656 -0.225000\n" +
            "v -0.555861 -0.439527 -0.225000\n" +
            "v -0.525417 -0.458000 -0.225000\n" +
            "v -0.491941 -0.472816 -0.225000\n" +
            "v -0.455677 -0.483719 -0.225000\n" +
            "v -0.416866 -0.490449 -0.225000\n" +
            "v -0.375750 -0.492750 -0.225000\n" +
            "v -0.344708 -0.492000 -0.225000\n" +
            "v -0.315479 -0.489750 -0.225000\n" +
            "v -0.287719 -0.486000 -0.225000\n" +
            "v -0.261083 -0.480750 -0.225000\n" +
            "v -0.235229 -0.474000 -0.225000\n" +
            "v -0.209812 -0.465750 -0.225000\n" +
            "v -0.184490 -0.456000 -0.225000\n" +
            "v -0.158917 -0.444750 -0.225000\n" +
            "v -0.132750 -0.432000 -0.225000\n" +
            "v -0.105646 -0.417750 -0.225000\n" +
            "v -0.077260 -0.402000 -0.225000\n" +
            "v -0.047250 -0.384750 -0.225000\n" +
            "v 0.009534 -0.350066 -0.225000\n" +
            "v 0.068208 -0.311656 -0.225000\n" +
            "v 0.128320 -0.270105 -0.225000\n" +
            "v 0.189417 -0.226000 -0.225000\n" +
            "v 0.251044 -0.179926 -0.225000\n" +
            "v 0.312750 -0.132469 -0.225000\n" +
            "v 0.374081 -0.084215 -0.225000\n" +
            "v 0.434583 -0.035750 -0.225000\n" +
            "v 0.493805 0.012340 -0.225000\n" +
            "v 0.551292 0.059469 -0.225000\n" +
            "v 0.606591 0.105051 -0.225000\n" +
            "v 0.659250 0.148500 -0.225000\n" +
            "v 0.675000 0.148500 -0.225000\n" +
            "v 0.675000 -0.823500 -0.225000\n" +
            "v -0.879750 0.454500 -0.225000\n" +
            "v -0.879750 0.549000 -0.225000\n" +
            "v 0.069750 1.325250 -0.225000\n" +
            "v 0.171000 1.325250 -0.225000\n" +
            "v 0.171000 0.675000 -0.225000\n" +
            "v 0.675000 0.675000 -0.225000\n" +
            "v 0.675000 0.454500 -0.225000\n" +
            "v 0.171000 0.454500 -0.225000\n" +
            "v 0.171000 0.290250 -0.225000\n" +
            "v -0.027000 0.290250 -0.225000\n" +
            "v -0.027000 0.454500 -0.225000\n" +
            "v -0.463500 0.679500 -0.225000\n" +
            "v -0.463500 0.675000 -0.225000\n" +
            "v -0.027000 0.675000 -0.225000\n" +
            "v -0.027000 1.030500 -0.225000\n" +
            "usemtl None\n" +
            "s off\n" +
            "f 27 26 25\n" +
            "f 28 27 25\n" +
            "f 29 28 25\n" +
            "f 29 25 24\n" +
            "f 30 29 24\n" +
            "f 30 24 23\n" +
            "f 31 30 23\n" +
            "f 32 31 23\n" +
            "f 32 23 22\n" +
            "f 33 32 22\n" +
            "f 34 33 22\n" +
            "f 34 22 21\n" +
            "f 35 34 21\n" +
            "f 36 35 21\n" +
            "f 36 21 20\n" +
            "f 37 36 20\n" +
            "f 38 37 20\n" +
            "f 38 20 19\n" +
            "f 39 38 40\n" +
            "f 40 38 41\n" +
            "f 41 38 42\n" +
            "f 42 38 43\n" +
            "f 43 38 44\n" +
            "f 44 38 45\n" +
            "f 45 38 46\n" +
            "f 46 38 47\n" +
            "f 47 38 48\n" +
            "f 48 38 49\n" +
            "f 49 38 50\n" +
            "f 50 38 19\n" +
            "f 50 19 51\n" +
            "f 51 19 18\n" +
            "f 52 51 18\n" +
            "f 53 52 18\n" +
            "f 54 53 18\n" +
            "f 54 18 17\n" +
            "f 55 54 17\n" +
            "f 56 55 17\n" +
            "f 57 56 17\n" +
            "f 57 17 16\n" +
            "f 58 57 16\n" +
            "f 59 58 16\n" +
            "f 59 16 15\n" +
            "f 60 59 15\n" +
            "f 61 60 15\n" +
            "f 61 15 14\n" +
            "f 62 61 14\n" +
            "f 63 62 14\n" +
            "f 63 14 13\n" +
            "f 64 63 13\n" +
            "f 65 64 13\n" +
            "f 65 13 12\n" +
            "f 66 65 12\n" +
            "f 67 66 12\n" +
            "f 67 12 11\n" +
            "f 68 67 11\n" +
            "f 69 68 11\n" +
            "f 69 11 10\n" +
            "f 70 69 10\n" +
            "f 71 70 10\n" +
            "f 72 71 10\n" +
            "f 72 10 9\n" +
            "f 73 72 9\n" +
            "f 74 73 9\n" +
            "f 74 9 8\n" +
            "f 75 74 8\n" +
            "f 76 75 8\n" +
            "f 76 8 7\n" +
            "f 77 76 7\n" +
            "f 77 7 6\n" +
            "f 78 77 6\n" +
            "f 78 6 5\n" +
            "f 79 78 5\n" +
            "f 80 79 5\n" +
            "f 80 5 4\n" +
            "f 81 80 4\n" +
            "f 81 4 3\n" +
            "f 82 81 3\n" +
            "f 82 3 2\n" +
            "f 83 82 2\n" +
            "f 84 83 2\n" +
            "f 84 2 1\n" +
            "f 84 1 89\n" +
            "f 85 84 89\n" +
            "f 86 85 89\n" +
            "f 87 86 89\n" +
            "f 88 87 89\n" +
            "f 92 91 90\n" +
            "f 92 90 101\n" +
            "f 101 90 102\n" +
            "f 102 90 100\n" +
            "f 92 101 104\n" +
            "f 103 102 100\n" +
            "f 92 104 103\n" +
            "f 92 103 100\n" +
            "f 92 100 99\n" +
            "f 92 99 98\n" +
            "f 93 92 98\n" +
            "f 94 93 98\n" +
            "f 95 94 97\n" +
            "f 97 94 98\n" +
            "f 95 97 96\n" +
            "f 131 129 130\n" +
            "f 132 129 131\n" +
            "f 133 129 132\n" +
            "f 133 128 129\n" +
            "f 134 128 133\n" +
            "f 134 127 128\n" +
            "f 135 127 134\n" +
            "f 136 127 135\n" +
            "f 136 126 127\n" +
            "f 137 126 136\n" +
            "f 138 126 137\n" +
            "f 138 125 126\n" +
            "f 139 125 138\n" +
            "f 140 125 139\n" +
            "f 140 124 125\n" +
            "f 141 124 140\n" +
            "f 142 124 141\n" +
            "f 142 123 124\n" +
            "f 143 144 142\n" +
            "f 144 145 142\n" +
            "f 145 146 142\n" +
            "f 146 147 142\n" +
            "f 147 148 142\n" +
            "f 148 149 142\n" +
            "f 149 150 142\n" +
            "f 150 151 142\n" +
            "f 151 152 142\n" +
            "f 152 153 142\n" +
            "f 153 154 142\n" +
            "f 154 123 142\n" +
            "f 154 155 123\n" +
            "f 155 122 123\n" +
            "f 156 122 155\n" +
            "f 157 122 156\n" +
            "f 158 122 157\n" +
            "f 158 121 122\n" +
            "f 159 121 158\n" +
            "f 160 121 159\n" +
            "f 161 121 160\n" +
            "f 161 120 121\n" +
            "f 162 120 161\n" +
            "f 163 120 162\n" +
            "f 163 119 120\n" +
            "f 164 119 163\n" +
            "f 165 119 164\n" +
            "f 165 118 119\n" +
            "f 166 118 165\n" +
            "f 167 118 166\n" +
            "f 167 117 118\n" +
            "f 168 117 167\n" +
            "f 169 117 168\n" +
            "f 169 116 117\n" +
            "f 170 116 169\n" +
            "f 171 116 170\n" +
            "f 171 115 116\n" +
            "f 172 115 171\n" +
            "f 173 115 172\n" +
            "f 173 114 115\n" +
            "f 174 114 173\n" +
            "f 175 114 174\n" +
            "f 176 114 175\n" +
            "f 176 113 114\n" +
            "f 177 113 176\n" +
            "f 178 113 177\n" +
            "f 178 112 113\n" +
            "f 179 112 178\n" +
            "f 180 112 179\n" +
            "f 180 111 112\n" +
            "f 181 111 180\n" +
            "f 181 110 111\n" +
            "f 182 110 181\n" +
            "f 182 109 110\n" +
            "f 183 109 182\n" +
            "f 184 109 183\n" +
            "f 184 108 109\n" +
            "f 185 108 184\n" +
            "f 185 107 108\n" +
            "f 186 107 185\n" +
            "f 186 106 107\n" +
            "f 187 106 186\n" +
            "f 188 106 187\n" +
            "f 188 105 106\n" +
            "f 188 193 105\n" +
            "f 189 193 188\n" +
            "f 190 193 189\n" +
            "f 191 193 190\n" +
            "f 192 193 191\n" +
            "f 196 194 195\n" +
            "f 196 205 194\n" +
            "f 205 206 194\n" +
            "f 206 204 194\n" +
            "f 196 208 205\n" +
            "f 207 204 206\n" +
            "f 196 207 208\n" +
            "f 196 204 207\n" +
            "f 196 203 204\n" +
            "f 196 202 203\n" +
            "f 197 202 196\n" +
            "f 198 202 197\n" +
            "f 199 201 198\n" +
            "f 201 202 198\n" +
            "f 199 200 201\n" +
            "f 55 56 160\n" +
            "f 77 78 181\n" +
            "f 102 103 207\n" +
            "f 71 72 176\n" +
            "f 82 83 187\n" +
            "f 91 92 196\n" +
            "f 101 102 206\n" +
            "f 84 85 189\n" +
            "f 90 91 195\n" +
            "f 95 96 200\n" +
            "f 49 50 154\n" +
            "f 42 43 147\n" +
            "f 64 65 169\n" +
            "f 50 51 155\n" +
            "f 79 80 184\n" +
            "f 74 75 179\n" +
            "f 12 13 117\n" +
            "f 46 47 151\n" +
            "f 25 26 130\n" +
            "f 22 23 127\n" +
            "f 69 70 173\n" +
            "f 19 20 124\n" +
            "f 51 52 155\n" +
            "f 97 98 202\n" +
            "f 4 5 109\n" +
            "f 23 24 128\n" +
            "f 86 87 190\n" +
            "f 48 49 153\n" +
            "f 58 59 163\n" +
            "f 54 55 159\n" +
            "f 88 89 193\n" +
            "f 31 32 136\n" +
            "f 7 8 112\n" +
            "f 29 30 134\n" +
            "f 98 99 203\n" +
            "f 53 54 158\n" +
            "f 6 7 111\n" +
            "f 15 16 120\n" +
            "f 26 27 131\n" +
            "f 100 90 194\n" +
            "f 96 97 201\n" +
            "f 78 79 183\n" +
            "f 93 94 198\n" +
            "f 9 10 114\n" +
            "f 80 81 185\n" +
            "f 99 100 204\n" +
            "f 8 9 113\n" +
            "f 41 42 145\n" +
            "f 76 77 180\n" +
            "f 18 19 123\n" +
            "f 1 2 106\n" +
            "f 63 64 168\n" +
            "f 60 61 164\n" +
            "f 56 57 161\n" +
            "f 66 67 170\n" +
            "f 92 93 197\n" +
            "f 14 15 118\n" +
            "f 45 46 150\n" +
            "f 27 28 132\n" +
            "f 34 35 138\n" +
            "f 3 4 108\n" +
            "f 20 21 125\n" +
            "f 87 88 192\n" +
            "f 72 73 177\n" +
            "f 11 12 116\n" +
            "f 32 33 137\n" +
            "f 17 18 122\n" +
            "f 70 71 174\n" +
            "f 44 45 149\n" +
            "f 38 39 143\n" +
            "f 30 31 134\n" +
            "f 37 38 142\n" +
            "f 5 6 110\n" +
            "f 52 53 157\n" +
            "f 89 1 105\n" +
            "f 81 82 186\n" +
            "f 28 29 133\n" +
            "f 57 58 162\n" +
            "f 68 69 173\n" +
            "f 65 66 170\n" +
            "f 40 41 145\n" +
            "f 73 74 177\n" +
            "f 85 86 190\n" +
            "f 43 44 148\n" +
            "f 75 76 180\n" +
            "f 2 3 106\n" +
            "f 62 63 166\n" +
            "f 10 11 115\n" +
            "f 13 14 118\n" +
            "f 59 60 164\n" +
            "f 47 48 152\n" +
            "f 35 36 140\n" +
            "f 67 68 172\n" +
            "f 39 40 144\n" +
            "f 104 101 208\n" +
            "f 16 17 121\n" +
            "f 36 37 140\n" +
            "f 61 62 166\n" +
            "f 24 25 129\n" +
            "f 83 84 188\n" +
            "f 94 95 199\n" +
            "f 33 34 138\n" +
            "f 21 22 126\n" +
            "f 103 104 208\n" +
            "f 159 55 160\n" +
            "f 77 181 180\n" +
            "f 206 102 207\n" +
            "f 175 71 176\n" +
            "f 186 82 187\n" +
            "f 195 91 196\n" +
            "f 205 101 206\n" +
            "f 188 84 189\n" +
            "f 194 90 195\n" +
            "f 199 95 200\n" +
            "f 153 49 154\n" +
            "f 146 42 147\n" +
            "f 168 64 169\n" +
            "f 154 50 155\n" +
            "f 183 79 184\n" +
            "f 178 74 179\n" +
            "f 116 12 117\n" +
            "f 150 46 151\n" +
            "f 129 25 130\n" +
            "f 126 22 127\n" +
            "f 37 141 140\n" +
            "f 123 19 124\n" +
            "f 67 171 170\n" +
            "f 201 97 202\n" +
            "f 108 4 109\n" +
            "f 127 23 128\n" +
            "f 63 167 166\n" +
            "f 152 48 153\n" +
            "f 162 58 163\n" +
            "f 158 54 159\n" +
            "f 192 88 193\n" +
            "f 135 31 136\n" +
            "f 111 7 112\n" +
            "f 133 29 134\n" +
            "f 202 98 203\n" +
            "f 157 53 158\n" +
            "f 110 6 111\n" +
            "f 119 15 120\n" +
            "f 130 26 131\n" +
            "f 204 100 194\n" +
            "f 200 96 201\n" +
            "f 182 78 183\n" +
            "f 197 93 198\n" +
            "f 113 9 114\n" +
            "f 184 80 185\n" +
            "f 203 99 204\n" +
            "f 112 8 113\n" +
            "f 15 119 118\n" +
            "f 31 135 134\n" +
            "f 122 18 123\n" +
            "f 105 1 106\n" +
            "f 167 63 168\n" +
            "f 52 156 155\n" +
            "f 160 56 161\n" +
            "f 196 92 197\n" +
            "f 70 174 173\n" +
            "f 149 45 150\n" +
            "f 131 27 132\n" +
            "f 61 165 164\n" +
            "f 107 3 108\n" +
            "f 124 20 125\n" +
            "f 191 87 192\n" +
            "f 176 72 177\n" +
            "f 115 11 116\n" +
            "f 136 32 137\n" +
            "f 121 17 122\n" +
            "f 74 178 177\n" +
            "f 148 44 149\n" +
            "f 142 38 143\n" +
            "f 35 139 138\n" +
            "f 141 37 142\n" +
            "f 109 5 110\n" +
            "f 156 52 157\n" +
            "f 193 89 105\n" +
            "f 185 81 186\n" +
            "f 132 28 133\n" +
            "f 161 57 162\n" +
            "f 172 68 173\n" +
            "f 169 65 170\n" +
            "f 144 40 145\n" +
            "f 42 146 145\n" +
            "f 189 85 190\n" +
            "f 147 43 148\n" +
            "f 179 75 180\n" +
            "f 87 191 190\n" +
            "f 71 175 174\n" +
            "f 114 10 115\n" +
            "f 117 13 118\n" +
            "f 163 59 164\n" +
            "f 151 47 152\n" +
            "f 139 35 140\n" +
            "f 171 67 172\n" +
            "f 143 39 144\n" +
            "f 3 107 106\n" +
            "f 120 16 121\n" +
            "f 78 182 181\n" +
            "f 165 61 166\n" +
            "f 128 24 129\n" +
            "f 187 83 188\n" +
            "f 198 94 199\n" +
            "f 137 33 138\n" +
            "f 125 21 126\n" +
            "f 207 103 208\n" +
            "f 101 205 208";

            */
}
