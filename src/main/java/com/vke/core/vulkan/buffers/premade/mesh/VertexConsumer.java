package com.vke.core.vulkan.buffers.premade.mesh;

import com.vke.api.draw.IVertexConsumer;
import com.vke.api.draw.Mesh;
import com.vke.api.draw.Vertex;
import com.vke.api.rendering.abstraction.commands.CommandBuffer;
import com.vke.api.rendering.abstraction.enums.buffer.BufferUsage;
import com.vke.api.rendering.vulkan.buffer.CpuBuffer;
import com.vke.core.VKEngine;
import com.vke.core.vulkan.VulkanRenderer;
import com.vke.core.vulkan.buffers.MappedGpuRingBuffer;
import com.vke.core.vulkan.buffers.premade.IndexBuffer;
import com.vke.core.vulkan.buffers.premade.vbo.DynamicVertexBuffer;
import com.vke.core.vulkan.command.VulkanCmdBuffers;
import com.vke.utils.io.Disposable;
import com.vke.utils.tuple.Pair;
import org.lwjgl.util.vma.Vma;
import org.lwjgl.vulkan.VK14;

import java.util.ArrayList;

public abstract class VertexConsumer<T extends Vertex> implements IVertexConsumer<T>, Disposable {

    private static final int BASE_VERTEX_COUNT = 1000;
    private static final int BASE_INDEX_COUNT = 1000;

    private final DynamicVertexBuffer<T> _cpuVertices;
    private final IndexBuffer _cpuIndices;

    private final VKEngine _engine;
    private final VulkanRenderer _renderer;

    private final T _template;

    private int maxVertexCount;
    private int maxIndexCount;

    private int currentVertexCount;
    private int currentIndexCount;

    private int frozenIndexCount;

    private int currentFrame;

    private MappedGpuRingBuffer _gpuVertices;
    private MappedGpuRingBuffer _gpuIndices;

    private ArrayList<Pair<Integer, MappedGpuRingBuffer>> _gpuBuffersOld = new ArrayList<>();

    public VertexConsumer(VKEngine engine, VulkanRenderer renderer, T template) {
        this(engine, renderer, template, BASE_VERTEX_COUNT, BASE_INDEX_COUNT);
    }

    public VertexConsumer(VKEngine engine, VulkanRenderer renderer, T template, int estVertexCount, int estIndexCount) {
        this.maxVertexCount = estVertexCount;
        this.maxIndexCount = estIndexCount;
        this._engine = engine;
        this._renderer = renderer;
        this._template = template;

        this._cpuVertices = new DynamicVertexBuffer<>(template, estVertexCount);
        this._cpuIndices = new IndexBuffer(estIndexCount);

        this._gpuVertices = genVertexBuffer(estVertexCount);
        this._gpuIndices = genIndexBuffer(estIndexCount);
    }

    public long getIndicesOffset() { return this._gpuIndices.getOffset(); }
    public long getVerticesOffset() { return this._gpuVertices.getOffset(); }

    public int getWrittenIndices(){ return this.currentIndexCount; }
    public int getWrittenVertices(){ return this.currentVertexCount; }

    public void bindIBO(CommandBuffer buffer) {
        VulkanCmdBuffers cmd =  (VulkanCmdBuffers) buffer;

        VK14.vkCmdBindIndexBuffer(cmd.getBuffer(), this._gpuIndices.getGpuBuffer().getBuffer(), this.getIndicesOffset(), VK14.VK_INDEX_TYPE_UINT16);
    }

    public void bindVBO(CommandBuffer buffer) {
        VulkanCmdBuffers cmd =  (VulkanCmdBuffers) buffer;

        VK14.vkCmdBindVertexBuffers(cmd.getBuffer(), 0, new long[]{ this._gpuVertices.getGpuBuffer().getBuffer() }, new long[]{ getVerticesOffset() });
    }

    @Override
    public void free() {
        this._gpuVertices.free();
        this._gpuIndices.free();
        this._cpuVertices.free();
        this._cpuIndices.free();
    }

    @Override
    public void begin() {
        this.frozenIndexCount = currentIndexCount;
    }

    @Override
    public void vertex(T vertex) {
        ensureVertexSpace(1);
        this._cpuVertices.putVertex(vertex);
        this.currentVertexCount++;
    }

    @Override
    public void index(int index) {
        ensureIndexSpace(1);
        this._cpuIndices.put(this.frozenIndexCount + index);
        this.currentIndexCount++;
    }

    @Override
    public void upload() {
        this._gpuIndices.write(this._cpuIndices.getAddress(), 0, this._cpuIndices.getSizeBytes());
        this._gpuVertices.write(this._cpuVertices.getAddress(), 0, this._cpuVertices.getSizeBytes());
        this.currentFrame++;

        this._gpuBuffersOld.forEach((v) -> v.v1++);

        ArrayList<Integer> toRemove = new ArrayList<>();
        ArrayList<Pair<Integer, MappedGpuRingBuffer>> gpuBuffersOld = this._gpuBuffersOld;
        for (int i = 0; i < gpuBuffersOld.size(); i++) {
            Pair<Integer, MappedGpuRingBuffer> pair = gpuBuffersOld.get(i);
            if (pair.v1 > _renderer.getFramesInFlight()) {
                pair.v2.free();
                toRemove.add(i);
            }
        }

        toRemove.forEach(this._gpuBuffersOld::remove);
    }

    @Override
    public void mesh(Mesh<T> mesh) {
        ensureVertexSpace(mesh.getVertices().length);
        ensureIndexSpace(mesh.getIndices().length);

        this._cpuVertices.putVertices(mesh.getVertices());
        this._cpuIndices.put(mesh.getIndices());
        this.currentVertexCount += mesh.getVertices().length;
        this.currentIndexCount += mesh.getIndices().length;
    }

    protected void ensureVertexSpace(int additionalSpace) {
        int newCount = this.currentIndexCount + additionalSpace;
        if (newCount >= this.maxVertexCount) {
            while (newCount > this.maxVertexCount) {
                this.maxVertexCount = (int) (((double) this.maxVertexCount) * CpuBuffer.GROWTH_FAC);
            }
            reallocVertexBuffer(this.maxVertexCount);
        }
    }

    protected void ensureIndexSpace(int additional) {
        int newCount = this.currentIndexCount + additional;
        if (newCount >= this.maxIndexCount) {
            while (newCount > this.maxIndexCount) {
                this.maxIndexCount = (int) (((double) this.maxIndexCount) * CpuBuffer.GROWTH_FAC);
            }
            reallocIndexBuffer(this.maxIndexCount);
        }
    }

    protected void reallocVertexBuffer(int newSize) {
        this._gpuBuffersOld.add(new Pair<>(0, this._gpuVertices));
        this._gpuVertices = genVertexBuffer(newSize);
    }

    protected void reallocIndexBuffer(int newSize) {
        this._gpuBuffersOld.add(new Pair<>(0, this._gpuIndices));
        this._gpuIndices= genIndexBuffer(newSize);
    }

    protected MappedGpuRingBuffer genVertexBuffer(int vertexCount) {
        BufferUsage vboUsage = new BufferUsage(
                BufferUsage.Bits.VBO
        );
       return new MappedGpuRingBuffer(_engine, _renderer.getDevice(), (long) vertexCount * _template.getByteStride(), _renderer.getFramesInFlight(), vboUsage, Vma.VMA_ALLOCATION_CREATE_HOST_ACCESS_SEQUENTIAL_WRITE_BIT);
    }

    protected MappedGpuRingBuffer genIndexBuffer(int indexCount) {
        BufferUsage iboUsage = new BufferUsage(
                BufferUsage.Bits.IBO
        );
        return new MappedGpuRingBuffer(_engine, _renderer.getDevice(), indexCount, _renderer.getFramesInFlight(), iboUsage, Vma.VMA_ALLOCATION_CREATE_HOST_ACCESS_SEQUENTIAL_WRITE_BIT);
    }

}
