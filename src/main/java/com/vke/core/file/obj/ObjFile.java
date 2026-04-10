package com.vke.core.file.obj;

import com.carrotsearch.hppc.IntArrayList;
import com.carrotsearch.hppc.ObjectIntHashMap;
import com.vke.api.draw.MeshPrefab;
import com.vke.api.rendering.vulkan.buffer.CpuBuffer;
import com.vke.core.file.utils.DataUtils;
import com.vke.utils.iter.Iter;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;

public class ObjFile {
    private final ArrayList<float[]> positions;
    private final ArrayList<float[]> normals;
    private final ArrayList<float[]> uvs;

    private final ObjectIntHashMap<VertexKey> seenVertices;
    private ArrayList<MeshPrefab.PrefabVertex> vertices;
    private final IntArrayList indices;

    private MeshPrefab meshCache;

    public ObjFile(InputStream input) throws ObjException {
        this.positions = new ArrayList<>();
        this.normals = new ArrayList<>();
        this.uvs = new ArrayList<>();

        this.seenVertices = new ObjectIntHashMap<>();
        this.vertices = null;
        this.indices = new IntArrayList();

        Reader reader = new InputStreamReader(input, StandardCharsets.UTF_8);
        Iter<String> lines = DataUtils.readerLines(reader);
        for (String line : lines) {
            String[] parts = line.split("\\s+");
            if (parts.length == 0) throw new ObjException("Invalid length of line! At least 1 is needed! No Command!");

            String[] arguments = new String[parts.length - 1];
            System.arraycopy(parts, 1, arguments, 0, arguments.length);

            String command = parts[0];
            switch (command) {
                case "v" -> handleVertex(arguments);
                case "vn" -> handleVertexNormal(arguments);
                case "vt" -> handleVertexTexture(arguments);
                case "f" -> handleFace(arguments);
                default -> {} //skip for now
            }
        }
    }

    private void handleFace(String[] args) throws ObjException {
        if (vertices == null) {
            //educated guess
            vertices = new ArrayList<>((int) ((double) positions.size() * CpuBuffer.GROWTH_FAC));
        }

        if (args.length != 3) throw new ObjException("Illegal amount of face arguments: " + args.length);
        for (int i = 0; i < 3; i++) {
            String arg = args[i];

            VertexKey vertexKey;
            if (arg.contains("/")) {
                //either v/vt or v//vn or v/vt/vn
                if (arg.contains("//")) {
                    //  v//vn
                    String[] elements = arg.split("//");
                    int v = Integer.parseInt(elements[0]) - 1;
                    int vn = Integer.parseInt(elements[1]) - 1;
                    vertexKey = new VertexKey(positions.get(v), normals.get(vn), noUv());
                } else {
                    //  either v/vt or v/vt/vn
                    String[] elements = arg.split("/");
                    if (elements.length == 2) {
                        // v/vt
                        int v = Integer.parseInt(elements[0]) - 1;
                        int vt = Integer.parseInt(elements[1]) - 1;
                        vertexKey = new VertexKey(positions.get(v), noNorm(), uvs.get(vt));
                    } else if (elements.length == 3) {
                        // v/vt/vn
                        int v = Integer.parseInt(elements[0]) - 1;
                        int vt = Integer.parseInt(elements[1]) - 1;
                        int vn = Integer.parseInt(elements[2]) - 1;
                        vertexKey = new VertexKey(positions.get(v), normals.get(vn), uvs.get(vt));
                    } else {
                        throw new ObjException("Illegal amount of face segment elements: " + elements.length);
                    }
                }
            } else {
                //just v
                int v = Integer.parseInt(arg) - 1;
                vertexKey = new VertexKey(positions.get(v), noNorm(), noUv());
            }

            if (seenVertices.containsKey(vertexKey)) {
                int index = seenVertices.get(vertexKey);
                indices.add(index);
            } else {
                int index = vertices.size();
                vertices.add(vertexKey.toPrefabVertex());
                indices.add(index);
            }
        }
    }

    private static float[] noNorm() {
        return new float[] {0,0,0};
    }

    private static float[] noUv() {
        return new float[] {0,0};
    }

    private void handleVertexTexture(String[] args) throws ObjException {
        try {
            Float[] floats = Iter.of(args).faultyMap(Float::parseFloat).toArray();
            if (floats.length < 2 || floats.length > 3) {
                throw new ObjException("Illegal amount of texture arguments: " + floats.length);
            }

            float[] uv = new float[2];
            uv[0] = floats[0];
            uv[1] = floats[1];

            uvs.add(uv);
        } catch (RuntimeException e) {
            throw new ObjException(e);
        }
    }

    private void handleVertexNormal(String[] args) throws ObjException {
        try {
            Float[] floats = Iter.of(args).faultyMap(Float::parseFloat).toArray();
            if (floats.length != 3) {
                throw new ObjException("Illegal amount of normal arguments: " + floats.length);
            }

            float[] normal = new float[3];
            for (int i = 0; i < 3; i++) {
                normal[i] = floats[i];
            }

            normals.add(normal);
        } catch (RuntimeException e) {
            throw new ObjException(e);
        }
    }

    private void handleVertex(String[] args) throws ObjException {
        try {
            Float[] floats = Iter.of(args).faultyMap(Float::parseFloat).toArray();
            if (floats.length != 3 && floats.length != 4) throw new ObjException("Illegal amount of vertex arguments: " + floats.length);
            float[] xyz = new float[3];

            for (int i = 0; i < 3; i++) {
                xyz[i] = floats[i];
            }

            positions.add(xyz);
        } catch (RuntimeException probablyTheNumberFormatExceptionFromWithinTheIter) {
            throw new ObjException(probablyTheNumberFormatExceptionFromWithinTheIter);
        }
    }

    public MeshPrefab toMeshPrefab() {
        if (meshCache != null) return meshCache;
        int[] ind = indices.toArray();
        meshCache = new MeshPrefab(vertices.toArray(MeshPrefab.PrefabVertex[]::new), ind);
        return meshCache;
    }

    private record VertexKey(float[] pos, float[] norm, float[] uv) {
        public MeshPrefab.PrefabVertex toPrefabVertex() {
            return new MeshPrefab.PrefabVertex(pos, norm, uv);
        }
    }
}
