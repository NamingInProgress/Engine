# Contexts

The `Context` is one of the most critical objects in **vke**. It serves as your primary gateway to the engine's ecosystem, granting access to both [Services](./service.md) and [Identifiers](./ident.md).

Every `Context` is bound to a specific **namespace**. It uses this namespace to automatically resolve resource paths and scope engine services.

---

## The Context Hierarchy

There are two primary types of contexts you will interact with:

### The Global Context (`VkEngine`)
The core engine instance itself is a `Context` bound to the global `"vke"` namespace.

### Module Contexts
When your custom module or addon is loaded, the engine provides you with your own localized `Context` bound to your module's namespace.

> **Best Practice:** Always use your module's specific `Context` rather than the global `VkEngine` instance whenever possible. This ensures proper resource scoping and prevents namespace collisions with other modules.

---

## Namespaced Resource Resolution

When you request an `Identifier` from a `Context`, the context uses its assigned namespace to resolve the path. If you provide a path *without* an explicit namespace, the context automatically prepends its own.

Here is how different contexts resolve identical string queries:

```java
VkEngine engine = ...;        // Bound to namespace: "vke"
Context testContext = ...;    // Bound to namespace: "test"

// 1. Implicitly scoped to the engine
Identifier a = engine.id("hello");           // Result -> "vke:hello"

// 2. Explicitly scoped (bypassing the engine's default namespace)
Identifier b = engine.id("test:hello");      // Result -> "test:hello"

// 3. Implicitly scoped to your module context
Identifier c = testContext.id("hello");     // Result -> "test:hello"

// 4. Explicitly scoped (bypassing the module's namespace to get an engine asset)
Identifier d = testContext.id("vke:hello");  // Result -> "vke:hello"