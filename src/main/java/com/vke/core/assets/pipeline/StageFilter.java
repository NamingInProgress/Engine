package com.vke.core.assets.pipeline;

import com.vke.api.assets.Protocols;
import com.vke.api.parsing.config.node.ConfigNode;
import com.vke.api.parsing.config.node.EmptyConfigArray;
import com.vke.core.assets.pipeline.apis.AssetData;
import com.vke.core.assets.pipeline.apis.AssetProtocol;
import com.vke.core.assets.pipeline.apis.AssetUri;
import com.vke.core.assets.pipeline.apis.AssetConverter;
import com.vke.core.assets.pipeline.stages.CompoundPipelineStage;
import com.vke.utils.io.SegmentedPath;

import java.net.URI;

public class StageFilter extends CompoundPipelineStage {
    public static final String STAGE = "stage-filter";

    private final PipelineContext context;

    private final AssetUri uri;
    private final Op op;
    private final String query;

    public StageFilter(ConfigNode node, PipelineContext context) throws AssetPipelineException {
        super(node.asArray(), context, "uri", "op", "query");
        this.context = context;
        String uriString = node.getString("uri");
        String opStr = node.getStringOption("op").unwrapOr(Op.EQUALS.name());
        String query = node.getString("query");

        try {
            this.uri = new AssetUri(URI.create(uriString));
        } catch (IllegalArgumentException e) {
            throw AssetPipelineException.illegalURI(STAGE, uriString, e.getMessage());
        }
        this.op = Op.valueOf(opStr.toUpperCase());
        this.query = query;
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
    public void execute(StageElement stageElement, ExecutionTarget target) throws AssetPipelineException {
        String dataProtocolName = getProtocol();
        AssetProtocol<?> dataProtocol = context.getProtocol(dataProtocolName);
        AssetData data = dataProtocol.getField(stageElement.getAssetData(dataProtocolName), uri);
        dataProtocolName = data.getProtocol();
        dataProtocol = context.getProtocol(dataProtocolName);
        StageElement dataElement = new StageElement(stageElement.getPath(), data);

        String queryProtocolName = Protocols.PLAIN;
        AssetProtocol<?> queryProtocol = context.getProtocol(queryProtocolName);
        AssetData queryData = AssetData.plain(query);
        StageElement queryElement = new StageElement(queryData);

        if (queryProtocolName.equals(dataProtocolName)) {
            if (dataProtocol.applies(data, queryData, op)) {
                processInnerPipeline(stageElement, target);
            }
        } else {
            AssetConverter converter = context.getConverter(queryProtocolName, dataProtocolName);
            if (converter != null) {
                //query --> data
                AssetData data2 = converter.performConversion(queryElement, new EmptyConfigArray());
                if (dataProtocol.applies(data, data2, op)) {
                    processInnerPipeline(stageElement, target);
                }
                return;
            }
            converter = context.getConverter(dataProtocolName, queryProtocolName);
            if (converter == null) {
                throw new AssetPipelineException("Unable to convert between '%s' and '%s'! At least one way must be possible for a filter to work.".formatted(dataProtocolName, queryProtocolName));
            }
            //data --> query
            AssetData query2 = converter.performConversion(dataElement, new EmptyConfigArray());
            if (queryProtocol.applies(data, query2, op)) {
                processInnerPipeline(stageElement, target);
            }
        }
    }
}
