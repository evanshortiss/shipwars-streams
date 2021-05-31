package org.acme.kafka.streams.aggregator.rest;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.acme.kafka.streams.aggregator.streams.InteractiveQueries;

@ApplicationScoped
@Path("/shot-distribution")
public class ShotDataEndpoint {

    @Inject
    InteractiveQueries interactiveQueries;

    @GET
    @Path("/")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllShotAnalysisData () {
        return Response.ok(interactiveQueries.getAllShotData()).build();
    }
}
