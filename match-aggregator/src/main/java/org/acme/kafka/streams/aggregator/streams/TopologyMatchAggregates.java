package org.acme.kafka.streams.aggregator.streams;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

import org.acme.kafka.streams.aggregator.model.MatchAggregates;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Grouped;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.state.KeyValueBytesStoreSupplier;
import org.apache.kafka.streams.state.Stores;
import org.jboss.logging.Logger;
import io.quarkus.kafka.client.serialization.ObjectMapperSerde;

@ApplicationScoped
public class TopologyMatchAggregates {

    private static final Logger LOG = Logger.getLogger(TopologyMatchAggregates.class);

    static final String SHOTS_TOPIC = "shipwars-attacks-lite";
    static final String MATCHES_STORE = "shipwars-streams-matches-aggregate-store";
    static final String MATCHES_AGGREGATE_TOPIC = "shipwars-streams-matches-aggregate";

    @Produces
    public Topology buildTopology() {
        StreamsBuilder builder = new StreamsBuilder();

        final ObjectMapperSerde<MatchAggregates> aggregateSerde = new ObjectMapperSerde<>(MatchAggregates.class);
        KeyValueBytesStoreSupplier storeSupplier = Stores.persistentKeyValueStore(MATCHES_STORE);

        builder.stream(SHOTS_TOPIC, Consumed.with(Serdes.String(), Serdes.String()))
            .groupByKey(Grouped.with(Serdes.String(), Serdes.String()))
            .aggregate(
                MatchAggregates::new,
                (aggId, shot, aggregation) -> {
                    LOG.info("Update aggregate record for game \"" + aggId + "\"" + " with data: " + shot);

                    return aggregation.updateWithShotForMatch(shot, aggId);
                },
                Materialized.<String, MatchAggregates> as(storeSupplier)
                    .withKeySerde(Serdes.String())
                    .withValueSerde(aggregateSerde)
            )
            .toStream()
            .to(
                MATCHES_AGGREGATE_TOPIC,
                Produced.with(Serdes.String(), aggregateSerde)
            );

        return builder.build();
    }

}
