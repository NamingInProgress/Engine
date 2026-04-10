package com.vke.test.rendering;

import com.vke.api.assets.r.R;
import com.vke.api.draw.MeshPrefab;
import com.vke.api.draw.Meshes;
import com.vke.api.draw.Vertex;
import com.vke.api.rendering.abstraction.pipeline.RenderPipeline;
import com.vke.api.rendering.vulkan.buffer.VertexByteSink;
import com.vke.api.rendering.vulkan.pushconstants.PushConstantHandle;
import com.vke.api.scene.Scene;
import com.vke.core.Context;
import com.vke.core.assets.handles.utils.LazyAssetHandle;
import com.vke.core.file.obj.ObjException;
import com.vke.core.file.obj.ObjFile;
import com.vke.core.services.Services;
import com.vke.core.vulkan.Scissor;
import com.vke.core.vulkan.Viewport;
import com.vke.core.vulkan.VulkanRenderer;
import com.vke.core.vulkan.buffers.premade.mesh.StaticMeshBuffer;
import com.vke.core.vulkan.command.VulkanCmdBuffers;
import com.vke.core.vulkan.pipeline.VulkanRenderPipeline;
import com.vke.core.window.Window;
import com.vke.utils.io.Identifier;
import org.joml.Matrix4f;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK14;

import java.io.IOException;

public class MainScene extends Scene {

    public MainScene(Identifier name, Context context) {
        super(name, context);
    }

    private VulkanRenderPipeline cubePipeline;
    private LazyAssetHandle<RenderPipeline> CUBE = R.pipelines.get("spinny_cub.pipeline_vt.json");

    private PushConstantHandle projMatrixHandle;
    private PushConstantHandle transformMatrixHandle;

    private StaticMeshBuffer mesh;

    @Override
    public void onLoad() {
        cubePipeline = (VulkanRenderPipeline) CUBE.assume(context);
        
        projMatrixHandle = cubePipeline.resolvePushConstant("world");
        transformMatrixHandle = cubePipeline.resolvePushConstant("translation");

        MeshPrefab prefab;
        try {
            ObjFile objFile = new ObjFile(context.id("bear.obj").asInputStream());
            prefab = objFile.toMeshPrefab();
        } catch (ObjException | IOException e) {
            throw new RuntimeException(e);
        }

        float[] color = {1, 1, 1, 1};

        mesh = StaticMeshBuffer.uploadOnce(context.getEngine(), context.service(Services.VULKAN_RENDERER),
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
    public void drawLoop(Window window, VulkanRenderer.FrameData fd) {
        int width = window.getSize().width();
        int height = window.getSize().height();

        try (MemoryStack stack = MemoryStack.stackPush()) {
            VulkanCmdBuffers cmd = fd.frame().getBuffers();
            cmd.bindRenderPipeline(CUBE);

            Scissor sc = new Scissor(0, 0, width, height);
            Viewport wp = new Viewport(0, 0, width, height);

            cmd.setViewport(wp);
            cmd.setScissor(sc);

            Matrix4f mat = new Matrix4f();
            //mat.setOrtho(0, wp.width(), 0, wp.height(), 0, 1000, true);
            mat.setPerspective((float) Math.toRadians(90), (float) 800 / 600, 0.1f, 1000, true);

            cmd.bindRenderPipeline(CUBE);


            Matrix4f model = new Matrix4f();

            float time = (System.nanoTime() / 1_000_000_000.0f);

            float speed = 1.0f;

//            model.identity()
//                    .translate(400.0f, 300.0f, -50) // move to center (adjust as needed)
//                    .scale((float) (200 + 5 * Math.sin(Math.toRadians(time * 10))),
//                            (float) (200 + 5 * Math.sin(Math.toRadians(time * 10))),
//                            (float) (200 + 5 * Math.sin(Math.toRadians(time * 10))))
//                    .rotateXYZ(time * speed, time * speed, time * speed);


            float scale = 10;
            model.identity()
                    .translate(200.0f, -250.0f, -550)
                    .scale(scale, scale, scale)
                    .rotateY(time * speed);
                    //.rotateX(45)
                    ;//.rotateY(90);

            projMatrixHandle.write(buf -> buf.putMat4(mat));
            transformMatrixHandle.write(buf -> buf.putMat4(model));

            cmd.setPushConstants(CUBE);

            VK14.vkCmdBindIndexBuffer(cmd.getBuffer(), mesh.getIndicesBuf().getGpuBuffer().getBuffer(), 0, VK14.VK_INDEX_TYPE_UINT32);
            VK14.vkCmdBindVertexBuffers(cmd.getBuffer(), 0, stack.longs(mesh.getVerticesBuf().getGpuBuffer().getBuffer()), stack.longs(0));

            VK14.vkCmdDrawIndexed(cmd.getBuffer(), mesh.getIndexCount(), 1, 0, 0, 0);
        }
    }

    @Override
    public void onUnload() {
        mesh.free();
    }

    @Override
    public void free() {
    }

    public static class CubeVertexFormat implements Vertex {

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
