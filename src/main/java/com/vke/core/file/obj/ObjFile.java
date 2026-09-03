package com.vke.core.file.obj;

import com.carrotsearch.hppc.IntArrayList;
import com.carrotsearch.hppc.ObjectIntHashMap;
import com.vke.core.file.utils.DataUtils;
import com.vke.core.mesh.MeshPrefab;
import com.vke.utils.iter.Iter;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class ObjFile {
    private final ArrayList<float[]> positions;
    private final ArrayList<float[]> normals;
    private final ArrayList<float[]> uvs;

    private final ObjectIntHashMap<VertexKey> seenVertices;
    private final ArrayList<MeshPrefab.PrefabVertex> vertices;
    private final IntArrayList indices;

    private MeshPrefab meshCache;

    public ObjFile(InputStream input) throws ObjException {
        this.positions = new ArrayList<>();
        this.normals = new ArrayList<>();
        this.uvs = new ArrayList<>();

        this.seenVertices = new ObjectIntHashMap<>();
        this.vertices = new ArrayList<>();
        this.indices = new IntArrayList();

        Reader reader = new InputStreamReader(input, StandardCharsets.UTF_8);
        Iter<String> lines = DataUtils.readerLines(reader);

        for (String line : lines) {
            line = line.trim();

            // Empty lines and comments.
            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }

            String[] parts = line.split("\\s+");

            if (parts.length == 0) {
                throw new ObjException(
                        "Invalid length of line! At least 1 is needed! No Command!"
                );
            }

            String[] arguments = new String[parts.length - 1];
            System.arraycopy(parts, 1, arguments, 0, arguments.length);

            String command = parts[0];

            switch (command) {
                case "v" -> handleVertex(arguments);
                case "vn" -> handleVertexNormal(arguments);
                case "vt" -> handleVertexTexture(arguments);
                case "f" -> handleFace(arguments);
                default -> {
                    // Ignore unsupported OBJ commands for now.
                }
            }
        }
    }

    /**
     * Handles an OBJ face.
     *
     * Supports:
     *
     *     v
     *     v/vt
     *     v//vn
     *     v/vt/vn
     *
     * Triangles are emitted directly.
     * Quads are triangulated as:
     *
     *     0 1 2
     *     0 2 3
     *
     * Polygons with more than 4 vertices are currently rejected.
     */
    private void handleFace(String[] args) throws ObjException {
        if (args.length < 3) {
            throw new ObjException(
                    "Illegal amount of face arguments: " + args.length
            );
        }

        if (args.length > 4) {
            throw new ObjException(
                    "Faces with more than 4 vertices are currently unsupported: "
                            + args.length
            );
        }

        int[] faceVertices = new int[args.length];

        for (int i = 0; i < args.length; i++) {
            faceVertices[i] = getOrCreateVertex(args[i]);
        }

        // Triangle.
        indices.add(faceVertices[0]);
        indices.add(faceVertices[1]);
        indices.add(faceVertices[2]);

        // Quad -> second triangle.
        if (args.length == 4) {
            indices.add(faceVertices[0]);
            indices.add(faceVertices[2]);
            indices.add(faceVertices[3]);
        }
    }

    /**
     * Converts an OBJ face vertex such as:
     *
     *     4
     *     4/7
     *     4//2
     *     4/7/2
     *
     * into our indexed vertex representation.
     */
    private int getOrCreateVertex(String arg) throws ObjException {
        int positionIndex;
        int uvIndex = -1;
        int normalIndex = -1;

        try {
            if (arg.contains("/")) {
                if (arg.contains("//")) {
                    // v//vn
                    String[] elements = arg.split("//");

                    if (elements.length != 2) {
                        throw new ObjException(
                                "Illegal face vertex: " + arg
                        );
                    }

                    positionIndex = parseObjIndex(
                            elements[0],
                            positions.size()
                    );

                    normalIndex = parseObjIndex(
                            elements[1],
                            normals.size()
                    );
                } else {
                    // v/vt or v/vt/vn
                    String[] elements = arg.split("/", -1);

                    if (elements.length == 2) {
                        // v/vt
                        positionIndex = parseObjIndex(
                                elements[0],
                                positions.size()
                        );

                        uvIndex = parseObjIndex(
                                elements[1],
                                uvs.size()
                        );
                    } else if (elements.length == 3) {
                        // v/vt/vn
                        positionIndex = parseObjIndex(
                                elements[0],
                                positions.size()
                        );

                        if (!elements[1].isEmpty()) {
                            uvIndex = parseObjIndex(
                                    elements[1],
                                    uvs.size()
                            );
                        }

                        if (!elements[2].isEmpty()) {
                            normalIndex = parseObjIndex(
                                    elements[2],
                                    normals.size()
                            );
                        }
                    } else {
                        throw new ObjException(
                                "Illegal amount of face segment elements: "
                                        + elements.length
                        );
                    }
                }
            } else {
                // Just v.
                positionIndex = parseObjIndex(
                        arg,
                        positions.size()
                );
            }
        } catch (NumberFormatException e) {
            throw new ObjException(e);
        }

        VertexKey key = new VertexKey(
                positionIndex,
                normalIndex,
                uvIndex
        );

        int existing = seenVertices.getOrDefault(key, -1);

        if (existing != -1) {
            return existing;
        }

        int index = vertices.size();

        float[] position = positions.get(positionIndex);

        float[] normal = normalIndex >= 0
                ? normals.get(normalIndex)
                : noNorm();

        float[] uv = uvIndex >= 0
                ? uvs.get(uvIndex)
                : noUv();

        /*
         * Tangent is filled in later by generateTangents().
         */
        float[] tangent = noTangent();

        vertices.add(
                new MeshPrefab.PrefabVertex(
                        position,
                        normal,
                        uv,
                        tangent
                )
        );

        seenVertices.put(key, index);

        return index;
    }

    /**
     * OBJ indices are 1-based.
     *
     * Negative OBJ indices are relative to the end of the corresponding
     * attribute array:
     *
     *     -1 = last element
     *     -2 = second-last element
     *
     * The returned index is always zero-based.
     */
    private static int parseObjIndex(
            String value,
            int size
    ) {
        int index = Integer.parseInt(value);

        if (index > 0) {
            index--;
        } else if (index < 0) {
            index = size + index;
        } else {
            throw new IllegalArgumentException(
                    "OBJ indices cannot be zero."
            );
        }

        if (index < 0 || index >= size) {
            throw new IllegalArgumentException(
                    "OBJ index out of bounds: " + value
                            + " for size " + size
            );
        }

        return index;
    }

    private static float[] noNorm() {
        return new float[] {0, 0, 0};
    }

    private static float[] noUv() {
        return new float[] {0, 0};
    }

    private static float[] noTangent() {
        return new float[] {1, 0, 0, 1};
    }

    private void handleVertexTexture(String[] args) throws ObjException {
        try {
            Float[] floats = Iter.of(args)
                    .faultyMap(Float::parseFloat)
                    .toArray();

            if (floats.length < 2 || floats.length > 3) {
                throw new ObjException(
                        "Illegal amount of texture arguments: "
                                + floats.length
                );
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
            Float[] floats = Iter.of(args)
                    .faultyMap(Float::parseFloat)
                    .toArray();

            if (floats.length != 3) {
                throw new ObjException(
                        "Illegal amount of normal arguments: "
                                + floats.length
                );
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
            Float[] floats = Iter.of(args)
                    .faultyMap(Float::parseFloat)
                    .toArray();

            if (floats.length != 3 && floats.length != 4) {
                throw new ObjException(
                        "Illegal amount of vertex arguments: "
                                + floats.length
                );
            }

            float[] xyz = new float[3];

            for (int i = 0; i < 3; i++) {
                xyz[i] = floats[i];
            }

            positions.add(xyz);
        } catch (RuntimeException e) {
            throw new ObjException(e);
        }
    }

    /**
     * Generates a tangent for every vertex.
     *
     * The tangent is calculated from the relationship between:
     *
     *     position space
     *     UV space
     *
     * For each triangle we calculate a tangent and bitangent, accumulate
     * them into the triangle's vertices, then construct the final tangent
     * basis for each vertex.
     *
     * tangent.xyz = tangent direction
     * tangent.w   = handedness (+1 or -1)
     */
    private void generateTangents(int[] indices) {
        float[] tangentAccum = new float[vertices.size() * 3];
        float[] bitangentAccum = new float[vertices.size() * 3];

        for (int i = 0; i < indices.length; i += 3) {
            int i0 = indices[i];
            int i1 = indices[i + 1];
            int i2 = indices[i + 2];

            MeshPrefab.PrefabVertex v0 = vertices.get(i0);
            MeshPrefab.PrefabVertex v1 = vertices.get(i1);
            MeshPrefab.PrefabVertex v2 = vertices.get(i2);

            float[] p0 = v0.position();
            float[] p1 = v1.position();
            float[] p2 = v2.position();

            float[] uv0 = v0.uv();
            float[] uv1 = v1.uv();
            float[] uv2 = v2.uv();

            float edge1x = p1[0] - p0[0];
            float edge1y = p1[1] - p0[1];
            float edge1z = p1[2] - p0[2];

            float edge2x = p2[0] - p0[0];
            float edge2y = p2[1] - p0[1];
            float edge2z = p2[2] - p0[2];

            float du1 = uv1[0] - uv0[0];
            float dv1 = uv1[1] - uv0[1];

            float du2 = uv2[0] - uv0[0];
            float dv2 = uv2[1] - uv0[1];

            float determinant = du1 * dv2 - du2 * dv1;

            /*
             * Degenerate UV mapping.
             *
             * There is no meaningful tangent if the triangle has zero
             * area in UV space.
             */
            if (Math.abs(determinant) < 1e-8f) {
                continue;
            }

            float f = 1.0f / determinant;

            float tx = f * (
                    dv2 * edge1x -
                            dv1 * edge2x
            );

            float ty = f * (
                    dv2 * edge1y -
                            dv1 * edge2y
            );

            float tz = f * (
                    dv2 * edge1z -
                            dv1 * edge2z
            );

            float bx = f * (
                    -du2 * edge1x +
                            du1 * edge2x
            );

            float by = f * (
                    -du2 * edge1y +
                            du1 * edge2y
            );

            float bz = f * (
                    -du2 * edge1z +
                            du1 * edge2z
            );

            add(
                    tangentAccum,
                    i0 * 3,
                    tx, ty, tz
            );

            add(
                    tangentAccum,
                    i1 * 3,
                    tx, ty, tz
            );

            add(
                    tangentAccum,
                    i2 * 3,
                    tx, ty, tz
            );

            add(
                    bitangentAccum,
                    i0 * 3,
                    bx, by, bz
            );

            add(
                    bitangentAccum,
                    i1 * 3,
                    bx, by, bz
            );

            add(
                    bitangentAccum,
                    i2 * 3,
                    bx, by, bz
            );
        }

        finalizeTangents(tangentAccum, bitangentAccum);
    }

    private void finalizeTangents(
            float[] tangentAccum,
            float[] bitangentAccum
    ) {
        for (int i = 0; i < vertices.size(); i++) {
            MeshPrefab.PrefabVertex vertex = vertices.get(i);

            float[] n = vertex.normal();

            /*
             * If the OBJ didn't provide normals, there is no reliable
             * tangent basis to construct here.
             *
             * Leave the default tangent in that case.
             */
            float normalLengthSquared =
                    n[0] * n[0] +
                            n[1] * n[1] +
                            n[2] * n[2];

            float tangentLengthSquared =
                    tangentAccum[i * 3] * tangentAccum[i * 3] +
                            tangentAccum[i * 3 + 1] * tangentAccum[i * 3 + 1] +
                            tangentAccum[i * 3 + 2] * tangentAccum[i * 3 + 2];

            if (
                    normalLengthSquared < 1e-8f ||
                            tangentLengthSquared < 1e-8f
            ) {
                continue;
            }

            /*
             * Normalize the normal.
             *
             * OBJ normals should normally already be normalized, but
             * doing this makes the tangent calculation more robust.
             */
            float normalLength =
                    (float) Math.sqrt(normalLengthSquared);

            float nx = n[0] / normalLength;
            float ny = n[1] / normalLength;
            float nz = n[2] / normalLength;

            /*
             * Gram-Schmidt orthogonalization:
             *
             * T = T - N * dot(N, T)
             *
             * This guarantees that tangent and normal are perpendicular.
             */
            float dot =
                    nx * tangentAccum[i * 3] +
                            ny * tangentAccum[i * 3 + 1] +
                            nz * tangentAccum[i * 3 + 2];

            float tx = tangentAccum[i * 3] - nx * dot;
            float ty = tangentAccum[i * 3 + 1] - ny * dot;
            float tz = tangentAccum[i * 3 + 2] - nz * dot;

            float tangentLength =
                    (float) Math.sqrt(
                            tx * tx +
                                    ty * ty +
                                    tz * tz
                    );

            if (tangentLength < 1e-8f) {
                continue;
            }

            tx /= tangentLength;
            ty /= tangentLength;
            tz /= tangentLength;

            /*
             * B = cross(N, T)
             */
            float crossX = ny * tz - nz * ty;
            float crossY = nz * tx - nx * tz;
            float crossZ = nx * ty - ny * tx;

            /*
             * Determine whether the accumulated bitangent agrees
             * with cross(N, T).
             *
             * +1 = right-handed
             * -1 = left-handed
             */
            float handedness =
                    crossX * bitangentAccum[i * 3] +
                            crossY * bitangentAccum[i * 3 + 1] +
                            crossZ * bitangentAccum[i * 3 + 2];

            float sign = handedness < 0.0f
                    ? -1.0f
                    : 1.0f;

            float[] tangent = {
                    tx,
                    ty,
                    tz,
                    sign
            };

            /*
             * Replace the temporary tangent with the final tangent.
             */
            vertices.set(
                    i,
                    new MeshPrefab.PrefabVertex(
                            vertex.position(),
                            vertex.normal(),
                            vertex.uv(),
                            tangent
                    )
            );
        }
    }

    private static void add(
            float[] value,
            int i,
            float x,
            float y,
            float z
    ) {
        value[i] += x;
        value[i + 1] += y;
        value[i + 2] += z;
    }

    public MeshPrefab toMeshPrefab() {
        if (meshCache != null) {
            return meshCache;
        }

        int[] ind = indices.toArray();

        generateTangents(ind);

        meshCache = new MeshPrefab(
                vertices.toArray(
                        MeshPrefab.PrefabVertex[]::new
                ),
                ind
        );

        return meshCache;
    }

    /**
     * Represents one unique combination of OBJ attributes.
     *
     * OBJ has separate indices for position, UV and normal, so these
     * three indices together identify one GPU vertex.
     */
    private record VertexKey(
            int position,
            int normal,
            int uv
    ) {}
}