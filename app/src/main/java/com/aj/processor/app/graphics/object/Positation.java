package com.aj.processor.app.graphics.object;

import com.aj.processor.app.mathematics.Matrix.Matrix4x4;
import com.aj.processor.app.mathematics.Vector.Vector3;

/**
 * Created by AJ on 02.11.2014.
 */
public class Positation {

    private boolean matrix_changed;

    //translation, scale, rotation...
    private Vector3 pos;
    private double angle;
    private Vector3 rot;
    private Vector3 scl;

    private Matrix4x4 mat_pos = new Matrix4x4();
    private Matrix4x4 mat_rot = new Matrix4x4();
    private Matrix4x4 mat_scl = new Matrix4x4();

    private Matrix4x4 mat_m = new Matrix4x4();

    public Positation(){
        pos = new Vector3(0,0,0);
        angle = 0.0;
        rot = new Vector3(0,0,0);
        scl = new Vector3(1,1,1);

        matrix_changed = true;
    }

    public void set_data(Positation positation){
        this.pos = positation.pos;
        this.rot = positation.rot;
        this.scl = positation.scl;
        this.mat_pos = positation.mat_pos;
        this.mat_rot = positation.mat_rot;
        this.mat_scl = positation.mat_scl;
        this.mat_m = positation.mat_m;
        this.matrix_changed = positation.matrix_changed;
    }

    public Vector3 getPosition(){
        return pos;
    }

    public void set_position(double x, double y, double z){
        pos.set_x(x);
        pos.set_y(y);
        pos.set_z(z);

        set_matrix_pos();
    }

    public void set_position(Vector3 position){
        pos = position;

        set_matrix_pos();
    }


    public void set_rotation(double angle, double x, double y, double z){
        this.angle = angle;
        rot.set_x(x);
        rot.set_y(y);
        rot.set_z(z);
        set_matrix_rot();
    }

    public void set_rotation(double angle, Vector3 rotation){
        this.angle = angle;
        rot = rotation;
        set_matrix_rot();
    }

    public void clear_rotation(){
        mat_rot.set_to_identity();
        matrix_changed = true;
    }

    public void add_rotation(double angle, double x, double y, double z){
        mat_rot.rotate(angle, x, y, z);
        matrix_changed = true;
    }

    public void add_rotation(double angle, Vector3 rotation){
        mat_rot.rotate(angle, rotation);
        matrix_changed = true;
    }

    public void set_rotation_matrix(Matrix4x4 mat){
        mat_rot = mat;
        matrix_changed = true;
    }

    public void set_scale(double x, double y, double z){
        scl.set_x(x);
        scl.set_y(y);
        scl.set_z(z);
        set_matrix_scl();
    }

    public void set_scale(Vector3 scale){
        scl = scale;
        set_matrix_scl();
    }

    public Vector3 get_scale(){
        return scl;
    }

    public Matrix4x4 get_model_matrix(){
        build_model_matrix();
        return mat_m;
    }

    private void set_matrix_pos(){
        mat_pos.translate(pos);
        matrix_changed = true;
    }

    private void set_matrix_rot(){
        /*
        Matrix4x4 rot_x;
        Matrix4x4 rot_y;
        Matrix4x4 rot_z;
        rot_x.rotate_x(rot[0]);
        rot_y.rotate_y(rot[1]);
        rot_z.rotate_z(rot[2]);
        mat_rot = rot_x * rot_y * rot_z;
        */
        Matrix4x4 rotation = new Matrix4x4();
        rotation.rotate(angle,rot);
        mat_rot = rotation;
        matrix_changed = true;
    }

    private void set_matrix_scl(){
        mat_scl.scale(scl);
        matrix_changed = true;
    }

    private void build_model_matrix(){
        if(matrix_changed){
            mat_m =  mat_pos.multiply(mat_scl).multiply(mat_rot);
            matrix_changed = false;
        }
    }
}
