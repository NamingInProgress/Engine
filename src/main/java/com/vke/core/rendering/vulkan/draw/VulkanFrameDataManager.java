package com.vke.core.rendering.vulkan.draw;

import com.vke.api.rendering.abstraction.renderer.data.FrameDataManager;
import com.vke.api.rendering.vulkan.descriptors2.DescriptorSetGroup;
import com.vke.api.rendering.vulkan.descriptors2.handles.buf.BufferHandle;
import com.vke.api.rendering.vulkan.descriptors2.handles.buf.FieldHandle;
import com.vke.core.game.object.CameraGameObject;
import com.vke.core.rendering.vulkan.descriptor.EngineDescriptorSetsManager;
import org.joml.Matrix4f;

public class VulkanFrameDataManager implements FrameDataManager {

    private final EngineDescriptorSetsManager mgr;

    private final BufferHandle handle;
    private final FieldHandle cameraHandle;
    private final FieldHandle time;

    private CameraGameObject camera;

    private final long startTime = System.nanoTime();

    public VulkanFrameDataManager(EngineDescriptorSetsManager mgr) {
        this.mgr = mgr;

        DescriptorSetGroup group = mgr.ENGINE_PIPELINE_LAYOUT.getGroup();

        this.handle = group.resolve("frameData");
        this.cameraHandle = group.resolve("frameData.camera");
        this.time = group.resolve("frameData.time");
    }

    @Override
    public void setCamera(CameraGameObject camera) {
        this.camera = camera;
    }

    @Override
    public void onDraw() {
        if (camera != null) {
            cameraHandle.write((slice) -> {
                Matrix4f proj = camera.getProjectionMatrix(), view = camera.getViewMatrix();
                slice.mat4(proj);
                slice.mat4(view);
                slice.mat4(new Matrix4f(proj).invert());
                slice.mat4(new Matrix4f(view).invert());
                slice.float3(camera.lookAt().x, camera.lookAt().y, camera.lookAt().z);
                slice.float3(camera.getX(), camera.getY(), camera.getZ());
            });
        }
        time.write((slice) -> slice.float1((System.nanoTime() - startTime) / 1_000_000_000f));
    }
}
