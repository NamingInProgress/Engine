package com.vke.api.services2;

import com.vke.utils.io.Disposable;

import java.util.List;

public interface Service extends Disposable {
    String getId();

    List<String> dependencies();
}
