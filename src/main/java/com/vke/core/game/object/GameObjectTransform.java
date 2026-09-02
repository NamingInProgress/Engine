package com.vke.core.game.object;

import com.vke.core.ecs.ComponentProxy;
import com.vke.core.ecs.ComponentReference;
import com.vke.core.game.scene.NodeHierarchy;
import com.vke.impl.ecs.TransformC;
import com.vke.impl.ecs.WorldTransformC;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

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
        tc.dirty[i] = true;
    }

    @Override
    public void setIndexInternal(int index) {
        this.i = index;
        tc.dirty[i] = true;
    }

    public float getX() {
        return tc.x[i];
    }

    public void setX(float x) {
        tc.x[i] = x;
        tc.dirty[i] = true;
    }

    public void changeX(float dx) {
        tc.x[i] += dx;
        tc.dirty[i] = true;
    }

    public float getY() {
        return tc.y[i];
    }

    public void setY(float dy) {
        tc.y[i] = dy;
        tc.dirty[i] = true;
    }

    public void changeY(float dy) {
        tc.y[i] += dy;
        tc.dirty[i] = true;
    }

    public float getZ() {
        return tc.z[i];
    }

    public void setZ(float z) {
        tc.z[i] = z;
        tc.dirty[i] = true;
    }

    public void changeZ(float dz) {
        tc.z[i] += dz;
        tc.dirty[i] = true;
    }

    public void setXYZ(float x, float y, float z) {
        tc.x[i] = x;
        tc.y[i] = y;
        tc.z[i] = z;
        tc.dirty[i] = true;
    }

    public void changeXYZ(float dx, float dy, float dz) {
        tc.x[i] += dx;
        tc.y[i] += dy;
        tc.z[i] += dz;
        tc.dirty[i] = true;
    }

    public Vector3f getPosition() {
        return new Vector3f(getX(), getY(), getZ());
    }

    public void getPosition(Vector3f dest) {
        dest.set(tc.x[i], tc.y[i], tc.z[i]);
    }

    public void setPosition(Vector3f pos) {
        setXYZ(pos.x, pos.y, pos.z);
        tc.dirty[i] = true;
    }

    public void changePosition(Vector3f delta) {
        changeXYZ(delta.x, delta.y, delta.z);
        tc.dirty[i] = true;
    }

    public Quaternionf getRotation() {
        return new Quaternionf(tc.rx[i], tc.ry[i], tc.rz[i], tc.rw[i]);
    }

    public void getRotation(Quaternionf dest) {
        dest.set(tc.rx[i], tc.ry[i], tc.rz[i], tc.rw[i]);
    }

    public void setRotation(Quaternionf quat) {
        tc.setQuaternion(i, quat);
    }

    public void setRotation(Vector3f angles) {
        tc.setRotation(i, angles);
    }

    public void setRotationXYZ(float x, float y, float z) {
        tc.setRotation(i, x, y, z);
    }

    public float getRQX() {
        return tc.rx[i];
    }

    public float getRQY() {
        return tc.ry[i];
    }

    public float getRQZ() {
        return tc.rz[i];
    }

    public float getRQW() {
        return tc.rw[i];
    }

    public float getOriginX() {
        return tc.ox[i];
    }

    public void setOriginX(float x) {
        tc.ox[i] = x;
        tc.dirty[i] = true;
    }

    public void changeOriginX(float dx) {
        tc.ox[i] += dx;
        tc.dirty[i] = true;
    }

    public float getOriginY() {
        return tc.oy[i];
    }

    public void setOriginY(float dy) {
        tc.oy[i] = dy;
        tc.dirty[i] = true;
    }

    public void changeOriginY(float dy) {
        tc.oy[i] += dy;
        tc.dirty[i] = true;
    }

    public float getOriginZ() {
        return tc.oz[i];
    }

    public void setOriginZ(float z) {
        tc.oz[i] = z;
        tc.dirty[i] = true;
    }

    public void changeOriginZ(float dz) {
        tc.oz[i] += dz;
        tc.dirty[i] = true;
    }

    public void setOriginXYZ(float x, float y, float z) {
        tc.ox[i] = x;
        tc.oy[i] = y;
        tc.oz[i] = z;
        tc.dirty[i] = true;
    }

    public void changeOriginXYZ(float dx, float dy, float dz) {
        tc.ox[i] += dx;
        tc.oy[i] += dy;
        tc.oz[i] += dz;
        tc.dirty[i] = true;
    }

    public Vector3f getOrigin() {
        return new Vector3f(getOriginX(), getOriginY(), getOriginZ());
    }

    public void getOrigin(Vector3f dest) {
        dest.set(tc.ox[i], tc.oy[i], tc.oz[i]);
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
        tc.dirty[i] = true;
    }

    public void changeScaleX(float dx) {
        tc.sx[i] += dx;
        tc.dirty[i] = true;
    }

    public float getScaleY() {
        return tc.sy[i];
    }

    public void setScaleY(float dy) {
        tc.sy[i] = dy;
        tc.dirty[i] = true;
    }

    public void changeScaleY(float dy) {
        tc.sy[i] += dy;
        tc.dirty[i] = true;
    }

    public float getScaleZ() {
        return tc.sz[i];
    }

    public void setScaleZ(float z) {
        tc.sz[i] = z;
        tc.dirty[i] = true;
    }

    public void changeScaleZ(float dz) {
        tc.sz[i] += dz;
        tc.dirty[i] = true;
    }

    public void setScaleXYZ(float x, float y, float z) {
        tc.sx[i] = x;
        tc.sy[i] = y;
        tc.sz[i] = z;
        tc.dirty[i] = true;
    }

    public void changeScaleXYZ(float dx, float dy, float dz) {
        tc.sx[i] += dx;
        tc.sy[i] += dy;
        tc.sz[i] += dz;
        tc.dirty[i] = true;
    }

    public Vector3f getScale() {
        return new Vector3f(getScaleX(), getScaleY(), getScaleZ());
    }

    public void getScale(Vector3f dest) {
        dest.set(tc.sx[i], tc.sy[i], tc.sz[i]);
    }

    public void setScale(Vector3f scale) {
        setScaleXYZ(scale.x, scale.y, scale.z);
    }

    public void changeScale(Vector3f delta) {
        changeScaleXYZ(delta.x, delta.y, delta.z);
    }

    public GameObject getParent() {
        return hierarchy.getParent(getGameObject().entityId());
    }

    public List<GameObject> getChildren() {
        List<GameObject> dest = new ArrayList<>();
        getChildren(dest);
        return dest;
    }

    /**
     * @param dest The MODIFYABLE list that will be populated by this GameObjects children. Previous contents are kept.
     */
    public void getChildren(List<GameObject> dest) {
        hierarchy.getChildren(getGameObject().entityId(), dest);
    }

    public void addChild(GameObject object) {
        hierarchy.addChild(getGameObject().entityId(), object.entityId());
    }

    public void getWorldMatrix(float[] arr) {
        worldComponent();
        float[] allMats = world.getComponent().worldMatrix;
        int index = world.getIndex() * 16;
        System.arraycopy(allMats, index, arr, 0, 16);
    }

    public void getWorldMatrix(Matrix4f dest) {
        worldComponent();
        dest.set(world.getComponent().worldMatrix, world.getIndex() * 16);
    }

    public Vector3f getWorldPosition() {
        Vector3f dest = new Vector3f();
        getWorldPosition(dest);
        return dest;
    }

    public void getWorldPosition(Vector3f dest) {
        worldComponent();
        float[] allMats = world.getComponent().worldMatrix;
        int index = world.getIndex() * 16;
        dest.x = allMats[index + 12];
        dest.y = allMats[index + 13];
        dest.z = allMats[index + 14];
    }
}
