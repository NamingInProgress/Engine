# VKECompilerPlugin
## Vertex Format Creation
The vertex extension of the compiler plugin aims to ease the creation of different
vertex formats. This can be both tedious and dangerous, since manually calculating
the byte stride can cause mistakes, so the compiler plugin does everything for you.

Let's take a look at a manual vertex definition:

```java
import com.vke.api.rendering.abstraction.draw.Vertex;
import com.vke.api.rendering.abstraction.renderer.data.TexturableEncoder;
import com.vke.api.rendering.abstraction.renderer.data.Texture;

public class MyVertex implements Vertex {
    /** 
     * This field, while not strictly necessary is a nice helper
     * for creating vertex consumers via the VertexConsumerProvider
     **/
    public static final MyVertex TEMPLATE = new MyVertex(0, 0, 0, 0, null);

    private final float x, y;
    private final float u, v;
    private final Texture texture;

    public MyVertex(float x, float y, float u, float v, Texture tex) {
        this.x = x;
        this.y = y;
        this.u = u;
        this.v = v;
        this.texture = tex;
    }

    /**
     * 4 floats + 1 int for the bindless array as specified in rendering/textures.md
     **/
    @Override
    public int getByteStride() {
        return 4 * Float.BYTES + 1 * Integer.BYTES;
    }

    @Override
    public void putSelf(TexturableEncoder buf) {
        buf.float2(x, y);
        buf.float2(u, v);
        buf.sampler2D(texture);
    }
}
```

This is susceptible to many mistakes, such as wrong ordering in the `putSelf` method
or an incorrect byte stride. The vertex extension allows you to omit most of these methods like so:

```java
import com.vke.api.rendering.abstraction.draw.Vertex;
import com.vke.api.rendering.abstraction.renderer.data.TexturableEncoder;
import com.vke.api.rendering.abstraction.renderer.data.Texture;
import pl.epsi.MakeVertex;
import pl.epsi.Type;

@MakeVertex
public class MyVertex implements Vertex {
    /**
     * When using the @MakeVertex annotation, this field is required and should be
     * set to null, as it gets replaced by a new call automatically.
     **/
    public static final MyVertex TEMPLATE = null;

    @Type.Float2
    private final float x, y;
    @Type.Float2
    private final float u, v;
    @Type.Sampler2D
    private final Texture texture;

    public MyVertex(float x, float y, float u, float v, Texture tex) {
        this.x = x;
        this.y = y;
        this.u = u;
        this.v = v;
        this.texture = tex;
    }
}
```
The constructor is left for the user to implement, because having non-initialized final variables
in your class is an error and IDEs will complain about it. Sadly the `TEMPLATE` field cannot be
fully automatically generated, because once again before compilation `MyVertex.TEMPLATE` does not exist
which would cause errors in IDEs.

Vertex formats defined with the `@MakeVertex` annotation should not have any fields
that are not meant to end up in the final construction of the vertex. If these
fields do exist the behavior is undefined and **may cause crashes**.

Available types (Defined in the [Type Annotation](https://github.com/NamingInProgress/VKECompilerPlugin/blob/master/annotations/src/main/java/pl/epsi/Type.java)):
 - Float
   - Float1
   - Float2
   - Float3
   - Float4
 - Double
   - Double1
   - Double2
   - Double3
   - Double4
 - Int
   - Int1
   - Int2
   - Int3
   - Int4
 - UInt
   - UInt1
   - UInt2
   - UInt3
   - UInt4
 - Textures
   - Sampler2D