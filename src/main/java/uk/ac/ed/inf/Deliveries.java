package uk.ac.ed.inf;

public class Deliveries {
    private final DeliveryEntry[] deliveryEntries;

    public Deliveries(DeliveryEntry[] deliveryEntries) {
        this.deliveryEntries = deliveryEntries;
    }

    public DeliveryEntry[] getDeliveryEntries() {
        return deliveryEntries;
    }
}
