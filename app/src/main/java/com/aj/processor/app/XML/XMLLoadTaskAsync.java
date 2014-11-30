package com.aj.processor.app.XML;

import android.content.res.AssetManager;
import android.os.AsyncTask;
import android.util.Log;

import com.aj.processor.app.GlobalContext;
import com.aj.processor.app.R;
import com.aj.processor.app.XML.Process.Components.PComponent;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class XMLLoadTaskAsync extends AsyncTask<String, Void, String> {

    private List<PComponent> pcomp_list;

    private final int type_network = 1;
    private final int type_assets = 2;
    private int type = type_assets;

    public XMLLoadTaskAsync(){

    }


    public void retreiveXMLFromNetwork(String url){
        type = type_network;
        doInBackground(url);
    }

    public void retreiveXMLFromAssets(String path){
        Log.e("XMLLoadTaskAsync","start");
        type = type_assets;
        doInBackground(path);
        Log.e("XMLLoadTaskAsync","started");
    }



    @Override
    protected String doInBackground(String... urls) {
        Log.e("XMLLoadTaskAsync","threading...");
        try {
            if(type == type_network) {
                return loadXmlFromNetwork(urls[0]);
            }
            else if(type == type_assets){
                return loadXmlFromAssets(urls[0]);
            }
            else{
                return "fail...";
            }
        } catch (IOException e) {
            return null;
        } catch (XmlPullParserException e) {
             return null;
        }
    }

    @Override
    protected void onPostExecute(String result) {
        //from now on the entry_list is ready...
        Log.e("XMLLoadTaskAsync","result");
    }

    private String loadXmlFromNetwork(String urlString) throws XmlPullParserException, IOException {
        InputStream stream = null;
        // Instantiate the parser
        XMLParser parser = new XMLParser();
        List<PComponent> pcomps = null;

        try {
            stream = downloadUrl(urlString);
            pcomps = parser.parse(stream);
            // Makes sure that the InputStream is closed after the app is
            // finished using it.
        } finally {
            if (stream != null) {
                stream.close();
            }
        }

        pcomp_list = pcomps;

        return "loadXmlFromNetwork done.";
    }

    private String loadXmlFromAssets(String pathString) throws XmlPullParserException, IOException {


        Log.e("XMLLoadTaskAsync","loadXmlFromAssets");
        InputStream stream = null;
        // Instantiate the parser
        XMLParser parser = new XMLParser();
        List<PComponent> pcomps = null;

        try {
            stream = openAsset(pathString);
            pcomps = parser.parse(stream);
            // Makes sure that the InputStream is closed after the app is
            // finished using it.
        } finally {
            if (stream != null) {
                stream.close();
            }
        }

        pcomp_list = pcomps;
        Log.e("XMLLoadTaskAsync","loadXmlFromAssets done?");

        return "loadXmlFromAssets done.";
    }

    // Given a string representation of a URL, sets up a connection and gets
    // an input stream.
    private InputStream downloadUrl(String urlString) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setReadTimeout(10000 /* milliseconds */);
        conn.setConnectTimeout(15000 /* milliseconds */);
        conn.setRequestMethod("GET");
        conn.setDoInput(true);
        // Starts the query
        conn.connect();
        return conn.getInputStream();
    }

    private InputStream openAsset(String pathString) throws IOException {
        if(pathString == null){
            return null;
        }

        AssetManager assetManager = GlobalContext.getAppContext().getAssets();
        InputStream istr = null;
        try {
            istr = assetManager.open(pathString);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return istr;
    }

}