package com.aj.processor.app.graphics.model.parser;

import android.util.Log;

import com.aj.processor.app.Debugger;
import com.aj.processor.app.graphics.model.Model;
import com.aj.processor.app.graphics.model.parser.format_obj.Loader_obj;

/**
 * Created by AJ on 27.10.2014.
 *
 * a wrapper/unifier for different file formats
 *
 *    basic idea:
 * call one function, it decides which loader to call.
 */
public class Loader {
//constructor not needed ... class receives static functions anyways...
    public Loader()
    {
    }

    public Model import_model(String path){

        Debugger.warning("Loader", path);

        String pathlist[] = path.split("\\.");

        if(pathlist.length >= 2) {
            String suffix = pathlist[pathlist.length - 1];

            if (suffix.equalsIgnoreCase("fbx")) {
                return import_model_format_fbx(path);
            } else if (suffix.equalsIgnoreCase("bin")) {
                return import_model_format_bin(path);
            } else if (suffix.equalsIgnoreCase("obj")) {
                return import_model_format_obj(path);
            }
        }

        Debugger.warning("Loader", "is not a supported file extension...");

        //qDebug("no suitable format detected...");
        return new Model();
    }

    //not implemented yet...
    boolean export_model_bin(String path, Model mdl){
        return true;
    }

    //not implemented yet...
    private Model import_model_format_bin(String path){
        return new Model();
    }
    //not implemented yet...
    private Model import_model_format_fbx(String path){
        return new Model();
    }

    //works!
    private Model import_model_format_obj(String path){
        Model mdl = new Model();

        Loader_obj loader_obj = new Loader_obj();

        if(!loader_obj.load_model_data(mdl,path)){
            //qDebug("  import of obj mdl FAILED.");
        }
        return mdl;
    }
}