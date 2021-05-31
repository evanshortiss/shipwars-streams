package org.acme.kafka.streams.aggregator.model;
import java.util.HashMap;

import org.jboss.logging.Logger;

public class ShipwarsShotDataAggregate {
    private static final Logger LOG = Logger.getLogger(ShipwarsShotDataAggregate.class);

    public HashMap<String, Integer> shotCountData;

    public ShipwarsShotDataAggregate() {
        this.shotCountData = new HashMap<String, Integer>();
    }

    public ShipwarsShotDataAggregate updateFrom (String gameId, ShipwarsShotDataJSON payload) {
        // Create key with format "x,y"
        String key = payload.getOrigin().toCoordinateString();

        if (this.shotCountData.containsKey(key) == false) {
            LOG.info("This is the first shot on cell " + key + " for game " + gameId);
            this.shotCountData.put(key, 1);
        } else {
            Integer count = this.shotCountData.get(key);
            LOG.info("Existing count for cell " + key + " game " + gameId + " is " + count.toString());
            this.shotCountData.replace(key, count + 1);
        }

        return this;
    }
}
