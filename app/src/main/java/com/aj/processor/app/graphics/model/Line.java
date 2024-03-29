package com.aj.processor.app.graphics.model;

import com.aj.processor.app.mathematics.Vector.Vector3;
import com.aj.processor.app.mathematics.Vector.Vector4;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * Created by AJ on 12.11.2014.
 *
 * A simple geometry class representing a line...
 * Doesn't even needs a mesh... or a material...
 * We won't use VBOs for lines because lines might change their
 * vertex position during runtime more often than a "static mesh"
 * thus we won't upload our geometry into GPU mem-side buffers...
 *
 * Our lines won't be that huge, so we won't upload much data and
 * cause high BUS traffic anyways...
 *
 * 1 INT vs 2 * 3 FLOATS ...
 */
public class Line {

    //the buffer that stores our vertices...
    private FloatBuffer vertexBuffer;

    // number of coordinates per vertex in this array
    private int COORDS_PER_VERTEX = 3;
    private float lineCoords[] = {
            -999.0f, 999.0f, -999.0f,
            999.0f, -999.0f, 999.0f
    };
    private int vertexCount = lineCoords.length / COORDS_PER_VERTEX;
    private  int vertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per vertex

    //default white
    private float color[] = { 1.0f, 0.0f, 0.0f, 1.0f };

    //init a simple line with 2 vertices
    public Line(){
        //see Mesh class for more details...
        ByteBuffer bb = ByteBuffer.allocateDirect(
                lineCoords.length * 4);
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(lineCoords);
        vertexBuffer.position(0);
    }

    public Line(float v0, float v1, float v2, float v3, float v4, float v5,
                float red, float green, float blue, float alpha){
        lineCoords[0] = v0;
        lineCoords[1] = v1;
        lineCoords[2] = v2;
        lineCoords[3] = v3;
        lineCoords[4] = v4;
        lineCoords[5] = v5;

        color[0] = red;
        color[1] = green;
        color[2] = blue;
        color[3] = alpha;

        //see Mesh class for more details...
        ByteBuffer bb = ByteBuffer.allocateDirect(
                lineCoords.length * 4);
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(lineCoords);
        vertexBuffer.position(0);
    }

    public Line( Vector3 p1, Vector3 p2, Vector4 c){
        lineCoords[0] = (float) p1.x();
        lineCoords[1] = (float) p1.y();
        lineCoords[2] = (float) p1.z();
        lineCoords[3] = (float) p2.x();
        lineCoords[4] = (float) p2.y();
        lineCoords[5] = (float) p2.z();

        color[0] = (float) c.x();
        color[1] = (float) c.y();
        color[2] = (float) c.z();
        color[3] = (float) c.w();

        //see Mesh class for more details...
        ByteBuffer bb = ByteBuffer.allocateDirect(
                lineCoords.length * 4);
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(lineCoords);
        vertexBuffer.position(0);
    }

    public void setVertices(float v0, float v1, float v2, float v3, float v4, float v5)
    {
        lineCoords[0] = v0;
        lineCoords[1] = v1;
        lineCoords[2] = v2;
        lineCoords[3] = v3;
        lineCoords[4] = v4;
        lineCoords[5] = v5;

        vertexBuffer.put(lineCoords);
        vertexBuffer.position(0);
    }

    public FloatBuffer getVertexBuffer(){
        return vertexBuffer;
    }

    public void setColor(float red, float green, float blue, float alpha)
    {
        color[0] = red;
        color[1] = green;
        color[2] = blue;
        color[3] = alpha;
    }

    public float[] getColor(){
        return color;
    }

    public int getCoordsPerVertex(){
        return COORDS_PER_VERTEX;
    }

    public int getVertexStride(){
        return vertexStride;
    }

    public int getVertexCount(){
        return vertexCount;
    }

}
