package com.aj.processor.app.graphics.world;

import com.aj.processor.app.graphics.model.Model;
import com.aj.processor.app.graphics.model.ModelLoader;
import com.aj.processor.app.graphics.object.CompositeObject;
import com.aj.processor.app.graphics.object.Positation;

/**
 * Created by AJ on 01.11.2014.
 *
 * This class stores all objects within the 3D world...
 */
public class ObjectWorld {

    ModelLoader ml;

    //we don't use lights for now...
    /*
    QString light_model_path;
    SP<Model> light_model;
    */

    //we don't use the unitcube for now...
    String unitcube_model_path;
    Model unitcube_model;

    //we don't use octrees for now...
    /*
    SP<OctTree> ot;
    SP<OctTreeFast> ot_dynamic_model;
    SP<OctTreeFast> ot_dynamic_lights;
    */

    public ObjectWorld(){
        ml = new ModelLoader();
    }

    //dynamic (no position as argument)
    public CompositeObject loadModelObject(String name, String path){

        //create new dynamic Composite Object
        CompositeObject co = new CompositeObject(name,
                CompositeObject.Object_Movement_Type_MovementDynamic);

        //add Pisitation
        Positation posi = new Positation();
        co.setPositation(posi);

        //create load and add Model
        Model m = new Model();
        m.set_path(path);
        loadModel(m);
        co.setModel(m);
        return co;
    }



    //private functions...
    private void loadModel(Model m){
        ml.loadModel(m);
    }
}
