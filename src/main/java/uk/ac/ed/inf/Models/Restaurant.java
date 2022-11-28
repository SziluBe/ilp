package uk.ac.ed.inf.Models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.IOException;
import java.net.URL;

public class Restaurant {
    private final String name;
    private final LngLat lnglat;
    private final MenuItem[] menuItems;
    private static Restaurant[] restaurants;
    private static URL baseAddress;

    /**
     * Constructor annotated with @JsonCreator to enable Jackson de-serialisation
     *
     * @param name      The name of the restaurant
     * @param longitude The longitude coordinate of the restaurant
     * @param latitude  The latitude coordinate of the restaurant
     * @param menuItems      The menu of the restaurant: an array of Menu objects
     */
    @JsonCreator
    private Restaurant(@JsonProperty("name") String name, @JsonProperty("longitude") double longitude, @JsonProperty("latitude") double latitude, @JsonProperty("menu") MenuItem[] menuItems) {
        this.name = name;
        this.lnglat = new LngLat(longitude, latitude);
        this.menuItems = menuItems;
    }

    /**
     * @return name
     */
    public String getName() {
        return name;
    }

    /**
     * @return lnglat   The restaurant's coordinates as a LngLat object
     */
    public LngLat getLnglat() {
        return lnglat;
    }

    /**
     * @return menu
     */
    public MenuItem[] getMenu() {
        return menuItems;
    }
}
