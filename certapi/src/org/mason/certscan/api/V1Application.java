
package org.mason.certscan.api;

import io.swagger.jaxrs.config.BeanConfig;
import org.mason.certscan.api.v1.CertificateImpl;

import javax.ws.rs.ApplicationPath;
import java.util.HashSet;
import java.util.Set;

/**
 * Added a real comment
 */
@ApplicationPath("v1")
public class V1Application extends AbstractApplication {


    public V1Application() {
        registerClasses(io.swagger.jaxrs.listing.ApiListingResource.class, io.swagger.jaxrs.listing.SwaggerSerializers.class);
        BeanConfig beanConfig = new BeanConfig();
        beanConfig.setVersion("1.0.0");
        beanConfig.setSchemes(new String[]{"http", "https"});
        beanConfig.setHost("localhost:8080");
        beanConfig.setBasePath("/v1");
        beanConfig.setResourcePackage("org.mason.certscan.api.v1");
        beanConfig.setScan(true);
    }

    @Override
    public Set<Class<?>> getServants() {
        Set<Class<?>> servants = super.getServants();
        Set<Class<?>> ret=new HashSet<>(servants);
        ret.add(CertificateImpl.class);
        return ret;
    }
}
