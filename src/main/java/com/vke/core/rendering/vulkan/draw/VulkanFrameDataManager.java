package com.vke.core.rendering.vulkan.draw;

import com.vke.api.rendering.abstraction.renderer.data.FrameDataManager;
import com.vke.api.rendering.vulkan.descriptors2.DescriptorSetGroup;
import com.vke.api.rendering.vulkan.descriptors2.handles.buf.BufferHandle;
import com.vke.api.rendering.vulkan.descriptors2.handles.buf.FieldHandle;
import com.vke.core.game.object.CameraGameObject;
import com.vke.core.game.object.GameObjectTransform;
import com.vke.core.rendering.vulkan.descriptor.EngineDescriptorSetsManager;
import org.joml.Matrix4f;
import org.joml.Vector3f;

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
        if (!camera.isSpawned()) {
            throw new IllegalStateException("This camera hasnt been spawned in yet!");
        }
        this.camera = camera;
    }

    private final Matrix4f viewMatrix = new Matrix4f();
    private final Vector3f lookAt = new Vector3f();
    private final Matrix4f invertProj = new Matrix4f();
    private final Matrix4f invertView = new Matrix4f();
    private final Vector3f worldPos = new Vector3f();

    @Override
    public void onDraw() {
        if (camera != null) {
            GameObjectTransform transform = camera.getTransform();
            transform.getWorldPosition(worldPos);

            cameraHandle.write((slice) -> {
                Matrix4f proj = camera.getProjectionMatrix();
                camera.getViewMatrixInvert(invertView);
                proj.invert(invertProj);
                invertView.invert(viewMatrix);
                camera.lookAt(lookAt);
                slice.mat4(proj);
                slice.mat4(viewMatrix);
                slice.mat4(invertProj);
                slice.mat4(invertView);
                slice.float3(lookAt.x, lookAt.y, lookAt.z);
                slice.float3(worldPos.x, worldPos.y, worldPos.z);
            });
        }
        time.write((slice) -> slice.float1((System.nanoTime() - startTime) / 1_000_000_000f));
    }
}
