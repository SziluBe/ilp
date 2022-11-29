package uk.ac.ed.inf;

import com.fasterxml.jackson.databind.ObjectMapper;
import uk.ac.ed.inf.Models.*;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;

public class ApplicationData {
    private final Restaurant[] restaurants;
    // TODO: maybe change this to a list?
    private Order[] orders;
    private final Area[] noFlyZones;
    private final Area centralArea;
    private final LngLat deliveryOrigin;

    // TODO: talk about how we use this instead of singletons in the report
    // TODO: handle exceptions?
    // TODO: ensure no nulls?
    public ApplicationData(URL baseAddress, String date, LngLat deliveryOrigin, ObjectMapper objectMapper) throws IOException {
        this.restaurants = objectMapper.readValue(new URL(baseAddress + "restaurants/"), Restaurant[].class);
        this.orders = objectMapper.readValue(new URL(baseAddress + "orders/" + date), Order[].class);
        this.noFlyZones = Arrays.stream((objectMapper.readValue(new URL(baseAddress + "noFlyZones"), Area[].class))).toArray(Area[]::new);
        this.centralArea = new Area(Arrays.stream(objectMapper.readValue(new URL(baseAddress + "centralArea"), NamedLocation[].class)).map(NamedLocation::getLocation).toArray(LngLat[]::new));
        this.deliveryOrigin = deliveryOrigin;
    }

    public Restaurant[] getRestaurants() {
        return restaurants;
    }

    public Order[] getOrders() {
        return orders;
    }

    // TODO: maybe should use this?
    public void setOrders(Order[] orders) {
        this.orders = orders;
    }

    public Area[] getNoFlyZones() {
        return noFlyZones;
    }

    public Area getCentralArea() {
        return centralArea;
    }

    public LngLat getDeliveryOrigin() {
        return deliveryOrigin;
    }
}
