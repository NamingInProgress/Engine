package com.vke.core.assets;

import com.vke.api.assets.AssetHandle;
import com.vke.api.assets.Bundle;
import com.vke.api.assets.Protocols;
import com.vke.api.logger.Logger;
import com.vke.core.Context;
import com.vke.core.FileIdentifier;
import com.vke.core.Identifier;
import com.vke.core.assets.handles.ResolvedAssetHandle;
import com.vke.core.assets.meta.BasicAssetMeta;
import com.vke.core.assets.meta.AssetMetaAttributes;
import com.vke.core.assets.pipeline.AssetPipeline;
import com.vke.core.assets.pipeline.StageElement;
import com.vke.core.assets.pipeline.apis.AssetData;
import com.vke.core.assets.pipeline.stages.PipelineStage;
import com.vke.api.parsing.config.ConfigDocument;
import com.vke.api.parsing.config.node.*;
import com.vke.core.logger.LoggerFactory;
import com.vke.utils.io.FileUtils;
import com.vke.utils.iter.helpers.Option;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.function.Consumer;

public class BundleCollector {
    public static Bundle getBundle(Context context, String bundleKey, FileIdentifier ident, AssetPipeline pipeline, Consumer<Bundle> afterPhase) {
        Bundle bundle = new Bundle(context);
        FileIdentifier bundleVCLIdent = ident.extend("bundle.vcl");

        if (bundleVCLIdent.existsFile()) {
            try {
                ConfigDocument document = ConfigDocument.parseIdentifier(bundleVCLIdent);
                readBundleXml(context, bundle, bundleKey, document);
            } catch (IOException e) {
                context.throwException(e, "BundleVCL");
                System.exit(67);
            }
        }
        Logger logger = LoggerFactory.get("AssetPipeline");
        try {
            HashSet<FileIdentifier> processedFiles = new HashSet<>();
            if (!ident.isJavaEmbed()) {
                FileIdentifier cachedIdent = ident.addCachePath();
                if (cachedIdent.existsFile()) {
                    for (FileIdentifier file : cachedIdent.walkFiles()) {
                        Option<CacheHandler.CachedAsset> maybeAsset = CacheHandler.handleCachedFile(pipeline.getContext(), file, bundleKey);
                        if (maybeAsset.isSome()) {
                            CacheHandler.CachedAsset asset = maybeAsset.unwrap();
                            processedFiles.add(asset.originalName());
                            bundle.addAsset(asset.handle().getMeta().getAssetName(), asset.handle());
                        }
                    }
                }
            }

            pipeline.forEachPhase(phase -> {
                logger.info("Running Phase '" + phase.getName() + "' (pseudo) for bundle: " + ident);

                for (FileIdentifier file : ident.walkFiles()) {
                    if (file.equals(bundleVCLIdent)) continue;
                    if ("vka".equals(FileUtils.getExtensionLower(file))) continue;
                    if (processedFiles.contains(file)) continue;
                    StageElement element = new StageElement(file.getNamespace(), file.toPath(), bundleKey, AssetData.plain(file), null);
                    phase.execute(element, PipelineStage.ExecutionTarget.Pseudo);
                    if (element.wasProcessed()) {
                        AssetMetaAttributes attribs = new AssetMetaAttributes(context, file);
                        AssetMetaAttributes.PhaseFilter phaseFilter = attribs.getPhaseFilter();
                        if (phaseFilter != null) {
                            if (!phaseFilter.isAccepted(phase.getName())) {
                                logger.info("Asset '%s' would be added to the current phase '%s', but its pipeline-config's phase-filter blocked it.", file, phase.getName());
                                continue;
                            }
                        }

                        AssetHandle<?> handle = phase.extractHandle(element, attribs);
                        Identifier overrideName = attribs.getOverrideName();
                        if (overrideName != null) {
                            logger.info("Asset '%s' got renamed by their vka config: '%s' -> '%s'", file, element.getAssetName(), overrideName);
                            bundle.addAsset(overrideName, handle);
                        } else {
                            bundle.addAsset(element.getAssetName(), handle);
                        }
                    }
                }

                afterPhase.accept(bundle);
            });
        } catch (AssetException e) {
            context.throwException(e, "AssetPipeline pseudoExecute");
        }

        return bundle;
    }

    public static Bundle collectGlobalBundle(Context context, AssetPipeline pipeline, Consumer<Bundle> afterPhase) {
        Bundle globalBundle = new Bundle(context);

        FileIdentifier globalBundleIdent = context.fid("assets/global");
        if (globalBundleIdent.existsFile()) {
            Bundle thisOne = getBundle(context, "global", globalBundleIdent, pipeline, afterPhase);
            globalBundle.extendBundle(thisOne);
        }

        return globalBundle;
    }

    public static Map<String, Bundle> collectBundles(Context context, AssetPipeline pipeline) {
        HashMap<String, Bundle> bundles = new HashMap<>();
        FileIdentifier ident = context.fid("assets");
        for (FileIdentifier dir : ident.walkDirectories(1)) {
            if (dir.equals(context.fid("assets/global"))) continue;
            String bundleKey = dir.dropPrefix().strip().getPath();
            Bundle bundle = getBundle(context, bundleKey, dir, pipeline, afterPhase -> bundles.put(bundleKey, afterPhase));
            bundles.put(bundleKey, bundle);
        }
        return bundles;
    }

    private static void readBundleXml(Context context, Bundle target, String bundleName, ConfigDocument xml) {
        ConfigNode root = xml.getRoot();
        ConfigObjectNode bundle = root.getObject("bundle");
        ConfigArrayNode assets = bundle.getArray("assets");

        for (ConfigNode v : assets.values()) {
            ConfigArrayNode asset = v.asArray();
            Identifier id = context.id(asset.getString("name"));
            ConfigNode value = asset.values()[1];

            switch (asset.getNodeName()) {
                case "bool" -> target.addAsset(id, new ResolvedAssetHandle<>(value.asBoolean(), new BasicAssetMeta(Protocols.PRIMITIVE_BOOL, bundleName, id)));
                case "string" -> target.addAsset(id, new ResolvedAssetHandle<>(value.asString(), new BasicAssetMeta(Protocols.PLAIN, bundleName, id)));
                case "number" -> target.addAsset(id, new ResolvedAssetHandle<>(value.asNumber(), new BasicAssetMeta(Protocols.PRIMITIVE_NUMBER, bundleName, id)));
                default -> throw new IllegalStateException("Unknown asset type: " + asset.getNodeName());
            }
        }
    }
}
