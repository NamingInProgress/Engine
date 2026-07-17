package com.vke.core.assets.pipeline;

import com.vke.core.assets.AssetException;
import com.vke.utils.functionalinterface.FaultyConsumer;

import java.util.ArrayList;

public class AssetPipeline {
    private final ArrayList<AssetPipelinePhase> phases;
    private final PipelineContext context;

    public AssetPipeline(PipelineContext context) {
        this.context = context;
        this.phases = new ArrayList<>();
    }

    public void addPhase(AssetPipelinePhase phase) {
        this.phases.add(phase);
    }

    public void forEachPhase(FaultyConsumer<AssetPipelinePhase, ? extends AssetException> action) throws AssetException {
        for (AssetPipelinePhase phase : phases) {
            action.accept(phase);
        }
    }

    public PipelineContext getContext() {
        return context;
    }
}
