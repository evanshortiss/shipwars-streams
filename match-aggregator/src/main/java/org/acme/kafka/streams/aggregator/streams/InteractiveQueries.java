package org.acme.kafka.streams.aggregator.streams;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.acme.kafka.streams.aggregator.model.MatchAggregate;
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

    public HashMap<String, MatchAggregate> getAllShotData (Integer count) {
        HashMap<String, MatchAggregate> results = new HashMap<String, MatchAggregate>();
        ReadOnlyKeyValueStore<String, MatchAggregate> keyValueStore = getShotAnalysisStore();
        KeyValueIterator<String, MatchAggregate> it = keyValueStore.all();

        while (it.hasNext() && results.size() < count) {
            KeyValue<String, MatchAggregate> entry = it.next();
            results.put(entry.key, entry.value);
        }

        return results;
    }

    public HashMap<String, MatchAggregate> getMatchesForUser (String user) {
        HashMap<String, MatchAggregate> results = new HashMap<String, MatchAggregate>();
        ReadOnlyKeyValueStore<String, MatchAggregate> keyValueStore = getShotAnalysisStore();
        KeyValueIterator<String, MatchAggregate> it = keyValueStore.all();

        while (it.hasNext()) {
            KeyValue<String, MatchAggregate> entry = it.next();
            if (entry.value.human_name.equals(user) || entry.value.ai_name.equals(user)) {
                results.put(entry.key, entry.value);
            }
        }

        return results;
    }

    private ReadOnlyKeyValueStore<String, MatchAggregate> getShotAnalysisStore() {
        while (true) {
            try {
                return streams.store(TopologyMatchAggregates.MATCHES_STORE, QueryableStoreTypes.keyValueStore());
            } catch (InvalidStateStoreException e) {
                // ignore, store not ready yet
            }
        }
    }
}
