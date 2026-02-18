package com.vke.api.services;

@FunctionalInterface
public interface ServiceProvider<T extends Service> {

    T create(ServiceCreateContext ctx);

}
