package com.vke.demo;

import com.vke.api.assets.r.R;
import com.vke.api.parsing.config.schema.SchemaMismatchException;
import com.vke.api.rendering.abstraction.draw.Vertex;
import com.vke.api.game.camera.Camera;
import com.vke.api.game.camera.CameraController;
import com.vke.api.rendering.abstraction.renderer.RenderResourceManager;
import com.vke.api.rendering.abstraction.renderer.data.StaticMesh;
import com.vke.api.rendering.abstraction.renderer.data.VertexEncoder;
import com.vke.api.rendering.abstraction.rendergraph.RenderGraphDefinition;
import com.vke.api.scene.RenderingScene;
import com.vke.core.Context;
import com.vke.core.game.camera.PerspectiveCamera;
import com.vke.core.game.camera.controllers.FreecamController;
import com.vke.core.game.camera.controllers.PoopWASDController;
import com.vke.core.mesh.MeshPrefab;
import com.vke.core.rendering.pipeline.RenderPipelines;
import com.vke.utils.io.Identifier;
import org.joml.Matrix4f;

import java.io.IOException;

public class DemoScene extends RenderingScene {

    public DemoScene(Identifier name, Context context) {
        super(name, context);
    }

    private StaticMesh mesh;

    private static final long START = System.nanoTime();

    @Override
    public void onLoad() {
        try {
            RenderGraphDefinition rgd = new RenderGraphDefinition(context, new Identifier("renderpasses/3d.vcl"));
        } catch (IOException | SchemaMismatchException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        MeshPrefab prefab;
        try {
            prefab = R.meshprefabs.get("bear.obj").acquire(context);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        float[] color = {1, 1, 1, 1};

        RenderResourceManager resManager = getRenderer().resourceManager();

        mesh = resManager.uploadStaticMesh(
                prefab.toMesh((prefabVertex -> new CubeVertexFormat(
                        prefabVertex.position()[0],
                        prefabVertex.position()[1],
                        prefabVertex.position()[2],

                        prefabVertex.normal()[0],
                        prefabVertex.normal()[1],
                        prefabVertex.normal()[2],

                        color[0],
                        color[1],
                        color[2],
                        color[3])))
        );

        Camera camera = new PerspectiveCamera(context, 90);
        CameraController controller = new FreecamController(context);
        camera.setController(controller);

        camera.use();
    }

    @Override
    public void onDraw() {
        Matrix4f model = new Matrix4f();

        float time = ((System.nanoTime() - START) / 1_000_000_000.0f);

        float speed = 1.0f;

        float scale = 10;
        model.identity()
                .translate(0, 0.0f, -550)
                .scale(scale, scale, scale)
                .rotateY(time * speed);

        RenderPipelines.DEMO.setLocal(model);
        RenderPipelines.DEMO.use();
        mesh.draw();
    }

    @Override
    public void free() {

    }

    public static class CubeVertexFormat implements Vertex {

        private float x, y, z;
        private float nx, ny, nz;
        private float r, g, b, a;

        public CubeVertexFormat(float x, float y, float z, float nx, float ny, float nz, float r, float g, float b, float a) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.nx = nx;
            this.ny = ny;
            this.nz = nz;
            this.r = r;
            this.g = g;
            this.b = b;
            this.a = a;
        }

        @Override
        public int getByteStride() {
            return 4*10;
        }

        @Override
        public void putSelf(VertexEncoder buf) {
            buf.float3(x, y, z);
            buf.float3(nx, ny, nz);
            buf.float4(r, g, b, a);
        }
    }

}
