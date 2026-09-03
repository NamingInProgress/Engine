package com.vke.core.game.object;

import com.vke.core.ecs.ComponentProxy;
import com.vke.core.ecs.ComponentReference;
import com.vke.core.game.scene.NodeHierarchy;
import com.vke.impl.ecs.TransformC;
import com.vke.impl.ecs.WorldTransformC;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.ArrayList;
import java.util.List;

public class GameObjectTransform extends ComponentProxy<TransformC> {
    private TransformC tc;
    private int i;

    private final GameObject owner;
    private final NodeHierarchy hierarchy;

    private ComponentReference<TransformC> comp;
    private ComponentReference<WorldTransformC> world;

    public GameObjectTransform(GameObject owner, NodeHierarchy hierarchy) {
        this.owner = owner;
        this.hierarchy = hierarchy;
    }

    public ComponentReference<TransformC> transformComponent() {
        if (comp == null) {
            comp = owner.getComponent(TransformC.ID);
            comp.linkProxy(this);
        }
        return comp;
    }

    public ComponentReference<WorldTransformC> worldComponent() {
        if (world == null) {
            world = owner.getComponent(WorldTransformC.ID);
        }
        return world;
    }

    public GameObject getGameObject() {
        return owner;
    }

    @Override
    public void setComponentInternal(TransformC component) {
        this.tc = component;
        if (component != null) {
            tc.dirty[i] = true;
        }
    }

    @Override
    public void setIndexInternal(int index) {
        this.i = index;
        if (tc != null) {
            tc.dirty[i] = true;
        }
    }

    public float getX() { return tc.x[i]; }
    public void setX(float x) { tc.setX(i, x); }
    public void changeX(float dx) { tc.changeXYZ(i, dx, 0f, 0f); }

    public float getY() { return tc.y[i]; }
    public void setY(float y) { tc.setY(i, y); }
    public void changeY(float dy) { tc.changeXYZ(i, 0f, dy, 0f); }

    public float getZ() { return tc.z[i]; }
    public void setZ(float z) { tc.setZ(i, z); }
    public void changeZ(float dz) { tc.changeXYZ(i, 0f, 0f, dz); }

    public void setXYZ(float x, float y, float z) { tc.setPosition(i, x, y, z); }
    public void changeXYZ(float dx, float dy, float dz) { tc.changeXYZ(i, dx, dy, dz); }

    public Vector3f getPosition() { return new Vector3f(getX(), getY(), getZ()); }
    public void getPosition(Vector3f dest) { dest.set(tc.x[i], tc.y[i], tc.z[i]); }
    public void setPosition(Vector3f pos) { tc.setPosition(i, pos.x, pos.y, pos.z); }
    public void changePosition(Vector3f delta) { tc.changeXYZ(i, delta.x, delta.y, delta.z); }

    public Quaternionf getRotation() {
        return new Quaternionf(tc.rx[i], tc.ry[i], tc.rz[i], tc.rw[i]);
    }
    public void getRotation(Quaternionf dest) {
        dest.set(tc.rx[i], tc.ry[i], tc.rz[i], tc.rw[i]);
    }
    public void setRotation(Quaternionf quat) { tc.setQuaternion(i, quat); }
    public void setRotation(Vector3f angles) { tc.setRotation(i, angles); }
    public void setRotationXYZ(float x, float y, float z) { tc.setRotation(i, x, y, z); }

    public float getRQX() { return tc.rx[i]; }
    public float getRQY() { return tc.ry[i]; }
    public float getRQZ() { return tc.rz[i]; }
    public float getRQW() { return tc.rw[i]; }


    public float getOriginX() { return tc.ox[i]; }
    public void setOriginX(float x) { tc.setOriginX(i, x); }
    public void changeOriginX(float dx) { tc.changeOriginXYZ(i, dx, 0f, 0f); }

    public float getOriginY() { return tc.oy[i]; }
    public void setOriginY(float y) { tc.setOriginY(i, y); }
    public void changeOriginY(float dy) { tc.changeOriginXYZ(i, 0f, dy, 0f); }

    public float getOriginZ() { return tc.oz[i]; }
    public void setOriginZ(float z) { tc.setOriginZ(i, z); }
    public void changeOriginZ(float dz) { tc.changeOriginXYZ(i, 0f, 0f, dz); }

    public void setOriginXYZ(float x, float y, float z) { tc.setOrigin(i, x, y, z); }
    public void changeOriginXYZ(float dx, float dy, float dz) { tc.changeOriginXYZ(i, dx, dy, dz); }

    public Vector3f getOrigin() { return new Vector3f(getOriginX(), getOriginY(), getOriginZ()); }
    public void getOrigin(Vector3f dest) { dest.set(tc.ox[i], tc.oy[i], tc.oz[i]); }
    public void setOrigin(Vector3f origin) { setOriginXYZ(origin.x, origin.y, origin.z); }
    public void changeOrigin(Vector3f delta) { changeOriginXYZ(delta.x, delta.y, delta.z); }


    public float getScaleX() { return tc.sx[i]; }
    public void setScaleX(float x) { tc.setScaleX(i, x); }
    public void changeScaleX(float dx) { tc.changeScaleXYZ(i, dx, 0f, 0f); }

    public float getScaleY() { return tc.sy[i]; }
    public void setScaleY(float y) { tc.setScaleY(i, y); }
    public void changeScaleY(float dy) { tc.changeScaleXYZ(i, 0f, dy, 0f); }

    public float getScaleZ() { return tc.sz[i]; }
    public void setScaleZ(float z) { tc.setScaleZ(i, z); }
    public void changeScaleZ(float dz) { tc.changeScaleXYZ(i, 0f, 0f, dz); }

    public void setScaleXYZ(float x, float y, float z) { tc.setScale(i, x, y, z); }
    public void changeScaleXYZ(float dx, float dy, float dz) { tc.changeScaleXYZ(i, dx, dy, dz); }

    public Vector3f getScale() { return new Vector3f(getScaleX(), getScaleY(), getScaleZ()); }
    public void getScale(Vector3f dest) { dest.set(tc.sx[i], tc.sy[i], tc.sz[i]); }
    public void setScale(Vector3f scale) { setScaleXYZ(scale.x, scale.y, scale.z); }
    public void changeScale(Vector3f delta) { changeScaleXYZ(delta.x, delta.y, delta.z); }

    public GameObject getParent() {
        return hierarchy.getParent(getGameObject().entityId());
    }

    public List<GameObject> getChildren() {
        List<GameObject> dest = new ArrayList<>();
        getChildren(dest);
        return dest;
    }

    public void getChildren(List<GameObject> dest) {
        hierarchy.getChildren(getGameObject().entityId(), dest);
    }

    public void addChild(GameObject object) {
        hierarchy.addChild(getGameObject().entityId(), object.entityId());
    }

    public void getWorldMatrix(float[] arr) {
        worldComponent().getComponent().getWorldMatrix(world.getIndex(), arr);
    }

    public void getWorldMatrix(Matrix4f dest) {
        worldComponent().getComponent().getWorldMatrix(world.getIndex(), dest);
    }

    public Vector3f getWorldPosition() {
        Vector3f dest = new Vector3f();
        getWorldPosition(dest);
        return dest;
    }

    public void getWorldPosition(Vector3f dest) {
        worldComponent().getComponent().getWorldPosition(world.getIndex(), dest);
    }

    public Vector4f getWorldForward() {
        return worldComponent().getComponent().getForward(world.getIndex());
    }

    public void getWorldForward(Vector4f dest) {
        worldComponent().getComponent().getForward(world.getIndex(), dest);
    }
}