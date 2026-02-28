package com.vke.api.assets.pipeline.stages;

import com.vke.api.parsing.config.node.ConfigArrayNode;
import com.vke.api.parsing.config.node.ConfigNode;
import com.vke.api.parsing.config.node.EmptyConfigArray;
import com.vke.api.assets.pipeline.AssetPipelineException;

import java.lang.reflect.InvocationTargetException;

public abstract class ExecutingStage<T> implements PipelineStage {
    protected final Class<T> executorClass;
    protected final T instance;

    @SuppressWarnings("unchecked")
    public ExecutingStage(ConfigNode node, String executorTagName) throws AssetPipelineException {
        String parserClassName = node.getString(executorTagName);
        ConfigArrayNode argumentsNode = node.getArrayOption("arguments").unwrapOrElse(EmptyConfigArray::new);

        try {
            this.executorClass = (Class<T>) Class.forName(parserClassName);
        } catch (ClassNotFoundException e) {
            throw AssetPipelineException.noClass(getStageName(), parserClassName);
        }

        try {
            this.instance = executorClass.getDeclaredConstructor(ConfigArrayNode.class).newInstance(argumentsNode);
        } catch (InvocationTargetException | InstantiationException | IllegalAccessException | NoSuchMethodException e) {
            throw AssetPipelineException.noConstructor(getStageName(), executorClass, e);
        }
    }

    protected abstract String getStageName();
}
