package uk.ac.ed.inf.OutPutGenerator;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;
import uk.ac.ed.inf.DeliveryPlanner.DeliveryPlanner;
import uk.ac.ed.inf.Models.Direction;
import uk.ac.ed.inf.Models.Step;
import uk.ac.ed.inf.Models.Input.Order;
import uk.ac.ed.inf.Models.Output.DeliveryEntry;
import uk.ac.ed.inf.Models.Output.FlightPathEntry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class DefaultOutPutGenerator implements OutPutGenerator {
    private final DeliveryPlanner deliveryPlanner;
    private final ObjectMapper objectMapper;

    public DefaultOutPutGenerator(DeliveryPlanner deliveryPlanner,
                           ObjectMapper objectMapper) {
        this.deliveryPlanner = deliveryPlanner;
        this.objectMapper = objectMapper;
    }

    public String generateFlightPathOutPut(Order[] deliveredOrders) throws JsonProcessingException {
        return generateOutFlightPath(deliveredOrders);
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
        List<Step> steps = Arrays.stream(deliveredOrders)
                .map(deliveryPlanner::getPathForOrder)
                .flatMap(List::stream)
                .toList();

        // generate the points for the geojson from the steps
        List<Point> flightPathPoints = steps.stream()
                .map(step -> Point.fromLngLat(step.to().lng(),
                                              step.to().lat()))
                .collect(Collectors.toList());
//
//        // add the origin to the start of the list
//        flightPathPoints.add(0, Point.fromLngLat(steps.get(0).from().lng(),
//                                                 steps.get(0).from().lat()));

        FeatureCollection flightPathGeoJson = FeatureCollection.fromFeatures(
                new Feature[]{Feature.fromGeometry(LineString.fromLngLats(flightPathPoints))}
        );

        return flightPathGeoJson.toJson();
    }

    public String generateOutFlightPath(Order[] deliveredOrders) throws JsonProcessingException {
        List<FlightPathEntry> outFlightPathEntries = new ArrayList<>();
        for (Order order : deliveredOrders) {
            List<Step> stepsForOrder = deliveryPlanner.getPathForOrder(order);
            for (Step step : stepsForOrder) {
                Direction dir = step.direction();
                FlightPathEntry flightPathEntry = new FlightPathEntry(
                        order.orderNo(),
                        step.from().lng(),
                        step.from().lat(),
                        dir != null ? dir.getAngle() : 0, // TODO: mention in report??
                        step.to().lng(),
                        step.to().lat(),
                        System.nanoTime()
                );
                outFlightPathEntries.add(flightPathEntry);
            }

//            outFlightPathEntries.addAll(flightPathForOrder);
//
//            outFlightPathEntries.add(new FlightPathEntry(
//                    order.orderNo(),
//                    outFlightPathEntries.get(outFlightPathEntries.size() - 1).toLongitude(),
//                    outFlightPathEntries.get(outFlightPathEntries.size() - 1).toLatitude(),
//                    0, // TODO: report
//                    outFlightPathEntries.get(outFlightPathEntries.size() - 1).toLongitude(),
//                    outFlightPathEntries.get(outFlightPathEntries.size() - 1).toLatitude(),
//                    System.nanoTime()
//            )); // hover on pickup
//
//            // add reverse of pathToRestaurant
//            for (int i = flightPathForOrder.size() - 1; i >= 0; i--) {
//                FlightPathEntry flightPathEntry = flightPathForOrder.get(i);
//                FlightPathEntry backwardsEntry = new FlightPathEntry(
//                        order.orderNo(),
//                        flightPathEntry.toLongitude(),
//                        flightPathEntry.toLatitude(),
//                        stepsForOrder.get(i).direction().getOpposite().getAngle(),
//                        flightPathEntry.fromLongitude(),
//                        flightPathEntry.fromLatitude(),
//                        System.nanoTime()
//                );
//                outFlightPathEntries.add(backwardsEntry);
//            }
//
//            outFlightPathEntries.add(new FlightPathEntry(
//                    order.orderNo(),
//                    outFlightPathEntries.get(outFlightPathEntries.size() - 1).toLongitude(),
//                    outFlightPathEntries.get(outFlightPathEntries.size() - 1).toLatitude(),
//                    0,
//                    outFlightPathEntries.get(outFlightPathEntries.size() - 1).toLongitude(),
//                    outFlightPathEntries.get(outFlightPathEntries.size() - 1).toLatitude(),
//                    System.nanoTime()
//            )); // hover on drop-off
        }
        System.out.println("outFlightPathEntries = " + outFlightPathEntries.size());
        return objectMapper.writeValueAsString(outFlightPathEntries);
    }
}
