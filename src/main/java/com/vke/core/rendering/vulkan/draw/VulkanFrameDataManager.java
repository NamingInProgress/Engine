package com.vke.core.rendering.vulkan.draw;

import com.vke.api.game.camera.Camera;
import com.vke.api.rendering.abstraction.renderer.data.FrameDataManager;
import com.vke.api.rendering.vulkan.descriptors2.handles.buf.BufferHandle;
import com.vke.api.rendering.vulkan.descriptors2.handles.buf.MultiWriteFieldHandle;
import com.vke.core.rendering.vulkan.descriptor.EngineDescriptorSetsManager;

public class VulkanFrameDataManager implements FrameDataManager {

    private final EngineDescriptorSetsManager mgr;

    private final BufferHandle handle;
    private final MultiWriteFieldHandle cameraHandle;

    private Camera camera;

    public VulkanFrameDataManager(EngineDescriptorSetsManager mgr) {
        this.mgr = mgr;
        this.handle = mgr.ENGINE_PIPELINE_LAYOUT.getGroup().resolve("frameData");
        this.cameraHandle = mgr.ENGINE_PIPELINE_LAYOUT.getGroup().resolve("frameData.camera");
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
    }
}
