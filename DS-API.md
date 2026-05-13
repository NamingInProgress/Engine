# VKE Descriptor Set API — Design Specification

## 1. Overview

This document specifies the architecture for the refactored descriptor set API in VKEngine. The design preserves the path-based handle system and shader-reflection-driven layout creation while fixing frame-in-flight correctness, multi-write semantics, and the engine/user set boundary conflict.

The API has two distinct layers:
- **Core layer** (`com.vke.core.vulkan.descriptor`) — Vulkan objects, pool management, buffer allocation, frame rotation. Never exposed to the user.
- **API layer** (`com.vke.api.rendering.vulkan.descriptors`) — Handles, path resolution, typed write surfaces. What the user touches.

---

## 2. Set Numbering Convention

This is a fixed, engine-enforced contract. It must be documented and never violated.

| Set | Owner  | Contents                                          | User-visible? |
|-----|--------|---------------------------------------------------|---------------|
| 0   | Engine | Per-frame globals: camera, time, etc. (FrameData) | Handles only  |
| 1   | Engine | Bindless texture/sampler array                    | ID only       |
| 2+  | User   | Any user-defined UBOs, SSBOs, samplers, images    | Full access   |

**Why this matters for implementation:** When the pipeline layout is built, sets 0 and 1 must always be present and use the engine's compiled layouts — regardless of what the user's shaders declare. Reflection output for sets 0 and 1 must be validated against the engine's canonical layouts and rejected (with a clear error) if they conflict.

The engine-reserved sets are injected automatically into every pipeline layout. The user's shaders never need to redeclare them to use them; the engine binds them at the start of every draw.

---

## 3. Core Concept: `DescriptorSetGroup`

`DescriptorSetGroup` is the primary user-facing object. It represents all descriptor sets for a single pipeline's user-defined resources (sets 2+). It is obtained from the pipeline and cached by the user.

```
DescriptorSetGroup group = pipeline.getDescriptorSets();
```

Internally, it holds:
- One `DescriptorSetInstance` per unique user set number (e.g., one for set 2, one for set 3)
- Each `DescriptorSetInstance` contains `framesInFlight` Vulkan descriptor set handles for image/sampler bindings
- Each buffer binding is backed by a `MappedRingBuffer` — a single large buffer subdivided into `framesInFlight` regions

The user never sees frames. The group internally queries the renderer's current frame index whenever a write or bind operation is performed.

---

## 4. Handle Types

All handles are resolved once and cached. They are cheap value objects — resolving them is the only non-trivial cost.

### 4.1 Buffer Handles

Used for UBO and SSBO descriptors.

**`FieldHandle`** — access to a single field within a struct-backed binding. Replaces `EntryHandle`.

```
FieldHandle proj = group.resolve("frame_data.proj");
proj.write(slice -> slice.putMat4(camera.projection()));
```

- The `BufferSlice` passed to the consumer is bounded to exactly the field's byte range. Writing outside the range is impossible.
- Internally, the slice's base address is `ringBuffer.currentFrameBase() + bindingOffset + fieldOffset`.

**`BufferHandle`** — access to the entire buffer backing a binding. Used when you want to write the full struct at once, or when the binding is a runtime-size SSBO.

```
BufferHandle buf = group.resolve("myData");
buf.write(slice -> { ... });
```

### 4.2 Image / Sampler Handles

These handles are not write-to-memory. Calling `.set(...)` queues a `vkUpdateDescriptorSets` call that is flushed before the next queue submit.

**`CISHandle`** — combined image sampler (single). Replaces `CombinedImageSamplerHandle`.

```
CISHandle tex = group.resolve("myTexture");
tex.set(sampler, texture);
```

**`CISArrayHandle`** — combined image sampler array.

```
CISArrayHandle arr = group.resolve("myTextures");
arr.set(0, sampler, texture);
arr.set(3, sampler, otherTexture);
```

**`CISHandle` from array element** — resolve a specific slot for use like a single handle:

```
CISHandle slot3 = group.resolve("myTextures[3]");
slot3.set(sampler, texture);
```

**`StorageImageHandle`** — for `image2D` / `uimage2D` etc. Same `.set(texture)` API, no sampler.

```
StorageImageHandle depth = group.resolve("depthOutput");
depth.set(depthTexture);
```

**`SamplerHandle`** / **`SampledImageHandle`** — for separate sampler and separate image bindings. Same API pattern.

---

## 5. Path Resolution

Paths follow the GLSL name structure.

| Path                    | Resolved Handle Type | Notes                                    |
|-------------------------|----------------------|------------------------------------------|
| `"frame_data.proj"`     | `FieldHandle`        | Navigates struct tree from reflection    |
| `"frame_data"`          | `BufferHandle`       | Root of the buffer binding               |
| `"myTexture"`           | `CISHandle`          | Non-array sampler2D                      |
| `"myTextures"`          | `CISArrayHandle`     | Array sampler2D — whole array            |
| `"myTextures[3]"`       | `CISHandle`          | Array sampler2D — single element         |
| `"myData.items[2].x"`   | `FieldHandle`        | Nested struct + array + field            |

Resolution is performed by a lexer/parser that walks the reflection tree. The existing `HandleLexer`, `HandleParser`, and `LayoutResolver` are the right approach; they need to be updated to emit the new handle types.

**Flaw (current code):** The path resolver is in the API layer but takes `CompiledDescriptorSetLayout` (a core Vulkan object) as input, violating the layer boundary. Resolution should operate on the reflection type tree (`StructType`, `ArrayType`, etc.) and only touch the compiled layout at handle construction time to get the buffer reference and binding number.

---

## 6. Frame-in-Flight Strategy

### 6.1 Buffer Bindings — Ring Buffer

Every buffer binding (`UBO`, `SSBO`) is backed by a single large persistently-mapped `VkBuffer` partitioned into `framesInFlight` equal regions:

```
[ frame 0 data | frame 1 data | frame 2 data ]
```

When a `FieldHandle` or `BufferHandle` write is called:
1. The group queries the renderer for the current frame index `f`.
2. The slice is calculated as: `bufferBase + (f * frameStride) + bindingOffset + fieldOffset`.
3. The write goes to CPU-visible memory. The GPU only reads it when the command buffer referencing that frame's region executes.

The descriptor set itself points at the whole buffer with a `VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER_DYNAMIC` or `VK_DESCRIPTOR_TYPE_STORAGE_BUFFER_DYNAMIC` binding. The correct frame offset is passed as a dynamic offset at `vkCmdBindDescriptorSets` time.

This means there is only **one** Vulkan descriptor set handle per user set, regardless of frames-in-flight. The dynamic offset selects the frame region at bind time. This is the correct, minimal-overhead Vulkan pattern.

**The engine already uses this approach for set 0 (FRAME_DATA_BUFFER / MappedGpuRingBuffer).** The refactor must apply the same strategy to all user buffer bindings.

### 6.2 Image / Sampler Bindings — Per-Frame Descriptor Set Copies

Image and sampler descriptors cannot use dynamic offsets (that only applies to buffers). Instead, each user set that contains image/sampler bindings must have `framesInFlight` Vulkan descriptor set handles:

```
setInstances[0] // VkDescriptorSet for frame 0
setInstances[1] // VkDescriptorSet for frame 1
setInstances[2] // VkDescriptorSet for frame 2
```

When `.set(sampler, texture)` is called on a `CISHandle`:
1. A pending update is recorded for the handle's binding.
2. Before the queue submit for frame `f`, the engine flushes all pending image updates to `setInstances[f]` via `vkUpdateDescriptorSets`.
3. At bind time, `setInstances[currentFrame]` is bound.

**Flaw (current code):** The `DynamicDescriptorAllocator` holds a flat `ArrayList<DescriptorSet>` with no per-frame indexing. There is no frame awareness. This is incorrect for image descriptors and must be replaced by the per-frame copy strategy above.

### 6.3 Mixed Sets (Buffers + Images)

When a user set contains both buffer and image bindings, both strategies apply simultaneously:
- The set has one dynamic-offset buffer per frame sharing a single descriptor set.
- The image bindings use per-frame descriptor set copies (or the buffer can be shared across all copies if it's the same VkBuffer with dynamic offsets).

The simplest correct implementation: always allocate `framesInFlight` VkDescriptorSet copies per user set. Buffer bindings point to the same VkBuffer (dynamic offset selects the frame region). Image bindings differ per frame copy. This avoids the mixed-strategy complexity at the cost of slightly more descriptor set objects.

---

## 7. Multiple Writes Per Frame

### 7.1 Buffer fields (case a — same field, multiple writes per frame)

This works correctly with persistent mapping. Writes go to CPU memory. The GPU reads the buffer only during command execution, which happens after submit. Multiple `handle.write()` calls on the same field before submit are safe — the last write wins. No special mechanism is needed beyond what the ring buffer already provides.

### 7.2 Multiple draw calls writing different data (case b)

If draw call A and draw call B need different values in the same binding (e.g., per-object transform in a UBO), using a single `FieldHandle` and writing between draws does **not** work. The buffer is mapped and both draws would see the last-written value at GPU execution time.

The correct Vulkan solution for per-object data is one of:
- **Push constants** — for small data (typically ≤128 bytes). Already supported.
- **Dynamic UBO with per-draw offset** — allocate one large buffer, each object gets its own region, bind a different offset per draw. This requires API support for "allocate a slot from the per-object buffer" which is a distinct operation from field-level writes.
- **Separate descriptor sets per object** — allocate and cache one set per material/object. Bind the matching set per draw.

This document does not specify the per-object dynamic UBO API in full. It is flagged as a known gap. For now, the per-draw-call problem is left to push constants or separate descriptor sets.

---

## 8. Pool Management

Recommendation: a single global `DynamicDescriptorAllocator` per device (pool-of-pools with growth), matching the existing implementation's approach. Pipelines do not own their pools — they allocate from the global one.

The current `DynamicDescriptorAllocator` grows via:
```
newSize = min(currentSize * 1.5, 4096)
```
This is reasonable. The only required change is ensuring that when allocating `framesInFlight` copies of a set, the pool sizing accounts for the multiplier.

**Flaw (current code):** `DynamicDescriptorAllocator.createPool()` passes `framesInFlight=1` unconditionally. The allocator is not told how many frames are in flight and therefore allocates pools that are too small. The allocator must either receive the frames-in-flight count at construction, or the caller must request `framesInFlight` sets when allocating, not one at a time.

**Flaw (current code):** `DynamicDescriptorAllocator.allocate()` mixes concerns — it allocates, constructs a `DescriptorSet`, tracks it internally, and the `update()` / `bindDescriptors()` methods expect you to use the same allocator instance as the binding and update authority. This turns the allocator into a de-facto descriptor set manager, which is not a clean separation. The allocator should only allocate VkDescriptorSet handles. Construction of the higher-level `DescriptorSet` / `DescriptorSetGroup` objects should happen elsewhere.

---

## 9. Engine-Reserved Sets (0 and 1)

### 9.1 Set 0 — FrameData

Managed entirely by the engine. Users do not call `group.resolve()` for set 0.

Contents (defined programmatically, not from user shader reflection):
- Binding 0: `UNIFORM_BUFFER_DYNAMIC` — camera matrices, time, and other per-frame scalars

The `MappedGpuRingBuffer` (already used in `ShaderDataManager.FRAME_DATA_BUFFER`) is the backing store. At the start of each frame, the engine writes the current camera and time into the current frame's region. The dynamic offset for the current frame is passed at bind time — the user never sees it.

Engine-provided handles for set 0 can be obtained through a separate, engine-managed API (not `DescriptorSetGroup`), e.g.:
```
FieldHandle timeHandle = engine.frameData().resolve("camera.proj");
```
These handles behave identically to user handles — same `BufferSlice` write surface, same frame-transparency.

**Flaw (current code):** `EngineDescriptorSetsManager.initLayouts()` discovers the engine layout by matching resource names ("camera", "textures") from a "truth" shader. This is fragile: a typo in a shader name silently breaks the engine layouts. Engine layouts should be defined programmatically and the truth shader's reflection should only be used to verify compatibility, not to construct the layout.

**Flaw (current code):** `FRAME_DATA`, `EMPTY`, and `BINDLESS` descriptor set instances are commented out in `initLayouts()`. The engine-reserved sets are not currently being allocated at all.

**Flaw (current code):** `EngineDescriptorSetsManager.getDefaults()` is missing a return statement — this is a compile error.

### 9.2 Set 1 — Bindless Texture Array

Managed by the engine. The user registers a texture and receives an integer ID. In the shader, they index into `textures[id]`.

```
int id = engine.textures().register(myTexture);
// shader: textures[id]
engine.textures().unregister(id);
```

The bindless set uses:
- `PARTIALLY_BOUND_BIT` — slots may be null
- `VARIABLE_DESCRIPTOR_COUNT_BIT` — size is device-capability-driven
- `UPDATE_AFTER_BIND_BIT` — textures can be registered mid-frame

This is already partially modeled in `CompiledDescriptorSetLayout`'s `partialBinding` path and `ShaderDataManager.textures[]`. The registration/unregistration API and the linkage between `ShaderDataManager` and the bindless descriptor set need to be completed.

**Note on "user wants their own image at set 0":** The scenario from the previous issue doc (`image2D depth` at `set=0, binding=0`) is resolved by the set numbering convention — that resource belongs in set 2 or higher, defined by the user's pipeline. The engine never injects competing resources into user sets. If the user needs access to an engine-internal resource like the depth buffer as an input to their shader, it must be exposed through a render-graph / render-pass resource system, not by re-declaring it at a fixed set/binding. This is out of scope for the descriptor set API.

---

## 10. Creation Flow

The full lifecycle from shader compilation to a usable handle:

```
1. Shader compilation produces SPIR-V
2. ShaderReflector parses SPIR-V → ReflectedShader
   (set, binding, name, StructType tree, array dimensions)

3. For each user set (set >= 2):
   a. Validate: no set collides with engine-reserved (0, 1)
   b. Build DescriptorSetLayout from BindingLayout list (already exists)
   c. Separate bindings into buffer bindings and image bindings

4. CompiledDescriptorSetLayout is created per user set
   - Buffer bindings: always DYNAMIC type (for frame-offset support)
   - Image bindings: with UPDATE_AFTER_BIND_BIT if any image is present in the set

5. Allocate from global DynamicDescriptorAllocator:
   - framesInFlight VkDescriptorSet handles per user set
   - Each handle gets initial descriptor writes (buffer info pointing to the ring buffer)

6. Allocate MappedRingBuffer per buffer binding
   - Size: (binding byte size) × framesInFlight
   - One VkBuffer, persistently mapped

7. Construct DescriptorSetGroup
   - Holds the VkDescriptorSet array and ring buffers
   - Exposes resolve(path) for handle creation

8. resolve(path) walks the StructType tree, returns the appropriate handle subtype
```

---

## 11. Runtime-Size Arrays and Dynamic Buffers

### 11.1 Runtime-size SSBOs

GLSL SSBOs can have an unsized trailing array:
```glsl
layout (set = 2, binding = 0) buffer MyData {
    uint count;
    MyStruct items[];
};
```

Reflection produces an `ArrayType` with `length = -1`. The user must supply the intended size before the descriptor set is created. This is the current `DescriptorsInfo.runtimeSizeArraySizes` mechanism, which is correct.

The API surface for this should be on the `DescriptorSetGroup` builder, not a raw `HashMap` passed to a constructor:

```
pipeline.getDescriptorSets()
        .runtimeSize("myData.items", 1024)
        .build();
```

### 11.2 Dynamic UBO / SSBO descriptor types

All user buffer bindings should be created as `UNIFORM_BUFFER_DYNAMIC` or `STORAGE_BUFFER_DYNAMIC`. The frame offset is the dynamic offset passed at bind time. This is the correct approach and eliminates the need to update the descriptor set between frames for buffer bindings.

**Flaw (current code):** `DescriptorSet.createDescriptorBinding()` creates `MappedBuffer` directly based on `layout.type` — it does not enforce that buffer bindings use the dynamic variant. Whether a binding is dynamic or static is currently controlled by an ad-hoc `isDynamic` flag passed into `BindingLayout.fromDescriptorResource()`. The new system should always use dynamic for user buffer bindings.

---

## 12. Per-Frame Query Contract

The handles need to know the current frame index at write time without exposing it to the user. The proposed mechanism:

- The renderer exposes a package-internal `RendererFrameClock.currentFrame()` method.
- `DescriptorSetGroup` holds a reference to the `RendererFrameClock`.
- All handle write operations call through the group (or hold a reference to the clock).

An alternative is to make handles stateless and pass the frame context at write time — but this would expose frames to the user, which is explicitly not desired.

The clock must be the **same** source of truth used by the command buffer recorder to determine which semaphore/fence to wait on. Frame index drift between handles and the command buffer would cause data corruption.

---

## 13. Identified Flaws Summary

| # | Location | Flaw | Severity |
|---|----------|------|----------|
| 1 | `EngineDescriptorSetsManager.getDefaults()` | Missing return statement — does not compile | Critical |
| 2 | `EngineDescriptorSetsManager.initLayouts()` | `FRAME_DATA`, `EMPTY`, `BINDLESS` sets are commented out — never allocated | Critical |
| 3 | `DynamicDescriptorAllocator.createPool()` | Passes `framesInFlight=1` unconditionally — pool is undersized for multi-frame | High |
| 4 | `DynamicDescriptorAllocator` | Flat `allocatedSets` list — no per-frame awareness for image descriptor sets | High |
| 5 | `BufferHandle` / `EntryHandle` | `cpuAddress` is an absolute address baked at construction — frame-unaware, always writes to frame 0's region | High |
| 6 | `EngineDescriptorSetsManager` | Engine layouts discovered by matching resource names ("camera", "textures") from a shader — fragile | Medium |
| 7 | `DynamicDescriptorAllocator` | Mixes allocation, descriptor updating, and draw-time binding into one class | Medium |
| 8 | `LayoutResolver` / path resolution | Takes `CompiledDescriptorSetLayout` (Vulkan core object) — layer boundary violation | Medium |
| 9 | `DescriptorSet.createDescriptorBinding()` | Does not enforce `DYNAMIC` variant for buffer bindings — frame offsets cannot be applied | Medium |
| 10 | `ShaderDataManager` | `removeTexture()` is a TODO — bindless texture slots can never be freed | Low |

---

## 14. Out of Scope

The following are **not** addressed by this spec and should be tracked separately:

- Render-graph / pass resource system (how engine textures like depth are made available as shader inputs)
- Per-draw-call dynamic UBO allocation (per-object data beyond push constants)
- Acceleration structure descriptors
- Multi-device / secondary command buffer descriptor set inheritance
