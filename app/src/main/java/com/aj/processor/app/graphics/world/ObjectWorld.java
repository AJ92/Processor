package com.aj.processor.app.graphics.world;

import com.aj.processor.app.graphics.model.Line;
import com.aj.processor.app.graphics.model.Model;
import com.aj.processor.app.graphics.model.ModelLoader;
import com.aj.processor.app.graphics.object.CompositeObject;
import com.aj.processor.app.graphics.object.Positation;
import com.aj.processor.app.mathematics.Vector.Vector3;
import com.aj.processor.app.mathematics.Vector.Vector4;

import java.util.ArrayList;

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


    //Data store modes
    public final static int store_mode_simple = 0x0001;

    //not implemented yet !!!
    public final static int store_mode_simple_sorted = 0x0002;
    public final static int store_mode_octree = 0x0003;

    //the data store mode in use
    private int store_mode = 0x0001;

    //lets use simple list for now...
    ArrayList<CompositeObject> compositeObjects_Models = new ArrayList<CompositeObject>();
    ArrayList<CompositeObject> compositeObjects_Lines = new ArrayList<CompositeObject>();

    public ObjectWorld(){
        ml = new ModelLoader();
    }

    public ObjectWorld(ModelLoader ml){
        this.ml = ml;
    }

    public ObjectWorld(int store_mode){
        this.store_mode = store_mode;
        ml = new ModelLoader();
    }

    public ObjectWorld(ModelLoader ml, int store_mode){
        this.store_mode = store_mode;
        this.ml = ml;
    }

    //dynamic (no position as argument)
    public CompositeObject loadLineObject(String name, Vector3 p1, Vector3 p2, Vector4 color){
        new Line(
                0.0f,0.0f,0.0f,
                0.5f,0.0f,0.0f,
                1.0f,0.0f,0.0f,1.0f
        );

        //create new dynamic Composite Object
        CompositeObject co = new CompositeObject(name,
                CompositeObject.Object_Movement_Type_MovementDynamic);

        //add Positation
        Positation posi = new Positation();
        co.setPositation(posi);

        co.setLine(new Line(p1,p2,color));

        //add the co to a list we can iterate trough later to render or what ever we want to do...
        //this is the store_mode_simple case !!!
        compositeObjects_Lines.add(co);

        return co;
    }

    //dynamic (no position as argument)
    public CompositeObject loadModelObject(String name, String path, boolean allow_instance){

        //create new dynamic Composite Object
        CompositeObject co = new CompositeObject(name,
                CompositeObject.Object_Movement_Type_MovementDynamic);

        //add Positation
        Positation posi = new Positation();
        co.setPositation(posi);

        //create load and add Model
        Model m = new Model();
        m.set_path(path);
        loadModel(m, allow_instance);
        co.setModel(m);

        //add the co to a list we can iterate trough later to render or what ever we want to do...
        //this is the store_mode_simple case !!!
        compositeObjects_Models.add(co);


        return co;
    }


    //this is the store_mode_simple case !!!
    //but also awailable for all the complex modes too !!!
    public ArrayList<CompositeObject> getCompositeObjectsModels(){
        return compositeObjects_Models;
    }

    public ArrayList<CompositeObject> getCompositeObjectsLines(){
        return compositeObjects_Lines;
    }

    //private functions...
    private void loadModel(Model m, boolean allow_instance){
        ml.loadModel(m, allow_instance);
    }

    public int getStoreMode(){
        return this.store_mode;
    }
}
