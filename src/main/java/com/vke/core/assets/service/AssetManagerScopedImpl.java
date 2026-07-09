package com.vke.core.assets.service;

import com.vke.api.assets.*;
import com.vke.api.parsing.config.ConfigDocument;
import com.vke.api.parsing.config.ConfigParser;
import com.vke.api.parsing.config.node.ConfigArrayNode;
import com.vke.api.parsing.config.node.ConfigNode;
import com.vke.core.Context;
import com.vke.core.assets.BundleCollector;
import com.vke.core.assets.pipeline.AssetPipeline;
import com.vke.core.assets.pipeline.AssetPipelinePhase;
import com.vke.core.assets.AssetException;
import com.vke.core.assets.pipeline.PipelineContext;
import com.vke.core.parsing.config.xml.XmlParser;
import com.vke.utils.Utils;
import com.vke.utils.io.Identifier;
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
            context.throwException(new AssetException("No assets.xml specified!"), "Init AssetManager");
        }
        Bundle globalBundle = BundleCollector.collectGlobalBundle(context, pipeline);
        base.globalBundle.extendBundle(globalBundle);
        globalBundle.preloadAll(base.getCallbacks());

        Map<String, Bundle> bundleMap = BundleCollector.collectBundles(context, pipeline);
        base.mergeBundles(bundleMap);
    }

    private void initPipeline() {
        Identifier assetsXMLIdent = context.id("assets/assets.xml");
        if (assetsXMLIdent.existsFile()) {
            try {
                ConfigDocument document = parseXml(assetsXMLIdent);
                ConfigArrayNode assetsNode = document.getRoot().getArray("assets");
                //i will probably validate the assets.xml against a schema so thats probably fine here lmao

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

    public static ConfigDocument parseXml(Identifier identifier) throws IOException, ConfigParser.ConfigParseException {
        //yes i hardcode this to xml here, go cry somewhere
        ConfigParser parser = new XmlParser();
        char[] source = Utils.readCharsFromInputStream(identifier.asInputStream());
        parser.setSource(source);
        return parser.parse(ConfigParser.PARSE_LITERALS | ConfigParser.ATTRIBS_TO_FIELDS);
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
