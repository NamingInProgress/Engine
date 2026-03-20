package com.vke.core.assets;

import com.vke.api.assets.AssetHandle;
import com.vke.api.assets.Bundle;
import com.vke.api.assets.Protocols;
import com.vke.core.assets.handles.utils.ResolvedAssetHandle;
import com.vke.core.assets.pipeline.AssetPipeline;
import com.vke.core.assets.pipeline.AssetPipelineException;
import com.vke.core.assets.pipeline.StageElement;
import com.vke.core.assets.pipeline.apis.AssetData;
import com.vke.core.assets.pipeline.stages.PipelineStage;
import com.vke.api.parsing.config.ConfigDocument;
import com.vke.api.parsing.config.ConfigParser;
import com.vke.api.parsing.config.node.*;
import com.vke.core.VKEngine;
import com.vke.utils.io.Identifier;
import com.vke.utils.Utils;
import com.vke.utils.exception.Unreachable;

import java.io.IOException;
import java.io.InputStream;

public class AssetUtils {

    public static Bundle getBundle(VKEngine engine, Identifier ident, AssetPipeline pipeline) {
        Bundle bundle = new Bundle(engine);
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

                readBundleXml(engine, bundle, document);
            } catch (ConfigParser.ConfigParseException | IOException e) {
                engine.throwException(e, "BundleXML");
                System.exit(67);
            }
        }

        for (Identifier file : ident.walkFiles()) {
            if (file.equals(bundleXMLIdent)) continue;

            Identifier id = file.strip();
            AssetHandle<?> handle;
            if (pipeline != null) {
                try {
                    StageElement element = new StageElement(file.toPath(), AssetData.plain(file));
                    pipeline.execute(element, PipelineStage.ExecutionTarget.Pseudo);
                    handle = pipeline.extractHandle(element);
                    bundle.addAsset(element.getAssetName(), handle);
                } catch (AssetPipelineException e) {
                    engine.throwException(e, "AssetPipeline pseudoExecute");
                }
            } else {
                handle = AssetHandle.ofFile(file);
                bundle.addAsset(id, handle);
            }
        }

        return bundle;
    }

    public static Bundle collectGlobalBundles(VKEngine engine, AssetPipeline pipeline) {
        Bundle globalBundle = new Bundle(engine);

        for (String namespace : engine.getAllNamespaces()) {
            Identifier globalBundleIdent = new Identifier(namespace, "assets/global");
            if (globalBundleIdent.existsFile()) {
                Bundle thisOne = getBundle(engine, globalBundleIdent, pipeline);
                globalBundle.extendBundle(thisOne);
            }
        }

        return globalBundle;
    }

    public static void collectBundles(VKEngine engine, VKEAssetManager manager, AssetPipeline pipeline) {
        for (String namespace : engine.getAllNamespaces()) {
            Identifier ident = new Identifier(namespace, "assets");
            for (Identifier dir : ident.walkDirectories(1)) {
                if (dir.equals(new Identifier(namespace, "assets/global"))) continue;
                Bundle bundle = getBundle(engine, dir, pipeline);
                manager.addBundle(dir.strip(), bundle);
            }
        }
    }

    private static void readBundleXml(VKEngine engine, Bundle target, ConfigDocument xml) {
        ConfigNode root = xml.getRoot();
        ConfigObjectNode bundle = root.getObject("bundle");
        ConfigArrayNode assets = bundle.getArray("assets");

        for (ConfigNode v : assets.values()) {
            ConfigArrayNode asset = v.asArray();
            Identifier id = engine.id(asset.getString("name"));
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
