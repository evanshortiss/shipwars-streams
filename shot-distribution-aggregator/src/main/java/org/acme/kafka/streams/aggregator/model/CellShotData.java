package org.acme.kafka.streams.aggregator.model;

public class CellShotData {
    public Integer ai_miss;
    public Integer ai_hit;
    public Integer human_miss;
    public Integer human_hit;

    public CellShotData () {
        this.ai_miss = 0;
        this.ai_hit = 0;
        this.human_miss = 0;
        this.human_hit = 0;
    }

    public void updateFrom (Boolean isAi, Boolean isHit) {
        if (isAi == true) {
            if (isHit) {
                ai_hit++;
            } else {
                ai_miss++;
            }
        } else {
            if (isHit) {
                human_hit++;
            } else {
                human_miss++;
            }
        }
    }
}
