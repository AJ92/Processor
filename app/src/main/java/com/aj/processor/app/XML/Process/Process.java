package com.aj.processor.app.XML.Process;



import android.util.Log;

import com.aj.processor.app.XML.Process.Components.Edge;
import com.aj.processor.app.XML.Process.Components.Node;
import com.aj.processor.app.XML.Process.Components.StructuralNodeData;
import com.aj.processor.app.graphics.world.ObjectWorld;

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
    private List<PComponent> node_structuralNodeData_pc_sorted_ = new ArrayList<PComponent>();
    //corresponding following nodes
    private ArrayList<ArrayList<PComponent> > next_nodes_pc_ = new ArrayList<ArrayList<PComponent> >();


    private List<PComponent> edges_pc_unsorted_ = new ArrayList<PComponent>();








//UNUSED
    //multi dimensional LIST
    private ArrayList<Integer> branchID_ = new ArrayList<Integer>();
    private ArrayList<ArrayList<ArrayList<PComponent> > > branches_ = new ArrayList<ArrayList<ArrayList<PComponent> > >();
    private ArrayList<ArrayList<ArrayList<Integer> > > branches_next_branchID_ = new ArrayList<ArrayList<ArrayList<Integer> > >();
//UNUSED END

    //dummy constructor
    public Process(List<PComponent> pComponents){
        componentList_ = pComponents;
        //generateSortedStructure();
        generateSortedStructure_v2();
    }

    //called by openGL's render thread...
    public void generate3dDataObjects(ObjectWorld ow){
        //lets create the 3d data...



    }

    private void generateSortedStructure_v2(){
        ArrayList<PComponent>       nodes_pc = new ArrayList<PComponent>();
        ArrayList<PComponent>       structuralNodeData_pc = new ArrayList<PComponent>();

        //sort in all NODES
        for(PComponent pc : componentList_) {
            if(pc.hasNode()){
                nodes_pc.add(pc);
            }
        }

        //sort in all structuralNodeDatas by NODE's ID
        for(PComponent node : nodes_pc){
            String node_id = node.getNode().getID();
            //find the structuralDataNode by ID

            PComponent structuralNodeData = null;

            for(PComponent pc : componentList_){
                if(pc.hasStructuralNodeData()){
                    if(pc.getStructuralNodeData().getID().equalsIgnoreCase(node_id)){
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

        while(sorted_nodes < nodes_to_sort) {
            for (PComponent pc_snd : structuralNodeData_pc_unsorted_) {
                if(pc_snd != null) {
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
                }
                else {
                    Log.e(TAG, "generateSortedStructure_v2()  StructuralNodeData PComponent is null...");
                }
            }
            topology += 1;

            //if we have not enough structuralNodeDatas we might encounter infinit loop...
            if((nodes_to_sort * 3) < topology){
                //if we have checked 3 times higher topological id than we actually have nodes
                //we bail out
                Log.e(TAG, "generateSortedStructure_v2()  found malformed Nodes, bailing out of sorting...");
                break;
            }

        }



        //lets join Nodes and StructuralNodeData
        for(int i = 0; i < nodes_pc_sorted_.size(); i++){
            PComponent n_snd = nodes_pc_sorted_.get(i);
            n_snd.addStructuralNodeData(structuralNodeData_pc_sorted_.get(i).getStructuralNodeData());
            node_structuralNodeData_pc_sorted_.add(n_snd);

            //fill list with empty lists
            next_nodes_pc_.add(new ArrayList<PComponent>());
        }
        //now PComponents in node_structuralNodeData_pc_sorted_ contain
        // a Node and a StructuralNodeData

        Log.e(TAG, "generateSortedStructure_v2()  pre sorted Nodes and structuralNodeDatas data ...");








        //now sort out edges...
        for(PComponent pc : componentList_){
            if(pc.hasEdge()){
                if(pc.getEdge() != null){
                    edges_pc_unsorted_.add(pc);
                }
                else{
                    Log.e(TAG, "generateSortedStructure_v2()  PComponent says it has an Edge but returns null ...");
                }
            }
        }
        //we have all edges...






        //check if first node is startnode...
        boolean has_start = false;
        PComponent pc_start = node_structuralNodeData_pc_sorted_.get(0);
        if(pc_start != null){
            if(pc_start.hasStructuralNodeData()){
                if(pc_start.getStructuralNodeData() != null){
                    if(pc_start.getStructuralNodeData().hasType()){
                        if(pc_start.getStructuralNodeData().getType() != null){
                            if(pc_start.getStructuralNodeData().getType().equalsIgnoreCase("NT_STARTFLOW")){
                                has_start = true;
                            }
                        }
                    }
                }
            }
        }
        if(!has_start){
            Log.e(TAG, "generateSortedStructure_v2()  no start node ...");
            return;
        }


        //find the following nodes for the node_structuralNodeData_pc_sorted_ list
        //next_nodes_pc_

        int max_depth = 0;

        int index_to_sort_in = 0;
        for(PComponent pc : node_structuralNodeData_pc_sorted_){
            //find following nodes for pc
            if(pc.hasNode()){
                if(pc.getNode() != null){
                    String nodeID = pc.getNode().getID();
                    //find following nodes for nodeID

                    for(PComponent pc_edge : edges_pc_unsorted_){
                        if(pc_edge.getEdge().getSourceNodeID().equalsIgnoreCase(nodeID)){
                            max_depth += 1;

                            //find the destinationID's node and sort it in...

                            String destinationID = pc_edge.getEdge().getDestinationNodeID();

                            for(PComponent pc_dest : node_structuralNodeData_pc_sorted_){
                                if(pc_dest.hasNode()) {
                                    if (pc_dest.getNode() != null) {
                                        if(pc_dest.getNode().getID().equalsIgnoreCase(destinationID)){
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


    }








    //everything below is not needed...

    private int getBranchLength(int branchID){
        int length = 0;

        //first get the branchIndex from the branchID
        int index = getBranchIDIndex(branchID);
        if(index < 0){
            return length;
        }

        //now check the branches for their length
        //get the branch to start with
        ArrayList<ArrayList<PComponent> > branch = branches_.get(index);
        int branch_part_index = 0;
        for(ArrayList<PComponent> branch_part : branch){
            length += branch_part.size();
            for(ArrayList<Integer> branch_next_branchID_part : branches_next_branchID_.get(index)){
                length += getBranchLength(branch_next_branchID_part.get(branch_part_index).intValue());
            }
            branch_part_index += 1;
        }
        return length;
    }

    private int getBranchHeight(int branchID){
        int height = 0;

        //first get the branchIndex from the branchID
        int index = getBranchIDIndex(branchID);
        if(index < 0){
            return height;
        }

        height = 1;

        //now check the branches for their height
        //get the branch to start with
        ArrayList<ArrayList<Integer> > branch_next_branchID = branches_next_branchID_.get(index);
        for(ArrayList<Integer> branch_next_branchID_part : branch_next_branchID){
            int branches = 0;
            int temp_height = 0;
            for(Integer next_branchID : branch_next_branchID_part){
                branches += 1;
                temp_height = getBranchHeight(next_branchID.intValue());
            }
            if(temp_height > branches){
                height = Math.max(height,temp_height);
            }
            else if(branches > 0){
                height = branches;
            }
        }

        return height;
    }

    //called in thread of async xmlparser
    private void generateSortedStructure(){
        ArrayList<PComponent>       nodes_pc = new ArrayList<PComponent>();
        ArrayList<PComponent>       structuralNodeData_pc = new ArrayList<PComponent>();

        //sort in all NODES
        for(PComponent pc : componentList_) {
            if(pc.hasNode()){
                nodes_pc.add(pc);
            }
        }

        //sort in all structuralNodeDatas by NODE's ID
        for(PComponent node : nodes_pc){
            String node_id = node.getNode().getID();
            //find the structuralDataNode by ID

            PComponent structuralNodeData = null;

            for(PComponent pc : componentList_){
                if(pc.hasStructuralNodeData()){
                    if(pc.getStructuralNodeData().getID().equalsIgnoreCase(node_id)){
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

        while(sorted_nodes < nodes_to_sort) {
            for (PComponent pc_snd : structuralNodeData_pc_unsorted_) {
                if(pc_snd != null) {
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
                }
                else {
                    Log.e(TAG, "generateSortedStructure()  StructuralNodeData PComponent is null...");
                }
            }
            topology += 1;

            //if we have not enough structuralNodeDatas we might encounter infinit loop...
            if((nodes_to_sort * 3) < topology){
                //if we have checked 3 times higher topological id than we actually have nodes
                //we bail out
                Log.e(TAG, "generateSortedStructure()  found malformed Nodes, bailing out of sorting...");
                break;
            }

        }



        //lets join Nodes and StructuralNodeData
        for(int i = 0; i < nodes_pc_sorted_.size(); i++){
            PComponent n_snd = nodes_pc_sorted_.get(i);
            n_snd.addStructuralNodeData(structuralNodeData_pc_sorted_.get(i).getStructuralNodeData());
            node_structuralNodeData_pc_sorted_.add(n_snd);
        }
        //now PComponents in node_structuralNodeData_pc_sorted_ contain
        // a Node and a StructuralNodeData




        //now we need to create branches
        for(PComponent pc : node_structuralNodeData_pc_sorted_){
            try{
                int bID = Integer.parseInt(pc.getStructuralNodeData().getBranchID());
                int indexBID = getBranchIDIndex(bID);
                if(indexBID > -1){
                    int size = branches_.get(indexBID).size();
                    //if no list exsists create one !!!
                    if(size == 0){
                        branches_.get(indexBID).add(new ArrayList<PComponent>());
                        branches_next_branchID_.get(indexBID).add(new ArrayList<Integer>());
                        size = 1;
                    }
                    branches_.get(indexBID).get(size-1).add(pc);
                }
                else{
                    branchID_.add(new Integer(bID));
                    branches_.add(new ArrayList<ArrayList<PComponent> >());
                    branches_next_branchID_.add(new ArrayList<ArrayList<Integer> >());
                }
            }
            catch (Exception e){
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
        for(ArrayList<ArrayList<PComponent> > branch : branches_){
            //get first element
            PComponent first_pc = branch.get(0).get(0);
            if(first_pc.hasStructuralNodeData()){
                StructuralNodeData snd = first_pc.getStructuralNodeData();
                if(snd.hasType()){
                    //we dont need to connect start to other nodes...
                    if(!snd.getType().equalsIgnoreCase("NT_STARTFLOW")){
                        if(snd.hasSplitNodeID()){
                            String splitNodeID = snd.getSplitNodeID();
                            PComponent splitNode = getPComponentByNodeID(splitNodeID);

                            if(splitNode == null){
                                Log.e(TAG,"splitNode is null !!! snd:splitNodeID:" + splitNodeID);

                                if(first_pc.hasNode()){
                                    if(first_pc.getNode().hasID()){
                                        Log.e(TAG,"node:NodeID:" + first_pc.getNode().getID());
                                    }
                                }
                                continue;
                            }

                            //get the branchID and branchPartIndex of the splitNode
                            if(splitNode.hasStructuralNodeData()){
                                StructuralNodeData snd_splitNode = splitNode.getStructuralNodeData();
                                if(snd_splitNode.hasBranchID()){
                                    try {
                                        int branchID = Integer.parseInt(snd_splitNode.getBranchID());
                                        int nodesBranchPartIndex = getNodesBranchPartIndex(splitNode);

                                        //insert branchIndex into third array...
                                        //use branchID and nodesBranchPartIndex

                                        int branchI = getBranchIDIndex(branchID);

                                        if((nodesBranchPartIndex > -1) && branchI > -1){
                                            branches_next_branchID_.get(branchI).get(nodesBranchPartIndex).add(branchID_.get(branchIndex));
                                        }

                                    }
                                    catch (Exception e){
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
    public int getNodesBranchPartIndex(PComponent pc){
        if(pc.hasStructuralNodeData()){
            StructuralNodeData snd = pc.getStructuralNodeData();
            if(snd.hasBranchID()){
                try {
                    int branchID = Integer.parseInt(snd.getBranchID());
                    int branchIDIndex = getBranchIDIndex(branchID);
                    if(branchIDIndex > -1){
                        ArrayList<ArrayList<PComponent>> branch_parts = branches_.get(branchIDIndex);

                        int branch_part_index = -1;
                        for(ArrayList<PComponent> branch_part : branch_parts){
                            branch_part_index += 1;
                            for(PComponent branch_part_pc : branch_part){
                                //compair IDs
                                if(pc.hasNode() && branch_part_pc.hasNode()){
                                    if(pc.getNode().getID().equalsIgnoreCase(branch_part_pc.getNode().getID())){
                                        return branch_part_index;
                                    }
                                }
                                else{
                                    Log.e(TAG, "getNodesBranchPartIndex(PComponent pc)  no nodes ...");
                                }
                            }
                        }
                    }
                    else{
                        Log.e(TAG, "getNodesBranchPartIndex(PComponent pc)  branchIDIndex could not be found ...");
                    }
                }
                catch (Exception e){
                    Log.e(TAG, "getNodesBranchPartIndex(PComponent pc)  StructuralNodeData contains malformed BranchID...");
                }
            }
            else{
                Log.e(TAG, "getNodesBranchPartIndex(PComponent pc)  no BranchID in StructuralNodeData...");
            }
        }
        else{
            Log.e(TAG, "getNodesBranchPartIndex(PComponent pc)  no StructuralNodeData in PComponent...");
        }
        return -1;
    }

    public int getBranchIDIndex(int branchID){
        int i = -1;
        for(Integer index : branchID_){
            i += 1;
            if(index.intValue() == branchID){
                return i;
            }
        }
        return -1;
    }

    public PComponent getPComponentByNodeID(String nodeID){
        for(PComponent pc : node_structuralNodeData_pc_sorted_){
            if(pc.hasNode()){
                if(pc.getNode().getID().equalsIgnoreCase(nodeID)){
                    return pc;
                }
            }
        }
        return null;
    }

    public PComponent getNodesStructuralNodeData(PComponent pc){
        int index = getNodesIndex(pc);
        if(index > -1){
            return structuralNodeData_pc_unsorted_.get(index);
        }
        return null;
    }

    public int getNodesIndex(PComponent pc){
        Node n = pc.getNode();
        if(nodes_pc_unsorted_.size() > -1){
            int index = 0;
            for(PComponent pc_n : nodes_pc_unsorted_){
                if(n.getID().equalsIgnoreCase(pc_n.getNode().getID())){
                    return index;
                }
                index += 1;
            }
        }
        return -1;
    }

    public PComponent getStructuralNodeDatasNode(PComponent pc){
        int index = getStructuralNodeDatasIndex(pc);
        if(index > -1){
            return nodes_pc_unsorted_.get(index);
        }
        return null;
    }

    public int getStructuralNodeDatasIndex(PComponent pc){
        StructuralNodeData snd = pc.getStructuralNodeData();
        if((structuralNodeData_pc_unsorted_.size() > -1) && (snd!= null)){
            int index = 0;
            for(PComponent pc_snd : structuralNodeData_pc_unsorted_){
                StructuralNodeData snd2 = pc_snd.getStructuralNodeData();
                if(snd2 != null) {
                    if (snd.getID().equalsIgnoreCase(snd2.getID())) {
                        return index;
                    }
                }
                index += 1;
            }
        }
        return -1;
    }

    public List<PComponent> getPComponents(){
        return componentList_;
    }


}



