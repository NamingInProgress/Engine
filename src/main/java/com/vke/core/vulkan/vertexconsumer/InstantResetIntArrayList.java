package com.vke.core.vulkan.vertexconsumer;

import com.carrotsearch.hppc.IntArrayList;

public class InstantResetIntArrayList extends IntArrayList {
    @Override
    public void clear() {
        //DONT fill the array with 0s here!
        elementsCount = 0;
    }
}
