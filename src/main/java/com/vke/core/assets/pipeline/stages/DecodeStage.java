package com.vke.core.assets.pipeline.stages;

import com.vke.api.file.*;
import com.vke.api.parsing.config.node.ConfigNode;
import com.vke.core.assets.AssetException;
import com.vke.core.assets.pipeline.PipelineContext;
import com.vke.core.assets.pipeline.StageElement;
import com.vke.core.assets.pipeline.apis.AssetData;
import com.vke.utils.io.Identifier;

import java.io.InputStream;

public class DecodeStage extends ParameterizedStage {
    public static final String STAGE = "decode";
    private final PipelineContext context;
    private final String toName;
    private final String using;
    private AnyDecoder anyDecoder;

    public DecodeStage(ConfigNode node, PipelineContext context) {
        super(node);
        this.context = context;
        this.toName = node.getString("to");
        this.using = node.getString("using");
    }

    @Override
    public void execute(StageElement stageElement, ExecutionTarget executionTarget) throws AssetException {
        if (executionTarget == ExecutionTarget.Pseudo) {
            stageElement.setData(stageElement.getAssetData().reinterpret(toName));
            return;
        }

        if (anyDecoder == null) {
            anyDecoder = Decoders.find(using);
        }
        if (anyDecoder == null) {
            throw new AssetException("Cannot find decoder " + using);
        }

        AssetData data = stageElement.getAssetData();
        if (data.isResolved()) {
            throw new AssetException("The <decode> stage can only be used  for unresolved assets! Make sure that no filter requests data from the asset beforehand, so it doesnt get loaded in!");
        }
        Identifier identifier = data.getUnresolved();

        try {
            if (anyDecoder instanceof LazyDecoder<?>) {
                throw new AssetException("Lazy decoders are not supported in the <decode> stage! Using decoder " + using);
            } else if (anyDecoder instanceof Decoder<?> decoder) {
                Object decoded = decoder.decode(identifier);
                stageElement.setData(new AssetData(toName, decoded));
            }
        } catch (DecodeException e) {
            throw new AssetException(e);
        }
    }
}
