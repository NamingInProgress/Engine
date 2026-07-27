package com.vke.core.rendering.vulkan.draw;

import com.vke.api.game.camera.Camera;
import com.vke.api.rendering.abstraction.renderer.data.FrameDataManager;
import com.vke.api.rendering.vulkan.descriptors2.DescriptorSetGroup;
import com.vke.api.rendering.vulkan.descriptors2.handles.buf.BufferHandle;
import com.vke.api.rendering.vulkan.descriptors2.handles.buf.FieldHandle;
import com.vke.api.rendering.vulkan.descriptors2.handles.buf.MultiWriteFieldHandle;
import com.vke.core.rendering.vulkan.descriptor.EngineDescriptorSetsManager;
import org.joml.Matrix4f;

public class VulkanFrameDataManager implements FrameDataManager {

    private final EngineDescriptorSetsManager mgr;

    private final BufferHandle handle;
    private final FieldHandle cameraHandle;
    private final FieldHandle time;

    private Camera camera;

    private final long startTime = System.nanoTime();

    public VulkanFrameDataManager(EngineDescriptorSetsManager mgr) {
        this.mgr = mgr;

        DescriptorSetGroup group = mgr.ENGINE_PIPELINE_LAYOUT.getGroup();

        this.handle = group.resolve("frameData");
        this.cameraHandle = group.resolve("frameData.camera");
        this.time = group.resolve("frameData.time");
    }

    @Override
    public void setCamera(Camera camera) {
        this.camera = camera;
    }

    @Override
    public void onDraw() {
        if (camera != null) {
            cameraHandle.write((slice) -> {
                slice.putMat4(camera.projectionMatrix());
                slice.putMat4(camera.viewMatrix());
                slice.putMat4(new Matrix4f(camera.projectionMatrix()).invert());
                slice.putMat4(new Matrix4f(camera.viewMatrix()).invert());
                slice.putFloat3(camera.lookAt().x, camera.lookAt().y, camera.lookAt().z);
                slice.putFloat3(camera.position().x, camera.position().y, camera.position().z);
            });
        }
        time.write((slice) -> slice.putFloat((System.nanoTime() - startTime) / 1_000_000_000f));
    }
}
