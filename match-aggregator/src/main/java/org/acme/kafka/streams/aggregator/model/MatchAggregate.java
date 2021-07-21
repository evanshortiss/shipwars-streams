package org.acme.kafka.streams.aggregator.model;

import java.util.ArrayList;


/**
 * Representation of a match aggregate. Turns are added to the ArrayList in
 * in order. Order is guaranteed since each turn uses the same partition key.
 */
public class MatchAggregate {
    public ArrayList<CellShotData> shots;
    public String ai_name;
    public String human_name;

    public MatchAggregate () {
        this.shots = new ArrayList<CellShotData>();
    }

    public MatchAggregate updateWithShotForMatch (String shot, String aggId) {
        // Incoming string is formatted as "ai:hit:2,1" or "human:miss:2,1"
        String parts[] = shot.split(":");

        Boolean hit = "hit".equals(parts[1]);
        Boolean ai = "ai".equals(parts[0]);
        Integer x = Integer.parseInt(parts[2].split(",")[0]);
        Integer y = Integer.parseInt(parts[2].split(",")[1]);
        String name = parts[3];

        if (ai == true) {
            this.ai_name = name;
        } else {
            this.human_name = name;
        }

        CellShotData s = new CellShotData(hit, ai, x, y);

        this.shots.add(s);

        return this;
    }

}
