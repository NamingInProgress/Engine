package com.vke.core.rendering.graph2;

import com.vke.api.parsing.config.node.ConfigArrayNode;
import com.vke.api.rendering.abstraction.renderer.RenderSystem;
import com.vke.api.rendering.abstraction.renderer.commands.CommandBuffer;
import com.vke.api.rendering.abstraction.renderer.data.Texture;
import com.vke.api.rendering.abstraction.renderer.enums.texture.Format;
import com.vke.api.scene.Scene;
import com.vke.api.window.Window;
import com.vke.core.Identifier;
import com.vke.core.rendering.graph2.parse.RenderPassReconstructor;
import com.vke.core.rendering.graph2.renderpass.DataRenderPass;
import com.vke.core.rendering.graph2.renderpass.RenderPass;
import com.vke.core.scene.loading.RectLoadingScene;
import com.vke.utils.io.SegmentedPath;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class RenderGraph {
    private final RenderSystem sys;
    private final Def def;

    private final RenderPassReconstructor reconstructor;
    private final ArrayList<RenderPass> renderPasses;

    private final TexturePool pool;

    private final GraphContext context;

    public RenderGraph(RenderSystem sys, Def def, TexturePool pool) {
        this.sys = sys;
        this.def = def;
        this.pool = pool;
        this.context = new GraphContext(sys);

        this.reconstructor = def.reconstructor;
        this.renderPasses = new ArrayList<>();
    }

    public void onLoad() {
        renderPasses.clear();

        var defs = reconstructor.iterDefs(def).collectToList();
        defs.sort(Comparator.comparingInt(RenderPass.Def::order));

        for (RenderPass.Def def : defs) {
            RenderPass renderPass = createInstance(def);
            if (renderPass != null) {
                renderPasses.add(renderPass);
            }
        }

        for (RenderPass renderPass : renderPasses) {
            renderPass.onLoad();
        }

        Window.Size size = sys.getEngine().getWindow().getSize();
        rebuild(size.width(), size.height());
    }

    private RenderPass createInstance(RenderPass.Def def) {
        String clazzName = def.clazz();
        if (clazzName == null) {
            RenderPass.LOGGER.fatal("The RenderPass '%s' in '%s' does NOT have a class assigned to it! Please use a parent with a class or specify it directly!", def.name(), this.def.name());
            return null;
        }
        Class<? extends  RenderPass> clazz;
        try {
            @SuppressWarnings("unchecked")
            var tmp = (Class<? extends RenderPass>) Class.forName(clazzName);
            clazz = tmp;
        } catch (ClassNotFoundException e) {
            RenderPass.LOGGER.fatal("The RenderPass '%s' in '%s' does have a class assigned to it, but this class is actually illegal and cant be found! Please check your spelling.", def.name(), this.def.name());
            return null;
        }

        try {
            RenderPass instance;
            if (DataRenderPass.class.isAssignableFrom(clazz)) {
                @SuppressWarnings("unchecked") //its literally being checked dumbass
                Class<? extends DataRenderPass> dataClass = (Class<? extends DataRenderPass>) clazz;
                Constructor<? extends DataRenderPass> constructor = dataClass.getDeclaredConstructor(RenderSystem.class, RenderGraph.class, RenderPass.Def.class, ConfigArrayNode.class);
                instance = constructor.newInstance(sys, this, def, def.data());
            } else {
                Constructor<? extends RenderPass> constructor = clazz.getDeclaredConstructor(RenderSystem.class, RenderGraph.class, RenderPass.Def.class);
                instance = constructor.newInstance(sys, this, def);
            }

            return instance;
        } catch (NoSuchMethodException e) {
            RenderPass.LOGGER.error("Class '%s' should have a constructor that is compatible with the spec!", clazz);
        } catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
            RenderPass.LOGGER.error("Class '%s' cannot be instantiated!", clazz);
        }

        return null;
    }

    public void releaseTexture(Texture tex) {
        pool.release(tex);
    }

    public void rebuild(int windowWidth, int windowHeight) {
        for (RenderPass renderPass : renderPasses) {
            renderPass.releaseTextures(this);
        }

        for (RenderPass renderPass : renderPasses) {
            renderPass.buildOutputTextures(windowWidth, windowHeight, this);
        }

        for (RenderPass renderPass : renderPasses) {
            renderPass.buildInputTextures(this);
        }
    }

    public Texture getTextureAllocation(int width, int height, RenderPass.TextureType type, Format format) {
        return pool.acquire(width, height, type, format);
    }

    public Texture[] getAllScreenTextures() {
        return pool.getAllRenderTargets();
    }

    public RenderPass searchRenderPass(Identifier name) {
        for (RenderPass renderPass : renderPasses) {
            if (renderPass.getDefinition().name().equals(name)) {
                return renderPass;
            }
        }
        throw new IllegalArgumentException("RenderPass '" + name + "' not found in RenderGraph '" + def.name + "'!");
    }

    public GraphTexture searchTexture(String textureFQL, TextureCategory cat) {
        return searchTexture(new SegmentedPath(textureFQL), cat);
    }

    public GraphTexture searchTexture(SegmentedPath textureFQL, TextureCategory cat) {
        String[] parts = textureFQL.getParts();
        String renderPassName = parts[0];
        String textureName = parts[1];

        RenderPass renderPass = searchRenderPass(sys.id(renderPassName));
        if (cat == TextureCategory.Input) {
            return renderPass.searchInputTexture(textureName);
        } else {
            return renderPass.searchOutputTexture(textureName);
        }
    }

    public GraphContext getContext() {
        return context;
    }

    public void onDraw(Scene runner) {
        CommandBuffer cmd = sys.getCurrentCommandBuffer();
        for (RenderPass pass : renderPasses) {
            pass.beforeExecute();
            pass.execute(cmd, context);
            runner.onRenderPassFinished(pass, context);
        }
        endRendering();
    }

    public void endRendering() {
        context.clear();
    }

    public Identifier getName() {
        return def.name();
    }

    public record Def(Identifier name, RenderPassReconstructor reconstructor) {}
}
