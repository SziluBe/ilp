package uk.ac.ed.inf;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;

public class ApplicationData {
    private Restaurant[] restaurants;
    private URL baseAddress;
    private Order[] orders;
    private Area[] noFlyZones;
    private Area centralArea;
    private LngLat deliveryTarget;

    public ApplicationData(URL baseAddress, String date) throws IOException {
        this.baseAddress = baseAddress;
        this.restaurants = Constants.MAPPER.readValue(new URL(baseAddress + "restaurants/"), Restaurant[].class);
        this.orders = Constants.MAPPER.readValue(new URL(baseAddress + "orders/" + date), Order[].class);
        this.noFlyZones = Arrays.stream((Constants.MAPPER.readValue(new URL(baseAddress + "noFlyZones"), NamedArea[].class))).map(NamedArea::getArea).toArray(Area[]::new);
        this.centralArea = Constants.MAPPER.readValue(new URL(baseAddress + "centralArea"), NamedArea.class).getArea();
        this.deliveryTarget = Constants.AT;
    }

    public Restaurant[] getRestaurants() {
        return restaurants;
    }

    public Order[] getOrders() {
        return orders;
    }

    public Area[] getNoFlyZones() {
        return noFlyZones;
    }

    public Area getCentralArea() {
        return centralArea;
    }

    public LngLat getDeliveryTarget() {
        return deliveryTarget;
    }

    public URL getBaseAddress() {
        return baseAddress;
    }
}
