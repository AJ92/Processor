package com.aj.processor.app.XML.Process;

import java.util.ArrayList;

/**
 * Created by AJ on 16.12.2014.
 */
public class Branch {


    private static int branchID = 0;
    private int myBranchID = 0;

    ArrayList<PComponent> elements = new ArrayList<PComponent>();
    ArrayList<Branch> branches = new ArrayList<Branch>();


    private int pcomponentCount = 0;
    private int branchCount = 0;

    public Branch(){
        Branch.branchID += 1;
        myBranchID = Branch.branchID;
    }

    public int getBranchID(){
        return myBranchID;
    }

    public void addElement(PComponent pc){
        elements.add(pc);
        pcomponentCount += 1;
    }

    public void addBranch(Branch b){
        branches.add(b);
        branchCount += 1;
    }

    public int getPComponentCount(){
        return pcomponentCount;
    }

    public int getBranchCount(){
        return branchCount;
    }

    public ArrayList<PComponent> getElements(){
        return elements;
    }

    public ArrayList<Branch> getBranches(){
        return branches;
    }


    //calculate graphics specific values...
    //should be called on the root branch...
    //but on the other hand it shouldn't make any difference xD
    //only for the depth...
    public void optimize_x(int init_depth, int init_distance){
        setDepth(init_depth);
        setDistance(init_distance);
        int temp_branch_id = 0;

        ArrayList<Branch> current_branches = branches;
        boolean work = false;
        if(current_branches.size() > 0){
            for (Branch b : current_branches) {
                b.setLocalBranchID(temp_branch_id);
                temp_branch_id += 1;
                b.optimize_x(depth += 1, distance + pcomponentCount);
            }
        }
    }



    //depth in the graph/tree
    private int depth = 0;
    //id for current/local branching
    private int local_branch_id = 0;
    //overlapping branch counter
    private int overlapping_branches = 0;
    //id for y axis stretching...
    private double y_spread = 0.0;
    //distance from root
    private int distance = 0;

    //how far is this branch away from the first branch (in terms of splits)
    private int branchLevel = 0;



    public int getDepth(){
        return depth;
    }

    public void setDepth(int d){
        depth = Math.max(d,depth);
    }

    public int getLocalBranchID(){
        return local_branch_id;
    }

    public void setLocalBranchID(int lbid){
        local_branch_id = Math.max(lbid,local_branch_id);
    }

    public double getYSpread(){
        return y_spread;
    }

    public void setYSpread(double ySpread){
        y_spread = ySpread;
    }

    public int getDistance(){
        return distance;
    }

    public void setDistance(int d){
        distance = Math.max(d,distance);
    }

    public int getOverlappingBranchCount(){
        return overlapping_branches;
    }

    public void setOverlappingBranchCount(int count){
        overlapping_branches = count;
    }

    public int getBranchLevel(){
        return branchLevel;
    }

    public void setBranchLevel(int lvl){
        branchLevel = lvl;
    }

    public int getBranchCountInTREE(Branch b){
        int count = 0;

        if(this.getBranchID() == b.getBranchID()){
            return 1;
        }

        for(Branch b2 : branches){
            count += b2.getBranchCountInTREE(b);
        }
        return count;
    }

}
