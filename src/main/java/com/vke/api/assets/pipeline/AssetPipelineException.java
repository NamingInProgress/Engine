package com.vke.api.assets.pipeline;

import java.io.IOException;

public class AssetPipelineException extends Exception {
    public AssetPipelineException(String message) {
        super(message);
    }

    public AssetPipelineException(Exception e) {
        super(e);
    }

    public static AssetPipelineException inStage(String stage, String message) {
        String msg = String.format("[%s]: %s", stage, message);
        return new AssetPipelineException(msg);
    }

    public static AssetPipelineException noClass(String stage, String name) {
        String msg = String.format("Class not found: %s", name);
        return inStage(stage, msg);
    }

    public static AssetPipelineException doesntImplement(String stage, Class<?> found, Class<?> superClass) {
        String msg = String.format("Class %s doesnt implement %s, but this is required in this stage!", found, superClass);
        return inStage(stage, msg);
    }

    public static AssetPipelineException illegalURI(String stage, String uri, String uriParseMessage) {
        String msg = String.format("Failed to parse invalid URI '%s' with message: %s", uri, uriParseMessage);
        return inStage(stage, msg);
    }

    public static AssetPipelineException noConstructor(String stage, Class<?> clazz, Exception exception) {
        String msg = String.format("Failed to run constructor for class %s -> %s", clazz, exception);
        return inStage(stage, msg);
    }

    public static AssetPipelineException unknownStage(String stage) {
        String msg = String.format("The stage '%s' is unknown!", stage);
        return new AssetPipelineException(msg);
    }

    public static AssetPipelineException unknownProtocol(String protocol) {
        String msg = String.format("The protocol '%s' cannot be resolved!", protocol);
        return new AssetPipelineException(msg);
    }

    public static AssetPipelineException unknownSelector(String protocol, String selector) {
        String msg = String.format("The protocol '%s' doesnt contain the selector %s!", protocol, selector);
        return new AssetPipelineException(msg);
    }

    public static AssetPipelineException incompatibleStageType(String stage, String protocol, Object dataObj, Class<?> expectedClass) {
        String msg = String.format("The protocol %s doesnt support StageElement type %s, expected %s!", protocol, dataObj.getClass(), expectedClass);
        return inStage(stage, msg);
    }

    public static AssetPipelineException incompatibleProtocol(String stage, String supported, String found) {
        String msg = String.format("Incompatible protocol '%s' found, supported is %s", found, supported);
        return inStage(stage, msg);
    }
}
