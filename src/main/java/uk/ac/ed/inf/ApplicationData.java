package uk.ac.ed.inf;

import com.fasterxml.jackson.databind.ObjectMapper;
import uk.ac.ed.inf.Models.*;
import uk.ac.ed.inf.Models.Input.Area;
import uk.ac.ed.inf.Models.Input.Order;
import uk.ac.ed.inf.Models.Input.Restaurant;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;

public record ApplicationData(Restaurant[] restaurants, Order[] orders, Area[] noFlyZones, Area centralArea, LngLat deliveryOrigin) {
    // TODO: talk about how we use this instead of singletons in the report
    public ApplicationData(URL baseAddress, String date, LngLat deliveryOrigin, ObjectMapper objectMapper) throws IOException {
        this(
                objectMapper.readValue(new URL(baseAddress + "restaurants/"), Restaurant[].class),
                objectMapper.readValue(new URL(baseAddress + "orders/" + date), Order[].class),
                Arrays.stream((objectMapper.readValue(new URL(baseAddress + "noFlyZones"), Area[].class))).toArray(Area[]::new),
                new Area(Arrays.stream(objectMapper.readValue(new URL(baseAddress + "centralArea"), LngLat[].class)).toArray(LngLat[]::new)),
                deliveryOrigin
        );
    }
}
