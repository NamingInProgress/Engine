package com.vke.api.pipeline;

import com.vke.core.services.shr.ReflectedShader;

public class Entry {

    public static final String[] AUTO_NAMES = new String[] {
            "projectionMatrix"
    };

    int parentBaseTypeId;
    int idx;

    public long size;
    public int offset;

    public String name;
    public int id;
    public long typeHandle;
    public BaseType baseType;
    public int rawBaseType;
    public int baseTypeId;

    public boolean auto;

    public ArrayData arrayData;
    public MatrixData matrixData;
    public Struct structData;

    public static class ArrayData {
        public int nArrayDim;
        public int[] arrayDim;
        public int arrayStride;
    }

    public static class MatrixData {
        public int matrixRows;
        public int matrixColumns;
        public int matrixStride;
    }

    public Entry(String name, long size, int offset) {
        this.name = name;
        this.size = size;
        this.offset = offset;
        this.auto = isNameAutoable(name);
    }

    public void digestDiscoverableMember(ReflectedShader.DiscoverableMember memberToDigest) {
        parentBaseTypeId = memberToDigest.parentBaseTypeId;
        idx = memberToDigest.idx;

        id = memberToDigest.id;
        typeHandle = memberToDigest.typeHandle;
        rawBaseType = memberToDigest.baseType;
        baseType = BaseType.fromSpvc(rawBaseType);
        baseTypeId = memberToDigest.baseTypeId;

        // honestly idk if chatgpt is hallucinating or not so this is a fallback incase all goes to shit (length 1 array)
        if (memberToDigest.nArrayDim > 1 || (memberToDigest.nArrayDim == 1 && memberToDigest.arrayDim[0] > 1)) {
            arrayData = new ArrayData();
            arrayData.nArrayDim = memberToDigest.nArrayDim;
            arrayData.arrayDim = memberToDigest.arrayDim;
            arrayData.arrayStride = memberToDigest.arrayStride;
        }

        if (memberToDigest.matrixRows > 1 || memberToDigest.matrixColumns > 1) {
            matrixData = new MatrixData();
            matrixData.matrixRows = memberToDigest.matrixRows;
            matrixData.matrixColumns = memberToDigest.matrixColumns;
            matrixData.matrixStride = memberToDigest.matrixStride;
        }

        structData = memberToDigest.struct;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Entry e = (Entry) o;
        return name.equals(e.name) && size == e.size && offset == e.offset && auto == e.auto;
    }

    public static boolean isNameAutoable(String name) {
        for (String autoName : AUTO_NAMES) {
            if (autoName.equals(name)) return true;
        }
        return false;
    }

}
