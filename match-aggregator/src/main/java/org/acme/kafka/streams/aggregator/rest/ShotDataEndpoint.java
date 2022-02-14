package org.acme.kafka.streams.aggregator.rest;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.acme.kafka.streams.aggregator.streams.InteractiveQueries;

@ApplicationScoped
@Path("/replays")
public class ShotDataEndpoint {

    @Inject
    InteractiveQueries interactiveQueries;

    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllShotAnalysisData (@QueryParam("count") Integer count) {
        if (count == null) {
            // Default to twelve since the replay UI assumes this number of entries
            count = 12;
        }

        return Response.ok(interactiveQueries.getAllShotData(count)).build();
    }

    @GET
    @Path("/{user}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getGamesForUsername (@PathParam("user") String user) {
        return Response.ok(interactiveQueries.getMatchesForUser(user)).build();
    }
}
