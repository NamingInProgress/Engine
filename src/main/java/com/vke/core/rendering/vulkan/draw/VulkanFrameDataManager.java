package com.vke.core.rendering.vulkan.draw;

import com.vke.api.rendering.abstraction.renderer.data.FrameDataManager;
import com.vke.api.rendering.vulkan.descriptors2.DescriptorSetGroup;
import com.vke.api.rendering.vulkan.descriptors2.handles.buf.BufferHandle;
import com.vke.api.rendering.vulkan.descriptors2.handles.buf.FieldHandle;
import com.vke.core.color.RgbColor;
import com.vke.impl.gameobject.CameraGameObject;
import com.vke.core.game.object.GameObjectTransform;
import com.vke.core.rendering.vulkan.descriptor.EngineDescriptorSetsManager;
import com.vke.impl.rendering.debug.DebugContext;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

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
    private final Vector4f lookAt = new Vector4f();
    private final Matrix4f invertProj = new Matrix4f();
    private final Matrix4f invertView = new Matrix4f();
    private final Vector3f worldPos = new Vector3f();

    @Override
    public void onDraw() {
        if (camera != null) {
            GameObjectTransform transform = camera.getTransform();
            transform.getWorldPosition(worldPos);
            transform.getWorldForward(lookAt);

            cameraHandle.write((slice) -> {
                Matrix4f proj = camera.getProjectionMatrix();
                camera.getViewMatrixInvert(invertView);
                proj.invert(invertProj);
                invertView.invert(viewMatrix);
                slice.mat4(proj);
                slice.mat4(viewMatrix);
                slice.mat4(invertProj);
                slice.mat4(invertView);
                slice.float4(lookAt.x, lookAt.y, lookAt.z, 0);
                slice.float4(worldPos.x, worldPos.y, worldPos.z, 0);
            });
        }
        time.write((slice) -> slice.float1((System.nanoTime() - startTime) / 1_000_000_000f));
    }
}
