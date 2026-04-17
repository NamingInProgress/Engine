package com.vke.core.spline.triangulate;

import com.carrotsearch.hppc.IntArrayList;

import java.util.function.BiConsumer;

public class TriangulatedPolyLine {
    private IntArrayList verticesX;
    private IntArrayList verticesY;
    private IntArrayList indices;
    private int vertcount;
    private int frozenIndex;

    public TriangulatedPolyLine(int linePointCount) {
        this.verticesX = new IntArrayList(linePointCount);
        this.verticesY = new IntArrayList(linePointCount);
        this.indices = new IntArrayList(linePointCount);
        this.vertcount = 0;
    }

    public int[] getIndices() {
        return indices.toArray();
    }

    public void forEachVertex(BiConsumer<Integer, Integer> xyConsumer) {
        for (int i = 0; i < verticesX.size(); i++) {
            xyConsumer.accept(verticesX.get(i), verticesY.get(i));
        }
    }

    public void addSingleTriangle(int x1, int y1, int x2, int y2, int x3, int y3) {
         verticesX.add(x1, x2, x3);
         verticesY.add(y1, y2, y3);
         indices.add(vertcount, vertcount + 1, vertcount + 2);
         vertcount += 3;
    }

    public void begin() {
        this.frozenIndex = vertcount;
    }

    public void batchTriangle(int x1, int y1, int x2, int y2, int x3, int y3) {
        verticesX.add(x1, x2, x3);
        verticesY.add(y1, y2, y3);
        vertcount += 3;
    }

    public void batchIndices(int... indices) {
        for (int i = 0; i < indices.length; i++) {
            indices[i] += frozenIndex;
        }
        this.indices.add(indices);
    }

    public void batchVertex(int vx, int vy) {
        this.verticesX.add(vx);
        this.verticesY.add(vy);
        vertcount++;
    }

    public int vertexCount() {
        return verticesX.size();
    }
}
