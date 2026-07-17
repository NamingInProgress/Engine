package com.vke.core.framable.service;

import com.vke.api.framable.Framable;
import com.vke.api.services2.ServiceAPI;
import com.vke.api.services2.ServiceImpl;
import org.jetbrains.annotations.Nullable;

public class FramableManagerAPI extends ServiceAPI implements FramableManager {
    public FramableManagerAPI(ServiceImpl baseImpl) {
        super(baseImpl.getId(), baseImpl);
    }

    private FramableManager getImpl() {
        return (FramableManager) getImplementation();
    }

    @Override
    public void registerFramable(Framable framable) {
        getImpl().registerFramable(framable);
    }

    @Override
    public void removeFramable(Framable framable) {
        getImpl().removeFramable(framable);
    }

    @Override
    public Framable.Glossary getAllFramables() {
        return getImpl().getAllFramables();
    }

    @Override
    public void handlePossibleFrame() {
        getImpl().handlePossibleFrame();
    }

    @Override
    public void skipThisFrame() {
        getImpl().skipThisFrame();
    }

    @Override
    public @Nullable FramableManager.TransferState createTransferState() {
        return getImpl().createTransferState();
    }

    @Override
    public void applyTransferState(@Nullable TransferState state) {
        getImpl().applyTransferState(state);
    }
}
