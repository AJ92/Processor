package com.aj.processor.app.XML.Process.Components;

/**
 * Created by AJ on 30.11.2014.
 */
public class StructuralNodeData {
    //ID IS A MUST HAVE !!!
    private String id_ = null;
    private String type_ = null;
    private String topologicalID_ = null;
    //more to follow...


    public static final int attribute_none              = 0x0000;
    public static final int attribute_id                = 0x0001;
    public static final int attribute_type              = 0x0002;
    public static final int attribute_topological_ID    = 0x0004;

    //more to follow...


    private int attributes_ = attribute_none;


    public StructuralNodeData(String node_id){
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



    //type stuff...
    public void setType(String type){
        type_ = type;
        this.attributes_ = this.attributes_ | attribute_type; //binary or
    }

    public boolean hasType(){
        if((attributes_ & attribute_type) == attribute_type){ //binary and
            return true;
        }
        return false;
    }

    public String getType(){
        return type_;
    }



    //topologicalID stuff...
    public void setTopologicalID(String topologicalID){
        topologicalID_ = topologicalID;
        this.attributes_ = this.attributes_ | attribute_topological_ID; //binary or
    }

    public boolean hasTopologicalID(){
        if((attributes_ & attribute_topological_ID) == attribute_topological_ID){ //binary and
            return true;
        }
        return false;
    }

    public String getTopologicalID(){
        return topologicalID_;
    }

}
