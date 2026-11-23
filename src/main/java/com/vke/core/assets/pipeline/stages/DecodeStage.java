package com.vke.core.assets.pipeline.stages;

import com.vke.api.file.*;
import com.vke.api.parsing.config.node.ConfigNode;
import com.vke.core.FileIdentifier;
import com.vke.core.assets.AssetException;
import com.vke.core.assets.pipeline.PipelineContext;
import com.vke.core.assets.pipeline.StageElement;
import com.vke.core.assets.pipeline.apis.AssetData;

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
            anyDecoder = Decoders.make(using);
        }
        if (anyDecoder == null) {
            throw new AssetException("Cannot find decoder " + using);
        }

        AssetData data = stageElement.getAssetData();
        if (data.isResolved()) {
            throw new AssetException("The <decode> stage can only be used for unresolved assets! Make sure that no filter requests data from the asset beforehand, so it doesnt get loaded in!");
        }
        FileIdentifier identifier = data.getUnresolved();

        try {
            if (anyDecoder instanceof Decoder<?> decoder) {
                if (stageElement.getAssetName().toString().equals("vke:textures/material/defaults/default_albedo.png")) {
                    System.out.println("this is in the decode stage remember to remove");
                }
                Object decoded = decoder.decode(context, identifier);
                stageElement.setData(new AssetData(toName, decoded));
            }
        } catch (DecodeException e) {
            throw new AssetException(e);
        }
    }
}
