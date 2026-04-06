package com.vke.test.shader;

import com.vke.api.assets.AssetHandle;
import com.vke.api.assets.r.R;
import com.vke.api.rendering.abstraction.shader.Shader;
import com.vke.api.window.WindowCreateInfo;
import com.vke.core.EngineCreateInfo;
import com.vke.core.VKEngine;

import java.io.IOException;

public class ShaderLoadTest {
    public static void main(String[] args) throws IOException {
        EngineCreateInfo createInfo = new EngineCreateInfo("idfk", "vke");
        createInfo.releaseMode = false;
        createInfo.windowCreateInfo = new WindowCreateInfo("My Window");

        VKEngine engine = new VKEngine(createInfo);

        AssetHandle<Shader> testShader = R.shaders.get("shader.frag.hello");
        System.out.println(testShader.acquire(engine));
    }
}
