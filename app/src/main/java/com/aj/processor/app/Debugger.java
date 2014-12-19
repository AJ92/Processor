package com.aj.processor.app;

import android.util.Log;

/**
 * Created by AJ on 18.12.2014.
 */
public class Debugger {
    public static boolean output_errors = true;
    public static boolean output_warnings = true;

    public static void error(String tag, String msg){
        if(output_errors) {
            Log.e(tag, msg);
        }
    }

    public static void warning(String tag, String msg){
        if(output_warnings) {
            Log.w(tag, msg);
        }
    }

}
