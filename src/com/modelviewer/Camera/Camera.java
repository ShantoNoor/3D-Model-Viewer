package com.modelviewer.Camera;

import com.modelviewer.Utils.Constants;
import com.modelviewer.Utils.Utils;
import com.modelviewer.Window.Input.KeyListener;
import com.modelviewer.Window.Input.MouseListener;
import org.joml.*;
import org.joml.Math;

import static org.lwjgl.glfw.GLFW.*;

public class Camera {
    private Vector3f position, UP, RIGHT, viewDirection, defaultPosition;
    private float movementSpeed, mouseSensitivity, zoomSpeed;
    private float pitch, yaw, pitchLimit;

    private Vector3f normalizedCameraRightVector;

    boolean firstClick;

    Quaternionf cameraRotations;
    private Vector3f previousOrbitMouseVector;

    CameraModes cameraModes = CameraModes.None;

    public Camera () {
        this.position = new Vector3f(0.0f, 0.0f, 0.0f);
        this.defaultPosition = new Vector3f(0.0f, 0.0f, 0.0f);
        this.UP = new Vector3f(0.0f, 1.0f, 0.0f);
        this.RIGHT = new Vector3f(1.0f, 0.0f, 0.0f);
        this.viewDirection = new Vector3f(0.0f, 0.0f, -1.0f);
        this.movementSpeed = 20.0f;
        this.mouseSensitivity = 1.0f;
        this.normalizedCameraRightVector = new Vector3f();
        this.pitchLimit = 45.0f * Constants.TO_RADIAN;
        this.previousOrbitMouseVector = new Vector3f(1.0f);
        this.firstClick = false;
        this.cameraRotations = new Quaternionf();
        this.zoomSpeed = 0.5f;

        calculateCameraVectors();
    }

    private void calculateCameraVectors() {
        viewDirection.cross(UP, normalizedCameraRightVector);
        normalizedCameraRightVector.normalize();
    }

    public Matrix4f getViewMatrix() {
        return new Matrix4f().lookAt(position, viewDirection.add(position, new Vector3f()), UP);
    }

    public void mouseUpdate(long glfwWindow, int windowWidth, int windowHeight) {
        Vector2f mousePosition = MouseListener.getXY();

        if( (mousePosition.x >= 0.0f) && (mousePosition.x <= (float) windowWidth) &&
                (mousePosition.y >= 0.0f) && (mousePosition.y <= (float) windowHeight) && cameraModes == CameraModes.None )
        {
            calculateCameraVectors();

            if(MouseListener.isMouseButtonPressed(GLFW_MOUSE_BUTTON_RIGHT)) {
                cameraModes = CameraModes.FirstPerson;
            } else if(MouseListener.isMouseButtonPressed(GLFW_MOUSE_BUTTON_LEFT)) {
                cameraModes = CameraModes.Orbit;
                firstClick = true;
            } else if(MouseListener.isScrollingOverMainGlfwWindow()) {
                cameraModes = CameraModes.Zoom;
            }
        }

        if(cameraModes == CameraModes.FirstPerson && MouseListener.isDraggingOverMainGlfwWindow()) {
            glfwSetInputMode(glfwWindow, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
            updateFirstPerson();
        } else if(cameraModes == CameraModes.Orbit && MouseListener.isDraggingOverMainGlfwWindow()) {
            updateOrbit(windowWidth, windowHeight);
        } else if(cameraModes == CameraModes.Zoom) {
            updateZoom();
            cameraModes = CameraModes.None;
        } else {
            glfwSetInputMode(glfwWindow, GLFW_CURSOR, GLFW_CURSOR_NORMAL);
            cameraModes = CameraModes.None;
        }
    }

    private void updateZoom() {
        float zoom = MouseListener.getScrollY();
        position.add(new Vector3f(viewDirection).mul(zoom * zoomSpeed).normalize());
        MouseListener.stopScrolling();
    }

    public void updateFirstPerson() {
        Quaternionf firstPersonRotation = new Quaternionf();
        Vector2f mouseDelta = MouseListener.getDxy();

        float dYaw = mouseDelta.x * mouseSensitivity * Constants.TO_RADIAN;
        yaw += dYaw;
        new Quaternionf(new AxisAngle4f(dYaw, UP)).mul(firstPersonRotation, firstPersonRotation);

        float dPitch = mouseDelta.y * mouseSensitivity * Constants.TO_RADIAN;

        if(pitch + dPitch >= -pitchLimit && pitch + dPitch <= pitchLimit) {
            pitch += dPitch;
            new Quaternionf(new AxisAngle4f(dPitch, normalizedCameraRightVector)).mul(firstPersonRotation, firstPersonRotation);
        }

        viewDirection = Utils.mulVector3fWithQuaternionf(viewDirection, firstPersonRotation);
        firstPersonRotation.mul(cameraRotations, cameraRotations);
    }

    public void updateOrbit(int windowWidth, int windowHeight) {
        Vector2f mousePos = MouseListener.getXY();
        mousePos.x = 2 * (mousePos.x - (windowWidth / 2)) / windowHeight;
        mousePos.y = 2 * (mousePos.y - (windowHeight / 2)) / windowHeight * -1.0f;

        float radius = 1;
        radius *= radius;

        float xPlusYSquare = mousePos.x * mousePos.x + mousePos.y * mousePos.y;

        float z;
        if(xPlusYSquare <= radius * 0.5f) {
            z = Math.sqrt(radius-xPlusYSquare);
        } else {
            z = 0.5f * radius * (1.0f / Math.sqrt(xPlusYSquare));
        }

        Vector3f orbitMouseVector = new Vector3f(mousePos.x, mousePos.y, z);
        orbitMouseVector.normalize();

        AxisAngle4f rotationAxisAngle = Utils.axisAndAngleBetweenTwoVector3f(orbitMouseVector, previousOrbitMouseVector);

        if(rotationAxisAngle.angle > 0.0f && !firstClick) {
            Vector3f axisOfRotation = Utils.getAxisAsVector3fFromAxisAngle4f(rotationAxisAngle);

            axisOfRotation = Utils.mulVector3fWithQuaternionf(axisOfRotation, cameraRotations);
            axisOfRotation.normalize();

            Quaternionf dOrbitRotation =  new Quaternionf(new AxisAngle4f(rotationAxisAngle.angle, axisOfRotation));
            dOrbitRotation.mul(cameraRotations, cameraRotations);

            position = Utils.mulVector3fWithQuaternionf(position, dOrbitRotation);
            viewDirection = Utils.mulVector3fWithQuaternionf(viewDirection, dOrbitRotation);
            UP = Utils.mulVector3fWithQuaternionf(UP, dOrbitRotation);
        }

        previousOrbitMouseVector = orbitMouseVector;
        firstClick = false;
    }

    public void keyboardUpdate(float dt) {
        calculateCameraVectors();

        Vector3f normalizedViewDirection = new Vector3f(viewDirection).normalize();

        if(KeyListener.get().isKeyPressed(GLFW_KEY_L)) {
            movementSpeed = 0.5f;
        }

        if(KeyListener.get().isKeyPressed(GLFW_KEY_K)) {
            movementSpeed = 9.5f;
        }

        if(KeyListener.get().isKeyPressed(GLFW_KEY_W)) {
            position.add(normalizedViewDirection.mul(movementSpeed * dt, new Vector3f()));
        }

        if(KeyListener.get().isKeyPressed(GLFW_KEY_S)) {
            position.add(normalizedViewDirection.mul(-1.0f * movementSpeed * dt, new Vector3f()));
        }

        if(KeyListener.get().isKeyPressed(GLFW_KEY_A)) {
            position.add(normalizedCameraRightVector.mul(-1.0f * movementSpeed * dt, new Vector3f()));
        }

        if(KeyListener.get().isKeyPressed(GLFW_KEY_D)) {
            position.add(normalizedCameraRightVector.mul(movementSpeed * dt, new Vector3f()));
        }

        if(KeyListener.get().isKeyPressed(GLFW_KEY_E)) {
            position.add(UP.mul(movementSpeed * dt, new Vector3f()));
        }

        if(KeyListener.get().isKeyPressed(GLFW_KEY_Q)) {
            position.add(UP.mul(-1.0f * movementSpeed * dt, new Vector3f()));
        }

        if(KeyListener.get().isKeyPressed(GLFW_KEY_R)) {
            resetCamera();
        }
    }

    public Vector3f getPosition() {
        return position;
    }

    public void setPosition(Vector3f position) {
        this.position = new Vector3f(position);
        this.defaultPosition = new Vector3f(position);
    }

    public Vector3f getViewDirection() {
        return viewDirection;
    }

    private void resetCamera() {
        position = new Vector3f(defaultPosition);
        UP = new Vector3f(0.0f, 1.0f, 0.0f);
        RIGHT = new Vector3f(1.0f, 0.0f, 0.0f);
        viewDirection = new Vector3f(0.0f, 0.0f, -1.0f);
        yaw = 0.0f;
        pitch = 0.0f;
        cameraRotations.identity();
    }

    public void clear() {
        position = null;
        UP = null;
        RIGHT = null;
        viewDirection = null;
        defaultPosition = null;
        normalizedCameraRightVector = null;
        cameraRotations = null;
        previousOrbitMouseVector = null;
        cameraModes = null;
    }
}
