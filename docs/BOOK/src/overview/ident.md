# Identifiers

Identifiers are the primary key used to locate and reference assets, namespaces, and paths within vke.

An identifier consists of two parts: a **namespace** and a **path**, separated by a colon:
`vke:example/something`

* **Namespace:** `vke` — Represents the origin or owner of the resource (e.g., the engine itself, or a specific mod/addon).
* **Path:** `example/something` — Points to the specific resource.

> **Note:** The path component does not strictly need to be a valid file system path, though it usually points to one.

---

## File & Directory I/O

When an `Identifier` is used to target a file or directory, you can use various built-in I/O methods directly on the object:

| Method            | Return Type        | Description                                                              |
|:------------------|:-------------------|:-------------------------------------------------------------------------|
| `asInputStream()` | `InputStream`      | Returns an input stream to read the target file.                         |
| `walkFiles()`     | `Iter<Identifier>` | Returns a recursive iterator over **all** files in the target directory. |

---

## Creating Identifiers

Identifiers should **not** be instantiated manually. Use the `id` method on any Namespace-like object.
For example a [Context](./context.md) works here:

```java
// Example usage
Context ctx = ...;
Identifier myId = ctx.id("vke:example/something");