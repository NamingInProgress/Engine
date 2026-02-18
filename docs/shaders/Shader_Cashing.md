# Shader Caching
Shader compilation is an expensive operation that can significantly increase application startup time and cause runtime stutters if performed repeatedly. 
Shader caching avoids unnecessary recompilation by storing precompiled shader binaries and reusing them across runs, 
resulting in faster startup, more stable frame times, and a smoother development and user experience.

To ensure correctness and avoid using stale or incompatible shader binaries, the cache is invalidated and shaders are recompiled under the following conditions:

- ### Application version mismatch
  If the application version stored in the shader cache differs from the version of the application at runtime, all cached shaders are recompiled. 
  This guarantees that shader binaries always match the exact code and configuration they were built against.

- ### Non-release (debug) mode
  If EngineCreateInfo.releaseMode is set to false (for example, when running in debug mode), shader caching is bypassed and shaders are always recompiled. 
  This ensures that any changes to shader source code are immediately reflected without requiring manual cache invalidation.

This approach balances fast startup times in release builds with correctness and rapid iteration during development.