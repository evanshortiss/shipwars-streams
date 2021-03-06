package org.acme.kafka.streams.aggregator.streams;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

import org.acme.kafka.streams.aggregator.model.GameShotDataAggregate;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KeyValue;
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
public class TopologyShotAnalysis {

    private static final Logger LOG = Logger.getLogger(TopologyShotAnalysis.class);

    static final String SHOTS_TOPIC = "shipwars-attacks-lite";
    static final String SHOTS_ANALYSIS_STORE = "shipwars-streams-shots-aggregate-store";
    static final String SHOTS_ANALYSIS_AGGREGATE_TOPIC = "shipwars-streams-shots-aggregate";

    @Produces
    public Topology buildTopology() {
        StreamsBuilder builder = new StreamsBuilder();

        final ObjectMapperSerde<GameShotDataAggregate> aggregateSerde = new ObjectMapperSerde<>(GameShotDataAggregate.class);
        KeyValueBytesStoreSupplier storeSupplier = Stores.persistentKeyValueStore(SHOTS_ANALYSIS_STORE);

        /**
         * Aggregation topology. This creates an aggregate record of all shots
         * targeting specific cells for a given game generation. A generation
         * is a unique ID generated on the startup of the game server. Each
         * restart/deployment will be a new generation.
         */
        builder.stream(SHOTS_TOPIC, Consumed.with(Serdes.String(), Serdes.String()))
            .map((k, v) -> {
                // Use the overall game generation/id as the key so we keep a
                // record of shot distribution for the overall game generation
                // instead of per match. The generation changes when the game
                // server is redeployed
                String newKey = k.split(":")[0];
                LOG.info("Rekey incoming key (" + k + ") as: " + newKey);
                return KeyValue.pair(newKey, v);
            })
            .groupByKey(Grouped.with(Serdes.String(), Serdes.String()))
            .aggregate(
                GameShotDataAggregate::new,
                (gameId, payload, aggregation) -> {
                    LOG.info("Update shots record for game \"" + gameId + "\"" + " with data: " + payload);

                    return aggregation.updateWithShot(payload);
                },
                Materialized.<String, GameShotDataAggregate> as(storeSupplier)
                    .withKeySerde(Serdes.String())
                    .withValueSerde(aggregateSerde)
            )
            .toStream()
            .to(
                SHOTS_ANALYSIS_AGGREGATE_TOPIC,
                Produced.with(Serdes.String(), aggregateSerde)
            );

        return builder.build();
    }

}
