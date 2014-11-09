package com.aj.processor.app.graphics.model.Components;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.util.Log;

import com.aj.processor.app.GlobalContext;
import com.aj.processor.app.mathematics.Vector.Vector3;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by AJ on 28.10.2014.
 */
public class Material {

    private String TAG = "Material";

    private String mtl_name;
    private String mtl_path;

    private Vector3 mtl_ambient_c;
    private Vector3 mtl_diffuse_c;
    private Vector3 mtl_specular_c;
    private float mtl_specular_ns;
    private float mtl_specular_ni;
    private float mtl_transparency_d;
    private float mtl_transparency_tr;
    private Vector3 mtl_transparency_tf;


    private boolean mtl_ambient_loaded;
    private boolean mtl_diffuse_loaded;
    private boolean mtl_specular_loaded;
    private boolean mtl_bump_loaded;

    private Bitmap  mtl_ambient_img;
    private Bitmap  mtl_diffuse_img;
    private Bitmap  mtl_specular_img;
    private Bitmap  mtl_bump_img;

    private String mtl_ambient_map;
    private String mtl_diffuse_map;
    private String mtl_specular_map;
    private String mtl_bump_map;

    private String mtl_ambient_map_path;
    private String mtl_diffuse_map_path;
    private String mtl_specular_map_path;
    private String mtl_bump_map_path;


    //tex handles for GLES
    private int gl_ambient_map;
    private int gl_diffuse_map;
    private int gl_specular_map;
    private int gl_bump_map;

    private int mtl_illumination;


    //texture slots
    private int tex_slots;


    private boolean loaded = false;

    private int flagBits;


    private int Material_Type_None            = 0x0001;   // No Texture   map defined...
    private int Material_Type_Ambient         = 0x0002;   // Ambient      map defined
    private int Material_Type_Diffuse         = 0x0004;   // Diffuse      map defined
    private int Material_Type_Specular        = 0x0008;   // Specular     map defined
    private int Material_Type_Bump            = 0x0010;   // Bump         map defined


    public Material(String name,String path)
    {
        mtl_name = name;
        mtl_path = path;
        mtl_ambient_loaded = false;
        mtl_diffuse_loaded = false;
        mtl_specular_loaded =false;
        mtl_bump_loaded = false;
        tex_slots = 4;
        loaded = false;
    }

    //Material::~Material();
    public void Material_detroy(){
        //qDebug("Material DESTRUCTOR CALLED");
        //glDeleteTextures(tex_slots,gl_mtls);
        //might need to delete those...

        //qDebug("Material DESTRUCTOR FINISHED");
    }

    public void loadData(){
        //load data, GL not involved
        mtl_ambient_img = load_map_rgba(mtl_ambient_map_path);
        if(mtl_ambient_img != null){
            mtl_ambient_loaded = true;
            int gl_map[] = new int[1];
            GLES20.glGenTextures(1, gl_map, 0);
            if(gl_map[0] == 0){
                Log.e(TAG, "ambient_map slot could not be generated");
            }
            gl_ambient_map = gl_map[0];
        }
        mtl_diffuse_img = load_map_rgba(mtl_diffuse_map_path);
        if(mtl_diffuse_img != null){
            mtl_diffuse_loaded = true;
            int gl_map[] = new int[1];
            GLES20.glGenTextures(1, gl_map, 0);
            if(gl_map[0] == 0){
                Log.e(TAG, "diffuse_map slot could not be generated");
            }
            gl_diffuse_map = gl_map[0];
        }
        mtl_specular_img = load_map_rgba(mtl_specular_map_path);
        if(mtl_specular_img != null){
            mtl_specular_loaded = true;
            int gl_map[] = new int[1];
            GLES20.glGenTextures(1, gl_map, 0);
            if(gl_map[0] == 0){
                Log.e(TAG, "specular_map slot could not be generated");
            }
            gl_specular_map = gl_map[0];
        }
        mtl_bump_img = load_map_rgba(mtl_bump_map_path);
        if(mtl_bump_img != null){
            mtl_bump_loaded = true;
            int gl_map[] = new int[1];
            GLES20.glGenTextures(1, gl_map, 0);
            if(gl_map[0] == 0){
                Log.e(TAG, "bump_map slot could not be generated");
            }
            gl_bump_map = gl_map[0];
        }
    }

    public void loadGLdata(){
        //don't load again
        if(loaded){
            return;
        }

        //qDebug("loading material");

        if(mtl_ambient_loaded && mtl_ambient_img!=null){
            Log.e(TAG, "gl_ambient_map loading...");
            load_gl_map(gl_ambient_map, mtl_ambient_img);
        }
        else{
            Log.e(TAG, "gl_ambient_map loading skipped...");
        }
        if(mtl_diffuse_loaded && mtl_diffuse_img!=null){
            Log.e(TAG, "gl_diffuse_map loading...");
            load_gl_map(gl_diffuse_map, mtl_diffuse_img);
        }
        else{
            Log.e(TAG, "gl_diffuse_map loading skipped...");
        }
        if(mtl_specular_loaded && mtl_specular_img!=null){
            Log.e(TAG, "gl_specular_map loading...");
            load_gl_map(gl_specular_map, mtl_specular_img);
        }
        else{
            Log.e(TAG, "gl_specular_map loading skipped...");
        }
        if(mtl_bump_loaded && mtl_bump_img!=null){
            Log.e(TAG, "gl_bump_map loading...");
            load_gl_map(gl_bump_map, mtl_bump_img);
        }
        else{
            Log.e(TAG, "gl_bump_map loading skipped...");
        }
        //qDebug("loaded material!");
        loaded = true;
    }

    public boolean isLoaded(){
        return loaded;
    }

    public boolean load_gl_map(int slot, Bitmap image){
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, slot);

        /* WORKS (but no mipmaps and no filtering)
        GLES20.glTexParameteri( GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameteri( GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, image, 0);
        */




        // Set filtering
        //GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
        //GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);

        // Load the bitmap into the bound texture.
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, image, 0);

        GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_2D);  //Generate num_mipmaps number of mipmaps here.
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT);

        //for mipmaps smooth
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR_MIPMAP_LINEAR);

        //mipmaps pixelated
        //glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        //glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST_MIPMAP_NEAREST);


        //smooth
        //glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        //glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

        //pixelated
        //glTexParameteri( GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        //glTexParameteri( GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

        //qDebug("mipmap generated...");


        //clean up the image...
        //image.~QImage();
        image.recycle();

        // Unbind from the texture.
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);

        if(slot == 0){
            //looks like we couldn't load the texture and GLES gave us back the default black tex...

            Log.e(TAG,"tex slot " + slot + " could not be loaded...");

            return false;
        }
        return true;
    }


    public String get_name(){
        return mtl_name;
    }

    public Vector3 get_ambient_c(){
        return mtl_ambient_c;
    }

    public Vector3 get_diffuse_c(){
        return mtl_diffuse_c;
    }

    public Vector3 get_specular_c(){
        return mtl_specular_c;
    }

    public float get_specular_ns(){
        return mtl_specular_ns;
    }

    public float get_specular_ni(){
        return mtl_specular_ni;
    }

    public float get_transparency_d(){
        return mtl_transparency_d;
    }

    public float get_transparency_tr(){
        return mtl_transparency_tr;
    }

    public Vector3 get_transparency_tf(){
        return mtl_transparency_tf;
    }

    public String get_ambient_map_name(){
        return mtl_ambient_map;
    }

    public int get_ambient_map_texture(){
        return gl_ambient_map;
    }

    public String get_diffuse_map_name(){
        return mtl_diffuse_map;
    }

    public int get_diffuse_map_texture(){
        return gl_diffuse_map;
    }

    public String get_specular_map_name(){
        return mtl_specular_map;
    }

    public int get_specular_map_texture(){
        return gl_specular_map;
    }

    public String get_bump_map_name(){
        return mtl_bump_map;
    }

    public int get_bump_map_texture(){
        return gl_bump_map;
    }

    public int get_illumination(){
        return mtl_illumination;
    }


    //set
    public void set_name(String name){
        mtl_name = name;
    }

    public void set_ambient_c(Vector3 color){
        mtl_ambient_c = color;
    }

    public void set_diffuse_c(Vector3 color){
        mtl_diffuse_c = color;
    }

    public void set_specular_c(Vector3 color){
        mtl_specular_c = color;
    }

    public void set_specular_ns(float value){
        mtl_specular_ns = value;
    }

    public void set_specular_ni(float value){
        mtl_specular_ni = value;
    }

    public void set_transparency_d(float value){
        mtl_transparency_d = value;
    }

    public void set_transparency_tr(float value){
        mtl_transparency_tr = value;
    }

    public void set_transparency_tf(Vector3 color){
        mtl_transparency_tf = color;
    }


    public void set_ambient_map_name(String map_name){
        mtl_ambient_map = map_name;
    }

    public void set_diffuse_map_name(String map_name){
        mtl_diffuse_map = map_name;
    }

    public void set_specular_map_name(String map_name){
        mtl_specular_map = map_name;
    }

    public void set_bump_map_name(String map_name){
        mtl_bump_map = map_name;
    }

    public void set_illumination(int value){
        mtl_illumination = value;
    }

    public void set_ambient_map_path(String map_path){
        mtl_ambient_map_path = map_path;
    }

    public void set_diffuse_map_path(String map_path){
        mtl_diffuse_map_path = map_path;
    }

    public void set_specular_map_path(String map_path){
        mtl_specular_map_path = map_path;
    }

    public void set_bump_map_path(String map_path){
        mtl_bump_map_path = map_path;
    }


    private Bitmap load_map_rgba(String path){

        if(path == null){
            return null;
        }

        AssetManager assetManager = GlobalContext.getAppContext().getAssets();
        InputStream istr = null;
        try {
            istr = assetManager.open(path);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Rect outPadding = new Rect();
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;
        // Read in the resource
        //image = BitmapFactory.decodeStream(istr);
        Bitmap image = BitmapFactory.decodeStream(istr, outPadding, options);

        if(image == null){
            Log.e(TAG,"Bitmap: " + path + " could not decode the Stream...");
            return null;
        }
        else{
            Log.e(TAG, "Bitmap: " + path + " width: " + image.getWidth() + " height: " + image.getHeight());

            int pix = image.getPixel(0,0);
            Log.e(TAG,"color: " + pix);
        }
        return image;
    }
}