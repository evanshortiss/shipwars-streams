package org.acme.kafka.streams.aggregator.rest;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.acme.kafka.streams.aggregator.streams.InteractiveQueries;
import org.jboss.resteasy.annotations.SseElementType;
import io.smallrye.reactive.messaging.annotations.Broadcast;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.reactivestreams.Publisher;

@ApplicationScoped
@Path("/shot-distribution")
public class ShotDataEndpoint {

    @Inject
    InteractiveQueries interactiveQueries;

    @Inject
    @Broadcast
    @Channel("shipwars-attacks")
    Publisher<String> shots;

    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllShotAnalysisData () {
        return Response.ok(interactiveQueries.getAllShotData()).build();
    }

    @GET
    @Path("/stream")
    @Produces(MediaType.SERVER_SENT_EVENTS)
    @SseElementType("text/json")
    public Publisher<String> getAllShotDataStream () {
        return shots;
    }
}
