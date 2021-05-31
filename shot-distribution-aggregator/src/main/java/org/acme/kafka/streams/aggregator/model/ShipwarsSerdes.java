package org.acme.kafka.streams.aggregator.model;

import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.Serializer;

public class ShipwarsSerdes {
    final static Map<String, Object> serdeProps = new HashMap<>();

    public static Serde<ShipwarsShotDataWrapperJSON> getShotJsonSerde () {
        final Serializer<ShipwarsShotDataWrapperJSON> serializer = new JsonPOJOSerializer<>();
        serdeProps.put("JsonPOJOClass", ShipwarsShotDataWrapperJSON.class);
        serializer.configure(serdeProps, false);

        final Deserializer<ShipwarsShotDataWrapperJSON> deserializer = new JsonPOJODeserializer<>();
        serdeProps.put("JsonPOJOClass", ShipwarsShotDataWrapperJSON.class);
        deserializer.configure(serdeProps, false);

        final Serde<ShipwarsShotDataWrapperJSON> shotDataSerde = Serdes.serdeFrom(serializer, deserializer);

        return shotDataSerde;
    }
}
