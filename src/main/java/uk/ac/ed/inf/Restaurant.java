package uk.ac.ed.inf;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.IOException;
import java.net.URL;

public class Restaurant {
    private final String name;
    private final LngLat lnglat;
    private final Menu[] menu;
    private static Restaurant[] restaurants;
    private static URL baseAddress;

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
//    public static Restaurant[] getRestaurantsFromRestServer(URL serverBaseAddress) throws IOException {
//        return
//    }

//    public static Restaurant[] getRestaurants(URL serverBaseAddress) throws IOException {
//        if (restaurants == null) {
//            restaurants = getRestaurantsFromRestServer(serverBaseAddress);
//            baseAddress = serverBaseAddress;
//        }
//        else if (!serverBaseAddress.equals(baseAddress)) {
//            // TODO: is err the right place to print this?
//            System.err.println("Warning: Restaurants base address has changed from " + baseAddress + " to " + serverBaseAddress + ", but restaurants have already been fetched. This may cause unexpected behaviour.");
//        }
//        return restaurants;
//    }

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
