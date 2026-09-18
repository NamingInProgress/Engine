package com.vke.impl.rendering.vertex;

import com.vke.api.rendering.abstraction.draw.MeshVertexFactory;
import com.vke.api.rendering.abstraction.draw.Vertex;
import com.vke.api.rendering.pbr.Material;
import org.jetbrains.annotations.Nullable;
import pl.epsi.MakeVertex;
import pl.epsi.Type;

@MakeVertex
public class VertexFormatDeferred implements Vertex {

    public static final MeshVertexFactory MESH_VERTEX_FACTORY = (prefabVertex -> new VertexFormatDeferred(
            prefabVertex.position()[0], prefabVertex.position()[1], prefabVertex.position()[2],
            prefabVertex.normal()[0], prefabVertex.normal()[1], prefabVertex.normal()[2],
            prefabVertex.uv()[0], prefabVertex.uv()[1],
            null,
            prefabVertex.tangent()[0], prefabVertex.tangent()[1], prefabVertex.tangent()[2], prefabVertex.tangent()[3]
    ));

    @Type.Float3
    private final float x, y, z;
    @Type.Float3
    private final float nx, ny, nz;
    @Type.Float2
    private final float u, v;
    @Type.Material
    private final Material material;
    @Type.Float4
    private final float tx, ty, tz, tw;

    public VertexFormatDeferred(float x, float y, float z, float nx, float ny, float nz, float u, float v, Material material, float tx, float ty, float tz, float tw) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.nx = nx;
        this.ny = ny;
        this.nz = nz;
        this.u = u;
        this.v = v;
        this.material = material;
        this.tx = tx;
        this.ty = ty;
        this.tz = tz;
        this.tw = tw;
    }
}
