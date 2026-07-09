# Questions about the proposed API
1. How do you propose binding descriptor should look like? When binding you will need to provide dynamic offsets, the user should not have access to that.
2. Multiple buffer writes, different data. What do you think about an API where:
    
    Define a buffer which allows N writes in one frame like so:
    ```glsl
    @MultipleWrites(10)
    layout(...) uniform UBO {
        ...
    } myUbo;
    ```
    There is already a shader pre processor planned with the asset manager service, what this would do is see the `@MultipleWrites(n)`, figure out what the name of the struct (in this case `UBO`) and the field name (in this case `myUbo`) and pass the data that this buffer should support multiple writes into the actual shader reflection and descriptor set creation. What this would do is create a `MappedGpuRingBuffer` for frames in flight but the size of each frame would not be `structSize` but rather `structSize * multipleWritesCount`.

3. Where do you propose the
    ```java
    pipeline.getDescriptorSets()
            .runtimeSize(...);
    ```
    Should be called? The asset system automatically loads pipelines and the user only resolves and passes preferably only [handles](src/main/java/com/vke/api/assets/AssetHandle.java), they should never have access to the underlying pipeline. If you need to understand the shader loading system, the main files would be [Shader Protocol Directory](src/main/java/com/vke/core/assets/pipeline/protocols/shader/) and [Assets XML file](src/main/resources/vke/assets/assets.xml)

4. How do i handle multiple writes with different data to samplers, is there a need to support that?