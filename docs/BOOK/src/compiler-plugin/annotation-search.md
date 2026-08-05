# VKECompilerPlugin
## Annotation Searching
The Annotation Search extension of the vke compiler plugin allows for compile-time
collection of classes annotated with a specified annotation. This serves to reduce the
usage of reflection, which while powerful can significantly slow down the startup time
of the application, especially in large projects such as **vke** at over 800 different classes, 
excluding libraries.

This is why, if runtime scanning is not a necessity, but hard coding classes is too tedious
you can resort to the annotation scanner, like for example the Asset Manager's [Pipeline Context](https://github.com/NamingInProgress/Engine/blob/ecs/src/main/java/com/vke/core/assets/pipeline/PipelineContext.java).

An example implementation would look like this:
```java
public @interface MyAnnotation {}
```

```java
@MyAnnotation
public class MyClass1 {...}
```

```java
@MyAnnotation
public class MyClass2 {...}
```

```java
import pl.epsi.SearchAnnotation;

public class MyClassCollector {
    @SearchAnnotation(target = MyAnnotation.class)
    private static final List<Class<?>> MY_CLASSES = null;
}
```

The vke compiler plugin will search through all classes and collect their annotations,
then it will replace the null-initializer with a `List.of()` call with every class it found along the way.

The field doesn't necessarily have to be static or final, however it usually is, as there is no reason
to later replace it. One thing to keep in mind, is that the original initializer **will** get replaced
at compile time, and it is **not safe** to assume the original value is still there after compilation.

This is an example of how the compiled code for `MyClassCollector` would look like:
```java
import pl.epsi.SearchAnnotation;

public class MyClassCollector {
    @SearchAnnotation(target = MyAnnotation.class)
    private static final List<Class<?>> MY_CLASSES = List.of(MyClass1.class, MyClass2.class);
}
```
