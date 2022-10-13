package uk.ac.ed.inf;

import java.net.URL;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.MalformedURLException;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Restaurant {
    public Restaurant(String name, double longitude, double latitude, Menu[] menu) {
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
        } catch (JsonMappingException e) {
            System.out.println("Uh oh, an error has occurred while parsing the restaurants' data. Are you sure the source data matches the required format?");
            e.printStackTrace();
            return null;
        } catch (JsonParseException e) {
            System.out.println("Uh oh, an error has occurred while parsing the restaurants' data. It seems like the source data is not valid JSON, are you sure you have provided a correct URL?");
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            System.out.println("Uh oh, an error has occurred while retrieving the restaurants' data. Are you sure you have provided a correct URL?");
            e.printStackTrace();
            return null;
        }
    }
}
