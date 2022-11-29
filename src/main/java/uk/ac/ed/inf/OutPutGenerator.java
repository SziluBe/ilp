package uk.ac.ed.inf;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;
import uk.ac.ed.inf.Models.LngLat;
import uk.ac.ed.inf.Models.Order;
import uk.ac.ed.inf.Models.Deliveries;
import uk.ac.ed.inf.Models.DeliveryEntry;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class OutPutGenerator {
    private final DeliveryPlanner deliveryPlanner;
    private final FlightpathCalculator flightpathCalculator;
    private final ObjectMapper objectMapper;

    public OutPutGenerator(DeliveryPlanner deliveryPlanner, FlightpathCalculator flightpathCalculator, ObjectMapper objectMapper) {
        this.deliveryPlanner = deliveryPlanner;
        this.flightpathCalculator = flightpathCalculator;
        this.objectMapper = objectMapper;
    }

//    public OutFlightPath generateFlightPathOutPut(Order[] orders) {
//        LngLat[] flightPath = generateFullFlightpath(orders);
//        // TODO + how do we get angles?
//        return null;
//    }

    // TODO: handle exception?
    public String generateDeliveriesOutPut(Order[] orders) throws IOException {
        ArrayList<DeliveryEntry> deliveryEntries = new ArrayList<>();
        for (Order order : orders) {
            // for invalid orders we don't need to worry about calculating the price,
            // for the rest it will be correct anyway
            deliveryEntries.add(new DeliveryEntry(order.getOrderNo(), order.getOutcome(), order.getPriceTotalInPence()));
        }
        Deliveries deliveries = new Deliveries(deliveryEntries.toArray(new DeliveryEntry[0]));

        return objectMapper.writeValueAsString(deliveries);
    }

    public String generateFlightPathGeoJsonOutPut() {
        ArrayList<LngLat> flightPath = flightpathCalculator.calculateFlightpath(deliveryPlanner.getDeliveredOrders());
        List<Point> flightPathPoints = flightPath.stream().map(lngLat -> Point.fromLngLat(lngLat.lng(), lngLat.lat())).toList();

        FeatureCollection flightPathGeoJson = FeatureCollection.fromFeatures(new Feature[]{Feature.fromGeometry(LineString.fromLngLats(flightPathPoints))});

        return flightPathGeoJson.toJson();
    }
}
