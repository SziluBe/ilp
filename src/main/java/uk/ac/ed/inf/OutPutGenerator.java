package uk.ac.ed.inf;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;
import uk.ac.ed.inf.Models.FlightPathEntry;
import uk.ac.ed.inf.Models.Input.Restaurant;
import uk.ac.ed.inf.Models.Input.Order;
import uk.ac.ed.inf.Models.Output.DeliveryEntry;
import uk.ac.ed.inf.Models.Output.OutFlightPathEntry;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class OutPutGenerator {
    private final DeliveryPlanner deliveryPlanner;
    private final FlightPathCalculator flightpathCalculator;
    private final ObjectMapper objectMapper;

    public OutPutGenerator(DeliveryPlanner deliveryPlanner,
                           FlightPathCalculator flightpathCalculator,
                           ObjectMapper objectMapper) {
        this.deliveryPlanner = deliveryPlanner;
        this.flightpathCalculator = flightpathCalculator;
        this.objectMapper = objectMapper;
    }

    public String generateFlightPathOutPut(Order[] deliveredOrders) throws JsonProcessingException {
        return objectMapper.writeValueAsString(generateOutFlightPath(deliveredOrders));
    }

    public String generateDeliveriesOutPut(Order[] orders) throws JsonProcessingException {
        List<DeliveryEntry> deliveryEntries = new ArrayList<>();
        for (Order order : orders) {
            // for invalid orders we don't need to worry about calculating the price,
            // for the rest it will be correct anyway
            deliveryEntries.add(
                    new DeliveryEntry(
                            order.orderNo(),
                            deliveryPlanner.getOrderOutcome(order),
                            order.priceTotalInPence()
                    )
            );
        }

        return objectMapper.writeValueAsString(deliveryEntries);
    }

    public String generateFlightPathGeoJsonOutPut() {
        Order[] deliveredOrders = deliveryPlanner.getDeliveredOrders();
        List<OutFlightPathEntry> outFlightPathEntries = generateOutFlightPath(deliveredOrders);

        List<Point> flightPathPoints = outFlightPathEntries.stream()
                .map(flightPathEntry -> Point.fromLngLat(flightPathEntry.fromLongitude(),
                                                         flightPathEntry.fromLatitude()))
                .collect(Collectors.toList()); // not .toList() because it's immutable

        FeatureCollection flightPathGeoJson = FeatureCollection.fromFeatures(
                new Feature[]{Feature.fromGeometry(LineString.fromLngLats(flightPathPoints))}
        );

        return flightPathGeoJson.toJson();
    }

    public List<OutFlightPathEntry> generateOutFlightPath(Order[] deliveredOrders) {
        List<OutFlightPathEntry> outFlightPathEntries = new ArrayList<>();
        for (Order order : deliveredOrders) {
            Restaurant restaurant = deliveryPlanner.getRestaurantForOrder(order);

            List<OutFlightPathEntry> outFlightPathToRestaurant = new ArrayList<>();
            List<FlightPathEntry> flightPathToRestaurant = flightpathCalculator.getFlightPath(restaurant);
            for (FlightPathEntry step : flightPathToRestaurant) {
                OutFlightPathEntry outFlightPathEntry = new OutFlightPathEntry(
                        order.orderNo(),
                        step.from().lng(),
                        step.from().lat(),
                        step.direction().getAngle(),
                        step.to().lng(),
                        step.to().lat(),
                        System.nanoTime()
                );
                outFlightPathToRestaurant.add(outFlightPathEntry);
            }

            outFlightPathEntries.addAll(outFlightPathToRestaurant);

            outFlightPathEntries.add(new OutFlightPathEntry(
                    order.orderNo(),
                    outFlightPathEntries.get(outFlightPathEntries.size() - 1).toLongitude(),
                    outFlightPathEntries.get(outFlightPathEntries.size() - 1).toLatitude(),
                    0, // TODO: report
                    outFlightPathEntries.get(outFlightPathEntries.size() - 1).toLongitude(),
                    outFlightPathEntries.get(outFlightPathEntries.size() - 1).toLatitude(),
                    System.nanoTime()
            )); // hover on pickup

            // add reverse of pathToRestaurant
            for (int i = outFlightPathToRestaurant.size() - 1; i >= 0; i--) {
                OutFlightPathEntry outFlightPathEntry = outFlightPathToRestaurant.get(i);
                OutFlightPathEntry backwardsEntry = new OutFlightPathEntry(
                        order.orderNo(),
                        outFlightPathEntry.toLongitude(),
                        outFlightPathEntry.toLatitude(),
                        flightPathToRestaurant.get(i).direction().getOpposite().getAngle(),
                        outFlightPathEntry.fromLongitude(),
                        outFlightPathEntry.fromLatitude(),
                        System.nanoTime()
                );
                outFlightPathEntries.add(backwardsEntry);
            }

            outFlightPathEntries.add(new OutFlightPathEntry(
                    order.orderNo(),
                    outFlightPathEntries.get(outFlightPathEntries.size() - 1).toLongitude(),
                    outFlightPathEntries.get(outFlightPathEntries.size() - 1).toLatitude(),
                    0,
                    outFlightPathEntries.get(outFlightPathEntries.size() - 1).toLongitude(),
                    outFlightPathEntries.get(outFlightPathEntries.size() - 1).toLatitude(),
                    System.nanoTime()
            )); // hover on drop-off
        }
        return outFlightPathEntries;
    }
}
