package com.aj.processor.app.XML.Process.Components;

/**
 * Created by AJ on 30.11.2014.
 */
public class DataEdge {

    //this class is more or less exactly the same as Edge...
    //nobody knows why xmls have 2 almost identical edge types ...

    //no id and name ???
    private String destinationNodeID_    = null;
    private String sourceNodeID_         = null;
    private String edgeType_             = null;
    //more to follow...


    public static final int attribute_none                  = 0x0000;
    public static final int attribute_destinationNodeID     = 0x0001;
    public static final int attribute_sourceNodeID          = 0x0002;
    public static final int attribute_edgeType              = 0x0004;
    //more to follow...


    private int attributes_ = attribute_none;

    //dummy constructor
    public DataEdge(){

    }

    public DataEdge(String destinationNodeID, String sourceNodeID, String edgeType){

    }


    //destinationNodeID stuff...
    public void setDestinationNodeID(String destinationNodeID){
        destinationNodeID_ = destinationNodeID;
        this.attributes_ = this.attributes_ | attribute_destinationNodeID; //binary or
    }

    public boolean hasDestinationNodeID(){
        if((attributes_ & attribute_destinationNodeID) == attribute_destinationNodeID){ //binary and
            return true;
        }
        return false;
    }

    public String getDestinationNodeID(){
        return destinationNodeID_;
    }



    //attribute_sourceNodeID stuff...
    public void setSourceNodeID(String sourceNodeID){
        sourceNodeID_ = sourceNodeID;
        this.attributes_ = this.attributes_ | attribute_sourceNodeID; //binary or
    }

    public boolean hasSourceNodeID(){
        if((attributes_ & attribute_sourceNodeID) == attribute_sourceNodeID){ //binary and
            return true;
        }
        return false;
    }

    public String getSourceNodeID(){
        return sourceNodeID_;
    }



    //edgeType stuff...
    public void setEdgeType(String edgeType){
        edgeType_ = edgeType;
        this.attributes_ = this.attributes_ | attribute_edgeType; //binary or
    }

    public boolean hasEdgeType(){
        if((attributes_ & attribute_edgeType) == attribute_edgeType){ //binary and
            return true;
        }
        return false;
    }

    public String getEdgeType(){
        return edgeType_;
    }

}
