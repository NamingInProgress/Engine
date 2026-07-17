package com.vke.api.services2;

import org.jetbrains.annotations.Nullable;

public interface StatefulService<S> extends Service {
    @Nullable S createTransferState();
    void applyTransferState(@Nullable S state);
}
