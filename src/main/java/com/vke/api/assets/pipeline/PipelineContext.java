package com.vke.api.assets.pipeline;

import com.vke.api.assets.pipeline.stages.ParseStage;
import com.vke.api.assets.pipeline.stages.ValidateStage;
import com.vke.api.parsing.config.node.ConfigNode;
import com.vke.core.VKEngine;
import com.vke.api.assets.pipeline.apis.ProtocolResolver;
import com.vke.core.assets.protocols.ConfigProtocolResolver;
import com.vke.core.assets.protocols.GlobalProtocolResolver;
import com.vke.api.assets.pipeline.stages.PipelineStage;
import com.vke.core.assets.protocols.PlainProtocolResolver;

import java.util.HashMap;

public class PipelineContext {
    private final HashMap<String, StageFactory> registryRegistry;
    private final HashMap<String, ProtocolResolver<?>> resolverRegistry;
    private final ProtocolResolver<?> globalResolver;

    public PipelineContext(VKEngine engine) {
        this.registryRegistry = new HashMap<>();
        this.resolverRegistry = new HashMap<>();
        this.globalResolver = new GlobalProtocolResolver(engine);

        //register engine default protocols
        registerProtocol("plain", new PlainProtocolResolver());
        registerProtocol("config", new ConfigProtocolResolver());

        //register engine default stages
        registerStage(ParseStage.STAGE, ParseStage::new);
        registerStage(ValidateStage.STAGE, ValidateStage::new);
        registerStage(StageFilter.STAGE, StageFilter::new);
    }

    public void registerStage(String stageName, StageFactory factory) {
        this.registryRegistry.put(stageName, factory);
    }

    public void registerProtocol(String protocol, ProtocolResolver<?> resolver) {
        this.resolverRegistry.put(protocol, resolver);
    }

    public PipelineStage produceStage(String stageName, ConfigNode node) throws AssetPipelineException {
        StageFactory fac = registryRegistry.get(stageName);
        if (fac == null) throw AssetPipelineException.unknownStage(stageName);
        return fac.produce(node, this);
    }

    @SuppressWarnings("unchecked")
    public <T> ProtocolResolver<T> getResolver(String protocol) throws AssetPipelineException {
        ProtocolResolver<?> res = resolverRegistry.get(protocol);
        if (res == null) throw AssetPipelineException.unknownProtocol(protocol);
        return (ProtocolResolver<T>) res;
    }

    public ProtocolResolver<?> getGlobalResolver() {
        return globalResolver;
    }
}
