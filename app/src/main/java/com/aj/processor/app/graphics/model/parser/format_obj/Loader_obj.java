package com.aj.processor.app.graphics.model.parser.format_obj;

import android.content.res.AssetManager;
import android.util.Log;

import com.aj.processor.app.graphics.model.Components.Material;
import com.aj.processor.app.graphics.model.Components.Mesh;
import com.aj.processor.app.graphics.model.Model;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.regex.Pattern;


import com.aj.processor.app.GlobalContext;
import com.aj.processor.app.mathematics.Vector.Vector3;

/**
 * Created by AJ on 27.10.2014.
 */
public class Loader_obj {

    private String TAG = "Loader_obj";

    public double min_value(double x, double y){
        if(x > y){
            return y;
        }
        return x;
    }

    public double max_value(double x, double y){
        if(x < y){
            return y;
        }
        return x;
    }

    public String combine(String[] s, String glue)
    {
        int k = s.length;
        if ( k == 0 )
        {
            return null;
        }
        StringBuilder out = new StringBuilder();
        out.append( s[0] );
        for ( int x=1; x < k; ++x )
        {
            out.append(glue).append(s[x]);
        }
        return out.toString();
    }

    //mdl is a reference...
    public boolean load_model_data(Model mdl, String path){

        Log.e(TAG,"Model import: started.");

        String pathlist[] = path.split(Pattern.quote(File.separator)); //KeepEmptyParts
        String model_name = pathlist[pathlist.length-1];
        Log.e(TAG,"Model import: ModelName: " + model_name);

        //LOAD MESH DATA
        AssetManager am = GlobalContext.getAppContext().getAssets();
        InputStream is = null;
        try {
            is = am.open(path);
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG,"Model import: Error 1: InputStream could not be initialized...");
            return false;
        }
        InputStreamReader isr = new InputStreamReader(is);
        try {
            if(!isr.ready()){
                Log.e(TAG,"Model import: Error 2: InputStreamReader could not be initialized...");
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG,"Model import: Error 2: InputStreamReader could not be initialized...");
            return false;
        }
        BufferedReader file= new BufferedReader(isr);


        try {
            if (!file.ready())
            {
                Log.e(TAG,"Model import: Error 3: Model file could not be loaded...");
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG,"Model import: Error 3: Model file could not be loaded...");
            return false;
        }

        String line = "";

        String mtllib = "";

        String current_mesh = "";

        ArrayList<String> meshs_strings = new ArrayList<String>();

        LinkedHashMap<String,ArrayList<Integer>> mesh_faces = new LinkedHashMap<String,ArrayList<Integer>>();
        LinkedHashMap<String,String> mesh_mtl = new LinkedHashMap<String,String>();
        LinkedHashMap<String,Material> mtln_mtl = new LinkedHashMap<String,Material>();
        ArrayList<Vector3> model_vertices = new ArrayList<Vector3>();
        ArrayList<Vector3> model_vertex_normals = new ArrayList<Vector3>();
        ArrayList<Vector3> model_vertex_texture_coordinates = new ArrayList<Vector3>();

        while( true ) {
            try {
                line = file.readLine();
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(TAG,"Model import: Error 4: Model file line could not be read...");
                return false;
            }
            //end of file...
            if(line == null){
                break;
            }
            String list[] = line.split("\\s+"); //SkipEmptyParts

            if(list.length != 0){
                if(list[0].equalsIgnoreCase("mtllib")){
                    //list.last();
                    mtllib = list[list.length-1];
                }

                else if(list[0].equalsIgnoreCase("v")){
                    model_vertices.add( new Vector3(
                            Double.parseDouble(list[1]),
                            Double.parseDouble(list[2]),
                            Double.parseDouble(list[3])));
                }
                else if(list[0].equalsIgnoreCase("vn")){
                    model_vertex_normals.add( new Vector3(
                            Double.parseDouble(list[1]),
                            Double.parseDouble(list[2]),
                            Double.parseDouble(list[3])));
                }
                else if(list[0].equalsIgnoreCase("vt")){
                    model_vertex_texture_coordinates.add( new Vector3(
                            Double.parseDouble(list[1]),
                            Double.parseDouble(list[2]),
                            Double.parseDouble(list[3])));
                }
                else if(list[0].equalsIgnoreCase("g")){
                    current_mesh = list[1];
                    meshs_strings.add(current_mesh);
                    mesh_faces.put(current_mesh, new ArrayList<Integer>());
                }
                else if(list[0].equalsIgnoreCase("usemtl")){
                    mesh_mtl.put(current_mesh,list[1]);
                }
                else if(list[0].equalsIgnoreCase("f")){
                    String face_part_1_list[] = list[1].split("/"); //SkipEmptyParts
                    String face_part_2_list[] = list[2].split("/"); //SkipEmptyParts
                    String face_part_3_list[] = list[3].split("/"); //SkipEmptyParts

                    ArrayList<Integer> mesh_faces_array = mesh_faces.get(current_mesh);

                    mesh_faces_array.add(Integer.parseInt(face_part_1_list[0]));
                    mesh_faces_array.add(Integer.parseInt(face_part_1_list[1]));
                    mesh_faces_array.add(Integer.parseInt(face_part_1_list[2]));

                    mesh_faces_array.add(Integer.parseInt(face_part_2_list[0]));
                    mesh_faces_array.add(Integer.parseInt(face_part_2_list[1]));
                    mesh_faces_array.add(Integer.parseInt(face_part_2_list[2]));

                    mesh_faces_array.add(Integer.parseInt(face_part_3_list[0]));
                    mesh_faces_array.add(Integer.parseInt(face_part_3_list[1]));
                    mesh_faces_array.add(Integer.parseInt(face_part_3_list[2]));

                }
            }

        }
        try {
            file.close();
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG,"Model import: Error 5: Model file could not be closed...");
            return false;
        }


        //LOAD MTL DATA


        //copy the string list but without the last entry...
        String mtlpathlist[] = new String[pathlist.length-1];
        for(int i = 0; i < pathlist.length-1; i++){
            mtlpathlist[i] = pathlist[i];
        }


        String combined_mtl_path = combine(mtlpathlist,"/");

        String mtl_path;
        String tex_path;
        if(combined_mtl_path != null) {
            mtl_path = combine(mtlpathlist, "/") + "/" + mtllib;
            tex_path = combine(mtlpathlist, "/") + "/";
        }
        else{
            mtl_path = mtllib;
            tex_path = "";
        }


        InputStream ismtl = null;
        try {
            ismtl = am.open(mtl_path);
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG,"Model import: Error 6: InputStreamcould not be initialized...");
            return false;
        }
        InputStreamReader isrmtl = new InputStreamReader(ismtl);
        try {
            if(!isrmtl.ready()){
                Log.e(TAG,"Model import: Error 7: InputStreamReader could not be initialized...");
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG,"Model import: Error 7: InputStreamReader could not be initialized...");
            return false;
        }
        BufferedReader filemtl = new BufferedReader(isrmtl);


        try {
            if (!filemtl.ready())
            {
                Log.e(TAG,"Model import: Error 8: Model mtl file could not be loaded...");
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG,"Model import: Error 8: Model mtl file could not be loaded...");
            return false;
        }


        String mtlline = "";


        String current_mtl = "";

        ArrayList<String> mtls_strings = new ArrayList<String>();

        LinkedHashMap<String,Vector3>     mtl_ambient_c =       new LinkedHashMap<String,Vector3>();            //Ka
        LinkedHashMap<String,Vector3>     mtl_diffuse_c =       new LinkedHashMap<String,Vector3>();            //Kd
        LinkedHashMap<String,Vector3>     mtl_specular_c =      new LinkedHashMap<String,Vector3>();            //Ks
        LinkedHashMap<String,Float>       mtl_specular_ns =     new LinkedHashMap<String,Float>();              //Ns
        LinkedHashMap<String,Float>       mtl_transparency_d =  new LinkedHashMap<String,Float>();              //d
        LinkedHashMap<String,Float>       mtl_transparency_tr = new LinkedHashMap<String,Float>();              //Tr
        LinkedHashMap<String,Vector3>     mtl_transparency_tf = new LinkedHashMap<String,Vector3>();            //Tf
        LinkedHashMap<String,String>      mtl_ambient_map =     new LinkedHashMap<String,String>();             //map_Ka
        LinkedHashMap<String,String>      mtl_diffuse_map =     new LinkedHashMap<String,String>();             //map_Kd
        LinkedHashMap<String,String>      mtl_specular_map =    new LinkedHashMap<String,String>();             //map_Ks
        LinkedHashMap<String,String>      mtl_bump_map =        new LinkedHashMap<String,String>();             //map_bump
        LinkedHashMap<String,Integer>     mtl_illumination =    new LinkedHashMap<String,Integer>();            //illum

        //stream
        while( true ) {
            try {
                mtlline = filemtl.readLine();
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(TAG,"Model import: Error 9: Model mtl file line could not be read...");
                return false;
            }
            //end of file...
            if(mtlline == null){
                break;
            }


            String list[] = mtlline.split("\\s+"); //SkipEmptyParts


            if(list.length != 0){
                if(list[0].equalsIgnoreCase("newmtl")){
                    current_mtl = list[list.length-1];
                    mtls_strings.add(current_mtl);
                }
                //THE MTL FILE CONTAINS AN EMPTY ENTRY IN THE SPLITTED LIST ARRAY
                else if((list[0].equalsIgnoreCase("")) && (list.length > 1)) {
                    if (list[1].equalsIgnoreCase("Ka")) {
                        mtl_ambient_c.put(
                                current_mtl,
                                new Vector3(
                                        Double.parseDouble(list[2]),
                                        Double.parseDouble(list[3]),
                                        Double.parseDouble(list[4])));
                    } else if (list[1].equalsIgnoreCase("Kd")) {
                        mtl_diffuse_c.put(
                                current_mtl,
                                new Vector3(
                                        Double.parseDouble(list[2]),
                                        Double.parseDouble(list[3]),
                                        Double.parseDouble(list[4])));
                    } else if (list[1].equalsIgnoreCase("Ks")) {
                        mtl_specular_c.put(
                                current_mtl,
                                new Vector3(
                                        Double.parseDouble(list[2]),
                                        Double.parseDouble(list[3]),
                                        Double.parseDouble(list[4])));
                    } else if (list[1].equalsIgnoreCase("Ns")) {
                        mtl_specular_ns.put(current_mtl, Float.parseFloat(list[2]));

                    } else if (list[1].equalsIgnoreCase("d")) {
                        mtl_transparency_d.put(current_mtl, Float.parseFloat(list[2]));

                    } else if (list[1].equalsIgnoreCase("Tr")) {
                        mtl_transparency_tr.put(current_mtl, Float.parseFloat(list[2]));

                    } else if (list[1].equalsIgnoreCase("Tf")) {
                        mtl_transparency_tf.put(
                                current_mtl,
                                new Vector3(
                                        Double.parseDouble(list[2]),
                                        Double.parseDouble(list[3]),
                                        Double.parseDouble(list[4])));
                    } else if (list[1].equalsIgnoreCase("map_Ka")) {
                        //  \\\\ might be possible error cause...
                        String mtl_split_list[] = list[2].split("\\\\");
                        //last element
                        String mtl = mtl_split_list[mtl_split_list.length - 1];
                        mtl_ambient_map.put(
                                current_mtl,
                                mtl);
                    } else if (list[1].equalsIgnoreCase("map_Kd")) {
                        //  \\\\ might be possible error cause...
                        String mtl_split_list[] = list[2].split("\\\\");
                        //last element
                        String mtl = mtl_split_list[mtl_split_list.length - 1];
                        mtl_diffuse_map.put(
                                current_mtl,
                                mtl);
                    } else if (list[1].equalsIgnoreCase("map_Ks")) {
                        //  \\\\ might be possible error cause...
                        String mtl_split_list[] = list[2].split("\\\\");
                        //last element
                        String mtl = mtl_split_list[mtl_split_list.length - 1];
                        mtl_specular_map.put(
                                current_mtl,
                                mtl);
                    } else if ((list[1].equalsIgnoreCase("map_bump")) || (list[0].equalsIgnoreCase("bump"))) {
                        //  \\\\ might be possible error cause...
                        String mtl_split_list[] = list[2].split("\\\\");
                        //last element
                        String mtl = mtl_split_list[mtl_split_list.length - 1];
                        mtl_bump_map.put(
                                current_mtl,
                                mtl);
                    } else if (list[1].equalsIgnoreCase("illum")) {
                        mtl_illumination.put(current_mtl, Integer.parseInt(list[2]));
                    }
                }
            }
        }
        //stream end


        try {
            filemtl.close();
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG,"Model import: Error 10: Model mtl file could not be closed...");
            return false;
        }


        //CREATE MTLS (if needed...)

        //using diffuse mat cause its the major map used (other maps are optional)...


        for(int i = 0; i < mtls_strings.size(); i++){
            String mtl_map_index = mtls_strings.get(i);
            Material mtl = new Material(mtl_map_index, tex_path);
            //everything is optional ... so lets check if we even have something here...

            //color
            if(mtl_ambient_c.containsKey(mtl_map_index))
                mtl.set_ambient_c(mtl_ambient_c.get(mtl_map_index));
            if(mtl_diffuse_c.containsKey(mtl_map_index))
                mtl.set_diffuse_c(mtl_diffuse_c.get(mtl_map_index));
            if(mtl_specular_c.containsKey(mtl_map_index))
                mtl.set_specular_c(mtl_specular_c.get(mtl_map_index));

            //mtl settings
            if(mtl_specular_ns.containsKey(mtl_map_index))
                mtl.set_specular_ns(mtl_specular_ns.get(mtl_map_index));
            if(mtl_transparency_d.containsKey(mtl_map_index))
                mtl.set_transparency_d(mtl_transparency_d.get(mtl_map_index));
            if(mtl_transparency_tr.containsKey(mtl_map_index))
                mtl.set_transparency_tr(mtl_transparency_tr.get(mtl_map_index));
            if(mtl_transparency_tf.containsKey(mtl_map_index))
                mtl.set_transparency_tf(mtl_transparency_tf.get(mtl_map_index));

            //maps
            if(mtl_ambient_map.containsKey(mtl_map_index)) {
                mtl.set_ambient_map_name(mtl_ambient_map.get(mtl_map_index));
                Log.e(TAG,"Model import: ambient map:" + mtl.get_ambient_map_name());
            }
            if(mtl_diffuse_map.containsKey(mtl_map_index)) {
                mtl.set_diffuse_map_name(mtl_diffuse_map.get(mtl_map_index));
                Log.e(TAG,"Model import: diffuse map:" + mtl.get_diffuse_map_name());
            }
            if(mtl_specular_map.containsKey(mtl_map_index)) {
                mtl.set_specular_map_name(mtl_specular_map.get(mtl_map_index));
                Log.e(TAG,"Model import: specular map:" + mtl.get_specular_map_name());
            }
            if(mtl_bump_map.containsKey(mtl_map_index)) {
                mtl.set_bump_map_name(mtl_bump_map.get(mtl_map_index));
                Log.e(TAG,"Model import: bump map:" + mtl.get_bump_map_name());
            }

            //illuminate ?
            if(mtl_illumination.containsKey(mtl_map_index))
                mtl.set_illumination(mtl_illumination.get(mtl_map_index));

            //init texture maps

            //as this function gets called in a thread we need to do this in main...
            /*
            mtl->load_ambient_map(tex_path + mtl_ambient_map[mtl_names.value(i)]);
            mtl->load_diffuse_map(tex_path + mtl_diffuse_map[mtl_names.value(i)]);
            mtl->load_specular_map(tex_path + mtl_specular_map[mtl_names.value(i)]);
            mtl->load_bump_map(tex_path + mtl_bump_map[mtl_names.value(i)]);
            */

            Log.e(TAG,"Model import: TEXTURE PATH: " + tex_path);

            mtl.set_ambient_map_path(tex_path + mtl_ambient_map.get(mtl_map_index));
            mtl.set_diffuse_map_path(tex_path + mtl_diffuse_map.get(mtl_map_index));
            mtl.set_specular_map_path(tex_path + mtl_specular_map.get(mtl_map_index));
            mtl.set_bump_map_path(tex_path + mtl_bump_map.get(mtl_map_index));

            mtl.loadData();

            /*
            qDebug("        MTL ambient m:   " + mtl->get_ambient_map_name().toUtf8());
            qDebug("        MTL diffuse m:   " + mtl->get_diffuse_map_name().toUtf8());
            qDebug("        MTL specular m:  " + mtl->get_specular_map_name().toUtf8());
            qDebug("        MTL bump m:      " + mtl->get_bump_map_name().toUtf8());
            */

            mtln_mtl.put(mtl_map_index, mtl);
        }



        //CREATE MESHS (if needed...)
        //QMap<QString,QVector<QVector3D> > mesh_faces;
        //QMap<QString,QString> mesh_mtl;
        //QVector<QVector3D> model_vertices;
        //QVector<QVector3D> model_vertex_normals;
        //QVector<QVector3D> model_vertex_texture_coordinates;

        //using mesh_mtl to iterate ...



        for(int i = 0; i < meshs_strings.size(); i++){
            String mesh_map_index = meshs_strings.get(i);

            //min/max vertex pos on all 3 axis
            double v_min_x = 0.0f;
            double v_max_x = 0.0f;

            double v_min_y = 0.0f;
            double v_max_y = 0.0f;

            double v_min_z = 0.0f;
            double v_max_z = 0.0f;


            int triangle_count = mesh_faces.get(mesh_map_index).size() / 3 / 3;
            //qDebug("        Triangles: %i",triangle_count);
            float vertices[]    = new float[mesh_faces.get(mesh_map_index).size()];
            float texcoords[]   = new float[mesh_faces.get(mesh_map_index).size()]; //should be wrong ... 108/3*2 is right ...
            float normals[]     = new float[mesh_faces.get(mesh_map_index).size()];

            //qDebug("Mesh...");

            for(int j = 0; j < mesh_faces.get(mesh_map_index).size(); j+=9){
                //  1 v/vt/vn   2 v/vt/vn   3 v/vt/vn

                //  v
                Vector3 vertex1 =  model_vertices.get(mesh_faces.get(mesh_map_index).get(j)  -1);
                vertices[j]     = (float) vertex1.x();
                vertices[j+1]   = (float) vertex1.y();
                vertices[j+2]   = (float) vertex1.z();

                Vector3 vertex2 =  model_vertices.get(mesh_faces.get(mesh_map_index).get(j+3)-1);
                vertices[3+j]   = (float) vertex2.x();
                vertices[3+j+1] = (float) vertex2.y();
                vertices[3+j+2] = (float) vertex2.z();

                Vector3 vertex3 =  model_vertices.get(mesh_faces.get(mesh_map_index).get(j+6)-1);
                vertices[6+j]   = (float) vertex3.x();
                vertices[6+j+1] = (float) vertex3.y();
                vertices[6+j+2] = (float) vertex3.z();

                //get the min/max vertex pos on all 3 axis
                //x axis
                v_min_x = min_value(v_min_x,vertex1.x());
                v_min_x = min_value(v_min_x,vertex2.x());
                v_min_x = min_value(v_min_x,vertex3.x());

                v_max_x = max_value(v_max_x,vertex1.x());
                v_max_x = max_value(v_max_x,vertex2.x());
                v_max_x = max_value(v_max_x,vertex3.x());

                //y axis
                v_min_y = min_value(v_min_y,vertex1.y());
                v_min_y = min_value(v_min_y,vertex2.y());
                v_min_y = min_value(v_min_y,vertex3.y());

                v_max_y = max_value(v_max_y,vertex1.y());
                v_max_y = max_value(v_max_y,vertex2.y());
                v_max_y = max_value(v_max_y,vertex3.y());

                //z axis
                v_min_z = min_value(v_min_z,vertex1.z());
                v_min_z = min_value(v_min_z,vertex2.z());
                v_min_z = min_value(v_min_z,vertex3.z());

                v_max_z = max_value(v_max_z,vertex1.z());
                v_max_z = max_value(v_max_z,vertex2.z());
                v_max_z = max_value(v_max_z,vertex3.z());




                //  vt  (t value inverted)
                Vector3 texcoord1 = model_vertex_texture_coordinates.get(mesh_faces.get(mesh_map_index).get(j+1)-1);
                texcoords[j]     = (float) texcoord1.x();
                texcoords[j+1]   = (float) -texcoord1.y();
                texcoords[j+2]   = (float) texcoord1.z();

                Vector3 texcoord2 = model_vertex_texture_coordinates.get(mesh_faces.get(mesh_map_index).get(j+4)-1);
                texcoords[3+j]   = (float) texcoord2.x();
                texcoords[3+j+1] = (float) -texcoord2.y();
                texcoords[3+j+2] = (float) texcoord2.z();

                Vector3 texcoord3 = model_vertex_texture_coordinates.get(mesh_faces.get(mesh_map_index).get(j+7)-1);
                texcoords[6+j]   = (float) texcoord3.x();
                texcoords[6+j+1] = (float) -texcoord3.y();
                texcoords[6+j+2] = (float) texcoord3.z();


                //  vn
                Vector3 normal1 = model_vertex_normals.get(mesh_faces.get(mesh_map_index).get(j+2)-1);
                normal1.normalize();

                //normalize
                normals[j]     = (float) normal1.x();
                normals[j+1]   = (float) normal1.y();
                normals[j+2]   = (float) normal1.z();


                Vector3 normal2 = model_vertex_normals.get(mesh_faces.get(mesh_map_index).get(j+5)-1);
                normal2.normalize();

                //normalize
                normals[3+j]   = (float) normal2.x();
                normals[3+j+1] = (float) normal2.y();
                normals[3+j+2] = (float) normal2.z();


                Vector3 normal3 = model_vertex_normals.get(mesh_faces.get(mesh_map_index).get(j+8)-1);
                normal3.normalize();

                //normalize
                normals[6+j]   = (float) normal3.x();
                normals[6+j+1] = (float) normal3.y();
                normals[6+j+2] = (float) normal3.z();


            }



            Vector3 vert1 = new Vector3(v_min_x, v_min_y, v_min_z);
            Vector3 vert2 = new Vector3(v_max_x, v_max_y, v_max_z);

            Vector3 bounding_sphere_pos = new Vector3((v_min_x + v_max_x)/2.0f, (v_min_y + v_max_y)/2.0f, (v_min_z + v_max_z)/2.0f);
            double bounding_sphere_radius = vert1.distance(vert2) / 2.0f;


            //bounding_sphere_radius = 10.0;

            //Create Mesh and add it to the Mesh-list of the model.
            Mesh mesh = new Mesh(mesh_map_index, triangle_count, vertices, texcoords, normals,
                    mtln_mtl.get(mesh_mtl.get(mesh_map_index)));

            mesh.setBoundingSpherePos(bounding_sphere_pos);
            mesh.setBoundingSphereRadius(bounding_sphere_radius);

            //meshs.append(mesh);
            mdl.add_mesh(mesh);

        }

        mdl.set_path(path);

        Log.e(TAG,"Model import: done.");

        return true;
    }


}
