package com.vke.api.assets.pipeline;

import com.vke.api.parsing.config.node.ConfigNode;
import com.vke.api.assets.pipeline.apis.ProtocolResolver;
import com.vke.api.assets.pipeline.stages.CompoundPipelineStage;
import com.vke.utils.Utils;

import java.net.URI;
import java.nio.file.Path;
import java.util.Objects;
import java.util.regex.Pattern;

public class StageFilter extends CompoundPipelineStage {
    public static final String STAGE = "stage-filter";
    public static final String[] GLOBAL_PROTOCOLS = { "file", "meta" };

    private final PipelineContext context;

    private final URI uri;
    private final Op op;
    private final String query;
    private final Pattern regexPattern;

    public StageFilter(ConfigNode node, PipelineContext context) throws AssetPipelineException {
        super(node.asArray(), context, "uri", "op", "query");
        this.context = context;
        String uriString = node.getString("uri");
        String opStr = node.getStringOption("op").unwrapOr(Op.EQUALS.name());
        String query = node.getString("query");

        try {
            this.uri = URI.create(uriString);
        } catch (IllegalArgumentException e) {
            throw AssetPipelineException.illegalURI(STAGE, uriString, e.getMessage());
        }
        this.op = Op.valueOf(opStr.toUpperCase());
        this.query = query;
        if (op == Op.MATCHES) {
            //compile regex here to make it more efficient
            this.regexPattern = Pattern.compile(query);
        } else {
            this.regexPattern = null;
        }
    }

    public String getProtocol() {
        return uri.getScheme();
    }

    public String getSelector() {
        return uri.getAuthority();
    }

    public String getPath() {
        String rawPath = uri.getPath();
        if (rawPath == null) return null;
        if (rawPath.startsWith("/")) {
            return rawPath.substring(1);
        }
        return rawPath;
    }

    public URI getUri() {
        return uri;
    }

    public Op getOp() {
        return op;
    }

    public String getQuery() {
        return query;
    }

    @Override
    public void execute(StageElement stageElement) throws AssetPipelineException {
        String filterProtocol = getProtocol();
        boolean use;
        if (Utils.arrayContains(GLOBAL_PROTOCOLS, filterProtocol)) {
            //filter that works for every content type
            ProtocolResolver<?> resolver = context.getGlobalResolver();
            use = resolver.checkProtocolContent(this, stageElement);
        } else {
            //this filter is restricted to a specific content type
            ProtocolResolver<?> resolver = context.getResolver(filterProtocol);
            use = resolver.checkProtocolContent(this, stageElement);
        }

        if (use) {
            processInnerPipeline(stageElement);
        }
    }

    public boolean applyForString(String value) {
        return switch (op) {
            case EQUALS -> Objects.equals(value, query);
            case CONTAINS -> safeContains(query, value);
            case MATCHES -> regexPattern.asPredicate().test(value);
        };
    }

    private boolean safeContains(String a, String b) {
        if (a == null) return false;
        return a.contains(b);
    }

    public boolean applyForPathString(StageFilter filter, Path path) {
        String normalized = path.toString().replace('\\', '/');
        return switch (filter.getOp()) {
            case EQUALS -> normalized.equals(filter.getQuery());
            case CONTAINS -> normalized.contains(filter.getQuery());
            case MATCHES -> regexPattern.asMatchPredicate().test(normalized);
        };
    }

    public enum Op {
        EQUALS,
        CONTAINS,
        MATCHES
    }
}
