package com.aj.processor.app.XML.Process.Components;

/**
 * Created by AJ on 30.11.2014.
 */
public class Node {

    //ID IS A MUST HAVE !!!
    private String id_ = null;
    private String name_ = null;
    private String description_ = null;
    //more to follow...


    public static final int attribute_none              = 0x0000;
    public static final int attribute_id                = 0x0001;
    public static final int attribute_name              = 0x0002;
    public static final int attribute_description       = 0x0004;
    //more to follow...


    private int attributes_ = attribute_none;


    public Node(String node_id){
        this.id_ = node_id;
        this.attributes_ = this.attributes_ | attribute_id; //binary or
    }


    //id stuff...
    public void setID(String node_id){
        id_ = node_id;
        this.attributes_ = this.attributes_ | attribute_id; //binary or
    }

    public boolean hasID(){
        if((attributes_ & attribute_id) == attribute_id){ //binary and
            return true;
        }
        return false;
    }

    public String getID(){
        return id_;
    }



    //name stuff...
    public void setName(String name){
        name_ = name;
        this.attributes_ = this.attributes_ | attribute_name; //binary or
    }

    public boolean hasName(){
        if((attributes_ & attribute_name) == attribute_name){ //binary and
            return true;
        }
        return false;
    }

    public String getName(){
        return name_;
    }



    //desc stuff...
    public void setDescription(String description){
        description_ = description;
        this.attributes_ = this.attributes_ | attribute_description; //binary or
    }

    public boolean hasDescription(){
        if((attributes_ & attribute_description) == attribute_description){ //binary and
            return true;
        }
        return false;
    }

    public String getDescription(){
        return description_;
    }

}
