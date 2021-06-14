package org.acme.kafka.streams.enricher.model;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Generated;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "attacker",
    "scoreDelta",
    "destroyed",
    "hit",
    "origin"
})
@Generated("jsonschema2pojo")
public class ShipwarsShotData {

    @JsonProperty("game")
    private String game;
    @JsonProperty("match")
    private String match;
    @JsonProperty("attacker")
    private String attacker;
    @JsonProperty("scoreDelta")
    private Integer scoreDelta;
    @JsonProperty("destroyed")
    private String destroyed;
    @JsonProperty("hit")
    private Boolean hit;
    @JsonProperty("origin")
    private ShipwarsShotOrigin origin;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("game")
    public String getGame() {
        return game;
    }

    @JsonProperty("game")
    public void setGame(String game) {
        this.game = game;
    }

    @JsonProperty("match")
    public String getMatch() {
        return match;
    }

    @JsonProperty("match")
    public void setMatch(String match) {
        this.match = match;
    }

    @JsonProperty("attacker")
    public String getAttacker() {
        return attacker;
    }

    @JsonProperty("attacker")
    public void setAttacker(String attacker) {
        this.attacker = attacker;
    }

    @JsonProperty("scoreDelta")
    public Integer getScoreDelta() {
        return scoreDelta;
    }

    @JsonProperty("scoreDelta")
    public void setScoreDelta(Integer scoreDelta) {
    this.scoreDelta = scoreDelta;
    }

    @JsonProperty("destroyed")
    public String getDestroyed() {
        return destroyed;
    }

    @JsonProperty("destroyed")
    public void setDestroyed(String destroyed) {
        this.destroyed = destroyed;
    }

    @JsonProperty("hit")
    public Boolean getHit() {
        return hit;
    }

    @JsonProperty("hit")
    public void setHit(Boolean hit) {
        this.hit = hit;
    }

    @JsonProperty("origin")
    public ShipwarsShotOrigin getOrigin() {
        return origin;
    }

    @JsonProperty("origin")
    public void setOrigin(ShipwarsShotOrigin origin) {
        this.origin = origin;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}
