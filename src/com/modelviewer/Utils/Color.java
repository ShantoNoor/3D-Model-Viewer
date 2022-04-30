package com.modelviewer.Utils;

import org.joml.Vector3f;
import org.joml.Vector4f;

public class Color {
    public float r;
    public float g;
    public float b;
    public float a;

    public Color(float r, float g, float b, float a) {
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
    }

    public Color(float r, float g, float b) {
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = 1.0f;
    }

    public Color(float v) {
        this.r = v;
        this.g = v;
        this.b = v;
        this.a = v;
    }

    public float getR() {
        return r;
    }

    public void setR(float r) {
        this.r = r;
    }

    public float getG() {
        return g;
    }

    public void setG(float g) {
        this.g = g;
    }

    public float getB() {
        return b;
    }

    public void setB(float b) {
        this.b = b;
    }

    public float getA() {
        return a;
    }

    public void setA(float a) {
        this.a = a;
    }

    public Vector4f toVec4f(){
        return new Vector4f(r, g, b, a);
    }

    public Vector3f toVec3f(){
        return new Vector3f(r, g, b);
    }
}
