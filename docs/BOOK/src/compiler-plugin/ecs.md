# VKECompilerPlugin
## ECS Component Creation
The vke compiler plugin's ecs extension aims to ease the creation of ECS components.
To create an ECS component the "standard" way in java, it would look something like this:
```java
public class MyComponent implements Component {
    public static final int ID = ComponentRegistry.register(MyComponent.class);
    
    private float[] x, y, z;
    
    public MyComponent(int initialSize) {
        this.x = new float[initialSize];
        this.y = new float[initialSize];
        this.z = new float[initialSize];
    }
    
    public int getId() { return MyComponent.ID; }
    
    public void resize(int newSize) {
        this.x = Arrays.copyOf(x, newSize);
        this.y = Arrays.copyOf(y, newSize);
        this.z = Arrays.copyOf(z, newSize);
    }
    
    public void swap(int from, int to) {
        float tempX, tempY, tempZ;
        tempX = x[to];
        tempY = y[to];
        tempZ = z[to];
        x[to] = x[from];
        y[to] = y[from];
        z[to] = z[from];
        x[from] = tempX;
        y[from] = tempY;
        z[from] = tempZ;
    }
    
    public void copyFrom(Component other, int thisIndex, int otherIndex) {
        if (other instanceof MyComponent mc) {
            x[thisIndex] = mc.x[otherIndex];
            y[thisIndex] = mc.y[otherIndex];
            z[thisIndex] = mc.z[otherIndex];
        }
    }
}
```
However, as you can see, this can be very tedious and prone to breaking. This is why the VKECompilerPlugin
offers the `@EcsComponent` annotation.
With this annotation creating an ECS component is as simple as this:

```java
import pl.epsi.EcsComponent;

@EcsComponent
public class MyComponent implements Component {

    private float[] x, y, z;
    
}
```
And that's it! The compiler will automatically generate all the needed methods and fields.
