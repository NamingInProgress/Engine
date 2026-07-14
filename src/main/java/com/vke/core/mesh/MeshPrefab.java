package com.vke.core.mesh;

import com.vke.api.rendering.abstraction.draw.Vertex;
import com.vke.api.rendering.abstraction.draw.MeshVertexFactory;
import com.vke.api.serializer.Loader;
import com.vke.api.serializer.Saver;
import com.vke.api.serializer.Serializer;
import com.vke.core.serializer.LoadException;
import com.vke.core.serializer.SaveException;

public class MeshPrefab {
    private final PrefabVertex[] vertices;
    private final int[] indices;

    public MeshPrefab(PrefabVertex[] vertices, int[] indices) {
        this.vertices = vertices;
        this.indices = indices;
    }

    public Mesh toMesh(MeshVertexFactory factory) {
        Vertex[] vertices = new Vertex[this.vertices.length];
        for (int i = 0; i < vertices.length; i++) {
            vertices[i] = factory.formatVertex(this.vertices[i]);
        }
        return new Mesh(vertices, indices);
    }

    public static void registerSerializers() {
        Serializer.registerSerializerFor(PrefabVertex.class, new PrefabVertex.S());
        Serializer.registerSerializerFor(MeshPrefab.class, new MeshPrefab.S());
    }

    public record PrefabVertex(float[] position, float[] normal, float[] uv) {
        public static class S implements Serializer<PrefabVertex> {
            @Override
            public Class<?> getObjectClass() {
                return PrefabVertex.class;
            }

            @Override
            public void save(PrefabVertex value, Saver saver) throws SaveException {
                saver.saveFloat(value.position[0]);
                saver.saveFloat(value.position[1]);
                saver.saveFloat(value.position[2]);

                saver.saveFloat(value.normal[0]);
                saver.saveFloat(value.normal[1]);
                saver.saveFloat(value.normal[2]);

                saver.saveFloat(value.uv[0]);
                saver.saveFloat(value.uv[1]);
            }

            @Override
            public PrefabVertex load(Loader loader) throws LoadException {
                float[] position = new float[3];
                position[0] = loader.loadFloat();
                position[1] = loader.loadFloat();
                position[2] = loader.loadFloat();

                float[] normal = new float[3];
                normal[0] = loader.loadFloat();
                normal[1] = loader.loadFloat();
                normal[2] = loader.loadFloat();

                float[] uv = new float[2];
                uv[0] = loader.loadFloat();
                uv[1] = loader.loadFloat();

                return new PrefabVertex(position, normal, uv);
            }
        }
    }

    public static class S implements Serializer<MeshPrefab> {
        @Override
        public Class<?> getObjectClass() {
            return MeshPrefab.class;
        }

        @Override
        public void save(MeshPrefab value, Saver saver) throws SaveException {
            Serializer.saveObject(value.vertices, saver, false);
            saver.saveIntArray(value.indices);
        }

        @Override
        public MeshPrefab load(Loader loader) throws LoadException {
            PrefabVertex[] vertices = Serializer.loadObject(PrefabVertex[].class, loader, false);
            int[] indices = loader.loadIntArray();
            return new MeshPrefab(vertices, indices);
        }
    }
}
