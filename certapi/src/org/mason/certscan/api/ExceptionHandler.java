package org.mason.certscan.api;

import org.glassfish.jersey.server.ContainerRequest;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Created by masonb on 3/25/2017.
 */
public class ExceptionHandler implements ExceptionMapper<Throwable> {

    @Inject
    private javax.inject.Provider<ContainerRequest> containerRequestProvider;
    @Inject
    private javax.inject.Provider<HttpServletRequest> requestProvider;

    public ExceptionHandler() {

    }


    @Override
    public Response toResponse(Throwable e) {
        Response ret;
        Locale clientLocale;
        if (requestProvider != null) {
            HttpServletRequest request = requestProvider.get();
            clientLocale = request.getLocale();
        } else {
            clientLocale = Locale.US;
        }
        ResourceBundle bundle = ResourceBundle.getBundle("org.mason.certscan.api.Messages", clientLocale);
        if (e instanceof WebApplicationException) {
            ret = ((WebApplicationException) e).getResponse();
        } else {
            Response.ResponseBuilder resp;
            resp = Response.status(422);
            ret = resp.entity("").build();
        }
        return ret;
    }
}

