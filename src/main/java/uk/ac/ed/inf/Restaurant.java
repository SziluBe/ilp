package uk.ac.ed.inf;

import java.net.URL;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.MalformedURLException;
import java.io.IOException;

public class Restaurant {
    public Restaurant(String name, double longitude, double latitude, Menu[] menu){
        // TODO: maybe validate inputs?
        this.name = name;
        this.longitude = longitude;
        this.latitude = latitude;
        this.menu = menu;
    }

    private final String name;
    private final double longitude;
    private final double latitude;
    private final Menu[] menu;

    public String getName() {
        return name;
    }

    public double getLongitude() {
        return longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public Menu[] getMenu() {
        return menu;
    }

    static Restaurant[] getRestaurantsFromRestServer(URL serverBaseAddress) {
        try {
            return new ObjectMapper().readValue(new URL(serverBaseAddress + "restaurants/"), Restaurant[].class);
        } catch (IOException e) {
            // TODO
            e.printStackTrace();
            return null;
        }
    }
}
