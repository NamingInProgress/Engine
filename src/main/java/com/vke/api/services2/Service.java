package com.vke.api.services2;

import com.vke.utils.io.Disposable;

import java.util.List;

public interface Service extends Disposable {
    String getId();

    List<String> dependencies();

    @SuppressWarnings("unchecked")
    default <T extends Service> T assumeImplementation() {
        if (this instanceof ServiceImpl impl) {
            return (T) impl;
        }
        if (this instanceof ServiceAPI api) {
            return (T) api.getImplementation();
        }
        return null;
    }
}
