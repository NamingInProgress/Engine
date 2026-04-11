package com.vke.core.assets.pipeline.converters;

import com.vke.api.assets.Protocols;
import com.vke.api.parsing.config.node.ConfigArrayNode;
import com.vke.core.Context;
import com.vke.core.assets.AssetException;
import com.vke.core.assets.pipeline.StageElement;
import com.vke.core.assets.pipeline.apis.AssetConverter;
import com.vke.core.assets.pipeline.apis.AssetData;
import com.vke.core.file.obj.ObjFile;

public class ObjMeshprefabConverter implements AssetConverter {
    @Override
    public String from() {
        return Protocols.OBJ;
    }

    @Override
    public String to() {
        return Protocols.MESHPREFAB;
    }

    @Override
    public AssetData performConversion(Context context, StageElement input, ConfigArrayNode arguments) throws AssetException {
        ObjFile objFile = input.getAssetData().getDataAs();
        return new AssetData(Protocols.MESHPREFAB, objFile.toMeshPrefab());
    }
}
