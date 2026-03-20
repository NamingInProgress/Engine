package com.vke.core.assets.pipeline.apis;

import com.vke.utils.io.SegmentedPath;

import java.net.URI;

public class AssetUri {
    private final URI uri;
    private final SegmentedPath path;
    private final SegmentedPath segments;

    public AssetUri(URI uri) {
        this.uri = uri;
        String rawPath = uri.getPath();
        if (rawPath.startsWith("/")) {
            rawPath = rawPath.substring(1);
        }
        this.path = new SegmentedPath(rawPath, "/");
        int partsLen = this.path.getParts().length;
        String[] segmentsArr = new String[partsLen + 1];
        if (partsLen > 0) {
            System.arraycopy(this.path.getParts(), 0, segmentsArr, 1, partsLen);
        }
        segmentsArr[0] = uri.getAuthority();
        this.segments = new SegmentedPath(segmentsArr, "/");
    }

    public URI getUri() {
        return uri;
    }

    public String getProtocol() {
        return uri.getScheme();
    }

    public String getSelector() {
        return uri.getAuthority();
    }

    public SegmentedPath getPath() {
        return path;
    }

    public SegmentedPath getSegments() {
        return segments;
    }
}
