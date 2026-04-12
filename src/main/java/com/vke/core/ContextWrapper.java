package com.vke.core;

public class ContextWrapper extends Context {
    private final Context baseContext;
    
    public ContextWrapper(Context baseContext) {
        super(baseContext);
        this.baseContext = baseContext;
    }

    @Override
    public VKEngine getEngine() {
        return baseContext.getEngine();
    }
}
