package com.vke.core.vkz;

public class VkzPath {
    private String path;
    private String[] parts;
    
    public VkzPath(CharSequence path) {
        this.path = path.toString();
        this.parts = this.path.split("/");
    }

    public int getLength() {
        return parts.length;
    }

    public String getPart(int index) {
        if (index >= 0 && index < parts.length) {
            return parts[index];
        }
        return null;
    }

    public boolean isLast(int index) {
        return index >= parts.length - 1;
    }
}
