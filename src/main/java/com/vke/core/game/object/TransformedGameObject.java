package com.vke.core.game.object;

import com.vke.core.ecs.ComponentProxy;
import com.vke.core.ecs.ComponentReference;
import com.vke.impl.ecs.TransformC;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public abstract class TransformedGameObject extends ComponentProxy<TransformC> implements RestrictedGameObject {
    private TransformC tc;
    private int i;

    private ComponentReference<TransformC> comp;

    public TransformedGameObject() {

    }

    public ComponentReference<TransformC> transformComponent() {
        if (comp == null) {
            comp = getComponent(TransformC.ID);
            comp.linkProxy(this);
        }
        return comp;
    }

    @Override
    public void setComponentInternal(TransformC component) {
        this.tc = component;
    }

    @Override
    public void setIndexInternal(int index) {
        this.i = index;
    }

    @Override
    public int[] getFixedComponents() {
        return new int[] { TransformC.ID };
    }

    public float getX() {
        return tc.x[i];
    }

    public void setX(float x) {
        tc.x[i] = x;
    }

    public void changeX(float dx) {
        tc.x[i] += dx;
    }

    public float getY() {
        return tc.y[i];
    }

    public void setY(float dy) {
        tc.y[i] = dy;
    }

    public void changeY(float dy) {
        tc.y[i] += dy;
    }

    public float getZ() {
        return tc.z[i];
    }

    public void setZ(float z) {
        tc.z[i] = z;
    }

    public void changeZ(float dz) {
        tc.z[i] += dz;
    }

    public void setXYZ(float x, float y, float z) {
        tc.x[i] = x;
        tc.y[i] = y;
        tc.z[i] = z;
    }

    public void changeXYZ(float dx, float dy, float dz) {
        tc.x[i] += dx;
        tc.y[i] += dy;
        tc.z[i] += dz;
    }

    public Vector3f getPosition() {
        return new Vector3f(getX(), getY(), getZ());
    }

    public void setPosition(Vector3f pos) {
        setXYZ(pos.x, pos.y, pos.z);
    }

    public void changePosition(Vector3f delta) {
        changeXYZ(delta.x, delta.y, delta.z);
    }

    public Quaternionf getRotation() {
        return new Quaternionf(tc.rx[i], tc.ry[i], tc.rz[i], tc.rw[i]);
    }

    public void setRotation(Quaternionf quat) {
        tc.rx[i] = quat.x;
        tc.ry[i] = quat.y;
        tc.rz[i] = quat.z;
        tc.rw[i] = quat.w;
    }

    public float getOriginX() {
        return tc.x[i];
    }

    public void setOriginX(float x) {
        tc.x[i] = x;
    }

    public void changeOriginX(float dx) {
        tc.x[i] += dx;
    }

    public float getOriginY() {
        return tc.y[i];
    }

    public void setOriginY(float dy) {
        tc.y[i] = dy;
    }

    public void changeOriginY(float dy) {
        tc.y[i] += dy;
    }

    public float getOriginZ() {
        return tc.z[i];
    }

    public void setOriginZ(float z) {
        tc.z[i] = z;
    }

    public void changeOriginZ(float dz) {
        tc.z[i] += dz;
    }

    public void setOriginXYZ(float x, float y, float z) {
        tc.x[i] = x;
        tc.y[i] = y;
        tc.z[i] = z;
    }

    public void changeOriginXYZ(float dx, float dy, float dz) {
        tc.x[i] += dx;
        tc.y[i] += dy;
        tc.z[i] += dz;
    }

    public Vector3f getOrigin() {
        return new Vector3f(getOriginX(), getOriginY(), getOriginZ());
    }

    public void setOrigin(Vector3f origin) {
        setOriginXYZ(origin.x, origin.y, origin.z);
    }

    public void changeOrigin(Vector3f delta) {
        changeOriginXYZ(delta.x, delta.y, delta.z);
    }

    public float getScaleX() {
        return tc.sx[i];
    }

    public void setScaleX(float x) {
        tc.sx[i] = x;
    }

    public void changeScaleX(float dx) {
        tc.sx[i] += dx;
    }

    public float getScaleY() {
        return tc.sy[i];
    }

    public void setScaleY(float dy) {
        tc.sy[i] = dy;
    }

    public void changeScaleY(float dy) {
        tc.sy[i] += dy;
    }

    public float getScaleZ() {
        return tc.sz[i];
    }

    public void setScaleZ(float z) {
        tc.sz[i] = z;
    }

    public void changeScaleZ(float dz) {
        tc.sz[i] += dz;
    }

    public void setScaleXYZ(float x, float y, float z) {
        tc.sx[i] = x;
        tc.sy[i] = y;
        tc.sz[i] = z;
    }

    public void changeScaleXYZ(float dx, float dy, float dz) {
        tc.sx[i] += dx;
        tc.sy[i] += dy;
        tc.sz[i] += dz;
    }

    public Vector3f getScale() {
        return new Vector3f(getScaleX(), getScaleY(), getScaleZ());
    }

    public void setScale(Vector3f scale) {
        setScaleXYZ(scale.x, scale.y, scale.z);
    }

    public void changeScale(Vector3f delta) {
        changeScaleXYZ(delta.x, delta.y, delta.z);
    }

    public Matrix4f buildTransformMatrix() {
        return new Matrix4f()
                .translate(getX(), getY(), getZ())
                .rotateAround(getRotation(), getOriginX(), getOriginY(), getOriginZ())
                .scaleAround(getScaleX(), getScaleY(), getScaleZ(), getOriginX(), getOriginY(), getOriginZ());
    }
}
