package uk.ac.ed.inf;

import java.net.URL;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

public class Restaurant {
    @JsonCreator
    public Restaurant(@JsonProperty("name") String name, @JsonProperty("longitude") double longitude, @JsonProperty("latitude") double latitude, @JsonProperty("menu") Menu[] menu) {
        this.name = name;
        this.lnglat = new LngLat(longitude, latitude);
        this.menu = menu;
    }

    private final String name;
    private final LngLat lnglat;
    private final Menu[] menu;

    public String getName() {
        return name;
    }

    public LngLat getLnglat() { return lnglat; }

    public Menu[] getMenu() {
        return menu;
    }

    static Restaurant[] getRestaurantsFromRestServer(URL serverBaseAddress) throws IOException {
        return new ObjectMapper().readValue(new URL(serverBaseAddress + "restaurants/"), Restaurant[].class);
    }
}
