package uk.ac.ed.inf;

import com.fasterxml.jackson.databind.ObjectMapper;
import uk.ac.ed.inf.Models.*;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;

public class ApplicationData {
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private final Restaurant[] restaurants;
    private Order[] orders;
    private final Area[] noFlyZones;
    private final Area centralArea;
    private final LngLat deliveryOrigin;

    // TODO: talk about how we use this instead of singletons in the report
    public ApplicationData(URL baseAddress, String date, LngLat deliveryOrigin) throws IOException {
        this.restaurants = MAPPER.readValue(new URL(baseAddress + "restaurants/"), Restaurant[].class);
        this.orders = MAPPER.readValue(new URL(baseAddress + "orders/" + date), Order[].class);
        this.noFlyZones = Arrays.stream((MAPPER.readValue(new URL(baseAddress + "noFlyZones"), NamedArea[].class))).map(NamedArea::getArea).toArray(Area[]::new);
        this.centralArea = MAPPER.readValue(new URL(baseAddress + "centralArea"), NamedArea.class).getArea();
        this.deliveryOrigin = deliveryOrigin;
//        // validate orders
//        Arrays.stream(this.orders).forEach(order -> order.setOutcome(order.validateOrder(restaurants)));
//        this.orders = Arrays.stream(this.orders).filter(order -> order.getOutcome() == OrderOutcome.ValidButNotDelivered).toArray(Order[]::new);
    }

    public Restaurant[] getRestaurants() {
        return restaurants;
    }

    public Order[] getOrders() {
        return orders;
    }

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
