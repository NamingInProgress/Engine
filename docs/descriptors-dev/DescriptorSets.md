# VKE Descriptor Set API Design Doc

## Introduction
You are in the root of the VKEngine project. VKE uses vulkan as it's rendering backed and uses a multiple layer style abstration layer over vulkan.
A core part of this abstraction layer is already made, like physical, logical devices, queue families and others. Right now we are working on the descriptor set API.
There is already an abstraction API over descriptor sets, however it is not correct and in accordance with vulkan programming standards and mechanics. The old system is located in packages [API](src/main/java/com/vke/api/rendering/vulkan/descriptors/) and [CORE](src/main/java/com/vke/core/vulkan/descriptor/)

## Goals
The goal of this descriptor set refactor is to end up with a descriptor set system that is understandable and not very hard to use (to an average person who did rendering). It should be performant, while still safe, preventing overflows or writing outside of your allowed space and intuitive.

The descriptor set API should support both engine reserved descriptor sets like the `FrameData` for later AND user defined descriptor sets.

What to focus on: creating a good, understandable and vulkan compliant road map of the new descriptor set API. Point out where descriptor sets should be stored, how they should be created from reflection, what the best way to access the descriptors while keeping the path based system, how to allow for multiple writes per frame to both sampler style descriptors and buffer style (struct vs no struct).

## Suggestions
1. VKE currently has a [shader reflection service](src/main/java/com/vke/core/services/shr/ShaderReflector.java), which provides the old descriptor set API with all the data needed so the user does not have to worry about creating set/binding layouts. There are however a few problems with the descriptor set API and how it uses the data provided by reflection. I would like if the shader reflection stayed and kept the user out of creating layouts.

2. Shader reflection provides a tree like structure for any buffer uniforms (UBO/SSBO), where each member has the correct name, i would like to keep the ability to write to a descriptor by that path. Take this example:

    We have this UBO in our shader
    ```glsl
    layout (set = 0, binding = 1) uniform FrameData {
        mat4 proj;
        mat4 view;
        float time;
    } frame_data;
    ```
    The shader reflection service would generate a [DescriptorResource](src/main/java/com/vke/core/services/shr/ReflectedShader$DescriptorResource.java) with a [StructType](src/main/java/com/vke/api/rendering/vulkan/descriptors/types/StructType.java) looking something like this:
    ```
    Struct Type
        Members:
            MatrixType("proj", 4, 4, 4);
            MatrixType("view", 4, 4, 4);
            PrimitiveType("time", PrimitiveBaseType.FLOAT, 1);
    ```
    With the current descriptor system you would access and write to for example the projection matrix like so:
    ```java
    EntryHandle handle = pipeline.resolveHandle("frame_data.proj");

    void render() {
        handle.write((slice) -> slice.putMat4(projectionMatrix))
    }
    ```
    We first cache the [handle](src/main/java/com/vke/api/rendering/vulkan/descriptors/handles/single/EntryHandle.java) which holds the offset of the field, the size in bytes and other information like the actual GPU buffer to write to.
    Calling `handle.write()` takes in a `Consumer<BufferSlice>` which is a part of the mapped memory that starts at an offset stored in the handle and of size that the handles dictates. This prevents errors like writing more than is allowed (eg. writing a mat4 but only mat3 is defined).
3. The ideas mentioned in the previous two points is something i would like to keep: having a size protected writer like the `BufferSlice` and getting descriptors by the path.

## Previous attempts
The previous attempts at a Descriptor Set API have failed and had big issues like the following.

1. The current system has a hacked together and very bad way of allowing for multiple writes in a single frame, which is necessary for multiple things.

2. The system has basically no support for runtime size arrays and dynamic UBOs/buffers.

3. A newer rewrite which is where you are currently (a mix of the old and new system) has engine reserved descriptor sets which works fine for general textures, but if a user wanted to add their own single texture, like using the depth texture via 
    ```glsl
    layout (set = 0, binding = 0) image2D depth
    ```
    Tt would be pretty much impossible and would go back to the previous issue of multiple writes to a descriptor per frame not being supported by vulkan.


## Non struct descriptors
For descriptors like 
```glsl
layout (...) uniform sampler2D myTex
``` 
The path would be `.resolve("myTex")`, which would preferably give you a special type of handle like the EntryHandle mentioned earlier but only for samplers with a method such as `.set(Sampler, Texture)`

If, however, you have a case like this:
```glsl
layout (...) uniform sampler2D myTextures[10];
```
Resolving "myTextures" would not give a CISHandle (or something of that sort) but rather a CISArrayHandle, with a method like `.set(index, Sampler, Texture)`

To select a specific one and access it like in the first example you would use `.resolve("myTextures[n]")` which will give the same type as the first example.