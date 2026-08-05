package com.vke.demo;

import com.vke.api.assets.r.R;
import com.vke.api.rendering.abstraction.draw.Vertex;
import com.vke.api.game.camera.Camera;
import com.vke.api.game.camera.CameraController;
import com.vke.api.rendering.abstraction.renderer.RenderResourceManager;
import com.vke.api.rendering.abstraction.renderer.data.StaticMesh;
import com.vke.api.rendering.abstraction.renderer.data.Texture;
import com.vke.api.rendering.pbr.Material;
import com.vke.core.color.Color;
import com.vke.core.input.PressableState;
import com.vke.core.input.keyboard.Key;
import com.vke.core.input.keyboard.KeyboardInput;
import com.vke.core.input.service.InputManager;
import com.vke.core.rendering.graph.GraphContext;
import com.vke.core.rendering.graph.RenderGraph;
import com.vke.api.scene.Scene;
import com.vke.core.Context;
import com.vke.core.game.camera.PerspectiveCamera;
import com.vke.core.game.camera.controllers.FreecamController;
import com.vke.core.mesh.MeshPrefab;
import com.vke.core.services2.Services;
import com.vke.impl.debug.DebugContext;
import com.vke.impl.vertex.VertexFormatDeferred;
import com.vke.utils.io.Identifier;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryUtil;
import pl.epsi.MakeVertex;
import pl.epsi.Type;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.lwjgl.glfw.GLFW.*;

public class DemoScene extends Scene {

    public DemoScene(Identifier name, Context context) {
        super(name, context);
    }

    public static StaticMesh MESH;

    private static final long START = System.nanoTime();
    private RenderGraph graph;

    private PressableState esc, t, j;

    Random random = new Random();

    List<Instance> instances = new ArrayList<>(100000);

    ByteBuffer buf;
    float size = 50;

    Camera camera;

    @Override
    public void onLoad() {
        MeshPrefab prefab;
        Material mat;
        Texture t1, t2;
        try {
            prefab = R.meshprefabs.get("backpack.obj").acquire(context);
            t1 = R.textures.get("vke:textures/diffuse.png").acquire(context);
            t2 = R.textures.get("vke:textures/specular.png").acquire(context);
            mat = R.materials.get("vke:materials/bear.vcl").acquire(context);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        float[] color = {1, 1, 1, 1};

        RenderResourceManager resManager = getRenderer().resourceManager();

        MESH = resManager.uploadStaticMesh(
                prefab.toMesh((prefabVertex -> new VertexFormatDeferred(
                        prefabVertex.position()[0],
                        prefabVertex.position()[1],
                        prefabVertex.position()[2],

                        prefabVertex.normal()[0],
                        prefabVertex.normal()[1],
                        prefabVertex.normal()[2],

                        prefabVertex.uv()[0],
                        prefabVertex.uv()[1],
                        mat)))
        );

        camera = new PerspectiveCamera(context, 90);
        CameraController controller = new FreecamController(context);
        camera.setController(controller);

        InputManager input = context.service(Services.INPUT_MANAGER);

        KeyboardInput keyboard = input.keyboard();
        t = keyboard.key(Key.T);
        esc = keyboard.key(Key.ESCAPE);
        j = keyboard.key(Key.J);

        camera.use();

        for (int i = 0; i < 100000; i++) {
            Instance instance = new Instance();

            instance.position.set(
                    (random.nextFloat() - 0.5f) * size,
                    (random.nextFloat() - 0.5f) * size,
                    (random.nextFloat() - 0.5f) * size
            );

            instance.velocity.set(
                    (random.nextFloat() - 0.5f) * 0.3f,
                    (random.nextFloat() - 0.5f) * 0.3f,
                    (random.nextFloat() - 0.5f) * 0.3f
            );

            instance.rotation.set(
                    random.nextFloat() * (float)Math.PI * 2f,
                    random.nextFloat() * (float)Math.PI * 2f,
                    random.nextFloat() * (float)Math.PI * 2f
            );

            instance.angularVelocity.set(
                    (random.nextFloat() - 0.5f) * 0.01f,
                    (random.nextFloat() - 0.5f) * 0.01f,
                    (random.nextFloat() - 0.5f) * 0.01f
            );

            instances.add(instance);
        }

        float scale = 10;

        float half = size / 2;

        for (Instance instance : instances) {
            instance.position.fma(0.1f, instance.velocity);
            instance.rotation.fma(0.01f, instance.angularVelocity);

            // Bounce off cube walls
            if (instance.position.x > half || instance.position.x < -half) {
                instance.velocity.x *= -1;
                instance.position.x = Math.clamp(instance.position.x, -half, half);
            }

            if (instance.position.y > half || instance.position.y < -half) {
                instance.velocity.y *= -1;
                instance.position.y = Math.clamp(instance.position.y, -half, half);
            }

            if (instance.position.z > half || instance.position.z < -half) {
                instance.velocity.z *= -1;
                instance.position.z = Math.clamp(instance.position.z, -half, half);
            }

            instance.matrix.identity()
                    .translate(instance.position);
        }
        buf = MemoryUtil.memAlloc(100000 * 64);
    }

    private boolean lockedCursor = true;

    @Override
    public void onPrepareRendering(GraphContext context) {
        if (esc.isPressed()) this.context.getEngine().getWindow().requestClose();
        if (t.wasJustPressed()) {
            lockedCursor = !lockedCursor;
            if (lockedCursor) {
                glfwSetInputMode(getRenderSystem().windowHandle(), GLFW_CURSOR, GLFW_CURSOR_NORMAL);
            } else {
                getRenderSystem().getEngine().getWindow().disableCursor();
            }
        }

        if (j.wasJustPressed()) {
            System.out.println(camera.position());
        }

        Matrix4f model = new Matrix4f();

        float time = (System.nanoTime() / 1_000_000_000.0f);

        float speed = 1.0f;



        buf.position(0);
        buf.limit(100000 * 64);

        for (Instance instance : instances) {


//                    .rotateXYZ(
//                            instance.rotation.x,
//                            instance.rotation.y,
//                            instance.rotation.z
//                    );

            instance.matrix.get(buf);
            buf.position(buf.position() + 64);
        }
        buf.flip();

//        model.identity()
//                .translate(0, 0.0f, 0)
//                .scale(scale, scale, scale)
//                .rotateY(time * speed);
        context.put("mats", buf);
        //PostProcessingRenderPass.disableStages(context, "blur", "invert_colors", "idk_something");

        DebugContext.arrow(new Vector3f(0, 0, 0), new Vector3f(0, 10, 0), Color.RED);
        DebugContext.boundingBox(new Vector3f(-5, -5, -5), new Vector3f(5, 5, 5), Color.WHITE);
    }

    @Override
    public void free() {

    }

    public static class Instance {
        Vector3f position = new Vector3f();
        Vector3f velocity = new Vector3f();

        Vector3f rotation = new Vector3f();
        Vector3f angularVelocity = new Vector3f();

        Matrix4f matrix = new Matrix4f();
    }

}
