# Services

**vke** is a highly modular game engine that provides almost all of its core functionality through **Services**.

While the engine ships with default implementations for every service, the system is designed to be completely flexible. You can swap in custom implementations at any time—and for some services, you can even swap them dynamically at runtime.

---

## Service Types

Services are divided into three core interfaces depending on their lifecycle and state requirements:

### `Service`
A standard service whose implementation **can be swapped at runtime**. Any active references in the engine will automatically adjust when the implementation changes.

### `PinnedService`
A strict service that **cannot change its implementation** after the engine's initial startup phase. This is reserved for critical, low-level systems.

### `StatefulService<S>`
A specialized service that holds a transfer state `S`. When a new implementation replaces the old one, this state is passed along to initialize the new instance.

> *Note: Stateful services are handled internally by **vke** and are generally not important for the average developer.*

---

## Core Engine Services

These are some of the primary services driving the engine:

| Service        | Type            | Description                                               |
|:---------------|:----------------|:----------------------------------------------------------|
| `Renderer`     | `PinnedService` | The primary Vulkan rendering pipeline.                    |
| `InputManager` | `Service`       | Handles keyboard, mouse, and controller inputs.           |
| `AssetManager` | `PinnedService` | Manages loading, caching, and unloading of engine assets. |

Every service is registered under a unique identifier. All official engine service IDs are stored as constants in the `Services` class.

---

## Working with Services

### 1. Retrieving a Service
You can obtain a service instance from any [`Context`](./context.md) by passing its registered ID:


```java
Context ctx = ...;
AssetManager assetManager = ctx.service(Services.ASSET_MANAGER);

```

### 2. Context-Linked Features & Implicit Namespacing

The Java instance returned by `ctx.service(...)` remains permanently linked to the `Context` that created it. This enables powerful Quality-of-Life (QoL) behaviors like **automatic namespacing**.

If your active context has the namespace `"test"`, the engine will automatically prepend it to relative resource lookups:

```java
// Because the context namespace is "test", this implicitly resolves to "test:textures/test.png"
AssetHandle<Texture> texture = assetManager.getAsset("textures/test.png");

```

If you need to bypass the context's default namespace to grab a global engine resource, simply provide an explicit namespace:

```java
// Explicitly targets the core engine namespace instead of "test"
AssetHandle<Texture> missingTexture = assetManager.getAsset("vke:textures/missing.png");

```

### 3. Dynamic Hot-Swapping & Caching

Because of how the service architecture is designed, you do not need to worry about stale references. If a `Service` implementation is changed at runtime, any cached instances or references held by your code will automatically redirect to the new implementation behind the scenes.
