package com.vke.impl.gameobject.convenientapi;

import com.vke.api.assets.AssetHandle;
import com.vke.api.assets.r.R;
import com.vke.api.rendering.abstraction.draw.MeshVertexFactory;
import com.vke.api.rendering.abstraction.renderer.RenderResourceManager;
import com.vke.api.rendering.abstraction.renderer.Renderer;
import com.vke.api.rendering.abstraction.renderer.data.StaticMesh;
import com.vke.api.rendering.pbr.Material;
import com.vke.core.Context;
import com.vke.core.Identifier;
import com.vke.core.ecs.CRef;
import com.vke.core.ecs.component.Component;
import com.vke.core.ecs.services.EcsManager;
import com.vke.core.game.object.GameObject;
import com.vke.core.mesh.Mesh;
import com.vke.core.mesh.MeshPrefab;
import com.vke.core.services2.Services;
import com.vke.impl.ecs.mesh.StaticMeshC;
import com.vke.impl.rendering.vertex.SceneVertexFormat;

import java.io.IOException;

public class GameMeshes {
    private static <C extends Component> void withComponent(GameObject object, @SuppressWarnings("all") int id, CRef.With<C> with) {
        CRef<C> ref = object.getComponent(id);
        ref.with((comp, i) -> {
            if (comp == null) {
                Context context = object.getContext();
                EcsManager ecs = context.service(Services.ECS);
                throw new IllegalArgumentException("GameObject doesnt have component " + ecs.getComponentName(id));
            }
            with.with(comp, i);
        });
    }

    public static void setMaterialStatic(GameObject object, Material material) {
        withComponent(object, StaticMeshC.ID, (StaticMeshC comp, int i) -> comp.setMaterial(i, material));
    }

    public static void setMaterialStatic(GameObject object, AssetHandle<Material> material) throws IOException {
        Context ctx = object.getContext();
        Material mat = material.acquire(ctx);
        setMaterialStatic(object, mat);
    }

    public static void setMaterialStatic(GameObject object, Identifier material) throws IOException {
        setMaterialStatic(object, R.materials.get(material));
    }

    public static void setMaterialStatic(GameObject object, String material) throws IOException {
        setMaterialStatic(object, R.materials.get(material));
    }

    public static void setMeshStatic(GameObject object, StaticMesh mesh) {
        withComponent(object, StaticMeshC.ID, (StaticMeshC comp, int i) -> comp.setMesh(i, mesh));
    }

    public static void setMeshStatic(GameObject object, MeshPrefab mesh, MeshVertexFactory factory) {
        Context context = object.getContext();
        Renderer renderer = context.service(Services.RENDERER);
        RenderResourceManager resMgr = renderer.renderSystem().resourceManager();
        Mesh<?> actualMesh = mesh.toMesh(factory);
        StaticMesh staticMesh = resMgr.uploadStaticMesh(actualMesh);
        setMeshStatic(object, staticMesh);
    }

    public static void setMeshStatic(GameObject object, AssetHandle<MeshPrefab> mesh, MeshVertexFactory factory) throws IOException {
        Context ctx = object.getContext();
        MeshPrefab prefab = mesh.acquire(ctx);
        setMeshStatic(object, prefab, factory);
    }

    public static void setMeshStatic(GameObject object, Identifier mesh, MeshVertexFactory factory) throws IOException {
        setMeshStatic(object, R.meshprefabs.get(mesh), factory);
    }

    public static void setMeshStatic(GameObject object, String mesh, MeshVertexFactory factory) throws IOException {
        setMeshStatic(object, R.meshprefabs.get(mesh), factory);
    }

    public static void setMeshStatic(GameObject object, MeshPrefab mesh) {
        Context context = object.getContext();
        Renderer renderer = context.service(Services.RENDERER);
        RenderResourceManager resMgr = renderer.renderSystem().resourceManager();
        MeshVertexFactory factory = SceneVertexFormat.MESH_VERTEX_FACTORY;
        Mesh<?> actualMesh = mesh.toMesh(factory);
        StaticMesh staticMesh = resMgr.uploadStaticMesh(actualMesh);
        setMeshStatic(object, staticMesh);
    }

    public static void setMeshStatic(GameObject object, AssetHandle<MeshPrefab> mesh) throws IOException {
        Context ctx = object.getContext();
        MeshPrefab prefab = mesh.acquire(ctx);
        setMeshStatic(object, prefab);
    }

    public static void setMeshStatic(GameObject object, Identifier mesh) throws IOException {
        setMeshStatic(object, R.meshprefabs.get(mesh));
    }

    public static void setMeshStatic(GameObject object, String mesh) throws IOException {
        setMeshStatic(object, R.meshprefabs.get(mesh));
    }
}
