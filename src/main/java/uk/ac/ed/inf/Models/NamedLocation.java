package uk.ac.ed.inf.Models;

import com.fasterxml.jackson.annotation.JsonCreator;

public class NamedLocation {
    private final String name;
    private final LngLat location;

    /**
     * Constructor annotated with @JsonCreator to enable Jackson de-serialisation
     *
     * @param name The name of the location
     * @param longitude The longitude coordinate of the location
     * @param latitude The latitude coordinate of the location
     */
    @JsonCreator
    // name, longitude, latitude
    private NamedLocation(@com.fasterxml.jackson.annotation.JsonProperty("name") String name, @com.fasterxml.jackson.annotation.JsonProperty("longitude") double longitude, @com.fasterxml.jackson.annotation.JsonProperty("latitude") double latitude) {
        this.name = name;
        this.location = new LngLat(longitude, latitude);
    }

    public LngLat getLocation() {
        return location;
    }
}
