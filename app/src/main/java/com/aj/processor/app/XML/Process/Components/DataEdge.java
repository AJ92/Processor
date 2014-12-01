package com.aj.processor.app.XML.Process.Components;

/**
 * Created by AJ on 30.11.2014.
 */
public class DataEdge {

    //this class is more or less exactly the same as Edge...
    //nobody knows why xmls have 2 almost identical edge types ...

    //no id and name ???
    private String connectorID_         = null;
    private String dataEdgeType_        = null;
    private String dataElementID_       = null;
    private String nodeID_              = null;
    //more to follow...


    public static final int attribute_none                  = 0x0000;
    public static final int attribute_connectorID           = 0x0001;
    public static final int attribute_dataEdgeType          = 0x0002;
    public static final int attribute_dataElementID         = 0x0004;
    public static final int attribute_nodeID                = 0x0008;
    //more to follow...


    private int attributes_ = attribute_none;

    //dummy constructor
    public DataEdge(){

    }

    public DataEdge(String connectorID, String dataEdgeType, String dataElementID, String nodeID){

    }


    //connectorID stuff...
    public void setConnectorID(String connectorID){
        connectorID_ = connectorID;
        this.attributes_ = this.attributes_ | attribute_connectorID; //binary or
    }

    public boolean hasConnectorID(){
        if((attributes_ & attribute_connectorID) == attribute_connectorID){ //binary and
            return true;
        }
        return false;
    }

    public String getConnectorID(){
        return connectorID_;
    }


    //dataEdgeType stuff...
    public void setDataEdgeType(String dataEdgeType){
        dataEdgeType_ = dataEdgeType;
        this.attributes_ = this.attributes_ | attribute_dataEdgeType; //binary or
    }

    public boolean hasDataEdgeType(){
        if((attributes_ & attribute_dataEdgeType) == attribute_dataEdgeType){ //binary and
            return true;
        }
        return false;
    }

    public String getDataEdgeType(){
        return dataEdgeType_;
    }


    //dataElementID stuff...
    public void setDataElementID(String dataElementID){
        dataElementID_ = dataElementID;
        this.attributes_ = this.attributes_ | attribute_dataElementID; //binary or
    }

    public boolean hasDataElementID(){
        if((attributes_ & attribute_dataElementID) == attribute_dataElementID){ //binary and
            return true;
        }
        return false;
    }

    public String getDataElementID(){
        return dataElementID_;
    }


    //nodeID stuff...
    public void setNodeID(String nodeID){
        nodeID_ = nodeID;
        this.attributes_ = this.attributes_ | attribute_nodeID; //binary or
    }

    public boolean hasNodeID(){
        if((attributes_ & attribute_nodeID) == attribute_nodeID){ //binary and
            return true;
        }
        return false;
    }

    public String getNodeID(){
        return nodeID_;
    }


}
