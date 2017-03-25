package org.mason.certscan.api.v1;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

/**
 * Created by masonb on 3/25/2017.
 */
@Path("certs")
@Api("certs")
public class CertificateImpl {
    @GET
    @ApiOperation("Gets the certs")
    public Response getCerts(){
        return Response.ok("hello").build();
    }
}
