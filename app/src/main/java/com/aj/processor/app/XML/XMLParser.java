package com.aj.processor.app.XML;

import android.util.Log;
import android.util.Xml;

import com.aj.processor.app.Debugger;
import com.aj.processor.app.XML.Process.Components.DataEdge;
import com.aj.processor.app.XML.Process.Components.DataElement;
import com.aj.processor.app.XML.Process.Components.Edge;
import com.aj.processor.app.XML.Process.Components.Node;
import com.aj.processor.app.XML.Process.PComponent;
import com.aj.processor.app.XML.Process.Components.StructuralNodeData;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class XMLParser {
    // We don't use namespaces
    private static final String ns = null;

    public List parse(InputStream in) throws XmlPullParserException, IOException {
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in, null);
            parser.nextTag();
            Debugger.warning("XMLParser", "parse");
            return readProcess(parser);
        }
        finally {
            in.close();
        }
    }

    private List readProcess(XmlPullParser parser) throws XmlPullParserException, IOException {
        List<PComponent> pcomps = new ArrayList<PComponent>();

        //the first tag in the xml...
        //ignore namespaces... (ns == null)
        parser.require(XmlPullParser.START_TAG, ns, "template");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String tag = parser.getName();
            // Starts by looking for the entry tag
            if (tag.equals("nodes")) {

                Debugger.warning("XMLParser","nodes");

                List<PComponent> nodes = readNodes(parser);
                for(PComponent pc: nodes){
                    pcomps.add(pc);
                }
            }
            else if(tag.equals("dataElements")){
                Debugger.warning("XMLParser","dataElements");

                List<PComponent> dataElements = readDataElements(parser);
                for(PComponent pc: dataElements){
                    pcomps.add(pc);
                }
            }
            else if(tag.equals("edges")){
                Debugger.warning("XMLParser","edges");

                List<PComponent> edges = readEdges(parser);
                for(PComponent pc: edges){
                    pcomps.add(pc);
                }
            }
            else if(tag.equals("dataEdges")){
                Debugger.warning("XMLParser","dataEdges");

                List<PComponent> dataEdges = readDataEdges(parser);
                for(PComponent pc: dataEdges){
                    pcomps.add(pc);
                }
            }
            else if(tag.equals("structuralData")){
                Debugger.warning("XMLParser","structuralData");

                List<PComponent> dataEdges = readStructuralNodeDatas(parser);
                for(PComponent pc: dataEdges){
                    pcomps.add(pc);
                }
            }
            else{
                skip(parser);
            }
        }

        Debugger.warning("XMLParser","parse done.");

        //temporary log output...
        for(PComponent p : pcomps){
            Debugger.warning("XMLParser:","PComponent...");
            if(p.hasNode())
                Debugger.warning("XMLParser:","Node: " + p.getNode().getName());
        }

        return pcomps;
    }


    //READING NODES...
    private List readNodes(XmlPullParser parser) throws XmlPullParserException, IOException {
        List<PComponent> pcomps = new ArrayList<PComponent>();
        parser.require(XmlPullParser.START_TAG, ns, "nodes");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals("node")) {
                Debugger.warning("XMLParser","node");
                PComponent pc = readNode(parser);
                pcomps.add(pc);
            }
            else {
                skip(parser);
            }
        }
        return pcomps;
    }

    private PComponent readNode(XmlPullParser parser) throws XmlPullParserException, IOException {
        Debugger.warning("XMLParser","1");
        parser.require(XmlPullParser.START_TAG, ns, "node");
        String node_id = readNodeIDAttrib(parser);
        String node_name = null;
        String node_description = null;
        String node_staffAssignmentRule = null;
        Debugger.warning("XMLParser","2");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals("name")) {
                node_name = readName(parser);
            }
            else if (name.equals("description")) {
                node_description = readDescription(parser);
            }
            else if (name.equals("staffAssignmentRule")) {
                node_staffAssignmentRule = readStaffAssignmentRule(parser);
            }
            else {
                skip(parser);
            }
            Debugger.warning("XMLParser","...");
        }
        Debugger.warning("XMLParser","3");
        Node n = new Node(node_id);
        n.setName(node_name);
        n.setDescription(node_description);
        n.setStaffAssignmentRule(node_staffAssignmentRule);
        PComponent pc = new PComponent(n);
        return pc;
    }







    //READING DATAELEMENTS...
    private List readDataElements(XmlPullParser parser) throws XmlPullParserException, IOException {
        List<PComponent> pcomps = new ArrayList<PComponent>();
        parser.require(XmlPullParser.START_TAG, ns, "dataElements");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals("dataElement")) {
                Debugger.warning("XMLParser","dataElement");
                PComponent pc = readDataElement(parser);
                pcomps.add(pc);
            }
            else {
                skip(parser);
            }
        }
        return pcomps;
    }

    private PComponent readDataElement(XmlPullParser parser) throws XmlPullParserException, IOException {
        Debugger.warning("XMLParser","1");
        parser.require(XmlPullParser.START_TAG, ns, "dataElement");
        String dataElement_id = readDataElementIDAttrib(parser);
        String dataElement_type = null;
        String dataElement_name = null;
        String dataElement_description = null;
        Debugger.warning("XMLParser","2");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals("type")) {
                dataElement_type = readType(parser);
            }
            else if (name.equals("name")) {
                dataElement_name = readName(parser);
            }
            else if (name.equals("description")) {
                dataElement_description = readDescription(parser);
            }
            else {
                skip(parser);
            }
            Debugger.warning("XMLParser","...");
        }
        Debugger.warning("XMLParser","3");
        DataElement de = new DataElement(dataElement_id);
        de.setType(dataElement_type);
        de.setName(dataElement_name);
        de.setDescription(dataElement_description);
        PComponent pc = new PComponent(de);
        return pc;
    }




    //READING EDGES...
    private List readEdges(XmlPullParser parser) throws XmlPullParserException, IOException {
        List<PComponent> pcomps = new ArrayList<PComponent>();
        parser.require(XmlPullParser.START_TAG, ns, "edges");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals("edge")) {
                Debugger.warning("XMLParser","edge");
                PComponent pc = readEdge(parser);
                pcomps.add(pc);
            }
            else {
                skip(parser);
            }
        }
        return pcomps;
    }

    private PComponent readEdge(XmlPullParser parser) throws XmlPullParserException, IOException {
        Debugger.warning("XMLParser","1");
        parser.require(XmlPullParser.START_TAG, ns, "edge");
        String edge_destinationId = readEdgeDestinationNodeIDAttrib(parser);
        String edge_edgeType = readEdgeTypeAttrib(parser);
        String edge_sourceId = readEdgeSourceNodeIDAttrib(parser);
        Debugger.warning("XMLParser","2");

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }

            skip(parser);

            Debugger.warning("XMLParser","...");
        }

        Debugger.warning("XMLParser","3");
        Edge e = new Edge(edge_destinationId,edge_sourceId,edge_edgeType);
        PComponent pc = new PComponent(e);
        return pc;
    }



    //READING EDGES...
    private List readDataEdges(XmlPullParser parser) throws XmlPullParserException, IOException {
        List<PComponent> pcomps = new ArrayList<PComponent>();
        parser.require(XmlPullParser.START_TAG, ns, "dataEdges");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals("dataEdge")) {
                Debugger.warning("XMLParser","dataEdge");
                PComponent pc = readDataEdge(parser);
                pcomps.add(pc);
            }
            else {
                skip(parser);
            }
        }
        return pcomps;
    }

    private PComponent readDataEdge(XmlPullParser parser) throws XmlPullParserException, IOException {
        Debugger.warning("XMLParser","1");
        parser.require(XmlPullParser.START_TAG, ns, "dataEdge");
        String dataEdge_connectorId = readDataEdgeConnectorIDAttrib(parser);
        String dataEdge_dataEdgeType = readDataEdgeTypeAttrib(parser);
        String dataEdge_dataElementID = readDataEdgeDataElementIDAttrib(parser);
        String dataEdge_nodeID = readDataEdgeNodeIDAttrib(parser);
        Debugger.warning("XMLParser","2");

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            skip(parser);
            Debugger.warning("XMLParser","...");
        }

        Debugger.warning("XMLParser","3");
        DataEdge de = new DataEdge(dataEdge_connectorId,
                dataEdge_dataEdgeType,
                dataEdge_dataElementID,
                dataEdge_nodeID);
        PComponent pc = new PComponent(de);
        return pc;
    }



    //READING STRUCTURALNODEDATA...
    private List readStructuralNodeDatas(XmlPullParser parser) throws XmlPullParserException, IOException {
        List<PComponent> pcomps = new ArrayList<PComponent>();
        parser.require(XmlPullParser.START_TAG, ns, "structuralData");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals("structuralNodeData")) {
                Debugger.warning("XMLParser","structuralNodeData");
                PComponent pc = readStructuralNodeData(parser);
                pcomps.add(pc);
            }
            else {
                skip(parser);
            }
        }
        return pcomps;
    }

    private PComponent readStructuralNodeData(XmlPullParser parser) throws XmlPullParserException, IOException {
        Debugger.warning("XMLParser","1");
        parser.require(XmlPullParser.START_TAG, ns, "structuralNodeData");
        String structuralNodeData_nodeId = readStructuralNodeDataIDAttrib(parser);
        String structuralNodeData_type = null;
        String structuralNodeData_topologicalID = null;
        String structuralNodeData_branchID = null;
        String structuralNodeData_splitNodeID = null;
        String structuralNodeData_correspondingBlockNodeID = null;
        Debugger.warning("XMLParser","2");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals("type")) {
                structuralNodeData_type = readType(parser);
            }
            else if (name.equals("topologicalID")) {
                structuralNodeData_topologicalID = readTopologicalID(parser);
            }
            else if (name.equals("branchID")) {
                structuralNodeData_branchID = readBranchID(parser);
            }
            else if (name.equals("splitNodeID")) {
                structuralNodeData_splitNodeID = readSplitNodeID(parser);
            }
            else if (name.equals("correspondingBlockNodeID")) {
                structuralNodeData_correspondingBlockNodeID = readCorrespondingBlockNodeID(parser);
            }
            else {
                skip(parser);
            }
            Debugger.warning("XMLParser","...");
        }
        Debugger.warning("XMLParser","3");
        StructuralNodeData snd = new StructuralNodeData(structuralNodeData_nodeId);
        snd.setType(structuralNodeData_type);
        snd.setTopologicalID(structuralNodeData_topologicalID);
        snd.setBranchID(structuralNodeData_branchID);
        if(structuralNodeData_splitNodeID!=null) {
            snd.setSplitNodeID(structuralNodeData_splitNodeID);
        }
        if(structuralNodeData_correspondingBlockNodeID!=null) {
            snd.setCorrespondingBlockNodeID(structuralNodeData_correspondingBlockNodeID);
        }
        PComponent pc = new PComponent(snd);
        return pc;
    }









    //can't reuse the ID function... checking for correct tag!!!

    private String readNodeIDAttrib(XmlPullParser parser) throws XmlPullParserException, IOException {
        Debugger.warning("XMLParser","readNodeIDAttrib");
        parser.require(XmlPullParser.START_TAG, ns, "node");
        String nodeID = parser.getAttributeValue(null, "id");
        Debugger.warning("XMLParser","readNodeIDAttrib done!");
        return nodeID;
    }

    private String readDataElementIDAttrib(XmlPullParser parser) throws XmlPullParserException, IOException {
        Debugger.warning("XMLParser","readDataElementIDAttrib");
        parser.require(XmlPullParser.START_TAG, ns, "dataElement");
        String dataElementID = parser.getAttributeValue(null, "id");
        Debugger.warning("XMLParser","readDataElementIDAttrib done!");
        return dataElementID;
    }


    private String readEdgeDestinationNodeIDAttrib(XmlPullParser parser) throws XmlPullParserException, IOException {
        Debugger.warning("XMLParser","readEdgeDestinationNodeIDAttrib");
        parser.require(XmlPullParser.START_TAG, ns, "edge");
        String edgeDestinationNodeID = parser.getAttributeValue(null, "destinationNodeID");
        Debugger.warning("XMLParser","readEdgeDestinationNodeIDAttrib done!");
        return edgeDestinationNodeID;
    }

    private String readEdgeSourceNodeIDAttrib(XmlPullParser parser) throws XmlPullParserException, IOException {
        Debugger.warning("XMLParser","readEdgeSourceNodeIDAttrib");
        parser.require(XmlPullParser.START_TAG, ns, "edge");
        String edgeSourceNodeID = parser.getAttributeValue(null, "sourceNodeID");
        Debugger.warning("XMLParser","readEdgeSourceNodeIDAttrib done!");
        return edgeSourceNodeID;
    }

    private String readEdgeTypeAttrib(XmlPullParser parser) throws XmlPullParserException, IOException {
        Debugger.warning("XMLParser","readEdgeTypeAttrib");
        parser.require(XmlPullParser.START_TAG, ns, "edge");
        String edgeTypeAttrib = parser.getAttributeValue(null, "edgeType");
        Debugger.warning("XMLParser","readEdgeTypeAttribute done!");
        return edgeTypeAttrib;
    }

    private String readDataEdgeTypeAttrib(XmlPullParser parser) throws XmlPullParserException, IOException {
        Debugger.warning("XMLParser","readDataEdgeTypeAttrib");
        parser.require(XmlPullParser.START_TAG, ns, "dataEdge");
        String dataEdgeTypeAttrib = parser.getAttributeValue(null, "dataEdgeType");
        Debugger.warning("XMLParser","readDataEdgeTypeAttrib done!");
        return dataEdgeTypeAttrib;
    }

    private String readDataEdgeConnectorIDAttrib(XmlPullParser parser) throws XmlPullParserException, IOException {
        Debugger.warning("XMLParser","readDataEdgeconnectorIDAttrib");
        parser.require(XmlPullParser.START_TAG, ns, "dataEdge");
        String dataEdgeConnectorIDAttrib = parser.getAttributeValue(null, "connectorID");
        Debugger.warning("XMLParser","readDataEdgeconnectorIDAttrib done!");
        return dataEdgeConnectorIDAttrib;
    }

    private String readDataEdgeDataElementIDAttrib(XmlPullParser parser) throws XmlPullParserException, IOException {
        Debugger.warning("XMLParser","readDataEdgeDataElementIDAttrib");
        parser.require(XmlPullParser.START_TAG, ns, "dataEdge");
        String dataEdgeDataElementIDAttrib = parser.getAttributeValue(null, "dataElementID");
        Debugger.warning("XMLParser","readDataEdgeDataElementIDAttrib done!");
        return dataEdgeDataElementIDAttrib;
    }

    private String readDataEdgeNodeIDAttrib(XmlPullParser parser) throws XmlPullParserException, IOException {
        Debugger.warning("XMLParser","readDataEdgeNodeIDAttrib");
        parser.require(XmlPullParser.START_TAG, ns, "dataEdge");
        String dataEdgeNodeIDAttrib = parser.getAttributeValue(null, "nodeID");
        Debugger.warning("XMLParser","readDataEdgeNodeIDAttrib done!");
        return dataEdgeNodeIDAttrib;
    }

    private String readStructuralNodeDataIDAttrib(XmlPullParser parser) throws XmlPullParserException, IOException {
        Debugger.warning("XMLParser","readStructuralNodeDataIDAttrib");
        parser.require(XmlPullParser.START_TAG, ns, "structuralNodeData");
        String structuralNodeDataIDAttrib = parser.getAttributeValue(null, "nodeID");
        Debugger.warning("XMLParser","readStructuralNodeDataIDAttrib done!");
        return structuralNodeDataIDAttrib;
    }











    //reuse those functions...

    private String readType(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, ns, "type");
        String type = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "type");
        return type;
    }

    private String readName(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, ns, "name");
        String name = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "name");
        return name;
    }

    private String readDescription(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, ns, "description");
        String description = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "description");
        return description;
    }

    private String readStaffAssignmentRule(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, ns, "staffAssignmentRule");
        String staffAssignmentRule = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "staffAssignmentRule");
        return staffAssignmentRule;
    }

    private String readEdgeType(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, ns, "edgeType");
        String edgeType = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "edgeType");
        return edgeType;
    }

    private String readDataEdgeType(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, ns, "dataEdgeType");
        String dataEdgeType = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "dataEdgeType");
        return dataEdgeType;
    }

    private String readTopologicalID(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, ns, "topologicalID");
        String topologicalID = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "topologicalID");
        return topologicalID;
    }

    private String readBranchID(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, ns, "branchID");
        String branchID = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "branchID");
        return branchID;
    }

    private String readSplitNodeID(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, ns, "splitNodeID");
        String splitNodeID = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "splitNodeID");
        return splitNodeID;
    }

    private String readCorrespondingBlockNodeID(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, ns, "correspondingBlockNodeID");
        String correspondingBlockNodeID = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "correspondingBlockNodeID");
        return correspondingBlockNodeID;
    }

    // For the tags title and summary, extracts their text values.
    private String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
        String result = "";
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.getText();
            parser.nextTag();
        }
        return result;
    }



    private void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
        if (parser.getEventType() != XmlPullParser.START_TAG) {
            throw new IllegalStateException();
        }
        int depth = 1;
        while (depth != 0) {
            switch (parser.next()) {
                case XmlPullParser.END_TAG:
                    depth--;
                    break;
                case XmlPullParser.START_TAG:
                    depth++;
                    break;
            }
        }
    }

		
}