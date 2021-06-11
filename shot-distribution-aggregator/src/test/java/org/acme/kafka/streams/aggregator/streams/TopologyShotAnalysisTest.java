package org.acme.kafka.streams.aggregator.streams;

import static org.acme.kafka.streams.aggregator.streams.TopologyShotAnalysis.SHOTS_ANALYSIS_AGGREGATE_TOPIC;
import static org.acme.kafka.streams.aggregator.streams.TopologyShotAnalysis.SHOTS_TOPIC;
import static org.acme.kafka.streams.aggregator.streams.TopologyShotAnalysis.SHOTS_ANALYSIS_STORE;


import java.util.Properties;

import javax.inject.Inject;

import org.acme.kafka.streams.aggregator.model.ShipwarsSerdes;
import org.acme.kafka.streams.aggregator.model.ShipwarsShotDataAggregate;
import org.acme.kafka.streams.aggregator.model.ShipwarsShotData;
import org.acme.kafka.streams.aggregator.model.ShipwarsShotDataWrapper;
import org.acme.kafka.streams.aggregator.model.ShipwarsShotOrigin;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.TestInputTopic;
import org.apache.kafka.streams.TestOutputTopic;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.TopologyTestDriver;
import org.apache.kafka.streams.test.TestRecord;

import io.quarkus.kafka.client.serialization.ObjectMapperDeserializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

/**
 * Testing of the Topology without a broker, using TopologyTestDriver
 */
@QuarkusTest
public class TopologyShotAnalysisTest {

    @Inject
    Topology topology;
    TopologyTestDriver testDriver;
    TestInputTopic<String, ShipwarsShotDataWrapper> shotsIn;
    TestOutputTopic<String, ShipwarsShotDataAggregate> aggregateOut;

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
        aggregateOut = testDriver.createOutputTopic(
            SHOTS_ANALYSIS_AGGREGATE_TOPIC,
            new StringDeserializer(),
            new ObjectMapperDeserializer<>(ShipwarsShotDataAggregate.class)
        );
    }

    @AfterEach
    public void tearDown(){
        testDriver.getTimestampedKeyValueStore(SHOTS_ANALYSIS_STORE).flush();
        testDriver.close();
    }

    @Test
    public void testAggregationForSameGameGeneration (){
        String key1 = "gameA:matchA";
        String key2 = "gameA:matchB";

        ShipwarsShotOrigin origin = new ShipwarsShotOrigin();
        origin.setX(0);
        origin.setY(0);

        ShipwarsShotData shot1 = new ShipwarsShotData();
        shot1.setAttacker("Jane");
        shot1.setDestroyed("Carrier");
        shot1.setHit(true);
        shot1.setOrigin(origin);
        shot1.setScoreDelta(5);

        ShipwarsShotData shot2 = new ShipwarsShotData();
        shot2.setAttacker("Jane");
        shot2.setDestroyed("Carrier");
        shot2.setHit(true);
        shot2.setOrigin(origin);
        shot2.setScoreDelta(5);

        // Write the same shot for two separate matches (matchA and matchB)
        ShipwarsShotDataWrapper w1 = new ShipwarsShotDataWrapper();
        w1.setData(shot1);
        ShipwarsShotDataWrapper w2 = new ShipwarsShotDataWrapper();
        w2.setData(shot2);

        shotsIn.pipeInput(key1, w1);
        shotsIn.pipeInput(key2, w2);

        TestRecord<String, ShipwarsShotDataAggregate> result1 = aggregateOut.readRecord();;
        TestRecord<String, ShipwarsShotDataAggregate> result2 = aggregateOut.readRecord();;

        Assertions.assertEquals(1, result1.getValue().shotCountData.get("0,0"));
        Assertions.assertEquals(2, result2.getValue().shotCountData.get("0,0"));
    }

    @Test
    public void testAggregationForDifferentGameGeneration (){
        String key1 = "gameX:matchA";
        String key2 = "gameY:matchA";
        String key3 = "gameY:matchB";

        ShipwarsShotOrigin origin = new ShipwarsShotOrigin();
        origin.setX(0);
        origin.setY(0);

        ShipwarsShotData shot1 = new ShipwarsShotData();
        shot1.setAttacker("Jane");
        shot1.setDestroyed("Carrier");
        shot1.setHit(true);
        shot1.setOrigin(origin);
        shot1.setScoreDelta(5);

        ShipwarsShotData shot2 = new ShipwarsShotData();
        shot2.setAttacker("Jane");
        shot2.setDestroyed("Carrier");
        shot2.setHit(true);
        shot2.setOrigin(origin);
        shot2.setScoreDelta(5);

        ShipwarsShotData shot3 = new ShipwarsShotData();
        shot3.setAttacker("Jane");
        shot3.setDestroyed("Carrier");
        shot3.setHit(true);
        shot3.setOrigin(origin);
        shot3.setScoreDelta(5);

        ShipwarsShotDataWrapper w1 = new ShipwarsShotDataWrapper();
        w1.setData(shot1);
        shotsIn.pipeInput(key1, w1);

        ShipwarsShotDataWrapper w2 = new ShipwarsShotDataWrapper();
        w2.setData(shot2);
        shotsIn.pipeInput(key2, w2);

        ShipwarsShotDataWrapper w3 = new ShipwarsShotDataWrapper();
        w3.setData(shot3);
        shotsIn.pipeInput(key3, w3);

        TestRecord<String, ShipwarsShotDataAggregate> result1 = aggregateOut.readRecord();
        TestRecord<String, ShipwarsShotDataAggregate> result2 = aggregateOut.readRecord();
        TestRecord<String, ShipwarsShotDataAggregate> result3 = aggregateOut.readRecord();

        Assertions.assertEquals(1, result1.getValue().shotCountData.get("0,0"));
        Assertions.assertEquals(1, result2.getValue().shotCountData.get("0,0"));

        // The "gameY" key should have two hits (from two different games) on 0,0
        Assertions.assertEquals(2, result3.getValue().shotCountData.get("0,0"));
    }
}
