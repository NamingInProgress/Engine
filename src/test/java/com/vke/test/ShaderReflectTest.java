package com.vke.test;

import com.vke.api.rendering.abstraction.enums.ShaderType;
import com.vke.api.app.App;
import com.vke.api.window.WindowCreateInfo;
import com.vke.core.EngineCreateInfo;
import com.vke.core.VKEngine;
import com.vke.core.rendering.draw.DrawContext;
import com.vke.core.services.Services;
import com.vke.core.vulkan.VulkanRenderer;
import com.vke.core.vulkan.shader.ShaderCompiler;
import com.vke.core.window.Window;
import com.vke.utils.Utils;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.spvc.Spv;
import org.lwjgl.util.spvc.Spvc;
import org.lwjgl.util.spvc.SpvcReflectedResource;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.system.MemoryUtil.*;

public class ShaderReflectTest {

    public static void main(String[] args) throws Exception {
        EngineCreateInfo createInfo = new EngineCreateInfo("idfk", "vke");
        createInfo.releaseMode = false;
        createInfo.windowCreateInfo = new WindowCreateInfo("My Window");

        VKEngine engine = new VKEngine(createInfo);
        engine.start(new App() {
            @Override
            public void onInit(VKEngine engine) {
                VulkanRenderer renderer = engine.service(Services.VULKAN_RENDERER);
                ShaderCompiler sc = engine.service(Services.SHADER_COMPILER);

                byte[] bytes;
                ByteBuffer spv;

                try {
                    bytes = Utils.readAllBytesAndClose(engine.id("shaders/test.vsh").asInputStream());
                    spv = sc.compileGlslToSpirV(bytes, ShaderType.VERTEX, engine.id("shaders/test.vsh"));
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

                try (MemoryStack stack = MemoryStack.stackPush()) {
                    PointerBuffer pContext = stack.mallocPointer(1);

                    int err = Spvc.spvc_context_create(pContext);
                    if (err != Spvc.SPVC_SUCCESS) {
                        throw new RuntimeException("Failed to create SPIRV-Cross context");
                    }

                    long context = pContext.get(0);

                    Spvc.spvc_context_set_error_callback(
                            context,
                            (userData, error) -> System.err.println("SPIRV-Cross error: " + memUTF8(error)),
                            0
                    );

                    PointerBuffer pIr = stack.mallocPointer(1);
                    Spvc.spvc_context_parse_spirv(
                            context,
                            spv.asIntBuffer(),
                            spv.asIntBuffer().remaining(),
                            pIr
                    );
                    long ir = pIr.get(0);

                    PointerBuffer pCompiler = stack.mallocPointer(1);
                    Spvc.spvc_context_create_compiler(
                            context,
                            Spvc.SPVC_BACKEND_NONE, // reflection only
                            ir,
                            Spvc.SPVC_CAPTURE_MODE_TAKE_OWNERSHIP,
                            pCompiler
                    );
                    long compiler = pCompiler.get(0);

                    // ---- Get shader resources ----
                    PointerBuffer pResources = stack.mallocPointer(1);
                    Spvc.spvc_compiler_create_shader_resources(compiler, pResources);
                    long resources = pResources.get(0);

                    PointerBuffer pResourceList = stack.mallocPointer(1);
                    PointerBuffer pResourceCount = stack.mallocPointer(1);

                    Spvc.spvc_resources_get_resource_list_for_type(resources, Spvc.SPVC_RESOURCE_TYPE_UNIFORM_BUFFER, pResourceList, pResourceCount);

                    SpvcReflectedResource.Buffer resourcesBuffer = SpvcReflectedResource.createSafe(pResourceList.get(0), (int) pResourceCount.get(0));

                    for (int i = 0; i < pResourceCount.get(0); i++) {
                        SpvcReflectedResource resource = resourcesBuffer.get(i);

                        int set = Spvc.spvc_compiler_get_decoration(compiler, resource.id(), Spv.SpvDecorationDescriptorSet);
                        int binding = Spvc.spvc_compiler_get_decoration(compiler, resource.id(), Spv.SpvDecorationBinding);

                        // Block type name
                        String blockName = Spvc.spvc_compiler_get_name(compiler, resource.base_type_id());

                        // Variable name (what you want)
                        String varName = Spvc.spvc_compiler_get_name(compiler, resource.id());

                        System.out.println("Uniform: " + resource.nameString());
                        System.out.println("BASE TYPE NAME: " + blockName);
                        System.out.println("ID TYPE NAME: " + varName);
                        System.out.println("ID: " + resource.id());
                        System.out.println("Set = " + set);
                        System.out.println("Binding = " + binding);
                        System.out.println("Struct: ");

                        long typeHandle = Spvc.spvc_compiler_get_type_handle(compiler, resource.base_type_id());
                        int memberCount = Spvc.spvc_type_get_num_member_types(typeHandle);

                        int nArrayDim = Spvc.spvc_type_get_num_array_dimensions(typeHandle);
                        System.out.println("num array dim: " + nArrayDim);
                        System.out.println("array dimension: " + Spvc.spvc_type_get_array_dimension(typeHandle, 1));


                        for (int j = 0; j < memberCount; j++) {
                            String memberName = Spvc.spvc_compiler_get_member_name(compiler, resource.base_type_id(), j);
                            int memberTypeId = Spvc.spvc_type_get_member_type(typeHandle, j);
                            IntBuffer buf = stack.mallocInt(1);
                            Spvc.spvc_compiler_type_struct_member_offset(compiler, typeHandle, j, buf);
                            PointerBuffer pSize = stack.mallocPointer(1);
                            Spvc.spvc_compiler_get_declared_struct_member_size(compiler, typeHandle, j, pSize);
                            PointerBuffer pFullSize = stack.mallocPointer(1);
                            Spvc.spvc_compiler_get_declared_struct_size(compiler, typeHandle, pFullSize);
                            // Use spvc_compiler_get_type(compiler, memberTypeId) to inspect member type
                            System.out.println("Member: " + memberName + " (" + memberTypeId + ")" + " (" + buf.get(0) + ")" + " size: " + pSize.get(0) + ", full size: " + pFullSize.get(0));
                        }
                    }

                    Spvc.spvc_context_destroy(context);
                }
            }



            @Override
            public void onDraw(DrawContext ctx) {

            }

            @Override
            public String getName() {
                return "vke";
            }

            @Override
            public void free() {}
        });


    }

}
