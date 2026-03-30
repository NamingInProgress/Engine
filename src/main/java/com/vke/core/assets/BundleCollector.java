package com.vke.core.assets;

import com.vke.api.assets.AssetHandle;
import com.vke.api.assets.Bundle;
import com.vke.api.assets.Protocols;
import com.vke.core.Context;
import com.vke.core.assets.handles.utils.ResolvedAssetHandle;
import com.vke.core.assets.pipeline.AssetPipeline;
import com.vke.core.assets.pipeline.StageElement;
import com.vke.core.assets.pipeline.apis.AssetData;
import com.vke.core.assets.pipeline.stages.PipelineStage;
import com.vke.api.parsing.config.ConfigDocument;
import com.vke.api.parsing.config.ConfigParser;
import com.vke.api.parsing.config.node.*;
import com.vke.utils.io.Identifier;
import com.vke.utils.Utils;
import com.vke.utils.exception.Unreachable;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class BundleCollector {
    public static Bundle getBundle(Context context, Identifier ident, AssetPipeline pipeline) {
        Bundle bundle = new Bundle(context);
        Identifier bundleXMLIdent = ident.extend("bundle.xml");

        if (bundleXMLIdent.existsFile()) {
            try {
                InputStream xmlStream = bundleXMLIdent.asInputStream();
                ConfigParser parser = ConfigParser.forFileType(bundleXMLIdent.getPath());
                if (parser == null) {
                    throw new Unreachable();
                }
                parser.setSource(Utils.readCharsFromInputStream(xmlStream));
                ConfigDocument document = parser.parse(ConfigParser.ATTRIBS_TO_FIELDS | ConfigParser.PARSE_LITERALS);

                readBundleXml(context, bundle, document);
            } catch (ConfigParser.ConfigParseException | IOException e) {
                context.throwException(e, "BundleXML");
                System.exit(67);
            }
        }

        for (Identifier file : ident.walkFiles()) {
            if (file.equals(bundleXMLIdent)) continue;

            try {
                StageElement element = new StageElement(file.toPath(), AssetData.plain(file));
                pipeline.execute(element, PipelineStage.ExecutionTarget.Pseudo);
                AssetHandle<?> handle = pipeline.extractHandle(element);
                bundle.addAsset(element.getAssetName(), handle);
            } catch (AssetException e) {
                context.throwException(e, "AssetPipeline pseudoExecute");
            }
        }

        return bundle;
    }

    public static Bundle collectGlobalBundle(Context context, AssetPipeline pipeline) {
        Bundle globalBundle = new Bundle(context);

        Identifier globalBundleIdent = context.id("assets/global");
        if (globalBundleIdent.existsFile()) {
            Bundle thisOne = getBundle(context, globalBundleIdent, pipeline);
            globalBundle.extendBundle(thisOne);
        }

        return globalBundle;
    }

    public static Map<String, Bundle> collectBundles(Context context, AssetPipeline pipeline) {
        HashMap<String, Bundle> bundles = new HashMap<>();
        Identifier ident = context.id("assets");
        for (Identifier dir : ident.walkDirectories(1)) {
            if (dir.equals(context.id("assets/global"))) continue;
            Bundle bundle = getBundle(context, dir, pipeline);
            bundles.put(dir.strip().getPath(), bundle);
        }
        return bundles;
    }

    private static void readBundleXml(Context context, Bundle target, ConfigDocument xml) {
        ConfigNode root = xml.getRoot();
        ConfigObjectNode bundle = root.getObject("bundle");
        ConfigArrayNode assets = bundle.getArray("assets");

        for (ConfigNode v : assets.values()) {
            ConfigArrayNode asset = v.asArray();
            Identifier id = context.id(asset.getString("name"));
            ConfigNode value = asset.values()[1];

            switch (asset.getNodeName()) {
                case "bool" -> target.addAsset(id, new ResolvedAssetHandle<>(Protocols.PRIMITIVE_BOOL, value.asBoolean()));
                case "string" -> target.addAsset(id, new ResolvedAssetHandle<>(Protocols.PLAIN, value.asString()));
                case "number" -> target.addAsset(id, new ResolvedAssetHandle<>(Protocols.PRIMITIVE_NUMBER, value.asNumber()));
                default -> throw new IllegalStateException("Unknown asset type: " + asset.getNodeName());
            }
        }
    }
}
