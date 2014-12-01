package com.aj.processor.app.XML.Process;

import com.aj.processor.app.XML.Process.Components.DataEdge;
import com.aj.processor.app.XML.Process.Components.DataElement;
import com.aj.processor.app.XML.Process.Components.Edge;
import com.aj.processor.app.XML.Process.Components.Node;
import com.aj.processor.app.XML.Process.Components.StructuralNodeData;

/**
 * Created by AJ on 30.11.2014.
 */
public class PComponent {
    private Edge edge_               = null;
    private DataEdge dataEdge_       = null;
    private Node node_               = null;
    private DataElement dataElement_ = null;
    private StructuralNodeData structuralNodeData_ = null;

    public final static int type_empty          = 0x0000;
    public final static int type_edge           = 0x0001;
    public final static int type_dataEdge       = 0x0002;
    public final static int type_node           = 0x0004;
    public final static int type_dataElement    = 0x0008;
    public final static int type_structuralNodeData    = 0x0010;

    //empty...
    private int type_               = type_empty;

    //empty...
    public PComponent(){

    }

    public PComponent(Edge edge){
        this.edge_ = edge;
        this.type_ = this.type_ | type_edge; //binary or
    }

    public PComponent(DataEdge dataEdge){
        this.dataEdge_ = dataEdge;
        this.type_ = this.type_ | type_dataEdge; //binary or
    }

    public PComponent(Node node){
        this.node_ = node;
        this.type_ = this.type_ | type_node; //binary or
    }

    public PComponent(DataElement dataElement){
        this.dataElement_ = dataElement;
        this.type_ = this.type_ | type_dataElement; //binary or
    }

    public PComponent(StructuralNodeData structuralNodeData){
        this.structuralNodeData_ = structuralNodeData;
        this.type_ = this.type_ | type_structuralNodeData; //binary or
    }

    public boolean hasEdge(){
        if((type_ & type_edge) == type_edge){ //binary and
            return true;
        }
        return false;
    }

    public boolean hasdataEdge(){
        if((type_ & type_dataEdge) == type_dataEdge){ //binary and
            return true;
        }
        return false;
    }

    public boolean hasNode(){
        if((type_ & type_node) == type_node){ //binary and
            return true;
        }
        return false;
    }

    public boolean hasDataElement(){
        if((type_ & type_dataElement) == type_dataElement){ //binary and
            return true;
        }
        return false;
    }

    public boolean hasStructuralNodeData(){
        if((type_ & type_structuralNodeData) == type_structuralNodeData){ //binary and
            return true;
        }
        return false;
    }

    public Edge getEdge(){
        return edge_;
    }

    public DataEdge getdataEdge(){
        return dataEdge_;
    }

    public Node getNode(){
        return node_;
    }

    public DataElement getDataElement(){
        return dataElement_;
    }

    public StructuralNodeData getStructuralNodeData(){
        return structuralNodeData_;
    }
}
