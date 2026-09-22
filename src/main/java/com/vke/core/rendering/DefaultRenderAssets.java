package com.vke.core.rendering;

import com.vke.api.assets.r.R;
import com.vke.api.rendering.abstraction.renderer.RenderResourceManager;
import com.vke.api.rendering.abstraction.renderer.RenderSystem;
import com.vke.api.rendering.abstraction.renderer.data.MaterialManager;
import com.vke.api.rendering.abstraction.renderer.data.StaticMesh;
import com.vke.api.rendering.pbr.Material;
import com.vke.core.mesh.MeshPrefab;
import com.vke.impl.rendering.vertex.SceneVertexFormat;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

public class DefaultRenderAssets {

    @Nullable
    private static Material DEFAULT_MATERIAL;

    @Nullable
    private static StaticMesh CUBE_MESH;

    private static boolean initialized;
    private static RenderSystem sys;

    public static void initialize(RenderSystem sys) {
        try {
            DefaultRenderAssets.sys = sys;
            initMaterials(sys);
            initMeshes(sys);
        } catch (IOException e) {
            sys.throwException(new RuntimeException("Failed to initialize Default Render Assets", e), "DefaultRenderAssets");
        }

        DefaultRenderAssets.initialized = true;
    }

    private static void initMaterials(RenderSystem sys) throws IOException {
        DEFAULT_MATERIAL = R.materials.get("vke:materials/default.vcl").acquire(sys);
    }

    private static void initMeshes(RenderSystem sys) throws IOException {
        RenderResourceManager resManager = sys.resourceManager();
        MeshPrefab cubePrefab = R.meshprefabs.get("models/cube.obj").acquire(sys);

        CUBE_MESH = resManager.uploadStaticMesh(cubePrefab.toMesh(SceneVertexFormat.MESH_VERTEX_FACTORY));
    }

    public static RenderSystem getRenderSystem() {
        ensureInitialized();
        return sys;
    }

    public static Material defaultMaterial() {
        ensureInitialized();
        return DEFAULT_MATERIAL;
    }

    public static StaticMesh defaultMesh() {
        ensureInitialized();
        return CUBE_MESH;
    }

    public static int copyDefaultMaterialIndex() {
        ensureInitialized();
        MaterialManager mm = sys.materialManager();
        Material defaultMaterial = defaultMaterial().copy();
        return mm.registerMaterial(defaultMaterial);
    }

    private static void ensureInitialized() {
        if (!initialized) throw new IllegalStateException("Default Render Assets are not initialized!");
    }

}
