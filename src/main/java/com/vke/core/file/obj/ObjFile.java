package com.vke.core.file.obj;

import com.carrotsearch.hppc.IntArrayList;

import java.io.InputStream;
import java.util.ArrayList;

public class ObjFile {
    private final ArrayList<float[]> positions;
    private final ArrayList<float[]> normals;
    private final ArrayList<float[]> texCoords;
    private final IntArrayList indices;

    public ObjFile(InputStream input) {
        this.positions = new ArrayList<>();
        this.normals = new ArrayList<>();
        this.texCoords = new ArrayList<>();
        this.indices = new IntArrayList();
    }
}
