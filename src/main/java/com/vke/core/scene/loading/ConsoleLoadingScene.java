package com.vke.core.scene.loading;

import com.vke.api.scene.LoadingScene;
import com.vke.core.Context;
import com.vke.utils.io.Identifier;
import com.vke.utils.types.StaticFinal;

public class ConsoleLoadingScene extends LoadingScene {
    private static final StaticFinal<ConsoleLoadingScene> INSTANCE = new StaticFinal<>();

    public ConsoleLoadingScene(Identifier name, Context context) {
        super(name, context);
        INSTANCE.trySet(this);
    }

    @Override
    public void onAssetStartLoad(AssetDesc desc) {
        System.out.println(desc);
    }

    @Override
    public void onAssetEndLoad(AssetDesc desc) {
        System.out.println("100%%");
    }

    @Override
    public void onAssetException(AssetDesc desc, Throwable exception) {
        context.throwException(exception, desc.toString());
    }

    @Override
    public void onLoadingComplete() {
        try {
            completeLoading();
        } catch (Exception e) {
            context.throwException(e, "DefaultVkeLoadingScene#onLoadingComplete");
        }
    }

    @Override
    public void onLoad() {
        System.out.println("Starting asset loading using the Vke LoadingScene");
    }

    @Override
    public void onUnload() {
        System.out.println("Finished asset loading with the Vke LoadingScene");
    }

    @Override
    public void free() {

    }

    public static ConsoleLoadingScene getInstance() {
        return INSTANCE.getNullable();
    }
}
