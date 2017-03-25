
package org.mason.certscan.api;

import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;

import java.util.Set;

/**
 * Created by masonb on 5/15/2016.
 */
abstract public class AbstractApplication extends ResourceConfig {
    static JacksonJaxbJsonProvider provider = new JacksonJaxbJsonProvider();

    static {
        provider.setMapper(ObjectMapperManager.getObjectMapper());
    }

    public Set<Class<?>> getServants() {
        final Set<Class<?>> resources = new java.util.HashSet<>();
        resources.add(ExceptionHandler.class);
        return resources;
    }

    public AbstractApplication() {
        register(provider);
        Set<Class<?>> allResources = getServants();
        registerClasses(allResources);
        initInjection();
    }

    protected void initInjection() {
        register(new AbstractBinder() {
            @Override
            protected void configure() {
                //bind place holder
                //bind(some class).to(some interface);
            }
        });
    }
}
