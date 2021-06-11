package org.acme.kafka.streams.mapper.model;

import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.Serializer;

public class ShipwarsSerdes {
    final static Map<String, Object> serdeProps = new HashMap<>();

    public static Serde<ShipwarsShotDataWrapper> getShotJsonSerde () {
        final Serializer<ShipwarsShotDataWrapper> serializer = new JsonPOJOSerializer<>();
        serdeProps.put("JsonPOJOClass", ShipwarsShotDataWrapper.class);
        serializer.configure(serdeProps, false);

        final Deserializer<ShipwarsShotDataWrapper> deserializer = new JsonPOJODeserializer<>();
        serdeProps.put("JsonPOJOClass", ShipwarsShotDataWrapper.class);
        deserializer.configure(serdeProps, false);

        final Serde<ShipwarsShotDataWrapper> shotDataSerde = Serdes.serdeFrom(serializer, deserializer);

        return shotDataSerde;
    }

    public static Serde<ShipwarsPlayerDataWrapper> getPlayerJsonSerde () {
        final Serializer<ShipwarsPlayerDataWrapper> serializer = new JsonPOJOSerializer<>();
        serdeProps.put("JsonPOJOClass", ShipwarsMessageWrapper.class);
        serializer.configure(serdeProps, false);

        final Deserializer<ShipwarsPlayerDataWrapper> deserializer = new JsonPOJODeserializer<>();
        serdeProps.put("JsonPOJOClass", ShipwarsPlayerDataWrapper.class);
        deserializer.configure(serdeProps, false);

        final Serde<ShipwarsPlayerDataWrapper> shotDataSerde = Serdes.serdeFrom(serializer, deserializer);

        return shotDataSerde;
    }
}
