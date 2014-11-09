package com.aj.processor.app.graphics.object;

import com.aj.processor.app.graphics.camera.Camera;
import com.aj.processor.app.graphics.model.Model;

/**
 * Created by AJ on 01.11.2014.
 *
 * This class' objects will contain certain types of objects which create as an unit a full Object in
 * 3D space.
 *
 * E.g. a model with all it's components it needs.
 * E.g. a light? with all it's components...
 * more ?
 *
 * It's a wrapper/container to achieve a loose interconnection between classes.
 */
public class CompositeObject {

    //extend to 32 bit if needed... ( 0xFFFFFFFF)
    public final static int Object_Type_ObjectEmpty                 = 0x0000;

    public final static int Object_Type_ObjectPositionRotation      = 0x0001;

    public final static int Object_Type_ObjectModel                 = 0x0002;
    public final static int Object_Type_ObjectPhysics               = 0x0004;
    public final static int Object_Type_ObjectInput                 = 0x0008;
    public final static int Object_Type_ObjectLight                 = 0x0010;

    public final static int Object_Type_ObjectCamera                = 0x0020;

    public final static int Object_Type_ObjectError                 = 0xFFFF;
    //more to follow...


    //octree storage choice...
    public final static int Object_Movement_Type_MovementStatic     = 0x0000;   //gets stored different (list)
    public final static int Object_Movement_Type_MovementDynamic    = 0x0001;   //gets stored different (multiple lists)

    private String name_ = "none";
    private int type_ = 0;          //Object_Type_ObjectEmpty
    private int movementType_ = 1;  //Object_Movement_Type_MovementDynamic

    Model model_;
    Camera camera_;
    Positation positation_;
    //more to follow...
    //Cpp
    //SP<Light> light_;



    public CompositeObject(){
        type_ = Object_Type_ObjectEmpty;
        movementType_ = Object_Movement_Type_MovementDynamic;
    }

    public CompositeObject(String name, int movementType){
        name_ = name;
        type_ = Object_Type_ObjectEmpty;
        movementType_ = movementType;
    }


    //stuff for model
    public void setModel(Model model){
        model_ = model;
        type_ = type_ | Object_Type_ObjectModel; //binary or
    }

    public boolean hasModel(){
        if((type_ & Object_Type_ObjectModel) == Object_Type_ObjectModel){ //binary and
            return true;
        }
        return false;
    }

    public Model getModel(){
        return model_;
    }


    //stuff for camera
    public void setCamera(Camera camera){
        camera_ = camera;
        type_ = type_ | Object_Type_ObjectCamera; //binary or
    }

    public boolean hasCamera(){
        if((type_ & Object_Type_ObjectCamera) == Object_Type_ObjectCamera){ //binary and
            return true;
        }
        return false;
    }

    public Camera getCamera(){
        return camera_;
    }



    //stuff for positation
    public void setPositation(Positation positation){
        positation_ = positation;
        type_ = type_ | Object_Type_ObjectPositionRotation; //binary or
    }

    public boolean hasPositation(){
        if((type_ & Object_Type_ObjectPositionRotation) == Object_Type_ObjectPositionRotation){ //binary and
            return true;
        }
        return false;
    }

    public Positation getPositation(){
        return positation_;
    }




    public boolean hasDynamicMovement(){
        if((movementType_ & Object_Movement_Type_MovementDynamic) == Object_Movement_Type_MovementDynamic){ //binary and
            return true;
        }
        return false;
    }

    public boolean hasStaticMovement(){
        if((movementType_ & Object_Movement_Type_MovementStatic) == Object_Movement_Type_MovementStatic){ //binary and
            return true;
        }
        return false;
    }



    //next 2 methods not needed actually...
    public int getObjectType(){
        return type_;
    }

    public int getObjectMovementType(){
        return movementType_;
    }



}
