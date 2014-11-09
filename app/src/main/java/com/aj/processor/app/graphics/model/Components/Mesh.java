package com.aj.processor.app.graphics.model.Components;

import android.opengl.GLES20;
import android.util.Log;

import com.aj.processor.app.mathematics.Vector.Vector3;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * Created by AJ on 28.10.2014.
 */
public class Mesh {

    private String TAG = "MESH";

    int bytes_per_float = 4;

    String mesh_name;
    Material material;

    int triangle_count;

    //pointer to float arrays
    float vertices[];
    float texcoords[];
    float normals[];


    public FloatBuffer vertex_pos_buffer;
    public FloatBuffer tex_coord_buffer;
    public FloatBuffer normal_buffer;

    //vertex buffer objects (vertices, texcoords and normals)
    int vertex_vbo;
    int texcoord_vbo;
    int normal_vbo;

    int vbs[] = new int[3];

    int vertex_array_object;

    boolean loaded;

    //for frustum culling
    //the bounding sphere in model space
    Vector3 bounding_sphere_position;
    double bounding_sphere_radius;


    public Mesh(String name, int triangle_count, float vertices[], float texcoords[], float normals[], Material material)
    {
        loaded = false;
        mesh_name = name;

        this.triangle_count = triangle_count;

        this.vertices  = vertices;
        this.texcoords = texcoords;
        this.normals   = normals;

        this.material  = material;
    }

    //Mesh::~Mesh(){
    public void Mesh_destroy(){
        //delete material;

        /*
        material = NULL;
        glDeleteBuffers(1,&vertex_vbo);
        glDeleteBuffers(1,&texcoord_vbo);
        glDeleteBuffers(1,&normal_vbo);
        vertex_vbo = 0;
        texcoord_vbo = 0;
        normal_vbo = 0;
        delete [] vertices;
        delete [] texcoords;
        delete [] normals;
        vertices = NULL;
        texcoords = NULL;
        normals = NULL;
        */
    }

    public String get_name(){
        return mesh_name;
    }

    public Material get_material(){
        return material;
    }

    public void set_material(Material mtl){
        material = mtl;
    }

    public int get_triangle_count(){
        return triangle_count;
    }

    public float[] get_vertices(){
        return vertices;
    }

    public float[] get_texcoords(){
        return texcoords;
    }

    public float[] get_normals(){
        return normals;
    }

    public int get_vertex_vbo(){
        return vbs[0];//vertex_vbo;
    }

    public int get_texcoord_vbo(){
        return vbs[1];//texcoord_vbo;
    }

    public int get_normal_vbo(){
        return vbs[2];//normal_vbo;
    }

    public int get_vertex_array_object(){
        return vertex_array_object;
    }

    public void loadGLdata(){
        //don't load again
        if(loaded){
            return;
        }


        //detailed comments for vertices:
        //generate buffers on gpu
        GLES20.glGenBuffers(3, vbs,0);


        // Allocate a direct block of memory on the native heap,
        // size in bytes is equal to cubePositions.length * BYTES_PER_FLOAT.
        // BYTES_PER_FLOAT is equal to 4, since a float is 32-bits, or 4 bytes.
        vertex_pos_buffer = ByteBuffer.allocateDirect(vertices.length * bytes_per_float)

        // Floats can be in big-endian or little-endian order.
        // We want the same as the native platform.
                .order(ByteOrder.nativeOrder())

        // Give us a floating-point view on this byte buffer.
                .asFloatBuffer();

        //Transferring data from the Java heap to the native heap is then a matter of a couple calls:
        // Copy data from the Java heap to the native heap.
        vertex_pos_buffer.put(vertices)

        // Reset the buffer position to the beginning of the buffer.
                .position(0);




        //Bind the vertices buffer and give OpenGL the data
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbs[0]);
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, triangle_count * 3* 3 * bytes_per_float, vertex_pos_buffer, GLES20.GL_STATIC_DRAW);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
        //glVertexAttribPointer(0, 3, GL_FLOAT, GL_FALSE, 0, 0);
        //glEnableVertexAttribArray(0);



        tex_coord_buffer = ByteBuffer.allocateDirect(texcoords.length * bytes_per_float).order(ByteOrder.nativeOrder()).asFloatBuffer();
        tex_coord_buffer.put(texcoords).position(0);

        /*
        for(int i = 0; i < texcoords.length; i+=3){
            Log.e(TAG, "texcoord: " + texcoords[i] + " " + texcoords[i+1] + " " + texcoords[i+2]);
        }
        */

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbs[1]);
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, triangle_count * 3* 3 * bytes_per_float, tex_coord_buffer, GLES20.GL_STATIC_DRAW);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
        //glVertexAttribPointer(1, 3, GL_FLOAT, GL_FALSE, 0, 0);
        //glEnableVertexAttribArray(1);



        //normals
        normal_buffer = ByteBuffer.allocateDirect(normals.length * bytes_per_float).order(ByteOrder.nativeOrder()).asFloatBuffer();
        normal_buffer.put(normals).position(0);

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbs[2]);
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, triangle_count * 3* 3 * bytes_per_float, normal_buffer, GLES20.GL_STATIC_DRAW);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
        //glVertexAttribPointer(2, 3, GL_FLOAT, GL_FALSE, 0, 0);
        //glEnableVertexAttribArray(2);



        //qDebug("loaded mesh!");

        loaded = true;

    }

    public boolean isLoaded(){
        return loaded;
    }

    public void set_vertex(int index, float x, float y, float z){

        float vertex[] = new float[3];
        vertex[0] = x;
        vertex[1] = y;
        vertex[2] = z;

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vertex_vbo);
        //GLES20.glBufferSubData(GLES20.GL_ARRAY_BUFFER, sizeof(float)*3*index, sizeof(float)*3, vertex);
        //construct a Buffer

        FloatBuffer vertexBuf = FloatBuffer.wrap(vertex);

        GLES20.glBufferSubData(GLES20.GL_ARRAY_BUFFER, bytes_per_float*3*index, bytes_per_float*3, vertexBuf);

        vertices[index*3]   = x;
        vertices[index*3+1] = y;
        vertices[index*3+2] = z;
    }

    public void set_vertex(int index, Vector3 vector){
        set_vertex(index, (float) vector.x(), (float) vector.y(), (float) vector.z());
    }

    public Vector3 get_vertex(int index){
        return new Vector3(
                vertices[index*3],
                vertices[index*3+1],
                vertices[index*3+2]);
    }


    public Vector3 getBoundingSpherePos(){
        return bounding_sphere_position;
    }

    public double getBoundingSphereRadius(){
        return bounding_sphere_radius;
    }

    public void setBoundingSpherePos(Vector3 pos){
        this.bounding_sphere_position = pos;
    }

    public void setBoundingSphereRadius(double radius){
        this.bounding_sphere_radius = radius;
    }


}
