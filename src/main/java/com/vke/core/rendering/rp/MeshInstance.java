package com.vke.core.rendering.rp;

import com.vke.api.rendering.abstraction.renderer.data.StaticMesh;
import com.vke.core.rendering.vulkan.buffers.premade.slice.BufferSlice;
import org.joml.Matrix4f;

import java.util.Objects;

public final class MeshInstance {
    private final int entityId;
    private final StaticMesh mesh;
    private float[] mat;
    private final int materialIndex;

    public MeshInstance(int entityId, StaticMesh mesh, float[] mat, int materialIndex) {
        this.entityId = entityId;
        this.mesh = mesh;
        this.mat = mat;
        this.materialIndex = materialIndex;
    }

    public void setMat(float[] mat) {
        this.mat = mat;
    }

    public void putSelf(BufferSlice writer) {
        writer.mat4(mat);
        writer.int1(materialIndex);
    }

    public int entityId() {
        return entityId;
    }

    public StaticMesh mesh() {
        return mesh;
    }

    public float[] mat() {
        return mat;
    }

    public int materialIndex() {
        return materialIndex;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (MeshInstance) obj;
        return this.entityId == that.entityId &&
                Objects.equals(this.mesh, that.mesh) &&
                Objects.equals(this.mat, that.mat) &&
                this.materialIndex == that.materialIndex;
    }

    @Override
    public int hashCode() {
        return Objects.hash(entityId, mesh, mat, materialIndex);
    }

    @Override
    public String toString() {
        return "MeshInstance[" +
                "entityId=" + entityId + ", " +
                "mesh=" + mesh + ", " +
                "mat=" + mat + ", " +
                "materialIndex=" + materialIndex + ']';
    }


}
