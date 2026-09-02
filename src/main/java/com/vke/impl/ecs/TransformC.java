package com.vke.impl.ecs;

import com.vke.core.ecs.component.Component;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import pl.epsi.EcsComponent;

@EcsComponent
public class TransformC implements Component {
    public float[] x, y, z;
    public float[] ox, oy, oz;
    public float[] rx, ry, rz, rw;
    public float[] sx, sy, sz;
    public boolean[] dirty;

    @Override
    public void initialize(int i) {
        sx[i] = sy[i] = sz[i] = 1f;
        setRotation(i, new Vector3f(0, 90, 0));
    }

    public void setQuaternion(int i, Quaternionf quaternionf) {
        this.rx[i] = quaternionf.x;
        this.ry[i] = quaternionf.y;
        this.rz[i] = quaternionf.z;
        this.rw[i] = quaternionf.w;
        this.dirty[i] = true;
    }

    public void setRotation(int i, Vector3f angles) {
        setRotation(i, angles.x, angles.y, angles.z);
    }

    public void setRotation(int i, float x, float y, float z) {
        //took from Quaternionf.java:2000 just so we dont make a new object every time just java objects are kinda expensive unlike rust
        float sx = org.joml.Math.sin(x * 0.5f);
        float cx = org.joml.Math.cosFromSin(sx, x * 0.5f);
        float sy = org.joml.Math.sin(y * 0.5f);
        float cy = org.joml.Math.cosFromSin(sy, y * 0.5f);
        float sz = org.joml.Math.sin(z * 0.5f);
        float cz = org.joml.Math.cosFromSin(sz, z * 0.5f);

        float cycz = cy * cz;
        float sysz = sy * sz;
        float sycz = sy * cz;
        float cysz = cy * sz;
        this.rw[i] = cx*cycz - sx*sysz;
        this.rx[i] = sx*cycz + cx*sysz;
        this.ry[i] = cx*sycz - sx*cysz;
        this.rz[i] = cx*cysz + sx*sycz;
        this.dirty[i] = true;
    }
}
