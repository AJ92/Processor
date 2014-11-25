package com.aj.processor.app.graphics.model;


import android.util.Log;

import com.aj.processor.app.graphics.model.Components.Material;
import com.aj.processor.app.graphics.model.Components.Mesh;
import com.aj.processor.app.mathematics.Vector.Vector3;

import java.util.ArrayList;

/**
 * Created by AJ on 27.10.2014.
 */
public class Model {

    boolean isReady = false;
    String path = "none";
    //bounding size...
    double size;
    ArrayList<Mesh> meshs = new ArrayList<Mesh>();

    //flags
    private boolean visible = true;

    String TAG = "Model";

    //empty constructor...
    //sets up an empty model, used by the model importer to create new models...
    public Model(){
        isReady = false;
        size = 0.0;
    }

    //copy constructor
    //copy the stuff from mdl to the new object!
    public Model(Model mdl){

    }

    //getter n setter...
    public void set_path(String path){
        this.path = path;
    }

    public String get_path(){
        return this.path;
    }

    public void add_mesh(Mesh mesh){
        meshs.add(mesh);
        recalculate_size();
    }

    public ArrayList<Mesh> get_meshs(){
        return meshs;
    }

    public void set_meshs(ArrayList<Mesh> mesh_list){
        meshs = mesh_list;
    }

    public boolean isReadyToRender(){
        return isReady;
    }




    //load gles data for it's components...
    public void loadGLdata(){
        for(int i = 0; i < meshs.size(); i++){
            Mesh mesh = meshs.get(i);
            Material mtl = mesh.get_material();

            if(mtl != null) {
                if (!mtl.isLoaded()) {
                    mtl.loadGLdata();
                }
            }
            else{
                Log.e(TAG,"mtl is missing for mesh: " + mesh.get_name());
            }
            if(!mesh.isLoaded()){
                mesh.loadGLdata();
            }
        }
        isReady = true;
        //qDebug("Model::loadGLdata()");
    }

    //simple helper fu
    private double max(double a, double b){
        if(a > b){
            return a;
        }
        return b;
    }

    //recalc the size of this model ...
    private void recalculate_size(){
        double temp_size = this.size;
        for(int i = 0; i < meshs.size(); i++){
            Vector3 pos = meshs.get(i).getBoundingSpherePos();
            double rad = meshs.get(i).getBoundingSphereRadius();
            temp_size = max(pos.length() + rad, temp_size);
        }
        this.size = temp_size;
    }


    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

}