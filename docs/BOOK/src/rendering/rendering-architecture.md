# Rendering Architecture

## Graphics API abstraction
The renderer is abstracted into 4 distinct parts like so: 
```
User API (High Level) - User facing api.
↓
Low Level - Low level data management.
↓
API Abstraction Layer - Abstraction over common backend objects.
↓
Implementation Level - Backed specific, communicates via the interfaces provided by the API Abtraction layer.
```

The common developer will mostly interact with the User API layer, which exposes interfaces like `VertexConsumer`, `StaticMesh` and classes like `ShapeRenderer` to 
ease communication through the deeper api levels.

The low level layer is the layer that handles managing engine data via classes like `TextureManager`, `FrameDataManager` and `MaterialManager` (this doesnt exist yet lol)
these, interfaces handle uploading the actual textures, commonly used frame data and materials to the textures at the correct time. They are abstracted because they might be
backed specific.

The API abstraction layer handles resource creation, submitting commands. The average user should not have to interface with these classes at all. This layer provides abstractions
over classes like `RenderDevice`, `Renderer`, `CommandBuffers` etc. They allow for drop-in replacements of the engine's included Vulkan Renderer.

The implementation level, this level is **backend specific**, and therefore should not be used in most cases. Interfacing with this layer in objects like the `Scene`
will prevent the possibility of using a different rendering API. If a different rendering API is used and the user accesses class from a different implementation this **will** cause crashes.
This layer is only for people with significant experience who want to implement their own rendering backed.

## Render loop
```
Window
↓
Renderer
↓
Render Graph Manager
↓
Render Passes
↓
Draw Calls
```
The render loop is entirely managed by the engine, the user only specifies the render graph of the current scene and provides any additional data for the `RenderPass` objects
via the `GraphContext` provided in the `Scene`'s `onPrepareRendering` and `onRenderPassFinished` methods.

