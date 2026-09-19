package com.vke.core.rendering.graph2.parse;

import com.vke.core.Identifier;
import com.vke.core.rendering.graph2.RenderGraph;
import com.vke.core.rendering.graph2.renderpass.RenderPass;
import com.vke.utils.iter.Iter;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

public class RenderPassReconstructor {
    private final HashMap<Identifier, RenderPass.Def> renderPassLookup;
    private final HashMap<Identifier, HashSet<RenderPass.Def>> waiting;
    private final RenderPassReconstructor parent;
    //its fine that this isnt a list because we will be parsing graphs one after another so it wont ever need an old child
    //actually fuck i do bro i hate ts
    //alright it wasnt that bad
    private final LinkedList<WeakReference<RenderPassReconstructor>> children;

    public RenderPassReconstructor() {
        this.renderPassLookup = new HashMap<>();
        this.waiting = new HashMap<>();
        this.parent = null;
        this.children = new LinkedList<>();
    }

    public RenderPassReconstructor(RenderPassReconstructor parent) {
        this.renderPassLookup = new HashMap<>();
        this.waiting = new HashMap<>();
        this.parent = parent;
        this.children = new LinkedList<>();
        parent.children.add(new WeakReference<>(this));
    }

    public Iter<RenderPass.Def> iterDefs(RenderGraph.Def graphDef) {
        waiting.forEach((waitingFor, w) -> {
            for (RenderPass.Def def : w) {
                RenderPass.LOGGER.error("The RenderPass '%s' is waiting for its parent '%s' to appear, but that parent doesnt exist! The enclosing graph is: '%s'", def.name(), waitingFor, graphDef.name());
            }
        });

        return Iter.of(renderPassLookup.values());
    }

    public RenderPass.Def findRenderPassByName(Identifier name) {
        return renderPassLookup.get(name);
    }

    public void onRenderPassParsed(RenderPass.Def def) {
        onRenderPassParsed(def, null);
    }

    public void onRenderPassParsed(RenderPass.Def def, @Nullable RenderPass.Def parent) {
        handleDef(def, parent);
        for (var child : children) {
            RenderPassReconstructor unwrapped = child.get();
            if (unwrapped != null) {
                unwrapped.checkWaiting(def);
            }
        }
    }

    private void handleDef(RenderPass.Def def, @Nullable RenderPass.Def parent) {
        RenderPass.Def p = parent;
        Identifier parentId = def.parent();
        if (p == null && parentId != null) {
            p = renderPassLookup.get(parentId);
            if (p == null && this.parent != null) {
                p = this.parent.renderPassLookup.get(parentId);
            }
        }

        RenderPass.Def finalDef = def;
        if (parentId != null) {
            if (p == null) {
                waiting.computeIfAbsent(parentId, _ -> new HashSet<>()).add(def);
                return;
            }

            finalDef = def.extend(p);
        }

        renderPassLookup.put(finalDef.name(), finalDef);
        checkWaiting(finalDef);
    }

    private void checkWaiting(RenderPass.Def def) {
        Identifier name = def.name();
        var waiting = this.waiting.remove(name);
        if (waiting != null) {
            for (RenderPass.Def waited : waiting) {
                handleDef(waited, def);
            }
        }
    }
}
