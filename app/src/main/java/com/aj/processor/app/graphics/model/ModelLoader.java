package com.aj.processor.app.graphics.model;

import android.util.Log;

import com.aj.processor.app.graphics.model.parser.Loader;

import java.util.ArrayList;


/**
 * Created by AJ on 27.10.2014.
 *
 * a simple class that loads models,
 * stores them in a linear list,
 * or creates a "reference" to already existing models. (instance)
 */
public class ModelLoader {
    //ArrayList is better than LinkedList... but is not synchronized
    //Vector is synchronized between threads...
    private ArrayList<Model> all_models_list = new ArrayList<Model>();      //all models (includes instances)

    //next 3 lists are logically connected by the same index
    private ArrayList<Model> unique_models_list = new ArrayList<Model>();   //unique by data!!!
    private ArrayList<Integer> unique_models_reference_count_list = new ArrayList<Integer>();
    private ArrayList<String> unique_models_path_list = new ArrayList<String>();


    private Loader loader;

    public ModelLoader(){
        loader = new Loader();
    }

    public void loadModel(Model m, boolean allow_instance){
        //first we check if we have the model already loaded...
        //we check if we have the path of the to be loaded model in the
        //unique_models_path_list...

        Log.e("ModelLoader",m.get_path());


        int index = containsPath(m.get_path());
        if(index < 0 || !allow_instance){
            //we haven't found an already loaded model...
            Model new_mdl = loader.import_model(m.get_path());
            //copy the loaded model data to the old model...
            m.set_meshs(new_mdl.get_meshs());
            //check if already loaded...
            m.loadGLdata();

            unique_models_list.add(m);
            unique_models_path_list.add(m.get_path());
            unique_models_reference_count_list.add(1);

        }
        else{
            //looks like the model has already been loaded...
            m.set_meshs(unique_models_list.get(index).get_meshs());
            //check if already loaded...
            m.loadGLdata();

            //update the ref counter
            int count = unique_models_reference_count_list.get(index);
            unique_models_reference_count_list.set(index,count+1);
        }

        all_models_list.add(m);
    }

    //returns the index of the path/model
    //helper function for:
    // public void loadModel(model m)
    private int containsPath(String path){
        int i = 0;
        boolean found = false;
        for (String s : unique_models_path_list){
            if(path.equals(s)){
                found = true;
                break;
            }
            i++;
        }

        //have found the index ?
        if(found == true){
            return i;
        }
        //we haven't...
        return -1;
    }

    public ArrayList<Model> getModels(){
        return all_models_list;
    }

    // should be unsigned long long or so (cpp)
    public int modelCount(){
        return all_models_list.size();
    }
}