package com.vke.core.parsing.config.schema.vks.parser.tkn;

public class VksT {
    private final VksTT type;
    private final Object data;

    public VksT(VksTT type) {
        this.type = type;
        this.data = null;
    }

    public VksT(VksTT type, Object data) {
        this.type = type;
        this.data = data;
    }

    public VksTT getType() {
        return type;
    }

    public Object getData() {
        return data;
    }

    @SuppressWarnings("unchecked")
    public <T> T getDataAs() {
        return (T) data;
    }
}
