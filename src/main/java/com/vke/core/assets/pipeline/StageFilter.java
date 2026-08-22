package com.vke.core.assets.pipeline;

import com.vke.api.assets.Protocols;
import com.vke.api.parsing.config.node.ConfigNode;
import com.vke.core.parsing.config.utils.EmptyConfigArray;
import com.vke.core.assets.AssetException;
import com.vke.core.assets.meta.AssetMetaAttributes;
import com.vke.core.assets.pipeline.apis.AssetData;
import com.vke.core.assets.pipeline.apis.AssetProtocol;
import com.vke.core.assets.pipeline.apis.AssetUri;
import com.vke.core.assets.pipeline.apis.AssetConverter;
import com.vke.core.assets.pipeline.stages.CompoundPipelineStage;
import com.vke.core.assets.pipeline.stages.FilterElseStage;
import com.vke.core.assets.pipeline.stages.PipelineStage;
import com.vke.utils.io.SegmentedPath;

import java.net.URI;
import java.util.HashSet;

public class StageFilter extends CompoundPipelineStage {
    public static final String STAGE = "stage-filter";

    private final PipelineContext context;

    private final AssetUri uri;
    private final Op op;
    private final String query;

    private boolean wasSuccessful;
    private String elseTag;
    private final HashSet<String> elseTags;

    public StageFilter(ConfigNode node, PipelineContext context) throws AssetException {
        super(node.asArray(), context, "uri", "op", "query", "else");
        this.context = context;
        String uriString = node.getString("uri");
        String opStr = node.getStringOption("op").unwrapOr(Op.EQUALS.name());
        String query = node.getString("query");

        try {
            this.uri = new AssetUri(URI.create(uriString));
        } catch (IllegalArgumentException e) {
            throw AssetException.illegalURI(STAGE, uriString, e.getMessage());
        }
        this.op = Op.valueOf(opStr.toUpperCase());
        this.query = query;
        this.elseTag = node.getStringOption("else").unwrapOrNull();
        this.elseTags = new HashSet<>();
    }

    public String getProtocol() {
        return uri.getProtocol();
    }

    public String getSelector() {
        return uri.getSelector();
    }

    public SegmentedPath getPath() {
        return uri.getPath();
    }

    public URI getUri() {
        return uri.getUri();
    }

    public Op getOp() {
        return op;
    }

    public String getQuery() {
        return query;
    }

    @Override
    protected void processInnerPipeline(StageElement element, ExecutionTarget executionTarget) throws AssetException {
        elseTags.clear();
        for (PipelineStage stage : stages) {
            if (stage.executionTarget().isUsable(executionTarget)) {
                if (stage instanceof FilterElseStage filterElseStage) {
                    if (elseTags.contains(filterElseStage.getTag())) {
                        stage.execute(element, executionTarget);
                    }
                } else {
                    stage.execute(element, executionTarget);
                }

                if (stage instanceof StageFilter filter) {
                    if (!filter.wasSuccessful && filter.elseTag != null) {
                        elseTags.add(filter.elseTag);
                    }
                }
            }
        }
    }

    @Override
    public void execute(StageElement stageElement, ExecutionTarget target) throws AssetException {
        wasSuccessful = false;

        String dataProtocolName = getProtocol();
        AssetProtocol<?> dataProtocol = context.getProtocol(dataProtocolName);
        AssetData resolvedData = stageElement.getAssetDataResolved(context.context(), dataProtocol, target);
        if (!resolvedData.isResolved()) {
            return;
        }

        AssetData data;
        try {
            data = dataProtocol.getField(resolvedData, uri);
            if (data.getData() == null) {
                System.out.println(uri.getUri());
            }
        } catch (AssetException e) {
            //if the field doesnt exist or smth, we dont care
            return;
        }


        dataProtocolName = data.getProtocol();
        dataProtocol = context.getProtocol(dataProtocolName);
        StageElement dataElement = new StageElement(stageElement.getAssetName().getNamespace(), stageElement.getPath(), stageElement.getBundleName(), data, stageElement.getMetaAttributes());

        String queryProtocolName = Protocols.PLAIN;
        AssetProtocol<?> queryProtocol = context.getProtocol(queryProtocolName);
        AssetData queryData = AssetData.plain(query);
        StageElement queryElement = new StageElement(queryData, new AssetMetaAttributes());

        if (queryProtocolName.equals(dataProtocolName)) {
            if (dataProtocol.applies(data, queryData, op)) {
                wasSuccessful = true;
                stageElement.setProcessed();
                processInnerPipeline(stageElement, target);
            }
        } else {
            AssetConverter converter = context.getConverter(queryProtocolName, dataProtocolName);
            if (converter != null) {
                //query --> data
                AssetData data2 = converter.performConversion(context.context(), queryElement, new EmptyConfigArray());
                if (dataProtocol.applies(data, data2, op)) {
                    wasSuccessful = true;
                    stageElement.setProcessed();
                    processInnerPipeline(stageElement, target);
                }
                return;
            }
            converter = context.getConverter(dataProtocolName, queryProtocolName);
            if (converter == null) {
                throw new AssetException("Unable to convert between '%s' and '%s'! At least one way must be possible for a filter to work.".formatted(dataProtocolName, queryProtocolName));
            }
            //data --> query
            AssetData query2 = converter.performConversion(context.context(), dataElement, new EmptyConfigArray());
            if (queryProtocol.applies(data, query2, op)) {
                wasSuccessful = true;
                stageElement.setProcessed();
                processInnerPipeline(stageElement, target);
            }
        }
    }
}
