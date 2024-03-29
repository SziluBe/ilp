package uk.ac.ed.inf.Models.Input;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.ac.ed.inf.Models.LngLat;

/**
 * Represents a restaurant.
 */
public record Restaurant(String name, LngLat lnglat, MenuItem[] menuItems) {
    /**
     * Default record constructor.
     *
     * @param String name The name of the restaurant.
     * @param LngLat lnglat The location of the restaurant.
     * @param MenuItem[] menuItems The menu items of the restaurant.
     */

    /**
     * Constructor annotated with @JsonCreator to enable Jackson de-serialisation
     *
     * @param name      The name of the restaurant
     * @param longitude The longitude coordinate of the restaurant
     * @param latitude  The latitude coordinate of the restaurant
     * @param menuItems The menu of the restaurant: an array of Menu objects
     */
    @JsonCreator
    private Restaurant(@JsonProperty("name") String name, @JsonProperty("longitude") double longitude, @JsonProperty("latitude") double latitude, @JsonProperty("menu") MenuItem[] menuItems) {
        this(name, new LngLat(longitude, latitude), menuItems);
    }
}
