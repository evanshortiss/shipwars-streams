package org.acme.kafka.streams.aggregator.streams;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.acme.kafka.streams.aggregator.model.MatchAggregates;
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
        return streams.allMetadataForStore(TopologyMatchAggregates.MATCHES_STORE)
            .stream()
            .map(m -> new PipelineMetadata(
                m.hostInfo().host() + ":" + m.hostInfo().port(),
                m.topicPartitions()
                    .stream()
                    .map(TopicPartition::toString)
                    .collect(Collectors.toSet())))
            .collect(Collectors.toList());
    }

    public HashMap<String, MatchAggregates> getAllShotData (Integer count) {
        HashMap<String, MatchAggregates> results = new HashMap<String, MatchAggregates>();
        ReadOnlyKeyValueStore<String, MatchAggregates> keyValueStore = getShotAnalysisStore();
        KeyValueIterator<String, MatchAggregates> it = keyValueStore.all();

        while (it.hasNext() && results.size() < count) {
            KeyValue<String, MatchAggregates> entry = it.next();
            results.put(entry.key, entry.value);
        }

        return results;
    }

    private ReadOnlyKeyValueStore<String, MatchAggregates> getShotAnalysisStore() {
        while (true) {
            try {
                return streams.store(TopologyMatchAggregates.MATCHES_STORE, QueryableStoreTypes.keyValueStore());
            } catch (InvalidStateStoreException e) {
                // ignore, store not ready yet
            }
        }
    }
}
