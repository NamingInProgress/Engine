package com.vke.demo;

import com.vke.api.assets.r.R;
import com.vke.api.rendering.abstraction.renderer.RenderResourceManager;
import com.vke.api.rendering.abstraction.renderer.data.StaticMesh;
import com.vke.api.rendering.pbr.Material;
import com.vke.api.scene.Scene;
import com.vke.api.window.Window;
import com.vke.core.Context;
import com.vke.core.Identifier;
import com.vke.core.audio.playback.service.AudioManagerMaster;
import com.vke.core.audio.playback2d.service.AudioManager2D;
import com.vke.core.audio.playback3d.Ear;
import com.vke.core.audio.playback3d.Speaker;
import com.vke.core.audio.playback3d.service.AudioManager3D;
import com.vke.core.audio.source.ToneGenerator;
import com.vke.core.color.RgbColor;
import com.vke.core.ecs.CRef;
import com.vke.core.game.camera.controllers.FreecamController;
import com.vke.impl.ecs.mesh.StaticMeshC;
import com.vke.impl.gameobject.*;
import com.vke.core.game.scene.service.HierarchyManager;
import com.vke.core.input.PressableState;
import com.vke.core.input.keyboard.Key;
import com.vke.core.input.keyboard.KeyboardInput;
import com.vke.core.input.service.InputManager;
import com.vke.core.mesh.MeshPrefab;
import com.vke.core.rendering.graph2.GraphContext;
import com.vke.core.services2.Services;
import com.vke.impl.gameobject.CameraGameObject;
import com.vke.impl.gameobject.DirectionalLightGameObject;
import com.vke.impl.gameobject.PointLightGameObject;
import com.vke.impl.gameobject.SpotLightGameObject;
import com.vke.impl.gameobject.convenientapi.GameAudio;
import com.vke.impl.rendering.debug.DebugContext;
import com.vke.impl.rendering.vertex.SceneVertexFormat;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.glfw.GLFW.*;

public class DemoScene extends Scene {

    public static StaticMesh MESH;

    // Grid configuration
    private static final int GRID_SIZE_X = 2;
    private static final int GRID_SIZE_Y = 1;
    private static final int GRID_SIZE_Z = 1;
    private static final int TOTAL_INSTANCES = GRID_SIZE_X * GRID_SIZE_Y * GRID_SIZE_Z;
    private static final float SPACING = 30.0f;

    private final List<Instance> instances = new ArrayList<>(TOTAL_INSTANCES);

    private PressableState keyEsc;
    private PressableState keyToggleCursor;
    private PressableState keyLogCamera;
    private boolean lockedCursor = true;

    private HierarchyManager hierarchyManager;

    private CameraGameObject cam;
    private EmptyGameObject bear1, cube;

    public static float[][] positions = {
            {45, -45, 45},
            {-45, -45, 45},
            {-45, -45, -45},
            {45, -45, -45},
            {45, 45, 45},
            {-45, 45, 45},
            {-45, 45, -45},
            {45, 45, -45},
            {0, 20, 0}
    };

    private SpotLightGameObject spotLight;

    public DemoScene(Identifier name, Context context) {
        super(name, context);
    }

    @Override
    public void onLoad() {
        loadMeshResources();
        buildGridInstances();

        hierarchyManager = context.service(Services.HIERARCHY);

        PointLightGameObject pointLightBase = new PointLightGameObject(getRenderSystem());
        pointLightBase.spawn();
        PointLightGameObject[] lights = pointLightBase.spawnBatch(positions.length);

        for (int i = 0; i < lights.length; i++) {
            PointLightGameObject light = lights[i];
            light.setColor(new RgbColor(0, 1, 1, 1));
            light.setIntensity(10);
            light.getTransform().setXYZ(positions[i][0], positions[i][1], positions[i][2]);
        }

        pointLightBase.destroy();

        spotLight = new SpotLightGameObject(getRenderSystem());
        spotLight.spawn();
        spotLight.setColor(new RgbColor(0, 1, 1, 1));
        spotLight.setIntensity(100);
        spotLight.setInnerConeAngle(5);
        spotLight.setOuterConeAngle(30);

        DirectionalLightGameObject dirLight = new DirectionalLightGameObject(getRenderSystem());
        dirLight.spawn();
        dirLight.setColor(RgbColor.BLUE);
        dirLight.setIntensity(10);
        dirLight.getTransform().setX(20);
        dirLight.getTransform().setRotationXYZ(-90, 0, 0);

        setupInputAndCamera();
    }

    private void loadMeshResources() {
        try {
            MeshPrefab prefab = R.meshprefabs.get("bear_smooth.obj").acquire(context);
            Material mat = R.materials.get("vke:materials/bear.vcl").acquire(context);
            Material cubeMat = R.materials.get("vke:materials/emissive-cube.vcl").acquire(context);

            RenderResourceManager resManager = getRenderSystem().resourceManager();
            MESH = resManager.uploadStaticMesh(
                    prefab.toMesh(SceneVertexFormat.MESH_VERTEX_FACTORY)
            );

            bear1 = new EmptyGameObject(context);
            bear1.spawn();
            bear1.addComponents(StaticMeshC.ID);
            CRef<StaticMeshC> ref = bear1.getComponent(StaticMeshC.ID);
            ref.with((c, i) -> c.setMesh(i, MESH));

            EmptyGameObject bear2 = bear1.duplicate();
            CRef<StaticMeshC> ref2 = bear2.getComponent(StaticMeshC.ID);
            ref2.with((c, idx) -> c.setMaterial(idx, mat));
            bear2.getTransform().setXYZ(30, 0, 0);

            cube = new EmptyGameObject(context);
            cube.spawn();
            cube.addComponents(StaticMeshC.ID);
            cube.getTransform().setXYZ(-30, 0, 0);
            cube.getTransform().changeScaleXYZ(5, 5, 5);
            CRef<StaticMeshC> ref3 = cube.getComponent(StaticMeshC.ID);
            ref3.with((c, idx) -> c.setMaterial(idx, cubeMat));

            EmptyGameObject floor = new EmptyGameObject(context);
            floor.spawn();
            floor.addComponents(StaticMeshC.ID);
            floor.getTransform().setXYZ(0, -25, 0);
            floor.getTransform().changeScaleXYZ(50, 1, 50);
            CRef<StaticMeshC> ref4 = floor.getComponent(StaticMeshC.ID);
            ref4.with((c, idx) -> c.setMaterial(idx, cubeMat));
        } catch (IOException e) {
            throw new RuntimeException("Failed to load scene resources", e);
        }
    }

    private void setupInputAndCamera() {
        cam = new CameraGameObject(context);
        cam.spawn();
        cam.setIsOrtho(false);
        cam.control(new FreecamController(context));

        cam.getTransform().addChild(spotLight);

        GameAudio.setEar(cam);

        getRenderSystem().frameDataManager().setCamera(cam);

        InputManager input = context.service(Services.INPUT_MANAGER);
        KeyboardInput keyboard = input.keyboard();
        keyToggleCursor = keyboard.key(Key.T);
        keyEsc = keyboard.key(Key.ESCAPE);
        keyLogCamera = keyboard.key(Key.J);

        Window window = context.getEngine().getWindow();
        keyboard.key(Key.F11).listen(newState -> {
            if (newState == PressableState.State.JustPressed) {
                Window.OpenWindowState winState = window.getOpenState();
                if (winState != Window.OpenWindowState.Fullscreen) {
                    winState = Window.OpenWindowState.Fullscreen;
                } else {
                    winState = Window.OpenWindowState.Normal;
                }
                window.setOpenState(winState);
            }
        });
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

                    instance.matrix.identity().translate(instance.position);//.scale(3, 3, 3);//.rotateXYZ((float) Math.random(), (float) Math.random(), (float) Math.random());
                    instances.add(instance);
                    //inst.add(new MeshInstance(instance.matrix, x));
                }
            }
        }
    }

    @Override
    public void onPrepareRendering(GraphContext context) {
        handleInput();
        Vector3f camWorldPos = cam.getTransform().getWorldPosition();
        Quaternionf camRot = cam.getTransform().getRotation();

        hierarchyManager.updateTransforms();
        context.put("inst", TOTAL_INSTANCES);

        // Debug visualizers
        //DebugContext.arrow(new Vector3f(0, 0, 0), new Vector3f(0, 10, 0), Color.RED);
        //DebugContext.boundingBox(new Vector3f(-5, -5, -5), new Vector3f(5, 5, 5), Color.WHITE);
        for (int i = 0; i < positions.length; i++) {
            float[] poss = positions[i];
            float y = poss[1];
//            if (i == positions.length - 1) {
//                poss[1] += (float) (20 * Math.max(Math.sin(System.nanoTime() / 1_000_000_000.0), 0.0));
//            }
            DebugContext.boundingBox(new Vector3f(poss[0] - 1, poss[1] - 1, poss[2] - 1),
                    new Vector3f(poss[0] + 1, poss[1] + 1, poss[2] + 1), RgbColor.RED);
            poss[1] = y;
        }
//        bear1.getTransform().setXYZ((float) Math.sin(Math.toRadians(System.nanoTime() / 1_000_000_000)), 0, 0);
//        bear1.getTransform().setRotationXYZ(0, System.nanoTime() / 1_000_000_0, 0);
//        cube.getTransform().setRotationXYZ(0, System.nanoTime() / 1_000_000_0, 0);
//        DebugContext.boundingBox(new Vector3f(0, 0, 0), new Vector3f(45, 45, 45), Color.RED);
//        DebugContext.boundingBox(new Vector3f(0, 0, 0), new Vector3f(-45, -45, -45), Color.BLUE);
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
                getRenderSystem().getEngine().getWindow().hideCursor();
            }
        }

        if (keyLogCamera.wasJustPressed()) {
            System.out.println("Camera position: " + cam.getTransform().getPosition());
        }
    }

    @Override
    public void free() {}

    public static class Instance {
        public final Vector3f position = new Vector3f();
        public final Matrix4f matrix = new Matrix4f();
    }
}