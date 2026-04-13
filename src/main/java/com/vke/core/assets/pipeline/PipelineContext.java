package com.vke.core.assets.pipeline;

import com.vke.core.Context;
import com.vke.core.ContextWrapper;
import com.vke.core.assets.AssetException;
import com.vke.core.assets.pipeline.apis.AssetCache;
import com.vke.core.assets.pipeline.apis.AssetConverter;
import com.vke.core.assets.pipeline.apis.AssetProtocol;
import com.vke.core.assets.pipeline.converters.*;
import com.vke.core.assets.pipeline.converters.pipeline.ConfigComputePipelineConverter;
import com.vke.core.assets.pipeline.converters.pipeline.ConfigRenderPipelineConverter;
import com.vke.core.assets.pipeline.protocols.*;
import com.vke.core.assets.pipeline.protocols.pipeline.ComputePipelineProtocol;
import com.vke.core.assets.pipeline.protocols.pipeline.RenderPipelineProtocol;
import com.vke.core.assets.pipeline.protocols.shader.ComputeShaderProtocol;
import com.vke.core.assets.pipeline.protocols.shader.FragmentShaderProtocol;
import com.vke.core.assets.pipeline.protocols.shader.VertexShaderProtocol;
import com.vke.core.assets.pipeline.stages.*;
import com.vke.api.parsing.config.node.ConfigNode;
import com.vke.core.mesh.MeshPrefabCache;

import java.util.HashMap;

public class PipelineContext extends ContextWrapper {
    private final Context vkeContext;
    private final HashMap<String, StageFactory> registryRegistry;
    private final HashMap<String, AssetProtocol<?>> protocolRegistry;
    private final HashMap<String, AssetCache> cacheRegistry;
    private final HashMap<String, HashMap<String, AssetConverter>> converterRegistry;

    public PipelineContext(Context vkeContext) {
        super(vkeContext);
        this.vkeContext = vkeContext;
        this.registryRegistry = new HashMap<>();
        this.protocolRegistry = new HashMap<>();
        this.cacheRegistry = new HashMap<>();
        this.converterRegistry = new HashMap<>();

        //register engine default protocols
        registerProtocol(new FileProtocol());
        registerProtocol(new MetaProtocol(vkeContext.getEngine()));
        registerProtocol(new PlainProtocol());
        registerProtocol(new ConfigProtocol());
        registerProtocol(new LangProtocol());

        registerProtocol(new FragmentShaderProtocol());
        registerProtocol(new VertexShaderProtocol());
        registerProtocol(new ComputeShaderProtocol());

        registerProtocol(new RenderPipelineProtocol());
        registerProtocol(new ComputePipelineProtocol());

        registerProtocol(new PngTextureProtocol());

        registerProtocol(new ObjProtocol());
        registerProtocol(new MeshprefabProtocol());

        //register engine default stages
        registerStage(ConvertStage.STAGE, ConvertStage::new);
        registerStage(StageFilter.STAGE, StageFilter::new);
        registerStage(RenameStage.STAGE, RenameStage::new);
        registerStage(DecodeStage.STAGE, DecodeStage::new);
        registerStage(CacheAssetStage.STAGE, CacheAssetStage::new);

        //register converters
        registerConverter(new PlainPathConverter());
        registerConverter(new PlainConfigConverter());
        registerConverter(new ConfigLangConverter());
        registerConverter(new ConfigRenderPipelineConverter());
        registerConverter(new ConfigComputePipelineConverter());
        registerConverter(new ObjMeshprefabConverter());

        //register cache handlers
        registerCacheHandler(new MeshPrefabCache());
    }

    public void registerStage(String stageName, StageFactory factory) {
        this.registryRegistry.put(stageName, factory);
    }

    public void registerProtocol(AssetProtocol<?> protocol) {
        this.protocolRegistry.put(protocol.getProtocolName(), protocol);
    }

    public void registerConverter(AssetConverter converter) {
        HashMap<String, AssetConverter> second = converterRegistry.computeIfAbsent(converter.from(), s -> new HashMap<>());
        second.put(converter.to(), converter);
    }

    public void registerCacheHandler(AssetCache cacheHandler) {
        this.cacheRegistry.put(cacheHandler.getTargetProtocol(), cacheHandler);
    }

    public PipelineStage produceStage(String stageName, ConfigNode node) throws AssetException {
        StageFactory fac = registryRegistry.get(stageName);
        if (fac == null) throw AssetException.unknownStage(stageName);
        return fac.produce(node, this);
    }

    @SuppressWarnings("unchecked")
    public <T> AssetProtocol<T> getProtocol(String protocol) throws AssetException {
        AssetProtocol<?> res = protocolRegistry.get(protocol);
        if (res == null) throw AssetException.unknownProtocol(protocol);
        return (AssetProtocol<T>) res;
    }

    public AssetConverter getConverter(String fromName, String toName) {
        if (fromName != null && fromName.equals(toName)) return new IdentityConverter();
        HashMap<String, AssetConverter> second = converterRegistry.get(fromName);
        if (second == null) return null;
        return second.get(toName);
    }

    public AssetCache getCacheHandler(String protocol) {
        return cacheRegistry.get(protocol);
    }

    public Context context() {
        return vkeContext;
    }
}
