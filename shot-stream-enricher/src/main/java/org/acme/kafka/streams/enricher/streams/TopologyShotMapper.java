package org.acme.kafka.streams.enricher.streams;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

import org.acme.kafka.streams.enricher.model.ShipwarsPlayerDataWrapper;
import org.acme.kafka.streams.enricher.model.ShipwarsSerdes;
import org.acme.kafka.streams.enricher.model.ShipwarsShotDataWrapper;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.GlobalKTable;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Produced;
import org.jboss.logging.Logger;

@ApplicationScoped
public class TopologyShotMapper {

    private static final Logger LOG = Logger.getLogger(TopologyShotMapper.class);

    public static final String SHOTS_TOPIC = "shipwars-attacks";
    public static final String PLAYERS_TOPIC = "shipwars-players";
    public static final String SHOTS_LITE_TOPIC = "shipwars-attacks-lite";

    @Produces
    public Topology buildTopology() {
        StreamsBuilder builder = new StreamsBuilder();

        final Serde<ShipwarsPlayerDataWrapper> playerJsonDataSerde = ShipwarsSerdes.getPlayerJsonSerde();
        final Serde<ShipwarsShotDataWrapper> shotJsonDataSerde = ShipwarsSerdes.getShotJsonSerde();

        KStream<String, ShipwarsShotDataWrapper> shotsStream = builder.stream(
            SHOTS_TOPIC,
            Consumed.with(Serdes.String(), shotJsonDataSerde)
        );

        GlobalKTable<String, ShipwarsPlayerDataWrapper> players = builder.globalTable(
            PLAYERS_TOPIC,
            Consumed.with(Serdes.String(), playerJsonDataSerde)
        );

        shotsStream
            .join(
                players,
                (key, shot) -> {
                    // The join requires both source keys to match, i.e we need to
                    // construct a key from the incoming shot that matches a player
                    // key in the GlobalKTable.
                    //
                    // Players are keyed as "$GAME_ID:$PLAYER_ID", whereas shots are
                    // keyed as "$GAME_ID:$MATCH_ID", so we use the attacker ID to
                    // construct a matching key for the join operation.
                    //
                    // The key written to the output join topic is $GAME_ID:$MATCH_ID
                    String matchingKey = shot.getData().getGame() + ":" + shot.getData().getAttacker();

                    LOG.info("Shot with key \"" + key + "\" is rekeyed to player key \"" + matchingKey + "\"");

                    return matchingKey;
                },
                (shot, player) -> {
                    String playerType = player.getData().getHuman() ? "human" : "ai";
                    String playerName = player.getData().getUsername();
                    String shotResult = shot.getData().getHit() ? "hit" : "miss";
                    String shotOrigin = shot.getData().getOrigin().toCoordinateString();

                    // Create a new record String, e.g "human:miss:1,2"
                    String joinResult = playerType + ":" + shotResult + ":" + shotOrigin + ":" + playerName;

                    LOG.info("Shot join result: " + joinResult);

                    return joinResult;
                }
            )
            .to(
                SHOTS_LITE_TOPIC,
                Produced.with(Serdes.String(), Serdes.String())
            );

        return builder.build();
    }

}
