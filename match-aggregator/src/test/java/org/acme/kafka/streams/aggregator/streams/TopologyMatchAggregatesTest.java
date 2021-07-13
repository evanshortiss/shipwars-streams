package org.acme.kafka.streams.aggregator.streams;

import static org.acme.kafka.streams.aggregator.streams.TopologyMatchAggregates.MATCHES_AGGREGATE_TOPIC;
import static org.acme.kafka.streams.aggregator.streams.TopologyMatchAggregates.SHOTS_TOPIC;
import static org.acme.kafka.streams.aggregator.streams.TopologyMatchAggregates.MATCHES_STORE;


import java.util.Properties;
import java.util.Set;

import javax.inject.Inject;

import org.acme.kafka.streams.aggregator.model.MatchAggregates;
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
public class TopologyMatchAggregatesTest {

    @Inject
    Topology topology;
    TopologyTestDriver testDriver;
    TestInputTopic<String, String> shotsIn;
    TestOutputTopic<String, MatchAggregates> aggregateOut;

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
            MATCHES_AGGREGATE_TOPIC,
            new StringDeserializer(),
            new ObjectMapperDeserializer<>(MatchAggregates.class)
        );
    }

    @AfterEach
    public void tearDown(){
        testDriver.getTimestampedKeyValueStore(MATCHES_STORE).flush();
        testDriver.close();
    }

    @Test
    public void testAggregation (){
        String key1 = "gameA:matchA";
        String shot1 = "ai:hit:0,0";

        String key2 = "gameA:matchB";
        String shot2 = "human:miss:1,3";

        shotsIn.pipeInput(key1, shot1);
        shotsIn.pipeInput(key2, shot2);

        TestRecord<String, MatchAggregates> result1 = aggregateOut.readRecord();
        TestRecord<String, MatchAggregates> result2 = aggregateOut.readRecord();

        Assertions.assertEquals("gameA:matchA", result1.getKey());
        Assertions.assertEquals(0, result1.getValue().get(0).x);
        Assertions.assertEquals(0, result1.getValue().get(0).y);
        Assertions.assertEquals(true, result1.getValue().get(0).ai);
        Assertions.assertEquals(true, result1.getValue().get(0).hit);

        Assertions.assertEquals("gameA:matchB", result2.getKey());
        Assertions.assertEquals(1, result2.getValue().get(0).x);
        Assertions.assertEquals(3, result2.getValue().get(0).y);
        Assertions.assertEquals(false, result2.getValue().get(0).ai);
        Assertions.assertEquals(false, result2.getValue().get(0).hit);
    }
}
