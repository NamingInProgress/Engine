package com.vke.core.assets.meta;

import com.vke.api.assets.r.R;
import com.vke.api.parsing.config.ConfigDocument;
import com.vke.api.parsing.config.node.ConfigArrayNode;
import com.vke.api.parsing.config.node.ConfigNode;
import com.vke.api.parsing.config.node.ConfigObjectNode;
import com.vke.api.parsing.config.node.ConfigValueNode;
import com.vke.api.parsing.config.schema.ConfigSchema;
import com.vke.core.Context;
import com.vke.core.assets.AssetException;
import com.vke.core.assets.handles.LazyAssetHandle;
import com.vke.utils.Utils;
import com.vke.utils.io.Identifier;
import com.vke.utils.iter.helpers.Option;

import java.util.ArrayList;
import java.util.List;

public class AssetMetaAttributes {
    private static final LazyAssetHandle<ConfigSchema> SCHEMA = R.schemas.get("asset-meta.schema.json");
    private static ConfigSchema schema;

    private final String tag;
    private final Identifier overrideName;
    private final PhaseFilter phaseFilter;
    private final ConfigNode assetConfig;

    public AssetMetaAttributes() {
        this.tag = null;
        this.overrideName = null;
        this.phaseFilter = null;
        this.assetConfig = null;
    }

    public AssetMetaAttributes(Context context, Identifier file) throws AssetException {
        Identifier vkaFile = vkaFileIdent(file);
        if (vkaFile.existsFile()) {
            //important that this only happens here because in the first phase, schemas dont exist yet
            if (schema == null) {
                schema = SCHEMA.assume(context);
            }

            ConfigDocument vkaDoc = Utils.chainExceptions(() -> {
                ConfigDocument d = ConfigDocument.parseIdentifier(vkaFile);
                d.validate(schema, vkaFile.getPath());
                return d;
            });
            ConfigNode assetNode = vkaDoc.getRoot().getObject("asset-meta");
            this.assetConfig = assetNode.getObject("asset-config");

            ConfigObjectNode pipelineConfig = assetNode.getObject("pipeline-config");
            this.tag = getVclString(pipelineConfig, "tag").unwrapOrNull();
            this.overrideName = getVclString(pipelineConfig, "override-name").map(context::id).unwrapOrNull();
            this.phaseFilter = new PhaseFilter(pipelineConfig.getObject("phase-filter"));
        } else {
            this.tag = null;
            this.overrideName = null;
            this.phaseFilter = null;
            this.assetConfig = null;
        }
    }

    private static Option<String> getVclString(ConfigObjectNode node, String key) {
        ConfigNode inner = node.getNode(key);
        return getVclString(inner);
    }

    private static Option<String> getVclString(ConfigNode node) {
        if (node instanceof ConfigValueNode vn) return Option.some(vn.getValue());
        if (node instanceof ConfigArrayNode an) {
            ConfigNode[] values = an.values();
            if (values.length > 0) {
                return Option.some(values[0].asString());
            }
        }
        return Option.none();
    }

    private static Identifier vkaFileIdent(Identifier file) {
        return file.extendRaw(".vka");
    }

    public String getTag() {
        return tag;
    }

    public Identifier getOverrideName() {
        return overrideName;
    }

    public PhaseFilter getPhaseFilter() {
        return phaseFilter;
    }

    public ConfigNode getAssetConfig() {
        return assetConfig;
    }

    public static class PhaseFilter {
        private boolean containsAllow;
        private final List<String> allowed;
        private final List<String> banned;

        public PhaseFilter(ConfigNode node) {
            this.allowed = new ArrayList<>();
            this.banned = new ArrayList<>();

            if (node instanceof ConfigArrayNode array) {
                for (ConfigNode entry : array.values()) {
                    if ("ban-phase".equals(entry.getNodeName())) {
                        String toban = getVclString(entry).unwrapOrNull();
                        if (toban != null) {
                            banned.add(toban);
                        }
                    }
                    if ("allow-phase".equals(entry.getNodeName())) {
                        containsAllow = true;
                        String toAllow = getVclString(entry).unwrapOrNull();
                        if (toAllow != null) {
                            allowed.add(toAllow);
                        }
                    }
                }
            }
        }

        public boolean containsAllow() {
            return containsAllow;
        }

        public List<String> getAllowed() {
            return allowed;
        }

        public List<String> getBanned() {
            return banned;
        }

        public boolean isAccepted(String phaseName) {
            if (containsAllow) {
                return allowed.contains(phaseName) && !banned.contains(phaseName);
            }
            return !banned.contains(phaseName);
        }
    }
}
