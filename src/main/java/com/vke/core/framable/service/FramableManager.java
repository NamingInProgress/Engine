package com.vke.core.framable.service;

import com.vke.api.framable.Framable;
import com.vke.api.services2.StatefulService;

public interface FramableManager extends StatefulService<FramableManager.TransferState> {
    record TransferState(Framable.Glossary registered) {}

    void registerFramable(Framable framable);
    void removeFramable(Framable framable);
    Framable.Glossary getAllFramables();

    void handlePossibleFrame();
    void skipThisFrame();
}
