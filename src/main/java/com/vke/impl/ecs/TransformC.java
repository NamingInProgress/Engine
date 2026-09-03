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
        setRotation(i, 0f, 90f, 0f);
    }

    public void setPosition(int i, float x, float y, float z) {
        if (this.x[i] != x || this.y[i] != y || this.z[i] != z) {
            this.x[i] = x;
            this.y[i] = y;
            this.z[i] = z;
            this.dirty[i] = true;
        }
    }

    public void setX(int i, float x) {
        if (this.x[i] != x) {
            this.x[i] = x;
            this.dirty[i] = true;
        }
    }

    public void setY(int i, float y) {
        if (this.y[i] != y) {
            this.y[i] = y;
            this.dirty[i] = true;
        }
    }

    public void setZ(int i, float z) {
        if (this.z[i] != z) {
            this.z[i] = z;
            this.dirty[i] = true;
        }
    }

    public void changeXYZ(int i, float dx, float dy, float dz) {
        if (dx != 0f || dy != 0f || dz != 0f) {
            this.x[i] += dx;
            this.y[i] += dy;
            this.z[i] += dz;
            this.dirty[i] = true;
        }
    }

    public void setQuaternion(int i, Quaternionf q) {
        if (rx[i] != q.x || ry[i] != q.y || rz[i] != q.z || rw[i] != q.w) {
            this.rx[i] = q.x;
            this.ry[i] = q.y;
            this.rz[i] = q.z;
            this.rw[i] = q.w;
            this.dirty[i] = true;
        }
    }

    public void setRotation(int i, Vector3f angles) {
        setRotation(i, angles.x, angles.y, angles.z);
    }

    public void setRotation(int i, float x, float y, float z) {
        x = (float) Math.toRadians(x);
        y = (float) Math.toRadians(y);
        z = (float) Math.toRadians(z);
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

        float rw = cx * cycz - sx * sysz;
        float rx = sx * cycz + cx * sysz;
        float ry = cx * sycz - sx * cysz;
        float rz = cx * cysz + sx * sycz;

        if (this.rw[i] != rw || this.rx[i] != rx || this.ry[i] != ry || this.rz[i] != rz) {
            this.rw[i] = rw;
            this.rx[i] = rx;
            this.ry[i] = ry;
            this.rz[i] = rz;
            this.dirty[i] = true;
        }
    }

    public void setOrigin(int i, float x, float y, float z) {
        if (this.ox[i] != x || this.oy[i] != y || this.oz[i] != z) {
            this.ox[i] = x;
            this.oy[i] = y;
            this.oz[i] = z;
            this.dirty[i] = true;
        }
    }

    public void setOriginX(int i, float x) {
        if (this.ox[i] != x) {
            this.ox[i] = x;
            this.dirty[i] = true;
        }
    }

    public void setOriginY(int i, float y) {
        if (this.oy[i] != y) {
            this.oy[i] = y;
            this.dirty[i] = true;
        }
    }

    public void setOriginZ(int i, float z) {
        if (this.oz[i] != z) {
            this.oz[i] = z;
            this.dirty[i] = true;
        }
    }

    public void changeOriginXYZ(int i, float dx, float dy, float dz) {
        if (dx != 0f || dy != 0f || dz != 0f) {
            this.ox[i] += dx;
            this.oy[i] += dy;
            this.oz[i] += dz;
            this.dirty[i] = true;
        }
    }

    public void setScale(int i, float x, float y, float z) {
        if (this.sx[i] != x || this.sy[i] != y || this.sz[i] != z) {
            this.sx[i] = x;
            this.sy[i] = y;
            this.sz[i] = z;
            this.dirty[i] = true;
        }
    }

    public void setScaleX(int i, float x) {
        if (this.sx[i] != x) {
            this.sx[i] = x;
            this.dirty[i] = true;
        }
    }

    public void setScaleY(int i, float y) {
        if (this.sy[i] != y) {
            this.sy[i] = y;
            this.dirty[i] = true;
        }
    }

    public void setScaleZ(int i, float z) {
        if (this.sz[i] != z) {
            this.sz[i] = z;
            this.dirty[i] = true;
        }
    }

    public void changeScaleXYZ(int i, float dx, float dy, float dz) {
        if (dx != 0f || dy != 0f || dz != 0f) {
            this.sx[i] += dx;
            this.sy[i] += dy;
            this.sz[i] += dz;
            this.dirty[i] = true;
        }
    }
}