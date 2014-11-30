package com.aj.processor.app.XML;

import android.util.Log;
import android.util.Xml;

import com.aj.processor.app.XML.Process.Components.Node;
import com.aj.processor.app.XML.Process.Components.PComponent;

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
            else if(tag.equals("dataElement")){

            }
            else if(tag.equals("edge")){

            }
            else if(tag.equals("dataEdge")){

            }
            else{
                skip(parser);
            }
        }

        Log.e("XMLParser","parse done.");

        //temporary log output...
        for(PComponent p : pcomps){
            Log.e("XMLParser:","Node...");
            if(p.hasNode())
                Log.e("XMLParser:","Node: " + p.getNode().getName());
        }

        return pcomps;
    }

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
        String node_id = readNodeID(parser);
        String node_name = null;
        String node_description = null;
        Log.e("XMLParser","2");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals("name")) {
                node_name = readNodeName(parser);
            }
            else if (name.equals("description")) {
                node_description = readNodeDescription(parser);
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

    private String readNodeID(XmlPullParser parser) throws XmlPullParserException, IOException {
        Log.e("XMLParser","readNodeID");
        parser.require(XmlPullParser.START_TAG, ns, "node");
        String nodeID = parser.getAttributeValue(null, "id");
        //parser.require(XmlPullParser.END_TAG, ns, "node");
        Log.e("XMLParser","readNodeID done!");
        return nodeID;
    }

    private String readNodeName(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, ns, "name");
        String nodeName = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "name");
        return nodeName;
    }

    private String readNodeDescription(XmlPullParser parser) throws XmlPullParserException, IOException {
        String nodeDescription = "";
        parser.require(XmlPullParser.START_TAG, ns, "description");
        String nodeName = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "description");
        return nodeDescription;
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