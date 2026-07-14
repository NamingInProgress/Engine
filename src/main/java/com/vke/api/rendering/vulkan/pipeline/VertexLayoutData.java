package com.vke.api.rendering.vulkan.pipeline;

import com.vke.api.rendering.abstraction.renderer.enums.texture.Format;

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
        private final Format format;

        public Attribute(int byteStride, Format format) {
            this.byteStride = byteStride;
            this.format = format;
        }

        public int getByteStride() {
            return byteStride;
        }

        public Format getFormat() {
            return format;
        }
    }
}
