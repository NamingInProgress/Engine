# VKE Descriptor Set API — Q&A and Technical Addendum

This document answers the questions in `DS-API-questions.md`, adds technical implementation depth to the proposals in `DS-API.md`, corrects findings from a code audit of the current refactor branch, and lists open questions that require your input before implementation can be specified fully.

---

## Audit findings — current refactor branch

Before answering the questions, several important facts discovered in the code affect the answers directly.

**The new `DescriptorSets` class already does per-frame descriptor set copies correctly.**

`DescriptorSets.java` allocates `framesInFlight` `DescriptorSet` objects per set (the `List<DescriptorSet[]> sets` field) and `getDescriptorSetHandles()` picks the right frame at bind time using `renderer.getCurrentFrameIndex()`. The image update path in `update()` also correctly picks the current frame's set. This is already correct for image/sampler bindings.

**Buffer bindings are still frame-unaware at the handle level.**

`resolveDeep()` and `resolveShallowBuffer()` always capture `cpuAddress` from `sets.get(i)[0]` — the frame-0 `DescriptorSet`. Buffer writes from `EntryHandle.write()` and `BufferHandle.write()` always target frame 0's mapped memory, regardless of which frame is currently rendering. This is the central bug.

**`vkCmdBindDescriptorSets` is called with `null` for the dynamic offsets parameter.**

If any binding uses `UNIFORM_BUFFER_DYNAMIC` or `STORAGE_BUFFER_DYNAMIC` (which the `EngineDescriptorSetsManager` creates for camera data), the Vulkan spec requires that the count and values of dynamic offsets match the number of dynamic bindings. Passing `null` is undefined behaviour. Currently nothing uses dynamic bindings in a live path (the engine sets are commented out), so this hasn't crashed, but it must be fixed before any dynamic binding is active.

**`DescriptorSets` constructor has an index modulo bug.**

```
new DescriptorSet(allocator.allocate(compiledLayouts.get(i % renderer.getFramesInFlight())), ...)
```
When there are more set layouts than `framesInFlight`, this wraps the layout index. It should be `compiledLayouts.get(i)`.

---

## Question 1 — How should descriptor binding look?

### The requirement

The user must be able to bind descriptor sets without seeing raw Vulkan handles or dynamic offsets. The binding must automatically apply the correct frame offset and handle dynamic-type bindings.

### Proposed binding flow

**Step 1 — pipeline bind.** This is unchanged from today. Internally, it also binds the engine-managed sets 0 and 1, passing the correct dynamic offset for set 0's frame-data ring buffer:

```
cmd.bindPipeline(pipelineHandle);
```

Internally at bind time:
- `vkCmdBindPipeline` runs.
- `vkCmdBindDescriptorSets` is called immediately after, binding sets 0 and 1 with the engine's `DescriptorSet[]` handles and computing the frame dynamic offset: `currentFrame * frameDataRegionSize`.

**Step 2 — user set bind.** The user binds their descriptor sets by passing their handles. Since the handles originated from the pipeline's `DescriptorSets`, the `DescriptorSets` object has enough information to compute all dynamic offsets internally:

```
cmd.bindDescriptorSets(pipelineHandle, handle1, handle2, ...);
```

Internally:
- The command buffer calls `descriptorSets.collectFrameHandles(currentFrame)` → `long[]` of VkDescriptorSet handles for the current frame (one per user set, ordered by set number).
- It calls `descriptorSets.collectDynamicOffsets(currentFrame)` → `int[]` of dynamic offsets, ordered by set number then binding number within the set (Vulkan spec requirement). For each dynamic buffer binding: `currentFrame * alignedFrameRegionSize` (plus slot offset for multi-write bindings, see Q2).
- `vkCmdBindDescriptorSets(buffer, bindPoint, layout, firstSet=2, setHandles, dynamicOffsets)` is called once.

The caller never provides handles or offsets.

### Why `firstSet = 2` is set separately for user sets

Vulkan allows partial rebinds. The engine sets (0, 1) stay bound for the duration of a pipeline bind. Only user sets (2+) need rebinding when switching materials or descriptor groups. Keeping them as a separate call means the engine does not re-submit set 0/1 descriptors on every draw.

### How the user obtains handles for resolution

`resolveUniform(path)` should be on the `RenderPipeline` interface (it is already on `VulkanRenderPipeline`, it just needs to be promoted to the interface). The user calls it once at initialization through the asset handle and caches the result:

```
FieldHandle proj = pipelineHandle.get().resolveUniform("camera.proj");
CISHandle tex   = pipelineHandle.get().resolveUniform("myTexture");
```

`pipelineHandle.get()` is a valid one-time init call. The restriction "never access the underlying pipeline" should be read as "never configure or build the pipeline in user code" — reading handles from an already-built pipeline is a necessary read operation and is fine.

### When image descriptor updates are flushed

Pending `vkUpdateDescriptorSets` calls (from `cisHandle.set(...)`, `imageHandle.set(...)`) are flushed lazily at the start of `cmd.bindDescriptorSets(...)`, before `vkCmdBindDescriptorSets` is issued. This is automatic and invisible to the user. The `UPDATE_AFTER_BIND_BIT` is already present in the new `DescriptorPool` and `CompiledDescriptorSetLayout` code paths, making this safe.

### Required change to CommandBuffer interface

Add an overload to `CommandBuffer`:

```
void bindDescriptorSets(AssetHandle<? extends Pipeline> pipeline, UniformHandle... userHandles);
```

The existing `bindDescriptorSets(AssetHandle, long... handles)` overload should be made internal (or removed from the public interface), since passing raw `long` handles directly is what the new API is hiding.

---

## Question 2 — `@MultipleWrites(n)` — multi-write buffer bindings

### What this needs to do in Vulkan terms

A multi-write binding is a `UNIFORM_BUFFER_DYNAMIC` binding whose per-frame region is sized `alignedStructSize * n` instead of `alignedStructSize`. Each "slot" within the frame region is one struct copy. The dynamic offset at bind time selects which slot the GPU reads:

```
dynamicOffset = currentFrame * (alignedStructSize * n) + currentSlot * alignedStructSize
```

**Critical constraint:** Both `alignedStructSize` and all slot offsets must be multiples of `minUniformBufferOffsetAlignment` (commonly 256 bytes on desktop GPUs). The engine must pad `structSize` up to this alignment automatically, the same way `ShaderDataManager.initialize()` already does for the frame-data buffer. The user never sees this padding.

### The ring buffer layout for n = 3, framesInFlight = 2

```
[ frame0: slot0 | slot1 | slot2 | frame1: slot0 | slot1 | slot2 ]
```

Each `slotK` region is `alignedStructSize` bytes. The VkBuffer covers the whole thing. The descriptor binding covers the whole buffer; the dynamic offset selects the correct slot at bind time.

### Handle API for multi-write

A multi-write binding is exposed through a `MultiWriteFieldHandle` (or `MultiWriteBufferHandle` for whole-struct access). It extends the normal `FieldHandle` contract with two additional operations:

```
// advance the write cursor to the next slot (call after each draw)
handle.advance();

// write to the current slot
handle.write(slice -> slice.putMat4(myTransform));

// reset to slot 0 — called automatically by the engine at frame rotation
handle.reset();
```

The `DescriptorSets` object tracks the current slot for each multi-write binding. `advance()` increments the slot counter for this binding only. `reset()` zeroes it. Multiple independent multi-write bindings (even in the same set) each have their own slot counter.

### Per-draw usage pattern

```
// Initialization (once)
MultiWriteFieldHandle xformHandle = pipeline.resolveUniform("myUbo.transform");

// Frame render loop
for (Entity e : visibleEntities) {
    // Write this entity's data into the current slot
    xformHandle.write(slice -> slice.putMat4(e.worldTransform()));

    // Rebind user sets — passes the updated dynamic offset for the current slot
    cmd.bindDescriptorSets(pipelineHandle, xformHandle);

    cmd.draw(...);

    // Advance to the next slot before the next entity
    xformHandle.advance();
}

// Engine resets xformHandle.slot to 0 automatically during frame rotation
```

### Why rebinding is necessary per draw

Vulkan's dynamic offset is fixed at `vkCmdBindDescriptorSets` time, not at `vkCmdDraw` time. There is no way to change which slot the GPU sees mid-draw without reissuing `vkCmdBindDescriptorSets`. This is by design — dynamic offsets exist precisely for this pattern and the call is cheap. Engine sets (0, 1) are not rebound in this loop; only the user sets (2+).

### What `DescriptorSets.collectDynamicOffsets()` computes for multi-write

For each dynamic binding in a user set, in binding-number order:

- Non-multi-write: `currentFrame * alignedStructSize`
- Multi-write: `currentFrame * (alignedStructSize * n) + currentSlot * alignedStructSize`

The `cmd.bindDescriptorSets(...)` call always reads the current slot at the moment it is invoked.

### Hard limit: the n cap

If the user writes more than `n` slots in one frame (more than `n` draw calls with different data), the engine must throw an error — writing slot `n` would overflow into the next frame's region. The engine enforces this: `advance()` beyond `n-1` throws `IllegalStateException`.

### Where `n` comes from — config integration

The existing pipeline JSON config already handles `runtimeSizeArrays` and `dynamicBuffers`. The `@MultipleWrites` data follows the same pattern:

```json
{
  "multipleWrites": [
    { "name": "myUbo", "count": 10 }
  ]
}
```

This is parsed in `RenderPipelineData.fromConfig()` and stored in a new `multipleWritesCount: HashMap<String, Integer>` field on `DescriptorsInfo`. During `DescriptorSet.createDescriptorBinding()`, if a binding's name is in this map, its ring buffer is sized with the multiplier.

**The `@MultipleWrites(n)` GLSL annotation** is a convenience that the shader preprocessor strips before compilation and injects as the `multipleWrites` entry into the pipeline config (or alongside it as metadata). Regardless of whether the preprocessor does this automatically, the JSON config entry is the authoritative source — the system works without the preprocessor as long as the JSON is written correctly.

**Open question** — see section at the bottom.

---

## Question 3 — Where does runtime size array configuration live?

**Already solved by the existing asset system.** `RenderPipelineData.fromConfig()` already parses `runtimeSizeArrays` from the pipeline JSON:

```json
{
  "runtimeSizeArrays": [
    { "name": "myData.items", "size": 1024 }
  ]
}
```

This feeds directly into `DescriptorsInfo.runtimeSizeArraySizes`, which is consumed by `DescriptorSet.resolveRuntimeArraySizes()`. No Java builder call is needed. The user writes the pipeline JSON; the asset system handles the rest.

`pipeline.getDescriptorSets().runtimeSize(...)` as proposed in `DS-API.md` is therefore incorrect — that method should not exist. Runtime sizes are a pipeline-load-time configuration, not a runtime API call.

### Verification that the existing path is correct

The flow from disk to descriptor:

```
assets.xml
  └─ pipeline JSON file (contains "runtimeSizeArrays")
       └─ ConfigRenderPipelineConverter.performConversion()
            └─ RenderPipelineData.fromConfig()
                 └─ RenderPipelineData.descriptorsInfo()
                      └─ DescriptorsInfo.runtimeSizeArraySizes populated
                           └─ VulkanRenderPipeline.createDescriptorSets(...)
                                └─ DescriptorSet(allocator.allocate(layout), device, engine, layout, additionalInfo)
                                     └─ DescriptorSet.resolveRuntimeArraySizes(tl, additionalInfo.runtimeSizeArraySizes)
```

This path is complete except for the bugs listed in the audit above. No new API surface is needed.

### SSBO with runtime arrays and the buffer sizing

When a runtime-size array is resolved, `DescriptorSet.recomputeSize()` recalculates the total buffer size. The `MappedBuffer` is then allocated at that final size. This works, but one existing issue: if the runtime size is 0 or missing, `resolveRuntimeArraySizes()` throws `IllegalStateException`. The pipeline JSON must always declare a size for any SSBO with an unsized trailing array — there is no sensible default.

---

## Question 4 — Multiple writes with different data to samplers

**No, this does not need a first-class API feature.** Here is why, and what to use instead.

### Why it can't work with the current descriptor model

Image and sampler descriptors do not support dynamic offsets. There is no Vulkan equivalent of "choose which texture the GPU samples from by setting an offset at draw time." The descriptor set entry itself encodes the VkImageView pointer.

If you update the same `CISHandle` twice before submit (calling `.set(sampler, texA)` then `.set(sampler, texB)`), both draw calls that use this descriptor set will see `texB` — the last update wins. You cannot have draw call 1 see `texA` and draw call 2 see `texB` with a single binding.

### Correct Vulkan solutions for per-draw different textures

**Option A — Texture array binding** (for a known, bounded set of textures per pipeline):

```glsl
layout(set = 2, binding = 0) uniform sampler2D myTextures[32];
```

```
int texId = ...; // set as push constant before each draw
```

The texture array is populated once (or rarely). The draw-call-specific selection is a push constant — an integer. `vkCmdPushConstants` is one CPU call and has essentially zero overhead. No descriptor rebinding is needed.

**Option B — Bindless (engine set 1)** (for arbitrary textures from the asset system):

Register the texture once, receive an integer ID:

```
int texId = engine.textures().register(myTexture);
```

Pass it as a push constant before each draw. The shader reads `textures[texId]` from the engine's bindless array. Unregister when no longer needed.

This is the recommended approach for material textures that vary per object.

**Option C — Pre-written per-material descriptor sets** (for complex multi-texture materials):

Create a separate `DescriptorSetGroup` per material at load time. Bind the correct group before each draw. Since `vkCmdBindDescriptorSets` for user sets (2+) is cheap and does not disturb engine sets (0, 1), this is acceptable for material switching.

### Summary

`CISHandle.set()` is for changing a texture binding that applies to all subsequent draws until the next call to `.set()`. It is not a per-draw operation. If you need per-draw textures, use push constants with texture arrays or bindless. The descriptor API does not need to support the per-draw sampler update pattern.

---

## Resolved questions

### OQ-1: `@MultipleWrites` preprocessor integration — RESOLVED

Both the pipeline JSON and the preprocessor can supply multi-write counts. They are merged, with preprocessor data taking priority over JSON when both declare the same binding name.

**JSON side (already implementable):**

```json
{
  "multipleWrites": [
    { "name": "myUbo", "count": 10 }
  ]
}
```

Parsed in `RenderPipelineData.fromConfig()` into `DescriptorsInfo.multipleWritesCounts: HashMap<String, Integer>`.

**Preprocessor side (pending asset manager API):**

The preprocessor runs before or during shader compilation and extracts `@MultipleWrites(n)` annotations from the GLSL source alongside their binding names. It does not write any file. Instead, it passes extra data directly into the `AssetProtocol` at load time — a capability not yet implemented in the asset manager. When that API exists, the preprocessor injects `multipleWritesCounts` into `DescriptorsInfo` before `RenderPipelineData.fromConfig()` finishes, and those values take priority over any JSON entries for the same binding name.

**Merge logic in `RenderPipelineData.fromConfig()` once preprocessor API exists:**

```
descriptorsInfo.multipleWritesCounts.putAll(jsonMultipleWrites);       // JSON baseline
descriptorsInfo.multipleWritesCounts.putAll(preprocessorMultipleWrites); // preprocessor wins
```

Until the preprocessor API is implemented, the JSON config is the only source. This is fully usable today.

### OQ-2: Frame rotation and `reset()` ownership — RESOLVED

The pipeline registry drives rotation. At `beginFrame`, the renderer iterates the registry and calls `descriptorSets.rotate()` on every registered pipeline's `DescriptorSets` instance.

`DescriptorSets.rotate()` does two things:
1. For each buffer binding backed by a `MappedGpuRingBuffer`: calls `ringBuffer.rotate()` to advance the internal frame index.
2. For each multi-write binding: resets the slot counter to 0.

This keeps rotation and slot management fully self-contained inside `DescriptorSets`. The pipeline registry already exists and iterating it at frame start is a negligible cost even with many loaded pipelines, since `rotate()` on a ring buffer is a single modulo increment and the slot reset is an array zero-fill.

## Static Buffers
define static buffers that will only be written to once in the pipeline.json

Good catch. The problem is real: advance() on a FieldHandle for "myUbo.proj" needs to advance the slot for the entire myUbo binding, not just for the proj field. The field is just a window into one slot — the slot index belongs to the
  binding.

  The fix is a shared SlotCursor object that lives on the BufferBinding, not on the handle.

  ---
  How it works:

  The BufferBinding owns a SlotCursor when it's a multi-write binding (null otherwise). Every FieldHandle and BufferHandle that resolves into that binding receives a reference to the same cursor object at construction time.

  BufferBinding "myUbo"
    └─ SlotCursor cursor  ←─────────────────────────┐
                                                     │ (shared reference)
  resolve("myUbo.proj")  → FieldHandle { slotCursor ─┤, fieldOffset=0,  size=64 }
  resolve("myUbo.view")  → FieldHandle { slotCursor ─┘, fieldOffset=64, size=64 }

  At write time, the FieldHandle computes:

  writeAddress = ringBuffer.base
               + currentFrame * (alignedStructSize * n)   // frame region
               + cursor.slot  * alignedStructSize          // slot within frame
               + fieldOffset                               // field within slot

  advance() on any field handle from this binding just calls cursor.advance(). Because it's the same object, projHandle.advance() and viewHandle.advance() are identical operations — they both move the same slot counter forward.

  ---
  Implication the user must understand:

  Call advance() exactly once per draw per multi-write binding, on whichever field handle is convenient. Calling it twice (once on proj, once on view) would advance the slot twice and skip a slot. This should be documented clearly, and if
   cursor.slot would exceed n, throw an IllegalStateException.

  ---
  Single-write field handles:

  The slotCursor reference is null for non-multi-write bindings. advance() checks for null and throws immediately so the user gets a clear error if they accidentally call it on a regular binding.

  ---
  DescriptorSets.rotate() at frame start:
  
  It iterates all BufferBindings that have a non-null SlotCursor and calls cursor.reset(). Since the cursor is shared, one reset call covers all handles that reference that binding.
