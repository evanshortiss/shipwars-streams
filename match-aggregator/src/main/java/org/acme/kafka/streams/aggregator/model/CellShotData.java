package org.acme.kafka.streams.aggregator.model;

public class CellShotData {
    public Boolean hit;
    public Boolean ai;
    public Integer x;
    public Integer y;

    public CellShotData () {}
    public CellShotData (Boolean hit, Boolean ai, Integer x, Integer y) {
        this.hit = hit;
        this.ai = ai;
        this.x = x;
        this.y = y;
    }
}
