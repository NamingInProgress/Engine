package com.vke.core.assets.service;

import com.vke.api.assets.*;
import com.vke.api.parsing.config.ConfigDocument;
import com.vke.api.parsing.config.ConfigParser;
import com.vke.api.parsing.config.node.ConfigArrayNode;
import com.vke.api.parsing.config.node.ConfigNode;
import com.vke.core.Context;
import com.vke.core.FileIdentifier;
import com.vke.core.Identifier;
import com.vke.core.assets.BundleCollector;
import com.vke.core.assets.pipeline.AssetPipeline;
import com.vke.core.assets.pipeline.AssetPipelinePhase;
import com.vke.core.assets.AssetException;
import com.vke.core.assets.pipeline.PipelineContext;
import com.vke.core.parsing.config.xml.XmlParser;
import com.vke.core.services2.Services;
import com.vke.utils.Utils;
import com.vke.utils.iter.Iter;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class AssetManagerScopedImpl implements AssetManager {
    private boolean init;
    private final AssetManagerBaseImpl base;
    private final Context context;
    private AssetPipeline pipeline;

    AssetManagerScopedImpl(AssetManagerBaseImpl base, Context context) {
        this.base = base;
        this.context = context;
    }

    @Override
    public PipelineContext getPipelineContext() {
        return base.getPipelineContext();
    }

    @Override
    public void initAssets() {
        if (init) return;
        init = true;
        initPipeline();
        if (pipeline == null) {
            context.throwException(new AssetException("No assets.vcl specified!"), "Init AssetManager");
        }
        Bundle globalBundle = BundleCollector.collectGlobalBundle(context, pipeline, afterPhase -> {
            base.globalBundle.extendBundle(afterPhase);
        });
        base.globalBundle.extendBundle(globalBundle);

        // ===== IMPORTANT ====== DO NOT REMOVE OR MOVE ======
        //i cant do anything about this, after here the renderer will load shaders twice becuase it will get
        //initialized by the shaders load code which in turn ac1quires the same shader again during pipeline creation
        //before here shit wont exist cuz the pseudo pass hasnt completed yet. that here is literally the only spot
        //that we can initalize the renderer in without any problems.
        context.service(Services.RENDERER);

        globalBundle.preloadAll(base.getCallbacks());

        Map<String, Bundle> bundleMap = BundleCollector.collectBundles(context, pipeline);
        base.mergeBundles(bundleMap);
    }

    private void initPipeline() {
        FileIdentifier assetsXMLIdent = context.fid("assets/assets.vcl");
        if (assetsXMLIdent.existsFile()) {
            try {
                ConfigDocument document = ConfigDocument.parseIdentifier(assetsXMLIdent);
                ConfigArrayNode assetsNode = document.getRoot().getArray("assets");
                //i will probably validate the assets.vcl against a schema so thats probably fine here lmao

                AssetPipeline pipeline = new AssetPipeline(base.getPipelineContext());
                for (ConfigNode phaseNode : assetsNode.values()) {
                    String phaseName = phaseNode.getString("name");
                    //every xml node is an array so were chilling
                    AssetPipelinePhase phase = new AssetPipelinePhase(phaseName, phaseNode.asArray(), base.getPipelineContext());
                    pipeline.addPhase(phase);
                }

                this.pipeline = pipeline;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public <T> AssetHandle<T> getAsset(Identifier id) {
        return base.getAsset(id);
    }

    @Override
    public <T> AssetHandle<T> getAsset(String path) {
        return base.getAsset(context.id(path));
    }

    @Override
    public Iter<AssetHandle<?>> allAssets() {
        return base.allAssets();
    }

    @Override
    public Iter<AssetHandle<?>> allCurrentlyLoadedAssets() {
        return base.allCurrentlyLoadedAssets();
    }

    @Override
    public BundleExchange beginExchange() {
        return base.beginExchange();
    }

    public String getAssetProtocol(String id) {
        return getAssetProtocol(context.id(id));
    }

    public void registerLoadCallback(BundleLoadingCallback callback) {
        base.registerLoadCallback(callback);
    }

    public void removeLoadCallback(BundleLoadingCallback callback) {
        base.removeLoadCallback(callback);
    }

    @Override
    public String getId() {
        return base.getId();
    }

    @Override
    public List<String> dependencies() {
        return base.dependencies();
    }

    @Override
    public void free() {

    }
}
