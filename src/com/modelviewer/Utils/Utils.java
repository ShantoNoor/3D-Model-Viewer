package com.modelviewer.Utils;

import org.joml.*;
import org.lwjgl.BufferUtils;
import org.lwjgl.assimp.AIMatrix4x4;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;

import static org.lwjgl.glfw.GLFW.glfwGetTime;
import static org.lwjgl.opengl.GL11.glGetError;
import static org.lwjgl.opengl.GL11.glGetIntegerv;
import static org.lwjgl.opengl.GL20.GL_MAX_TEXTURE_IMAGE_UNITS;

public class Utils {
    public static float getTime() {
        return (float) glfwGetTime();
    }

    public static Vector3f mulVector3fWithMatrix4f(Vector3f vec3, Matrix4f mat4) {
        Vector4f vec4 = new Vector4f(vec3, 1.0f);
        vec4.mul(mat4, vec4);
        return new Vector3f(vec4.x, vec4.y, vec4.z);
    }

    public static Matrix4f convertAIMatrix4x4ToMatrix4f(AIMatrix4x4 transform) {
        Matrix4f mat = new Matrix4f();

        mat.m00(transform.a1());
        mat.m01(transform.b1());
        mat.m02(transform.c1());
        mat.m03(transform.d1());

        mat.m10(transform.a2());
        mat.m11(transform.b2());
        mat.m12(transform.c2());
        mat.m13(transform.d2());

        mat.m20(transform.a3());
        mat.m21(transform.b3());
        mat.m22(transform.c3());
        mat.m23(transform.d3());

        mat.m30(transform.a4());
        mat.m31(transform.b4());
        mat.m32(transform.c4());
        mat.m33(transform.d4());

        return mat;
    }

    public static float[] convertFloatArrayListToFloatArray(ArrayList<Float> floatArrayList) {
        float[] floatArray = new float[floatArrayList.size()];
        for(int i = 0; i < floatArrayList.size(); ++i) {
            floatArray[i] = floatArrayList.get(i);
        }
        return floatArray;
    }

    public static FloatBuffer convertFloatArrayToFloatBuffer(float[] floatArray) {
        FloatBuffer floatBuffer = BufferUtils.createFloatBuffer(floatArray.length);
        floatBuffer.put(floatArray);
        floatBuffer.flip();
        return floatBuffer;
    }

    public static FloatBuffer convertFloatArrayListToFloatBuffer(ArrayList<Float> floatArrayList) {
        float[] floatArray = convertFloatArrayListToFloatArray(floatArrayList);
        return convertFloatArrayToFloatBuffer(floatArray);
    }

    public static int[] convertIntArrayListToIntArray(ArrayList<Integer> intArrayList) {
        int[] intArray = new int[intArrayList.size()];
        for(int i = 0; i < intArrayList.size(); ++i) {
            intArray[i] = intArrayList.get(i);
        }
        return intArray;
    }

    public static IntBuffer convertIntArrayToIntBuffer(int[] data) {
        IntBuffer buffer = BufferUtils.createIntBuffer(data.length);
        buffer.put(data);
        buffer.flip();
        return buffer;
    }

    public static IntBuffer convertIntArrayListToIntBuffer(ArrayList<Integer> intArrayList) {
        int[] intArray = convertIntArrayListToIntArray(intArrayList);
        return convertIntArrayToIntBuffer(intArray);
    }

    public static void printVector3f(Vector3f vec3) {
        System.out.println(vec3.x + " " + vec3.y + " " + vec3.z);
    }

    public static void printVector3f(Vector3f vec3, String vec3Name) {
        System.out.println(vec3Name + ": " + vec3.x + " " + vec3.y + " " + vec3.z);
    }

    public static Vector3f mulVector3fWithQuaternionf(Vector3f vec3, Quaternionf quat) {
        Quaternionf pquat = new Quaternionf(vec3.x, vec3.y, vec3.z, 0.0f);
        Quaternionf rquat = new Quaternionf();
        quat.mul(pquat, rquat);
        rquat.mul(quat.invert(new Quaternionf()), rquat);
        return new Vector3f(rquat.x, rquat.y, rquat.z);
    }

    public static Vector3f rotateVector3fByAngleAndAxis(Vector3f vec3, float radianInAngle, Vector3f axisOfRotation) {
        Quaternionf quat = new Quaternionf(new AxisAngle4f(radianInAngle, axisOfRotation));
        return mulVector3fWithQuaternionf(vec3, quat);
    }

    public static AxisAngle4f axisAndAngleBetweenTwoVector3f(Vector3f vec31, Vector3f vec32) {
        float angle = vec31.angle(vec32);

        Vector3f axis = new Vector3f();
        vec31.cross(vec32, axis);
        axis.normalize();

        return new AxisAngle4f(angle, axis);
    }

    public static Vector3f getAxisAsVector3fFromAxisAngle4f(AxisAngle4f axisAngle) {
        return new Vector3f(axisAngle.x, axisAngle.y, axisAngle.z);
    }

    public static int queryNumberOfMaxTextureUnits() {
        IntBuffer maximumTextureUnits = BufferUtils.createIntBuffer(1);
        glGetIntegerv(GL_MAX_TEXTURE_IMAGE_UNITS, maximumTextureUnits);
        return maximumTextureUnits.get();
    }

    public static void printOpenGLError() {
        System.out.println(glGetError());
    }

    public static String convertByteBufferToString(ByteBuffer dat) {
        String ret = "";
        for (int k = 0; k < dat.capacity(); k++) {
            ret += (char)dat.get(k);
        }
        return ret.trim();
    }
}
