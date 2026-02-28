package com.vke.api.assets.pipeline.validators;

import com.vke.api.assets.pipeline.AssetPipelineException;
import com.vke.api.assets.pipeline.PipelineContext;
import com.vke.api.assets.pipeline.StageElement;
import com.vke.api.assets.pipeline.apis.AssetValidator;
import com.vke.api.assets.pipeline.apis.ProtocolResolver;
import com.vke.api.parsing.config.ConfigDocument;
import com.vke.api.parsing.config.ConfigParser;
import com.vke.api.parsing.config.node.ConfigArrayNode;
import com.vke.api.parsing.config.schema.ConfigSchema;
import com.vke.api.parsing.config.schema.SchemaMismatchException;
import com.vke.core.parsing.config.schema.VkeSchema;
import com.vke.utils.Identifier;

import java.io.IOException;

public class ConfigValidator implements AssetValidator<SchemaMismatchException> {
    private final Identifier schemaIdent;
    private ConfigSchema schema;
    private String filename;

    public ConfigValidator(ConfigArrayNode arguments) {
        String schemaIdentLit = arguments.getString("schema");
        this.schemaIdent = Identifier.of(schemaIdentLit);
        this.filename = schemaIdent.strip().getPath();
    }

    @Override
    public SchemaMismatchException processStageElement(StageElement stageElement, PipelineContext context) throws AssetPipelineException {
        if (schema == null) {
            try {
                ConfigDocument schemaDoc = ConfigDocument.parseIdentifier(schemaIdent);
                schema = new VkeSchema(schemaDoc, filename);
            } catch (ConfigParser.ConfigParseException | IOException e) {
                throw new AssetPipelineException(e);
            }
        }

        String protocol = stageElement.getProtocol();
        ProtocolResolver<ConfigDocument> resolver = context.getResolver(protocol);
        ConfigDocument document = resolver.resolveData(stageElement);
        try {
            document.validate(schema, filename);
        } catch (SchemaMismatchException e) {
            return e;
        }
        return null;
    }
}
