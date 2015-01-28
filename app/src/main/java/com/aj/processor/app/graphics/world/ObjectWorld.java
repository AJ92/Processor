package com.aj.processor.app.graphics.world;

import com.aj.processor.app.graphics.model.Components.Material;
import com.aj.processor.app.graphics.model.Components.Mesh;
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
    /*
    String unitcube_model_path;
    Model unitcube_model;
    */

    //we don't use octrees for now...
    /*
    SP<OctTree> ot;
    SP<OctTreeFast> ot_dynamic_model;
    SP<OctTreeFast> ot_dynamic_lights;
    */


    //Data store modes
    public final static int store_mode_simple = 0x0001;
    public final static int store_mode_simple_sorted = 0x0002;

    //not implemented yet !!!
    public final static int store_mode_octree = 0x0003;

    //the data store mode in use
    private int store_mode = 0x0001;

    //lets use simple list for now...
    ArrayList<CompositeObject> compositeObjects_Models = new ArrayList<CompositeObject>();
    ArrayList<CompositeObject> compositeObjects_Lines = new ArrayList<CompositeObject>();


    //store_mode_simple_sorted index gives us the the corresponding:
    //  CompositeObject, Mesh, Model and Material...
    ArrayList<ArrayList<CompositeObject> > compositeObjects_mesh_list_simple_sorted = new ArrayList<ArrayList<CompositeObject> >();
    ArrayList<ArrayList<Mesh> > mesh_model_simple_sorted = new ArrayList<ArrayList<Mesh> >();
    ArrayList<ArrayList<Model> > model_mesh_simple_sorted = new ArrayList<ArrayList<Model> >();
    ArrayList<Material> material_mesh_simple_sorted = new ArrayList<Material>();




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
        //lines do not have textures and stuff that we need to sort...
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

        if(store_mode == store_mode_simple_sorted){
            addModelData_simple_sorted(co);
        }

        return co;
    }


    //this is the store_mode_simple case !!!
    //but also available for all the complex modes too !!!
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

    //adding the model and its data into the simple_sorted arrays
    private void addModelData_simple_sorted(CompositeObject co){
        //first get he model if it has any...
        if(!co.hasModel()){
            return;
        }
        Model mdl = co.getModel();

        //now get the model's meshs
        ArrayList<Mesh> mdl_meshs = mdl.get_meshs();


        if(mdl.isInstance()) {
            //iterate trough the meshs and check if they are already sorted in...
            //or in other words, if the material was already used in our lists...
            for (Mesh mesh : mdl_meshs) {
                //get the material...
                Material mesh_mtl = mesh.get_material();
                //check if the material was already used...
                boolean sort_in = false;
                int sort_in_index = -1;
                for (int j = 0; j < material_mesh_simple_sorted.size(); j++) {
                    if (mesh_mtl.get_name().equalsIgnoreCase(material_mesh_simple_sorted.get(j).get_name())) {
                        //we found the material, so it was sorted in...
                        //init the addition of the model data to the existing lists...
                        sort_in_index = j;
                        sort_in = true;
                        break;
                    }
                }
                if (sort_in) {
                    //add the model data at the index of the material...
                    mesh_model_simple_sorted.get(sort_in_index).add(mesh);
                    model_mesh_simple_sorted.get(sort_in_index).add(mdl);
                    compositeObjects_mesh_list_simple_sorted.get(sort_in_index).add(co);
                } else {
                    //model/material is not found in the lists, create a new entry
                    material_mesh_simple_sorted.add(mesh_mtl);
                    mesh_model_simple_sorted.add(new ArrayList<Mesh>());
                    model_mesh_simple_sorted.add(new ArrayList<Model>());
                    compositeObjects_mesh_list_simple_sorted.add(new ArrayList<CompositeObject>());
                    int size_index = mesh_model_simple_sorted.size() - 1;
                    mesh_model_simple_sorted.get(size_index).add(mesh);
                    model_mesh_simple_sorted.get(size_index).add(mdl);
                    compositeObjects_mesh_list_simple_sorted.get(size_index).add(co);
                }
            }
        }
        else{
            //our model is not an instance so just sort the model's data in....
            for (Mesh mesh : mdl_meshs) {
                //get the material...
                Material mesh_mtl = mesh.get_material();

                material_mesh_simple_sorted.add(mesh_mtl);
                mesh_model_simple_sorted.add(new ArrayList<Mesh>());
                model_mesh_simple_sorted.add(new ArrayList<Model>());
                compositeObjects_mesh_list_simple_sorted.add(new ArrayList<CompositeObject>());
                int size_index = mesh_model_simple_sorted.size() - 1;
                mesh_model_simple_sorted.get(size_index).add(mesh);
                model_mesh_simple_sorted.get(size_index).add(mdl);
                compositeObjects_mesh_list_simple_sorted.get(size_index).add(co);
            }
        }
    }

    //getters for the store_mode_simple_sorted lists...
    public ArrayList<ArrayList<CompositeObject> > getCompositeObjects_mesh_list_simple_sorted(){
        return compositeObjects_mesh_list_simple_sorted;
    }
    public ArrayList<ArrayList<Mesh> > getMesh_model_simple_sorted(){
        return mesh_model_simple_sorted;
    }
    public ArrayList<ArrayList<Model> > getModel_mesh_simple_sorted(){
        return model_mesh_simple_sorted;
    }
    public ArrayList<Material> getMaterial_mesh_simple_sorted(){
        return material_mesh_simple_sorted;
    }


    public int getStoreMode(){
        return this.store_mode;
    }
}
