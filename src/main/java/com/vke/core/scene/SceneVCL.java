package com.vke.core.scene;

import com.vke.api.parsing.config.ConfigDocument;
import com.vke.api.parsing.config.node.ConfigArrayNode;
import com.vke.api.parsing.config.node.ConfigNode;
import com.vke.api.scene.SceneException;
import com.vke.core.Context;
import com.vke.utils.io.FileUtils;
import com.vke.utils.io.Identifier;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SceneVCL {
    public final Identifier name;
    public final Class<?> clazz;
    public final List<String> bundles;
    public final Identifier loadingScene;
    public final Identifier renderGraph;
    public final ConfigNode config;

    public SceneVCL(Identifier file, Context context) throws SceneException {
        try {
            String filename = FileUtils.getFileNickname(file.toPath());
            this.name = new Identifier(file.getNamespace(), filename);

            ConfigDocument document = ConfigDocument.parseIdentifier(file);
            ConfigNode root = document.getRoot();
            ConfigNode sceneTag = root.getObject("scene");

            if (sceneTag.hasField("config")) {
                this.config = sceneTag.getObject("config");
            } else {
                this.config = null;
            }

            ConfigNode classTag = sceneTag.getObject("class");
            String className = classTag.getString("name");
            this.clazz = Class.forName(className, false, getClass().getClassLoader());

            var rgn = sceneTag.getObjectOption("render-graph").unwrapOrPanic(new IllegalStateException("Could not find render-graph on scene config " + name));
            this.renderGraph = context.id(rgn.getString("name"));

            ConfigNode loadingSceneTag = sceneTag.getObject("loading-scene");
            if (loadingSceneTag != null) {
                String literal = loadingSceneTag.getString("name");
                this.loadingScene = context.id(literal);
            } else {
                this.loadingScene = null;
            }

            ConfigArrayNode bundlesTag = sceneTag.getArray("bundles");
            if (bundlesTag != null) {
                this.bundles = new ArrayList<>(bundlesTag.values().length);
                for (ConfigNode bundleTag : bundlesTag.values()) {
                    String bundleName = bundleTag.getString("name");
                    this.bundles.add(bundleName);
                }
            } else {
                this.bundles = List.of();
            }
        } catch (IOException | ClassNotFoundException e) {
            throw new SceneException(e);
        }
    }
}
