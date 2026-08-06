package com.vke.core.assets.pipeline.protocols.mesh;

import com.vke.api.assets.Protocols;
import com.vke.api.assets.anot.Protocol;
import com.vke.core.mesh.MeshPrefab;
import com.vke.core.assets.AssetException;
import com.vke.core.assets.pipeline.Op;
import com.vke.core.assets.pipeline.apis.AssetData;
import com.vke.core.assets.pipeline.apis.AssetProtocol;
import com.vke.core.assets.pipeline.apis.AssetUri;

@Protocol
public class MeshprefabProtocol implements AssetProtocol<MeshPrefab> {
    @Override
    public String getProtocolName() {
        return Protocols.MESHPREFAB;
    }

    @Override
    public AssetData getField(AssetData data, AssetUri uri) throws AssetException {
        throw AssetException.unknownSelector(getProtocolName(), uri.getSelector());
    }

    @Override
    public Loader getLoader() {
        //no loader here cuz u have to decode first using obj or whatever
        return null;
    }

    @Override
    public boolean applies(AssetData a, AssetData b, Op op) {
        return false;
    }
}
