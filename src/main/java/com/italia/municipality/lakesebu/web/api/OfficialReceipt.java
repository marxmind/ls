package com.italia.municipality.lakesebu.web.api;

import jakarta.enterprise.context.RequestScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/official")
@RequestScoped
public class OfficialReceipt {

	@GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response sayHello() {
        var person = String.valueOf("00000");
        return Response.ok(person).build();
    }
}

