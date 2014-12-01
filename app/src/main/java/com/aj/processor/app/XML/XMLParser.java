package com.aj.processor.app.XML;

import android.util.Log;
import android.util.Xml;

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
            Log.e("XMLParser","parse");
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

                Log.e("XMLParser","nodes");

                List<PComponent> nodes = readNodes(parser);
                for(PComponent pc: nodes){
                    pcomps.add(pc);
                }
            }
            else if(tag.equals("dataElements")){
                Log.e("XMLParser","dataElements");

                List<PComponent> dataElements = readDataElements(parser);
                for(PComponent pc: dataElements){
                    pcomps.add(pc);
                }
            }
            else if(tag.equals("edges")){
                Log.e("XMLParser","edges");

                List<PComponent> edges = readEdges(parser);
                for(PComponent pc: edges){
                    pcomps.add(pc);
                }
            }
            else if(tag.equals("dataEdges")){
                Log.e("XMLParser","dataEdges");

                List<PComponent> dataEdges = readDataEdges(parser);
                for(PComponent pc: dataEdges){
                    pcomps.add(pc);
                }
            }
            else if(tag.equals("structuralData")){
                Log.e("XMLParser","structuralData");

                List<PComponent> dataEdges = readStructuralNodeDatas(parser);
                for(PComponent pc: dataEdges){
                    pcomps.add(pc);
                }
            }
            else{
                skip(parser);
            }
        }

        Log.e("XMLParser","parse done.");

        //temporary log output...
        for(PComponent p : pcomps){
            Log.e("XMLParser:","PComponent...");
            if(p.hasNode())
                Log.e("XMLParser:","Node: " + p.getNode().getName());
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
                Log.e("XMLParser","node");
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
        Log.e("XMLParser","1");
        parser.require(XmlPullParser.START_TAG, ns, "node");
        String node_id = readNodeIDAttrib(parser);
        String node_name = null;
        String node_description = null;
        Log.e("XMLParser","2");
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
            else {
                skip(parser);
            }
            Log.e("XMLParser","...");
        }
        Log.e("XMLParser","3");
        Node n = new Node(node_id);
        n.setName(node_name);
        n.setDescription(node_description);
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
                Log.e("XMLParser","dataElement");
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
        Log.e("XMLParser","1");
        parser.require(XmlPullParser.START_TAG, ns, "dataElement");
        String dataElement_id = readDataElementIDAttrib(parser);
        String dataElement_type = null;
        String dataElement_name = null;
        String dataElement_description = null;
        Log.e("XMLParser","2");
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
            Log.e("XMLParser","...");
        }
        Log.e("XMLParser","3");
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
                Log.e("XMLParser","edge");
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
        Log.e("XMLParser","1");
        parser.require(XmlPullParser.START_TAG, ns, "edge");
        String edge_destinationId = readEdgeDestinationNodeIDAttrib(parser);
        String edge_edgeType = readEdgeTypeAttrib(parser);
        String edge_sourceId = readEdgeSourceNodeIDAttrib(parser);
        Log.e("XMLParser","2");

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }

            skip(parser);

            Log.e("XMLParser","...");
        }

        Log.e("XMLParser","3");
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
                Log.e("XMLParser","dataEdge");
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
        Log.e("XMLParser","1");
        parser.require(XmlPullParser.START_TAG, ns, "dataEdge");
        String dataEdge_connectorId = readDataEdgeConnectorIDAttrib(parser);
        String dataEdge_dataEdgeType = readDataEdgeTypeAttrib(parser);
        String dataEdge_dataElementID = readDataEdgeDataElementIDAttrib(parser);
        String dataEdge_nodeID = readDataEdgeNodeIDAttrib(parser);
        Log.e("XMLParser","2");

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            skip(parser);
            Log.e("XMLParser","...");
        }

        Log.e("XMLParser","3");
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
                Log.e("XMLParser","structuralNodeData");
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
        Log.e("XMLParser","1");
        parser.require(XmlPullParser.START_TAG, ns, "structuralNodeData");
        String structuralNodeData_nodeId = readStructuralNodeDataIDAttrib(parser);
        String structuralNodeData_type = null;
        String structuralNodeData_topologicalID = null;
        Log.e("XMLParser","2");
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
            else {
                skip(parser);
            }
            Log.e("XMLParser","...");
        }
        Log.e("XMLParser","3");
        StructuralNodeData snd = new StructuralNodeData(structuralNodeData_nodeId);
        snd.setType(structuralNodeData_type);
        snd.setTopologicalID(structuralNodeData_topologicalID);
        PComponent pc = new PComponent(snd);
        return pc;
    }









    //can't reuse the ID function... checking for correct tag!!!

    private String readNodeIDAttrib(XmlPullParser parser) throws XmlPullParserException, IOException {
        Log.e("XMLParser","readNodeIDAttrib");
        parser.require(XmlPullParser.START_TAG, ns, "node");
        String nodeID = parser.getAttributeValue(null, "id");
        Log.e("XMLParser","readNodeIDAttrib done!");
        return nodeID;
    }

    private String readDataElementIDAttrib(XmlPullParser parser) throws XmlPullParserException, IOException {
        Log.e("XMLParser","readDataElementIDAttrib");
        parser.require(XmlPullParser.START_TAG, ns, "dataElement");
        String dataElementID = parser.getAttributeValue(null, "id");
        Log.e("XMLParser","readDataElementIDAttrib done!");
        return dataElementID;
    }


    private String readEdgeDestinationNodeIDAttrib(XmlPullParser parser) throws XmlPullParserException, IOException {
        Log.e("XMLParser","readEdgeDestinationNodeIDAttrib");
        parser.require(XmlPullParser.START_TAG, ns, "edge");
        String edgeDestinationNodeID = parser.getAttributeValue(null, "destinationNodeID");
        Log.e("XMLParser","readEdgeDestinationNodeIDAttrib done!");
        return edgeDestinationNodeID;
    }

    private String readEdgeSourceNodeIDAttrib(XmlPullParser parser) throws XmlPullParserException, IOException {
        Log.e("XMLParser","readEdgeSourceNodeIDAttrib");
        parser.require(XmlPullParser.START_TAG, ns, "edge");
        String edgeSourceNodeID = parser.getAttributeValue(null, "sourceNodeID");
        Log.e("XMLParser","readEdgeSourceNodeIDAttrib done!");
        return edgeSourceNodeID;
    }

    private String readEdgeTypeAttrib(XmlPullParser parser) throws XmlPullParserException, IOException {
        Log.e("XMLParser","readEdgeTypeAttrib");
        parser.require(XmlPullParser.START_TAG, ns, "edge");
        String edgeTypeAttrib = parser.getAttributeValue(null, "edgeType");
        Log.e("XMLParser","readEdgeTypeAttribute done!");
        return edgeTypeAttrib;
    }

    private String readDataEdgeTypeAttrib(XmlPullParser parser) throws XmlPullParserException, IOException {
        Log.e("XMLParser","readDataEdgeTypeAttrib");
        parser.require(XmlPullParser.START_TAG, ns, "dataEdge");
        String dataEdgeTypeAttrib = parser.getAttributeValue(null, "dataEdgeType");
        Log.e("XMLParser","readDataEdgeTypeAttrib done!");
        return dataEdgeTypeAttrib;
    }

    private String readDataEdgeConnectorIDAttrib(XmlPullParser parser) throws XmlPullParserException, IOException {
        Log.e("XMLParser","readDataEdgeconnectorIDAttrib");
        parser.require(XmlPullParser.START_TAG, ns, "dataEdge");
        String dataEdgeConnectorIDAttrib = parser.getAttributeValue(null, "connectorID");
        Log.e("XMLParser","readDataEdgeconnectorIDAttrib done!");
        return dataEdgeConnectorIDAttrib;
    }

    private String readDataEdgeDataElementIDAttrib(XmlPullParser parser) throws XmlPullParserException, IOException {
        Log.e("XMLParser","readDataEdgeDataElementIDAttrib");
        parser.require(XmlPullParser.START_TAG, ns, "dataEdge");
        String dataEdgeDataElementIDAttrib = parser.getAttributeValue(null, "dataElementID");
        Log.e("XMLParser","readDataEdgeDataElementIDAttrib done!");
        return dataEdgeDataElementIDAttrib;
    }

    private String readDataEdgeNodeIDAttrib(XmlPullParser parser) throws XmlPullParserException, IOException {
        Log.e("XMLParser","readDataEdgeNodeIDAttrib");
        parser.require(XmlPullParser.START_TAG, ns, "dataEdge");
        String dataEdgeNodeIDAttrib = parser.getAttributeValue(null, "nodeID");
        Log.e("XMLParser","readDataEdgeNodeIDAttrib done!");
        return dataEdgeNodeIDAttrib;
    }

    private String readStructuralNodeDataIDAttrib(XmlPullParser parser) throws XmlPullParserException, IOException {
        Log.e("XMLParser","readStructuralNodeDataIDAttrib");
        parser.require(XmlPullParser.START_TAG, ns, "structuralNodeData");
        String structuralNodeDataIDAttrib = parser.getAttributeValue(null, "nodeID");
        Log.e("XMLParser","readStructuralNodeDataIDAttrib done!");
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

    private String readEdgeType(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, ns, "edgeType");
        String description = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "edgeType");
        return description;
    }

    private String readDataEdgeType(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, ns, "dataEdgeType");
        String description = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "dataEdgeType");
        return description;
    }

    private String readTopologicalID(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, ns, "topologicalID");
        String description = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "topologicalID");
        return description;
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