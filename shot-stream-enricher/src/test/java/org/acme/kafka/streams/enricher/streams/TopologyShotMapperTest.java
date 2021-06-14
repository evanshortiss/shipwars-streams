package org.acme.kafka.streams.enricher.streams;

import static org.acme.kafka.streams.enricher.streams.TopologyShotMapper.PLAYERS_TOPIC;
import static org.acme.kafka.streams.enricher.streams.TopologyShotMapper.SHOTS_TOPIC;
import static org.acme.kafka.streams.enricher.streams.TopologyShotMapper.SHOTS_LITE_TOPIC;


import java.util.Properties;

import javax.inject.Inject;

import org.acme.kafka.streams.enricher.model.ShipwarsPlayerData;
import org.acme.kafka.streams.enricher.model.ShipwarsPlayerDataWrapper;
import org.acme.kafka.streams.enricher.model.ShipwarsSerdes;
import org.acme.kafka.streams.enricher.model.ShipwarsShotData;
import org.acme.kafka.streams.enricher.model.ShipwarsShotDataWrapper;
import org.acme.kafka.streams.enricher.model.ShipwarsShotOrigin;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.TestInputTopic;
import org.apache.kafka.streams.TestOutputTopic;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.TopologyTestDriver;
import org.apache.kafka.streams.test.TestRecord;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

/**
 * Testing of the Topology without a broker, using TopologyTestDriver
 */
@QuarkusTest
public class TopologyShotMapperTest {

    @Inject
    Topology topology;
    TopologyTestDriver testDriver;
    TestInputTopic<String, ShipwarsShotDataWrapper> shotsIn;
    TestInputTopic<String, ShipwarsPlayerDataWrapper> playersIn;
    TestOutputTopic<String, String> joinedTopic;

    @BeforeEach
    public void setUp(){
        Properties config = new Properties();
        config.put(StreamsConfig.APPLICATION_ID_CONFIG, "testApplicationId");
        config.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "dummy:1234");
        testDriver = new TopologyTestDriver(topology, config);

        shotsIn = testDriver.createInputTopic(
            SHOTS_TOPIC,
            new StringSerializer(),
            ShipwarsSerdes.getShotJsonSerde().serializer()
        );

        playersIn = testDriver.createInputTopic(
            PLAYERS_TOPIC,
            new StringSerializer(),
            ShipwarsSerdes.getPlayerJsonSerde().serializer()
        );

        joinedTopic = testDriver.createOutputTopic(
            SHOTS_LITE_TOPIC,
            new StringDeserializer(),
            new StringDeserializer()
        );
    }

    @AfterEach
    public void tearDown(){
        testDriver.close();
    }

    @Test
    public void testJoinHumanHit () {
        String gameId = "gameId";
        String playerId = "playerId";
        String matchId = "matchId";

        String playerKey = gameId + ":" + playerId;
        String shotKey = gameId + ":" + matchId;

        ShipwarsShotOrigin origin = new ShipwarsShotOrigin();
        origin.setX(0);
        origin.setY(0);
        ShipwarsShotData shot = new ShipwarsShotData();
        shot.setAttacker(playerId);
        shot.setGame(gameId);
        shot.setMatch(matchId);
        shot.setDestroyed("Carrier");
        shot.setHit(true);
        shot.setOrigin(origin);
        shot.setScoreDelta(5);
        ShipwarsShotDataWrapper shotWrapper = new ShipwarsShotDataWrapper();
        shotWrapper.setData(shot);

        ShipwarsPlayerDataWrapper playerWrapper = new ShipwarsPlayerDataWrapper();
        ShipwarsPlayerData playerData = new ShipwarsPlayerData();
        playerData.setHuman(true);
        playerData.setUsername("Big Dinosaur");
        playerData.setUuid(playerId);
        playerWrapper.setData(playerData);

        playersIn.pipeInput(playerKey, playerWrapper);
        shotsIn.pipeInput(shotKey, shotWrapper);

        TestRecord<String, String> result = joinedTopic.readRecord();;

        Assertions.assertEquals("human:hit:0,0", result.getValue());
    }

    @Test
    public void testJoinAiMiss () {
        String gameId = "gameId";
        String playerId = "playerId";
        String matchId = "matchId";

        String playerKey = gameId + ":" + playerId;
        String shotKey = gameId + ":" + matchId;

        ShipwarsShotOrigin origin = new ShipwarsShotOrigin();
        origin.setX(0);
        origin.setY(0);
        ShipwarsShotData shot = new ShipwarsShotData();
        shot.setAttacker(playerId);
        shot.setGame(gameId);
        shot.setMatch(matchId);
        shot.setHit(false);
        shot.setOrigin(origin);
        shot.setScoreDelta(5);
        ShipwarsShotDataWrapper shotWrapper = new ShipwarsShotDataWrapper();
        shotWrapper.setData(shot);

        ShipwarsPlayerDataWrapper playerWrapper = new ShipwarsPlayerDataWrapper();
        ShipwarsPlayerData playerData = new ShipwarsPlayerData();
        playerData.setHuman(false);
        playerData.setUsername("Big Dinosaur");
        playerData.setUuid(playerId);
        playerWrapper.setData(playerData);

        playersIn.pipeInput(playerKey, playerWrapper);
        shotsIn.pipeInput(shotKey, shotWrapper);

        TestRecord<String, String> result = joinedTopic.readRecord();;

        Assertions.assertEquals("ai:miss:0,0", result.getValue());
    }
}
