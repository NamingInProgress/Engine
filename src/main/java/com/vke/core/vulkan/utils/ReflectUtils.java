package com.vke.core.vulkan.utils;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class ReflectUtils {

    private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();

    public static List<Method> getAnnotatedMethods(Class<?> clazz, Class<? extends Annotation> annotation) {
        List<Method> methods = new ArrayList<>();

        for (Method method : clazz.getDeclaredMethods()) {
            if (method.isAnnotationPresent(annotation)) methods.add(method);
        }

        return methods;
    }

    public static MethodHandle asMethodHandle(Method method) throws IllegalAccessException {
        return LOOKUP.unreflect(method);
    }

    public static List<MethodHandle> getAnnotatedMethodHandles(Class<?> clazz, Class<? extends Annotation> annotation) throws IllegalAccessException {
        List<MethodHandle> handles = new ArrayList<>();

        for (Method method : clazz.getDeclaredMethods()) {
            if (method.isAnnotationPresent(annotation)) handles.add(LOOKUP.unreflect(method));
        }

        return handles;
    }

}
