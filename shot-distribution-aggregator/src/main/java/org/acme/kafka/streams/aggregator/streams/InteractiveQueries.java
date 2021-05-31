package org.acme.kafka.streams.aggregator.streams;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.acme.kafka.streams.aggregator.model.ShipwarsShotDataAggregate;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.errors.InvalidStateStoreException;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class InteractiveQueries {

    @ConfigProperty(name = "hostname")
    String host;

    @Inject
    KafkaStreams streams;

    public List<PipelineMetadata> getMetaData() {
        return streams.allMetadataForStore(TopologyShotAnalysis.SHOTS_ANALYSIS_STORE)
            .stream()
            .map(m -> new PipelineMetadata(
                m.hostInfo().host() + ":" + m.hostInfo().port(),
                m.topicPartitions()
                    .stream()
                    .map(TopicPartition::toString)
                    .collect(Collectors.toSet())))
            .collect(Collectors.toList());
    }

    public HashMap<String, ShipwarsShotDataAggregate> getAllShotData () {
        HashMap<String, ShipwarsShotDataAggregate> results = new HashMap<String, ShipwarsShotDataAggregate>();
        ReadOnlyKeyValueStore<String, ShipwarsShotDataAggregate> keyValueStore = getShotAnalysisStore();
        KeyValueIterator<String, ShipwarsShotDataAggregate> it = keyValueStore.all();

        while (it.hasNext()) {
            KeyValue<String, ShipwarsShotDataAggregate> entry = it.next();
            results.put(entry.key, entry.value);
        }

        return results;
    }

    private ReadOnlyKeyValueStore<String, ShipwarsShotDataAggregate> getShotAnalysisStore() {
        while (true) {
            try {
                return streams.store(TopologyShotAnalysis.SHOTS_ANALYSIS_STORE, QueryableStoreTypes.keyValueStore());
            } catch (InvalidStateStoreException e) {
                // ignore, store not ready yet
            }
        }
    }
}
