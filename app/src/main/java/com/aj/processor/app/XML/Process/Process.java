package com.aj.processor.app.XML.Process;


import android.util.Log;

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


    private ArrayList<PComponent> used_start_nodes_for_linear_branches = new ArrayList<PComponent>();
    private ArrayList<PComponent> used_end_nodes_for_linear_branches = new ArrayList<PComponent>();
    private ArrayList<Branch> linear_branches_unsorted = new ArrayList<Branch>();

    private Branch root = null;





    //UNUSED
    //multi dimensional LIST
    private ArrayList<Integer> branchID_ = new ArrayList<Integer>();
    private ArrayList<ArrayList<ArrayList<PComponent>>> branches_ = new ArrayList<ArrayList<ArrayList<PComponent>>>();
    private ArrayList<ArrayList<ArrayList<Integer>>> branches_next_branchID_ = new ArrayList<ArrayList<ArrayList<Integer>>>();
    //UNUSED END

    //dummy constructor
    public Process(List<PComponent> pComponents) {
        componentList_ = pComponents;
        //generateSortedStructure();
        generateSortedStructure_v2();

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
        if(root == null){
            return;
        }

        //POS VARS
        double y_pos = 0.0;
        double x_pos = 0.0;
        //END POS VARS


        //used nodes...
        ArrayList<PComponent> used_nodes = new ArrayList<PComponent>();

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

            int branch_count = 0;
            int current_branch_count = 0;

            for(Branch b : current_branches){

                branch_count = b.getOverlappingBranchCount();

                if(!used_branches.contains(b)) {
                    used_branches.add(b);

                    int b_depth = b.getDepth();
                    int b_branch_id = b.getLocalBranchID();
                    int b_distance = b.getDistance(); // x pos

                    y_pos =  ((double) (current_branch_count - 1)) - (((double) (branch_count - 1)) / 2.0);
                    current_branch_count += 1;

                    Log.e(TAG,"b_depth: " + b_depth);
                    Log.e(TAG,"b_branch_id: " + b_branch_id);
                    Log.e(TAG,"b_distance: " + b_distance);
                    Log.e(TAG,"y_pos: " + y_pos);
                    Log.e(TAG,"branch_count: " + branch_count);

                    x_pos = b_distance;

                    //loop trough all PComponents of b
                    for (PComponent pc : b.getElements()) {

                        if(!used_nodes.contains(pc)) {
                            used_nodes.add(pc);

                            //get the nodes index
                            int co_index = getStructuralNodeDataIndexByNode(pc);

                            CompositeObject test_node = gen3DNode(ow, pc);
                            if (test_node != null) {
                                test_node.getPositation().set_position(x_pos * 4.0, -y_pos * 4.0, 0.0);
                                test_node.getPositation().set_scale(
                                        1.8 * test_node.getPositation().get_scale().x(),
                                        1.8 * test_node.getPositation().get_scale().y(),
                                        1.8 * test_node.getPositation().get_scale().z());

                                //sort the node in...
                                co_node_sorted_.set(co_index, test_node);
                            }

                            CompositeObject text_co = gen3DNodeText(ow, pc);
                            if (text_co != null) {
                                text_co.getPositation().set_position(0.5f + x_pos * 4.0, 0.0f - y_pos * 4.0, 0.3f);

                                //sort the node text in
                                co_node_text_sorted_.set(co_index, text_co);
                            }

                            x_pos += 1;
                        }
                    }



                    //get overlapping branches


                    ArrayList<Branch> o_branches = getOverlappingBranches(b_distance,
                            b_distance + b.getElements().size() - 1);
                    for(Branch t_b : o_branches){
                        next_branches.add(t_b);
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
        Log.e(TAG,"3D Objects generation done");
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
                            line_list.add(ow.loadLineObject("line control",p1,p2,new Vector4(0.0f,1.0f,0.0f,1.0f)));
                            Log.e(TAG,"p1: " + p1.x() + " " + p1.y() + " " + p1.z() +
                                  "    p2: " + p2.x() + " " + p2.y() + " " + p2.z());
                            Log.e(TAG,"Line created!");
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
                    if(type.equalsIgnoreCase("NT_NORMAL")){
                        return ow.loadModelObject("node normal", "processComponents/Node/node_normal.obj", true);
                    }

                    if(type.equalsIgnoreCase("NT_XOR_JOIN")){
                        CompositeObject co = ow.loadModelObject("node conditional", "processComponents/Node/node_conditional.obj", true);
                        co.getPositation().set_scale(-1.0,1.0,1.0);
                        return co;
                    }

                    if(type.equalsIgnoreCase("NT_XOR_SPLIT")){
                        CompositeObject co = ow.loadModelObject("node conditional", "processComponents/Node/node_conditional.obj", true);
                        return co;
                    }

                    if(type.equalsIgnoreCase("NT_AND_JOIN")){
                        CompositeObject co = ow.loadModelObject("node split", "processComponents/Node/node_parallel.obj", true);
                        co.getPositation().set_scale(-1.0,1.0,1.0);
                        return co;
                    }

                    if(type.equalsIgnoreCase("NT_AND_SPLIT")){
                        CompositeObject co = ow.loadModelObject("node join", "processComponents/Node/node_parallel.obj", true);
                        return co;
                    }
                }
            }
        }
        //create standard node...
        return ow.loadModelObject("node normal", "processComponents/Node/node_normal.obj", true);
    }

    private CompositeObject gen3DNodeText(ObjectWorld ow, PComponent pc){
        if(pc.hasNode()){
            if(pc.getNode().hasName()){
                String node_name = pc.getNode().getName();
                if(node_name != null) {
                    //skip empty name... if somebody was so smart to enter more than
                    //one space there... it's not my fault xD
                    if (!node_name.equals("") && !node_name.equals(" ")){
                        CompositeObject text_co = ow.loadModelObject("test_text", "text_model.obj", false);
                        text_co.getModel().get_meshs().get(0).get_material().setDiffuseText(
                                "FF00FF_TEXT_BG.png", node_name, 100.0f, 0, 255, 0
                        );
                        return text_co;
                    }
                }
            }
            if(pc.getNode().hasID()){
                String node_id = pc.getNode().getID();
                if(node_id != null){
                    CompositeObject text_co = ow.loadModelObject("test_text", "text_model.obj", false);
                    text_co.getModel().get_meshs().get(0).get_material().setDiffuseText(
                            "FF00FF_TEXT_BG.png", node_id, 100.0f, 0, 255, 0
                    );
                    return text_co;
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
            b.setOverlappingBranchCount(
                getVerticalOverlappingBranchCount(
                        b.getDistance(),
                        b.getDistance() + b.getElements().size() - 1
                )
            );
        }

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

                if(!used_branches.contains(b)) {
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

        //add the start node to the used start nodes
        used_end_nodes_for_linear_branches.add(start_node);


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
        if(pc.hasNode()){
            if(pc.getNode().hasID()){
                id = pc.getNode().getID();
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



