package com.vke.core.assets;

public class AssetException extends Exception {
    public AssetException(String message) {
        super(message);
    }

    public AssetException(Exception e) {
        super(e);
    }

    public static AssetException inStage(String stage, String message) {
        String msg = String.format("[%s]: %s", stage, message);
        return new AssetException(msg);
    }

    public static AssetException noClass(String stage, String name) {
        String msg = String.format("Class not found: %s", name);
        return inStage(stage, msg);
    }

    public static AssetException doesntImplement(String stage, Class<?> found, Class<?> superClass) {
        String msg = String.format("Class %s doesnt implement %s, but this is required in this stage!", found, superClass);
        return inStage(stage, msg);
    }

    public static AssetException illegalURI(String stage, String uri, String uriParseMessage) {
        String msg = String.format("Failed to parse invalid URI '%s' with message: %s", uri, uriParseMessage);
        return inStage(stage, msg);
    }

    public static AssetException noConstructor(String stage, Class<?> clazz, Exception exception) {
        String msg = String.format("Failed to run constructor for class %s -> %s", clazz, exception);
        return inStage(stage, msg);
    }

    public static AssetException unknownStage(String stage) {
        String msg = String.format("The stage '%s' is unknown!", stage);
        return new AssetException(msg);
    }

    public static AssetException unknownProtocol(String protocol) {
        String msg = String.format("The protocol '%s' cannot be resolved!", protocol);
        return new AssetException(msg);
    }

    public static AssetException unknownSelector(String protocol, String selector) {
        String msg = String.format("The protocol '%s' doesnt contain the selector %s!", protocol, selector);
        return new AssetException(msg);
    }

    public static AssetException incompatibleStageType(String stage, String protocol, Object dataObj, Class<?> expectedClass) {
        String msg = String.format("The protocol %s doesnt support StageElement type %s, expected %s!", protocol, dataObj.getClass(), expectedClass);
        return inStage(stage, msg);
    }

    public static AssetException incompatibleProtocol(String stage, String supported, String found) {
        String msg = String.format("Incompatible protocol '%s' found, supported is %s", found, supported);
        return inStage(stage, msg);
    }
}
