package org.acme.kafka.streams.aggregator.streams;

import static org.acme.kafka.streams.aggregator.streams.TopologyMatchAggregates.MATCHES_AGGREGATE_TOPIC;
import static org.acme.kafka.streams.aggregator.streams.TopologyMatchAggregates.SHOTS_TOPIC;
import static org.acme.kafka.streams.aggregator.streams.TopologyMatchAggregates.MATCHES_STORE;


import java.util.Properties;
import java.util.Set;

import javax.inject.Inject;

import org.acme.kafka.streams.aggregator.model.MatchAggregate;
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
    TestOutputTopic<String, MatchAggregate> aggregateOut;

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
            new ObjectMapperDeserializer<>(MatchAggregate.class)
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
        String shot1 = "ai:hit:0,0:Shiba Inu";

        String key2 = "gameA:matchB";
        String shot2 = "human:miss:1,3:Jindo";

        String key3 = "gameA:matchB";
        String shot3 = "ai:miss:2,2:Kishu Ken";

        shotsIn.pipeInput(key1, shot1);
        shotsIn.pipeInput(key2, shot2);

        TestRecord<String, MatchAggregate> result1 = aggregateOut.readRecord();
        TestRecord<String, MatchAggregate> result2 = aggregateOut.readRecord();

        Assertions.assertEquals("gameA:matchA", result1.getKey());
        Assertions.assertEquals(0, result1.getValue().shots.get(0).x);
        Assertions.assertEquals(0, result1.getValue().shots.get(0).y);
        Assertions.assertEquals(true, result1.getValue().shots.get(0).ai);
        Assertions.assertEquals(true, result1.getValue().shots.get(0).hit);
        Assertions.assertEquals(null, result1.getValue().human_name);
        Assertions.assertEquals("Shiba Inu", result1.getValue().ai_name);

        Assertions.assertEquals("gameA:matchB", result2.getKey());
        Assertions.assertEquals(1, result2.getValue().shots.get(0).x);
        Assertions.assertEquals(3, result2.getValue().shots.get(0).y);
        Assertions.assertEquals(false, result2.getValue().shots.get(0).ai);
        Assertions.assertEquals(false, result2.getValue().shots.get(0).hit);
        Assertions.assertEquals("Jindo", result2.getValue().human_name);
        Assertions.assertEquals(null, result2.getValue().ai_name);

        shotsIn.pipeInput(key3, shot3);
        TestRecord<String, MatchAggregate> result3 = aggregateOut.readRecord();
        Assertions.assertEquals("gameA:matchB", result3.getKey());
        Assertions.assertEquals(2, result3.getValue().shots.get(1).x);
        Assertions.assertEquals(2, result3.getValue().shots.get(1).y);
        Assertions.assertEquals(true, result3.getValue().shots.get(1).ai);
        Assertions.assertEquals(false, result3.getValue().shots.get(1).hit);
        Assertions.assertEquals("Jindo", result3.getValue().human_name);
        Assertions.assertEquals("Kishu Ken", result3.getValue().ai_name);
    }
}
