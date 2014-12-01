package com.aj.processor.app.XML.Process;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by AJ on 30.11.2014.
 */
public class Process {

    private List<PComponent> componentList = new ArrayList<PComponent>();

    //dummy constructor
    public Process(){

    }

    public void addPCcomponent(PComponent pc){
        componentList.add(pc);
    }

    //not sure if it works... not even sure if we need this...
    public void removePComponent(PComponent pc){
        componentList.remove(pc);
    }

    public List<PComponent> getPComponents(){
        return componentList;
    }
}
