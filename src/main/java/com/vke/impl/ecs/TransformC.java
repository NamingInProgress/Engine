package com.vke.impl.ecs;

import com.vke.core.ecs.component.Component;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import pl.epsi.EcsComponent;

@EcsComponent
public class TransformC implements Component {

    public float[] x, y, z;
    public float[] rx, ry, rz, rw;
    public float[] sx, sy, sz;

    public void initialize(int i) {
        x[i] = y[i] = z[i] = 0;
        sx[i] = sy[i] = sz[i] = 1f;
        setRotation(i, new Vector3f(0, 90, 0));
    }

    public void setQuaternion(int i, Quaternionf quaternionf) {
        this.rx[i] = quaternionf.x;
        this.ry[i] = quaternionf.y;
        this.rz[i] = quaternionf.z;
        this.rw[i] = quaternionf.w;
    }

    public void setRotation(int i, Vector3f angles) {
        setQuaternion(i, new Quaternionf().rotationXYZ((float) Math.toRadians(angles.x), (float) Math.toRadians(angles.y),
                (float) Math.toRadians(angles.z)));
    }

    public Vector3f position(int i) {
        return new Vector3f(x[i], y[i], z[i]);
    }

    public Quaternionf rotation(int i) {
        return new Quaternionf(rx[i], ry[i], rz[i], rw[i]);
    }

    public Vector3f rotationEulerRad(int i) {
        return rotation(i).getEulerAnglesXYZ(new Vector3f());
    }

    public Vector3f rotationEuler(int i) {
        return rotationEulerRad(i).mul((float) Math.toDegrees(1.0));
    }

    public Vector3f scale(int i) {
        return new Vector3f(sx[i], sy[i], sz[i]);
    }

    public Vector3f forward(int i) {
        return rotation(i).transform(0, 0, -1, new Vector3f()).normalize();
    }

}
