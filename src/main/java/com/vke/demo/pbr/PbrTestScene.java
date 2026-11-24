package com.vke.demo.pbr;

import com.vke.api.rendering.pbr.BaseLayer;
import com.vke.api.rendering.pbr.Material;
import com.vke.api.scene.Scene;
import com.vke.api.window.Window;
import com.vke.core.Context;
import com.vke.core.Identifier;
import com.vke.core.color.RgbColor;
import com.vke.core.game.object.GameObject;
import com.vke.core.input.PressableState;
import com.vke.core.input.keyboard.Key;
import com.vke.core.input.keyboard.KeyboardInput;
import com.vke.core.input.service.InputManager;
import com.vke.core.rendering.graph2.GraphContext;
import com.vke.core.services2.Services;
import com.vke.impl.gameobject.CameraGameObject;
import com.vke.impl.gameobject.PointLightGameObject;
import com.vke.impl.gameobject.controller.FreecamController;
import com.vke.impl.gameobject.convenientapi.GameAudio;
import com.vke.impl.gameobject.convenientapi.GameMeshes;
import com.vke.impl.gameobject.convenientapi.GameObjects;
import org.joml.Vector3f;

import java.io.IOException;
import java.util.List;

public class PbrTestScene extends Scene {

    private final int nrRows = 7;
    private final int nrColumns = 7;
    private final float spacing = 2.5f;

    private PressableState keyEsc;
    private PressableState keyToggleCursor;
    private PressableState keyLogCamera;
    private boolean lockedCursor = true;

    private CameraGameObject cam;

    public PbrTestScene(Identifier name, Context context) {
        super(name, context);
    }

    @Override
    public void onLoad() throws Exception {
        GameObject go = GameObjects.staticMesh(context);

        GameMeshes.setMeshStatic(go, context.id("sphere.obj"));

        BaseLayer base = new BaseLayer(RgbColor.RED, BaseLayer.ALBEDO, BaseLayer.NORMAL, BaseLayer.METALLIC_ROUGHNESS, BaseLayer.OCCLUSION, BaseLayer.EMISSIVE, BaseLayer.SPECULAR);

        for (int row = 0; row < nrRows; ++row) {
            for (int col = 0; col < nrColumns; ++col) {
                BaseLayer m = (BaseLayer) base.copy();
                GameObject g = go.duplicate();

                m.roughness = Math.clamp((float)col / (float)nrColumns, 0.05f, 1.0f);
                m.metallic = (float)row / (float)nrRows;

                Material mat = new Material(getRenderSystem(), List.of(m));
                getRenderSystem().materialManager().registerMaterial(mat);

                GameMeshes.setMaterialStatic(g, mat);

                g.getTransform().setXYZ(
                        (float)(col - (nrColumns / 2)) * spacing,
                        (float)(row - (nrRows / 2)) * spacing,
                        -2.0f);
            }
        }

        go.destroy();

        PointLightGameObject lightBase = new PointLightGameObject(context);
        lightBase.spawn();
        lightBase.setColor(3, 3, 3);
        lightBase.setIntensity(10);

        Vector3f[] positions = new Vector3f[]{
                new Vector3f(-10,  10, 10),
                new Vector3f( 10,  10, 10),
                new Vector3f(-10, -10, 10),
                new Vector3f( 10,  -10, 10)
        };

        for (Vector3f position : positions) {
            PointLightGameObject light = lightBase.duplicate();
            light.getTransform().setPosition(position);
        }

        lightBase.destroy();

        getEnvironment().setEnvironmentMap("cubemaps/something.png");
        setupInputAndCamera();
    }

    private void setupInputAndCamera() {
        cam = new CameraGameObject(context);
        cam.spawn();
        cam.setIsOrtho(false);
        cam.control(new FreecamController(context));

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
        }
    }
}
