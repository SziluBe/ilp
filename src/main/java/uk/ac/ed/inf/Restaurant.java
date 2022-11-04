package uk.ac.ed.inf;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URL;

public class Restaurant {
    private final String name;
    private final LngLat lnglat;
    private final Menu[] menu;

    /**
     * Constructor annotated with @JsonCreator to enable Jackson de-serialisation
     *
     * @param name      The name of the restaurant
     * @param longitude The longitude coordinate of the restaurant
     * @param latitude  The latitude coordinate of the restaurant
     * @param menu      The menu of the restaurant: an array of Menu objects
     */
    @JsonCreator
    private Restaurant(@JsonProperty("name") String name, @JsonProperty("longitude") double longitude, @JsonProperty("latitude") double latitude, @JsonProperty("menu") Menu[] menu) {
        this.name = name;
        this.lnglat = new LngLat(longitude, latitude);
        this.menu = menu;
    }

    /**
     * Returns the current array of available restaurants from the 'restaurants/' endpoint of the given base address
     *
     * @param serverBaseAddress The base URL of the REST endpoint
     * @return The available restaurants de-serialised as an array of Restaurant objects
     * @throws IOException In case there is an issue retrieving the data
     */
    static Restaurant[] getRestaurantsFromRestServer(URL serverBaseAddress) throws IOException {
        return new ObjectMapper().readValue(new URL(serverBaseAddress + "restaurants/"), Restaurant[].class);
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
    public Menu[] getMenu() {
        return menu;
    }
}
