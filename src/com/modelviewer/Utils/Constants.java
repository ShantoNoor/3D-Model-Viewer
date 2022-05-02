package com.modelviewer.Utils;

import org.joml.Vector3f;

import static java.lang.Math.PI;

public class Constants {
    public static final float TO_RADIAN = (float)PI / 180;

    public static final Vector3f X_AXIS = new Vector3f(1.0f, 0.0f, 0.0f);
    public static final Vector3f Y_AXIS = new Vector3f(0.0f, 1.0f, 0.0f);
    public static final Vector3f Z_AXIS = new Vector3f(0.0f, 0.0f, 1.0f);
    public static final Vector3f UP = X_AXIS;
}
