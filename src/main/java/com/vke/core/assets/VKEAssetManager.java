package com.vke.core.assets;

import com.vke.api.assets.AssetHandle;
import com.vke.api.assets.AssetManager;
import com.vke.api.assets.Bundle;
import com.vke.core.assets.pipeline.AssetPipeline;
import com.vke.core.assets.pipeline.PipelineContext;
import com.vke.api.parsing.config.ConfigDocument;
import com.vke.api.parsing.config.ConfigParser;
import com.vke.api.parsing.config.node.ConfigNode;
import com.vke.api.services.Service;
import com.vke.core.VKEngine;
import com.vke.core.event.events.assets.BundleSwapEvent;
import com.vke.core.parsing.config.xml.XmlParser;
import com.vke.core.services.Services;
import com.vke.utils.io.Disposable;
import com.vke.utils.io.Identifier;
import com.vke.utils.Utils;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.List;

public class VKEAssetManager extends Service implements AssetManager {
    private final VKEngine mEngine;
    private final PipelineContext pipelineContext;
    private AssetPipeline pipeline;

    private Bundle mLoadedBundle;
    private Bundle mGlobalBundleWhichImplementsAssetManager;
    private HashMap<Identifier, Bundle> mAllBundles;

    private boolean ready;

    public VKEAssetManager(VKEngine engine) {
        super(Services.ASSET_MANAGER);
        this.mEngine = engine;
        this.mAllBundles = new HashMap<>();
        this.pipelineContext = new PipelineContext(engine);
        this.ready = false;
    }

    private void checkReady() {
        if (!ready) {
            throw new AssetManagerNotInitializedException();
        }
    }

    public void swapBundle(Identifier bundle) {
        Bundle b = mAllBundles.get(bundle);
        if (b == null) {
            mEngine.throwException(new FileNotFoundException(String.format("Bundle '%s' does not exist! Please check your input.", bundle)), "CAssetManager");
        }
        if (!mEngine.EVENT_BUS.fire(new BundleSwapEvent(this.mLoadedBundle, b))) {
            return;
        }
        if (this.mLoadedBundle != null) {
            this.mLoadedBundle.free();
        }
        this.mLoadedBundle = b;
    }

    public void swapBundle(String id) {
        swapBundle(mEngine.id(id));
    }

    public void addBundle(Identifier id, Bundle bundle) {
        this.mAllBundles.put(id, bundle);
    }

    public void unloadBundle() {
        if (this.mLoadedBundle != null) {
            this.mLoadedBundle.free();
        }
        this.mLoadedBundle = null;
    }

    @Override
    public PipelineContext getPipelineContext() {
        return pipelineContext;
    }

    @Override
    public void initialize() {
        initPipeline();
        this.mGlobalBundleWhichImplementsAssetManager = AssetUtils.collectGlobalBundles(mEngine, pipeline);
        AssetUtils.collectBundles(mEngine, this, pipeline);
        this.ready = true;
    }

    private void initPipeline() {
        Identifier assetsXMLIdent = new Identifier(mEngine.getAppNamespace(), "assets/assets.xml");
        if (assetsXMLIdent.existsFile()) {
            try {
                //yes i hardcode this to xml here, go cry somewhere
                ConfigParser parser = new XmlParser();
                char[] source = Utils.readCharsFromInputStream(assetsXMLIdent.asInputStream());
                parser.setSource(source);
                ConfigDocument document = parser.parse(ConfigParser.PARSE_LITERALS | ConfigParser.ATTRIBS_TO_FIELDS);
                ConfigNode assetsNode = document.getRoot().asObject().getNode("assets");
                //every xml node is an array so were chilling
                this.pipeline = new AssetPipeline(assetsNode.asArray(), pipelineContext);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public <T> AssetHandle<T> getAsset(Identifier id) {
        checkReady();
        AssetHandle<T> tried = mLoadedBundle == null ? null : mLoadedBundle.getAsset(id);
        if (tried == null) {
            tried = mGlobalBundleWhichImplementsAssetManager.getAsset(id);
        }
        return tried;
    }

    @Override
    public <T> AssetHandle<T> getAsset(String path) {
        return getAsset(mEngine.id(path));
    }

    @Override
    protected List<String> dependencies() {
        return List.of();
    }

    @Override
    public void free() {
        mGlobalBundleWhichImplementsAssetManager.free();
        mAllBundles.values().forEach(Disposable::free);
    }
}
