package org.acme.kafka.streams.aggregator.streams;

import static org.acme.kafka.streams.aggregator.streams.TopologyShotAnalysis.SHOTS_ANALYSIS_AGGREGATE_TOPIC;
import static org.acme.kafka.streams.aggregator.streams.TopologyShotAnalysis.SHOTS_TOPIC;
import static org.acme.kafka.streams.aggregator.streams.TopologyShotAnalysis.SHOTS_ANALYSIS_STORE;


import java.util.Properties;
import java.util.Set;

import javax.inject.Inject;

import org.acme.kafka.streams.aggregator.model.GameShotDataAggregate;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.TestInputTopic;
import org.apache.kafka.streams.TestOutputTopic;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.TopologyTestDriver;
import org.apache.kafka.streams.test.TestRecord;
import org.jboss.logging.Logger;

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
    private static final Logger LOG = Logger.getLogger(TopologyShotAnalysisTest.class);

    @Inject
    Topology topology;
    TopologyTestDriver testDriver;
    TestInputTopic<String, String> shotsIn;
    TestOutputTopic<String, GameShotDataAggregate> aggregateOut;

    @BeforeEach
    public void setUp(){
        Properties config = new Properties();
        config.put(StreamsConfig.APPLICATION_ID_CONFIG, "testApplicationId");
        config.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "dummy:1234");
        testDriver = new TopologyTestDriver(topology, config);

        shotsIn = testDriver.createInputTopic(
            SHOTS_TOPIC,
            new StringSerializer(),
            new StringSerializer()
        );
        aggregateOut = testDriver.createOutputTopic(
            SHOTS_ANALYSIS_AGGREGATE_TOPIC,
            new StringDeserializer(),
            new ObjectMapperDeserializer<>(GameShotDataAggregate.class)
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
        String shot1 = "ai:hit:0,0";

        String key2 = "gameA:matchB";
        String shot2 = "human:miss:1,3";

        shotsIn.pipeInput(key1, shot1);
        shotsIn.pipeInput(key2, shot2);

        TestRecord<String, GameShotDataAggregate> result1 = aggregateOut.readRecord();;
        TestRecord<String, GameShotDataAggregate> result2 = aggregateOut.readRecord();;

        Assertions.assertEquals("gameA", result1.getKey());
        Assertions.assertEquals(1, result1.getValue().get("0,0").ai_hit);
        Assertions.assertEquals(0, result1.getValue().get("0,0").ai_miss);

        Assertions.assertEquals("gameA", result2.getKey());
        Assertions.assertEquals(0, result2.getValue().get("1,3").human_hit);
        Assertions.assertEquals(1, result2.getValue().get("1,3").human_miss);
    }

    @Test
    public void testAggregationForDifferentGameGeneration (){
        String key1 = "gameX:matchA";
        String key2 = "gameY:matchA";
        String key3 = "gameY:matchB";


        String shot1 = "human:miss:3,3";
        String shot2 = "ai:hit:1,3";
        String shot3 = "ai:hit:1,2";

        shotsIn.pipeInput(key1, shot1);
        shotsIn.pipeInput(key2, shot2);
        shotsIn.pipeInput(key3, shot3);

        TestRecord<String, GameShotDataAggregate> result1 = aggregateOut.readRecord();
        TestRecord<String, GameShotDataAggregate> result2 = aggregateOut.readRecord();
        TestRecord<String, GameShotDataAggregate> result3 = aggregateOut.readRecord();

        Assertions.assertEquals("gameX", result1.getKey());
        Assertions.assertEquals(1, result1.getValue().get("3,3").human_miss);

        Assertions.assertEquals("gameY", result2.getKey());
        Assertions.assertEquals(1, result2.getValue().get("1,3").ai_hit);

        Assertions.assertEquals("gameY", result3.getKey());
        Assertions.assertEquals(1, result3.getValue().get("1,2").ai_hit);
    }
}
