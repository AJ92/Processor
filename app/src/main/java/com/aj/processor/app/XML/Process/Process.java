package com.aj.processor.app.XML.Process;



import android.util.Log;

import com.aj.processor.app.XML.Process.Components.Node;
import com.aj.processor.app.XML.Process.Components.StructuralNodeData;

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

    //2 dimensional LIST
    private ArrayList<Integer> branchID_ = new ArrayList<Integer>();
    private ArrayList<ArrayList<ArrayList<PComponent> > > branches_ = new ArrayList<ArrayList<ArrayList<PComponent> > >();
    private ArrayList<ArrayList<ArrayList<Integer> > > branches_next_branchID_ = new ArrayList<ArrayList<ArrayList<Integer> > >();


    //dummy constructor
    public Process(List<PComponent> pComponents){
        componentList_ = pComponents;
        generateSortedStructure();
    }

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
                break;
            }
        }
        return i;
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



