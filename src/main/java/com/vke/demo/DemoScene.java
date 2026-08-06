package com.vke.demo;

import com.vke.api.assets.r.R;
import com.vke.api.game.camera.Camera;
import com.vke.api.game.camera.CameraController;
import com.vke.api.rendering.abstraction.renderer.RenderResourceManager;
import com.vke.api.rendering.abstraction.renderer.data.StaticMesh;
import com.vke.api.rendering.pbr.Material;
import com.vke.api.scene.Scene;
import com.vke.core.Context;
import com.vke.core.color.Color;
import com.vke.core.game.camera.PerspectiveCamera;
import com.vke.core.game.camera.controllers.FreecamController;
import com.vke.core.input.PressableState;
import com.vke.core.input.keyboard.Key;
import com.vke.core.input.keyboard.KeyboardInput;
import com.vke.core.input.service.InputManager;
import com.vke.core.mesh.MeshPrefab;
import com.vke.core.rendering.graph.GraphContext;
import com.vke.core.services2.Services;
import com.vke.impl.debug.DebugContext;
import com.vke.impl.vertex.VertexFormatDeferred;
import com.vke.utils.io.Identifier;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryUtil;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.glfw.GLFW.GLFW_CURSOR;
import static org.lwjgl.glfw.GLFW.GLFW_CURSOR_NORMAL;
import static org.lwjgl.glfw.GLFW.glfwSetInputMode;

public class DemoScene extends Scene {

    public static StaticMesh MESH;

    // Grid configuration
    private static final int GRID_SIZE_X = 10;
    private static final int GRID_SIZE_Y = 10;
    private static final int GRID_SIZE_Z = 10;
    private static final int TOTAL_INSTANCES = GRID_SIZE_X * GRID_SIZE_Y * GRID_SIZE_Z;
    private static final float SPACING = 5.0f;

    private final List<Instance> instances = new ArrayList<>(TOTAL_INSTANCES);
    private ByteBuffer matrixBuffer;

    private Camera camera;
    private PressableState keyEsc;
    private PressableState keyToggleCursor;
    private PressableState keyLogCamera;
    private boolean lockedCursor = true;

    public DemoScene(Identifier name, Context context) {
        super(name, context);
    }

    @Override
    public void onLoad() {
        loadMeshResources();
        setupInputAndCamera();
        buildGridInstances();

        // Allocate 64 bytes per 4x4 float matrix
        matrixBuffer = MemoryUtil.memAlloc(TOTAL_INSTANCES * 64);
    }

    private void loadMeshResources() {
        try {
            MeshPrefab prefab = R.meshprefabs.get("bck.obj").acquire(context);
            Material mat = R.materials.get("vke:materials/bear.vcl").acquire(context);

            RenderResourceManager resManager = getRenderer().resourceManager();
            MESH = resManager.uploadStaticMesh(
                    prefab.toMesh(prefabVertex -> new VertexFormatDeferred(
                            prefabVertex.position()[0], prefabVertex.position()[1], prefabVertex.position()[2],
                            prefabVertex.normal()[0], prefabVertex.normal()[1], prefabVertex.normal()[2],
                            prefabVertex.uv()[0], prefabVertex.uv()[1],
                            mat
                    ))
            );
        } catch (IOException e) {
            throw new RuntimeException("Failed to load scene resources", e);
        }
    }

    private void setupInputAndCamera() {
        camera = new PerspectiveCamera(context, 90);
        CameraController controller = new FreecamController(context);
        camera.setController(controller);
        camera.use();

        InputManager input = context.service(Services.INPUT_MANAGER);
        KeyboardInput keyboard = input.keyboard();
        keyToggleCursor = keyboard.key(Key.T);
        keyEsc = keyboard.key(Key.ESCAPE);
        keyLogCamera = keyboard.key(Key.J);
    }

    private void buildGridInstances() {
        instances.clear();

        // Offset grid to center it around (0, 0, 0)
        float offsetX = (GRID_SIZE_X - 1) * SPACING * 0.5f;
        float offsetY = (GRID_SIZE_Y - 1) * SPACING * 0.5f;
        float offsetZ = (GRID_SIZE_Z - 1) * SPACING * 0.5f;

        for (int x = 0; x < GRID_SIZE_X; x++) {
            for (int y = 0; y < GRID_SIZE_Y; y++) {
                for (int z = 0; z < GRID_SIZE_Z; z++) {
                    Instance instance = new Instance();
                    instance.position.set(
                            x * SPACING - offsetX,
                            y * SPACING - offsetY,
                            z * SPACING - offsetZ
                    );

                    instance.matrix.identity().translate(instance.position);
                    instances.add(instance);
                }
            }
        }
    }

    @Override
    public void onPrepareRendering(GraphContext context) {
        handleInput();

        // Populate matrix buffer
        matrixBuffer.clear();
        for (int i = 0; i < instances.size(); i++) {
            instances.get(i).matrix.get(i * 64, matrixBuffer);
        }
        matrixBuffer.position(0);
        matrixBuffer.limit(TOTAL_INSTANCES * 64);

        context.put("mats", matrixBuffer);
        context.put("inst", TOTAL_INSTANCES);

        // Debug visualizers
        DebugContext.arrow(new Vector3f(0, 0, 0), new Vector3f(0, 10, 0), Color.RED);
        DebugContext.boundingBox(new Vector3f(-5, -5, -5), new Vector3f(5, 5, 5), Color.WHITE);
    }

    private void handleInput() {
        if (keyEsc.isPressed()) {
            this.context.getEngine().getWindow().requestClose();
        }

        if (keyToggleCursor.wasJustPressed()) {
            lockedCursor = !lockedCursor;
            if (lockedCursor) {
                glfwSetInputMode(getRenderSystem().windowHandle(), GLFW_CURSOR, GLFW_CURSOR_NORMAL);
            } else {
                getRenderSystem().getEngine().getWindow().disableCursor();
            }
        }

        if (keyLogCamera.wasJustPressed()) {
            System.out.println("Camera position: " + camera.position());
        }
    }

    @Override
    public void free() {
        if (matrixBuffer != null) {
            MemoryUtil.memFree(matrixBuffer);
            matrixBuffer = null;
        }
    }

    public static class Instance {
        public final Vector3f position = new Vector3f();
        public final Matrix4f matrix = new Matrix4f();
    }
}