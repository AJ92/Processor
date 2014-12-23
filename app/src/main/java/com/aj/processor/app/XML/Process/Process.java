package com.aj.processor.app.XML.Process;


import android.util.Log;

import com.aj.processor.app.Debugger;
import com.aj.processor.app.XML.Process.Components.Edge;
import com.aj.processor.app.XML.Process.Components.Node;
import com.aj.processor.app.XML.Process.Components.StructuralNodeData;
import com.aj.processor.app.graphics.object.CompositeObject;
import com.aj.processor.app.graphics.object.Positation;
import com.aj.processor.app.graphics.world.ObjectWorld;
import com.aj.processor.app.mathematics.Vector.Vector3;
import com.aj.processor.app.mathematics.Vector.Vector4;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by AJ on 30.11.2014.
 */
public class Process {

    private String TAG = "Process";

    private List<PComponent> componentList_ = new ArrayList<PComponent>();

    //more or less sorted data structures
    private List<PComponent> nodes_pc_unsorted_ = new ArrayList<PComponent>();
    private List<PComponent> structuralNodeData_pc_unsorted_ = new ArrayList<PComponent>();


    private List<PComponent> nodes_pc_sorted_ = new ArrayList<PComponent>();
    private List<PComponent> structuralNodeData_pc_sorted_ = new ArrayList<PComponent>();

    //sorted by topologyID
    private ArrayList<PComponent> node_structuralNodeData_pc_sorted_ = new ArrayList<PComponent>();
    private ArrayList<CompositeObject> co_node_sorted_ = new ArrayList<CompositeObject>();
    private ArrayList<CompositeObject> co_node_text_sorted_ = new ArrayList<CompositeObject>();
    //corresponding following nodes
    private ArrayList<ArrayList<PComponent>> next_nodes_pc_ = new ArrayList<ArrayList<PComponent>>();


    private ArrayList<PComponent> edges_pc_unsorted_ = new ArrayList<PComponent>();
    private ArrayList<ArrayList<CompositeObject> > co_edges_unsorted_ = new ArrayList<ArrayList<CompositeObject> >();


    private ArrayList<CompositeObject> all3dObjects = new ArrayList<CompositeObject>();


    //branch stuff
    private ArrayList<Branch> linear_branches_unsorted = new ArrayList<Branch>();
    private Branch root = null;



    //layered graph approach...
    private ArrayList<ArrayList<PComponent>> graph_layers = new ArrayList<ArrayList<PComponent>>();



    private int dummy_node_id = 0;



    //UNUSED
    //multi dimensional LIST
    private ArrayList<Integer> branchID_ = new ArrayList<Integer>();
    private ArrayList<ArrayList<ArrayList<PComponent>>> branches_ = new ArrayList<ArrayList<ArrayList<PComponent>>>();
    private ArrayList<ArrayList<ArrayList<Integer>>> branches_next_branchID_ = new ArrayList<ArrayList<ArrayList<Integer>>>();
    //UNUSED END





    //line colors

    private Vector4 line_et_control_color = new Vector4(250.0/255.0, 105.0/255.0, 0.0/255.0, 1.0);
    //not sure if correct name...
    //TODO:  get correct name ...
    private Vector4 line_et_control_back_color = new Vector4(105.0/255.0, 210.0/255.0, 231.0/255.0, 1.0); //???


    private String name = "";


    //dummy constructor
    public Process(String name, List<PComponent> pComponents) {
        this.name = name;
        componentList_ = pComponents;
        //generateSortedStructure();
        generateSortedStructure_v3();
    }

    public String getName(){
        return name;
    }

    public ArrayList<CompositeObject> getAll3dObjects(){
        return all3dObjects;
    }

    public void setVisible(){
        for(CompositeObject co : all3dObjects){
            co.setRenderType(CompositeObject.render_standard);
        }
    }

    public void setInvisible(){
        for(CompositeObject co : all3dObjects){
            co.setRenderType(CompositeObject.render_none);
        }
    }

    //called by openGL's render thread...
    public void generate3dDataObjects_v2(ObjectWorld ow) {
        //lets create the 3d data...

        ArrayList<PComponent> used_nodes = new ArrayList<PComponent>();

        //get first node
        PComponent pc_start = node_structuralNodeData_pc_sorted_.get(0);

        int x = 0;
        float y = 0.0f;

        //gen start node...
        CompositeObject start_node = gen3DNode(ow,pc_start);
        if(start_node != null) {
            start_node.getPositation().set_position(x * 4.0, -y * 4.0, 0.0);
            start_node.getPositation().set_scale(
                    1.8 *start_node.getPositation().get_scale().x(),
                    1.8 *start_node.getPositation().get_scale().y(),
                    1.8 *start_node.getPositation().get_scale().z());
            co_node_sorted_.set(0, start_node);
        }

        CompositeObject start_text_co = gen3DNodeText(ow,pc_start);
        if(start_text_co != null) {
            start_text_co.getPositation().set_position(0.5f + x * 4.0, 0.0f - y * 4.0, 0.3f);
            co_node_text_sorted_.set(0, start_text_co);
        }

        used_nodes.add(pc_start);

        x += 1;

        //gen following nodes
        boolean has_next_nodes = true;
        int current_node_index = getStructuralNodeDataIndexByNode(pc_start);
        ArrayList<PComponent> current_nodes = new ArrayList<PComponent>();
        if(current_node_index >= 0){
            current_nodes = next_nodes_pc_.get(current_node_index);
        }

        while(has_next_nodes) {

            ArrayList<PComponent> next_nodes = new ArrayList<PComponent>();

            int nodes_per_level = 0;

            //pre calc height
            int graph_height = -1;
            ArrayList<PComponent> double_nodes = new ArrayList<PComponent>();
            for (PComponent pc : current_nodes) {
                if (!used_nodes.contains(pc)) {
                    if(!double_nodes.contains(pc)) {
                        graph_height += 1;
                        double_nodes.add(pc);
                    }
                }
            }

            y = -graph_height/2.0f;

            for (PComponent pc : current_nodes) {

                if(!used_nodes.contains(pc)) {

                    //get the nodes index
                    int co_index = getStructuralNodeDataIndexByNode(pc);

                    CompositeObject test_node = gen3DNode(ow, pc);
                    if (test_node != null) {
                        test_node.getPositation().set_position(x * 4.0, -y * 4.0, 0.0);
                        test_node.getPositation().set_scale(
                                1.8 * test_node.getPositation().get_scale().x(),
                                1.8 * test_node.getPositation().get_scale().y(),
                                1.8 * test_node.getPositation().get_scale().z());

                        //sort the node in...
                        co_node_sorted_.set(co_index, test_node);
                    }

                    CompositeObject text_co = gen3DNodeText(ow, pc);
                    if (text_co != null) {
                        text_co.getPositation().set_position(0.5f + x * 4.0, 0.0f - y * 4.0, 0.3f);

                        //sort the node text in
                        co_node_text_sorted_.set(co_index, text_co);
                    }


                    //get the nodes next nodes
                    int next_node_index = getStructuralNodeDataIndexByNode(pc);
                    if (next_node_index >= 0) {
                        ArrayList<PComponent> next_nodes_nodes = next_nodes_pc_.get(next_node_index);
                        //append em to the next_nodes list
                        for (PComponent pc_n : next_nodes_nodes) {
                            next_nodes.add(pc_n);
                        }
                    }

                    used_nodes.add(pc);

                    nodes_per_level += 1;
                    y += 1.0;
                }
            }
            if(nodes_per_level == 0){
                has_next_nodes = false;
            }

            current_nodes = next_nodes;
            x += 1;
        }



        //now gen the connections...

        int edge_index = 0;
        for(PComponent pc_edge : edges_pc_unsorted_){

            //check if we have 2 connections...


            if(pc_edge.hasEdge()){
                Edge edge = pc_edge.getEdge();
                if(edge != null){
                    //get source
                    CompositeObject source_co = null;
                    int source_index = -1;
                    if(edge.hasSourceNodeID()){
                        String sourceID = edge.getSourceNodeID();
                        if(sourceID != null){
                            source_index = getStructuralNodeDataIndexByNodeID(sourceID);
                            if(source_index >= 0){
                                source_co = co_node_sorted_.get(source_index);
                            }
                        }
                    }

                    //get destination
                    CompositeObject destination_co = null;
                    int destination_index = -1;
                    if(edge.hasDestinationNodeID()){
                        String destinationID = edge.getDestinationNodeID();
                        if(destinationID != null){
                            destination_index = getStructuralNodeDataIndexByNodeID(destinationID);
                            if(destination_index >= 0){
                                destination_co = co_node_sorted_.get(destination_index);
                            }
                        }
                    }


                    //now lets check if we got both
                    if((source_co != null) && (destination_co != null)){
                        Log.e(TAG,"creating line from: " + source_index + "  to: " + destination_index);
                        //create a connection...
                        ArrayList<CompositeObject> line_list = gen3DLine(ow, pc_edge, source_co, destination_co);
                        co_edges_unsorted_.set(edge_index, line_list);
                    }

                }
            }

            //co_edges_unsorted_
            edge_index += 1;
        }

    }

    //called by openGL's render thread...
    public void generate3dDataObjects_v3(ObjectWorld ow){
        Debugger.error(TAG,"Starting generate3dDataObjects_v3()....");
        if(root == null){
            Debugger.error(TAG,"    no root branch...");
            Debugger.error(TAG,"End.");
            return;
        }

        //we first generate the objects and store them in a 2d array...
        // and then spread them out on vertical and horizontal axis...

        //first find out how long the array has to be,
        //distance of last element in branches...

        int max_distance = 0;
        for(Branch b : linear_branches_unsorted){
            max_distance = Math.max(max_distance,b.getDistance()+b.getElements().size());
        }

        //create arrays with empty Arrays where we can store our objects
        ArrayList<ArrayList<CompositeObject> > co_nodes = new ArrayList<ArrayList<CompositeObject>>();
        ArrayList<ArrayList<CompositeObject> > co_texts = new ArrayList<ArrayList<CompositeObject>>();
        //store how much space the need
        ArrayList<Integer> co_vertical_space = new ArrayList<Integer>();

        //pre fill em with emtpy lists
        for(int i = 0; i < max_distance; i++){
            co_nodes.add(new ArrayList<CompositeObject>());
            co_texts.add(new ArrayList<CompositeObject>());
            co_vertical_space.add(0);
        }

        //now go trough all branches again and draw their elements and fill the above arrays...
        //distance of branches/elements is the index for the above lists...

        int y = 0;

        for(Branch b : linear_branches_unsorted){
            int b_distance = b.getDistance();
            int b_overlapping_branches = b.getOverlappingBranchCount();

            for(PComponent pc : b.getElements()){

                //get the nodes index to sort the pc into a mapping between 3d and data objects
                int co_index = getStructuralNodeDataIndexByNode(pc);

                CompositeObject test_node = gen3DNode(ow, pc);
                if (test_node != null) {
                    test_node.getPositation().set_position(b_distance * 4.0, -y * 4.0, 0.0);
                    test_node.getPositation().set_scale(
                            1.8 * test_node.getPositation().get_scale().x(),
                            1.8 * test_node.getPositation().get_scale().y(),
                            1.8 * test_node.getPositation().get_scale().z());

                    //sort the node in...
                    co_node_sorted_.set(co_index, test_node);
                }
                //add into array which we will use to re-position objects
                co_nodes.get(b_distance).add(test_node);

                CompositeObject text_co = gen3DNodeText(ow, pc);
                if (text_co != null) {
                    text_co.getPositation().set_position(0.2f + b_distance * 4.0, 0.0f - y * 4.0, 0.3f);

                    //sort the node text in
                    co_node_text_sorted_.set(co_index, text_co);
                }
                //add into array which we will use to re-position objects
                co_texts.get(b_distance).add(text_co);

                //set the space we need...
                co_vertical_space.set(b_distance, b_overlapping_branches);

                b_distance += 1;
            }

            y += 1;
        }


        //reposition the objects...
        for(int i = 0; i < max_distance; i++){
            int overlapping_branch_count = co_vertical_space.get(i);

            ArrayList<CompositeObject> co_node_column = co_nodes.get(i);
            ArrayList<CompositeObject> co_text_column = co_texts.get(i);

            for(int j = 0; j < co_node_column.size(); j++){
                CompositeObject co_node = co_node_column.get(j);
                CompositeObject co_text = co_text_column.get(j);

                double scale = ((double) overlapping_branch_count) / ((double) co_node_column.size());

                double y_pos = scale * ((double) j) - ((((double) co_node_column.size()) - 1.0) / 2.0);

                if(co_node != null){
                    Vector3 pos = co_node.getPositation().getPosition();
                    co_node.getPositation().set_position(pos.x(),y_pos * 4.0,pos.z());
                }
                if(co_text != null){
                    Vector3 pos = co_text.getPositation().getPosition();
                    co_text.getPositation().set_position(pos.x(),y_pos * 4.0,pos.z());
                }
            }
        }


        Debugger.error(TAG,"End.");
    }

    //called by openGL's render thread...
    public void generate3dDataObjects_v4(ObjectWorld ow){
        Debugger.error(TAG,"--------GENERATING 3D OBJECTS-------");

        int x_pos_index = 0;
        int y_pos_index = 0;

        for(ArrayList<PComponent> layer : graph_layers){
            y_pos_index = 0;
            for(PComponent pc : layer){

                double x_pos = (double) x_pos_index;
                double y_pos = ((double) y_pos_index) - ((((double) layer.size()) - 1.0) / 2.0);

                //get the nodes index to sort the pc into a mapping between 3d and data objects
                int co_index = getStructuralNodeDataIndexByNode(pc);

                CompositeObject test_node = gen3DNode(ow, pc);
                if (test_node != null) {
                    test_node.getPositation().set_position(x_pos * 4.0, -y_pos * 2.0, 0.0);
                    test_node.getPositation().set_scale(
                            1.8 * test_node.getPositation().get_scale().x(),
                            1.8 * test_node.getPositation().get_scale().y(),
                            1.8 * test_node.getPositation().get_scale().z());

                    //sort the node in...
                    co_node_sorted_.set(co_index, test_node);
                }


                CompositeObject text_co = gen3DNodeText(ow, pc);
                if (text_co != null) {
                    text_co.getPositation().set_position(0.0f + x_pos * 4.0, 0.0f - y_pos * 2.0, 0.25f);
                    text_co.getPositation().set_scale(
                            0.8 * text_co.getPositation().get_scale().x(),
                            0.8 * text_co.getPositation().get_scale().y(),
                            0.8 * text_co.getPositation().get_scale().z());
                    //sort the node text in
                    co_node_text_sorted_.set(co_index, text_co);
                }

                y_pos_index += 1;
            }
            x_pos_index += 1;
        }







        //now gen the connections...

        int edge_index = 0;
        for(PComponent pc_edge : edges_pc_unsorted_){

            //check if we have 2 connections...


            if(pc_edge.hasEdge()){
                Edge edge = pc_edge.getEdge();
                if(edge != null){
                    //get source
                    CompositeObject source_co = null;
                    int source_index = -1;
                    if(edge.hasSourceNodeID()){
                        String sourceID = edge.getSourceNodeID();
                        if(sourceID != null){
                            source_index = getStructuralNodeDataIndexByNodeID(sourceID);
                            if(source_index >= 0){
                                source_co = co_node_sorted_.get(source_index);
                            }
                        }
                    }

                    //get destination
                    CompositeObject destination_co = null;
                    int destination_index = -1;
                    if(edge.hasDestinationNodeID()){
                        String destinationID = edge.getDestinationNodeID();
                        if(destinationID != null){
                            destination_index = getStructuralNodeDataIndexByNodeID(destinationID);
                            if(destination_index >= 0){
                                destination_co = co_node_sorted_.get(destination_index);
                            }
                        }
                    }


                    //now lets check if we got both
                    if((source_co != null) && (destination_co != null)){
                        Log.e(TAG,"creating line from: " + source_index + "  to: " + destination_index);
                        //create a connection...
                        ArrayList<CompositeObject> line_list = gen3DLine(ow, pc_edge, source_co, destination_co);
                        co_edges_unsorted_.set(edge_index, line_list);
                    }

                }
            }

            //co_edges_unsorted_
            edge_index += 1;
        }



        //add all 3d object to a list so we can access the easily

        for(CompositeObject co : co_node_sorted_){
            all3dObjects.add(co);
        }
        for(CompositeObject co : co_node_text_sorted_){
            all3dObjects.add(co);
        }
        for(ArrayList<CompositeObject> co_list : co_edges_unsorted_){
            for(CompositeObject co : co_list){
                all3dObjects.add(co);
            }
        }



        Debugger.error(TAG,"--------3D OBJECTS generated!-------");
    }

    private boolean containsBranch(ArrayList<Branch> list, Branch b){
        int bID = b.getBranchID();
        for(Branch b2 : list){
            if(b2.getBranchID() == bID){
                return true;
            }
        }
        return false;
    }

    private int getBranchDepthCount(int depth){
        int count = 0;
        for(Branch b : linear_branches_unsorted){
            if(b.getDepth() == depth){
                count += 1;
            }
        }
        return count;
    }


    private ArrayList<CompositeObject> gen3DLine(ObjectWorld ow, PComponent pc_edge, CompositeObject co_from, CompositeObject co_to){
        //do all the nasty checks...
        ArrayList<CompositeObject> line_list = new ArrayList<CompositeObject>();

        if((pc_edge != null) && (co_from != null) && (co_to != null)){
            if(pc_edge.hasEdge()){
                if(pc_edge.getEdge().hasEdgeType()){
                    String type = pc_edge.getEdge().getEdgeType();

                    //test if both CompositeObjects have Positations
                    if(co_from.hasPositation() && co_to.hasPositation()){

                        //get the 2 positions from the CompositeObjects
                        Vector3 p1 = co_from.getPositation().getPosition().add(new Vector3(0.9,0.0,0.0));
                        Vector3 p2 = co_to.getPositation().getPosition().add(new Vector3(-0.9,0.0,0.0));

                        //check the type..


                        if(type.equalsIgnoreCase("ET_CONTROL")){
                            line_list.add(ow.loadLineObject("line control",p1,p2,line_et_control_color));

                        }

                        //ceates 2 lines
                        //"ET_CONTROL_TO_DUMMY"
                        if(type.equalsIgnoreCase("ET_CONTROL_TO_DUMMY")){
                            line_list.add(ow.loadLineObject("line control",p1,p2,line_et_control_color));
                            line_list.add(ow.loadLineObject("line control",p2,
                                    p2.add(new Vector3(0.9*2.0,0.0,0.0)),
                                    line_et_control_color));

                        }

                        //"ET_CONTROL_FROM_DUMMY"   same as "ET_CONTROL"
                        if(type.equalsIgnoreCase("ET_CONTROL_FROM_DUMMY")){
                            line_list.add(ow.loadLineObject("line control",p1,p2,line_et_control_color));
                        }

                        //NOT SURE IF NAME IS CORRECT
                        //TODO: find out correct name...
                        if(type.equalsIgnoreCase("ET_CONTROL_BACK???")){

                            line_list.add(ow.loadLineObject("line control back",
                                    p1,
                                    p1.add(new Vector3(0.0,0.0,-1.0)),
                                    line_et_control_back_color));
                            line_list.add(ow.loadLineObject("line control back",
                                    p1.add(new Vector3(0.0,0.0,-1.0)),
                                    p2.add(new Vector3(0.0,0.0,-1.0)),
                                    line_et_control_back_color));
                            line_list.add(ow.loadLineObject("line control back",
                                    p2,
                                    p2.add(new Vector3(0.0,0.0,-1.0)),
                                    line_et_control_back_color));
                        }
                    }
                    else{
                        Log.e(TAG,"co_from or co_start not ready...");
                    }
                }
            }
        }

        return line_list;
    }

    private CompositeObject gen3DNode(ObjectWorld ow, PComponent pc){
        if(pc.hasStructuralNodeData()){
            if(pc.getStructuralNodeData().hasType()){
                String type = pc.getStructuralNodeData().getType();
                if(type != null){


                    if(type.equalsIgnoreCase("NT_STARTFLOW")){
                        return ow.loadModelObject("node start", "processComponents/Node/node_start.obj", true);
                    }

                    if(type.equalsIgnoreCase("NT_ENDFLOW")){
                        return ow.loadModelObject("node end", "processComponents/Node/node_end.obj", true);
                    }


                    if(type.equalsIgnoreCase("NT_NORMAL")){
                        return ow.loadModelObject("node normal", "processComponents/Node/node_normal.obj", true);
                    }

                    if(type.equalsIgnoreCase("NT_XOR_JOIN")){
                        CompositeObject co = ow.loadModelObject("node conditional join", "processComponents/Node/node_conditional.obj", true);
                        co.getPositation().set_scale(-1.0,1.0,1.0);
                        return co;
                    }

                    if(type.equalsIgnoreCase("NT_XOR_SPLIT")){
                        CompositeObject co = ow.loadModelObject("node conditional split", "processComponents/Node/node_conditional.obj", true);
                        return co;
                    }

                    if(type.equalsIgnoreCase("NT_AND_JOIN")){
                        CompositeObject co = ow.loadModelObject("node parallel split", "processComponents/Node/node_parallel.obj", true);
                        co.getPositation().set_scale(-1.0,1.0,1.0);
                        return co;
                    }

                    if(type.equalsIgnoreCase("NT_AND_SPLIT")){
                        CompositeObject co = ow.loadModelObject("node parallel join", "processComponents/Node/node_parallel.obj", true);
                        return co;
                    }

                    if(type.equalsIgnoreCase("DUMMY_NODE")){
                        CompositeObject co = new CompositeObject("Dummy node", CompositeObject.Object_Movement_Type_MovementDynamic);
                        Positation posi = new Positation();
                        co.setPositation(posi);
                        return co;
                    }
                }
            }
        }
        //create standard node...
        return ow.loadModelObject("node normal", "processComponents/Node/node_normal.obj", true);
    }

    private CompositeObject gen3DNodeText(ObjectWorld ow, PComponent pc){
        if(pc.hasStructuralNodeData()) {
            if (pc.getStructuralNodeData().hasType()) {
                String type = pc.getStructuralNodeData().getType();

                if (!type.equalsIgnoreCase("DUMMY_NODE")) {

                    if (pc.hasNode()) {
                        if (pc.getNode().hasName()) {
                            String node_name = pc.getNode().getName();
                            if (node_name != null) {
                                //skip empty name... if somebody was so smart to enter more than
                                //one space there... it's not my fault xD
                                if (!node_name.equals("") && !node_name.equals(" ")) {
                                    CompositeObject text_co = ow.loadModelObject("test_text", "text_model.obj", false);
                                    text_co.getModel().get_meshs().get(0).get_material().setDiffuseText(
                                            "FF00FF_TEXT_BG.png", node_name, 22.0f, 15, 15, 15
                                    );
                                    return text_co;
                                }
                            }
                        }
                        if (pc.getNode().hasID()) {
                            String node_id = pc.getNode().getID();
                            if (node_id != null) {
                                CompositeObject text_co = ow.loadModelObject("test_text", "text_model.obj", false);
                                text_co.getModel().get_meshs().get(0).get_material().setDiffuseText(
                                        "FF00FF_TEXT_BG.png", node_id, 22.0f, 15, 15, 15
                                );
                                return text_co;
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    private void generateSortedStructure_v2() {
        ArrayList<PComponent> nodes_pc = new ArrayList<PComponent>();
        ArrayList<PComponent> structuralNodeData_pc = new ArrayList<PComponent>();

        //sort in all NODES
        for (PComponent pc : componentList_) {
            if (pc.hasNode()) {
                nodes_pc.add(pc);
            }
        }

        //sort in all structuralNodeDatas by NODE's ID
        for (PComponent node : nodes_pc) {
            String node_id = node.getNode().getID();
            //find the structuralDataNode by ID

            PComponent structuralNodeData = null;

            for (PComponent pc : componentList_) {
                if (pc.hasStructuralNodeData()) {
                    if (pc.getStructuralNodeData().getID().equalsIgnoreCase(node_id)) {
                        structuralNodeData = pc;
                        break;
                    }
                }
            }
            //might contain null!!! (xml specs probably forbid it but we check it later...)
            structuralNodeData_pc.add(structuralNodeData);
        }

        //store them so we can ask later what node has which structural data and visa versa
        nodes_pc_unsorted_ = nodes_pc;
        structuralNodeData_pc_unsorted_ = structuralNodeData_pc;


        //now sort it so we can build branches and stuff..
        int sorted_nodes = 0;
        int nodes_to_sort = nodes_pc_unsorted_.size();

        int topology = 0;

        while (sorted_nodes < nodes_to_sort) {
            for (PComponent pc_snd : structuralNodeData_pc_unsorted_) {
                if (pc_snd != null) {
                    if (pc_snd.hasStructuralNodeData()) {
                        StructuralNodeData snd = pc_snd.getStructuralNodeData();
                        if (snd.hasTopologicalID()) {
                            try {
                                int topologicalID = Integer.parseInt(snd.getTopologicalID());
                                if (topologicalID == topology) {
                                    structuralNodeData_pc_sorted_.add(pc_snd);
                                    nodes_pc_sorted_.add(getStructuralNodeDatasNode(pc_snd));
                                    sorted_nodes += 1;
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "generateSortedStructure_v2()  StructuralNodeData's TopologicalID is a malformed integer...");
                            }
                        } else {
                            Log.e(TAG, "generateSortedStructure_v2()  StructuralNodeData has no TopologicalID...");
                        }
                    } else {
                        Log.e(TAG, "generateSortedStructure_v2()  StructuralNodeData PComponent does not contain any StructuralNodeData...");
                    }
                } else {
                    Log.e(TAG, "generateSortedStructure_v2()  StructuralNodeData PComponent is null...");
                }
            }
            topology += 1;

            //if we have not enough structuralNodeDatas we might encounter infinit loop...
            if ((nodes_to_sort * 3) < topology) {
                //if we have checked 3 times higher topological id than we actually have nodes
                //we bail out
                Log.e(TAG, "generateSortedStructure_v2()  found malformed Nodes, bailing out of sorting...");
                break;
            }

        }


        //lets join Nodes and StructuralNodeData
        for (int i = 0; i < nodes_pc_sorted_.size(); i++) {
            PComponent n_snd = nodes_pc_sorted_.get(i);
            n_snd.addStructuralNodeData(structuralNodeData_pc_sorted_.get(i).getStructuralNodeData());
            node_structuralNodeData_pc_sorted_.add(n_snd);

            //fill list with empty lists
            next_nodes_pc_.add(new ArrayList<PComponent>());
            co_node_sorted_.add(new CompositeObject());
            co_node_text_sorted_.add(new CompositeObject());
        }
        //now PComponents in node_structuralNodeData_pc_sorted_ contain
        // a Node and a StructuralNodeData

        Log.e(TAG, "generateSortedStructure_v2()  pre sorted Nodes and structuralNodeDatas data ...");


        //now sort out edges...
        for (PComponent pc : componentList_) {
            if (pc.hasEdge()) {
                if (pc.getEdge() != null) {
                    edges_pc_unsorted_.add(pc);
                    co_edges_unsorted_.add(new ArrayList<CompositeObject>());
                } else {
                    Log.e(TAG, "generateSortedStructure_v2()  PComponent says it has an Edge but returns null ...");
                }
            }
        }
        //we have all edges...


        //check if first node is startnode...
        boolean has_start = false;
        PComponent pc_start = node_structuralNodeData_pc_sorted_.get(0);
        if (pc_start != null) {
            if (pc_start.hasStructuralNodeData()) {
                if (pc_start.getStructuralNodeData() != null) {
                    if (pc_start.getStructuralNodeData().hasType()) {
                        if (pc_start.getStructuralNodeData().getType() != null) {
                            if (pc_start.getStructuralNodeData().getType().equalsIgnoreCase("NT_STARTFLOW")) {
                                has_start = true;
                            }
                        }
                    }
                }
            }
        }
        if (!has_start) {
            Log.e(TAG, "generateSortedStructure_v2()  no start node ...");
            return;
        }


        //find the following nodes for the node_structuralNodeData_pc_sorted_ list
        //next_nodes_pc_

        int max_depth = 0;

        int index_to_sort_in = 0;
        for (PComponent pc : node_structuralNodeData_pc_sorted_) {
            //find following nodes for pc
            if (pc.hasNode()) {
                if (pc.getNode() != null) {
                    String nodeID = pc.getNode().getID();
                    //find following nodes for nodeID

                    for (PComponent pc_edge : edges_pc_unsorted_) {
                        if (pc_edge.getEdge().getSourceNodeID().equalsIgnoreCase(nodeID)) {
                            max_depth += 1;

                            //find the destinationID's node and sort it in...

                            String destinationID = pc_edge.getEdge().getDestinationNodeID();

                            for (PComponent pc_dest : node_structuralNodeData_pc_sorted_) {
                                if (pc_dest.hasNode()) {
                                    if (pc_dest.getNode() != null) {
                                        if (pc_dest.getNode().getID().equalsIgnoreCase(destinationID)) {
                                            next_nodes_pc_.get(index_to_sort_in).add(pc_dest);
                                        }
                                    }
                                }
                            }

                        }
                    }

                }
            }
            index_to_sort_in += 1;
        }




        //create linear branches...
        //used_start_nodes_for_linear_branches
        //used_end_nodes_for_linear_branches
        //linear_branches_unsorted


        //get all start nodes
        //start nodes are either
        //START_FLOW
        //follow nodes of any kind of split
        //any kind of join...



        //get the start_flow node and create linear branch
        PComponent start_flow = getStartNode();
        linear_branches_unsorted.add(createLinearBranch(start_flow));

        //get follow nodes of any kind of split and create linear branches
        for (PComponent pc : node_structuralNodeData_pc_sorted_) {
            if(isSplitNode(pc)){
                //get it's next nodes
                ArrayList<PComponent> splits_nodes = new ArrayList<PComponent>();
                int node_index = getStructuralNodeDataIndexByNode(pc);
                if (node_index >= 0) {
                    ArrayList<PComponent> next_nodes_nodes = next_nodes_pc_.get(node_index);
                    //append em to the next_nodes list
                    for (PComponent pc_n : next_nodes_nodes) {
                        splits_nodes.add(pc_n);
                    }
                }
                //create linear branches
                for(PComponent splits_next : splits_nodes){
                    if(!isJoinNode(splits_next))
                        linear_branches_unsorted.add(createLinearBranch(splits_next));
                }
            }
        }

        ArrayList<PComponent> used_joins = new ArrayList<PComponent>();

        //get any kind of join and create linear branches
        for (PComponent pc : node_structuralNodeData_pc_sorted_) {
            if(isJoinNode(pc)){
                if(!used_joins.contains(pc)) {
                    linear_branches_unsorted.add(createLinearBranch(pc));
                    used_joins.add(pc);
                    debugPComponent(pc);
                }
            }
        }




        //connect the branches...

        //get the last element of 1st loop's branch and check if we need
        //to connect it with 2nd loop's (first element)
        for(Branch loop_1_b : linear_branches_unsorted){
            //check if last element
            int count_1 = loop_1_b.getElements().size();
            if(count_1 > 0) {
                PComponent pc_source = loop_1_b.getElements().get(count_1 - 1);

                for (Branch loop_2_b : linear_branches_unsorted) {
                    //check if first element
                    int count_2 = loop_2_b.getElements().size();
                    if(count_2 > 0) {
                        PComponent pc_destination = loop_2_b.getElements().get(0);

                        //now check if they are connected
                        if(areConnected(pc_source, pc_destination)){
                            //connect branches
                            loop_1_b.addBranch(loop_2_b);
                        }
                    }

                }
            }
        }

        if(linear_branches_unsorted.size() > 0){
            root = linear_branches_unsorted.get(0);
        }

        //optimize the branches...

        //x axis optimization
        //recursive
        root.optimize_x(0, 0);

        //y axis optimization
        //difficult to solve recursive...

        //set for each branch how many braches there are vertically
        for(Branch b : linear_branches_unsorted){

            Debugger.error(TAG,"branch id: " + b.getBranchID());

            b.setOverlappingBranchCount(
                    getVerticalOverlappingBranchCount(
                            b.getDistance(),
                            b.getDistance() + b.getElements().size() - 1
                    )
            );
        }


        Debugger.error(TAG, "SND_NODES COUNT: " + node_structuralNodeData_pc_sorted_.size());
        Debugger.error(TAG, "LINEAR_BRANCH COUNT: " + linear_branches_unsorted.size());





    }

    private void generateSortedStructure_v3() {

        if(componentList_ == null){
            return;
        }

        if(componentList_.size() == 0){
            return;
        }


        ArrayList<PComponent> nodes_pc = new ArrayList<PComponent>();
        ArrayList<PComponent> structuralNodeData_pc = new ArrayList<PComponent>();

        //sort in all NODES
        for (PComponent pc : componentList_) {
            if (pc.hasNode()) {
                nodes_pc.add(pc);
            }
        }

        //sort in all structuralNodeDatas by NODE's ID
        for (PComponent node : nodes_pc) {
            String node_id = node.getNode().getID();
            //find the structuralDataNode by ID

            PComponent structuralNodeData = null;

            for (PComponent pc : componentList_) {
                if (pc.hasStructuralNodeData()) {
                    if (pc.getStructuralNodeData().getID().equalsIgnoreCase(node_id)) {
                        structuralNodeData = pc;
                        break;
                    }
                }
            }
            //might contain null!!! (xml specs probably forbid it but we check it later...)
            structuralNodeData_pc.add(structuralNodeData);
        }

        //store them so we can ask later what node has which structural data and visa versa
        nodes_pc_unsorted_ = nodes_pc;
        structuralNodeData_pc_unsorted_ = structuralNodeData_pc;


        //now sort it so we can build branches and stuff..
        int sorted_nodes = 0;
        int nodes_to_sort = nodes_pc_unsorted_.size();

        int topology = 0;

        while (sorted_nodes < nodes_to_sort) {
            for (PComponent pc_snd : structuralNodeData_pc_unsorted_) {
                if (pc_snd != null) {
                    if (pc_snd.hasStructuralNodeData()) {
                        StructuralNodeData snd = pc_snd.getStructuralNodeData();
                        if (snd.hasTopologicalID()) {
                            try {
                                int topologicalID = Integer.parseInt(snd.getTopologicalID());
                                if (topologicalID == topology) {
                                    structuralNodeData_pc_sorted_.add(pc_snd);
                                    nodes_pc_sorted_.add(getStructuralNodeDatasNode(pc_snd));
                                    sorted_nodes += 1;
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "generateSortedStructure_v2()  StructuralNodeData's TopologicalID is a malformed integer...");
                            }
                        } else {
                            Log.e(TAG, "generateSortedStructure_v2()  StructuralNodeData has no TopologicalID...");
                        }
                    } else {
                        Log.e(TAG, "generateSortedStructure_v2()  StructuralNodeData PComponent does not contain any StructuralNodeData...");
                    }
                } else {
                    Log.e(TAG, "generateSortedStructure_v2()  StructuralNodeData PComponent is null...");
                }
            }
            topology += 1;

            //if we have not enough structuralNodeDatas we might encounter infinit loop...
            if ((nodes_to_sort * 3) < topology) {
                //if we have checked 3 times higher topological id than we actually have nodes
                //we bail out
                Log.e(TAG, "generateSortedStructure_v2()  found malformed Nodes, bailing out of sorting...");
                break;
            }

        }


        //lets join Nodes and StructuralNodeData
        for (int i = 0; i < nodes_pc_sorted_.size(); i++) {
            PComponent n_snd = nodes_pc_sorted_.get(i);
            n_snd.addStructuralNodeData(structuralNodeData_pc_sorted_.get(i).getStructuralNodeData());
            node_structuralNodeData_pc_sorted_.add(n_snd);

            //fill list with empty lists
            next_nodes_pc_.add(new ArrayList<PComponent>());
            co_node_sorted_.add(new CompositeObject());
            co_node_text_sorted_.add(new CompositeObject());
        }
        //now PComponents in node_structuralNodeData_pc_sorted_ contain
        // a Node and a StructuralNodeData

        Log.e(TAG, "generateSortedStructure_v2()  pre sorted Nodes and structuralNodeDatas data ...");


        //now sort out edges...
        for (PComponent pc : componentList_) {
            if (pc.hasEdge()) {
                if (pc.getEdge() != null) {
                    edges_pc_unsorted_.add(pc);
                    co_edges_unsorted_.add(new ArrayList<CompositeObject>());
                } else {
                    Log.e(TAG, "generateSortedStructure_v2()  PComponent says it has an Edge but returns null ...");
                }
            }
        }
        //we have all edges...


        //check if first node is startnode...
        boolean has_start = false;
        PComponent pc_start = getStartNode();
        has_start = isStartNode(pc_start);
        if (!has_start) {
            Log.e(TAG, "generateSortedStructure_v2()  no start node ...");
            return;
        }


        //find the following nodes for the node_structuralNodeData_pc_sorted_ list
        //next_nodes_pc_

        int max_depth = 0;

        int index_to_sort_in = 0;
        for (PComponent pc : node_structuralNodeData_pc_sorted_) {
            //find following nodes for pc
            if (pc.hasNode()) {
                if (pc.getNode() != null) {
                    String nodeID = pc.getNode().getID();
                    //find following nodes for nodeID

                    for (PComponent pc_edge : edges_pc_unsorted_) {
                        if (pc_edge.getEdge().getSourceNodeID().equalsIgnoreCase(nodeID)) {
                            max_depth += 1;

                            //find the destinationID's node and sort it in...

                            String destinationID = pc_edge.getEdge().getDestinationNodeID();

                            for (PComponent pc_dest : node_structuralNodeData_pc_sorted_) {
                                if (pc_dest.hasNode()) {
                                    if (pc_dest.getNode() != null) {
                                        if (pc_dest.getNode().getID().equalsIgnoreCase(destinationID)) {
                                            next_nodes_pc_.get(index_to_sort_in).add(pc_dest);
                                        }
                                    }
                                }
                            }

                        }
                    }

                }
            }
            index_to_sort_in += 1;
        }




        ///////////////////////////////////////////////////////////////////////////////////
        //
        //
        //                        CREATE LINEAR BRANCHES AND CREATE A TREE
        //
        //
        ///////////////////////////////////////////////////////////////////////////////////

        //create linear branches...
        //used_start_nodes_for_linear_branches
        //used_end_nodes_for_linear_branches
        //linear_branches_unsorted


        //get all start nodes
        //start nodes are either
        //START_FLOW
        //follow nodes of any kind of split
        //any kind of join...



        //get the start_flow node and create linear branch
        PComponent start_flow = getStartNode();
        linear_branches_unsorted.add(createLinearBranch(start_flow));

        //get follow nodes of any kind of split and create linear branches
        for (PComponent pc : node_structuralNodeData_pc_sorted_) {
            if(isSplitNode(pc)){
                //get it's next nodes
                ArrayList<PComponent> splits_nodes = new ArrayList<PComponent>();
                int node_index = getStructuralNodeDataIndexByNode(pc);
                if (node_index >= 0) {
                    ArrayList<PComponent> next_nodes_nodes = next_nodes_pc_.get(node_index);
                    //append em to the next_nodes list
                    for (PComponent pc_n : next_nodes_nodes) {
                        splits_nodes.add(pc_n);
                    }
                }
                //create linear branches
                for(PComponent splits_next : splits_nodes){
                    if(!isJoinNode(splits_next))
                        linear_branches_unsorted.add(createLinearBranch(splits_next));
                }
            }
        }

        ArrayList<PComponent> used_joins = new ArrayList<PComponent>();

        //get any kind of join and create linear branches
        for (PComponent pc : node_structuralNodeData_pc_sorted_) {
            if(isJoinNode(pc)){
                if(!used_joins.contains(pc)) {
                    linear_branches_unsorted.add(createLinearBranch(pc));
                    used_joins.add(pc);
                    debugPComponent(pc);
                }
            }
        }




        //connect the branches...

        //get the last element of 1st loop's branch and check if we need
        //to connect it with 2nd loop's (first element)
        for(Branch loop_1_b : linear_branches_unsorted){
            //check if last element
            int count_1 = loop_1_b.getElements().size();
            if(count_1 > 0) {
                PComponent pc_source = loop_1_b.getElements().get(count_1 - 1);

                for (Branch loop_2_b : linear_branches_unsorted) {
                    //check if first element
                    int count_2 = loop_2_b.getElements().size();
                    if(count_2 > 0) {
                        PComponent pc_destination = loop_2_b.getElements().get(0);

                        //now check if they are connected
                        if(areConnected(pc_source, pc_destination)){
                            //connect branches
                            loop_1_b.addBranch(loop_2_b);
                        }
                    }

                }
            }
        }

        if(linear_branches_unsorted.size() > 0){
            root = linear_branches_unsorted.get(0);
        }

        //optimize the branches...

        //x axis optimization
        //recursive
        root.optimize_x(0, 0);

        //y axis optimization
        //difficult to solve recursive...

        //set for each branch how many braches there are vertically
        for(Branch b : linear_branches_unsorted){

            Debugger.error(TAG,"branch id: " + b.getBranchID());

            b.setOverlappingBranchCount(
                    getVerticalOverlappingBranchCount(
                            b.getDistance(),
                            b.getDistance() + b.getElements().size() - 1
                    )
            );
        }


        Debugger.error(TAG, "SND_NODES COUNT: " + node_structuralNodeData_pc_sorted_.size());
        Debugger.error(TAG, "LINEAR_BRANCH COUNT: " + linear_branches_unsorted.size());




        ///////////////////////////////////////////////////////////////////////////////////
        //
        //
        //                        CREATE LAYERED GRAPH STRUCTURE...
        //
        //
        ///////////////////////////////////////////////////////////////////////////////////

        //        graph_layers


        //get the start_flow node and create linear branch
        PComponent start_node = getStartNode();
        if(start_node == null){
            Debugger.error(TAG,"no start node ???");
            return;
        }

        ArrayList<PComponent> used_nodes = new ArrayList<PComponent>();
        ArrayList<PComponent> layer_nodes = new ArrayList<PComponent>();
        layer_nodes.add(start_node);

        boolean work = true;
        while(work){

            graph_layers.add(layer_nodes);
            ArrayList<PComponent> next_nodes = new ArrayList<PComponent>();
            for(PComponent pc : layer_nodes){
                ArrayList<PComponent> new_nodes = getNodesNextNodes(pc);
                for(PComponent new_pc : new_nodes){
                    if(!used_nodes.contains(new_pc)){
                        used_nodes.add(new_pc);
                        next_nodes.add(new_pc);
                    }
                }
            }
            layer_nodes = next_nodes;
            if(next_nodes.size() == 0){
                work = false;
            }
        }


        /*
        Debugger.error(TAG,"-----------DEBUG LAYERS------------");
        //debug layers...
        for(ArrayList<PComponent> layer : graph_layers){
            for(PComponent pc : layer){
                debugPComponent(pc);
            }
            Debugger.error(TAG,"-----------LAYER END------------");
        }
        */

        Debugger.error(TAG,"stretch layers...");

        //we have filled the layers...
        //now we need to stretch them out if needed...

        //basically check if destination nodes are in the same layer as source nodes...
        int layer_index = 0;
        while(layer_index < graph_layers.size()){

            ArrayList<PComponent> layer = graph_layers.get(layer_index);
            boolean found = true;
            while(found){
                found = false;
                int found_index = 0;
                for(PComponent pc : layer){
                    //get previous nodes...
                    ArrayList<PComponent> previous_nodes = getNodesPreviousNodes(pc);
                    for(PComponent p_pc : previous_nodes){
                        if(isNodeInLayer(p_pc, layer)){
                            found = true;
                            debugPComponent(pc);
                            break;
                        }
                    }
                    if(found){
                        break;
                    }
                    found_index += 1;

                }

                if(found) {
                    //found one... use the index to move it one layer down

                    //get element to move down
                    PComponent element_to_move = layer.get(found_index);
                    layer.remove(found_index);

                    //check if next layer is existing
                    if ((layer_index + 1) >= graph_layers.size()) {
                        //add new layer
                        graph_layers.add(new ArrayList<PComponent>());
                    }

                    //add the found element to the next layer
                    graph_layers.get(layer_index + 1).add(element_to_move);
                }
            }
            layer_index += 1;
        }

        Debugger.error(TAG,"layers are stretched END");


        //check if we have edges which go over multiple nodes...
        //divide those and insert dummy nodes...

        /*

                ---                 ---
                 o                   o
                -|-                 -|-
                 |      becomes      @
                -|-                 -|-
                 o                   o
                ---                 ---

        */

        ArrayList<PComponent> cut_edges = new ArrayList<PComponent>();
        for(PComponent edge : edges_pc_unsorted_){
            if(isEdgeOverMultipleLayers(edge,graph_layers)){
                //we need to cut this edge...
                cut_edges.add(edge);
            }
        }
        Debugger.error(TAG,"CUT EDGES: " + cut_edges.size());
        for(PComponent edge : cut_edges){
            cutEdgeInGraphLayer(edge);
        }

        /*
        Debugger.error(TAG,"-----------DEBUG LAYERS------------");
        //debug layers...
        for(ArrayList<PComponent> layer : graph_layers){

            for(PComponent pc : layer){
                debugPComponent(pc);
            }
            Debugger.error(TAG,"-----------LAYER END------------");
        }
        */

        //check all edges if they are crossing
        Debugger.error(TAG,"-----------CHECK FOR CROSSINGS------------");
        int max_loops = 1000;
        int loop = 0;
        boolean crossing_edges = true;

        while(crossing_edges){

            crossing_edges = false;

            for(PComponent edge_l_1 : edges_pc_unsorted_) {
                if (edge_l_1.hasEdge()) {
                    if (edge_l_1.getEdge() != null) {
                        for (PComponent edge_l_2 : edges_pc_unsorted_) {
                            if (edge_l_2.hasEdge()) {
                                if (edge_l_2.getEdge() != null) {


                                    if(areEdgesCrossing(edge_l_1, edge_l_2, graph_layers)){

                                        Debugger.error(TAG,"-------CROSSING-");

                                        //ok edges are crossing, get their destination layer indices
                                        //and swap their position in the layers

                                        crossing_edges = true;

                                        //get destination nodes
                                        String destination_node_1_id = getEdgeDestinationID(edge_l_1);
                                        String destination_node_2_id = getEdgeDestinationID(edge_l_2);

                                        PComponent destination_node_1 = getStructuralNodeDataByNodeID(destination_node_1_id);
                                        PComponent destination_node_2 = getStructuralNodeDataByNodeID(destination_node_2_id);

                                        if((destination_node_1 == null) || (destination_node_2 == null)){
                                            continue;
                                        }

                                        //get destination Nodes indices  of layer in graph_layers...
                                        int graph_layer_index_destination_node_1 = getGraphLayerIndexOfNode(destination_node_1,graph_layers);


                                        //found nodes in layers
                                        if(graph_layer_index_destination_node_1 >= 0){
                                            //swap pos in layer

                                            ArrayList<PComponent> layer = graph_layers.get(graph_layer_index_destination_node_1);
                                            int node_1_index = getIndexOfNodeInLayer(destination_node_1,graph_layers.get(graph_layer_index_destination_node_1));
                                            int node_2_index = getIndexOfNodeInLayer(destination_node_2,graph_layers.get(graph_layer_index_destination_node_1));


                                            layer.set(node_1_index,destination_node_2);
                                            layer.set(node_2_index,destination_node_1);

                                        }

                                    }

                                }
                            }
                        }
                    }
                }
            }

            loop += 1;

            if(loop >= max_loops){
                crossing_edges = false;
            }
        }

        Debugger.error(TAG,"-----------DEBUG LAYERS------------");
        //debug layers...
        for(ArrayList<PComponent> layer : graph_layers){

            for(PComponent pc : layer){
                debugPComponent(pc);
            }
            Debugger.error(TAG,"-----------LAYER END------------");
        }

    }

    private boolean areEdgesCrossing(PComponent edge1,
                                     PComponent edge2,
                                     ArrayList<ArrayList<PComponent>> graph_layers){

        //check if input edges actually have edges...
        if(!edge1.hasEdge() || !edge2.hasEdge()){
            Debugger.error(TAG,"--------------edge1 or edge2 has no EDGE");
            return false;
        }
        if((edge1.getEdge() == null) || (edge2.getEdge() == null)){
            Debugger.error(TAG,"--------------edge1 or edge2 EDGE is null");
            return false;
        }


        //check the indices on the layers...


        //get source nodes
        String source_node_1_id = getEdgeSourceID(edge1);
        String source_node_2_id = getEdgeSourceID(edge2);

        PComponent source_node_1 = getStructuralNodeDataByNodeID(source_node_1_id);
        PComponent source_node_2 = getStructuralNodeDataByNodeID(source_node_2_id);

        if((source_node_1 == null) || (source_node_2 == null)){
            Debugger.error(TAG,"--------------source_node_1 or source_node_2 are null");
            return false;
        }

        Debugger.error(TAG,"---CROSSING TEST:");

        //get source Nodes indices of layer in graph_layers...
        int graph_layer_index_source_node_1 = getGraphLayerIndexOfNode(source_node_1,graph_layers);
        int graph_layer_index_source_node_2 = getGraphLayerIndexOfNode(source_node_2,graph_layers);



        //couldn't find nodes in layers
        if((graph_layer_index_source_node_1 < 0) || (graph_layer_index_source_node_1 < 0)){
            return false;
        }

        //nodes are not on same level so they do not cross...
        if(graph_layer_index_source_node_1 != graph_layer_index_source_node_2){
            return false;
        }

        Debugger.error(TAG,"---graph_layer_index_source_node_1: " + graph_layer_index_source_node_1);
        Debugger.error(TAG,"---graph_layer_index_source_node_2: " + graph_layer_index_source_node_2);



        //get destination nodes
        String destination_node_1_id = getEdgeDestinationID(edge1);
        String destination_node_2_id = getEdgeDestinationID(edge2);

        Debugger.error(TAG,"----destination_node_1_id: " + destination_node_1_id);
        Debugger.error(TAG,"----destination_node_2_id: " + destination_node_2_id);


        PComponent destination_node_1 = getStructuralNodeDataByNodeID(destination_node_1_id);
        PComponent destination_node_2 = getStructuralNodeDataByNodeID(destination_node_2_id);

        if((destination_node_1 == null) || (destination_node_2 == null)){
            return false;
        }

        //get destination Nodes indices  of layer in graph_layers...
        int graph_layer_index_destination_node_1 = getGraphLayerIndexOfNode(destination_node_1,graph_layers);
        int graph_layer_index_destination_node_2 = getGraphLayerIndexOfNode(destination_node_2,graph_layers);

        //couldn't find nodes in layers
        if((graph_layer_index_destination_node_1 < 0) || (graph_layer_index_destination_node_2 < 0)){
            return false;
        }

        Debugger.error(TAG,"---graph_layer_index_destination_node_1: " + graph_layer_index_destination_node_1);
        Debugger.error(TAG,"---graph_layer_index_destination_node_2: " + graph_layer_index_destination_node_2);

        //nodes are not on same level so they do not cross...
        if(graph_layer_index_destination_node_1 != graph_layer_index_destination_node_2){
            return false;
        }



        //get the node's indices on their levels...
        int layer_index_source_node_1 = getIndexOfNodeInLayer(
                source_node_1,
                graph_layers.get(graph_layer_index_source_node_1)
        );
        int layer_index_source_node_2 = getIndexOfNodeInLayer(
                source_node_2,
                graph_layers.get(graph_layer_index_source_node_2)
        );

        int layer_index_destination_node_1 = getIndexOfNodeInLayer(
                destination_node_1,
                graph_layers.get(graph_layer_index_destination_node_1)
        );
        int layer_index_destination_node_2 = getIndexOfNodeInLayer(
                destination_node_2,
                graph_layers.get(graph_layer_index_destination_node_2)
        );

        //check if indices are correct...
        if(     (layer_index_source_node_1 < 0) ||
                (layer_index_source_node_2 < 0) ||
                (layer_index_destination_node_1 < 0) ||
                (layer_index_destination_node_2 < 0)){
            return false;
        }


        //now check if crossing...
        //same root ? no cross
        if(layer_index_source_node_1 == layer_index_source_node_2){
            return false;
        }

        //same destination ? no cross
        if(layer_index_destination_node_1 == layer_index_destination_node_2){
            return false;
        }

        Debugger.error(TAG,"---checking....");

        if(layer_index_source_node_1 < layer_index_source_node_2){
            if(layer_index_destination_node_1 < layer_index_destination_node_2){
                return false;
            }
            else{
                return true;
            }
        }

        if(layer_index_source_node_1 > layer_index_source_node_2){
            if(layer_index_destination_node_1 > layer_index_destination_node_2){
                return false;
            }
            else{
                return true;
            }
        }




        return false;
    }



    //looks almost the same as isEdgeOverMultipleLayers...
    private void cutEdgeInGraphLayer(PComponent edge){
        //get both nodes from edge
        String destination_id = getEdgeDestinationID(edge);
        String source_id = getEdgeSourceID(edge);

        if((destination_id != null) && (source_id != null)){
            //get nodes
            PComponent destination_node = getStructuralNodeDataByNodeID(destination_id);
            PComponent source_node = getStructuralNodeDataByNodeID(source_id);
            if((destination_node != null) && (source_node != null)){
                int destination_index = 0;
                int source_index = 0;

                //find layer index of destination node
                int temp_index = 0;
                for(ArrayList<PComponent> layer : graph_layers){
                    if(isNodeInLayer(destination_node,layer)){
                        destination_index = temp_index;
                    }
                    temp_index += 1;
                }

                temp_index = 0;
                for(ArrayList<PComponent> layer : graph_layers){
                    if(isNodeInLayer(source_node,layer)){
                        source_index = temp_index;
                    }
                    temp_index += 1;
                }

                if(Math.abs(destination_index - source_index)>1){
                    if(source_index < destination_index){
                        int current_index = source_index + 1;
                        edges_pc_unsorted_.remove(edge);
                        PComponent last_dummy = null;
                        while(current_index < destination_index){

                            //divide edge !!!
                            last_dummy = createNewDummyNode();
                            graph_layers.get(current_index).add(last_dummy);
                            Debugger.error(TAG,"CUTTING...");

                            //make space for new data
                            node_structuralNodeData_pc_sorted_.add(last_dummy);
                            co_node_sorted_.add(new CompositeObject());
                            co_node_text_sorted_.add(new CompositeObject());

                            edges_pc_unsorted_.add(createNewEdge(source_id,last_dummy.getNode().getID(),"ET_CONTROL_TO_DUMMY"));
                            co_edges_unsorted_.add(new ArrayList<CompositeObject>());
                            current_index += 1;
                        }

                        if(last_dummy!=null) {
                            edges_pc_unsorted_.add(createNewEdge(last_dummy.getNode().getID(), destination_id, "ET_CONTROL_FROM_DUMMY"));
                            co_edges_unsorted_.add(new ArrayList<CompositeObject>());
                        }
                    }
                }
            }
        }
    }

    private PComponent createNewEdge(String from_nodeID, String to_nodeID, String type){
        PComponent pc = new PComponent();
        Edge e = new Edge(to_nodeID, from_nodeID, type);
        pc.addEdge(e);
        return pc;
    }

    private PComponent createNewDummyNode(){
        PComponent pc = new PComponent();

        String new_node_id = "dummy_n" + dummy_node_id;
        dummy_node_id += 1;

        Node n = new Node(new_node_id);
        pc.addNode(n);

        StructuralNodeData snd = new StructuralNodeData(new_node_id);
        snd.setType("DUMMY_NODE");
        pc.addStructuralNodeData(snd);


        return pc;
    }

    private boolean isEdgeOverMultipleLayers(PComponent edge, ArrayList<ArrayList<PComponent>> graph_layers){
        //get both nodes from edge
        String destination_id = getEdgeDestinationID(edge);
        String source_id = getEdgeSourceID(edge);

        if((destination_id != null) && (source_id != null)){
            //get nodes
            PComponent destination_node = getStructuralNodeDataByNodeID(destination_id);
            PComponent source_node = getStructuralNodeDataByNodeID(source_id);
            if((destination_node != null) && (source_node != null)){
                int destination_index = 0;
                int source_index = 0;

                //find layer index of destination node
                int temp_index = 0;
                for(ArrayList<PComponent> layer : graph_layers){
                    if(isNodeInLayer(destination_node,layer)){
                        destination_index = temp_index;
                    }
                    temp_index += 1;
                }

                temp_index = 0;
                for(ArrayList<PComponent> layer : graph_layers){
                    if(isNodeInLayer(source_node,layer)){
                        source_index = temp_index;
                    }
                    temp_index += 1;
                }

                if(Math.abs(destination_index - source_index)>1){
                    return true;
                }

            }
        }

        return false;
    }

    private int getGraphLayerIndexOfNode(PComponent pc, ArrayList<ArrayList<PComponent>> graph_layers){
        int index = 0;
        for(ArrayList<PComponent> layer : graph_layers){
            if(isNodeInLayer(pc,layer)){
                return index;
            }
            index += 1;
        }
        return -1;
    }

    private boolean isNodeInLayer(PComponent pc, ArrayList<PComponent> pc_list){
        String node_id = getNodesID(pc);
        if(node_id == null){
            return false;
        }
        for(PComponent pc_l : pc_list){
            String l_node_id = getNodesID(pc_l);
            if(l_node_id != null){
                if(node_id.equalsIgnoreCase(l_node_id)){
                    return true;
                }
            }
        }
        return false;
    }

    private int getIndexOfNodeInLayer(PComponent pc, ArrayList<PComponent> pc_list){
        String node_id = getNodesID(pc);
        if(node_id == null){
            return -1;
        }
        int index = 0;
        for(PComponent pc_l : pc_list){
            String l_node_id = getNodesID(pc_l);
            if(l_node_id != null){
                if(node_id.equalsIgnoreCase(l_node_id)){
                    return index;
                }
            }
            index += 1;
        }
        return -1;
    }


    private ArrayList<PComponent> getNodesNextNodes(PComponent node){
        ArrayList<PComponent> nodes = new ArrayList<PComponent>();

        //search for all nodes... iterate trough all edges
        for(PComponent pc_edge : edges_pc_unsorted_){
            if(nodeIsSource(pc_edge,node)){
                //get edge's destination node...
                String dest_id = getEdgeDestinationID(pc_edge);
                if(dest_id != null) {
                    PComponent new_node = getStructuralNodeDataByNodeID(dest_id);
                    if(new_node != null){
                        nodes.add(new_node);
                    }
                }
            }
        }
        return nodes;
    }

    private ArrayList<PComponent> getNodesPreviousNodes(PComponent node){
        ArrayList<PComponent> nodes = new ArrayList<PComponent>();

        //search for all nodes... iterate trough all edges
        for(PComponent pc_edge : edges_pc_unsorted_){
            if(nodeIsDestination(pc_edge,node)){
                //get edge's destination node...
                String source_id = getEdgeSourceID(pc_edge);
                if(source_id != null) {
                    PComponent new_node = getStructuralNodeDataByNodeID(source_id);
                    if(new_node != null){
                        nodes.add(new_node);
                    }
                }
            }
        }
        return nodes;
    }

    private boolean nodeIsSource(PComponent edge, PComponent node){
        String node_id = getNodesID(node);
        if(node_id ==null){
            return false;
        }
        String edge_source = getEdgeSourceID(edge);
        if(edge_source ==null){
            return false;
        }
        if(node_id.equalsIgnoreCase(edge_source)){
            return true;
        }
        return false;
    }

    private boolean nodeIsDestination(PComponent edge, PComponent node){
        String node_id = getNodesID(node);
        if(node_id ==null){
            return false;
        }
        String edge_destination = getEdgeDestinationID(edge);
        if(edge_destination ==null){
            return false;
        }
        if(node_id.equalsIgnoreCase(edge_destination)){
            return true;
        }
        return false;
    }

    private String getEdgeSourceID(PComponent edge){
        if(edge.hasEdge()) {
            Edge e = edge.getEdge();
            if (e.hasSourceNodeID()) {
                return e.getSourceNodeID();
            }
        }
        return null;
    }

    private String getEdgeDestinationID(PComponent edge){
        if(edge.hasEdge()) {
            Edge e = edge.getEdge();
            if (e.hasDestinationNodeID()) {
                return e.getDestinationNodeID();
            }
        }
        return null;
    }

    //boolean magic
    private boolean isBranchBetweenDepths(Branch b, int startDepth, int endDepth){
        int b_start_depth = b.getDistance();
        int b_end_depth = b_start_depth + b.getPComponentCount() - 1;

        //check if both depths do not overlap !!! and inverse the decision...
        boolean start_in = (b_start_depth <= endDepth) && (b_start_depth >= startDepth);
        boolean end_in = (b_end_depth <= endDepth) && (b_end_depth >= startDepth);

        boolean inside = start_in || end_in;

        return inside;
    }

    private int getVerticalOverlappingBranchCount(int startDepth, int endDepth){
        int count = 0;
        for(Branch b : linear_branches_unsorted){
            if(isBranchBetweenDepths(b,startDepth,endDepth)){
                count += 1;
            }
        }
        Log.e(TAG,"vertical overlapings: " + count);
        return count;
    }

    private ArrayList<Branch> getOverlappingBranches(int startDepth, int endDepth){
        ArrayList<Branch> overlapping_branches = new ArrayList<Branch>();
        //iterate trough the root branch, so we assure correct order...

        if(root == null){
            return overlapping_branches;
        }

        //used branches
        ArrayList<Branch> used_branches = new ArrayList<Branch>();

        //temporary store next branches till they get current branches
        ArrayList<Branch> next_branches = new ArrayList<Branch>();

        //loop trough all branches...
        ArrayList<Branch> current_branches = new ArrayList<Branch>();
        current_branches.add(root);

        boolean work = true;
        while(work){
            next_branches = new ArrayList<Branch>();
            for(Branch b : current_branches){
                if(!containsBranch(used_branches,b)) {
                    used_branches.add(b);

                    if(isBranchBetweenDepths(b,startDepth,endDepth)){
                        overlapping_branches.add(b);
                    }

                    //add next branches
                    ArrayList<Branch> temp_branches = b.getBranches();
                    for(Branch t_b : temp_branches){
                        next_branches.add(t_b);
                    }
                }
            }
            if(next_branches.size() == 0){
                work = false;
            }
            current_branches = next_branches;
        }
        return overlapping_branches;
    }

    private void debugPComponent(PComponent pc){
        if(pc.hasStructuralNodeData()){
            StructuralNodeData snd = pc.getStructuralNodeData();
            if(snd.hasID()){
                Log.e(TAG,"    getID: " + snd.getID());
            }
            if(snd.hasType()){
                Log.e(TAG,"    getType: " + snd.getType());
            }
        }
    }

    private Branch createLinearBranch(PComponent start_node){
        Branch b = new Branch();
        b.addElement(start_node);


        ArrayList<PComponent> next_nodes = new ArrayList<PComponent>();
        ArrayList<PComponent> current_nodes = new ArrayList<PComponent>();

        //get next nodes of start_node
        //get the nodes next nodes
        int node_index = getStructuralNodeDataIndexByNode(start_node);
        if (node_index >= 0) {
            ArrayList<PComponent> next_nodes_nodes = next_nodes_pc_.get(node_index);
            //append em to the next_nodes list
            for (PComponent pc_n : next_nodes_nodes) {
                current_nodes.add(pc_n);
            }
        }

        boolean work = false;
        if(current_nodes.size() > 0){
            work = true;
        }

        while(work){

            if(current_nodes.size() == 1){
                PComponent pc_node = current_nodes.get(0);

                //exclude joins...
                if(!isJoinNode(pc_node)){
                    b.addElement(pc_node);

                    next_nodes.clear();
                    int next_node_index = getStructuralNodeDataIndexByNode(pc_node);
                    if (next_node_index >= 0) {
                        ArrayList<PComponent> next_nodes_nodes = next_nodes_pc_.get(next_node_index);
                        //append em to the next_nodes list
                        for (PComponent pc_n : next_nodes_nodes) {
                            next_nodes.add(pc_n);
                        }
                    }
                    current_nodes = next_nodes;
                }
                else{
                    work = false;
                }
            }
            else{
                //more nodes means we have a split... or an end if we have 0
                work = false;
            }

        }

        return b;
    }


    private boolean areConnected(PComponent pc_source, PComponent pc_destination){
        //get the node's IDs
        String pc_source_ID = getNodesID(pc_source);
        String pc_destination_ID = getNodesID(pc_destination);


        for (PComponent pc_edge : edges_pc_unsorted_) {
            if(pc_edge.hasEdge()){
                Edge e = pc_edge.getEdge();
                if(e.hasSourceNodeID() && e.hasDestinationNodeID()){
                    String sourceID = e.getSourceNodeID();
                    String destinationID = e.getDestinationNodeID();
                    if((sourceID != null) && (destinationID != null)){
                        //compair the IDs
                        if(sourceID.equalsIgnoreCase(pc_source_ID) &&
                                destinationID.equalsIgnoreCase(pc_destination_ID)){
                            return true;
                        }
                    }
                    else{
                        Log.e(TAG,"broken edge, has source or destination... but might be null");
                    }
                }
                else{
                    Log.e(TAG,"broken edge, has source or destination... but not both");
                }
            }
        }
        return false;
    }


    private boolean isSplitNode(PComponent pc){
        //check if pc is a split...
        if(pc.hasStructuralNodeData()) {
            if (pc.getStructuralNodeData().hasType()) {
                String type = pc.getStructuralNodeData().getType();
                if (type != null) {
                    if (type.equalsIgnoreCase("NT_XOR_SPLIT") ||
                            type.equalsIgnoreCase("NT_AND_SPLIT")) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean isJoinNode(PComponent pc){
        //check if pc is a split...
        if(pc.hasStructuralNodeData()) {
            if (pc.getStructuralNodeData().hasType()) {
                String type = pc.getStructuralNodeData().getType();
                if (type != null) {
                    if (type.equalsIgnoreCase("NT_XOR_JOIN") ||
                            type.equalsIgnoreCase("NT_AND_JOIN")) {
                        return true;
                    }
                }
            }
        }
        return false;
    }


    private boolean isStartNode(PComponent pc){
        //check if pc is a split...
        if(pc.hasStructuralNodeData()) {
            if (pc.getStructuralNodeData().hasType()) {
                String type = pc.getStructuralNodeData().getType();
                if (type != null) {
                    if (type.equalsIgnoreCase("NT_STARTFLOW")) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean isEndNode(PComponent pc){
        //check if pc is a split...
        if(pc.hasStructuralNodeData()) {
            if (pc.getStructuralNodeData().hasType()) {
                String type = pc.getStructuralNodeData().getType();
                if (type != null) {
                    if (type.equalsIgnoreCase("NT_ENDFLOW")) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private PComponent getEndNode(){
        for (PComponent pc : node_structuralNodeData_pc_sorted_) {
            if(isEndNode(pc)){
                return pc;
            }
        }
        return null;
    }

    private PComponent getStartNode(){
        for (PComponent pc : node_structuralNodeData_pc_sorted_) {
            if(isStartNode(pc)){
                return pc;
            }
        }
        return null;
    }



    private PComponent getSplitNodesJoinNode(PComponent pc){
        //check if pc is a split...
        if(isSplitNode(pc)) {
            //if we pass the isSplitNode test then we know we have a structural node...

            //now check if we have a correspondingBlockNodeID which is the join...
            if (pc.getStructuralNodeData().hasCorrespondingBlockNodeID()) {
                String cBlockNodeID = pc.getStructuralNodeData().getCorrespondingBlockNodeID();
                if (cBlockNodeID != null) {
                    //find the Node by its ID

                    PComponent pc_blockNode = getPComponentByNodeID(cBlockNodeID);
                    if(pc_blockNode != null){
                        //check if it is a join node...
                        if(isJoinNode(pc_blockNode)){
                            return pc_blockNode;
                        }
                    }
                }
            }
        }
        return null;
    }

    public PComponent getPComponentByNodeID(String nodeID) {
        for (PComponent pc : node_structuralNodeData_pc_sorted_) {
            if (pc.hasNode()) {
                if (pc.getNode().getID().equalsIgnoreCase(nodeID)) {
                    return pc;
                }
            }
        }
        return null;
    }

    private String getNodesID(PComponent pc){
        String id = null;
        if(pc != null) {
            if (pc.hasNode()) {
                if (pc.getNode() != null) {
                    if (pc.getNode().hasID()) {
                        id = pc.getNode().getID();
                    }
                }
            }
        }
        return id;
    }


    //get the amount of nodes verticaly at pc's position
    private int getNodeCountByDepth(PComponent pc){
        int depth_count = 1;


        int pc_index = getStructuralNodeDataIndexByNode(pc);
        if(pc_index < 0){
            return depth_count;
        }

        ArrayList<Integer> temp_results = new ArrayList<Integer>();
        int branch_count = 0;
        for(PComponent next_pc : next_nodes_pc_.get(pc_index)){
            temp_results.add(getNodeCountByDepth(next_pc));
            branch_count += 1;
        }


        //get the max value of the results...
        for(Integer temp_result : temp_results){
            int val = temp_result.intValue();
            depth_count = Math.max(branch_count + val - 1,depth_count);
        }

        return depth_count;
    }


    private int getNodeXPosFromRoot(PComponent pc){
        //we asume that by now the root is the first element in the
        //node_structuralNodeData_pc_sorted_ list...
        return getNodeXPos(pc, node_structuralNodeData_pc_sorted_.get(0));
    }

    //get pc's horizontal position
    //check if we need more space because some branches are shorter or longer ...
    private int getNodeXPos(PComponent pc, PComponent start_pc){
        int xPos = -1;

        //check if the list even contains the components we are looking for...
        int start_pc_index = getStructuralNodeDataIndexByNode(start_pc);
        if(start_pc_index < 0){
            return -1;
        }

        //check if start node has a node id and if it is the same we are looking for...
        String start_nodeID;
        String nodeID;
        if(start_pc.hasNode() && pc.hasNode()){
            if(start_pc.getNode().hasID() && pc.getNode().hasID()){
                start_nodeID = start_pc.getNode().getID();
                nodeID = pc.getNode().getID();
            }
            else{
                return -1;
            }
        }
        else{
            return -1;
        }
        if((start_nodeID != null) && (nodeID != null)){
            if(start_nodeID.equalsIgnoreCase(nodeID)){
                //well start and end is same so lenght is 0
                return 1;
            }
        }



        int pc_index = getStructuralNodeDataIndexByNode(pc);
        if(pc_index < 0){
            return -1;
        }

        ArrayList<Integer> temp_results = new ArrayList<Integer>();
        for(PComponent next_pc : next_nodes_pc_.get(start_pc_index)){
            int result = getNodeXPos(pc, next_pc);
            if(result > 0){
                temp_results.add(1 + result);
            }
            else{
                temp_results.add(result);
            }
        }


        //get the max value of the results...
        for(Integer temp_result : temp_results){
            int val = temp_result.intValue();
            xPos = Math.max(val,xPos);
        }

        return xPos;
    }




    private int getStructuralNodeDataIndexByNode(PComponent pc) {
        if(pc.hasNode()){
            if(pc.getNode().hasID()){
                String nodeID = pc.getNode().getID();
                if(nodeID != null){

                    int index = 0;
                    for(PComponent pc_ : node_structuralNodeData_pc_sorted_){
                        if(pc_.hasNode()) {
                            if (pc_.getNode().hasID()) {
                                String nodeID2 = pc_.getNode().getID();
                                if (nodeID2 != null) {
                                    if(nodeID.equalsIgnoreCase(nodeID2)){
                                        return index;
                                    }
                                }
                            }
                        }
                        index += 1;
                    }

                }
            }
        }

        return -1;
    }

    private int getStructuralNodeDataIndexByNodeID(String nodeID) {

        if(nodeID != null){

            int index = 0;
            for(PComponent pc_ : node_structuralNodeData_pc_sorted_){
                if(pc_.hasNode()) {
                    if (pc_.getNode().hasID()) {
                        String nodeID2 = pc_.getNode().getID();
                        if (nodeID2 != null) {
                            if(nodeID.equalsIgnoreCase(nodeID2)){
                                return index;
                            }
                        }
                    }
                }
                index += 1;
            }

        }

        return -1;
    }

    private PComponent getStructuralNodeDataByNodeID(String nodeID) {

        if(nodeID != null){

            int index = 0;
            for(PComponent pc_ : node_structuralNodeData_pc_sorted_){
                if(pc_.hasNode()) {
                    if (pc_.getNode().hasID()) {
                        String nodeID2 = pc_.getNode().getID();
                        if (nodeID2 != null) {
                            if(nodeID.equalsIgnoreCase(nodeID2)){
                                return pc_;
                            }
                        }
                    }
                }
                index += 1;
            }

        }

        return null;
    }








    //everything below is not needed...
    private int getBranchLength(int branchID) {
        int length = 0;

        //first get the branchIndex from the branchID
        int index = getBranchIDIndex(branchID);
        if (index < 0) {
            return length;
        }

        //now check the branches for their length
        //get the branch to start with
        ArrayList<ArrayList<PComponent>> branch = branches_.get(index);
        int branch_part_index = 0;
        for (ArrayList<PComponent> branch_part : branch) {
            length += branch_part.size();
            for (ArrayList<Integer> branch_next_branchID_part : branches_next_branchID_.get(index)) {
                length += getBranchLength(branch_next_branchID_part.get(branch_part_index).intValue());
            }
            branch_part_index += 1;
        }
        return length;
    }

    private int getBranchHeight(int branchID) {
        int height = 0;

        //first get the branchIndex from the branchID
        int index = getBranchIDIndex(branchID);
        if (index < 0) {
            return height;
        }

        height = 1;

        //now check the branches for their height
        //get the branch to start with
        ArrayList<ArrayList<Integer>> branch_next_branchID = branches_next_branchID_.get(index);
        for (ArrayList<Integer> branch_next_branchID_part : branch_next_branchID) {
            int branches = 0;
            int temp_height = 0;
            for (Integer next_branchID : branch_next_branchID_part) {
                branches += 1;
                temp_height = getBranchHeight(next_branchID.intValue());
            }
            if (temp_height > branches) {
                height = Math.max(height, temp_height);
            } else if (branches > 0) {
                height = branches;
            }
        }

        return height;
    }

    //called in thread of async xmlparser
    private void generateSortedStructure() {
        ArrayList<PComponent> nodes_pc = new ArrayList<PComponent>();
        ArrayList<PComponent> structuralNodeData_pc = new ArrayList<PComponent>();

        //sort in all NODES
        for (PComponent pc : componentList_) {
            if (pc.hasNode()) {
                nodes_pc.add(pc);
            }
        }

        //sort in all structuralNodeDatas by NODE's ID
        for (PComponent node : nodes_pc) {
            String node_id = node.getNode().getID();
            //find the structuralDataNode by ID

            PComponent structuralNodeData = null;

            for (PComponent pc : componentList_) {
                if (pc.hasStructuralNodeData()) {
                    if (pc.getStructuralNodeData().getID().equalsIgnoreCase(node_id)) {
                        structuralNodeData = pc;
                        break;
                    }
                }
            }
            //might contain null!!! (xml specs probably forbid it but we check it later...)
            structuralNodeData_pc.add(structuralNodeData);
        }

        //store them so we can ask later what node has which structural data and visa versa
        nodes_pc_unsorted_ = nodes_pc;
        structuralNodeData_pc_unsorted_ = structuralNodeData_pc;


        //now sort it so we can build branches and stuff..
        int sorted_nodes = 0;
        int nodes_to_sort = nodes_pc_unsorted_.size();

        int topology = 0;

        while (sorted_nodes < nodes_to_sort) {
            for (PComponent pc_snd : structuralNodeData_pc_unsorted_) {
                if (pc_snd != null) {
                    if (pc_snd.hasStructuralNodeData()) {
                        StructuralNodeData snd = pc_snd.getStructuralNodeData();
                        if (snd.hasTopologicalID()) {
                            try {
                                int topologicalID = Integer.parseInt(snd.getTopologicalID());
                                if (topologicalID == topology) {
                                    structuralNodeData_pc_sorted_.add(pc_snd);
                                    nodes_pc_sorted_.add(getStructuralNodeDatasNode(pc_snd));
                                    sorted_nodes += 1;
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "generateSortedStructure()  StructuralNodeData's TopologicalID is a malformed integer...");
                            }
                        } else {
                            Log.e(TAG, "generateSortedStructure()  StructuralNodeData has no TopologicalID...");
                        }
                    } else {
                        Log.e(TAG, "generateSortedStructure()  StructuralNodeData PComponent does not contain any StructuralNodeData...");
                    }
                } else {
                    Log.e(TAG, "generateSortedStructure()  StructuralNodeData PComponent is null...");
                }
            }
            topology += 1;

            //if we have not enough structuralNodeDatas we might encounter infinit loop...
            if ((nodes_to_sort * 3) < topology) {
                //if we have checked 3 times higher topological id than we actually have nodes
                //we bail out
                Log.e(TAG, "generateSortedStructure()  found malformed Nodes, bailing out of sorting...");
                break;
            }

        }


        //lets join Nodes and StructuralNodeData
        for (int i = 0; i < nodes_pc_sorted_.size(); i++) {
            PComponent n_snd = nodes_pc_sorted_.get(i);
            n_snd.addStructuralNodeData(structuralNodeData_pc_sorted_.get(i).getStructuralNodeData());
            node_structuralNodeData_pc_sorted_.add(n_snd);
        }
        //now PComponents in node_structuralNodeData_pc_sorted_ contain
        // a Node and a StructuralNodeData


        //now we need to create branches
        for (PComponent pc : node_structuralNodeData_pc_sorted_) {
            try {
                int bID = Integer.parseInt(pc.getStructuralNodeData().getBranchID());
                int indexBID = getBranchIDIndex(bID);
                if (indexBID > -1) {
                    int size = branches_.get(indexBID).size();
                    //if no list exsists create one !!!
                    if (size == 0) {
                        branches_.get(indexBID).add(new ArrayList<PComponent>());
                        branches_next_branchID_.get(indexBID).add(new ArrayList<Integer>());
                        size = 1;
                    }
                    branches_.get(indexBID).get(size - 1).add(pc);
                } else {
                    branchID_.add(new Integer(bID));
                    branches_.add(new ArrayList<ArrayList<PComponent>>());
                    branches_next_branchID_.add(new ArrayList<ArrayList<Integer>>());
                }
            } catch (Exception e) {
                Log.e(TAG, "generateSortedStructure()  StructuralNodeData's BranchID is a malformed integer......");
            }
        }

        //now we have 2 arrays
        //1 with a branch ID
        //and the other one contains the corresponding branch parts
        //
        //third array is initialized with correct size but does not contain any data yet...
        //
        /*
                     -
                    n50
                    n40
             -       -       -               -               -
             n1     n39     n44             n42             n46
             -      n33      -       -       -       -       -       -
            <n0,    n32,    n45,    n48,    n41,    n47,    n45,    n49>
             -       -       -       -       -       -       -       -


            <0,     1,      10,     13,     9,      12,     11,     14>



             -       -       -               -               -
             /       /       /               /               /
             -       -       -       -       -       -       -       -
            </,      /,      /,      /,      /,      /,      /,      />
             -       -       -       -       -       -       -       -

         */


        //fill third array which tells which branch follows after which end of the branches...
        //we only need to check the first elements of each branch...

        int branchIndex = 0;
        for (ArrayList<ArrayList<PComponent>> branch : branches_) {
            //get first element
            PComponent first_pc = branch.get(0).get(0);
            if (first_pc.hasStructuralNodeData()) {
                StructuralNodeData snd = first_pc.getStructuralNodeData();
                if (snd.hasType()) {
                    //we dont need to connect start to other nodes...
                    if (!snd.getType().equalsIgnoreCase("NT_STARTFLOW")) {
                        if (snd.hasSplitNodeID()) {
                            String splitNodeID = snd.getSplitNodeID();
                            PComponent splitNode = getPComponentByNodeID(splitNodeID);

                            if (splitNode == null) {
                                Log.e(TAG, "splitNode is null !!! snd:splitNodeID:" + splitNodeID);

                                if (first_pc.hasNode()) {
                                    if (first_pc.getNode().hasID()) {
                                        Log.e(TAG, "node:NodeID:" + first_pc.getNode().getID());
                                    }
                                }
                                continue;
                            }

                            //get the branchID and branchPartIndex of the splitNode
                            if (splitNode.hasStructuralNodeData()) {
                                StructuralNodeData snd_splitNode = splitNode.getStructuralNodeData();
                                if (snd_splitNode.hasBranchID()) {
                                    try {
                                        int branchID = Integer.parseInt(snd_splitNode.getBranchID());
                                        int nodesBranchPartIndex = getNodesBranchPartIndex(splitNode);

                                        //insert branchIndex into third array...
                                        //use branchID and nodesBranchPartIndex

                                        int branchI = getBranchIDIndex(branchID);

                                        if ((nodesBranchPartIndex > -1) && branchI > -1) {
                                            branches_next_branchID_.get(branchI).get(nodesBranchPartIndex).add(branchID_.get(branchIndex));
                                        }

                                    } catch (Exception e) {
                                        Log.e(TAG, "generateSortedStructure()  StructuralNodeData's BranchID is a malformed integer......(2)");
                                    }

                                }
                            }

                        }
                    }
                }
            }


            branchIndex += 1;
        }


        //final result
        /*


                     -
                    n50
                    n40
             -       -       -               -               -
             n1     n39     n44             n42             n46
             -      n33      -       -       -       -       -       -
            <n0,    n32,    n45,    n48,    n41,    n47,    n45,    n49>
             -       -       -       -       -       -       -       -


            <0,     1,      10,     13,     9,      12,     11,     14>


                     -
                     /
             -       -       -               -               -
             /      11       /               /               /
             -      10       -       -       -       -       -       -
            <1,      9,     13,      /,     12,      /,     14,      />
             -       -       -       -       -       -       -       -



        */

        Log.e(TAG, "generateSortedStructure() DONE!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
    }

    //PComponent's Structural data contains a BranchID,
    //use the Id to get the branchIDIndex...
    public int getNodesBranchPartIndex(PComponent pc) {
        if (pc.hasStructuralNodeData()) {
            StructuralNodeData snd = pc.getStructuralNodeData();
            if (snd.hasBranchID()) {
                try {
                    int branchID = Integer.parseInt(snd.getBranchID());
                    int branchIDIndex = getBranchIDIndex(branchID);
                    if (branchIDIndex > -1) {
                        ArrayList<ArrayList<PComponent>> branch_parts = branches_.get(branchIDIndex);

                        int branch_part_index = -1;
                        for (ArrayList<PComponent> branch_part : branch_parts) {
                            branch_part_index += 1;
                            for (PComponent branch_part_pc : branch_part) {
                                //compair IDs
                                if (pc.hasNode() && branch_part_pc.hasNode()) {
                                    if (pc.getNode().getID().equalsIgnoreCase(branch_part_pc.getNode().getID())) {
                                        return branch_part_index;
                                    }
                                } else {
                                    Log.e(TAG, "getNodesBranchPartIndex(PComponent pc)  no nodes ...");
                                }
                            }
                        }
                    } else {
                        Log.e(TAG, "getNodesBranchPartIndex(PComponent pc)  branchIDIndex could not be found ...");
                    }
                } catch (Exception e) {
                    Log.e(TAG, "getNodesBranchPartIndex(PComponent pc)  StructuralNodeData contains malformed BranchID...");
                }
            } else {
                Log.e(TAG, "getNodesBranchPartIndex(PComponent pc)  no BranchID in StructuralNodeData...");
            }
        } else {
            Log.e(TAG, "getNodesBranchPartIndex(PComponent pc)  no StructuralNodeData in PComponent...");
        }
        return -1;
    }

    public int getBranchIDIndex(int branchID) {
        int i = -1;
        for (Integer index : branchID_) {
            i += 1;
            if (index.intValue() == branchID) {
                return i;
            }
        }
        return -1;
    }

    public PComponent getNodesStructuralNodeData(PComponent pc) {
        int index = getNodesIndex(pc);
        if (index > -1) {
            return structuralNodeData_pc_unsorted_.get(index);
        }
        return null;
    }

    public int getNodesIndex(PComponent pc) {
        Node n = pc.getNode();
        if (nodes_pc_unsorted_.size() > -1) {
            int index = 0;
            for (PComponent pc_n : nodes_pc_unsorted_) {
                if (n.getID().equalsIgnoreCase(pc_n.getNode().getID())) {
                    return index;
                }
                index += 1;
            }
        }
        return -1;
    }

    public PComponent getStructuralNodeDatasNode(PComponent pc) {
        int index = getStructuralNodeDatasIndex(pc);
        if (index > -1) {
            return nodes_pc_unsorted_.get(index);
        }
        return null;
    }

    public int getStructuralNodeDatasIndex(PComponent pc) {
        StructuralNodeData snd = pc.getStructuralNodeData();
        if ((structuralNodeData_pc_unsorted_.size() > -1) && (snd != null)) {
            int index = 0;
            for (PComponent pc_snd : structuralNodeData_pc_unsorted_) {
                StructuralNodeData snd2 = pc_snd.getStructuralNodeData();
                if (snd2 != null) {
                    if (snd.getID().equalsIgnoreCase(snd2.getID())) {
                        return index;
                    }
                }
                index += 1;
            }
        }
        return -1;
    }

    public List<PComponent> getPComponents() {
        return componentList_;
    }


}
