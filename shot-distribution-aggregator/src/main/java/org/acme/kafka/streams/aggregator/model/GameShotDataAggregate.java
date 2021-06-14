package org.acme.kafka.streams.aggregator.model;

import java.util.HashMap;


/**
 * Stores data for each cell in a given game. Key (String) is cell the
 * coordinate, e.g "0,1" and the data is defined as CellShotData.
 */
public class GameShotDataAggregate extends HashMap<String, CellShotData> {
    public GameShotDataAggregate updateWithShot (String enrichedShot) {
        // Incoming string is formatted as "ai:hit:2,1"
        String parts[] = enrichedShot.split(":");

        Boolean isAi = "ai".equals(parts[0]);
        Boolean isHit = "hit".equals(parts[1]);
        String key = parts[2];

        CellShotData csd = this.getOrDefault(key, new CellShotData());

        csd.updateFrom(isAi, isHit);

        this.put(key, csd);

        return this;
    }
}
