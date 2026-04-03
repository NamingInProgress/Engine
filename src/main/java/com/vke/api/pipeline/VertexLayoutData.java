package com.vke.api.pipeline;

import com.vke.api.rendering.abstraction.enums.texture.TextureFormat;

import java.util.List;

public class VertexLayoutData {
    private final List<Attribute> attributeTypes;

    public VertexLayoutData(List<Attribute> attributeTypes) {
        this.attributeTypes = attributeTypes;
    }

    public List<Attribute> getAttributeTypes() {
        return attributeTypes;
    }

    public static class Attribute {
        private final int byteStride;
        private final TextureFormat format;

        public Attribute(int byteStride, TextureFormat format) {
            this.byteStride = byteStride;
            this.format = format;
        }

        public int getByteStride() {
            return byteStride;
        }

        public TextureFormat getFormat() {
            return format;
        }
    }
}
