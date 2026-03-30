package com.vke.api.registry.registries;

import com.vke.api.services.Service;
import com.vke.api.services.ServiceCreateContext;
import com.vke.api.services.ServiceProvider;
import com.vke.utils.io.Identifier;

public class ServicesRegistry extends VKERegistry.String<Service> {

    public static final VKERegistry.String<ServiceProvider<? extends Service>> PROVIDERS = new VKERegistry.String<>(new Identifier("service_providers"));

    public ServicesRegistry() {
        super(new Identifier("service_cache"));
    }

    public void register(java.lang.String k, ServiceProvider<?> provider) {
        PROVIDERS.register(k, provider);
    }

    public Service get(java.lang.String key, ServiceCreateContext ctx) {
        if (super.get(key) == null) super.register(key, PROVIDERS.get(key).create(ctx));
        return super.get(key);
    }

}
