
package org.mason.certscan.api;

import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;
import org.glassfish.jersey.server.ResourceConfig;
import org.mason.certscan.api.utils.AboutImpl;

import javax.ws.rs.ApplicationPath;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by bmason42 on 10/8/15.
 */
@ApplicationPath("utils")
public class UtilsApplication extends AbstractApplication {
    public UtilsApplication() {
    }

    @Override
    public Set<Class<?>> getServants() {
        Set<Class<?>> servants = super.getServants();
        Set<Class<?>> ret=new HashSet<>(servants);
        ret.add(AboutImpl.class);
        return ret;
    }
}
