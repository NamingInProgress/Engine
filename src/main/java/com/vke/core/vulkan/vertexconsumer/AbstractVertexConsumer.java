package com.vke.core.vulkan.vertexconsumer;

import com.vke.api.draw.VertexConsumer;
import com.vke.core.mesh.Mesh;
import com.vke.api.draw.Vertex;
import com.vke.api.rendering.abstraction.enums.buffer.BufferUsage;
import com.vke.api.rendering.vulkan.buffer.CpuBuffer;
import com.vke.core.VKEngine;
import com.vke.core.rendering.draw.DrawContext;
import com.vke.core.vulkan.VulkanRenderer;
import com.vke.core.vulkan.buffers.MappedGpuRingBuffer;
import com.vke.core.vulkan.buffers.premade.ibo.DynamicIndexBuffer;
import com.vke.core.vulkan.buffers.premade.vbo.DynamicVertexBuffer;
import com.vke.core.vulkan.command.VulkanCmdBuffers;
import org.lwjgl.util.vma.Vma;
import org.lwjgl.vulkan.VK14;

import java.util.ArrayList;
import java.util.Arrays;

public abstract class AbstractVertexConsumer<T extends Vertex> implements VertexConsumer<T> {

    private static final int BASE_VERTEX_COUNT = 1000;
    private static final int BASE_INDEX_COUNT = 1000;

    private final DynamicVertexBuffer<T> _cpuVertices;
    private final DynamicIndexBuffer _cpuIndices;

    private final VKEngine _engine;
    private final VulkanRenderer _renderer;

    private final T _template;

    private int maxVertexCount;
    private int maxIndexCount;

    private int currentVertexCount;
    private int currentIndexCount;

    private int frozenIndexCount;

    private MappedGpuRingBuffer _gpuVertices;
    private MappedGpuRingBuffer _gpuIndices;

    private int lastVertexCount;
    private int lastIndexCount;

    private ArrayList<MappedGpuRingBuffer> _gpuBuffersOld = new ArrayList<>();

    public AbstractVertexConsumer(VKEngine engine, VulkanRenderer renderer, T template) {
        this(engine, renderer, template, BASE_VERTEX_COUNT, BASE_INDEX_COUNT);
    }

    public AbstractVertexConsumer(VKEngine engine, VulkanRenderer renderer, T template, int estVertexCount, int estIndexCount) {
        this.maxVertexCount = estVertexCount;
        this.maxIndexCount = estIndexCount;
        this._engine = engine;
        this._renderer = renderer;
        this._template = template;

        this._cpuVertices = new DynamicVertexBuffer<>(template, estVertexCount);
        this._cpuIndices = new DynamicIndexBuffer(estIndexCount);

        this._gpuVertices = genVertexBuffer(estVertexCount);
        this._gpuIndices = genIndexBuffer(estIndexCount);
    }

    @Override
    public void free() {
        this._gpuVertices.free();
        this._gpuIndices.free();
        this._cpuVertices.free();
        this._cpuIndices.free();
    }

    @Override
    public void beginFrame() {
        this._gpuVertices.rotate();
        this._gpuIndices.rotate();
        this.lastIndexCount = 0;
        this.lastVertexCount = 0;
    }

    @Override
    public void begin() {
        this.frozenIndexCount = currentIndexCount;
    }

    protected void putVertices(T... verts) {
        ensureVertexSpace(verts.length);
        this._cpuVertices.putVertices(verts);
        this.currentVertexCount += verts.length;
    }

    protected void putIndices(int... indices) {
        ensureIndexSpace(indices.length);
        this._cpuIndices.put(Arrays.stream(indices).map(i -> frozenIndexCount + i).toArray());
        this.currentIndexCount += indices.length;
    }

    public void putMesh(Mesh<T> mesh) {
        ensureVertexSpace(mesh.getVertices().length);
        ensureIndexSpace(mesh.getIndices().length);

        this._cpuVertices.putVertices(mesh.getVertices());
        this._cpuIndices.put(mesh.getIndices());
        this.currentVertexCount += mesh.getVertices().length;
        this.currentIndexCount += mesh.getIndices().length;
    }

    @Override
    public void draw(DrawContext ctx) {
        VulkanCmdBuffers buf = (VulkanCmdBuffers) ctx.getCommandBuffer();
        this.upload();
        this.bindIBO(ctx);
        this.bindVBO(ctx);

        VK14.vkCmdDrawIndexed(buf.getBuffer(), this.getWrittenIndices(), 1, 0, 0, 0);

        this.lastVertexCount += this.currentVertexCount;
        this.lastIndexCount += this.currentIndexCount;

        this.currentVertexCount = 0;
        this.currentIndexCount = 0;

        handleOldBuffers();
    }

    @Override
    public void upload() {
        this._cpuIndices.reset();
        this._cpuVertices.reset();

        this._gpuIndices.write(this._cpuIndices.getAddress(), this.lastIndexCount * 4L, (long) this.getWrittenIndices() * 4);
        this._gpuVertices.write(this._cpuVertices.getAddress(),
                (long) this.lastVertexCount * _template.getByteStride(), (long) this.getWrittenVertices() * _template.getByteStride());
    }

    public long getRingIndicesOffset() { return this._gpuIndices.getLastOffset(); }
    public long getRingVerticesOffset() { return this._gpuVertices.getLastOffset(); }

    public int getWrittenIndices(){ return this.currentIndexCount; }
    public int getWrittenVertices(){ return this.currentVertexCount; }

    public void bindIBO(DrawContext ctx) {
        VulkanCmdBuffers cmd =  (VulkanCmdBuffers) ctx.getCommandBuffer();

        VK14.vkCmdBindIndexBuffer(cmd.getBuffer(), this._gpuIndices.getGpuBuffer().getBuffer(),
                this.getRingIndicesOffset() + lastIndexCount * 4L, VK14.VK_INDEX_TYPE_UINT32);
    }

    public void bindVBO(DrawContext ctx) {
        VulkanCmdBuffers cmd =  (VulkanCmdBuffers) ctx.getCommandBuffer();

        VK14.vkCmdBindVertexBuffers(cmd.getBuffer(), 0, new long[]{ this._gpuVertices.getGpuBuffer().getBuffer() },
                new long[]{ getRingVerticesOffset() + (long) lastVertexCount * _template.getByteStride()});
    }

    private void handleOldBuffers() {
        ArrayList<MappedGpuRingBuffer> toRemove = new ArrayList<>();
        for (MappedGpuRingBuffer buf : this._gpuBuffersOld) {
            buf.free();
            toRemove.add(buf);
        }

        toRemove.forEach(_gpuBuffersOld::remove);
    }

    protected void ensureVertexSpace(int additionalSpace) {
        int newCount = this.lastVertexCount + this.currentVertexCount + additionalSpace;
        if (newCount >= this.maxVertexCount) {
            while (newCount > this.maxVertexCount) {
                this.maxVertexCount = (int) (((double) this.maxVertexCount) * CpuBuffer.GROWTH_FAC);
            }
            reallocVertexBuffer(this.maxVertexCount);
        }
    }

    protected void ensureIndexSpace(int additional) {
        int newCount = this.lastIndexCount + this.currentIndexCount + additional;
        if (newCount >= this.maxIndexCount) {
            while (newCount > this.maxIndexCount) {
                this.maxIndexCount = (int) (((double) this.maxIndexCount) * CpuBuffer.GROWTH_FAC);
            }
            reallocIndexBuffer(this.maxIndexCount);
        }
    }

    protected void reallocVertexBuffer(int newSize) {
        this._gpuBuffersOld.add(this._gpuVertices);
        this._gpuVertices = genVertexBuffer(newSize);
    }

    protected void reallocIndexBuffer(int newSize) {
        this._gpuBuffersOld.add(this._gpuIndices);
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
        return new MappedGpuRingBuffer(_engine, _renderer.getDevice(), indexCount * 4L, _renderer.getFramesInFlight(), iboUsage, Vma.VMA_ALLOCATION_CREATE_HOST_ACCESS_SEQUENTIAL_WRITE_BIT);
    }

}
