package com.vke.app;

import com.vke.api.assets.r.R;
import com.vke.api.draw.Vertex;
import com.vke.api.rendering.vulkan.buffer.VertexByteSink;
import com.vke.api.scene.Scene;
import com.vke.core.Context;
import com.vke.core.mesh.MeshPrefab;
import com.vke.core.rendering.draw.FrameContext;
import com.vke.core.rendering.pipeline.RenderPipelines;
import com.vke.core.vulkan.buffers.premade.mesh.StaticMeshBuffer;
import com.vke.core.vulkan.service.VulkanRenderer;
import com.vke.utils.io.Identifier;
import org.joml.Matrix4f;

import java.io.IOException;

public class DemoScene extends Scene {

    public DemoScene(Identifier name, Context context) {
        super(name, context);
    }

    private StaticMeshBuffer mesh;

    @Override
    public void onLoad() {
        MeshPrefab prefab;
        try {
            prefab = R.meshprefabs.get("bear.obj").acquire(context);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        float[] color = {1, 1, 1, 1};

        mesh = StaticMeshBuffer.uploadOnce(context.getEngine(),
                prefab.toMesh((prefabVertex -> new CubeVertexFormat(
                        prefabVertex.position()[0],
                        prefabVertex.position()[1],
                        prefabVertex.position()[2],

                        prefabVertex.normal()[0],
                        prefabVertex.normal()[1],
                        prefabVertex.normal()[2],

                        color[0],
                        color[1],
                        color[2],
                        color[3]))));
    }

    @Override
    public void onDraw(FrameContext ctx) {
        Matrix4f model = new Matrix4f();

        float time = (System.nanoTime() / 1_000_000_000.0f);

        float speed = 1.0f;

        float scale = 10;
        model.identity()
                .translate(0, 0.0f, -550)
                .scale(scale, scale, scale)
                .rotateY(time * speed);

        RenderPipelines.DEMO.setLocal(model);
        RenderPipelines.DEMO.use(ctx);
        mesh.draw(ctx);
    }

    @Override
    public void free() {
        mesh.free();
    }

    public static class CubeVertexFormat extends Vertex {

        private float x, y, z;
        private float nx, ny, nz;
        private float r, g, b, a;

        public CubeVertexFormat(float x, float y, float z, float nx, float ny, float nz, float r, float g, float b, float a) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.nx = nx;
            this.ny = ny;
            this.nz = nz;
            this.r = r;
            this.g = g;
            this.b = b;
            this.a = a;
        }

        @Override
        public int getByteStride() {
            return 4*10;
        }

        @Override
        public void putSelf(VertexByteSink buf) {
            buf.float3(x, y, z);
            buf.float3(nx, ny, nz);
            buf.float4(r, g, b, a);
        }
    }

}
