package com.vke.core.rendering.vulkan.draw;

import com.vke.api.game.camera.Camera;
import com.vke.api.rendering.abstraction.renderer.data.FrameDataManager;
import com.vke.api.rendering.vulkan.descriptors2.DescriptorSetGroup;
import com.vke.api.rendering.vulkan.descriptors2.handles.buf.BufferHandle;
import com.vke.api.rendering.vulkan.descriptors2.handles.buf.FieldHandle;
import com.vke.api.rendering.vulkan.descriptors2.handles.buf.MultiWriteFieldHandle;
import com.vke.core.rendering.vulkan.descriptor.EngineDescriptorSetsManager;

public class VulkanFrameDataManager implements FrameDataManager {

    private final EngineDescriptorSetsManager mgr;

    private final BufferHandle handle;
    private final MultiWriteFieldHandle cameraHandle;
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
            });
        }
        time.write((slice) -> slice.putFloat((System.nanoTime() - startTime) / 1_000_000_000f));
    }
}
