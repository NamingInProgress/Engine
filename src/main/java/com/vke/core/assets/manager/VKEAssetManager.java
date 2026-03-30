package com.vke.core.assets.manager;

import com.vke.api.assets.*;
import com.vke.api.parsing.config.ConfigDocument;
import com.vke.api.parsing.config.ConfigParser;
import com.vke.api.parsing.config.node.ConfigNode;
import com.vke.core.Context;
import com.vke.core.assets.BundleCollector;
import com.vke.core.assets.pipeline.AssetPipeline;
import com.vke.core.assets.AssetException;
import com.vke.core.assets.pipeline.PipelineContext;
import com.vke.core.parsing.config.xml.XmlParser;
import com.vke.utils.Utils;
import com.vke.utils.io.Identifier;

import java.util.Map;

public class VKEAssetManager implements AssetManager {
    private boolean init;
    private final VKEAssetManagerService base;
    private final Context context;
    private AssetPipeline pipeline;

    VKEAssetManager(VKEAssetManagerService base, Context context) {
        this.base = base;
        this.context = context;
    }

    @Override
    public PipelineContext getPipelineContext() {
        return base.getPipelineContext();
    }

    @Override
    public void initialize() {
        if (init) return;
        init = true;
        initPipeline();
        if (pipeline == null) {
            context.throwException(new AssetException("No assets.xml specified!"), "Init AssetManager");
        }
        Bundle globalBundle = BundleCollector.collectGlobalBundle(context, pipeline);
        base.globalBundle.extendBundle(globalBundle);

        Map<String, Bundle> bundleMap = BundleCollector.collectBundles(context, pipeline);
        base.mergeBundles(bundleMap);
    }

    private void initPipeline() {
        Identifier assetsXMLIdent = context.id("assets/assets.xml");
        if (assetsXMLIdent.existsFile()) {
            try {
                //yes i hardcode this to xml here, go cry somewhere
                ConfigParser parser = new XmlParser();
                char[] source = Utils.readCharsFromInputStream(assetsXMLIdent.asInputStream());
                parser.setSource(source);
                ConfigDocument document = parser.parse(ConfigParser.PARSE_LITERALS | ConfigParser.ATTRIBS_TO_FIELDS);
                ConfigNode assetsNode = document.getRoot().asObject().getNode("assets");
                //every xml node is an array so were chilling
                this.pipeline = new AssetPipeline(assetsNode.asArray(), base.getPipelineContext());
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
}
