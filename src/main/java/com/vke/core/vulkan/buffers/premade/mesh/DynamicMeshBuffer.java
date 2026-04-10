package com.vke.core.vulkan.buffers.premade.mesh;

import com.vke.api.draw.Vertex;
import com.vke.api.rendering.abstraction.enums.buffer.BufferUsage;
import com.vke.core.VKEngine;
import com.vke.core.vulkan.VulkanRenderer;
import com.vke.core.vulkan.buffers.MappedGpuRingBuffer;
import com.vke.core.vulkan.buffers.premade.IndexBuffer;
import com.vke.core.vulkan.buffers.premade.vbo.DynamicVertexBuffer;
import com.vke.utils.io.Disposable;
import org.lwjgl.util.vma.Vma;

public class DynamicMeshBuffer implements Disposable {

    private static final int BASE_VERTEX_COUNT = 1000;
    private static final int BASE_INDEX_COUNT = 1000;

    private int vertexCount;
    private int indexCount;

    private DynamicVertexBuffer<?> verticesTemp;
    private IndexBuffer indexTemp;

    private MappedGpuRingBuffer verticesMapped;
    private MappedGpuRingBuffer indicesMapped;

    private int currentVertexCount;
    private int currentIndexCount;

    private DynamicMeshBuffer() {}

    public static <T extends Vertex> DynamicMeshBuffer createBuffer(VKEngine engine, VulkanRenderer renderer, T template, int estVertexCount, int estIndexCount) {
        DynamicMeshBuffer self = new DynamicMeshBuffer();

        self.vertexCount = estVertexCount;
        self.indexCount = estIndexCount;

        self.verticesTemp = new DynamicVertexBuffer<>(template, estVertexCount);
        self.indexTemp = new IndexBuffer(estIndexCount);

        BufferUsage vboUsage = new BufferUsage(
                BufferUsage.Bits.VBO
        );
        self.verticesMapped = new MappedGpuRingBuffer(engine, renderer.getDevice(), (long) estVertexCount * template.getByteStride(), renderer.getFramesInFlight(), vboUsage);

        BufferUsage iboUsage = new BufferUsage(
                BufferUsage.Bits.IBO
        );
        self.indicesMapped = new MappedGpuRingBuffer(engine, renderer.getDevice(), (long) estVertexCount * template.getByteStride(), renderer.getFramesInFlight(), iboUsage, Vma.VMA_ALLOCATION_CREATE_HOST_ACCESS_SEQUENTIAL_WRITE_BIT);



        return self;
    }

    @Override
    public void free() {
        this.verticesMapped.free();
        this.indicesMapped.free();
        this.verticesTemp.free();
        this.indexTemp.free();
    }

}
