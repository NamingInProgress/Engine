package com.vke.api.draw;

public class Meshes {
    public static final MeshPrefab CUBE = new MeshPrefab(
            new float[][] { // positions
                    {-0.5f, -0.5f, -0.5f},
                    { 0.5f, -0.5f, -0.5f},
                    { 0.5f,  0.5f, -0.5f},
                    {-0.5f,  0.5f, -0.5f},
                    {-0.5f, -0.5f,  0.5f},
                    { 0.5f, -0.5f,  0.5f},
                    { 0.5f,  0.5f,  0.5f},
                    {-0.5f,  0.5f,  0.5f}
            },
            new float[][] {
                    {0f, 0f, -1f},
                    {0f, 0f, -1f},
                    {0f, 0f, -1f},
                    {0f, 0f, -1f},
                    {0f, 0f, 1f},
                    {0f, 0f, 1f},
                    {0f, 0f, 1f},
                    {0f, 0f, 1f}
            },
            new int[] {
                    0, 0,
                    1, 1,
                    2, 2,
                    3, 3,
                    4, 4,
                    5, 5
            },
            new int[] { // indices (12 triangles)
                    0, 1, 2,  2, 3, 0, // back
                    4, 5, 6,  6, 7, 4, // front
                    0, 3, 7,  7, 4, 0, // left
                    1, 5, 6,  6, 2, 1, // right
                    0, 1, 5,  5, 4, 0, // bottom
                    3, 2, 6,  6, 7, 3  // top
            }
    );
}
