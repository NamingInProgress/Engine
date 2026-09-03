package com.vke.core.assets.pipeline;

import com.vke.api.assets.anot.Cache;
import com.vke.api.assets.anot.Converter;
import com.vke.api.assets.anot.Processor;
import com.vke.api.assets.anot.Protocol;
import com.vke.api.logger.Logger;
import com.vke.core.Context;
import com.vke.core.ContextWrapper;
import com.vke.core.assets.AssetException;
import com.vke.core.assets.pipeline.apis.AssetCache;
import com.vke.core.assets.pipeline.apis.AssetConverter;
import com.vke.core.assets.pipeline.apis.AssetProcessor;
import com.vke.core.assets.pipeline.apis.AssetProtocol;
import com.vke.core.assets.pipeline.converters.*;
import com.vke.core.assets.pipeline.converters.audio.preload.WavAudioPreloadConverter;
import com.vke.core.assets.pipeline.converters.pipeline.ConfigComputePipelineConverter;
import com.vke.core.assets.pipeline.converters.pipeline.ConfigRenderPipelineConverter;
import com.vke.core.assets.pipeline.protocols.*;
import com.vke.core.assets.pipeline.protocols.audio.AudioPreloadProtocol;
import com.vke.core.assets.pipeline.protocols.audio.WavPreloadProtocol;
import com.vke.core.assets.pipeline.protocols.mesh.MeshprefabProtocol;
import com.vke.core.assets.pipeline.protocols.mesh.ObjProtocol;
import com.vke.core.assets.pipeline.protocols.meta.FileProtocol;
import com.vke.core.assets.pipeline.protocols.meta.MetaProtocol;
import com.vke.core.assets.pipeline.protocols.pipeline.ComputePipelineProtocol;
import com.vke.core.assets.pipeline.protocols.pipeline.RenderPipelineProtocol;
import com.vke.core.assets.pipeline.protocols.shader.ComputeShaderProtocol;
import com.vke.core.assets.pipeline.protocols.shader.FragmentShaderProtocol;
import com.vke.core.assets.pipeline.protocols.shader.VertexShaderProtocol;
import com.vke.core.assets.pipeline.protocols.texture.PngProtocol;
import com.vke.core.assets.pipeline.protocols.texture.TextureProtocol;
import com.vke.core.assets.pipeline.stages.*;
import com.vke.api.parsing.config.node.ConfigNode;
import com.vke.core.logger.LoggerFactory;
import com.vke.core.mesh.MeshPrefabCache;
import pl.epsi.SearchAnnotation;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;

public class PipelineContext extends ContextWrapper {

    public static final Logger ASSET_PIPELINE_LOGGER = LoggerFactory.get("Asset Pipeline");

    @SearchAnnotation(target = Protocol.class)
    private static final List<Class<? extends AssetProtocol<?>>> PROTOCOLS = null;

    @SearchAnnotation(target = Cache.class)
    private static final List<Class<? extends AssetCache>> CACHES = null;

    @SearchAnnotation(target = Converter.class)
    private static final List<Class<? extends AssetConverter>> CONVERTERS = null;

    @SearchAnnotation(target = Processor.class)
    private static final List<Class<? extends AssetProcessor>> PROCESSORS = null;

    private final Context vkeContext;
    private final HashMap<String, StageFactory> registryRegistry;
    private final HashMap<String, AssetProtocol<?>> protocolRegistry;
    private final HashMap<String, AssetCache> cacheRegistry;
    private final HashMap<String, HashMap<String, AssetConverter>> converterRegistry;
    private final HashMap<String, AssetProcessor> processorRegistry;

    public PipelineContext(Context vkeContext) {
        super(vkeContext);
        this.vkeContext = vkeContext;
        this.registryRegistry = new HashMap<>();
        this.protocolRegistry = new HashMap<>();
        this.cacheRegistry = new HashMap<>();
        this.converterRegistry = new HashMap<>();
        this.processorRegistry = new HashMap<>();

        //i have to make a better system this class is awful

        //register engine default protocols
        registerProtocol(new MetaProtocol(vkeContext.getEngine()));
        try {
            registerProtocols();
            registerConverters();
            registerCacheHandlers();
            registerProcessors();
        } catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
            throw new RuntimeException("Make sure all asset handlers registered via annotations have a no-arg constructor!", e);
        }

        //register engine default stages
        registerStage(ConvertStage.STAGE, ConvertStage::new);
        registerStage(StageFilter.STAGE, StageFilter::new);
        registerStage(RenameStage.STAGE, RenameStage::new);
        registerStage(DecodeStage.STAGE, DecodeStage::new);
        registerStage(CacheAssetStage.STAGE, CacheAssetStage::new);
        registerStage(ProcessStage.STAGE, ProcessStage::new);
        registerStage(FilterElseStage.STAGE, FilterElseStage::new);
        registerStage(LogStage.STAGE, LogStage::new);
        registerStage(IncludeStage.STAGE, IncludeStage::new);
    }

    public void registerStage(String stageName, StageFactory factory) {
        this.registryRegistry.put(stageName, factory);
    }

    public void registerProtocol(AssetProtocol<?> protocol) {
        this.protocolRegistry.put(protocol.getProtocolName(), protocol);
    }

    @SuppressWarnings("all") // stfu intellij it gets ✨ COMPILE TIME COLLECTED ✨
    public void registerProtocols() throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        if (PROTOCOLS == null) return;
        for (Class<? extends AssetProtocol<?>> protocol : PROTOCOLS) {
            AssetProtocol<?> prot = protocol.getDeclaredConstructor().newInstance();
            ASSET_PIPELINE_LOGGER.trace("Registered protocol: " + prot.getProtocolName());
            registerProtocol(prot);
        }
    }

    public void registerConverter(AssetConverter converter) {
        HashMap<String, AssetConverter> second = converterRegistry.computeIfAbsent(converter.from(), s -> new HashMap<>());
        second.put(converter.to(), converter);
    }

    @SuppressWarnings("all") // stfu intellij it gets ✨ COMPILE TIME COLLECTED ✨
    public void registerConverters() throws NoSuchMethodException, InvocationTargetException,
            InstantiationException, IllegalAccessException {
        if (CONVERTERS == null) return;
        for (Class<? extends AssetConverter> converter : CONVERTERS) {
            registerConverter(converter.getDeclaredConstructor().newInstance());
        }
    }

    public void registerCacheHandler(AssetCache cacheHandler) {
        this.cacheRegistry.put(cacheHandler.getTargetProtocol(), cacheHandler);
    }

    @SuppressWarnings("all") // stfu intellij it gets ✨ COMPILE TIME COLLECTED ✨
    public void registerCacheHandlers() throws NoSuchMethodException, InvocationTargetException,
            InstantiationException, IllegalAccessException {
        if (CACHES == null) return;
        for (Class<? extends AssetCache> cache : CACHES) {
            registerCacheHandler(cache.getDeclaredConstructor().newInstance());
        }
    }

    public void registerProcessor(AssetProcessor processor) {
        this.processorRegistry.put(processor.getName(), processor);
    }

    @SuppressWarnings("all") // stfu intellij it gets ✨ COMPILE TIME COLLECTED ✨
    public void registerProcessors() throws NoSuchMethodException, InvocationTargetException,
            InstantiationException, IllegalAccessException {
        if (PROCESSORS == null) return;
        for (Class<? extends AssetProcessor> processor : PROCESSORS) {
            registerProcessor(processor.getDeclaredConstructor().newInstance());
        }
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

    public AssetProcessor getProcessor(String name) {
        return processorRegistry.get(name);
    }
}
