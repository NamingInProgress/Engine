package com.vke.impl.vertex;

import com.vke.api.rendering.abstraction.draw.Vertex;
import com.vke.api.rendering.pbr.Material;
import pl.epsi.MakeVertex;
import pl.epsi.Type;

@MakeVertex
public class VertexFormatDeferred implements Vertex {

    @Type.Float3
    private final float x, y, z;
    @Type.Float3
    private final float nx, ny, nz;
    @Type.Float2
    private final float u, v;
    @Type.Material
    private final Material material;

    public VertexFormatDeferred(float x, float y, float z, float nx, float ny, float nz, float u, float v, Material material) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.nx = nx;
        this.ny = ny;
        this.nz = nz;
        this.u = u;
        this.v = v;
        this.material = material;
    }
}
