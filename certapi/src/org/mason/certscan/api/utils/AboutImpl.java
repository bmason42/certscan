package org.mason.certscan.api.utils;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.mason.certscan.api.model.About;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

/**
 * Created by masonb on 3/25/2017.
 */
@Path("about")
@Api("utils")
public class AboutImpl {
    @GET
    @Produces("application/json")
    @ApiOperation("Gives version information about the API")
    public About getVersion() {
        return new About();
    }
}