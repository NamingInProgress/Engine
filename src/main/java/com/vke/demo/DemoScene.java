package com.vke.demo;

import com.vke.api.audio.playback.PlayingAudio;
import com.vke.api.scene.Scene;
import com.vke.api.window.Window;
import com.vke.core.Context;
import com.vke.core.Identifier;
import com.vke.core.color.RgbColor;
import com.vke.core.ecs.CRef;
import com.vke.core.game.camera.controllers.FreecamController;
import com.vke.core.game.object.GameObject;
import com.vke.core.input.PressableState;
import com.vke.core.input.keyboard.Key;
import com.vke.core.input.keyboard.KeyboardInput;
import com.vke.core.input.service.InputManager;
import com.vke.core.rendering.graph2.GraphContext;
import com.vke.core.services2.Services;
import com.vke.impl.ecs.text.BasicTextC;
import com.vke.impl.gameobject.CameraGameObject;
import com.vke.impl.gameobject.DirectionalLightGameObject;
import com.vke.impl.gameobject.PointLightGameObject;
import com.vke.impl.gameobject.SpotLightGameObject;
import com.vke.impl.gameobject.convenientapi.GameAudio;
import com.vke.impl.gameobject.convenientapi.GameMeshes;
import com.vke.impl.gameobject.convenientapi.GameObjects;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.io.IOException;

public class DemoScene extends Scene {

    private PressableState keyEsc;
    private PressableState keyToggleCursor;
    private PressableState keyLogCamera;
    private boolean lockedCursor = true;

    private CameraGameObject cam;
    private GameObject bear1, cube, text, sphere, bear2;

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
    public void onLoad() throws IOException {
        loadMeshResources();

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
        dirLight.setColor(RgbColor.WHITE);
        dirLight.setIntensity(10);
        dirLight.getTransform().setX(20);
        //dirLight.getTransform().setRotationXYZ(-90, 0, 0);
        dirLight.getTransform().setRotation(new Quaternionf(2.256E-1, -4.214E-1, -1.091E-1, -8.716E-1));

        setupInputAndCamera();
    }

    private void loadMeshResources() {
        try {
            bear1 = GameObjects.staticMesh(context);
            GameMeshes.setMeshStatic(bear1, "vke:bear.obj");

            bear2 = bear1.duplicate();
            GameMeshes.setMaterialStatic(bear2, "vke:materials/bear.vcl");
            bear2.getTransform().setXYZ(30, 0, 0);

            bear1.getTransform().addChild(bear2);

            sphere = bear1.duplicate();
            GameMeshes.setMaterialStatic(sphere, "vke:materials/bear.vcl");
            sphere.getTransform().setXYZ(5, 0, 0);

            bear2.getTransform().addChild(sphere);

            //PlayingAudio audio = GameAudio.playAudio(bear2, "preloaded.wav");
            //audio.setLooping(true);

            cube = GameObjects.staticMesh(context);
            cube.getTransform().setXYZ(-30, 0, 0);
            cube.getTransform().changeScaleXYZ(5, 5, 5);
            GameMeshes.setMaterialStatic(cube, "vke:materials/emissive-cube.vcl");

            GameObject floor = GameObjects.staticMesh(context);
            floor.getTransform().setXYZ(0, -25, 0);
            floor.getTransform().changeScaleXYZ(50, 1, 50);
            GameMeshes.setMaterialStatic(floor, "vke:materials/emissive-cube.vcl");

            text = GameObjects.basicText(context);
            CRef<BasicTextC> textC = text.getComponent(BasicTextC.ID);
            textC.with((c, idx) -> {
                c.setText(idx, "Hello world!");
                c.setFontScale(idx, 10);
            });
            text.getTransform().setPosition(new Vector3f(0, 0, -5));
            text.getTransform().setScaleXYZ(0.01f, 0.01f, 0.01f);
            text.getTransform().setRotationXYZ(0, 90, 0);

            bear1.getTransform().setScaleXYZ(1, 1, 1);
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
        cam.getTransform().setXYZ(0, 0, 0);
        cam.getTransform().setRotationXYZ(0, -90, 0);

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

    @Override
    public void onPrepareRendering(GraphContext context) {
        handleInput();
//        bear1.getTransform().setXYZ((float) Math.sin(Math.toRadians(System.nanoTime() / 1_000_000_000)), 0, 0);
        bear1.getTransform().setRotationXYZ(0, (float) System.nanoTime() / 1_000_000_0, 0);
        bear2.getTransform().setRotationXYZ(0, (float) System.nanoTime() / 500_000_0, 0);
//        cube.getTransform().setRotationXYZ(0, System.nanoTime() / 1_000_000_0, 0);
    }

    private void handleInput() {
        if (keyEsc.isPressed()) {
            this.context.getEngine().getWindow().requestClose();
        }

        if (keyToggleCursor.wasJustPressed()) {
            lockedCursor = !lockedCursor;
            Window window = context.getEngine().getWindow();
            if (lockedCursor) {
                window.showCursor();
            } else {
                window.hideCursor();
            }
        }

        if (keyLogCamera.wasJustPressed()) {
            System.out.println("Camera position: " + cam.getTransform().getPosition());
            System.out.println("Camera rotaton: " + cam.getTransform().getRotation());
            try {
                getEnvironment().setEnvironmentMap("cubemaps/testMap.exr");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}