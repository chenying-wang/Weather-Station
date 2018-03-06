package com.course.project.hardware.weatherstation;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class GLFragment extends Fragment{

    private AccGLSurfaceView mGLView;
    private int glMode;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        mGLView = new AccGLSurfaceView(getContext());
        mGLView.setMode(glMode);
        return mGLView;
    }

    @Override
    public void onPause() {
        super.onPause();
        mGLView.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        mGLView.onResume();
    }

    void refreashAcc(float[] acc) {
        if(mGLView==null) return;
        float[] uniAcc = new float[acc.length];
        float accLength = 0;

        for (float accAxis : acc) accLength += accAxis*accAxis;
        accLength = (float) Math.sqrt((double) accLength);
        for(int i=0; i<acc.length; i++) uniAcc[i] = acc[i]/accLength;

        mGLView.setAcc(uniAcc);
    }

    void refreashGyro(float[] gyro) {

    }

    void refreashMode(int mode) {
        glMode = mode;
        if(mGLView==null) return;
        mGLView.setMode(mode);
    }

}

class AccGLSurfaceView extends GLSurfaceView {

    protected AccGL20Renderer mRenderer;

    AccGLSurfaceView(Context context) {
        super(context);

        setEGLContextClientVersion(2);

        mRenderer = new AccGL20Renderer();
        setRenderer(mRenderer);
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);

    }

    void setAcc(float[] acc) {
        mRenderer.setAcc(acc);
        requestRender();
    }

    void setMode(int mode) {
        mRenderer.setMode(mode);
    }

}

class AccGL20Renderer implements GLSurfaceView.Renderer {

    private Square mSquare;
    private int mode;
    private final float[] mMVPMatrix = new float[16];
    private final float[] mProjectionMatrix = new float[16];
    private final float[] mViewMatrix = new float[16];
    private float[] acc;

    AccGL20Renderer() {
        super();
    }

    @Override
    public void onSurfaceCreated(GL10 unused, EGLConfig config) {
        GLES20.glClearColor(0.87f, 0.87f, 0.87f, 1f);

        float left = -0.5f, right = 0.5f;

        float top = 0.5f, bottom = -0.5f;
        float depth = 1.0f;
        mSquare = new Square(new float[] {
                left, top, depth/2,
                right, top, depth/2,
                right, bottom, depth/2,
                left, bottom, depth/2,
                left, top, -depth/2,
                right, top, -depth/2,
                right, bottom, -depth/2,
                left, bottom, -depth/2 },
                new float[] {
                        0.2f, 0.5f, 0.9f, 1.0f
                });
    }

    @Override
    public void onDrawFrame(GL10 unused) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        if(acc != null) {
            if(mode == Constants.SETTING_CUBE_ROTATION) {
                Matrix.setLookAtM
                        (mViewMatrix, 0, 2*acc[0], 2*acc[1], 2*acc[2], 0f, 0f, 0f, 0f, 1f, 0f);
                Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0);
                mSquare.draw(mMVPMatrix, new float[]{1f, 1f, 1f, 0f});
            }
            else if(mode == Constants.SETTING_CUBE_COLOR) {
                Matrix.setLookAtM(
                        mViewMatrix, 0, 0, 0, 2.0f, 0f, 0f, 0f, 0f, 1f, 0f);
                Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0);
                mSquare.draw(mMVPMatrix, new float[]{acc[0], acc[1], acc[2], 0f});
            }
        }
    }

    @Override
    public void onSurfaceChanged(GL10 unused, int width, int height) {
        GLES20.glViewport(0, 0, width, height);

        Matrix.frustumM(mProjectionMatrix, 0, -0.5f, 0.5f, -0.5f, 0.5f, 1f, 3f);

    }

    static int loadShader(int type, String shaderCode) {

        int shader = GLES20.glCreateShader(type);

        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);
        return shader;
    }

    void setAcc(float[] acc) {
        this.acc = acc;
    }

    void setMode(int mode) {
        this.mode = mode;
    }

}

class Square {

    private final FloatBuffer vertexBuffer;
    private final ShortBuffer squareDrawListBuffer;
    private final int mSquareProgram;

    private float mCoords[] = new float[24];

    private final short squareDrawOrder[] = {
            0, 1, 2, 0, 2, 3,
            4, 5, 6, 4, 6, 7,
            0, 1, 4, 1, 4, 5,
            2, 3, 6, 3, 6, 7,
            0, 3, 4, 3, 4, 7,
            1, 2, 5, 2, 5, 6 };

    private float color[] = new float[4];

    Square(float[] GLCoords, float[] GLColor) {

        final String squareVertexShaderCode =
                "uniform mat4 uMVPMatrix;" +
                        "uniform vec4 vGravity;" +
                        "attribute vec4 vPosition;" +
                        "varying float normalPosition;" +
                        "void main() {" +
                        "  gl_Position = uMVPMatrix * vPosition;" +
                        "  normalPosition = dot(vGravity, vPosition);" +
                        "}";

        final String squareFragmentShaderCode =
                "precision mediump float;" +
                        "uniform vec4 vColor;" +
                        "varying float normalPosition;" +
                        "void main() {" +
                        "  gl_FragColor = (0.5+normalPosition) * vec4(0.87, 0.87, 0.87, 1)" +
                        "    + (0.5-normalPosition) * vColor;" +
                        "}";

        setGLCoords(GLCoords);
        setGLColor(GLColor);

        ByteBuffer bb = ByteBuffer.allocateDirect(
                mCoords.length * 4);
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(mCoords);
        vertexBuffer.position(0);

        ByteBuffer dlb = ByteBuffer.allocateDirect(
                squareDrawOrder.length * 2);
        dlb.order(ByteOrder.nativeOrder());
        squareDrawListBuffer = dlb.asShortBuffer();
        squareDrawListBuffer.put(squareDrawOrder);
        squareDrawListBuffer.position(0);

        int squareVertexShader = AccGL20Renderer.loadShader(
                GLES20.GL_VERTEX_SHADER,
                squareVertexShaderCode);
        int squareFragmentShader = AccGL20Renderer.loadShader(
                GLES20.GL_FRAGMENT_SHADER,
                squareFragmentShaderCode);

        mSquareProgram = GLES20.glCreateProgram();
        GLES20.glAttachShader(mSquareProgram, squareVertexShader);
        GLES20.glAttachShader(mSquareProgram, squareFragmentShader);
        GLES20.glLinkProgram(mSquareProgram);
    }

    void draw(float[] mvpMatrix, float[] gravityVector) {

        final int COORDS_PER_VERTEX = 3;
        final int vertexStride = COORDS_PER_VERTEX * 4;

        int mPositionHandle;
        int mMVPMatrixHandle;
        int mColorHandle;
        int mGravityHandle;

        GLES20.glUseProgram(mSquareProgram);
        mPositionHandle = GLES20.glGetAttribLocation(mSquareProgram, "vPosition");
        GLES20.glEnableVertexAttribArray(mPositionHandle);
        mMVPMatrixHandle = GLES20.glGetUniformLocation(mSquareProgram, "uMVPMatrix");
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);
        GLES20.glVertexAttribPointer(
                mPositionHandle, COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false,
                vertexStride, vertexBuffer);

        mColorHandle = GLES20.glGetUniformLocation(mSquareProgram, "vColor");
        GLES20.glUniform4fv(mColorHandle, 1, color, 0);

        mGravityHandle = GLES20.glGetUniformLocation(mSquareProgram, "vGravity");
        GLES20.glUniform4fv(mGravityHandle, 1, gravityVector, 0);

        GLES20.glDrawElements(
                GLES20.GL_TRIANGLES, squareDrawOrder.length,
                GLES20.GL_UNSIGNED_SHORT, squareDrawListBuffer);

        GLES20.glDisableVertexAttribArray(mPositionHandle);
    }

    private void setGLCoords(float[] GLCoords) {
        System.arraycopy(GLCoords, 0, this.mCoords, 0, mCoords.length);
    }

    private void setGLColor(float[] GLColor) {
        System.arraycopy(GLColor, 0, this.color, 0, color.length);
    }

}
